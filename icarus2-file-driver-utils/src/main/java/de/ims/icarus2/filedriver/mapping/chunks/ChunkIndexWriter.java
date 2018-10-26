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

import java.io.Flushable;

import de.ims.icarus2.model.api.io.SynchronizedAccessor;

/**
 * Defines the writer interface to get data into a {@link ChunkIndex}.
 *
 * @author Markus Gärtner
 */
@Deprecated
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
