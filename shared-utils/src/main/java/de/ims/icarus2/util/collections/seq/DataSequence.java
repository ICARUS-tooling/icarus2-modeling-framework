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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.collections.set.DataSet;


/**
 * An array like storage for containers that allows unified access to
 * collections of items while still preventing foreign
 * objects from making modifications.
 * <p>
 * Note that the {@link #entryCount() size} of a sequence modeled
 * by this interface is not limited to {@code int} values.
 *
 * @param <E> The type of elements stored in this sequence
 *
 * @author Markus Gärtner
 *
 * @see DataSet
 */
public interface DataSequence<E extends Object> extends Iterable<E> {

	/**
	 * Returns the total number of items in this sequence
	 *
	 * @return
	 */
	long entryCount();

	/**
	 * Fetches and returns the element stored at the specified index.
	 *
	 * @param index
	 * @return
	 * @throws ModelExceptionif the backing data is unreachable or if
	 * modifications have been made to the backing data that rendered
	 * it inconsistent.
	 * @throws IndexOutOfBoundsException if the given index lies outside the
	 * boundaries for legal index values for this sequence
	 */
	E elementAt(long index);

	default void forEachEntry(Consumer<? super E> action) {
		for(long i =0L; i<entryCount(); i++) {
			action.accept(elementAt(i));
		}
	}

	default List<E> getEntries() {
		int size = IcarusUtils.ensureIntegerValueRange(entryCount());

		LazyCollection<E> result = LazyCollection.lazyList(size);

		forEachEntry(result);

		return result.getAsList();
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	default Iterator<E> iterator() {
		return new DataSequenceIterator<>(this);
	}

	@SuppressWarnings("unchecked")
	public static <E extends Object> DataSequence<E> emptySequence() {
		return (DataSequence<E>) EMPTY_SEQUENCE;
	}

	public static final DataSequence<Object> EMPTY_SEQUENCE = new DataSequence<Object>() {

		@Override
		public long entryCount() {
			return 0L;
		}

		@Override
		public Object elementAt(long index) {
			throw new IndexOutOfBoundsException("Sequence is empty");
		}
	};

	public static class DataSequenceIterator<E extends Object> implements Iterator<E> {
		private final DataSequence<? extends E> source;
		private long pos = 0;
		private final long fence;

		public DataSequenceIterator(DataSequence<? extends E> source) {
			this(source, 0, source.entryCount());
		}

		public DataSequenceIterator(DataSequence<? extends E> source, long beginIndex) {
			this(source, beginIndex, source.entryCount());
		}

		/**
		 *
		 * @param source
		 * @param beginIndex first index position to be accessed by the iterator (inclusive)
		 * @param endIndex last index position to be accessed by the iterator (exclusive)
		 */
		public DataSequenceIterator(DataSequence<? extends E> source, long beginIndex, long endIndex) {
			checkNotNull(source);
			checkArgument(beginIndex>=0);
			checkArgument(beginIndex<endIndex);
			checkArgument(endIndex<=source.entryCount());

			this.source = source;
			pos = beginIndex;
			fence = endIndex;
		}

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return pos<fence;
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		@Override
		public E next() {
			if(pos>=fence)
				throw new NoSuchElementException();
			return source.elementAt(pos++);
		}
	}
}
