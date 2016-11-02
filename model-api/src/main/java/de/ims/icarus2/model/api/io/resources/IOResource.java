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
 *
 */
package de.ims.icarus2.model.api.io.resources;

import java.io.IOException;
import java.net.URL;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;

import de.ims.icarus2.util.annotations.OptionalMethod;

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

	/**
	 * Opens the resource for writing. The returned channel is expected to be fully
	 * initialized and it will be only used for a single operation or batch of operations
	 * and then {@link AutoCloseable#close() closed} again.
	 *
	 * @return
	 * @throws IOException
	 */
	SeekableByteChannel getWriteChannel() throws IOException;

	/**
	 * Opens the resource for reading. The returned channel is expected to be fully
	 * initialized and it will be only used for a single operation or batch of operations
	 * and then {@link AutoCloseable#close() closed} again.
	 *
	 * @return
	 * @throws IOException
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
