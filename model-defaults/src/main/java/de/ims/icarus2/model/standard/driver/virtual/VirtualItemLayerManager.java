/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 */
package de.ims.icarus2.model.standard.driver.virtual;

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static de.ims.icarus2.util.Conditions.checkNotNull;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.driver.ChunkInfo;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.ItemLayerManager;
import de.ims.icarus2.model.api.members.item.ItemList;
import de.ims.icarus2.model.standard.driver.ChunkInfoBuilder;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
public class VirtualItemLayerManager implements ItemLayerManager {

	private final List<Layer> layers = new ArrayList<>();
	private final TIntObjectMap<RootContainer> rootContainers = new TIntObjectHashMap<>();


	@Override
	public Collection<Layer> getLayers() {
		return CollectionUtils.getListProxy(layers);
	}

	public RootContainer addLayer(ItemLayer layer, ItemList itemList) {
		return addLayer(layer, () -> itemList);
	}

	public RootContainer addLayer(ItemLayer layer) {
		return addLayer(layer, () -> new DefaultItemList());
	}

	public RootContainer addLayer(ItemLayer layer, Supplier<ItemList> supplier) {
		checkNotNull(supplier);

		final int uid = layer.getManifest().getUID();
		RootContainer container = rootContainers.get(uid);

		if(container!=null)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Layer already contained in manager: "+getName(layer));

		container = new RootContainer(layer, supplier);

		layers.add(layer);
		rootContainers.put(uid, container);

		return container;
	}

	public void addLayers(Context context) {
		checkNotNull(context);

		context.forEachLayer(layer -> {
			if(ModelUtils.isItemLayer(layer)) {
				addLayer((ItemLayer) layer);
			}
		});
	}

	public void removeLayer(ItemLayer layer) {
		if(rootContainers.remove(layer.getManifest().getUID()) == null)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Layer not contained in manager: "+getName(layer));
	}

	public RootContainer getRootContainer(ItemLayer layer) {
		checkNotNull(layer);

		RootContainer result = rootContainers.get(layer.getManifest().getUID());

		if(result==null)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Unknown layer: "+getName(layer));

		return result;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.ItemLayerManager#getItemCount(de.ims.icarus2.model.api.layer.ItemLayer)
	 */
	@Override
	public long getItemCount(ItemLayer layer) {
		return getRootContainer(layer).getItemCount();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.ItemLayerManager#getItem(de.ims.icarus2.model.api.layer.ItemLayer, long)
	 */
	@Override
	public Item getItem(ItemLayer layer, long index) {
		return getRootContainer(layer).getItemAt(index);
	}

	public long indexOfItem(ItemLayer layer, Item item) {
		return getRootContainer(layer).indexOfItem(item);
	}

	public void addItem(ItemLayer layer, long index, Item item) {
		getRootContainer(layer).addItem(index, item);
	}

	public void addItem(ItemLayer layer, Item item) {
		getRootContainer(layer).addItem(item);
	}

	public void removeItem(ItemLayer layer, Item item) {
		getRootContainer(layer).removeItem(item);
	}

	public void removeItem(ItemLayer layer, long index) {
		getRootContainer(layer).removeItem(index);
	}

	public void moveItem(ItemLayer layer, long index0, long index1) {
		getRootContainer(layer).moveItem(index0, index1);
	}

	public void addItems(ItemLayer layer, long index, DataSequence<? extends Item> items) {
		getRootContainer(layer).addItems(index, items);
	}

	public DataSequence<? extends Item> removeItems(ItemLayer layer, long index0, long index1) {
		return getRootContainer(layer).removeItems(index0, index1);
	}

	public void clear() {
		for(RootContainer rootContainer : rootContainers.valueCollection()) {
			rootContainer.clear();
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.ItemLayerManager#load(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.layer.ItemLayer, java.util.function.Consumer)
	 */
	@Override
	public long load(IndexSet[] indices, ItemLayer layer,
			Consumer<ChunkInfo> action) throws InterruptedException {
		checkNotNull(indices);
		checkNotNull(layer);

		long count = IndexUtils.count(indices);
		RootContainer rootContainer = getRootContainer(layer);
		ChunkInfoBuilder infoBuilder = ChunkInfoBuilder.newBuilder(Math.min(200, (int)count)); // Somehow arbitrary capacity number

		for(IndexSet set : indices) {
			for(int i=0; i<set.size(); i++) {
				long index = set.indexAt(i);
				Item item = rootContainer.getItemAt(index);

				// Just push all items into the chunk info and publish when buffer is full
				if(action!=null && infoBuilder.addValid(index, item)) {
					action.accept(infoBuilder.build());
					infoBuilder.reset();
				}

				if(Thread.interrupted())
					throw new InterruptedException();
			}
		}

		// Flush final content of builder
		if(action!=null && !infoBuilder.isEmpty()) {
			action.accept(infoBuilder.build());
		}

		return count;
	}

	/**
	 * This implementation does not cache data in the usual sense, so this method is empty.
	 *
	 * @see de.ims.icarus2.model.api.members.item.ItemLayerManager#release(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.layer.ItemLayer)
	 */
	@Override
	public void release(IndexSet[] indices, ItemLayer layer)
			throws InterruptedException {
		// no-op
	}

}
