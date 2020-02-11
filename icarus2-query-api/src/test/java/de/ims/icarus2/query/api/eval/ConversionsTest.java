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
import de.ims.icarus2.query.api.eval.Conversions.IntegerCast;
import de.ims.icarus2.query.api.eval.Conversions.TextCast;
import de.ims.icarus2.query.api.eval.Conversions.TextListCast;
import de.ims.icarus2.query.api.eval.Expression.BooleanExpression;
import de.ims.icarus2.query.api.eval.Expression.BooleanListExpression;
import de.ims.icarus2.query.api.eval.Expression.FloatingPointListExpression;
import de.ims.icarus2.query.api.eval.Expression.IntegerListExpression;
import de.ims.icarus2.query.api.eval.Expression.ListExpression;
import de.ims.icarus2.query.api.eval.Expression.NumericalExpression;
import de.ims.icarus2.query.api.eval.Expression.TextExpression;
import de.ims.icarus2.query.api.eval.ExpressionTest.BooleanExpressionTest;
import de.ims.icarus2.query.api.eval.ExpressionTest.BooleanListExpressionTest;
import de.ims.icarus2.query.api.eval.ExpressionTest.FloatingPointExpressionTest;
import de.ims.icarus2.query.api.eval.ExpressionTest.IntegerExpressionTest;
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
			assertThat(instance.compute()).hasToString(String.valueOf(source.computeAsBoolean()));
		}

		@Test
		@RandomizedTest
		void testFromInteger(RandomGenerator rng) {
			NumericalExpression source = Literals.of(rng.nextInt());
			TextExpression instance = Conversions.toText(source);
			assertThat(instance.compute()).hasToString(String.valueOf(source.computeAsLong()));
		}

		@Test
		@RandomizedTest
		void testFromFloatingPoint(RandomGenerator rng) {
			NumericalExpression source = Literals.of(rng.nextDouble());
			TextExpression instance = Conversions.toText(source);
			assertThat(instance.compute()).hasToString(String.valueOf(source.computeAsDouble()));
		}

		@Test
		@RandomizedTest
		void testFromGeneric(RandomGenerator rng) {
			String value = rng.randomUnicodeString(10);
			Expression<Object> source = ExpressionTestUtils.generic(value);
			TextExpression instance = Conversions.toText(source);
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
			public TextExpression createWithValue(CharSequence value) {
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
			public TextExpression createWithValue(CharSequence value) {
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
				return String.valueOf(rng.nextDouble());
			}

			@Override
			public TextExpression createWithValue(CharSequence value) {
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
			public TextExpression createWithValue(CharSequence value) {
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
			ListExpression<?, ?> source = ArrayLiterals.of(
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
					rng.nextDouble(),
					rng.nextDouble(),
					rng.nextDouble(),
					rng.nextDouble());
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
			ListExpression<?, ?> source = ArrayLiterals.of(
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
			public ListExpression<CharSequence[], CharSequence> createForSize(int size) {
				CharSequence[] content = IntStream.range(0, size)
						.mapToObj(i -> String.valueOf(i%2==0))
						.toArray(CharSequence[]::new);
				return createWithValue(content);
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
			public ListExpression<CharSequence[], CharSequence> createForSize(int size) {
				CharSequence[] content = IntStream.range(0, size)
						.mapToObj(i -> String.valueOf(i+1000))
						.toArray(CharSequence[]::new);
				return createWithValue(content);
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
						String.valueOf(rng.nextDouble()),
						String.valueOf(rng.nextDouble()),
						String.valueOf(rng.nextDouble()),
						String.valueOf(rng.nextDouble())
				};
			}

			@Override
			public ListExpression<CharSequence[], CharSequence> createForSize(int size) {
				CharSequence[] content = IntStream.range(0, size)
						.mapToObj(i -> String.valueOf(i+1000))
						.toArray(CharSequence[]::new);
				return createWithValue(content);
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
			public ListExpression<CharSequence[], CharSequence> createForSize(int size) {
				CharSequence[] content = IntStream.range(0, size)
						.mapToObj(i -> "item_"+String.valueOf(i+1000))
						.toArray(CharSequence[]::new);
				return createWithValue(content);
			}

			@SuppressWarnings("unchecked")
			@Override
			public ListExpression<CharSequence[], CharSequence> createWithValue(CharSequence[] value) {
				Object[] content = new Object[value.length];
				for (int i = 0; i < content.length; i++) {
					content[i] = ExpressionTestUtils.dummy(value[i].toString());
				}
				return (ListExpression<CharSequence[], CharSequence>)
						Conversions.toTextList(ArrayLiterals.of(content));
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
	class ToBooleanList {

		@Test
		@RandomizedTest
		void testFromText(RandomGenerator rng) {
			ListExpression<?, CharSequence> source = ArrayLiterals.of(
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
					rng.nextBoolean() ? rng.nextDouble() : 0.0,
					rng.nextBoolean() ? rng.nextDouble() : 0.0,
					rng.nextBoolean() ? rng.nextDouble() : 0.0,
					rng.nextBoolean() ? rng.nextDouble() : 0.0);
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
			ListExpression<?, ?> source = ArrayLiterals.of(
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
			public boolean nativeConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return BooleanListCast.class; }

			@Override
			public TypeInfo getExpectedType() { return TypeInfo.of(boolean[].class, true); }

			@Override
			public BooleanListExpression<boolean[]> createForSize(int size) {
				boolean[] content = new boolean[size];
				for (int i = 0; i < content.length; i++) {
					content[i] = i%2==0;
				}
				return createWithValue(content);
			}
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
						Conversions.toBooleanList(ArrayLiterals.of(content));
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
						Conversions.toBooleanList(ArrayLiterals.of(content));
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
