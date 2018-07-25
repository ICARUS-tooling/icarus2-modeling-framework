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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import de.ims.icarus2.IcarusException;
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
	 * @throws IcarusException if the backing data is unreachable or if
	 * modifications have been made to the backing data that rendered
	 * it inconsistent.
	 * @throws IndexOutOfBoundsException if the given index lies outside the
	 * boundaries for legal index values for this sequence
	 */
	E elementAt(long index);

	/**
	 * Applies the given {@code action} sequentially to every {@link #elementAt(long) element}
	 * in this sequence.
	 * @param action
	 */
	default void forEachEntry(Consumer<? super E> action) {
		for(long i =0L; i<entryCount(); i++) {
			action.accept(elementAt(i));
		}
	}

	/**
	 * Helper method to transform the content of this sequence into a regular
	 * {@link List}. Note that this method will fail if the size of this
	 * sequence exceeds the capacity of integer addressing.
	 *
	 * @see IcarusUtils#ensureIntegerValueRange(long)
	 *
	 * @return
	 */
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
			requireNonNull(source);
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
