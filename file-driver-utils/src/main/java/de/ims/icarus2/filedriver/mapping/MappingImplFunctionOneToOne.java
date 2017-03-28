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

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.function.LongUnaryOperator;
import java.util.function.UnaryOperator;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexCollector;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingReader;
import de.ims.icarus2.model.api.driver.mapping.RequestSettings;
import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;

/**
 * Implements a 1-to-1 mapping that is based
 *
 * @author Markus Gärtner
 *
 */
public class MappingImplFunctionOneToOne extends AbstractVirtualMapping {

	private final LongUnaryOperator unaryFunction;
	private final UnaryOperator<IndexSet> batchFunction;

	protected MappingImplFunctionOneToOne(Builder builder) {
		super(builder);

		unaryFunction = builder.getUnaryFunction();
		batchFunction = builder.getBatchFunction();
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.mapping.Mapping#newReader()
	 */
	@Override
	public MappingReader newReader() {
		return this.new Reader();
	}

	public class Reader implements MappingReader {

		private final Coverage coverage = getManifest().getCoverage();

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#getSource()
		 */
		@Override
		public Mapping getSource() {
			return MappingImplFunctionOneToOne.this;
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#begin()
		 */
		@Override
		public void begin() {
			// no-op
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#end()
		 */
		@Override
		public void end() {
			// no-op
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#close()
		 */
		@Override
		public void close() throws ModelException {
			// no-op
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
			return unaryFunction.applyAsLong(sourceIndex);
		}

		private IndexSet lookup0(IndexSet indices) {
			if(batchFunction!=null) {
				return batchFunction.apply(indices);
			} else {
				int size = indices.size();
				IndexBuffer result = new IndexBuffer(size);

				for(int i=0; i<size; i++) {
					result.add(unaryFunction.applyAsLong(indices.indexAt(i)));
				}

				return result;
			}
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(long, de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean lookup(long sourceIndex, IndexCollector collector, RequestSettings settings)
				throws InterruptedException {
			long index = lookup0(sourceIndex);

			if(index==UNSET_LONG) {
				return false;
			} else {
				collector.add(index);
				return true;
			}
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(long, RequestSettings)
		 */
		@Override
		public IndexSet[] lookup(long sourceIndex, RequestSettings settings) throws ModelException,
				InterruptedException {
			return IndexUtils.wrap(lookup0(sourceIndex));
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
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean lookup(IndexSet[] sourceIndices, IndexCollector collector, RequestSettings settings)
				throws InterruptedException {

			for(IndexSet set : sourceIndices) {
				collector.add(lookup0(set));
			}

			return false;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getBeginIndex(de.ims.icarus2.model.api.driver.indices.IndexSet[], RequestSettings)
		 */
		@Override
		public long getBeginIndex(IndexSet[] sourceIndices, RequestSettings settings)
				throws InterruptedException {

			if(coverage.isMonotonic()) {
				return lookup0(IndexUtils.firstIndex(sourceIndices));
			} else {
				// Extremely inefficient!!!
				IndexSet[] targetIndices = lookup(sourceIndices, null);
				return IndexUtils.firstIndex(targetIndices);
			}
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getEndIndex(de.ims.icarus2.model.api.driver.indices.IndexSet[], RequestSettings)
		 */
		@Override
		public long getEndIndex(IndexSet[] sourceIndices, RequestSettings settings)
				throws InterruptedException {

			if(coverage.isMonotonic()) {
				return lookup0(IndexUtils.lastIndex(sourceIndices));
			} else {
				// Extremely inefficient!!!
				IndexSet[] targetIndices = lookup(sourceIndices, null);
				return IndexUtils.lastIndex(targetIndices);
			}
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#find(long, long, long, RequestSettings)
		 */
		@Override
		public long find(long fromSource, long toSource, long targetIndex, RequestSettings settings)
				throws InterruptedException {
			return UNSET_LONG;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#find(long, long, de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean find(long fromSource, long toSource,
				IndexSet[] targetIndices, IndexCollector collector, RequestSettings settings)
				throws InterruptedException {
			return false;
		}

	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class Builder extends MappingBuilder<Builder, MappingImplFunctionOneToOne> {

		private LongUnaryOperator unaryFunction;
		private UnaryOperator<IndexSet> batchFunction;

		public LongUnaryOperator getUnaryFunction() {
			return unaryFunction;
		}

		public Builder unaryFunction(LongUnaryOperator unaryFunction) {
			requireNonNull(unaryFunction);
			checkState(this.unaryFunction==null);

			this.unaryFunction = unaryFunction;

			return thisAsCast();
		}

		public UnaryOperator<IndexSet> getBatchFunction() {
			return batchFunction;
		}

		public Builder batchFunction(UnaryOperator<IndexSet> batchFunction) {
			requireNonNull(batchFunction);
			checkState(this.batchFunction==null);

			this.batchFunction = batchFunction;

			return thisAsCast();
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.AbstractVirtualMapping.MappingBuilder#validate()
		 */
		@Override
		protected void validate() {
			super.validate();

			checkState("Missing unary function", unaryFunction!=null);
		}

		@Override
		protected MappingImplFunctionOneToOne create() {
			return new MappingImplFunctionOneToOne(this);
		}

	}
}
