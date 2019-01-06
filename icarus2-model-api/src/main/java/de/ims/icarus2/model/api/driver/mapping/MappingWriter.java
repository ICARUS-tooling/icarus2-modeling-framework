/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.io.SynchronizedAccessor;

/**
 * Provides the standard interface for writing to a {@link AbstractStoredMapping} implementation.
 * Note that implementations will typically require the write operations to be performed in an
 * ordered manner according e.g. to the order of source indices. So in other words data should
 * only be appended to an index storage.
 *
 * @author Markus Gärtner
 *
 */
public interface MappingWriter extends SynchronizedAccessor<Mapping> {

	/**
	 * Maps the given {@code sourceIndex} to a single {@code targetIndex}.
	 *
	 * @param sourceIndex
	 * @param targetIndex
	 */
	void map(long sourceIndex, long targetIndex);

	/**
	 * Maps 2 spans which might be of length 1. This method exists as a
	 * combined version of 1-to-many and many-to-1 span mappings.
	 *
	 * @param sourceFrom begin of the source span, inclusive
	 * @param sourceTo end of the source span, inclusive
	 * @param targetFrom begin of the target span, inclusive
	 * @param targetTo end of the target span, inclusive
	 */
	void map(long sourceFrom, long sourceTo, long targetFrom, long targetTo);

	void map(IndexSet sourceIndices, IndexSet targetIndices);

	void map(IndexSet[] sourceIndices, IndexSet[] targetIndices);
}
