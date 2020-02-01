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
package de.ims.icarus2.query.api.eval;

import static de.ims.icarus2.util.lang.Primitives._int;
import static java.lang.Character.isHighSurrogate;
import static java.lang.Character.isLowSurrogate;
import static java.lang.Character.toCodePoint;
import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;

/**
 * Provides utility methods for the query evaluation engine for proper handling of
 * unicode codepoints while converting to fixed casing or ignoring case altogether.
 * <p>
 * Note that the matching methods implemented in this class are designed for efficiency
 * and cannot handle certain locale-specific conversion issues (such as the
 * <a href="http://mattryall.net/blog/2009/02/the-infamous-turkish-locale-bug">Turkish
 * locale problem</a>.
 *
 * @author Markus Gärtner
 *
 */
public final class StringEquals {

	private StringEquals() { /* no-op */ }

	/** Runs a basic character-wise equality check of two character sequences */
	public static boolean equal(CharSequence cs1, CharSequence cs2) {
		int len1 = cs1.length();
		int len2 = cs2.length();
		if(len1!=len2) {
			return false;
		}
		for (int i = 0; i < len1; i++) {
			if(cs1.charAt(i)!=cs2.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	/** Runs a basic character-wise inequality check of two character sequences */
	public static boolean notEqual(CharSequence cs1, CharSequence cs2) {
		int len1 = cs1.length();
		int len2 = cs2.length();
		if(len1!=len2) {
			return true;
		}
		for (int i = 0; i < len1; i++) {
			if(cs1.charAt(i)!=cs2.charAt(i)) {
				return true;
			}
		}
		return false;
	}

	private static IcarusRuntimeException forIncompleteSurrogatePair(
			CharSequence cs1, CharSequence cs2, int index) {
		//TODO change message to only include the context of each char sequence around index
		return new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT,
				String.format("Incomplete surrogate pair at char index %d in one of '%s' or '%s'",
						_int(index), cs1.toString(), cs2.toString()));
	}

	/**
	 * Runs a codepoint-aware equality check of two character sequences such that
	 * all codepoints or basic characters are first converted to lowercase.
	 */
	public static boolean equalLowerCase(CharSequence cs1, CharSequence cs2) {
		int len1 = cs1.length();
		int len2 = cs2.length();
		if(len1!=len2) {
			return false;
		}
		for (int i = 0; i < len1; i++) {
			char c1 = cs1.charAt(i);
			char c2 = cs2.charAt(i);

			if(isHighSurrogate(c1) && isHighSurrogate(c2) && ++i<len1) {
				// Need to check code point
				char c1_2 = cs1.charAt(i);
				char c2_2 = cs2.charAt(i);
				if(isLowSurrogate(c1_2) && isLowSurrogate(c2_2)) {
					// Proper surrogate pair
					int cp1 = toCodePoint(c1, c1_2);
					int cp2 = toCodePoint(c2, c2_2);
					if(toLowerCase(cp1)!=toLowerCase(cp2)) {
						return false;
					}
				} else {
					// Missing low surrogate(s)
					throw forIncompleteSurrogatePair(cs1, cs2, i);
				}
			} else if(toLowerCase(c1) != toLowerCase(c2)) { // BMP
				return false;
			}
		}
		return true;
	}

	/**
	 * Runs a codepoint-aware inequality check of two character sequences such that
	 * all codepoints or basic characters are first converted to lowercase.
	 */
	public static boolean notEqualLowerCase(CharSequence cs1, CharSequence cs2) {
		int len1 = cs1.length();
		int len2 = cs2.length();
		if(len1!=len2) {
			return true;
		}
		for (int i = 0; i < len1; i++) {
			char c1 = cs1.charAt(i);
			char c2 = cs2.charAt(i);

			if(isHighSurrogate(c1) && isHighSurrogate(c2) && ++i<len1) {
				// Need to check code point
				char c1_2 = cs1.charAt(i);
				char c2_2 = cs2.charAt(i);
				if(isLowSurrogate(c1_2) && isLowSurrogate(c2_2)) {
					// Proper surrogate pair
					int cp1 = toCodePoint(c1, c1_2);
					int cp2 = toCodePoint(c2, c2_2);
					if(toLowerCase(cp1)!=toLowerCase(cp2)) {
						return true;
					}
				} else {
					// Missing low surrogate(s)
					throw forIncompleteSurrogatePair(cs1, cs2, i);
				}
			} else if(toLowerCase(c1) != toLowerCase(c2)) { // BMP
				return true;
			}
		}
		return false;
	}

	/**
	 * Runs a codepoint-aware equality check of two character sequences such that
	 * case is ignored completely (two characters or code points are considered
	 * equal if they either match exactly or their lower or upper case versions
	 * match).
	 */
	public static boolean equalIgnoreCase(CharSequence cs1, CharSequence cs2) {
		int len1 = cs1.length();
		int len2 = cs2.length();
		if(len1!=len2) {
			return false;
		}
		for (int i = 0; i < len1; i++) {
			char c1 = cs1.charAt(i);
			char c2 = cs2.charAt(i);

			if(isHighSurrogate(c1) && isHighSurrogate(c2) && ++i<len1) {
				// Need to check code point
				char c1_2 = cs1.charAt(i);
				char c2_2 = cs2.charAt(i);
				if(isLowSurrogate(c1_2) && isLowSurrogate(c2_2)) {
					// Proper surrogate pair
					int cp1 = toCodePoint(c1, c1_2);
					int cp2 = toCodePoint(c2, c2_2);
					if(cp1 != cp2
						&& toLowerCase(cp1) != toLowerCase(cp2)
						&& toUpperCase(cp1) != toUpperCase(cp2)) {
						return false;
					}
				} else {
					// Missing low surrogate(s)
					throw forIncompleteSurrogatePair(cs1, cs2, i);
				}
			} else if(c1 != c2
					&& toLowerCase(c1) != toLowerCase(c2)
					&& toUpperCase(c1) != toUpperCase(c2)) { // BMP
				return false;
			}
		}
		return true;
	}

	/**
	 * Runs a codepoint-aware inequality check of two character sequences such that
	 * case is ignored completely (two characters or code points are considered
	 * equal if they either match exactly or their lower or upper case versions
	 * match).
	 */
	public static boolean notEqualIgnoreCase(CharSequence cs1, CharSequence cs2) {
		int len1 = cs1.length();
		int len2 = cs2.length();
		if(len1!=len2) {
			return true;
		}
		for (int i = 0; i < len1; i++) {
			char c1 = cs1.charAt(i);
			char c2 = cs2.charAt(i);

			if(isHighSurrogate(c1) && isHighSurrogate(c2) && ++i<len1) {
				// Need to check code point
				char c1_2 = cs1.charAt(i);
				char c2_2 = cs2.charAt(i);
				if(isLowSurrogate(c1_2) && isLowSurrogate(c2_2)) {
					// Proper surrogate pair
					int cp1 = toCodePoint(c1, c1_2);
					int cp2 = toCodePoint(c2, c2_2);
					if(cp1 != cp2
						&& toLowerCase(cp1) != toLowerCase(cp2)
						&& toUpperCase(cp1) != toUpperCase(cp2)) {
						return true;
					}
				} else {
					// Missing low surrogate(s)
					throw forIncompleteSurrogatePair(cs1, cs2, i);
				}
			} else if(c1 != c2
					&& toLowerCase(c1) != toLowerCase(c2)
					&& toUpperCase(c1) != toUpperCase(c2)) { // BMP
				return true;
			}
		}
		return false;
	}
}
