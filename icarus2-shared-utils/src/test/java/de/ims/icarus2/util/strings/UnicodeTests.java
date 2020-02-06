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

import static de.ims.icarus2.util.lang.Primitives._int;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Learner tests for handling unicode in Java...
 *
 * @author Markus Gärtner
 *
 */
public class UnicodeTests {

	@Test
	void ASCIIUmlauts() {
		String s = "ÄäÜüÖö";
		System.out.println("Umlauts from "+s);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			System.out.print(c);
			System.out.print(Character.isHighSurrogate(c));
			System.out.print(Character.isLowSurrogate(c));
			System.out.println();
		}
	}

	@Test
	void codePointToString() {
		int cp = 164119;
		System.out.println("String from codepoint: "+cp);
		System.out.println(new String(new int[] {cp}, 0, 1));
	}

	@Test
	void codePointFromString() {
		String s = "𨄗";
		System.out.println("Codepoint from string: "+s);
		System.out.println(s.length());
		System.out.println(s.codePointCount(0, s.length()));
	}

	@Test
	void surrogateChars() {
		String s = "𨄗";
		System.out.println("Surrogates from string: "+s);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			System.out.print(c);
			System.out.print(Character.isHighSurrogate(c));
			System.out.print(Character.isLowSurrogate(c));
			System.out.println();
		}
	}

	@Test
	void lowerCaseString() {
		String s = "这是一个测试";
		System.out.println("Lowercase from string: "+s);
		System.out.println(s.toLowerCase());
	}

	@Test
	void lowerCaseCodepoints() {
		String s = "这是一个测试";
		System.out.println("Lowercase codepoints from string: "+s);
		int[] cps = new int[s.codePointCount(0, s.length())];
		int idx = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if(Character.isHighSurrogate(c) && ++i<s.length()) {
				char c2 = s.charAt(i);
				if(Character.isLowSurrogate(c2)) {
					int cp = Character.toCodePoint(c, c2);
					cps[idx++] = cp;
				} else {
					cps[idx++] = c2;
				}
			} else {
				cps[idx++] = c;
			}
		}
		String sl = new String(cps, 0, idx);
		System.out.println(sl);
		assertThat(sl).isEqualTo(s.toLowerCase());
	}

	private static String _string(int codepoint) {
		return new String(new int[] {codepoint}, 0, 1);
	}

	@Test
	void unicodeCharCountConsistency() {
		for (int cp_base = Character.MIN_CODE_POINT; cp_base <= Character.MAX_CODE_POINT; cp_base++) {
			int cp_lower = Character.toLowerCase(cp_base);
			int cp_upper = Character.toUpperCase(cp_base);
			int cp_title = Character.toTitleCase(cp_base);

			if(Character.charCount(cp_base)!=Character.charCount(cp_lower)) {
				System.out.printf("LowerCase length mismatch: %s base=%s (%d) lc=%s (%d)%n",
						Character.getName(cp_base), _string(cp_base), _int(Character.charCount(cp_base)),
						_string(cp_lower), _int(Character.charCount(cp_lower)));
			}
			if(Character.charCount(cp_base)!=Character.charCount(cp_upper)) {
				System.out.printf("UpperCase length mismatch: %s base=%s (%d) lc=%s (%d)%n",
						Character.getName(cp_base), _string(cp_base), _int(Character.charCount(cp_base)),
						_string(cp_upper), _int(Character.charCount(cp_upper)));
			}
			if(Character.charCount(cp_base)!=Character.charCount(cp_title)) {
				System.out.printf("TitleCase length mismatch: %s base=%s (%d) lc=%s (%d)%n",
						Character.getName(cp_base), _string(cp_base), _int(Character.charCount(cp_base)),
						_string(cp_title), _int(Character.charCount(cp_title)));
			}
		}
	}
}
