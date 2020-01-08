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

import static de.ims.icarus2.test.TestUtils.assertNPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.text.DecimalFormat;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.test.TestUtils;

/**
 * @author Markus Gärtner
 *
 */
class ToStringBuilderTest {

	private static void assertToString(ToStringBuilder builder, String expected) {
		assertEquals(expected, builder.build());
	}

	private static void assertToString(ToStringBuilder builder, String prefix,
			String content, String suffix) {
		boolean hasContent = content!=null && !content.isEmpty();
		if(!hasContent) {
			prefix = prefix.trim();
			suffix = suffix.trim();
			content = "";
		}

		String expected = prefix+content+suffix;
		assertToString(builder, expected);
	}

	@Nested
	class Create implements BuilderTest {

		/**
		 * @see de.ims.icarus2.util.strings.ToStringBuilderTest.BuilderTest#builder()
		 */
		@Override
		public ToStringBuilder builder() {
			return ToStringBuilder.create();
		}

	}

	@Nested
	class CreateWithObject implements BuilderTest {

		/**
		 * @see de.ims.icarus2.util.strings.ToStringBuilderTest.BuilderTest#prefix()
		 */
		@Override
		public String prefix() {
			return "[Target ";
		}

		/**
		 * @see de.ims.icarus2.util.strings.ToStringBuilderTest.BuilderTest#suffix()
		 */
		@Override
		public String suffix() {
			return "]";
		}

		/**
		 * @see de.ims.icarus2.util.strings.ToStringBuilderTest.BuilderTest#builder()
		 */
		@Override
		public ToStringBuilder builder() {
			return ToStringBuilder.create(new Target());
		}

	}

	interface BuilderTest {

		default String prefix() {
			return "";
		}

		default String suffix() {
			return "";
		}

		ToStringBuilder builder();

		/**
		 * Test method for {@link de.ims.icarus2.util.strings.ToStringBuilder#add(java.lang.String)}.
		 */
		@ParameterizedTest
		@ValueSource(strings = {"test", TestUtils.EMOJI})
		default void testAddString(String value) {
			assertToString(builder().add(value), prefix(), value, suffix());
		}

		@Test
		default void testAddStringNPE() {
			assertNPE(() -> builder().add(null));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.strings.ToStringBuilder#add(char)}.
		 */
		@ParameterizedTest
		@ValueSource(chars = {'x'})
		default void testAddChar(char value) {
			assertToString(builder().add(value), prefix(), String.valueOf(value), suffix());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.strings.ToStringBuilder#add(java.lang.String, java.lang.String)}.
		 */
		@ParameterizedTest
		@ValueSource(strings = {"test", TestUtils.EMOJI})
		default void testAddStringString(String value) {
			assertToString(builder().add("value", value), prefix(), "value="+value, suffix());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.strings.ToStringBuilder#add(java.lang.String, java.lang.Object)}.
		 */
		@SuppressWarnings("boxing")
		@TestFactory
		default Stream<DynamicTest> testAddStringObject() {
			return Stream.of("test", TestUtils.EMOJI, 2, 2.5, Long.MAX_VALUE, 3.4F, 'X',
						new DynToString("test"), new DynToString(TestUtils.EMOJI))
					.map(value -> dynamicTest(TestUtils.displayString(value),
							() -> assertToString(builder().add("value", value),
									prefix(), "value="+value, suffix())));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.strings.ToStringBuilder#add(java.lang.String, byte)}.
		 */
		@ParameterizedTest
		@ValueSource(bytes = {Byte.MIN_VALUE, Byte.MAX_VALUE, 0, 1})
		default void testAddStringByte(byte value) {
			assertToString(builder().add("value", value), prefix(), "value="+value, suffix());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.strings.ToStringBuilder#add(java.lang.String, short)}.
		 */
		@ParameterizedTest
		@ValueSource(shorts = {Short.MIN_VALUE, Short.MAX_VALUE, 0, 1})
		default void testAddStringShort(short value) {
			assertToString(builder().add("value", value), prefix(), "value="+value, suffix());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.strings.ToStringBuilder#addFormatted(java.lang.String, short)}.
		 */
		@ParameterizedTest
		@ValueSource(shorts = {Short.MIN_VALUE, Short.MAX_VALUE, 0, 1})
		default void testAddFormattedStringShort(short value) {
			DecimalFormat format = new DecimalFormat("#,###");
			assertToString(builder().addFormatted("value", value), prefix(),
					"value="+format.format(value), suffix());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.strings.ToStringBuilder#add(java.lang.String, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 1})
		default void testAddStringInt(int value) {
			assertToString(builder().add("value", value), prefix(), "value="+value, suffix());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.strings.ToStringBuilder#addFormatted(java.lang.String, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 1})
		default void testAddFormattedStringInt(int value) {
			DecimalFormat format = new DecimalFormat("#,###");
			assertToString(builder().addFormatted("value", value), prefix(),
					"value="+format.format(value), suffix());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.strings.ToStringBuilder#add(java.lang.String, long)}.
		 */
		@ParameterizedTest
		@ValueSource(longs = {Long.MIN_VALUE, Long.MAX_VALUE, 0, 1})
		default void testAddStringLong(long value) {
			assertToString(builder().add("value", value), prefix(), "value="+value, suffix());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.strings.ToStringBuilder#addFormatted(java.lang.String, long)}.
		 */
		@ParameterizedTest
		@ValueSource(longs = {Long.MIN_VALUE, Long.MAX_VALUE, 0, 1})
		default void testAddFormattedStringLong(long value) {
			DecimalFormat format = new DecimalFormat("#,###");
			assertToString(builder().addFormatted("value", value), prefix(),
					"value="+format.format(value), suffix());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.strings.ToStringBuilder#add(java.lang.String, float)}.
		 */
		@ParameterizedTest
		@ValueSource(floats = {-Float.MAX_VALUE, Float.MAX_VALUE, 0, 1, 2.5F})
		default void testAddStringFloat(float value) {
			assertToString(builder().add("value", value), prefix(), "value="+value, suffix());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.strings.ToStringBuilder#addFormatted(java.lang.String, float)}.
		 */
		@ParameterizedTest
		@ValueSource(floats = {-Float.MAX_VALUE, Float.MAX_VALUE, 0, 1, 2.5F})
		default void testAddFormattedStringFloat(float value) {
			DecimalFormat format = new DecimalFormat("#,##0.00");
			assertToString(builder().addFormatted("value", value), prefix(),
					"value="+format.format(value), suffix());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.strings.ToStringBuilder#add(java.lang.String, double)}.
		 */
		@ParameterizedTest
		@ValueSource(doubles = {-Double.MAX_VALUE, Double.MAX_VALUE, 0, 1, -3.5D})
		default void testAddStringDouble(double value) {
			assertToString(builder().add("value", value), prefix(), "value="+value, suffix());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.strings.ToStringBuilder#addFormatted(java.lang.String, double)}.
		 */
		@ParameterizedTest
		@ValueSource(doubles = {-Double.MAX_VALUE, Double.MAX_VALUE, 0, 1, -3.5D})
		default void testAddFormattedStringDouble(double value) {
			DecimalFormat format = new DecimalFormat("#,##0.00");
			assertToString(builder().addFormatted("value", value), prefix(),
					"value="+format.format(value), suffix());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.strings.ToStringBuilder#add(java.lang.String, boolean)}.
		 */
		@TestFactory
		default Stream<DynamicTest> testAddStringBoolean() {
			return Stream.of(Boolean.TRUE, Boolean.FALSE)
					.map(b -> dynamicTest(String.valueOf(b),
							() -> assertToString(builder().add("value", b),
									prefix(), "value="+b, suffix())));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.strings.ToStringBuilder#add(java.lang.String, char)}.
		 */
		@ParameterizedTest
		@ValueSource(chars = {Character.MIN_VALUE, Character.MAX_VALUE, '\0', 'x', 'Ä'})
		default void testAddStringChar(char value) {
			assertToString(builder().add("value", value), prefix(), "value="+value, suffix());
		}
	}

	static class Target {
		// dummy
	}

	static class DynToString {
		private final String value;

		public DynToString(String value) {
			super();
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}
	}
}
