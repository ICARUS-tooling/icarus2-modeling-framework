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
package de.ims.icarus2.util.io.resource;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.annotations.TestableImplementation;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(IOResource.class)
public final class FileResource extends ReadWriteResource {


	private final Path file;

	public FileResource(Path file) {
		this(file, AccessMode.READ_WRITE);
	}

	public FileResource(Path file, AccessMode accessMode) {
		super(accessMode);

		requireNonNull(file);

		this.file = file;
	}

	@Override
	public String toString() {
		return "FileResource[file="+file+"]";
	}

	/**
	 * @see de.ims.icarus2.util.io.resource.IOResource#getWriteChannel()
	 */
	@Override
	public SeekableByteChannel getWriteChannel() throws IOException {
		checkWriteAccess();

		return Files.newByteChannel(file, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
	}

	/**
	 * @see de.ims.icarus2.util.io.resource.IOResource#getReadChannel()
	 */
	@Override
	public SeekableByteChannel getReadChannel() throws IOException {
		checkReadAccess();

		return Files.newByteChannel(file, StandardOpenOption.READ);
	}

	/**
	 * @see de.ims.icarus2.util.io.resource.IOResource#delete()
	 */
	@Override
	public void delete() throws IOException {
		checkWriteAccess();

		Files.delete(file);
	}

	/**
	 * @see de.ims.icarus2.util.io.resource.IOResource#prepare()
	 */
	@Override
	public void prepare() throws IOException {

		if(!Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
			checkWriteAccess();

			try {
				Files.createFile(file);
			} catch (IOException e) {
				throw new IcarusRuntimeException(GlobalErrorCode.IO_ERROR,
						"Failed to open managed resource", e);
			}
		}

		if(!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS))
			throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE,
					"Supplied file is not regular file: "+file);
	}

	/**
	 * @see de.ims.icarus2.util.io.resource.IOResource#size()
	 */
	@Override
	public long size() throws IOException {
		checkReadAccess();

		return Files.size(file);
	}

	/**
	 * @see de.ims.icarus2.util.io.resource.IOResource#getLocalPath()
	 */
	@Override
	public final Path getLocalPath() {
		return file;
	}
}
