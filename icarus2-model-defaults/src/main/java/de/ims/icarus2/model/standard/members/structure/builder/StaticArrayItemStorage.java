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
package de.ims.icarus2.model.standard.members.structure.builder;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.standard.members.container.AbstractImmutableItemStorage;
import de.ims.icarus2.model.standard.members.container.ItemStorage;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.annotations.TestableImplementation;

/**
 * Array based sorted storage for small containers using binary search for the
 * {@link #indexOfItem(Container, Item) index lookup} method.
 * <p>
 * <b>Important implementation note:</b><br>
 * This implementation is only usable for cases where the desired order
 * of the items stored is directly reflected in the begin and end offsets
 * covered by those items. The items provided to instances of this class
 * will be sorted according to this criterion at construction time.
 *
 * @author Markus Gärtner
 *
 */
@TestableImplementation(ItemStorage.class)
public class StaticArrayItemStorage extends AbstractImmutableItemStorage {

	public static final int MAX_SIZE = 1<<10;

	private static final Comparator<Item> sorter = new Comparator<Item>() {

		@Override
		public int compare(Item m1, Item m2) {
			long result = m1.getBeginOffset()-m2.getBeginOffset();

			if(result==0L) {
				result = m1.getEndOffset()-m2.getEndOffset();
			}

			if(result>Integer.MAX_VALUE || result<Integer.MIN_VALUE) {
				result >>= 32;
			}

			return (int) result;
		}
	};

	private final Item[] items;

	/**
	 * Constructs a new {@link StaticArrayItemStorage} that directly uses the
	 * supplied array of items. This constructor should only be used by driver
	 * implementations.
	 * <p>
	 * Note that the given array will be sorted!
	 *
	 * @param items
	 */
	public StaticArrayItemStorage(Item[] items) {
		requireNonNull(items);
		if(items.length>MAX_SIZE)
			throw new IllegalArgumentException("Buffer size not supported: "+items.length);
		this.items = items;

		Arrays.sort(this.items, sorter);
	}

	/**
	 * Constructs a new {@code StaticArrayItemStorage} containing the given collection
	 * of items.
	 * <p>
	 * Note that the internal array buffer will be sorted!
	 * @param items
	 */
	public StaticArrayItemStorage(Collection<? extends Item> items) {
		if(items.size()>MAX_SIZE)
			throw new IllegalArgumentException("Buffer size not supported: "+items.size());

		this.items = new Item[items.size()];
		items.toArray(this.items);

		Arrays.sort(this.items, sorter);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#addNotify(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public void addNotify(@Nullable Container context) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#removeNotify(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public void removeNotify(@Nullable Container context) {
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
	public long getItemCount(@Nullable Container context) {
		return items.length;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getItemAt(de.ims.icarus2.model.api.members.container.Container, long)
	 */
	@Override
	public Item getItemAt(@Nullable Container context, long index) {
		return items[IcarusUtils.ensureIntegerValueRange(index)];
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#indexOfItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long indexOfItem(@Nullable Container context, Item item) {
		int index = Arrays.binarySearch(items, item, sorter);

		return index < 0 ? IcarusUtils.UNSET_LONG : index;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getBeginOffset(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public long getBeginOffset(@Nullable Container context) {
		return items[0].getBeginOffset();
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getEndOffset(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public long getEndOffset(@Nullable Container context) {
		return items[items.length-1].getEndOffset();
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
				"Cannot recycle final item storage");
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return false;
	}
}
