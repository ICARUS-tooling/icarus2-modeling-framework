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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;

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

	default void forEachEntry(Consumer<? super E> action) {
		for(int i = 0; i<entryCount(); i++) {
			action.accept(entryAt(i));
		}
	}

	default Set<E> toSet() {
		LazyCollection<E> result = LazyCollection.lazySet(entryCount());

		forEachEntry(result);

		return result.getAsSet();
	}

	default List<E> toList() {
		LazyCollection<E> result = LazyCollection.lazyList(entryCount());

		forEachEntry(result);

		return result.getAsList();
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
		return new DataSetIterator<>(this);
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
