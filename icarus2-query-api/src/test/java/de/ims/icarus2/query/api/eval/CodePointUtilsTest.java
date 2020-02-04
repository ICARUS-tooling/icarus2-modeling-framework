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

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.ims.icarus2.query.api.eval.BinaryOperations.StringMode;
import de.ims.icarus2.util.strings.CodePointSequence;

/**
 * @author Markus Gärtner
 *
 */
class CodePointUtilsTest {

	private static final String test = "This is a test";

	/** 'This is a test' in Simplified Chinese */
	private static final String test_chinese = "这是一个测试";
//	private static final String test_chinese_uc = test_chinese.toUpperCase(Locale.SIMPLIFIED_CHINESE);
//	private static final String test_chinese_lc = test_chinese.toLowerCase(Locale.SIMPLIFIED_CHINESE);

	/** 'This is a test' in Georgian */
	private static final String test_georgian = "ეს ტესტია";
	private static final String test_georgian_uc = test_georgian.toUpperCase(Locale.ENGLISH);
	private static final String test_georgian_lc = test_georgian.toLowerCase(Locale.ENGLISH);

	/** 'This is a test' in Urdu */
	private static final String test_urdu = "یہ ایک امتحان ہے";
	private static final String test_urdu_uc = test_urdu.toUpperCase(Locale.ENGLISH);
	private static final String test_urdu_lc = test_urdu.toLowerCase(Locale.ENGLISH);

	/** 'This is a test' in Hebrew */
	private static final String test_hebrew = "یזה מבחן";
	private static final String test_hebrew_uc = test_hebrew.toUpperCase(Locale.ENGLISH);
	private static final String test_hebrew_lc = test_hebrew.toLowerCase(Locale.ENGLISH);

	// dummy strings using Old Turkic block starting from U+10C00
	private static final String test_mixed  = "𐰀𐰁𐰂𐰃𐰄𐰅𐰆𐰇𐰈𐰉";
	private static final String test_mixed_uc = test_mixed.toUpperCase(Locale.ENGLISH);
	private static final String test_mixed_lc = test_mixed.toLowerCase(Locale.ENGLISH);

	private static final String test_mixed2  = "𐰀𐰁𐰂𐰃𐰄𐰅𐰆𐰇𐰈𐰊";
	private static final String test_mixed2_uc = test_mixed2.toUpperCase(Locale.ENGLISH);
	private static final String test_mixed2_lc = test_mixed2.toLowerCase(Locale.ENGLISH);

	private static final String test_mixed3  = "𐰀𐰁𐰂𐰃𐰄𐰎𐰆𐰇𐰈𐰉";
	private static final String test_mixed3_uc = test_mixed3.toUpperCase(Locale.ENGLISH);
	private static final String test_mixed3_lc = test_mixed3.toLowerCase(Locale.ENGLISH);

	public static Stream<String> testValues() {
		return Stream.of(
				"x",
				"xxxx",
				"Äö",
				test,
				test_chinese,
				test_georgian,
				test_urdu,
				test_hebrew,
				test_mixed,
				test_mixed2,
				test_mixed3
		);
	}

	public static Stream<Arguments> mismatchedPairsForEquals() {
		return Stream.of(
				arguments("x", "y"),
				arguments("xxxxx", "xxx_xx"),
				arguments("Äö", "Öä"),
				arguments(test, test_chinese),
				arguments(test_chinese, test_georgian),
				arguments(test_georgian, test_urdu),
				arguments(test_urdu, test_hebrew),
				arguments(test_hebrew, test_mixed),
				arguments(test_mixed, test),
				arguments(test_mixed2, test_mixed),
				arguments(test_mixed3, test_mixed2)
		);
	}

	public static Stream<Arguments> matchedPairsForContains() {
		return Stream.of(
				arguments("x", "x"),
				arguments("xxxxx", "xx"),
				arguments("Äö", "ö"),
				arguments("This is a test", "is"),
				arguments("This is a test", " a "),
				arguments(test_mixed, "𐰆𐰇𐰈𐰉"),
				arguments(test_mixed, "𐰀𐰁𐰂𐰃"),
				arguments(test_mixed, "𐰃𐰄𐰅𐰆"),
				arguments(test_mixed2, test_mixed2),
				arguments(test_mixed3, test_mixed3)
		);
	}

	public static Stream<Arguments> mismatchedPairsForContains() {
		return Stream.of(
				arguments("x", "y"),
				arguments("xxxxx", "xy"),
				arguments("Äö", "Öä"),
				arguments("This is a test", "are"),
				arguments("This is a test", "\t"),
				arguments(test_mixed, "test"),
				arguments(test_mixed2, "𐰉"),
				arguments(test_mixed3, "𐰆𐰇𐰈𐰊"),
				arguments(test_mixed3, test_mixed2),
				arguments(test_mixed3, test_mixed3+test_mixed2) // ensure longer queries get skipped
		);
	}

	abstract class EqualityTests {

		private final StringMode mode;

		public EqualityTests(StringMode mode) { this.mode = requireNonNull(mode); }

		/**
		 * Test method for {@link de.ims.icarus2.query.api.eval.CodePointUtils#equalsChars(java.lang.CharSequence, java.lang.CharSequence, de.ims.icarus2.util.function.CharBiPredicate)}.
		 */
		@ParameterizedTest
		@MethodSource("de.ims.icarus2.query.api.eval.CodePointUtilsTest#testValues")
		void testEqualsChars_true(String value) {
			assertThat(CodePointUtils.equalsChars(value, value, mode.getCharComparator())).isTrue();
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.eval.CodePointUtils#equalsChars(java.lang.CharSequence, java.lang.CharSequence, de.ims.icarus2.util.function.CharBiPredicate)}.
		 */
		@ParameterizedTest
		@MethodSource("de.ims.icarus2.query.api.eval.CodePointUtilsTest#mismatchedPairsForEquals")
		void testEqualsChars_false(String s1, String s2) {
			assertThat(CodePointUtils.equalsChars(s1, s2, mode.getCharComparator())).isFalse();
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.eval.CodePointUtils#equalsCodePoints(de.ims.icarus2.util.strings.CodePointSequence, de.ims.icarus2.util.strings.CodePointSequence, de.ims.icarus2.util.function.IntBiPredicate)}.
		 */
		@ParameterizedTest
		@MethodSource("de.ims.icarus2.query.api.eval.CodePointUtilsTest#testValues")
		void testEqualsCodePoints_true(String value) {
			assertThat(CodePointUtils.equalsCodePoints(CodePointSequence.fixed(value),
					CodePointSequence.fixed(value), mode.getCodePointComparator()));
		}

	}

	@Nested
	class ForEquality {

		@Nested
		class DefaultMode extends EqualityTests {

			public DefaultMode() {
				super(StringMode.DEFAULT);
			}

		}

		@Nested
		class LowerCaseMode extends EqualityTests {

			public LowerCaseMode() {
				super(StringMode.LOWERCASE);
			}

			//TODO add special tests for different casings that transform to the same results
		}

		@Nested
		class IgnoreCaseMode extends EqualityTests {

			public IgnoreCaseMode() {
				super(StringMode.IGNORE_CASE);
			}

			//TODO add special tests for different casings that transform to the same results
		}

	}

	abstract class ContainsTests {

		private final StringMode mode;

		public ContainsTests(StringMode mode) { this.mode = requireNonNull(mode); }

		/**
		 * Test method for {@link de.ims.icarus2.query.api.eval.CodePointUtils#containsChars(java.lang.CharSequence, java.lang.CharSequence, de.ims.icarus2.util.function.CharBiPredicate)}.
		 */
		@ParameterizedTest
		@MethodSource("de.ims.icarus2.query.api.eval.CodePointUtilsTest#matchedPairsForContains")
		void testContainsChars_true(String target, String query) {
			assertThat(CodePointUtils.containsChars(target, query, mode.getCharComparator())).isTrue();
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.eval.CodePointUtils#containsChars(java.lang.CharSequence, java.lang.CharSequence, de.ims.icarus2.util.function.CharBiPredicate)}.
		 */
		@ParameterizedTest
		@MethodSource("de.ims.icarus2.query.api.eval.CodePointUtilsTest#mismatchedPairsForContains")
		void testContainsChars_false(String target, String query) {
			assertThat(CodePointUtils.containsChars(target, query, mode.getCharComparator())).isFalse();
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.eval.CodePointUtils#containsCodePoints(de.ims.icarus2.util.strings.CodePointSequence, de.ims.icarus2.util.strings.CodePointSequence, de.ims.icarus2.util.function.IntBiPredicate)}.
		 */
		@ParameterizedTest
		@MethodSource("de.ims.icarus2.query.api.eval.CodePointUtilsTest#matchedPairsForContains")
		void testContainsCodePoints_true(String target, String query) {
			assertThat(CodePointUtils.containsCodePoints(CodePointSequence.fixed(target),
					CodePointSequence.fixed(query), mode.getCodePointComparator())).isTrue();
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.eval.CodePointUtils#containsCodePoints(de.ims.icarus2.util.strings.CodePointSequence, de.ims.icarus2.util.strings.CodePointSequence, de.ims.icarus2.util.function.IntBiPredicate)}.
		 */
		@ParameterizedTest
		@MethodSource("de.ims.icarus2.query.api.eval.CodePointUtilsTest#mismatchedPairsForContains")
		void testContainsCodePoints_false(String target, String query) {
			assertThat(CodePointUtils.containsCodePoints(CodePointSequence.fixed(target),
					CodePointSequence.fixed(query), mode.getCodePointComparator())).isFalse();
		}

	}

	@Nested
	class ForContains {

		@Nested
		class DefaultMode extends ContainsTests {

			public DefaultMode() {
				super(StringMode.DEFAULT);
			}

		}

		@Nested
		class LowerCaseMode extends ContainsTests {

			public LowerCaseMode() {
				super(StringMode.LOWERCASE);
			}

			//TODO add special tests for different casings that transform to the same results
		}

		@Nested
		class IgnoreCaseMode extends ContainsTests {

			public IgnoreCaseMode() {
				super(StringMode.IGNORE_CASE);
			}

			//TODO add special tests for different casings that transform to the same results
		}
	}
}
