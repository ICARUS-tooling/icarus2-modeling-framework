/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.strings;

public class CharArraySequence extends AbstractString {

	private final char[] buffer;
	private final int offset;
	private final int len;

	public CharArraySequence() {
		buffer = null;
		offset = -1;
		len = 0;
	}

	public CharArraySequence(char[] buffer, int offset, int len) {
		if (buffer == null)
			throw new NullPointerException("Invalid buffer"); //$NON-NLS-1$
		if(offset<0 || offset>=buffer.length)
			throw new IndexOutOfBoundsException("offset"); //$NON-NLS-1$
		if(len<0 || len>buffer.length-offset)
			throw new IndexOutOfBoundsException("length"); //$NON-NLS-1$

		this.buffer = buffer;
		this.offset = offset;
		this.len = len;
	}

	/**
	 * @see java.lang.CharSequence#length()
	 */
	@Override
	public int length() {
		return len;
	}

	/**
	 * @see java.lang.CharSequence#charAt(int)
	 */
	@Override
	public char charAt(int index) {
		if(index>=len || index<0)
			throw new IndexOutOfBoundsException();

		return buffer[offset+index];
	}

	/**
	 * @see java.lang.CharSequence#subSequence(int, int)
	 */
	@Override
	public CharSequence subSequence(int start, int end) {
		if(start<0 || start>=len)
			throw new IndexOutOfBoundsException("start"); //$NON-NLS-1$
		if(end<0 || end>len || end<start)
			throw new IndexOutOfBoundsException("end"); //$NON-NLS-1$

		return new CharArraySequence(buffer, offset+start, end-start);
	}
}