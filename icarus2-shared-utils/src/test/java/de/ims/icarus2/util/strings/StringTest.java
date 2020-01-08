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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.annotations.PostponedTest;

/**
 * Test case for {@link AbstractString} instances
 *
 * @author Markus Gärtner
 *
 */
interface StringTest<S extends AbstractString> extends CharSequenceTest<S> {

	default S createEmptyString() {
		return createEmptySequence();
	}

	default S createString(int length) {
		return createSequence(length);
	}

	default S createString(String source) {
		return createSequence(source);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.AbstractString#hashCode()}.
	 */
	@Test
	default void testHashCode() {
		S empty = createEmptyString();
		if(empty!=null) {
			assertEquals("".hashCode(), empty.hashCode());
			cleanup(empty);
		}

		S withLength = createString(10);
		if(withLength!=null) {
			assertEquals(withLength.toString().hashCode(), withLength.hashCode());
			cleanup(withLength);
		}

		S fromSource = createString("test123");
		if(fromSource!=null) {
			assertEquals("test123".hashCode(), fromSource.hashCode());
			cleanup(fromSource);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.AbstractString#isEmpty()}.
	 */
	@Test
	default void testIsEmpty() {
		S empty = createEmptyString();
		if(empty!=null) {
			assertTrue(empty.isEmpty());
			cleanup(empty);
		}

		S withLength = createString(10);
		if(withLength!=null) {
			assertFalse(withLength.isEmpty());
			cleanup(withLength);
		}

		S fromSource = createString("test123");
		if(fromSource!=null) {
			assertFalse(fromSource.isEmpty());
			cleanup(fromSource);
		}

		S fromEmptySource = createString("");
		if(fromEmptySource!=null) {
			assertTrue(fromEmptySource.isEmpty());
			cleanup(fromEmptySource);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.AbstractString#compareTo(java.lang.CharSequence)}.
	 */
	@Test
	@PostponedTest
	default void testCompareTo() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.AbstractString#regionMatches(int, java.lang.CharSequence, int, int)}.
	 */
	@Test
	@PostponedTest
	default void testRegionMatchesIntCharSequenceIntInt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.AbstractString#regionMatches(boolean, int, java.lang.CharSequence, int, int)}.
	 */
	@Test
	@PostponedTest
	default void testRegionMatchesBooleanIntCharSequenceIntInt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.AbstractString#startsWith(java.lang.CharSequence, int)}.
	 */
	@Test
	@PostponedTest
	default void testStartsWithCharSequenceInt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.AbstractString#startsWith(java.lang.CharSequence)}.
	 */
	@Test
	@PostponedTest
	default void testStartsWithCharSequence() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.AbstractString#endsWith(java.lang.CharSequence)}.
	 */
	@Test
	@PostponedTest
	default void testEndsWith() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.AbstractString#indexOf(char)}.
	 */
	@Test
	@PostponedTest
	default void testIndexOfChar() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.AbstractString#indexOf(char, int)}.
	 */
	@Test
	@PostponedTest
	default void testIndexOfCharInt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.AbstractString#lastIndexOf(char)}.
	 */
	@Test
	@PostponedTest
	default void testLastIndexOfChar() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.AbstractString#lastIndexOf(char, int)}.
	 */
	@Test
	@PostponedTest
	default void testLastIndexOfCharInt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.AbstractString#indexOf(java.lang.CharSequence)}.
	 */
	@Test
	@PostponedTest
	default void testIndexOfCharSequence() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.AbstractString#indexOf(java.lang.CharSequence, int)}.
	 */
	@Test
	@PostponedTest
	default void testIndexOfCharSequenceInt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.AbstractString#lastIndexOf(java.lang.CharSequence)}.
	 */
	@Test
	@PostponedTest
	default void testLastIndexOfCharSequence() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.AbstractString#lastIndexOf(java.lang.CharSequence, int)}.
	 */
	@Test
	@PostponedTest
	default void testLastIndexOfCharSequenceInt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.AbstractString#matches(java.lang.String)}.
	 */
	@Test
	@PostponedTest
	default void testMatches() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.AbstractString#contains(java.lang.CharSequence)}.
	 */
	@Test
	default void testContains() {
		S empty = createEmptyString();
		if(empty!=null) {
			assertTrue(empty.contains(""));
			assertFalse(empty.contains("x"));
			cleanup(empty);
		}

		String s = "test123";
		S fromSource = createString(s);
		if(fromSource!=null) {
			assertTrue(fromSource.contains("test"));
			assertTrue(fromSource.contains("123"));
			assertTrue(fromSource.contains(s));
			assertTrue(fromSource.contains(fromSource));
			assertTrue(fromSource.contains(""));
			assertFalse(fromSource.contains("x"));
			cleanup(fromSource);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.AbstractString#toCharArray()}.
	 */
	@Test
	@PostponedTest
	default void testToCharArray() {
		S empty = createEmptyString();
		if(empty!=null) {
			assertEquals(new char[0], empty.toCharArray());
			cleanup(empty);
		}

		S withLength = createString(10);
		if(withLength!=null) {
			assertEquals(10, withLength.toCharArray().length);
			cleanup(withLength);
		}

		String s = "test123";
		S fromSource = createString(s);
		if(fromSource!=null) {
			assertEquals(s.toCharArray(), fromSource.toCharArray());
			cleanup(fromSource);
		}

		S fromEmptySource = createString("");
		if(fromEmptySource!=null) {
			assertEquals(new char[0], fromEmptySource.toCharArray());
			cleanup(fromEmptySource);
		}
	}
}
