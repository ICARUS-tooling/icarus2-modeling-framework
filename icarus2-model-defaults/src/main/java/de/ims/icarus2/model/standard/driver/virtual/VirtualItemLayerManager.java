/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ims.icarus2.model.standard.driver.virtual;

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ObjLongConsumer;
import java.util.function.Supplier;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.driver.ChunkInfo;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.manager.ItemLayerManager;
import de.ims.icarus2.model.api.members.item.manager.ItemList;
import de.ims.icarus2.model.standard.driver.ChunkInfoBuilder;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.collections.seq.DataSequence;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(ItemLayerManager.class)
public class VirtualItemLayerManager implements ItemLayerManager {

	private final List<ItemLayer> layers = new ArrayList<>();
	private final Int2ObjectMap<RootContainer> rootContainers = new Int2ObjectOpenHashMap<>();

	private final ObjLongConsumer<ItemLayer> missingItemHandler;

	public static final ObjLongConsumer<ItemLayer> IGNORE_MISSING_ITEMS =
			(layer, index) -> { /* no-op */ };

	public static final ObjLongConsumer<ItemLayer> REPORT_MISSING_ITEMS =
			(layer, index) -> { throw new ModelException(
					ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					"No item available for given index: "+index); };

	public VirtualItemLayerManager() {
		this(REPORT_MISSING_ITEMS);
	}

	public VirtualItemLayerManager(ObjLongConsumer<ItemLayer> missingItemHandler) {
		this.missingItemHandler = requireNonNull(missingItemHandler);
	}

	@Override
	public List<ItemLayer> getItemLayers() {
		return CollectionUtils.getListProxy(layers);
	}

	public RootContainer addLayer(ItemLayer layer, ItemList itemList) {
		return addLayer(layer, () -> itemList);
	}

	public RootContainer addLayer(ItemLayer layer) {
		return addLayer(layer, () -> new DefaultItemList());
	}

	public RootContainer addLayer(ItemLayer layer, Supplier<ItemList> supplier) {
		requireNonNull(supplier);

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

	private static int key(ItemLayer layer) {
		return layer.getManifest().getUID();
	}

	public void addLayers(Context context) {
		requireNonNull(context);

		context.forEachLayer(layer -> {
			if(ModelUtils.isItemLayer(layer)) {
				addLayer((ItemLayer) layer);
			}
		});
	}

	public void removeLayer(ItemLayer layer) {
		if(rootContainers.remove(key(layer)) == null)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Layer not contained in manager: "+getName(layer));
	}

	public RootContainer getRootContainer(ItemLayer layer) {
		requireNonNull(layer);

		RootContainer result = rootContainers.get(key(layer));

		if(result==null)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Unknown layer: "+getName(layer));

		return result;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemLayerManager#getItemCount(de.ims.icarus2.model.api.layer.ItemLayer)
	 */
	@Override
	public long getItemCount(ItemLayer layer) {
		return getRootContainer(layer).getItemCount();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemLayerManager#hasItems(de.ims.icarus2.model.api.layer.ItemLayer)
	 */
	@Override
	public boolean hasItems(ItemLayer layer) {
		return !getRootContainer(layer).isEmpty();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemLayerManager#getItem(de.ims.icarus2.model.api.layer.ItemLayer, long)
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
		getRootContainer(layer).swapItems(index0, index1);
	}

	public void addItems(ItemLayer layer, long index, DataSequence<? extends Item> items) {
		getRootContainer(layer).addItems(index, items);
	}

	public DataSequence<? extends Item> removeItems(ItemLayer layer, long index0, long index1) {
		return getRootContainer(layer).removeItems(index0, index1);
	}

	/**
	 *  Releases <b>all</b> internal data in this manager. This includes all stored items,
	 *  {@link RootContainer root containers} and {@link #getItemLayers() layers}!
	 */
	public void clear() {
		for(RootContainer rootContainer : rootContainers.values()) {
			rootContainer.clear();
		}

		rootContainers.clear();
		layers.clear();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemLayerManager#load(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.layer.ItemLayer, java.util.function.Consumer)
	 */
	@Override
	public long load(IndexSet[] indices, ItemLayer layer,
			Consumer<ChunkInfo> action) throws InterruptedException {
		requireNonNull(indices);
		requireNonNull(layer);

		long count = 0;
		RootContainer rootContainer = getRootContainer(layer);
		ChunkInfoBuilder infoBuilder = ChunkInfoBuilder.builder(Math.min(200, (int)count)); // Somehow arbitrary capacity number

		final long size = rootContainer.getItemCount();
		for(IndexSet set : indices) {
			for(int i=0; i<set.size(); i++) {
				long index = set.indexAt(i);
				// Make sure we adhere to what client code desired as policy for missing items
				if(index==UNSET_LONG || index>=size) {
					missingItemHandler.accept(layer, index);
					// In any case do we skip the requested index
					continue;
				}
				Item item = rootContainer.getItemAt(index);
				count++;

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
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemLayerManager#release(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.layer.ItemLayer)
	 */
	@Override
	public void release(IndexSet[] indices, ItemLayer layer)
			throws InterruptedException {
		// no-op
	}


}
