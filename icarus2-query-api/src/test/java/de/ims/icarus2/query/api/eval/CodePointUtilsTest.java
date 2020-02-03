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

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

/**
 * @author Markus Gärtner
 *
 */
class CodePointUtilsTest {

	private static final String test = "This is a test";

	/** 'This is a test' in Simplified Chinese */
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

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.CodePointUtils#equalsChars(java.lang.CharSequence, java.lang.CharSequence, de.ims.icarus2.util.function.CharBiPredicate)}.
	 */
	@Test
	void testEqualsChars() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.CodePointUtils#equalsCodePoints(de.ims.icarus2.util.strings.CodePointSequence, de.ims.icarus2.util.strings.CodePointSequence, de.ims.icarus2.util.function.IntBiPredicate)}.
	 */
	@Test
	void testEqualsCodePoints() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.CodePointUtils#containsChars(java.lang.CharSequence, java.lang.CharSequence, de.ims.icarus2.util.function.CharBiPredicate)}.
	 */
	@Test
	void testContainsChars() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.CodePointUtils#containsCodePoints(de.ims.icarus2.util.strings.CodePointSequence, de.ims.icarus2.util.strings.CodePointSequence, de.ims.icarus2.util.function.IntBiPredicate)}.
	 */
	@Test
	void testContainsCodePoints() {
		fail("Not yet implemented"); // TODO
	}

}
