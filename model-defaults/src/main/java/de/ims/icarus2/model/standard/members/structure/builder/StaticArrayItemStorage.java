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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/structure/builder/StaticArrayItemStorage.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members.structure.builder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.standard.members.container.AbstractImmutableItemStorage;

/**
 * Array based storage for small containers using binary search for the
 * {@link #indexOfItem(Container, Item) index lookup} method.
 *
 * @author Markus Gärtner
 * @version $Id: StaticArrayItemStorage.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
public class StaticArrayItemStorage extends AbstractImmutableItemStorage {

	public static final int MAX_SIZE = 256;

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
	public void addNotify(Container context) throws ModelException {
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#removeNotify(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public void removeNotify(Container context) throws ModelException {
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
		return items[ensureIntegerValueRange(index)];
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#indexOfItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long indexOfItem(Container context, Item item) {
		int index = Arrays.binarySearch(items, item, sorter);

		return index < 0 ? NO_INDEX : index;
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
