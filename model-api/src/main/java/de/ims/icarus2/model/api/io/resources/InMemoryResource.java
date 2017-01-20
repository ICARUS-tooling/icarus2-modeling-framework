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
import java.nio.channels.SeekableByteChannel;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.nio.MemoryByteStorage;

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
 *
 */
public class InMemoryResource implements IOResource, ModelConstants {

	private MemoryByteStorage buffer;

	// Initial capacity for buffer creation
	private final int capacity;

	public InMemoryResource(int capacity) {
		if(capacity<0 || capacity>IcarusUtils.MAX_INTEGER_INDEX)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Capacity must be 0<=capacity<=MAX_INDEX: "+capacity);

		this.capacity = capacity;
	}

	@Override
	public String toString() {
		return getClass().getName()+"[capacity="+capacity+"]";
	}

	private void checkOpen() {
		if(buffer==null || !buffer.isOpen())
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
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
		if(buffer!=null) {
			buffer.close();
		}
		buffer = null;
	}

	/**
	 * @see de.ims.icarus2.model.api.io.resources.IOResource#prepare()
	 */
	@Override
	public void prepare() throws IOException {
		if(buffer!=null) {
			return;
		}

		buffer = new MemoryByteStorage(capacity);
	}

	/**
	 * @see de.ims.icarus2.model.api.io.resources.IOResource#size()
	 */
	@Override
	public long size() throws IOException {
		return buffer.size();
	}
}
