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

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.util.AccessMode;

/**
 * @author Markus Gärtner
 *
 */
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
	 * @see de.ims.icarus2.model.api.io.resources.IOResource#getWriteChannel()
	 */
	@Override
	public SeekableByteChannel getWriteChannel() throws IOException {
		checkWriteAccess();

		return Files.newByteChannel(file, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
	}

	/**
	 * @see de.ims.icarus2.model.api.io.resources.IOResource#getReadChannel()
	 */
	@Override
	public SeekableByteChannel getReadChannel() throws IOException {
		checkReadAccess();

		return Files.newByteChannel(file, StandardOpenOption.READ);
	}

	/**
	 * @see de.ims.icarus2.model.api.io.resources.IOResource#delete()
	 */
	@Override
	public void delete() throws IOException {
		checkWriteAccess();

		Files.delete(file);
	}

	/**
	 * @see de.ims.icarus2.model.api.io.resources.IOResource#prepare()
	 */
	@Override
	public void prepare() throws IOException {

		if(!Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
			checkWriteAccess();

			try {
				Files.createFile(file);
			} catch (IOException e) {
				throw new ModelException(ModelErrorCode.DRIVER_INDEX_IO,
						"Failed to open managed resource", e); //$NON-NLS-1$
			}
		}

		if(!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS))
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					"Supplied file is not regular file: "+file); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus2.model.api.io.resources.IOResource#size()
	 */
	@Override
	public long size() throws IOException {
		checkReadAccess();

		return Files.size(file);
	}

	/**
	 * @see de.ims.icarus2.model.api.io.resources.IOResource#getLocalPath()
	 */
	@Override
	public final Path getLocalPath() {
		return file;
	}
}
