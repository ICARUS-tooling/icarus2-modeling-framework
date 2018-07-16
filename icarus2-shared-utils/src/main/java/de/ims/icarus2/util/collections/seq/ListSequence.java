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
package de.ims.icarus2.util.collections.seq;

import java.util.Iterator;
import java.util.List;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusException;
import de.ims.icarus2.util.IcarusUtils;

/**
 *
 * @author Markus Gärtner
 *
 */
public class ListSequence<E extends Object> implements DataSequence<E>, Iterable<E> {

	protected final List<E> list;

	public ListSequence(List<E> list) {
		if (list == null)
			throw new NullPointerException("Invalid list");
		if (list.isEmpty())
			throw new IcarusException(GlobalErrorCode.INVALID_INPUT, "List of elements must not be empty");

		this.list = list;
	}

	/**
	 * @see de.ims.icarus2.util.collections.seq.DataSequence#entryCount()
	 */
	@Override
	public long entryCount() {
		return list.size();
	}

	/**
	 * @see de.ims.icarus2.util.collections.seq.DataSequence#elementAt(long)
	 */
	@Override
	public E elementAt(long index) {
		return list.get(IcarusUtils.ensureIntegerValueRange(index));
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return list.iterator();
	}
}