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
import static de.ims.icarus2.util.lang.Primitives._int;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.eval.Conversions.BooleanCast;
import de.ims.icarus2.query.api.eval.Conversions.BooleanListCast;
import de.ims.icarus2.query.api.eval.Conversions.FloatingPointCast;
import de.ims.icarus2.query.api.eval.Conversions.FloatingPointListCast;
import de.ims.icarus2.query.api.eval.Conversions.IntegerCast;
import de.ims.icarus2.query.api.eval.Conversions.IntegerListCast;
import de.ims.icarus2.query.api.eval.Conversions.TextCast;
import de.ims.icarus2.query.api.eval.Conversions.TextListCast;
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
import de.ims.icarus2.query.api.eval.ExpressionTest.TextExpressionTest;
import de.ims.icarus2.query.api.eval.ExpressionTest.TextListExpressionTest;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.MutablePrimitives.MutableDouble;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.MutablePrimitives.Primitive;
import de.ims.icarus2.util.strings.StringPrimitives;

/**
 * @author Markus Gärtner
 *
 */
class ConversionsTest {

	private static IcarusRuntimeException assertFailedCast(Executable executable) {
		return assertIcarusException(QueryErrorCode.INCORRECT_USE, executable);
	}

	private static double randomDouble(RandomGenerator rng) {
		return rng.random((double)Integer.MIN_VALUE, (double)Integer.MAX_VALUE);
	}

	@Nested
	class ToText {

		@Test
		@RandomizedTest
		void testFromText(RandomGenerator rng) {
			Expression<CharSequence> source = Literals.of(rng.randomUnicodeString(10));
			assertThat(Conversions.toText(source)).isSameAs(source);
		}

		@Test
		@RandomizedTest
		void testFromBoolean(RandomGenerator rng) {
			Expression<Primitive<Boolean>> source = Literals.of(rng.nextBoolean());
			Expression<CharSequence> instance = Conversions.toText(source);
			assertThat(instance.compute()).hasToString(String.valueOf(source.computeAsBoolean()));
		}

		@Test
		@RandomizedTest
		void testFromInteger(RandomGenerator rng) {
			Expression<?> source = Literals.of(rng.nextInt());
			Expression<CharSequence> instance = Conversions.toText(source);
			assertThat(instance.compute()).hasToString(String.valueOf(source.computeAsLong()));
		}

		@Test
		@RandomizedTest
		void testFromFloatingPoint(RandomGenerator rng) {
			Expression<?> source = Literals.of(randomDouble(rng));
			Expression<CharSequence> instance = Conversions.toText(source);
			assertThat(instance.compute()).hasToString(String.valueOf(source.computeAsDouble()));
		}

		@Test
		@RandomizedTest
		void testFromGeneric(RandomGenerator rng) {
			String value = rng.randomUnicodeString(10);
			Expression<Object> source = ExpressionTestUtils.generic(value);
			Expression<CharSequence> instance = Conversions.toText(source);
			assertThat(instance.compute()).hasToString(value);
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
			public CharSequence constant() { return String.valueOf(true); }

			@Override
			public CharSequence random(RandomGenerator rng) {
				return String.valueOf(rng.nextBoolean());
			}

			@Override
			public Expression<CharSequence> createWithValue(CharSequence value) {
				Expression<?> source = Literals.of(StringPrimitives.parseBoolean(value));
				return Conversions.toText(source);
			}
		}

		@Nested
		class FromInteger extends ToTextTestBase {

			@Override
			public CharSequence constant() { return String.valueOf(1234); }

			@Override
			public CharSequence random(RandomGenerator rng) {
				return String.valueOf(rng.nextInt());
			}

			@Override
			public Expression<CharSequence> createWithValue(CharSequence value) {
				Expression<?> source = Literals.of(StringPrimitives.parseLong(value));
				return Conversions.toText(source);
			}
		}

		@Nested
		class FromFloatingPoint extends ToTextTestBase {

			@Override
			public CharSequence constant() { return String.valueOf(1234.5678); }

			@Override
			public CharSequence random(RandomGenerator rng) {
				return String.valueOf(randomDouble(rng));
			}

			@Override
			public Expression<CharSequence> createWithValue(CharSequence value) {
				Expression<?> source = Literals.of(StringPrimitives.parseDouble(value));
				return Conversions.toText(source);
			}
		}

		@Nested
		class FromGeneric extends ToTextTestBase {

			@Override
			public CharSequence constant() { return "test"; }

			@Override
			public CharSequence random(RandomGenerator rng) {
				return rng.randomUnicodeString(10);
			}

			@Override
			public Expression<CharSequence> createWithValue(CharSequence value) {
				Expression<?> source = ExpressionTestUtils.generic(value.toString());
				return Conversions.toText(source);
			}
		}
	}

	@Nested
	class ToTextList {

		@Test
		@RandomizedTest
		void testFromText(RandomGenerator rng) {
			ListExpression<?, ?> source = ArrayLiterals.ofGeneric(
					rng.randomUnicodeString(5),
					rng.randomUnicodeString(10),
					rng.randomUnicodeString(20),
					rng.randomUnicodeString(15));
			assertThat(Conversions.toTextList(source)).isSameAs(source);
		}

		@Test
		@RandomizedTest
		void testFromBoolean(RandomGenerator rng) {
			ListExpression<?, Primitive<Boolean>> source = ArrayLiterals.of(
					rng.nextBoolean(),
					rng.nextBoolean(),
					rng.nextBoolean(),
					rng.nextBoolean());
			ListExpression<?, CharSequence> instance = Conversions.toTextList(source);
			assertThat(instance.size()).isEqualTo(4);
			for (int i = 0; i < instance.size(); i++) {
				assertThat(instance.get(i)).as("Mismatch at index %d", _int(i))
					.hasToString(String.valueOf(source.get(i).booleanValue()));
			}
		}

		@Test
		@RandomizedTest
		void testFromInteger(RandomGenerator rng) {
			ListExpression<?, Primitive<Long>> source = ArrayLiterals.of(
					rng.nextInt(),
					rng.nextInt(),
					rng.nextInt(),
					rng.nextInt());
			ListExpression<?, CharSequence> instance = Conversions.toTextList(source);
			assertThat(instance.size()).isEqualTo(4);
			for (int i = 0; i < instance.size(); i++) {
				assertThat(instance.get(i)).as("Mismatch at index %d", _int(i))
					.hasToString(String.valueOf(source.get(i).longValue()));
			}
		}

		@Test
		@RandomizedTest
		void testFromFloatingPoint(RandomGenerator rng) {
			ListExpression<?, Primitive<Double>> source = ArrayLiterals.of(
					randomDouble(rng),
					randomDouble(rng),
					randomDouble(rng),
					randomDouble(rng));
			ListExpression<?, CharSequence> instance = Conversions.toTextList(source);
			assertThat(instance.size()).isEqualTo(4);
			for (int i = 0; i < instance.size(); i++) {
				assertThat(instance.get(i)).as("Mismatch at index %d", _int(i))
					.hasToString(String.valueOf(source.get(i).doubleValue()));
			}
		}

		@Test
		@RandomizedTest
		void testFromGeneric() {
			ListExpression<?, ?> source = ArrayLiterals.ofGeneric(
					new Object(),
					new Object(),
					new Object(),
					new Object());
			ListExpression<?, CharSequence> instance = Conversions.toTextList(source);
			assertThat(instance.size()).isEqualTo(4);
			for (int i = 0; i < instance.size(); i++) {
				assertThat(instance.get(i)).as("Mismatch at index %d", _int(i))
					.hasToString(String.valueOf(source.get(i)));
			}
		}

		abstract class ToTextListTestBase implements TextListExpressionTest<CharSequence[]> {

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return TextListCast.class; }

			@Override
			public TypeInfo getExpectedType() { return TypeInfo.of(CharSequence[].class, true); }
		}

		@Nested
		class FromBoolean extends ToTextListTestBase {

			@Override
			public CharSequence[] constant() {
				return new CharSequence[] {
						"true",
						"false",
						"true"
				};
			}

			@Override
			public CharSequence[] random(RandomGenerator rng) {
				return new CharSequence[] {
						String.valueOf(rng.nextBoolean()),
						String.valueOf(rng.nextBoolean()),
						String.valueOf(rng.nextBoolean()),
						String.valueOf(rng.nextBoolean())
				};
			}

			@Override
			public CharSequence[] randomForGet(RandomGenerator rng) { return random(rng); }

			@Override
			public CharSequence[] sized(int size) {
				return IntStream.range(0, size)
						.mapToObj(i -> String.valueOf(i%2==0))
						.toArray(CharSequence[]::new);
			}

			@SuppressWarnings("unchecked")
			@Override
			public ListExpression<CharSequence[], CharSequence> createWithValue(CharSequence[] value) {
				boolean[] content = new boolean[value.length];
				for (int i = 0; i < content.length; i++) {
					content[i] = StringPrimitives.parseBoolean(value[i]);
				}
				return (ListExpression<CharSequence[], CharSequence>)
						Conversions.toTextList(ArrayLiterals.of(content));
			}
		}

		@Nested
		class FromInteger extends ToTextListTestBase {

			@Override
			public CharSequence[] constant() {
				return new CharSequence[] {
						"1",
						"123",
						"-456"
				};
			}

			@Override
			public CharSequence[] random(RandomGenerator rng) {
				return new CharSequence[] {
						String.valueOf(rng.nextInt()),
						String.valueOf(rng.nextInt()),
						String.valueOf(rng.nextInt()),
						String.valueOf(rng.nextInt())
				};
			}

			@Override
			public CharSequence[] randomForGet(RandomGenerator rng) { return random(rng); }

			@Override
			public CharSequence[] sized(int size) {
				return IntStream.range(0, size)
						.mapToObj(i -> String.valueOf(i))
						.toArray(CharSequence[]::new);
			}

			@SuppressWarnings("unchecked")
			@Override
			public ListExpression<CharSequence[], CharSequence> createWithValue(CharSequence[] value) {
				long[] content = new long[value.length];
				for (int i = 0; i < content.length; i++) {
					content[i] = StringPrimitives.parseLong(value[i]);
				}
				return (ListExpression<CharSequence[], CharSequence>)
						Conversions.toTextList(ArrayLiterals.of(content));
			}
		}

		@Nested
		class FromFloatingPoint extends ToTextListTestBase {

			@Override
			public CharSequence[] constant() {
				return new CharSequence[] {
						"1.0",
						"123.456",
						"-456.789"
				};
			}

			@Override
			public CharSequence[] random(RandomGenerator rng) {
				return new CharSequence[] {
						String.valueOf(randomDouble(rng)),
						String.valueOf(randomDouble(rng)),
						String.valueOf(randomDouble(rng)),
						String.valueOf(randomDouble(rng))
				};
			}

			@Override
			public CharSequence[] randomForGet(RandomGenerator rng) { return random(rng); }

			@Override
			public CharSequence[] sized(int size) {
				return IntStream.range(0, size)
						.mapToObj(i -> String.valueOf(i+0.5))
						.toArray(CharSequence[]::new);
			}

			@SuppressWarnings("unchecked")
			@Override
			public ListExpression<CharSequence[], CharSequence> createWithValue(CharSequence[] value) {
				double[] content = new double[value.length];
				for (int i = 0; i < content.length; i++) {
					content[i] = StringPrimitives.parseDouble(value[i]);
				}
				return (ListExpression<CharSequence[], CharSequence>)
						Conversions.toTextList(ArrayLiterals.of(content));
			}
		}

		@Nested
		class FromGeneric extends ToTextListTestBase {

			@Override
			public CharSequence[] constant() {
				return new CharSequence[] {
						"1.0",
						"test",
						CodePointUtilsTest.test,
						CodePointUtilsTest.test_mixed
				};
			}

			@Override
			public CharSequence[] random(RandomGenerator rng) {
				return new CharSequence[] {
						rng.randomUnicodeString(10),
						rng.randomUnicodeString(5),
						rng.randomUnicodeString(15),
						rng.randomUnicodeString(20)
				};
			}

			@Override
			public CharSequence[] randomForGet(RandomGenerator rng) { return random(rng); }

			@Override
			public CharSequence[] sized(int size) {
				return IntStream.range(0, size)
						.mapToObj(i -> String.valueOf(i%2==0))
						.toArray(CharSequence[]::new);
			}

			@SuppressWarnings("unchecked")
			@Override
			public ListExpression<CharSequence[], CharSequence> createWithValue(CharSequence[] value) {
				Object[] content = new Object[value.length];
				for (int i = 0; i < content.length; i++) {
					content[i] = ExpressionTestUtils.dummy(value[i].toString());
				}
				return (ListExpression<CharSequence[], CharSequence>)
						Conversions.toTextList(ArrayLiterals.ofGeneric(content));
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
			public Expression<Primitive<Boolean>> createWithValue(Primitive<Boolean> value) {
				Expression<?> source = Literals.of(value.booleanValue() ? "test" : "");
				return Conversions.toBoolean(source);
			}
		}

		@Nested
		class FromInteger extends ToBooleanTestBase {
			@Override
			public Expression<Primitive<Boolean>> createWithValue(Primitive<Boolean> value) {
				Expression<?> source = Literals.of(value.booleanValue() ? 1234 : 0);
				return Conversions.toBoolean(source);
			}
		}

		@Nested
		class FromFloatingPoint extends ToBooleanTestBase {
			@Override
			public Expression<Primitive<Boolean>> createWithValue(Primitive<Boolean> value) {
				Expression<?> source = Literals.of(value.booleanValue() ? 1234.5678 : 0.0);
				return Conversions.toBoolean(source);
			}
		}

		@Nested
		class FromGeneric extends ToBooleanTestBase {
			@Override
			public Expression<Primitive<Boolean>> createWithValue(Primitive<Boolean> value) {
				Expression<?> source = value.booleanValue() ? ExpressionTestUtils.generic("test") : Literals.ofNull();
				return Conversions.toBoolean(source);
			}
		}
	}

	@Nested
	class ToBooleanList {

		@Test
		@RandomizedTest
		void testFromText(RandomGenerator rng) {
			ListExpression<?, CharSequence> source = ArrayLiterals.ofGeneric(
					String.valueOf(rng.nextBoolean()),
					String.valueOf(rng.nextBoolean()),
					String.valueOf(rng.nextBoolean()),
					String.valueOf(rng.nextBoolean()));
			BooleanListExpression<?> instance = Conversions.toBooleanList(source);
			assertThat(instance.size()).isEqualTo(4);
			for (int i = 0; i < instance.size(); i++) {
				assertThat(instance.getAsBoolean(i)).as("Mismatch at index %d", _int(i))
					.isEqualTo(EvaluationUtils.string2Boolean(source.get(i)));
			}
		}

		@Test
		@RandomizedTest
		void testFromBoolean(RandomGenerator rng) {
			ListExpression<?, ?> source = ArrayLiterals.of(
					rng.nextBoolean(),
					rng.nextBoolean(),
					rng.nextBoolean(),
					rng.nextBoolean());
			assertThat(Conversions.toBooleanList(source)).isSameAs(source);
		}

		@Test
		@RandomizedTest
		void testFromInteger(RandomGenerator rng) {
			IntegerListExpression<?> source = ArrayLiterals.of(
					rng.nextBoolean() ? rng.nextInt(1000)+1 : 0,
					rng.nextBoolean() ? rng.nextInt(1000)+1 : 0,
					rng.nextBoolean() ? rng.nextInt(1000)+1 : 0,
					rng.nextBoolean() ? rng.nextInt(1000)+1 : 0);
			BooleanListExpression<?> instance = Conversions.toBooleanList(source);
			assertThat(instance.size()).isEqualTo(4);
			for (int i = 0; i < instance.size(); i++) {
				assertThat(instance.getAsBoolean(i)).as("Mismatch at index %d", _int(i))
					.isEqualTo(EvaluationUtils.int2Boolean(source.getAsLong(i)));
			}
		}

		@Test
		@RandomizedTest
		void testFromFloatingPoint(RandomGenerator rng) {
			FloatingPointListExpression<?> source = ArrayLiterals.of(
					rng.nextBoolean() ? randomDouble(rng) : 0.0,
					rng.nextBoolean() ? randomDouble(rng) : 0.0,
					rng.nextBoolean() ? randomDouble(rng) : 0.0,
					rng.nextBoolean() ? randomDouble(rng) : 0.0);
			BooleanListExpression<?> instance = Conversions.toBooleanList(source);
			assertThat(instance.size()).isEqualTo(4);
			for (int i = 0; i < instance.size(); i++) {
				assertThat(instance.getAsBoolean(i)).as("Mismatch at index %d", _int(i))
					.isEqualTo(EvaluationUtils.float2Boolean(source.getAsDouble(i)));
			}
		}

		@Test
		@RandomizedTest
		void testFromGeneric(RandomGenerator rng) {
			ListExpression<?, ?> source = ArrayLiterals.ofGeneric(
					rng.nextBoolean() ? new Object() : null,
					rng.nextBoolean() ? new Object() : null,
					rng.nextBoolean() ? new Object() : null,
					rng.nextBoolean() ? new Object() : null);
			BooleanListExpression<?> instance = Conversions.toBooleanList(source);
			assertThat(instance.size()).isEqualTo(4);
			for (int i = 0; i < instance.size(); i++) {
				assertThat(instance.getAsBoolean(i)).as("Mismatch at index %d", _int(i))
					.isEqualTo(EvaluationUtils.object2Boolean(source.get(i)));
			}
		}

		abstract class ToBooleanListTestBase implements BooleanListExpressionTest<boolean[]> {

			@Override
			public boolean[] constant() {
				return new boolean[]{
						true,
						false,
						false,
						true
				};
			}

			@Override
			public boolean[] random(RandomGenerator rng) {
				return new boolean[]{
						rng.nextBoolean(),
						rng.nextBoolean(),
						rng.nextBoolean(),
						rng.nextBoolean()
				};
			}

			@Override
			public boolean[] sized(int size) {
				boolean[] content = new boolean[size];
				for (int i = 0; i < content.length; i++) {
					content[i] = i%2==0;
				}
				return content;
			}

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return BooleanListCast.class; }

			@Override
			public TypeInfo getExpectedType() { return TypeInfo.of(boolean[].class, true); }
		}

		@Nested
		class FromString extends ToBooleanListTestBase {
			@SuppressWarnings("unchecked")
			@Override
			public BooleanListExpression<boolean[]> createWithValue(boolean[] value) {
				CharSequence[] content = new CharSequence[value.length];
				for (int i = 0; i < content.length; i++) {
					content[i] = value[i] ? "item_"+i : "";
				}
				return (BooleanListExpression<boolean[]>)
						Conversions.toBooleanList(ArrayLiterals.ofGeneric(content));
			}
		}

		@Nested
		class FromInteger extends ToBooleanListTestBase {
			@SuppressWarnings("unchecked")
			@Override
			public BooleanListExpression<boolean[]> createWithValue(boolean[] value) {
				long[] content = new long[value.length];
				for (int i = 0; i < content.length; i++) {
					content[i] = value[i] ? 1_000+i : 0L;
				}
				return (BooleanListExpression<boolean[]>)
						Conversions.toBooleanList(ArrayLiterals.of(content));
			}
		}

		@Nested
		class FromFloatingPoint extends ToBooleanListTestBase {
			@SuppressWarnings("unchecked")
			@Override
			public BooleanListExpression<boolean[]> createWithValue(boolean[] value) {
				double[] content = new double[value.length];
				for (int i = 0; i < content.length; i++) {
					content[i] = value[i] ? (1_000+i)*1.1 : 0.0;
				}
				return (BooleanListExpression<boolean[]>)
						Conversions.toBooleanList(ArrayLiterals.of(content));
			}
		}

		@Nested
		class FromGeneric extends ToBooleanListTestBase {
			@SuppressWarnings("unchecked")
			@Override
			public BooleanListExpression<boolean[]> createWithValue(boolean[] value) {
				Object[] content = new Object[value.length];
				for (int i = 0; i < content.length; i++) {
					content[i] = value[i] ? new Object() : null;
				}
				return (BooleanListExpression<boolean[]>)
						Conversions.toBooleanList(ArrayLiterals.ofGeneric(content));
			}
		}
	}

	@Nested
	class ToInteger {

		@Test
		@RandomizedTest
		void testFromText(RandomGenerator rng) {
			long value = rng.nextInt();
			Expression<CharSequence> source = Literals.of(String.valueOf(value));
			Expression<?> instance = Conversions.toInteger(source);
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
			Expression<?> source = Literals.of(rng.nextInt());
			assertThat(Conversions.toInteger(source)).isSameAs(source);
		}

		@Test
		@RandomizedTest
		void testFromFloatingPoint(RandomGenerator rng) {
			double value = randomDouble(rng);
			Expression<?> source = Literals.of(value);
			Expression<?> instance = Conversions.toInteger(source);
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
			public Expression<?> createWithValue(Primitive<? extends Number> value) {
				Expression<?> source = Literals.of(String.valueOf(value.longValue()));
				return Conversions.toInteger(source);
			}
		}

		@Nested
		class FromFloatingPoint extends ToIntegerTestBase {

			@Override
			public Expression<?> createWithValue(Primitive<? extends Number> value) {
				double val = value.longValue();
				val += Math.signum(val)*0.0001;

				Expression<?> source = Literals.of(val);
				return Conversions.toInteger(source);
			}
		}
	}

	@Nested
	class ToIntegerList {

		@Test
		@RandomizedTest
		void testFromText(RandomGenerator rng) {
			ListExpression<?, CharSequence> source = ArrayLiterals.ofGeneric(
					String.valueOf(rng.nextInt()),
					String.valueOf(rng.nextInt()),
					String.valueOf(rng.nextInt()),
					String.valueOf(rng.nextInt()));
			IntegerListExpression<?> instance = Conversions.toIntegerList(source);
			assertThat(instance.size()).isEqualTo(4);
			for (int i = 0; i < instance.size(); i++) {
				assertThat(instance.getAsLong(i)).as("Mismatch at index %d", _int(i))
					.isEqualTo(StringPrimitives.parseLong(source.get(i)));
			}
		}

		@Test
		@RandomizedTest
		void testFromBoolean(RandomGenerator rng) {
			assertFailedCast(() -> Conversions.toIntegerList(ArrayLiterals.of(true, false, true)));
		}

		@Test
		@RandomizedTest
		void testFromInteger(RandomGenerator rng) {
			ListExpression<?, Primitive<Long>> source = ArrayLiterals.of(
					rng.nextInt(),
					rng.nextInt(),
					rng.nextInt(),
					rng.nextInt());
			assertThat(Conversions.toIntegerList(source)).isSameAs(source);
		}

		@Test
		@RandomizedTest
		void testFromFloatingPoint(RandomGenerator rng) {
			FloatingPointListExpression<?> source = ArrayLiterals.of(
					randomDouble(rng),
					randomDouble(rng),
					randomDouble(rng),
					randomDouble(rng));
			IntegerListExpression<?> instance = Conversions.toIntegerList(source);
			assertThat(instance.size()).isEqualTo(4);
			for (int i = 0; i < instance.size(); i++) {
				assertThat(instance.getAsLong(i)).as("Mismatch at index %d", _int(i))
					.isEqualTo((long)source.getAsDouble(i));
			}
		}

		@Test
		void testFromGeneric() {
			assertFailedCast(() -> Conversions.toIntegerList(
					ArrayLiterals.ofGeneric(new Object(), "test", new Object())));
		}

		abstract class ToIntegerListTestBase implements IntegerListExpressionTest<long[]> {

			@Override
			public long[] constant() {
				return new long[]{
						0,
						10,
						10_000,
						-1000
				};
			}

			@Override
			public long[] random(RandomGenerator rng) {
				return new long[]{
						rng.nextInt(),
						rng.nextInt(),
						rng.nextInt(),
						rng.nextInt()
				};
			}

			@Override
			public long[] sized(int size) {
				long[] content = new long[size];
				for (int i = 0; i < content.length; i++) {
					content[i] = i+1000L;
				}
				return content;
			}

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return IntegerListCast.class; }

			@Override
			public TypeInfo getExpectedType() { return TypeInfo.of(long[].class, true); }
		}

		@Nested
		class FromText extends ToIntegerListTestBase {

			@SuppressWarnings("unchecked")
			@Override
			public IntegerListExpression<long[]> createWithValue(long[] value) {
				CharSequence[] content = new CharSequence[value.length];
				for (int i = 0; i < content.length; i++) {
					content[i] = String.valueOf(value[i]);
				}
				return (IntegerListExpression<long[]>)
						Conversions.toIntegerList(ArrayLiterals.ofGeneric(content));
			}
		}

		@Nested
		class FromFloatingPoint extends ToIntegerListTestBase {

			@SuppressWarnings("unchecked")
			@Override
			public IntegerListExpression<long[]> createWithValue(long[] value) {
				double[] content = new double[value.length];
				for (int i = 0; i < content.length; i++) {
					content[i] = value[i] + Math.signum(value[i])*0.000;
				}
				return (IntegerListExpression<long[]>)
						Conversions.toIntegerList(ArrayLiterals.of(content));
			}
		}
	}

	@Nested
	class ToFloatingPoint {

		@Test
		@RandomizedTest
		void testFromText(RandomGenerator rng) {
			double value = randomDouble(rng);
			Expression<CharSequence> source = Literals.of(String.valueOf(value));
			Expression<?> instance = Conversions.toFloatingPoint(source);
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
			Expression<?> source = Literals.of(value);
			Expression<?> instance = Conversions.toFloatingPoint(source);
			assertThat(instance.computeAsDouble()).isEqualTo(value);
		}

		@Test
		@RandomizedTest
		void testFromFloatingPoint(RandomGenerator rng) {
			Expression<?> source = Literals.of(randomDouble(rng));
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
			public Primitive<? extends Number> random(RandomGenerator rng) { return new MutableDouble(randomDouble(rng)); }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return FloatingPointCast.class; }
		}

		@Nested
		class FromText extends ToFloatingPointTestBase {

			@Override
			public Expression<?> createWithValue(Primitive<? extends Number> value) {
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
				return new MutableDouble(Math.floor(randomDouble(rng)));
			}

			@Override
			public Expression<?> createWithValue(Primitive<? extends Number> value) {
				Expression<?> source = Literals.of(value.longValue());
				return Conversions.toFloatingPoint(source);
			}
		}

	}

	@Nested
	class ToFloatingPointList {

		@Test
		@RandomizedTest
		void testFromText(RandomGenerator rng) {
			ListExpression<?, CharSequence> source = ArrayLiterals.ofGeneric(
					String.valueOf(randomDouble(rng)),
					String.valueOf(randomDouble(rng)),
					String.valueOf(randomDouble(rng)),
					String.valueOf(randomDouble(rng)));
			FloatingPointListExpression<?> instance = Conversions.toFloatingPointList(source);
			assertThat(instance.size()).isEqualTo(4);
			for (int i = 0; i < instance.size(); i++) {
				assertThat(instance.getAsDouble(i)).as("Mismatch at index %d", _int(i))
					.isEqualTo(StringPrimitives.parseDouble(source.get(i)));
			}
		}

		@Test
		@RandomizedTest
		void testFromBoolean(RandomGenerator rng) {
			assertFailedCast(() -> Conversions.toFloatingPointList(
					ArrayLiterals.of(true, false, true, false)));
		}

		@Test
		@RandomizedTest
		void testFromInteger(RandomGenerator rng) {
			IntegerListExpression<?> source = ArrayLiterals.of(
					rng.nextInt(),
					rng.nextInt(),
					rng.nextInt(),
					rng.nextInt());
			FloatingPointListExpression<?> instance = Conversions.toFloatingPointList(source);
			assertThat(instance.size()).isEqualTo(4);
			for (int i = 0; i < instance.size(); i++) {
				assertThat(instance.getAsDouble(i)).as("Mismatch at index %d", _int(i))
					.isEqualTo(source.getAsLong(i));
			}
		}

		@Test
		@RandomizedTest
		void testFromFloatingPoint(RandomGenerator rng) {
			ListExpression<?, Primitive<Double>> source = ArrayLiterals.of(
					randomDouble(rng),
					randomDouble(rng),
					randomDouble(rng),
					randomDouble(rng));
			assertThat(Conversions.toFloatingPointList(source)).isSameAs(source);
		}

		@Test
		void testFromGeneric() {
			assertFailedCast(() -> Conversions.toFloatingPointList(
					ArrayLiterals.ofGeneric(new Object(), "test", new Object())));
		}

		abstract class ToFloatingPointListTestBase implements FloatingPointListExpressionTest<double[]> {

			@Override
			public double[] constant() {
				return new double[]{
						0.0,
						10.5,
						10_000.123,
						-1000.001
				};
			}

			@Override
			public double[] random(RandomGenerator rng) {
				return new double[]{
						randomDouble(rng),
						randomDouble(rng),
						randomDouble(rng),
						randomDouble(rng)
				};
			}

			@Override
			public double[] sized(int size) {
				double[] content = new double[size];
				for (int i = 0; i < content.length; i++) {
					content[i] = (i+1000L)*1.1;
				}
				return content;
			}

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return FloatingPointListCast.class; }

			@Override
			public TypeInfo getExpectedType() { return TypeInfo.of(double[].class, true); }
		}

		@Nested
		class FromText extends ToFloatingPointListTestBase {

			@SuppressWarnings("unchecked")
			@Override
			public FloatingPointListExpression<double[]> createWithValue(double[] value) {
				CharSequence[] content = new CharSequence[value.length];
				for (int i = 0; i < content.length; i++) {
					content[i] = String.valueOf(value[i]);
				}
				return (FloatingPointListExpression<double[]>)
						Conversions.toFloatingPointList(ArrayLiterals.ofGeneric(content));
			}
		}

		@Nested
		class FromInteger extends ToFloatingPointListTestBase {
			@Override
			public double[] constant() {
				return new double[]{
						0.0,
						10.0,
						10_000.0,
						-1000.0
				};
			}

			@Override
			public double[] random(RandomGenerator rng) {
				return new double[]{
						Math.floor(randomDouble(rng)),
						Math.floor(randomDouble(rng)),
						Math.floor(randomDouble(rng)),
						Math.floor(randomDouble(rng))
				};
			}

			@SuppressWarnings("unchecked")
			@Override
			public FloatingPointListExpression<double[]> createWithValue(double[] value) {
				long[] content = new long[value.length];
				for (int i = 0; i < content.length; i++) {
					content[i] = (long) value[i];
				}
				return (FloatingPointListExpression<double[]>)
						Conversions.toFloatingPointList(ArrayLiterals.of(content));
			}
		}

	}
}
