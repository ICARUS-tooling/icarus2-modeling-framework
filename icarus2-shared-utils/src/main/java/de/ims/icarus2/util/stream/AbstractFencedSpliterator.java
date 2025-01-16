/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.stream;

import static de.ims.icarus2.util.Conditions.checkArgument;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractFencedSpliterator<T extends Object> implements Spliterator<T> {

	protected static final long UNDEFINED_FENCE = IcarusUtils.UNSET_LONG;

	/**
	 * Maximum index (exclusive)
	 */
	protected long fence;

	/**
	 * Current position in the container
	 */
	protected long pos;

	protected AbstractFencedSpliterator(long pos, long fence) {
		checkArgument("pos must be positive or 0",pos>=-1L);
		checkArgument("fence must be greater than pos or undefined (-1)",
				fence==UNDEFINED_FENCE || fence>pos);

		this.pos = pos;
		this.fence = fence;
	}

	protected void updateFence() {
		if(fence==UNDEFINED_FENCE)
			throw new IllegalStateException("Base implementation does not support late adjustment of fence");
	}

	/**
	 * @see java.util.Spliterator#tryAdvance(java.util.function.Consumer)
	 */
	@Override
	public boolean tryAdvance(Consumer<? super T> action) {
		updateFence();

		if(pos<fence) {
			action.accept(current());
			pos++;
			return true;
		}

		return false;
	}

	protected abstract T current();

	/**
	 * @see java.util.Spliterator#trySplit()
	 */
	@Override
	public Spliterator<T> trySplit() {
		updateFence();

		long lo = pos; // divide range in half
		long mid = ((lo + fence) >>> 1) & ~1; // force midpoint to be even
		if (lo < mid) { // split out left half
			pos = mid; // reset this Spliterator's origin
			return split(lo, mid);
		}

		// too small to split
		return null;
	}

	protected abstract Spliterator<T> split(long pos, long fence);

	/**
	 * @see java.util.Spliterator#estimateSize()
	 */
	@Override
	public long estimateSize() {
		updateFence();

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
		updateFence();

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
			updateFence();

			if(pos<fence) {
				action.accept(currentInt());
				pos++;
				return true;
			}

			return false;
		}

		@Override
		public void forEachRemaining(IntConsumer action) {
			updateFence();

			for(;pos<fence;pos++) {
				action.accept(currentInt());
			}
		}

		protected abstract int currentInt();

		@Override
		protected Integer current() {
			return Integer.valueOf(currentInt());
		}

		/**
		 * @see java.util.Spliterator#getComparator()
		 */
		@Override
		public Comparator<? super Integer> getComparator() {
			return Integer::compare;
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
			updateFence();

			if(pos<fence) {
				action.accept(currentLong());
				pos++;
				return true;
			}

			return false;
		}

		@Override
		public void forEachRemaining(LongConsumer action) {
			updateFence();

			for(;pos<fence;pos++) {
				action.accept(currentLong());
			}
		}

		protected abstract long currentLong();

		@Override
		protected Long current() {
			return Long.valueOf(currentLong());
		}

		/**
		 * @see java.util.Spliterator#getComparator()
		 */
		@Override
		public Comparator<? super Long> getComparator() {
			return Long::compare;
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
			updateFence();

			if(pos<fence) {
				action.accept(currentDouble());
				pos++;
				return true;
			}

			return false;
		}

		@Override
		public void forEachRemaining(DoubleConsumer action) {
			updateFence();

			for(;pos<fence;pos++) {
				action.accept(currentDouble());
			}
		}

		protected abstract double currentDouble();

		@Override
		protected Double current() {
			return Double.valueOf(currentDouble());
		}

		/**
		 * @see java.util.Spliterator#getComparator()
		 */
		@Override
		public Comparator<? super Double> getComparator() {
			return Double::compare;
		}
	}
}
