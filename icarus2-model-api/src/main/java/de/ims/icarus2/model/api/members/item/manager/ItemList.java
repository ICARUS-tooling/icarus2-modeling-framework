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
package de.ims.icarus2.model.api.members.item.manager;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * Extends the {@link ItemLookup} interface with matching
 * write methods to manipulate the ordered collection of {@link Item items}.
 *
 * @author Markus Gärtner
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

		if(index==IcarusUtils.UNSET_LONG)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Unknown item: "+ModelUtils.getName(item));

		removeItem(index);
	}

	/**
	 * Adds a new item to this container at the specified {@code index};
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
	 * Appends a new item to the end of the list or at whatever position
	 * the implementation deems appropriate.
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
	void swapItems(long index0, long index1);

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
	 * contained items in their former order. If the list is empty this method
	 * returns also {@link DataSequence#emptySequence() an empty} sequence.
	 * Otherwise this default implementation delegates to {@link #removeItems(long, long)}
	 * with a range from {@code 0} to {@link #getItemCount()}{@code -1}.
	 *
	 * @return
	 */
	default DataSequence<? extends Item> removeAllItems() {
		if(isEmpty()) {
			return DataSequence.emptySequence();
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
