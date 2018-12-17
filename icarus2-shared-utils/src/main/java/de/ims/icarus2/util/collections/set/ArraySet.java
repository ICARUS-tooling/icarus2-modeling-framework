/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.util.Conditions.checkState;

import java.util.Arrays;
import java.util.List;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.util.collections.ArrayUtils;
import de.ims.icarus2.util.mem.Assessable;
import de.ims.icarus2.util.mem.Reference;
import de.ims.icarus2.util.mem.ReferenceType;

@Assessable
public class ArraySet<E extends Object> extends AbstractDataSet<E> {

	@Reference(ReferenceType.DOWNLINK)
	private Object[] items;

	public ArraySet() {
		// no-op
	}

	@SafeVarargs
	public ArraySet(E...items) {
		reset(items);
	}

	public ArraySet(List<? extends E> items) {
		reset(items);
	}

	private void checkItems() {
		checkState("Missing items", items!=null);
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
		if(items==null)
			throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE, "Missing items");
		@SuppressWarnings("unchecked")
		E item = (E) items[index];
		return item;
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
			throw new IllegalArgumentException("Size must not be negative"); //$NON-NLS-1$

		if(items!=null && items.length==size) {
			return;
		}

		items = new Object[size];
	}

	public void set(int index, E member) {
		if(items==null)
			throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE, "Missing items");

		items[index] = member;
	}

	public void reset(Object[] elements) {
		if (elements == null)
			throw new NullPointerException("Invalid elements"); //$NON-NLS-1$

		items = elements;
	}

	public void reset(List<? extends E> elements) {
		if (elements == null)
			throw new NullPointerException("Invalid elements"); //$NON-NLS-1$

		items = new Object[elements.size()];
		elements.toArray(items);
	}

	/**
	 *
	 * @see de.ims.icarus2.util.collections.set.DataSet#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(E member) {
		if(items==null)
			throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE, "Missing items");
		if (member == null)
			throw new NullPointerException("Invalid member"); //$NON-NLS-1$

		return ArrayUtils.contains(items, member);
	}

	/**
	 * @see de.ims.icarus2.util.collections.set.AbstractDataSet#add(java.lang.Object)
	 */
	@Override
	public void add(E element) {
		if(items==null)
			throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE, "Missing items");
		if (element == null)
			throw new NullPointerException("Invalid element"); //$NON-NLS-1$

		for(int i=0; i<items.length; i++) {
			if(items[i]==null) {
				items[i] = element;
				return;
			}
		}

		throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE, "Set already full"); //$NON-NLS-1$
	}
}