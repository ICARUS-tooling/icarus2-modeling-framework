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
package de.ims.icarus2.filedriver.mapping;

import static de.ims.icarus2.model.api.driver.indices.IndexUtils.binarySearch;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.ensureSorted;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.firstIndex;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.isContinuous;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.lastIndex;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.wrap;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotNull;
import static de.ims.icarus2.util.Conditions.checkState;
import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.io.BufferedIOResource;
import de.ims.icarus2.filedriver.io.BufferedIOResource.Block;
import de.ims.icarus2.filedriver.io.BufferedIOResource.PayloadConverter;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexCollector;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingReader;
import de.ims.icarus2.model.api.driver.mapping.MappingWriter;
import de.ims.icarus2.model.api.driver.mapping.RequestSettings;
import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;

/**
 * Mapping implementation that relies on another reverse mapping to map contents of spans to
 * single indices. The span elements are first grouped into blocks of equal size and then the
 * first and last original source items are stored as the "reverse span".
 * To perform a mapping lookup the implementation then first looks which source indices are
 * potential matches and then delegates to an inverse mapping provided at construction time
 * to do the final searching.
 *
 * @author Markus Gärtner
 *
 */
public class MappingImplSpanManyToOne extends AbstractStoredMapping {

	private final IndexBlockStorage blockStorage;

	private static final int DEFAULT_GROUP_POWER = 8;

//	@SuppressWarnings("unused")
//	private static final int INDICES_PER_GROUP = 1<<GROUP_POWER;

	private static final int DEFAULT_BLOCK_POWER = 14;

//	private static final int BLOCK_MASK = (1<<DEFAULT_BLOCK_POWER)-1;
//
//	private static final int ENTRIES_PER_BLOCK = 1<<DEFAULT_BLOCK_POWER;

	private final int groupPower;
	private final int blockPower;
	private final int blockMask;
	private final int entriesPerBlock;

	private final Mapping inverseMapping;

	protected MappingImplSpanManyToOne(Builder builder) {
		super(builder);

		blockPower = builder.getBlockPower();
		blockMask = builder.getBlockMask();
		entriesPerBlock = builder.getEntriesPerBlock();
		groupPower = builder.getGroupPower();
		inverseMapping = builder.getInverseMapping();
		blockStorage = builder.getBlockStorage();
	}

	@Override
	protected void toString(StringBuilder sb) {
		sb.append(" groupPower=").append(groupPower)
		.append(" blockPower=").append(blockPower)
		.append(" blockMask=").append(blockMask)
		.append(" entriesPerBlock=").append(entriesPerBlock)
		.append(" blockStorage=").append(blockStorage);
	}

	public int getBlockPower() {
		return blockPower;
	}

	public IndexBlockStorage getBlockStorage() {
		return blockStorage;
	}

	public int getGroupPower() {
		return groupPower;
	}

	public Mapping getInverseMapping() {
		return inverseMapping;
	}

	public int getBlockMask() {
		return blockMask;
	}

	public int getEntriesPerBlock() {
		return entriesPerBlock;
	}

	/**
	 * Returns group id for the given index, with {@value #INDICES_PER_GROUP} indices
	 * per group.
	 */
	private long group(long index) {
		return index>>>groupPower;
	}

	/**
	 * Translates index values into their respective block ids.
	 */
	private int id(long index) {
		return (int) (index>>>blockPower);
	}

	/**
	 * Extracts from a given group {@code index} its position within the
	 * hosting block.
	 */
	private int localIndex(long group) {
		return (int)(group & blockMask);
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

		// Used for the final step in lookup resolution
		// Represents one-to-many mapping (of spans)
		private final MappingReader inverseReader = inverseMapping.newReader();
		private final Coverage inverseCoverage = inverseMapping.getManifest().getCoverage();

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getIndicesCount(long, de.ims.icarus2.model.api.driver.mapping.RequestSettings)
		 */
		@Override
		public long getIndicesCount(long sourceIndex, RequestSettings settings)
				throws InterruptedException {
			return 1L;
		}

		/**
		 * Resolves the group a given source index belongs to and fetches the
		 * reverse mapping for that group, which in turn describes the lower and
		 * upper index limits in the target space of this index. Finally calls the
		 * inverse reader to run a search for a mapping index in the given interval.
		 */
		private long lookup0(long sourceIndex) throws InterruptedException {
			// Source group of index
			long group = group(sourceIndex);
			// Block id for group
			int id = id(group);
			// Index of data within block
			int localIndex = localIndex(group);

			Block block = getBlock(id, false);
			if(block==null) {
				return NO_INDEX;
			}

			long fromTarget = blockStorage.getSpanBegin(block.getData(), localIndex);
			long toTarget = blockStorage.getSpanEnd(block.getData(), localIndex);

			// Delegate to inverse reader for interval search
			return inverseReader.find(fromTarget, toTarget, sourceIndex, null);
		}

		/**
		 * @return
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(long, de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean lookup(long sourceIndex, IndexCollector collector, RequestSettings settings)
				throws InterruptedException {
			long result = lookup0(sourceIndex);

			if(result!=NO_INDEX) {
				collector.add(result);
				return true;
			} else {
				return false;
			}
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(long, RequestSettings)
		 */
		@Override
		public IndexSet[] lookup(long sourceIndex, RequestSettings settings) throws ModelException,
				InterruptedException {
			return wrap(lookup0(sourceIndex));
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getBeginIndex(long, RequestSettings)
		 */
		@Override
		public long getBeginIndex(long sourceIndex, RequestSettings settings) throws ModelException,
				InterruptedException {
			return lookup0(sourceIndex);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getEndIndex(long, RequestSettings)
		 */
		@Override
		public long getEndIndex(long sourceIndex, RequestSettings settings) throws ModelException,
				InterruptedException {
			return lookup0(sourceIndex);
		}

		/**
		 * @return
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean lookup(IndexSet[] sourceIndices, IndexCollector collector, RequestSettings settings)
				throws InterruptedException {
			ensureSorted(sourceIndices);

			boolean result = false;

			if(inverseCoverage.isMonotonic() && isContinuous(sourceIndices)) {
				// Easiest case: just get the begin and end index of the original source span
				long beginIndex = lookup0(firstIndex(sourceIndices));
				long endIndex = lookup0(lastIndex(sourceIndices));

				if(beginIndex!=NO_INDEX && endIndex!=NO_INDEX) {
					if(beginIndex==endIndex) {
						collector.add(beginIndex);
					} else {
						collector.add(beginIndex, endIndex);
					}
					result = true;
				}
			} else {
				// Current source index to be resolved
				long sourceIndex = NO_INDEX;

				long sourceLimit = lastIndex(sourceIndices);

				// Flag to signal that we should search for the occurrence of 'sourceIndex' to determine next 'index'
				boolean doScan = false;

				set_loop : for(IndexSet set : sourceIndices) {

					int size = set.size();
					int index = 0;

					while(index <size) {
						if(doScan) {
							index = binarySearch(set, index, size, sourceIndex);

							if(index==-1) {
								break set_loop;
							} else if(index<0) {
								index = -(index+1);

								/*
								 *  If index has to be in the next index set, just skip over.
								 *  This means we can leave the doScan flag active since a valid
								 *  local index hasn't been found yet.
								 */
								if(index>=size) {
									continue set_loop;
								}
							}
						}

						sourceIndex = set.indexAt(index);

						long targetIndex = lookup0(sourceIndex);
						if(targetIndex!=NO_INDEX) {
							// Report to collector
							collector.add(targetIndex);
							/*
							 * Skip over to next sourceIndex not covered by the span
							 * mapped to 'targetIndex'. This is the end index of that span +1.
							 *
							 * Note: Since we got a valid reverse mapping it is impossible
							 * to get a result of NO_INDEX from the reverse reader!
							 */
							sourceIndex = inverseReader.getEndIndex(targetIndex, null) + 1;

							if(sourceIndex>sourceLimit) {
								break set_loop;
							}

							// To actually profit from that strategy, we need to force a new scan in next iteration
							doScan = true;
						} else {
							// Just make one step
							index++;
							// No scanning, blindly take the next local value and try to reverse map
							doScan = false;
						}
					}
				}
			}

			return result;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getBeginIndex(de.ims.icarus2.model.api.driver.indices.IndexSet[], RequestSettings)
		 */
		@Override
		public long getBeginIndex(IndexSet[] sourceIndices, RequestSettings settings)
				throws InterruptedException {

			// Optimized handling of monotonic inverseCoverage: use only first source index
			if(inverseCoverage.isMonotonic()) {
				return lookup0(firstIndex(sourceIndices));
			} else {
				// Expensive alternative: traverse all indices
				long result = Long.MAX_VALUE;

				for(IndexSet indices : sourceIndices) {
					for(int i=0; i<indices.size(); i++) {
						long sourceIndex = indices.indexAt(i);
						result = Math.min(result, lookup0(sourceIndex));
					}
				}

				return result;
			}
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getEndIndex(de.ims.icarus2.model.api.driver.indices.IndexSet[], RequestSettings)
		 */
		@Override
		public long getEndIndex(IndexSet[] sourceIndices, RequestSettings settings)
				throws InterruptedException {
			ensureSorted(sourceIndices);

			// Optimized handling of monotonic inverseCoverage: use only last source index
			if(inverseCoverage.isMonotonic()) {
				return lookup0(lastIndex(sourceIndices));
			} else {
				// Expensive alternative: traverse all indices
				long result = Long.MIN_VALUE;

				for(IndexSet indices : sourceIndices) {
					for(int i=0; i<indices.size(); i++) {
						long sourceIndex = indices.indexAt(i);
						result = Math.max(result, lookup0(sourceIndex));
					}
				}

				return result;
			}
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#find(long, long, long, RequestSettings)
		 */
		@Override
		public long find(long fromSource, long toSource, long targetIndex, RequestSettings settings)
				throws InterruptedException {
			throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
					"FIND operation not supported since inverse reader is used");

//			long spanBegin = inverseReader.getBeginIndex(targetIndex);
//			long spanEnd = inverseReader.getEndIndex(targetIndex);
//
//			// If span is outside our window, treat it as NO_INDEX result
//			if(spanBegin==NO_INDEX || spanEnd==NO_INDEX
//					|| spanBegin>toSource || spanEnd<fromSource) {
//				return NO_INDEX;
//			}
//
//			return spanBegin;
		}

		/**
		 * @return
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#find(long, long, de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean find(long fromSource, long toSource,
				IndexSet[] targetIndices, IndexCollector collector, RequestSettings settings)
				throws InterruptedException {
			throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
					"FIND operation not supported since inverse reader is used");

//			boolean result = false;
//
//			if(inverseCoverage.isMonotonic()) {
//				/*
//				 * Fetch the area covered by targetIndices and shrink it
//				 * to be inside the defined search space (fromSource, toSource).
//				 * If the remaining span is non-empty, report it to the collector.
//				 *
//				 * FIXME: this could potentially lead to an overestimate of index values
//				 * being reported, since we have no direct way to check whether the original
//				 * source indices formed a continuous collection of spans!
//				 *
//				 * EDIT: above warning only applies to source index collections that are
//				 * not 'total'
//				 */
//
//				long spanBegin = inverseReader.getBeginIndex(firstIndex(targetIndices));
//				long spanEnd = inverseReader.getEndIndex(lastIndex(targetIndices));
//
//				if(spanBegin!=NO_INDEX && spanEnd!=NO_INDEX) {
//					fromSource = Math.max(fromSource, spanBegin);
//					toSource = Math.min(toSource, spanEnd);
//
//					if(fromSource<=toSource) {
//						collector.add(fromSource, toSource);
//						result = true;
//					}
//				}
//			} else {
//				//TODO expensive iterating!!!
//			}
//
//			return result;
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

		private void map0(long group, long targetIndex) {
			// Block id for group
			int id = id(group);
			// Index of data within block
			int localIndex = localIndex(group);

			Block block = getBlock(id, true);
			Object buffer = block.getData();

			long currentFrom = blockStorage.getSpanBegin(buffer, localIndex);
			long currentTo = blockStorage.getSpanEnd(buffer, localIndex);

			if(currentFrom==-1) {
				// New entry
				currentFrom = currentTo = targetIndex;
			} else {
				// Old entry => update bounds
				currentFrom = Math.min(currentFrom, targetIndex);
				currentTo = Math.max(currentTo, targetIndex);
			}

			blockStorage.setSpanBegin(buffer, localIndex, currentFrom);
			blockStorage.setSpanEnd(buffer, localIndex, currentTo);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingWriter#map(long, long, long)
		 */
		@Override
		public void map(long sourceFrom, long sourceTo, long targetFrom, long targetTo) {
			if(targetFrom!=targetTo)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT,
						"Can only map to single index values");

			long targetIndex = targetFrom;

			// Source group of index
			long group1 = group(sourceFrom);

			// Span begin
			map0(group1, targetIndex);

			// If source was a span of length 1 we don't have to go further
			if(sourceFrom==sourceTo) {
				return;
			}

			// Source group of index
			long group2 = group(sourceTo);

			if(group1==group2) {
				return;
			}

			// Span end
			map0(group2, targetIndex);
		}

		/**
		 * This implementation delegates to {@link #map(long, long, long)} assuming that the
		 * {@code targetIndex} should be mapped to a span of length {@code 1}.
		 *
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingWriter#map(long, long)
		 */
		@Override
		public void map(long sourceIndex, long targetIndex) {
			map(sourceIndex, sourceIndex, targetIndex, targetIndex);
		}

		@Override
		public void map(IndexSet sourceIndices, IndexSet targetIndices) {
			if(targetIndices.size()>1)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Can only map to single index values");
			if(!isContinuous(sourceIndices))
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Can only map from spans"); //$NON-NLS-1$

			map(sourceIndices.firstIndex(), sourceIndices.firstIndex(),
					targetIndices.firstIndex(), targetIndices.lastIndex());
		}

		@Override
		public void map(IndexSet[] sourceIndices, IndexSet[] targetIndices) {
			if(targetIndices.length>1 || targetIndices[0].size()>1)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Can only map to single index values");
			if(!isContinuous(sourceIndices))
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Can only map from spans"); //$NON-NLS-1$

			long targetIndex = firstIndex(targetIndices);
			map(firstIndex(sourceIndices), lastIndex(sourceIndices), targetIndex, targetIndex);
		}

	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class Builder extends StoredMappingBuilder<Builder, MappingImplSpanManyToOne> {

		private Integer blockPower;
		private Integer groupPower;
		private Mapping inverseMapping;

		public Builder blockPower(int blockPower) {
			checkArgument(blockPower>0);
			checkState(this.blockPower==null);

			this.blockPower = Integer.valueOf(blockPower);

			return thisAsCast();
		}

		public Builder groupPower(int groupPower) {
			checkArgument(groupPower>0);
			checkState(this.groupPower==null);

			this.groupPower = Integer.valueOf(groupPower);

			return thisAsCast();
		}

		public Builder inverseMapping(Mapping inverseMapping) {
			checkNotNull(inverseMapping);
			checkState(this.inverseMapping==null);

			this.inverseMapping = inverseMapping;

			return thisAsCast();
		}

		public int getBlockPower() {
			return blockPower==null ? DEFAULT_BLOCK_POWER : blockPower.intValue();
		}

		public int getGroupPower() {
			return groupPower==null ? DEFAULT_GROUP_POWER : groupPower.intValue();
		}

		public Mapping getInverseMapping() {
			return inverseMapping;
		}

		public int getBlockMask() {
			return (1<<getBlockPower())-1;
		}

		public int getEntriesPerBlock() {
			return (1<<getBlockPower());
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.AbstractStoredMapping.StoredMappingBuilder#createBufferedIOResource()
		 */
		@Override
		public BufferedIOResource createBufferedIOResource() {
			IndexBlockStorage blockStorage = getBlockStorage();
			int bytesPerBlock = getEntriesPerBlock()*blockStorage.spanSize();
			PayloadConverter payloadConverter = new PayloadConverterImpl(blockStorage);

			return new BufferedIOResource.Builder()
				.resource(getResource())
				.blockCache(getBlockCache())
				.cacheSize(getCacheSize())
				.bytesPerBlock(bytesPerBlock)
				.payloadConverter(payloadConverter)
				.build();
		}

		@Override
		protected void validate() {
			super.validate();
			checkState("Missing inverse mapping", inverseMapping!=null);
		}

		@Override
		protected MappingImplSpanManyToOne create() {
			return new MappingImplSpanManyToOne(this);
		}

	}
}
