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

import de.ims.icarus2.util.MutablePrimitives.Primitive;

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
	public interface NumericalExpression<N extends Number> extends Expression<Primitive<N>> {
		long computeAsLong();
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
