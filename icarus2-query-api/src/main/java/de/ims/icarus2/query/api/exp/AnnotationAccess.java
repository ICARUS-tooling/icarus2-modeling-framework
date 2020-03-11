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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.exp.EvaluationContext.AnnotationInfo;
import de.ims.icarus2.query.api.exp.Expression.BooleanListExpression;
import de.ims.icarus2.query.api.exp.Expression.FloatingPointListExpression;
import de.ims.icarus2.query.api.exp.Expression.IntegerListExpression;
import de.ims.icarus2.query.api.exp.Expression.ListExpression;
import de.ims.icarus2.query.api.exp.Expression.PrimitiveExpression;
import de.ims.icarus2.query.api.exp.Expression.SharedExpression;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.MutablePrimitives.MutableDouble;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.MutablePrimitives.Primitive;

/**
 * @author Markus Gärtner
 *
 */
public class AnnotationAccess {

	/*
	 * Implementation note:
	 *
	 * We want to keep the expression implementations clear from dependencies
	 * on too many details of the model framework. As such we create multiple
	 * classes for primitive annotation access instead of a single one that
	 * directly interacts with the associated annotation storage.
	 */

	public static Expression<?> of(Expression<? extends Item> item, EvaluationContext context,
			Expression<CharSequence> key) {
		requireNonNull(item);
		requireNonNull(context);
		requireNonNull(key);

		if(key.isConstant()) {
			String annotationKey = key.compute().toString();
			checkNotEmpty(annotationKey);
			AnnotationInfo info = context.findAnnotation(
					QualifiedIdentifier.parseIdentifier(annotationKey)).orElseThrow(
					() -> new QueryException(QueryErrorCode.UNKNOWN_IDENTIFIER,
							"No annotation available for key: "+annotationKey));
			return of(item, info);
		}

		//TODO delegate to implementation that looks up annotation layer at evaluation time
		throw new QueryException(QueryErrorCode.UNSUPPORTED_FEATURE,
				"Dynamic lookup of annotation layers not supported (yet)");
	}

	public static Expression<?> of(Expression<? extends Item> item, AnnotationInfo info) {
		requireNonNull(item);
		requireNonNull(info);

		TypeInfo type = info.getType();
		if(type.isList())
			throw new QueryException(QueryErrorCode.UNSUPPORTED_FEATURE,
					"List types not supported as annotation results: "+type);

		if(TypeInfo.isInteger(type)) {
			return new SingleKeyInteger(item, info.getIntegerSource());
		} else if(TypeInfo.isFloatingPoint(type)) {
			return new SingleKeyFloatingPoint(item, info.getFloatingPointSource());
		} else if(TypeInfo.isBoolean(type)) {
			return new SingleKeyBoolean(item, info.getBooleanSource());
		}
		return new SingleKeyObject<>(type, item, info.getObjectSource());
	}

	public static Expression<?> of(Expression<? extends Item> item, EvaluationContext context,
			Expression<CharSequence>[] keys) {
		requireNonNull(item);
		requireNonNull(context);
		requireNonNull(keys);
		checkArgument("Keys array must not be empty", keys.length>0);

		// No point in the array overhead for a single argument
		if(keys.length==1) {
			return of(item, context, keys[0]);
		}

		if(Stream.of(keys).allMatch(Expression::isConstant)) {
			String[] annotationKeys = annotationKeysFor(keys);
			return multiLookupOf(context, item, annotationKeys);
		}

		//TODO delegate to implementation that looks up annotation layer at evaluation time
		throw new QueryException(QueryErrorCode.UNSUPPORTED_FEATURE,
				"Dynamic lookup of annotation layers not supported (yet)");
	}

	public static ListExpression<?, ?> of(Expression<? extends Item> item,
			EvaluationContext context,
			ListExpression<?, CharSequence> keys) {

		if(keys.isConstant()) {
			String[] annotationKeys = annotationKeysFor(keys);
			return multiLookupOf(context, item, annotationKeys);
		}

		//TODO delegate to implementation that looks up annotation layer at evaluation time
		throw new QueryException(QueryErrorCode.UNSUPPORTED_FEATURE,
				"Dynamic lookup of annotation layers not supported (yet)");
	}

	private static ListExpression<?, ?> multiLookupOf(EvaluationContext context,
			Expression<? extends Item> item, String[] annotationKeys) {
		AnnotationInfo[] infos = findAnnotations(context, annotationKeys);
		TypeInfo type = deduceType(context, infos);

		if(TypeInfo.isInteger(type)) {
			return new MultiKeyInteger(item, Stream.of(infos)
					.map(AnnotationInfo::getIntegerSource)
					.toArray(ToLongFunction[]::new));
		} else if(TypeInfo.isFloatingPoint(type)) {
			return new MultiKeyFloatingPoint(item, Stream.of(infos)
					.map(AnnotationInfo::getFloatingPointSource)
					.toArray(ToDoubleFunction[]::new));
		} else if(TypeInfo.isBoolean(type)) {
			return new MultiKeyBoolean(item, Stream.of(infos)
					.map(AnnotationInfo::getBooleanSource)
					.toArray(Predicate[]::new));
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		ListExpression<?, ?> result = new MultiKeyObject(type, item, wrapGeneric(infos));
		return result;
	}

	private static String[] annotationKeysFor(ListExpression<?, CharSequence> keys) {
		return IntStream.range(0, keys.size())
			.mapToObj(keys::get)
			.map(CharSequence::toString)
			.toArray(String[]::new);
	}

	private static String[] annotationKeysFor(Expression<CharSequence>[] keys) {
		return Stream.of(keys)
			.map(Expression::compute)
			.map(CharSequence::toString)
			.toArray(String[]::new);
	}

	private static AnnotationInfo[] findAnnotations(EvaluationContext context, String[] keys) {
		return Stream.of(keys)
				.map(QualifiedIdentifier::parseIdentifier)
				.map(context::findAnnotation)
				.map(Optional::get)
				.toArray(AnnotationInfo[]::new);
	}

	private static TypeInfo deduceType(EvaluationContext context, AnnotationInfo[] infos) {
		TypeInfo result = null;

		for (int i = 0; i < infos.length; i++) {
			TypeInfo type = infos[i].getType();
			if(type.isList())
				throw new QueryException(QueryErrorCode.UNSUPPORTED_FEATURE,
						"List types not supported as annotation results: "+type);

			if(result==null) {
				result = type;
			} else if(!result.equals(type)) {
				result = TypeInfo.GENERIC;
			}
		}

		return result;
	}

	private static Function<Item, ?>[] wrapGeneric(AnnotationInfo[] infos) {
		return Stream.of(infos)
				.map(AnnotationAccess::ensureGenericSource)
				.toArray(Function[]::new);
	}

	private static Function<?, ?> ensureGenericSource(AnnotationInfo info) {
		TypeInfo type = info.getType();
		if(TypeInfo.isInteger(type)) {
			return Expressions.wrap(info.getIntegerSource());
		} else if(TypeInfo.isFloatingPoint(type)) {
			return Expressions.wrap(info.getFloatingPointSource());
		} else if(TypeInfo.isBoolean(type)) {
			return Expressions.wrap(info.getBooleanSource());
		}
		return info.getObjectSource();
	}

	static final class SingleKeyObject<T> implements Expression<T>, SharedExpression {

		private final Expression<? extends Item> item;
		private final Function<Item, T> lookup;
		private final TypeInfo type;

		SingleKeyObject(TypeInfo type, Expression<? extends Item> item,
				Function<Item, T> lookup) {
			this.type = requireNonNull(type);
			this.item = requireNonNull(item);
			this.lookup = requireNonNull(lookup);
		}

		@Override
		public T compute() { return lookup.apply(item.compute()); }

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public Expression<T> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new SingleKeyObject<>(type, item.duplicate(context), lookup);
		}
	}

	static final class SingleKeyInteger implements Expression<Primitive<Long>>,
			PrimitiveExpression, SharedExpression {

		private final Expression<? extends Item> item;
		private final ToLongFunction<Item> lookup;
		private final MutableLong value;

		SingleKeyInteger(Expression<? extends Item> item,
				ToLongFunction<Item> lookup) {
			this.item = requireNonNull(item);
			this.lookup = requireNonNull(lookup);
			value = new MutableLong();
		}

		@Override
		public Primitive<Long> compute() {
			value.setLong(computeAsLong());
			return value;
		}

		@Override
		public long computeAsLong() { return lookup.applyAsLong(item.compute()); }

		@Override
		public double computeAsDouble() { return computeAsLong(); }

		@Override
		public TypeInfo getResultType() { return TypeInfo.INTEGER; }

		@Override
		public Expression<Primitive<Long>> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new SingleKeyInteger(item.duplicate(context), lookup);
		}
	}

	static final class SingleKeyFloatingPoint implements Expression<Primitive<Double>>,
			PrimitiveExpression, SharedExpression {

		private final Expression<? extends Item> item;
		private final ToDoubleFunction<Item> lookup;
		private final MutableDouble value;

		SingleKeyFloatingPoint(Expression<? extends Item> item,
				ToDoubleFunction<Item> lookup) {
			this.item = requireNonNull(item);
			this.lookup = requireNonNull(lookup);
			value = new MutableDouble();
		}

		@Override
		public Primitive<Double> compute() {
			value.setDouble(computeAsDouble());
			return value;
		}

		@Override
		public double computeAsDouble() { return lookup.applyAsDouble(item.compute()); }

		@Override
		public TypeInfo getResultType() { return TypeInfo.FLOATING_POINT; }

		@Override
		public Expression<Primitive<Double>> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new SingleKeyFloatingPoint(item.duplicate(context), lookup);
		}
	}

	static final class SingleKeyBoolean implements Expression<Primitive<Boolean>>,
			PrimitiveExpression, SharedExpression {

		private final Expression<? extends Item> item;
		private final Predicate<Item> lookup;
		private final MutableBoolean value;

		SingleKeyBoolean(Expression<? extends Item> item,
				Predicate<Item> lookup) {
			this.item = requireNonNull(item);
			this.lookup = requireNonNull(lookup);
			value = new MutableBoolean();
		}

		@Override
		public Primitive<Boolean> compute() {
			value.setBoolean(computeAsBoolean());
			return value;
		}

		@Override
		public boolean computeAsBoolean() { return lookup.test(item.compute()); }

		@Override
		public TypeInfo getResultType() { return TypeInfo.BOOLEAN; }

		@Override
		public Expression<Primitive<Boolean>> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new SingleKeyBoolean(item.duplicate(context), lookup);
		}
	}

	static final class MultiKeyObject<E> implements ListExpression<E[], E>, SharedExpression {

		private final Expression<? extends Item> item;
		private final Function<Item, E>[] lookups;
		private final E[] buffer;

		private final TypeInfo type, elementType;

		MultiKeyObject(TypeInfo elementType, Expression<? extends Item> item,
				Function<Item, E>[] lookups) {
			this.elementType = requireNonNull(elementType);
			type = EvaluationUtils.arrayType(elementType);
			this.item = requireNonNull(item);
			this.lookups = requireNonNull(lookups);
			buffer = EvaluationUtils.arrayOf(elementType, lookups.length);
		}

		@Override
		public E[] compute() {
			// Fill entire buffer
			Item item = this.item.compute();
			for (int i = 0; i < buffer.length; i++) {
				buffer[i] = lookups[i].apply(item);
			}
			return buffer;
		}

		@Override
		public E get(int index) {
			return lookups[index].apply(item.compute());
		}

		@Override
		public boolean isFixedSize() { return true; }

		@Override
		public int size() { return buffer.length; }

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public TypeInfo getElementType() { return elementType; }

		@Override
		public ListExpression<E[], E> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new MultiKeyObject<>(elementType, item.duplicate(context), lookups);
		}
	}

	static final class MultiKeyInteger implements IntegerListExpression<long[]>, SharedExpression {

		private final Expression<? extends Item> item;
		private final ToLongFunction<Item>[] lookups;
		private final long[] buffer;
		private final MutableLong value;

		private static final TypeInfo type = TypeInfo.of(long[].class, true);

		MultiKeyInteger(Expression<? extends Item> item, ToLongFunction<Item>[] lookups) {
			this.item = requireNonNull(item);
			this.lookups = requireNonNull(lookups);
			buffer = new long[lookups.length];
			value = new MutableLong();
		}

		@Override
		public long[] compute() {
			// Fill entire buffer
			Item item = this.item.compute();
			for (int i = 0; i < buffer.length; i++) {
				buffer[i] = lookups[i].applyAsLong(item);
			}
			return buffer;
		}

		@Override
		public Primitive<Long> get(int index) {
			value.setLong(getAsLong(index));
			return value;
		}

		@Override
		public long getAsLong(int index) { return lookups[index].applyAsLong(item.compute()); }

		@Override
		public boolean isFixedSize() { return true; }

		@Override
		public int size() { return buffer.length; }

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public IntegerListExpression<long[]> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new MultiKeyInteger(item.duplicate(context), lookups);
		}
	}

	static final class MultiKeyFloatingPoint implements FloatingPointListExpression<double[]>, SharedExpression {

		private final Expression<? extends Item> item;
		private final ToDoubleFunction<Item>[] lookups;
		private final double[] buffer;
		private final MutableDouble value;

		private static final TypeInfo type = TypeInfo.of(double[].class, true);

		MultiKeyFloatingPoint(Expression<? extends Item> item, ToDoubleFunction<Item>[] lookups) {
			this.item = requireNonNull(item);
			this.lookups = requireNonNull(lookups);
			buffer = new double[lookups.length];
			value = new MutableDouble();
		}

		@Override
		public double[] compute() {
			// Fill entire buffer
			Item item = this.item.compute();
			for (int i = 0; i < buffer.length; i++) {
				buffer[i] = lookups[i].applyAsDouble(item);
			}
			return buffer;
		}

		@Override
		public Primitive<Double> get(int index) {
			value.setDouble(getAsDouble(index));
			return value;
		}

		@Override
		public double getAsDouble(int index) { return lookups[index].applyAsDouble(item.compute()); }

		@Override
		public boolean isFixedSize() { return true; }

		@Override
		public int size() { return buffer.length; }

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public FloatingPointListExpression<double[]> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new MultiKeyFloatingPoint(item.duplicate(context), lookups);
		}
	}

	static final class MultiKeyBoolean implements BooleanListExpression<boolean[]>, SharedExpression {

		private final Expression<? extends Item> item;
		private final Predicate<Item>[] lookups;
		private final boolean[] buffer;
		private final MutableBoolean value;

		private static final TypeInfo type = TypeInfo.of(boolean[].class, true);

		MultiKeyBoolean(Expression<? extends Item> item, Predicate<Item>[] lookups) {
			this.item = requireNonNull(item);
			this.lookups = requireNonNull(lookups);
			buffer = new boolean[lookups.length];
			value = new MutableBoolean();
		}

		@Override
		public boolean[] compute() {
			// Fill entire buffer
			Item item = this.item.compute();
			for (int i = 0; i < buffer.length; i++) {
				buffer[i] = lookups[i].test(item);
			}
			return buffer;
		}

		@Override
		public Primitive<Boolean> get(int index) {
			value.setBoolean(getAsBoolean(index));
			return value;
		}

		@Override
		public boolean getAsBoolean(int index) { return lookups[index].test(item.compute()); }

		@Override
		public boolean isFixedSize() { return true; }

		@Override
		public int size() { return buffer.length; }

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public BooleanListExpression<boolean[]> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new MultiKeyBoolean(item.duplicate(context), lookups);
		}
	}
}
