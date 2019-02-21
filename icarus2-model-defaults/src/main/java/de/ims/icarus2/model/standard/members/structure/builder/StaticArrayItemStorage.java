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

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.standard.members.container.AbstractImmutableItemStorage;
import de.ims.icarus2.util.IcarusUtils;

/**
 * Array based sorted storage for small containers using binary search for the
 * {@link #indexOfItem(Container, Item) index lookup} method.
 *
 * @author Markus Gärtner
 *
 */
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
	public void addNotify(Container context) {
		// no-op
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
		return items.length;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getItemAt(de.ims.icarus2.model.api.members.container.Container, long)
	 */
	@Override
	public Item getItemAt(Container context, long index) {
		return items[IcarusUtils.ensureIntegerValueRange(index)];
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#indexOfItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long indexOfItem(Container context, Item item) {
		int index = Arrays.binarySearch(items, item, sorter);

		return index < 0 ? IcarusUtils.UNSET_LONG : index;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getBeginOffset(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public long getBeginOffset(Container context) {
		return items[0].getBeginOffset();
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getEndOffset(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public long getEndOffset(Container context) {
		return items[items.length-1].getEndOffset();
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		throw new UnsupportedOperationException("Cannot recycle final item storage");
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return false;
	}
}
