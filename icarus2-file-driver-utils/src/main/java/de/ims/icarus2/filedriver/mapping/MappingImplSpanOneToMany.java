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
package de.ims.icarus2.filedriver.mapping;

import static de.ims.icarus2.model.api.driver.indices.IndexUtils.EMPTY;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.ensureSorted;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.firstIndex;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.isContinuous;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.lastIndex;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.wrapSpan;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.apiguard.Api;
import de.ims.icarus2.apiguard.Api.ApiType;
import de.ims.icarus2.apiguard.Guarded;
import de.ims.icarus2.apiguard.Guarded.MethodType;
import de.ims.icarus2.filedriver.io.BufferedIOResource;
import de.ims.icarus2.filedriver.io.BufferedIOResource.Block;
import de.ims.icarus2.filedriver.io.BufferedIOResource.PayloadConverter;
import de.ims.icarus2.filedriver.io.BufferedIOResource.SimpleHeader;
import de.ims.icarus2.filedriver.io.Range;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexCollector;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.IndexUtils.SpanProcedure;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingReader;
import de.ims.icarus2.model.api.driver.mapping.MappingWriter;
import de.ims.icarus2.model.api.driver.mapping.RequestSettings;
import de.ims.icarus2.model.api.driver.mapping.WritableMapping;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.strings.ToStringBuilder;

/**
 * Implements a one-to-many mapping for containers of type {@link ContainerType#SPAN}.
 * It stores the begin and end offsets for each span as a pair of either short, integer or long
 * values (resulting in 4,8 or 16 bytes per entry). The nature of spans allows for some
 * very efficient optimizations for the corresponding {@link MappingReader reader} implementations
 * this mapping provides:
 * <p>
 * If a mapping function is monotonic, then the begin index of the (partially) covered target
 * indices of a set of sorted source spans is always the target begin index of the first span
 * (the same holds for end index values, where the last index in the last span is used).
 * In addition a continuous collection of spans always maps to a continuous subset of the
 * target index space, described by the projected target indices of the collections first and
 * last spans.
 * <p>
 * For reverse lookups the two {@code find} methods of the {@code MappingReader} interface are
 * implemented to use binary search in order to pin down source spans in a predefined range.
 * <p>
 * The file based storage is organized in blocks with <tt>2^{@value #DEFAULT_BLOCK_POWER}</tt>
 * entries each.
 *
 * @author Markus Gärtner
 *
 */
public class MappingImplSpanOneToMany extends AbstractStoredMapping<SimpleHeader> {

	public static Builder builder() {
		return new Builder();
	}

	private final IndexBlockStorage blockStorage;

	public static final int DEFAULT_BLOCK_POWER = 14;

//	public static final int DEFAULT_BLOCK_MASK = (1<<DEFAULT_BLOCK_POWER)-1;
//
//	public static final int DEFAULT_ENTRIES_PER_BLOCK = 1<<DEFAULT_BLOCK_POWER;

	private final int blockPower;
	private final int blockMask;
	private final int entriesPerBlock;

	protected MappingImplSpanOneToMany(Builder builder) {
		super(builder);

		blockPower = builder.getBlockPower();
		blockMask = builder.getBlockMask();
		entriesPerBlock = builder.getEntriesPerBlock();
		blockStorage = builder.getBlockStorage();
	}

	@Override
	protected void toString(ToStringBuilder sb) {
		sb.add("blockPower", blockPower)
		.add("blockMask", Integer.toBinaryString(blockMask))
		.add("entriesPerBlock", entriesPerBlock)
		.add("blockStorage", blockStorage);
	}

	public IndexBlockStorage getBlockStorage() {
		return blockStorage;
	}

	public int getEntriesPerBlock() {
		return entriesPerBlock;
	}

	private int id(long index) {
		return (int) (index>>blockPower);
	}

	private int localIndex(long index) {
		return (int)(index & blockMask);
	}

	private void checkTargetIndex(long value) {
		blockStorage.checkValue(value);
	}

	private void checkSourceIndex(long value) {
		if(value<0L)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Value is negative: "+value);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.mapping.Mapping#newReader()
	 */
	@Override
	public MappingReader newReader() {
		return this.new Reader();
	}

	/**
	 * @see de.ims.icarus2.filedriver.mapping.AbstractStoredMapping#newWriter()
	 */
	@Override
	public MappingWriter newWriter() {
		return this.new Writer();
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public class Reader extends ResourceAccessor<Mapping> implements MappingReader {

		protected Reader() {
			super(true);
		}

		private final Coverage coverage = ManifestUtils.require(
				getManifest(), MappingManifest::getCoverage, "coverage");

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getIndicesCount(long, de.ims.icarus2.model.api.driver.mapping.RequestSettings)
		 */
		@Override
		public long getIndicesCount(long sourceIndex, @Nullable RequestSettings settings)
				throws InterruptedException {

			if(!getHeader().isUsedIndex(sourceIndex)) {
				return UNSET_LONG;
			}

			int id = id(sourceIndex);
			int localIndex = localIndex(sourceIndex);

			Block block = getBlock(id);
			if(block==null) {
				return UNSET_LONG;
			}

			Object data = block.getData();
			long begin = blockStorage.getSpanBegin(data, localIndex);
			long end = blockStorage.getSpanEnd(data, localIndex);

			return end<begin || begin==UNSET_LONG ? UNSET_LONG : (end-begin+1);
		}

		/**
		 * @return
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(long, de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean lookup(long sourceIndex, IndexCollector collector,
				@Nullable RequestSettings settings) throws ModelException {
			requireNonNull(collector);

			if(!getHeader().isUsedIndex(sourceIndex)) {
				return false;
			}

			int id = id(sourceIndex);
			int localIndex = localIndex(sourceIndex);

			Block block = getBlock(id);
			if(block==null) {
				return false;
			}

			long targetBegin = blockStorage.getSpanBegin(block.getData(), localIndex);
			long targetEnd = blockStorage.getSpanEnd(block.getData(), localIndex);

			if(targetBegin==UNSET_LONG) {
				return false;
			}

			// Use direct span collection method to avoid object creation!
			collector.add(targetBegin, targetEnd);

			return true;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(long, RequestSettings)
		 */
		@Override
		public IndexSet[] lookup(long sourceIndex, @Nullable RequestSettings settings) throws ModelException {
			int id = id(sourceIndex);
			int localIndex = localIndex(sourceIndex);

			Block block = getBlock(id);
			if(block==null) {
				return EMPTY;
			}

			long targetBegin = blockStorage.getSpanBegin(block.getData(), localIndex);
			long targetEnd = blockStorage.getSpanEnd(block.getData(), localIndex);

			if(targetBegin==UNSET_LONG) {
				return EMPTY;
			}

			return wrapSpan(targetBegin, targetEnd);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getBeginIndex(long, RequestSettings)
		 */
		@Override
		public long getBeginIndex(long sourceIndex, @Nullable RequestSettings settings) throws ModelException {
			if(!getHeader().isUsedIndex(sourceIndex)) {
				return UNSET_LONG;
			}

			int id = id(sourceIndex);
			int localIndex = localIndex(sourceIndex);

			Block block = getBlock(id);
			return block==null ? UNSET_LONG : blockStorage.getSpanBegin(block.getData(), localIndex);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getEndIndex(long, RequestSettings)
		 */
		@Override
		public long getEndIndex(long sourceIndex, @Nullable RequestSettings settings) throws ModelException {
			if(!getHeader().isUsedIndex(sourceIndex)) {
				return UNSET_LONG;
			}

			int id = id(sourceIndex);
			int localIndex = localIndex(sourceIndex);

			Block block = getBlock(id);
			return block==null ? UNSET_LONG : blockStorage.getSpanEnd(block.getData(), localIndex);
		}

		/**
		 * Runs a check for interrupted thread state before processing an index set.
		 *
		 * @return
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean lookup(IndexSet[] sourceIndices, IndexCollector collector,
				@Nullable RequestSettings settings)
				throws InterruptedException {
			requireNonNull(sourceIndices);
			requireNonNull(collector);
			ensureSorted(sourceIndices, settings);

			boolean result = false;

			if(coverage.isMonotonic()) {
				if(coverage.isTotal() && isContinuous(sourceIndices)) {
					// Special case of a single big span
					long beginIndex = getBeginIndex(firstIndex(sourceIndices), null);
					long endIndex = getEndIndex(firstIndex(sourceIndices), null);
					if(beginIndex!=UNSET_LONG && endIndex!=UNSET_LONG) {
						collector.add(beginIndex, endIndex);
						result = true;
					}
				} else {
					// Requires checks on all individual index sets
					for(IndexSet indices : sourceIndices) {
						checkInterrupted();

						if(coverage.isTotal() && isContinuous(indices)) {
							// Spans get projected on other spans
							collector.add(getBeginIndex(indices.firstIndex(), null), getEndIndex(indices.lastIndex(), null));
						} else {
							// Expensive version: traverse values and add individual target spans
							for(int i=0; i<indices.size(); i++) {
								long sourceIndex = indices.indexAt(i);
								long beginIndex = getBeginIndex(sourceIndex, null);
								long endIndex = getEndIndex(sourceIndex, null);
								if(beginIndex!=UNSET_LONG && endIndex!=UNSET_LONG) {
									collector.add(beginIndex, endIndex);
									result = true;
								}
							}
						}
					}
				}

			} else {
				// Expensive version: traverse ALL individual indices and add target spans.
				// Remember however, that we still have an injective index function, so no
				// duplicate checks required!
				for(IndexSet indices : sourceIndices) {
					checkInterrupted();

					// Expensive version: traverse values and add individual target spans
					for(int i=0; i<indices.size(); i++) {
						long sourceIndex = indices.indexAt(i);
						long beginIndex = getBeginIndex(sourceIndex, null);
						long endIndex = getEndIndex(sourceIndex, null);
						if(beginIndex!=UNSET_LONG && endIndex!=UNSET_LONG) {
							collector.add(beginIndex, endIndex);
							result = true;
						}
					}
				}
			}

			return result;
		}

		/**
		 * Optimized behavior in case of {@link Coverage#isMonotonic() continuous coverage}:<br>
		 * Since
		 *
		 * Runs a check for interrupted thread state before processing an index set.
		 *
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getBeginIndex(de.ims.icarus2.model.api.driver.indices.IndexSet[], RequestSettings)
		 */
		@Override
		public long getBeginIndex(IndexSet[] sourceIndices, @Nullable RequestSettings settings)
				throws InterruptedException {
			requireNonNull(sourceIndices);
			ensureSorted(sourceIndices, settings);

			// Optimized handling of monotonic coverage: use only first source index
			if(coverage.isMonotonic()) {
				return getBeginIndex(firstIndex(sourceIndices), null);
			}

			// Expensive alternative: traverse all indices
			long result = Long.MAX_VALUE;

			for(IndexSet indices : sourceIndices) {
				checkInterrupted();

				for(int i=0; i<indices.size(); i++) {
					long sourceIndex = indices.indexAt(i);
					result = Math.min(result, getBeginIndex(sourceIndex, null));
				}
			}

			return result;
		}

		/**
		 *
		 * Runs a check for interrupted thread state before processing an index set.
		 *
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getEndIndex(de.ims.icarus2.model.api.driver.indices.IndexSet[], RequestSettings)
		 */
		@Override
		public long getEndIndex(IndexSet[] sourceIndices, @Nullable RequestSettings settings)
				throws InterruptedException {
			requireNonNull(sourceIndices);
			ensureSorted(sourceIndices, settings);

			// Optimized handling of monotonic coverage: use only last source index
			if(coverage.isMonotonic()) {
				return getEndIndex(lastIndex(sourceIndices), null);
			}

			// Expensive alternative: traverse all indices
			long result = UNSET_LONG;

			for(IndexSet indices : sourceIndices) {
				checkInterrupted();

				for(int i=0; i<indices.size(); i++) {
					long sourceIndex = indices.indexAt(i);
					result = Math.max(result, getEndIndex(sourceIndex, null));
				}
			}

			return result;
		}

		/**
		 * Translate the position {@code localIndex} in block with given {@code id}
		 * into a global index value.
		 */
		private long translate(int id, int localIndex) {
			return localIndex==UNSET_INT ? UNSET_LONG : id*entriesPerBlock + localIndex;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#find(long, long, long, RequestSettings)
		 */
		@Override
		public long find(long fromSource, long toSource, long targetIndex, RequestSettings settings) throws ModelException {
			checkSourceIndex(fromSource);
			checkSourceIndex(toSource);
			checkTargetIndex(targetIndex);
			// Skip unused target indices
			if(!getHeader().isUsedTarget(targetIndex)) {
				return UNSET_LONG;
			}

			// Restrict source range with header information
			Range sourceRange = getHeader().getUsedIndices(fromSource, toSource);
			if(sourceRange.isUnset()) {
				return UNSET_LONG;
			}

			int idFrom = id(sourceRange.getMin());
			int idTo = id(sourceRange.getMax());
			int localFrom = localIndex(sourceRange.getMin());
			int localTo = localIndex(sourceRange.getMax())+1; // need +1 due to internal methods using exclusive end index

			return findSingle(idFrom, idTo, localFrom, localTo, targetIndex);
		}

		private long findSingle(int idFrom, int idTo, int localFrom, int localTo, long targetIndex) {
			// Special case of a single block search
			if(idFrom==idTo) {
				return find0(idFrom, localFrom, localTo, targetIndex);
			}

			long result;
			// Check first block
			if((result = find0(idFrom, localFrom, entriesPerBlock, targetIndex))!=UNSET_LONG) {
				return result;
			}

			// Check last block
			if((result = find0(idTo, 0, localTo, targetIndex))!=UNSET_LONG) {
				return result;
			}

			// Iterate intermediate blocks
			for(int id=idFrom+1; id<idTo; id++) {
				// Now always include the entire block to search
				if((result = find0(id, 0, entriesPerBlock, targetIndex))!=UNSET_LONG) {
					return result;
				}
			}

			return IcarusUtils.UNSET_LONG;
		}

		private long find0(int id, int localFrom, int localTo, long targetIndex) {

			Block block = getBlock(id);

			if(block==null) {
				return UNSET_LONG;
			}

			int localIndex = -1;

			//TODO unless the entire mapping is already written, this check will lead us to a fail
//			if(coverage.isMonotonic() && coverage.isTotal()) {
//				localIndex = blockStorage.findSortedSpan(block.getData(), localFrom, localTo, targetIndex);
//			} else
			if(coverage.isMonotonic()) {
				localIndex = blockStorage.sparseFindSortedSpan(block.getData(), localFrom, localTo, targetIndex);
			} else {
				localIndex = blockStorage.findSpan(block.getData(), localFrom, localTo, targetIndex);
			}

			return translate(id, localIndex);
		}

		private long findContinuous(int idFrom, int idTo, int localFrom, int localTo,
				long targetBegin, long targetEnd, IndexCollector collector) {

			// Find first span covering the targetBegin
			long sourceBegin = findSingle(idFrom, idTo, localFrom, localTo, targetBegin);

			if(sourceBegin==UNSET_LONG) {
				return UNSET_LONG;
			}

			// Refresh left end of search interval
			idFrom = id(sourceBegin);
			localFrom = localIndex(sourceBegin);

			// Find last span covering targetEnd
			long sourceEnd = findSingle(idFrom, idTo, localFrom, localTo, targetEnd);

			if(sourceEnd==UNSET_LONG) {
				return UNSET_LONG;
			}

			collector.add(sourceBegin, sourceEnd);

			return sourceEnd;
		}

		/** Helper class to carry search state for find() methods */
		private abstract class SpanSearch implements SpanProcedure {
			boolean found = false;

			int idFrom, idTo, localFrom, localTo;

			final IndexCollector collector;

			SpanSearch(Range sourceRange, IndexCollector collector) {
				this.collector = requireNonNull(collector);

				idFrom = id(sourceRange.getMin());
				idTo = id(sourceRange.getMax());
				localFrom = localIndex(sourceRange.getMin());
				localTo = localIndex(sourceRange.getMax())+1; // need +1 due to internal methods using exclusive end index
			}
		}

		/**
		 * In case of monotonic index we can adjust our search interval for
		 * the source index space whenever we successfully resolve some
		 * source indices. In addition the first miss is bound to cause the
		 * entire search to fail.
		 */
		private class MonotonicSpanSearch extends SpanSearch {

			final long targetEnd;
			final long sourceEnd;

			MonotonicSpanSearch(Range sourceRange, IndexCollector collector,
					long targetEnd, long sourceEnd) {
				super(sourceRange, collector);

				this.targetEnd = targetEnd;
				this.sourceEnd = sourceEnd;
			}

			@Override
			public boolean process(long from, long to) throws InterruptedException {
				long sourceIndex;

				if(from==to) {
					sourceIndex = findSingle(idFrom, idTo, localFrom, localTo, from);
					if(sourceIndex==UNSET_LONG) {
						return false;
					}

					// Manually add mapped source index
					collector.add(sourceIndex);
					found = true;
				} else {
					// The mapped span will already be added inside the findContinuous method
					sourceIndex = findContinuous(idFrom, idTo,
							localFrom, localTo, from, to, collector);

					// Here sourceIndex is the index of the last span that was found
					if(sourceIndex==UNSET_LONG) {
						return false;
					}
					found = true;
				}

				if(sourceIndex>=sourceEnd || getEndIndex(sourceIndex, null)>=targetEnd) {
					return false;
				}

				// There has to be space left to map the remaining target indices, so
				// reset interval begin to the next span after the current

				idFrom = id(sourceIndex+1);
				localFrom = localIndex(sourceIndex+1);

				return true;
			}

		}

		/**
		 * Non-monotonic mapping means the only way of optimizing the search
		 * is to shrink the source interval whenever we encounter spans that
		 * overlap with the current end of the interval.
		 */
		private class PoorSpanSearch extends SpanSearch {

			long _fromSource, _toSource;

			PoorSpanSearch(Range sourceRange, IndexCollector collector) {
				super(sourceRange, collector);

				_fromSource = sourceRange.getMin();
				_toSource = sourceRange.getMax();
			}

			@Override
			public boolean process(long from, long to) throws InterruptedException {

				while(from<=to) {
					long sourceIndex = findSingle(idFrom, idTo, localFrom, localTo, from);

					if(sourceIndex==UNSET_LONG) {
						// Continue through the search space when no match was found
						from++;
					} else {
						// Add found source index and start pruning search space
						collector.add(sourceIndex);
						found = true;

						// Fetch end of span to prune some target indices
						long spanEnd = getEndIndex(sourceIndex, null);

						// Step forward to either the next target index or after
						// the end of the found span, whichever is greater
						from = Math.max(spanEnd, from)+1;

						// Shrink search interval if possible
						if(sourceIndex==_fromSource) {
							_fromSource++;
							idFrom = id(_fromSource);
							localFrom = localIndex(_fromSource);
						}
						if(sourceIndex==_toSource) {
							_toSource--;
							idTo  = id(_toSource);
							localTo = localIndex(_toSource)+1;
						}
					}

					// Global state check of the search window
					if(_toSource<_fromSource) {
						// Search space exhausted, abort future processing
						return false;
					}
				}

				// Only way of finishing search is exhaustion of search space,
				// so always allow to continue here
				return true;
			}
		}

		/**
		 * @return
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#find(long, long, de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean find(final long fromSource, final long toSource,
				final IndexSet[] targetIndices, final IndexCollector collector,
				@Nullable RequestSettings settings)
				throws InterruptedException {
			requireNonNull(targetIndices);
			requireNonNull(collector);
			ensureSorted(targetIndices, settings);

			// Restrict source range with header information
			Range sourceRange = getHeader().getUsedIndices(fromSource, toSource);
			if(sourceRange.isUnset()) {
				// No overlap of estimated range and our actually mapped sources
				return false;
			}

			SpanSearch search = coverage.isMonotonic() ?
					new MonotonicSpanSearch(sourceRange, collector, lastIndex(targetIndices), toSource)
					: new PoorSpanSearch(sourceRange, collector);

			IndexUtils.forEachSpan(targetIndices, search);

			return search.found;
		}

	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public class Writer extends ResourceAccessor<WritableMapping> implements MappingWriter {

		protected Writer() {
			super(false);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingWriter#map(long, long, long)
		 */
		@Override
		public void map(long sourceFrom, long sourceTo, long targetFrom, long targetTo) {
			if(sourceFrom!=sourceTo)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT,
						"Can only map from single index values");

			int id = id(sourceFrom);
			int localIndex = localIndex(sourceFrom);

			Block block = getBlock(id);
			assert block!=null : "missing block for id "+id;
			Object buffer = block.getData();

			long oldBegin = blockStorage.setSpanBegin(buffer, localIndex, targetFrom);
			long oldEnd = blockStorage.setSpanEnd(buffer, localIndex, targetTo);

			if(oldBegin==targetFrom && oldEnd==targetTo) {
				return;
			}

			lockBlock(block, localIndex);

			SimpleHeader header = getHeader();
			if(oldBegin==UNSET_LONG) {
				header.growSize();
			}
			header.updateUsedIndex(sourceFrom);
			header.updateTargetIndex(targetFrom);
			header.updateTargetIndex(targetTo);
		}

		/**
		 * This implementation delegates to {@link #map(long, long, long, long)} assuming that the
		 * {@code sourceIndex} should be mapped to a span of length {@code 1}.
		 *
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingWriter#map(long, long)
		 */
		@Override
		public void map(long sourceIndex, long targetIndex) {
			map(sourceIndex, sourceIndex, targetIndex, targetIndex);
		}

		@Override
		public void map(IndexSet sourceIndices, IndexSet targetIndices) {
			requireNonNull(sourceIndices);
			requireNonNull(targetIndices);
			if(sourceIndices.size()>1)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Can only map from single index values");
			if(!isContinuous(targetIndices))
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Can only map to spans");

			map(sourceIndices.firstIndex(), sourceIndices.firstIndex(),
					targetIndices.firstIndex(), targetIndices.lastIndex());
		}

		@Override
		public void map(IndexSet[] sourceIndices, IndexSet[] targetIndices) {
			requireNonNull(sourceIndices);
			requireNonNull(targetIndices);
			if(sourceIndices.length>1 || sourceIndices[0].size()>1)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Can only map from single index values");
			if(!isContinuous(targetIndices))
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Can only map to spans");

			long sourceIndex = firstIndex(sourceIndices);
			map(sourceIndex, sourceIndex, firstIndex(targetIndices), lastIndex(targetIndices));
		}

	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	@Api(type=ApiType.BUILDER)
	public static class Builder extends AbstractStoredMappingBuilder<Builder, MappingImplSpanOneToMany> {

		private Integer blockPower;

		protected Builder() {
			// no-op
		}

		@Guarded(methodType=MethodType.GETTER, defaultValue="14")
		public int getBlockPower() {
			return blockPower==null ? DEFAULT_BLOCK_POWER : blockPower.intValue();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder blockPower(int blockPower) {
			checkArgument(blockPower>0);
			checkState(this.blockPower==null);

			this.blockPower = Integer.valueOf(blockPower);

			return thisAsCast();
		}

		public int getBlockMask() {
			return (1<<getBlockPower())-1;
		}

		public int getEntriesPerBlock() {
			return (1<<getBlockPower());
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.AbstractStoredMapping.AbstractStoredMappingBuilder#createBufferedIOResource()
		 */
		@Override
		public BufferedIOResource createBufferedIOResource() {
			IndexBlockStorage blockStorage = getBlockStorage();
			int bytesPerBlock = getEntriesPerBlock()*blockStorage.spanSize();
			PayloadConverter payloadConverter = new SpanConverter(blockStorage);

			return BufferedIOResource.builder()
				.resource(getResource())
				.blockCache(getBlockCache())
				.cacheSize(getCacheSize())
				.header(new SimpleHeader())
				.bytesPerBlock(bytesPerBlock)
				.payloadConverter(payloadConverter)
				.build();
		}

		@Override
		protected MappingImplSpanOneToMany create() {
			return new MappingImplSpanOneToMany(this);
		}

	}
}
