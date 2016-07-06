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

import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
public class ArraySequence<E extends Object> implements DataSequence<E> {

	private final E[] elements;

	public ArraySequence(E[] elements) {
		if (elements == null)
			throw new NullPointerException("Invalid elements");

		this.elements = elements;
	}

	/**
	 * @see de.ims.icarus2.util.collections.seq.DataSequence#entryCount()
	 */
	@Override
	public long entryCount() {
		return elements.length;
	}

	/**
	 * @see de.ims.icarus2.util.collections.seq.DataSequence#elementAt(long)
	 */
	@Override
	public E elementAt(long index) {
		return elements[IcarusUtils.ensureIntegerValueRange(index)];
	}

}
