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
package de.ims.icarus2.util.strings;

import java.io.IOException;
import java.io.Reader;

/**
 * @author Markus Gärtner
 *
 */
public class CharSequenceReader extends Reader {

	private CharSequence source;
	private int pos;
	private int mark;

	public CharSequenceReader() {
		this("");
	}

	public CharSequenceReader(CharSequence source) {
		setSource(source);
	}

	public void setSource(CharSequence source) {
		if (source == null)
			throw new NullPointerException("Invalid source");

		close0();

		this.source = source;
	}

	public void clear() {
		setSource("");
	}

	private void close0() {
		pos = 0;
		mark = 0;
	}

	/**
	 * @see java.io.Reader#read(char[], int, int)
	 */
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
        if (pos >= source.length()) {
            return -1;
        }
        if (cbuf == null)
            throw new NullPointerException("Character array is missing");
        if (len < 0 || (off + len) > cbuf.length)
            throw new IndexOutOfBoundsException("Array Size=" + cbuf.length +
                    ", offset=" + off + ", length=" + len);
        int count = 0;
        for (int i = 0; i < len; i++) {
            int c = read();
            if (c == -1) {
                return count;
            }
            cbuf[off + i] = (char)c;
            count++;
        }
        return count;
	}

	/**
	 * @see java.io.Reader#close()
	 */
	@Override
	public void close() throws IOException {
		close0();
	}

	@Override
	public int read() throws IOException {
		if(pos<source.length()) {
			return source.charAt(pos++);
		} else {
			return -1;
		}
	}

	@Override
	public long skip(long n) throws IOException {
        if (n < 0L)
            throw new IllegalArgumentException(
                    "Number of characters to skip is less than zero: " + n);
        if (pos >= source.length()) {
            return -1;
        }
        int target = (int)Math.min(source.length(), (pos + n));
        int skipped = target - pos;
        pos = target;
        return skipped;
	}

	@Override
	public boolean ready() throws IOException {
		return true;
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	@Override
	public void mark(int readAheadLimit) throws IOException {
		mark = pos;
	}

	@Override
	public void reset() throws IOException {
		pos = mark;
	}

}