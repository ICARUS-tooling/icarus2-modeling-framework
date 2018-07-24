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
 */
package de.ims.icarus2.model.api.io.resources;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.io.IOUtil;
import de.ims.icarus2.util.nio.MemoryByteStorage;

/**
 * @author Markus Gärtner
 *
 */
public class ReadOnlyURLResource implements IOResource {

	private URL source;

	private MemoryByteStorage buffer;

	/**
	 * @param url
	 */
	public ReadOnlyURLResource(URL url) {
		this.source = requireNonNull(url);
	}

	/**
	 * @see de.ims.icarus2.model.api.io.resources.IOResource#getAccessMode()
	 */
	@Override
	public AccessMode getAccessMode() {
		return AccessMode.READ;
	}

	private void checkOpen() {
		if(buffer==null || source==null)
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					"Buffer not prepared or already deleted");
	}

	/**
	 * @see de.ims.icarus2.model.api.io.resources.IOResource#getWriteChannel()
	 */
	@Override
	public SeekableByteChannel getWriteChannel() throws IOException {
		throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Cant write to URL resource");
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
		source = null;
	}

	/**
	 * @see de.ims.icarus2.model.api.io.resources.IOResource#prepare()
	 */
	@Override
	public void prepare() throws IOException {
		if(buffer!=null) {
			return;
		}

		if(source==null)
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Resource already deleted");

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
	 * @see de.ims.icarus2.model.api.io.resources.IOResource#size()
	 */
	@Override
	public long size() throws IOException {
		checkOpen();

		return buffer.size();
	}

}
