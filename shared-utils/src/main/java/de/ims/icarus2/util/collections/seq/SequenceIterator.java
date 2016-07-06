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
import java.util.NoSuchElementException;

/**
 * @author Markus Gärtner
 *
 */
public class SequenceIterator<E extends Object> implements Iterator<E> {

	@SuppressWarnings("rawtypes")
	private final DataSequence sequence;
	private long index;

	public SequenceIterator(DataSequence<? extends E> sequence, long index) {
		if (sequence == null)
			throw new NullPointerException("Invalid sequence");

		this.sequence = sequence;
		this.index = index;
	}

	public SequenceIterator(DataSequence<? extends E> sequence) {
		this(sequence, 0L);
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return index<sequence.entryCount();
	}

	/**
	 * @see java.util.Iterator#next()
	 */
	@Override
	public E next() {
		if(index>=sequence.entryCount())
			throw new NoSuchElementException();

		@SuppressWarnings("unchecked")
		E element = (E) sequence.elementAt(index);

		index++;

		return element;
	}
}
