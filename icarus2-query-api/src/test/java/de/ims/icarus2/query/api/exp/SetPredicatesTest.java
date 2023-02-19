/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.exp;

import static de.ims.icarus2.query.api.exp.ExpressionTestUtils.dynamic;
import static de.ims.icarus2.query.api.exp.ExpressionTestUtils.dynamicDoubles;
import static de.ims.icarus2.query.api.exp.ExpressionTestUtils.dynamicLongs;
import static de.ims.icarus2.query.api.exp.ExpressionTestUtils.mockContext;
import static de.ims.icarus2.query.api.exp.ExpressionTestUtils.optimizable;
import static de.ims.icarus2.query.api.exp.ExpressionTestUtils.optimizableDoubles;
import static de.ims.icarus2.query.api.exp.ExpressionTestUtils.optimizableLongs;
import static de.ims.icarus2.test.TestUtils.displayString;
import static de.ims.icarus2.util.lang.Primitives._boolean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.DoubleStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.query.api.exp.Expression.FloatingPointListExpression;
import de.ims.icarus2.query.api.exp.Expression.IntegerListExpression;
import de.ims.icarus2.query.api.exp.Expression.ListExpression;
import de.ims.icarus2.query.api.exp.ExpressionTest.BooleanExpressionTest;
import de.ims.icarus2.query.api.exp.SetPredicates.FloatingPointSetPredicate;
import de.ims.icarus2.query.api.exp.SetPredicates.IntegerSetPredicate;
import de.ims.icarus2.query.api.exp.SetPredicates.TextSetPredicate;
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
			public Expression<Primitive<Boolean>> createWithValue(Primitive<Boolean> value) {
				Expression<?>[] elements = {
					Literals.of(123),
					Literals.of(234),
					Literals.of(345),
					Literals.of(456),
					Literals.of(567),
					Literals.of(678),
				};

				Expression<?> query = value.booleanValue() ?
						Literals.of(234) : Literals.of(-1234);

				return SetPredicates.in(query, elements);
			}

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return IntegerSetPredicate.class; }

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
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, elements);
				assertThat(pred.computeAsBoolean()).isEqualTo(result);

				Expression<Primitive<Boolean>> duplicate = mockContext().duplicate(pred);
				assertThat(duplicate.computeAsBoolean()).isEqualTo(result);

				Expression<Primitive<Boolean>> optimized = mockContext().optimize(pred);
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
									.toArray(Expression<?>[]::new), data.result);
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
				Expression<?> target = Literals.of(11);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

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
				Expression<?> target = Literals.of(4);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

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
				Expression<?> target = dynamic(dummy::longValue);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

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
				Expression<?> target = dynamic(dummy::longValue);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

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
				Expression<?> target = Literals.of(10);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

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
				Expression<?> target = Literals.of(10);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);
				assertThat(mockContext().optimize(pred)).isSameAs(pred);
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
				Expression<?> target = Literals.of(10);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);
				assertThat(mockContext().optimize(pred)).isSameAs(pred);
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
				Expression<?> target = dynamic(dummy::longValue);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				Expression<Primitive<Boolean>> optimized = mockContext().optimize(pred);
				assertThat(optimized).isInstanceOf(IntegerSetPredicate.class);
				Expression<?>[] optimizedSet = ((IntegerSetPredicate)optimized).getDynamicElements();
				assertThat(optimizedSet).hasSize(1);
				LongSet optimizedConstants = ((IntegerSetPredicate)optimized).getFixedLongs();
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
				Expression<?> target = dynamic(dummy::longValue);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				Expression<Primitive<Boolean>> optimized = mockContext().optimize(pred);
				assertThat(optimized).isInstanceOf(IntegerSetPredicate.class);
				IntegerSetPredicate intPred = (IntegerSetPredicate)optimized;
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
				Expression<?> target = optimizable(0);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				Expression<Primitive<Boolean>> optimized = mockContext().optimize(pred);
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
				Expression<?> target = optimizable(4);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				Expression<Primitive<Boolean>> optimized = mockContext().optimize(pred);
				assertThat(Literals.isLiteral(optimized)).isTrue();
				assertThat(optimized.computeAsBoolean()).isTrue();
			}
		}

		@Nested
		class ForFloatingPoint implements BooleanExpressionTest {

			@Override
			public Expression<Primitive<Boolean>> createWithValue(Primitive<Boolean> value) {
				Expression<?>[] elements = {
					Literals.of(123.456),
					Literals.of(234.567),
					Literals.of(345.678),
					Literals.of(456.789),
					Literals.of(567.890),
					Literals.of(678.901),
				};

				Expression<?> query = value.booleanValue() ?
						Literals.of(234.567) : Literals.of(-1234.5678);

				return SetPredicates.in(query, elements);
			}

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return FloatingPointSetPredicate.class; }

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
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, elements);
				assertThat(pred.computeAsBoolean()).isEqualTo(result);

				Expression<Primitive<Boolean>> duplicate = mockContext().duplicate(pred);
				assertThat(duplicate.computeAsBoolean()).isEqualTo(result);

				Expression<Primitive<Boolean>> optimized = mockContext().optimize(pred);
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
									.toArray(Expression<?>[]::new), data.result);
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
				Expression<?> target = dynamic(dummy::doubleValue);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

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
				Expression<?> target = Literals.of(10.1);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

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
				Expression<?> target = Literals.of(10.1);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);
				assertThat(mockContext().optimize(pred)).isSameAs(pred);
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
				Expression<?> target = dynamic(dummy::doubleValue);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				Expression<Primitive<Boolean>> optimized = mockContext().optimize(pred);
				assertThat(optimized).isInstanceOf(FloatingPointSetPredicate.class);
				Expression<?>[] optimizedSet = ((FloatingPointSetPredicate)optimized).getDynamicElements();
				assertThat(optimizedSet).hasSize(1);
				DoubleSet optimizedConstants = ((FloatingPointSetPredicate)optimized).getFixedDoubles();
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
				Expression<?> target = optimizable(0.1);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				Expression<Primitive<Boolean>> optimized = mockContext().optimize(pred);
				assertThat(Literals.isLiteral(optimized)).isTrue();
				assertThat(optimized.computeAsBoolean()).isFalse();
			}

			//TODO create expansion tests similar to integer version!
		}

		@Nested
		class ForText implements BooleanExpressionTest {

			@Override
			public Expression<Primitive<Boolean>> createWithValue(Primitive<Boolean> value) {
				Expression<?>[] elements = {
					Literals.of(""),
					Literals.of("x"),
					Literals.of("1234567890"),
					Literals.of(CodePointUtilsTest.test),
					Literals.of("some value stuff"),
					Literals.of("test"),
				};

				Expression<CharSequence> query = value.booleanValue() ?
						Literals.of("test") : Literals.of("not contained test");

				return SetPredicates.in(query, elements);
			}

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return TextSetPredicate.class; }

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
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, elements);
				assertThat(pred.computeAsBoolean()).isEqualTo(result);

				Expression<Primitive<Boolean>> duplicate = mockContext().duplicate(pred);
				assertThat(duplicate.computeAsBoolean()).isEqualTo(result);

				Expression<Primitive<Boolean>> optimized = mockContext().optimize(pred);
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
									.toArray(Expression[]::new), data.result);
				}));
			}

			@Test
			void testDynamicTarget() {
				Expression<CharSequence>[] set = CodePointUtilsTest.testValues()
					.map(Literals::of)
					.toArray(Expression[]::new);
				Mutable<CharSequence> dummy = new MutableObject<>("");
				Expression<CharSequence> target = dynamic(dummy::get);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

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
				Expression<?>[] set = {
						Literals.of("123"),
						dynamic(v1::get),
						Literals.of(CodePointUtilsTest.test_mixed),
						dynamic(v2::get),
				};
				Expression<CharSequence> target = Literals.of("test");
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

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
				Expression<?>[] set = {
						Literals.of("123"),
						dynamic(v1::get),
						Literals.of("1234"),
						dynamic(v2::get),
				};
				Expression<CharSequence> target = Literals.of("test");
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);
				assertThat(mockContext().optimize(pred)).isSameAs(pred);
			}

			@Test
			void testOptimizable() {
				Mutable<CharSequence> v2 = new MutableObject<>("test123");
				Expression<?>[] set = {
						Literals.of("123"),
						optimizable(CodePointUtilsTest.test_mixed),
						Literals.of("1234"),
						dynamic(v2::get),
				};
				Mutable<CharSequence> dummy = new MutableObject<>("");
				Expression<CharSequence> target = dynamic(dummy::get);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				Expression<Primitive<Boolean>> optimized = mockContext().optimize(pred);
				assertThat(optimized).isInstanceOf(TextSetPredicate.class);
				Expression<CharSequence>[] optimizedSet = ((TextSetPredicate)optimized).getDynamicElements();
				assertThat(optimizedSet).hasSize(1);
				Set<CharSequence> optimizedConstants = ((TextSetPredicate)optimized).getFixedElements();
				assertThat(optimizedConstants).hasSize(3);
			}

			@Test
			void testOptimizableToConstant() {
				Expression<?>[] set = {
						Literals.of("123"),
						optimizable(CodePointUtilsTest.test_mixed),
						Literals.of("1234"),
						optimizable(CodePointUtilsTest.test),
				};
				Expression<CharSequence> target = optimizable("test");
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				Expression<Primitive<Boolean>> optimized = mockContext().optimize(pred);
				assertThat(Literals.isLiteral(optimized)).isTrue();
				assertThat(optimized.computeAsBoolean()).isFalse();
			}
		}
	}

	@Nested
	class ForListTarget {

		@Nested
		class ForInteger implements BooleanExpressionTest {

			@Override
			public Expression<Primitive<Boolean>> createWithValue(Primitive<Boolean> value) {
				Expression<?>[] elements = {
					Literals.of(123),
					Literals.of(234),
					Literals.of(345),
					Literals.of(456),
					Literals.of(567),
					Literals.of(678),
				};

				IntegerListExpression<?> query = value.booleanValue() ?
						ArrayLiterals.of(123, 567, 234, 345) :
						ArrayLiterals.of(-1234, 100, Integer.MAX_VALUE, -999);

				return SetPredicates.in(query, elements);
			}

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return IntegerSetPredicate.class; }

			private class FixedData {
				final long[] target;
				final List<long[]> set = new ArrayList<>();
				final boolean result;
				FixedData(boolean result, long...target) {
					this.target = target;
					this.result = result;
				}

				FixedData add(long...data) {
					set.add(data);
					return this;
				}

				Expression<?>[] wrap() {
					return set.stream()
							.map(data -> data.length==1 ? Literals.of(data[0]) : ArrayLiterals.of(data))
							.toArray(Expression[]::new);
				}
			}

			FixedData data(boolean result, long...target) {
				return new FixedData(result, target);
			}

			private void assertSet(boolean allIn, Expression<?> target, Expression<?>[] elements, boolean result) {
				Expression<Primitive<Boolean>> pred = allIn ? SetPredicates.allIn((ListExpression<?, ?>) target, elements)
						: SetPredicates.in(target, elements);
				assertThat(pred.computeAsBoolean()).isEqualTo(result);

				Expression<Primitive<Boolean>> duplicate = mockContext().duplicate(pred);
				assertThat(duplicate.computeAsBoolean()).isEqualTo(result);

				Expression<Primitive<Boolean>> optimized = mockContext().optimize(pred);
				assertThat(optimized.computeAsBoolean()).isEqualTo(result);
			}

			@TestFactory
			Stream<DynamicNode> testStaticOnly() {
				return Stream.of(
						data(true, 1, 2).add(1, 4, 5).add(2).add(3),
						data(true, 1, 2, 3, 4).add(10, 9, 8, 7).add(5).add(3, -1),
						data(false, 2, 3).add(1, 4).add(9, 8, 7).add(6),
						data(false, -1, 2, 3, 4).add(5, 6, 7).add(-2, -3)
				).map(data -> dynamicTest(String.format("%s in %s -> %b",
						displayString(data.target), data.set.toString(), _boolean(data.result)), () -> {
							assertSet(false, ArrayLiterals.of(data.target), data.wrap(), data.result);
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
				Expression<?> target = ArrayLiterals.of(11, 12, 999);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

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
				Expression<?> target = ArrayLiterals.of(11, 4, 999);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

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
				Mutable<long[]> dummy = new MutableObject<>(new long[] {4});
				Expression<?> target = dynamicLongs(dummy::get);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				assertThat(pred.computeAsBoolean()).isFalse();

				dummy.set(new long[] {999, 99, 2, -10});
				assertThat(pred.computeAsBoolean()).isTrue();

				dummy.set(new long[] {999, 99, 50, -10});
				assertThat(pred.computeAsBoolean()).isFalse();

				dummy.set(new long[] {999, 99, -2, -10, 100_000});
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
				Mutable<long[]> dummy = new MutableObject<>(new long[] {-4});
				Expression<?> target = dynamicLongs(dummy::get);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				assertThat(pred.computeAsBoolean()).isFalse();

				dummy.set(new long[] {999, 99, 4, -10});
				assertThat(pred.computeAsBoolean()).isTrue();

				dummy.set(new long[] {-999, 99, 0});
				assertThat(pred.computeAsBoolean()).isFalse();

				dummy.set(new long[] {99_999, -999, 0, -1_000_000});
				assertThat(pred.computeAsBoolean()).isTrue();

				dummy.set(new long[] {0, -1, -10, 1});
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
				Expression<?> target = ArrayLiterals.of(-1, 5, 10);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				assertThat(pred.computeAsBoolean()).isFalse();

				v1.setLong(10);
				assertThat(pred.computeAsBoolean()).isTrue();

				v1.setLong(50);
				assertThat(pred.computeAsBoolean()).isFalse();

				v2.setLong(5);
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
				Expression<?> target = ArrayLiterals.of(-1, 5, 10);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);
				assertThat(mockContext().optimize(pred)).isSameAs(pred);
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
				Expression<?> target = ArrayLiterals.of(-1, 5, 10);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);
				assertThat(mockContext().optimize(pred)).isSameAs(pred);
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
				Mutable<long[]> dummy = new MutableObject<>(new long[] {0, 12, -999});
				Expression<?> target = dynamicLongs(dummy::get);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				Expression<Primitive<Boolean>> optimized = mockContext().optimize(pred);
				assertThat(optimized).isInstanceOf(IntegerSetPredicate.class);
				Expression<?>[] optimizedSet = ((IntegerSetPredicate)optimized).getDynamicElements();
				assertThat(optimizedSet).hasSize(1);
				LongSet optimizedConstants = ((IntegerSetPredicate)optimized).getFixedLongs();
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
				Mutable<long[]> dummy = new MutableObject<>(new long[] {0, 12, -999});
				Expression<?> target = dynamicLongs(dummy::get);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				Expression<Primitive<Boolean>> optimized = mockContext().optimize(pred);
				assertThat(optimized).isInstanceOf(IntegerSetPredicate.class);
				IntegerSetPredicate intPred = (IntegerSetPredicate)optimized;
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
				Expression<?> target = optimizableLongs(0, -1, 999);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				Expression<Primitive<Boolean>> optimized = mockContext().optimize(pred);
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
				Expression<?> target = optimizableLongs(0, -1, 4, -4, 999);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				Expression<Primitive<Boolean>> optimized = mockContext().optimize(pred);
				assertThat(Literals.isLiteral(optimized)).isTrue();
				assertThat(optimized.computeAsBoolean()).isTrue();
			}
		}
		//TODO

		@Nested
		class ForFloatingPoint implements BooleanExpressionTest {

			@Override
			public Expression<Primitive<Boolean>> createWithValue(Primitive<Boolean> value) {
				Expression<?>[] elements = {
					Literals.of(123.1),
					Literals.of(234.1),
					Literals.of(345.1),
					Literals.of(456.1),
					Literals.of(567.1),
					Literals.of(678.1),
				};

				FloatingPointListExpression<?> query = value.booleanValue() ?
						ArrayLiterals.of(123.1, 567.1, 234.1, 345.1) :
						ArrayLiterals.of(-1234.1, 100.1, Integer.MAX_VALUE, -999.1);

				return SetPredicates.in(query, elements);
			}

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return FloatingPointSetPredicate.class; }

			private class FixedData {
				final double[] target;
				final List<double[]> set = new ArrayList<>();
				final boolean result;
				FixedData(boolean result, double...target) {
					this.target = target;
					this.result = result;
				}

				FixedData add(double...data) {
					set.add(data);
					return this;
				}

				Expression<?>[] wrap() {
					return set.stream()
							.map(data -> data.length==1 ? Literals.of(data[0]) : ArrayLiterals.of(data))
							.toArray(Expression[]::new);
				}
			}

			FixedData data(boolean result, double...target) {
				return new FixedData(result, target);
			}

			private void assertSet(boolean allIn, Expression<?> target, Expression<?>[] elements, boolean result) {
				Expression<Primitive<Boolean>> pred = allIn ? SetPredicates.allIn((ListExpression<?, ?>) target, elements)
						: SetPredicates.in(target, elements);
				assertThat(pred.computeAsBoolean()).isEqualTo(result);

				Expression<Primitive<Boolean>> duplicate = mockContext().duplicate(pred);
				assertThat(duplicate.computeAsBoolean()).isEqualTo(result);

				Expression<Primitive<Boolean>> optimized = mockContext().optimize(pred);
				assertThat(optimized.computeAsBoolean()).isEqualTo(result);
			}

			@TestFactory
			Stream<DynamicNode> testStaticOnly() {
				return Stream.of(
						data(true, 1.1, 2.1).add(1.1, 4.1, 5.1).add(2.1).add(3.1),
						data(true, 1.1, 2.1, 3.1, 4.1).add(10.1, 9.1, 8.1, 7.1).add(5.1).add(3.1, -1.1),
						data(false, 2.1, 3.1).add(1.1, 4.1).add(9.1, 8.1, 7.1).add(6.1),
						data(false, -1.1, 2.1, 3.1, 4.1).add(5.1, 6.1, 7.1).add(-2.1, -3.1)
				).map(data -> dynamicTest(String.format("%s in %s -> %b",
						displayString(data.target), data.set.toString(), _boolean(data.result)), () -> {
							assertSet(false, ArrayLiterals.of(data.target), data.wrap(), data.result);
				}));
			}

			@Test
			void testStaticOnlyWithExpand_false() {
				Expression<?>[] set = {
						Literals.of(1.1),
						ArrayLiterals.of(2.1, 3.1, 4.1, 5.1),
						Literals.of(10.1),
						ArrayLiterals.of(100_000.1, -1_000_000.1),
				};
				Expression<?> target = ArrayLiterals.of(11.1, 12.1, 999.1);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				assertThat(pred.computeAsBoolean()).isFalse();
			}

			@Test
			void testStaticOnlyWithExpand_true() {
				Expression<?>[] set = {
						Literals.of(1.1),
						ArrayLiterals.of(2.1, 3.1, 4.1, 5.1),
						Literals.of(10.1),
						ArrayLiterals.of(100_000.1, -1_000_000.1),
				};
				Expression<?> target = ArrayLiterals.of(11.1, 4.1, 999.1);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				assertThat(pred.computeAsBoolean()).isTrue();
			}

			@Test
			void testDynamicTarget() {
				Expression<?>[] set = {
						Literals.of(1.1),
						Literals.of(2.1),
						Literals.of(10.1),
						Literals.of(100_000.1),
				};
				Mutable<double[]> dummy = new MutableObject<>(new double[] {4.1});
				Expression<?> target = dynamicDoubles(dummy::get);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				assertThat(pred.computeAsBoolean()).isFalse();

				dummy.set(new double[] {999.1, 99.1, 2.1, -10.1});
				assertThat(pred.computeAsBoolean()).isTrue();

				dummy.set(new double[] {999.1, 99.1, 50.1, -10.1});
				assertThat(pred.computeAsBoolean()).isFalse();

				dummy.set(new double[] {999.1, 99.1, -2.1, -10.1, 100_000.1});
				assertThat(pred.computeAsBoolean()).isTrue();
			}

			@Test
			void testDynamicTargetWithExpand() {
				Expression<?>[] set = {
						Literals.of(1.1),
						ArrayLiterals.of(2.1, 3.1, 4.1, 5.1),
						Literals.of(10.1),
						ArrayLiterals.of(100_000.1, -1_000_000.1),
				};
				Mutable<double[]> dummy = new MutableObject<>(new double[] {-4.1});
				Expression<?> target = dynamicDoubles(dummy::get);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				assertThat(pred.computeAsBoolean()).isFalse();

				dummy.set(new double[] {999.1, 99.1, 4.1, -10.1});
				assertThat(pred.computeAsBoolean()).isTrue();

				dummy.set(new double[] {-999.1, 99.1, 0.1});
				assertThat(pred.computeAsBoolean()).isFalse();

				dummy.set(new double[] {99_999.1, -999.1, 0.1, -1_000_000.1});
				assertThat(pred.computeAsBoolean()).isTrue();

				dummy.set(new double[] {0.1, -1.1, -10.1, 1.1});
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
				Expression<?> target = ArrayLiterals.of(-1.1, 5.1, 10.1);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				assertThat(pred.computeAsBoolean()).isFalse();

				v1.setDouble(10.1);
				assertThat(pred.computeAsBoolean()).isTrue();

				v1.setDouble(50.1);
				assertThat(pred.computeAsBoolean()).isFalse();

				v2.setDouble(5.1);
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
				Expression<?> target = ArrayLiterals.of(-1.1, 5.1, 10.1);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);
				assertThat(mockContext().optimize(pred)).isSameAs(pred);
			}

			@Test
			void testUnoptimizableWithExpand() {
				Mutable<double[]> v1 = new MutableObject<>(new double[0]);
				Mutable<double[]> v2 = new MutableObject<>(new double[0]);
				Expression<?>[] set = {
						Literals.of(1.1),
						dynamicDoubles(v1::get),
						Literals.of(11.1),
						dynamicDoubles(v2::get),
				};
				Expression<?> target = ArrayLiterals.of(-1.1, 5.1, 10.1);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);
				assertThat(mockContext().optimize(pred)).isSameAs(pred);
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
				Mutable<double[]> dummy = new MutableObject<>(new double[] {0.1, 12.1, -999.1});
				Expression<?> target = dynamicDoubles(dummy::get);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				Expression<Primitive<Boolean>> optimized = mockContext().optimize(pred);
				assertThat(optimized).isInstanceOf(FloatingPointSetPredicate.class);
				Expression<?>[] optimizedSet = ((FloatingPointSetPredicate)optimized).getDynamicElements();
				assertThat(optimizedSet).hasSize(1);
				DoubleSet optimizedConstants = ((FloatingPointSetPredicate)optimized).getFixedDoubles();
				assertThat(optimizedConstants).hasSize(3);
			}

			@Test
			void testOptimizableWithExpand() {
				Mutable<double[]> v2 = new MutableObject<>(new double[] {10_000.1, -1_000_000.1});
				Expression<?>[] set = {
						Literals.of(1.1),
						optimizableDoubles(1.1, 2.1, 3.1, 4.1, 5.1),
						Literals.of(11.1),
						dynamicDoubles(v2::get),
				};
				Mutable<double[]> dummy = new MutableObject<>(new double[] {0.1, 12.1, -999.1});
				Expression<?> target = dynamicDoubles(dummy::get);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				Expression<Primitive<Boolean>> optimized = mockContext().optimize(pred);
				assertThat(optimized).isInstanceOf(FloatingPointSetPredicate.class);
				FloatingPointSetPredicate intPred = (FloatingPointSetPredicate)optimized;
				assertThat(intPred.getDynamicElements()).isEmpty();
				assertThat(intPred.getDynamicLists()).hasSize(1);
				assertThat(intPred.getFixedDoubles()).hasSize(6); // 2 + array of size 5 with 1 redundant
			}

			@Test
			void testOptimizableToConstant() {
				Expression<?>[] set = {
						Literals.of(1.1),
						optimizable(2.1),
						Literals.of(11.1),
						optimizable(10_000.1),
				};
				Expression<?> target = optimizableDoubles(0.1, -1.1, 999.1);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				Expression<Primitive<Boolean>> optimized = mockContext().optimize(pred);
				assertThat(Literals.isLiteral(optimized)).isTrue();
				assertThat(optimized.computeAsBoolean()).isFalse();
			}

			@Test
			void testOptimizableToConstantWithExpand() {
				Expression<?>[] set = {
						Literals.of(1.1),
						optimizableDoubles(1.1, 2.1, 3.1, 4.1, 5.1),
						Literals.of(11.1),
						optimizableDoubles(10_000.1, -1_000_000.1, 123456.1),
				};
				Expression<?> target = optimizableDoubles(0.1, -1.1, 4.1, -4.1, 999.1);
				Expression<Primitive<Boolean>> pred = SetPredicates.in(target, set);

				Expression<Primitive<Boolean>> optimized = mockContext().optimize(pred);
				assertThat(Literals.isLiteral(optimized)).isTrue();
				assertThat(optimized.computeAsBoolean()).isTrue();
			}
		}
	}

}
