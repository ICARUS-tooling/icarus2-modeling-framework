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
package de.ims.icarus2.util.strings;

import static java.util.Objects.requireNonNull;

import java.awt.Component;
import java.awt.FontMetrics;
import java.text.Collator;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.util.id.Identifiable;
import de.ims.icarus2.util.id.Identity;
import de.ims.icarus2.util.intern.Interner;
import de.ims.icarus2.util.intern.StrongInterner;
import de.ims.icarus2.util.intern.WeakInterner;


/**
 * @author Markus Gärtner
 *
 */
public final class StringUtil {

	private static final Logger log = LoggerFactory.getLogger(StringUtil.class);

	public static final String TEXT_WILDCARD = "[...]";

	public static final String WEAK_INTERN_PROPERTY =
			"de.ims.icarus2.strings.useWeakIntern";

	public static final String NATIVE_INTERN_PROPERTY =
			"de.ims.icarus2.strings.useNativeIntern";

	private static final Locale ICARUS_LOCALE;
	static {
		ICARUS_LOCALE = new Locale("en", "us");
	}

	private static final Collator NAME_COMPARATOR;
	static {
		NAME_COMPARATOR = Collator.getInstance(ICARUS_LOCALE);
		NAME_COMPARATOR.setStrength(Collator.PRIMARY);
	}

	private StringUtil() {
		// no-op
	}

	public static Collator getCollator() {
		return NAME_COMPARATOR;
	}

	public static int compareLocaleAware(String s1, String s2) {
		return NAME_COMPARATOR.compare(s1, s2);
	}

	/**
	 * Implements {@link String#compareTo(String)} for arbitrary
	 * {@link CharSequence} objects.
	 *
	 * @param cs1
	 * @param cs2
	 * @return
	 */
	public static int compare(CharSequence cs1, CharSequence cs2) {
        int len1 = cs1.length();
        int len2 = cs2.length();
        int lim = Math.min(len1, len2);

        int k = 0;
        while (k < lim) {
            char c1 = cs1.charAt(k);
            char c2 = cs2.charAt(k);
            if (c1 != c2) {
                return c1 - c2;
            }
            k++;
        }
        return len1 - len2;
	}

	public static String format(String text, Object...params) {
		requireNonNull(text);
		if(text.indexOf('{')==-1) {
			return text;
		}

		StringBuilder result = new StringBuilder();
		String index = null;

		int paramsIndex = 0;

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);

			if (c == '{') {
				index = "";
			} else if (index != null && c == '}') {

				int tmp = paramsIndex;

				if(!index.isEmpty()) {
					tmp = Integer.parseInt(index) - 1;
				} else {
					paramsIndex++;
				}

				if (tmp >= 0 && params!=null && tmp < params.length) {
					result.append(params[tmp]);
				}

				index = null;
			} else if (index != null) {
				index += c;
			} else {
				result.append(c);
			}
		}

		return result.toString();
	}

	/**
	 * Current implementation ignores the {@code locale} parameter and
	 * delegates to {@link #format(String, Object...)}.
	 *
	 * @param locale
	 * @param text
	 * @param params
	 * @return
	 */
	//FIXME actually implement locale sensitive output!
	public static String format(Locale locale, String text, Object...params) {
		return format(text, params);
	}

	public static final Comparator<CharSequence> CHAR_SEQUENCE_COMPARATOR = new Comparator<CharSequence>() {

		@Override
		public int compare(CharSequence cs1, CharSequence cs2) {
	        int len1 = cs1.length();
	        int len2 = cs2.length();
	        int lim = Math.min(len1, len2);

	        int k = 0;
	        while (k < lim) {
	            char c1 = cs1.charAt(k);
	            char c2 = cs2.charAt(k);
	            if (c1 != c2) {
	                return c1 - c2;
	            }
	            k++;
	        }
	        return len1 - len2;
		}
	};

	//DEBUG
//	private static Interner<String> interner = new EmptyInterner<>();

	private static Interner<CharSequence> interner;
	private static final int defaultInternerCapacity = 2000;

	static {
		Interner<CharSequence> i;
		if("true".equals(System.getProperty(WEAK_INTERN_PROPERTY, "false"))) {
			i = new WeakInterner<CharSequence>(defaultInternerCapacity){

				/**
				 * @see de.ims.icarus2.util.intern.WeakInterner#delegate(java.lang.Object)
				 */
				@Override
				protected CharSequence delegate(CharSequence item) {
					return item instanceof String ? item : StringUtil.toString(item);
				}

			};
		} else if("true".equals(System.getProperty(NATIVE_INTERN_PROPERTY, "false"))) {
			i = new NativeStringInterner();
		} else {
			i = new StrongInterner<CharSequence>(defaultInternerCapacity){

				/**
				 * @see de.ims.icarus2.util.intern.StrongInterner#delegate(java.lang.Object)
				 */
				@Override
				protected CharSequence delegate(CharSequence item) {
					return item instanceof String ? item : StringUtil.toString(item);
				}

			};
		}
		interner = i;
	}

	/**
	 * Interns the given {@code CharSequence} and returns the shared {@code String} instance
	 * that equals its content. Allowing {@code CharSequence} objects to being interned is done
	 * to greatly speed up processes such as parsing, when millions of small strings would have
	 * to be created just to be interned and discarded a moment later. With the help of utility
	 * classes such as {@link CharTableBuffer} it is possible to buffer big chunks of character
	 * data and perform string operations on them by the use of cursor-like {@code CharSequence}
	 * implementations without having to keep unnecessary string objects in memory.
	 *
	 * @param s
	 * @return
	 */
	public static String intern(CharSequence s) {
		return s==null ? null : (String)interner.intern(s);
	}

	// EQUALITY

	public static boolean equals(CharSequence cs, Object obj) {
		if(obj instanceof CharSequence) {
			return equals(cs, (CharSequence)obj);
		}

		return false;
	}

	public static boolean equals(CharSequence cs, CharSequence other) {
		if(cs==null || other==null) {
			return cs==other;
		}

		int len = cs.length();
		if(len!=other.length()) {
			return false;
		}

		for(int i=len-1; i>=0; i--) {
			if(cs.charAt(i)!=other.charAt(i)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns the empty string in case the given one is {@code null}
	 */
	public static String notNull(String s) {
		return s==null ? "" : s;
	}

	/**
	 * Returns the specified {@code fallback} string in case the
	 * given one is {@code null}.
	 *
	 * @throws NullPointerException in case {@code fallback} is {@code null}
	 */
	public static String notNull(String s, String fallback) {
		requireNonNull(fallback);
		return s==null ? fallback : s;
	}

	private static final Pattern lineBreak = Pattern.compile("\r\n|\n|\r");

	public static String[] splitLines(String s) {
		return s==null ? null : lineBreak.split(s);
	}

	// HASHING
	//
	// both hash functions mirror the default behavior of String.hashCode() so that
	// substitution in hash-tables is possible.
	// If the hash function of String should ever be changed this needs to be addressed!

	public static int hash(CharSequence cs) {
		// String instances cache their hash codes, so we can utilize that!
		if(cs.getClass()==String.class) {
			return cs.hashCode();
		}

		int h = 0;

        for (int i = 0; i < cs.length(); i++) {
            h = 31 * h + cs.charAt(i);
        }

        return h;
	}

	public static int hash(char[] c, int offset, int len) {

		int h = 0;

        for (int i = 0; i < len; i++) {
            h = 31 * h + c[offset+i];
        }

        return h;
	}

	// STRING CONVERSION

	public static String toString(CharSequence cs) {
		if(cs instanceof String) {
			return (String) cs;
		}

		char[] tmp = new char[cs.length()];

		for(int i=cs.length()-1; i>=0; i--) {
			tmp[i] = cs.charAt(i);
		}

		return new String(tmp);
	}

	/**
	 *
	 * @param cs
	 * @param beginIndex the beginning index, inclusive
	 * @param endIndex the ending index, exclusive
	 * @return
	 */
	public static String toString(CharSequence cs, int beginIndex, int endIndex) {
		if(cs instanceof String) {
			return ((String) cs).substring(beginIndex, endIndex);
		}

		int length = endIndex-beginIndex;

		char[] tmp = new char[length];

		while(--length>=0) {
			tmp[length] = cs.charAt(beginIndex+length);
		}

		return new String(tmp);
	}

	/**
	 * @see String#startsWith(String, int)
	 */
    public static boolean startsWith(CharSequence s, CharSequence prefix, int toffset) {
        int to = toffset;
        int po = 0;
        int pc = prefix.length();
        // Note: toffset might be near -1>>>1.
        if ((toffset < 0) || (toffset > s.length() - pc)) {
            return false;
        }
        while (--pc >= 0) {
            if (s.charAt(to++) != prefix.charAt(po++)) {
                return false;
            }
        }
        return true;
    }

    /**
	 * @see String#startsWith(String)
	 */
    public static boolean startsWith(CharSequence s, CharSequence prefix) {
        return startsWith(s, prefix, 0);
    }

    /**
	 * @see String#endsWith(String)
	 */
    public static boolean endsWith(CharSequence s, CharSequence suffix) {
        return startsWith(s, suffix, s.length() - suffix.length());
    }

    /**
	 * @see String#indexOf(int)
	 */
    public static int indexOf(CharSequence s, char ch) {
        return indexOf(s, ch, 0);
    }

    /**
	 * @see String#indexOf(int, int)
	 */
    public static int indexOf(CharSequence s, char ch, int fromIndex) {
//        final int max = s.length();
//        if (fromIndex < 0) {
//            fromIndex = 0;
//        } else if (fromIndex >= max) {
//            // Note: fromIndex might be near -1>>>1.
//            return -1;
//        }
//
//        for (int i = fromIndex; i < max; i++) {
//            if (s.charAt(i) == ch) {
//                return i;
//            }
//        }
//        return -1;
    	return indexOf(s, ch, fromIndex, s.length()-1);
    }

    /**
	 * @see String#indexOf(int, int)
	 */
    public static int indexOf(CharSequence s, char ch, int fromIndex, int toIndex) {
    	if(s.length()==0) {
    		return -1;
    	}

        if (fromIndex < 0) {
            fromIndex = 0;
        } else if (fromIndex > toIndex) {
            // Note: fromIndex might be near -1>>>1.
            return -1;
        }

        for (int i = fromIndex; i <= toIndex; i++) {
            if (s.charAt(i) == ch) {
                return i;
            }
        }
        return -1;
    }

    /**
	 * @see String#lastIndexOf(int)
	 */
    public static int lastIndexOf(CharSequence s, char ch) {
        return lastIndexOf(s, ch, s.length() - 1);
    }

    /**
	 * @see String#lastIndexOf(int, int)
	 */
    public static int lastIndexOf(CharSequence s, char ch, int fromIndex) {
    	if(s.length()==0) {
    		return -1;
    	}

        int i = Math.min(fromIndex, s.length() - 1);
        for (; i >= 0; i--) {
            if (s.charAt(i) == ch) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOf(CharSequence s, CharSequence str) {
        return indexOf(s, str, 0);
    }

    public static int indexOf(CharSequence s, CharSequence str, int fromIndex) {
        return indexOf(s, 0, s.length(), str, 0, str.length(), fromIndex);
    }

    public static int lastIndexOf(CharSequence s, CharSequence str) {
        return lastIndexOf(s, str, s.length());
    }

    public static int lastIndexOf(CharSequence s, CharSequence str, int fromIndex) {
        return lastIndexOf(s, 0, s.length(),
                str, 0, str.length(), fromIndex);
    }

    public static int indexOf(CharSequence source, int sourceOffset, int sourceCount,
    		CharSequence target, int targetOffset, int targetCount,
            int fromIndex) {
        if (fromIndex >= sourceCount) {
            return (targetCount == 0 ? sourceCount : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetCount == 0) {
            return fromIndex;
        }

        char first = target.charAt(targetOffset);
        int max = sourceOffset + (sourceCount - targetCount);

        for (int i = sourceOffset + fromIndex; i <= max; i++) {
            /* Look for first character. */
            if (source.charAt(i) != first) {
                while (++i <= max && source.charAt(i) != first) { /* no-op */}
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for (int k = targetOffset + 1; j < end && source.charAt(j)
                        == target.charAt(k); j++, k++) { /* no-op */}

                if (j == end) {
                    /* Found whole string. */
                    return i - sourceOffset;
                }
            }
        }
        return -1;
    }

    public static int lastIndexOf(CharSequence source, int sourceOffset, int sourceCount,
    		CharSequence target, int targetOffset, int targetCount,
            int fromIndex) {
        /*
         * Check arguments; return immediately where possible. For
         * consistency, don't check for null str.
         */
        int rightIndex = sourceCount - targetCount;
        if (fromIndex < 0) {
            return -1;
        }
        if (fromIndex > rightIndex) {
            fromIndex = rightIndex;
        }
        /* Empty string always matches. */
        if (targetCount == 0) {
            return fromIndex;
        }

        int strLastIndex = targetOffset + targetCount - 1;
        char strLastChar = target.charAt(strLastIndex);
        int min = sourceOffset + targetCount - 1;
        int i = min + fromIndex;

        startSearchForLastChar:
        while (true) {
            while (i >= min && source.charAt(i) != strLastChar) {
                i--;
            }
            if (i < min) {
                return -1;
            }
            int j = i - 1;
            int start = j - (targetCount - 1);
            int k = strLastIndex - 1;

            while (j > start) {
                if (source.charAt(j--) != target.charAt(k--)) {
                    i--;
                    continue startSearchForLastChar;
                }
            }
            return start - sourceOffset + 1;
        }
    }

    public static boolean isNullOrEmpty(String s) {
    	return s==null || s.trim().isEmpty();
    }

    /**
     *
     * @param obj
     * @return
     *
     * @see NamedObject
     * @see Identifiable
     * @see Identity
     */
	public static String getName(Object obj) {
		if(obj==null)
			return null;

		if(obj instanceof NamedObject)
			return ((NamedObject)obj).getName();
		if(obj instanceof Identifiable) {
			obj = ((Identifiable)obj).getIdentity();
		}

		Optional<String> opt = Optional.empty();
		if(obj instanceof Identity)
			opt = ((Identity)obj).getName();

		return opt.orElseGet(obj::toString);
	}

	/**
	 *
	 * @param obj
	 * @return
	 *
	 * @see TextItem
	 */
	public static String asText(Object obj) {
		if(obj==null)
			return null;

		if(obj instanceof TextItem)
			return ((TextItem)obj).getText();

		return obj.toString();
	}

	private static volatile Pattern indexPattern;

	private static Pattern indexPattern() {
		if(indexPattern==null) {
			// Don't care about synchronization, since concurrent initializations still
			// yield functionally identical Pattern instances
			indexPattern = Pattern.compile("\\((\\d+)\\)$");
		}

		return indexPattern;
	}

	public static String getBaseName(String name) {
		Matcher matcher = indexPattern().matcher(name);
		if(matcher.find())
			return name.substring(0, name.length()-matcher.group().length()).trim();

		return name;
	}

	public static int getCurrentCount(String name) {
		Matcher matcher = indexPattern().matcher(name);
		if(matcher.find()) {
			int currentCount = 0;
			try {
				currentCount = Integer.parseInt(matcher.group(1));
			} catch(NumberFormatException e) {
				log.error("Failed to parse existing base name index suffix: {}", name, e);
			}
			return currentCount;
		}

		return -1;
	}


	public static String getUniqueName(String baseName, Set<String> usedNames) {
		return getUniqueName(baseName, usedNames, false);
	}

	public static String getUniqueName(String baseName, Set<String> usedNames, boolean allowBaseName) {
		if(baseName==null)
			throw new NullPointerException("Invalid basename");
		if(usedNames==null)
			throw new NullPointerException("Invalid used name set");

		if(usedNames.isEmpty())
			return baseName;

		if(allowBaseName) {
			usedNames.remove(baseName);
		}

		String name = baseName;
		int count = Math.max(2, getCurrentCount(baseName)+1);
		baseName = getBaseName(baseName);

		if(usedNames.contains(name)) {
			while(usedNames.contains((name = baseName+" ("+count+")"))) {
				count++;
			}
		}

		return name;
	}

	public static String fit(String s, int maxLength) {
		return fit(s, maxLength, null);
	}

	public static String fit(String s, int maxLength, String wildcard) {
		if(s==null)
			return "";
		if(s.length()<=maxLength)
			return s;
		if(wildcard==null || wildcard.isEmpty()) {
			wildcard = TEXT_WILDCARD;
		}

		int chunkLength = (maxLength-wildcard.length())/2;

		StringBuilder sb = new StringBuilder(maxLength);
		sb.append(s, 0, chunkLength)
		.append(wildcard)
		.append(s, chunkLength+wildcard.length(), maxLength-chunkLength);

		return sb.toString();
	}

	private static DecimalFormat decimalFormat = new DecimalFormat("#,###");

	public static String formatDecimal(int value) {
		synchronized (decimalFormat) {
			return decimalFormat.format(value);
		}
	}
	public static String formatDecimal(long value) {
		synchronized (decimalFormat) {
			return decimalFormat.format(value);
		}
	}

	private static DecimalFormat fractionDecimalFormat = new DecimalFormat("#,##0.00");

	public static String formatDecimal(double value) {
		synchronized (fractionDecimalFormat) {
			return fractionDecimalFormat.format(value);
		}
	}
	public static String formatDecimal(float value) {
		synchronized (fractionDecimalFormat) {
			return fractionDecimalFormat.format(value);
		}
	}
	public static String formatShortenedDecimal(double value) {
		synchronized (fractionDecimalFormat) {
			if(value>=1_000_000_000)
				return fractionDecimalFormat.format(value/1_000_000_000)+'G';
			else if(value>=1_000_000)
				return fractionDecimalFormat.format(value/1_000_000)+'M';
			else if(value>=1_000)
				return fractionDecimalFormat.format(value/1_000)+'K';
			else
				return formatDecimal((int) value);
		}
	}

	public static String formatShortenedDecimal(int value) {
		if(value>=1_000_000_000)
			return formatDecimal(value/1_000_000_000)+'G';
		else if(value>=1_000_000)
			return formatDecimal(value/1_000_000)+'M';
		else if(value>=1_000)
			return formatDecimal(value/1_000)+'K';
		else
			return formatDecimal(value);
	}

	public static String formatDuration(long time) {
		if(time<=0)
			return null;

		long s = time/1000;
		long m = s/60;
		long h = m/60;
		long d = h/24;

		s = s%60;
		m = m%60;
		h = h%24;

		StringBuilder sb = new StringBuilder();
		if(d>0) {
			sb.append(' ').append(d).append('D');
		}
		if(h>0) {
			sb.append(' ').append(h).append('H');
		}
		if(m>0) {
			sb.append(' ').append(m).append('M');
		}
		if(s>0) {
			sb.append(' ').append(s).append('S');
		}

		StringUtil.trim(sb);

		return sb.toString();
	}

	public static void trim(StringBuilder sb) {
		while(sb.length()>0 && Character.isWhitespace(sb.charAt(0))) {
			sb.delete(0, 1);
		}
		while(sb.length()>0 && Character.isWhitespace(sb.charAt(sb.length()-1))) {
			sb.delete(sb.length()-1, sb.length());
		}
	}

	public static void trimLeft(StringBuilder sb) {
		while(sb.length()>0 && Character.isWhitespace(sb.charAt(0))) {
			sb.delete(0, 1);
		}
	}

	public static void trimRight(StringBuilder sb) {
		while(sb.length()>0 && Character.isWhitespace(sb.charAt(sb.length()-1))) {
			sb.delete(sb.length()-1, sb.length());
		}
	}

	public static final int MIN_WRAP_WIDTH = 50;

	public static String wrap(String s, Component comp, int width) {
		return wrap(s, comp.getFontMetrics(comp.getFont()), width);
	}

	/**
	 * Wraps a given {@code String} so that its lines do not exceed
	 * the specified {@code width} value in length.
	 */
	public static String wrap(String s, FontMetrics fm, int width) {
		if(fm==null)
			throw new NullPointerException("Invalid font metrics");
		//if(width<MIN_WRAP_WIDTH)
		//	throw new IllegalArgumentException("Width must not be less than "+MIN_WRAP_WIDTH+" pixels");

		if(s==null || s.length()==0)
			return s;

		// FIXME if input string contains multiple linebreaks the last one will be omitted

		StringBuilder sb = new StringBuilder();
		StringBuilder line = new StringBuilder();
		StringBuilder block = new StringBuilder();

		int size = s.length();
		int len = 0;
		int blen = 0;
		for(int i=0; i<size; i++) {
			char c = s.charAt(i);
			boolean lb = false;
			if(c=='\r') {
				continue;
			} else if(c=='\n' || isBreakable(c)) {
				lb = c=='\n';
				if(!lb) {
					block.append(c);
					blen += fm.charWidth(c);
				}
				line.append(block);
				block.setLength(0);
				len += blen;
				blen = 0;
			} else {
				block.append(c);
				blen += fm.charWidth(c);
				lb = (len+blen) >= width;
			}

			if(lb && sb.length()>0) {
				sb.append('\n');
			}
			if(i==size-1) {
				line.append(block);
			}
			if(lb || i==size-1) {
				sb.append(line.toString().trim());
				line.setLength(0);
				len = 0;
			}
		}

		return sb.toString();
	}

	public static String[] split(String s, Component comp, int width) {
		return split(s, comp.getFontMetrics(comp.getFont()), width);
	}

	public static String[] split(String s, FontMetrics fm, int width) {
		if(fm==null)
			throw new NullPointerException("Invalid font metrics");
		//if(width<MIN_WRAP_WIDTH)
		//	throw new IllegalArgumentException("Width must not be less than "+MIN_WRAP_WIDTH+" pixels");

		if(s==null || s.length()==0)
			return new String[0];

		List<String> result = new ArrayList<>();
		StringBuilder line = new StringBuilder();
		StringBuilder block = new StringBuilder();

		int size = s.length();
		int len = 0;
		int blen = 0;
		for(int i=0; i<size; i++) {
			char c = s.charAt(i);
			boolean lb = false;
			if(c=='\r') {
				continue;
			} else if(c=='\n' || isBreakable(c)) {
				lb = c=='\n';
				if(!lb) {
					block.append(c);
					blen += fm.charWidth(c);
				}
				line.append(block);
				block.setLength(0);
				len += blen;
				blen = 0;
			} else {
				block.append(c);
				blen += fm.charWidth(c);
				lb = (len+blen) >= width;
			}

			if(i==size-1) {
				line.append(block);
				lb = true;
			}

			if(lb) {
				result.add(line.toString().trim());
				line.setLength(0);
				len = 0;
			}
		}

		return result.toArray(new String[result.size()]);
	}

	private static boolean isBreakable(char c) {
		return Character.isWhitespace(c) || !Character.isLetterOrDigit(c);
	}

	public static String capitalize(String s) {
		if(s==null || s.length()<2)
			throw new NullPointerException("Invalid string");

		return Character.toUpperCase(s.charAt(0))+s.substring(1);
	}

	public static String join(String[] tokens) {
		return join(tokens, ", ", '[', ']');
	}

	public static String join(String[] tokens, String separator, char start, char end) {
		if(tokens==null || tokens.length==0)
			return "";
		StringBuilder sb = new StringBuilder();

		sb.append(start);
		for(int i=0; i<tokens.length; i++) {
			if(i>0) {
				sb.append(separator);
			}
			sb.append(tokens[i]);
		}
		sb.append(end);

		return sb.toString();
	}

	public static String join(String[] tokens, String separator) {
		if(tokens==null || tokens.length==0)
			return "";
		StringBuilder sb = new StringBuilder();

		for(int i=0; i<tokens.length; i++) {
			if(i>0) {
				sb.append(separator);
			}
			sb.append(tokens[i]);
		}

		return sb.toString();
	}

	public static int compareNumberAwareIgnoreCase(String s1, String s2) {
		try {
			int i1 = Integer.parseInt(s1);
			int i2 = Integer.parseInt(s2);

			return i1-i2;
		} catch(NumberFormatException e) {
			// ignore
		}

		return s1.compareToIgnoreCase(s2);
	}

	public static int compareNumberAware(String s1, String s2) {
		try {
			int i1 = Integer.parseInt(s1);
			int i2 = Integer.parseInt(s2);

			return i1-i2;
		} catch(NumberFormatException e) {
			// ignore
		}

		return s1.compareToIgnoreCase(s2);
	}

	public static boolean endsWith(CharSequence s, char c) {
		return s.length()>0 && s.charAt(s.length()-1)==c;
	}

	public static String padRight(String s, int n) {
	     return String.format("%1$-" + n + "s", s);
	}

	public static String padLeft(String s, int n) {
	    return String.format("%1$" + n + "s", s);
	}

	/**
	 * Replaces all occurrences of {@code c} in {@code source} with the string
	 * {@code r}.
	 * @param source
	 * @param s
	 * @param r
	 * @return
	 */
	public static String replaceAll(String source, char s, String r) {
		requireNonNull(source);
		requireNonNull(r);

		if(source.indexOf(s)==-1) {
			return source;
		}

		StringBuilder sb = new StringBuilder(source.length()+10*r.length()); //TODO smart estimation?

		for (int i = 0; i < source.length(); i++) {
			char c = source.charAt(i);
			if(source.charAt(i)==s) {
				sb.append(r);
			} else {
				sb.append(c);
			}
		}

		return sb.toString();
	}

    /**
     * All possible chars for representing a number as a String
     */
    final static char[] digits = {
        '0' , '1' , '2' , '3' , '4' , '5' ,
        '6' , '7' , '8' , '9' , 'a' , 'b' ,
        'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
        'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
        'o' , 'p' , 'q' , 'r' , 's' , 't' ,
        'u' , 'v' , 'w' , 'x' , 'y' , 'z'
    };

    static NumberFormatException forInputString(char[] src, int offset, int len) {
        return new NumberFormatException("For input string: \"" + new String(src, offset, len) + "\"");
    }

	/**
	 * Reads a portion of a character sequence as a hexadecimal encoded
	 * {@code long} value.
	 *
	 * @param src
	 * @param offset
	 * @param len
	 * @return
	 */
	public static long parseHexString(char[] src, int offset, int len) {

		String tmp = new String(src, offset, len);
		return Long.parseUnsignedLong(tmp, 16);

//        long result = 0;
//        boolean negative = false;
//        int i = offset;
//        long limit = -Long.MAX_VALUE;
//        long multmin;
//        int digit;
//
//        if (len > 0) {
//            char firstChar = src[offset];
//            if (firstChar < '0') { // Possible leading "+" or "-"
//                if (firstChar == '-') {
//                    negative = true;
//                    limit = Long.MIN_VALUE;
//                } else if (firstChar != '+')
//                    throw forInputString(src, offset, len);
//
//                if (len == 1) // Cannot have lone "+" or "-"
//                    throw forInputString(src, offset, len);
//                i++;
//            }
//            multmin = limit >>> 4;
//            while (i < len) {
//                // Accumulating negatively avoids surprises near MAX_VALUE
//                digit = Character.digit(src[i++], 16);
//                if (digit < 0) {
//                    throw forInputString(src, offset, len);
//                }
//                if (result < multmin) {
//                    throw forInputString(src, offset, len);
//                }
//                result <<= 4;
//                if (result < limit + digit) {
//                    throw forInputString(src, offset, len);
//                }
//                result -= digit;
//            }
//        } else {
//            throw forInputString(src, offset, len);
//        }
//
//        return negative ? result : -result;
	}

	/**
	 * Writes a {@code long} value into a character array in hexadecimal form
	 * and returns the number of characters written.
	 *
	 * @param val
	 * @param dst
	 * @param offset
	 * @return
	 */
	public static int writeHexString(long val, char[] dst, int offset) {
		String tmp = Long.toHexString(val);
		tmp.getChars(0, tmp.length(), dst, offset);
		return tmp.length();

//		checkArgument(val!=Long.MIN_VALUE);
//
//		boolean negative = val<0L;
//		if(negative) {
//			val = -val;
//			dst[offset++] = '-';
//		}
//        int mag = Long.SIZE - Long.numberOfLeadingZeros(val);
//        int chars = Math.max(((mag + 3) >>>2), 1);
//
//        int charPos = chars;
//        final int mask = (1 << 4) - 1;
//        do {
//        	dst[offset + --charPos] = digits[((int) val) & mask];
//            val >>>= 4;
//        } while (val != 0 && charPos > 0);
//
//        if(negative) {
//        	chars++;
//        }
//
//        return chars;
	}
}
