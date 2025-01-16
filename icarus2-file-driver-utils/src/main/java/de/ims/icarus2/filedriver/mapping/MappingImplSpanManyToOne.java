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
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.binarySearch;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.ensureSorted;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.firstIndex;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.isContinuous;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.lastIndex;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.wrap;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.IcarusUtils.signalUnsupportedMethod;
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
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexCollector;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingReader;
import de.ims.icarus2.model.api.driver.mapping.MappingWriter;
import de.ims.icarus2.model.api.driver.mapping.RequestSettings;
import de.ims.icarus2.model.api.driver.mapping.ReverseMapping;
import de.ims.icarus2.model.api.driver.mapping.WritableMapping;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.strings.ToStringBuilder;

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
public class MappingImplSpanManyToOne extends AbstractStoredMapping<SimpleHeader> implements ReverseMapping {

	public static Builder builder() {
		return new Builder();
	}

	private final IndexBlockStorage blockStorage;

	public static final int DEFAULT_GROUP_POWER = 8;

//	@SuppressWarnings("unused")
//	private static final int INDICES_PER_GROUP = 1<<GROUP_POWER;

	public static final int DEFAULT_BLOCK_POWER = 14;

//	private static final int BLOCK_MASK = (1<<DEFAULT_BLOCK_POWER)-1;
//
//	private static final int ENTRIES_PER_BLOCK = 1<<DEFAULT_BLOCK_POWER;

	private final int groupPower;
	private final int blockPower;
	private final int blockMask;
	private final int entriesPerGroup;
	private final int groupsPerBlock;

	private final Mapping inverseMapping;

	protected MappingImplSpanManyToOne(Builder builder) {
		super(builder);

		blockPower = builder.getBlockPower();
		blockMask = builder.getBlockMask();
		groupsPerBlock = builder.getGroupsPerBlock();
		groupPower = builder.getGroupPower();
		entriesPerGroup = builder.getEntriesPerGroup();
		inverseMapping = builder.getInverseMapping();
		blockStorage = builder.getBlockStorage();
	}

	@Override
	protected void toString(ToStringBuilder sb) {
		sb.add("groupPower", groupPower)
		.add("blockPower", blockPower)
		.add("blockMask", blockMask)
		.add("groupsPerBlock", groupsPerBlock)
		.add("entriesPerGroup", entriesPerGroup)
		.add("blockStorage", blockStorage);
	}

	public IndexBlockStorage getBlockStorage() {
		return blockStorage;
	}

	@Override
	public Mapping getInverseMapping() {
		return inverseMapping;
	}

	public int getGroupsPerBlock() {
		return groupsPerBlock;
	}

	public int getEntriesPerGroup() {
		return entriesPerGroup;
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

	private static final String FIND_NOT_SUPPORTED_MESSAGE =
			"FIND operation not supported since inverse reader is used";

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public class Reader extends ResourceAccessor<Mapping> implements MappingReader {

		protected Reader() {
			super(true);
		}

		// Used for the final step in lookup resolution
		// Represents one-to-many mapping (of spans)
		private final MappingReader inverseReader = inverseMapping.newReader();
		private final Coverage inverseCoverage = ManifestUtils.require(
				inverseMapping.getManifest(), MappingManifest::getCoverage, "coverage");

		/**
		 * @see de.ims.icarus2.filedriver.mapping.AbstractStoredMapping.ResourceAccessor#beginHook()
		 */
		@Override
		protected void beginHook() {
			inverseReader.begin();
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.AbstractStoredMapping.ResourceAccessor#endHook()
		 */
		@Override
		protected void endHook() {
			inverseReader.end();
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getIndicesCount(long, de.ims.icarus2.model.api.driver.mapping.RequestSettings)
		 */
		@Override
		public long getIndicesCount(long sourceIndex, @Nullable RequestSettings settings)
				throws InterruptedException {
			return getHeader().isUsedIndex(sourceIndex) ? 1L : 0L;
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

			Block block = getBlock(id);
			if(block==null) {
				return IcarusUtils.UNSET_LONG;
			}

			long fromTarget = blockStorage.getSpanBegin(block.getData(), localIndex);
			long toTarget = blockStorage.getSpanEnd(block.getData(), localIndex);

			// Delegate to inverse reader for interval search
			return inverseReader.find(fromTarget, toTarget, sourceIndex, RequestSettings.none());
		}

		/**
		 * @return
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(long, de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean lookup(long sourceIndex, IndexCollector collector, @Nullable RequestSettings settings)
				throws InterruptedException {
			requireNonNull(collector);

			if(!getHeader().isUsedIndex(sourceIndex)) {
				return false;
			}

			long result = lookup0(sourceIndex);

			if(result!=IcarusUtils.UNSET_LONG) {
				collector.add(result);
				return true;
			}
			return false;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(long, RequestSettings)
		 */
		@Override
		public IndexSet[] lookup(long sourceIndex, @Nullable RequestSettings settings) throws ModelException,
				InterruptedException {
			if(!getHeader().isUsedIndex(sourceIndex)) {
				return EMPTY;
			}
			return wrap(lookup0(sourceIndex));
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getBeginIndex(long, RequestSettings)
		 */
		@Override
		public long getBeginIndex(long sourceIndex, @Nullable RequestSettings settings) throws ModelException,
				InterruptedException {
			if(!getHeader().isUsedIndex(sourceIndex)) {
				return UNSET_LONG;
			}
			return lookup0(sourceIndex);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getEndIndex(long, RequestSettings)
		 */
		@Override
		public long getEndIndex(long sourceIndex, @Nullable RequestSettings settings) throws ModelException,
				InterruptedException {
			if(!getHeader().isUsedIndex(sourceIndex)) {
				return UNSET_LONG;
			}
			return lookup0(sourceIndex);
		}

		/**
		 * @return
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean lookup(IndexSet[] sourceIndices, IndexCollector collector, @Nullable RequestSettings settings)
				throws InterruptedException {
			requireNonNull(sourceIndices);
			requireNonNull(collector);
			ensureSorted(sourceIndices, settings);

			boolean result = false;

			if(inverseCoverage.isMonotonic() && isContinuous(sourceIndices)) {
				// Easiest case: just get the begin and end index of the original source span
				long beginIndex = lookup0(firstIndex(sourceIndices));
				long endIndex = lookup0(lastIndex(sourceIndices));

				if(beginIndex!=UNSET_LONG && endIndex!=UNSET_LONG) {
					if(beginIndex==endIndex) {
						collector.add(beginIndex);
					} else {
						collector.add(beginIndex, endIndex);
					}
					result = true;
				}
			} else {
				// Current source index to be resolved
				long sourceIndex = UNSET_LONG;

				long sourceLimit = lastIndex(sourceIndices);

				// Flag to signal that we should search for the occurrence of 'sourceIndex' to determine next 'index'
				boolean doScan = false;

				set_loop : for(IndexSet set : sourceIndices) {

					int size = set.size();
					int index = 0;

					while(index <size) {
						if(doScan) {
							index = binarySearch(set, index, size, sourceIndex);

							if(index==UNSET_INT) {
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
						if(targetIndex!=UNSET_LONG) {
							// Report to collector
							collector.add(targetIndex);
							result = true;
							/*
							 * Skip over to next sourceIndex not covered by the span
							 * mapped to 'targetIndex'. This is the end index of that span +1.
							 *
							 * Note: Since we got a valid reverse mapping it is impossible
							 * to get a result of UNSET_LONG from the reverse reader!
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
		public long getBeginIndex(IndexSet[] sourceIndices, @Nullable RequestSettings settings)
				throws InterruptedException {
			requireNonNull(sourceIndices);

			// Optimized handling of monotonic inverseCoverage: use only first source index
			if(inverseCoverage.isMonotonic()) {
				return lookup0(firstIndex(sourceIndices));
			}

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

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getEndIndex(de.ims.icarus2.model.api.driver.indices.IndexSet[], RequestSettings)
		 */
		@Override
		public long getEndIndex(IndexSet[] sourceIndices, @Nullable RequestSettings settings)
				throws InterruptedException {
			requireNonNull(sourceIndices);
			ensureSorted(sourceIndices, settings);

			// Optimized handling of monotonic inverseCoverage: use only last source index
			if(inverseCoverage.isMonotonic()) {
				return lookup0(lastIndex(sourceIndices));
			}

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

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#find(long, long, long, RequestSettings)
		 */
		@SuppressWarnings("boxing")
		@Override
		public long find(long fromSource, long toSource, long targetIndex, @Nullable RequestSettings settings)
				throws InterruptedException {
			return signalUnsupportedMethod(FIND_NOT_SUPPORTED_MESSAGE);
		}

		/**
		 * @return
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#find(long, long, de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@SuppressWarnings("boxing")
		@Override
		public boolean find(long fromSource, long toSource,
				IndexSet[] targetIndices, IndexCollector collector, @Nullable RequestSettings settings)
				throws InterruptedException {
			return signalUnsupportedMethod(FIND_NOT_SUPPORTED_MESSAGE);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#find(long, long, de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.mapping.RequestSettings)
		 */
		@Override
		public IndexSet[] find(long fromSource, long toSource, IndexSet[] targetIndices, RequestSettings settings)
				throws InterruptedException {
			return signalUnsupportedMethod(FIND_NOT_SUPPORTED_MESSAGE);
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

		private void map0(long group, long targetIndex) {
			// Block id for group
			int id = id(group);
			// Index of data within block
			int localIndex = localIndex(group);

			Block block = getBlock(id);
			Object buffer = block.getData();

			long currentFrom = blockStorage.getSpanBegin(buffer, localIndex);
			long currentTo = blockStorage.getSpanEnd(buffer, localIndex);

			if(currentFrom==-1) {
				// New entry
				currentFrom = currentTo = targetIndex;
				getHeader().growSize();
			} else {
				// Old entry => update bounds
				currentFrom = Math.min(currentFrom, targetIndex);
				currentTo = Math.max(currentTo, targetIndex);
			}

			blockStorage.setSpanBegin(buffer, localIndex, currentFrom);
			blockStorage.setSpanEnd(buffer, localIndex, currentTo);
			lockBlock(block, localIndex);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingWriter#map(long, long, long)
		 */
		@Override
		public void map(long sourceFrom, long sourceTo, long targetFrom, long targetTo) {
			if(targetFrom!=targetTo)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT,
						"Can only map to single index values");

			SimpleHeader header = getHeader();
			header.updateUsedIndex(sourceFrom);
			header.updateTargetIndex(targetFrom);

			long targetIndex = targetFrom;

			// Source group of index
			long group1 = group(sourceFrom);

			// Span begin
			map0(group1, targetIndex);

			// If source was a span of length 1 we don't have to go further
			if(sourceFrom==sourceTo) {
				return;
			}

			header.updateUsedIndex(sourceTo);

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
			requireNonNull(sourceIndices);
			requireNonNull(targetIndices);

			if(targetIndices.size()>1)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Can only map to single index values");
			if(!isContinuous(sourceIndices))
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Can only map from spans");

			map(sourceIndices.firstIndex(), sourceIndices.lastIndex(),
					targetIndices.firstIndex(), targetIndices.lastIndex());
		}

		@Override
		public void map(IndexSet[] sourceIndices, IndexSet[] targetIndices) {
			requireNonNull(sourceIndices);
			requireNonNull(targetIndices);

			if(targetIndices.length>1 || targetIndices[0].size()>1)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Can only map to single index values");
			if(!isContinuous(sourceIndices))
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Can only map from spans");

			long targetIndex = firstIndex(targetIndices);
			map(firstIndex(sourceIndices), lastIndex(sourceIndices), targetIndex, targetIndex);
		}

	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	@Api(type=ApiType.BUILDER)
	public static class Builder extends AbstractStoredMappingBuilder<Builder, MappingImplSpanManyToOne> {

		private Integer blockPower;
		private Integer groupPower;
		private Mapping inverseMapping;

		protected Builder() {
			// no-op
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder blockPower(int blockPower) {
			checkArgument(blockPower>0);
			checkState(this.blockPower==null);

			this.blockPower = Integer.valueOf(blockPower);

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder groupPower(int groupPower) {
			checkArgument(groupPower>0);
			checkState(this.groupPower==null);

			this.groupPower = Integer.valueOf(groupPower);

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder inverseMapping(Mapping inverseMapping) {
			requireNonNull(inverseMapping);
			checkState(this.inverseMapping==null);

			this.inverseMapping = inverseMapping;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.SETTER, defaultValue="14")
		public int getBlockPower() {
			return blockPower==null ? DEFAULT_BLOCK_POWER : blockPower.intValue();
		}

		@Guarded(methodType=MethodType.SETTER, defaultValue="8")
		public int getGroupPower() {
			return groupPower==null ? DEFAULT_GROUP_POWER : groupPower.intValue();
		}

		public Mapping getInverseMapping() {
			return inverseMapping;
		}

		public int getBlockMask() {
			return (1<<getBlockPower())-1;
		}

		public int getGroupsPerBlock() {
			return (1<<getBlockPower());
		}

		public int getEntriesPerGroup() {
			return (1<<getGroupPower());
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.AbstractStoredMapping.AbstractStoredMappingBuilder#createBufferedIOResource()
		 */
		@Override
		public BufferedIOResource createBufferedIOResource() {
			IndexBlockStorage blockStorage = getBlockStorage();
			int bytesPerBlock = getGroupsPerBlock()*blockStorage.spanSize();
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
