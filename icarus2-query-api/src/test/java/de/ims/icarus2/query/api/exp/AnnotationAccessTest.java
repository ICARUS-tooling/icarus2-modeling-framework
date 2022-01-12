/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.query.api.exp.ExpressionTestUtils.dynamicGeneric;
import static de.ims.icarus2.query.api.exp.ExpressionTestUtils.raw;
import static de.ims.icarus2.test.util.Pair.pair;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.collections.CollectionUtils.feedItems;
import static de.ims.icarus2.util.lang.Primitives._double;
import static de.ims.icarus2.util.lang.Primitives._long;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.query.api.exp.AnnotationAccess.MultiKeyBoolean;
import de.ims.icarus2.query.api.exp.AnnotationAccess.MultiKeyFloatingPoint;
import de.ims.icarus2.query.api.exp.AnnotationAccess.MultiKeyInteger;
import de.ims.icarus2.query.api.exp.AnnotationAccess.MultiKeyObject;
import de.ims.icarus2.query.api.exp.AnnotationAccess.SingleKeyBoolean;
import de.ims.icarus2.query.api.exp.AnnotationAccess.SingleKeyFloatingPoint;
import de.ims.icarus2.query.api.exp.AnnotationAccess.SingleKeyInteger;
import de.ims.icarus2.query.api.exp.AnnotationAccess.SingleKeyObject;
import de.ims.icarus2.query.api.exp.Expression.BooleanListExpression;
import de.ims.icarus2.query.api.exp.Expression.FloatingPointListExpression;
import de.ims.icarus2.query.api.exp.Expression.IntegerListExpression;
import de.ims.icarus2.query.api.exp.Expression.ListExpression;
import de.ims.icarus2.query.api.exp.ExpressionTest.BooleanExpressionTest;
import de.ims.icarus2.query.api.exp.ExpressionTest.FloatingPointExpressionTest;
import de.ims.icarus2.query.api.exp.ExpressionTest.IntegerExpressionTest;
import de.ims.icarus2.query.api.exp.ExpressionTestMixins.BooleanArrayMixin;
import de.ims.icarus2.query.api.exp.ExpressionTestMixins.DoubleArrayMixin;
import de.ims.icarus2.query.api.exp.ExpressionTestMixins.LongArrayMixin;
import de.ims.icarus2.query.api.exp.ExpressionTestMixins.TextArrayMixin;
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

	private static AnnotationInfo info(String key, ValueType valueType, TypeInfo type,
			Consumer<? super AnnotationInfo.Builder> sourceSetter) {
		AnnotationInfo.Builder builder = AnnotationInfo.builer()
				.rawKey(key)
				.key(key)
				.valueType(valueType)
				.type(type);
		sourceSetter.accept(builder);
		return builder.build();
	}

	private static AnnotationInfo info(String rawKey, String key, ValueType valueType, TypeInfo type,
			Consumer<? super AnnotationInfo.Builder> sourceSetter) {
		AnnotationInfo.Builder builder = AnnotationInfo.builer()
				.rawKey(rawKey)
				.key(key)
				.valueType(valueType)
				.type(type);
		sourceSetter.accept(builder);
		return builder.build();
	}

	private static EvaluationContext makeContext(AnnotationInfo...infos) {
		checkArgument(infos.length>0);
		Map<String, AnnotationInfo> lookup = new Object2ObjectOpenHashMap<>();
		for(AnnotationInfo info : infos) {
			if(lookup.putIfAbsent(info.getRawKey(), info)!=null)
				throw new InternalError("Duplicate key: "+info.getKey());
		}

		EvaluationContext context = mock(EvaluationContext.class);

		doAnswer(inv -> {
			QualifiedIdentifier identifier = inv.getArgument(0);
			return Optional.ofNullable(lookup.get(identifier.getRawText()));
		}).when(context).findAnnotation(any());

		return context;
	}

	@SafeVarargs
	private final static Consumer<AnnotationInfo.Builder> objSource(Pair<Item, Object>...entries) {
		Map<Item, Object> map = new Reference2ObjectOpenHashMap<>();
		for(Pair<Item, Object> entry : entries) {
			map.put(entry.first, entry.second);
		}

		return builder -> builder.objectSource(map::get);
	}

	@SafeVarargs
	private final static Consumer<AnnotationInfo.Builder> intSource(Pair<Item, ? extends Number>...entries) {
		Object2LongMap<Item> map = new Object2LongOpenHashMap<>();
		for(Pair<Item, ? extends Number> entry : entries) {
			map.put(entry.first, entry.second.longValue());
		}

		return builder -> builder.integerSource(map::getLong);
	}

	@SafeVarargs
	private final static Consumer<AnnotationInfo.Builder> fpSource(Pair<Item, ? extends Number>...entries) {
		Object2DoubleMap<Item> map = new Object2DoubleOpenHashMap<>();
		for(Pair<Item, ? extends Number> entry : entries) {
			map.put(entry.first, entry.second.doubleValue());
		}

		return builder -> builder.floatingPointSource(map::getDouble);
	}

	@SafeVarargs
	private final static Consumer<AnnotationInfo.Builder> boolSource(Item...entries) {
		Set<Item> lookup = new ReferenceOpenHashSet<>();
		feedItems(lookup, entries);

		return builder -> builder.booleanSource(lookup::contains);
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
				AnnotationInfo info = info(key, ValueType.STRING, TypeInfo.TEXT, objSource(pair(item, value)));
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

				String value1 = "value1";
				String value2 = "value2";
				AnnotationInfo info = info(key, ValueType.STRING, TypeInfo.TEXT,
						objSource(pair(item1, value1), pair(item2, value2)));
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
				AnnotationInfo info = info(key, ValueType.LONG, TypeInfo.INTEGER,
						intSource(pair(item, value.get())));
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

				long value1 = 123;
				long value2 = 456;
				AnnotationInfo info = info(key, ValueType.LONG, TypeInfo.INTEGER,
						intSource(pair(item1, _long(value1)), pair(item2, _long(value2))));
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
				AnnotationInfo info = info(key, ValueType.DOUBLE, TypeInfo.FLOATING_POINT,
						fpSource(pair(item, value.get())));
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

				double value1 = 123.456;
				double value2 = Double.MAX_VALUE;
				AnnotationInfo info = info(key, ValueType.DOUBLE, TypeInfo.FLOATING_POINT,
						fpSource(pair(item1, _double(value1)), pair(item2, _double(value2))));
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
				AnnotationInfo info = info(key, ValueType.BOOLEAN, TypeInfo.BOOLEAN,
						boolSource(value.booleanValue() ? item : null));
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

				boolean value1 = false;
				boolean value2 = true;
				AnnotationInfo info = info(key, ValueType.BOOLEAN, TypeInfo.BOOLEAN, boolSource(item2));
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
		class ForObject implements TextArrayMixin {

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public boolean optimizeToConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return MultiKeyObject.class; }

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
							AnnotationInfo info = info(keys[i], ValueType.STRING, TypeInfo.TEXT,
									objSource(pair(item, value[i])));
							return info;
						})
						.toArray(AnnotationInfo[]::new);
				EvaluationContext context = makeContext(infos);

				return (ListExpression<CharSequence[], CharSequence>) AnnotationAccess.of(
						itemSource, context, ArrayLiterals.ofGeneric(keys));
			}

		}

		@Nested
		class ForInteger implements LongArrayMixin {
			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public boolean optimizeToConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return MultiKeyInteger.class; }

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
							AnnotationInfo info = info(keys[i], ValueType.LONG, TypeInfo.INTEGER,
									intSource(pair(item, _long(value[i]))));
							return info;
						})
						.toArray(AnnotationInfo[]::new);
				EvaluationContext context = makeContext(infos);

				return (IntegerListExpression<long[]>) AnnotationAccess.of(
						itemSource, context, ArrayLiterals.ofGeneric(keys));
			}

		}

		@Nested
		class ForFloatingPoint implements DoubleArrayMixin {

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public boolean optimizeToConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return MultiKeyFloatingPoint.class; }

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
							AnnotationInfo info = info(keys[i], ValueType.DOUBLE, TypeInfo.FLOATING_POINT,
									fpSource(pair(item, _double(value[i]))));
							return info;
						})
						.toArray(AnnotationInfo[]::new);
				EvaluationContext context = makeContext(infos);

				return (FloatingPointListExpression<double[]>) AnnotationAccess.of(
						itemSource, context, ArrayLiterals.ofGeneric(keys));
			}

		}

		@Nested
		class ForBoolean implements BooleanArrayMixin {

			@Override
			public boolean nativeConstant() { return false; }

			@Override
			public boolean optimizeToConstant() { return false; }

			@Override
			public Class<?> getTestTargetClass() { return MultiKeyBoolean.class; }

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
							AnnotationInfo info = info(keys[i], ValueType.BOOLEAN, TypeInfo.BOOLEAN,
									boolSource(value[i] ? item : null));
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
