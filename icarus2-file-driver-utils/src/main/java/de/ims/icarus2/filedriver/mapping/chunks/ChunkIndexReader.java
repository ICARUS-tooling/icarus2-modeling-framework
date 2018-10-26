/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import de.ims.icarus2.model.api.io.SynchronizedAccessor;

/**
 * Specifies a reader for {@code ChunkIndex} data.
 *
 * @author Markus Gärtner
 *
 */
@Deprecated
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
