/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.collections.set;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.util.collections.ArrayUtils;
import de.ims.icarus2.util.mem.Assessable;
import de.ims.icarus2.util.mem.Reference;
import de.ims.icarus2.util.mem.ReferenceType;

/**
 *
 * @author Markus Gärtner
 *
 * @param <E> type of elements contained in this set
 */
@Assessable
public class ArraySet<E extends Object> extends AbstractDataSet<E> {

	@Reference(ReferenceType.DOWNLINK)
	private Object[] items;

	public ArraySet() {
		items = new Object[0];
	}

	@SafeVarargs
	public ArraySet(E...items) {
		reset(items);
	}

	public ArraySet(List<? extends E> items) {
		reset(items);
	}

	private void checkItems() {
		if(items==null)
			throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE, "Missing items");
	}

	/**
	 * @see de.ims.icarus2.util.collections.set.DataSet#entryCount()
	 */
	@Override
	public int entryCount() {
		checkItems();
		return items.length;
	}

	/**
	 * @see de.ims.icarus2.util.collections.set.DataSet#entryAt(int)
	 */
	@Override
	public E entryAt(int index) {
		checkItems();
		@SuppressWarnings("unchecked")
		E item = (E) items[index];
		return requireNonNull(item);
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		if(items!=null) {
			Arrays.fill(items, null);
		}
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return items!=null && !ArrayUtils.contains(items, null);
	}

	public void reset(int size) {
		if(size<1)
			throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT,
					"Size must not be negative");

		// If our size already matches, just clear content
		if(items!=null && items.length==size) {
			Arrays.fill(items, null);
			return;
		}

		items = new Object[size];
	}

	public void set(int index, E member) {
		requireNonNull(member);
		checkItems();

		items[index] = member;
	}

	public void reset(Object[] elements) {
		requireNonNull(elements);

		items = elements;
	}

	public void reset(List<? extends E> elements) {
		requireNonNull(elements);

		items = new Object[elements.size()];
		elements.toArray(items);
	}

	/**
	 *
	 * @see de.ims.icarus2.util.collections.set.DataSet#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(E member) {
		requireNonNull(member);
		checkItems();

		return ArrayUtils.contains(items, member);
	}

	/**
	 * @see de.ims.icarus2.util.collections.set.AbstractDataSet#add(java.lang.Object)
	 */
	@Override
	public void add(E element) {
		requireNonNull(element);
		checkItems();

		for(int i=0; i<items.length; i++) {
			if(items[i]==null) {
				items[i] = element;
				return;
			}
		}

		throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE, "Set already full");
	}
}