/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.io.IOUtil;
import de.ims.icarus2.util.nio.MemoryByteStorage;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(IOResource.class)
public class ReadOnlyURLResource extends AbstractIOResource {

	private URL source;

	private MemoryByteStorage buffer;

	/**
	 * @param url
	 */
	public ReadOnlyURLResource(Path path, URL url) {
		super(path, AccessMode.READ);
		this.source = requireNonNull(url);
	}

	/**
	 * @return the source
	 */
	public URL getSource() {
		return source;
	}

	private void checkOpen() {
		if(buffer==null || source==null)
			throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE,
					"Buffer not prepared or already deleted");
	}

	/**
	 * @see de.ims.icarus2.util.io.resource.IOResource#getWriteChannel()
	 */
	@Override
	public SeekableByteChannel getWriteChannel() throws IOException {
		throw forMissingWriteAccess();
	}

	/**
	 * @see de.ims.icarus2.util.io.resource.IOResource#getReadChannel()
	 */
	@Override
	public SeekableByteChannel getReadChannel() throws IOException {
		checkOpen();

		return buffer.newChannel();
	}

	/**
	 * @see de.ims.icarus2.util.io.resource.IOResource#delete()
	 */
	@Override
	public void delete() throws IOException {
		buffer = null;
		source = null;
	}

	/**
	 * @see de.ims.icarus2.util.io.resource.IOResource#prepare()
	 */
	@Override
	public void prepare() throws IOException {
		if(buffer!=null) {
			return;
		}

		if(source==null)
			throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE, "Resource already deleted");

		// Copy all data from the URL's stream into local buffer
		try(InputStream in = source.openStream()) {

			buffer = new MemoryByteStorage();

			// InputStream needs array, our MemoryByteStorage needs ByteBuffer
			byte[] b = new byte[IOUtil.DEFAULT_BUFFER_SIZE];
			ByteBuffer bb = ByteBuffer.wrap(b);

			int len;

			while((len=in.read(b))>0) {

				bb.clear().limit(len);
				buffer.write(buffer.size(), bb);
			}
		}
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
