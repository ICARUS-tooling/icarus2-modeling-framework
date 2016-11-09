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
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.Map;
import java.util.PrimitiveIterator.OfLong;
import java.util.function.LongConsumer;
import java.util.function.ObjLongConsumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.standard.driver.cache.TrackedMember;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
public class BufferedItemManager {

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
		layerBuffers.forEach((key, buffer) -> {buffer.clear();});
		layerBuffers.clear();
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class LayerBuffer {
		private final int estimatedSize;
		private final boolean useSmallIndices;

		private final boolean trackItemUse;

		private volatile Object entries;
		private volatile Object pendingEntries;


		protected LayerBuffer(int estimatedSize, boolean useSmallIndices, boolean trackItemUse) {
			this.estimatedSize = estimatedSize;
			this.useSmallIndices = useSmallIndices;
			this.trackItemUse = trackItemUse;
		}

		public int getEstimatedSize() {
			return estimatedSize;
		}

		public boolean isUseSmallIndices() {
			return useSmallIndices;
		}

		public boolean isTrackItemUse() {
			return trackItemUse;
		}

		@SuppressWarnings("rawtypes")
		public boolean isEmpty() {
			Object entries = this.entries;
			return entries==null || ((Map)entries).isEmpty();
		}

		@SuppressWarnings("rawtypes")
		public boolean hasPendingEntries() {
			Object pendingEntries = this.pendingEntries;
			return pendingEntries!=null && !((Map)pendingEntries).isEmpty();
		}

		protected Object createStorage(int size) {
			if(size<=0) {
				size = 100;
			}

			return useSmallIndices ?
					new Int2ObjectOpenHashMap<Item>(size)
					: new Long2ObjectOpenHashMap<Item>(size);
		}

		protected Object entries() {
			if(entries==null) {
				synchronized (this) {
					if(entries==null) {
						entries = createStorage(estimatedSize);
					}
				}
			}

			return entries;
		}

		protected Object pendingEntries() {
			if(pendingEntries==null) {
				synchronized (this) {
					if(pendingEntries==null) {
						int size = Math.min(estimatedSize, 100_000);
						pendingEntries = createStorage(size);
					}
				}
			}

			return pendingEntries;
		}

		public int remainingCapacity() {
			Object entries = entries();
			@SuppressWarnings("rawtypes")
			int currentSize = ((Map)entries).size();

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
		@SuppressWarnings("unchecked")
		public Item fetch(long index) {
			Object entries = entries();
			if(useSmallIndices) {
				return ((Int2ObjectMap<Item>)entries).get(IcarusUtils.ensureIntegerValueRange(index));
			} else {
				return ((Long2ObjectMap<Item>)entries).get(index);
			}
		}

		/**
		 * Deletes the mapping for the given {@code index}.
		 * <p>
		 * Does not modify the use counter of removed items!
		 *
		 * @param index
		 */
		@SuppressWarnings("unchecked")
		public Item remove(long index) {
			Object entries = entries();
			if(useSmallIndices) {
				return ((Int2ObjectMap<Item>)entries).remove(IcarusUtils.ensureIntegerValueRange(index));
			} else {
				return ((Long2ObjectMap<Item>)entries).remove(index);
			}
		}

		/**
		 * Adds a new {@link Item} and maps it to the given {@code index}.
		 *
		 * @param index
		 * @param item
		 */
		@SuppressWarnings("unchecked")
		public void add(long index, Item item) {
			Object entries = entries();
			if(useSmallIndices) {
				((Int2ObjectMap<Item>)entries).put(IcarusUtils.ensureIntegerValueRange(index), item);
			} else {
				((Long2ObjectMap<Item>)entries).put(index, item);
			}
		}

		/**
		 * Adds the given mapping of {@link Item} and {@code index} to the
		 * collection of pending items.
		 *
		 * @param index
		 * @param item
		 */
		@SuppressWarnings("unchecked")
		public void offer(long index, Item item) {
			Object pendingEntries = pendingEntries();
			if(useSmallIndices) {
				((Int2ObjectMap<Item>)pendingEntries).put(IcarusUtils.ensureIntegerValueRange(index), item);
			} else {
				((Long2ObjectMap<Item>)pendingEntries).put(index, item);
			}
		}

		/**
		 * Shifts all pending items into the actual storage.
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void commit() {
			Object pendingEntries = this.pendingEntries;

			if(pendingEntries==null || ((Map)pendingEntries).isEmpty()) {
				return;
			}

			if(useSmallIndices) {
				((Int2ObjectMap<Item>)pendingEntries).int2ObjectEntrySet().forEach(entry -> {
					add(entry.getIntKey(), entry.getValue());
				});
			} else {
				((Long2ObjectMap<Item>)pendingEntries).long2ObjectEntrySet().forEach(entry -> {
					add(entry.getLongKey(), entry.getValue());
				});
			}
		}

		public int discardPending() {
			Object pendingEntries = this.pendingEntries;

			if(pendingEntries==null) {
				return 0;
			}

			@SuppressWarnings("rawtypes")
			Map map = (Map) pendingEntries;

			int size = map.size();

			map.clear();

			return size;
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
		public void release(OfLong ids, ObjLongConsumer<Item> disposeItemAction) {
			while(ids.hasNext()) {
				long index = ids.nextLong();
				Item item = fetch(index);

				if(((TrackedMember)item).decrementUseCounter()<=0) {
					remove(index);

					disposeItemAction.accept(item, index);
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
					((TrackedMember)item).incrementUseCounter();
					presentItemAction.accept(item, index);
				}
			}
		}

		@SuppressWarnings("rawtypes")
		public void clear() {
			Object entries = this.entries;
			if(entries!=null) {
				((Map)entries).clear();
				this.entries = null;
			}

			Object pendingEntries = this.pendingEntries;
			if(pendingEntries!=null) {
				((Map)pendingEntries).clear();
				this.pendingEntries = null;
			}
		}
	}
}
