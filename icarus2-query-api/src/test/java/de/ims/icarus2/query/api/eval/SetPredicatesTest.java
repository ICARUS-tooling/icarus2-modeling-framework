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
import static de.ims.icarus2.test.TestUtils.displayString;
import static de.ims.icarus2.util.lang.Primitives._boolean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.query.api.eval.Expression.BooleanExpression;
import de.ims.icarus2.query.api.eval.Expression.NumericalExpression;
import de.ims.icarus2.query.api.eval.ExpressionTest.BooleanExpressionTest;
import de.ims.icarus2.query.api.eval.SetPredicates.FlatIntegerSetPredicate;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.MutablePrimitives.Primitive;

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
				NumericalExpression[] elements = {
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
			Stream<DynamicNode> testStaticOnly() {
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
			void testDynamic() {
				NumericalExpression[] set = {
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
		}
	}

	@Nested
	class ForListTarget {
		//TODO
	}

}
