/**
 *
 */
package de.ims.icarus2.query.api.eval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.query.api.eval.ExpressionTest.BooleanExpressionTest;
import de.ims.icarus2.query.api.eval.ExpressionTest.FloatingPointExpressionTest;
import de.ims.icarus2.query.api.eval.ExpressionTest.IntegerExpressionTest;
import de.ims.icarus2.query.api.eval.UnaryOperations.BooleanNegation;
import de.ims.icarus2.query.api.eval.UnaryOperations.FloatingPointNegation;
import de.ims.icarus2.query.api.eval.UnaryOperations.IntegerBitwiseNegation;
import de.ims.icarus2.query.api.eval.UnaryOperations.IntegerNegation;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.MutablePrimitives.MutableDouble;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.MutablePrimitives.Primitive;

/**
 * @author Markus Gärtner
 *
 */
class UnaryOperationsTest {

	@Nested
	class IntegerMinus implements IntegerExpressionTest {

		@Override
		public Expression<?> createWithValue(Primitive<? extends Number> value) {
			return UnaryOperations.minus(ExpressionTestUtils.optimizable(-value.longValue()));
		}

		@Override
		public boolean nativeConstant() {return false; }

		@Override
		public Class<?> getTestTargetClass() { return IntegerNegation.class; }

		@ParameterizedTest
		@ValueSource(longs = {
				1,
				-1,
				Integer.MAX_VALUE,
				Integer.MIN_VALUE,
		})
		void testMinus(long value) {
			assertThat(UnaryOperations.minus(Literals.of(value)).computeAsLong()).isEqualTo(-value);
		}

		@Test
		void testUnomptimizable() {
			MutableLong value = new MutableLong(999);
			Expression<?> expression = ExpressionTestUtils.dynamic(value::longValue);
			expression = UnaryOperations.minus(expression);
			assertThat(expression.optimize(mock(EvaluationContext.class))).isSameAs(expression);
		}
	}

	@Nested
	class FlaotingPointMinus implements FloatingPointExpressionTest {

		@Override
		public Expression<?> createWithValue(Primitive<? extends Number> value) {
			return UnaryOperations.minus(ExpressionTestUtils.optimizable(-value.doubleValue()));
		}

		@Override
		public boolean nativeConstant() {return false; }

		@Override
		public Class<?> getTestTargetClass() { return FloatingPointNegation.class; }

		@ParameterizedTest
		@ValueSource(doubles = {
				1,
				-1,
				Integer.MAX_VALUE,
				Integer.MIN_VALUE,
				123,456,
				-123.456,
				Float.MAX_VALUE,
				-Float.MAX_VALUE,
		})
		void testMinus(double value) {
			assertThat(UnaryOperations.minus(Literals.of(value)).computeAsDouble()).isEqualTo(-value);
		}

		@Test
		void testUnomptimizable() {
			MutableDouble value = new MutableDouble(999.888);
			Expression<?> expression = ExpressionTestUtils.dynamic(value::doubleValue);
			expression = UnaryOperations.minus(expression);
			assertThat(expression.optimize(mock(EvaluationContext.class))).isSameAs(expression);
		}
	}


	@Nested
	class BooleanNot implements BooleanExpressionTest {

		@Override
		public Expression<Primitive<Boolean>> createWithValue(Primitive<Boolean> value) {
			return UnaryOperations.not(ExpressionTestUtils.optimizable(!value.booleanValue()));
		}

		@Override
		public boolean nativeConstant() {return false; }

		@Override
		public Class<?> getTestTargetClass() { return BooleanNegation.class; }

		@Test
		void testNot() {
			assertThat(UnaryOperations.not(ExpressionTestUtils.optimizable(true))
					.computeAsBoolean()).isEqualTo(false);
			assertThat(UnaryOperations.not(ExpressionTestUtils.optimizable(false))
					.computeAsBoolean()).isEqualTo(true);
		}

		@Test
		void testConstant() {
			assertThat(UnaryOperations.not(Literals.of(true)).computeAsBoolean()).isEqualTo(false);
			assertThat(UnaryOperations.not(Literals.of(false)).computeAsBoolean()).isEqualTo(true);
		}

		@Test
		void testUnomptimizable() {
			MutableBoolean value = new MutableBoolean(true);
			Expression<Primitive<Boolean>> expression = ExpressionTestUtils.dynamic(value::booleanValue);
			expression = UnaryOperations.not(expression);
			assertThat(expression.optimize(mock(EvaluationContext.class))).isSameAs(expression);
		}
	}

	@Nested
	class IntegerBitwiseNot implements IntegerExpressionTest {

		@Override
		public Expression<?> createWithValue(Primitive<? extends Number> value) {
			return UnaryOperations.bitwiseNot(ExpressionTestUtils.optimizable(~value.longValue()));
		}

		@Override
		public boolean nativeConstant() {return false; }

		@Override
		public Class<?> getTestTargetClass() { return IntegerBitwiseNegation.class; }

		@ParameterizedTest
		@ValueSource(longs = {
				1,
				-1,
				Integer.MAX_VALUE,
				Integer.MIN_VALUE,
		})
		void testMinus(long value) {
			assertThat(UnaryOperations.bitwiseNot(Literals.of(value)).computeAsLong()).isEqualTo(~value);
		}

		@Test
		void testUnomptimizable() {
			MutableLong value = new MutableLong(999);
			Expression<?> expression = ExpressionTestUtils.dynamic(value::longValue);
			expression = UnaryOperations.bitwiseNot(expression);
			assertThat(expression.optimize(mock(EvaluationContext.class))).isSameAs(expression);
		}
	}
}
