/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.test.TestUtils.assertIOOB;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.ObjectTest;

/**
 * Test case for {@link CharSequence} implementations
 *
 * @author Markus Gärtner
 *
 */
public interface CharSequenceTest<S extends CharSequence> extends ObjectTest {


	S createEmptySequence();

	S createSequence(int length);

	S createSequence(String source);

	/**
	 * @see de.ims.icarus2.test.ObjectTest#createDifferent()
	 */
	@Override
	default Object[] createDifferent() {
		return new Object[]{
				createEmptySequence(),
				createSequence(5),
				createSequence(10),
				createSequence("test123"),
				createSequence("123test")
		};
	}

	/**
	 * @see de.ims.icarus2.test.ObjectTest#createEqual()
	 */
	@Override
	default Object[] createEqual() {
		return new Object[]{
				createSequence("test123"),
				createSequence("test123")
		};
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.AbstractString#subSequence(int, int)}.
	 */
	@Test
	default void testSubSequence() {
		S empty = createEmptySequence();
		if(empty!=null) {
			assertIOOB(() -> empty.subSequence(0, 1));
			cleanup(empty);
		}

		S withLength = createSequence(10);
		if(withLength!=null) {
			CharSequence ss = withLength.subSequence(0, 10);
			assertNotNull(ss);
			assertEquals(withLength.length(), ss.length());
			assertEquals(withLength.toString(), ss.toString());
			assertEquals(withLength.hashCode(), ss.hashCode());

			assertIOOB(() -> withLength.subSequence(11, 12));

			cleanup(withLength);
		}

		String s = "test123";
		S fromSource = createSequence(s);
		if(fromSource!=null) {
			CharSequence ss = fromSource.subSequence(0, 4);
			assertNotNull(ss);
			assertEquals(4, ss.length());
			assertEquals("test", ss.toString());
			assertEquals('t', ss.charAt(0));
			assertEquals('e', ss.charAt(1));
			assertEquals('s', ss.charAt(2));
			assertEquals('t', ss.charAt(3));

			assertIOOB(() -> fromSource.subSequence(-1, 0));

			cleanup(fromSource);
		}
	}

	/**
	 * Test method for {@link CharSequence#length()}.
	 */
	@Test
	default void testLength() {
		S empty = createEmptySequence();
		if(empty!=null) {
			assertEquals(0, empty.length());
			cleanup(empty);
		}

		S withLength = createSequence(10);
		if(withLength!=null) {
			assertEquals(10, withLength.length());
			cleanup(withLength);
		}

		String s = "test123";
		S fromSource = createSequence(s);
		if(fromSource!=null) {
			assertEquals(s.length(), fromSource.length());
			cleanup(fromSource);
		}
	}

	/**
	 * Test method for {@link CharSequence#charAt(int)}.
	 */
	@Test
	default void testCharAt() {
		S empty = createEmptySequence();
		if(empty!=null) {
			assertIOOB(() -> empty.charAt(0));
			assertIOOB(() -> empty.charAt(-1));
			assertIOOB(() -> empty.charAt(Integer.MAX_VALUE));

			cleanup(empty);
		}

		String s = "test123";
		S fromSource = createSequence(s);
		if(fromSource!=null) {
			assertEquals('t', fromSource.charAt(0));
			assertEquals('e', fromSource.charAt(1));
			assertEquals('s', fromSource.charAt(2));
			assertEquals('t', fromSource.charAt(3));
			assertEquals('1', fromSource.charAt(4));
			assertEquals('2', fromSource.charAt(5));
			assertEquals('3', fromSource.charAt(6));

			assertIOOB(() -> fromSource.charAt(-1));
			assertIOOB(() -> fromSource.charAt(Integer.MAX_VALUE));

			cleanup(fromSource);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.AbstractString#toString()}.
	 */
	@Test
	default void testToString() {
		S empty = createEmptySequence();
		if(empty!=null) {
			assertEquals("", empty.toString());
			cleanup(empty);
		}

		S withLength = createSequence(10);
		if(withLength!=null) {
			assertEquals(10, withLength.toString().length());
			cleanup(withLength);
		}

		S fromSource = createSequence("test123");
		if(fromSource!=null) {
			assertEquals("test123", fromSource.toString());
			cleanup(fromSource);
		}
	}
}
