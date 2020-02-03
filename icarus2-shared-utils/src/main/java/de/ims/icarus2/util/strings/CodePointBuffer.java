/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.util.strings;

import static java.lang.Character.isHighSurrogate;
import static java.lang.Character.isLowSurrogate;
import static java.lang.Character.toCodePoint;

import java.util.Arrays;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.util.Mutable;

/**
 * @author Markus Gärtner
 *
 */
public class CodePointBuffer implements CodePointSequence, Mutable<CharSequence> {

	/** Raw character data */
	private CharSequence source;
	/** Growing buffer for translated codepoints */
	private int[] codepoints;

	/** Total number of detected codepoints */
	int codePointCount;

	public CodePointBuffer() {
		// no-op
	}

	public CodePointBuffer(@Nullable CharSequence source) {
		set(source);
	}

	public void set(@Nullable CharSequence source) {
		this.source = source;
		codePointCount = 0;
		if(source==null) {
			// Delete buffer data
			codepoints = null;
		} else {
			int len = source.length();
			// Ensure buffer size is sufficient for expected codepoints
			if(codepoints==null || codepoints.length<len) {
				codepoints = new int[len];
			}
			for (int i = 0; i < len; i++) {
				char c = source.charAt(i);
				if(isHighSurrogate(c)) {
					// Supplementary codepoint handling
					if(++i<len) {
						char c2 = source.charAt(i);
						if(isLowSurrogate(c2)) {
							codepoints[codePointCount++] = toCodePoint(c, c2);
						} else
							throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT,
									String.format("Incomplete surrogate pair: %s is not a valid "
											+ "low surrogate after %s", Integer.toHexString(c),
											Integer.toHexString(c2)));
					} else
						throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT,
								"Incomplete high surrogate: "+Integer.toHexString(c));
				} else {
					// Mere BMP codepoint, so store as char
					codepoints[codePointCount++] = c;
				}
			}
		}
	}

	/**
	 * @see java.lang.CharSequence#length()
	 */
	@Override
	public int length() {
		return source==null ? 0 : source.length();
	}

	/**
	 * @see java.lang.CharSequence#charAt(int)
	 */
	@Override
	public char charAt(int index) {
		return source.charAt(index);
	}

	/**
	 * @see java.lang.CharSequence#subSequence(int, int)
	 */
	@Override
	public CharSequence subSequence(int start, int end) {
		return source.subSequence(start, end);
	}

	/**
	 * @see de.ims.icarus2.util.strings.CodePointSequence#codePointCount()
	 */
	@Override
	public int codePointCount() {
		return codePointCount;
	}

	/**
	 * @see de.ims.icarus2.util.strings.CodePointSequence#codePointAt(int)
	 */
	@Override
	public int codePointAt(int index) {
		if(index>=codePointCount)
			throw new ArrayIndexOutOfBoundsException();
		return codepoints[index];
	}

	/**
	 * @see java.lang.CharSequence#chars()
	 */
	@Override
	public IntStream chars() {
		return source==null ? IntStream.empty() : source.chars();
	}

	/**
	 * @see java.lang.CharSequence#codePoints()
	 */
	@Override
	public IntStream codePoints() {
		return source==null ? IntStream.empty() : Arrays.stream(codepoints, 0, codePointCount);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new String(codepoints, 0, codePointCount);
	}

	/**
	 * @see de.ims.icarus2.util.Wrapper#get()
	 */
	@Override
	public CharSequence get() {
		return source;
	}

	/**
	 * @see de.ims.icarus2.util.Mutable#set(java.lang.Object)
	 */
	@Override
	public void set(@Nullable Object value) {
		set((CharSequence)value);
	}

	/**
	 * @see de.ims.icarus2.util.Mutable#clear()
	 */
	@Override
	public void clear() {
		set(null);
	}

	/**
	 * @see de.ims.icarus2.util.Mutable#isPrimitive()
	 */
	@Override
	public boolean isPrimitive() {
		return false;
	}

	/**
	 * @see de.ims.icarus2.util.Mutable#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return source==null;
	}
}
