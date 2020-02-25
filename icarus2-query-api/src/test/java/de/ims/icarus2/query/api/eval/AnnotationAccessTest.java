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

import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.query.api.eval.ExpressionTestUtils.dynamicGeneric;
import static de.ims.icarus2.query.api.eval.ExpressionTestUtils.raw;
import static de.ims.icarus2.test.util.Pair.pair;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.collections.CollectionUtils.feedItems;
import static de.ims.icarus2.util.lang.Primitives._double;
import static de.ims.icarus2.util.lang.Primitives._long;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.query.api.eval.AnnotationAccess.MultiKeyBoolean;
import de.ims.icarus2.query.api.eval.AnnotationAccess.MultiKeyFloatingPoint;
import de.ims.icarus2.query.api.eval.AnnotationAccess.MultiKeyInteger;
import de.ims.icarus2.query.api.eval.AnnotationAccess.MultiKeyObject;
import de.ims.icarus2.query.api.eval.AnnotationAccess.SingleKeyBoolean;
import de.ims.icarus2.query.api.eval.AnnotationAccess.SingleKeyFloatingPoint;
import de.ims.icarus2.query.api.eval.AnnotationAccess.SingleKeyInteger;
import de.ims.icarus2.query.api.eval.AnnotationAccess.SingleKeyObject;
import de.ims.icarus2.query.api.eval.EvaluationContext.AnnotationInfo;
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
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.util.Mutable;
import de.ims.icarus2.util.Mutable.MutableObject;
import de.ims.icarus2.util.MutablePrimitives.Primitive;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

/**
 * @author Markus Gärtner
 *
 */
class AnnotationAccessTest {

	private static AnnotationInfo info(String key, ValueType valueType, TypeInfo type) {
		return new AnnotationInfo(key, key, valueType, type);
	}

	private static AnnotationInfo info(String rawKey, String key, ValueType valueType, TypeInfo type) {
		return new AnnotationInfo(rawKey, key, valueType, type);
	}

	private static EvaluationContext makeContext(AnnotationInfo...infos) {
		checkArgument(infos.length>0);
		Map<String, AnnotationInfo> lookup = new Object2ObjectOpenHashMap<>();
		for(AnnotationInfo info : infos) {
			if(lookup.putIfAbsent(info.getKey(), info)!=null)
				throw new InternalError("Duplicate key: "+info.getKey());
		}

		EvaluationContext context = mock(EvaluationContext.class);

		doAnswer(inv -> {
			return Optional.ofNullable(lookup.get(inv.getArgument(0)));
		}).when(context).findAnnotation(anyString());

		return context;
	}

	@SafeVarargs
	private final static Function<Item, Object> objSource(Pair<Item, Object>...entries) {
		Map<Item, Object> map = new Reference2ObjectOpenHashMap<>();
		for(Pair<Item, Object> entry : entries) {
			map.put(entry.first, entry.second);
		}

		return map::get;
	}

	@SafeVarargs
	private final static ToLongFunction<Item> intSource(Pair<Item, ? extends Number>...entries) {
		Object2LongMap<Item> map = new Object2LongOpenHashMap<>();
		for(Pair<Item, ? extends Number> entry : entries) {
			map.put(entry.first, entry.second.longValue());
		}

		return map::getLong;
	}

	@SafeVarargs
	private final static ToDoubleFunction<Item> fpSource(Pair<Item, ? extends Number>...entries) {
		Object2DoubleMap<Item> map = new Object2DoubleOpenHashMap<>();
		for(Pair<Item, ? extends Number> entry : entries) {
			map.put(entry.first, entry.second.doubleValue());
		}

		return map::getDouble;
	}

	@SafeVarargs
	private final static Predicate<Item> boolSource(Item...entries) {
		Set<Item> lookup = new ReferenceOpenHashSet<>();
		feedItems(lookup, entries);

		return lookup::contains;
	}

	@Nested
	class ForSingleKey {

		@Nested
		class ForObject implements ExpressionTest<String> {

			@Override
			public Class<?> getTestTargetClass() { return SingleKeyObject.class; }

			@Override
			public String constant() { return "test123"; }

			@Override
			public String random(RandomGenerator rng) { return rng.randomString(12); }

			@Override
			public TypeInfo getExpectedType() { return TypeInfo.TEXT; }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public boolean optimizeToConstant() { return false; }

			@Override
			public Expression<?> createWithValue(String value) {
				String key = "key";
				Item item = mockItem();
				Expression<Item> itemSource = raw(item);
				AnnotationInfo info = info(key, ValueType.STRING, TypeInfo.TEXT);
				info.objectSource = objSource(pair(item, value));
				EvaluationContext context = makeContext(info);

				return AnnotationAccess.of(itemSource, context, Literals.of(key));
			}

			@Test
			void testMultipleItems() {
				String key = "key";
				Item item1 = mockItem();
				Item item2 = mockItem();
				Mutable<Item> item = new MutableObject<>(item1);
				Expression<Item> itemSource = dynamicGeneric(item::get);
				AnnotationInfo info = info(key, ValueType.STRING, TypeInfo.TEXT);

				String value1 = "value1";
				String value2 = "value2";
				info.objectSource = objSource(pair(item1, value1), pair(item2, value2));
				EvaluationContext context = makeContext(info);

				Expression<?> exp = AnnotationAccess.of(itemSource, context, Literals.of(key));

				assertThat(exp.compute()).isEqualTo(value1);

				item.set(item2);
				assertThat(exp.compute()).isEqualTo(value2);
			}
		}

		@Nested
		class ForInteger implements IntegerExpressionTest {

			@Override
			public Class<?> getTestTargetClass() { return SingleKeyInteger.class; }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public boolean optimizeToConstant() { return false; }

			@Override
			public Expression<?> createWithValue(Primitive<? extends Number> value) {
				String key = "key";
				Item item = mockItem();
				Expression<Item> itemSource = raw(item);
				AnnotationInfo info = info(key, ValueType.LONG, TypeInfo.INTEGER);
				info.integerSource = intSource(pair(item, value.get()));
				EvaluationContext context = makeContext(info);

				return AnnotationAccess.of(itemSource, context, Literals.of(key));
			}

			@Test
			void testMultipleItems() {
				String key = "key";
				Item item1 = mockItem();
				Item item2 = mockItem();
				Mutable<Item> item = new MutableObject<>(item1);
				Expression<Item> itemSource = dynamicGeneric(item::get);
				AnnotationInfo info = info(key, ValueType.LONG, TypeInfo.INTEGER);

				long value1 = 123;
				long value2 = 456;
				info.integerSource = intSource(pair(item1, _long(value1)), pair(item2, _long(value2)));
				EvaluationContext context = makeContext(info);

				Expression<?> exp = AnnotationAccess.of(itemSource, context, Literals.of(key));
				assertThat(exp.isInteger()).isTrue();

				assertThat(exp.computeAsLong()).isEqualTo(value1);

				item.set(item2);
				assertThat(exp.computeAsLong()).isEqualTo(value2);
			}
		}

		@Nested
		class ForFloatingPoint implements FloatingPointExpressionTest {

			@Override
			public Class<?> getTestTargetClass() { return SingleKeyFloatingPoint.class; }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public boolean optimizeToConstant() { return false; }

			@Override
			public Expression<?> createWithValue(Primitive<? extends Number> value) {
				String key = "key";
				Item item = mockItem();
				Expression<Item> itemSource = raw(item);
				AnnotationInfo info = info(key, ValueType.DOUBLE, TypeInfo.FLOATING_POINT);
				info.floatingPointSource = fpSource(pair(item, value.get()));
				EvaluationContext context = makeContext(info);

				return AnnotationAccess.of(itemSource, context, Literals.of(key));
			}

			@Test
			void testMultipleItems() {
				String key = "key";
				Item item1 = mockItem();
				Item item2 = mockItem();
				Mutable<Item> item = new MutableObject<>(item1);
				Expression<Item> itemSource = dynamicGeneric(item::get);
				AnnotationInfo info = info(key, ValueType.DOUBLE, TypeInfo.FLOATING_POINT);

				double value1 = 123.456;
				double value2 = Double.MAX_VALUE;
				info.floatingPointSource = fpSource(pair(item1, _double(value1)), pair(item2, _double(value2)));
				EvaluationContext context = makeContext(info);

				Expression<?> exp = AnnotationAccess.of(itemSource, context, Literals.of(key));
				assertThat(exp.isFloatingPoint()).isTrue();

				assertThat(exp.computeAsDouble()).isEqualTo(value1);

				item.set(item2);
				assertThat(exp.computeAsDouble()).isEqualTo(value2);
			}
		}

		@Nested
		class ForBoolean implements BooleanExpressionTest {

			@Override
			public Class<?> getTestTargetClass() { return SingleKeyBoolean.class; }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public boolean optimizeToConstant() { return false; }

			@Override
			public Expression<?> createWithValue(Primitive<Boolean> value) {
				String key = "key";
				Item item = mockItem();
				Expression<Item> itemSource = raw(item);
				AnnotationInfo info = info(key, ValueType.BOOLEAN, TypeInfo.BOOLEAN);
				info.booleanSource = boolSource(value.booleanValue() ? item : null);
				EvaluationContext context = makeContext(info);

				return AnnotationAccess.of(itemSource, context, Literals.of(key));
			}

			@Test
			void testMultipleItems() {
				String key = "key";
				Item item1 = mockItem();
				Item item2 = mockItem();
				Mutable<Item> item = new MutableObject<>(item1);
				Expression<Item> itemSource = dynamicGeneric(item::get);
				AnnotationInfo info = info(key, ValueType.BOOLEAN, TypeInfo.BOOLEAN);

				boolean value1 = false;
				boolean value2 = true;
				info.booleanSource = boolSource(item2);
				EvaluationContext context = makeContext(info);

				Expression<?> exp = AnnotationAccess.of(itemSource, context, Literals.of(key));
				assertThat(exp.isBoolean()).isTrue();

				assertThat(exp.computeAsBoolean()).isEqualTo(value1);

				item.set(item2);
				assertThat(exp.computeAsBoolean()).isEqualTo(value2);
			}
		}

	}

	@Nested
	class ForMultipleKeys {

		@Nested
		class ForObject implements ListExpressionTest<CharSequence[], CharSequence> {

			@Override
			public CharSequence[] constant() {
				return new CharSequence[]{
					"test1",
					"test2",
					"test3",
				};
			}

			@Override
			public CharSequence[] random(RandomGenerator rng) {
				return new CharSequence[]{
						rng.randomString(15),
						rng.randomString(5),
						rng.randomString(10),
					};
			}

			@Override
			public TypeInfo getExpectedType() { return TypeInfo.of(CharSequence[].class, true); }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public boolean optimizeToConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return MultiKeyObject.class; }

			@Override
			public String[] sized(int size) {
				return IntStream.range(0, size)
						.mapToObj(i -> "item_"+i)
						.toArray(String[]::new);
			}

			@Override
			public TypeInfo getExpectedElementType() { return TypeInfo.TEXT; }

			@SuppressWarnings("unchecked")
			@Override
			public ListExpression<CharSequence[], CharSequence> createWithValue(CharSequence[] value) {
				String[] keys = IntStream.range(0, value.length)
						.mapToObj(i -> "key"+i)
						.toArray(String[]::new);
				Item item = mockItem();
				Expression<Item> itemSource = raw(item);
				AnnotationInfo[] infos = IntStream.range(0, keys.length)
						.mapToObj(i -> {
							AnnotationInfo info = info(keys[i], ValueType.STRING, TypeInfo.TEXT);
							info.objectSource = objSource(pair(item, value[i]));
							return info;
						})
						.toArray(AnnotationInfo[]::new);
				EvaluationContext context = makeContext(infos);

				return (ListExpression<CharSequence[], CharSequence>) AnnotationAccess.of(
						itemSource, context, ArrayLiterals.ofGeneric(keys));
			}

		}

		@Nested
		class ForInteger implements IntegerListExpressionTest<long[]> {

			@Override
			public long[] constant() {
				return new long[]{
					123,
					10_000_000,
					Long.MIN_VALUE,
				};
			}

			@Override
			public long[] random(RandomGenerator rng) {
				return new long[]{
						rng.nextInt(),
						rng.nextInt(),
						rng.nextInt(),
						rng.nextInt(),
						rng.nextInt(),
					};
			}

			@Override
			public TypeInfo getExpectedType() { return TypeInfo.of(long[].class, true); }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public boolean optimizeToConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return MultiKeyInteger.class; }

			@Override
			public long[] sized(int size) {
				return LongStream.range(0, size).toArray();
			}

			@Override
			public TypeInfo getExpectedElementType() { return TypeInfo.INTEGER; }

			@SuppressWarnings("unchecked")
			@Override
			public IntegerListExpression<long[]> createWithValue(long[] value) {
				String[] keys = IntStream.range(0, value.length)
						.mapToObj(i -> "key"+i)
						.toArray(String[]::new);
				Item item = mockItem();
				Expression<Item> itemSource = raw(item);
				AnnotationInfo[] infos = IntStream.range(0, keys.length)
						.mapToObj(i -> {
							AnnotationInfo info = info(keys[i], ValueType.LONG, TypeInfo.INTEGER);
							info.integerSource = intSource(pair(item, _long(value[i])));
							return info;
						})
						.toArray(AnnotationInfo[]::new);
				EvaluationContext context = makeContext(infos);

				return (IntegerListExpression<long[]>) AnnotationAccess.of(
						itemSource, context, ArrayLiterals.ofGeneric(keys));
			}

		}

		@Nested
		class ForFloatingPoint implements FloatingPointListExpressionTest<double[]> {

			@Override
			public double[] constant() {
				return new double[]{
					123.456,
					-10_000_000.1,
					Double.MAX_VALUE,
				};
			}

			@Override
			public double[] random(RandomGenerator rng) {
				return new double[]{
						rng.nextFloat(),
						rng.nextFloat(),
						rng.nextFloat(),
						rng.nextFloat(),
						rng.nextFloat(),
					};
			}

			@Override
			public TypeInfo getExpectedType() { return TypeInfo.of(double[].class, true); }

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public boolean optimizeToConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return MultiKeyFloatingPoint.class; }

			@Override
			public double[] sized(int size) {
				return LongStream.range(0, size).mapToDouble(i->i+0.5).toArray();
			}

			@Override
			public TypeInfo getExpectedElementType() { return TypeInfo.FLOATING_POINT; }

			@SuppressWarnings("unchecked")
			@Override
			public FloatingPointListExpression<double[]> createWithValue(double[] value) {
				String[] keys = IntStream.range(0, value.length)
						.mapToObj(i -> "key"+i)
						.toArray(String[]::new);
				Item item = mockItem();
				Expression<Item> itemSource = raw(item);
				AnnotationInfo[] infos = IntStream.range(0, keys.length)
						.mapToObj(i -> {
							AnnotationInfo info = info(keys[i], ValueType.DOUBLE, TypeInfo.FLOATING_POINT);
							info.floatingPointSource = fpSource(pair(item, _double(value[i])));
							return info;
						})
						.toArray(AnnotationInfo[]::new);
				EvaluationContext context = makeContext(infos);

				return (FloatingPointListExpression<double[]>) AnnotationAccess.of(
						itemSource, context, ArrayLiterals.ofGeneric(keys));
			}

		}

		@Nested
		class ForBoolean implements BooleanListExpressionTest<boolean[]> {

			@Override
			public boolean[] constant() {
				return new boolean[]{
					true,
					false,
					false,
					true,
					false
				};
			}

			@Override
			public boolean[] random(RandomGenerator rng) {
				return new boolean[]{
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
			public boolean optimizeToConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return MultiKeyBoolean.class; }

			@Override
			public boolean[] sized(int size) {
				boolean[] values = new boolean[size];
				for (int i = 0; i < values.length; i++) {
					values[i] = i%3==0;
				}
				return values;
			}

			@Override
			public TypeInfo getExpectedElementType() { return TypeInfo.BOOLEAN; }

			@SuppressWarnings("unchecked")
			@Override
			public BooleanListExpression<boolean[]> createWithValue(boolean[] value) {
				String[] keys = IntStream.range(0, value.length)
						.mapToObj(i -> "key"+i)
						.toArray(String[]::new);
				Item item = mockItem();
				Expression<Item> itemSource = raw(item);
				AnnotationInfo[] infos = IntStream.range(0, keys.length)
						.mapToObj(i -> {
							AnnotationInfo info = info(keys[i], ValueType.BOOLEAN, TypeInfo.BOOLEAN);
							info.booleanSource = boolSource(value[i] ? item : null);
							return info;
						})
						.toArray(AnnotationInfo[]::new);
				EvaluationContext context = makeContext(infos);

				return (BooleanListExpression<boolean[]>) AnnotationAccess.of(
						itemSource, context, ArrayLiterals.ofGeneric(keys));
			}

		}
	}
}
