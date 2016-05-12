/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 429 $
 * $Date: 2015-10-07 17:08:17 +0200 (Mi, 07 Okt 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/driver/mapping/MappingImplOneToOne.java $
 *
 * $LastChangedDate: 2015-10-07 17:08:17 +0200 (Mi, 07 Okt 2015) $
 * $LastChangedRevision: 429 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.filedriver.mapping;

import static de.ims.icarus2.model.api.driver.indices.IndexUtils.firstIndex;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.lastIndex;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.LongBinaryOperator;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexCollector;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingReader;
import de.ims.icarus2.model.api.driver.mapping.MappingWriter;
import de.ims.icarus2.model.api.driver.mapping.RequestSettings;
import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;

/**
 * Implements a one-to-one mapping
 *
 * @author Markus Gärtner
 * @version $Id: MappingImplOneToOne.java 429 2015-10-07 15:08:17Z mcgaerty $
 *
 */
public class MappingImplOneToOne extends AbstractStoredMapping {

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

		int blockPower = builder.getBlockPower();

		this.blockPower = blockPower;
		blockMask = (1<<blockPower)-1;
		entriesPerBlock = 1<<blockPower;

		blockStorage = IndexBlockStorage.forValueType(builder.getValueType());
		setBytesPerBlock(entriesPerBlock * blockStorage.entrySize());
	}

	@Override
	protected void toString(StringBuilder sb) {
		sb.append(" blockPower=").append(blockPower)
		.append(" blockMask=").append(blockMask)
		.append(" entriesPerBlock=").append(entriesPerBlock)
		.append(" blockStorage=").append(blockStorage);
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
	 * @see de.ims.icarus2.filedriver.io.BufferedIOResource#write(java.lang.Object, java.nio.ByteBuffer, int)
	 */
	@Override
	protected void write(Object source, ByteBuffer buffer, int length)
			throws IOException {
		blockStorage.write(source, buffer, 0, length);
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.BufferedIOResource#read(java.lang.Object, java.nio.ByteBuffer)
	 */
	@Override
	protected int read(Object target, ByteBuffer buffer) throws IOException {
		int length = buffer.remaining()/blockStorage.spanSize();
		blockStorage.read(target, buffer, 0, length);
		return length;
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.BufferedIOResource#newBlockData()
	 */
	@Override
	protected Object newBlockData() {
		return blockStorage.createBuffer(getBytesPerBlock());
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

	public class Reader extends ReadAccessor<Mapping> implements MappingReader {

		private final Coverage coverage = getManifest().getCoverage();

		private long lookup0(long sourceIndex) {
			int id = id(sourceIndex);
			int localIndex = localIndex(sourceIndex);

			Block block = getBlock(id, false);
			if(block==null) {
				return NO_INDEX;
			}

			return blockStorage.getEntry(block.getData(), localIndex);
		}

		/**
		 * @return
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(long, de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean lookup(long sourceIndex, IndexCollector collector, RequestSettings settings) throws ModelException {
			long targetIndex = lookup0(sourceIndex);

			if(targetIndex==NO_INDEX) {
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
			return IndexUtils.wrap(lookup0(sourceIndex));
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getBeginIndex(long, RequestSettings)
		 */
		@Override
		public long getBeginIndex(long sourceIndex, RequestSettings settings) throws ModelException {
			return lookup0(sourceIndex);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getEndIndex(long, RequestSettings)
		 */
		@Override
		public long getEndIndex(long sourceIndex, RequestSettings settings) throws ModelException {
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

			boolean result = false;

			if(coverage.isMonotonic()) {
				if(coverage.isTotal() && IndexUtils.isContinuous(sourceIndices)) {
					// Special case of a single big span -> result is again a span
					long beginIndex = getBeginIndex(firstIndex(sourceIndices), null);
					long endIndex = getEndIndex(firstIndex(sourceIndices), null);
					if(beginIndex!=NO_INDEX && endIndex!=NO_INDEX) {
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
							if(beginIndex!=NO_INDEX && endIndex!=NO_INDEX) {
								collector.add(beginIndex, endIndex);
								result = true;
							}
						} else {
							// Expensive version: traverse values and add individual target spans
							for(int i=0; i<indices.size(); i++) {
								long sourceIndex = indices.indexAt(i);
								long targetIndex = lookup0(sourceIndex);
								if(targetIndex!=NO_INDEX) {
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
						if(targetIndex!=NO_INDEX) {
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
			return localIndex==-1 ? NO_INDEX : id*entriesPerBlock + localIndex;
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
			if(result!=NO_INDEX) {
				return result;
			}

			// Check last block
			result = find0(idTo, 0, localTo, targetIndex);
			if(result!=NO_INDEX) {
				return result;
			}

			// Iterate intermediate blocks
			for(int id=idFrom+1; id<idTo; id++) {
				// Now always include the entire block to search
				result = find0(id, 0, entriesPerBlock, targetIndex);
				if(result!=NO_INDEX) {
					return result;
				}
			}

			return NO_INDEX;
		}

		private long find0(int id, int localFrom, int localTo, long targetIndex) {

			Block block = getBlock(id, false);

			if(block==null) {
				return NO_INDEX;
			}

			int localIndex = -1;

			if(coverage.isMonotonic()) {
				localIndex = blockStorage.findSorted(block.getData(), localFrom, localTo, targetIndex);
			} else {
				localIndex = blockStorage.find(block.getData(), localFrom, localTo, targetIndex);
			}

			return translate(id, localIndex);
		}

		/**
		 * @return
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#find(long, long, de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean find(final long fromSource, final long toSource,
				final IndexSet[] targetIndices, final IndexCollector collector, RequestSettings settings)
				throws InterruptedException {

			throw new UnsupportedOperationException();
		}

	}

	public class Writer extends WriteAccessor<Mapping> implements MappingWriter {

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

			Block block = getBlock(id, true);
			blockStorage.setEntry(block.getData(), localIndex, targetIndex);
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

	public static class Builder extends StoredMappingBuilder<Builder, MappingImplOneToOne> {

		private Integer blockPower;

		public int getBlockPower() {
			return blockPower==null ? DEFAULT_BLOCK_POWER : blockPower.intValue();
		}

		public Builder blockPower(int blockPower) {
			checkArgument(blockPower>0);
			checkState(this.blockPower==null);

			this.blockPower = Integer.valueOf(blockPower);

			return thisAsCast();
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.AbstractMapping.MappingBuilder#build()
		 */
		@Override
		public MappingImplOneToOne create() {
			MappingImplOneToOne mapping = new MappingImplOneToOne(this);

			applyDefaults(mapping);

			return mapping;
		}

	}
}
