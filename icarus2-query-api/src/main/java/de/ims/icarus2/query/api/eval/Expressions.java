/**
 *
 */
package de.ims.icarus2.query.api.eval;

import static java.util.Objects.requireNonNull;

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
}
