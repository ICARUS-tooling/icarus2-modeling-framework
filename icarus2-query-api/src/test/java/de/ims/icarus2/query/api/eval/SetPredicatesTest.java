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

import static de.ims.icarus2.query.api.eval.ExpressionTestUtils.dynamic;
import static de.ims.icarus2.query.api.eval.ExpressionTestUtils.dynamicLongs;
import static de.ims.icarus2.query.api.eval.ExpressionTestUtils.optimizable;
import static de.ims.icarus2.query.api.eval.ExpressionTestUtils.optimizableLongs;
import static de.ims.icarus2.test.TestUtils.displayString;
import static de.ims.icarus2.util.lang.Primitives._boolean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.DoubleStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.query.api.eval.Expression.BooleanExpression;
import de.ims.icarus2.query.api.eval.Expression.NumericalExpression;
import de.ims.icarus2.query.api.eval.Expression.TextExpression;
import de.ims.icarus2.query.api.eval.ExpressionTest.BooleanExpressionTest;
import de.ims.icarus2.query.api.eval.SetPredicates.FlatFloatingPointSetPredicate;
import de.ims.icarus2.query.api.eval.SetPredicates.FlatIntegerSetPredicate;
import de.ims.icarus2.query.api.eval.SetPredicates.FlatTextSetPredicate;
import de.ims.icarus2.util.Mutable;
import de.ims.icarus2.util.Mutable.MutableObject;
import de.ims.icarus2.util.MutablePrimitives.MutableDouble;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.MutablePrimitives.Primitive;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.longs.LongSet;

/**
 * @author Markus Gärtner
 *
 */
class SetPredicatesTest {

	@Nested
	class ForFlatTarget {

		@Nested
		class ForInteger implements BooleanExpressionTest {

			@Override
			public BooleanExpression createWithValue(Primitive<Boolean> value) {
				Expression<?>[] elements = {
					Literals.of(123),
					Literals.of(234),
					Literals.of(345),
					Literals.of(456),
					Literals.of(567),
					Literals.of(678),
				};

				NumericalExpression query = value.booleanValue() ?
						Literals.of(234) : Literals.of(-1234);

				return SetPredicates.in(query, elements);
			}

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return FlatIntegerSetPredicate.class; }

			private class FixedData {
				final long target;
				final long[] set;
				final boolean result;
				FixedData(long target, boolean result, long...set) {
					this.target = target;
					this.result = result;
					this.set = set;
				}
			}

			FixedData data(long target, boolean result, long...set) {
				return new FixedData(target, result, set);
			}

			private void assertSet(Expression<?> target, Expression<?>[] elements, boolean result) {
				BooleanExpression pred = SetPredicates.in(target, elements);
				assertThat(pred.computeAsBoolean()).isEqualTo(result);

				BooleanExpression duplicate = (BooleanExpression) pred.duplicate(mock(EvaluationContext.class));
				assertThat(duplicate.computeAsBoolean()).isEqualTo(result);

				BooleanExpression optimized = (BooleanExpression) pred.optimize(mock(EvaluationContext.class));
				assertThat(optimized.computeAsBoolean()).isEqualTo(result);
			}

			@TestFactory
			Stream<DynamicNode> testStaticOnlyNoExpand() {
				return Stream.of(
						data(1, true, 1),
						data(1, true, 1, 2, 3, 4),
						data(1, false, 2),
						data(1, false, -1, 2, 3, 4)
				).map(data -> dynamicTest(String.format("%s in %s -> %b",
						displayString(data.target), Arrays.toString(data.set), _boolean(data.result)), () -> {
							assertSet(Literals.of(data.target), LongStream.of(data.set)
									.mapToObj(Literals::of)
									.toArray(NumericalExpression[]::new), data.result);
				}));
			}

			@Test
			void testStaticOnlyWithExpand_false() {
				Expression<?>[] set = {
						Literals.of(1),
						ArrayLiterals.of(2, 3, 4, 5),
						Literals.of(10),
						ArrayLiterals.of(100_000, -1_000_000),
				};
				NumericalExpression target = Literals.of(11);
				BooleanExpression pred = SetPredicates.in(target, set);

				assertThat(pred.computeAsBoolean()).isFalse();
			}

			@Test
			void testStaticOnlyWithExpand_true() {
				Expression<?>[] set = {
						Literals.of(1),
						ArrayLiterals.of(2, 3, 4, 5),
						Literals.of(10),
						ArrayLiterals.of(100_000, -1_000_000),
				};
				NumericalExpression target = Literals.of(4);
				BooleanExpression pred = SetPredicates.in(target, set);

				assertThat(pred.computeAsBoolean()).isTrue();
			}

			@Test
			void testDynamicTarget() {
				Expression<?>[] set = {
						Literals.of(1),
						Literals.of(2),
						Literals.of(10),
						Literals.of(100_000),
				};
				MutableLong dummy = new MutableLong(0);
				NumericalExpression target = dynamic(dummy::longValue);
				BooleanExpression pred = SetPredicates.in(target, set);

				assertThat(pred.computeAsBoolean()).isFalse();

				dummy.setLong(1);
				assertThat(pred.computeAsBoolean()).isTrue();

				dummy.setLong(50);
				assertThat(pred.computeAsBoolean()).isFalse();

				dummy.setLong(100_000);
				assertThat(pred.computeAsBoolean()).isTrue();
			}

			@Test
			void testDynamicTargetWithExpand() {
				Expression<?>[] set = {
						Literals.of(1),
						ArrayLiterals.of(2, 3, 4, 5),
						Literals.of(10),
						ArrayLiterals.of(100_000, -1_000_000),
				};
				MutableLong dummy = new MutableLong(0);
				NumericalExpression target = dynamic(dummy::longValue);
				BooleanExpression pred = SetPredicates.in(target, set);

				assertThat(pred.computeAsBoolean()).isFalse();

				dummy.setLong(1);
				assertThat(pred.computeAsBoolean()).isTrue();

				dummy.setLong(50);
				assertThat(pred.computeAsBoolean()).isFalse();

				dummy.setLong(4);
				assertThat(pred.computeAsBoolean()).isTrue();

				dummy.setLong(100_000);
				assertThat(pred.computeAsBoolean()).isTrue();
			}

			@Test
			void testDynamicSet() {
				MutableLong v1 = new MutableLong(2);
				MutableLong v2 = new MutableLong(10_000);
				Expression<?>[] set = {
						Literals.of(1),
						dynamic(v1::longValue),
						Literals.of(11),
						dynamic(v2::longValue),
				};
				NumericalExpression target = Literals.of(10);
				BooleanExpression pred = SetPredicates.in(target, set);

				assertThat(pred.computeAsBoolean()).isFalse();

				v1.setLong(10);
				assertThat(pred.computeAsBoolean()).isTrue();

				v1.setLong(50);
				assertThat(pred.computeAsBoolean()).isFalse();

				v2.setLong(10);
				assertThat(pred.computeAsBoolean()).isTrue();
			}

			@Test
			void testUnoptimizable() {
				MutableLong v1 = new MutableLong(2);
				MutableLong v2 = new MutableLong(10_000);
				Expression<?>[] set = {
						Literals.of(1),
						dynamic(v1::longValue),
						Literals.of(11),
						dynamic(v2::longValue),
				};
				NumericalExpression target = Literals.of(10);
				BooleanExpression pred = SetPredicates.in(target, set);
				assertThat(pred.optimize(mock(EvaluationContext.class))).isSameAs(pred);
			}

			@Test
			void testUnoptimizableWithExpand() {
				Mutable<long[]> v1 = new MutableObject<>(new long[0]);
				Mutable<long[]> v2 = new MutableObject<>(new long[0]);
				Expression<?>[] set = {
						Literals.of(1),
						dynamicLongs(v1::get),
						Literals.of(11),
						dynamicLongs(v2::get),
				};
				NumericalExpression target = Literals.of(10);
				BooleanExpression pred = SetPredicates.in(target, set);
				assertThat(pred.optimize(mock(EvaluationContext.class))).isSameAs(pred);
			}

			@Test
			void testOptimizable() {
				MutableLong v2 = new MutableLong(10_000);
				Expression<?>[] set = {
						Literals.of(1),
						optimizable(2),
						Literals.of(11),
						dynamic(v2::longValue),
				};
				MutableLong dummy = new MutableLong(0);
				NumericalExpression target = dynamic(dummy::longValue);
				BooleanExpression pred = SetPredicates.in(target, set);

				BooleanExpression optimized = (BooleanExpression) pred.optimize(mock(EvaluationContext.class));
				assertThat(optimized).isInstanceOf(FlatIntegerSetPredicate.class);
				NumericalExpression[] optimizedSet = ((FlatIntegerSetPredicate)optimized).getDynamicElements();
				assertThat(optimizedSet).hasSize(1);
				LongSet optimizedConstants = ((FlatIntegerSetPredicate)optimized).getFixedLongs();
				assertThat(optimizedConstants).hasSize(3);
			}

			@Test
			void testOptimizableWithExpand() {
				Mutable<long[]> v2 = new MutableObject<>(new long[] {10_000, -1_000_000});
				Expression<?>[] set = {
						Literals.of(1),
						optimizableLongs(1, 2, 3, 4, 5),
						Literals.of(11),
						dynamicLongs(v2::get),
				};
				MutableLong dummy = new MutableLong(0);
				NumericalExpression target = dynamic(dummy::longValue);
				BooleanExpression pred = SetPredicates.in(target, set);

				BooleanExpression optimized = (BooleanExpression) pred.optimize(mock(EvaluationContext.class));
				assertThat(optimized).isInstanceOf(FlatIntegerSetPredicate.class);
				FlatIntegerSetPredicate intPred = (FlatIntegerSetPredicate)optimized;
				assertThat(intPred.getDynamicElements()).isEmpty();
				assertThat(intPred.getDynamicLists()).hasSize(1);
				assertThat(intPred.getFixedLongs()).hasSize(6); // 2 + array of size 5 with 1 redundant
			}

			@Test
			void testOptimizableToConstant() {
				Expression<?>[] set = {
						Literals.of(1),
						optimizable(2),
						Literals.of(11),
						optimizable(10_000),
				};
				NumericalExpression target = optimizable(0);
				BooleanExpression pred = SetPredicates.in(target, set);

				BooleanExpression optimized = (BooleanExpression) pred.optimize(mock(EvaluationContext.class));
				assertThat(Literals.isLiteral(optimized)).isTrue();
				assertThat(optimized.computeAsBoolean()).isFalse();
			}

			@Test
			void testOptimizableToConstantWithExpand() {
				Expression<?>[] set = {
						Literals.of(1),
						optimizableLongs(1, 2, 3, 4, 5),
						Literals.of(11),
						optimizableLongs(10_000, -1_000_000, 123456),
				};
				NumericalExpression target = optimizable(4);
				BooleanExpression pred = SetPredicates.in(target, set);

				BooleanExpression optimized = (BooleanExpression) pred.optimize(mock(EvaluationContext.class));
				assertThat(Literals.isLiteral(optimized)).isTrue();
				assertThat(optimized.computeAsBoolean()).isTrue();
			}
		}

		@Nested
		class ForFloatingPoint implements BooleanExpressionTest {

			@Override
			public BooleanExpression createWithValue(Primitive<Boolean> value) {
				Expression<?>[] elements = {
					Literals.of(123.456),
					Literals.of(234.567),
					Literals.of(345.678),
					Literals.of(456.789),
					Literals.of(567.890),
					Literals.of(678.901),
				};

				NumericalExpression query = value.booleanValue() ?
						Literals.of(234.567) : Literals.of(-1234.5678);

				return SetPredicates.in(query, elements);
			}

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return FlatFloatingPointSetPredicate.class; }

			private class FixedData {
				final double target;
				final double[] set;
				final boolean result;
				FixedData(double target, boolean result, double...set) {
					this.target = target;
					this.result = result;
					this.set = set;
				}
			}

			FixedData data(double target, boolean result, double...set) {
				return new FixedData(target, result, set);
			}

			private void assertSet(Expression<?> target, Expression<?>[] elements, boolean result) {
				BooleanExpression pred = SetPredicates.in(target, elements);
				assertThat(pred.computeAsBoolean()).isEqualTo(result);

				BooleanExpression duplicate = (BooleanExpression) pred.duplicate(mock(EvaluationContext.class));
				assertThat(duplicate.computeAsBoolean()).isEqualTo(result);

				BooleanExpression optimized = (BooleanExpression) pred.optimize(mock(EvaluationContext.class));
				assertThat(optimized.computeAsBoolean()).isEqualTo(result);
			}

			@TestFactory
			Stream<DynamicNode> testStaticOnly() {
				return Stream.of(
						data(1.1, true, 1.1),
						data(1.1, true, 1.1, 2.1, 3.1, 4.1),
						data(1.1, false, 2.1),
						data(1.1, false, -1.1, 2.1, 3.1, 4.1)
				).map(data -> dynamicTest(String.format("%s in %s -> %b",
						displayString(data.target), Arrays.toString(data.set), _boolean(data.result)), () -> {
							assertSet(Literals.of(data.target), DoubleStream.of(data.set)
									.mapToObj(Literals::of)
									.toArray(NumericalExpression[]::new), data.result);
				}));
			}

			@Test
			void testDynamicTarget() {
				Expression<?>[] set = {
						Literals.of(1.1),
						Literals.of(2.1),
						Literals.of(10.1),
						Literals.of(100_000.1),
				};
				MutableDouble dummy = new MutableDouble(0.1);
				NumericalExpression target = dynamic(dummy::doubleValue);
				BooleanExpression pred = SetPredicates.in(target, set);

				assertThat(pred.computeAsBoolean()).isFalse();

				dummy.setDouble(1.1);
				assertThat(pred.computeAsBoolean()).isTrue();

				dummy.setDouble(50.1);
				assertThat(pred.computeAsBoolean()).isFalse();

				dummy.setDouble(100_000.1);
				assertThat(pred.computeAsBoolean()).isTrue();
			}

			@Test
			void testDynamicSet() {
				MutableDouble v1 = new MutableDouble(2.1);
				MutableDouble v2 = new MutableDouble(10_000.1);
				Expression<?>[] set = {
						Literals.of(1.1),
						dynamic(v1::doubleValue),
						Literals.of(11.1),
						dynamic(v2::doubleValue),
				};
				NumericalExpression target = Literals.of(10.1);
				BooleanExpression pred = SetPredicates.in(target, set);

				assertThat(pred.computeAsBoolean()).isFalse();

				v1.setDouble(10.1);
				assertThat(pred.computeAsBoolean()).isTrue();

				v1.setDouble(50.1);
				assertThat(pred.computeAsBoolean()).isFalse();

				v2.setDouble(10.1);
				assertThat(pred.computeAsBoolean()).isTrue();
			}

			@Test
			void testUnoptimizable() {
				MutableDouble v1 = new MutableDouble(2.1);
				MutableDouble v2 = new MutableDouble(10_000.1);
				Expression<?>[] set = {
						Literals.of(1.1),
						dynamic(v1::doubleValue),
						Literals.of(11.1),
						dynamic(v2::doubleValue),
				};
				NumericalExpression target = Literals.of(10.1);
				BooleanExpression pred = SetPredicates.in(target, set);
				assertThat(pred.optimize(mock(EvaluationContext.class))).isSameAs(pred);
			}

			@Test
			void testOptimizable() {
				MutableDouble v2 = new MutableDouble(10_000.1);
				Expression<?>[] set = {
						Literals.of(1.1),
						optimizable(2.1),
						Literals.of(11.1),
						dynamic(v2::doubleValue),
				};
				MutableDouble dummy = new MutableDouble(0.1);
				NumericalExpression target = dynamic(dummy::doubleValue);
				BooleanExpression pred = SetPredicates.in(target, set);

				BooleanExpression optimized = (BooleanExpression) pred.optimize(mock(EvaluationContext.class));
				assertThat(optimized).isInstanceOf(FlatFloatingPointSetPredicate.class);
				NumericalExpression[] optimizedSet = ((FlatFloatingPointSetPredicate)optimized).getDynamicElements();
				assertThat(optimizedSet).hasSize(1);
				DoubleSet optimizedConstants = ((FlatFloatingPointSetPredicate)optimized).getFixedDoubles();
				assertThat(optimizedConstants).hasSize(3);
			}

			@Test
			void testOptimizableToConstant() {
				Expression<?>[] set = {
						Literals.of(1.1),
						optimizable(2.1),
						Literals.of(11.1),
						optimizable(10_000.1),
				};
				NumericalExpression target = optimizable(0.1);
				BooleanExpression pred = SetPredicates.in(target, set);

				BooleanExpression optimized = (BooleanExpression) pred.optimize(mock(EvaluationContext.class));
				assertThat(Literals.isLiteral(optimized)).isTrue();
				assertThat(optimized.computeAsBoolean()).isFalse();
			}
		}

		@Nested
		class ForText implements BooleanExpressionTest {

			@Override
			public BooleanExpression createWithValue(Primitive<Boolean> value) {
				TextExpression[] elements = {
					Literals.of(""),
					Literals.of("x"),
					Literals.of("1234567890"),
					Literals.of(CodePointUtilsTest.test),
					Literals.of("some value stuff"),
					Literals.of("test"),
				};

				TextExpression query = value.booleanValue() ?
						Literals.of("test") : Literals.of("not contained test");

				return SetPredicates.in(query, elements);
			}

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return FlatTextSetPredicate.class; }

			private class FixedData {
				final CharSequence target;
				final CharSequence[] set;
				final boolean result;
				FixedData(CharSequence target, boolean result, CharSequence...set) {
					this.target = target;
					this.result = result;
					this.set = set;
				}
			}

			FixedData data(CharSequence target, boolean result, CharSequence...set) {
				return new FixedData(target, result, set);
			}

			private void assertSet(Expression<?> target, Expression<?>[] elements, boolean result) {
				BooleanExpression pred = SetPredicates.in(target, elements);
				assertThat(pred.computeAsBoolean()).isEqualTo(result);

				BooleanExpression duplicate = (BooleanExpression) pred.duplicate(mock(EvaluationContext.class));
				assertThat(duplicate.computeAsBoolean()).isEqualTo(result);

				BooleanExpression optimized = (BooleanExpression) pred.optimize(mock(EvaluationContext.class));
				assertThat(optimized.computeAsBoolean()).isEqualTo(result);
			}

			@TestFactory
			Stream<DynamicNode> testStaticOnly() {
				return Stream.of(
						data("test", true, "test"),
						data("test", true, "fu", "bar", "test", "xxxxxxxxxxx\nyyyyyyyy"),
						data("test", false, "fu"),
						data("test", false, "fu", "bar", "xxxxxxxxxxx\nyyyyyyyy")
				).map(data -> dynamicTest(String.format("%s in %s -> %b",
						displayString(data.target), Arrays.toString(data.set), _boolean(data.result)), () -> {
							assertSet(Literals.of(data.target), Stream.of(data.set)
									.map(Literals::of)
									.toArray(TextExpression[]::new), data.result);
				}));
			}

			@Test
			void testDynamicTarget() {
				TextExpression[] set = CodePointUtilsTest.testValues()
					.map(Literals::of)
					.toArray(TextExpression[]::new);
				Mutable<CharSequence> dummy = new MutableObject<>("");
				TextExpression target = dynamic(dummy::get);
				BooleanExpression pred = SetPredicates.in(target, set);

				assertThat(pred.computeAsBoolean()).isFalse();

				dummy.set(CodePointUtilsTest.test);
				assertThat(pred.computeAsBoolean()).isTrue();

				dummy.set("test123");
				assertThat(pred.computeAsBoolean()).isFalse();

				dummy.set(CodePointUtilsTest.test_mixed);
				assertThat(pred.computeAsBoolean()).isTrue();
			}

			@Test
			void testDynamicSet() {
				Mutable<CharSequence> v1 = new MutableObject<>("test2");
				Mutable<CharSequence> v2 = new MutableObject<>("test123");
				TextExpression[] set = {
						Literals.of("123"),
						dynamic(v1::get),
						Literals.of(CodePointUtilsTest.test_mixed),
						dynamic(v2::get),
				};
				TextExpression target = Literals.of("test");
				BooleanExpression pred = SetPredicates.in(target, set);

				assertThat(pred.computeAsBoolean()).isFalse();

				v1.set("test");
				assertThat(pred.computeAsBoolean()).isTrue();

				v1.set(CodePointUtilsTest.test_chinese);
				assertThat(pred.computeAsBoolean()).isFalse();

				v2.set("test");
				assertThat(pred.computeAsBoolean()).isTrue();
			}

			@Test
			void testUnoptimizable() {
				Mutable<CharSequence> v1 = new MutableObject<>("test2");
				Mutable<CharSequence> v2 = new MutableObject<>("test123");
				TextExpression[] set = {
						Literals.of("123"),
						dynamic(v1::get),
						Literals.of("1234"),
						dynamic(v2::get),
				};
				TextExpression target = Literals.of("test");
				BooleanExpression pred = SetPredicates.in(target, set);
				assertThat(pred.optimize(mock(EvaluationContext.class))).isSameAs(pred);
			}

			@Test
			void testOptimizable() {
				Mutable<CharSequence> v2 = new MutableObject<>("test123");
				TextExpression[] set = {
						Literals.of("123"),
						optimizable(CodePointUtilsTest.test_mixed),
						Literals.of("1234"),
						dynamic(v2::get),
				};
				Mutable<CharSequence> dummy = new MutableObject<>("");
				TextExpression target = dynamic(dummy::get);
				BooleanExpression pred = SetPredicates.in(target, set);

				BooleanExpression optimized = (BooleanExpression) pred.optimize(mock(EvaluationContext.class));
				assertThat(optimized).isInstanceOf(FlatTextSetPredicate.class);
				TextExpression[] optimizedSet = ((FlatTextSetPredicate)optimized).getDynamicElements();
				assertThat(optimizedSet).hasSize(1);
				Set<CharSequence> optimizedConstants = ((FlatTextSetPredicate)optimized).getFixedElements();
				assertThat(optimizedConstants).hasSize(3);
			}

			@Test
			void testOptimizableToConstant() {
				TextExpression[] set = {
						Literals.of("123"),
						optimizable(CodePointUtilsTest.test_mixed),
						Literals.of("1234"),
						optimizable(CodePointUtilsTest.test),
				};
				TextExpression target = optimizable("test");
				BooleanExpression pred = SetPredicates.in(target, set);

				BooleanExpression optimized = (BooleanExpression) pred.optimize(mock(EvaluationContext.class));
				assertThat(Literals.isLiteral(optimized)).isTrue();
				assertThat(optimized.computeAsBoolean()).isFalse();
			}
		}
	}

	@Nested
	class ForListTarget {
		//TODO
	}

}
