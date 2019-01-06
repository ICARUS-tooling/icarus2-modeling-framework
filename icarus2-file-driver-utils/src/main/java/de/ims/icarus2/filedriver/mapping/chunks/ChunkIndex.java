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
package de.ims.icarus2.filedriver.mapping.chunks;

import de.ims.icarus2.filedriver.io.sets.ResourceSet;

/**
 * A simple lookup structure to map from item index values to actual
 * physical file locations and byte offsets within those files. To
 * actually access the data within an index or in order to modify it,
 * one has to use appropriate implementations of the {@link ChunkIndexReader}
 * and {@link ChunkIndexWriter} interfaces. Note that there are in principal
 * 3 different types (or levels) of chunk indices possible:
 * <p>
 * <ul>
 * <li><b>Small</b> indices using integer representations for both element index
 * values and byte offsets. This type of index is recommended for small corpora that
 * neither host too great a number of elements, nor reside in multi-gigabyte files.</li>
 * <li><b>Medium</b> indices that still only host a number of elements that can be
 * addressed with integer values, but map to one or more files which exceed the
 * {@link Integer#MAX_VALUE} limit in terms of total size. This type of index differs from the <i>small</i>
 * version only in the value range available for byte offsets, which in this case is {@code long}.</li>
 * <li><b>Large</b> indices finally provide the means to map extremely big corpora that by far exceed
 * the value range of {@link Integer#MAX_VALUE} for both element index and byte offset. Unlike the
 * aforementioned two types, this one no longer can be represented as one big primitive array holding
 * all the index data. Quite the opposite, it requires another level of caching to account for the fact,
 * that an index this large would itself be too memory consuming to be loaded as a whole. Implementations
 * for this type will try to keep the footprint of loaded chunks as small as possible while still
 * providing fast response times for read/write operations.</li>
 * </ul>
 * <p>
 * Note that a driver may use multiple {@code ChunkIndex} instances for managing the
 * mapping from index values to physical blocks of data. But those instances must be
 * independent with regards to physical overlaps in the data. This is so that a driver
 * cannot accidentally compromise integrity of its item storage by redundantly loading
 * "nested" data.
 *
 * @author Markus Gärtner
 *
 */
public interface ChunkIndex {

	/**
	 * Returns the file storage that file indices returned by {@link ChunkIndexReader#getFileId(long)}
	 * refer to.
	 *
	 * @return
	 */
	ResourceSet getFileSet();

	/**
	 * @deprecated use {@link #newCursor(boolean)} with a {@code readOnly} parameter of {@code true} instead
	 */
	@Deprecated
	ChunkIndexReader newReader();

	/**
	 * @deprecated use {@link #newCursor(boolean)} instead
	 */
	@Deprecated
	ChunkIndexWriter newWriter();

	/**
	 * Creates and returns a new cursor to interact with the data stored in the chunk index.
	 *
	 * @param readOnly specifies whether or not the cursor should also be able to write to the chunk index
	 * @return
	 */
	ChunkIndexCursor newCursor(boolean readOnly);
}
