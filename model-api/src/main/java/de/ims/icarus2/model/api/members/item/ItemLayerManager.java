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
package de.ims.icarus2.model.api.members.item;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.ObjLongConsumer;

import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.driver.ChunkInfo;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;

/**
 * Specifies an abstract manager that handles the content of potentially
 * several {@link ItemLayer} instances.
 *
 * @author Markus Gärtner
 *
 */
public interface ItemLayerManager extends ModelConstants {

	/**
	 * Returns all {@link ItemLayer item layer} instances that are accessible
	 * through this manager.
	 * <p>
	 * The returned collection must not be {@code null} or empty!
	 * <p>
	 * Note: the return type has been chosen to be a collection of layers
	 * whereas it is required to only contain item layer instances. This is
	 * to not interfere too much with the more general methods defined in
	 * other members of the ICARUS corpus framework such as {@link Context}.
	 *
	 * @return
	 */
	Collection<Layer> getLayers();

	/**
	 * Attempts to fetch the number of elements stored in the top-level container for the given
	 * layer. The returned value is meant to be the total number of items in that layer,
	 * unaffected by horizontal filtering. Implementations should cache these counts for all
	 * layers they are meant to manage. A return value of {@code -1} indicates that the
	 * implementation has no information about the specified layer's item count.
	 *
	 * @param layer
	 * @return
	 * @throws ModelException
	 */
	long getItemCount(ItemLayer layer);

	/**
	 * Accesses the internal cache for the specified layer and attempts to lookup the
	 * item mapped to the given index value. If no item is stored for that index
	 * this method returns {@code null}.
	 *
	 * @param index
	 * @param layer
	 * @return
	 * @throws ModelException if the index is out of bounds or this implementation
	 * is not responsible for the specified layer
	 */
	Item getItem(ItemLayer layer, long index);

	/**
	 * Attempts to load the items specified by the {@code indices} argument belonging to the given
	 * {@code layer}. Chunks of loaded items will be wrapped into {@link ChunkInfo} instances and
	 * passed on to the optional {@code action}.
	 * <p>
	 * Note that aforementioned {@code ChunkInfo} instances are only valid for the duration of the
	 * {@code action}'s {@link Consumer#accept(Object)} invocation. Implementations are encouraged to
	 * share chunk info objects during the entire loading process and therefore client code should
	 * not store them or make other persistence based expectations.
	 *
	 * @param indices
	 * @param layer
	 * @param action
	 * @return
	 * @throws InterruptedException
	 */
	long load(IndexSet[] indices, ItemLayer layer, Consumer<ChunkInfo> action) throws InterruptedException;

	/**
	 * Calls {@link #load(IndexSet[], ItemLayer, Consumer) load} without the optional {@code action} argument.
	 * This method only exists as an alternative for client code to load data chunks when it is not actually
	 * interested in directly handling the intermediate parts of the loading process.
	 *
	 * @see #load(IndexSet[], ItemLayer, Consumer)
	 */
	default long load(IndexSet[] indices, ItemLayer layer) throws InterruptedException {
		return load(indices, layer, null);
	}

	/**
	 * Called when data provided by this implementation is no longer required.
	 * <p>
	 * When dynamically loading and releasing data, an implementation should keep track of
	 * the number of times a top level item in its managed layers (or one of its groups) is
	 * requested in load or release operations. Once that virtual 'reference' counter reaches
	 * {@code 0} as a result of a release request, the implementation is free to release the
	 * associated resources and effectively destroy or recycle the item instance.
	 */
	void release(IndexSet[] indices, ItemLayer layer) throws InterruptedException;

	/**
	 * Looks up the item stored for the given combination of {@code layer} and {@code index}
	 * and applies the specified {@code action}. Returns {@code true} if an item was found
	 * and was not {@code null}.
	 *
	 * @param layer
	 * @param index
	 * @param action
	 * @return {@code true} iff the requested item was not {@code null}
	 */
	default boolean forItem(ItemLayer layer, long index, Consumer<Item> action) {
		Item item = getItem(layer, index);
		action.accept(item);
		return item!=null;
	}

	/**
	 * Batch version of {@link #forItem(ItemLayer, long, Consumer)} that takes a collection
	 * of index values and applies the given {@code action} to all the items mapped to them.
	 * Returns the number of items that were found to be non-null.
	 * The {@code action} is to be applied to non-null items in the order their respective
	 * index value appears in the given {@link IndexSet}!
	 *
	 * @param layer
	 * @param indices
	 * @param action
	 * @return
	 * @throws InterruptedException
	 */
	default long forItems(ItemLayer layer, IndexSet[] indices, Consumer<Item> action) throws InterruptedException {
		long count = 0L;

		for(IndexSet set : indices) {
			for(int i=0; i<set.size(); i++) {
				Item item = getItem(layer, set.indexAt(i));

				action.accept(item);

				if(item!=null) {
					count++;
				}

				if(Thread.interrupted())
					throw new InterruptedException();
			}
		}

		return count;
	}

	/**
	 * Similar to {@link #forItems(ItemLayer, IndexSet[], Consumer)} but takes a special
	 * {@code action} argument that expects the associated {@code index} value for each item.
	 *
	 * @param layer
	 * @param indices
	 * @param action
	 * @return
	 * @throws InterruptedException
	 */
	default long forItems(ItemLayer layer, IndexSet[] indices, ObjLongConsumer<Item> action) throws InterruptedException {
		long count = 0L;

		for(IndexSet set : indices) {
			for(int i=0; i<set.size(); i++) {
				long index = set.indexAt(i);
				Item item = getItem(layer, index);

				action.accept(item, index);

				if(item!=null) {
					count++;
				}

				if(Thread.interrupted())
					throw new InterruptedException();
			}
		}

		return count;
	}
}
