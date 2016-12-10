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
 */
package de.ims.icarus2.model.standard.driver;

import static de.ims.icarus2.util.Conditions.checkNotNull;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.lang.ref.WeakReference;
import java.util.PrimitiveIterator.OfLong;
import java.util.function.LongConsumer;
import java.util.function.ObjLongConsumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.standard.driver.cache.TrackedMember;
import de.ims.icarus2.model.util.ModelUtils;

/**
 * @author Markus Gärtner
 *
 */
public class BufferedItemManager {

	//TODO create builder that allows driver to inject recycle actions through LayerBuffer constructors
	private final Int2ObjectMap<LayerBuffer> layerBuffers;

	public LayerBuffer getBuffer(ItemLayer layer) {
		checkNotNull(layer);

		LayerBuffer result = layerBuffers.get(keyForLayer(layer));

		if(result==null)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Layer not managed by this buffer or buffer already closed: "+ModelUtils.getUniqueId(layer));

		return result;
	}

	private static int keyForLayer(ItemLayer layer) {
		return layer.getManifest().getUID();
	}

	public void close() {
		layerBuffers.forEach((key, buffer) -> buffer.clear());
		layerBuffers.clear();
	}

	/**
	 * Helper class for use within methods that load/produce chunks and then add
	 * them to a {@link LayerBuffer} instance.
	 * <p>
	 * Not thread-safe!
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class InputCache {

		private final WeakReference<LayerBuffer> buffer;

		private volatile Long2ObjectMap<Item> pendingEntries;

		public InputCache(LayerBuffer buffer) {
			this.buffer = new WeakReference<>(buffer);
		}

		public LayerBuffer getBuffer() {
			LayerBuffer buffer = this.buffer.get();
			if(buffer==null)
				throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Source buffer no longer available");
			return buffer;
		}

		protected Long2ObjectMap<Item> pendingEntries() {
			if(pendingEntries==null) {
				int size = Math.min(getBuffer().getEstimatedSize(), 100_000);
				pendingEntries = new Long2ObjectOpenHashMap<>(size);
			}

			return pendingEntries;
		}

		public void offer(long index, Item item) {
			pendingEntries().put(index, item);
		}

		public boolean hasPendingEntries() {
			Long2ObjectMap<Item> pendingEntries = this.pendingEntries;
			return pendingEntries!=null && !pendingEntries.isEmpty();
		}

		public void forEach(ObjLongConsumer<Item> action) {
			Long2ObjectMap<Item> pendingEntries = this.pendingEntries;
			if(pendingEntries!=null) {
				pendingEntries.long2ObjectEntrySet().forEach(entry -> {
					action.accept(entry.getValue(), entry.getLongKey());
				});
			}
		}

		public int discard() {
			Long2ObjectMap<Item> pendingEntries = this.pendingEntries;
			int result = 0;

			if(pendingEntries!=null && !pendingEntries.isEmpty()) {
				result = pendingEntries.size();
				pendingEntries.clear();
			}

			return result;
		}

		public int commit() {
			Long2ObjectMap<Item> pendingEntries = this.pendingEntries;
			int result = 0;

			if(pendingEntries!=null && !pendingEntries.isEmpty()) {
				result = pendingEntries.size();
				getBuffer().commit(this);
			}

			return result;
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class LayerBuffer {
		private final int estimatedSize;
		private final boolean trackItemUse;

		private volatile Long2ObjectMap<Item> entries;

		private final ObjLongConsumer<Item> disposeItemAction;


		protected LayerBuffer(int estimatedSize, boolean trackItemUse, ObjLongConsumer<Item> disposeItemAction) {
			this.estimatedSize = estimatedSize;
			this.trackItemUse = trackItemUse;
			this.disposeItemAction = disposeItemAction;
		}

		public int getEstimatedSize() {
			return estimatedSize;
		}

		public boolean isTrackItemUse() {
			return trackItemUse;
		}

		public boolean isEmpty() {
			Long2ObjectMap<Item> entries = this.entries;
			return entries==null || entries.isEmpty();
		}

		protected Long2ObjectMap<Item> entries() {
			if(entries==null) {
				synchronized (this) {
					if(entries==null) {
						entries = new Long2ObjectOpenHashMap<>(estimatedSize);
					}
				}
			}

			return entries;
		}

		public int remainingCapacity() {
			Long2ObjectMap<Item> entries = entries();
			int currentSize = entries.size();

			return Math.max(0, estimatedSize-currentSize);
		}

		/**
		 * Returns the {@link Item} currently mapped to the given {@code index}
		 * or {@code null} if no such mapping exists.
		 * <p>
		 * Does not modify the use counter of returned items!
		 *
		 * @param index
		 * @return
		 */
		public Item fetch(long index) {
			return entries().get(index);
		}

		/**
		 * Deletes the mapping for the given {@code index}.
		 * <p>
		 * Does not modify the use counter of removed items!
		 *
		 * @param index
		 */
		public Item remove(long index) {
			return entries().remove(index);
		}

		/**
		 * Adds a new {@link Item} and maps it to the given {@code index}.
		 * @param item
		 * @param index
		 */
		public void add(Item item, long index) {
			entries().put(index, item);
		}

		/**
		 * Adds a new {@link Item} and maps it using its own {@link Item#getIndex() index}.
		 * @param item
		 */
		public void add(Item item) {
			entries().put(item.getIndex(), item);
		}

		public InputCache newCache() {
			return new InputCache(this);
		}

		/**
		 * Shifts all pending items from the given {@code cache} into the actual storage.
		 * <p>
		 * Will only be called by package-private code, so we do not need to make additional
		 * checks on the provided cache.
		 */
		void commit(InputCache cache) {

			/*
			 *  Long2ObjectOpenHashMap internally checks if the source of a putAll() implements
			 *  the Long2ObjectMap interface and uses faster non-boxing iterators to add all
			 *  the new entries.
			 */
			entries.putAll(cache.pendingEntries);
		}

		/**
		 * Releases all items mapped to index values provided by the given iterator.
		 * All items whose use counter reaches {@code 0} will be passed to the {@code disposeItemAction}
		 * argument to be recycled or finally discarded.
		 * <p>
		 * Note that this method will cause a {@link ClassCastException} if used for
		 * most items outside of those in a primary layer's root container!
		 *
		 * @param ids
		 * @param action
		 */
		public void release(OfLong ids) {
			while(ids.hasNext()) {
				long index = ids.nextLong();
				Item item = fetch(index);

				if(trackItemUse && ((TrackedMember)item).decrementUseCounter()<=0) {
					remove(index);

					if(disposeItemAction!=null) {
						disposeItemAction.accept(item, index);
					}
				}
			}
		}

		/**
		 * Fetches and forwards all items mapped to index values provided by the given iterator.
		 * If a mapping is present for a given index value the respective item will have its
		 * use counter increased and both item and index are send to the {@code presentItemAction},
		 * otherwise the {@code missingItemAction} callback is used to signal that an item needs
		 * to be loaded from the physical backend storage.
		 * <p>
		 * Note that this method will cause a {@link ClassCastException} if used for
		 * most items outside of those in a primary layer's root container!
		 *
		 * @param ids
		 * @param presentItemAction
		 * @param missingItemAction
		 */
		public void load(OfLong ids, ObjLongConsumer<Item> presentItemAction, LongConsumer missingItemAction) {

			while(ids.hasNext()) {
				long index = ids.nextLong();
				Item item = fetch(index);

				if(item==null) {
					missingItemAction.accept(index);
				} else {
					if(trackItemUse) {
						((TrackedMember)item).incrementUseCounter();
					}
					presentItemAction.accept(item, index);
				}
			}
		}

		public void clear() {
			Long2ObjectMap<Item> entries = this.entries;
			if(entries!=null) {
				entries.clear();
				this.entries = null;
			}
		}
	}
}
