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

import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

import org.antlr.v4.runtime.ParserRuleContext;

import de.ims.icarus2.apiguard.Unguarded;
import de.ims.icarus2.query.api.eval.Environment.NsEntry;
import de.ims.icarus2.query.api.eval.Expression.ProxyExpression;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.MutablePrimitives.MutableDouble;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.MutablePrimitives.Primitive;

/**
 * Utility implementations of various {@link Expression} interfaces that don't fit into
 * the thematically named other utility factories.
 *
 * @author Markus Gärtner
 *
 */
public class Expressions {


	/** Wraps a single value into a constant expression. */
	public static <T> Expression<T> constant(T value) {
		return new ConstantValue<>(value);
	}

	static final class ConstantValue<T> implements Expression<T> {

		private final T value;
		private final TypeInfo type;

		ConstantValue(T value) {
			this.value = requireNonNull(value);
			type = TypeInfo.of(value.getClass());
		}

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public T compute() { return value; }

		@Override
		public Expression<T> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return this;
		}

		@Override
		public boolean isConstant() { return true; }
	}

	public static <T> Function<T, Primitive<Long>> wrap(ToLongFunction<T> source) {
		return new ToLongWrapper<>(source);
	}

	static final class ToLongWrapper<T> implements Function<T, Primitive<Long>> {
		private final MutableLong value = new MutableLong();
		private final ToLongFunction<T> source;

		ToLongWrapper(ToLongFunction<T> source) { this.source = requireNonNull(source); }

		@Override
		public Primitive<Long> apply(T target) {
			value.setLong(source.applyAsLong(target));
			return value;
		}
	}

	public static <T> Function<T, Primitive<Double>> wrap(ToDoubleFunction<T> source) {
		return new ToDoubleWrapper<>(source);
	}

	static final class ToDoubleWrapper<T> implements Function<T, Primitive<Double>> {
		private final MutableDouble value = new MutableDouble();
		private final ToDoubleFunction<T> source;

		ToDoubleWrapper(ToDoubleFunction<T> source) { this.source = requireNonNull(source); }

		@Override
		public Primitive<Double> apply(T target) {
			value.setDouble(source.applyAsDouble(target));
			return value;
		}
	}

	public static <T> Function<T, Primitive<Boolean>> wrap(Predicate<T> source) {
		return new PredicateWrapper<>(source);
	}

	static final class PredicateWrapper<T> implements Function<T, Primitive<Boolean>> {
		private final MutableBoolean value = new MutableBoolean();
		private final Predicate<T> source;

		PredicateWrapper(Predicate<T> source) { this.source = requireNonNull(source); }

		@Override
		public Primitive<Boolean> apply(T target) {
			value.setBoolean(source.test(target));
			return value;
		}
	}

	public static Expression<?> pathProxy(Expression<?> source, String name, ParserRuleContext context) {
		return new PathProxy<>(requireNonNull(source), name, context);
	}

	public static Expression<?> pathProxy(String name, ParserRuleContext context) {
		return new PathProxy<>(null, name, context);
	}

	public static final class PathProxy<T> implements Expression<T>, ProxyExpression {
		private final Optional<Expression<?>> source;
		private final String name;
		private final ParserRuleContext context;

		private PathProxy(Expression<?> source, String name, ParserRuleContext context) {
			this.source = Optional.ofNullable(source);
			this.name = checkNotEmpty(name);
			this.context = context;
		}

		public String getName() { return name; }

		public Optional<Expression<?>> getSource() { return source; }

		public ParserRuleContext getContext() { return context; }

		@Override
		public TypeInfo getResultType() { return TypeInfo.GENERIC; }

		@Override
		public T compute() { throw EvaluationUtils.forProxyCall(); }

		@Override
		@Unguarded("No supposed to be called")
		public Expression<T> duplicate(EvaluationContext context) { throw EvaluationUtils.forProxyCall(); }
	}

	private static abstract class Proxy<V,T> implements Expression<T> {
		private final NsEntry entry;
		private final Expression<?>[] arguments;
		protected final Expression<V> source;

		@SuppressWarnings("unchecked")
		protected Proxy(NsEntry entry, Expression<?> source, Expression<?>[] arguments) {
			this.entry = requireNonNull(entry);
			this.source = (Expression<V>) source;
			this.arguments = arguments;
		}

		@SuppressWarnings("rawtypes")
		private static final Expression[] NO_ARGS = {};

		@Override
		public TypeInfo getResultType() { return entry.getValueType(); }

		@SuppressWarnings("unchecked")
		@Override
		public Expression<T> duplicate(EvaluationContext context) {
			Expression<V> newSource = source==null ? null : source.duplicate(context);
			Expression<?>[] newArguments = arguments==null ? NO_ARGS
					: EvaluationUtils.duplicate(arguments, context);

			return (Expression<T>) entry.instantiate(context, newSource, newArguments);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Expression<T> optimize(EvaluationContext context) {
			Expression<V> newSource = source==null ? null : source.optimize(context);

			Expression<?>[] newArguments = new Expression[entry.argumentCount()];
			boolean argsChanged = false;
			for (int i = 0; i < newArguments.length; i++) {
				newArguments[i] = arguments[i].optimize(context);
				argsChanged |= newArguments[i] != arguments[i];
			}

			if(newSource!=source || argsChanged) {
				return (Expression<T>) entry.instantiate(context, newSource, newArguments);
			}

			return this;
		}
	}

	public static <V,T> Expression<?> wrapObj(NsEntry entry, Function<V, T> objFunc,
			Expression<?> source, Expression<?>...arguments) {
		return new ObjectProxy<>(entry, objFunc, source, arguments);
	}

	private static final class ObjectProxy<V,T> extends Proxy<V, T> {

		private final Function<V, T> objFunc;

		private ObjectProxy(NsEntry entry, Function<V, T> objFunc, Expression<?> source, Expression<?>...arguments) {
			super(entry, source, arguments);
			this.objFunc = requireNonNull(objFunc);
		}

		@Override
		public T compute() { return objFunc.apply(source.compute()); }
	}

	public static <V> Expression<Primitive<Long>> wrapInt(NsEntry entry, ToLongFunction<V> intFunc,
			Expression<?> source, Expression<?>...arguments) {
		return new IntegerProxy<>(entry, intFunc, source, arguments);
	}

	private static final class IntegerProxy<V> extends Proxy<V, Primitive<Long>> {

		private final ToLongFunction<V> intFunc;
		private final MutableLong value = new MutableLong();

		private IntegerProxy(NsEntry entry, ToLongFunction<V> intFunc, Expression<?> source, Expression<?>...arguments) {
			super(entry, source, arguments);
			this.intFunc = requireNonNull(intFunc);
		}

		@Override
		public Primitive<Long> compute() {
			value.setLong(computeAsLong());
			return value;
		}

		@Override
		public long computeAsLong() { return intFunc.applyAsLong(source.compute()); }
		@Override
		public double computeAsDouble() { return computeAsLong(); }
		@Override
		public TypeInfo getResultType() { return TypeInfo.INTEGER; }
	}

	public static <V> Expression<Primitive<Double>> wrapFloat(NsEntry entry, ToDoubleFunction<V> fpFunc,
			Expression<?> source, Expression<?>...arguments) {
		return new FloatingPointProxy<>(entry, fpFunc, source, arguments);
	}

	private static final class FloatingPointProxy<V> extends Proxy<V, Primitive<Double>> {

		private final ToDoubleFunction<V> fpFunc;
		private final MutableDouble value = new MutableDouble();

		private FloatingPointProxy(NsEntry entry, ToDoubleFunction<V> fpFunc, Expression<?> source, Expression<?>...arguments) {
			super(entry, source, arguments);
			this.fpFunc = requireNonNull(fpFunc);
		}

		@Override
		public Primitive<Double> compute() {
			value.setDouble(computeAsDouble());
			return value;
		}

		@Override
		public double computeAsDouble() { return fpFunc.applyAsDouble(source.compute()); }
		@Override
		public TypeInfo getResultType() { return TypeInfo.FLOATING_POINT; }
	}

	public static <V> Expression<Primitive<Boolean>> wrapBool(NsEntry entry, Predicate<V> boolFunc,
			Expression<?> source, Expression<?>...arguments) {
		return new BooleanProxy<>(entry, boolFunc, source, arguments);
	}

	private static final class BooleanProxy<V> extends Proxy<V, Primitive<Boolean>> {

		private final Predicate<V> boolFunc;
		private final MutableBoolean value = new MutableBoolean();

		private BooleanProxy(NsEntry entry, Predicate<V> boolFunc, Expression<?> source, Expression<?>...arguments) {
			super(entry, source, arguments);
			this.boolFunc = requireNonNull(boolFunc);
		}

		@Override
		public Primitive<Boolean> compute() {
			value.setBoolean(computeAsBoolean());
			return value;
		}

		@Override
		public boolean computeAsBoolean() { return boolFunc.test(source.compute()); }
		@Override
		public TypeInfo getResultType() { return TypeInfo.BOOLEAN; }
	}
}
