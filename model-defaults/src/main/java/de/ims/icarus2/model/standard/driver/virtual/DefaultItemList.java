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
package de.ims.icarus2.model.standard.driver.virtual;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.ItemList;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.LookupList;
import de.ims.icarus2.util.collections.seq.DataSequence;
import de.ims.icarus2.util.collections.seq.DataSequenceCollectionWrapper;
import de.ims.icarus2.util.collections.seq.ListSequence;

/**
 * @author Markus Gärtner
 *
 */
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
	 * @see de.ims.icarus2.model.api.members.item.ItemLookup#getItemCount()
	 */
	@Override
	public long getItemCount() {
		return size();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.ItemLookup#getItemAt(long)
	 */
	@Override
	public Item getItemAt(long index) {
		return get(IcarusUtils.ensureIntegerValueRange(index));
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.ItemLookup#indexOfItem(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long indexOfItem(Item item) {
		return indexOf(item);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.ItemList#removeItem(long)
	 */
	@Override
	public Item removeItem(long index) {
		return remove(IcarusUtils.ensureIntegerValueRange(index));
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.ItemList#addItem(long, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public void addItem(long index, Item item) {
		add(IcarusUtils.ensureIntegerValueRange(index), item);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.ItemList#moveItem(long, long)
	 */
	@Override
	public void moveItem(long index0, long index1) {
		move(IcarusUtils.ensureIntegerValueRange(index0), IcarusUtils.ensureIntegerValueRange(index1));
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.ItemList#addItems(long, de.ims.icarus2.util.collections.seq.DataSequence)
	 */
	@Override
	public void addItems(long index, DataSequence<? extends Item> items) {
		addAll(IcarusUtils.ensureIntegerValueRange(index),
				new DataSequenceCollectionWrapper<>(items));
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.ItemList#removeItems(long, long)
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
