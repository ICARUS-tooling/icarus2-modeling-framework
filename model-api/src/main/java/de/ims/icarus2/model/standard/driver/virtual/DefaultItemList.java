/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 457 $
 * $Date: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/driver/virtual/DefaultItemList.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.driver.virtual;

import static de.ims.icarus2.model.standard.util.CorpusUtils.ensureIntegerValueRange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.ItemList;
import de.ims.icarus2.model.standard.sequences.DataSequenceCollectionWrapper;
import de.ims.icarus2.model.standard.sequences.ListSequence;
import de.ims.icarus2.model.util.DataSequence;
import de.ims.icarus2.util.collections.LookupList;

/**
 * @author Markus Gärtner
 * @version $Id: DefaultItemList.java 457 2016-04-20 13:08:11Z mcgaerty $
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
		return get(ensureIntegerValueRange(index));
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
		return remove(ensureIntegerValueRange(index));
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.ItemList#addItem(long, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public void addItem(long index, Item item) {
		add(ensureIntegerValueRange(index), item);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.ItemList#moveItem(long, long)
	 */
	@Override
	public void moveItem(long index0, long index1) {
		move(ensureIntegerValueRange(index0), ensureIntegerValueRange(index1));
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.ItemList#addItems(long, de.ims.icarus2.model.util.DataSequence)
	 */
	@Override
	public void addItems(long index, DataSequence<? extends Item> items) {
		addAll(ensureIntegerValueRange(index),
				new DataSequenceCollectionWrapper<>(items));
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.ItemList#removeItems(long, long)
	 */
	@Override
	public DataSequence<? extends Item> removeItems(long index0, long index1) {
		int idx0 = ensureIntegerValueRange(index0);
		int idx1 = ensureIntegerValueRange(index1);

		final List<Item> buffer = new ArrayList<>(Math.max(10, idx1-idx0+1));

		removeAll(idx0, idx1, buffer::add);


		return new ListSequence<>(buffer);
	}

}
