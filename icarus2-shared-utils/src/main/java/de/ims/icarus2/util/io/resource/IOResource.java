/**
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
package de.ims.icarus2.util.io.resource;

import java.io.IOException;
import java.net.URL;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.apiguard.OptionalMethod;
import de.ims.icarus2.util.AccessMode;

/**
 * Models abstract access to an arbitrary byte storage that can be connected to
 * in both read and write mode. This interface exists primarily for testing of the
 * various {@link AbstractStoredMapping} implementations. Additionally it allows
 * for greater flexibility for {@link BufferedIOResource}s in general by splitting
 * off the actual storage part from the management and  access synchronization.
 *
 * @author Markus Gärtner
 *
 */
public interface IOResource {

	//TODO allow direct locking mechanism?

	/**
	 * Returns the {@link AccessMode} that defines whether this resource supports
	 * read or write operations or both.
	 *
	 * @return
	 */
	AccessMode getAccessMode();

	/**
	 * Opens the resource for writing. The returned channel is expected to be fully
	 * initialized and it will be only used for a single operation or batch of operations
	 * and then {@link AutoCloseable#close() closed} again.
	 *
	 * @return
	 * @throws IOException
	 * @throws IcarusRuntimeException of type {@link GlobalErrorCode#UNSUPPORTED_OPERATION} if
	 * the {@link #getAccessMode() access mode} does not allow write operations.
	 */
	SeekableByteChannel getWriteChannel() throws IOException;

	/**
	 * Opens the resource for reading. The returned channel is expected to be fully
	 * initialized and it will be only used for a single operation or batch of operations
	 * and then {@link AutoCloseable#close() closed} again.
	 *
	 * @return
	 * @throws IOException
	 * @throws ModelException of type {@link GlobalErrorCode#UNSUPPORTED_OPERATION} if
	 * the {@link #getAccessMode() access mode} does not allow read operations.
	 */
	SeekableByteChannel getReadChannel() throws IOException;

	/**
	 * Deletes this resource and all the contained data permanently.
	 *
	 * @throws IOException
	 */
	@OptionalMethod
	void delete() throws IOException;

	/**
	 * Initializes the resource so that subsequent calls to fetch {@link #getReadChannel() read}
	 * and {@link #getWriteChannel() write} access to data will not require expensive preparation time.
	 * <p>
	 * The implementation of this method should be reentrant, i.e. repeated calls should not cause
	 * any issues or unnecessary resource allocations!
	 *
	 * @throws IOException
	 */
	void prepare() throws IOException;

	/**
	 * Returns the size in bytes of the data currently stored inside this resource.
	 *
	 * @return
	 * @throws IOException
	 */
	long size() throws IOException;

	/**
	 * Returns the path on the local file system this resource is pointing at.
	 * A return value of {@code null} indicates the resource is not referring to local data
	 * (i.e. it's either based on remote {@link URL} data or just exists virtually in
	 * memory).
	 * <p>
	 * The default implementation returns {@code null}.
	 *
	 * @return
	 */
	default Path getLocalPath() {
		return null;
	}
}
