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

 * $Revision: 387 $
 * $Date: 2015-04-22 00:17:30 +0200 (Mi, 22 Apr 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/driver/mapping/chunks/ChunkIndexWriter.java $
 *
 * $LastChangedDate: 2015-04-22 00:17:30 +0200 (Mi, 22 Apr 2015) $
 * $LastChangedRevision: 387 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.filedriver.mapping.chunks;

import java.io.Flushable;

import de.ims.icarus2.model.api.io.SynchronizedAccessor;

/**
 * Defines the writer interface to get data into a {@link ChunkIndex}.
 *
 * @author Markus Gärtner
 * @version $Id: ChunkIndexWriter.java 387 2015-04-21 22:17:30Z mcgaerty $
 *
 */
public interface ChunkIndexWriter extends SynchronizedAccessor<ChunkIndex>, Flushable {

	/**
	 * Changes the file id for the given {@code index} to the new value
	 * and returns the old value if one was set.
	 *
	 * @param index
	 * @param fileId
	 * @return the value previously stored as file id for the given {@code index}
	 */
	int setFileId(long index, int fileId);

	long setBeginOffset(long index, long offset);

	long setEndOffset(long index, long offset);
}
