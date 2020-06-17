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

import static de.ims.icarus2.query.api.exp.EvaluationUtils.castComparable;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.castText;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.checkComparableType;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.checkNumericalType;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.checkTextType;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.forUnsupportedCast;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.forUnsupportedFloatingPoint;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.forUnsupportedTextComp;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.requiresFloatingPointOp;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;
import static java.util.Objects.requireNonNull;

import java.util.function.BiPredicate;
import java.util.function.IntPredicate;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToLongBiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.query.api.exp.Expression.PrimitiveExpression;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.MutablePrimitives.MutableDouble;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.MutablePrimitives.Primitive;
import de.ims.icarus2.util.function.CharBiPredicate;
import de.ims.icarus2.util.function.IntBiPredicate;

/**
 * @author Markus Gärtner
 *
 */
public class BinaryOperations {


	public static Expression<?>  numericalOp(AlgebraicOp op,
			Expression<?> left, Expression<?> right) {
		requireNonNull(op);
		checkNumericalType(left);
		checkNumericalType(right);

		Expression<?>  expression;

		if(requiresFloatingPointOp(left, right)) {
			expression = new BinaryDoubleOperation(left, right, op.getFloatingPointOp());
		} else {
			expression = new BinaryLongOperation(left, right, op.getIntegerOp());
		}

		return expression;
	}

	public static Expression<Primitive<Boolean>> numericalPred(NumericalComparator pred,
			Expression<?> left, Expression<?> right) {
		requireNonNull(pred);
		checkNumericalType(left);
		checkNumericalType(right);

		Expression<Primitive<Boolean>> expression;

		if(requiresFloatingPointOp(left, right)) {
			expression = new BinaryNumericalPredicate(left, right, pred.getFloatingPointPred());
		} else {
			expression = new BinaryNumericalPredicate(left, right, pred.getIntegerPred());
		}

		return expression;
	}

	public static Expression<Primitive<Boolean>> comparablePred(ComparableComparator pred,
			Expression<?> left, Expression<?> right) {
		requireNonNull(pred);
		checkComparableType(left);
		checkComparableType(right);

		return new BinaryObjectPredicate<>(castComparable(left), castComparable(right), pred.getPred());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Expression<Primitive<Boolean>> equalityPred(EqualityPred pred,
			Expression<?> left, Expression<?> right) {
		requireNonNull(pred);
		return new BinaryObjectPredicate(left, right, pred.getPred());
	}

	public static Expression<Primitive<Boolean>> unicodeOp(StringOp op, StringMode mode,
			Expression<?> left, Expression<?> right) {
		requireNonNull(op);
		checkTextType(left);
		checkTextType(right);
		switch (op) {
		case EQUALS: return new UnicodeEquality(castText(left), castText(right), mode.getCodePointComparator());
		case CONTAINS: return new UnicodeContainment(castText(left), castText(right), mode.getCodePointComparator());
		case MATCHES: return new StringRegex(castText(left), castText(right), mode, true);

		case LESS:
		case LESS_OR_EQUAL:
		case GREATER:
		case GREATER_OR_EQUAL:
			return new UnicodeComparison(castText(left), castText(right), mode.getCodePointComparator(), op.getComp());

		default:
			throw new IcarusRuntimeException(GlobalErrorCode.INTERNAL_ERROR,
					"Unknown string operation: "+op);
		}
	}

	public static Expression<Primitive<Boolean>> asciiOp(StringOp op, StringMode mode,
			Expression<?> left, Expression<?> right) {
		requireNonNull(op);
		checkTextType(left);
		checkTextType(right);
		switch (op) {
		case EQUALS: return new CharsEquality(castText(left), castText(right), mode.getCharComparator());
		case CONTAINS: return new CharsContainment(castText(left), castText(right), mode.getCharComparator());
		case MATCHES: return new StringRegex(castText(left), castText(right), mode, false);

		case LESS:
		case LESS_OR_EQUAL:
		case GREATER:
		case GREATER_OR_EQUAL:
			return new CharsComparison(castText(left), castText(right), mode.getCharComparator(), op.getComp());

		default:
			throw new IcarusRuntimeException(GlobalErrorCode.INTERNAL_ERROR,
					"Unknown string operation: "+op);
		}
	}

	// Helpers

	/**
	 * Base class for all operations that take two operands and produce a single
	 * result value.
	 *
	 * @author Markus Gärtner
	 *
	 * @param <T> the result type for {@link #compute()} and similar value retrieving method
	 * @param <E> the expression types expected as input for the left and right operands
	 */
	@SuppressWarnings("rawtypes")
	private static abstract class AbstractBinaryOperation<T, E extends Expression> implements Expression<T> {
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
		 */
		@Override
		public Expression<T> duplicate(EvaluationContext context) {
			requireNonNull(context);

			@SuppressWarnings("unchecked")
			E newLeft = (E) context.duplicate(left);
			@SuppressWarnings("unchecked")
			E newRight = (E) context.duplicate(right);

			if(usesSideEffects || newLeft!=left || newRight!=right) {
				return duplicate(newLeft, newRight);
			}

			return this;
		}

		/**
		 * Asks the left and right operands to optimize themselves and
		 * {@link #toConstant(Expression, Expression) simplifies} this expression into a constant
		 * if both the optimized operands report being constant. Otherwise this
		 * expression is returned as-is.
		 *
		 * @see de.ims.icarus2.query.api.exp.Expression#optimize(EvaluationContext)
		 */
		@Override
		public Expression<T> optimize(EvaluationContext context) {
			requireNonNull(context);

			@SuppressWarnings("unchecked")
			E newLeft = (E) context.optimize(left);
			@SuppressWarnings("unchecked")
			E newRight = (E) context.optimize(right);

			if(newLeft.isConstant() && newRight.isConstant()) {
				return toConstant(newLeft, newRight);
			}

			return this;
		}

		protected abstract Expression<T> duplicate(E left, E right);

		protected abstract Expression<T> toConstant(E left, E right);
	}

	static class BinaryDoubleOperation
			extends AbstractBinaryOperation<Primitive<Double>, Expression<?>>
	 		implements PrimitiveExpression {

		private final ToDoubleBiFunction<Expression<?>, Expression<?>> op;
		private final MutableDouble value;

		protected BinaryDoubleOperation(Expression<?>  left, Expression<?>  right,
				ToDoubleBiFunction<Expression<?>, Expression<?>> op) {
			super(left, right, true);
			this.op = requireNonNull(op);
			value = new MutableDouble();
		}

		@Override
		public Primitive<Double> compute() {
			value.setDouble(computeAsDouble());
			return value;
		}

		@Override
		public long computeAsLong() { throw forUnsupportedCast(TypeInfo.FLOATING_POINT, TypeInfo.INTEGER); }

		@Override
		public TypeInfo getResultType() { return TypeInfo.FLOATING_POINT; }

		@Override
		public double computeAsDouble() {
			return op.applyAsDouble(left, right);
		}

		@Override
		protected BinaryDoubleOperation duplicate(Expression<?> left,
				Expression<?>  right) {
			return new BinaryDoubleOperation(left, right, op);
		}

		@Override
		protected Expression<Primitive<Double>>  toConstant(Expression<?> left, Expression<?> right) {
			return Literals.of(op.applyAsDouble(left, right));
		}
	}

	static final class BinaryLongOperation
			extends AbstractBinaryOperation<Primitive<Long>, Expression<?>>
	 		implements PrimitiveExpression {

		private final ToLongBiFunction<Expression<?>, Expression<?>> op;
		private final MutableLong value;

		protected BinaryLongOperation(Expression<?> left, Expression<?> right,
				ToLongBiFunction<Expression<?>, Expression<?>> op) {
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
		public TypeInfo getResultType() { return TypeInfo.INTEGER; }

		@Override
		public long computeAsLong() {
			return op.applyAsLong(left, right);
		}

		@Override
		protected BinaryLongOperation duplicate(Expression<?>  left,
				Expression<?>  right) {
			return new BinaryLongOperation(left, right, op);
		}

		@Override
		protected Expression<Primitive<Long>>  toConstant(Expression<?>  left,
				Expression<?>  right) {
			return Literals.of(op.applyAsLong(left, right));
		}
	}

	private static abstract class AbstractBinaryPredicate<E extends Expression<?>>
			extends AbstractBinaryOperation<Primitive<Boolean>, E>
			implements Expression<Primitive<Boolean>>, PrimitiveExpression {

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

	static final class BinaryObjectPredicate<T> extends AbstractBinaryPredicate<Expression<T>> {

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
		protected Expression<Primitive<Boolean>> toConstant(Expression<T> left,
				Expression<T> right) {
			return Literals.of(pred.test(left, right));
		}
	}

	static final class BinaryNumericalPredicate extends AbstractBinaryPredicate<Expression<?> > {

		private final BiPredicate<Expression<?>, Expression<?>> pred;

		BinaryNumericalPredicate(Expression<?> left, Expression<?> right,
				BiPredicate<Expression<?>, Expression<?>> pred) {
			super(left, right);
			this.pred = requireNonNull(pred);
		}

		@Override
		public boolean computeAsBoolean() {
			return pred.test(left, right);
		}

		@Override
		protected BinaryNumericalPredicate duplicate(Expression<?>  left, Expression<?>  right) {
			return new BinaryNumericalPredicate(left, right, pred);
		}

		@Override
		protected Expression<Primitive<Boolean>> toConstant(Expression<?>  left, Expression<?>  right) {
			return Literals.of(pred.test(left, right));
		}
	}

	static final class CharsEquality extends AbstractBinaryPredicate<Expression<CharSequence>> {

		private final CharBiPredicate comparator;

		CharsEquality(Expression<CharSequence> left, Expression<CharSequence> right,
				CharBiPredicate comparator) {
			super(left, right);
			this.comparator = requireNonNull(comparator);
		}

		@Override
		public boolean computeAsBoolean() {
			return CodePointUtils.equalsChars(left.compute(), right.compute(), comparator);
		}

		@Override
		protected CharsEquality duplicate(Expression<CharSequence> left, Expression<CharSequence> right) {
			return new CharsEquality(left, right, comparator);
		}

		@Override
		protected Expression<Primitive<Boolean>> toConstant(Expression<CharSequence> left, Expression<CharSequence> right) {
			return Literals.of(CodePointUtils.equalsChars(
					left.compute(), right.compute(), comparator));
		}
	}

	static final class UnicodeEquality extends AbstractBinaryPredicate<Expression<CharSequence>> {

		private final IntBiPredicate comparator;

		UnicodeEquality(Expression<CharSequence> left, Expression<CharSequence> right,
				IntBiPredicate comparator) {
			super(left, right);
			this.comparator = requireNonNull(comparator);
		}

		@Override
		public boolean computeAsBoolean() {
			return CodePointUtils.equalsCodePoints(left.compute(), right.compute(), comparator);
		}

		@Override
		protected UnicodeEquality duplicate(Expression<CharSequence> left, Expression<CharSequence> right) {
			return new UnicodeEquality(left, right, comparator);
		}

		@Override
		protected Expression<Primitive<Boolean>> toConstant(Expression<CharSequence> left, Expression<CharSequence> right) {
			return Literals.of(CodePointUtils.equalsCodePoints(
					left.compute(), right.compute(), comparator));
		}
	}

	static final class CharsContainment extends AbstractBinaryPredicate<Expression<CharSequence>> {

		private final CharBiPredicate comparator;

		CharsContainment(Expression<CharSequence> left, Expression<CharSequence> right,
				CharBiPredicate comparator) {
			super(left, right);
			this.comparator = requireNonNull(comparator);
		}

		@Override
		public boolean computeAsBoolean() {
			return CodePointUtils.containsChars(left.compute(), right.compute(), comparator);
		}

		@Override
		protected CharsContainment duplicate(Expression<CharSequence> left, Expression<CharSequence> right) {
			return new CharsContainment(left, right, comparator);
		}

		@Override
		protected Expression<Primitive<Boolean>> toConstant(Expression<CharSequence> left, Expression<CharSequence> right) {
			return Literals.of(CodePointUtils.containsChars(
					left.compute(), right.compute(), comparator));
		}
	}

	static final class UnicodeContainment extends AbstractBinaryPredicate<Expression<CharSequence>> {

		private final IntBiPredicate comparator;

		UnicodeContainment(Expression<CharSequence> left, Expression<CharSequence> right,
				IntBiPredicate comparator) {
			super(left, right);
			this.comparator = requireNonNull(comparator);
		}

		@Override
		public boolean computeAsBoolean() {
			return CodePointUtils.containsCodePoints(left.compute(), right.compute(), comparator);
		}

		@Override
		protected UnicodeContainment duplicate(Expression<CharSequence> left, Expression<CharSequence> right) {
			return new UnicodeContainment(left, right, comparator);
		}

		@Override
		protected Expression<Primitive<Boolean>> toConstant(Expression<CharSequence> left, Expression<CharSequence> right) {
			return Literals.of(CodePointUtils.containsCodePoints(
					left.compute(), right.compute(), comparator));
		}
	}

	static final class CharsComparison extends AbstractBinaryPredicate<Expression<CharSequence>> {

		private final CharBiPredicate charComparator;
		private final IntPredicate resultComparator;

		CharsComparison(Expression<CharSequence> left, Expression<CharSequence> right,
				CharBiPredicate charComparator, IntPredicate resultComparator) {
			super(left, right);
			this.charComparator = requireNonNull(charComparator);
			this.resultComparator = requireNonNull(resultComparator);
		}

		private static boolean test(CharSequence left, CharSequence right,
				CharBiPredicate charComparator, IntPredicate resultComparator) {
			return resultComparator.test(CodePointUtils.compare(left, right, charComparator));
		}

		@Override
		public boolean computeAsBoolean() {
			return test(left.compute(), right.compute(), charComparator, resultComparator);
		}

		@Override
		protected CharsComparison duplicate(Expression<CharSequence> left, Expression<CharSequence> right) {
			return new CharsComparison(left, right, charComparator, resultComparator);
		}

		@Override
		protected Expression<Primitive<Boolean>> toConstant(Expression<CharSequence> left, Expression<CharSequence> right) {
			return Literals.of(test(left.compute(), right.compute(), charComparator, resultComparator));
		}
	}

	static final class UnicodeComparison extends AbstractBinaryPredicate<Expression<CharSequence>> {

		private final IntBiPredicate codePointComparator;
		private final IntPredicate resultComparator;

		UnicodeComparison(Expression<CharSequence> left, Expression<CharSequence> right,
				IntBiPredicate codePointComparator, IntPredicate resultComparator) {
			super(left, right);
			this.codePointComparator = requireNonNull(codePointComparator);
			this.resultComparator = requireNonNull(resultComparator);
		}

		private static boolean test(CharSequence left, CharSequence right,
				IntBiPredicate codePointComparator, IntPredicate resultComparator) {
			return resultComparator.test(CodePointUtils.compareCodePoints(left, right, codePointComparator));
		}

		@Override
		public boolean computeAsBoolean() {
			return test(left.compute(), right.compute(), codePointComparator, resultComparator);
		}

		@Override
		protected UnicodeComparison duplicate(Expression<CharSequence> left, Expression<CharSequence> right) {
			return new UnicodeComparison(left, right, codePointComparator, resultComparator);
		}

		@Override
		protected Expression<Primitive<Boolean>> toConstant(Expression<CharSequence> left, Expression<CharSequence> right) {
			return Literals.of(test(left.compute(), right.compute(), codePointComparator, resultComparator));
		}
	}

	/**
	 * Wraps around an instance of {@link Matcher} to perform the actual regular expression
	 * matching. Note that the {@code right} expression for this operation must be constant!
	 * <p>
	 * Performance aspects:
	 * Regular expression matching is inherently a lot more expensive than simple equality
	 * or containment checks. The {@link Pattern} implementation already does a good job
	 * at optimizing simple expressions. Depending on the {@link StringMode mode} defined
	 * at constructor time, further performance penalties might come into effect when
	 * casing is meant to be ignored. This will be amplified when the regex expression itself
	 * also contains supplementary code points, as that will cause the pattern matching to
	 * be done based on unicode code points instead of simple char comparison.
	 *
	 * @author Markus Gärtner
	 *
	 */
	static final class StringRegex extends AbstractBinaryPredicate<Expression<CharSequence>> {

		private final Matcher matcher;

		StringRegex(Expression<CharSequence> left, Expression<CharSequence> right, StringMode mode, boolean allowUnicode) {
			super(left, right);
			checkArgument("Query part of regex expression must be constant", right.isConstant());

			String regex = right.compute().toString();

			matcher = EvaluationUtils.pattern(regex, mode, allowUnicode).matcher("");
		}

		/**
		 * Copy-constructor, possible due to {@link Pattern} being sharable ({@link Matcher}
		 * holds all the dynamic state for doing the actual matching!).
		 */
		private StringRegex(Expression<CharSequence> left, Expression<CharSequence> right, Pattern source) {
			super(left, right);
			matcher = source.matcher("");
		}

		@Override
		public boolean computeAsBoolean() {
			return matcher.reset(left.compute()).find();
		}

		@Override
		protected StringRegex duplicate(Expression<CharSequence> left, Expression<CharSequence> right) {
			// Use copy-constructor with the pre-compiled sharable Pattern
			return new StringRegex(left, right, matcher.pattern());
		}

		@Override
		protected Expression<Primitive<Boolean>> toConstant(Expression<CharSequence> left,
				Expression<CharSequence> right) {
			// We effectively ignore 'right', as that one is already supposed to be constant!!
			return Literals.of(matcher.reset(left.compute()).find());
		}
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
		MOD((l, r) -> l.computeAsLong()%r.computeAsLong(), null),

		// Bitwise operations
		LSHIFT((l, r) -> l.computeAsLong()<<r.computeAsLong(), null),
		RSHIFT((l, r) -> l.computeAsLong()>>r.computeAsLong(), null),
		BIT_AND((l, r) -> l.computeAsLong()&r.computeAsLong(), null),
		BIT_OR((l, r) -> l.computeAsLong()|r.computeAsLong(), null),
		BIT_XOR((l, r) -> l.computeAsLong()^r.computeAsLong(), null),
		;

		private final ToLongBiFunction<Expression<?>, Expression<?>> integerOp;
		private final ToDoubleBiFunction<Expression<?>, Expression<?>> floatingPointOp;

		private AlgebraicOp(ToLongBiFunction<Expression<?>, Expression<?>> integerOp,
				ToDoubleBiFunction<Expression<?>, Expression<?>> floatingPointOp) {
			this.integerOp = integerOp;
			this.floatingPointOp = floatingPointOp;
		}

		public ToLongBiFunction<Expression<?>, Expression<?>> getIntegerOp() { return integerOp; }

		public ToDoubleBiFunction<Expression<?>, Expression<?>> getFloatingPointOp() {
			if(floatingPointOp==null)
				throw forUnsupportedFloatingPoint(name());
			return floatingPointOp;
		}
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
		NOT_EQUALS((l, r) -> l.computeAsLong()!=r.computeAsLong(),
				(l, r) -> Double.compare(l.computeAsDouble(), r.computeAsDouble())!=0),
		;

		private final BiPredicate<Expression<?>, Expression<?>> integerPred;
		private final BiPredicate<Expression<?>, Expression<?>> floatingPointPred;

		private NumericalComparator(BiPredicate<Expression<?>, Expression<?>> integerPred,
				BiPredicate<Expression<?>, Expression<?>> floatingPointPred) {
			this.integerPred = integerPred;
			this.floatingPointPred = floatingPointPred;
		}

		public BiPredicate<Expression<?>, Expression<?>> getIntegerPred() { return integerPred; }

		public BiPredicate<Expression<?>, Expression<?>> getFloatingPointPred() { return floatingPointPred; }
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

	public enum StringOp {
		EQUALS(null),
		CONTAINS(null),
		MATCHES(null),

		LESS(v -> v<0),
		GREATER(v -> v>0),
		LESS_OR_EQUAL(v -> v<=0),
		GREATER_OR_EQUAL(v -> v>=0),
		;

		private final IntPredicate comp;

		private StringOp(IntPredicate comp) { this.comp = comp; }

		public IntPredicate getComp() {
			if(comp==null)
				throw forUnsupportedTextComp(name());
			return comp;
		}
	}

	public enum StringMode {
		DEFAULT((cp1, cp2) -> cp1==cp2, (c1, c2) -> c1==c2),
		IGNORE_CASE((cp1, cp2) -> cp1==cp2 || toLowerCase(cp1)==toLowerCase(cp2)
					|| toUpperCase(cp1)==toUpperCase(cp2),
				(c1, c2) -> c1==c2 || toLowerCase(c1)==toLowerCase(c2)
					|| toUpperCase(c1)==toUpperCase(c2)),
		;

		private final IntBiPredicate codePointComparator;
		private final CharBiPredicate charComparator;

		private StringMode(IntBiPredicate codePointComparator, CharBiPredicate charComparator) {
			this.codePointComparator = requireNonNull(codePointComparator);
			this.charComparator = requireNonNull(charComparator);
		}

		public IntBiPredicate getCodePointComparator() { return codePointComparator; }

		public CharBiPredicate getCharComparator() { return charComparator; }
	}
}
