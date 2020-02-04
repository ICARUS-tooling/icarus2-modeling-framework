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

import static de.ims.icarus2.SharedTestUtils.assertIcarusException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.eval.BinaryOperations.AlgebraicOp;
import de.ims.icarus2.query.api.eval.BinaryOperations.StringMode;
import de.ims.icarus2.query.api.eval.BinaryOperations.StringOp;
import de.ims.icarus2.query.api.eval.Expression.BooleanExpression;
import de.ims.icarus2.query.api.eval.Expression.NumericalExpression;

/**
 * @author Markus Gärtner
 *
 */
class BinaryOperationsTest {

	/**
	 * Test class for {@link de.ims.icarus2.query.api.eval.BinaryOperations#numericalOp(de.ims.icarus2.query.api.eval.BinaryOperations.AlgebraicOp, de.ims.icarus2.query.api.eval.Expression.NumericalExpression, de.ims.icarus2.query.api.eval.Expression.NumericalExpression)}.
	 */
	@Nested
	class ForNumericalOps {

		@Nested
		class ForInteger {

			private NumericalExpression create(AlgebraicOp op, long left, long right) {
				return BinaryOperations.numericalOp(op, Literals.of(left), Literals.of(right));
			}

			private void assertIntOp(AlgebraicOp op, long left, long right, long expected) {
				NumericalExpression expression = create(op, left, right);
				assertThat(expression.isFPE()).isFalse();
				assertThat(expression.computeAsLong()).isEqualTo(expected);

				NumericalExpression duplicate = (NumericalExpression) expression.duplicate(mock(EvaluationContext.class));
				assertThat(duplicate.computeAsLong()).isEqualTo(expected);

				NumericalExpression optimized = (NumericalExpression) expression.optimize(mock(EvaluationContext.class));
				assertThat(optimized.computeAsLong()).isEqualTo(expected);
			}

			@ParameterizedTest
			@CsvSource({
				"1, 2"
			})
			void testAdd(long left, long right) {
				assertIntOp(AlgebraicOp.ADD, left, right, left+right);
			}
		}
	}

	/**
	 * Test class for {@link de.ims.icarus2.query.api.eval.BinaryOperations#numericalPred(de.ims.icarus2.query.api.eval.BinaryOperations.NumericalComparator, de.ims.icarus2.query.api.eval.Expression.NumericalExpression, de.ims.icarus2.query.api.eval.Expression.NumericalExpression)}.
	 */
	@Nested
	class ForNumericalPreds {

	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.BinaryOperations#comparablePred(de.ims.icarus2.query.api.eval.BinaryOperations.ComparableComparator, de.ims.icarus2.query.api.eval.Expression, de.ims.icarus2.query.api.eval.Expression)}.
	 */
	@Nested
	class ForComparablePreds {

	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.BinaryOperations#equalityPred(de.ims.icarus2.query.api.eval.BinaryOperations.EqualityPred, de.ims.icarus2.query.api.eval.Expression, de.ims.icarus2.query.api.eval.Expression)}.
	 */
	@Nested
	class ForEqualityPreds {

	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.BinaryOperations#unicodeOp(de.ims.icarus2.query.api.eval.BinaryOperations.StringOp, de.ims.icarus2.query.api.eval.BinaryOperations.StringMode, de.ims.icarus2.query.api.eval.Expression.TextExpression, de.ims.icarus2.query.api.eval.Expression.TextExpression)}.
	 */
	@Nested
	class ForUnicodeOps {

		private BooleanExpression create(StringOp op, StringMode mode, String left, String right) {
			return BinaryOperations.unicodeOp(op, mode, Literals.of(left), Literals.of(right));
		}

		private void assertUnicodeOp(StringOp op, StringMode mode, String left, String right, boolean equals) {
			BooleanExpression expression = create(op, mode, left, right);
			assertThat(expression.computeAsBoolean()).isEqualTo(equals);

			BooleanExpression duplicate = (BooleanExpression) expression.duplicate(mock(EvaluationContext.class));
			assertThat(duplicate.computeAsBoolean()).isEqualTo(equals);

			BooleanExpression optimized = (BooleanExpression) expression.optimize(mock(EvaluationContext.class));
			assertThat(optimized.computeAsBoolean()).isEqualTo(equals);
		}

		@Nested
		class ForEquals {
			private final StringOp op = StringOp.EQUALS;

			@ParameterizedTest
			@MethodSource("de.ims.icarus2.query.api.eval.CodePointUtilsTest#testValues")
			void testDefault_true(String value) {
				assertUnicodeOp(op, StringMode.DEFAULT, value, value, true);
			}

			@ParameterizedTest
			@MethodSource("de.ims.icarus2.query.api.eval.CodePointUtilsTest#mismatchedPairsForEquals")
			void testDefault_false(String left, String right) {
				assertUnicodeOp(op, StringMode.DEFAULT, left, right, false);
			}

			@ParameterizedTest
			@CsvSource({
				"x, x, true",
				"X, x, true",
				"x, X, true",
				"X, X, true",
				"test, test, true",
				"test, Test, true",
				"the first one, is longer, false",
				"this time, the second one is far longer, false",
			})
			void testLowerCase(String left, String right, boolean equals) {
				assertUnicodeOp(op, StringMode.LOWERCASE, left, right, equals);
			}

			@ParameterizedTest
			@CsvSource({
				"x, x, true",
				"X, x, true",
				"x, X, true",
				"X, X, true",
				"test, test, true",
				"test, Test, true",
				"TEST, tesT, true",
				"the first one, is longer, false",
				"this time, the second one is far longer, false",
			})
			//TODO this is effectively the same as lower case
			void testIgnoreCase(String left, String right, boolean equals) {
				assertUnicodeOp(op, StringMode.IGNORE_CASE, left, right, equals);
			}
		}

		@Nested
		class ForContains {
			private final StringOp op = StringOp.CONTAINS;

			@ParameterizedTest
			// [target, query]
			@CsvSource({
				"x, x, true",
				"test, test, true",
				"test, Test, false",
				"this is, not in this long string, false",
				"this is to long, to fit, false",
				"yyyyyxyyyy, x, true",
				"xyyyyyyyyy, x, true",
				"yyyyyyyyyx, x, true",
				"yyyyyXyyyy, x, false",
				//TODO add search for surrogate symbals that ignores codepoints
			})
			void testDefault(String left, String right, boolean equals) {
				assertUnicodeOp(op, StringMode.DEFAULT, left, right, equals);
			}

			@ParameterizedTest
			// [target, query]
			@CsvSource({
				"x, x, true",
				"test, test, true",
				"test, Test, true",
				"this is, not in this long string, false",
				"this is to long, to fit, false",
				"yyyyyxyyyy, x, true",
				"xyyyyyyyyy, x, true",
				"yyyyyyyyyx, x, true",
				"yyyyyXyyyy, x, true",
				"Xyyyyyyyyy, x, true",
				"yyyyyyyyyX, x, true",
				"is in this long string, This, true",
				"is in THIS long string, this, true",
				//TODO add search for surrogate symbals that ignores codepoints
			})
			void testLowerCase(String left, String right, boolean equals) {
				assertUnicodeOp(op, StringMode.LOWERCASE, left, right, equals);
			}

			@ParameterizedTest
			// [target, query]
			@CsvSource({
				"x, x, true",
				"test, test, true",
				"test, Test, true",
				"this is, not in this long string, false",
				"this is to long, to fit, false",
				"yyyyyxyyyy, x, true",
				"xyyyyyyyyy, x, true",
				"yyyyyyyyyx, x, true",
				"yyyyyXyyyy, x, true",
				"Xyyyyyyyyy, x, true",
				"yyyyyyyyyX, x, true",
				"is in this long string, This, true",
				"is in THIS long string, this, true",
				//TODO add search for surrogate symbals that ignores codepoints
			})
			//TODO this is effectively the same as lower case
			void testIgnoreCase(String left, String right, boolean equals) {
				assertUnicodeOp(op, StringMode.IGNORE_CASE, left, right, equals);
			}
		}

		@Nested
		class ForMatches {
			private final StringOp op = StringOp.MATCHES;

			@ParameterizedTest
			// [target, query]
			@CsvSource({
				"x, x, true",
				"a b x c, x, true",
				"a 123 b, [1-9]+, true",
				"ABC 123 XYZ, [a-z]+, false",
				"ABC 123 XYZ, [A-Z][a-z]+, false",
				"abc, ^abc$, true",
				"a b c, ^abc$, false",
			})
			void testDefault(String left, String right, boolean equals) {
				assertUnicodeOp(op, StringMode.DEFAULT, left, right, equals);
			}

			@Test
			void testLowerCase() {
				assertIcarusException(QueryErrorCode.INCORRECT_USE,
						() -> BinaryOperations.asciiOp(op, StringMode.LOWERCASE,
								Literals.of("left"), Literals.of("right")));
			}

			@ParameterizedTest
			// [target, query]
			@CsvSource({
				"x, x, true",
				"a b x c, x, true",
				"a 123 b, [1-9]+, true",
				"ABC 123 XYZ, [a-z]+, true",
				"ABC 123 XYZ, [A-Z][a-z]+, true",
				"abc, ^abc$, true",
				"a B c, ^abc$, false",
			})
			//TODO this is effectively the same as lower case
			void testIgnoreCase(String left, String right, boolean equals) {
				assertUnicodeOp(op, StringMode.IGNORE_CASE, left, right, equals);
			}
		}

	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.BinaryOperations#asciiOp(de.ims.icarus2.query.api.eval.BinaryOperations.StringOp, de.ims.icarus2.query.api.eval.BinaryOperations.StringMode, de.ims.icarus2.query.api.eval.Expression.TextExpression, de.ims.icarus2.query.api.eval.Expression.TextExpression)}.
	 */
	@Nested
	class ForAsciiOps {

		private BooleanExpression create(StringOp op, StringMode mode, String left, String right) {
			return BinaryOperations.asciiOp(op, mode, Literals.of(left), Literals.of(right));
		}

		private void assertAsciiOp(StringOp op, StringMode mode, String left, String right, boolean equals) {
			BooleanExpression expression = create(op, mode, left, right);
			assertThat(expression.computeAsBoolean()).isEqualTo(equals);

			BooleanExpression duplicate = (BooleanExpression) expression.duplicate(mock(EvaluationContext.class));
			assertThat(duplicate.computeAsBoolean()).isEqualTo(equals);

			BooleanExpression optimized = (BooleanExpression) expression.optimize(mock(EvaluationContext.class));
			assertThat(optimized.computeAsBoolean()).isEqualTo(equals);
		}

		@Nested
		class ForEquals {
			private final StringOp op = StringOp.EQUALS;

			@ParameterizedTest
			@CsvSource({
				"x, x, true",
				"test, test, true",
				"test, Test, false",
				"the first one, is longer, false",
				"this time, the second one is far longer, false",
			})
			void testDefault(String left, String right, boolean equals) {
				assertAsciiOp(op, StringMode.DEFAULT, left, right, equals);
			}

			@ParameterizedTest
			@CsvSource({
				"x, x, true",
				"X, x, true",
				"x, X, true",
				"X, X, true",
				"test, test, true",
				"test, Test, true",
				"the first one, is longer, false",
				"this time, the second one is far longer, false",
			})
			void testLowerCase(String left, String right, boolean equals) {
				assertAsciiOp(op, StringMode.LOWERCASE, left, right, equals);
			}

			@ParameterizedTest
			@CsvSource({
				"x, x, true",
				"X, x, true",
				"x, X, true",
				"X, X, true",
				"test, test, true",
				"test, Test, true",
				"TEST, tesT, true",
				"the first one, is longer, false",
				"this time, the second one is far longer, false",
			})
			//TODO this is effectively the same as lower case
			void testIgnoreCase(String left, String right, boolean equals) {
				assertAsciiOp(op, StringMode.IGNORE_CASE, left, right, equals);
			}
		}

		@Nested
		class ForContains {
			private final StringOp op = StringOp.CONTAINS;

			@ParameterizedTest
			// [target, query]
			@CsvSource({
				"x, x, true",
				"test, test, true",
				"test, Test, false",
				"this is, not in this long string, false",
				"this is to long, to fit, false",
				"yyyyyxyyyy, x, true",
				"xyyyyyyyyy, x, true",
				"yyyyyyyyyx, x, true",
				"yyyyyXyyyy, x, false",
				//TODO add search for surrogate symbals that ignores codepoints
			})
			void testDefault(String left, String right, boolean equals) {
				assertAsciiOp(op, StringMode.DEFAULT, left, right, equals);
			}

			@ParameterizedTest
			// [target, query]
			@CsvSource({
				"x, x, true",
				"test, test, true",
				"test, Test, true",
				"this is, not in this long string, false",
				"this is to long, to fit, false",
				"yyyyyxyyyy, x, true",
				"xyyyyyyyyy, x, true",
				"yyyyyyyyyx, x, true",
				"yyyyyXyyyy, x, true",
				"Xyyyyyyyyy, x, true",
				"yyyyyyyyyX, x, true",
				"is in this long string, This, true",
				"is in THIS long string, this, true",
				//TODO add search for surrogate symbals that ignores codepoints
			})
			void testLowerCase(String left, String right, boolean equals) {
				assertAsciiOp(op, StringMode.LOWERCASE, left, right, equals);
			}

			@ParameterizedTest
			// [target, query]
			@CsvSource({
				"x, x, true",
				"test, test, true",
				"test, Test, true",
				"this is, not in this long string, false",
				"this is to long, to fit, false",
				"yyyyyxyyyy, x, true",
				"xyyyyyyyyy, x, true",
				"yyyyyyyyyx, x, true",
				"yyyyyXyyyy, x, true",
				"Xyyyyyyyyy, x, true",
				"yyyyyyyyyX, x, true",
				"is in this long string, This, true",
				"is in THIS long string, this, true",
				//TODO add search for surrogate symbals that ignores codepoints
			})
			//TODO this is effectively the same as lower case
			void testIgnoreCase(String left, String right, boolean equals) {
				assertAsciiOp(op, StringMode.IGNORE_CASE, left, right, equals);
			}
		}

		@Nested
		class ForMatches {
			private final StringOp op = StringOp.MATCHES;

			@ParameterizedTest
			// [target, query]
			@CsvSource({
				"x, x, true",
				"a b x c, x, true",
				"a 123 b, [1-9]+, true",
				"ABC 123 XYZ, [a-z]+, false",
				"ABC 123 XYZ, [A-Z][a-z]+, false",
				"abc, ^abc$, true",
				"a b c, ^abc$, false",
			})
			void testDefault(String left, String right, boolean equals) {
				assertAsciiOp(op, StringMode.DEFAULT, left, right, equals);
			}

			@Test
			void testLowerCase() {
				assertIcarusException(QueryErrorCode.INCORRECT_USE,
						() -> BinaryOperations.asciiOp(op, StringMode.LOWERCASE,
								Literals.of("left"), Literals.of("right")));
			}

			@ParameterizedTest
			// [target, query]
			@CsvSource({
				"x, x, true",
				"a b x c, x, true",
				"a 123 b, [1-9]+, true",
				"ABC 123 XYZ, [a-z]+, true",
				"ABC 123 XYZ, [A-Z][a-z]+, true",
				"abc, ^abc$, true",
				"a B c, ^abc$, false",
			})
			//TODO this is effectively the same as lower case
			void testIgnoreCase(String left, String right, boolean equals) {
				assertAsciiOp(op, StringMode.IGNORE_CASE, left, right, equals);
			}
		}
	}

}
