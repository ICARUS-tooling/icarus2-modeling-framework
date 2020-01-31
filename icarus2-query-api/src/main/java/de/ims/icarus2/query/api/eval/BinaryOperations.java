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

import static java.util.Objects.requireNonNull;

import java.util.function.BiPredicate;
import java.util.function.DoubleBinaryOperator;
import java.util.function.LongBinaryOperator;

import de.ims.icarus2.query.api.eval.Expression.NumericalExpression;
import de.ims.icarus2.util.MutablePrimitives.MutableDouble;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.MutablePrimitives.Primitive;
import de.ims.icarus2.util.function.DoubleBiPredicate;
import de.ims.icarus2.util.function.LongBiPredicate;

/**
 * @author Markus Gärtner
 *
 */
public class BinaryOperations {

	/**
	 * Base class for all operations that take two operands and produce a single
	 * result value.
	 *
	 * @author Markus Gärtner
	 *
	 * @param <T> the result type for {@link #compute()} and similar value retrieving method
	 * @param <E> the expression types expected as input for the left and right operands
	 */
	private static abstract class AbstractBinaryOperation<T, E extends Expression<?>> implements Expression<T> {
		protected final E left;
		protected final E right;

		protected AbstractBinaryOperation(E left, E right) {
			this.left = requireNonNull(left);
			this.right = requireNonNull(right);
			//TODO verify compatible result types
		}

		@Override
		public Expression<T> duplicate(EvaluationContext context) {
			@SuppressWarnings("unchecked")
			E newLeft = (E) left.duplicate(context);
			@SuppressWarnings("unchecked")
			E newRight = (E) right.duplicate(context);

			if(newLeft!=left || newRight!=right) {
				return duplicate(newLeft, newRight);
			}

			return this;
		}

		protected abstract Expression<T> duplicate(E left, E right);
	}

	static class BinaryDoubleOperation
			extends AbstractBinaryOperation<Primitive<Double>, NumericalExpression<? extends Number>>
			implements NumericalExpression<Double> {

		private final DoubleBinaryOperator op;
		private final MutableDouble value;

		protected BinaryDoubleOperation(NumericalExpression<?> left, NumericalExpression<?> right,
				DoubleBinaryOperator op) {
			super(left, right);
			this.op = requireNonNull(op);
			value = new MutableDouble();
		}

		@Override
		public Primitive<Double> compute() {
			value.setDouble(computeAsDouble());
			return value;
		}

		//TODO do we really want to simply cast to long?
		@Override
		public long computeAsLong() { return (long) computeAsDouble(); }

		@Override
		public TypeInfo getResultType() { return TypeInfo.DOUBLE; }

		@Override
		public double computeAsDouble() {
			return op.applyAsDouble(left.computeAsDouble(), right.computeAsDouble());
		}

		@Override
		public NumericalExpression<Double> optimize(EvaluationContext context) {
			@SuppressWarnings("rawtypes")
			NumericalExpression<?> newLeft = (NumericalExpression) left.optimize(context);
			@SuppressWarnings("rawtypes")
			NumericalExpression<?> newRight = (NumericalExpression) right.optimize(context);

			if(newLeft.isConstant() && newRight.isConstant()) {
				return new Literals.FloatingPointLiteral(
						op.applyAsDouble(newLeft.computeAsDouble(), newRight.computeAsDouble()));
			}

			return this;
		}

		@Override
		protected BinaryDoubleOperation duplicate(NumericalExpression<?> left,
				NumericalExpression<?> right) {
			return new BinaryDoubleOperation(left, right, op);
		}
	}

	static class BinaryLongOperation
			extends AbstractBinaryOperation<Primitive<Long>, NumericalExpression<? extends Number>>
			implements NumericalExpression<Long> {

		private final LongBinaryOperator op;
		private final MutableLong value;

		protected BinaryLongOperation(NumericalExpression<?> left, NumericalExpression<?> right,
				LongBinaryOperator op) {
			super(left, right);
			this.op = requireNonNull(op);
			value = new MutableLong();
		}

		@Override
		public Primitive<Long> compute() {
			value.setLong(computeAsLong());
			return value;
		}

		@Override
		public double computeAsDouble() { return computeAsLong(); }

		@Override
		public TypeInfo getResultType() { return TypeInfo.DOUBLE; }

		@Override
		public long computeAsLong() {
			return op.applyAsLong(left.computeAsLong(), right.computeAsLong());
		}

		@Override
		public NumericalExpression<Long> optimize(EvaluationContext context) {
			@SuppressWarnings("rawtypes")
			NumericalExpression<?> newLeft = (NumericalExpression) left.optimize(context);
			@SuppressWarnings("rawtypes")
			NumericalExpression<?> newRight = (NumericalExpression) right.optimize(context);

			if(newLeft.isConstant() && newRight.isConstant()) {
				return new Literals.IntegerLiteral(
						op.applyAsLong(newLeft.computeAsLong(), newRight.computeAsLong()));
			}

			return this;
		}

		@Override
		protected BinaryLongOperation duplicate(NumericalExpression<?> left,
				NumericalExpression<?> right) {
			return new BinaryLongOperation(left, right, op);
		}
	}


	public enum BinaryNumericalOp {
		// Mathematical operations
		ADD((l, r) -> l+r, (l, r) -> l+r),
		SUB((l, r) -> l-r, (l, r) -> l-r),
		MULT((l, r) -> l*r, (l, r) -> l*r),
		DIV((l, r) -> l/r, (l, r) -> l/r),
		MOD((l, r) -> l%r, (l, r) -> l%r),

		// Bitwise operations
		LSHIFT((l, r) -> l<<r, null),
		RSHIFT((l, r) -> l>>r, null),
		BIT_AND((l, r) -> l&r, null),
		BIT_OR((l, r) -> l|r, null),
		BIT_XOR((l, r) -> l^r, null),
		;

		private final LongBinaryOperator integerOp;
		private final DoubleBinaryOperator floatingPointOp;

		private BinaryNumericalOp(LongBinaryOperator integerOp, DoubleBinaryOperator floatingPointOp) {
			this.integerOp = integerOp;
			this.floatingPointOp = floatingPointOp;
		}
	}

	public enum BinaryNumericalPredicate {
		// Comparison (predicates)
		LESS((l, r) -> l<r, (l, r) -> Double.compare(l, r)<0),
		GREATER((l, r) -> l>r, (l, r) -> Double.compare(l, r)>0),
		LESS_OR_EQUAL((l, r) -> l<=r, (l, r) -> Double.compare(l, r)<=0),
		GREATER_OR_EQUAL((l, r) -> l>=r, (l, r) -> Double.compare(l, r)>=0),

		// Equality checks
		EQUALS((l, r) -> l==r, (l, r) -> Double.compare(l, r)==0),
		NOT_EQUALS((l, r) -> l==r, (l, r) -> Double.compare(l, r)==0),
		;

		private final LongBiPredicate integerPred;
		private final DoubleBiPredicate floatingPointPred;

		private BinaryNumericalPredicate(LongBiPredicate integerPred, DoubleBiPredicate floatingPointPred) {
			this.integerPred = integerPred;
			this.floatingPointPred = floatingPointPred;
		}
	}

	public enum BinaryObjectPredicate {

		// Equality checks
		EQUALS((l, r) -> l.equals(r)),
		NOT_EQUALS((l, r) -> !l.equals(r)),
		;

		private final BiPredicate<?, ?> pred;

		private BinaryObjectPredicate(BiPredicate<?, ?> pred) {
			this.pred = pred;
		}
	}
}
