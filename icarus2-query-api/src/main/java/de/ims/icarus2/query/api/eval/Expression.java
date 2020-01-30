/**
 *
 */
package de.ims.icarus2.query.api.eval;

/**
 * @author Markus Gärtner
 *
 */
public interface Expression<T> {

	/** Returns the most precise description of the result type for this expression. */
	TypeInfo getResultType();

	/** Returns the result of evaluating this expression, computing it first if needed. */
	T compute();

	/**
	 * Returns {@code true} iff the return value of this expression won't change in the
	 * future. This information is used when optimizing complex expressions by hoisting
	 * the constant results of nested expressions to pre-compute the final values of
	 * expressions higher up the tree.
	 * <p>
	 * The default implementation returns {@code false}.
	 */
	default boolean isConstant() {
		return false;
	}

	/**
	 * Allows an expression to analyze the nested expressions it uses or the
	 * {@link EvaluationContext context} it is being used in and to provide an
	 * optimized alternate version of it.
	 * <p>
	 * The default implementation returns the expression itself.
	 */
	default Expression<T> optimize(EvaluationContext context) {
		return this;
	}

	/**
	 * Effectively clones this expression to be used in the new {@link EvaluationContext context}.
	 *
	 */
	Expression<T> duplicate(EvaluationContext context);

	/**
	 * Specialized expression with primitive numerical return value.
	 *
	 * @author Markus Gärtner
	 *
	 * @param <N> actual wrapper type of the primitive source type
	 */
	public interface NumericalExpression<N extends Number> extends Expression<N> {
//		int computeAsInt();
		long computeAsLong();
//		float computeAsFloat();
		double computeAsDouble();
	}

	/**
	 * Specialized expression with primitive return value.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface BooleanExpression extends Expression<Boolean> {
		boolean computeAsBoolean();
	}

	/** Gives list-style access to the underlying data */
	public interface ListExpression<T, E> extends Expression<T>, ListProxy<E> {

	}

	//TODO
}
