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
package de.ims.icarus2.model.standard.driver;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.PrimitiveIterator.OfLong;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.ObjLongConsumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.apiguard.Api;
import de.ims.icarus2.apiguard.Api.ApiType;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.Item.ManagedItem;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.model.standard.driver.cache.TrackedMember;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.AbstractBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class BufferedItemManager {

	public static Builder builder() {
		return new Builder();
	}

	//TODO create builder that allows driver to inject recycle actions through LayerBuffer constructors
	private final Int2ObjectMap<LayerBuffer> layerBuffers;

	protected BufferedItemManager(Builder builder) {
		Objects.requireNonNull(builder);

		this.layerBuffers = builder.getLayerBuffers();
	}

	public LayerBuffer getBuffer(ItemLayer layer) {
		requireNonNull(layer);

		LayerBuffer result = layerBuffers.get(keyForLayer(layer));

		if(result==null)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Layer not managed by this buffer or buffer already closed: "+ModelUtils.getUniqueId(layer));

		return result;
	}

	private static int keyForLayer(ItemLayer layer) {
		return layer.getManifest().getUID();
	}

	private static int keyForLayer(ItemLayerManifestBase<?> layerManifest) {
		return layerManifest.getUID();
	}

	public void close() {
		layerBuffers.values().forEach(LayerBuffer::clear);
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
	public interface InputCache {

		/** Add a given item and index to the cache */
		void offer(Item item, long index);

		/** Returns {@code true} iff this cache is not empty */
		boolean hasPendingEntries();

		/**
		 * Applies the given action to all item-index pairs in this cache.
		 * Note that the order in which the cached entries will be presented
		 * to the action is <b>not</b> guaranteed.
		 */
		void forEach(ObjLongConsumer<Item> action);

		/**
		 * Provides an alternative way of traversing the elements in this cache.
		 * @see #forEach(ObjLongConsumer)
		 */
		Iterator<Item> pendingItemIterator();

		/** Remove all entries in this cache */
		int discard();

		/** Persist the content of this cache in the back-end storage and clear the cache */
		int commit();

	}

	public static final InputCache EMPTY_CACHE = new EmptyCache();

	public static class EmptyCache implements InputCache {

		/**
		 * @see de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache#offer(de.ims.icarus2.model.api.members.item.Item, long)
		 */
		@Override
		public void offer(Item item, long index) {
			// no-op
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache#hasPendingEntries()
		 */
		@Override
		public boolean hasPendingEntries() {
			return false;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache#forEach(java.util.function.ObjLongConsumer)
		 */
		@Override
		public void forEach(ObjLongConsumer<Item> action) {
			// no-op
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache#pendingItemIterator()
		 */
		@Override
		public Iterator<Item> pendingItemIterator() {
			return null;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache#discard()
		 */
		@Override
		public int discard() {
			return 0;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache#commit()
		 */
		@Override
		public int commit() {
			return 0;
		}
	}

	/**
	 * Default implementation of a cache that uses a {@link Long2ObjectMap} as buffer
	 * for pending entries.
	 * <p>
	 * Not thread-safe!
	 *
	 * @author Markus Gärtner
	 *
	 */
	static class InputCacheImpl implements InputCache {

		private final WeakReference<LayerBuffer> buffer;
		private final Consumer<? super InputCache> cleanupAction;

		private volatile Long2ObjectMap<Item> pendingEntries;

		public InputCacheImpl(LayerBuffer buffer, Consumer<? super InputCache> cleanupAction) {
			this.buffer = new WeakReference<>(buffer);
			this.cleanupAction = cleanupAction;
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

		/**
		 * @see de.ims.icarus2.model.standard.driver.InputCache#offer(de.ims.icarus2.model.api.members.item.Item, long)
		 */
		@Override
		public void offer(Item item, long index) {
			pendingEntries().put(index, item);
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.InputCache#hasPendingEntries()
		 */
		@Override
		public boolean hasPendingEntries() {
			Long2ObjectMap<Item> pendingEntries = this.pendingEntries;
			return pendingEntries!=null && !pendingEntries.isEmpty();
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.InputCache#forEach(java.util.function.ObjLongConsumer)
		 */
		@Override
		public void forEach(ObjLongConsumer<Item> action) {
			Long2ObjectMap<Item> pendingEntries = this.pendingEntries;
			if(pendingEntries!=null) {
				pendingEntries.long2ObjectEntrySet().forEach(entry -> {
					action.accept(entry.getValue(), entry.getLongKey());
				});
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache#pendingItemIterator()
		 */
		@Override
		public Iterator<Item> pendingItemIterator() {
			Long2ObjectMap<Item> pendingEntries = this.pendingEntries;
			Collection<Item> c = Collections.emptyList();
			if(pendingEntries!=null) {
				c = pendingEntries.values();
			}
			return c.iterator();
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.InputCache#discard()
		 */
		@Override
		public int discard() {
			Long2ObjectMap<Item> pendingEntries = this.pendingEntries;
			int result = 0;

			if(pendingEntries!=null && !pendingEntries.isEmpty()) {
				result = pendingEntries.size();

				try {
					if(cleanupAction!=null) {
						cleanupAction.accept(this);
					}
				} finally {
					// Make sure we really clear our buffer
					pendingEntries.clear();
				}
			}

			return result;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.InputCache#commit()
		 */
		@Override
		public int commit() {
			Long2ObjectMap<Item> pendingEntries = this.pendingEntries;
			int result = 0;

			if(pendingEntries!=null && !pendingEntries.isEmpty()) {
				result = pendingEntries.size();
				getBuffer().commit(this);
				// Soft reset by discarding the current buffer content
				pendingEntries.clear();
			}

			return result;
		}
	}

	/**
	 * An {@link ObjLongConsumer} implementation that is usable as <i>dispose action</i>
	 * for a {@link LayerBuffer}. This implementation will do nothing except set the
	 * disposed item's {@link Item#isAlive() alive} flag to {@code false}. This was
	 *
	 */
	public static final ObjLongConsumer<Item> DEFAULT_DISPOSE_ACTION = (item, index) -> {
		if(item instanceof ManagedItem) {
			ManagedItem managedItem = (ManagedItem) item;
			managedItem.setAlive(false);
		}
	};

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class LayerBuffer {

		/**
		 * Hint for the lazy creation of the internal item storage as to
		 * what the initial size of the map should be. A value that is close
		 * to the largest buffer size needed during runtime will greatly
		 * reduce overhead in map expansion.
		 */
		private final int estimatedSize;

		/**
		 * Flag to indicate that for every item that's part in a call to
		 * {@link #load(OfLong, ObjLongConsumer, LongConsumer)} or
		 * {@link #release(OfLong)} the item's use counter will be
		 * incremented or decremented respectively.
		 */
		private final boolean trackItemUse;

		/**
		 * Actual storage of mappings from indices to their respective
		 * items. Will be created lazily once actually needed.
		 */
		private volatile Long2ObjectMap<Item> entries;

		/**
		 * Callback used when the use counter of an item reaches {@code 0}.
		 * This allows drivers to inject recycling facilities for expensive
		 * item implementations.
		 */
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

		/**
		 * Creates a new {@link InputCache} that when ordered to {@link InputCache#commit()}
		 * will add its content to this buffer and use the provided {@code action} for
		 * cleaning up connected data when a {@link InputCache#discard() discard} is called.
		 *
		 * @return
		 */
		public InputCache newCache(Consumer<? super InputCache> cleanupAction) {
			return new InputCacheImpl(this, cleanupAction);
		}

		/**
		 * Shifts all pending items from the given {@code cache} into the actual storage.
		 * <p>
		 * Will only be called by package-private code, so we do not need to make additional
		 * checks on the provided cache.
		 */
		void commit(InputCache cache) {

			InputCacheImpl cacheImpl = (InputCacheImpl) cache;

			/*
			 *  Long2ObjectOpenHashMap internally checks if the source of a putAll() implements
			 *  the Long2ObjectMap interface and uses faster non-boxing iterators to add all
			 *  the new entries.
			 */
			entries().putAll(cacheImpl.pendingEntries);
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

	@Api(type=ApiType.BUILDER)
	public static class Builder extends AbstractBuilder<Builder, BufferedItemManager> {

		private final Int2ObjectMap<LayerBuffer> layerBuffers = new Int2ObjectOpenHashMap<>();

		protected Builder() {
			// no-op
		}

		/**
		 * Adds a new {@link LayerBuffer} for the specified {@link ItemLayerManifestBase<?> layer}
		 * that has no {@link ObjLongConsumer action} assigned to dispose items with and has
		 * an unspecified default starting size for the internal map.
		 *
		 * @param itemLayerManifest
		 * @return
		 */
		public Builder addBuffer(ItemLayerManifestBase<?> itemLayerManifest) {
			requireNonNull(itemLayerManifest);

			addBuffer0(itemLayerManifest, -1, null);

			return thisAsCast();
		}

		/**
		 * Adds a new {@link LayerBuffer} for the specified {@link ItemLayerManifestBase<?> layer}
		 * that has no {@link ObjLongConsumer action} assigned to dispose items with and uses
		 * the given capacity as starting size for the internal map.
		 *
		 * @param itemLayerManifest
		 * @param capacity
		 * @return
		 */
		public Builder addBuffer(ItemLayerManifestBase<?> itemLayerManifest, int capacity) {
			requireNonNull(itemLayerManifest);
			checkArgument(capacity>0);

			addBuffer0(itemLayerManifest, capacity, null);

			return thisAsCast();
		}

		/**
		 * Adds a new {@link LayerBuffer} for the specified {@link ItemLayerManifestBase<?> layer}
		 * with the given {@code disposeItemAction} to dispose items with and that has
		 * an unspecified default starting size for the internal map.
		 *
		 * @param itemLayerManifest
		 * @param disposeItemAction
		 * @return
		 */
		public Builder addBuffer(ItemLayerManifestBase<?> itemLayerManifest, ObjLongConsumer<Item> disposeItemAction) {
			requireNonNull(itemLayerManifest);
			requireNonNull(disposeItemAction);

			addBuffer0(itemLayerManifest, -1, disposeItemAction);

			return thisAsCast();
		}

		/**
		 * Adds a new {@link LayerBuffer} for the specified {@link ItemLayerManifestBase<?> layer}
		 * with the given {@code disposeItemAction} to dispose items with and that uses
		 * the given capacity as starting size for the internal map.
		 *
		 * @param itemLayerManifest
		 * @param capacity
		 * @param disposeItemAction
		 * @return
		 */
		public Builder addBuffer(ItemLayerManifestBase<?> itemLayerManifest, int capacity, ObjLongConsumer<Item> disposeItemAction) {
			requireNonNull(itemLayerManifest);
			requireNonNull(disposeItemAction);
			checkArgument(capacity>0);

			addBuffer0(itemLayerManifest, capacity, disposeItemAction);

			return thisAsCast();
		}

		private void addBuffer0(ItemLayerManifestBase<?> itemLayerManifest, int capacity, ObjLongConsumer<Item> disposeItemAction) {
			int key = keyForLayer(itemLayerManifest);

			if(layerBuffers.containsKey(key))
				throw new ModelException(GlobalErrorCode.INVALID_INPUT,
						"Layer buffer already defiend for layer: "+ManifestUtils.getName(itemLayerManifest));

			if(capacity<0) {
				capacity = 100_000;
			}

			// Track item use only if the manifest represents a primary layer
			boolean trackItemUse = itemLayerManifest.isPrimaryLayerManifest();

			LayerBuffer buffer = new LayerBuffer(capacity, trackItemUse, disposeItemAction);

			layerBuffers.put(key, buffer);
		}

		public Int2ObjectMap<LayerBuffer> getLayerBuffers() {
			return layerBuffers;
		}

		public LayerBuffer getLayerBuffer(ItemLayerManifestBase<?> layerManifest) {
			return layerBuffers.get(keyForLayer(layerManifest));
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#validate()
		 */
		@Override
		protected void validate() {
			checkState("No layer buffers defined", !layerBuffers.isEmpty());
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#create()
		 */
		@Override
		protected BufferedItemManager create() {
			return new BufferedItemManager(this);
		}

	}
}
