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
 *
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
	public IndexSet[] call() throws Exception {
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
