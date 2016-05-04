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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/io/resources/InMemoryResource.java $
 *
 * $LastChangedDate: 2015-12-18 14:25:15 +0100 (Fr, 18 Dez 2015) $
 * $LastChangedRevision: 439 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.io.resources;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;

import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.util.io.MemoryByteStorage;

/**
 * Implements a {@link IOResource} based on an internal {@link MemoryByteStorage}.
 * <p>
 * Implementation detail: As stated in the description of {@link MemoryByteStorage}
 * a buffer instance will remain in memory while there are still references active
 * to channels created by it. Since this implementations {@link InMemoryResource#delete()}
 * method simply discards the backing buffer object (setting it to {@code null})
 * any channel open at that time will effectively prevent the buffer from getting {@code gc}ed.
 *
 * @author Markus Gärtner
 * @version $Id: InMemoryResource.java 439 2015-12-18 13:25:15Z mcgaerty $
 *
 */
public class InMemoryResource implements IOResource, ModelConstants {

	private MemoryByteStorage buffer;
	private final int capacity;

	public InMemoryResource(int capacity) {
		if(capacity<0 || capacity>MAX_INTEGER_INDEX)
			throw new ModelException(ModelErrorCode.INVALID_INPUT,
					"Capacity must be 0<=capacity<=MAX_INDEX: "+capacity);

		this.capacity = capacity;
	}

	@Override
	public String toString() {
		return getClass().getName()+"[capacity="+capacity+"]";
	}

	private void checkOpen() {
		if(buffer==null)
			throw new ModelException(ModelErrorCode.ILLEGAL_STATE,
					"Buffer not prepared");
	}

	/**
	 * @see de.ims.icarus2.model.api.io.resources.IOResource#getWriteChannel()
	 */
	@Override
	public SeekableByteChannel getWriteChannel() throws IOException {
		checkOpen();

		return buffer.newChannel();
	}

	/**
	 * @see de.ims.icarus2.model.api.io.resources.IOResource#getReadChannel()
	 */
	@Override
	public SeekableByteChannel getReadChannel() throws IOException {
		checkOpen();

		return buffer.newChannel();
	}

	/**
	 * @see de.ims.icarus2.model.api.io.resources.IOResource#delete()
	 */
	@Override
	public void delete() throws IOException {
		buffer = null;
	}

	/**
	 * @see de.ims.icarus2.model.api.io.resources.IOResource#prepare()
	 */
	@Override
	public void prepare() throws IOException {
		buffer = new MemoryByteStorage(capacity);
	}

	/**
	 * @see de.ims.icarus2.model.api.io.resources.IOResource#size()
	 */
	@Override
	public long size() throws IOException {
		return buffer.size();
	}

	/**
	 * @see de.ims.icarus2.model.api.io.resources.IOResource#getLocalPath()
	 */
	@Override
	public Path getLocalPath() {
		return null;
	}

}
