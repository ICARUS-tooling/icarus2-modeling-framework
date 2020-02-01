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

import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.eval.Expression.BooleanExpression;
import de.ims.icarus2.query.api.eval.Expression.NumericalExpression;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.MutablePrimitives.MutableDouble;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.MutablePrimitives.Primitive;
import de.ims.icarus2.util.strings.StringUtil;

/**
 * @author Markus Gärtner
 *
 */
public class BinaryOperations {


	public static NumericalExpression numericalOp(AlgebraicOp op,
			NumericalExpression left, NumericalExpression right) {
		requireNonNull(op);
		checkNumericalType(left);
		checkNumericalType(right);

		NumericalExpression expression;

		if(requiresFloatingPointOp(left, right)) {
			expression = new BinaryDoubleOperation(left, right, op.getFloatingPointOp());
		} else {
			expression = new BinaryLongOperation(left, right, op.getIntegerOp());
		}

		return expression;
	}

	public static BooleanExpression numericalPred(NumericalComparator pred,
			NumericalExpression left, NumericalExpression right) {
		requireNonNull(pred);
		checkNumericalType(left);
		checkNumericalType(right);

		BooleanExpression expression;

		if(requiresFloatingPointOp(left, right)) {
			expression = new BinaryNumericalPredicate(left, right, pred.getFloatingPointPred());
		} else {
			expression = new BinaryNumericalPredicate(left, right, pred.getIntegerPred());
		}

		return expression;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static BooleanExpression comparablePred(ComparableComparator pred,
			Expression<?> left, Expression<?> right) {
		requireNonNull(pred);
		checkComparableType(left);
		checkComparableType(right);

		return new BinaryObjectPredicate<>(
				(Expression<Comparable>)left, (Expression<Comparable>)right, pred.getPred());
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static BooleanExpression equalityPred(EqualityPred pred,
			Expression<?> left, Expression<?> right) {
		requireNonNull(pred);

		return new BinaryObjectPredicate(left, right, pred.getPred());
	}

	private static void checkNumericalType(Expression<?> exp) {
		if(!TypeInfo.isNumerical(exp.getResultType()))
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not a proper numerical type: "+exp.getResultType());
	}

	private static void checkComparableType(Expression<?> exp) {
		if(!TypeInfo.isComparable(exp.getResultType()))
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not a type compatible with java.lang.Comparable: "+exp.getResultType());
	}

	private static boolean requiresFloatingPointOp(
			Expression<?> left, Expression<?> right) {
		return left.getResultType()==TypeInfo.DOUBLE
				|| right.getResultType()==TypeInfo.DOUBLE;
	}

	private static QueryException forUnsupportedFloatingPoint(String op) {
		return new QueryException(QueryErrorCode.TYPE_MISMATCH,
				"Operation does not support floating point types: "+op);
	}

	/**
	 * Base class for all operations that take two operands and produce a single
	 * result value.
	 *
	 * @author Markus Gärtner
	 *
	 * @param <T> the result type for {@link #compute()} and similar value retrieving method
	 * @param <E> the expression types expected as input for the left and right operands
	 */
	private abstract class AbstractBinaryOperation<T, E extends Expression<?>> implements Expression<T> {
		protected final E left;
		protected final E right;
		private boolean usesSideEffects;

		protected AbstractBinaryOperation(E left, E right, boolean usesSideEffects) {
			this.left = requireNonNull(left);
			this.right = requireNonNull(right);
			this.usesSideEffects = usesSideEffects;
			//TODO verify compatible result types
		}

		/**
		 * Asks the left and right operands to duplicate themselves and then properly duplicates
		 * this expression if at least one of the following conditions is met:
		 * <ul>
		 * <li>This expression uses side effects as specified at constructor time</li>
		 * <li>The duplicated left operand is different from the original</li>
		 * <li>The duplicated right operand is different from the original</li>
		 * </ul>
		 *
		 * If none of these conditions are met, this expression is returned as-is.
		 *
		 * @see de.ims.icarus2.query.api.eval.Expression#duplicate(de.ims.icarus2.query.api.eval.EvaluationContext)
		 */
		@Override
		public Expression<T> duplicate(EvaluationContext context) {
			@SuppressWarnings("unchecked")
			E newLeft = (E) left.duplicate(context);
			@SuppressWarnings("unchecked")
			E newRight = (E) right.duplicate(context);

			if(usesSideEffects || newLeft!=left || newRight!=right) {
				return duplicate(newLeft, newRight);
			}

			return this;
		}

		/**
		 * Asks the left and right operands to optimize themselves and
		 * {@link #toConstant(Expression, Expression) simplifies} this expression into a constant
		 * if both the optimized operands report being constant. Otherwise TODO
		 *
		 * If none of these conditions are met, this expression is returned as-is.
		 *
		 * @see de.ims.icarus2.query.api.eval.Expression#duplicate(de.ims.icarus2.query.api.eval.EvaluationContext)
		 */
		@Override
		public Expression<T> optimize(EvaluationContext context) {
			@SuppressWarnings("unchecked")
			E newLeft = (E) left.optimize(context);
			@SuppressWarnings("unchecked")
			E newRight = (E) right.optimize(context);

			if(newLeft.isConstant() && newRight.isConstant()) {
				return toConstant(newLeft, newRight);
			}

			return this;
		}

		protected abstract Expression<T> duplicate(E left, E right);

		protected abstract Expression<T> toConstant(E left, E right);
	}

	static class BinaryDoubleOperation
			extends AbstractBinaryOperation<Primitive<?>, NumericalExpression>
			implements NumericalExpression {

		private final NumericalToDoubleOp op;
		private final MutableDouble value;

		protected BinaryDoubleOperation(NumericalExpression left, NumericalExpression right,
				NumericalToDoubleOp op) {
			super(left, right, true);
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
			return op.applyAsDouble(left, right);
		}

		@Override
		protected BinaryDoubleOperation duplicate(NumericalExpression left,
				NumericalExpression right) {
			return new BinaryDoubleOperation(left, right, op);
		}

		@Override
		protected NumericalExpression toConstant(NumericalExpression left,
				NumericalExpression right) {
			return Literals.of(op.applyAsDouble(left, right));
		}
	}

	static class BinaryLongOperation
			extends AbstractBinaryOperation<Primitive<?>, NumericalExpression>
			implements NumericalExpression {

		private final NumericalToLongOp op;
		private final MutableLong value;

		protected BinaryLongOperation(NumericalExpression left, NumericalExpression right,
				NumericalToLongOp op) {
			super(left, right, true);
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
			return op.applyAsLong(left, right);
		}

		@Override
		protected BinaryLongOperation duplicate(NumericalExpression left,
				NumericalExpression right) {
			return new BinaryLongOperation(left, right, op);
		}

		@Override
		protected NumericalExpression toConstant(NumericalExpression left,
				NumericalExpression right) {
			return Literals.of(op.applyAsLong(left, right));
		}
	}

	private static abstract class AbstractBinaryPredicate<E extends Expression<?>>
			extends AbstractBinaryOperation<Primitive<Boolean>, E>
			implements BooleanExpression {

		protected final MutableBoolean value;

		AbstractBinaryPredicate(E left, E right) {
			super(left, right, true);
			value = new MutableBoolean();
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.BOOLEAN; }

		@Override
		public Primitive<Boolean> compute() {
			value.setBoolean(computeAsBoolean());
			return value;
		}
	}

	static class BinaryObjectPredicate<T> extends AbstractBinaryPredicate<Expression<T>> {

		private final Pred<T> pred;

		BinaryObjectPredicate(Expression<T> left, Expression<T> right, Pred<T> pred) {
			super(left, right);
			this.pred = requireNonNull(pred);
		}

		@Override
		public boolean computeAsBoolean() {
			return pred.test(left, right);
		}

		@Override
		protected BinaryObjectPredicate<T> duplicate(Expression<T> left,
				Expression<T> right) {
			return new BinaryObjectPredicate<>(left, right, pred);
		}

		@Override
		protected BooleanExpression toConstant(Expression<T> left,
				Expression<T> right) {
			return Literals.of(pred.test(left, right));
		}
	}

	static class BinaryNumericalPredicate extends AbstractBinaryPredicate<NumericalExpression> {

		private final NumericalPred pred;

		BinaryNumericalPredicate(NumericalExpression left, NumericalExpression right,
				NumericalPred pred) {
			super(left, right);
			this.pred = requireNonNull(pred);
		}

		@Override
		public boolean computeAsBoolean() {
			return pred.test(left, right);
		}

		@Override
		protected BinaryNumericalPredicate duplicate(NumericalExpression left,
				NumericalExpression right) {
			return new BinaryNumericalPredicate(left, right, pred);
		}

		@Override
		protected BooleanExpression toConstant(NumericalExpression left,
				NumericalExpression right) {
			return Literals.of(pred.test(left, right));
		}
	}

	static class StringContainment extends AbstractBinaryPredicate<Expression<CharSequence>> {

		StringContainment(Expression<CharSequence> left, Expression<CharSequence> right,
				NumericalPred pred) {
			super(left, right);
		}

		@Override
		public boolean computeAsBoolean() {
			return StringUtil.indexOf(left.compute(), right.compute()) > -1;
		}

		@Override
		protected StringContainment duplicate(Expression<CharSequence> left,
				Expression<CharSequence> right) {
			return new BinaryNumericalPredicate(left, right, pred);
		}

		@Override
		protected BooleanExpression toConstant(Expression<CharSequence> left,
				Expression<CharSequence> right) {
			return Literals.of(pred.test(left, right));
		}
	}

	@FunctionalInterface
	private interface NumericalToLongOp {
		long applyAsLong(NumericalExpression left, NumericalExpression right);
	}

	@FunctionalInterface
	private interface NumericalToDoubleOp {
		double applyAsDouble(NumericalExpression left, NumericalExpression right);
	}

	public enum AlgebraicOp {
		// Mathematical operations
		ADD((l, r) -> l.computeAsLong()+r.computeAsLong(),
				(l, r) -> l.computeAsDouble()+r.computeAsDouble()),
		SUB((l, r) -> l.computeAsLong()-r.computeAsLong(),
				(l, r) -> l.computeAsDouble()-r.computeAsDouble()),
		MULT((l, r) -> l.computeAsLong()*r.computeAsLong(),
				(l, r) -> l.computeAsDouble()*r.computeAsDouble()),
		DIV((l, r) -> l.computeAsLong()/r.computeAsLong(),
				(l, r) -> l.computeAsDouble()/r.computeAsDouble()),
		MOD((l, r) -> l.computeAsLong()%r.computeAsLong(),
				(l, r) -> l.computeAsDouble()%r.computeAsDouble()),

		// Bitwise operations
		LSHIFT((l, r) -> l.computeAsLong()<<r.computeAsLong(), null),
		RSHIFT((l, r) -> l.computeAsLong()>>r.computeAsLong(), null),
		BIT_AND((l, r) -> l.computeAsLong()&r.computeAsLong(), null),
		BIT_OR((l, r) -> l.computeAsLong()|r.computeAsLong(), null),
		BIT_XOR((l, r) -> l.computeAsLong()^r.computeAsLong(), null),
		;

		private final NumericalToLongOp integerOp;
		private final NumericalToDoubleOp floatingPointOp;

		private AlgebraicOp(NumericalToLongOp integerOp, NumericalToDoubleOp floatingPointOp) {
			this.integerOp = integerOp;
			this.floatingPointOp = floatingPointOp;
		}

		public NumericalToLongOp getIntegerOp() { return integerOp; }

		public NumericalToDoubleOp getFloatingPointOp() {
			if(floatingPointOp==null)
				throw forUnsupportedFloatingPoint(name());
			return floatingPointOp;
		}
	}

	@FunctionalInterface
	private interface NumericalPred {
		boolean test(NumericalExpression left, NumericalExpression right);
	}

	@FunctionalInterface
	private interface Pred<T> {
		boolean test(Expression<T> left, Expression<T> right);
	}

	public enum NumericalComparator {
		// Comparison (predicates)
		LESS((l, r) -> l.computeAsLong()<r.computeAsLong(),
				(l, r) -> Double.compare(l.computeAsDouble(), r.computeAsDouble())<0),
		GREATER((l, r) -> l.computeAsLong()>r.computeAsLong(),
				(l, r) -> Double.compare(l.computeAsDouble(), r.computeAsDouble())>0),
		LESS_OR_EQUAL((l, r) -> l.computeAsLong()<=r.computeAsLong(),
				(l, r) -> Double.compare(l.computeAsDouble(), r.computeAsDouble())<=0),
		GREATER_OR_EQUAL((l, r) -> l.computeAsLong()>=r.computeAsLong(),
				(l, r) -> Double.compare(l.computeAsDouble(), r.computeAsDouble())>=0),

		// Equality checks
		EQUALS((l, r) -> l.computeAsLong()==r.computeAsLong(),
				(l, r) -> Double.compare(l.computeAsDouble(), r.computeAsDouble())==0),
		NOT_EQUALS((l, r) -> l.computeAsLong()==r.computeAsLong(),
				(l, r) -> Double.compare(l.computeAsDouble(), r.computeAsDouble())==0),
		;

		private final NumericalPred integerPred;
		private final NumericalPred floatingPointPred;

		private NumericalComparator(NumericalPred integerPred, NumericalPred floatingPointPred) {
			this.integerPred = integerPred;
			this.floatingPointPred = floatingPointPred;
		}

		public NumericalPred getIntegerPred() { return integerPred; }

		public NumericalPred getFloatingPointPred() { return floatingPointPred; }
	}

	public enum EqualityPred {
		// Equality checks
		EQUALS((l, r) -> l.compute().equals(r.compute())),
		NOT_EQUALS((l, r) -> !l.compute().equals(r.compute())),
		;

		private final Pred<?> pred;

		private EqualityPred(Pred<?> pred) {
			this.pred = requireNonNull(pred);
		}

		public Pred<?> getPred() {
			return pred;
		}
	}

	@SuppressWarnings("rawtypes")
	public enum ComparableComparator {
		// Comparison (predicates)
		LESS((l, r) -> l.compute().compareTo(r.compute())<0),
		GREATER((l, r) -> l.compute().compareTo(r.compute())>0),
		LESS_OR_EQUAL((l, r) -> l.compute().compareTo(r.compute())<=0),
		GREATER_OR_EQUAL((l, r) -> l.compute().compareTo(r.compute())>=0),

		// Equality checks
		EQUALS((l, r) -> l.compute().compareTo(r.compute())==0),
		NOT_EQUALS((l, r) -> l.compute().compareTo(r.compute())!=0),
		;

		private final Pred<Comparable> pred;

		private ComparableComparator(Pred<Comparable> pred) {
			this.pred = requireNonNull(pred);
		}

		public Pred<Comparable> getPred() {
			return pred;
		}
	}

	public enum StringPred implements BiPredicate<CharSequence, CharSequence> {
		EQUALS{
			@Override
			public boolean test(CharSequence cs1, CharSequence cs2) {
				int len1 = cs1.length();
				int len2 = cs2.length();
				if(len1!=len2) {
					return false;
				}
				for (int i = 0; i < len1; i++) {
					if(cs1.charAt(i)!=cs2.charAt(i)) {
						return false;
					}
				}
				return true;
			}
		},

		EQUALS_NOT{
			@Override
			public boolean test(CharSequence cs1, CharSequence cs2) {
				int len1 = cs1.length();
				int len2 = cs2.length();
				if(len1!=len2) {
					return true;
				}
				for (int i = 0; i < len1; i++) {
					if(cs1.charAt(i)!=cs2.charAt(i)) {
						return true;
					}
				}
				return false;
			}
		},

		EQUALS_LOWER_CASE{
			@Override
			public boolean test(CharSequence cs1, CharSequence cs2) {
				int len1 = cs1.length();
				int len2 = cs2.length();
				if(len1!=len2) {
					return false;
				}
				int cbLen1 = cs1.codePoints()
				for (int i = 0; i < len1; i++) {
					if(Character.toLowerCase(ch)cs1.charAt(i)!=cs2.charAt(i)) {
						return false;
					}
				}
				return true;
			}
		},
		;
	}
}
