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
 */
package de.ims.icarus2.util.stream;

import static de.ims.icarus2.util.Conditions.checkArgument;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractFencedSpliterator<T extends Object> implements Spliterator<T> {

	/**
	 * Maximum index (exclusive)
	 */
	protected final long fence;

	/**
	 * Current position in the container
	 */
	protected long pos;

	protected AbstractFencedSpliterator(long pos, long fence) {
		checkArgument(pos>=0L);
		checkArgument(fence>pos);

		this.pos = pos;
		this.fence = fence;
	}

	/**
	 * @see java.util.Spliterator#tryAdvance(java.util.function.Consumer)
	 */
	@Override
	public boolean tryAdvance(Consumer<? super T> action) {
		if(pos<fence) {
			action.accept(current());
			pos++;
			return true;
		} else {
			return false;
		}
	}

	protected abstract T current();

	/**
	 * @see java.util.Spliterator#trySplit()
	 */
	@Override
	public Spliterator<T> trySplit() {
		long lo = pos; // divide range in half
		long mid = ((lo + fence) >>> 1) & ~1; // force midpoint to be even
		if (lo < mid) { // split out left half
			pos = mid; // reset this Spliterator's origin
			return split(lo, mid);
		} else {
			// too small to split
			return null;
		}
	}

	protected abstract Spliterator<T> split(long pos, long fence);

	/**
	 * @see java.util.Spliterator#estimateSize()
	 */
	@Override
	public long estimateSize() {
		return fence-pos;
	}

	/**
	 * @see java.util.Spliterator#characteristics()
	 */
	@Override
	public int characteristics() {
		return ORDERED | SIZED | IMMUTABLE | SUBSIZED;
	}

	@Override
	public void forEachRemaining(Consumer<? super T> action) {
		for(;pos<fence;pos++) {
			action.accept(current());
		}
	}

	public static abstract class OfInt extends AbstractFencedSpliterator<Integer> implements Spliterator.OfInt {

		/**
		 * @param pos
		 * @param fence
		 */
		protected OfInt(long pos, long fence) {
			super(pos, fence);
		}

		@Override
		public AbstractFencedSpliterator.OfInt trySplit() {
			return (AbstractFencedSpliterator.OfInt) super.trySplit();
		}

		@Override
		public boolean tryAdvance(IntConsumer action) {
			if(pos<fence) {
				action.accept(currentInt());
				pos++;
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void forEachRemaining(IntConsumer action) {
			for(;pos<fence;pos++) {
				action.accept(currentInt());
			}
		}

		protected abstract int currentInt();

		@Override
		protected Integer current() {
			return Integer.valueOf(currentInt());
		}

	}

	public static abstract class OfLong extends AbstractFencedSpliterator<Long> implements Spliterator.OfLong {

		/**
		 * @param pos
		 * @param fence
		 */
		protected OfLong(long pos, long fence) {
			super(pos, fence);
		}

		@Override
		public AbstractFencedSpliterator.OfLong trySplit() {
			return (AbstractFencedSpliterator.OfLong) super.trySplit();
		}

		@Override
		public boolean tryAdvance(LongConsumer action) {
			if(pos<fence) {
				action.accept(currentLong());
				pos++;
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void forEachRemaining(LongConsumer action) {
			for(;pos<fence;pos++) {
				action.accept(currentLong());
			}
		}

		protected abstract long currentLong();

		@Override
		protected Long current() {
			return Long.valueOf(currentLong());
		}

	}

	public static abstract class OfDouble extends AbstractFencedSpliterator<Double> implements Spliterator.OfDouble {

		/**
		 * @param pos
		 * @param fence
		 */
		protected OfDouble(long pos, long fence) {
			super(pos, fence);
		}

		@Override
		public AbstractFencedSpliterator.OfDouble trySplit() {
			return (AbstractFencedSpliterator.OfDouble) super.trySplit();
		}

		@Override
		public boolean tryAdvance(DoubleConsumer action) {
			if(pos<fence) {
				action.accept(currentDouble());
				pos++;
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void forEachRemaining(DoubleConsumer action) {
			for(;pos<fence;pos++) {
				action.accept(currentDouble());
			}
		}

		protected abstract double currentDouble();

		@Override
		protected Double current() {
			return Double.valueOf(currentDouble());
		}

	}
}
