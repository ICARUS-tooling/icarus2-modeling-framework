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
package de.ims.icarus2.util.collections.set;

import static de.ims.icarus2.util.Conditions.checkState;

import java.util.Arrays;
import java.util.List;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusException;
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
			throw new IcarusException(GlobalErrorCode.ILLEGAL_STATE, "Missing items");
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
			throw new IcarusException(GlobalErrorCode.ILLEGAL_STATE, "Missing items");

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
			throw new IcarusException(GlobalErrorCode.ILLEGAL_STATE, "Missing items");
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
			throw new IcarusException(GlobalErrorCode.ILLEGAL_STATE, "Missing items");
		if (element == null)
			throw new NullPointerException("Invalid element"); //$NON-NLS-1$

		for(int i=0; i<items.length; i++) {
			if(items[i]==null) {
				items[i] = element;
				return;
			}
		}

		throw new IcarusException(GlobalErrorCode.ILLEGAL_STATE, "Set already full"); //$NON-NLS-1$
	}
}