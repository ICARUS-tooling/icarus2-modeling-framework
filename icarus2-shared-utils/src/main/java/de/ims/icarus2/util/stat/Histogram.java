/**
 *
 */
package de.ims.icarus2.util.stat;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.IcarusUtils.MAX_INTEGER_INDEX;
import static de.ims.icarus2.util.IcarusUtils.ensureIntegerValueRange;

import java.util.Arrays;
import java.util.function.LongConsumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;

/**
 * @author Markus Gärtner
 *
 */
public interface Histogram extends LongConsumer {

	/** Returns total number of bins in this histogram. */
	int bins();

	/** Returns the lower bound for values in the specified {@code bin}. */
	long lowerBound(int bin);

	/** Returns the lower bound for values in the specified {@code bin}. */
	long higherBound(int bin);

	/** Returns the total number of entries in this entire histogram. */
	long entries();

	/** Returns the frequency of the specified {@code bin}. */
	long freq(int bin);

	int bin(long value);

	/** Computes the average for all values in this histogram and returns it. If
	 * such value could not be computed (e.g. for lack of entries) the method
	 * should return {@link Double#NaN}.
	 * @return
	 */
	double average();

	default long min() {
		return lowerBound(0);
	}

	default long max() {
		return higherBound(bins()-1);
	}

	//TODO add method for average and percentile retrieval

	public static Histogram fixedHistogram(int capacity) {
		return ArrayHistogram.fixed(capacity);
	}

	public static Histogram rangedHistogram(int capacity) {
		return ArrayHistogram.fixed(capacity);
	}

	public static class ArrayHistogram implements Histogram, LongConsumer {

		public static ArrayHistogram fixed(int capacity) {
			return new ArrayHistogram(capacity, 0, true);
		}

		public static ArrayHistogram fixed(int capacity, long offset) {
			return new ArrayHistogram(capacity, offset, true);
		}

		public static ArrayHistogram range(long lowerBound, long upperBound) {
			checkArgument(lowerBound<=upperBound);
			long range = upperBound-lowerBound+1;
			return new ArrayHistogram(ensureIntegerValueRange(range), lowerBound, true);
		}

		public static ArrayHistogram open(int initialCapacity) {
			return new ArrayHistogram(initialCapacity, 0, false);
		}

		public static ArrayHistogram open(int initialCapacity, long offset) {
			return new ArrayHistogram(initialCapacity, offset, false);
		}

		/** Buffer for frequencies */
		private int[] bins;
		/** FLag to indicate if the bins array is allowed to grow */
		private boolean fixed;
		/** Total number of samples contained */
		private long entries;
		/** Total sum of all sampled values */
		private long sum;
		/** Offset to be applied when determining the bin for a given value */
		private long offset;

		private ArrayHistogram(int capacity, long offset, boolean fixed) {
			this.bins = new int[capacity];
			this.offset = offset;
			this.fixed = fixed;
		}

		/**
		 * @see de.ims.icarus2.util.stat.Histogram#bins()
		 */
		@Override
		public int bins() {
			return bins.length;
		}

		/**
		 * @see de.ims.icarus2.util.stat.Histogram#lowerBound(int)
		 */
		@Override
		public long lowerBound(int bin) {
			return offset + bin;
		}

		/**
		 * {@inheritDoc}
		 *
		 * This implementation defaults to {@link #lowerBound(int)}
		 *
		 * @see de.ims.icarus2.util.stat.Histogram#higherBound(int)
		 */
		@Override
		public long higherBound(int bin) {
			return lowerBound(bin);
		}

		/**
		 * @see de.ims.icarus2.util.stat.Histogram#entries()
		 */
		@Override
		public long entries() {
			return entries;
		}

		/**
		 * @see de.ims.icarus2.util.stat.Histogram#freq(int)
		 */
		@Override
		public long freq(int bin) {
			return bins[bin];
		}

		/**
		 * @see de.ims.icarus2.util.stat.Histogram#average()
		 */
		@Override
		public double average() {
			return entries==0L ? Double.NaN : (double) sum / entries;
		}

		private int rawBin(long v) {
			long adjusted = v-offset;
			ensureIntegerValueRange(adjusted);
			if(adjusted<0)
				throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT,
						"Given value is below the minimum offset: "+v);
			return (int) v;
		}

		/**
		 * @see de.ims.icarus2.util.stat.Histogram#bin(long)
		 */
		@Override
		public int bin(long value) {
			int rawBin = rawBin(value);
			if(rawBin>=bins.length)
				throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT,
						"Given value exceeds bin count: "+value);
			return rawBin;
		}

		/**
		 * @see java.util.function.LongConsumer#accept(long)
		 */
		@Override
		public void accept(long value) {
			int rawBin = rawBin(value);
			if(rawBin>=bins.length && !fixed) {
				int newCapacity = Math.max(rawBin, bins.length * 2);
				if(newCapacity<0) { // overflow-conscious
					newCapacity = MAX_INTEGER_INDEX;
				}
				bins = Arrays.copyOf(bins, newCapacity);
			}
			bins[rawBin]++;
			sum += value;
			entries++;
		}

		public ArrayHistogram add(long value) {
			accept(value);
			return this;
		}
	}
}
