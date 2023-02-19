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
package de.ims.icarus2.util.nio;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;

/**
 * @author Markus Gärtner
 *
 */
public class SubChannel implements SeekableByteChannel {

	/**
	 * Underlying channel to delegate actual I/O work to
	 */
	private SeekableByteChannel source;

	/**
	 * Flag to indicate that {@link #close()} has been called.
	 * As long as this value remains {@code false} (the initial
	 * value) the channel is considered to be {@link #isOpen()}.
	 */
	private boolean closed = false;

	public SubChannel() {
		// no-op
	}

	public SubChannel(SeekableByteChannel source) {
		setSource(source);
	}

	public SubChannel(SeekableByteChannel source, long beginOffset, long length) {
		setSource(source);
		setOffsets(beginOffset, length);
	}

	/**
	 * Starting point of this channel expressed in offset value
	 * of the origin channel.
	 */
	private long beginOffset;

	/**
	 * Current position in the channel expressed in local values
	 */
	private long position;

	/**
	 * Number of bytes available in this channel
	 */
	private long length;

	public SubChannel setSource(SeekableByteChannel source) {
		this.source = source;
		return this;
	}

	public SeekableByteChannel getSource() {
		return source;
	}

	public long getBeginOffset() {
		return beginOffset;
	}

	public long getLength() {
		return length;
	}

	public SubChannel setOffsets(long beginOffset, long length) {
		checkArgument(beginOffset>=0L);
		checkArgument(length>=0);

		this.beginOffset = beginOffset;
		this.length = length;

		position = 0L;

		return this;
	}

	/**
	 * @see java.nio.channels.Channel#isOpen()
	 */
	@Override
	public boolean isOpen() {
		return !closed;
	}

	/**
	 * @see java.nio.channels.Channel#close()
	 */
	@Override
	public void close() {
		closed = true;
		beginOffset = 0L;
		position = 0L;
		length = 0L;
	}

	private void checkOpen() throws ClosedChannelException {
		if(closed)
			throw new ClosedChannelException();
	}

	private final SeekableByteChannel source() {
		requireNonNull(source);
		return source;
	}

	/**
	 * @see java.nio.channels.SeekableByteChannel#read(java.nio.ByteBuffer)
	 */
	@Override
	public int read(ByteBuffer dst) throws IOException {
		checkOpen();

		long remaining = length-position;

		if(remaining<=0L) {
			return -1;
		}

		int bufferCapacity = dst.remaining();

		int bytesToRead = (int) Math.min(remaining, bufferCapacity);

		if(bytesToRead>0) {
			if(bytesToRead<=bufferCapacity) {
				// Keep track of original limit
				int limit = dst.limit();

				// Reset limit to
				dst.limit(limit - (bufferCapacity-bytesToRead));
				bytesToRead = source().position(beginOffset+position).read(dst);
				dst.limit(limit);//TODO verify that we need to do this step
			} else {
				bytesToRead = source().position(beginOffset+position).read(dst);
			}

			if(bytesToRead>0) {
				position += bytesToRead;
			}
		}

		return bytesToRead;
	}

	/**
	 * @see java.nio.channels.SeekableByteChannel#write(java.nio.ByteBuffer)
	 */
	@Override
	public int write(ByteBuffer src) throws IOException {
		checkOpen();

		long remaining = length-position;

		if(remaining<=0L) {
			return -1;
		}

		int bufferCapacity = src.remaining();

		int bytesToWrite = (int) Math.min(remaining, bufferCapacity);

		if(bytesToWrite>0) {
			if(bytesToWrite<=bufferCapacity) {
				// Keep track of original limit
				int limit = src.limit();

				// Reset limit to not allow too many elements
				src.limit(limit - (bufferCapacity-bytesToWrite));
				// Usual write delegation
				bytesToWrite = source().position(beginOffset+position).write(src);
				// Change limit back to stored value
				src.limit(limit);
			} else {
				bytesToWrite = source().position(beginOffset+position).write(src);
			}

			if(bytesToWrite>0) {
				position += bytesToWrite;
			}
		}

		return bytesToWrite;
	}

	/**
	 * @see java.nio.channels.SeekableByteChannel#position()
	 */
	@Override
	public long position() throws IOException {
		return position;
	}

	/**
	 * @see java.nio.channels.SeekableByteChannel#position(long)
	 */
	@Override
	public SeekableByteChannel position(long newPosition) throws IOException {
		checkArgument(newPosition>=0L);

		position = newPosition;

		return this;
	}

	/**
	 * @see java.nio.channels.SeekableByteChannel#size()
	 */
	@Override
	public long size() throws IOException {
		return length;
	}

	/**
	 * @see java.nio.channels.SeekableByteChannel#truncate(long)
	 */
	@Override
	public SeekableByteChannel truncate(long size) throws IOException {
		throw new IcarusRuntimeException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Cannot truncate sub-channel");
	}

}
