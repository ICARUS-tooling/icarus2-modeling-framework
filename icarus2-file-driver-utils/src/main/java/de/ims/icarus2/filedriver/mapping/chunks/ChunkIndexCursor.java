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

import java.io.Flushable;

import de.ims.icarus2.model.api.io.SynchronizedAccessor;

/**
 * Specifies a random-access cursor for {@link ChunkIndex} data.
 * <p>
 * This interface replaces both {@link ChunkIndexReader} and {@link ChunkIndexWriter}
 * which have both become obsolete. Interaction with chunk data is usually
 * performed in batches of requests that relate to the same chunk (get file for chunk {@code x},
 * then get begin index, then get end index, ...). Therefore it is much more efficient to
 * model this via a cursor that gets moved to where the desired portion of information can be
 * found and then provides fast access to details.
 *
 *
 * @author Markus Gärtner
 *
 */
public interface ChunkIndexCursor extends SynchronizedAccessor<ChunkIndex>, Flushable {

	boolean isReadOnly();

	/**
	 * Returns the number of chunks in the underlying
	 * {@code ChunkIndex}.
	 *
	 * @return
	 */
	long getEntryCount();

	/**
	 * Positions this cursor so that subsequent calls to getters and setters
	 * write into the storage position for the chunk identified by  the
	 * {@code index} parameter.
	 * <p>
	 * The return value indicates whether or not the cursor now points to a
	 * valid location to read or write data from/to. Note that this is mainly
	 * relevant if the cursor is {@link #isReadOnly() read-only} since for
	 * write access the underlying storage should be automatically expanded in
	 * order to accommodate arbitrary chunk data locations.
	 *
	 * @param index the address of the data chunk subsequent calls to getters
	 * 		  and setters should refer to.
	 * @return {@code true} iff the cursor points to a valid (existing) chunk of data
	 */
	boolean moveTo(long index);

	/**
	 * Returns the file the current chunk is located at. Note that for
	 * chunk indices that only cover a single corpus file this method
	 * will always return {@code 0}
	 *
	 * @param index
	 * @return
	 *
	 * @see ChunkIndexReader#getFileId(long)
	 */
	int getFileId();

	/**
	 * Points to the exact byte offset within a file obtained via
	 * {@link #getFileId()}
	 * that marks the <i>begin</i> of the specified data chunk.
	 *
	 * @param index
	 * @return
	 *
	 * @see ChunkIndexReader#getBeginOffset(long)
	 */
	long getBeginOffset();

	/**
	 * Points to the exact byte offset within a file obtained via
	 * {@link #getFileId(long)}
	 * that marks the <i>end</i> of the specified data chunk.
	 *
	 * @param index
	 * @return
	 *
	 * @see ChunkIndexReader#getEndOffset(long)
	 */
	long getEndOffset();

	/**
	 *
	 * @param index
	 * @param fileId
	 * @return
	 *
	 * @see ChunkIndexWriter#setFileId(long, int)
	 */
	int setFileId(long index, int fileId);

	/**
	 *
	 * @param index
	 * @param offset
	 * @return
	 *
	 * @see ChunkIndexWriter#setBeginOffset(long, long)
	 */
	long setBeginOffset(long index, long offset);

	/**
	 *
	 * @param index
	 * @param offset
	 * @return
	 *
	 * @see ChunkIndexWriter#setEndOffset(long, long)
	 */
	long setEndOffset(long index, long offset);
}
