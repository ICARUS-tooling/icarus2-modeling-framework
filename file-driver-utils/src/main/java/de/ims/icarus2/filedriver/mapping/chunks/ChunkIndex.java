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
package de.ims.icarus2.filedriver.mapping.chunks;

import de.ims.icarus2.filedriver.io.sets.FileSet;
import de.ims.icarus2.model.api.ModelConstants;

/**
 * A simple lookup structure to map from chunk index values to actual
 * physical file locations and byte offsets within those files. To
 * actually access the data within an index or in order to modify it,
 * one has to use appropriate implementations of the {@link ChunkIndexReader}
 * and {@link ChunkIndexWriter} interfaces. Note that there are in principal
 * 3 different types (or levels) of chunk indices possible:
 *
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
 *
 * @author Markus Gärtner
 *
 */
public interface ChunkIndex extends ModelConstants {

	/**
	 * Returns the file storage that file indices returned by {@link ChunkIndexReader#getFileId(long)}
	 * refer to.
	 *
	 * @return
	 */
	FileSet getFileSet();

	ChunkIndexReader newReader();

	ChunkIndexWriter newWriter();
}
