/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
import java.nio.channels.SeekableByteChannel;

import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
public class ByteArrayChannel implements SeekableByteChannel {

	private static final int MASK = 0xff;

	/**
	 * Decodes the given {@code CharSequence} into a read-only byte channel.
	 * Each character in the original sequence is decoded into 2 bytes and then
	 * stored in the backing array of the channel. The higher byte of each character
	 * is stored first, followed by the lower byte.
	 * <p>
	 * This method does <b>not</b> rely on any character encoding!
	 *
	 * @param s
	 * @return
	 */
	public static ByteArrayChannel fromChars(CharSequence s) {
		requireNonNull(s);

		int length = s.length();
		byte[] data = new byte[length<<1];

		for(int i=0; i<length; i++) {
			char c = s.charAt(i);

			int idx = i<<1;
			data[idx] = (byte) (c>>8);
			data[idx+1] = (byte) (c & MASK);
		}

		return new ByteArrayChannel(data);
	}

	private final byte[] data;
	private final boolean readOnly;

	private int position;
	private int size;

	public ByteArrayChannel(byte[] data) {
		this(data, true);
	}

	public ByteArrayChannel(byte[] data, boolean readOnly) {
		requireNonNull(data);

		this.data = data;
		this.readOnly = readOnly;
		size = data.length;
	}

	/**
	 * @see java.nio.channels.Channel#isOpen()
	 */
	@Override
	public boolean isOpen() {
		return true;
	}

	/**
	 * Does nothing on this implementation.
	 *
	 * @see java.nio.channels.Channel#close()
	 */
	@Override
	public void close() {
		// no-op
	}

	/**
	 * @see java.nio.channels.SeekableByteChannel#read(java.nio.ByteBuffer)
	 */
	@Override
	public int read(ByteBuffer dst) throws IOException {
		int length = Math.min(dst.remaining(), intSize()-position);

		dst.put(data, position, length);
		position += length;

		return length<=0 ? -1 : length;
	}

	/**
	 * @see java.nio.channels.SeekableByteChannel#write(java.nio.ByteBuffer)
	 */
	@Override
	public int write(ByteBuffer src) throws IOException {
		if(readOnly)
			throw new IOException("Channel is read-only");

		int length = Math.min(src.remaining(), intSize()-position);

		src.get(data, position, length);
		position += length;

		return length;
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

		this.position = IcarusUtils.ensureIntegerValueRange(newPosition);

		return this;
	}

	/**
	 * Limits the internal size of this channel.
	 *
	 * @param limit
	 * @return
	 */
	public SeekableByteChannel limit(int limit) {
		checkArgument(limit>=-1 && limit<=data.length);
		if(limit==-1) {
			size = data.length;
		} else {
			size = limit;
		}
		return this;
	}

	/**
	 * Sets the {@link #size() size} of this channel to the current
	 * {@link #position() position} and then sets the position to {@code 0};
	 * @return
	 */
	public SeekableByteChannel flip() {
		size = position;
		position = 0;

		return this;
	}

	/**
	 * @see java.nio.channels.SeekableByteChannel#size()
	 */
	@Override
	public long size() throws IOException {
		return intSize();
	}

	private int intSize() {
		return size;
	}

	/**
	 * @see java.nio.channels.SeekableByteChannel#truncate(long)
	 */
	@Override
	public SeekableByteChannel truncate(long size) throws IOException {
		return this;
	}

}
