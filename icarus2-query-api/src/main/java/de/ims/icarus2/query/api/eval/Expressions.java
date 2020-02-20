/**
 *
 */
package de.ims.icarus2.query.api.eval;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

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
		public Expression<T> duplicate(EvaluationContext context) { return this; }

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
}
