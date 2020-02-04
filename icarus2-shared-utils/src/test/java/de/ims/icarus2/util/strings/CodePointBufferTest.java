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

import static de.ims.icarus2.SharedTestUtils.assertIcarusException;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.test.util.Unicodes;

/**
 * @author Markus Gärtner
 *
 */
@RandomizedTest
class CodePointBufferTest implements CodePointSequenceTest<CodePointBuffer> {

	public RandomGenerator rng;

	private static String valueOf(int[] codepoints) {
		return new String(codepoints, 0, codepoints.length);
	}

	@Override
	public Class<?> getTestTargetClass() {
		return CodePointBuffer.class;
	}

	@Override
	public CodePointBuffer createEmptySequence() {
		return new CodePointBuffer();
	}

	@Override
	public CodePointBuffer createSequence(int length) {
		int[] tmp = Unicodes.randomSupplementaryCodepoints(rng, length).toArray();

		return new CodePointBuffer(valueOf(tmp));
	}

	public CodePointBuffer createSequence(char[] chars) {
		return new CodePointBuffer(new String(chars));
	}

	@Override
	public CodePointBuffer createSequence(String source) {
		return new CodePointBuffer(source);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CodePointBuffer#set(java.lang.CharSequence)}.
	 */
	@Test
	void testSetCharSequence_null() {
		CodePointBuffer instance = createSequence("test");
		instance.set((CharSequence)null);
		assertThat(instance.isEmpty()).isTrue();
		assertThat(instance.length()).isEqualTo(0);
		assertThat(instance.codePointCount()).isEqualTo(0);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CodePointBuffer#set(java.lang.CharSequence)}.
	 */
	@Test
	void testSetCharSequence_empty() {
		CodePointBuffer instance = createSequence("test");
		instance.set("");
		assertThat(instance.isEmpty()).isFalse();
		assertThat(instance.length()).isEqualTo(0);
		assertThat(instance.codePointCount()).isEqualTo(0);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CodePointBuffer#set(java.lang.CharSequence)}.
	 */
	@Test
	void testSetCharSequence(RandomGenerator rng) {
		int[] codepoints = Unicodes.randomSupplementaryCodepoints(rng, 13).toArray();
		String source = valueOf(codepoints);
		CodePointBuffer instance = createEmptySequence();
		instance.set(source);
		assertThat(instance.isEmpty()).isFalse();
		assertThat(instance.length()).isEqualTo(source.length());
		assertThat(instance.codePointCount()).isEqualTo(source.codePointCount(0, source.length()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CodePointBuffer#set(java.lang.Object)}.
	 */
	@Test
	void testSetObject() {
		String test = "test";
		CodePointBuffer instance = createSequence(test);
		instance.set((Object)null);
		assertThat(instance.get()).isNull();
		instance.set((Object)test);
		assertThat(instance.get()).isSameAs(test);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CodePointBuffer#get()}.
	 */
	@Test
	void testGet() {
		CharSequence source = "";
		CodePointBuffer instance = new CodePointBuffer();

		instance.set(source);
		assertThat(instance.get()).isSameAs(source);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CodePointBuffer#clear()}.
	 */
	@Test
	void testClear() {
		CodePointBuffer instance = createSequence("test");
		instance.clear();
		assertThat(instance.isEmpty()).isTrue();
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CodePointBuffer#isPrimitive()}.
	 */
	@Test
	void testIsPrimitive() {
		assertThat(createEmptySequence().isPrimitive()).isFalse();
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CodePointBuffer#isEmpty()}.
	 */
	@Test
	void testIsEmpty_initial() {
		assertThat(new CodePointBuffer().isEmpty()).isTrue();
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CodePointBuffer#isEmpty()}.
	 */
	@Test
	void testIsEmpty() {
		CodePointBuffer instance = createSequence("test");
		assertThat(instance.isEmpty()).isFalse();
		instance.set(null);
		assertThat(instance.isEmpty()).isTrue();
	}

	@Nested
	class SpecialCases {

		@RandomizedTest
		@Test
		void testIncrementalGrowth(RandomGenerator rng) {
			CodePointBuffer instance = new CodePointBuffer();
			instance.set(valueOf(Unicodes.randomBMPCodepoints(rng, 10).toArray()));
			int size1 = instance.length();
			instance.set(valueOf(Unicodes.randomSupplementaryCodepoints(rng, 20).toArray()));
			int size2 = instance.length();
			assertThat(size2).isGreaterThan(size1);
		}

		@RandomizedTest
		@Test
		void testTerminalHighSurrogate(RandomGenerator rng) {
			char badCP = Unicodes.randomHighSurrogate(rng);
			char[] data = {
					Unicodes.randomHighSurrogate(rng),
					Unicodes.randomLowSurrogate(rng),
					Unicodes.randomBMPCodepoint(rng),
					// This one will cause error
					badCP
			};

			IcarusRuntimeException ex = assertIcarusException(GlobalErrorCode.INVALID_INPUT,
					() -> createSequence(data));
			assertThat(ex).hasMessageContaining("Incomplete high surrogate")
					.hasMessageContaining(Integer.toHexString(badCP));
		}

		@RandomizedTest
		@Test
		void testDanglingHighSurrogate(RandomGenerator rng) {
			char badCP = Unicodes.randomBMPCodepoint(rng);
			char[] data = {
					Unicodes.randomHighSurrogate(rng),
					Unicodes.randomLowSurrogate(rng),
					Unicodes.randomBMPCodepoint(rng),
					Unicodes.randomHighSurrogate(rng),
					// This one will cause error
					badCP
			};

			IcarusRuntimeException ex = assertIcarusException(GlobalErrorCode.INVALID_INPUT,
					() -> createSequence(data));
			assertThat(ex).hasMessageContaining("Incomplete surrogate pair")
					.hasMessageContaining(Integer.toHexString(badCP));
		}
	}
}
