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

import static de.ims.icarus2.SharedTestUtils.assertIcarusException;
import static de.ims.icarus2.query.api.exp.ExpressionTestUtils.mockContext;
import static de.ims.icarus2.util.lang.Primitives._int;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.exp.Expression.BooleanListExpression;
import de.ims.icarus2.query.api.exp.Expression.FloatingPointListExpression;
import de.ims.icarus2.query.api.exp.Expression.IntegerListExpression;
import de.ims.icarus2.query.api.exp.Expression.ListExpression;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.guard.ApiGuard;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.MutablePrimitives.MutableDouble;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.MutablePrimitives.Primitive;

/**
 * @author Markus Gärtner
 *
 */
public interface ExpressionTest<T>
		extends ApiGuardedTest<Expression<?>>, GenericTest<Expression<?>> {

	@Override
	default void configureApiGuard(ApiGuard<Expression<?>> apiGuard) {
		ApiGuardedTest.super.configureApiGuard(apiGuard);

		apiGuard.forceAccessible(true);
	}

	T constant();

	T random(RandomGenerator rng);

	@Provider
	Expression<?> createWithValue(T value);

	@Override
	default Expression<?> createTestInstance(TestSettings settings) {
		return settings.process(createWithValue(constant()));
	}

	TypeInfo getExpectedType();

	boolean nativeConstant();

	default boolean equals(T x, T y) {
		return x.equals(y);
	}

	/** Return true if this test case expects the expression instances to always optimize into constants */
	default boolean optimizeToConstant() {
		return true;
	}

	default EvaluationContext context() {
		return mock(EvaluationContext.class);
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.exp.Expression#getResultType()}.
	 */
	@Test
	default void testGetResultType() {
		assertThat(create().getResultType()).isEqualTo(getExpectedType());
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.exp.Expression#compute()}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	@RandomizedTest
	default void testCompute(RandomGenerator rng) {
		T value = random(rng);
		Expression<?> instance = createWithValue(value);
		assertThat(instance.compute()).satisfies(newVal -> equals((T) newVal, value));
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.exp.Expression#isConstant()}.
	 */
	@Test
	default void testIsConstant() {
		assertThat(createWithValue(constant()).isConstant()).isEqualTo(nativeConstant());
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.exp.Expression#optimize(de.ims.icarus2.query.api.exp.EvaluationContext)}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	default void testOptimize() {
		T value = constant();
		Expression<?> instance = createWithValue(value);
		Expression<?> optimized = mockContext().optimize(instance);
		if(optimizeToConstant()) {
			assertThat(optimized.isConstant()).isTrue();
		}
		assertThat(optimized.compute()).satisfies(newVal -> equals((T) newVal, value));
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.exp.Expression#duplicate(de.ims.icarus2.query.api.exp.EvaluationContext)}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	default void testDuplicate() {
		T value = constant();
		Expression<?> instance = createWithValue(value);
		Expression<?> clone = mockContext().duplicate(instance);
		if(nativeConstant()) {
			assertThat(clone).isSameAs(instance);
		} else {
			assertThat(clone).isNotSameAs(instance);
		}
		assertThat(clone.compute()).satisfies(newVal -> equals((T) newVal, value));
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.exp.Expression#isText()}.
	 */
	@Test
	default void testIsText() {
		assertThat(create().isText()).isEqualTo(TypeInfo.isText(getExpectedType()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.exp.Expression#isNumerical()}.
	 */
	@Test
	default void testIsNumerical() {
		assertThat(create().isNumerical()).isEqualTo(TypeInfo.isNumerical(getExpectedType()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.exp.Expression#isBoolean()}.
	 */
	@Test
	default void testIsBoolean() {
		assertThat(create().isBoolean()).isEqualTo(TypeInfo.isBoolean(getExpectedType()));
	}

	@Test
	@RandomizedTest
	default void testComputeAsLong(RandomGenerator rng) {
		assertIcarusException(QueryErrorCode.TYPE_MISMATCH,
				() -> create().computeAsLong());
	}

	@Test
	@RandomizedTest
	default void testComputeAsDouble(RandomGenerator rng) {
		assertIcarusException(QueryErrorCode.TYPE_MISMATCH,
				() -> create().computeAsDouble());
	}

	@Test
	default void testComputeAsBoolean() {
		assertIcarusException(QueryErrorCode.TYPE_MISMATCH,
				() -> create().computeAsBoolean());
	}

	public interface TextExpressionTest extends ExpressionTest<CharSequence> {

		@Override
		default TypeInfo getExpectedType() { return TypeInfo.TEXT; }

		@Override
		default CharSequence constant() {
			return "test";
		}

		@Override
		default CharSequence random(RandomGenerator rng) {
			return rng.randomUnicodeString(10);
		}

		@Test
		@RandomizedTest
		default void testComputeAsChars(RandomGenerator rng) {
			CharSequence origin = random(rng);
			@SuppressWarnings("unchecked")
			Expression<CharSequence> instance = (Expression<CharSequence>) createWithValue(origin);
			CharSequence chars = instance.compute();
			assertThat(chars).hasToString(origin.toString());
		}
	}

	public interface BooleanExpressionTest extends ExpressionTest<Primitive<Boolean>> {

		@Override
		default TypeInfo getExpectedType() { return TypeInfo.BOOLEAN; }

		@Override
		default Primitive<Boolean> constant() { return new MutableBoolean(true); }

		@Override
		default Primitive<Boolean> random(RandomGenerator rng) { return new MutableBoolean(rng.nextBoolean()); }

		@Override
		@Test
		default void testComputeAsBoolean() {
			assertThat(createWithValue(new MutableBoolean(true)).computeAsBoolean()).isTrue();
			assertThat(createWithValue(new MutableBoolean(false)).computeAsBoolean()).isFalse();
		}
	}

	public interface IntegerExpressionTest extends ExpressionTest<Primitive<? extends Number>> {

		@Override
		default TypeInfo getExpectedType() { return TypeInfo.INTEGER; }

		@Override
		default Primitive<? extends Number> constant() { return new MutableLong(1234); }

		/** We return a random int from the generator to keep some room. */
		@Override
		default Primitive<? extends Number> random(RandomGenerator rng) { return new MutableLong(rng.nextInt()); }

		@Override
		@Test
		@RandomizedTest
		default void testComputeAsLong(RandomGenerator rng) {
			Primitive<? extends Number> value = random(rng);
			assertThat(createWithValue(value).computeAsLong()).isEqualTo(value.longValue());
		}

		@Override
		@Test
		@RandomizedTest
		default void testComputeAsDouble(RandomGenerator rng) {
			Primitive<? extends Number> value = random(rng);
			assertThat(createWithValue(value).computeAsDouble()).isEqualTo(value.doubleValue());
		}
	}

	public interface FloatingPointExpressionTest extends ExpressionTest<Primitive<? extends Number>> {

		@Override
		default TypeInfo getExpectedType() { return TypeInfo.FLOATING_POINT; }

		@Override
		default Primitive<? extends Number> constant() { return new MutableDouble(1234.5678); }

		/** We pick from float space to make testing easier */
		@Override
		default Primitive<? extends Number> random(RandomGenerator rng) { return new MutableDouble(rng.nextFloat()); }

		@Override
		@Test
		@RandomizedTest
		default void testComputeAsDouble(RandomGenerator rng) {
			Primitive<? extends Number> value = random(rng);
			assertThat(createWithValue(value).computeAsDouble()).isEqualTo(value.doubleValue());
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 * @param <T> type of list object
	 * @param <V> element type of list object
	 * @param <E> type of list expression under test
	 */
	public interface ListExpressionTest<T, V> extends ExpressionTest<T> {

		T sized(int size);

		default boolean expectFixedSize() {
			return true;
		}

		TypeInfo getExpectedElementType();

		@SuppressWarnings("unchecked")
		@Override
		default ListExpression<T, V> create() {
			return (ListExpression<T, V>) ExpressionTest.super.create();
		}

		@Override
		ListExpression<T, V> createWithValue(T value);

		/**
		 * Test method for {@link de.ims.icarus2.query.api.exp.Expression.ListExpression#getElementType()}.
		 */
		@Test
		default void testGetElementType() {
			assertThat(create().getElementType()).isEqualTo(getExpectedElementType());
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.exp.Expression.ListExpression#isFixedSize()}.
		 */
		@Test
		default void testIsFixedSize() {
			assertThat(create().isFixedSize()).isEqualTo(expectFixedSize());
		}

		@ParameterizedTest
		@ValueSource(ints = {1, 10, 10_000})
		default void testSize(int size) {
			assertThat(createWithValue(sized(size)).size()).isEqualTo(size);
		}
	}

	public interface TextListExpressionTest<T>
			extends ListExpressionTest<T, CharSequence> {

		@Provider
		ListExpression<T, CharSequence> createWithValue(CharSequence[] source);

		@Override
		default TypeInfo getExpectedElementType() { return TypeInfo.TEXT; }

		CharSequence[] randomForGet(RandomGenerator rng);

		@RandomizedTest
		@Test
		default void testGet(RandomGenerator rng) {
			CharSequence[] values = randomForGet(rng);
			ListExpression<T, CharSequence> list = createWithValue(values);
			assertThat(list.size()).isEqualTo(values.length);
			for (int i = 0; i < values.length; i++) {
				assertThat(list.get(i))
					.as("Mismatch at index %d", _int(i))
					.isEqualTo(values[i]);
			}
		}
	}

	public interface IntegerListExpressionTest<T>
			extends ListExpressionTest<T, Primitive<Long>> {

		@Provider
		IntegerListExpression<T> createWithValue(long[] source);

		@Override
		default TypeInfo getExpectedElementType() { return TypeInfo.INTEGER; }

		@RandomizedTest
		@Test
		default void testGet(RandomGenerator rng) {
			long[] values = rng.ints(10).mapToLong(i->i).toArray();
			IntegerListExpression<T> list = createWithValue(values);
			assertThat(list.size()).isEqualTo(values.length);
			for (int i = 0; i < values.length; i++) {
				assertThat(list.get(i).longValue())
					.as("Mismatch at index %d", _int(i))
					.isEqualTo(values[i]);
			}
		}

		@RandomizedTest
		@Test
		default void testGetAsLong(RandomGenerator rng) {
			long[] values = rng.ints(10).mapToLong(i->i).toArray();
			IntegerListExpression<T> list = createWithValue(values);
			assertThat(list.size()).isEqualTo(values.length);
			for (int i = 0; i < values.length; i++) {
				assertThat(list.getAsLong(i))
					.as("Mismatch at index %d", _int(i))
					.isEqualTo(values[i]);
			}
		}
	}

	public interface FloatingPointListExpressionTest<T>
			extends ListExpressionTest<T, Primitive<Double>> {

		@Provider
		FloatingPointListExpression<T> createWithValue(double[] source);

		@Override
		default TypeInfo getExpectedElementType() { return TypeInfo.FLOATING_POINT; }

		@RandomizedTest
		@Test
		default void testGet(RandomGenerator rng) {
			double[] values = rng.ints(10).mapToDouble(i->i).toArray();
			FloatingPointListExpression<T> list = createWithValue(values);
			assertThat(list.size()).isEqualTo(values.length);
			for (int i = 0; i < values.length; i++) {
				assertThat(list.get(i).doubleValue())
					.as("Mismatch at index %d", _int(i))
					.isEqualTo(values[i]);
			}
		}

		@RandomizedTest
		@Test
		default void testGetAsDouble(RandomGenerator rng) {
			double[] values = rng.ints(10).mapToDouble(i->i).toArray();
			FloatingPointListExpression<T> list = createWithValue(values);
			assertThat(list.size()).isEqualTo(values.length);
			for (int i = 0; i < values.length; i++) {
				assertThat(list.getAsDouble(i))
					.as("Mismatch at index %d", _int(i))
					.isEqualTo(values[i]);
			}
		}
	}

	public interface BooleanListExpressionTest<T>
			extends ListExpressionTest<T, Primitive<Boolean>> {

		@Provider
		BooleanListExpression<T> createWithValue(boolean[] source);

		@Override
		default TypeInfo getExpectedElementType() { return TypeInfo.BOOLEAN; }

		@RandomizedTest
		@Test
		default void testGet(RandomGenerator rng) {
			boolean[] values = new boolean[] {
					rng.nextBoolean(),
					rng.nextBoolean(),
					rng.nextBoolean(),
					rng.nextBoolean(),
					rng.nextBoolean(),
			};
			BooleanListExpression<T> list = createWithValue(values);
			assertThat(list.size()).isEqualTo(values.length);
			for (int i = 0; i < values.length; i++) {
				assertThat(list.get(i).booleanValue())
					.as("Mismatch at index %d", _int(i))
					.isEqualTo(values[i]);
			}
		}

		@RandomizedTest
		@Test
		default void testGetAsLong(RandomGenerator rng) {
			boolean[] values = new boolean[] {
					rng.nextBoolean(),
					rng.nextBoolean(),
					rng.nextBoolean(),
					rng.nextBoolean(),
					rng.nextBoolean(),
			};
			BooleanListExpression<T> list = createWithValue(values);
			assertThat(list.size()).isEqualTo(values.length);
			for (int i = 0; i < values.length; i++) {
				assertThat(list.getAsBoolean(i))
					.as("Mismatch at index %d", _int(i))
					.isEqualTo(values[i]);
			}
		}
	}

	//TODO add a list expression test once we finalized that interface
}
