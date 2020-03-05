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
import static de.ims.icarus2.query.api.eval.ExpressionTestUtils.assertListExpression;
import static de.ims.icarus2.query.api.iql.AntlrUtils.createParser;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;

import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
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

import de.ims.icarus2.query.api.eval.Expression.BooleanListExpression;
import de.ims.icarus2.query.api.eval.Expression.FloatingPointListExpression;
import de.ims.icarus2.query.api.eval.Expression.IntegerListExpression;
import de.ims.icarus2.query.api.eval.Expression.ListExpression;
import de.ims.icarus2.query.api.eval.SetPredicates.FloatingPointSetPredicate;
import de.ims.icarus2.query.api.eval.SetPredicates.IntegerSetPredicate;
import de.ims.icarus2.query.api.eval.SetPredicates.TextSetPredicate;
import de.ims.icarus2.query.api.eval.UnaryOperations.BooleanNegation;
import de.ims.icarus2.query.api.iql.antlr.IQLParser;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.StandaloneExpressionContext;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
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

		@Nested
		class ForArrays {

			@SuppressWarnings("unchecked")
			@CsvSource(delimiter=';', value = {
				"{null}; 1",
				"{null, null}; 2",
				"{NULL}; 1",
				"{NULL, null}; 2",
				"{null, null, null, null}; 4",
			})
			@ParameterizedTest
			void testNullArray(String input, int size) {
				Object[] target = Stream.generate(() -> null).limit(size).toArray();
				Expression<?> exp = parse(input);
				assertThat(exp.isList()).isTrue();
				assertListExpression((ListExpression<?, Object>)exp, context, Objects::equals, target);
			}

			private Stream<DynamicNode> makeTests(IntConsumer action) {
				return IntStream.range(1, 10).mapToObj(size -> dynamicTest(String.valueOf(size),
						() -> action.accept(size)));
			}

			@SuppressWarnings("unchecked")
			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testStringArray(RandomGenerator rng) {
				return makeTests(size -> {
					String[] expected = IntStream.range(0, size)
							.mapToObj(i -> "item_"+i)
							.map(EvaluationUtils::quote)
							.toArray(String[]::new);
					String[] elements = Stream.of(expected)
							.map(EvaluationUtils::quote)
							.toArray(String[]::new);
					String input = "{"+String.join(",", elements)+"}";
					Expression<?> exp = parse(input);
					assertThat(exp.isList()).isTrue();
					assertListExpression((ListExpression<?, CharSequence>)exp,
							context, StringUtil::equals, expected);
				});
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testIntegerArray(RandomGenerator rng) {
				return makeTests(size -> {
					long[] expected = rng.longs(size).toArray();
					String[] elements = LongStream.of(expected)
							.mapToObj(String::valueOf)
							.toArray(String[]::new);
					String input = "{"+String.join(",", elements)+"}";
					Expression<?> exp = parse(input);
					assertThat(exp.isList()).isTrue();
					assertListExpression((IntegerListExpression<?>)exp, context, expected);
				});
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testFloatingPointArray(RandomGenerator rng) {
				return makeTests(size -> {
					double[] expected = rng.doubles(size).toArray();
					String[] elements = DoubleStream.of(expected)
							.mapToObj(String::valueOf)
							.toArray(String[]::new);
					String input = "{"+String.join(",", elements)+"}";
					Expression<?> exp = parse(input);
					assertThat(exp.isList()).isTrue();
					assertListExpression((FloatingPointListExpression<?>)exp, context, expected);
				});
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testBooleanArray(RandomGenerator rng) {
				return makeTests(size -> {
					boolean[] expected = new boolean[size];
					String[] elements = new String[size];

					for (int i = 0; i < elements.length; i++) {
						expected[i] = i%2==0;
						elements[i] = String.valueOf(expected[i]);
					}

					String input = "{"+String.join(",", elements)+"}";
					Expression<?> exp = parse(input);
					assertThat(exp.isList()).isTrue();
					assertListExpression((BooleanListExpression<?>)exp, context, expected);
				});
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

		private Expression<?> unwrapNegation(Expression<?> exp) {
			if(exp instanceof BooleanNegation) {
				exp = ((BooleanNegation)exp).getSource();
			}
			return exp;
		}

		// We use the verbose syntax here for readability (so 'all not' instead of '*!')
		@ParameterizedTest
		@CsvSource(delimiter=';', value={
			// simple containment
			"2 in {1, 2, 3, 4,-10}; true",
			"2 in {1, 3, 4,-10}; false",
			// negated simple containment
			"2 not in {1, 3, 4,-10}; true",
			"2 not in {1, 2, 3, 4,-10}; false",
			// with formulas
			"2*2 in {1, 3, 4,-10}; true",
			"2+4 not in {1, 2, 3, 4,-10}; true",
			"2+2 not in {1, 2, 3, 4,-10}; false",
			"2*2 in {1, 3, 5-1,-10}; true",
			"2+4 not in {1, 2, 6/2, 4,-10}; true",
			"2+1 not in {1, 2, 6/2, 4,-10}; false",
			// expanded containment
			"{0, 2} in {1, 2, 3, 4,-10}; true",
			"{0, -1} in {1, 2, 3, 4,-10}; false",
			// negated expanded containment
			"{0, 2} not in {1, 2, 3, 4,-10}; false",
			"{0, -1} not in {1, 2, 3, 4,-10}; true",
			// 'all in'
			"{3, 2} all in {1, 2, 3, 4,-10}; true",
			"{0, 2} all in {1, 2, 3, 4,-10}; false",
			// not 'all in'
			"{3, 2} all not in {1, 2, 3, 4,-10}; false",
			"{0, 2} all not in {1, 2, 3, 4,-10}; false",
			"{0, -1} all not in {1, 2, 3, 4,-10}; true",
			"{0} all not in {1, 2, 3, 4,-10}; true"
		})
		void testIntegerSetPredicate(String input, boolean result) {
			Expression<?> exp = parse(input);
			assertThat(exp.isBoolean()).isTrue();
			assertThat(unwrapNegation(exp)).isInstanceOf(IntegerSetPredicate.class);
			assertExpression(exp, context, result);
		}

		// We use the verbose syntax here for readability (so 'all not' instead of '*!')
		@ParameterizedTest
		@CsvSource(delimiter=';', value={
			// simple containment
			"2.1 in {1.5, 2.1, 3.01, 4.001, -10.5}; true",
			"2.1 in {1.5, 2.2, 3.01, 4.001, -10.5}; false",
			// negated simple containment
			"2.1 not in {1.5, 2.2, 3.01, 4.001, -10.5}; true",
			"2.1 not in {1.5, 2.1, 3.01, 4.001, -10.5}; false",
			// with formulas
			"2*2.1 in {1.0, 3.01, 4.2, -10.5}; true",
			"2+4.3 not in {1.0, 2, 3.3, 4.001, -10.5}; true",
			"2+2.1 not in {1.0, 2, 3.3, 4.1, -10.5}; false",
			"2*2.1 in {1, 3, 5-0.8, -10.5}; true",
			"2+4.1 not in {1.0, 2.2, 6/2.1, 4.001, -10.5}; true",
			"2+1.5 not in {1.0, 2.2, 7.0/2, 4.001, -10.5}; false",
			// expanded containment
			"{0.0, 2.1} in {1.5, 2.1, 3.01, 4.001, -10.5}; true",
			"{0.0, -1.5} in {1.5, 2.1, 3.01, 4.001, -10.5}; false",
			// negated expanded containment
			"{0.0, 2.1} not in {1.5, 2.1, 3.01, 4.001, -10.5}; false",
			"{0.0, -1.0} not in {1.5, 2.1, 3.01, 4.001, -10.5}; true",
			// 'all in'
			"{3.01, 2.1} all in {1.5, 2.1, 3.01, 4.001, -10.5}; true",
			"{0.0, 2.1} all in {1.5, 2.1, 3.01, 4.001, -10.5}; false",
			// 'all not in'
			"{3.3, 2.1} all not in {1.5, 2.1, 3.01, 4.001, -10.5}; false",
			"{0.0, 2.1} all not in {1.5, 2.1, 3.01, 4.001, -10.5}; false",
			"{0.0, -1.5} all not in {1.5, 2.1, 3.01, 4.001, -10.5}; true",
			"{0.0} all not in {1.5, 2.1, 3.01, 4.001, -10.5}; true"
		})
		void testFlaotingPointSetPredicate(String input, boolean result) {
			Expression<?> exp = parse(input);
			assertThat(exp.isBoolean()).isTrue();
			assertThat(unwrapNegation(exp)).isInstanceOf(FloatingPointSetPredicate.class);
			assertExpression(exp, context, result);
		}

		// We use the verbose syntax here for readability (so 'all not' instead of '*!')
		@ParameterizedTest
		@CsvSource(delimiter=';', value={
			// simple containment
			"\"x\" in {\"x\", \"y\", \"z\"}; true",
			"\"x\" in {\"xx\", \"y\", \"z\"}; false",
			// negated simple containment
			"\"x\" not in {\"xx\", \"y\", \"z\"}; true",
			"\"x\" not in {\"x\", \"y\", \"z\"}; false",
			// with formulas
			"\"x\"+\"2\" in {\"x2\", \"y\", \"z\"}; true",
			"\"x\"+\"2\" not in {\"x\", \"y\", \"z\"}; true",
			"\"x\"+\"2\" not in {\"x2\", \"y\", \"z\"}; false",
			"\"x\"+\"2\" in {\"x\"+\"2\", \"y\", \"z\"}; true",
			"\"x\"+\"2\" not in {\"x\", \"y\"+\"2\", \"z\"}; true",
			"\"x\"+\"2\" not in {\"x\"+\"2\", \"y\", \"z\"}; false",
			// expanded containment
			"{\"x\", \"y2\"} in {\"x\", \"y\", \"z\"}; true",
			"{\"x2\", \"y2\"} in {\"x\", \"y\", \"z\"}; false",
			// negated expanded containment
			"{\"x\", \"y2\"} not in {\"x\", \"y\", \"z\"}; false",
			"{\"x2\", \"y2\"} not in {\"x\", \"y\", \"z\"}; true",
			// 'all in'
			"{\"x\", \"y\"} all in {\"x\", \"y\", \"z\"}; true",
			"{\"x\", \"y2\"} all in {\"x\", \"y\", \"z\"}; false",
			// 'all not in'
			"{\"x\", \"y\"} all not in {\"x\", \"y\", \"z\"}; false",
			"{\"x\", \"y2\"} all not in {\"x\", \"y\", \"z\"}; false",
			"{\"x2\", \"y2\"} all not in {\"x\", \"y\", \"z\"}; true",
			"{\"x2\"} all not in {\"x\", \"y\", \"z\"}; true"
		})
		void testTextSetPredicate(String input, boolean result) {
			Expression<?> exp = parse(input);
			assertThat(exp.isBoolean()).isTrue();
			assertThat(unwrapNegation(exp)).isInstanceOf(TextSetPredicate.class);
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
