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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/driver/mapping/chunks/ChunkIndexReader.java $
 *
 * $LastChangedDate: 2015-04-22 00:17:30 +0200 (Mi, 22 Apr 2015) $
 * $LastChangedRevision: 387 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.driver.mapping.chunks;

import de.ims.icarus2.model.io.SynchronizedAccessor;

/**
 * Specifies a reader for {@code ChunkIndex} data.
 *
 * @author Markus Gärtner
 * @version $Id: ChunkIndexReader.java 387 2015-04-21 22:17:30Z mcgaerty $
 *
 */
public interface ChunkIndexReader extends SynchronizedAccessor<ChunkIndex> {

	/**
	 * Returns the number of chunks in the underlying
	 * {@code ChunkIndex}.
	 *
	 * @return
	 */
	long getEntryCount();

	/**
	 * Returns the position at the file level a chunk specified
	 * via the {@code index} parameter is located at. Note that for
	 * chunk indices that only cover a single corpus file this method
	 * will always return {@code 0}
	 *
	 * @param index
	 * @return
	 */
	int getFileId(long index);

	/**
	 * Points to the exact byte offset within a file obtained via
	 * {@link #getFileId(long)} (with the same {@code index} argument!)
	 * that marks the <i>begin</i> of the specified data chunk.
	 *
	 * @param index
	 * @return
	 */
	long getBeginOffset(long index);

	/**
	 * Points to the exact byte offset within a file obtained via
	 * {@link #getFileId(long)} (with the same {@code index} argument!)
	 * that marks the <i>end</i> of the specified data chunk.
	 *
	 * @param index
	 * @return
	 */
	long getEndOffset(long index);
}
