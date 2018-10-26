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
package de.ims.icarus2.model.api.driver.mapping;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
public abstract class MappingRequest implements Callable<IndexSet[]> {

	protected final MappingReader reader;
	protected final RequestSettings settings;

	private final AtomicBoolean lookupStarted = new AtomicBoolean(false);

	protected MappingRequest(MappingReader reader, RequestSettings settings) {
		requireNonNull(reader);

		this.reader = reader;
		this.settings = settings;
	}

	protected abstract IndexSet[] doLookup() throws InterruptedException;

	/**
	 * Checks that this is the only ever call made for this request and if so
	 * executes the internal lookup method. The result of that method is ensured
	 * to be non-null and then returned. In addition this method ensures a prober
	 * synchronization of the underlying {@link MappingReader reader} by wrapping
	 * the internal lookup call inside the required {@link MappingReader#begin() begin}
	 * and {@link MappingReader#end() end} statements.
	 * <p>
	 * No special handling is implemented for the situation of an {@link InterruptedException}
	 * being thrown.
	 *
	 * @see java.util.concurrent.Callable#call()
	 *
	 * @throws ModelException if this is either an attempt to call the method more than once
	 * (resulting in {@link ModelErrorCode#ILLEGAL_STATE}) or the internal lookup method produced
	 * a {@code null} result ({@link ModelErrorCode#INTERNAL_ERROR}).
	 */
	@Override
	public IndexSet[] call() throws InterruptedException {
		if(!lookupStarted.compareAndSet(false, true))
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Lookup operation already initiated");

		IndexSet[] result = null;

		reader.begin();
		try {
			result = doLookup();
		} finally {
			reader.end();
		}

		if(result==null)
			throw new ModelException(GlobalErrorCode.INTERNAL_ERROR, "Result index set is null");

		return result;
	}

	public static class SingleValueRequest extends MappingRequest {

		private final long index;

		/**
		 * @param reader
		 * @param settings
		 */
		public SingleValueRequest(MappingReader reader, RequestSettings settings, long index) {
			super(reader, settings);

			checkArgument("Lookup index must be greater than -1", index>IcarusUtils.UNSET_LONG);

			this.index = index;
		}

		/**
		 * @throws InterruptedException
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingRequest#doLookup()
		 */
		@Override
		protected IndexSet[] doLookup() throws InterruptedException {
			return reader.lookup(index, settings);
		}

	}

	public static class MultiValueRequest extends MappingRequest {

		private final IndexSet[] indices;

		/**
		 * @param reader
		 * @param settings
		 */
		public MultiValueRequest(MappingReader reader, RequestSettings settings, IndexSet[] indices) {
			super(reader, settings);

			requireNonNull(indices);

			this.indices = indices;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingRequest#doLookup()
		 */
		@Override
		protected IndexSet[] doLookup() throws InterruptedException {
			return reader.lookup(indices, settings);
		}
	}
}
