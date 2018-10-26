/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.io.resources;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.io.IOUtil;
import de.ims.icarus2.util.nio.MemoryByteStorage;

/**
 * @author Markus Gärtner
 *
 */
public class ReadOnlyStringResource implements IOResource {

	private String source;
	private final Charset encoding;

	private MemoryByteStorage buffer;

	/**
	 * @param source
	 * @param encoding
	 */
	public ReadOnlyStringResource(String source, Charset encoding) {
		requireNonNull(source);
		requireNonNull(encoding);
		checkArgument("Source string must not be empty", !source.isEmpty());

		this.source = source;
		this.encoding = encoding;
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
		throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
				"Read only implementation -  backed by constant source string");
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

		buffer = new MemoryByteStorage(source.length());

		try(Writer writer = Channels.newWriter(buffer.newChannel(), encoding.newEncoder(), IOUtil.DEFAULT_BUFFER_SIZE)) {
			writer.write(source);
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
