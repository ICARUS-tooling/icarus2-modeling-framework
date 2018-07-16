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
package de.ims.icarus2.model.standard.members.item;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import de.ims.icarus2.model.api.driver.ChunkInfo;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.manager.ItemLayerManager;
import de.ims.icarus2.util.collections.LazyCollection;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class CompoundItemLayerManager implements ItemLayerManager {

	//FIXME

	private final Map<ItemLayer, ItemLayerManager> managerLookup;

	public CompoundItemLayerManager() {
		managerLookup = new Reference2ObjectOpenHashMap<>();
	}

	protected ItemLayerManager getManager(ItemLayer layer) {
		return managerLookup.get(layer);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemLayerManager#getItemLayers()
	 */
	@Override
	public Collection<Layer> getItemLayers() {
		LazyCollection<Layer> result = LazyCollection.lazyList();

		managerLookup.keySet().forEach(result);

		return result.getAsList();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemLayerManager#getItemCount(de.ims.icarus2.model.api.layer.ItemLayer)
	 */
	@Override
	public long getItemCount(ItemLayer layer) {
		return getManager(layer).getItemCount(layer);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemLayerManager#getItem(de.ims.icarus2.model.api.layer.ItemLayer, long)
	 */
	@Override
	public Item getItem(ItemLayer layer, long index) {
		return getManager(layer).getItem(layer, index);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemLayerManager#load(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.layer.ItemLayer, java.util.function.Consumer)
	 */
	@Override
	public long load(IndexSet[] indices, ItemLayer layer,
			Consumer<ChunkInfo> action) throws InterruptedException {
		return getManager(layer).load(indices, layer, action);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemLayerManager#release(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.layer.ItemLayer)
	 */
	@Override
	public void release(IndexSet[] indices, ItemLayer layer)
			throws InterruptedException {
		getManager(layer).release(indices, layer);
	}

}
