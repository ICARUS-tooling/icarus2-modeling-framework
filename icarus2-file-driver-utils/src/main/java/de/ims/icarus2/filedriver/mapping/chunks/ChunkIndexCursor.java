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
	 */
	long getEndOffset();

	/**
	 *
	 * @param index
	 * @param fileId
	 * @return
	 */
	int setFileId(int fileId);

	/**
	 *
	 * @param index
	 * @param offset
	 * @return
	 */
	long setBeginOffset(long offset);

	/**
	 *
	 * @param index
	 * @param offset
	 * @return
	 */
	long setEndOffset(long offset);
}
