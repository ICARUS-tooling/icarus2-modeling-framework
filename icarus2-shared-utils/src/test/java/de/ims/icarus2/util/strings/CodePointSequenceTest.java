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
/**
 *
 */
package de.ims.icarus2.util.strings;

import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives.strictToChar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.PostponedTest;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.test.util.Unicodes;

/**
 * @author Markus Gärtner
 *
 */
public interface CodePointSequenceTest<C extends CodePointSequence> extends ApiGuardedTest<C> {

	@Provider
	C createEmptySequence();

	@Provider
	C createSequence(int length);

	@Provider
	default C createSequence(int[] codepoints) {
		return createSequence(new String(codepoints, 0, codepoints.length));
	}

	@Provider
	C createSequence(String source);

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default C createTestInstance(TestSettings settings) {
		return settings.process(createEmptySequence());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CodePointSequence#codePointCount()}.
	 */
	@ParameterizedTest
	@ValueSource(ints = {1, 10, 10_000})
	default void testCodePointCount(int size) {
		assertThat(createSequence(size).codePointCount()).isEqualTo(size);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CodePointSequence#codePointAt(int)}.
	 */
	@Test
	@RandomizedTest
	default void testCodePointAt(RandomGenerator rng) {
		int[] codepoints = Unicodes.randomSupplementaryCodepoints(rng, 13).toArray();
		C instance = createSequence(codepoints);

		assertThat(instance.codePointCount()).isEqualTo(codepoints.length);
		for (int i = 0; i < codepoints.length; i++) {
			assertThat(instance.codePointAt(i))
				.as("Mismatch at index %d", _int(i))
				.isEqualTo(codepoints[i]);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CodePointSequence#containsSupplementaryCodePoints()}.
	 */
	@Test
	@RandomizedTest
	default void testContainsSupplementaryCodePoints_true(RandomGenerator rng) {
		int[] codepoints = Unicodes.randomSupplementaryCodepoints(rng, 13).toArray();
		assertThat(createSequence(codepoints).containsSupplementaryCodePoints()).isTrue();
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CodePointSequence#containsSupplementaryCodePoints()}.
	 */
	@Test
	@RandomizedTest
	default void testContainsSupplementaryCodePoints_false(RandomGenerator rng) {
		int[] codepoints = Unicodes.randomBMPCodepoints(rng, 13).toArray();
		assertThat(createSequence(codepoints).containsSupplementaryCodePoints()).isFalse();
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CodePointSequence#fixed(java.lang.String)}.
	 */
	@Test
	@RandomizedTest
	default void testFixed(RandomGenerator rng) {
		int[] codepoints = Unicodes.randomSupplementaryCodepoints(rng, 13).toArray();
		String source = new String(codepoints, 0, codepoints.length);

		CodePointSequence cps = CodePointSequence.fixed(source);
		assertThat(cps.toString()).isEqualTo(source);
	}

	/**
	 * Test method for {@link java.lang.CharSequence#length()}.
	 */
	@Test
	@RandomizedTest
	default void testLength_bmp(RandomGenerator rng) {
		int length = 13;
		int[] codepoints = Unicodes.randomBMPCodepoints(rng, length).toArray();
		assertThat(createSequence(codepoints).length()).isEqualTo(length);
	}

	/**
	 * Test method for {@link java.lang.CharSequence#length()}.
	 */
	@Test
	@RandomizedTest
	default void testLength_unicode(RandomGenerator rng) {
		int length = 13;
		int[] codepoints = Unicodes.randomSupplementaryCodepoints(rng, length).toArray();
		assertThat(createSequence(codepoints).length()).isEqualTo(length*2);
	}

	/**
	 * Test method for {@link java.lang.CharSequence#charAt(int)}.
	 */
	@Test
	@RandomizedTest
	default void testCharAt(RandomGenerator rng) {
		int[] codepoints = Unicodes.randomBMPCodepoints(rng, 13).toArray();
		C instance = createSequence(codepoints);
		for (int i = 0; i < codepoints.length; i++) {
			assertThat(instance.charAt(i)).isEqualTo(strictToChar(codepoints[i]));
		}
	}

	/**
	 * Test method for {@link java.lang.CharSequence#charAt(int)}.
	 */
	@Test
	@RandomizedTest
	default void testCharAt_unicode(RandomGenerator rng) {
		int[] codepoints = Unicodes.randomSupplementaryCodepoints(rng, 13).toArray();
		C instance = createSequence(codepoints);
		char[] chars = new char[codepoints.length*2];
		for (int i = 0; i < codepoints.length; i++) {
			int cp = codepoints[i];
			chars[i*2] = Character.highSurrogate(cp);
			chars[(i*2)+1] = Character.lowSurrogate(cp);
		}
		assertThat(instance.length()).isEqualTo(chars.length);
		for (int i = 0; i < chars.length; i++) {
			assertThat(instance.charAt(i))
				.as("char mismatch at index %d", _int(i))
				.isEqualTo(chars[i]);
		}
	}

	/**
	 * Test method for {@link java.lang.CharSequence#subSequence(int, int)}.
	 */
	@Test
	@PostponedTest
	default void testSubSequence() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.CharSequence#toString()}.
	 */
	@Test
	@RandomizedTest
	default void testToString(RandomGenerator rng) {
		int[] codepoints = Unicodes.randomSupplementaryCodepoints(rng, 13).toArray();
		String source = new String(codepoints, 0, codepoints.length);
		C instance = createSequence(source);
		assertThat(instance.toString()).isEqualTo(source);
	}

	/**
	 * Test method for {@link java.lang.CharSequence#chars()}.
	 */
	@Test
	@RandomizedTest
	default void testChars(RandomGenerator rng) {
		int[] codepoints = Unicodes.randomBMPCodepoints(rng, 13).toArray();
		C instance = createSequence(codepoints);
		assertThat(instance.chars().toArray()).containsExactly(codepoints);
	}

	/**
	 * Test method for {@link java.lang.CharSequence#chars()}.
	 */
	@Test
	@RandomizedTest
	default void testChars_unicode(RandomGenerator rng) {
		int[] codepoints = Unicodes.randomSupplementaryCodepoints(rng, 13).toArray();
		C instance = createSequence(codepoints);
		int[] chars = new int[codepoints.length*2];
		for (int i = 0; i < codepoints.length; i++) {
			int cp = codepoints[i];
			chars[i*2] = Character.highSurrogate(cp);
			chars[(i*2)+1] = Character.lowSurrogate(cp);
		}
		assertThat(instance.chars().toArray()).containsExactly(chars);
	}

	/**
	 * Test method for {@link java.lang.CharSequence#codePoints()}.
	 */
	@Test
	@RandomizedTest
	default void testCodePoints(RandomGenerator rng) {
		int[] codepoints = Unicodes.randomSupplementaryCodepoints(rng, 13).toArray();
		C instance = createSequence(codepoints);
		assertThat(instance.codePoints().toArray()).containsExactly(codepoints);
	}

}
