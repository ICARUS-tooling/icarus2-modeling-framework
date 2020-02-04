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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.query.api.eval.Expression.BooleanExpression;
import de.ims.icarus2.query.api.eval.Expression.NumericalExpression;
import de.ims.icarus2.query.api.eval.Expression.TextExpression;
import de.ims.icarus2.query.api.eval.ExpressionTest.BooleanExpressionTest;
import de.ims.icarus2.query.api.eval.ExpressionTest.FloatingPointExpressionTest;
import de.ims.icarus2.query.api.eval.ExpressionTest.IntegerExpressionTest;
import de.ims.icarus2.query.api.eval.ExpressionTest.TextExpressionTest;
import de.ims.icarus2.query.api.eval.Literals.BooleanLiteral;
import de.ims.icarus2.query.api.eval.Literals.FloatingPointLiteral;
import de.ims.icarus2.query.api.eval.Literals.IntegerLiteral;
import de.ims.icarus2.query.api.eval.Literals.NullLiteral;
import de.ims.icarus2.query.api.eval.Literals.StringLiteral;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.MutablePrimitives.Primitive;
import de.ims.icarus2.util.strings.CodePointSequence;

/**
 * @author Markus Gärtner
 *
 */
class LiteralsTest {

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.Literals#ofNull()}.
	 */
	@Test
	void testOfNull() {
		assertThat(Literals.ofNull())
			.isNotNull()
			.extracting(Expression::compute)
			.isNull();
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.Literals#of(java.lang.CharSequence)}.
	 */
	@Test
	void testOfCharSequence() {
		String value = "test";
		TextExpression expression = Literals.of((CharSequence)value);
		assertThat(expression.computeAsChars()).hasToString(value);
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.Literals#of(java.lang.String)}.
	 */
	@Test
	void testOfString() {
		String value = "test";
		TextExpression expression = Literals.of(value);
		assertThat(expression.computeAsChars()).hasToString(value);
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.Literals#of(de.ims.icarus2.util.strings.CodePointSequence)}.
	 */
	@Test
	void testOfCodePointSequence() {
		CodePointSequence value = CodePointSequence.fixed("test");
		TextExpression expression = Literals.of(value);
		assertThat(expression.compute()).isEqualTo(value);
		assertThat(expression.computeAsChars()).hasToString("test");
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.Literals#of(boolean)}.
	 */
	@Test
	void testOfBoolean() {
		assertThat(Literals.of(true).computeAsBoolean()).isTrue();
		assertThat(Literals.of(false).computeAsBoolean()).isFalse();
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.Literals#of(long)}.
	 */
	@Test
	@RandomizedTest
	void testOfLong(RandomGenerator rng) {
		long value = rng.nextLong();
		assertThat(Literals.of(value).computeAsLong()).isEqualTo(value);
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.eval.Literals#of(double)}.
	 */
	@Test
	@RandomizedTest
	void testOfDouble(RandomGenerator rng) {
		double value = rng.nextDouble();
		assertThat(Literals.of(value).computeAsDouble()).isEqualTo(value);
	}

	abstract class TestBase<T, E extends Expression<T>> implements ExpressionTest<T, E> {

		@Override
		public boolean nativeConstant() { return true; }

		@Test
		void testIsLiteral() {
			assertThat(Literals.isLiteral(create())).isTrue();
		}
	}

	@Nested
	class NullLiteralTest extends TestBase<Object, Expression<Object>> {

		@Override
		public Object constant() { return null; }

		@Override
		public Object random(RandomGenerator rng) { return null; }

		@Override
		public Expression<Object> createWithValue(Object value) {
			assertThat(value).isNull();
			return Literals.ofNull();
		}

		@Override
		public TypeInfo getExpectedType() { return TypeInfo.NULL; }

		@Override
		public Class<?> getTestTargetClass() { return NullLiteral.class; }
	}

	@Nested
	class StringLiteralTest extends TestBase<CodePointSequence, TextExpression> implements TextExpressionTest {

		@Override
		public TextExpression createWithValue(CodePointSequence value) { return Literals.of(value); }

		@Override
		public Class<?> getTestTargetClass() { return StringLiteral.class; }
	}

	@Nested
	class BooleanLiteralTest extends TestBase<Primitive<Boolean>, BooleanExpression> implements BooleanExpressionTest {

		@Override
		public BooleanExpression createWithValue(Primitive<Boolean> value) { return Literals.of(value.booleanValue()); }

		@Override
		public Class<?> getTestTargetClass() { return BooleanLiteral.class; }
	}

	@Nested
	class IntegerLiteralTest extends TestBase<Primitive<? extends Number>, NumericalExpression> implements IntegerExpressionTest {

		@Override
		public NumericalExpression createWithValue(Primitive<? extends Number> value) { return Literals.of(value.longValue()); }

		@Override
		public Class<?> getTestTargetClass() { return IntegerLiteral.class; }
	}

	@Nested
	class FloatingPointLiteralTest extends TestBase<Primitive<? extends Number>, NumericalExpression> implements FloatingPointExpressionTest {

		@Override
		public NumericalExpression createWithValue(Primitive<? extends Number> value) { return Literals.of(value.doubleValue()); }

		@Override
		public Class<?> getTestTargetClass() { return FloatingPointLiteral.class; }
	}
}
