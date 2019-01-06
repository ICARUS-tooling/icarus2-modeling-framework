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
package de.ims.icarus2.util.nio;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * Models access to a {@link SeekableByteChannel} as an {@link InputStream}
 * which is limited to a specific block of data in that channel.
 *
 * @author Markus Gärtner
 *
 */
public class ByteChannelBlockStream extends InputStream {

	private SeekableByteChannel channel;

	private final ByteBuffer buffer;

	private long position = -1;
	private long remaining = -1;

	// "end of stream" flag
	private boolean eos = false;

	public ByteChannelBlockStream(SeekableByteChannel channel) {
		this(channel, -1);
	}

	public ByteChannelBlockStream(SeekableByteChannel channel, int bufferSize) {
		this(channel, bufferSize, false);
	}

	public ByteChannelBlockStream(SeekableByteChannel channel, int bufferSize, boolean allocateDirect) {
		setChannel(channel);
		buffer = NIOUtil.allocate(bufferSize, allocateDirect);
	}

	public ByteChannelBlockStream(int bufferSize) {
		this(bufferSize, false);
	}

	public ByteChannelBlockStream(int bufferSize, boolean allocateDirect) {
		buffer = NIOUtil.allocate(bufferSize, allocateDirect);
	}

	/**
	 * @return the channel
	 */
	public SeekableByteChannel getChannel() {
		return channel;
	}

	/**
	 * @param channel the channel to set
	 */
	public void setChannel(SeekableByteChannel channel) {
		this.channel = requireNonNull(channel);
		position = -1L;
		remaining = -1L;
		eos = false;
	}

	public void reload(long position, long size) {
		if(position<0)
			throw new IllegalArgumentException("Position is negative: "+position);
		if(size<0)
			throw new IllegalArgumentException("Size is negative: "+size);

		this.position = position;
		this.remaining = size;

		eos = size==0;
		buffer.limit(0);
	}

	public long position() {
		return position;
	}

	public long remaining() {
		return remaining;
	}

	private void fillBuffer() throws IOException {
		if(position<0)
			throw new IllegalStateException();

		if(eos || buffer.hasRemaining()) {
			return;
		}

		buffer.clear();
		buffer.limit((int) Math.min(remaining, buffer.capacity()));

		// Make sure we read from the correct position
		channel.position(position);
		// Buffer data
		int bytesRead = channel.read(buffer);
		if(bytesRead<1) {
			eos = true;
		} else {
			remaining -= bytesRead;
			position += bytesRead;

			buffer.flip();
		}
	}

	/**
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		fillBuffer();
		return eos ? -1 : buffer.get();
	}

	/**
	 * @see java.io.InputStream#read(byte[])
	 */
	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	/**
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		fillBuffer();
		if(eos) {
			return -1;
		}
		// Prevent BufferUnderflowException
		// ByteBuffer.remaining() is guaranteed to return value
		// greater than 0 here since otherwise eos would be true
		len = Math.min(len, buffer.remaining());
		buffer.get(b, off, len);
		return len;
	}

	/**
	 * @see java.io.InputStream#skip(long)
	 */
	@Override
	public long skip(long n) throws IOException {
		if(position<0)
			throw new IllegalStateException();

		if(n<0L) {
			return 0L;
		}

		n = Math.min(n, remaining);

		remaining -= n;
		position += n;

		if(remaining==0) {
			eos = true;
		} else {
			buffer.limit(0);
		}

		return n;
	}

	/**
	 * @see java.io.InputStream#available()
	 */
	@Override
	public int available() throws IOException {
		fillBuffer();

		return eos ? 0 : buffer.remaining();
	}

	/**
	 * @see java.io.InputStream#close()
	 */
	@Override
	public void close() {
		position = -1L;
		remaining = -1L;
		eos = true;
		channel = null;
	}
}
