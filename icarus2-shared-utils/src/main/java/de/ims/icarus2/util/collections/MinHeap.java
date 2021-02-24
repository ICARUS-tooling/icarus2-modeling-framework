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

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;


/**
 * Implements a min-heap. This abstract base class only provides
 * the general <i>heapify</i> methods and basic internal navigation
 * support when presenting the heap via an array.
 * The derived classes {@link IntMinHeap}, {@link LongMinHeap},
 * {@link DoubleMinHeap} and {@link ObjectMinHeap} finally provide
 * specialized implementations for the respective primites or
 * general objects.
 *
 * Note that this implementation and its subclasses use a backing
 * array of static size, which is instantiated at constructor time.
 * Exceeding that capacity will result in an {@link ArrayIndexOutOfBoundsException}
 * being thrown when inserting a new value!
 *
 * @author Markus Gärtner
 *
 */
public abstract class MinHeap {

	// Number of elements currently in the heap
	protected int size;

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size==0;
	}

	public void clear() {
		size = 0;
	}

	protected void refreshUp(int nodeIndex) {
		if (nodeIndex != 0) {
			int parentIndex = parent(nodeIndex);

			if (compareValuesAt(parentIndex, nodeIndex)>0) {
				swap(nodeIndex, parentIndex);
				refreshUp(parentIndex);
			}
		}
	}

	protected void refreshDown(int nodeIndex) {

		int minIndex;

		int leftChildIndex = left(nodeIndex);

		int rightChildIndex = right(nodeIndex);

		if (rightChildIndex >= size) {
			if (leftChildIndex >= size) {
				return;
			}

			minIndex = leftChildIndex;
		} else {
			if (compareValuesAt(leftChildIndex, rightChildIndex)<=0) {
				minIndex = leftChildIndex;
			} else {
				minIndex = rightChildIndex;
			}
		}

		if (compareValuesAt(nodeIndex, minIndex)>0) {
			swap(nodeIndex, minIndex);
			refreshDown(minIndex);
		}

	}

	public abstract int capacity();

	public int remaining() {
		return capacity() - size;
	}

	protected abstract int compareValuesAt(int index0, int index1);
	protected abstract void swapValues(int index0, int index1);

	protected void swap(int index0, int index1) {
		swapValues(index0, index1);
	}

	/**
	 * Computes index of the given node's left child
	 */
	protected static int left(int nodeIndex) {
		return (nodeIndex<<1) + 1;
	}

	/**
	 * Computes index of the given node's right child
	 */
	protected static int right(int nodeIndex) {
		return (nodeIndex<<1) + 2;
	}

	/**
	 * Computes index of the given node's parent
	 */
	protected static int parent(int nodeIndex) {
		return (nodeIndex - 1) >>> 1;
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class IntMinHeap extends MinHeap implements IntConsumer {
		private final int[] values;

		public IntMinHeap(int size) {
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

		public void push(int value) {
			int index = size;
			size++;
			values[index] = value;

			refreshUp(index);
		}

		public int pop() {
			size--;

			int result = values[0];
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
		 * @see java.util.function.IntConsumer#accept(int)
		 */
		@Override
		public void accept(int value) {
			push(value);
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class LongMinHeap extends MinHeap implements LongConsumer {
		private final long[] values;

		public LongMinHeap(int size) {
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

		public void push(long value) {
			int index = size;
			size++;
			values[index] = value;

			refreshUp(index);
		}

		public long pop() {
			size--;

			long result = values[0];
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
		 * @see java.util.function.LongConsumer#accept(long)
		 */
		@Override
		public void accept(long value) {
			push(value);
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class DoubleMinHeap extends MinHeap implements DoubleConsumer {
		private final double[] values;

		public DoubleMinHeap(int size) {
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

		public void push(double value) {
			int index = size;
			size++;
			values[index] = value;

			refreshUp(index);
		}

		public double pop() {
			size--;

			double result = values[0];
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
		 * @see java.util.function.DoubleConsumer#accept( double)
		 */
		@Override
		public void accept(double value) {
			push(value);
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class ObjectMinHeap<E extends Object> extends MinHeap implements Consumer<E> {
		private final Object[] values;
		private final Comparator<? super E> comparator;

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public ObjectMinHeap(int size) {
			values = new Object[size];
			comparator = (o1, o2) -> ((Comparable)o1).compareTo(o2);
		}

		public ObjectMinHeap(int size, Comparator<? super E> comparator) {
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

		public void push(E value) {
			int index = size;
			size++;
			values[index] = value;

			refreshUp(index);
		}

		public E pop() {
			size--;

			@SuppressWarnings("unchecked")
			E result = (E) values[0];
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

		/**
		 * @see java.util.function.DoubleConsumer#accept( double)
		 */
		@Override
		public void accept(E value) {
			push(value);
		}
	}

}
