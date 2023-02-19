/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet;

/**
 * A factory that creates {@link MappingRequest} objects for lookup operations on a given
 * {@link MappingReader}.
 *
 * @author Markus Gärtner
 *
 */
public class MappingRequestFactory {

	private MappingReader reader;
	private RequestSettings settings;

	public MappingRequestFactory reader(MappingReader reader) {
		requireNonNull(reader);

		this.reader = reader;

		return this;
	}

	public MappingRequestFactory settings(RequestSettings settings) {
		this.settings = settings;

		return this;
	}

	public MappingReader getReader() {
		return reader;
	}

	public RequestSettings getSettings() {
		return settings;
	}

	private void checkReaderSet() {
		if(reader==null)
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "No reader set");
	}

	public MappingRequest forIndex(long index) {
		checkReaderSet();

		return new MappingRequest.SingleValueRequest(reader, settings, index);
	}

	private MappingRequest forIndices0(IndexSet[] indices) {
		checkReaderSet();

		return new MappingRequest.MultiValueRequest(reader, settings, indices);
	}

	public MappingRequest forIndices(IndexSet...indices) {
		requireNonNull(indices);

		return forIndices0(indices);
	}

	public MappingRequest forIndices(long...indices) {
		requireNonNull(indices);

		IndexSet set = new ArrayIndexSet(indices);

		return forIndices0(IndexUtils.wrap(set));
	}

	public MappingRequest forIndices(int...indices) {
		requireNonNull(indices);

		IndexSet set = new ArrayIndexSet(indices);

		return forIndices0(IndexUtils.wrap(set));
	}

	public MappingRequest forIndices(short...indices) {
		requireNonNull(indices);

		IndexSet set = new ArrayIndexSet(indices);

		return forIndices0(IndexUtils.wrap(set));
	}

	public MappingRequest forIndices(byte...indices) {
		requireNonNull(indices);

		IndexSet set = new ArrayIndexSet(indices);

		return forIndices0(IndexUtils.wrap(set));
	}
}
