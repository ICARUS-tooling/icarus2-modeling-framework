/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import de.ims.icarus2.util.IcarusUtils;

/**
 * Implements a 1-to-1 mapping that is based
 *
 * @author Markus Gärtner
 *
 */
public class MappingImplFunctionOneToOne extends AbstractVirtualMapping {

	public static Builder newBuilder() {
		return new Builder();
	}

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

			if(index==IcarusUtils.UNSET_LONG) {
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
			return IcarusUtils.UNSET_LONG;
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
	public static class Builder extends AbstractMappingBuilder<Builder, MappingImplFunctionOneToOne> {

		private LongUnaryOperator unaryFunction;
		private UnaryOperator<IndexSet> batchFunction;

		protected Builder() {
			// no-op
		}

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
		 * @see de.ims.icarus2.filedriver.mapping.AbstractVirtualMapping.AbstractMappingBuilder#validate()
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
