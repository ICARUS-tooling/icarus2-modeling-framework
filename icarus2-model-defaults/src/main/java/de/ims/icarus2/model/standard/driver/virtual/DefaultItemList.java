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
package de.ims.icarus2.model.standard.driver.virtual;

import static de.ims.icarus2.util.IcarusUtils.ensureIntegerValueRange;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.manager.ItemList;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.collections.LookupList;
import de.ims.icarus2.util.collections.seq.DataSequence;
import de.ims.icarus2.util.collections.seq.DataSequenceCollectionWrapper;
import de.ims.icarus2.util.collections.seq.ListSequence;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(ItemList.class)
public class DefaultItemList extends LookupList<Item> implements ItemList {

	public DefaultItemList() {
		super();
	}

	public DefaultItemList(Collection<? extends Item> c) {
		super(c);
	}

	public DefaultItemList(int capacity) {
		super(capacity);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemLookup#getItemCount()
	 */
	@Override
	public long getItemCount() {
		return size();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemLookup#getItemAt(long)
	 */
	@Override
	public Item getItemAt(long index) {
		return get(IcarusUtils.ensureIntegerValueRange(index));
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemLookup#indexOfItem(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long indexOfItem(Item item) {
		return indexOf(requireNonNull(item));
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemList#removeItem(long)
	 */
	@Override
	public Item removeItem(long index) {
		return remove(IcarusUtils.ensureIntegerValueRange(index));
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemList#addItem(long, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public void addItem(long index, Item item) {
		requireNonNull(item);
		add(IcarusUtils.ensureIntegerValueRange(index), item);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemList#swapItems(long, long)
	 */
	@Override
	public void swapItems(long index0, long index1) {
		int idx0 = ensureIntegerValueRange(index0);
		int idx1 = ensureIntegerValueRange(index1);
		Item item0 = get(idx0);
		Item item1 = get(idx1);
		set(idx0, item1);
		set(idx1, item0);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemList#addItems(long, de.ims.icarus2.util.collections.seq.DataSequence)
	 */
	@Override
	public void addItems(long index, DataSequence<? extends Item> items) {
		requireNonNull(items);
		addAll(IcarusUtils.ensureIntegerValueRange(index),
				new DataSequenceCollectionWrapper<>(items));
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemList#removeItems(long, long)
	 */
	@Override
	public DataSequence<? extends Item> removeItems(long index0, long index1) {
		int idx0 = IcarusUtils.ensureIntegerValueRange(index0);
		int idx1 = IcarusUtils.ensureIntegerValueRange(index1);

		final List<Item> buffer = new ArrayList<>(Math.max(10, idx1-idx0+1));

		removeAll(idx0, idx1, buffer::add);


		return new ListSequence<>(buffer);
	}

}
