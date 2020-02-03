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

import de.ims.icarus2.util.function.CharBiPredicate;
import de.ims.icarus2.util.function.IntBiPredicate;
import de.ims.icarus2.util.strings.CodePointSequence;

/**
 * Provides basic implementations for equality and containment checks of both
 * normal {@link CharSequence} objects and the unicode equivalent {@link CodePointSequence}.
 *
 * @author Markus Gärtner
 *
 */
public final class CodePointUtils {

	private CodePointUtils() { /* no-op */ }

	/** Runs a basic character-wise equality check of two character sequences */
	public static boolean equalsChars(CharSequence cs1, CharSequence cs2,
			CharBiPredicate comparator) {
		int len1 = cs1.length();
		int len2 = cs2.length();
		if(len1!=len2) {
			return false;
		}
		for (int i = 0; i < len1; i++) {
			if(!comparator.test(cs1.charAt(i), cs2.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/** Runs a basic character-wise equality check of two codepoint sequences */
	public static boolean equalsCodePoints(CodePointSequence cs1, CodePointSequence cs2,
			IntBiPredicate comparator) {
		int len1 = cs1.codePointCount();
		int len2 = cs2.codePointCount();
		if(len1!=len2) {
			return false;
		}
		for (int i = 0; i < len1; i++) {
			if(!comparator.test(cs1.codePointAt(i), cs2.codePointAt(i))) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Runs a basic character-wise check on whether {@code target} contains the
	 * specified {@code query} string.
	 */
	public static boolean containsChars(CharSequence target, CharSequence query,
			CharBiPredicate comparator) {
        char first = query.charAt(0);
        int count = query.length();
        int max = target.length() - count;

        for (int i = 0; i <= max; i++) {
            /* Look for first character. */
            while (!comparator.test(target.charAt(i), first) && ++i <= max) {
            	// no-op
            }

            /* Found first character, now look at the rest of query */
            if (i <= max) {
                int j = i + 1;
                int end = j + count - 1;
                for (int k = 1; j < end && comparator.test(target.charAt(j), query.charAt(k)); j++, k++) {
                	// no-op
                }

                if (j == end) {
                    /* Found whole query. */
                    return true;
                }
            }
        }
        return false;
	}

	/**
	 * Runs a basic character-wise check on whether {@code target} contains the
	 * specified {@code query} string while ignoring case.
	 */
	public static boolean containsCodePoints(CodePointSequence target, CodePointSequence query,
			IntBiPredicate comparator) {
        int first = query.codePointAt(0);
        int count = query.codePointCount();
        int max = target.codePointCount() - count;

        for (int i = 0; i <= max; i++) {
            /* Look for first code point. */
            while (!comparator.test(target.codePointAt(i), first) && ++i <= max) {
            	// no-op
            }

            /* Found first character, now look at the rest of query */
            if (i <= max) {
                int j = i + 1;
                int end = j + count - 1;
                for (int k = 1; j < end && comparator.test(target.codePointAt(j),
                		query.codePointAt(k)); j++, k++) {
                	// no-op
                }

                if (j == end) {
                    /* Found whole query. */
                    return true;
                }
            }
        }
        return false;
	}
}
