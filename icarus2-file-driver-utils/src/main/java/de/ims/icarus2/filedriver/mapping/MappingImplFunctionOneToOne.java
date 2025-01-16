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

import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.IcarusUtils.signalUnsupportedMethod;
import static java.util.Objects.requireNonNull;

import java.util.function.LongUnaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import de.ims.icarus2.apiguard.Api;
import de.ims.icarus2.apiguard.Api.ApiType;
import de.ims.icarus2.apiguard.Guarded;
import de.ims.icarus2.apiguard.Guarded.MethodType;
import de.ims.icarus2.apiguard.Mandatory;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexCollector;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingReader;
import de.ims.icarus2.model.api.driver.mapping.RequestSettings;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.IcarusUtils;

/**
 * Implements a 1-to-1 mapping that is based
 *
 * @author Markus Gärtner
 *
 */
public class MappingImplFunctionOneToOne extends AbstractVirtualMapping {

	public static Builder builder() {
		return new Builder();
	}

	private final LongUnaryOperator unaryFunction;
	private final UnaryOperator<IndexSet> batchFunction;
	//TODO add reverse functions?

	protected MappingImplFunctionOneToOne(Builder builder) {
		super(builder);

		unaryFunction = builder.getUnaryFunction();
		batchFunction = builder.getBatchFunction();
	}

	public UnaryOperator<IndexSet> getBatchFunction() {
		return batchFunction;
	}

	public LongUnaryOperator getUnaryFunction() {
		return unaryFunction;
	}

	@Override
	public MappingReader newReader() {
		return this.new Reader();
	}

	public class Reader implements MappingReader {

		private final Coverage coverage = ManifestUtils.require(
				getManifest(), MappingManifest::getCoverage, "coverage");

		protected Reader() {
			// no-op
		}

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
			}

			int size = indices.size();
			IndexBuffer result = new IndexBuffer(size);

			for(int i=0; i<size; i++) {
				result.add(unaryFunction.applyAsLong(indices.indexAt(i)));
			}

			return result;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(long, de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean lookup(long sourceIndex, IndexCollector collector, @Nullable RequestSettings settings)
				throws InterruptedException {
			requireNonNull(collector);

			long index = lookup0(sourceIndex);

			if(index==IcarusUtils.UNSET_LONG) {
				return false;
			}

			collector.add(index);
			return true;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(long, RequestSettings)
		 */
		@Override
		public IndexSet[] lookup(long sourceIndex, @Nullable RequestSettings settings) throws ModelException,
				InterruptedException {
			return IndexUtils.wrap(lookup0(sourceIndex));
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getBeginIndex(long, RequestSettings)
		 */
		@Override
		public long getBeginIndex(long sourceIndex, @Nullable RequestSettings settings) throws ModelException,
				InterruptedException {
			return lookup0(sourceIndex);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getEndIndex(long, RequestSettings)
		 */
		@Override
		public long getEndIndex(long sourceIndex, @Nullable RequestSettings settings) throws ModelException,
				InterruptedException {
			return lookup0(sourceIndex);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean lookup(IndexSet[] sourceIndices, IndexCollector collector, @Nullable RequestSettings settings)
				throws InterruptedException {
			requireNonNull(sourceIndices);
			requireNonNull(collector);

			for(IndexSet set : sourceIndices) {
				collector.add(lookup0(set));
			}

			return false;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getBeginIndex(de.ims.icarus2.model.api.driver.indices.IndexSet[], RequestSettings)
		 */
		@Override
		public long getBeginIndex(IndexSet[] sourceIndices, @Nullable RequestSettings settings)
				throws InterruptedException {
			requireNonNull(sourceIndices);

			if(coverage.isMonotonic()) {
				return lookup0(IndexUtils.firstIndex(sourceIndices));
			}

			return Stream.of(sourceIndices)
					.mapToLong(set -> IndexUtils.min(lookup0(set)))
					.min()
					.orElse(UNSET_LONG);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getEndIndex(de.ims.icarus2.model.api.driver.indices.IndexSet[], RequestSettings)
		 */
		@Override
		public long getEndIndex(IndexSet[] sourceIndices, @Nullable RequestSettings settings)
				throws InterruptedException {
			requireNonNull(sourceIndices);

			if(coverage.isMonotonic()) {
				return lookup0(IndexUtils.lastIndex(sourceIndices));
			}

			return Stream.of(sourceIndices)
					.mapToLong(set -> IndexUtils.max(lookup0(set)))
					.max()
					.orElse(UNSET_LONG);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#find(long, long, long, RequestSettings)
		 */
		@SuppressWarnings("boxing")
		@Override
		public long find(long fromSource, long toSource, long targetIndex, @Nullable RequestSettings settings)
				throws InterruptedException {
			return signalUnsupportedMethod("No reverse lookup provided");
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#find(long, long, de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@SuppressWarnings("boxing")
		@Override
		public boolean find(long fromSource, long toSource,
				IndexSet[] targetIndices, IndexCollector collector, @Nullable RequestSettings settings)
				throws InterruptedException {
			requireNonNull(targetIndices);
			requireNonNull(collector);

			return signalUnsupportedMethod("No reverse lookup provided");
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#find(long, long, de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.mapping.RequestSettings)
		 */
		@Override
		public IndexSet[] find(long fromSource, long toSource, IndexSet[] targetIndices, @Nullable RequestSettings settings)
				throws InterruptedException {
			requireNonNull(targetIndices);
			return signalUnsupportedMethod("No reverse lookup provided");
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	@Api(type=ApiType.BUILDER)
	public static class Builder extends AbstractMappingBuilder<Builder, MappingImplFunctionOneToOne> {

		private LongUnaryOperator unaryFunction;
		private UnaryOperator<IndexSet> batchFunction;

		protected Builder() {
			// no-op
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public LongUnaryOperator getUnaryFunction() {
			return unaryFunction;
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public Builder unaryFunction(LongUnaryOperator unaryFunction) {
			requireNonNull(unaryFunction);
			checkState(this.unaryFunction==null);

			this.unaryFunction = unaryFunction;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public UnaryOperator<IndexSet> getBatchFunction() {
			return batchFunction;
		}

		@Guarded(methodType=MethodType.BUILDER)
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
