/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.io;

import de.ims.icarus2.model.api.ModelException;

/**
 * Utility class for accessing distributed data or for loading
 * little chunks from a very big database.
 *
 * @author Markus Gärtner
 * @see ResourcePath
 *
 */
public interface PathResolver {

	/**
	 * Translates the given {@code chunkIndex} into a {@code ResourcePath}
	 * information that can be used to access data from an abstract
	 * data source.
	 *
	 * @param chunkIndex
	 * @return
	 * @throws ModelException if the {@code chunkIndex} violates
	 * the bounds of this resolver. For example a resolver translating chunk indices
	 * into row values for a database table might check for the overall size of that
	 * table to make sure the returned rows do not exceed the table's row count.
	 */
	ResourcePath getPath(int chunkIndex);

	/**
	 * Returns the total number of data chunks this resolver can address as individual
	 * paths.
	 *
	 * @return
	 */
	int getPathCount();

	/**
	 * Releases any associated resources and renders this path resolver unusable for further
	 * queries.
	 */
	void close();
}