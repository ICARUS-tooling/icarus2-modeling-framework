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

import javax.annotation.concurrent.NotThreadSafe;

import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.util.MutablePrimitives.Primitive;
import de.ims.icarus2.util.strings.CodePointSequence;

/**
 * Models the basic building blocks for evaluating and accessing corpus data through the
 * IQL evaluation engine. Note that {@link Expression} instances are not required to be
 * thread-safe and as such no expression may ever be used by more than a single thread
 * during its lifetime. A special method for {@link #duplicate(EvaluationContext) cloning}
 * is specified that allows to create a new expression instance that behaves exactly as
 * the original but can be used in another thread.
 *
 * @author Markus Gärtner
 *
 */
@NotThreadSafe
public interface Expression<T> {

	/** Returns the most precise description of the result type for this expression. */
	TypeInfo getResultType();

	/**
	 * Returns the result of evaluating this expression, computing it first if needed.
	 *
	 * @throws QueryException of type {@link QueryErrorCode#TYPE_MISMATCH} if the computed
	 * value's type clashes with the expectations set by the parameterized type {@code T}.
	 */
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
	 * <p>
	 * Note that every expression that operates via side effects or is in any way <b>not</b>
	 * thread-safe, <b>must</b> return a fresh and independent new instance as a result of
	 * this method!! Not doing so will inevitably result in race conditions during execution
	 * within the IQL evaluation engine and corrupt any results.
	 */
	Expression<T> duplicate(EvaluationContext context);

	default boolean isText() {
		return TypeInfo.isText(getResultType());
	}

	default boolean isNumerical() {
		return TypeInfo.isNumerical(getResultType());
	}

	default boolean isBoolean() {
		return TypeInfo.isBoolean(getResultType());
	}


	/**
	 * Specialized expression with primitive numerical return value.
	 *
	 * @author Markus Gärtner
	 */
	public interface NumericalExpression extends Expression<Primitive<? extends Number>> {

		/**
		 * Primitive equivalent of {@link #compute()} that avoids object generation and
		 * primitive wrapping.
		 * @return
		 * @throws QueryException of type {@link QueryErrorCode#TYPE_MISMATCH} if the computed
		 * value cannot directly be converted to a {@code long} value.
		 */
		long computeAsLong();

		/**
		 * Primitive equivalent of {@link #compute()} that avoids object generation and
		 * primitive wrapping.
		 * @return
		 * @throws QueryException of type {@link QueryErrorCode#TYPE_MISMATCH} if the computed
		 * value cannot directly be converted to a {@code double} value.
		 */
		double computeAsDouble();

		/** REturns true iff this expression is a floating point expression */
		default boolean isFPE() {
			return getResultType()==TypeInfo.DOUBLE;
		}
	}

	/**
	 * Specialized expression with primitive return value.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface BooleanExpression extends Expression<Primitive<Boolean>> {
		boolean computeAsBoolean();

		@Override
		default TypeInfo getResultType() {
			return TypeInfo.BOOLEAN;
		}
	}

	/**
	 * Specialized expression with unicode text value.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface TextExpression extends Expression<CodePointSequence> {

		CharSequence computeAsChars();

		@Override
		default TypeInfo getResultType() {
			return TypeInfo.TEXT;
		}
	}

	/** Gives list-style access to the underlying data */
	public interface ListExpression<T, E> extends Expression<T>, ListProxy<E> {

		/** Returns the type of elements that are accessable as part of this expressions output */
		TypeInfo getElementType();

	}

	//TODO
}
