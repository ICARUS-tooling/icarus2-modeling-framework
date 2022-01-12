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

import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static java.util.Objects.requireNonNull;

import javax.annotation.concurrent.NotThreadSafe;

import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.util.MutablePrimitives.Primitive;
import de.ims.icarus2.util.lang.Primitives;

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
		requireNonNull(context);
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

	default boolean isInteger() {
		return TypeInfo.isInteger(getResultType());
	}

	default boolean isFloatingPoint() {
		return TypeInfo.isFloatingPoint(getResultType());
	}

	default boolean isBoolean() {
		return TypeInfo.isBoolean(getResultType());
	}

	default boolean isList() {
		return getResultType().isList();
	}

	default boolean isMember() {
		return getResultType().isMember();
	}

	default boolean isProxy() {
		return this instanceof ProxyExpression;
	}

	/**
	 * Primitive equivalent of {@link #compute()} that avoids object generation and
	 * primitive wrapping.
	 * @return
	 * @throws QueryException of type {@link QueryErrorCode#TYPE_MISMATCH} if the computed
	 * value cannot directly be converted to a {@code long} value.
	 */
	default long computeAsLong() {
		throw EvaluationUtils.forUnsupportedCast(getResultType(), TypeInfo.INTEGER);
	}

	/**
	 * Utility default method to support situations where strictly {@code int} values
	 * are desired. Delegates to {@link #computeAsLong()} and converts to an integer
	 * via {@link Primitives#strictToInt(long)}.
	 */
	default int computeAsInt() {
		return strictToInt(computeAsLong());
	}

	/**
	 * Primitive equivalent of {@link #compute()} that avoids object generation and
	 * primitive wrapping.
	 * @return
	 * @throws QueryException of type {@link QueryErrorCode#TYPE_MISMATCH} if the computed
	 * value cannot directly be converted to a {@code double} value.
	 */
	default double computeAsDouble() {
		throw EvaluationUtils.forUnsupportedCast(getResultType(), TypeInfo.FLOATING_POINT);
	}

	default boolean computeAsBoolean() {
		throw EvaluationUtils.forUnsupportedCast(getResultType(), TypeInfo.BOOLEAN);
	}

	/**
	 * Marker interface to signal that the implementation returns some primitive
	 * wrapper, i.e an instance of {@link Primitive}.
	 *
	 * @author Markus Gärtner
	 */
	public interface PrimitiveExpression {
		// marker interface
	}

	/**
	 * Marker interface to signal that the expression is operating on some shared backend
	 * data, i.e. an annotation storage, and as such could represent a bottleneck in terms
	 * of performance when parallelizing the evaluation process.
	 * <p>
	 * Currently we only mark implementations and don't take special actions or perform
	 * analysis, but the foundation is there.
	 *
	 * @author Markus Gärtner
	 */
	public interface SharedExpression {
		// marker interface
	}

	/**
	 * Signals that an expression is designed as a temporary helper during the matcher
	 * construction process. Implementations of this interface are not meant to be used
	 * in the actual evaluation phase and are expected to throw an exception when trying
	 * to use any of the computation methods, such as {@link Expression#compute()}.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface ProxyExpression {
		// marker interface
	}

	/** Gives list-style access to the underlying data */
	public interface ListExpression<T, E> extends Expression<T>, ListProxy<E> {

		/** Returns the type of elements that are accessible as part of this expressions output */
		TypeInfo getElementType();

		/**
		 * Computes and/or returns the raw {@code list-style} object behind this expression.
		 * Since the actual
		 * @see de.ims.icarus2.query.api.exp.Expression#compute()
		 */
		@Override
		T compute();

		/**
		 * Returns whether the size of this list is constant, i.e. it behaves as an array.
		 */
		boolean isFixedSize();
	}

	public interface IntegerListExpression<T> extends ListExpression<T, Primitive<Long>>, ListProxy.OfInteger {
		@Override
		default TypeInfo getElementType() { return TypeInfo.INTEGER; }
	}

	public interface FloatingPointListExpression<T> extends ListExpression<T, Primitive<Double>>, ListProxy.OfFloatingPoint {
		@Override
		default TypeInfo getElementType() { return TypeInfo.FLOATING_POINT; }
	}

	public interface BooleanListExpression<T> extends ListExpression<T, Primitive<Boolean>>, ListProxy.OfBoolean {
		@Override
		default TypeInfo getElementType() { return TypeInfo.BOOLEAN; }
	}
}
