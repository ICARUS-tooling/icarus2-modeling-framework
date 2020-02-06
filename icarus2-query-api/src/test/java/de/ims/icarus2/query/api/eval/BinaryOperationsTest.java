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
import static de.ims.icarus2.test.TestUtils.displayString;
import static de.ims.icarus2.test.TestUtils.npeAsserter;
import static de.ims.icarus2.test.util.Triple.triple;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static de.ims.icarus2.util.lang.Primitives._boolean;
import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives._long;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.eval.BinaryOperations.AlgebraicOp;
import de.ims.icarus2.query.api.eval.BinaryOperations.BinaryDoubleOperation;
import de.ims.icarus2.query.api.eval.BinaryOperations.BinaryLongOperation;
import de.ims.icarus2.query.api.eval.BinaryOperations.BinaryNumericalPredicate;
import de.ims.icarus2.query.api.eval.BinaryOperations.BinaryObjectPredicate;
import de.ims.icarus2.query.api.eval.BinaryOperations.ComparableComparator;
import de.ims.icarus2.query.api.eval.BinaryOperations.EqualityPred;
import de.ims.icarus2.query.api.eval.BinaryOperations.NumericalComparator;
import de.ims.icarus2.query.api.eval.BinaryOperations.StringMode;
import de.ims.icarus2.query.api.eval.BinaryOperations.StringOp;
import de.ims.icarus2.query.api.eval.Expression.BooleanExpression;
import de.ims.icarus2.query.api.eval.Expression.NumericalExpression;
import de.ims.icarus2.query.api.eval.ExpressionTest.BooleanExpressionTest;
import de.ims.icarus2.query.api.eval.ExpressionTest.FloatingPointExpressionTest;
import de.ims.icarus2.query.api.eval.ExpressionTest.IntegerExpressionTest;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.test.util.Triple;
import de.ims.icarus2.util.MutablePrimitives.Primitive;

/**
 * @author Markus Gärtner
 *
 */
class BinaryOperationsTest {

	/** Number of randomly chosen test instances */
	private static final int RANDOM_INSTANCES = 10;

	private static class IntOpData {
		public final long left, right, result;
		IntOpData(long left, long right, long result) {
			this.left = left;
			this.right = right;
			this.result = result;
		}
	}

	private static class DoubleOpData {
		public final double left, right, result;
		DoubleOpData(double left, double right, double result) {
			this.left = left;
			this.right = right;
			this.result = result;
		}
	}

	private static class IntPredData {
		public final long left, right;
		public final boolean result;
		IntPredData(long left, long right, boolean result) {
			this.left = left;
			this.right = right;
			this.result = result;
		}
	}

	private static class DoublePredData {
		public final double left, right;
		public final boolean result;
		DoublePredData(double left, double right, boolean result) {
			this.left = left;
			this.right = right;
			this.result = result;
		}
	}

	private static class GenericPredData {
		public final Object left, right;
		public final boolean result;
		GenericPredData(Object left, Object right, boolean result) {
			this.left = requireNonNull(left);
			this.right = requireNonNull(right);
			this.result = result;
		}
	}

	private static long notZero(long v) {
		return v==0 ? 1 : v;
	}

	private static double notZero(double v) {
		return Double.compare(0.0, v)==0 ? 1.0 : v;
	}

	/**
	 * Test class for {@link de.ims.icarus2.query.api.eval.BinaryOperations#numericalOp(de.ims.icarus2.query.api.eval.BinaryOperations.AlgebraicOp, de.ims.icarus2.query.api.eval.Expression.NumericalExpression, de.ims.icarus2.query.api.eval.Expression.NumericalExpression)}.
	 */
	@Nested
	class ForNumericalOps {

		@TestFactory
		Stream<DynamicNode> testNonNumericalOperands() {
			List<Triple<Expression<?>, Expression<?>, String>> data = list(
					triple(Literals.of(true), Literals.of(1.0), "boolean+numeric"),
					triple(Literals.of("test"), Literals.of(1.0), "text+numeric"),
					triple(Literals.ofNull(), Literals.of(1.0), "null+numeric"),
					triple(ExpressionTestUtils.generic("dummy"), Literals.of(1.0), "generic+numeric"),

					triple(Literals.of(1.0), Literals.of(true), "numeric+boolean"),
					triple(Literals.of(1.0), Literals.of("test"), "numeric+text"),
					triple(Literals.of(1.0), Literals.ofNull(), "numeric+null"),
					triple(Literals.of(1.0), ExpressionTestUtils.generic("dummy"), "numeric+generic")
			);

			return data.stream().map(t -> dynamicTest(t.third, () -> {
				IcarusRuntimeException ex = assertIcarusException(QueryErrorCode.TYPE_MISMATCH,
						() -> BinaryOperations.numericalOp(AlgebraicOp.ADD, t.first, t.second));
				assertThat(ex).hasMessageContaining("numerical expression");
			}));
		}

		@TestFactory
		Stream<DynamicNode> testNullArgs() {
			return Stream.of(
					dynamicTest("null op", npeAsserter(() -> BinaryOperations.numericalOp(
							null, Literals.of(1), Literals.of(1)))),
					dynamicTest("null left", npeAsserter(() -> BinaryOperations.numericalOp(
							AlgebraicOp.ADD, null, Literals.of(1)))),
					dynamicTest("null right", npeAsserter(() -> BinaryOperations.numericalOp(
							AlgebraicOp.ADD, Literals.of(1), null)))
			);
		}

		@Nested
		class ForInteger implements IntegerExpressionTest {

			/** Use ADD as basic op for testing general behavior */
			@Override
			public NumericalExpression createWithValue(Primitive<? extends Number> value) {
				long result = value.longValue();
				long left = result-10;
				long right = 10;
				return create(AlgebraicOp.ADD, left, right);
			}

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return BinaryLongOperation.class; }

			private NumericalExpression create(AlgebraicOp op, long left, long right) {
				return BinaryOperations.numericalOp(op, Literals.of(left), Literals.of(right));
			}

			private IntOpData data(long left, long right, long result) {
				return new IntOpData(left, right, result);
			}

			private final long max = Long.MAX_VALUE;
			private final long min = Long.MIN_VALUE;
			private final long int_max = Integer.MAX_VALUE;

			/** Wrap op into test instance and verify result on original, duplicated and optimized */
			private DynamicNode assertIntOp(AlgebraicOp op, IntOpData data) {
				return dynamicTest(String.format("%s %s %s [= %s]",
						displayString(data.left), op, displayString(data.right), displayString(data.result)), () -> {
					NumericalExpression expression = create(op, data.left, data.right);
					assertThat(expression.isFPE()).isFalse();
					assertThat(expression.computeAsLong()).isEqualTo(data.result);
					assertThat(expression.computeAsDouble()).isEqualTo(data.result);
					assertThat(expression.compute().longValue()).isEqualTo(data.result);

					NumericalExpression duplicate = (NumericalExpression) expression.duplicate(mock(EvaluationContext.class));
					assertThat(duplicate.computeAsLong()).isEqualTo(data.result);
					assertThat(duplicate.computeAsDouble()).isEqualTo(data.result);
					assertThat(duplicate.compute().longValue()).isEqualTo(data.result);

					NumericalExpression optimized = (NumericalExpression) expression.optimize(mock(EvaluationContext.class));
					assertThat(optimized.computeAsLong()).isEqualTo(data.result);
					assertThat(optimized.computeAsDouble()).isEqualTo(data.result);
					assertThat(optimized.compute().longValue()).isEqualTo(data.result);
				});
			}

			// GENERAL ALGEBRAIC OPS

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testAdd(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(1, 2, 3),
						data(0, 0, 0),
						data(max, 0, max),
						data(0, min, min),
						data(-int_max, -int_max, -2*int_max),
						data(int_max, int_max, 2*int_max)
						),
						// Random test data
						rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
							long right = rng.nextLong();
							return data(left, right, left+right);
						})
				).map(data -> assertIntOp(AlgebraicOp.ADD, data));
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testSub(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(1, 2, -1),
						data(0, 0, 0),
						data(max, 0, max),
						data(0, min, min),
						data(0, max, -max),
						data(0, min, -min),
						data(-int_max, -int_max, 0),
						data(int_max, int_max, 0),
						data(int_max, -int_max, 2*int_max),
						data(-int_max, int_max, -2*int_max)
						),
						// Random test data
						rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
							long right = rng.nextLong();
							return data(left, right, left-right);
						})
				).map(data -> assertIntOp(AlgebraicOp.SUB, data));
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testMult(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(1, 2, 2),
						data(0, 0, 0),
						data(max, 0, 0),
						data(0, min, 0),
						data(max, 1, max),
						data(1, min, min),
						data(-int_max, -int_max, int_max*int_max),
						data(int_max, int_max, int_max*int_max)
						),
						// Random test data
						rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
							long right = rng.nextLong();
							return data(left, right, left*right);
						})
				).map(data -> assertIntOp(AlgebraicOp.MULT, data));
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testDiv(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(2, 1, 2),
						data(0, 1, 0),
						data(max, 1, max),
						data(0, min, 0),
						data(int_max, -int_max, -1),
						data(-int_max, int_max, -1),
						data(-int_max, -int_max, 1),
						data(int_max, int_max, 1)
						),
						// Random test data
						rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
							long right = notZero(rng.nextLong());
							return data(left, right, left/right);
						})
				).map(data -> assertIntOp(AlgebraicOp.DIV, data));
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testMod(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(1, 2, 1),
						data(0, 1, 0),
						data(99, 10, 9),
						data(40, 17, 6),
						data(max, 1, 0),
						data(max, max, 0),
						data(0, min, 0),
						data(-int_max, -int_max, 0),
						data(-int_max, int_max+1, -int_max),
						data(int_max+1, int_max-1, 2)
						),
						// Random test data
						rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
							long right = notZero(rng.nextLong());
							return data(left, right, left%right);
						})
				).map(data -> assertIntOp(AlgebraicOp.MOD, data));
			}

			// BITWISE OPS

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testBitAnd(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(1, 1, 1),
						data(0, 0, 0),
						data(0b00110111, 0b11001000, 0b00000000),
						data(0b00011111, 0b11111000, 0b00011000)
						),
						// Random test data
						rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
							long right = rng.nextLong();
							return data(left, right, left&right);
						})
				).map(data -> assertIntOp(AlgebraicOp.BIT_AND, data));
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testBitOr(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(1, 1, 1),
						data(0, 0, 0),
						data(1, 0, 1),
						data(0, 1, 1),
						data(0b00110111, 0b11001000, 0b11111111),
						data(0b00011111, 0b11111000, 0b11111111)
						),
						// Random test data
						rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
							long right = rng.nextLong();
							return data(left, right, left|right);
						})
				).map(data -> assertIntOp(AlgebraicOp.BIT_OR, data));
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testBitXor(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(1, 1, 0),
						data(0, 0, 0),
						data(1, 0, 1),
						data(0, 1, 1),
						data(0b00110111, 0b11001000, 0b11111111),
						data(0b00011111, 0b11111000, 0b11100111)
						),
						// Random test data
						rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
							long right = rng.nextLong();
							return data(left, right, left^right);
						})
				).map(data -> assertIntOp(AlgebraicOp.BIT_XOR, data));
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testBitLShift(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(0b00000000, 0, 0b00000000),
						data(0b00000001, 1, 0b00000010),
						data(0b00001111, 1, 0b00011110),
						data(0b00011000, 2, 0b01100000)
						),
						// Random test data
						rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
							long right = rng.nextLong();
							return data(left, right, left<<right);
						})
				).map(data -> assertIntOp(AlgebraicOp.LSHIFT, data));
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testBitRShift(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(0b00000000, 0, 0b00000000),
						data(0b00000001, 1, 0b00000000),
						data(0b00001111, 3, 0b00000001),
						data(0b01100000, 6, 0b00000001)
						),
						// Random test data
						rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
							long right = rng.nextLong();
							return data(left, right, left>>right);
						})
				).map(data -> assertIntOp(AlgebraicOp.RSHIFT, data));
			}
		}

		@Nested
		class ForFloatingPoint implements FloatingPointExpressionTest {

			/** Use ADD as basic op for testing general behavior */
			@Override
			public NumericalExpression createWithValue(Primitive<? extends Number> value) {
				double result = value.doubleValue();
				double left = result-10;
				double right = 10;
				return create(AlgebraicOp.ADD, left, right);
			}

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return BinaryDoubleOperation.class; }

			private NumericalExpression create(AlgebraicOp op, double left, double right) {
				return BinaryOperations.numericalOp(op, Literals.of(left), Literals.of(right));
			}

			private DoubleOpData data(double left, double right, double result) {
				return new DoubleOpData(left, right, result);
			}

			private final double max = Double.MAX_VALUE;
			private final double min = -Double.MAX_VALUE;
			private final double float_max = Float.MAX_VALUE;

			/** Wrap op into test instance and verify result on original, duplicated and optimized */
			private DynamicNode assertDoubleOp(AlgebraicOp op, DoubleOpData data) {
				return dynamicTest(String.format("%s %s %s [= %s]",
						displayString(data.left), op, displayString(data.right), displayString(data.result)), () -> {
					NumericalExpression expression = create(op, data.left, data.right);
					assertThat(expression.isFPE()).isTrue();
					assertThat(expression.computeAsDouble()).isEqualTo(data.result);
					assertThat(expression.compute().doubleValue()).isEqualTo(data.result);

					NumericalExpression duplicate = (NumericalExpression) expression.duplicate(mock(EvaluationContext.class));
					assertThat(duplicate.computeAsDouble()).isEqualTo(data.result);
					assertThat(duplicate.compute().doubleValue()).isEqualTo(data.result);

					NumericalExpression optimized = (NumericalExpression) expression.optimize(mock(EvaluationContext.class));
					assertThat(optimized.computeAsDouble()).isEqualTo(data.result);
					assertThat(optimized.compute().doubleValue()).isEqualTo(data.result);
				});
			}

			// GENERAL ALGEBRAIC OPS

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testAdd(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(1, 2, 3),
						data(0, 0, 0),
						data(max, 0, max),
						data(0, min, min),
						data(-float_max, -float_max, -2*float_max),
						data(float_max, float_max, 2*float_max)
						),
						// Random test data
						rng.doubles(RANDOM_INSTANCES).mapToObj(left -> {
							double right = rng.nextDouble();
							return data(left, right, left+right);
						})
				).map(data -> assertDoubleOp(AlgebraicOp.ADD, data));
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testSub(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(1, 2, -1),
						data(0, 0, 0),
						data(max, 0, max),
						data(0, min, max),
						data(0, max, -max),
						data(0, min, -min),
						data(-float_max, -float_max, 0),
						data(float_max, float_max, 0),
						data(float_max, -float_max, 2*float_max),
						data(-float_max, float_max, -2*float_max)
						),
						// Random test data
						rng.doubles(RANDOM_INSTANCES).mapToObj(left -> {
							double right = rng.nextDouble();
							return data(left, right, left-right);
						})
				).map(data -> assertDoubleOp(AlgebraicOp.SUB, data));
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testMult(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(1, 2, 2),
						data(0, 0, 0),
						data(max, 0, 0),
						data(0, min, -0.0),
						data(max, 1, max),
						data(1, min, min),
						data(-float_max, -float_max, float_max*float_max),
						data(float_max, float_max, float_max*float_max)
						),
						// Random test data
						rng.doubles(RANDOM_INSTANCES).mapToObj(left -> {
							double right = rng.nextDouble();
							return data(left, right, left*right);
						})
				).map(data -> assertDoubleOp(AlgebraicOp.MULT, data));
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testDiv(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(2, 1, 2),
						data(0, 1, 0),
						data(max, 1, max),
						data(0, min, -0.0),
						data(float_max, -float_max, -1),
						data(-float_max, float_max, -1),
						data(-float_max, -float_max, 1),
						data(float_max, float_max, 1)
						),
						// Random test data
						rng.doubles(RANDOM_INSTANCES).mapToObj(left -> {
							double right = notZero(rng.nextDouble());
							return data(left, right, left/right);
						})
				).map(data -> assertDoubleOp(AlgebraicOp.DIV, data));
			}

			// BITWISE OPS

			@TestFactory
			Stream<DynamicNode> testUnsupportedOPs() {
				return Stream.of(
						// IQL does not define modulo for floating point, but Java does, so ignore it
						AlgebraicOp.MOD,
						// All bitwise operations are not supported for floating point types
						AlgebraicOp.BIT_AND,
						AlgebraicOp.BIT_OR,
						AlgebraicOp.BIT_XOR,
						AlgebraicOp.LSHIFT,
						AlgebraicOp.RSHIFT
				).map(op -> dynamicTest(op.name(), () -> {
					IcarusRuntimeException ex = assertIcarusException(QueryErrorCode.TYPE_MISMATCH,
							() -> create(op, 1.0, 1.0));
					assertThat(ex).hasMessageContaining("does not support floating point");
				}));
			}
		}
	}

	/**
	 * Test class for {@link de.ims.icarus2.query.api.eval.BinaryOperations#numericalPred(de.ims.icarus2.query.api.eval.BinaryOperations.NumericalComparator, de.ims.icarus2.query.api.eval.Expression.NumericalExpression, de.ims.icarus2.query.api.eval.Expression.NumericalExpression)}.
	 */
	@Nested
	class ForNumericalPreds {

		@TestFactory
		Stream<DynamicNode> testNonNumericalOperands() {
			List<Triple<Expression<?>, Expression<?>, String>> data = list(
					triple(Literals.of(true), Literals.of(1.0), "boolean+numeric"),
					triple(Literals.of("test"), Literals.of(1.0), "text+numeric"),
					triple(Literals.ofNull(), Literals.of(1.0), "null+numeric"),
					triple(ExpressionTestUtils.generic("dummy"), Literals.of(1.0), "generic+numeric"),

					triple(Literals.of(1.0), Literals.of(true), "numeric+boolean"),
					triple(Literals.of(1.0), Literals.of("test"), "numeric+text"),
					triple(Literals.of(1.0), Literals.ofNull(), "numeric+null"),
					triple(Literals.of(1.0), ExpressionTestUtils.generic("dummy"), "numeric+generic")
			);

			return data.stream().map(t -> dynamicTest(t.third, () -> {
				IcarusRuntimeException ex = assertIcarusException(QueryErrorCode.TYPE_MISMATCH,
						() -> BinaryOperations.numericalPred(NumericalComparator.EQUALS, t.first, t.second));
				assertThat(ex).hasMessageContaining("numerical expression");
			}));
		}

		@TestFactory
		Stream<DynamicNode> testNullArgs() {
			return Stream.of(
					dynamicTest("null pred", npeAsserter(() -> BinaryOperations.numericalPred(
							null, Literals.of(1), Literals.of(1)))),
					dynamicTest("null left", npeAsserter(() -> BinaryOperations.numericalPred(
							NumericalComparator.EQUALS, null, Literals.of(1)))),
					dynamicTest("null right", npeAsserter(() -> BinaryOperations.numericalPred(
							NumericalComparator.EQUALS, Literals.of(1), null)))
			);
		}

		@Nested
		class ForInteger implements BooleanExpressionTest {

			/** Use EQUALS as basic op for testing general behavior */
			@Override
			public BooleanExpression createWithValue(Primitive<Boolean> value) {
				long left = 10;
				long right = value.booleanValue() ? 10 : 11;
				return create(NumericalComparator.EQUALS, left, right);
			}

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return BinaryNumericalPredicate.class; }

			private BooleanExpression create(NumericalComparator pred, long left, long right) {
				return BinaryOperations.numericalPred(pred, Literals.of(left), Literals.of(right));
			}

			private IntPredData data(long left, long right, boolean result) {
				return new IntPredData(left, right, result);
			}

			private final long max = Long.MAX_VALUE;
			private final long min = Long.MIN_VALUE;
			private final long int_max = Integer.MAX_VALUE;

			/** Wrap op into test instance and verify result on original, duplicated and optimized */
			private DynamicNode assertIntPred(NumericalComparator pred, IntPredData data) {
				return dynamicTest(String.format("%s %s %s [= %b]",
						displayString(data.left), pred, displayString(data.right), _boolean(data.result)), () -> {
					BooleanExpression expression = create(pred, data.left, data.right);
					assertThat(expression.computeAsBoolean()).isEqualTo(data.result);
					assertThat(expression.compute().booleanValue()).isEqualTo(data.result);

					BooleanExpression duplicate = (BooleanExpression) expression.duplicate(mock(EvaluationContext.class));
					assertThat(duplicate.computeAsBoolean()).isEqualTo(data.result);
					assertThat(duplicate.compute().booleanValue()).isEqualTo(data.result);

					BooleanExpression optimized = (BooleanExpression) expression.optimize(mock(EvaluationContext.class));
					assertThat(optimized.computeAsBoolean()).isEqualTo(data.result);
					assertThat(optimized.compute().booleanValue()).isEqualTo(data.result);
				});
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testEquals(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(0, 0, true),
						data(1, 2, false),
						data(max, 0, false),
						data(0, min, false),
						data(min, max, false),
						data(-int_max, -int_max, true),
						data(int_max, int_max, true)
						),
						// Random test data
						rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
							long right = rng.nextLong();
							return data(left, right, left==right);
						})
				).map(data -> assertIntPred(NumericalComparator.EQUALS, data));
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testNotEquals(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(0, 0, false),
						data(1, 2, true),
						data(max, 0, true),
						data(0, min, true),
						data(min, max, true),
						data(-int_max, -int_max, false),
						data(int_max, int_max, false)
						),
						// Random test data
						rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
							long right = rng.nextLong();
							return data(left, right, left!=right);
						})
				).map(data -> assertIntPred(NumericalComparator.NOT_EQUALS, data));
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testLess(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(0, 0, false),
						data(1, 2, true),
						data(2, 1, false),
						data(max, 0, false),
						data(0, min, false),
						data(min, 0, true),
						data(min, max, true),
						data(-int_max, -int_max, false),
						data(int_max, int_max, false)
						),
						// Random test data
						rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
							long right = rng.nextLong();
							return data(left, right, left<right);
						})
				).map(data -> assertIntPred(NumericalComparator.LESS, data));
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testLessOrEqual(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(0, 0, true),
						data(1, 2, true),
						data(2, 1, false),
						data(max, 0, false),
						data(0, min, false),
						data(min, 0, true),
						data(min, max, true),
						data(-int_max, -int_max, true),
						data(int_max, int_max, true)
						),
						// Random test data
						rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
							long right = rng.nextLong();
							return data(left, right, left<=right);
						})
				).map(data -> assertIntPred(NumericalComparator.LESS_OR_EQUAL, data));
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testGreater(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(0, 0, false),
						data(1, 2, false),
						data(2, 1, true),
						data(max, 0, true),
						data(0, min, true),
						data(min, 0, false),
						data(min, max, false),
						data(-int_max, -int_max, false),
						data(int_max, int_max, false)
						),
						// Random test data
						rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
							long right = rng.nextLong();
							return data(left, right, left>right);
						})
				).map(data -> assertIntPred(NumericalComparator.GREATER, data));
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testGreaterOrEqual(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(0, 0, true),
						data(1, 2, false),
						data(2, 1, true),
						data(max, 0, true),
						data(0, min, true),
						data(min, 0, false),
						data(min, max, false),
						data(-int_max, -int_max, true),
						data(int_max, int_max, true)
						),
						// Random test data
						rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
							long right = rng.nextLong();
							return data(left, right, left>=right);
						})
				).map(data -> assertIntPred(NumericalComparator.GREATER_OR_EQUAL, data));
			}
		}

		@Nested
		class ForFloatingPoint implements BooleanExpressionTest {

			/** Use EQUALS as basic op for testing general behavior */
			@Override
			public BooleanExpression createWithValue(Primitive<Boolean> value) {
				double left = 10.5;
				double right = value.booleanValue() ? 10.5 : 10.8;
				return create(NumericalComparator.EQUALS, left, right);
			}

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return BinaryNumericalPredicate.class; }

			private BooleanExpression create(NumericalComparator pred, double left, double right) {
				return BinaryOperations.numericalPred(pred, Literals.of(left), Literals.of(right));
			}

			private DoublePredData data(double left, double right, boolean result) {
				return new DoublePredData(left, right, result);
			}

			private final double max = Double.MAX_VALUE;
			private final double min = -Double.MAX_VALUE;
			private final double float_max = Float.MAX_VALUE;

			/** Wrap op into test instance and verify result on original, duplicated and optimized */
			private DynamicNode assertDoublePred(NumericalComparator pred, DoublePredData data) {
				return dynamicTest(String.format("%s %s %s [= %b]",
						displayString(data.left), pred, displayString(data.right), _boolean(data.result)), () -> {
					BooleanExpression expression = create(pred, data.left, data.right);
					assertThat(expression.computeAsBoolean()).isEqualTo(data.result);
					assertThat(expression.compute().booleanValue()).isEqualTo(data.result);

					BooleanExpression duplicate = (BooleanExpression) expression.duplicate(mock(EvaluationContext.class));
					assertThat(duplicate.computeAsBoolean()).isEqualTo(data.result);
					assertThat(duplicate.compute().booleanValue()).isEqualTo(data.result);

					BooleanExpression optimized = (BooleanExpression) expression.optimize(mock(EvaluationContext.class));
					assertThat(optimized.computeAsBoolean()).isEqualTo(data.result);
					assertThat(optimized.compute().booleanValue()).isEqualTo(data.result);
				});
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testEquals(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(0.0, 0.0, true),
						data(0.1, 0.2, false),
						data(max, 0.0, false),
						data(0.0, min, false),
						data(min, max, false),
						data(-float_max, -float_max, true),
						data(float_max, float_max, true)
						),
						// Random test data
						rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
							double right = rng.nextDouble();
							return data(left, right, Double.compare(left, right)==0);
						})
				).map(data -> assertDoublePred(NumericalComparator.EQUALS, data));
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testNotEquals(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(0.0, 0.0, false),
						data(0.1, 0.2, true),
						data(max, 0.0, true),
						data(0.0, min, true),
						data(min, max, true),
						data(-float_max, -float_max, false),
						data(float_max, float_max, false)
						),
						// Random test data
						rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
							double right = rng.nextDouble();
							return data(left, right, Double.compare(left, right)!=0);
						})
				).map(data -> assertDoublePred(NumericalComparator.NOT_EQUALS, data));
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testLess(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(0.0, 0.0, false),
						data(0.1, 0.2, true),
						data(0.2, 0.1, false),
						data(max, 0.0, false),
						data(0.0, min, false),
						data(min, 0.0, true),
						data(min, max, true),
						data(-float_max, -float_max, false),
						data(float_max, float_max, false)
						),
						// Random test data
						rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
							double right = rng.nextDouble();
							return data(left, right, Double.compare(left, right)<0);
						})
				).map(data -> assertDoublePred(NumericalComparator.LESS, data));
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testLessOrEqual(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(0.0, 0.0, true),
						data(0.1, 0.2, true),
						data(0.2, 0.1, false),
						data(max, 0.0, false),
						data(0.0, min, false),
						data(min, 0.0, true),
						data(min, max, true),
						data(-float_max, -float_max, true),
						data(float_max, float_max, true)
						),
						// Random test data
						rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
							double right = rng.nextDouble();
							return data(left, right, Double.compare(left, right)<=0);
						})
				).map(data -> assertDoublePred(NumericalComparator.LESS_OR_EQUAL, data));
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testGreater(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(0.0, 0.0, false),
						data(0.1, 0.2, false),
						data(0.2, 0.1, true),
						data(max, 0.0, true),
						data(0.0, min, true),
						data(min, 0.0, false),
						data(min, max, false),
						data(-float_max, -float_max, false),
						data(float_max, float_max, false)
						),
						// Random test data
						rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
							double right = rng.nextDouble();
							return data(left, right, Double.compare(left, right)>0);
						})
				).map(data -> assertDoublePred(NumericalComparator.GREATER, data));
			}

			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testGreaterOrEqual(RandomGenerator rng) {
				return Stream.concat(
						// Static test data
						Stream.of(
						data(0.0, 0.0, true),
						data(0.1, 0.2, false),
						data(0.2, 0.1, true),
						data(max, 0.0, true),
						data(0.0, min, true),
						data(min, 0.0, false),
						data(min, max, false),
						data(-float_max, -float_max, true),
						data(float_max, float_max, true)
						),
						// Random test data
						rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
							double right = rng.nextDouble();
							return data(left, right, Double.compare(left, right)>=0);
						})
				).map(data -> assertDoublePred(NumericalComparator.GREATER_OR_EQUAL, data));
			}
		}

	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.BinaryOperations#comparablePred(de.ims.icarus2.query.api.eval.BinaryOperations.ComparableComparator, de.ims.icarus2.query.api.eval.Expression, de.ims.icarus2.query.api.eval.Expression)}.
	 */
	@Nested
	class ForComparablePreds implements BooleanExpressionTest {

		@TestFactory
		Stream<DynamicNode> testNonComparableOperands() {
			List<Triple<Expression<?>, Expression<?>, String>> data = list(
					triple(Literals.of(1), ExpressionTestUtils.raw("comp"), "numeric+comparable"),
					triple(Literals.of(true), ExpressionTestUtils.raw("comp"), "boolean+comparable"),
					triple(Literals.of("test"), ExpressionTestUtils.raw("comp"), "text+comparable"),
					triple(Literals.ofNull(), ExpressionTestUtils.raw("comp"), "null+comparable"),
					triple(ExpressionTestUtils.generic("dummy"), ExpressionTestUtils.raw("comp"), "generic+comparable"),

					triple(ExpressionTestUtils.raw("comp"), Literals.of(1), "comparable+numeric"),
					triple(ExpressionTestUtils.raw("comp"), Literals.of(true), "comparable+boolean"),
					triple(ExpressionTestUtils.raw("comp"), Literals.of("test"), "comparable+text"),
					triple(ExpressionTestUtils.raw("comp"), Literals.ofNull(), "comparable+null"),
					triple(ExpressionTestUtils.raw("comp"), ExpressionTestUtils.generic("dummy"), "comparable+generic")
			);

			return data.stream().map(t -> dynamicTest(t.third, () -> {
				IcarusRuntimeException ex = assertIcarusException(QueryErrorCode.TYPE_MISMATCH,
						() -> BinaryOperations.comparablePred(ComparableComparator.EQUALS, t.first, t.second));
				assertThat(ex).hasMessageContaining("java.lang.Comparable");
			}));
		}

		@TestFactory
		Stream<DynamicNode> testNullArgs() {
			return Stream.of(
					dynamicTest("null pred", npeAsserter(() -> BinaryOperations.comparablePred(
							null, ExpressionTestUtils.raw("comp"), ExpressionTestUtils.raw("comp")))),
					dynamicTest("null left", npeAsserter(() -> BinaryOperations.comparablePred(
							ComparableComparator.EQUALS, null, ExpressionTestUtils.raw("comp")))),
					dynamicTest("null right", npeAsserter(() -> BinaryOperations.comparablePred(
							ComparableComparator.EQUALS, ExpressionTestUtils.raw("comp"), null)))
			);
		}

		/** Use EQUALS as basic op for testing general behavior */
		@Override
		public BooleanExpression createWithValue(Primitive<Boolean> value) {
			String left = "test1";
			String right = value.booleanValue() ? "test1" : "test234";
			return create(ComparableComparator.EQUALS, left, right);
		}

		@Override
		public boolean nativeConstant() { return false; }

		@Override
		public Class<?> getTestTargetClass() { return BinaryObjectPredicate.class; }

		@Override
		public boolean optimizeToConstant() { return false; }

		private BooleanExpression create(ComparableComparator pred, Object left, Object right) {
			return BinaryOperations.comparablePred(pred, ExpressionTestUtils.raw(left), ExpressionTestUtils.raw(right));
		}

		private GenericPredData data(Object left, Object right, boolean result) {
			return new GenericPredData(left, right, result);
		}

		/** Wrap op into test instance and verify result on original, duplicated and optimized */
		private DynamicNode assertComparablePred(ComparableComparator pred, GenericPredData data) {
			return dynamicTest(String.format("%s %s %s [= %b]",
					data.left, pred, data.right, _boolean(data.result)), () -> {
				BooleanExpression expression = create(pred, data.left, data.right);
				assertThat(expression.computeAsBoolean()).isEqualTo(data.result);
				assertThat(expression.compute().booleanValue()).isEqualTo(data.result);

				BooleanExpression duplicate = (BooleanExpression) expression.duplicate(mock(EvaluationContext.class));
				assertThat(duplicate.computeAsBoolean()).isEqualTo(data.result);
				assertThat(duplicate.compute().booleanValue()).isEqualTo(data.result);

				BooleanExpression optimized = (BooleanExpression) expression.optimize(mock(EvaluationContext.class));
				assertThat(optimized.computeAsBoolean()).isEqualTo(data.result);
				assertThat(optimized.compute().booleanValue()).isEqualTo(data.result);
			});
		}

		@TestFactory
		Stream<DynamicNode> testUncomparableArgs() {
			return Stream.of(
					triple("test", _int(0), "string vs. int"),
					triple(_int(100), "test", "int vs. string"),
					triple(new BigDecimal(Integer.MAX_VALUE), "test", "bigInt vs. string"),
					triple("test", new BigDecimal(Integer.MAX_VALUE), "string vs. bigInt")
			).map(data -> dynamicTest(data.third, () -> {
				assertThrows(RuntimeException.class,
						() -> create(ComparableComparator.LESS, data.first, data.second).compute());
			}));
		}

		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testEquals(RandomGenerator rng) {
			return Stream.concat(
					// Static test data
					Stream.of(
					data("test", "test", true),
					data("test", "test2", false),
					data(Boolean.FALSE, Boolean.TRUE, false),
					data(Boolean.TRUE, Boolean.TRUE, true),
					data(_int(0), _int(100), false)
					),
					// Random test data
					rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
						long right = rng.nextLong();
						return data(_long(left), _long(right), left==right);
					})
			).map(data -> assertComparablePred(ComparableComparator.EQUALS, data));
		}

		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testNotEquals(RandomGenerator rng) {
			return Stream.concat(
					// Static test data
					Stream.of(
					data("test", "test", false),
					data("test", "test2", true),
					data(Boolean.FALSE, Boolean.TRUE, true),
					data(Boolean.TRUE, Boolean.TRUE, false),
					data(_int(0), _int(100), true)
					),
					// Random test data
					rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
						long right = rng.nextLong();
						return data(_long(left), _long(right), left!=right);
					})
			).map(data -> assertComparablePred(ComparableComparator.NOT_EQUALS, data));
		}

		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testLess(RandomGenerator rng) {
			return Stream.concat(
					// Static test data
					Stream.of(
					data("test", "test", false),
					data("test", "test2", true),
					data(Boolean.FALSE, Boolean.TRUE, true),
					data(Boolean.TRUE, Boolean.FALSE, false),
					data(_int(0), _int(100), true)
					),
					// Random test data
					rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
						long right = rng.nextLong();
						return data(_long(left), _long(right), left<right);
					})
			).map(data -> assertComparablePred(ComparableComparator.LESS, data));
		}

		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testLessOrEqual(RandomGenerator rng) {
			return Stream.concat(
					// Static test data
					Stream.of(
					data("test", "test", true),
					data("test", "test2", true),
					data("test2", "test", false),
					data(Boolean.FALSE, Boolean.TRUE, true),
					data(Boolean.TRUE, Boolean.FALSE, false),
					data(Boolean.FALSE, Boolean.FALSE, true),
					data(Boolean.TRUE, Boolean.TRUE, true),
					data(_int(0), _int(100), true),
					data(_int(100), _int(100), true)
					),
					// Random test data
					rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
						long right = rng.nextLong();
						return data(_long(left), _long(right), left<=right);
					})
			).map(data -> assertComparablePred(ComparableComparator.LESS_OR_EQUAL, data));
		}

		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testGreater(RandomGenerator rng) {
			return Stream.concat(
					// Static test data
					Stream.of(
					data("test", "test", false),
					data("test", "test2", false),
					data("test2", "test", true),
					data(Boolean.FALSE, Boolean.TRUE, false),
					data(Boolean.TRUE, Boolean.FALSE, true),
					data(_int(0), _int(100), false),
					data(_int(100), _int(0), true)
					),
					// Random test data
					rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
						long right = rng.nextLong();
						return data(_long(left), _long(right), left>right);
					})
			).map(data -> assertComparablePred(ComparableComparator.GREATER, data));
		}

		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testGreaterOrEqual(RandomGenerator rng) {
			return Stream.concat(
					// Static test data
					Stream.of(
					data("test", "test", true),
					data("test", "test2", false),
					data("test2", "test", true),
					data(Boolean.FALSE, Boolean.TRUE, false),
					data(Boolean.TRUE, Boolean.FALSE, true),
					data(Boolean.FALSE, Boolean.FALSE, true),
					data(Boolean.TRUE, Boolean.TRUE, true),
					data(_int(0), _int(100), false),
					data(_int(100), _int(0), true),
					data(_int(100), _int(100), true)
					),
					// Random test data
					rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
						long right = rng.nextLong();
						return data(_long(left), _long(right), left>=right);
					})
			).map(data -> assertComparablePred(ComparableComparator.GREATER_OR_EQUAL, data));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.BinaryOperations#equalityPred(de.ims.icarus2.query.api.eval.BinaryOperations.EqualityPred, de.ims.icarus2.query.api.eval.Expression, de.ims.icarus2.query.api.eval.Expression)}.
	 */
	@Nested
	class ForEqualityPreds implements BooleanExpressionTest {

		@TestFactory
		Stream<DynamicNode> testNullArgs() {
			return Stream.of(
					dynamicTest("null pred", npeAsserter(() -> BinaryOperations.equalityPred(
							null, ExpressionTestUtils.raw("comp"), ExpressionTestUtils.raw("comp")))),
					dynamicTest("null left", npeAsserter(() -> BinaryOperations.equalityPred(
							EqualityPred.EQUALS, null, ExpressionTestUtils.raw("comp")))),
					dynamicTest("null right", npeAsserter(() -> BinaryOperations.equalityPred(
							EqualityPred.EQUALS, ExpressionTestUtils.raw("comp"), null)))
			);
		}

		/** Use EQUALS as basic op for testing general behavior */
		@Override
		public BooleanExpression createWithValue(Primitive<Boolean> value) {
			String left = "test1";
			String right = value.booleanValue() ? "test1" : "test234";
			return create(EqualityPred.EQUALS, left, right);
		}

		@Override
		public boolean nativeConstant() { return false; }

		@Override
		public Class<?> getTestTargetClass() { return BinaryObjectPredicate.class; }

		@Override
		public boolean optimizeToConstant() { return false; }

		private BooleanExpression create(EqualityPred pred, Object left, Object right) {
			return BinaryOperations.equalityPred(pred, ExpressionTestUtils.raw(left), ExpressionTestUtils.raw(right));
		}

		private GenericPredData data(Object left, Object right, boolean result) {
			return new GenericPredData(left, right, result);
		}

		/** Wrap op into test instance and verify result on original, duplicated and optimized */
		private DynamicNode assertEqualityPred(EqualityPred pred, GenericPredData data) {
			return dynamicTest(String.format("%s %s %s [= %b]",
					data.left, pred, data.right, _boolean(data.result)), () -> {
				BooleanExpression expression = create(pred, data.left, data.right);
				assertThat(expression.computeAsBoolean()).isEqualTo(data.result);
				assertThat(expression.compute().booleanValue()).isEqualTo(data.result);

				BooleanExpression duplicate = (BooleanExpression) expression.duplicate(mock(EvaluationContext.class));
				assertThat(duplicate.computeAsBoolean()).isEqualTo(data.result);
				assertThat(duplicate.compute().booleanValue()).isEqualTo(data.result);

				BooleanExpression optimized = (BooleanExpression) expression.optimize(mock(EvaluationContext.class));
				assertThat(optimized.computeAsBoolean()).isEqualTo(data.result);
				assertThat(optimized.compute().booleanValue()).isEqualTo(data.result);
			});
		}

		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testEquals(RandomGenerator rng) {
			Object dummy = new Object();
			return Stream.concat(
					// Static test data
					Stream.of(
					data("test", "test", true),
					data("test", "test2", false),
					data(Boolean.FALSE, Boolean.TRUE, false),
					data(Boolean.TRUE, Boolean.TRUE, true),
					data(Boolean.FALSE, Boolean.FALSE, true),
					data(dummy, dummy, true),
					data(dummy, new Object(), false),
					data(dummy, "test", false),
					data("test", dummy, false),
					data(_int(0), _int(100), false),
					data(_int(100), _int(100), true)
					),
					// Random test data
					rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
						long right = rng.nextLong();
						return data(_long(left), _long(right), left==right);
					})
			).map(data -> assertEqualityPred(EqualityPred.EQUALS, data));
		}

		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testNotEquals(RandomGenerator rng) {
			Object dummy = new Object();
			return Stream.concat(
					// Static test data
					Stream.of(
					data("test", "test", false),
					data("test", "test2", true),
					data(Boolean.FALSE, Boolean.TRUE, true),
					data(Boolean.TRUE, Boolean.TRUE, false),
					data(Boolean.FALSE, Boolean.FALSE, false),
					data(dummy, dummy, false),
					data(dummy, new Object(), true),
					data(dummy, "test", true),
					data("test", dummy, true),
					data(_int(0), _int(100), true),
					data(_int(100), _int(100), false)
					),
					// Random test data
					rng.longs(RANDOM_INSTANCES).mapToObj(left -> {
						long right = rng.nextLong();
						return data(_long(left), _long(right), left!=right);
					})
			).map(data -> assertEqualityPred(EqualityPred.NOT_EQUALS, data));
		}

	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.BinaryOperations#unicodeOp(de.ims.icarus2.query.api.eval.BinaryOperations.StringOp, de.ims.icarus2.query.api.eval.BinaryOperations.StringMode, de.ims.icarus2.query.api.eval.Expression.TextExpression, de.ims.icarus2.query.api.eval.Expression.TextExpression)}.
	 */
	@Nested
	class ForUnicodeOps {

		@TestFactory
		Stream<DynamicNode> testNonTextualOperands() {
			List<Triple<Expression<?>, Expression<?>, String>> data = list(
					triple(Literals.of(true), Literals.of("text"), "boolean+text"),
					triple(Literals.of(1), Literals.of("text"), "numeric+text"),
					triple(Literals.ofNull(), Literals.of("text"), "null+text"),
					triple(ExpressionTestUtils.generic("dummy"), Literals.of("text"), "generic+text"),

					triple(Literals.of("text"), Literals.of(true), "text+boolean"),
					triple(Literals.of("text"), Literals.of(1), "text+numeric"),
					triple(Literals.of("text"), Literals.ofNull(), "text+null"),
					triple(Literals.of("text"), ExpressionTestUtils.generic("dummy"), "text+generic")
			);

			return data.stream().map(t -> dynamicTest(t.third, () -> {
				IcarusRuntimeException ex = assertIcarusException(QueryErrorCode.TYPE_MISMATCH,
						() -> BinaryOperations.unicodeOp(StringOp.EQUALS, StringMode.DEFAULT, t.first, t.second));
				assertThat(ex).hasMessageContaining("text expression");
			}));
		}

		@TestFactory
		Stream<DynamicNode> testNullArgs() {
			return Stream.of(
					dynamicTest("null op", npeAsserter(() -> BinaryOperations.unicodeOp(null, StringMode.DEFAULT, Literals.of("test1"), Literals.of("test2")))),
					dynamicTest("null mode", npeAsserter(() -> BinaryOperations.unicodeOp(StringOp.EQUALS, null, Literals.of("test1"), Literals.of("test2")))),
					dynamicTest("null left", npeAsserter(() -> BinaryOperations.unicodeOp(StringOp.EQUALS, StringMode.DEFAULT, null, Literals.of("test2")))),
					dynamicTest("null right", npeAsserter(() -> BinaryOperations.unicodeOp(StringOp.EQUALS, StringMode.DEFAULT, Literals.of("test1"), null)))
			);
		}

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
				"TEST, tesT, true",
				"the first one, is longer, false",
				"this time, the second one is far longer, false",
			})
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
				//TODO add search for surrogate symbols that ignores codepoints
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
				//TODO add search for surrogate symbols that ignores codepoints
			})
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

		@TestFactory
		Stream<DynamicNode> testNonTextualOperands() {
			List<Triple<Expression<?>, Expression<?>, String>> data = list(
					triple(Literals.of(true), Literals.of("text"), "boolean+text"),
					triple(Literals.of(1), Literals.of("text"), "numeric+text"),
					triple(Literals.ofNull(), Literals.of("text"), "null+text"),
					triple(ExpressionTestUtils.generic("dummy"), Literals.of("text"), "generic+text"),

					triple(Literals.of("text"), Literals.of(true), "text+boolean"),
					triple(Literals.of("text"), Literals.of(1), "text+numeric"),
					triple(Literals.of("text"), Literals.ofNull(), "text+null"),
					triple(Literals.of("text"), ExpressionTestUtils.generic("dummy"), "text+generic")
			);

			return data.stream().map(t -> dynamicTest(t.third, () -> {
				IcarusRuntimeException ex = assertIcarusException(QueryErrorCode.TYPE_MISMATCH,
						() -> BinaryOperations.asciiOp(StringOp.EQUALS, StringMode.DEFAULT, t.first, t.second));
				assertThat(ex).hasMessageContaining("text expression");
			}));
		}

		@TestFactory
		Stream<DynamicNode> testNullArgs() {
			return Stream.of(
					dynamicTest("null op", npeAsserter(() -> BinaryOperations.asciiOp(null, StringMode.DEFAULT, Literals.of("test1"), Literals.of("test2")))),
					dynamicTest("null mode", npeAsserter(() -> BinaryOperations.asciiOp(StringOp.EQUALS, null, Literals.of("test1"), Literals.of("test2")))),
					dynamicTest("null left", npeAsserter(() -> BinaryOperations.asciiOp(StringOp.EQUALS, StringMode.DEFAULT, null, Literals.of("test2")))),
					dynamicTest("null right", npeAsserter(() -> BinaryOperations.asciiOp(StringOp.EQUALS, StringMode.DEFAULT, Literals.of("test1"), null)))
			);
		}

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
				"TEST, tesT, true",
				"the first one, is longer, false",
				"this time, the second one is far longer, false",
			})
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
				//TODO add search for surrogate symbols that ignores codepoints
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
				//TODO add search for surrogate symbols that ignores codepoints
			})
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
			void testIgnoreCase(String left, String right, boolean equals) {
				assertAsciiOp(op, StringMode.IGNORE_CASE, left, right, equals);
			}
		}
	}

}
