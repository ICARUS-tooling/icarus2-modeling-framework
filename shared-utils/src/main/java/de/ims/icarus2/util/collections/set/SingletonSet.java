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

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusException;
import de.ims.icarus2.util.mem.HeapMember;
import de.ims.icarus2.util.mem.Reference;
import de.ims.icarus2.util.mem.ReferenceType;

@HeapMember
public class SingletonSet<E extends Object> extends AbstractDataSet<E> {

	@Reference(ReferenceType.DOWNLINK)
	private E item;

	public SingletonSet() {
		// no-op
	}

	public SingletonSet(E item) {
		reset(item);
	}

	/**
	 * @see de.ims.icarus2.util.collections.set.DataSet#entryCount()
	 */
	@Override
	public int entryCount() {
		if(item==null)
			throw new IcarusException(GlobalErrorCode.ILLEGAL_STATE, "Missing item");

		return 1;
	}

	/**
	 * @see de.ims.icarus2.util.collections.set.DataSet#containerAt(int)
	 */
	@Override
	public E entryAt(int index) {
		if(item==null)
			throw new IcarusException(GlobalErrorCode.ILLEGAL_STATE, "Missing item");
		if(index!=0)
			throw new IndexOutOfBoundsException();

		return item;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		item = null;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return item!=null;
	}

	/**
	 *
	 * @see de.ims.icarus2.util.collections.set.DataSet#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(E member) {
		if(item==null)
			throw new IcarusException(GlobalErrorCode.ILLEGAL_STATE, "Missing item");
		if (member == null)
			throw new NullPointerException("Invalid member"); //$NON-NLS-1$
		return item==member;
	}

	public void reset(E member) {
		if (member == null)
			throw new NullPointerException("Invalid member"); //$NON-NLS-1$
		item = member;
	}

	/**
	 * @see de.ims.icarus2.util.collections.set.AbstractDataSet#add(java.lang.Object)
	 */
	@Override
	public void add(E element) {
		if (element == null)
			throw new NullPointerException("Invalid element"); //$NON-NLS-1$
		if(item!=null)
			throw new IcarusException(GlobalErrorCode.ILLEGAL_STATE, "Element already set"); //$NON-NLS-1$
		item = element;
	}
}