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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/members/item/ItemList.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.members.item;

import static de.ims.icarus2.model.standard.util.CorpusUtils.getName;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.util.collections.DataSequence;

/**
 * Extends the {@link ItemLookup} interface with matching
 * write methods to manipulate the ordered collection of {@link Item items}.
 *
 * @author Markus Gärtner
 * @version $Id: ItemList.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
public interface ItemList extends ItemLookup {

	/**
	 * Removes and returns the item at the given index. Shifts the
	 * indices of all items after the given position to account
	 * for the missing member.
	 *
	 * @param index The position of the item to be removed
	 * @return The item previously at position {@code index}.
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt;= getItemCount()</tt>)
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	Item removeItem(long index);

	default void removeItem(Item item) {
		long index = indexOfItem(item);

		if(index==NO_INDEX)
			throw new ModelException(ModelErrorCode.INVALID_INPUT,
					"Unknown item: "+getName(item));

		removeItem(index);
	}

	/**
	 * Adds a new item to this container
	 *
	 * Note that calling this method with an {@code index} parameter
	 * equal to the size of the mutating container as returned by
	 * {@link Container#getItemCount()} is equivalent to
	 * using {@link #addItem()}.
	 *
	 * @param index The position to insert the new item at
	 * @param item
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt; getItemCount()</tt>)
	 * @throws NullPointerException if the {@code item} argument is {@code null}
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	void addItem(long index, Item item);

	/**
	 * Appends a new item to the end of the list.
	 *
	 * @see #addItem(long, Item)
	 */
	default void addItem(Item item) {
		addItem(getItemCount(), item);
	}

	/**
	 * Moves the item currently located at position {@code index0}
	 * over to position {@code index1}. The item previously located
	 * at position {@code index1} will then be moved to {@code index0}.
	 *
	 * @param index0
	 * @param index1
	 * @throws IllegalArgumentException if <tt>index0 == index1</tt>
	 * @throws IndexOutOfBoundsException if either {@code index0} or {@code index1}
	 * is out of range (<tt>index &lt; 0 || index &gt;= getItemCount()</tt>)
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	void moveItem(long index0, long index1);

	/**
	 * Inserts a sequence of items at the specified position.
	 * Shifts subsequent items to the first position after the end of the
	 * inserted sequence.
	 *
	 * @param index
	 * @param items
	 */
	void addItems(long index, DataSequence<? extends Item> items);

	/**
	 * Clears the internal storage of this list and returns all the previously
	 * contained items in their former order. If the
	 *
	 * This default implementation
	 *
	 * @return
	 */
	default DataSequence<? extends Item> removeAllItems() {
		if(isEmpty()) {
			return null;
		}

		return removeItems(0, getItemCount()-1);
	}

	/**
	 * Removes a selected range of items from the list
	 *
	 * @param index0 index of first item to be removed (inclusive)
	 * @param index1 index of the last item to be removed (inclusive)
	 * @return
	 */
	DataSequence<? extends Item> removeItems(long index0, long index1);
}
