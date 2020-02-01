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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author Markus Gärtner
 *
 */
class UnicodeUtilsTest {

	private static final String test = "This is a test";

	/** 'This is a test' in simplified Chinese */
	private static final String test_chinese = "这是一个测试";
	/** 'This is a test' in Georgian */
	private static final String test_georgian = "ეს ტესტია";
	/** 'This is a test' in Urdu */
	private static final String test_urdu = "یہ ایک امتحان ہے";
	/** 'This is a test' in Hebrew */
	private static final String test_hebrew = "یזה מבחן";

	// dummy strings using Old Turkic block starting from U+10C00
	private static final String test_mixed  = "𐰀𐰁𐰂𐰃𐰄𐰅𐰆𐰇𐰈𐰉";
	private static final String test_mixed2  = "𐰀𐰁𐰂𐰃𐰄𐰅𐰆𐰇𐰈𐰊";
	private static final String test_mixed3  = "𐰀𐰁𐰂𐰃𐰄𐰎𐰆𐰇𐰈𐰉";

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

	public static Stream<Arguments> mismatchedPairs() {
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

	public static Stream<Arguments> matchedPairs() {
		return testValues().map(s -> arguments(s, s));
	}

	//TODO add tests for incomplete surrogate pairs

	@ParameterizedTest
	@MethodSource("matchedPairs")
	void testEqual_true(String s1, String s2) {
		assertThat(StringEquals.equal(s1, s2)).isTrue();
	}

	@ParameterizedTest
	@MethodSource("mismatchedPairs")
	void testEqual_false(String s1, String s2) {
		assertThat(StringEquals.equal(s1, s2)).isFalse();
	}

	@ParameterizedTest
	@MethodSource("mismatchedPairs")
	void testNotEqual_true(String s1, String s2) {
		assertThat(StringEquals.notEqual(s1, s2)).isTrue();
	}

	@ParameterizedTest
	@MethodSource("matchedPairs")
	void testNotEqual_false(String s1, String s2) {
		assertThat(StringEquals.notEqual(s1, s2)).isFalse();
	}

	@ParameterizedTest
	@MethodSource("matchedPairs")
	void testEqualLowerCase_true(String s1, String s2) {
		assertThat(StringEquals.equalLowerCase(s1, s2)).isTrue();
	}

	@ParameterizedTest
	@MethodSource("mismatchedPairs")
	void testEqualLowerCase_false(String s1, String s2) {
		assertThat(StringEquals.equalLowerCase(s1, s2)).isFalse();
	}

	@ParameterizedTest
	@MethodSource("testValues")
	void testEqualLowerCase_vs_String_LC(String s) {
		assertThat(StringEquals.equalLowerCase(s, s.toLowerCase())).isTrue();
	}

	@ParameterizedTest
	@MethodSource("testValues")
	void testEqualLowerCase_vs_String_UC(String s) {
		assertThat(StringEquals.equalLowerCase(s, s.toUpperCase())).isTrue();
	}

	@ParameterizedTest
	@MethodSource("mismatchedPairs")
	void testNotEqualLowerCase_true(String s1, String s2) {
		assertThat(StringEquals.notEqualLowerCase(s1, s2)).isTrue();
	}

	@ParameterizedTest
	@MethodSource("matchedPairs")
	void testNotEqualLowerCase_false(String s1, String s2) {
		assertThat(StringEquals.notEqualLowerCase(s1, s2)).isFalse();
	}

	@ParameterizedTest
	@MethodSource("matchedPairs")
	void testEqualIgnoreCase_true(String s1, String s2) {
		assertThat(StringEquals.equalIgnoreCase(s1, s2)).isTrue();
	}

	@ParameterizedTest
	@MethodSource("mismatchedPairs")
	void testEqualIgnoreCase_false(String s1, String s2) {
		assertThat(StringEquals.equalIgnoreCase(s1, s2)).isFalse();
	}

	@ParameterizedTest
	@MethodSource("mismatchedPairs")
	void testNotEqualIgnoreCase_true(String s1, String s2) {
		assertThat(StringEquals.notEqualIgnoreCase(s1, s2)).isTrue();
	}

	@ParameterizedTest
	@MethodSource("matchedPairs")
	void testNotEqualIgnoreCase_false(String s1, String s2) {
		assertThat(StringEquals.notEqualIgnoreCase(s1, s2)).isFalse();
	}

}
