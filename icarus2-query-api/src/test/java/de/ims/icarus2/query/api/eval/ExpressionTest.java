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
import de.ims.icarus2.util.strings.CodePointSequence;

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
		assertThat(instance.compute()).isEqualTo(value);
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
		assertThat(optimized.isConstant()).isTrue();
		assertThat(optimized.compute()).isEqualTo(value);
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
		assertThat(clone.compute()).isEqualTo(value);
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

	public interface TextExpressionTest extends ExpressionTest<CodePointSequence, TextExpression> {

		@Override
		default TypeInfo getExpectedType() { return TypeInfo.STRING; }

		@Override
		default CodePointSequence constant() {
			return CodePointSequence.fixed("test");
		}

		@Override
		default CodePointSequence random(RandomGenerator rng) {
			return CodePointSequence.fixed(rng.randomUnicodeString(10));
		}

		@Test
		@RandomizedTest
		default void testComputeAsChars(RandomGenerator rng) {
			CodePointSequence origin = random(rng);
			TextExpression instance = createWithValue(origin);
			CharSequence chars = instance.computeAsChars();
			assertThat(chars).isEqualTo(origin);
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
		default TypeInfo getExpectedType() { return TypeInfo.LONG; }

		@Override
		default Primitive<? extends Number> constant() { return new MutableLong(1234); }

		@Override
		default Primitive<? extends Number> random(RandomGenerator rng) { return new MutableLong(rng.nextLong()); }

		@Test
		@RandomizedTest
		default void testComputeAsLong(RandomGenerator rng) {
			long value = rng.nextLong();
			assertThat(createWithValue(new MutableLong(value)).computeAsLong()).isEqualTo(value);
		}

		@Test
		@RandomizedTest
		default void testComputeAsDouble(RandomGenerator rng) {
			long value = rng.nextLong();
			assertThat(createWithValue(new MutableLong(value)).computeAsDouble()).isEqualTo(value);
		}
	}

	public interface FloatingPointExpressionTest extends ExpressionTest<Primitive<? extends Number>, NumericalExpression> {

		@Override
		default TypeInfo getExpectedType() { return TypeInfo.DOUBLE; }

		@Override
		default Primitive<? extends Number> constant() { return new MutableDouble(1234.5678); }

		@Override
		default Primitive<? extends Number> random(RandomGenerator rng) { return new MutableDouble(rng.nextDouble()); }

		@Test
		@RandomizedTest
		default void testComputeAsLong(RandomGenerator rng) {
			double value = rng.nextDouble();
			assertIcarusException(QueryErrorCode.TYPE_MISMATCH,
					() -> createWithValue(new MutableDouble(value)).computeAsLong());
		}

		@Test
		@RandomizedTest
		default void testComputeAsDouble(RandomGenerator rng) {
			double value = rng.nextDouble();
			assertThat(createWithValue(new MutableDouble(value)).computeAsDouble()).isEqualTo(value);
		}
	}

	//TODO add a list expression test once we finalized that interface
}
