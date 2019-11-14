/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.driver.indices.standard;

import static de.ims.icarus2.model.api.driver.indices.IndexUtils.checkNotNegative;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.checkSorted;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.ensureSorted;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.isSorted;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.lang.Primitives._int;
import static java.util.Objects.requireNonNull;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexCollector;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.strings.StringUtil;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;


/**
 * Provides implementations of {@link IndexCollector} and the derived
 * {@link IndexSetBuilder} interface that allow for the efficient collection and
 * aggregation of large numbers of index values.
 * <p>
 * Implementation notes:<br>
 * When setting a {@link #chunkSizeLimit(int) chunk size limit}, keep in mind that
 * this only provides a guarantee for the <b>maximum</b> size of each individual {@link IndexSet}
 * returned from {@link IndexSetBuilder#build()}!
 *
 * @author Markus Gärtner
 *
 */
public class IndexCollectorFactory {

	private static final Logger log = LoggerFactory.getLogger(IndexCollectorFactory.class);

	public static final int UNDEFINED_CHUNK_SIZE = UNSET_INT;
	public static final long UNDEFINED_TOTAL_SIZE = UNSET_LONG;
	public static final int DEFAULT_CAPACITY = 1 << 18;

	private static void checkDiscouragedValueType(IndexValueType indexValueType) {
		if(indexValueType==IndexValueType.BYTE)
			log.warn(String.format("Use of %s.BYTE is discouraged here, use the SHORT enum!",
					IndexValueType.class.getName()));
	}

	private Boolean inputSorted;
	private Boolean outputSorted;

	private Long totalSizeLimit;
	private Integer chunkSizeLimit;
	private IndexValueType valueType;

	public IndexCollectorFactory inputSorted(boolean inputSorted) {
		checkState(this.inputSorted == null);

		this.inputSorted = Boolean.valueOf(inputSorted);

		return this;
	}

	public IndexCollectorFactory outputSorted(boolean outputSorted) {
		checkState(this.outputSorted == null);

		this.outputSorted = Boolean.valueOf(outputSorted);

		return this;
	}

	public boolean isInputSorted() {
		return inputSorted == null ? false : inputSorted.booleanValue();
	}

	public boolean isOutputSorted() {
		return outputSorted == null ? false : outputSorted.booleanValue();
	}

	public IndexCollectorFactory totalSizeLimit(long totalSizeLimit) {
		checkArgument("Size limit must be positive: "+totalSizeLimit, totalSizeLimit>0);
		checkState(this.totalSizeLimit == null);

		this.totalSizeLimit = Long.valueOf(totalSizeLimit);

		return this;
	}

	public long getTotalSizeLimit() {
		return totalSizeLimit == null ? UNDEFINED_TOTAL_SIZE : totalSizeLimit
				.longValue();
	}

	public IndexCollectorFactory chunkSizeLimit(int chunkSizeLimit) {
		checkArgument("Chunk size limit must be positive: "+chunkSizeLimit, chunkSizeLimit>0);
		checkState(this.chunkSizeLimit == null);

		this.chunkSizeLimit = Integer.valueOf(chunkSizeLimit);

		return this;
	}

	public int getChunkSizeLimit() {
		return chunkSizeLimit == null ? UNDEFINED_CHUNK_SIZE : chunkSizeLimit.intValue();
	}

	public IndexCollectorFactory valueType(IndexValueType valueType) {
		requireNonNull(valueType);
		checkState(this.valueType == null);

		checkDiscouragedValueType(valueType);

		this.valueType = valueType;

		return this;
	}

	public IndexValueType getValueType() {
		return valueType == null ? IndexValueType.LONG : valueType;
	}

	/**
	 * Applies all the specified settings and creates an instance of {@link IndexSetBuilder}
	 * that best fits the desired characteristics.
	 *
	 * @return a new {@link IndexSetBuilder}, never {@code null}
	 */
	public IndexSetBuilder create() {

		final IndexValueType valueType = getValueType();
		final boolean inputSorted = isInputSorted();
		final boolean outputSorted = isOutputSorted();
		final long totalLimit = getTotalSizeLimit();
		final int chunkLimit = getChunkSizeLimit();

		final boolean isLimited = totalLimit != UNDEFINED_TOTAL_SIZE
				&& totalLimit <= IcarusUtils.MAX_INTEGER_INDEX;
		final int capacity = (int) (isLimited ? Math.min(DEFAULT_CAPACITY,
				totalLimit) : UNSET_INT);

		IndexSetBuilder builder = null;

		if (inputSorted) {
			if (isLimited) {
				builder = new LimitedSortedSetBuilder(valueType, capacity, chunkLimit);
			} else {
				builder = new UnlimitedSortedSetBuilder(valueType, chunkLimit);
			}
		} else {
			if (isLimited) {
				switch (valueType) {

				// No reason to have a special case for BYTE
				case BYTE:
				case SHORT:
					builder = new LimitedUnsortedSetBuilderShort(capacity, chunkLimit, outputSorted);
					break;

				case INTEGER:
					builder = new LimitedUnsortedSetBuilderInt(capacity, chunkLimit, outputSorted);
					break;

				case LONG:
					builder = new LimitedUnsortedSetBuilderLong(capacity, chunkLimit, outputSorted);
					break;

				default:
					 throw new ModelException(GlobalErrorCode.NOT_IMPLEMENTED,
							 "Value type not supported: "+valueType);

				}
			} else {
				builder = new BucketSetBuilder(valueType, chunkLimit, outputSorted);
			}
		}

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
	private static IndexBuffer createSortedBuffer(IndexValueType valueType,
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

	private static void checkChunkSize(int chunkSize) {
		if(chunkSize!=UNDEFINED_CHUNK_SIZE && chunkSize<1)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Chunk size must not be negative (unless -1 as UNDEFINED marker): "+chunkSize);
	}

	private static void checkCapacity(int capacity) {
		if(capacity<1)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Capacity must be positive: "+capacity);
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
	public static class LimitedSortedSetBuilder implements IndexSetBuilder {

		private final IndexBuffer buffer;
		private final int chunkSize;

		private long lastIndex = UNSET_LONG;

		/**
		 * @param valueType
		 * @param bufferSize
		 */
		public LimitedSortedSetBuilder(IndexValueType valueType,
				int bufferSize, int chunkSize) {
			checkChunkSize(chunkSize);
			buffer = createSortedBuffer(valueType, bufferSize);
			this.chunkSize = chunkSize;
		}

		@Override
		public IndexSet[] build() {
			if(chunkSize==UNDEFINED_CHUNK_SIZE
					|| chunkSize>=buffer.size()) {
				return IndexUtils.wrap(buffer);
			}

			return buffer.split(chunkSize);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.IndexCollector#add(long)
		 */
		@Override
		public void add(long index) {
			checkNotNegative(index);
			checkSorted(lastIndex, index);
			buffer.add(index);
			lastIndex = index;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.IndexCollector#add(de.ims.icarus2.model.api.driver.indices.IndexSet)
		 */
		@Override
		public void add(IndexSet indices) {
			checkSorted(indices);
			checkSorted(lastIndex, indices.firstIndex());
			buffer.add(indices);
			lastIndex = indices.lastIndex();
		}
	}

	/**
	 * An implementation of {@link IndexSetBuilder} that uses a {@link List}
	 * of {@link IndexSet} objects and an {@link IndexBuffer} as internal buffer.
	 * All index values (provided individually or as {@code IndexSet}s) <b>must</b>
	 * be sorted!
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class UnlimitedSortedSetBuilder implements IndexSetBuilder {
		private List<IndexSet> chunks;
		private IndexBuffer buffer;

		private long lastIndex = IcarusUtils.UNSET_LONG;

		public UnlimitedSortedSetBuilder(IndexValueType valueType, int chunkSize) {
			requireNonNull(valueType);
			checkChunkSize(chunkSize);

			checkDiscouragedValueType(valueType);

			// Override sorting behavior to reflect the fact that we are
			// checking for sorted input
			// and the storage is never exposed to external code!
			buffer = createSortedBuffer(valueType, chunkSize);
			chunks = new ArrayList<>(1024);
		}

		@Override
		public void add(IndexSet indices) {
			checkSorted(indices);
			checkSorted(lastIndex, indices.firstIndex());

			int cursor = 0;
			int size = indices.size();

			while(cursor<size) {
				int chunkSize = Math.min(size-cursor, buffer.remaining());

				buffer.add(indices, cursor, cursor+chunkSize);
				lastIndex = buffer.lastIndex();

				maybeWrapupChunk();

				cursor += chunkSize;
			}
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.IndexCollector#add(long)
		 */
		@Override
		public void add(long index) {
			checkNotNegative(index);
			checkSorted(lastIndex, index);

			buffer.add(index);
			lastIndex = index;

			maybeWrapupChunk();
		}

		private void maybeWrapupChunk() {
			if (buffer.remaining() == 0) {
				wrapupChunk();
			}
		}

		private void wrapupChunk() {
			chunks.add(buffer.snapshot());
			buffer.clear();
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.IndexSetBuilder#build()
		 */
		@Override
		public IndexSet[] build() {
			// Consume remaining data
			if (!buffer.isEmpty()) {
				wrapupChunk();
			}

			IndexSet[] result = new IndexSet[chunks.size()];
			chunks.toArray(result);

			assert isSorted(result);

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
		private final boolean sortResult;

		private static final IndexValueType TYPE = IndexValueType.LONG;

		public LimitedUnsortedSetBuilderLong(int capacity, int chunkSize, boolean sortResult) {
			checkCapacity(capacity);
			checkChunkSize(chunkSize);
			buffer = new LongOpenHashSet(capacity);

			this.chunkSize = chunkSize;
			this.sortResult = sortResult;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.IndexCollector#add(long)
		 */
		@Override
		public void add(long index) {
			checkNotNegative(index);
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
			}

			return indices.split(chunkSize);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.IndexStorage#forEach(java.util.function.LongConsumer)
		 */
		@Override
		public void forEach(LongConsumer action) {
			buffer.forEach(action);
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
			IndexSet indices = new ArrayIndexSet(TYPE, buffer.toLongArray());
			if(sortResult) {
				ensureSorted(indices);
			}
			return indices;
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
		private final boolean sortResult;

		private static final IndexValueType TYPE = IndexValueType.INTEGER;

		public LimitedUnsortedSetBuilderInt(int capacity, int chunkSize, boolean sortResult) {
			checkCapacity(capacity);
			checkChunkSize(chunkSize);
			buffer = new IntOpenHashSet(capacity);

			this.chunkSize = chunkSize;
			this.sortResult = sortResult;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.IndexCollector#add(long)
		 */
		@Override
		public void add(long index) {
			checkNotNegative(index);
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
			}

			return indices.split(chunkSize);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.IndexStorage#forEach(java.util.function.LongConsumer)
		 */
		@Override
		public void forEach(LongConsumer action) {
			buffer.forEach((IntConsumer)v -> action.accept(v));
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
			IndexSet indices = new ArrayIndexSet(TYPE, buffer.toIntArray());
			if(sortResult) {
				ensureSorted(indices);
			}
			return indices;
		}
	}

	/**
	 * Implements a storage with limited but growing capacity. The total
	 * capacity of the storage is limited to {@link Integer#MAX_VALUE} and the
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
		private final boolean sortResult;

		private static final IndexValueType TYPE = IndexValueType.SHORT;

		public LimitedUnsortedSetBuilderShort(int capacity, int chunkSize, boolean sortResult) {
			checkCapacity(capacity);
			checkChunkSize(chunkSize);
			buffer = new ShortOpenHashSet(capacity);

			this.chunkSize = chunkSize;
			this.sortResult = sortResult;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.IndexCollector#add(long)
		 */
		@Override
		public void add(long index) {
			checkNotNegative(index);
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
			}

			return indices.split(chunkSize);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.IndexStorage#forEach(java.util.function.LongConsumer)
		 */
		@Override
		public void forEach(LongConsumer action) {
			buffer.forEach((IntConsumer)v -> action.accept(v));
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
			IndexSet indices = new ArrayIndexSet(TYPE, buffer.toShortArray());
			if(sortResult) {
				ensureSorted(indices);
			}
			return indices;
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
		/** Size of individual chunks, used as buffer size of buckets */
		private final int chunkSize;
		private final boolean sortResult;
		/** Root node to make rotations easier */
		private final Bucket virtualRoot = new Bucket(){
			@Override
			public boolean isRoot() {return true;}
			@Override
			public Bucket find(long val) {return getSmaller().find(val);}
			@Override
			public void insert(Bucket b) {
				if(getSmaller()==null) {
					setSmaller(b);
				} else {
					getSmaller().insert(b);
				}
			}
			@Override
			public String toString() {return "ROOT";}
		};

		/** Keeps track of growths */
		private int createdBuckets = 0;

		/** Keep track of actually used buckets */
		private int usedBuckets = 0;

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
		private long duplicates = 0L;
		private long cacheMisses = 0L;
		private long splits = 0L;
		private long closings = 0L;
		private long fringes = 0L;

		private double averageSplitRatio = 0D;
		private double minSplitRatio = 1D;
		private double maxSplitRation = 0D;

		public BucketSetBuilder(IndexValueType valueType, int chunkSize, boolean sortResult) {
			this(valueType, chunkSize, sortResult, false);
		}

		public BucketSetBuilder(IndexValueType valueType, int chunkSize, boolean sortResult, boolean useLastHitCache) {
			requireNonNull(valueType);
			checkChunkSize(chunkSize);

			checkDiscouragedValueType(valueType);

			this.valueType = valueType;
			this.chunkSize = chunkSize;
			this.sortResult = sortResult;
			this.useLastHitCache = useLastHitCache;

			virtualRoot.insert(createBucket(UNSET_INT));
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

		public long getClosingCount() {
			return closings;
		}

		public long getDuplicates() {
			return duplicates;
		}

		public long getFringeCount() {
			return fringes;
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

		public int getUsedBucketCount() {
			return usedBuckets;
		}

		public int getCreatedBucketCount() {
			return createdBuckets;
		}

		private Bucket getBucket(int capacity) {
			Bucket b;
			if(unusedBucket!=null && capacity(unusedBucket)>=capacity) {
				b = unusedBucket;
				unusedBucket = null;
			} else {
				b = createBucket(capacity);
			}

			return b;
		}

		private Bucket createBucket(int capacity) {
			IndexStorage storage = null;

			if(capacity==UNSET_INT) {
				capacity = chunkSize;
			}

			switch (valueType) {

			// No special case for BYTE
			case BYTE:
			case SHORT:
				storage = new LimitedUnsortedSetBuilderShort(capacity, UNDEFINED_CHUNK_SIZE, sortResult);
				break;

			case LONG:
				storage = new LimitedUnsortedSetBuilderLong(capacity, UNDEFINED_CHUNK_SIZE, sortResult);
				break;

			case INTEGER:
				storage = new LimitedUnsortedSetBuilderInt(capacity, UNDEFINED_CHUNK_SIZE, sortResult);
				break;

			default:
				throw new IllegalStateException(
						"Unsupported index value type: " + valueType);
			}

			createdBuckets = Math.incrementExact(createdBuckets);

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
			checkNotNegative(index);
			Bucket b = find(index);
			/*
			 *  Early escape check to prevent unnecessary splitting
			 *  and containment checks.
			 */
			if(!b.isClosed()) {
				add(b, index);
			} else {
				duplicates++;
			}
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
			IndexSet[] result = new IndexSet[usedBuckets];

//			printStats(System.out);

			collectSets(root(), result, 0);

			return result;
		}

		/**
		 * Collect the content of buckets into an index set buffer. Does an
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

			if(b.storage.size()>0) {
				buffer[index++] = b.storage.asSet();
				if(sortResult) {
					assert buffer[index-1].isSorted();
					if(index>1)
					assert buffer[index-1].firstIndex()>=buffer[index-2].lastIndex();
				}
			}

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
		Bucket find(long v) {
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

		/**
		 * Bucket must not be closed!
		 * @param b
		 * @param v
		 */
		private void add(Bucket b, long v) {
			assert !b.isClosed();
			int capacity = capacity(b);

			if(capacity<1) {
				// Redistribute space in the tree
				split(b);
				b = find(v);
			}

			if(b.add(v)) {
				if(b.storage.size()==1) {
					usedBuckets = Math.incrementExact(usedBuckets);
				}
			} else {
				duplicates++;
			}
			lastInsertionCache = b;
			insertions++;
		}

		private void split(Bucket b) {

			/**
			 * <pre>
			 *              /        ELEMENTS IN BUCKET         \
			 *   +---------+-------------------------------------+---------------+
			 *   |         |                                     |               |
			 * LEFT       MIN                                   MAX             RIGHT
			 * </pre>
			 *
			 * Additional values:
			 * CHUNKSIZE = upper bound on the number of elements in bucket
			 *
			 * Cases:
			 * (MAX-MIN+1) == CHUNKSIZE :
			 *        Bucket holds continuous sorted content, would be a waste to
			 *        split it into multiple chunks => Keep bucket and "split off"
			 *        the left and right areas if available
			 *
			 * else :
			 *        Bucket is just full of random numbers. We might want to further
			 *        investigate how to best pick a pivot and splitting, but for now
			 *        this scenario is solved in a rather naive fashion.
			 */

			if(b.isContinuous()) {
				closeAndCutFringes(b);
			} else {
				splitAndRedistribute(b);
			}
		}

		/**
		 * Leaves the bucket {@code b} in the tree ({@link Bucket#close() closing} it)
		 * and tries to cut off its fringes as new buckets if possible.
		 * This will add a maximum of two new buckets to the tree and leave the
		 * {@link #lastInsertionCache} untouched.
		 *
		 * @param b
		 */
		private void closeAndCutFringes(Bucket b) {
			// In any case close b
			b.close();
			closings++;

			boolean hasLeftFringe = b.getMin()>b.getLeft();
			boolean hasRightFringe = b.getMax()<b.getRight();

			// Consider left fringe
			if(hasLeftFringe) {
				long leftFringe = b.getMin()-b.getLeft(); // no +1 needed, as b.min is already covered
				assert leftFringe>0L;
				int capacity = leftFringe<chunkSize ? (int)leftFringe : UNSET_INT;

				Bucket left = getBucket(capacity);
				left.setLeft(b.getLeft());
				left.setRight(b.getMin()-1);
				b.setLeft(b.getMin());
				virtualRoot.insert(left);

				fringes++;
			}

			// Consider right fringe
			if(hasRightFringe) {
				long rightFringe = b.getRight()-b.getMax(); // no +1 needed, as b.max is already covered
				assert rightFringe>0L;
				int capacity = rightFringe<chunkSize ? (int)rightFringe : UNSET_INT;

				Bucket right = getBucket(capacity);
				right.setRight(b.getRight());
				right.setLeft(b.getMax()+1);
				b.setRight(b.getMax());
				virtualRoot.insert(right);

				fringes++;
			}
		}

		/**
		 * Divide given bucket {@code b} into 2 new buckets and redistribute values.
		 * This will remove {@code b} from the tree and make it available for future
		 * bucket reclamation.
		 * <p>
		 * This will also clear the {@link #lastInsertionCache} in case {@code b} was
		 * marked as the last bucket participating in an insertion.
		 * @param b
		 */
		private void splitAndRedistribute(Bucket b) {
			if(lastInsertionCache==b) {
				lastInsertionCache = null;
			}

//			System.out.println("------------------------------------------------------------------");
//			System.out.println("BEFORE  "+toString());
//			System.out.println("SPLITTING "+b);
			int totalCount = b.storage.size();

			// Fetch 2 new/empty buckets
			Bucket left = getBucket(UNSET_INT);
			Bucket right = getBucket(UNSET_INT);

			// Split content of old bucket into the new ones
			distribute(b, left, right);
			usedBuckets += 2;

			// Remove and recycle old bucket
			b.remove();
			usedBuckets--;
			destroy(b);

			// Instead of some local magic we just use the default insertion methods
			virtualRoot.insert(left);
			virtualRoot.insert(right);

			// Collect some meta info
			splits++;

			double leftCount = left.storage.size();
			double ratio = leftCount/totalCount;

			averageSplitRatio = ((averageSplitRatio * (splits-1)) + ratio) / splits;
			minSplitRatio = Math.min(minSplitRatio, ratio);
			maxSplitRation = Math.max(maxSplitRation, ratio);
//			System.out.println("AFTER  "+toString());
		}

		/**
		 * Returns number of leftover slots in storage of given {@link Bucket},
		 * i.e. {@code chunkSize - b.storage.size()}.
		 * @param b
		 * @return
		 */
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

		/**
		 * Splits a bucket in two halves based on a pivot element. The {@code left}
		 * bucket will hold all elements from {@code source} that are smaller or equal
		 * to the chosen pivot and the {@code right} bucket will hold all the ones that
		 * are greater.
		 *
		 * @param source
		 * @param left
		 * @param right
		 */
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
			out.println("DUPLICATES:          "+StringUtil.formatDecimal(duplicates));
			out.println("CREATED BUCKETS:     "+StringUtil.formatDecimal(createdBuckets));
			out.println("USED BUCKETS:        "+StringUtil.formatDecimal(usedBuckets));
			out.println("BUCKET SIZE:         "+StringUtil.formatDecimal(chunkSize));
			out.println("CACHE ACTIVE:        "+String.valueOf(useLastHitCache));
			out.println("CACHE MISSES:        "+StringUtil.formatDecimal(cacheMisses));
			out.println("SPLITS:              "+StringUtil.formatDecimal(splits));
			out.println("CLOSINGS:            "+StringUtil.formatDecimal(closings));
			out.println("CUT OFF FRINGES:     "+StringUtil.formatDecimal(fringes));
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
	static class Bucket implements Comparable<Bucket> {

		private static final long DEFAULT_MIN = UNSET_LONG;
		private static final long DEFAULT_MAX = UNSET_LONG;

		Bucket(IndexStorage storage) {
			this.storage = storage;
		}

		Bucket() {
			this(null);
		}

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
		private long left = 0L;
		/**
		 * Largest allowed value for bucket
		 */
		private long right = Long.MAX_VALUE;

		/**
		 * Uplink to parent.
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

		/**
		 * Flag to signal that a bucket has turned into a finalized storage
		 * of continuous index values for its maximum capacity and adding further
		 * values to it would have no effect and should therefore be prevented.
		 */
		private boolean closed = false;

		/**
		 * Adds the given value to this bucket and returns whether or not the internal
		 * storage has changed as a result (i.e. the value has not been present previously).
		 * @param v
		 * @return
		 */
		public boolean add(long v) {
			int oldSize = storage.size();
			storage.add(v);
			int newSize = storage.size();

			if(oldSize==newSize) {
				return false;
			}

			// Refresh lower and upper extremes
			if (min==DEFAULT_MIN || v < min) {
				min = v;
			}
			if (max==DEFAULT_MAX || v > max) {
				max = v;
			}

			return true;
		}

		private void checkNotRoot() {
			if(isRoot())
				throw new UnsupportedOperationException();
		}

		public boolean isRoot() {
			return false;
		}

		public boolean isLeaf() {
			return smaller==null || larger==null;
		}

		public boolean isClosed() {
			return closed;
		}

		/**
		 * Irrevocably switched the {@link #isClosed() closed} flag
		 * to {@code true}.
		 */
		private void close() {
			closed = true;
		}

		public boolean isLegalValue(long v) {
			return v>=left && v<=right;
		}

		/** Return smallest existing value in bucket */
		public long getMin() {
			return min;
		}

		/** Return largest existing value in bucket */
		public long getMax() {
			return max;
		}

		/** Return smallest allowed value in bucket */
		public long getLeft() {
			return left;
		}

		/** Return largest allowed value in bucket */
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

		/**
		 * Return {@code true} iff the values in this storage form
		 * a continuous sequence.
		 * @return
		 */
		public boolean isContinuous() {
			return IndexUtils.isContinuous(storage.size(), min, max);
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
			requireNonNull(parent);
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

		@SuppressWarnings("unused")
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
    	 * 2. Otherwise, determine node Y by finding the largest node in node X's left subtree
    	 *    (the in-order predecessor of X − it does not have a right child) or the smallest
    	 *    in its right subtree (the in-order successor of X − it does not have a left child).
    	 * 3. Exchange all the child and parent links of node X with those of node Y. In this step,
    	 *    the in-order sequence between nodes X and Y is temporarily disturbed, but the
    	 *    tree structure doesn't change.
    	 * 4. Choose node Z to be all the child and parent links of old node Y = those of new node X.
    	 * 5. If node Z has a subtree (which then is a leaf), attach it to Z's parent.
    	 * 6. If node Z was the root (its parent is null), update root.
    	 * 7. Delete node Z.
    	 * 8. Retrace the path back up the tree (starting with node Z's parent) to the root,
    	 *    adjusting the balance factors as needed.
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
			assert !closed;

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
			return String.format("{%s (%s,%s) b=%d s=%d %s}",
					text(left), text(min), text(max), _int(heightDiff()),
					_int(storage.size()), text(right));
		}
	}
}
