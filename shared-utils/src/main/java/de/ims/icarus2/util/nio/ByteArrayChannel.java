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
package de.ims.icarus2.util.nio;

import static de.ims.icarus2.util.Conditions.checkNotNull;

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
	 *
	 * @param s
	 * @return
	 */
	public static ByteArrayChannel fromChars(CharSequence s) {
		checkNotNull(s);

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

	public ByteArrayChannel(byte[] data) {
		this(data, true);
	}

	public ByteArrayChannel(byte[] data, boolean readOnly) {
		checkNotNull(data);

		this.data = data;
		this.readOnly = readOnly;
	}

	/**
	 * @see java.nio.channels.Channel#isOpen()
	 */
	@Override
	public boolean isOpen() {
		return true;
	}

	/**
	 * @see java.nio.channels.Channel#close()
	 */
	@Override
	public void close() throws IOException {
		// no-op
	}

	/**
	 * @see java.nio.channels.SeekableByteChannel#read(java.nio.ByteBuffer)
	 */
	@Override
	public int read(ByteBuffer dst) throws IOException {
		int length = Math.min(dst.remaining(), data.length-position+1);

		dst.put(data, position, length);
		position += length;

		return length;
	}

	/**
	 * @see java.nio.channels.SeekableByteChannel#write(java.nio.ByteBuffer)
	 */
	@Override
	public int write(ByteBuffer src) throws IOException {
		if(readOnly)
			throw new IOException("Channel is read-only");

		int length = Math.min(src.remaining(), data.length-position+1);

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
	 * @see java.nio.channels.SeekableByteChannel#size()
	 */
	@Override
	public long size() throws IOException {
		return data.length;
	}

	/**
	 * @see java.nio.channels.SeekableByteChannel#truncate(long)
	 */
	@Override
	public SeekableByteChannel truncate(long size) throws IOException {
		return this;
	}

}
