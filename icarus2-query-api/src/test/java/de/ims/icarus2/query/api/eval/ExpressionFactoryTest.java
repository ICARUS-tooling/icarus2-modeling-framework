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

import static de.ims.icarus2.query.api.eval.EvaluationUtils.quote;
import static de.ims.icarus2.query.api.eval.EvaluationUtils.unescape;
import static de.ims.icarus2.query.api.eval.ExpressionTestUtils.assertExpression;
import static de.ims.icarus2.query.api.iql.AntlrUtils.createParser;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;

import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.query.api.iql.antlr.IQLParser;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.StandaloneExpressionContext;
import de.ims.icarus2.util.strings.StringUtil;

/**
 * @author Markus Gärtner
 *
 */
class ExpressionFactoryTest {

	@Nested
	class Constructor {
		@Test
		void testWithNull() {
			assertNPE(() -> new ExpressionFactory(null));
		}

		@Test
		void testWithValid() {
			EvaluationContext ctx = mock(EvaluationContext.class);
			ExpressionFactory factory = new ExpressionFactory(ctx);
			assertThat(factory.getContext()).isSameAs(ctx);
		}
	}

	@Nested
	class ConstantInput {

		private EvaluationContext context;
		private ExpressionFactory factory;

		@BeforeEach
		void setUp() {
			context = mock(EvaluationContext.class);
			factory = new ExpressionFactory(context);
		}

		@AfterEach
		void tearDown() {
			context = null;
			factory = null;
		}

		private Expression<?> parse(String s) {
			IQLParser parser = createParser(s, "expression");
			StandaloneExpressionContext ctx = parser.standaloneExpression();
			return factory.processExpression(ctx.expression());
		}

		@Nested
		class ForLiterals {

			@ValueSource(strings = {
				"null",
				"NULL",
			})
			@ParameterizedTest
			void testNullLiteral(String input) {
				Expression<?> exp = parse(input);
				assertThat(exp.getResultType()).isSameAs(TypeInfo.NULL);
				assertExpression(exp, context, null, Objects::equals);
			}

			@ValueSource(strings = {
				"",
				"x",
				"test123",
				"x test 123",
				"test\\n123\\tbla\\tblub..."
			})
			@ParameterizedTest
			void testStringLiteral(String input) {
				input = quote(input);
				String result = unescape(input);
				Expression<?> exp = parse(input);
				assertThat(exp.isText()).isTrue();
				assertExpression(exp, context, result, StringUtil::equals);
			}

			@CsvSource({
				"1, 1",
				"100, 100",
				"1_000, 1000",
				"1___000, 1000",
				"90_999_999, 90999999",
			})
			@ParameterizedTest
			void testIntegerLiteral(String input, int result) {
				Expression<?> exp = parse(input);
				assertThat(exp.isInteger()).isTrue();
				assertExpression(exp, context, result);
			}

			@CsvSource({
				"1.0, 1.0",
				"100.01, 100.01",
				"1_000.5, 1000.5",
				"1000.5_5, 1000.55",
				"1_000.5_5, 1000.55",
				"1___000.001, 1000.001",
				"90_999_999.5, 90999999.5",
			})
			@ParameterizedTest
			void testFloatingPointLiteral(String input, double result) {
				Expression<?> exp = parse(input);
				assertThat(exp.isFloatingPoint()).isTrue();
				assertExpression(exp, context, result);
			}

			@CsvSource({
				"true, true",
				"false, false",
				"TRUE, true",
				"FALSE, false",
			})
			@ParameterizedTest
			void testBooleanLiteral(String input, boolean result) {
				Expression<?> exp = parse(input);
				assertThat(exp.isBoolean()).isTrue();
				assertExpression(exp, context, result);
			}
		}

		@ParameterizedTest
		@CsvSource({
			// Simple formulas
			"1, 1",
			"1+2, 3",
			"8*11, 88",
			"123/3, 41",
			"123%4, 3",

			// Nested formulas
			"2+3*4, 14",
			"2-3*4, -10",
			"12/3+2, 6"
			//TODO
		})
		void testMathematicalInteger(String input, long result) {
			Expression<?> exp = parse(input);
			assertThat(exp.isInteger()).isTrue();
			assertExpression(exp, context, result);
		}

		@ParameterizedTest
		@CsvSource(delimiter=';', value={
			"2 in {1, 2, 3, 4,-10}; true",
			"2 in {1, 3, 4,-10}; false",
			"2 not in {1, 3, 4,-10}; true",
			"2 not in {1, 2, 3, 4,-10}; false",
			//TODO find easy way to test the 'all in' expression with multiple source values
		})
		void testSetPredicate(String input, boolean result) {
			Expression<?> exp = parse(input);
			assertThat(exp.isBoolean()).isTrue();
			assertExpression(exp, context, result);
		}

		@TestFactory
		Stream<DynamicNode> testStringConcatenation() {
			return Stream.of(
					// general
					new String[] {"x1", "x2"},
					// with ws
					new String[] {"test 1 2 3", "other_test"},
					new String[] {"test: \\n1\\t2\\t3", "other_test"},
					// with empty elements
					new String[] {"", ""},
					new String[] {"x", ""},
					new String[] {"", "x"},
					// with ws
					new String[] {" ", " "},
					new String[] {"x", " "},
					new String[] {" ", "x"}
			).map(elements -> {
				String expected = unescape(String.join("", elements));
				String input = "\""+String.join("\"+\"", elements)+"\"";
				return dynamicTest(input, () -> {
					Expression<?> exp = parse(input);
					assertThat(exp.isText()).isTrue();
					assertExpression(exp, context, (CharSequence)expected, StringUtil::equals);
				});
			});
		}
 	}
}
