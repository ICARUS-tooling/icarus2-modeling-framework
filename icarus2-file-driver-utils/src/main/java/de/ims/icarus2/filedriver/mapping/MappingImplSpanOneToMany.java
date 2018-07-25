/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.forEachSpan;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.isContinuous;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.lastIndex;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.wrapSpan;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.io.BufferedIOResource;
import de.ims.icarus2.filedriver.io.BufferedIOResource.Block;
import de.ims.icarus2.filedriver.io.BufferedIOResource.PayloadConverter;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexCollector;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils.SpanProcedure;
import de.ims.icarus2.model.api.driver.mapping.MappingReader;
import de.ims.icarus2.model.api.driver.mapping.MappingWriter;
import de.ims.icarus2.model.api.driver.mapping.RequestSettings;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;
import de.ims.icarus2.util.IcarusUtils;

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
 * The file based storage is organized in blocks with {@value #DEFAULT_ENTRIES_PER_BLOCK} (<tt>2^14</tt>)
 * entries each.
 *
 * @author Markus Gärtner
 *
 */
public class MappingImplSpanOneToMany extends AbstractStoredMapping {

	public static Builder newBuilder() {
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
	protected void toString(StringBuilder sb) {
		sb.append(" blockPower=").append(blockPower)
		.append(" blockMask=").append(Integer.toBinaryString(blockMask))
		.append(" entriesPerBlock=").append(entriesPerBlock)
		.append(" blockStorage=").append(blockStorage);
	}

	public IndexBlockStorage getBlockStorage() {
		return blockStorage;
	}

	public int getBlockPower() {
		return blockPower;
	}

	public int getBlockMask() {
		return blockMask;
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
	public class Reader extends ResourceAccessor implements MappingReader {

		protected Reader() {
			super(true);
		}

		private final Coverage coverage = getManifest().getCoverage();

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getIndicesCount(long, de.ims.icarus2.model.api.driver.mapping.RequestSettings)
		 */
		@Override
		public long getIndicesCount(long sourceIndex, RequestSettings settings)
				throws InterruptedException {
			int id = id(sourceIndex);
			int localIndex = localIndex(sourceIndex);

			Block block = getBlock(id, false);
			if(block==null) {
				return IcarusUtils.UNSET_LONG;
			}

			long begin = blockStorage.getSpanBegin(block.getData(), localIndex);
			long end = blockStorage.getSpanEnd(block.getData(), localIndex);

			return end<begin ? IcarusUtils.UNSET_LONG : (end-begin+1);
		}

		/**
		 * @return
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(long, de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean lookup(long sourceIndex, IndexCollector collector, RequestSettings settings) throws ModelException {
			int id = id(sourceIndex);
			int localIndex = localIndex(sourceIndex);

			Block block = getBlock(id, false);
			if(block==null) {
				return false;
			}

			// Use direct span collection method to avoid object creation!
			collector.add(blockStorage.getSpanBegin(block.getData(), localIndex),
					blockStorage.getSpanEnd(block.getData(), localIndex));

			return true;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(long, RequestSettings)
		 */
		@Override
		public IndexSet[] lookup(long sourceIndex, RequestSettings settings) throws ModelException {
			int id = id(sourceIndex);
			int localIndex = localIndex(sourceIndex);

			Block block = getBlock(id, false);
			if(block==null) {
				return EMPTY;
			}

			return wrapSpan(
					blockStorage.getSpanBegin(block.getData(), localIndex),
					blockStorage.getSpanEnd(block.getData(), localIndex));
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getBeginIndex(long, RequestSettings)
		 */
		@Override
		public long getBeginIndex(long sourceIndex, RequestSettings settings) throws ModelException {
			int id = id(sourceIndex);
			int localIndex = localIndex(sourceIndex);

			Block block = getBlock(id, false);
			return block==null ? IcarusUtils.UNSET_LONG : blockStorage.getSpanBegin(block.getData(), localIndex);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getEndIndex(long, RequestSettings)
		 */
		@Override
		public long getEndIndex(long sourceIndex, RequestSettings settings) throws ModelException {
			int id = id(sourceIndex);
			int localIndex = localIndex(sourceIndex);

			Block block = getBlock(id, false);
			return block==null ? IcarusUtils.UNSET_LONG : blockStorage.getSpanEnd(block.getData(), localIndex);
		}

		/**
		 * Runs a check for interrupted thread state before processing an index set.
		 *
		 * @return
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean lookup(IndexSet[] sourceIndices, IndexCollector collector, RequestSettings settings)
				throws InterruptedException {
			ensureSorted(sourceIndices, settings);

			boolean result = false;

			if(coverage.isMonotonic()) {
				if(coverage.isTotal() && isContinuous(sourceIndices)) {
					// Special case of a single big span
					long beginIndex = getBeginIndex(firstIndex(sourceIndices), null);
					long endIndex = getEndIndex(firstIndex(sourceIndices), null);
					if(beginIndex!=IcarusUtils.UNSET_LONG && endIndex!=IcarusUtils.UNSET_LONG) {
						collector.add(beginIndex, endIndex);
						result = true;
					}
				} else {
					// Requires checks on all individual index sets
					for(IndexSet indices : sourceIndices) {
						if(isContinuous(indices)) {
							// Spans get projected on other spans
							collector.add(getBeginIndex(indices.firstIndex(), null), getEndIndex(indices.lastIndex(), null));
						} else {
							checkInterrupted();

							// Expensive version: traverse values and add individual target spans
							for(int i=0; i<indices.size(); i++) {
								long sourceIndex = indices.indexAt(i);
								long beginIndex = getBeginIndex(sourceIndex, null);
								long endIndex = getEndIndex(sourceIndex, null);
								if(beginIndex!=IcarusUtils.UNSET_LONG && endIndex!=IcarusUtils.UNSET_LONG) {
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
						if(beginIndex!=IcarusUtils.UNSET_LONG && endIndex!=IcarusUtils.UNSET_LONG) {
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
		public long getBeginIndex(IndexSet[] sourceIndices, RequestSettings settings)
				throws InterruptedException {
			ensureSorted(sourceIndices, settings);

			// Optimized handling of monotonic coverage: use only first source index
			if(coverage.isMonotonic()) {
				return getBeginIndex(firstIndex(sourceIndices), null);
			} else {
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
		}

		/**
		 *
		 * Runs a check for interrupted thread state before processing an index set.
		 *
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getEndIndex(de.ims.icarus2.model.api.driver.indices.IndexSet[], RequestSettings)
		 */
		@Override
		public long getEndIndex(IndexSet[] sourceIndices, RequestSettings settings)
				throws InterruptedException {

			// Optimized handling of monotonic coverage: use only last source index
			if(coverage.isMonotonic()) {
				return getEndIndex(lastIndex(sourceIndices), null);
			} else {
				// Expensive alternative: traverse all indices
				long result = Long.MIN_VALUE;

				for(IndexSet indices : sourceIndices) {
					checkInterrupted();

					for(int i=0; i<indices.size(); i++) {
						long sourceIndex = indices.indexAt(i);
						result = Math.max(result, getBeginIndex(sourceIndex, null));
					}
				}

				return result;
			}
		}

		/**
		 * Translate the position {@code localIndex} in block with given {@code id}
		 * into a global index value.
		 */
		private long translate(int id, int localIndex) {
			return localIndex==-1 ? IcarusUtils.UNSET_LONG : id*entriesPerBlock + localIndex;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#find(long, long, long, RequestSettings)
		 */
		@Override
		public long find(long fromSource, long toSource, long targetIndex, RequestSettings settings) throws ModelException {
			int idFrom = id(fromSource);
			int idTo = id(toSource);
			int localFrom = localIndex(fromSource);
			int localTo = localIndex(toSource)+1;

			return find(idFrom, idTo, localFrom, localTo, targetIndex);
		}

		private long find(int idFrom, int idTo, int localFrom, int localTo, long targetIndex) {
			// Special case of a single block search
			if(idFrom==idTo) {
				return find0(idFrom, localFrom, localTo, targetIndex);
			}

			// Check first block
			long result = find0(idFrom, localFrom, entriesPerBlock, targetIndex);
			if(result!=IcarusUtils.UNSET_LONG) {
				return result;
			}

			// Check last block
			result = find0(idTo, 0, localTo, targetIndex);
			if(result!=IcarusUtils.UNSET_LONG) {
				return result;
			}

			// Iterate intermediate blocks
			for(int id=idFrom+1; id<idTo; id++) {
				// Now always include the entire block to search
				result = find0(id, 0, entriesPerBlock, targetIndex);
				if(result!=IcarusUtils.UNSET_LONG) {
					return result;
				}
			}

			return IcarusUtils.UNSET_LONG;
		}

		private long find0(int id, int localFrom, int localTo, long targetIndex) {

			Block block = getBlock(id, false);

			if(block==null) {
				return IcarusUtils.UNSET_LONG;
			}

			int localIndex = -1;

			if(coverage.isMonotonic()) {
				localIndex = blockStorage.findSortedSpan(block.getData(), localFrom, localTo, targetIndex);
			} else {
				localIndex = blockStorage.findSpan(block.getData(), localFrom, localTo, targetIndex);
			}

			return translate(id, localIndex);
		}

		private long findContinuous(int idFrom, int idTo, int localFrom, int localTo,
				long targetBegin, long targetEnd, IndexCollector collector) {

			// Find first span covering the targetBegin
			long sourceBegin = find(idFrom, idTo, localFrom, localTo, targetBegin);

			if(sourceBegin==IcarusUtils.UNSET_LONG) {
				return IcarusUtils.UNSET_LONG;
			}

			// Refresh left end of search interval
			idFrom = id(sourceBegin);
			localFrom = localIndex(sourceBegin);

			// Find last span covering targetEnd
			long sourceEnd = find(idFrom, idTo, localFrom, localTo, targetEnd);

			if(sourceEnd==IcarusUtils.UNSET_LONG) {
				return IcarusUtils.UNSET_LONG;
			}

			collector.add(sourceBegin, sourceEnd);

			return sourceEnd;
		}

		/**
		 * @return
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#find(long, long, de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean find(final long fromSource, final long toSource,
				final IndexSet[] targetIndices, final IndexCollector collector, RequestSettings settings)
				throws InterruptedException {
			ensureSorted(targetIndices, settings);

			if(coverage.isMonotonic()) {

				/*
				 * In case of monotonic index we can adjust our search interval for
				 * the source index space whenever we successfully resolve some
				 * source indices. In addition the first miss is bound to cause the
				 * entire search to fail.
				 * each
				 */

				SpanProcedure proc = new SpanProcedure() {

					int idFrom = id(fromSource);
					int idTo = id(toSource);
					int localFrom = localIndex(fromSource);
					int localTo = localIndex(toSource)+1;

					long targetEnd = lastIndex(targetIndices);

					@Override
					public boolean process(long from, long to) {
						long sourceIndex;

						if(from==to) {
							sourceIndex = find(idFrom, idTo, localFrom, localTo, from);
							if(sourceIndex==IcarusUtils.UNSET_LONG) {
								return false;
							}

							// Manually add mapped source index
							collector.add(sourceIndex);
						} else {
							// The mapped span will already be added inside the findContinuous method
							sourceIndex = findContinuous(idFrom, idTo,
									localFrom, localTo, from, to, collector);

							// Here sourceIndex is the index of the last span that was found
							if(sourceIndex==IcarusUtils.UNSET_LONG) {
								return false;
							}
						}

						if(sourceIndex>=toSource || getEndIndex(sourceIndex, null)>=targetEnd) {
							return false;
						} else {
							// There has to be space left to map the remaining target indices, so
							// reset interval begin to the next span after the current

							idFrom = id(sourceIndex+1);
							localFrom = localIndex(sourceIndex+1);

							return true;
						}
					}
				};

				return forEachSpan(targetIndices, proc);
			} else {

				/*
				 * Non-monotonic mapping means the only way of optimizing the search
				 * is to shrink the source interval whenever we encounter spans that
				 * overlap with the current end of the interval.
				 */

				SpanProcedure proc = new SpanProcedure() {

					int idFrom = id(fromSource);
					int idTo = id(toSource);
					int localFrom = localIndex(fromSource);
					int localTo = localIndex(toSource)+1;

					long _fromSource = fromSource;
					long _toSource = toSource;

					@Override
					public boolean process(long from, long to) {

						while(from<=to) {
							long sourceIndex = find(idFrom, idTo, localFrom, localTo, from);

							if(sourceIndex==IcarusUtils.UNSET_LONG) {
								// Continue through the search space when no match was found
								from++;
							} else {

								collector.add(sourceIndex);

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
				};

				return forEachSpan(targetIndices, proc);
			}

		}

	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public class Writer extends ResourceAccessor implements MappingWriter {

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

			Block block = getBlock(id, true);
			blockStorage.setSpanBegin(block.getData(), localIndex, targetFrom);
			blockStorage.setSpanEnd(block.getData(), localIndex, targetTo);
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
			if(sourceIndices.size()>1)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Can only map from single index values");
			if(!isContinuous(targetIndices))
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Can only map to spans"); //$NON-NLS-1$

			map(sourceIndices.firstIndex(), sourceIndices.firstIndex(),
					targetIndices.firstIndex(), targetIndices.lastIndex());
		}

		@Override
		public void map(IndexSet[] sourceIndices, IndexSet[] targetIndices) {
			if(sourceIndices.length>1 || sourceIndices[0].size()>1)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Can only map from single index values");
			if(!isContinuous(targetIndices))
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Can only map to spans"); //$NON-NLS-1$

			long sourceIndex = firstIndex(sourceIndices);
			map(sourceIndex, sourceIndex, firstIndex(targetIndices), lastIndex(targetIndices));
		}

	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class Builder extends AbstractStoredMappingBuilder<Builder, MappingImplSpanOneToMany> {

		private Integer blockPower;

		protected Builder() {
			// no-op
		}

		public int getBlockPower() {
			return blockPower==null ? DEFAULT_BLOCK_POWER : blockPower.intValue();
		}

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
			PayloadConverter payloadConverter = new PayloadConverterImpl(blockStorage);

			return BufferedIOResource.newBuilder()
				.resource(getResource())
				.blockCache(getBlockCache())
				.cacheSize(getCacheSize())
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
