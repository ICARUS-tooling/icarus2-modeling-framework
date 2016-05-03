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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/sequences/SequenceIterator.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.sequences;

import java.util.Iterator;
import java.util.NoSuchElementException;

import de.ims.icarus2.model.util.DataSequence;

/**
 * @author Markus Gärtner
 * @version $Id: SequenceIterator.java 457 2016-04-20 13:08:11Z mcgaerty $
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
