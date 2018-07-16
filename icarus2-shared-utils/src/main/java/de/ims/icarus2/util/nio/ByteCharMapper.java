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

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

/**
 * Utility class for reading byte data from a {@link ReadableByteChannel channel} and converting
 * the byte stream into characters one at a time together with providing information about the
 * byte size of each character read.
 * <p>
 * IMPLEMENTATION NOTE:
 * Testing this implementation against the naive approach of decoding a byte stream into
 * characters, analyzing them for boundaries of a chunk and then again encoding them into bytes
 * has shown that the naive way is by far the more efficient and faster one.
 *
 * @author Markus Gärtner
 *
 */
@Deprecated
public class ByteCharMapper {

	private final ReadableByteChannel channel;
	private final CharsetDecoder decoder;

	private final ByteBuffer bb;

	private final CharBuffer chars = CharBuffer.allocate(2);
	private final ByteBuffer bytes = ByteBuffer.allocate(10);

	private boolean eof;

	private int byteCount;

	public ByteCharMapper(ReadableByteChannel channel, CharsetDecoder decoder, int capacity) {
		requireNonNull(channel);
		requireNonNull(decoder);

		this.channel = channel;
		this.decoder = decoder;

		bb = ByteBuffer.allocate(capacity);
		// Make buffer initially 'empty'
		bb.flip();
	}

	public int nextCharacter() throws IOException {
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
				/*
				 *  Theoretically should never happen since we only aim at
				 *  decoding 1 character at a time.
				 *
				 *  TODO maybe skip this condition check altogether and let's throw the exception
				 */
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
