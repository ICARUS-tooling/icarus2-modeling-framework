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

import static java.lang.Character.isValidCodePoint;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.stream.IntStream;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;

/**
 * An extension of the {@link CharSequence} interface that also provides a
 * view on the data in the form of unicode codepoints instead of UTF-16
 * characters.
 *
 * @author Markus Gärtner
 *
 */
public interface CodePointSequence extends CharSequence {

	int codePointCount();

	int codePointAt(int index);

	default boolean containsSupplementaryCodePoints() {
		return codePointCount()<length();
	}


	public static CodePointSequence fixed(String s) {
		requireNonNull(s);
		return s.isEmpty() ? EMPTY_SEQUENCE : new FixedCodePointSequence(s);
	}

	public static boolean equals(CodePointSequence cp1, Object obj) {
		if(cp1==obj) {
			return true;
		} else if(obj instanceof CodePointSequence) {
			CodePointSequence other = (CodePointSequence) obj;
			if(cp1.codePointCount() != other.codePointCount()) {
				return false;
			}
			int len = cp1.codePointCount();
			for (int i = 0; i < len; i++) {
				if(cp1.codePointAt(i) != other.codePointAt(i)) {
					return false;
				}
			}
			return true;
		} else if(obj instanceof CharSequence) {
			return StringUtil.equals(cp1, (CharSequence)obj);
		}
		return false;
	}

	public static CodePointSequence EMPTY_SEQUENCE = new CodePointSequence() {

		@Override
		public CharSequence subSequence(int start, int end) {
			throw new ArrayIndexOutOfBoundsException();
		}

		@Override
		public int length() { return 0; }

		@Override
		public char charAt(int index) { throw new ArrayIndexOutOfBoundsException(); }

		@Override
		public int codePointCount() { return 0; }

		@Override
		public int codePointAt(int index) { throw new ArrayIndexOutOfBoundsException(); }
	};

	public static class FixedCodePointSequence implements CodePointSequence {
		private final String source;
		private final int[] codepoints;

		private FixedCodePointSequence(String s) {
			source = requireNonNull(s);
			codepoints = s.codePoints()
					// Need validation to be consistent with CodePointBuffer
					.filter(cp -> {
						if(!isValidCodePoint(cp))
							throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT,
									"Not a valid unicode code point: "+Integer.toHexString(cp));
						return true;
					})
					.toArray();
		}

		@Override
		public int length() { return source.length(); }

		@Override
		public char charAt(int index) { return source.charAt(index); }

		@Override
		public CharSequence subSequence(int start, int end) {
			return source.subSequence(start, end);
		}

		@Override
		public int codePointCount() { return codepoints.length; }

		@Override
		public int codePointAt(int index) { return codepoints[index]; }

		@Override
		public IntStream chars() { return source.chars(); }

		@Override
		public IntStream codePoints() { return Arrays.stream(codepoints); }

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return new String(codepoints, 0, codepoints.length);
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 * @see CodePointSequence#equals(CodePointSequence, Object)
		 */
		@Override
		public boolean equals(Object obj) {
			return CodePointSequence.equals(this, obj);
		}
	}
}
