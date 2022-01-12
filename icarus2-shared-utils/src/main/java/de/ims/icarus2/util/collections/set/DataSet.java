/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.collections.CollectionUtils.emptyIterator;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.collections.seq.DataSequence;


/**
 * Defines an array like access protocol for members of a corpus which
 * in addition allows for containment checks. Note that unlike the
 * {@link DataSequence sequence} counterpart the size of this storage
 * is limited to {@code int} size.
 *
 * @param <E> The type of elements stored in this set
 *
 * @author Markus Gärtner
 *
 * @see DataSequence
 */
public interface DataSet<E extends Object> extends Iterable<E> {

	int entryCount();

	default boolean isEmpty() {
		return entryCount()==0;
	}

	E entryAt(int index);

	boolean contains(E element);

	default Set<E> toSet() {
		return LazyCollection.<E>lazySet(entryCount())
				.addFromForEach(this::forEach)
				.getAsSet();
	}

	default List<E> toList() {
		return LazyCollection.<E>lazyList(entryCount())
				.addFromForEach(this::forEach)
				.getAsList();
	}

	default Object[] toArray() {
		Object[] result = new Object[entryCount()];

		for(int i = 0; i<entryCount(); i++) {
			result[i] = entryAt(i);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	default <T> T[] toArray(T[] array) {
		requireNonNull(array);
		int size = entryCount();

		if(array.length<size) {
			array = (T[]) Array.newInstance(array.getClass().getComponentType(), size);
		}

		Object[] result = array;

		for(int i = 0; i<size; i++) {
			result[i] = entryAt(i);
		}

        if (array.length > size) {
        	array[size] = null;
        }

		return array;
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	default Iterator<E> iterator() {
		return isEmpty() ? emptyIterator() : new DataSetIterator<>(this);
	}

	@SuppressWarnings("unchecked")
	public static <E extends Object> DataSet<E> emptySet() {
		return (DataSet<E>) EMPTY_SET;
	}

	public static final DataSet<Object> EMPTY_SET = new DataSet<Object>() {

		@Override
		public int entryCount() {
			return 0;
		}

		@Override
		public Object entryAt(int index) {
			throw new IndexOutOfBoundsException("Set is empty");
		}

		@Override
		public boolean contains(Object element) {
			return false;
		}
	};

	public static class DataSetIterator<E extends Object> implements Iterator<E> {
		private final DataSet<? extends E> source;
		private int pos = 0;
		private final int fence;

		public DataSetIterator(DataSet<? extends E> source) {
			this(source, 0, source.entryCount());
		}

		public DataSetIterator(DataSet<? extends E> source, int beginIndex) {
			this(source, beginIndex, source.entryCount());
		}

		/**
		 *
		 * @param source
		 * @param beginIndex first index position to be accessed by the iterator (inclusive)
		 * @param endIndex last index position to be accessed by the iterator (exclusive)
		 */
		public DataSetIterator(DataSet<? extends E> source, int beginIndex, int endIndex) {
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
			return source.entryAt(pos++);
		}
	}
}
