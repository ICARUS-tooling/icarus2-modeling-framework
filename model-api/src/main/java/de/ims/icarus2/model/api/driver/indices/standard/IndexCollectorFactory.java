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
package de.ims.icarus2.model.api.driver.indices.standard;

import static de.ims.icarus2.model.api.driver.indices.IndexUtils.checkSorted;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotNull;
import static de.ims.icarus2.util.Conditions.checkState;
import it.unimi.dsi.fastutil.bytes.ByteIterator;
import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import it.unimi.dsi.fastutil.bytes.ByteSet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortIterator;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexCollector;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.strings.StringUtil;

/**
 * Provides implementations of {@link IndexCollector} and the derived
 * {@link IndexSetBuilder} interface that allow for the efficient collection and
 * aggregation of large numbers of index values.
 *
 * @author Markus Gärtner
 *
 */
public class IndexCollectorFactory implements ModelConstants {

	public static final int UNDEFINED_CHUNK_SIZE = -1;
	public static final long UNDEFINED_TOTAL_SIZE = -1L;
	public static final int DEFAULT_CAPACITY = 1 << 18;

	private Boolean inputSorted; // FIXME add flag for sorted output, since that
									// would affect the way indices are stored
									// in the collector implementation!!!
	private Long totalSizeLimit;
	private Integer chunkSizeLimit;
	private IndexValueType valueType;

	public IndexCollectorFactory inputSorted(boolean inputSorted) {
		checkState(this.inputSorted == null);

		this.inputSorted = Boolean.valueOf(inputSorted);

		return this;
	}

	public boolean isInputSorted() {
		return inputSorted == null ? false : inputSorted.booleanValue();
	}

	public IndexCollectorFactory totalSizeLimit(long totalSizeLimit) {
		checkState(this.totalSizeLimit == null);

		this.totalSizeLimit = Long.valueOf(totalSizeLimit);

		return this;
	}

	public long getTotalSizeLimit() {
		return totalSizeLimit == null ? UNDEFINED_TOTAL_SIZE : totalSizeLimit
				.longValue();
	}

	public IndexCollectorFactory chunkSizeLimit(int chunkSizeLimit) {
		checkState(this.chunkSizeLimit == null);

		this.chunkSizeLimit = Integer.valueOf(chunkSizeLimit);

		return this;
	}

	public int getChunkSizeLimit() {
		return chunkSizeLimit == null ? UNDEFINED_CHUNK_SIZE : chunkSizeLimit.intValue();
	}

	public IndexCollectorFactory valueType(IndexValueType valueType) {
		checkNotNull(valueType);
		checkState(this.valueType == null);

		this.valueType = valueType;

		return this;
	}

	public IndexValueType getValueType() {
		return valueType == null ? IndexValueType.LONG : valueType;
	}

	public IndexSetBuilder create() {

		final IndexValueType valueType = getValueType();
		final boolean sorted = isInputSorted();
		final long totalLimit = getTotalSizeLimit();
		final int chunkLimit = getChunkSizeLimit();

		final boolean isLimited = totalLimit != UNDEFINED_TOTAL_SIZE
				&& totalLimit <= IcarusUtils.MAX_INTEGER_INDEX;
		final int capacity = (int) (isLimited ? Math.min(DEFAULT_CAPACITY,
				totalLimit) : -1);

		IndexSetBuilder builder = null;

		if (sorted) {
			if (isLimited) {
				builder = new LimitedSortedSetBuilder(valueType, capacity, chunkLimit);
			} else {
				builder = new UnlimitedSortedSetBuilder(valueType, chunkLimit);
			}
		} else {
			if (isLimited) {
				switch (valueType) {
				case BYTE:
					builder = new LimitedUnsortedSetBuilderByte(capacity, chunkLimit);
					break;

				case SHORT:
					builder = new LimitedUnsortedSetBuilderShort(capacity, chunkLimit);
					break;

				case INTEGER:
					builder = new LimitedUnsortedSetBuilderInt(capacity, chunkLimit);
					break;

				case LONG:
					builder = new LimitedUnsortedSetBuilderLong(capacity, chunkLimit);
					break;

				}
			} else {
				builder = new BucketSetBuilder(valueType, chunkLimit);
			}
		}

		 if(builder==null)
			 throw new ModelException(GlobalErrorCode.NOT_IMPLEMENTED,
					 "Could not create IndexSetBuilder for factory configuration: "+toString());

//		if (builder == null) {
//			// FIXME for debug reasons we just default to some basic builder
//			// with limited capacity
//			builder = new UnlimitedSortedSetBuilder(IndexValueType.LONG, 1000);
//		}

		return builder;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder().append('[')
				.append(getClass().getSimpleName()).append(" inputSorted=")
				.append(isInputSorted()).append(" totalSizeLimit=")
				.append(getTotalSizeLimit()).append(" chunkSizeLimit=")
				.append(getChunkSizeLimit()).append(" valueType=")
				.append(getValueType()).append(']').toString();
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface IndexSetBuilder extends IndexCollector {
		IndexSet[] build();
	}

	public interface IndexStorage extends IndexCollector {
		void forEach(LongConsumer action);

		void clear();

		int size();

		IndexSet asSet();
	}

	/**
	 * Helper facility that creates an {@link IndexBuffer} with an overridden
	 * sorting behavior:<br>
	 * {@link IndexBuffer#sort()} will do nothing and always return {@code true}
	 * .<br>
	 * {@link IndexBuffer#isSorted()} will always return {@code true}.
	 */
	static IndexBuffer createSortedBuffer(IndexValueType valueType,
			int bufferSize) {
		return new IndexBuffer(valueType, bufferSize) {

			@Override
			public boolean sort() {
				return true;
			}

			@Override
			public boolean isSorted() {
				return true;
			}
		};
	}

	/**
	 * Implements the {@link IndexSetBuilder} interface by wrapping around the
	 * {@link IndexBuffer} class. It assumes every input to be sorted and arrive
	 * in sorted order! In addition, the number of available slots in this
	 * implementations is limited and defined at construction time.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class LimitedSortedSetBuilder extends IndexBuffer implements IndexSetBuilder {

		private final int chunkSize;

		/**
		 * @param valueType
		 * @param bufferSize
		 */
		public LimitedSortedSetBuilder(IndexValueType valueType,
				int bufferSize, int chunkSize) {
			super(valueType, bufferSize);

			this.chunkSize = chunkSize;
		}

		@Override
		public IndexSet[] build() {
			return split(chunkSize);
		}

		@Override
		public boolean sort() {
			return true;
		}

		@Override
		public boolean isSorted() {
			return true;
		}

		@Override
		public void accept(IndexSet indices) {
			checkSorted(indices);

			super.add(indices);
		}

	}

	public static class UnlimitedSortedSetBuilder implements IndexSetBuilder {
		private List<IndexSet> chunks;
		private IndexBuffer buffer;

		private long lastIndex = NO_INDEX;

		public UnlimitedSortedSetBuilder(IndexValueType valueType, int chunkSize) {
			checkNotNull(valueType);

			// Override sorting behavior to reflect the fact that we are
			// checking for sorted input
			// and the storage is never exposed to external code!
			buffer = createSortedBuffer(valueType, chunkSize);
			chunks = new ArrayList<>();
		}

		@Override
		public void add(IndexSet indices) {
			checkSorted(indices);
			checkArgument("Input sequence must be sorted", lastIndex <= indices.firstIndex());

			int cursor = 0;
			int size = indices.size();

			while(cursor<size) {
				int chunkSize = Math.min(size-cursor, buffer.remaining());

				buffer.add(indices, cursor, cursor+chunkSize);
				if(buffer.remaining()==0) {
					chunks.add(buffer.snapshot());
					buffer.clear();
				}

				cursor += chunkSize;
			}
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.IndexCollector#add(long)
		 */
		@Override
		public void add(long index) {
			checkArgument("Input sequence must be sorted", lastIndex <= index);

			buffer.add(index);
			lastIndex = index;

			if (buffer.remaining() == 0) {
				chunks.add(buffer.snapshot());
				buffer.clear();
			}
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.IndexSetBuilder#build()
		 */
		@Override
		public IndexSet[] build() {
			// Consume remaining data
			if (!buffer.isEmpty()) {
				chunks.add(buffer.snapshot());
			}

			IndexSet[] result = new IndexSet[chunks.size()];
			chunks.toArray(result);

			return result;
		}
	}

	/**
	 * Implements a storage with limited but growing capacity. The total
	 * capacity of the storage is limited to {@link Integer#MAX_VALUE} and the
	 * value type used when creating the resulting array of {@link IndexSet}
	 * instances is {@link IndexValueType#LONG}.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class LimitedUnsortedSetBuilderLong implements
			IndexSetBuilder, IndexStorage {
		private final LongSet buffer;
		private final int chunkSize;

		private static final IndexValueType TYPE = IndexValueType.LONG;

		public LimitedUnsortedSetBuilderLong(int capacity, int chunkSize) {
			buffer = new LongOpenHashSet(capacity);

			this.chunkSize = chunkSize;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.IndexCollector#add(long)
		 */
		@Override
		public void add(long index) {
			buffer.add(index);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.IndexSetBuilder#build()
		 */
		@Override
		public IndexSet[] build() {
			IndexSet indices = asSet();

			if (chunkSize == UNDEFINED_CHUNK_SIZE
					|| indices.size() <= chunkSize) {
				return IndexUtils.wrap(indices);
			} else {
				return indices.split(chunkSize);
			}
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.IndexStorage#forEach(java.util.function.LongConsumer)
		 */
		@Override
		public void forEach(LongConsumer action) {
			for(LongIterator it = buffer.iterator(); it.hasNext();) {
				action.accept(it.nextLong());
			}
		}

		@Override
		public void clear() {
			buffer.clear();
		}

		@Override
		public int size() {
			return buffer.size();
		}

		@Override
		public IndexSet asSet() {
			return new ArrayIndexSet(TYPE, buffer.toArray());
		}
	}

	/**
	 * Implements a storage with limited but growing capacity. The total
	 * capacity of the storage is limited to {@link Integer#MAX_VALUE} and the
	 * value type used when creating the resulting array of {@link IndexSet}
	 * instances is {@link IndexValueType#INTEGER}.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class LimitedUnsortedSetBuilderInt implements
			IndexSetBuilder, IndexStorage {
		private final IntSet buffer;
		private final int chunkSize;

		private static final IndexValueType TYPE = IndexValueType.INTEGER;

		public LimitedUnsortedSetBuilderInt(int capacity, int chunkSize) {
			buffer = new IntOpenHashSet(capacity);

			this.chunkSize = chunkSize;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.IndexCollector#add(long)
		 */
		@Override
		public void add(long index) {
			buffer.add((int) TYPE.checkValue(index));
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.IndexSetBuilder#build()
		 */
		@Override
		public IndexSet[] build() {
			IndexSet indices = asSet();

			if (chunkSize == UNDEFINED_CHUNK_SIZE
					|| indices.size() <= chunkSize) {
				return IndexUtils.wrap(indices);
			} else {
				return indices.split(chunkSize);
			}
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.IndexStorage#forEach(java.util.function.LongConsumer)
		 */
		@Override
		public void forEach(LongConsumer action) {
			for(IntIterator it = buffer.iterator(); it.hasNext();) {
				action.accept(it.nextInt());
			}
		}

		@Override
		public void clear() {
			buffer.clear();
		}

		@Override
		public int size() {
			return buffer.size();
		}

		@Override
		public IndexSet asSet() {
			return new ArrayIndexSet(TYPE, buffer.toArray());
		}
	}

	/**
	 * Implements a storage with limited but growing capacity. The total
	 * capacity of the storage is limited to {@link Short#MAX_VALUE} and the
	 * value type used when creating the resulting array of {@link IndexSet}
	 * instances is {@link IndexValueType#SHORT}.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class LimitedUnsortedSetBuilderShort implements
			IndexSetBuilder, IndexStorage {
		private final ShortSet buffer;
		private final int chunkSize;

		private static final IndexValueType TYPE = IndexValueType.SHORT;

		public LimitedUnsortedSetBuilderShort(int capacity, int chunkSize) {
			buffer = new ShortOpenHashSet(capacity);

			this.chunkSize = chunkSize;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.IndexCollector#add(long)
		 */
		@Override
		public void add(long index) {
			buffer.add((short) TYPE.checkValue(index));
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.IndexSetBuilder#build()
		 */
		@Override
		public IndexSet[] build() {
			IndexSet indices = asSet();

			if (chunkSize == UNDEFINED_CHUNK_SIZE
					|| indices.size() <= chunkSize) {
				return IndexUtils.wrap(indices);
			} else {
				return indices.split(chunkSize);
			}
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.IndexStorage#forEach(java.util.function.LongConsumer)
		 */
		@Override
		public void forEach(LongConsumer action) {
			for(ShortIterator it = buffer.iterator(); it.hasNext();) {
				action.accept(it.nextShort());
			}
		}

		@Override
		public void clear() {
			buffer.clear();
		}

		@Override
		public int size() {
			return buffer.size();
		}

		@Override
		public IndexSet asSet() {
			return new ArrayIndexSet(TYPE, buffer.toArray());
		}
	}

	/**
	 * Implements a storage with limited but growing capacity. The total
	 * capacity of the storage is limited to {@link Byte#MAX_VALUE} and the
	 * value type used when creating the resulting array of {@link IndexSet}
	 * instances is {@link IndexValueType#BYTE}.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class LimitedUnsortedSetBuilderByte implements
			IndexSetBuilder, IndexStorage {
		private final ByteSet buffer;
		private final int chunkSize;

		private static final IndexValueType TYPE = IndexValueType.BYTE;

		public LimitedUnsortedSetBuilderByte(int capacity, int chunkSize) {
			buffer = new ByteOpenHashSet(capacity);

			this.chunkSize = chunkSize;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.IndexCollector#add(long)
		 */
		@Override
		public void add(long index) {
			buffer.add((byte) TYPE.checkValue(index));
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.IndexSetBuilder#build()
		 */
		@Override
		public IndexSet[] build() {
			IndexSet indices = asSet();

			if (chunkSize == UNDEFINED_CHUNK_SIZE
					|| indices.size() <= chunkSize) {
				return IndexUtils.wrap(indices);
			} else {
				return indices.split(chunkSize);
			}
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.IndexStorage#forEach(java.util.function.LongConsumer)
		 */
		@Override
		public void forEach(LongConsumer action) {
			for(ByteIterator it = buffer.iterator(); it.hasNext();) {
				action.accept(it.nextByte());
			}
		}

		@Override
		public void clear() {
			buffer.clear();
		}

		@Override
		public int size() {
			return buffer.size();
		}

		@Override
		public IndexSet asSet() {
			return new ArrayIndexSet(TYPE, buffer.toArray());
		}
	}

	/**
	 * A builder implementation based on an AVL tree.
	 * The nodes in the tree are buffers for unsorted chunks of indices.
	 * Each node collects index values and maintains 2 sets of boundary
	 * values:
	 *
	 * An outer pair that describes the ultimate boundary of index values
	 * allowed for that bucket.
	 * And another pair that stores the smallest and largest index values
	 * currently contained in the bucket.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class BucketSetBuilder implements IndexSetBuilder {
		private final IndexValueType valueType;
		private final int chunkSize;
		private final Bucket virtualRoot = new Bucket(){
			@Override
			public boolean isRoot() {return true;};
			@Override
			public Bucket find(long val) {return getSmaller().find(val);};
			@Override
			public void insert(Bucket b) {
				if(getSmaller()==null) {
					setSmaller(b);
				} else {
					getSmaller().insert(b);
				}
			};
			@Override
			public String toString() {return "ROOT";};
		};
		private int size = 1;

		/**
		 * When splitting a bucket we leave the source to be used again. Reduces
		 * memory overhead.
		 */
		private Bucket unusedBucket;

		/**
		 * Points to the bucket used for the last insertion, since we
		 * assume that on average index values come in rather tight clusters
		 */
		private Bucket lastInsertionCache;

		/**
		 * Flag that tells us whether or not we should make use of the
		 * 'lastInsertionCache' hint to potentially speed up insertion time.
		 */
		private final boolean useLastHitCache;

		// RUNTIME STATS
		private long insertions = 0L;
		private long cacheMisses = 0L;
		private long splits = 0L;

		private double averageSplitRatio = 0D;
		private double minSplitRatio = 1D;
		private double maxSplitRation = 0D;

		public BucketSetBuilder(IndexValueType valueType, int chunkSize) {
			this(valueType, chunkSize, false);
		}

		public BucketSetBuilder(IndexValueType valueType, int chunkSize, boolean useLastHitCache) {
			checkNotNull(valueType);
			checkArgument("chunk size must be positive", chunkSize > 0);

			this.valueType = valueType;
			this.chunkSize = chunkSize;
			this.useLastHitCache = useLastHitCache;

			virtualRoot.setSmaller(createBucket());
		}

		public long getInsertions() {
			return insertions;
		}

		public long getCacheMisses() {
			return cacheMisses;
		}

		public long getSplitCount() {
			return splits;
		}

		public double getAverageSplitRatio() {
			return averageSplitRatio;
		}

		public double getMinSplitRatio() {
			return minSplitRatio;
		}

		public double getMaxSplitRation() {
			return maxSplitRation;
		}

		public int getBucketCount() {
			return size;
		}

		private Bucket getBucket() {
			Bucket b;
			if(unusedBucket!=null) {
				b = unusedBucket;
				unusedBucket = null;
			} else {
				b = createBucket();
			}

			return b;
		}

		private Bucket createBucket() {
			IndexStorage storage = null;

			switch (valueType) {
			case LONG:
				storage = new LimitedUnsortedSetBuilderLong(chunkSize,
						chunkSize);
				break;

			case INTEGER:
				storage = new LimitedUnsortedSetBuilderInt(chunkSize, chunkSize);
				break;

			case SHORT:
				storage = new LimitedUnsortedSetBuilderShort(chunkSize,
						chunkSize);
				break;

			case BYTE:
				storage = new LimitedUnsortedSetBuilderByte(chunkSize,
						chunkSize);
				break;

			default:
				throw new IllegalStateException(
						"Unsupported index value type: " + valueType);
			}

			return new Bucket(storage);
		}

		private void destroy(Bucket b) {
			b.reset();

			unusedBucket = b;
		}

		private Bucket root() {
			return virtualRoot.getSmaller();
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.IndexCollector#add(long)
		 */
		@Override
		public void add(long index) {
			Bucket b = find(index);
			add(b, index);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.IndexCollector#add(de.ims.icarus2.model.api.driver.indices.IndexSet)
		 */
		@Override
		public void add(IndexSet indices) {
			add(indices, 0, indices.size());
		}

		/**
		 * Returns a <i>pseudo-sorted</i> sequence of {@link IndexSet
		 * index-sets}. The returned sets will be sorted according to the
		 * contract for index set arrays:<br>
		 * For any sets s<sub>1</sub> and s<sub>2</sub> it is guaranteed that
		 * s<sub>1</sub>.lastIndex() < s<sub>2</sub>.firstIndex()<br>
		 * No guarantees are made regarding sorting of index values within
		 * individual sets!
		 *
		 * @see de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.IndexSetBuilder#build()
		 */
		@Override
		public IndexSet[] build() {
			IndexSet[] result = new IndexSet[size];

			collectSets(root(), result, 0);

			return result;
		}

		/**
		 * Collect the content of buckets into a index set buffer. Does an
		 * in-order traversal of the tree denoted by the given bucket. The
		 * return value is the index value to be used for further insertion into
		 * the buffer AFTER this tree has been processed.
		 *
		 * @return the index of the next free slot in the provided {@code buffer}
		 * 			that should be used for insertion.
		 */
		private int collectSets(Bucket b, IndexSet[] buffer, int index) {
			if (b.getSmaller() != null) {
				index = collectSets(b.getSmaller(), buffer, index);
			}

			buffer[index++] = b.storage.asSet();

			if (b.getLarger() != null) {
				index = collectSets(b.getLarger(), buffer, index);
			}

			return index;
		}

		private void add(IndexSet indices, int offset, int len) {
			// TODO implementation depends on whether or not given IndexSet is
			// sorted ?!

			for(int i=0; i<len; i++) {
				add(indices.indexAt(offset+i));
			}
		}

		/**
		 * Searches for the correct bucket that is responsible for storing the given
		 * index value {@code v}. If {@code useLastHitCache} is set then this method
		 * will do a fast check on the bucket used for the last insertion which can
		 * speed search time quite a bit for (partially) sorted input data.
		 * If no bucket has been cached or the cached bucket has produced a cache miss
		 * the further search is forwarded to the {@link #find(Bucket, long)} method
		 * which will do a binary tree search.
		 */
		private Bucket find(long v) {
			Bucket b = null;
			if(useLastHitCache && lastInsertionCache!=null) {
				if(lastInsertionCache.isLegalValue(v)) {
					b = lastInsertionCache;
				} else {
					cacheMisses++;
				}
			}

			// If no caching is done or we got a cache miss, do regular tree search
			if(b==null){
				b = find(root(), v);
			}

			return b;
		}

		/**
		 * Binary search method for finding the bucket that is responsible for storing
		 * the given index value {@code v}. The search is initiated at the given {@code root}
		 * bucket.
		 * If no bucket could be found for the given value, this method throws
		 * {@link IllegalStateException}.
		 */
		private Bucket find(Bucket b, long v) {
			while (b != null) {
				if (v < b.getLeft()) {
					b = b.getSmaller();
				} else if (v > b.getRight()) {
					b = b.getLarger();
				} else {
					return b;
				}
			}

			throw new IllegalStateException("No bucket for value: " + v);
		}

		private void add(Bucket b, long v) {
			int capacity = capacity(b);

			if(capacity==0) {
				split(b);
				b = find(v);
			}

			b.add(v);
			lastInsertionCache = b;
			insertions++;
		}

		private void split(Bucket b) {
			if(lastInsertionCache==b) {
				lastInsertionCache = null;
			}

//			System.out.println("------------------------------------------------------------------");
//			System.out.println("BEFORE  "+toString());
//			System.out.println("SPLITTING "+b);
			double totalCount = b.storage.size();

			// Fetch 2 new/empty buckets
			Bucket left = getBucket();
			Bucket right = getBucket();

			// Split content of old bucket into the enw ones
			distribute(b, left, right);

			// Remove and recycle old bucket
			b.remove();
			destroy(b);

			// Instead of some local magic we just use the default isnertion methods
			virtualRoot.insert(left);
			virtualRoot.insert(right);

			// Collect some meta info
			size++;
			splits++;

			double leftCount = left.storage.size();
			double ratio = leftCount/totalCount;

			averageSplitRatio = ((averageSplitRatio * (splits-1)) + ratio) / splits;
			minSplitRatio = Math.min(minSplitRatio, ratio);
			maxSplitRation = Math.max(maxSplitRation, ratio);
//			System.out.println("AFTER  "+toString());
		}

		private int capacity(Bucket b) {
			return chunkSize-b.storage.size();
		}

		/**
		 * Tries to find a good pivot element for splitting the content of bucket {@code b}.
		 * This method simply returns the arithmetic middle of the bucket's {@link Bucket#min}
		 * and {@link Bucket#max} values.
		 */
		private long pivot(Bucket b) {
			// TODO improve pivot estimation
			return b.min + ((b.max - b.min) >>> 1);
		}

		private void distribute(Bucket source, Bucket left, Bucket right) {
			long pivot = pivot(source);

//			System.out.println("PIVOT "+pivot);

			left.setLeft(source.getLeft());
			left.setRight(pivot);

			right.setLeft(pivot+1);
			right.setRight(source.getRight());

			source.storage.forEach(val -> {
				if (val <= pivot) {
					left.add(val);
				} else {
					right.add(val);
				}
			});
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			appendBucket(sb, virtualRoot);

			return sb.toString();
		}

		public void printStats(PrintStream out) {
			out.println("INSERTIONS:          "+StringUtil.formatDecimal(insertions));
			out.println("CACHE MISSES:        "+StringUtil.formatDecimal(cacheMisses));
			out.println("SPLITS:              "+StringUtil.formatDecimal(splits));
			out.println("AVERAGE SPLIT RATIO: "+StringUtil.formatDecimal(averageSplitRatio));
			out.println("MIN SPLIT RATIO:     "+StringUtil.formatDecimal(minSplitRatio));
			out.println("MAX SPLIT RATIO:     "+StringUtil.formatDecimal(maxSplitRation));
		}

		private void appendBucket(StringBuilder sb, Bucket b) {
			if(b==null) {
				return;
			}

			sb.append('[');
			appendBucket(sb, b.smaller);

			if(b.isRoot()) {
				sb.append(' ').append(b).append(' ');
			} else {
//				MutableBoolean added = new MutableBoolean(false);
//
//				b.storage.forEach(v -> {
//					if(added.booleanValue()) {
//						sb.append(',');
//					}
//					sb.append(v);
//
//					added.setBoolean(true);
//				});
				sb.append(b);
			}

			appendBucket(sb, b.larger);
			sb.append(']');
		}

	}

	/**
	 * Implementation note:
	 *
	 * Size property removed - it is implicitly available through the storage
	 * object!
	 *
	 *
	 * @author Markus Gärtner
	 *
	 */
	private static class Bucket implements Comparable<Bucket> {

		private static final long DEFAULT_MIN = Long.MIN_VALUE;
		private static final long DEFAULT_MAX = Long.MAX_VALUE;

		Bucket(IndexStorage storage) {
			this.storage = storage;
		}

		Bucket() {
			this(null);
		}

//		private int balance = 0;

		/**
		 * Type specific storage storage
		 */
		private IndexStorage storage;

		/**
		 * Smallest existing value in bucket
		 */
		private long min = DEFAULT_MAX;
		/**
		 * Largest existing value in bucket
		 */
		private long max = DEFAULT_MIN;

		/**
		 * Smallest allowed value for bucket
		 */
		private long left = DEFAULT_MIN;
		/**
		 * Largest allowed value for bucket
		 */
		private long right = DEFAULT_MAX;

		/**
		 * Uplink
		 */
		private Bucket parent;
		/**
		 * Child node containing index values smaller than {@link #left}
		 */
		private Bucket smaller;
		/**
		 * Child node containing index values larger than {@link #right}
		 */
		private Bucket larger;

		/**
		 * Height of tree, stored as a lazy value.
		 * A height value of {@code -1} indicates that the height of this subtree
		 * is not known and that it has to be calculated before retrieval.
		 */
		private int height = -1;

		public void add(long v) {
			storage.add(v);

			if (min==DEFAULT_MIN || v < min) {
				min = v;
			}
			if (max==DEFAULT_MAX || v > max) {
				max = v;
			}
		}

		private void checkNotRoot() {
			if(isRoot())
				throw new UnsupportedOperationException();
		}

		public boolean isRoot() {
			return false;
		}

		public boolean isLegalValue(long v) {
			return v>=left && v<=right;
		}

		public long getMin() {
			return min;
		}

		public long getMax() {
			return max;
		}

		public long getLeft() {
			return left;
		}

		public long getRight() {
			return right;
		}

		public void setLeft(long left) {
			this.left = left;
		}

		public void setRight(long right) {
			this.right = right;
		}

		public boolean isLeftChild() {
			checkNotRoot();

			return parent.smaller==this;
		}

		public boolean isRightChild() {
			checkNotRoot();

			return parent.larger==this;
		}

		public Bucket getParent() {
			return parent;
		}

		public Bucket getSmaller() {
			return smaller;
		}

		public Bucket getLarger() {
			return larger;
		}

		public void setParent(Bucket parent) {
			checkNotNull(parent);
			checkArgument(parent!=this);

			this.parent = parent;
		}

		public void setSmaller(Bucket smaller) {
			checkArgument(smaller!=this);

			this.smaller = smaller;
			if(this.smaller!=null) {
				this.smaller.setParent(this);
			}

			invalidate();
		}

		public void setLarger(Bucket larger) {
			checkArgument(larger!=this);

			this.larger = larger;
			if(this.larger!=null) {
				this.larger.setParent(this);
			}

			invalidate();
		}

		public void setChild(Bucket child, boolean isLeft) {
			if(isLeft) {
				setSmaller(child);
			} else {
				setLarger(child);
			}
		}

		private static Bucket find(Bucket b, long v) {
			while (b != null) {
				if (v < b.left) {
					b = b.smaller;
				} else if (v > b.right) {
					b = b.larger;
				} else {
					return b;
				}
			}

			throw new IllegalStateException("No bucket for value: " + v);
		}

		public Bucket find(long val) {
			return find(this, val);
		}

		private static Bucket treeMinimum(Bucket b) {
			while (b.smaller != null)
				b = b.smaller;

			return b;
		}

		private static Bucket treeMaximum(Bucket b) {
			while (b.larger != null)
				b = b.larger;

			return b;
		}

		private static Bucket predecessor(Bucket b) {
			Bucket x = b;

			if (x.smaller != null)
				return treeMaximum(x.smaller);

			return null;
		}

		private static Bucket successor(Bucket b) {
			Bucket x = b;

			if (x.larger != null)
				return treeMinimum(x.larger);

			return null;
		}

		// Structural modification

		private static void insert(Bucket b, Bucket anchor) {

			boolean inserted = false;

			while(!inserted) {
				int comp = b.compareTo(anchor);

				if(comp<0) {
					if(anchor.smaller==null) {
						anchor.setSmaller(b);
						inserted = true;
					} else {
						anchor = anchor.smaller;
					}
				} else {
					if(anchor.larger==null) {
						anchor.setLarger(b);
						inserted = true;
					} else {
						anchor = anchor.larger;
					}
				}
			}

//			System.out.println("INSERTED "+b);

			rebalance(anchor);
		}

		private static void rebalance(Bucket b) {

			while (b != null && !b.isRoot()) {
				int balance = b.heightDiff();

				if (balance == -2) {
					if (height(b.smaller.smaller) >= height(b.smaller.larger)) {
						b = rotateRight(b);
					} else {
						b = rotateLeftThenRight(b);
					}

				} else if (balance == 2) {
					if (height(b.larger.larger) >= height(b.larger.smaller)) {
						b = rotateLeft(b);
					} else {
						b = rotateRightThenLeft(b);
					}
				}

				b = b.parent;
			}
		}

		public void insert(Bucket b) {
			insert(b, this);
		}

		/*
		 * Let node X be the node with the value we need to delete, and let node Y be a node in the tree
		 * we need to find to take node X's place, and let node Z be the actual node we take out of the tree.
		 * Steps to consider when deleting a node in an AVL tree are the following:
		 *
    	 * 1. If node X is a leaf or has only one child, skip to step 5 with Z:=X.
    	 * 2. Otherwise, determine node Y by finding the largest node in node X's left subtree (the in-order predecessor of X − it does not have a right child) or the smallest in its right subtree (the in-order successor of X − it does not have a left child).
    	 * 3. Exchange all the child and parent links of node X with those of node Y. In this step, the in-order sequence between nodes X and Y is temporarily disturbed, but the tree structure doesn't change.
    	 * 4. Choose node Z to be all the child and parent links of old node Y = those of new node X.
    	 * 5. If node Z has a subtree (which then is a leaf), attach it to Z's parent.
    	 * 6. If node Z was the root (its parent is null), update root.
    	 * 7. Delete node Z.
    	 * 8. Retrace the path back up the tree (starting with node Z's parent) to the root, adjusting the balance factors as needed.
		 *
		 */
		private static void remove(Bucket b) {

//			System.out.println("REMOVE "+b);

			Bucket x;

			if(b.smaller==null) { // No left child
				x = b.larger;
			} else if(b.larger==null) { // No right child
				x = b.smaller;
			} else { // Both children
				// Replacement for b
				x = predecessor(b);
				// Remove x from its current position
				remove(x);

				// Anchor b's subtrees on x
				x.setSmaller(b.smaller);
				x.setLarger(b.larger);
			}

			Bucket parent = b.getParent();

			// Refresh tree structure above b
			if(b.isLeftChild()) {
				parent.setSmaller(x);
			} else {
				parent.setLarger(x);
			}

			rebalance(parent);
		}

		public void remove() {
			checkNotRoot();

			remove(this);
		}

		// Balancing

		private static int height(Bucket b) {
			return b==null ? 0 : b.getHeight();
		}

		public int getHeight() {

			if(height==-1) {
				height = 1 + Math.max(height(smaller), height(larger));
			}

			return height;
		}

		public int heightDiff() {
			return height(larger) - height(smaller);
		}

		/**
		 * Walk the parent chain up to the root or first node with an
		 * invalidated height and invalidate nodes on the path.
		 */
		public void invalidate() {
			invalidate(this);
		}

		private static void invalidate(Bucket b) {
			while(b!=null && b.height!=-1) {
				b.height = -1;
				b = b.parent;
			}
		}

		private void reset() {
			parent = smaller = larger = null;
			min = right = DEFAULT_MAX;
			max = left = DEFAULT_MIN;
			height = -1;
			storage.clear();
		}

		/**
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(Bucket o) {
			return Long.compare(left, o.left);
		}

		private static String text(long v) {
			if(v==DEFAULT_MAX) {
				return "++";
			} else if(v==DEFAULT_MIN) {
				return "--";
			}
			return String.valueOf(v);
		}

		public static Bucket rotateRight(Bucket root) {
//			System.out.println("ROTATE RIGHT "+virtualRoot);

			// Keep info about position relative to parent
			Bucket parent = root.getParent();
			boolean isLeftChild = root.isLeftChild();

			Bucket leftChild = root.getSmaller();
			root.setSmaller(leftChild.getLarger());
			leftChild.setLarger(root);

			parent.setChild(leftChild, isLeftChild);

			return leftChild;
		}

		public static Bucket rotateLeft(Bucket root) {
//			System.out.println("ROTATE LEFT "+virtualRoot);

			// Keep info about position relative to parent
			Bucket parent = root.getParent();
			boolean isLeftChild = root.isLeftChild();

			Bucket rightChild = root.getLarger();
			root.setLarger(rightChild.getSmaller());
			rightChild.setSmaller(root);

			parent.setChild(rightChild, isLeftChild);

			return rightChild;
		}

		public static Bucket rotateLeftThenRight(Bucket root) {
			root.setSmaller(rotateLeft(root.getSmaller()));
			return rotateRight(root);
		}

		public static Bucket rotateRightThenLeft(Bucket root) {
			root.setLarger(rotateRight(root.getLarger()));
			return rotateLeft(root);
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("{%s (%s,%s) b=%d s=%d %s}", text(left), text(min), text(max), heightDiff(), storage.size(), text(right));
		}
	}
}
