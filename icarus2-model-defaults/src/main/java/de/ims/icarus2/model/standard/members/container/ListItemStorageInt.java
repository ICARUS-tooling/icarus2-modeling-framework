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
package de.ims.icarus2.model.standard.members.container;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.LookupList;
import de.ims.icarus2.util.collections.seq.DataSequence;
import de.ims.icarus2.util.collections.seq.DataSequenceCollectionWrapper;
import de.ims.icarus2.util.collections.seq.ListSequence;

/**
 * Implements a {@link ItemStorage} that is backed by a {@link LookupList}.
 * Therefore the storage is limited to slightly less than {@value Integer#MAX_VALUE}
 * elements. It is guaranteed that all methods that return an index value declared to
 * be in the positive value space of {@value Long#MAX_VALUE}} do instead return values limited
 * to the smaller integer space. Similarly methods that take long index values as arguments
 * will fail with a {@link ModelException} of type {@link ModelErrorCode#INDEX_OVERFLOW} when
 * provided with values that exceed the integer space.
 *
 * @author Markus Gärtner
 *
 */
public class ListItemStorageInt implements ItemStorage {

	public static final int DEFAULT_CAPACITY = 10;

	/**
	 * Local storage for items
	 */
	protected final LookupList<Item> items;

	/**
	 *  The cached items found to represent the smallest and highest offset values
	 *
	 *  @see #doStoreItemsForOffset
	 */
	protected Item beginItem, endItem;

	/**
	 * Flag to indicate whether or not we should store the items
	 * for smallest and largest offset in this container.
	 */
	protected boolean doStoreItemsForOffset = false;

	public ListItemStorageInt() {
		this(-1);
	}

	public ListItemStorageInt(int initialCapacity) {
		items = createItemsBuffer(initialCapacity);
	}

	/**
	 * Creates a new {@link LookupList} instance to store future members
	 * of this container. The list should have an initial capacity no
	 * smaller than the specified {@code capacity} argument. If the
	 * {@code capacity} parameter is negative, the method is to choose
	 * an implementation specific value for it.
	 * <p>
	 * The default implementation will use {@value #DEFAULT_CAPACITY} as
	 * fallback in this case.
	 *
	 * @param capacity
	 * @return
	 */
	protected LookupList<Item> createItemsBuffer(int capacity) {
		if(capacity<0) {
			capacity = DEFAULT_CAPACITY;
		}

		return new LookupList<>(capacity);
	}

	/**
	 * Resets the cached {@code begin} and {@code end} items and determines whether or not to
	 * store those items for the new {@code context}. This decision is solely based on the
	 * presence of a declared <i>foundation layer</i> for the layer hosting the given container.
	 * If a foundation layer is defined then all the elements stored will use boundary offsets
	 * from within the same space and therefore caching of the aforementioned items will be activated.
	 * <p>
	 * Note that subclasses that wish to override this method should always perform a call to the super
	 * implementation to make sure that handling of boundary items is initialized properly.
	 *
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#addNotify(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public void addNotify(Container context) {
		doStoreItemsForOffset = context.getManifest().getLayerManifest()
				.flatMap(ItemLayerManifestBase::getFoundationLayerManifest)
				.isPresent();

		beginItem = endItem = null;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#removeNotify(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public void removeNotify(Container context) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return ContainerType.LIST;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getItemCount(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public long getItemCount(Container context) {
		return items.size();
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getItemAt(de.ims.icarus2.model.api.members.container.Container, long)
	 */
	@Override
	public Item getItemAt(Container context, long index) {
		return items.get(IcarusUtils.ensureIntegerValueRange(index));
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#indexOfItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long indexOfItem(Container context, Item item) {
		requireNonNull(item);
		return items.indexOf(item);
	}

	/**
	 * Iterates over all the elements in this storage and searches for the
	 * items with the smallest and highest offsets, respectively. Note that in
	 * case the {@link #doStoreItemsForOffset} flag is set to {@code false} this
	 * method simply returns and does nothing!
	 */
	protected void refreshOffsetItems() {
		if(!doStoreItemsForOffset) {
			return;
		}

		items.forEach(this::tryRefrechOffsetItems);
	}

	/**
	 * Checks whether or not the given {@code item} represents a new boundary marker
	 * (i.e. it is located outside the current boundaries) and in that case refreshes
	 * the appropriate boundary marker(s).
	 * <p>
	 * Does nothing if storing of boundary markers is not active.
	 */
	protected void tryRefrechOffsetItems(Item item) {
		if(!doStoreItemsForOffset) {
			return;
		}

		if(beginItem==null || item.getBeginOffset()<beginItem.getBeginOffset()) {
			beginItem = item;
		}

		if(endItem==null || item.getEndOffset()>endItem.getEndOffset()) {
			endItem = item;
		}
	}

	/**
	 * Checks whether the given {@code item} is one of the 2 boundary markers and if
	 * so clears those markers entirely (forcing a re-computation the next time a
	 * boundary offset is requested, which might be costly!).
	 * <p>
	 * Does nothing if storing of boundary markers is not active.
	 */
	protected void maybeClearOffsetItems(Item item) {
		if(!doStoreItemsForOffset) {
			return;
		}

		if(item==beginItem || item==endItem) {
			clearOffsetItems();
		}
	}

	protected void clearOffsetItems() {
		beginItem = endItem = null;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#addItem(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public void addItem(Container context, long index, Item item) {
		requireNonNull(item);
		items.add(IcarusUtils.ensureIntegerValueRange(index), item);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#addItems(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.util.collections.seq.DataSequence)
	 */
	@Override
	public void addItems(Container context, long index,
			DataSequence<? extends Item> items) {

		requireNonNull(items);
		this.items.addAll(IcarusUtils.ensureIntegerValueRange(index),
				new DataSequenceCollectionWrapper<>(items));

		//TODO maybe try to refresh during each loop cycle instead?
		clearOffsetItems();
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#removeItem(de.ims.icarus2.model.api.members.container.Container, long)
	 */
	@Override
	public Item removeItem(Container context, long index) {
		Item item = items.remove(IcarusUtils.ensureIntegerValueRange(index));

		maybeClearOffsetItems(item);

		return item;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#removeItems(de.ims.icarus2.model.api.members.container.Container, long, long)
	 */
	@Override
	public DataSequence<? extends Item> removeItems(Container context,
			long index0, long index1) {
		int idx0 = IcarusUtils.ensureIntegerValueRange(index0);
		int idx1 = IcarusUtils.ensureIntegerValueRange(index1);

		final List<Item> buffer = new ArrayList<>(Math.max(10, idx1-idx0+1));

		items.removeAll(idx0, idx1, buffer::add);

		clearOffsetItems();

		return new ListSequence<>(buffer);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#moveItem(de.ims.icarus2.model.api.members.container.Container, long, long)
	 */
	@Override
	public void moveItem(Container context, long index0, long index1) {
		int idx0 = IcarusUtils.ensureIntegerValueRange(index0);
		int idx1 = IcarusUtils.ensureIntegerValueRange(index1);

		items.move(idx0, idx1);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getBeginOffset(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public long getBeginOffset(Container context) {
		if(beginItem==null && doStoreItemsForOffset) {
			refreshOffsetItems();
		}

		return beginItem==null ? IcarusUtils.UNSET_LONG : beginItem.getBeginOffset();
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getEndOffset(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public long getEndOffset(Container context) {
		if(endItem==null && doStoreItemsForOffset) {
			refreshOffsetItems();
		}

		return endItem==null ? IcarusUtils.UNSET_LONG : endItem.getEndOffset();
	}

	/**
	 * Clears the cached items for begin and end index and empties
	 * the items storage.
	 */
	public void clear() {
		items.clear();

		clearOffsetItems();
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		clear();

		doStoreItemsForOffset = false;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return true;
	}

	// Utility methods for direct driver access to boundary markers

	public Item getBeginItem() {
		return beginItem;
	}

	public Item getEndItem() {
		return endItem;
	}

	public void setBeginItem(Item beginItem) {
		this.beginItem = beginItem;
	}

	public void setEndItem(Item endItem) {
		this.endItem = endItem;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#createEditVerifier(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public ContainerEditVerifier createEditVerifier(Container context) {
		return new DefaultContainerEditVerifier(context);
	}

	@Override
	public boolean isDirty(Container context) {
		return false;
	}
}
