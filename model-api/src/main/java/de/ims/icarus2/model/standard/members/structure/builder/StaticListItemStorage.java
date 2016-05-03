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

 * $Revision: 382 $
 * $Date: 2015-04-09 16:23:50 +0200 (Do, 09 Apr 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/structure/builder/StaticListItemStorage.java $
 *
 * $LastChangedDate: 2015-04-09 16:23:50 +0200 (Do, 09 Apr 2015) $
 * $LastChangedRevision: 382 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members.structure.builder;

import static de.ims.icarus2.model.standard.util.CorpusUtils.ensureIntegerValueRange;

import java.util.Collection;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.manifest.ContainerType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.standard.members.container.AbstractImmutableItemStorage;
import de.ims.icarus2.util.collections.LookupList;

/**
 * @author Markus Gärtner
 * @version $Id: StaticListItemStorage.java 382 2015-04-09 14:23:50Z mcgaerty $
 *
 */
public class StaticListItemStorage extends AbstractImmutableItemStorage {

	private final LookupList<Item> items;

	private final Item beginItem, endItem;

	public StaticListItemStorage(Collection<Item> items, Item beginItem, Item endItem) {
		this.items = new LookupList<>(items);

		this.beginItem = beginItem;
		this.endItem = endItem;
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
		return items.size();
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getItemAt(de.ims.icarus2.model.api.members.container.Container, long)
	 */
	@Override
	public Item getItemAt(Container context, long index) {
		return items.get(ensureIntegerValueRange(index));
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#indexOfItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long indexOfItem(Container context, Item item) {
		return items.indexOf(item);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getBeginOffset(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public long getBeginOffset(Container context) {
		return beginItem==null ? NO_INDEX : beginItem.getBeginOffset();
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getEndOffset(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public long getEndOffset(Container context) {
		return endItem==null ? NO_INDEX : endItem.getBeginOffset();
	}

	/**
	 * @see de.ims.icarus2.model.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		throw new UnsupportedOperationException("Cannot recycle final item storage");
	}

	/**
	 * @see de.ims.icarus2.model.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return false;
	}

}
