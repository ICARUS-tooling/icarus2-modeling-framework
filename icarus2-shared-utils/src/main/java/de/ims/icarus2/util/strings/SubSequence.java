/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.util.lang.Primitives._int;

/**
 * Implements a static slice of characters from a given source {@link CharSequence}.
 * Note that 'static' in this contest only refers to the location of the slice within
 * the source sequence! This class does not perform any caching, so any changes to
 * characters of the source sequence within the frame defined at constructor time are
 * visible through this class.
 *
 * @author Markus Gärtner
 *
 */
public class SubSequence extends AbstractString {

	private final CharSequence source;
	private final int offset;
	private final int len;

	/**
	 *
	 * @param source original sequence to slice subsequence from
	 * @param offset index of first character to include in the subsequence
	 * @param len total length of the subsequence
	 *
	 * @throws NullPointerException iff {@code source} is {@code null}
	 * @throws IndexOutOfBoundsException if {@code offset < 0} or
	 * 	if{@code offset} equals or exceeds {@code source.length()}
	 * @throws IllegalArgumentException if {@code len < 0} or
	 * 	if {@code offset + len} exceed {@code source.length()}
	 *
	 */
	public SubSequence(CharSequence source, int offset, int len) {
		if (source == null)
			throw new NullPointerException("Invalid source");
		if(offset<0 || offset>=source.length())
			throw new IndexOutOfBoundsException(String.format(
					"invalid offset %d for source '%s'", _int(offset), source));
		if(len<0 || offset+len>source.length())
			throw new IllegalArgumentException(String.format(
					"invalid length %d for offset %d on source '%s'", _int(len), _int(offset), source));

		this.source = source;
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
		if(index<0 || index>=len)
			throw new IndexOutOfBoundsException();

		return source.charAt(offset+index);
	}
}