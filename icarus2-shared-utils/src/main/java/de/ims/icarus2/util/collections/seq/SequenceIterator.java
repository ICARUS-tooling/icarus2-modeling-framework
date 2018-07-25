/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
