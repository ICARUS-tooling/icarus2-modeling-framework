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

import static org.junit.jupiter.api.Assertions.fail;

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
import de.ims.icarus2.query.api.eval.ExpressionTest.BooleanListExpressionTest;
import de.ims.icarus2.query.api.eval.ExpressionTest.FloatingPointExpressionTest;
import de.ims.icarus2.query.api.eval.ExpressionTest.FloatingPointListExpressionTest;
import de.ims.icarus2.query.api.eval.ExpressionTest.IntegerExpressionTest;
import de.ims.icarus2.query.api.eval.ExpressionTest.IntegerListExpressionTest;
import de.ims.icarus2.query.api.eval.ExpressionTest.ListExpressionTest;
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
		class ForInteger implements IntegerListExpressionTest<long[]> {

			@Override
			public long[] sized(int size) {
				return LongStream.range(0, size).toArray();
			}

			@Override
			public long[] constant() {
				return new long[] {
						0,
						1,
						Integer.MAX_VALUE,
						Long.MAX_VALUE,
						Integer.MIN_VALUE,
						Long.MIN_VALUE,
				};
			}

			@Override
			public long[] random(RandomGenerator rng) {
				return new long[] {
						rng.nextLong(),
						rng.nextLong(),
						rng.nextLong(),
						rng.nextLong(),
						rng.nextLong(),
						rng.nextLong(),
				};
			}

			@Override
			public TypeInfo getExpectedType() { return TypeInfo.of(long[].class, true); }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return IntegerBatchAccess.class; }

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
		class ForFloatingPoint implements FloatingPointListExpressionTest<double[]> {

			@Override
			public double[] sized(int size) {
				return DoubleStream.iterate(1.0, i -> -i*2.5).limit(size).toArray();
			}

			@Override
			public double[] constant() {
				return new double[] {
						0.1,
						1.1,
						Integer.MAX_VALUE+0.5,
						Long.MAX_VALUE-0.5,
						Integer.MIN_VALUE+.05,
						Long.MIN_VALUE+0.5,
				};
			}

			@Override
			public double[] random(RandomGenerator rng) {
				return new double[] {
						rng.nextDouble(),
						rng.nextDouble(),
						rng.nextDouble(),
						rng.nextDouble(),
						rng.nextDouble(),
						rng.nextDouble(),
				};
			}

			@Override
			public TypeInfo getExpectedType() { return TypeInfo.of(double[].class, true); }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return FloatingPointBatchAccess.class; }

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
		class ForBoolean implements BooleanListExpressionTest<boolean[]> {

			@Override
			public boolean[] sized(int size) {
				boolean[] data = new boolean[size];
				for (int i = 0; i < data.length; i++) {
					data[i] = i%2==0;
				}
				return data;
			}

			@Override
			public boolean[] constant() {
				return new boolean[] {
						false,
						true,
						true,
						false,
						true,
						false,
				};
			}

			@Override
			public boolean[] random(RandomGenerator rng) {
				return new boolean[] {
						rng.nextBoolean(),
						rng.nextBoolean(),
						rng.nextBoolean(),
						rng.nextBoolean(),
						rng.nextBoolean(),
						rng.nextBoolean(),
				};
			}

			@Override
			public TypeInfo getExpectedType() { return TypeInfo.of(boolean[].class, true); }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return BooleanBatchAccess.class; }

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
		class ForObject implements ListExpressionTest<CharSequence[], CharSequence> {

			@Override
			public CharSequence[] sized(int size) {
				return IntStream.range(0, size)
					.mapToObj(i -> "item_"+i)
					.toArray(CharSequence[]::new);
			}

			@Override
			public CharSequence[] constant() {
				return new CharSequence[] {
						"test",
						"test2",
						CodePointUtilsTest.test,
						CodePointUtilsTest.test_hebrew,
						CodePointUtilsTest.test_mixed2,
						CodePointUtilsTest.test_mixed3,
				};
			}

			@Override
			public CharSequence[] random(RandomGenerator rng) {
				return new CharSequence[] {
						rng.randomUnicodeString(10),
						rng.randomUnicodeString(5),
						rng.randomUnicodeString(15),
						rng.randomUnicodeString(22),
						rng.randomUnicodeString(25),
						rng.randomUnicodeString(2),
				};
			}

			@Override
			public TypeInfo getExpectedType() { return TypeInfo.of(CharSequence[].class, true); }

			@Override
			public TypeInfo getExpectedElementType() { return TypeInfo.TEXT; }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return ObjectBatchAccess.class; }

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
		class ForInteger implements IntegerListExpressionTest<long[]> {

			@Override
			public long[] sized(int size) {
				return LongStream.range(0, size).toArray();
			}

			@Override
			public long[] constant() {
				return new long[] {
						0,
						1,
						Integer.MAX_VALUE,
						Long.MAX_VALUE,
						Integer.MIN_VALUE,
						Long.MIN_VALUE,
				};
			}

			@Override
			public long[] random(RandomGenerator rng) {
				return new long[] {
						rng.nextLong(),
						rng.nextLong(),
						rng.nextLong(),
						rng.nextLong(),
						rng.nextLong(),
						rng.nextLong(),
				};
			}

			@Override
			public TypeInfo getExpectedType() { return TypeInfo.of(long[].class, true); }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return IntegerListWrapper.class; }

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
		class ForFloatingPoint implements FloatingPointListExpressionTest<double[]> {

			@Override
			public double[] sized(int size) {
				return DoubleStream.iterate(1.0, i -> -i*2.5).limit(size).toArray();
			}

			@Override
			public double[] constant() {
				return new double[] {
						0.1,
						1.1,
						Integer.MAX_VALUE+0.5,
						Long.MAX_VALUE-0.5,
						Integer.MIN_VALUE+.05,
						Long.MIN_VALUE+0.5,
				};
			}

			@Override
			public double[] random(RandomGenerator rng) {
				return new double[] {
						rng.nextDouble(),
						rng.nextDouble(),
						rng.nextDouble(),
						rng.nextDouble(),
						rng.nextDouble(),
						rng.nextDouble(),
				};
			}

			@Override
			public TypeInfo getExpectedType() { return TypeInfo.of(double[].class, true); }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return FloatingPointListWrapper.class; }

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
		class ForBoolean implements BooleanListExpressionTest<boolean[]> {

			@Override
			public boolean[] sized(int size) {
				boolean[] data = new boolean[size];
				for (int i = 0; i < data.length; i++) {
					data[i] = i%2==0;
				}
				return data;
			}

			@Override
			public boolean[] constant() {
				return new boolean[] {
						false,
						true,
						true,
						false,
						true,
						false,
				};
			}

			@Override
			public boolean[] random(RandomGenerator rng) {
				return new boolean[] {
						rng.nextBoolean(),
						rng.nextBoolean(),
						rng.nextBoolean(),
						rng.nextBoolean(),
						rng.nextBoolean(),
						rng.nextBoolean(),
				};
			}

			@Override
			public TypeInfo getExpectedType() { return TypeInfo.of(boolean[].class, true); }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return BooleanListWrapper.class; }

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
		class ForObject implements ListExpressionTest<CharSequence[], CharSequence> {

			@Override
			public CharSequence[] sized(int size) {
				return IntStream.range(0, size)
					.mapToObj(i -> "item_"+i)
					.toArray(CharSequence[]::new);
			}

			@Override
			public CharSequence[] constant() {
				return new CharSequence[] {
						"test",
						"test2",
						CodePointUtilsTest.test,
						CodePointUtilsTest.test_hebrew,
						CodePointUtilsTest.test_mixed2,
						CodePointUtilsTest.test_mixed3,
				};
			}

			@Override
			public CharSequence[] random(RandomGenerator rng) {
				return new CharSequence[] {
						rng.randomUnicodeString(10),
						rng.randomUnicodeString(5),
						rng.randomUnicodeString(15),
						rng.randomUnicodeString(22),
						rng.randomUnicodeString(25),
						rng.randomUnicodeString(2),
				};
			}

			@Override
			public TypeInfo getExpectedType() { return TypeInfo.of(CharSequence[].class, true); }

			@Override
			public TypeInfo getExpectedElementType() { return TypeInfo.TEXT; }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return ObjectWrapper.class; }

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

}
