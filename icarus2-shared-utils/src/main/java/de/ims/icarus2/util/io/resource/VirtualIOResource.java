/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.io.IOUtil;
import de.ims.icarus2.util.nio.MemoryByteStorage;

/**
 * Implements a {@link IOResource} based on an internal {@link MemoryByteStorage}.
 * <p>
 * Implementation detail: As stated in the description of {@link MemoryByteStorage}
 * a buffer instance will remain in memory while there are still references active
 * to channels created by it. Since this implementations {@link VirtualIOResource#delete()}
 * method simply discards the backing buffer object (setting it to {@code null})
 * any channel open at that time will effectively prevent the buffer from getting {@code gc}ed.
 *
 * @author Markus Gärtner
 *
 */
@TestableImplementation(IOResource.class)
public class VirtualIOResource extends AbstractIOResource {

	private MemoryByteStorage buffer;

	// Initial capacity for buffer creation
	private final int capacity;

	public VirtualIOResource(Path path) {
		this(path, IOUtil.DEFAULT_BUFFER_SIZE, AccessMode.READ_WRITE);
	}

	public VirtualIOResource(Path path, int capacity) {
		this(path, capacity, AccessMode.READ_WRITE);
	}

	public VirtualIOResource(Path path, AccessMode accessMode) {
		this(path, IOUtil.DEFAULT_BUFFER_SIZE, accessMode);
	}

	public VirtualIOResource(Path path, int capacity, AccessMode accessMode) {
		super(path, accessMode);

		if(capacity<=0 || capacity>IcarusUtils.MAX_INTEGER_INDEX)
			throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT,
					"Capacity must be 0<capacity<=MAX_INDEX: "+capacity);

		this.capacity = capacity;
	}

	@Override
	public String toString() {
		return getClass().getName()+"[capacity="+capacity+"]";
	}

	private void checkOpen() {
		if(buffer==null || !buffer.isOpen())
			throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE,
					"Buffer not prepared");
	}

	/**
	 * @see de.ims.icarus2.util.io.resource.IOResource#getWriteChannel()
	 */
	@Override
	public SeekableByteChannel getWriteChannel() throws IOException {
		checkWriteAccess();
		checkOpen();

		return buffer.newChannel();
	}

	/**
	 * @see de.ims.icarus2.util.io.resource.IOResource#getReadChannel()
	 */
	@Override
	public SeekableByteChannel getReadChannel() throws IOException {
		checkReadAccess();
		checkOpen();

		return buffer.newChannel();
	}

	/**
	 * @see de.ims.icarus2.util.io.resource.IOResource#delete()
	 */
	@Override
	public void delete() {
		checkWriteAccess();

		if(buffer!=null) {
			buffer.close();
		}
		buffer = null;
	}

	/**
	 * @see de.ims.icarus2.util.io.resource.IOResource#prepare()
	 */
	@Override
	public void prepare() throws IOException {
		if(buffer!=null) {
			return;
		}

		buffer = new MemoryByteStorage(capacity);
	}

	/**
	 * @return the buffer
	 */
	public MemoryByteStorage getBuffer() {
		return buffer;
	}

	/**
	 * @see de.ims.icarus2.util.io.resource.IOResource#size()
	 */
	@Override
	public long size() throws IOException {
		checkOpen();
		return buffer.size();
	}
	/**
	 * @see de.ims.icarus2.util.io.resource.IOResource#isLocal()
	 */
	@Override
	public boolean isLocal() {
		return false;
	}
}
