/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.members.item.manager;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.ObjLongConsumer;

import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.driver.ChunkInfo;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.IcarusUtils;

/**
 * Specifies an abstract manager that handles the content of potentially
 * several {@link ItemLayer} instances.
 *
 * @author Markus Gärtner
 *
 */
public interface ItemLayerManager {

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
	Collection<Layer> getItemLayers();

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
	default long getItemCount(ItemLayer layer) {
		return IcarusUtils.UNSET_LONG;
	}

	/**
	 * Signals that the specified layer has any items contained in it at all.
	 * The motivation for having this method besides {@link #getItemCount(ItemLayer)}
	 * is that implementations of this interface can have various forms of back-end
	 * storage, and depending on the individual architectures it can be cheaper to
	 * simply check if there are <i>any</i> items in a layer.
	 * <p>
	 * The default implementation simply checks if the reported {@link #getItemCount(ItemLayer) size}
	 * of the layer is greater than {@code 0}.
	 *
	 * @param layer
	 * @return
	 */
	default boolean hasItems(ItemLayer layer) {
		return getItemCount(layer)>0;
	}

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
	 * @throws IcarusApiException
	 */
	long load(IndexSet[] indices, ItemLayer layer, Consumer<ChunkInfo> action) throws InterruptedException, IcarusApiException;

	/**
	 * Calls {@link #load(IndexSet[], ItemLayer, Consumer) load} without the optional {@code action} argument.
	 * This method only exists as an alternative for client code to load data chunks when it is not actually
	 * interested in directly handling the intermediate parts of the loading process.
	 * @throws IcarusApiException
	 *
	 * @see #load(IndexSet[], ItemLayer, Consumer)
	 */
	default long load(IndexSet[] indices, ItemLayer layer) throws InterruptedException, IcarusApiException {
		return load(indices, layer, null);
	}

	/**
	 * Called when data provided by this manager is no longer required.
	 * <p>
	 * When dynamically loading and releasing data, an implementation should keep track of
	 * the number of times a top level item in its managed layers (or one of its groups) is
	 * requested in load or release operations. Once that virtual 'reference' counter reaches
	 * {@code 0} as a result of a release request, the implementation is free to release the
	 * associated resources and effectively destroy or recycle the item instance.
	 * @throws IcarusApiException
	 */
	void release(IndexSet[] indices, ItemLayer layer) throws InterruptedException, IcarusApiException;

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
