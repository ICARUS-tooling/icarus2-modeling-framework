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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.eval.Expression.BooleanExpression;
import de.ims.icarus2.query.api.eval.Expression.NumericalExpression;
import de.ims.icarus2.query.api.eval.Expression.TextExpression;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.MutablePrimitives.MutableDouble;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.MutablePrimitives.Primitive;

/**
 * @author Markus Gärtner
 *
 */
public interface ExpressionTest<T, E extends Expression<T>> extends ApiGuardedTest<E> {

	T constant();

	T random(RandomGenerator rng);

	@Provider
	E createWithValue(T value);

	@Override
	default E createTestInstance(TestSettings settings) {
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
	 * Test method for {@link de.ims.icarus2.query.api.eval.Expression#getResultType()}.
	 */
	@Test
	default void testGetResultType() {
		assertThat(create().getResultType()).isEqualTo(getExpectedType());
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.Expression#compute()}.
	 */
	@Test
	@RandomizedTest
	default void testCompute(RandomGenerator rng) {
		T value = random(rng);
		E instance = createWithValue(value);
		assertThat(instance.compute()).satisfies(newVal -> equals(newVal, value));
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.Expression#isConstant()}.
	 */
	@Test
	default void testIsConstant() {
		assertThat(createWithValue(constant()).isConstant()).isEqualTo(nativeConstant());
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.Expression#optimize(de.ims.icarus2.query.api.eval.EvaluationContext)}.
	 */
	@Test
	default void testOptimize() {
		T value = constant();
		E instance = createWithValue(value);
		Expression<T> optimized = instance.optimize(context());
		assertThat(optimized.isConstant()).isEqualTo(optimizeToConstant());
		assertThat(optimized.compute()).satisfies(newVal -> equals(newVal, value));
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.Expression#duplicate(de.ims.icarus2.query.api.eval.EvaluationContext)}.
	 */
	@Test
	default void testDuplicate() {
		T value = constant();
		E instance = createWithValue(value);
		Expression<T> clone = instance.duplicate(context());
		if(nativeConstant()) {
			assertThat(clone).isSameAs(instance);
		} else {
			assertThat(clone).isNotSameAs(instance);
		}
		assertThat(clone.compute()).satisfies(newVal -> equals(newVal, value));
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.Expression#isText()}.
	 */
	@Test
	default void testIsText() {
		assertThat(create().isText()).isEqualTo(TypeInfo.isText(getExpectedType()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.Expression#isNumerical()}.
	 */
	@Test
	default void testIsNumerical() {
		assertThat(create().isNumerical()).isEqualTo(TypeInfo.isNumerical(getExpectedType()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.Expression#isBoolean()}.
	 */
	@Test
	default void testIsBoolean() {
		assertThat(create().isBoolean()).isEqualTo(TypeInfo.isBoolean(getExpectedType()));
	}

	public interface TextExpressionTest extends ExpressionTest<CharSequence, TextExpression> {

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
			TextExpression instance = createWithValue(origin);
			CharSequence chars = instance.compute();
			assertThat(chars).hasToString(origin.toString());
		}
	}

	public interface BooleanExpressionTest extends ExpressionTest<Primitive<Boolean>, BooleanExpression> {

		@Override
		default TypeInfo getExpectedType() { return TypeInfo.BOOLEAN; }

		@Override
		default Primitive<Boolean> constant() { return new MutableBoolean(true); }

		@Override
		default Primitive<Boolean> random(RandomGenerator rng) { return new MutableBoolean(rng.nextBoolean()); }

		@Test
		default void testComputeAsBoolean() {
			assertThat(createWithValue(new MutableBoolean(true)).computeAsBoolean()).isTrue();
			assertThat(createWithValue(new MutableBoolean(false)).computeAsBoolean()).isFalse();
		}
	}

	public interface IntegerExpressionTest extends ExpressionTest<Primitive<? extends Number>, NumericalExpression> {

		@Override
		default TypeInfo getExpectedType() { return TypeInfo.INTEGER; }

		@Override
		default Primitive<? extends Number> constant() { return new MutableLong(1234); }

		/** We return a random int from the generator to keep some room. */
		@Override
		default Primitive<? extends Number> random(RandomGenerator rng) { return new MutableLong(rng.nextInt()); }

		@Test
		@RandomizedTest
		default void testComputeAsLong(RandomGenerator rng) {
			Primitive<? extends Number> value = random(rng);
			assertThat(createWithValue(value).computeAsLong()).isEqualTo(value.longValue());
		}

		@Test
		@RandomizedTest
		default void testComputeAsDouble(RandomGenerator rng) {
			Primitive<? extends Number> value = random(rng);
			assertThat(createWithValue(value).computeAsDouble()).isEqualTo(value.doubleValue());
		}
	}

	public interface FloatingPointExpressionTest extends ExpressionTest<Primitive<? extends Number>, NumericalExpression> {

		@Override
		default TypeInfo getExpectedType() { return TypeInfo.FLOATING_POINT; }

		@Override
		default Primitive<? extends Number> constant() { return new MutableDouble(1234.5678); }

		/** We pick from float space to make testing easier */
		@Override
		default Primitive<? extends Number> random(RandomGenerator rng) { return new MutableDouble(rng.nextFloat()); }

		@Test
		@RandomizedTest
		default void testComputeAsLong(RandomGenerator rng) {
			Primitive<? extends Number> value = random(rng);
			assertIcarusException(QueryErrorCode.TYPE_MISMATCH,
					() -> createWithValue(value).computeAsLong());
		}

		@Test
		@RandomizedTest
		default void testComputeAsDouble(RandomGenerator rng) {
			Primitive<? extends Number> value = random(rng);
			assertThat(createWithValue(value).computeAsDouble()).isEqualTo(value.doubleValue());
		}
	}

	//TODO add a list expression test once we finalized that interface
}
