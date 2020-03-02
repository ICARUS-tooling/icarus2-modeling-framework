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

import static de.ims.icarus2.query.api.eval.ExpressionTestUtils.dynamicLongs;
import static de.ims.icarus2.util.lang.Primitives._int;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.query.api.eval.Expression.BooleanListExpression;
import de.ims.icarus2.query.api.eval.Expression.FloatingPointListExpression;
import de.ims.icarus2.query.api.eval.Expression.IntegerListExpression;
import de.ims.icarus2.query.api.eval.Expression.ListExpression;
import de.ims.icarus2.query.api.eval.ExpressionTest.BooleanExpressionTest;
import de.ims.icarus2.query.api.eval.ExpressionTest.FloatingPointExpressionTest;
import de.ims.icarus2.query.api.eval.ExpressionTest.IntegerExpressionTest;
import de.ims.icarus2.query.api.eval.ExpressionTestMixins.BooleanArrayMixin;
import de.ims.icarus2.query.api.eval.ExpressionTestMixins.DoubleArrayMixin;
import de.ims.icarus2.query.api.eval.ExpressionTestMixins.LongArrayMixin;
import de.ims.icarus2.query.api.eval.ExpressionTestMixins.TextArrayMixin;
import de.ims.icarus2.query.api.eval.ListAccess.BooleanAccess;
import de.ims.icarus2.query.api.eval.ListAccess.BooleanBatchAccess;
import de.ims.icarus2.query.api.eval.ListAccess.BooleanListWrapper;
import de.ims.icarus2.query.api.eval.ListAccess.FloatingPointAccess;
import de.ims.icarus2.query.api.eval.ListAccess.FloatingPointBatchAccess;
import de.ims.icarus2.query.api.eval.ListAccess.FloatingPointListWrapper;
import de.ims.icarus2.query.api.eval.ListAccess.IntegerAccess;
import de.ims.icarus2.query.api.eval.ListAccess.IntegerBatchAccess;
import de.ims.icarus2.query.api.eval.ListAccess.IntegerListWrapper;
import de.ims.icarus2.query.api.eval.ListAccess.ObjectAccess;
import de.ims.icarus2.query.api.eval.ListAccess.ObjectBatchAccess;
import de.ims.icarus2.query.api.eval.ListAccess.ObjectWrapper;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.MutablePrimitives.Primitive;

/**
 * @author Markus Gärtner
 *
 */
class ListAccessTest {

	@Nested
	class SingleAccess {

		@Nested
		class ForInteger implements IntegerExpressionTest {

			@Override
			public Expression<?> createWithValue(Primitive<? extends Number> value) {
				long[] array = {1, 2, 3, 4, 5};
				int index = 2;
				array[index] = value.longValue();

				return ListAccess.atIndex(ArrayLiterals.of(array), Literals.of(index));
			}

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return IntegerAccess.class; }
		}

		@Nested
		class ForFloatingPoint implements FloatingPointExpressionTest {

			@Override
			public Expression<?> createWithValue(Primitive<? extends Number> value) {
				double[] array = {1.1, 2.1, 3.1, 4.1, 5.1};
				int index = 2;
				array[index] = value.doubleValue();

				return ListAccess.atIndex(ArrayLiterals.of(array), Literals.of(index));
			}

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return FloatingPointAccess.class; }
		}

		@Nested
		class ForBoolean implements BooleanExpressionTest {

			@Override
			public Expression<?> createWithValue(Primitive<Boolean> value) {
				boolean[] array = {true, false, false, true, true, false};
				int index = 2;
				array[index] = value.booleanValue();

				return ListAccess.atIndex(ArrayLiterals.of(array), Literals.of(index));
			}

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return BooleanAccess.class; }
		}

		@Nested
		class ForObject implements ExpressionTest<CharSequence> {

			@Override
			public CharSequence constant() {
				return "test";
			}

			@Override
			public CharSequence random(RandomGenerator rng) {
				return rng.randomUnicodeString(10);
			}

			@Override
			public Expression<?> createWithValue(CharSequence value) {
				CharSequence[] array = IntStream.range(0, 6)
						.mapToObj(i -> "item_"+i)
						.toArray(CharSequence[]::new);
				int index = 2;
				array[index] = value;

				return ListAccess.atIndex(ArrayLiterals.ofGeneric(array), Literals.of(index));
			}

			@Override
			public TypeInfo getExpectedType() { return TypeInfo.TEXT; }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return ObjectAccess.class; }
		}
	}

	@Nested
	class Filter {

		@Nested
		class ForInteger implements LongArrayMixin {

			@Override
			public Class<?> getTestTargetClass() { return IntegerBatchAccess.class; }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public IntegerListExpression<long[]> createWithValue(long[] source) {
				long[] array = LongStream.range(0, source.length*2).toArray();
				int[] indices = IntStream.range(0, source.length).map(i -> i*2).toArray();

				for (int i = 0; i < indices.length; i++) {
					array[indices[i]] = source[i];
				}

				return ListAccess.filter(ArrayLiterals.of(array), ArrayLiterals.of(indices));
			}
		}

		@Nested
		class ForFloatingPoint implements DoubleArrayMixin {

			@Override
			public Class<?> getTestTargetClass() { return FloatingPointBatchAccess.class; }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public FloatingPointListExpression<double[]> createWithValue(double[] source) {
				double[] array = DoubleStream.iterate(1.0, i -> -i*2.5).limit(source.length*2).toArray();
				int[] indices = IntStream.range(0, source.length).map(i -> i*2).toArray();

				for (int i = 0; i < indices.length; i++) {
					array[indices[i]] = source[i];
				}

				return ListAccess.filter(ArrayLiterals.of(array), ArrayLiterals.of(indices));
			}
		}

		@Nested
		class ForBoolean implements BooleanArrayMixin {

			@Override
			public Class<?> getTestTargetClass() { return BooleanBatchAccess.class; }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public BooleanListExpression<boolean[]> createWithValue(boolean[] source) {
				boolean[] array = new boolean[source.length*2];
				for (int i = 0; i < array.length; i++) {
					array[i] = i%2==1;
				}
				int[] indices = IntStream.range(0, source.length).map(i -> i*2).toArray();

				for (int i = 0; i < indices.length; i++) {
					array[indices[i]] = source[i];
				}

				return ListAccess.filter(ArrayLiterals.of(array), ArrayLiterals.of(indices));
			}
		}

		@Nested
		class ForObject implements TextArrayMixin {

			@Override
			public Class<?> getTestTargetClass() { return ObjectBatchAccess.class; }

			@Override
			public boolean nativeConstant() { return false; }

			@SuppressWarnings("unchecked")
			@Override
			public ListExpression<CharSequence[], CharSequence> createWithValue(CharSequence[] source) {
				CharSequence[] array = IntStream.range(0, source.length*2)
						.mapToObj(i -> "item_"+i)
						.toArray(CharSequence[]::new);
				int[] indices = IntStream.range(0, source.length).map(i -> i*2).toArray();

				for (int i = 0; i < indices.length; i++) {
					array[indices[i]] = source[i];
				}

				return (ListExpression<CharSequence[], CharSequence>)
						ListAccess.filter(ArrayLiterals.ofGeneric(array), ArrayLiterals.of(indices));
			}
		}
	}

	@Nested
	class Wrapper {

		@Nested
		class ForInteger implements LongArrayMixin {

			@Override
			public Class<?> getTestTargetClass() { return IntegerListWrapper.class; }

			@Override
			public boolean nativeConstant() { return false; }

			@SuppressWarnings("unchecked")
			@Override
			public IntegerListExpression<long[]> createWithValue(long[] source) {
				Expression<?>[] array = LongStream.of(source)
						.mapToObj(Literals::of)
						.toArray(Expression[]::new);

				return (IntegerListExpression<long[]>) ListAccess.wrap(array);
			}
		}

		@Nested
		class ForFloatingPoint implements DoubleArrayMixin {

			@Override
			public Class<?> getTestTargetClass() { return FloatingPointListWrapper.class; }

			@Override
			public boolean nativeConstant() { return false; }

			@SuppressWarnings("unchecked")
			@Override
			public FloatingPointListExpression<double[]> createWithValue(double[] source) {
				Expression<?>[] array = DoubleStream.of(source)
						.mapToObj(Literals::of)
						.toArray(Expression[]::new);

				return (FloatingPointListExpression<double[]>) ListAccess.wrap(array);
			}
		}

		@Nested
		class ForBoolean implements BooleanArrayMixin {

			@Override
			public Class<?> getTestTargetClass() { return BooleanListWrapper.class; }

			@Override
			public boolean nativeConstant() { return false; }

			@SuppressWarnings("unchecked")
			@Override
			public BooleanListExpression<boolean[]> createWithValue(boolean[] source) {
				Expression<?>[] array = new Expression[source.length];
				for (int i = 0; i < array.length; i++) {
					array[i] = Literals.of(source[i]);
				}

				return (BooleanListExpression<boolean[]>) ListAccess.wrap(array);
			}
		}

		@Nested
		class ForObject implements TextArrayMixin {

			@Override
			public Class<?> getTestTargetClass() { return ObjectWrapper.class; }

			@Override
			public boolean nativeConstant() { return false; }

			@SuppressWarnings("unchecked")
			@Override
			public ListExpression<CharSequence[], CharSequence> createWithValue(CharSequence[] source) {
				Expression<?>[] array = Stream.of(source)
						.map(Literals::of)
						.toArray(Expression[]::new);

				return (ListExpression<CharSequence[], CharSequence>) ListAccess.wrap(array);
			}
		}
	}

	@Nested
	class Unwrapper {

		@Test
		@RandomizedTest
		void testInteger(RandomGenerator rng) {
			long[] array = rng.randomLongs(10, Long.MIN_VALUE, Long.MAX_VALUE);
			Expression<?>[] elements = ListAccess.unwrap(ArrayLiterals.of(array));
			assertThat(elements).hasSize(array.length);
			for (int i = 0; i < elements.length; i++) {
				assertThat(elements[i].computeAsLong())
					.as("Mismatch at index %d", _int(i))
					.isEqualTo(array[i]);
			}
		}

		@Test
		@RandomizedTest
		void testFloatingPoint(RandomGenerator rng) {
			double[] array = rng.randomDoubles(10);
			Expression<?>[] elements = ListAccess.unwrap(ArrayLiterals.of(array));
			assertThat(elements).hasSize(array.length);
			for (int i = 0; i < elements.length; i++) {
				assertThat(elements[i].computeAsDouble())
					.as("Mismatch at index %d", _int(i))
					.isEqualTo(array[i]);
			}
		}

		@Test
		@RandomizedTest
		void testBoolean(RandomGenerator rng) {
			boolean[] array = new boolean[10];
			for (int i = 0; i < array.length; i++) {
				array[i] = rng.nextBoolean();
			}
			Expression<?>[] elements = ListAccess.unwrap(ArrayLiterals.of(array));
			assertThat(elements).hasSize(array.length);
			for (int i = 0; i < elements.length; i++) {
				assertThat(elements[i].computeAsBoolean())
					.as("Mismatch at index %d", _int(i))
					.isEqualTo(array[i]);
			}
		}

		@Test
		@RandomizedTest
		void testGeneric(RandomGenerator rng) {
			CharSequence[] array = new CharSequence[10];
			for (int i = 0; i < array.length; i++) {
				array[i] = rng.randomUnicodeString(10);
			}
			Expression<?>[] elements = ListAccess.unwrap(ArrayLiterals.ofGeneric(array));
			assertThat(elements).hasSize(array.length);
			for (int i = 0; i < elements.length; i++) {
				assertThat(elements[i].compute())
					.as("Mismatch at index %d", _int(i))
					.isEqualTo(array[i]);
			}
		}

		@Test
		void unwrapEmpty() {
			assertThat(ListAccess.unwrap(dynamicLongs(() -> new long[0]))).isEmpty();
		}
	}
}
