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

 * $Revision: 439 $
 * $Date: 2015-12-18 14:25:15 +0100 (Fr, 18 Dez 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/io/resources/FileResource.java $
 *
 * $LastChangedDate: 2015-12-18 14:25:15 +0100 (Fr, 18 Dez 2015) $
 * $LastChangedRevision: 439 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.io.resources;

import static de.ims.icarus2.model.util.Conditions.checkNotNull;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;

/**
 * @author Markus Gärtner
 * @version $Id: FileResource.java 439 2015-12-18 13:25:15Z mcgaerty $
 *
 */
public final class FileResource implements IOResource {


	private final Path file;

	public FileResource(Path file) {
		checkNotNull(file);
		this.file = file;
	}

	@Override
	public String toString() {
		return "FileResource[file="+file+"]";
	}

	/**
	 * @see de.ims.icarus2.model.io.resources.IOResource#getWriteChannel()
	 */
	@Override
	public SeekableByteChannel getWriteChannel() throws IOException {
		return Files.newByteChannel(file, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
	}

	/**
	 * @see de.ims.icarus2.model.io.resources.IOResource#getReadChannel()
	 */
	@Override
	public SeekableByteChannel getReadChannel() throws IOException {
		return Files.newByteChannel(file, StandardOpenOption.READ);
	}

	/**
	 * @see de.ims.icarus2.model.io.resources.IOResource#delete()
	 */
	@Override
	public void delete() throws IOException {
		Files.delete(file);
	}

	/**
	 * @see de.ims.icarus2.model.io.resources.IOResource#prepare()
	 */
	@Override
	public void prepare() throws IOException {

		if(!Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
			try {
				Files.createFile(file);
			} catch (IOException e) {
				throw new ModelException(ModelErrorCode.DRIVER_INDEX_IO,
						"Failed to open managed resource", e); //$NON-NLS-1$
			}
		}

		if(!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS))
			throw new ModelException(ModelErrorCode.ILLEGAL_STATE,
					"Supplied file is not regular file: "+file); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus2.model.io.resources.IOResource#size()
	 */
	@Override
	public long size() throws IOException {
		return Files.size(file);
	}

	/**
	 * @see de.ims.icarus2.model.io.resources.IOResource#getLocalPath()
	 */
	@Override
	public final Path getLocalPath() {
		return file;
	}
}
