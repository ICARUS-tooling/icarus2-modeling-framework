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
package de.ims.icarus2.query.api.exp;

import static de.ims.icarus2.query.api.exp.ExpressionTestUtils.assertExpression;
import static de.ims.icarus2.query.api.exp.ExpressionTestUtils.mockContext;
import static de.ims.icarus2.query.api.exp.ExpressionTestUtils.optimizable;
import static de.ims.icarus2.test.TestUtils.assertIAE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.query.api.exp.ExpressionTest.BooleanExpressionTest;
import de.ims.icarus2.query.api.exp.LogicalOperators.Conjunction;
import de.ims.icarus2.query.api.exp.LogicalOperators.Disjunction;
import de.ims.icarus2.util.MutablePrimitives.Primitive;

/**
 * @author Markus Gärtner
 *
 */
class LogicalOperatorsTest {

	@Nested
	class ForConjunction {

		abstract class ConjunctionTestBase implements BooleanExpressionTest {

			protected abstract boolean canOptimize();

			Expression<?> createFor(boolean...values) {
				Expression<?>[] elements = IntStream.range(0, values.length)
						.mapToObj(i -> optimizable(values[i]))
						.toArray(Expression[]::new);
				return createFor(elements);
			}

			Expression<?> createFor(Expression<?>[] elements) {
				return LogicalOperators.conjunction(elements, canOptimize());
			}

			@Override
			public Expression<?> createWithValue(Primitive<Boolean> value) {
				boolean[] values = new boolean[4];
				Arrays.fill(values, true);
				if(!value.booleanValue()) {
					values[2] = false;
				}

				return createFor(values);
			}

			@Override
			public boolean optimizeToConstant() { return canOptimize(); }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return Conjunction.class; }
		}

		@Nested
		class OptimizableConjunctionTest extends ConjunctionTestBase {

			@Override
			protected boolean canOptimize() { return true; }

			@Test
			void testFullCollapseToTrue() {
				Expression<?> exp = createFor(true, true, true);
				Expression<?> optimized = mockContext().optimize(exp);
				assertThat(Literals.isLiteral(optimized)).isTrue();
				assertExpression(exp, mockContext(), true);
			}

			@Test
			void testFullCollapseToFalse() {
				Expression<?> exp = createFor(false, false, false);
				Expression<?> optimized = mockContext().optimize(exp);
				assertThat(Literals.isLiteral(optimized)).isTrue();
				assertExpression(exp, mockContext(), false);
			}

			@Test
			void testUnoptimizable() {
				boolean[] values = {true, false, false, true};
				BooleanSupplier[] supplier = IntStream.range(0, values.length)
						.<BooleanSupplier>mapToObj(i -> {
							int index = i;
							return () -> values[index];
						}).toArray(BooleanSupplier[]::new);
				Expression<?>[] elements = Stream.of(supplier)
						.map(ExpressionTestUtils::dynamic)
						.toArray(Expression[]::new);

				Expression<?> exp = createFor(elements);
				assertThat(mockContext().optimize(exp)).isSameAs(exp);
			}

			@Test
			void testShrink() {
				boolean[] values = {true, false, false, true};
				BooleanSupplier[] supplier = IntStream.range(0, values.length)
						.<BooleanSupplier>mapToObj(i -> {
							int index = i;
							return () -> values[index];
						}).toArray(BooleanSupplier[]::new);
				Expression<?>[] elements = Stream.of(supplier)
						.map(ExpressionTestUtils::dynamic)
						.toArray(Expression[]::new);
				elements[3] = Literals.of(values[3]);

				Expression<?> exp = createFor(elements);
				Expression<?> optimized = mockContext().optimize(exp);
				assertThat(optimized).isInstanceOf(getTestTargetClass());
				assertThat(optimized).isNotSameAs(exp);
			}

			@ParameterizedTest
			@ValueSource(ints = {0, 1})
			void testInsufficientElements(int size) {
				boolean[] values = new boolean[size];
				assertIAE(() -> createFor(values));
			}
		}

		@Nested
		class InefficientConjunctionTest extends ConjunctionTestBase {

			@Override
			protected boolean canOptimize() { return false; }

			@Test
			void testFullCollapseToTrue() {
				Expression<?> exp = createFor(true, true, true);
				Expression<?> optimized = mockContext().optimize(exp);
				assertThat(Literals.isLiteral(optimized)).isTrue();
				assertExpression(exp, mockContext(), true);
			}

			@Test
			void testFullCollapseToFalse() {
				Expression<?> exp = createFor(false, false, false);
				Expression<?> optimized = mockContext().optimize(exp);
				assertThat(Literals.isLiteral(optimized)).isTrue();
				assertExpression(exp, mockContext(), false);
			}

			@Test
			void testUnoptimizable() {
				boolean[] values = {true, false, false, true};
				BooleanSupplier[] supplier = IntStream.range(0, values.length)
						.<BooleanSupplier>mapToObj(i -> {
							int index = i;
							return () -> values[index];
						}).toArray(BooleanSupplier[]::new);
				Expression<?>[] elements = Stream.of(supplier)
						.map(ExpressionTestUtils::dynamic)
						.toArray(Expression[]::new);

				Expression<?> exp = createFor(elements);
				assertThat(mockContext().optimize(exp)).isSameAs(exp);
			}

			@Test
			void testShrink() {
				boolean[] values = {true, false, false, true};
				BooleanSupplier[] supplier = IntStream.range(0, values.length)
						.<BooleanSupplier>mapToObj(i -> {
							int index = i;
							return () -> values[index];
						}).toArray(BooleanSupplier[]::new);
				Expression<?>[] elements = Stream.of(supplier)
						.map(ExpressionTestUtils::dynamic)
						.toArray(Expression[]::new);
				elements[3] = Literals.of(values[3]);

				Expression<?> exp = createFor(elements);
				Expression<?> optimized = mockContext().optimize(exp);
				assertThat(optimized).isInstanceOf(getTestTargetClass());
				assertThat(optimized).isNotSameAs(exp);
			}

			@ParameterizedTest
			@ValueSource(ints = {0, 1})
			void testInsufficientElements(int size) {
				boolean[] values = new boolean[size];
				assertIAE(() -> createFor(values));
			}
		}

	}

	@Nested
	class ForDisjunction {

		abstract class DisjunctionTestBase implements BooleanExpressionTest {

			protected abstract boolean canOptimize();

			Expression<?> createFor(boolean...values) {
				Expression<?>[] elements = IntStream.range(0, values.length)
						.mapToObj(i -> optimizable(values[i]))
						.toArray(Expression[]::new);
				return createFor(elements);
			}

			Expression<?> createFor(Expression<?>[] elements) {
				return LogicalOperators.disjunction(elements, canOptimize());
			}

			@Override
			public Expression<?> createWithValue(Primitive<Boolean> value) {
				boolean[] values = new boolean[4];
				Arrays.fill(values,false);
				if(value.booleanValue()) {
					values[2] = true;
				}
				return createFor(values);
			}

			@Override
			public boolean optimizeToConstant() { return canOptimize(); }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return Disjunction.class; }
		}

		@Nested
		class OptimizableDisjunctionTest extends DisjunctionTestBase {

			@Override
			protected boolean canOptimize() { return true; }

			@Test
			void testFullCollapseToTrue() {
				Expression<?> exp = createFor(true, true, true);
				Expression<?> optimized = mockContext().optimize(exp);
				assertThat(Literals.isLiteral(optimized)).isTrue();
				assertExpression(exp, mockContext(), true);
			}

			@Test
			void testFullCollapseToFalse() {
				Expression<?> exp = createFor(false, false, false);
				Expression<?> optimized = mockContext().optimize(exp);
				assertThat(Literals.isLiteral(optimized)).isTrue();
				assertExpression(exp, mockContext(), false);
			}

			@Test
			void testUnoptimizable() {
				boolean[] values = {false, false, false, false};
				BooleanSupplier[] supplier = IntStream.range(0, values.length)
						.<BooleanSupplier>mapToObj(i -> {
							int index = i;
							return () -> values[index];
						}).toArray(BooleanSupplier[]::new);
				Expression<?>[] elements = Stream.of(supplier)
						.map(ExpressionTestUtils::dynamic)
						.toArray(Expression[]::new);

				Expression<?> exp = createFor(elements);
				assertThat(mockContext().optimize(exp)).isSameAs(exp);
			}

			@Test
			void testShrink() {
				boolean[] values = {true, false, false, true};
				BooleanSupplier[] supplier = IntStream.range(0, values.length)
						.<BooleanSupplier>mapToObj(i -> {
							int index = i;
							return () -> values[index];
						}).toArray(BooleanSupplier[]::new);
				Expression<?>[] elements = Stream.of(supplier)
						.map(ExpressionTestUtils::dynamic)
						.toArray(Expression[]::new);
				elements[2] = Literals.of(values[2]);

				Expression<?> exp = createFor(elements);
				Expression<?> optimized = mockContext().optimize(exp);
				assertThat(optimized).isInstanceOf(getTestTargetClass());
				assertThat(optimized).isNotSameAs(exp);
			}

			@ParameterizedTest
			@ValueSource(ints = {0, 1})
			void testInsufficientElements(int size) {
				boolean[] values = new boolean[size];
				assertIAE(() -> createFor(values));
			}
		}

		@Nested
		class InefficientDisjunctionTest extends DisjunctionTestBase {

			@Override
			protected boolean canOptimize() { return false; }

			@Test
			void testFullCollapseToTrue() {
				Expression<?> exp = createFor(true, true, true);
				Expression<?> optimized = mockContext().optimize(exp);
				assertThat(Literals.isLiteral(optimized)).isTrue();
				assertExpression(exp, mockContext(), true);
			}

			@Test
			void testFullCollapseToFalse() {
				Expression<?> exp = createFor(false, false, false);
				Expression<?> optimized = mockContext().optimize(exp);
				assertThat(Literals.isLiteral(optimized)).isTrue();
				assertExpression(exp, mockContext(), false);
			}

			@Test
			void testUnoptimizable() {
				boolean[] values = {false, true, false, true};
				BooleanSupplier[] supplier = IntStream.range(0, values.length)
						.<BooleanSupplier>mapToObj(i -> {
							int index = i;
							return () -> values[index];
						}).toArray(BooleanSupplier[]::new);
				Expression<?>[] elements = Stream.of(supplier)
						.map(ExpressionTestUtils::dynamic)
						.toArray(Expression[]::new);

				Expression<?> exp = createFor(elements);
				assertThat(mockContext().optimize(exp)).isSameAs(exp);
			}

			@Test
			void testShrink() {
				boolean[] values = {true, false, false, true};
				BooleanSupplier[] supplier = IntStream.range(0, values.length)
						.<BooleanSupplier>mapToObj(i -> {
							int index = i;
							return () -> values[index];
						}).toArray(BooleanSupplier[]::new);
				Expression<?>[] elements = Stream.of(supplier)
						.map(ExpressionTestUtils::dynamic)
						.toArray(Expression[]::new);
				elements[2] = Literals.of(values[2]);

				Expression<?> exp = createFor(elements);
				Expression<?> optimized = mockContext().optimize(exp);
				assertThat(optimized).isInstanceOf(getTestTargetClass());
				assertThat(optimized).isNotSameAs(exp);
			}

			@ParameterizedTest
			@ValueSource(ints = {0, 1})
			void testInsufficientElements(int size) {
				boolean[] values = new boolean[size];
				assertIAE(() -> createFor(values));
			}
		}

	}
}
