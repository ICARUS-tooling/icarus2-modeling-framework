/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 410 $
 * $Date: 2015-06-26 16:44:18 +0200 (Fr, 26 Jun 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/driver/mapping/MappingWriter.java $
 *
 * $LastChangedDate: 2015-06-26 16:44:18 +0200 (Fr, 26 Jun 2015) $
 * $LastChangedRevision: 410 $
 * $LastChangedBy: mcgaerty $
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
 * @version $Id: MappingWriter.java 410 2015-06-26 14:44:18Z mcgaerty $
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
