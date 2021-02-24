/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.collections;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;

/**
 * Implements a min-heap that maps values in the heap to
 * instances of a custom type.
 *
 * @author Markus Gärtner
 *
 * @param <T> type of the associated payload in the heap
 */
public abstract class MappedMinHeap<T extends Object> extends MinHeap {

	/**
	 *  Mapped objects, order matches values in heap
	 */
	protected final Object[] elements;

	protected MappedMinHeap(int size) {
		checkArgument(size>0);

		elements = new Object[size];
	}

	@SuppressWarnings("unchecked")
	public T peekObject() {
		if(size<1)
			throw new NoSuchElementException();

		return (T) elements[0];
	}

	@Override
	public void clear() {
		super.clear();
		Arrays.fill(elements, null);
	}

	@Override
	protected void swap(int index0, int index1) {
		// Default value swap
		super.swap(index0, index1);

		// Now do the element swap
		Object tmpObj = elements[index0];
		elements[index0] = elements[index1];
		elements[index1] = tmpObj;
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 * @param <T> type of the associated payload in the heap
	 */
	public static class MappedIntMinHeap<T extends Object> extends MappedMinHeap<T> implements ObjIntConsumer<T> {
		private final int[] values;

		public MappedIntMinHeap(int size) {
			super(size);

			values = new int[size];
		}

		@Override
		public int capacity() {
			return values.length;
		}

		public int peekValue() {
			if(size<1)
				throw new NoSuchElementException();

			return values[0];
		}

		/**
		 * @see de.ims.icarus2.util.collections.MappedMinHeap#compareValuesAt(int, int)
		 */
		@Override
		protected int compareValuesAt(int index0, int index1) {
			return Integer.compare(values[index0], values[index1]);
		}

		public void push(T source, int value) {
			int index = size;
			size++;
			elements[index] = source;
			values[index] = value;

			refreshUp(index);
		}

		public T pop() {
			size--;

			@SuppressWarnings("unchecked")
			T result = (T)elements[0];

			elements[0] = elements[size];
			values[0] = values[size];

			if (size > 0) {
				refreshDown(0);
			}

			return result;
		}

		@Override
		protected void swapValues(int index0, int index1) {
			int tmpInt = values[index0];
			values[index0] = values[index1];
			values[index1] = tmpInt;
		}

		/**
		 * @see java.util.function.ObjIntConsumer#accept(java.lang.Object, int)
		 */
		@Override
		public void accept(T t, int value) {
			push(t, value);
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 * @param <T> type of the associated payload in the heap
	 */
	public static class MappedLongMinHeap<T extends Object> extends MappedMinHeap<T> implements ObjLongConsumer<T> {
		private final long[] values;

		public MappedLongMinHeap(int size) {
			super(size);

			values = new long[size];
		}

		@Override
		public int capacity() {
			return values.length;
		}

		public long peekValue() {
			if(size<1)
				throw new NoSuchElementException();

			return values[0];
		}

		/**
		 * @see de.ims.icarus2.util.collections.MappedMinHeap#compareValuesAt(int, int)
		 */
		@Override
		protected int compareValuesAt(int index0, int index1) {
			return Long.compare(values[index0], values[index1]);
		}

		public void push(T source, long value) {
			int index = size;
			size++;
			elements[index] = source;
			values[index] = value;

			refreshUp(index);
		}

		public T pop() {
			size--;

			@SuppressWarnings("unchecked")
			T result = (T)elements[0];

			elements[0] = elements[size];
			values[0] = values[size];

			if (size > 0) {
				refreshDown(0);
			}

			return result;
		}

		@Override
		protected void swapValues(int index0, int index1) {
			long tmpLong = values[index0];
			values[index0] = values[index1];
			values[index1] = tmpLong;
		}

		/**
		 * @see java.util.function.ObjLongConsumer#accept(java.lang.Object, long)
		 */
		@Override
		public void accept(T t, long value) {
			push(t, value);
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 * @param <T> type of the associated payload in the heap
	 */
	public static class MappedDoubleMinHeap<T extends Object> extends MappedMinHeap<T> implements ObjDoubleConsumer<T> {
		private final double[] values;

		public MappedDoubleMinHeap(int size) {
			super(size);

			values = new double[size];
		}

		@Override
		public int capacity() {
			return values.length;
		}

		public double peekValue() {
			if(size<1)
				throw new NoSuchElementException();

			return values[0];
		}

		/**
		 * @see de.ims.icarus2.util.collections.MappedMinHeap#compareValuesAt(int, int)
		 */
		@Override
		protected int compareValuesAt(int index0, int index1) {
			return Double.compare(values[index0], values[index1]);
		}

		public void push(T source, double value) {
			int index = size;
			size++;
			elements[index] = source;
			values[index] = value;

			refreshUp(index);
		}

		public T pop() {
			size--;

			@SuppressWarnings("unchecked")
			T result = (T)elements[0];

			elements[0] = elements[size];
			values[0] = values[size];

			if (size > 0) {
				refreshDown(0);
			}

			return result;
		}

		@Override
		protected void swapValues(int index0, int index1) {
			double tmpDouble = values[index0];
			values[index0] = values[index1];
			values[index1] = tmpDouble;
		}

		/**
		 * @see java.util.function.ObjDoubleConsumer#accept(java.lang.Object, double)
		 */
		@Override
		public void accept(T t, double value) {
			push(t, value);
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 * @param <T> type of the associated payload in the heap
	 * @param <E> type of the values in the heap
	 */
	public static class MappedObjectMinHeap<E extends Object, T extends Object> extends MappedMinHeap<T> implements BiConsumer<T, E> {
		/** Values of type {@code V} */
		private final Object[] values;
		private final Comparator<? super E> comparator;

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public MappedObjectMinHeap(int size) {
			super(size);
			values = new Object[size];
			comparator = (o1, o2) -> ((Comparable)o1).compareTo(o2);
		}

		public MappedObjectMinHeap(int size, Comparator<? super E> comparator) {
			super(size);
			requireNonNull(comparator);

			values = new Object[size];
			this.comparator = comparator;
		}

		@Override
		public int capacity() {
			return values.length;
		}

		@SuppressWarnings("unchecked")
		public E peekValue() {
			if(size<1)
				throw new NoSuchElementException();

			return (E) values[0];
		}

		/**
		 * @see de.ims.icarus2.util.collections.MappedMinHeap#compareValuesAt(int, int)
		 */
		@SuppressWarnings("unchecked")
		@Override
		protected int compareValuesAt(int index0, int index1) {
			return comparator.compare((E)values[index0], (E)values[index1]);
		}

		public void push(T source, E value) {
			int index = size;
			size++;
			elements[index] = source;
			values[index] = value;

			refreshUp(index);
		}

		public T pop() {
			size--;

			@SuppressWarnings("unchecked")
			T result = (T)elements[0];

			elements[0] = elements[size];
			values[0] = values[size];

			if (size > 0) {
				refreshDown(0);
			}

			return result;
		}

		@Override
		protected void swapValues(int index0, int index1) {
			Object tmp = values[index0];
			values[index0] = values[index1];
			values[index1] = tmp;
		}

		@Override
		public void accept(T t, E u) {
			push(t, u);
		}
	}
}
