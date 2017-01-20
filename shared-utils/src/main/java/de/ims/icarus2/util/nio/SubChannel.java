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
package de.ims.icarus2.util.nio;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusException;

/**
 * @author Markus Gärtner
 *
 */
public class SubChannel implements SeekableByteChannel {

//	private final ByteBuffer buffer;

	/**
	 * Underlying channel to delegate actual I/O work to
	 */
	private SeekableByteChannel source;

	/**
	 * Flag to indicate that {@link #close()} has been called
	 */
	private boolean closed = false;

//	public SubChannel() {
//		this(IOUtil.DEFAULT_BUFFER_SIZE);
//	}

//	public SubChannel(int bufferSize) {
//		buffer = ByteBuffer.allocate(bufferSize);
//	}

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

	public void setSource(SeekableByteChannel source) {
		this.source = source;
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

	public void setOffsets(long beginOffset, long length) {
		checkArgument(beginOffset>=0L);
		checkArgument(length>=0);

		this.beginOffset = beginOffset;
		this.length = length;

		position = 0L;
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

		long remaining = length-position-1;

		if(remaining<=0L) {
			return -1;
		}

		int bufferCapacity = dst.remaining();

		int bytesToRead = (int) Math.min(remaining, bufferCapacity);

		if(bytesToRead>0) {
			if(bytesToRead<bufferCapacity) {
				int limit = dst.limit();

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

		long remaining = length-position-1;

		if(remaining<=0L) {
			return -1;
		}

		int bufferCapacity = src.remaining();

		int bytesToWrite = (int) Math.min(remaining, bufferCapacity);

		if(bytesToWrite>0) {
			if(bytesToWrite<bufferCapacity) {
				int limit = src.limit();

				src.limit(limit - (bufferCapacity-bytesToWrite));
				bytesToWrite = source().position(beginOffset+position).write(src);
				src.limit(limit);//TODO verify that we need to do this step
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
		throw new IcarusException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Cannot truncate sub-channel");
	}

}
