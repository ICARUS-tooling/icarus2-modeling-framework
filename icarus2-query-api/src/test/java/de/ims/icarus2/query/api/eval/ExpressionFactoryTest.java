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

import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.query.api.eval.EvaluationUtils.castBooleanList;
import static de.ims.icarus2.query.api.eval.EvaluationUtils.castFloatingPointList;
import static de.ims.icarus2.query.api.eval.EvaluationUtils.castIntegerList;
import static de.ims.icarus2.query.api.eval.EvaluationUtils.castTextList;
import static de.ims.icarus2.query.api.eval.EvaluationUtils.quote;
import static de.ims.icarus2.query.api.eval.EvaluationUtils.unescape;
import static de.ims.icarus2.query.api.eval.ExpressionTestUtils.assertExpression;
import static de.ims.icarus2.query.api.eval.ExpressionTestUtils.assertListExpression;
import static de.ims.icarus2.query.api.eval.ExpressionTestUtils.assertQueryException;
import static de.ims.icarus2.query.api.iql.AntlrUtils.createParser;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.test.util.Pair.pair;
import static de.ims.icarus2.util.lang.Primitives._double;
import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives._long;
import static de.ims.icarus2.util.strings.StringUtil.formatDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
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

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.eval.EvaluationContext.AnnotationInfo;
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
import de.ims.icarus2.test.annotations.PostponedTest;
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
	class WithContext {

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

		private void prepareRef(String name, Expression<?> exp) {
			doReturn(Optional.of(exp)).when(context).resolve(isNull(), eq(name), any());
		}

		private void prepareVar(String name, Assignable<?> assignable) {
			doReturn(assignable).when(context).getVariable(name);
		}

		private void prepareMember(String name, Assignable<Item> member) {
			doReturn(Optional.of(member)).when(context).getMember(name);
		}

		private void prepareAnnotation(QualifiedIdentifier identifier, AnnotationInfo annotation) {
			doReturn(Optional.of(annotation)).when(context).findAnnotation(identifier);
		}

		private void prepareElement(Assignable<Item> item) {
			doReturn(Optional.of(item)).when(context).getElementStore();
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

			@Test
			void testUnparsableInteger() {
				char[] chars = new char[100];
				Arrays.fill(chars, '9');
				String input = new String(chars);

				assertQueryException(QueryErrorCode.INVALID_LITERAL, () -> parse(input));
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

			@PostponedTest("ignored until we figure out why Double.parseDouble() doesn't fail on overflow")
			@Test
			void testUnparsableFloatingPoint() {
				char[] chars = new char[100];
				Arrays.fill(chars, '9');
				chars[89] = '.';
				String input = new String(chars);

				assertQueryException(QueryErrorCode.INVALID_LITERAL, () -> parse(input));
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

			private String format(double d) {
				String s = formatDecimal(d);
				s = s.replace(',', '_');
				return s;
			}

			private Stream<DynamicNode> makeTests(IntConsumer action) {
				return IntStream.range(1, 10).mapToObj(size -> dynamicTest(String.valueOf(size),
						() -> action.accept(size)));
			}

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
					assertListExpression(castTextList(exp), context, StringUtil::equals, expected);
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
					assertListExpression(castIntegerList(exp), context, expected);
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
					assertListExpression(castFloatingPointList(exp), context, expected);
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
					assertListExpression(castBooleanList(exp), context, expected);
				});
			}

			/*
			 * Conversion priorities:
			 * text >> boolean >> floating point >> integer
			 */

			@TestFactory
			Stream<DynamicNode> testStringAutoCast() {
				String[] expected = {
						"test",
						"123",
						"123.5",
						"false",
						"TRUE",
				};

				return IntStream.range(0, expected.length).mapToObj(
						index -> dynamicTest(String.valueOf(index), () -> {
					String[] elements = expected.clone();
					for (int i = 0; i < expected.length; i++) {
						if(i==0 || i!=index) {
							elements[i] = quote(elements[i]);
						}
					}

					String input = "{"+String.join(",", elements)+"}";
					Expression<?> exp = parse(input);
					assertThat(exp.isList()).isTrue();
					assertListExpression(castTextList(exp), context, StringUtil::equals, expected);
				}));
			}

			@TestFactory
			Stream<DynamicNode> testBooleanAutoCast() {
				boolean[] expected = {
						true,
						false,
						false,
						false,
						true,
						true,
				};

				String[] castable = {
						"TRUE",
						"FALSE",
						"0",
						"0.0",
						"123",
						"-123.456",
				};

				return IntStream.range(0, expected.length).mapToObj(
						index -> dynamicTest(String.valueOf(index), () -> {
					String[] elements = new String[expected.length];
					for (int i = 0; i < expected.length; i++) {
						elements[i] = String.valueOf(expected[i]);
						if(i==index) {
							elements[i] = castable[i];
						}
					}

					String input = "{"+String.join(",", elements)+"}";
					Expression<?> exp = parse(input);
					assertThat(exp.isList()).isTrue();
					assertListExpression(castBooleanList(exp), context, expected);
				}));
			}

			@TestFactory
			Stream<DynamicNode> testFloatingPointAutoCast() {
				double[] expected = {
						1.01,
						2.5,
						123,
						Integer.MAX_VALUE,
						Integer.MIN_VALUE,
				};

				String[] castable = {
						String.valueOf(1.01),
						String.valueOf(2.5),
						String.valueOf(123),
						String.valueOf(Integer.MAX_VALUE),
						String.valueOf(Integer.MIN_VALUE),
				};

				return IntStream.range(0, expected.length).mapToObj(
						index -> dynamicTest(String.valueOf(index), () -> {
					String[] elements = new String[expected.length];
					for (int i = 0; i < expected.length; i++) {
						elements[i] = format(expected[i]);
						if(i==index) {
							elements[i] = castable[i];
						}
					}

					String input = "{"+String.join(",", elements)+"}";
					Expression<?> exp = parse(input);
					assertThat(exp.isList()).isTrue();
					assertListExpression(castFloatingPointList(exp), context, expected);
				}));
			}

			@TestFactory
			Stream<DynamicNode> testStringForcedCast() {
				String[] expected = {
						"123",
						"123.5",
						"false",
						"TRUE",
				};

				return IntStream.range(0, expected.length).mapToObj(
						size -> dynamicTest(String.valueOf(size), () -> {
					String[] elements = Arrays.copyOf(expected, size);
					String input = "string[]{"+String.join(",", elements)+"}";
					Expression<?> exp = parse(input);
					assertThat(exp.isList()).isTrue();
					assertListExpression(castTextList(exp), context, StringUtil::equals, elements);
				}));
			}

			@TestFactory
			Stream<DynamicNode> testBooleanForcedCast() {
				boolean[] expected = {
						true,
						false,
						false,
						false,
						true,
						true,
				};

				String[] castable = {
						"\"not empty\"",
						"\"\"",
						"0",
						"0.0",
						"123",
						"-123.456",
				};

				return IntStream.range(0, expected.length).mapToObj(size -> {
					String[] elements = Arrays.copyOf(castable, size);
					boolean[] values = Arrays.copyOf(expected, size);

					String input = "boolean[]{"+String.join(",", elements)+"}";

					return dynamicTest(input, () -> {
						Expression<?> exp = parse(input);
						assertThat(exp.isList()).isTrue();
						assertListExpression(castBooleanList(exp), context, values);
					});
				});
			}

			@TestFactory
			Stream<DynamicNode> testFloatingPointForcedCast() {
				double[] expected = {
						1.01,
						2.5,
						123,
						Integer.MAX_VALUE,
						Integer.MIN_VALUE,
				};

				String[] castable = {
						String.valueOf(1.01),
						String.valueOf(2.5),
						String.valueOf(123),
						String.valueOf(Integer.MAX_VALUE),
						String.valueOf(Integer.MIN_VALUE),
				};

				return IntStream.range(0, expected.length).mapToObj(size -> {
					String[] elements = Arrays.copyOf(castable, size);
					double[] values = Arrays.copyOf(expected, size);

					String input = "float[]{"+String.join(",", elements)+"}";

					return dynamicTest(input, () -> {
						Expression<?> exp = parse(input);
						assertThat(exp.isList()).isTrue();
						assertListExpression(castFloatingPointList(exp), context, values);
					});
				});
			}

			@TestFactory
			Stream<DynamicNode> testIntegerForcedCast() {
				long[] expected = {
						1,
						2,
						123,
						Integer.MAX_VALUE,
						Integer.MIN_VALUE,
				};

				String[] castable = {
						String.valueOf(1.01),
						String.valueOf(2.5),
						String.valueOf(123),
						String.valueOf(Integer.MAX_VALUE),
						String.valueOf(Integer.MIN_VALUE),
				};

				return IntStream.range(0, expected.length).mapToObj(size -> {
					String[] elements = Arrays.copyOf(castable, size);
					long[] values = Arrays.copyOf(expected, size);

					String input = "int[]{"+String.join(",", elements)+"}";

					return dynamicTest(input, () -> {
						Expression<?> exp = parse(input);
						assertThat(exp.isList()).isTrue();
						assertListExpression(castIntegerList(exp), context, values);
					});
				});
			}

			@Test
			void testEmptyStringArray() {
				Expression<?> exp = parse("string[]{}");
				assertThat(exp.isList()).isTrue();
				assertListExpression(castTextList(exp), context, StringUtil::equals);
			}

			@Test
			void testEmptyIntegerArray() {
				Expression<?> exp = parse("int[]{}");
				assertThat(exp.isList()).isTrue();
				assertListExpression(castIntegerList(exp), context);
			}

			@Test
			void testEmptyFloatingPointArray() {
				Expression<?> exp = parse("float[]{}");
				assertThat(exp.isList()).isTrue();
				assertListExpression(castFloatingPointList(exp), context);
			}

			@Test
			void testEmptyBooleanArray() {
				Expression<?> exp = parse("boolean[]{}");
				assertThat(exp.isList()).isTrue();
				assertListExpression(castBooleanList(exp), context);
			}

			@Test
			void testUntypedEmptyArray() {
				assertQueryException(QueryErrorCode.INCORRECT_USE,
						() -> parse("{}"));
			}
		}

		@Nested
		class ForListAccess {



			// SINGLE ACCESS

			@TestFactory
			Stream<DynamicNode> testText() {
				CharSequence[] array = IntStream.range(0, 10)
						.mapToObj(i -> "item_"+i)
						.toArray(CharSequence[]::new);
				prepareRef("array", ArrayLiterals.ofGeneric(array));

				return IntStream.range(0, array.length).mapToObj(
						index -> dynamicTest(String.valueOf(index), () -> {
							String input = String.format("array[%d]", _int(index));
							Expression<?> parsed = parse(input);
							assertThat(parsed.isText()).isTrue();
							assertExpression(parsed, context, array[index], StringUtil::equals);
						}));
			}

			@TestFactory
			Stream<DynamicNode> testInteger() {
				long[] array = LongStream.range(0, 10).toArray();
				prepareRef("array", ArrayLiterals.of(array));

				return IntStream.range(0, array.length).mapToObj(
						index -> dynamicTest(String.valueOf(index), () -> {
							String input = String.format("array[%d]", _int(index));
							Expression<?> parsed = parse(input);
							assertThat(parsed.isInteger()).isTrue();
							assertExpression(parsed, context, array[index]);
						}));
			}

			@TestFactory
			Stream<DynamicNode> testFloatingPoint() {
				double[] array = DoubleStream.iterate(0.6, v -> v+0.7).limit(10).toArray();
				prepareRef("array", ArrayLiterals.of(array));

				return IntStream.range(0, array.length).mapToObj(
						index -> dynamicTest(String.valueOf(index), () -> {
							String input = String.format("array[%d]", _int(index));
							Expression<?> parsed = parse(input);
							assertThat(parsed.isFloatingPoint()).isTrue();
							assertExpression(parsed, context, array[index]);
						}));
			}

			@TestFactory
			Stream<DynamicNode> testBoolean() {
				boolean[] array = new boolean[10];
				for (int i = 0; i < array.length; i++) {
					array[i] = i%3==1;
				}
				prepareRef("array", ArrayLiterals.of(array));

				return IntStream.range(0, array.length).mapToObj(
						index -> dynamicTest(String.valueOf(index), () -> {
							String input = String.format("array[%d]", _int(index));
							Expression<?> parsed = parse(input);
							assertThat(parsed.isBoolean()).isTrue();
							assertExpression(parsed, context, array[index]);
						}));
			}

			// FILTERS

			@TestFactory
			Stream<DynamicNode> testTextFilter() {
				CharSequence[] array = IntStream.range(0, 10)
						.mapToObj(i -> "item_"+i)
						.toArray(CharSequence[]::new);
				prepareRef("array", ArrayLiterals.ofGeneric(array));

				return IntStream.rangeClosed(2, array.length).mapToObj(size -> {
					int[] filter = IntStream.range(0, size).toArray();
					String input = String.format("array%s", Arrays.toString(filter));

					return dynamicTest(input, () -> {
						Expression<?> parsed = parse(input);
						assertThat(parsed.isList()).isTrue();
						assertThat(parsed).isInstanceOf(ListExpression.class);
						assertListExpression(castTextList(parsed), context,
								StringUtil::equals, Arrays.copyOf(array, size));
					});
				});
			}

			@TestFactory
			Stream<DynamicNode> testIntegerFilter() {
				long[] array = LongStream.range(0, 10).toArray();
				prepareRef("array", ArrayLiterals.of(array));

				return IntStream.rangeClosed(2, array.length).mapToObj(size -> {
					int[] filter = IntStream.range(0, size).toArray();
					String input = String.format("array%s", Arrays.toString(filter));

					return dynamicTest(input, () -> {
						Expression<?> parsed = parse(input);
						assertThat(parsed.isList()).isTrue();
						assertThat(parsed).isInstanceOf(IntegerListExpression.class);
						assertListExpression(castIntegerList(parsed), context, Arrays.copyOf(array, size));
					});
				});
			}

			@TestFactory
			Stream<DynamicNode> testFloatingPointFilter() {
				double[] array = DoubleStream.iterate(0.6, v -> v+0.7).limit(10).toArray();
				prepareRef("array", ArrayLiterals.of(array));

				return IntStream.rangeClosed(2, array.length).mapToObj(size -> {
					int[] filter = IntStream.range(0, size).toArray();
					String input = String.format("array%s", Arrays.toString(filter));

					return dynamicTest(input, () -> {
						Expression<?> parsed = parse(input);
						assertThat(parsed.isList()).isTrue();
						assertThat(parsed).isInstanceOf(FloatingPointListExpression.class);
						assertListExpression(castFloatingPointList(parsed), context, Arrays.copyOf(array, size));
					});
				});
			}

			@TestFactory
			Stream<DynamicNode> testBooleanFilter() {
				boolean[] array = new boolean[10];
				for (int i = 0; i < array.length; i++) {
					array[i] = i%3==1;
				}
				prepareRef("array", ArrayLiterals.of(array));

				return IntStream.rangeClosed(2, array.length).mapToObj(size -> {
					int[] filter = IntStream.range(0, size).toArray();
					String input = String.format("array%s", Arrays.toString(filter));

					return dynamicTest(input, () -> {
						Expression<?> parsed = parse(input);
						assertThat(parsed.isList()).isTrue();
						assertThat(parsed).isInstanceOf(BooleanListExpression.class);
						assertListExpression(castBooleanList(parsed), context, Arrays.copyOf(array, size));
					});
				});
			}

			// EXPANDED

			@Test
			void testTextExpansion() {
				CharSequence[] array = IntStream.range(0, 10)
						.mapToObj(i -> "item_"+i)
						.toArray(CharSequence[]::new);
				prepareRef("array", ArrayLiterals.ofGeneric(array));
				int[] filter = {2, 7};
				prepareRef("indices", ArrayLiterals.of(filter));

				String input = "array[indices]";
				CharSequence[] expected = new CharSequence[]{array[filter[0]], array[filter[1]]};

				Expression<?> parsed = parse(input);
				assertThat(parsed.isList()).isTrue();
				assertThat(parsed).isInstanceOf(ListExpression.class);
				assertListExpression(castTextList(parsed), context, StringUtil::equals, expected);
			}

			@Test
			void testIntegerExpansion() {
				long[] array = LongStream.range(0, 10).toArray();
				prepareRef("array", ArrayLiterals.of(array));
				int[] filter = {2, 7};
				prepareRef("indices", ArrayLiterals.of(filter));

				String input = "array[indices]";
				long[] expected = new long[]{array[filter[0]], array[filter[1]]};

				Expression<?> parsed = parse(input);
				assertThat(parsed.isList()).isTrue();
				assertThat(parsed).isInstanceOf(IntegerListExpression.class);
				assertListExpression(castIntegerList(parsed), context, expected);
			}

			@Test
			void testFloatingPointExpansion() {
				double[] array = DoubleStream.iterate(0.6, v -> v+0.7).limit(10).toArray();
				prepareRef("array", ArrayLiterals.of(array));
				int[] filter = {2, 7};
				prepareRef("indices", ArrayLiterals.of(filter));

				String input = "array[indices]";
				double[] expected = new double[]{array[filter[0]], array[filter[1]]};

				Expression<?> parsed = parse(input);
				assertThat(parsed.isList()).isTrue();
				assertThat(parsed).isInstanceOf(FloatingPointListExpression.class);
				assertListExpression(castFloatingPointList(parsed), context, expected);
			}

			@Test
			void testBooleanExpansion() {
				boolean[] array = new boolean[10];
				for (int i = 0; i < array.length; i++) {
					array[i] = i%3==1;
				}
				prepareRef("array", ArrayLiterals.of(array));
				int[] filter = {2, 7};
				prepareRef("indices", ArrayLiterals.of(filter));

				String input = "array[indices]";
				boolean[] expected = new boolean[]{array[filter[0]], array[filter[1]]};

				Expression<?> parsed = parse(input);
				assertThat(parsed.isList()).isTrue();
				assertThat(parsed).isInstanceOf(BooleanListExpression.class);
				assertListExpression(castBooleanList(parsed), context, expected);
			}

			@Test
			void testEmptyIndices() {
				prepareRef("array", ArrayLiterals.emptyArray(TypeInfo.GENERIC));
				assertQueryException(QueryErrorCode.SYNTAX_ERROR, () -> parse("array[]"));
			}
		}

		@Nested
		class ForReferences {

			@ParameterizedTest
			@ValueSource(strings = {
					"x",
					"x1234y",
					"myAwesomeLongVariableNameNobodyNeedsButILikeItXD"
			})
			void testVariable(String name) {
				Assignable<?> variable = mock(Assignable.class);
				prepareVar(name, variable);

				Expression<?> parsed = parse("@"+name);

				assertThat(parsed).isSameAs(variable);
			}

			@ParameterizedTest
			@ValueSource(strings = {
					"x",
					"x1234y",
					"myAwesomeLongVariableNameNobodyNeedsButILikeItXD"
			})
			void testMember(String name) {
				Assignable<Item> member = mock(Assignable.class);
				prepareMember(name, member);

				Expression<?> parsed = parse("$"+name);

				assertThat(parsed).isSameAs(member);
			}

			@ParameterizedTest
			@ValueSource(strings = {
					"x",
					"x1234y",
					"myAwesomeLongVariableNameNobodyNeedsButILikeItXD"
			})
			void testRef(String name) {
				Expression<?> ref = mock(Expression.class);
				prepareRef(name, ref);

				Expression<?> parsed = parse(name);

				assertThat(parsed).isSameAs(ref);
			}

			@ParameterizedTest
			@ValueSource(strings = {
					"x::y",
					"someDomain::x1234y",
					"myAwesomeLongVariableNameNobodyNeedsButILikeIt::itGetsEvenLongerAndNobodyCanDoAnythingAboutItXD"
			})
			void testQualifiedRef(String name) {
				Item item = mockItem();
				Assignable<Item> element = mock(Assignable.class);
				when(element.compute()).thenReturn(item);
				prepareElement(element);

				QualifiedIdentifier identifier = QualifiedIdentifier.parseIdentifier(name);

				String value = "testValue";
				AnnotationInfo annotation = new AnnotationInfo(name, identifier.getElement(),
						ValueType.STRING, TypeInfo.TEXT);
				Function<Item, Object> func = mock(Function.class);
				when(func.apply(eq(item))).thenReturn(value);
				annotation.objectSource = func;
				prepareAnnotation(identifier, annotation);

				Expression<?> parsed = parse(name);

				assertThat(parsed.isText()).isTrue();
				assertThat(parsed.compute()).isEqualTo(value);
			}
		}

		@Nested
		class ForFormulas {

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
				//TODO add wrapped formulas and other operators
			})
			void testIntegerFormulas(String input, long result) {
				Expression<?> exp = parse(input);
				assertThat(exp.isInteger()).isTrue();
				assertExpression(exp, context, result);
			}
		}

		@Nested
		class ForSetPredicates {

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
			void testFloatingPointSetPredicate(String input, boolean result) {
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

			@Test
			void testSingleElementAllIn() {
				assertQueryException(QueryErrorCode.INCORRECT_USE, () -> parse("2 all in {1, 2, 3}"));
			}
		}

		@Nested
		class ForStringConcatenation {

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

		@Nested
		class ForCasting {

			@TestFactory
			Stream<DynamicNode> testStringCast() {
				return Stream.of(
					// Direct casts
					pair("\"x\"", "x"),
					pair("123", "123"),
					pair("false", "false"),
					pair("FALSE", "false"),
					pair("true", "true"),
					pair("TRUE", "true"),
					pair("123.5", "123.5"),
					// Complex casts,
					pair("(123+5)", "128"),
					pair("(123+5.5)", "128.5"),
					pair("(123>456)", "false")
				).map(p -> {
					String input = "(string)"+p.first;
					return dynamicTest(input, () -> {
						Expression<?> exp = parse(input);
						assertThat(exp.isText()).isTrue();
						assertExpression(exp, context, p.second, StringUtil::equals);
					});
				});
			}

			@TestFactory
			Stream<DynamicNode> testIntegerCast() {
				return Stream.of(
					// Direct casts
					pair("\"123\"", _long(123)),
					pair("123", _long(123)),
					pair("123.5", _long(123)),
					// Complex casts,
					pair("(\"12\"+\"8\")", _long(128)),
					pair("(123+5.5)", _long(128))
				).map(p -> {
					String input = "(int)"+p.first;
					return dynamicTest(input, () -> {
						Expression<?> exp = parse(input);
						assertThat(exp.isInteger()).isTrue();
						assertExpression(exp, context, p.second.longValue());
					});
				});
			}

			@TestFactory
			Stream<DynamicNode> testFloatingPointCast() {
				return Stream.of(
					// Direct casts
					pair("\"123.5\"", _double(123.5)),
					pair("123", _double(123)),
					pair("123.5", _double(123.5)),
					// Complex casts,
					pair("(\"12\"+\"8.5\")", _double(128.5)),
					pair("(123+5.5)", _double(128.5))
				).map(p -> {
					String input = "(float)"+p.first;
					return dynamicTest(input, () -> {
						Expression<?> exp = parse(input);
						assertThat(exp.isFloatingPoint()).isTrue();
						assertExpression(exp, context, p.second.doubleValue());
					});
				});
			}

			@TestFactory
			Stream<DynamicNode> testBooleanCast() {
				return Stream.of(
					// Direct casts
					pair("\"123.5\"", Boolean.TRUE),
					pair("\"\"", Boolean.FALSE),
					pair("123", Boolean.TRUE),
					pair("0", Boolean.FALSE),
					pair("123.5", Boolean.TRUE),
					pair("0.0", Boolean.FALSE),
					// Complex casts,
					pair("(\"\"+\"\")", Boolean.FALSE),
					pair("(123<456)", Boolean.TRUE)
				).map(p -> {
					String input = "(boolean)"+p.first;
					return dynamicTest(input, () -> {
						Expression<?> exp = parse(input);
						assertThat(exp.isBoolean()).isTrue();
						assertExpression(exp, context, p.second.booleanValue());
					});
				});
			}
		}

		@Nested
		class ForWrapping {

			@ParameterizedTest
			@CsvSource(delimiter=';', value={
				"(false) ; false",
				"((false)) ; false",
				"(((false))) ; false",
				"((((false)))) ; false",
				"(true) ; true",
				"((true)) ; true",
				"(((true))) ; true",
				"((((true)))) ; true",
			})
			void testBoolean(String input, boolean result) {
				Expression<?> exp = parse(input);
				assertThat(exp.isBoolean()).isTrue();
				assertThat(Literals.isLiteral(exp)).isTrue();
				assertExpression(exp, context, result);
			}

			@ParameterizedTest
			@CsvSource(delimiter=';', value={
				"(\"test\") ; test",
				"((\"test\")) ; test",
				"(((\"test\"))) ; test",
				"((((\"test\")))) ; test",
			})
			void testString(String input, String result) {
				Expression<?> exp = parse(input);
				assertThat(exp.isText()).isTrue();
				assertThat(Literals.isLiteral(exp)).isTrue();
				assertExpression(exp, context, result, StringUtil::equals);
			}

			@ParameterizedTest
			@CsvSource(delimiter=';', value={
				"(123) ; 123",
				"((123)) ; 123",
				"(((123))) ; 123",
				"((((123)))) ; 123",
			})
			void testInteger(String input, long result) {
				Expression<?> exp = parse(input);
				assertThat(exp.isInteger()).isTrue();
				assertThat(Literals.isLiteral(exp)).isTrue();
				assertExpression(exp, context, result);
			}

			@ParameterizedTest
			@CsvSource(delimiter=';', value={
				"(123.5) ; 123.5",
				"((123.5)) ; 123.5",
				"(((123.5))) ; 123.5",
				"((((123.5)))) ; 123.5",
			})
			void testFloatingPoint(String input, double result) {
				Expression<?> exp = parse(input);
				assertThat(exp.isFloatingPoint()).isTrue();
				assertThat(Literals.isLiteral(exp)).isTrue();
				assertExpression(exp, context, result);
			}
		}

		@Nested
		class ForLogicalOperators {

			@ParameterizedTest
			@CsvSource(delimiter=';', value={
				// Literals
				"true && true ; true",
				"true && false ; false",
				"false && true ; false",
				"false && false ; false",
				// Literals and comparators
				"1>5 && true ; false",
				"5>1 && true ; true",
				"1>5 && false ; false",
				"5>1 && false ; false",
				// Wrapping
				"true && (false && true) ; false",
				"false && (true && false) ; false",
				"true && (true && true) ; true",
				"false && (false && false) ; false",
			})
			void testConjunction(String input, boolean result) {
				Expression<?> exp = parse(input);
				assertThat(exp.isBoolean()).isTrue();
				assertExpression(exp, context, result);
			}

			@ParameterizedTest
			@CsvSource(delimiter=';', value={
				// Literals
				"true || true ; true",
				"true || false ; true",
				"false || true ; true",
				"false || false ; false",
				// Literals and comparators
				"1>5 || true ; true",
				"5>1 || true ; true",
				"1>5 || false ; false",
				"5>1 || false ; true",
				// Wrapping
				"true || (false || true) ; true",
				"false || (true || false) ; true",
				"true || (true || true) ; true",
				"false || (false || false) ; false",
			})
			void testDisjunction(String input, boolean result) {
				Expression<?> exp = parse(input);
				assertThat(exp.isBoolean()).isTrue();
				assertExpression(exp, context, result);
			}

			@ParameterizedTest
			@CsvSource(delimiter=';', value={
				// No wrapping
				"true || false && true ; true",
				// Wrapping
				"true && (false || true) ; true",
				"false && (true || false) ; false",
				"true && (true || true) ; true",
				"false && (false || false) ; false",
			})
			void testMixed(String input, boolean result) {
				Expression<?> exp = parse(input);
				assertThat(exp.isBoolean()).isTrue();
				assertExpression(exp, context, result);
			}
		}

		@Nested
		class ForUnaryOperations {

			@ParameterizedTest
			@CsvSource(delimiter=';', value={
				"!false;true",
				"not false;true",
				"!true;false",
				"not true;false",
			})
			void testBooleanNegation(String input, boolean expected) {
				Expression<?> exp = parse(input);
				assertThat(exp.isBoolean()).isTrue();
				assertExpression(exp, context, expected);
			}
		}
 	}
}
