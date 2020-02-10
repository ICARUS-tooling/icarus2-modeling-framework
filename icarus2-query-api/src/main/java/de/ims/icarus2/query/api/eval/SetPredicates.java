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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.annotations.VisibleForTesting;

import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.eval.Expression.BooleanExpression;
import de.ims.icarus2.query.api.eval.Expression.NumericalExpression;
import de.ims.icarus2.query.api.eval.Expression.TextExpression;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.MutablePrimitives.Primitive;
import de.ims.icarus2.util.strings.StringUtil;
import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;

/**
 * @author Markus Gärtner
 *
 */
public class SetPredicates {

	/*
	 * IQL grammar for set predicate:
	 * source=expression not? IN all? LBRACE set=expressionList RBRACE
	 *
	 * Special constellations:
	 * 'not' alone -> negate before return or wrap into unary negation expression
	 * 'all' alone -> ensure all target elements are contained
	 * 'not' + 'all' -> fail as soon as a single target element is contained
	 *  (last option might also be better wrapped instead of making the implementation overhead)
	 */

	public static BooleanExpression in(Expression<?> query, Expression<?>...set) {
		TypeInfo queryType = query.getResultType();
		if(queryType.isList()) {
			//TODO delegate to expanding implementations
		} else if(query.isNumerical()) {
			NumericalExpression target = (NumericalExpression)query;
			if(target.isFPE()) {
				return new FlatFloatingPointSetPredicate(target, EvaluationUtils.ensureFloatingPoint(set));
			}
			return FlatIntegerSetPredicate.of(target, set);
		} else if(query.isText()) {
			return new FlatTextSetPredicate((TextExpression) query, EvaluationUtils.ensureText(set));
		}
		//TODO
		throw new UnsupportedOperationException();
	}


	// expects 'query' to be a list expression
	public static BooleanExpression allIn(Expression<?> query, Expression<?>...set) {
		//TODO
		throw new UnsupportedOperationException();
	}

	/**
	 * Simple set predicate implementation for {@link TypeInfo#INTEGER integer} values
	 * that uses a single level of optimization, i.e. distinction between fixed and
	 * dynamic expressions in the {@code elements} array. Fixed expressions are pre-computed
	 * and stored in a set for quick evaluation. Only dynamic expressions need to still be
	 * traversed linearly in order to do the set containment check.
	 * <p>
	 * This implementation does not support value expansion!
	 *
	 * @author Markus Gärtner
	 *
	 */
	static final class FlatIntegerSetPredicate implements BooleanExpression {

		static FlatIntegerSetPredicate of(NumericalExpression target, Expression<?>[] elements) {
			checkArgument("Need at least 1 element to check set containment", elements.length>0);

			LongSet fixedLongs = new LongOpenHashSet();
			List<IntegerListExpression<?>> dynamicLists = new ArrayList<>();
			List<NumericalExpression> dynamicElements = new ArrayList<>();

			for (Expression<?> element : elements) {
				if(element.isNumerical()) {
					NumericalExpression ne = (NumericalExpression)element;
					if(ne.isFPE())
						throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
								"Not an integer type: "+ne.getResultType());

					if(ne.isConstant()) {
						fixedLongs.add(ne.computeAsLong());
					} else {
						dynamicElements.add(ne);
					}
				} else if(element.isList()) {
					TypeInfo elementType = ((ListExpression<?, ?>)element).getElementType();
					if(!TypeInfo.isNumerical(elementType))
						throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
								"Not an numerical list type: "+elementType);
					if(TypeInfo.isFloatingPoint(elementType))
						throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
								"Not an integer type: "+elementType);

					IntegerListExpression<?> ie = (IntegerListExpression<?>) element;
					if(ie.isConstant()) {
						ie.forEachInteger(fixedLongs::add);
					} else {
						dynamicLists.add(ie);
					}
				}
			}

			return new FlatIntegerSetPredicate(target, fixedLongs,
					dynamicElements.toArray(new NumericalExpression[0]),
					dynamicLists.toArray(new IntegerListExpression[0]));
		}

		/** Pre-compiled (expanded) fixed values */
		private final LongSet fixedLongs;
		/** Bitmask computed by bitwise 'or' of all the fixed elements */
		private long quickFilter = 0L;
		/** Dynamic expressions producing integer list values */
		private final IntegerListExpression<?>[] dynamicLists;
		/** Single-value dynamic expressions */
		private final NumericalExpression[] dynamicElements;
		/** Buffer for result value when using the wrapper {@link #compute()} method. */
		private final MutableBoolean value;
		/** Query value to match against */
		private final NumericalExpression target;

		private FlatIntegerSetPredicate(NumericalExpression target, LongSet fixedLongs,
				NumericalExpression[] dynamicElements, IntegerListExpression<?>[] dynamicLists) {
			this.target = requireNonNull(target);
			this.fixedLongs = requireNonNull(fixedLongs);
			this.dynamicLists = requireNonNull(dynamicLists);
			this.dynamicElements = requireNonNull(dynamicElements);
			quickFilter = createQuickFilter(fixedLongs);
			value = new MutableBoolean();
		}

		@VisibleForTesting
		LongSet getFixedLongs() { return fixedLongs; }

		@VisibleForTesting
		NumericalExpression[] getDynamicElements() { return dynamicElements; }

		@VisibleForTesting
		IntegerListExpression<?>[] getDynamicLists() { return dynamicLists; }

		private static long createQuickFilter(LongSet set) {
			long filter = 0L;
			for(LongIterator it = set.iterator(); it.hasNext();) {
				filter |= it.nextLong();
			}
			return filter;
		}

		@Override
		public Primitive<Boolean> compute() {
			value.setBoolean(computeAsBoolean());
			return value;
		}

		private static boolean containsDynamic(long value, NumericalExpression[] elements) {
			for (int i = 0; i < elements.length; i++) {
				if(elements[i].computeAsLong()==value) {
					return true;
				}
			}
			return false;
		}

		private static boolean containsDynamic(long value, IntegerListExpression<?>[] elements) {
			for (int i = 0; i < elements.length; i++) {
				ListProxy.OfInteger iList = elements[i];
				for (int j = 0; j < iList.size(); j++) {
					if(value==iList.getAsLong(j)) {
						return true;
					}
				}
			}
			return false;
		}

		private boolean containsStatic(long value) {
			return (quickFilter & value)==value && fixedLongs.contains(value);
		}

		@Override
		public boolean computeAsBoolean() {
			long value = target.computeAsLong();
			return (!fixedLongs.isEmpty() && containsStatic(value))
					|| (dynamicElements.length>0 && containsDynamic(value, dynamicElements))
					|| (dynamicLists.length>0 && containsDynamic(value, dynamicLists));
		}

		@Override
		public Expression<Primitive<Boolean>> duplicate(EvaluationContext context) {
			return new FlatIntegerSetPredicate(
					(NumericalExpression)target.duplicate(context),
					new LongOpenHashSet(fixedLongs),
					Stream.of(dynamicElements)
						.map(expression -> expression.duplicate(context))
						.map(NumericalExpression.class::cast)
						.toArray(NumericalExpression[]::new),
					Stream.of(dynamicLists)
						.map(expression -> expression.duplicate(context))
						.map(IntegerListExpression.class::cast)
						.toArray(IntegerListExpression[]::new));
		}

		@Override
		public Expression<Primitive<Boolean>> optimize(EvaluationContext context) {

			// Main optimization comes from the target expression
			NumericalExpression newTarget = (NumericalExpression) target.optimize(context);
			// If we don't have any dynamic part going on, optimize to constant directly
			if(newTarget.isConstant() && dynamicElements.length==0
					&& dynamicLists.length==0) {
				return Literals.of(fixedLongs.contains(newTarget.computeAsLong()));
			}

			/*
			 * If we have at least 1 constant in the current set of dynamic elements,
			 * we can shift that over to the fixed set.
			 */
			LongSet fixedLongs = new LongOpenHashSet(this.fixedLongs);
			NumericalExpression[] dynamicElements = Stream.of(this.dynamicElements)
					.map(expression -> expression.optimize(context))
					.map(NumericalExpression.class::cast)
					.filter(ne -> {
						if(ne.isConstant()) {
							fixedLongs.add(ne.computeAsLong());
							return false;
						}
						return true;
					})
					.toArray(NumericalExpression[]::new);
			IntegerListExpression<?>[] dynamicLists = Stream.of(this.dynamicLists)
					.map(expression -> expression.optimize(context))
					.map(IntegerListExpression.class::cast)
					.filter(ile -> {
						if(ile.isConstant()) {
							ile.forEachInteger(fixedLongs::add);
							return false;
						}
						return true;
					})
					.toArray(IntegerListExpression[]::new);

			if(dynamicElements.length < this.dynamicElements.length
					|| dynamicLists.length < this.dynamicLists.length) {

				// Special case of full constant expression -> optimize into single boolean
				if(newTarget.isConstant() && dynamicElements.length==0 && dynamicLists.length==0) {
					return Literals.of(fixedLongs.contains(newTarget.computeAsLong()));
				}

				return new FlatIntegerSetPredicate(newTarget, fixedLongs, dynamicElements, dynamicLists);
			}

			return this;
		}
	}

	/**
	 * Simple set predicate implementation for {@link TypeInfo#FLOATING_POINT integer} values
	 * that uses a single level of optimization, i.e. distinction between fixed and
	 * dynamic expressions in the {@code elements} array. Fixed expressions are pre-computed
	 * and stored in a set for quick evaluation. Only dynamic expressions need to still be
	 * traversed linearly in order to do the set containment check.
	 * <p>
	 * This implementation does not support value expansion!
	 *
	 * @author Markus Gärtner
	 *
	 */
	static final class FlatFloatingPointSetPredicate implements BooleanExpression {
		private final DoubleSet fixedDoubles;
		private final NumericalExpression[] dynamicElements;
		private final MutableBoolean value;

		private final NumericalExpression target;

		FlatFloatingPointSetPredicate(NumericalExpression target, NumericalExpression[] elements) {
			requireNonNull(elements);
			this.target = requireNonNull(target);
			checkArgument("Need at least 1 element to check set containment", elements.length>0);

			fixedDoubles = new DoubleOpenHashSet();
			dynamicElements = filterConstant(Stream.of(elements), fixedDoubles);

			value = new MutableBoolean();
		}

		/** Copy constructor */
		private FlatFloatingPointSetPredicate(NumericalExpression target, DoubleSet fixedDoubles,
				NumericalExpression[] dynamicElements) {
			this.target = requireNonNull(target);
			this.fixedDoubles = requireNonNull(fixedDoubles);
			this.dynamicElements = requireNonNull(dynamicElements);
			value = new MutableBoolean();
		}

		@VisibleForTesting
		DoubleSet getFixedDoubles() {
			return fixedDoubles;
		}

		@VisibleForTesting
		NumericalExpression[] getDynamicElements() {
			return dynamicElements;
		}

		private static NumericalExpression[] filterConstant(Stream<NumericalExpression> elements,
				DoubleSet fixedDoubles) {
			return elements
					// Collect all the constant entries directly into the fixed set for lookup
					.filter(element -> {
						boolean constant = element.isConstant();
						if(constant) {
							fixedDoubles.add(element.computeAsDouble());
						}
						return !constant;
					})
					// The dynamic elements stay as they are
					.toArray(NumericalExpression[]::new);
		}

		@Override
		public Primitive<Boolean> compute() {
			value.setBoolean(computeAsBoolean());
			return value;
		}

		private static boolean containsDynamic(double value, NumericalExpression[] elements) {
			for (int i = 0; i < elements.length; i++) {
				if(elements[i].computeAsDouble()==value) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean computeAsBoolean() {
			double value = target.computeAsDouble();
			return (!fixedDoubles.isEmpty() && fixedDoubles.contains(value))
					|| (dynamicElements.length>0 && containsDynamic(value, dynamicElements));
		}

		@Override
		public Expression<Primitive<Boolean>> duplicate(EvaluationContext context) {
			return new FlatFloatingPointSetPredicate(
					(NumericalExpression)target.duplicate(context),
					new DoubleOpenHashSet(fixedDoubles),
					Stream.of(dynamicElements)
						.map(expression -> expression.duplicate(context))
						.toArray(NumericalExpression[]::new));
		}

		@Override
		public Expression<Primitive<Boolean>> optimize(EvaluationContext context) {
			// Main optimization comes from the target expression
			NumericalExpression newTarget = (NumericalExpression) target.optimize(context);
			// If we don't have any dynamic part going on, optimize to constant directly
			if(newTarget.isConstant() && dynamicElements.length==0) {
				return Literals.of(fixedDoubles.contains(newTarget.computeAsDouble()));
			}

			/*
			 * If we have at least 1 constant in the current set of dynamic elements,
			 * we can shift that over to the fixed set.
			 */
			DoubleSet fixedElements = new DoubleOpenHashSet(this.fixedDoubles);
			NumericalExpression[] dynamicElements = filterConstant(
					Stream.of(this.dynamicElements)
						.map(expression -> expression.optimize(context))
						.map(NumericalExpression.class::cast),
					fixedElements);

			if(dynamicElements.length<this.dynamicElements.length) {

				// Special case of full constant expression -> optimize into single boolean
				if(newTarget.isConstant() && dynamicElements.length==0) {
					return Literals.of(fixedElements.contains(newTarget.computeAsDouble()));
				}

				return new FlatFloatingPointSetPredicate(newTarget, fixedElements, dynamicElements);
			}

			return this;
		}
	}

	/**
	 * Simple set predicate implementation for {@link TypeInfo#TEXT integer} values
	 * that uses a single level of optimization, i.e. distinction between fixed and
	 * dynamic expressions in the {@code elements} array. Fixed expressions are pre-computed
	 * and stored in a set for quick evaluation. Only dynamic expressions need to still be
	 * traversed linearly in order to do the set containment check.
	 * <p>
	 * This implementation does not support value expansion!
	 *
	 * @author Markus Gärtner
	 *
	 */
	static final class FlatTextSetPredicate implements BooleanExpression {
		private final Set<CharSequence> fixedElements;
		private final TextExpression[] dynamicElements;
		private final MutableBoolean value;

		private final TextExpression target;

		private static final Strategy<CharSequence> STRATEGY = new Strategy<CharSequence>() {

			@Override
			public int hashCode(CharSequence o) { return StringUtil.hash(o); }

			@Override
			public boolean equals(CharSequence a, CharSequence b) {
				return StringUtil.equals(a, b);
			}
		};

		FlatTextSetPredicate(TextExpression target, TextExpression[] elements) {
			requireNonNull(elements);
			this.target = requireNonNull(target);
			checkArgument("Need at least 1 element to check set containment", elements.length>0);

			fixedElements = new ObjectOpenCustomHashSet<>(STRATEGY);
			dynamicElements = filterConstant(Stream.of(elements), fixedElements);

			value = new MutableBoolean();
		}

		/** Copy constructor */
		private FlatTextSetPredicate(TextExpression target, Set<CharSequence> fixedElements,
				TextExpression[] dynamicElements) {
			this.target = requireNonNull(target);
			this.fixedElements = requireNonNull(fixedElements);
			this.dynamicElements = requireNonNull(dynamicElements);
			value = new MutableBoolean();
		}

		@VisibleForTesting
		Set<CharSequence> getFixedElements() {
			return fixedElements;
		}

		@VisibleForTesting
		TextExpression[] getDynamicElements() {
			return dynamicElements;
		}

		private static TextExpression[] filterConstant(Stream<TextExpression> elements,
				Set<CharSequence> fixedElements) {
			return elements
					// Collect all the constant entries directly into the fixed set for lookup
					.filter(element -> {
						boolean constant = element.isConstant();
						if(constant) {
							// Decouple char sequences from whatever storage they use
							fixedElements.add(element.compute().toString());
						}
						return !constant;
					})
					// The dynamic elements stay as they are
					.toArray(TextExpression[]::new);
		}

		@Override
		public Primitive<Boolean> compute() {
			value.setBoolean(computeAsBoolean());
			return value;
		}

		private static boolean containsDynamic(CharSequence value, TextExpression[] elements) {
			for (int i = 0; i < elements.length; i++) {
				if(StringUtil.equals(elements[i].compute(), value)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean computeAsBoolean() {
			CharSequence value = target.compute();
			return (!fixedElements.isEmpty() && fixedElements.contains(value))
					|| (dynamicElements.length>0 && containsDynamic(value, dynamicElements));
		}

		@Override
		public Expression<Primitive<Boolean>> duplicate(EvaluationContext context) {
			return new FlatTextSetPredicate(
					(TextExpression)target.duplicate(context),
					new ObjectOpenCustomHashSet<>(fixedElements, STRATEGY),
					Stream.of(dynamicElements)
						.map(expression -> expression.duplicate(context))
						.toArray(TextExpression[]::new));
		}

		@Override
		public Expression<Primitive<Boolean>> optimize(EvaluationContext context) {
			// Main optimization comes from the target expression
			TextExpression newTarget = (TextExpression) target.optimize(context);
			// If we don't have any dynamic part going on, optimize to constant directly
			if(newTarget.isConstant() && dynamicElements.length==0) {
				return Literals.of(fixedElements.contains(newTarget.compute()));
			}

			/*
			 * If we have at least 1 constant in the current set of dynamic elements,
			 * we can shift that over to the fixed set.
			 */
			Set<CharSequence> fixedElements = new ObjectOpenCustomHashSet<>(this.fixedElements, STRATEGY);
			TextExpression[] dynamicElements = filterConstant(
					Stream.of(this.dynamicElements)
						.map(expression -> expression.optimize(context))
						.map(TextExpression.class::cast),
					fixedElements);

			if(dynamicElements.length<this.dynamicElements.length) {

				// Special case of full constant expression -> optimize into single boolean
				if(newTarget.isConstant() && dynamicElements.length==0) {
					return Literals.of(fixedElements.contains(newTarget.compute()));
				}

				return new FlatTextSetPredicate(newTarget, fixedElements, dynamicElements);
			}

			return this;
		}
	}
}
