/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus2.util.nio;

import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.Arrays;

import de.ims.icarus2.io.IOUtil;
import de.ims.icarus2.io.MemoryByteStorage;

/**
 * Utility class for reading byte data from a {@link ReadableByteChannel channel} and converting
 * the byte stream into characters one at a time together with providing information about the
 * byte size of each character read.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ByteCharMapper {

	public static void main(String[] args) throws IOException {
		MemoryByteStorage buffer = new MemoryByteStorage(1024);

		String s = "This is a blöder € test";

		SeekableByteChannel channel = buffer.newChannel();
		byte[] bytes = s.getBytes("UTF-8");
		channel.write(ByteBuffer.wrap(bytes));
		channel.position(0);

		System.out.println(Arrays.toString(bytes));

		ByteCharMapper mapper = new ByteCharMapper(channel, IOUtil.UTF8_CHARSET.newDecoder(), 10);

		int c;
		while((c = mapper.next())!=-1) {
			System.out.printf("char='%s' len=%d\n", (char)c, mapper.byteCount);
			if(c=='l') {
				c++;
			}
		}
	}

	private final ReadableByteChannel channel;
	private final CharsetDecoder decoder;

	private final ByteBuffer bb;

	private final CharBuffer chars = CharBuffer.allocate(2);
	private final ByteBuffer bytes = ByteBuffer.allocate(10);

	private boolean eof;

	private int byteCount;

	public ByteCharMapper(ReadableByteChannel channel, CharsetDecoder decoder, int capacity) {
		checkNotNull(channel);
		checkNotNull(decoder);

		this.channel = channel;
		this.decoder = decoder;

		bb = ByteBuffer.allocate(capacity);
		// Make buffer initially 'empty'
		bb.flip();
	}

	public int next() throws IOException {
		if (eof || !hasMoreBytes()) {
			return -1;
		}

		chars.clear();
		bytes.clear();
		byteCount = 1;

		for (;;) {
			bytes.limit(byteCount);
			bytes.put(byteCount-1, bb.get());
			bytes.position(0);
			//TODO check if eof flag is the correct indicator to use for 'endOfInput' parameter
			CoderResult cr = decoder.decode(bytes, chars, bb.hasRemaining() || !eof);

			if (chars.position() > 1)
				//TODO check if it is possible to require multi-byte lookahead that would result in more than 1 character being decoded
				throw new IllegalStateException("Unsupported byte sequence yielded more than 1 character");

			if (chars.position() > 0)
				break; // Block at most once

			if (cr.isUnderflow()) {
				if (eof)
					break;
				if(!hasMoreBytes())
					break;
				byteCount++;
				continue;
			}
			if (cr.isOverflow()) {
				break;
			}

			cr.throwException();
		}

		if(eof) {
			decoder.reset();
			return -1;
		} else {
			return chars.get(0);
		}
	}

	public int getByteCount() {
		return byteCount;
	}

	private boolean hasMoreBytes() throws IOException {
		if(!bb.hasRemaining()) {
			int n = readBytes();
			if (n < 0) {
				eof = true;
				if ((chars.position() == 0) && (!bb.hasRemaining()))
					return false;
				decoder.reset();
			}
		}
		return bb.hasRemaining();
	}

	private int readBytes() throws IOException {
		bb.compact();
		try {
			int n = channel.read(bb);
			if (n < 0)
				return n;
		} finally {
			// Flip even when an IOException is thrown,
			// otherwise the stream will stutter
			bb.flip();
		}

		return bb.remaining();
	}

}
