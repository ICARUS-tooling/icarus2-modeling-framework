/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

import de.ims.icarus2.apiguard.Api;
import de.ims.icarus2.apiguard.Api.ApiType;
import de.ims.icarus2.model.api.driver.indices.IndexCollector;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingReader;
import de.ims.icarus2.model.api.driver.mapping.RequestSettings;

/**
 * Implements a total index of type {@code one-to-one} which maps
 * indices to their own value between two layers. This index stores
 * <b>no</b> internal state and therefore the synchronization and
 * close methods on its reader instances have no effect!
 *
 * @author Markus Gärtner
 *
 */
public class MappingImplIdentity extends AbstractVirtualMapping {

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Creates a new {@code identity mapping} that maps between the two
	 * specified layers.
	 *
	 * @param driver
	 * @param manifest
	 * @param sourceLayer
	 * @param targetLayer
	 */
	private MappingImplIdentity(Builder builder) {
		super(builder);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.mapping.Mapping#newReader()
	 */
	@Override
	public MappingReader newReader() {
		return this.new Reader();
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public class Reader implements MappingReader {

		private Reader() {
			// no-op
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#getSource()
		 */
		@Override
		public Mapping getSource() {
			return MappingImplIdentity.this;
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
		public void close() {
			// no-op
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getIndicesCount(long, de.ims.icarus2.model.api.driver.mapping.RequestSettings)
		 */
		@Override
		public long getIndicesCount(long sourceIndex, @Nullable RequestSettings settings) {
			return 1L;
		}

		/**
		 * @return
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(long, de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean lookup(long sourceIndex, IndexCollector collector, @Nullable RequestSettings settings) {
			collector.add(sourceIndex);
			return true;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(long, RequestSettings)
		 */
		@Override
		public IndexSet[] lookup(long sourceIndex, @Nullable RequestSettings settings) {
			return IndexUtils.wrap(sourceIndex);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getBeginIndex(long, RequestSettings)
		 */
		@Override
		public long getBeginIndex(long sourceIndex, @Nullable RequestSettings settings) {
			return sourceIndex;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getEndIndex(long, RequestSettings)
		 */
		@Override
		public long getEndIndex(long sourceIndex, @Nullable RequestSettings settings){
			return sourceIndex;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(de.ims.icarus2.model.api.driver.indices.IndexSet[], RequestSettings)
		 */
		@Override
		public IndexSet[] lookup(IndexSet[] sourceIndices, @Nullable RequestSettings settings) {
			requireNonNull(sourceIndices);
			return sourceIndices;
		}

		/**
		 * @return
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean lookup(IndexSet[] sourceIndices, IndexCollector collector, @Nullable RequestSettings settings) {
			requireNonNull(sourceIndices);
			collector.add(sourceIndices);
			return true;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getBeginIndex(de.ims.icarus2.model.api.driver.indices.IndexSet[], RequestSettings)
		 */
		@Override
		public long getBeginIndex(IndexSet[] sourceIndices, @Nullable RequestSettings settings) {
			requireNonNull(sourceIndices);
			return firstIndex(sourceIndices);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getEndIndex(de.ims.icarus2.model.api.driver.indices.IndexSet[], RequestSettings)
		 */
		@Override
		public long getEndIndex(IndexSet[] sourceIndices, @Nullable RequestSettings settings) {
			requireNonNull(sourceIndices);
			return lastIndex(sourceIndices);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#find(long, long, long, RequestSettings)
		 */
		@Override
		public long find(long fromSource, long toSource, long targetIndex, @Nullable RequestSettings settings) {
			return targetIndex;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#find(long, long, de.ims.icarus2.model.api.driver.indices.IndexSet[], RequestSettings)
		 */
		@Override
		public IndexSet[] find(long fromSource, long toSource,
				IndexSet[] targetIndices, @Nullable RequestSettings settings) {
			requireNonNull(targetIndices);
			return targetIndices;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#find(long, long, de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean find(long fromSource, long toSource,
				IndexSet[] targetIndices, IndexCollector collector, @Nullable RequestSettings settings) {
			requireNonNull(targetIndices);
			collector.add(targetIndices);
			return true;
		}

	}

	@Api(type=ApiType.BUILDER)
	public static class Builder extends AbstractVirtualMapping.AbstractMappingBuilder<Builder, MappingImplIdentity> {

		private Builder() {
			// private to only be available via factory method
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#create()
		 */
		@Override
		protected MappingImplIdentity create() {
			return new MappingImplIdentity(this);
		}

	}
}
