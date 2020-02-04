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
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.ims.icarus2.query.api.eval.BinaryOperations.StringMode;
import de.ims.icarus2.query.api.eval.BinaryOperations.StringOp;
import de.ims.icarus2.query.api.eval.Expression.BooleanExpression;

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
				//TODO
			})
			void testDefault(String left, String right, boolean equals) {
				assertAsciiOp(op, StringMode.DEFAULT, left, right, equals);
			}

			@ParameterizedTest
			// [target, query]
			@CsvSource({
				"x, x, true",
				//TODO
			})
			void testLowerCase(String left, String right, boolean equals) {
				assertAsciiOp(op, StringMode.LOWERCASE, left, right, equals);
			}

			@ParameterizedTest
			// [target, query]
			@CsvSource({
				"x, x, true",
				//TODO
			})
			//TODO this is effectively the same as lower case
			void testIgnoreCase(String left, String right, boolean equals) {
				assertAsciiOp(op, StringMode.IGNORE_CASE, left, right, equals);
			}
		}
	}

}
