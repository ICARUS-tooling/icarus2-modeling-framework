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
package de.ims.icarus2.filedriver.mapping;

import static de.ims.icarus2.model.api.driver.indices.IndexUtils.firstIndex;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.lastIndex;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static java.util.Objects.requireNonNull;

import java.util.function.LongBinaryOperator;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.io.BufferedIOResource;
import de.ims.icarus2.filedriver.io.BufferedIOResource.Block;
import de.ims.icarus2.filedriver.io.BufferedIOResource.PayloadConverter;
import de.ims.icarus2.filedriver.io.BufferedIOResource.SimpleHeader;
import de.ims.icarus2.filedriver.io.Range;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexCollector;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingReader;
import de.ims.icarus2.model.api.driver.mapping.MappingWriter;
import de.ims.icarus2.model.api.driver.mapping.RequestSettings;
import de.ims.icarus2.model.api.driver.mapping.WritableMapping;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.strings.ToStringBuilder;

/**
 * Implements a one-to-one mapping.
 * <p>
 * Storage is managed in a way that for any item {@code i} the mapped value
 * is also stored at index {@code i} of the underlying virtual array.
 *
 * @author Markus Gärtner
 *
 */
public class MappingImplOneToOne extends AbstractStoredMapping<SimpleHeader> {

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

	protected MappingImplOneToOne(Builder builder) {
		super(builder);

		blockPower = builder.getBlockPower();
		blockMask = builder.getBlockMask();
		entriesPerBlock = builder.getEntriesPerBlock();
		blockStorage = builder.getBlockStorage();
	}

	@Override
	protected void toString(ToStringBuilder sb) {
		sb.add("blockPower", blockPower)
		.add("blockMask", blockMask)
		.add("entriesPerBlock", entriesPerBlock)
		.add("blockStorage", blockStorage);
	}

	public int getBlockPower() {
		return blockPower;
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

		private final Coverage coverage = ManifestUtils.require(
				getManifest(), MappingManifest::getCoverage, "coverage");

		protected Reader() {
			super(true);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getIndicesCount(long, de.ims.icarus2.model.api.driver.mapping.RequestSettings)
		 */
		@Override
		public long getIndicesCount(long sourceIndex, RequestSettings settings)
				throws InterruptedException {
			return 1L;
		}

		private long lookup0(long sourceIndex) {
			// Query header first (does not involve I/O)
			if(!getHeader().isUsedIndex(sourceIndex)) {
				return UNSET_LONG;
			}

			// Now query actual index
			int id = id(sourceIndex);
			int localIndex = localIndex(sourceIndex);

			Block block = getBlock(id);
			if(block==null) {
				return IcarusUtils.UNSET_LONG;
			}

			return blockStorage.getEntry(block.getData(), localIndex);
		}

		/**
		 * @return
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(long, de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean lookup(long sourceIndex, IndexCollector collector, RequestSettings settings) throws ModelException {
			checkSourceIndex(sourceIndex);
			requireNonNull(collector);

			long targetIndex = lookup0(sourceIndex);

			if(targetIndex==IcarusUtils.UNSET_LONG) {
				return false;
			}

			collector.add(targetIndex);
			return true;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(long, RequestSettings)
		 */
		@Override
		public IndexSet[] lookup(long sourceIndex, RequestSettings settings) throws ModelException {
			checkSourceIndex(sourceIndex);
			return IndexUtils.wrap(lookup0(sourceIndex));
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getBeginIndex(long, RequestSettings)
		 */
		@Override
		public long getBeginIndex(long sourceIndex, RequestSettings settings) throws ModelException {
			checkSourceIndex(sourceIndex);
			return lookup0(sourceIndex);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getEndIndex(long, RequestSettings)
		 */
		@Override
		public long getEndIndex(long sourceIndex, RequestSettings settings) throws ModelException {
			checkSourceIndex(sourceIndex);
			return lookup0(sourceIndex);
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
			requireNonNull(sourceIndices);
			requireNonNull(collector);

			IndexUtils.checkSorted(sourceIndices);

			boolean result = false;

			if(coverage.isMonotonic()) {
				if(coverage.isTotal() && IndexUtils.isContinuous(sourceIndices)) {
					// Special case of a single big span -> result is again a span
					long beginIndex = getBeginIndex(firstIndex(sourceIndices), null);
					long endIndex = getEndIndex(lastIndex(sourceIndices), null);
					if(beginIndex!=IcarusUtils.UNSET_LONG && endIndex!=IcarusUtils.UNSET_LONG) {
						collector.add(beginIndex, endIndex);
						result = true;
					}
				} else {

					// Requires checks on all individual index sets
					for(IndexSet indices : sourceIndices) {
						checkInterrupted();

						if(coverage.isTotal() && IndexUtils.isContinuous(indices)) {
							// Special case of a single big span -> result is again a span
							long beginIndex = getBeginIndex(indices.firstIndex(), null);
							long endIndex = getEndIndex(indices.lastIndex(), null);
							if(beginIndex!=IcarusUtils.UNSET_LONG && endIndex!=IcarusUtils.UNSET_LONG) {
								collector.add(beginIndex, endIndex);
								result = true;
							}
						} else {
							// Expensive version: traverse values and add individual target spans
							for(int i=0; i<indices.size(); i++) {
								long sourceIndex = indices.indexAt(i);
								long targetIndex = lookup0(sourceIndex);
								if(targetIndex!=IcarusUtils.UNSET_LONG) {
									collector.add(targetIndex);
									result = true;
								}
							}
						}

					}
				}

			} else {
				// Expensive version: traverse ALL individual indices and add target indices.
				// Remember however, that we still have an injective index function, so no
				// duplicate checks required!
				for(IndexSet indices : sourceIndices) {
					checkInterrupted();

					// Expensive version: traverse values and add individual target spans
					for(int i=0; i<indices.size(); i++) {
						long sourceIndex = indices.indexAt(i);
						long targetIndex = lookup0(sourceIndex);
						if(targetIndex!=IcarusUtils.UNSET_LONG) {
							collector.add(targetIndex);
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
			requireNonNull(sourceIndices);

			IndexUtils.checkSorted(sourceIndices);

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
		public long getEndIndex(IndexSet[] sourceIndices, RequestSettings settings)
				throws InterruptedException {
			requireNonNull(sourceIndices);

			IndexUtils.checkSorted(sourceIndices);

			// Optimized handling of monotonic coverage: use only last source index
			if(coverage.isMonotonic()) {
				return getEndIndex(lastIndex(sourceIndices), null);
			}

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

		/**
		 * @return
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#find(long, long, de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean find(final long fromSource, final long toSource,
				final IndexSet[] targetIndices, final IndexCollector collector, RequestSettings settings)
				throws InterruptedException {
			checkSourceIndex(fromSource);
			checkSourceIndex(toSource);
			requireNonNull(targetIndices);
			requireNonNull(collector);

			IndexUtils.checkSorted(targetIndices);

			// Restrict source range with header information
			Range sourceRange = getHeader().getUsedIndices(fromSource, toSource);
			if(sourceRange.isUnset()) {
				// No overlap of estimated range and our actually mapped sources
				return false;
			}

			boolean result = false;

			int idFrom = id(sourceRange.getMin());
			int idTo = id(sourceRange.getMax());
			int localFrom = localIndex(sourceRange.getMin());
			int localTo = localIndex(sourceRange.getMax())+1; // need +1 due to internal methods using exclusive end index

			if(coverage.isMonotonic()) {
				if(coverage.isTotal() && IndexUtils.isContinuous(targetIndices)) {
					// Single big span -> grab begin and end indices and report as span
					result = findSpan(idFrom, idTo, localFrom, localTo,
							firstIndex(targetIndices), lastIndex(targetIndices), collector);
				} else {
					// Check for all given IndexSet instances separately
					for(IndexSet indices : targetIndices) {
						checkInterrupted();
						if(coverage.isTotal() && IndexUtils.isContinuous(indices)) {
							// Single big span -> grab begin and end indices and report as span
							result |= findSpan(idFrom, idTo, localFrom, localTo,
									indices.firstIndex(), indices.lastIndex(), collector);
						} else {
							result |= findMulti(idFrom, idTo, localFrom, localTo, indices, collector);
						}
					}
				}
			} else {
				// Expensive traversal without chance of optimization
				for(IndexSet indices : targetIndices) {
					result |= findMulti(idFrom, idTo, localFrom, localTo, indices, collector);
				}
			}

			// All happy-path (not early exit) variants lead here
			return result;
		}

		private boolean findSpan(int idFrom, int idTo, int localFrom, int localTo, long spanBegin, long spanEnd, IndexCollector collector) {
			long sourceBegin = findSingle(idFrom, idTo, localFrom, localTo, spanBegin);
			if(sourceBegin==UNSET_LONG) {
				return false;
			}
			long sourceEnd = findSingle(idFrom, idTo, localFrom, localTo, spanEnd);
			assert sourceEnd>=sourceBegin;
			if (sourceEnd==UNSET_LONG) {
				return false;
			}

			// Give collector the chance to optimize and just throw him a big span
			collector.add(sourceBegin, sourceEnd);
			return true;
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

		private boolean findMulti(int idFrom, int idTo, int localFrom, int localTo,
				IndexSet indices, IndexCollector collector) {
			boolean result = false;
			for (int i = 0; i < indices.size(); i++) {
				long targetIndex = indices.indexAt(i);
				long sourceIndex = UNSET_LONG;

				// Special case of a single block search
				if(idFrom==idTo && (sourceIndex = find0(idFrom, localFrom, localTo, targetIndex))!=UNSET_LONG) {
					collector.accept(sourceIndex);
					result = true;
					continue;
				}

				// Check first block
				if((sourceIndex = find0(idFrom, localFrom, entriesPerBlock, targetIndex))!=UNSET_LONG) {
					collector.accept(sourceIndex);
					result = true;
					continue;
				}

				// Check last block
				if((sourceIndex = find0(idTo, 0, localTo, targetIndex))!=UNSET_LONG) {
					collector.accept(sourceIndex);
					result = true;
					continue;
				}

				// Iterate intermediate blocks
				intermediate : for(int id=idFrom+1; id<idTo; id++) {
					// Now always include the entire block to search
					if((sourceIndex = find0(id, 0, entriesPerBlock, targetIndex))!=UNSET_LONG) {
						collector.accept(sourceIndex);
						result = true;
						continue intermediate;
					}
				}
			}
			return result;
		}

		/**
		 * Search for specified {@code targetIndex} in block specified by {@code id},
		 * limiting the range to {@code localFrom} - {@code localTo}.
		 *
		 * @param id
		 * @param localFrom
		 * @param localTo
		 * @param targetIndex
		 * @return
		 */
		private long find0(int id, int localFrom, int localTo, long targetIndex) {

			Block block = getBlock(id);

			if(block==null) {
				return IcarusUtils.UNSET_LONG;
			}

			int localIndex = UNSET_INT;

			if(coverage.isMonotonic()) {
				localIndex = blockStorage.sparseFindSorted(block.getData(), localFrom, localTo, targetIndex);
			} else {
				localIndex = blockStorage.find(block.getData(), localFrom, localTo, targetIndex);
			}

			return translate(id, localIndex);
		}

		/**
		 * Translate the position {@code localIndex} in block with given {@code id}
		 * into a global index value.
		 */
		private long translate(int id, int localIndex) {
			return localIndex==UNSET_INT ? UNSET_LONG : id*entriesPerBlock + localIndex;
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
		 * Checks that both spans are of length {@code 1} and then delegates to
		 * {@link #map(long, long, long, long)}
		 *
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingWriter#map(long, long, long)
		 */
		@Override
		public void map(long sourceFrom, long sourceTo, long targetFrom, long targetTo) {
			if(sourceFrom!=sourceTo)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT,
						"Can only map from single index values");
			if(targetFrom!=targetTo)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT,
						"Can only map to single index values");

			map(sourceFrom, targetFrom);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingWriter#map(long, long)
		 */
		@Override
		public void map(long sourceIndex, long targetIndex) {

			int id = id(sourceIndex);
			int localIndex = localIndex(sourceIndex);

			Block block = getBlock(id);
			assert block!=null : "missing block for id "+id;
			long oldValue = blockStorage.setEntry(block.getData(), localIndex, targetIndex);
			if(oldValue==targetIndex) {
				return;
			}
			lockBlock(block, localIndex);

			SimpleHeader header = getHeader();
			if(oldValue==UNSET_LONG) {
				header.growSize();
			}
			header.updateUsedIndex(sourceIndex);
			header.updateTargetIndex(targetIndex);
		}

		/**
		 * For each
		 *
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingWriter#map(de.ims.icarus2.model.api.driver.indices.IndexSet, de.ims.icarus2.model.api.driver.indices.IndexSet)
		 *
		 * @throws ModelException in case the two index sets are of different size
		 */
		@Override
		public void map(IndexSet sourceIndices, IndexSet targetIndices) {
			LongBinaryOperator action = (sourceIndex, targetIndex) -> {
				map(sourceIndex, targetIndex);
				return 1L;
			};

			IndexUtils.forEachPair(sourceIndices, targetIndices, action);
		}

		@Override
		public void map(IndexSet[] sourceIndices, IndexSet[] targetIndices) {
			LongBinaryOperator action = (sourceIndex, targetIndex) -> {
				map(sourceIndex, targetIndex);
				return 1L;
			};

			IndexUtils.forEachPair(sourceIndices, targetIndices, action);
		}

	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class Builder extends AbstractStoredMappingBuilder<Builder, MappingImplOneToOne> {

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
			PayloadConverter payloadConverter = new ValueConverter(blockStorage);

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
		protected MappingImplOneToOne create() {
			return new MappingImplOneToOne(this);
		}

	}
}
