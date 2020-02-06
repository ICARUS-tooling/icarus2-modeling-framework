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

import static java.lang.Character.isHighSurrogate;
import static java.lang.Character.isLowSurrogate;
import static java.lang.Character.isSupplementaryCodePoint;
import static java.lang.Character.toCodePoint;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.util.function.CharBiPredicate;
import de.ims.icarus2.util.function.IntBiPredicate;

/**
 * Provides basic implementations for equality and containment checks of {@link CharSequence} objects.
 *
 * @author Markus Gärtner
 *
 */
public final class CodePointUtils {

	private CodePointUtils() { /* no-op */ }

	public static boolean containsSupplementaryCodePoints(CharSequence seq) {
		int len = seq.length();
		return codePointCount(seq, 0, len)<len;
	}

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

//	/** Runs a basic character-wise equality check of two codepoint sequences */
//	public static boolean equalsCodePoints(CodePointSequence cs1, CodePointSequence cs2,
//			IntBiPredicate comparator) {
//		int len1 = cs1.codePointCount();
//		int len2 = cs2.codePointCount();
//		if(len1!=len2) {
//			return false;
//		}
//		for (int i = 0; i < len1; i++) {
//			if(!comparator.test(cs1.codePointAt(i), cs2.codePointAt(i))) {
//				return false;
//			}
//		}
//		return true;
//	}

    public static int codePointAt(CharSequence seq, int index) {
        char c1 = seq.charAt(index);
        if (isHighSurrogate(c1)) {
			// Supplementary codepoint handling
			if(++index<seq.length()) {
				char c2 = seq.charAt(index);
				if(isLowSurrogate(c2)) {
					return toCodePoint(c1, c2);
				}
				throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT,
						String.format("Incomplete surrogate pair: %s is not a valid "
								+ "low surrogate after %s", Integer.toHexString(c1),
								Integer.toHexString(c2)));
			}
			throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT,
					"Incomplete high surrogate: "+Integer.toHexString(c1));
        }
        return c1;
    }

	/** Runs a basic character-wise equality check of two codepoint sequences */
	public static boolean equalsCodePoints(CharSequence cs1, CharSequence cs2,
			IntBiPredicate comparator) {
		int len1 = cs1.length();
		int len2 = cs2.length();
		// We do not support 1:m codepoint mappings, so length will stay the same
		if(len1!=len2) {
			return false;
		}
		for (int i1 = 0, i2 = 0; i1 < len1 && i2 < len2; i1++, i2++) {
			int cp1 = codePointAt(cs1, i1);
			int cp2 = codePointAt(cs2, i2);
			if(!comparator.test(cp1, cp2)) {
				return false;
			}

			if(isSupplementaryCodePoint(cp1)) {
				i1++;
			}
			if(isSupplementaryCodePoint(cp2)) {
				i2++;
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

//	/**
//	 * Runs a basic character-wise check on whether {@code target} contains the
//	 * specified {@code query} string while ignoring case.
//	 */
//	public static boolean containsCodePoints(CodePointSequence target, CodePointSequence query,
//			IntBiPredicate comparator) {
//        int first = query.codePointAt(0);
//        int count = query.codePointCount();
//        int max = target.codePointCount() - count;
//
//        for (int i = 0; i <= max; i++) {
//            /* Look for first code point. */
//            while (!comparator.test(target.codePointAt(i), first) && ++i <= max) {
//            	// no-op
//            }
//
//            /* Found first character, now look at the rest of query */
//            if (i <= max) {
//                int j = i + 1;
//                int end = j + count - 1;
//                for (int k = 1; j < end && comparator.test(target.codePointAt(j),
//                		query.codePointAt(k)); j++, k++) {
//                	// no-op
//                }
//
//                if (j == end) {
//                    /* Found whole query. */
//                    return true;
//                }
//            }
//        }
//        return false;
//	}

	/** Number of codepoint between beginIndex (inclusive) and endIndex (exclusive */
    public static int codePointCount(CharSequence seq, int beginIndex, int endIndex) {
        int length = seq.length();
        if (beginIndex < 0 || endIndex > length || beginIndex > endIndex)
            throw new IndexOutOfBoundsException();
        int n = endIndex - beginIndex;
        for (int i = beginIndex; i < endIndex; i++) {
        	int cp = codePointAt(seq, i);
        	if(Character.isSupplementaryCodePoint(cp)) {
        		i++;
        	}
        }
        return n;
    }

	/**
	 * Runs a basic character-wise check on whether {@code target} contains the
	 * specified {@code query} string while ignoring case.
	 */
	public static boolean containsCodePoints(CharSequence target, CharSequence query,
			IntBiPredicate comparator) {
	    int first = codePointAt(query, 0);
	    int qOffset = Character.isSupplementaryCodePoint(first) ? 2 : 1;
	    int count = query.length();
	    int max = target.length() - count + qOffset - 1;

	    for (int i = 0; i <= max; i++) {
	        /* Look for first code point. */
	    	int cursor = -1;
	    	do {
	    		cursor = codePointAt(target, i);
	    		if(Character.isSupplementaryCodePoint(cursor)) {
	    			i++;
	    		}
	    	}  while (!comparator.test(cursor, first) && ++i <= max);

	        /* Found first code point, now look at the rest of query */
	        if (i <= max) {
	            int j = i + 1;
	            int end = j + count - qOffset;
	            for (int k = qOffset; j < end; j++, k++) {
	            	int cp1 = codePointAt(target, j);
	            	int cp2 = codePointAt(query, k);
	            	if(!comparator.test(cp1, cp2)) {
	            		break;
	            	}
		    		if(Character.isSupplementaryCodePoint(cp1)) {
		    			j++;
		    		}
		    		if(Character.isSupplementaryCodePoint(cp1)) {
		    			k++;
		    		}
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
