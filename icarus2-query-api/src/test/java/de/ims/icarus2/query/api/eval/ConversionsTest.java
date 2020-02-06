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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.eval.Conversions.BooleanCast;
import de.ims.icarus2.query.api.eval.Conversions.FloatingPointCast;
import de.ims.icarus2.query.api.eval.Conversions.IntegerCast;
import de.ims.icarus2.query.api.eval.Conversions.TextCast;
import de.ims.icarus2.query.api.eval.Expression.BooleanExpression;
import de.ims.icarus2.query.api.eval.Expression.NumericalExpression;
import de.ims.icarus2.query.api.eval.Expression.TextExpression;
import de.ims.icarus2.query.api.eval.ExpressionTest.BooleanExpressionTest;
import de.ims.icarus2.query.api.eval.ExpressionTest.FloatingPointExpressionTest;
import de.ims.icarus2.query.api.eval.ExpressionTest.IntegerExpressionTest;
import de.ims.icarus2.query.api.eval.ExpressionTest.TextExpressionTest;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.MutablePrimitives.MutableDouble;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.MutablePrimitives.Primitive;
import de.ims.icarus2.util.strings.CodePointSequence;
import de.ims.icarus2.util.strings.StringPrimitives;

/**
 * @author Markus Gärtner
 *
 */
class ConversionsTest {

	private static IcarusRuntimeException assertFailedCast(Executable executable) {
		return assertIcarusException(QueryErrorCode.INCORRECT_USE, executable);
	}

	@Nested
	class ToText {

		@Test
		@RandomizedTest
		void testFromText(RandomGenerator rng) {
			TextExpression source = Literals.of(rng.randomUnicodeString(10));
			assertThat(Conversions.toText(source)).isSameAs(source);
		}

		@Test
		@RandomizedTest
		void testFromBoolean(RandomGenerator rng) {
			BooleanExpression source = Literals.of(rng.nextBoolean());
			TextExpression instance = Conversions.toText(source);
			assertThat(instance.computeAsChars()).hasToString(String.valueOf(source.computeAsBoolean()));
		}

		@Test
		@RandomizedTest
		void testFromInteger(RandomGenerator rng) {
			NumericalExpression source = Literals.of(rng.nextInt());
			TextExpression instance = Conversions.toText(source);
			assertThat(instance.computeAsChars()).hasToString(String.valueOf(source.computeAsLong()));
		}

		@Test
		@RandomizedTest
		void testFromFloatingPoint(RandomGenerator rng) {
			NumericalExpression source = Literals.of(rng.nextDouble());
			TextExpression instance = Conversions.toText(source);
			assertThat(instance.computeAsChars()).hasToString(String.valueOf(source.computeAsDouble()));
		}

		@Test
		@RandomizedTest
		void testFromGeneric(RandomGenerator rng) {
			String value = rng.randomUnicodeString(10);
			Expression<Object> source = ExpressionTestUtils.generic(value);
			TextExpression instance = Conversions.toText(source);
			assertThat(instance.computeAsChars()).hasToString(value);
		}

		abstract class ToTextTestBase implements TextExpressionTest {

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return TextCast.class; }
		}

		@Nested
		class FromBoolean extends ToTextTestBase {

			@Override
			public CodePointSequence constant() { return CodePointSequence.fixed(String.valueOf(true)); }

			@Override
			public CodePointSequence random(RandomGenerator rng) {
				return CodePointSequence.fixed(String.valueOf(rng.nextBoolean()));
			}

			@Override
			public TextExpression createWithValue(CodePointSequence value) {
				Expression<?> source = Literals.of(StringPrimitives.parseBoolean(value));
				return Conversions.toText(source);
			}
		}

		@Nested
		class FromInteger extends ToTextTestBase {

			@Override
			public CodePointSequence constant() { return CodePointSequence.fixed(String.valueOf(1234)); }

			@Override
			public CodePointSequence random(RandomGenerator rng) {
				return CodePointSequence.fixed(String.valueOf(rng.nextInt()));
			}

			@Override
			public TextExpression createWithValue(CodePointSequence value) {
				Expression<?> source = Literals.of(StringPrimitives.parseLong(value));
				return Conversions.toText(source);
			}
		}

		@Nested
		class FromFloatingPoint extends ToTextTestBase {

			@Override
			public CodePointSequence constant() { return CodePointSequence.fixed(String.valueOf(1234.5678)); }

			@Override
			public CodePointSequence random(RandomGenerator rng) {
				return CodePointSequence.fixed(String.valueOf(rng.nextDouble()));
			}

			@Override
			public TextExpression createWithValue(CodePointSequence value) {
				Expression<?> source = Literals.of(StringPrimitives.parseDouble(value));
				return Conversions.toText(source);
			}
		}

		@Nested
		class FromGeneric extends ToTextTestBase {

			@Override
			public CodePointSequence constant() { return CodePointSequence.fixed("test"); }

			@Override
			public CodePointSequence random(RandomGenerator rng) {
				return CodePointSequence.fixed(rng.randomUnicodeString(10));
			}

			@Override
			public TextExpression createWithValue(CodePointSequence value) {
				Expression<?> source = ExpressionTestUtils.generic(value.toString());
				return Conversions.toText(source);
			}
		}
	}

	@Nested
	class ToBoolean {

		@Test
		void testFromText() {
			assertThat(Conversions.toBoolean(Literals.of("")).computeAsBoolean()).isFalse();
			assertThat(Conversions.toBoolean(Literals.of("1234")).computeAsBoolean()).isTrue();
		}

		@Test
		void testFromBoolean() {
			assertThat(Conversions.toBoolean(Literals.of(false)).computeAsBoolean()).isFalse();
			assertThat(Conversions.toBoolean(Literals.of(true)).computeAsBoolean()).isTrue();
		}

		@Test
		void testFromInteger() {
			assertThat(Conversions.toBoolean(Literals.of(0)).computeAsBoolean()).isFalse();
			assertThat(Conversions.toBoolean(Literals.of(1234)).computeAsBoolean()).isTrue();
		}

		@Test
		void testFromFloatingPoint() {
			assertThat(Conversions.toBoolean(Literals.of(0.0)).computeAsBoolean()).isFalse();
			assertThat(Conversions.toBoolean(Literals.of(1234.5678)).computeAsBoolean()).isTrue();
		}

		@Test
		void testFromGeneric() {
			assertThat(Conversions.toBoolean(Literals.ofNull()).computeAsBoolean()).isFalse();
			assertThat(Conversions.toBoolean(ExpressionTestUtils.generic("test")).computeAsBoolean()).isTrue();
		}

		abstract class ToBooleanTestBase implements BooleanExpressionTest {

			@Override
			public Primitive<Boolean> constant() { return new MutableBoolean(true); }

			@Override
			public Primitive<Boolean> random(RandomGenerator rng) {
				return new MutableBoolean(rng.nextBoolean());
			}

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return BooleanCast.class; }
		}

		@Nested
		class FromString extends ToBooleanTestBase {
			@Override
			public BooleanExpression createWithValue(Primitive<Boolean> value) {
				Expression<?> source = Literals.of(value.booleanValue() ? "test" : "");
				return Conversions.toBoolean(source);
			}
		}

		@Nested
		class FromInteger extends ToBooleanTestBase {
			@Override
			public BooleanExpression createWithValue(Primitive<Boolean> value) {
				Expression<?> source = Literals.of(value.booleanValue() ? 1234 : 0);
				return Conversions.toBoolean(source);
			}
		}

		@Nested
		class FromFloatingPoint extends ToBooleanTestBase {
			@Override
			public BooleanExpression createWithValue(Primitive<Boolean> value) {
				Expression<?> source = Literals.of(value.booleanValue() ? 1234.5678 : 0.0);
				return Conversions.toBoolean(source);
			}
		}

		@Nested
		class FromGeneric extends ToBooleanTestBase {
			@Override
			public BooleanExpression createWithValue(Primitive<Boolean> value) {
				Expression<?> source = value.booleanValue() ? ExpressionTestUtils.generic("test") : Literals.ofNull();
				return Conversions.toBoolean(source);
			}
		}
	}

	@Nested
	class ToInteger {

		@Test
		@RandomizedTest
		void testFromText(RandomGenerator rng) {
			long value = rng.nextInt();
			TextExpression source = Literals.of(String.valueOf(value));
			NumericalExpression instance = Conversions.toInteger(source);
			assertThat(instance.computeAsLong()).isEqualTo(value);
		}

		@Test
		@RandomizedTest
		void testFromBoolean(RandomGenerator rng) {
			assertFailedCast(() -> Conversions.toInteger(Literals.of(true)));
		}

		@Test
		@RandomizedTest
		void testFromInteger(RandomGenerator rng) {
			NumericalExpression source = Literals.of(rng.nextInt());
			assertThat(Conversions.toInteger(source)).isSameAs(source);
		}

		@Test
		@RandomizedTest
		void testFromFloatingPoint(RandomGenerator rng) {
			double value = rng.nextDouble();
			NumericalExpression source = Literals.of(value);
			NumericalExpression instance = Conversions.toInteger(source);
			assertThat(instance.computeAsLong()).isEqualTo((long) value);
		}

		@Test
		void testFromGeneric() {
			assertFailedCast(() -> Conversions.toInteger(ExpressionTestUtils.generic("test")));
		}

		abstract class ToIntegerTestBase implements IntegerExpressionTest {

			@Override
			public Primitive<? extends Number> constant() { return new MutableLong(1234);}

			@Override
			public Primitive<? extends Number> random(RandomGenerator rng) { return new MutableLong(rng.nextInt()); }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return IntegerCast.class; }
		}

		@Nested
		class FromText extends ToIntegerTestBase {

			@Override
			public NumericalExpression createWithValue(Primitive<? extends Number> value) {
				Expression<?> source = Literals.of(String.valueOf(value.longValue()));
				return Conversions.toInteger(source);
			}
		}

		@Nested
		class FromFloatingPoint extends ToIntegerTestBase {

			@Override
			public NumericalExpression createWithValue(Primitive<? extends Number> value) {
				double val = value.longValue();
				val += Math.signum(val)*0.0001;

				Expression<?> source = Literals.of(val);
				return Conversions.toInteger(source);
			}
		}
	}

	@Nested
	class ToFloatingPoint {

		@Test
		@RandomizedTest
		void testFromText(RandomGenerator rng) {
			double value = rng.nextDouble();
			TextExpression source = Literals.of(String.valueOf(value));
			NumericalExpression instance = Conversions.toFloatingPoint(source);
			assertThat(instance.computeAsDouble()).isEqualTo(value);
		}

		@Test
		@RandomizedTest
		void testFromBoolean(RandomGenerator rng) {
			assertFailedCast(() -> Conversions.toFloatingPoint(Literals.of(true)));
		}

		@Test
		@RandomizedTest
		void testFromInteger(RandomGenerator rng) {
			long value = rng.nextInt();
			NumericalExpression source = Literals.of(value);
			NumericalExpression instance = Conversions.toFloatingPoint(source);
			assertThat(instance.computeAsDouble()).isEqualTo(value);
		}

		@Test
		@RandomizedTest
		void testFromFloatingPoint(RandomGenerator rng) {
			NumericalExpression source = Literals.of(rng.nextDouble());
			assertThat(Conversions.toFloatingPoint(source)).isSameAs(source);
		}

		@Test
		void testFromGeneric() {
			assertFailedCast(() -> Conversions.toFloatingPoint(ExpressionTestUtils.generic("test")));
		}

		abstract class ToFloatingPointTestBase implements FloatingPointExpressionTest {

			@Override
			public Primitive<? extends Number> constant() { return new MutableDouble(1234.5678);}

			@Override
			public Primitive<? extends Number> random(RandomGenerator rng) { return new MutableDouble(rng.nextDouble()); }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return FloatingPointCast.class; }
		}

		@Nested
		class FromText extends ToFloatingPointTestBase {

			@Override
			public NumericalExpression createWithValue(Primitive<? extends Number> value) {
				Expression<?> source = Literals.of(String.valueOf(value.doubleValue()));
				return Conversions.toFloatingPoint(source);
			}
		}

		@Nested
		class FromInteger extends ToFloatingPointTestBase {

			@Override
			public Primitive<? extends Number> constant() { return new MutableDouble(1234.0);}

			@Override
			public Primitive<? extends Number> random(RandomGenerator rng) {
				return new MutableDouble(Math.floor(rng.nextDouble()));
			}

			@Override
			public NumericalExpression createWithValue(Primitive<? extends Number> value) {
				Expression<?> source = Literals.of(value.longValue());
				return Conversions.toFloatingPoint(source);
			}
		}

	}
}
