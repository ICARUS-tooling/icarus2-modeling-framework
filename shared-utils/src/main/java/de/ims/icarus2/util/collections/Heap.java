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
package de.ims.icarus2.util.collections;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;


/**
 * @author Markus Gärtner
 *
 */
public abstract class Heap {

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
			} else {
				minIndex = leftChildIndex;
			}
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

	protected abstract int compareValuesAt(int index0, int index1);
	protected abstract void swapValues(int index0, int index1);

	protected void swap(int index0, int index1) {
		swapValues(index0, index1);
	}

	protected static int left(int nodeIndex) {
		return (nodeIndex<<1) + 1;
	}

	protected static int right(int nodeIndex) {
		return (nodeIndex<<1) + 2;
	}

	protected static int parent(int nodeIndex) {
		return (nodeIndex - 1) >>> 1;
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class IntHeap extends Heap implements IntConsumer {
		private final int[] values;

		public IntHeap(int size) {
			values = new int[size];
		}

		public int peekValue() {
			if(size<1)
				throw new NoSuchElementException();

			return values[0];
		}

		/**
		 * @see de.ims.icarus2.util.collections.MinHeap#compareValuesAt(int, int)
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
	public static class LongHeap extends Heap implements LongConsumer {
		private final long[] values;

		public LongHeap(int size) {
			values = new long[size];
		}

		public long peekValue() {
			if(size<1)
				throw new NoSuchElementException();

			return values[0];
		}

		/**
		 * @see de.ims.icarus2.util.collections.MinHeap#compareValuesAt(int, int)
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
	public static class DoubleHeap extends Heap implements DoubleConsumer {
		private final double[] values;

		public DoubleHeap(int size) {
			values = new double[size];
		}

		public double peekValue() {
			if(size<1)
				throw new NoSuchElementException();

			return values[0];
		}

		/**
		 * @see de.ims.icarus2.util.collections.MinHeap#compareValuesAt(int, int)
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
	public static class ObjectHeap<E extends Object> extends Heap implements Consumer<E> {
		private final Object[] values;
		private final Comparator<? super E> comparator;

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public ObjectHeap(int size) {
			values = new Object[size];
			comparator = (o1, o2) -> ((Comparable)o1).compareTo(o2);
		}

		public ObjectHeap(int size, Comparator<? super E> comparator) {
			requireNonNull(comparator);

			values = new Object[size];
			this.comparator = comparator;
		}

		@SuppressWarnings("unchecked")
		public E peekValue() {
			if(size<1)
				throw new NoSuchElementException();

			return (E) values[0];
		}

		/**
		 * @see de.ims.icarus2.util.collections.MinHeap#compareValuesAt(int, int)
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
