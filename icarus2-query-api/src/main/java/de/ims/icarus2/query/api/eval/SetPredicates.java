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

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.eval.Expression.BooleanExpression;
import de.ims.icarus2.query.api.eval.Expression.ListExpression;
import de.ims.icarus2.query.api.eval.Expression.TextExpression;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.MutablePrimitives.Primitive;
import de.ims.icarus2.util.strings.StringUtil;
import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
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
		Mode mode = Mode.SINGLE;
		TypeInfo queryType = query.getResultType();
		if(queryType.isList()) {
			mode = Mode.EXPAND;
			queryType = ((ListExpression<?, ?>)query).getElementType();
		}

		if(TypeInfo.isNumerical(queryType)) {
			if(TypeInfo.isFloatingPoint(queryType)) {
				return FloatingPointSetPredicate.of(mode, query, set);
			}
			return IntegerSetPredicate.of(mode, query, set);
		} else if(query.isText()) {
			return new TextSetPredicate((TextExpression) query, EvaluationUtils.ensureText(set));
		}
		//TODO
		throw new UnsupportedOperationException();
	}


	// expects 'query' to be a list expression
	public static BooleanExpression allIn(ListExpression<?, ?> query, Expression<?>...set) {
		//TODO
		throw new UnsupportedOperationException();
	}

	private enum Mode {
		SINGLE,
		EXPAND,
		EXPAND_EXHAUSTIVE,
		;
	}

	/**
	 * Simple set predicate implementation for {@link TypeInfo#INTEGER integer} values
	 * that uses a single level of optimization, i.e. distinction between fixed and
	 * dynamic expressions in the {@code elements} array. Fixed expressions are pre-computed
	 * and stored in a set for quick evaluation. Only dynamic expressions need to still be
	 * traversed linearly in order to do the set containment check.
	 * <p>
	 * This implementation automatically expands list type expressions when they are
	 * part of the {@code set} part.
	 *
	 * @author Markus Gärtner
	 *
	 */
	static final class IntegerSetPredicate implements BooleanExpression {

		static IntegerSetPredicate of(Mode mode, Expression<?> target, Expression<?>[] elements) {
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

			return new IntegerSetPredicate(mode, target, fixedLongs,
					dynamicElements.toArray(new NumericalExpression[0]),
					dynamicLists.toArray(new IntegerListExpression[0]));
		}

		/** Pre-compiled (expanded) fixed values */
		private final LongSet fixedLongs;
		/** Dynamic expressions producing integer list values */
		private final IntegerListExpression<?>[] dynamicLists;
		/** Single-value dynamic expressions */
		private final NumericalExpression[] dynamicElements;
		/** Buffer for result value when using the wrapper {@link #compute()} method. */
		private final MutableBoolean value;
		/** Query value to match against */
		private final NumericalExpression target;
		/** Query value to match against if mode is expanding */
		private final IntegerListExpression<?> listTarget;
		/** Indicator how to evaluate the predicate */
		private final Mode mode;

		private IntegerSetPredicate(Mode mode, Expression<?> target, LongSet fixedLongs,
				NumericalExpression[] dynamicElements, IntegerListExpression<?>[] dynamicLists) {
			requireNonNull(mode);
			requireNonNull(target);

			this.mode = mode;
			switch (mode) {
			case SINGLE:
				this.target = (NumericalExpression) target;
				listTarget = null;
				break;
			case EXPAND:
			case EXPAND_EXHAUSTIVE:
				this.target = null;
				listTarget = (IntegerListExpression<?>)target;
				break;

			default:
				throw forUnknownMode(mode);
			}

			this.fixedLongs = requireNonNull(fixedLongs);
			this.dynamicLists = requireNonNull(dynamicLists);
			this.dynamicElements = requireNonNull(dynamicElements);
			value = new MutableBoolean();
		}

		private QueryException forUnknownMode(Mode mode) {
			return new QueryException(GlobalErrorCode.INTERNAL_ERROR, "Unknown mode: "+mode);
		}

		@VisibleForTesting
		LongSet getFixedLongs() { return fixedLongs; }

		@VisibleForTesting
		NumericalExpression[] getDynamicElements() { return dynamicElements; }

		@VisibleForTesting
		IntegerListExpression<?>[] getDynamicLists() { return dynamicLists; }

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

		private static boolean containsDynamicExpanded(long value, IntegerListExpression<?>[] elements) {
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

		private static boolean containsAll(IntegerListExpression<?> target, LongSet fixedLongs,
				NumericalExpression[] dynamicElements, IntegerListExpression<?>[] dynamicLists) {
			int size = target.size();
			for (int i = 0; i < size; i++) {
				long value = target.getAsLong(i);
				if(!containsSingle(value, fixedLongs, dynamicElements, dynamicLists)) {
					return false;
				}
			}
			return true;
		}

		private static boolean containsAny(IntegerListExpression<?> target, LongSet fixedLongs,
				NumericalExpression[] dynamicElements, IntegerListExpression<?>[] dynamicLists) {
			int size = target.size();
			for (int i = 0; i < size; i++) {
				long value = target.getAsLong(i);
				if(containsSingle(value, fixedLongs, dynamicElements, dynamicLists)) {
					return true;
				}
			}
			return false;
		}

		private static boolean containsSingle(long value, LongSet fixedLongs,
				NumericalExpression[] dynamicElements, IntegerListExpression<?>[] dynamicLists) {
			return (!fixedLongs.isEmpty() && fixedLongs.contains(value))
					|| (dynamicElements.length>0 && containsDynamic(value, dynamicElements))
					|| (dynamicLists.length>0 && containsDynamicExpanded(value, dynamicLists));
		}

		@Override
		public boolean computeAsBoolean() {
			switch (mode) {
			case SINGLE: return containsSingle(target.computeAsLong(), fixedLongs,
					dynamicElements, dynamicLists);

			case EXPAND: return containsAny(listTarget, fixedLongs, dynamicElements, dynamicLists);
			case EXPAND_EXHAUSTIVE: return containsAll(listTarget, fixedLongs, dynamicElements, dynamicLists);

			default:
				throw forUnknownMode(mode);
			}
		}

		@Override
		public Expression<Primitive<Boolean>> duplicate(EvaluationContext context) {
			return new IntegerSetPredicate(
					mode,
					target==null ? listTarget.duplicate(context) : target.duplicate(context),
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
			Expression<?> oldTarget = target==null ? listTarget : target;
			Expression<?> newTarget = oldTarget.optimize(context);

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

			// Special case of full constant expression -> optimize into single boolean
			if(newTarget.isConstant() && dynamicElements.length==0 && dynamicLists.length==0) {
				switch (mode) {
				case SINGLE: return Literals.of(containsSingle(
						((NumericalExpression)newTarget).computeAsLong(), fixedLongs,
						dynamicElements, dynamicLists));

				case EXPAND: return Literals.of(containsAny((IntegerListExpression<?>)newTarget,
						fixedLongs, dynamicElements, dynamicLists));
				case EXPAND_EXHAUSTIVE: return Literals.of(
						containsAll((IntegerListExpression<?>)newTarget,
								fixedLongs, dynamicElements, dynamicLists));

				default:
					throw forUnknownMode(mode);
				}
			} else if(dynamicElements.length < this.dynamicElements.length
					|| dynamicLists.length < this.dynamicLists.length) {
				return new IntegerSetPredicate(mode, newTarget, fixedLongs, dynamicElements, dynamicLists);
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
	 * This implementation automatically expands list type expressions when they are
	 * part of the {@code set} part.
	 *
	 * @author Markus Gärtner
	 *
	 */
	static final class FloatingPointSetPredicate implements BooleanExpression {
		static FloatingPointSetPredicate of(Mode mode, Expression<?> target, Expression<?>[] elements) {
			checkArgument("Need at least 1 element to check set containment", elements.length>0);

			DoubleSet fixedDoubles = new DoubleOpenHashSet();
			List<FloatingPointListExpression<?>> dynamicLists = new ArrayList<>();
			List<NumericalExpression> dynamicElements = new ArrayList<>();

			for (Expression<?> element : elements) {
				if(element.isNumerical()) {
					NumericalExpression ne = (NumericalExpression)element;
					if(!ne.isFPE())
						throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
								"Not a floating point type: "+ne.getResultType());

					if(ne.isConstant()) {
						fixedDoubles.add(ne.computeAsDouble());
					} else {
						dynamicElements.add(ne);
					}
				} else if(element.isList()) {
					TypeInfo elementType = ((ListExpression<?, ?>)element).getElementType();
					if(!TypeInfo.isNumerical(elementType))
						throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
								"Not an numerical list type: "+elementType);
					if(!TypeInfo.isFloatingPoint(elementType))
						throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
								"Not a floating point type: "+elementType);

					FloatingPointListExpression<?> ie = (FloatingPointListExpression<?>) element;
					if(ie.isConstant()) {
						ie.forEachFloatingPoint(fixedDoubles::add);
					} else {
						dynamicLists.add(ie);
					}
				}
			}

			return new FloatingPointSetPredicate(mode, target, fixedDoubles,
					dynamicElements.toArray(new NumericalExpression[0]),
					dynamicLists.toArray(new FloatingPointListExpression[0]));
		}

		/** Pre-compiled (expanded) fixed values */
		private final DoubleSet fixedDoubles;
		/** Dynamic expressions producing integer list values */
		private final FloatingPointListExpression<?>[] dynamicLists;
		/** Single-value dynamic expressions */
		private final NumericalExpression[] dynamicElements;
		/** Buffer for result value when using the wrapper {@link #compute()} method. */
		private final MutableBoolean value;
		/** Query value to match against */
		private final NumericalExpression target;
		/** Query value to match against if mode is expanding */
		private final FloatingPointListExpression<?> listTarget;
		/** Indicator how to evaluate the predicate */
		private final Mode mode;

		private FloatingPointSetPredicate(Mode mode, Expression<?> target, DoubleSet fixedDoubles,
				NumericalExpression[] dynamicElements, FloatingPointListExpression<?>[] dynamicLists) {
			requireNonNull(mode);
			requireNonNull(target);

			this.mode = mode;
			switch (mode) {
			case SINGLE:
				this.target = (NumericalExpression) target;
				listTarget = null;
				break;
			case EXPAND:
			case EXPAND_EXHAUSTIVE:
				this.target = null;
				listTarget = (FloatingPointListExpression<?>)target;
				break;

			default:
				throw forUnknownMode(mode);
			}

			this.fixedDoubles = requireNonNull(fixedDoubles);
			this.dynamicLists = requireNonNull(dynamicLists);
			this.dynamicElements = requireNonNull(dynamicElements);
			value = new MutableBoolean();
		}

		private QueryException forUnknownMode(Mode mode) {
			return new QueryException(GlobalErrorCode.INTERNAL_ERROR, "Unknown mode: "+mode);
		}

		@VisibleForTesting
		DoubleSet getFixedDoubles() { return fixedDoubles; }

		@VisibleForTesting
		NumericalExpression[] getDynamicElements() { return dynamicElements; }

		@VisibleForTesting
		FloatingPointListExpression<?>[] getDynamicLists() { return dynamicLists; }

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

		private static boolean containsDynamicExpanded(double value, FloatingPointListExpression<?>[] elements) {
			for (int i = 0; i < elements.length; i++) {
				ListProxy.OfFloatingPoint iList = elements[i];
				for (int j = 0; j < iList.size(); j++) {
					if(value==iList.getAsDouble(j)) {
						return true;
					}
				}
			}
			return false;
		}

		private static boolean containsAll(FloatingPointListExpression<?> target, DoubleSet fixedDoubles,
				NumericalExpression[] dynamicElements, FloatingPointListExpression<?>[] dynamicLists) {
			int size = target.size();
			for (int i = 0; i < size; i++) {
				double value = target.getAsDouble(i);
				if(!containsSingle(value, fixedDoubles, dynamicElements, dynamicLists)) {
					return false;
				}
			}
			return true;
		}

		private static boolean containsAny(FloatingPointListExpression<?> target, DoubleSet fixedDoubles,
				NumericalExpression[] dynamicElements, FloatingPointListExpression<?>[] dynamicLists) {
			int size = target.size();
			for (int i = 0; i < size; i++) {
				double value = target.getAsDouble(i);
				if(containsSingle(value, fixedDoubles, dynamicElements, dynamicLists)) {
					return true;
				}
			}
			return false;
		}

		private static boolean containsSingle(double value, DoubleSet fixedDoubles,
				NumericalExpression[] dynamicElements, FloatingPointListExpression<?>[] dynamicLists) {
			return (!fixedDoubles.isEmpty() && fixedDoubles.contains(value))
					|| (dynamicElements.length>0 && containsDynamic(value, dynamicElements))
					|| (dynamicLists.length>0 && containsDynamicExpanded(value, dynamicLists));
		}

		@Override
		public boolean computeAsBoolean() {
			switch (mode) {
			case SINGLE: return containsSingle(target.computeAsDouble(), fixedDoubles,
					dynamicElements, dynamicLists);

			case EXPAND: return containsAny(listTarget, fixedDoubles, dynamicElements, dynamicLists);
			case EXPAND_EXHAUSTIVE: return containsAll(listTarget, fixedDoubles, dynamicElements, dynamicLists);

			default:
				throw forUnknownMode(mode);
			}
		}

		@Override
		public Expression<Primitive<Boolean>> duplicate(EvaluationContext context) {
			return new FloatingPointSetPredicate(
					mode,
					target==null ? listTarget.duplicate(context) : target.duplicate(context),
					new DoubleOpenHashSet(fixedDoubles),
					Stream.of(dynamicElements)
						.map(expression -> expression.duplicate(context))
						.map(NumericalExpression.class::cast)
						.toArray(NumericalExpression[]::new),
					Stream.of(dynamicLists)
						.map(expression -> expression.duplicate(context))
						.map(FloatingPointListExpression.class::cast)
						.toArray(FloatingPointListExpression[]::new));
		}

		@Override
		public Expression<Primitive<Boolean>> optimize(EvaluationContext context) {
			Expression<?> oldTarget = target==null ? listTarget : target;
			Expression<?> newTarget = oldTarget.optimize(context);

			/*
			 * If we have at least 1 constant in the current set of dynamic elements,
			 * we can shift that over to the fixed set.
			 */
			DoubleSet fixedDoubles = new DoubleOpenHashSet(this.fixedDoubles);
			NumericalExpression[] dynamicElements = Stream.of(this.dynamicElements)
					.map(expression -> expression.optimize(context))
					.map(NumericalExpression.class::cast)
					.filter(ne -> {
						if(ne.isConstant()) {
							fixedDoubles.add(ne.computeAsDouble());
							return false;
						}
						return true;
					})
					.toArray(NumericalExpression[]::new);
			FloatingPointListExpression<?>[] dynamicLists = Stream.of(this.dynamicLists)
					.map(expression -> expression.optimize(context))
					.map(FloatingPointListExpression.class::cast)
					.filter(ile -> {
						if(ile.isConstant()) {
							ile.forEachFloatingPoint(fixedDoubles::add);
							return false;
						}
						return true;
					})
					.toArray(FloatingPointListExpression[]::new);

			// Special case of full constant expression -> optimize into single boolean
			if(newTarget.isConstant() && dynamicElements.length==0 && dynamicLists.length==0) {
				switch (mode) {
				case SINGLE: return Literals.of(containsSingle(
						((NumericalExpression)newTarget).computeAsDouble(), fixedDoubles,
						dynamicElements, dynamicLists));

				case EXPAND: return Literals.of(containsAny((FloatingPointListExpression<?>)newTarget,
						fixedDoubles, dynamicElements, dynamicLists));
				case EXPAND_EXHAUSTIVE: return Literals.of(
						containsAll((FloatingPointListExpression<?>)newTarget,
								fixedDoubles, dynamicElements, dynamicLists));

				default:
					throw forUnknownMode(mode);
				}
			} else if(dynamicElements.length < this.dynamicElements.length
					|| dynamicLists.length < this.dynamicLists.length) {
				return new FloatingPointSetPredicate(mode, newTarget, fixedDoubles, dynamicElements, dynamicLists);
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
	static final class TextSetPredicate implements BooleanExpression {
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

		TextSetPredicate(TextExpression target, TextExpression[] elements) {
			requireNonNull(elements);
			this.target = requireNonNull(target);
			checkArgument("Need at least 1 element to check set containment", elements.length>0);

			fixedElements = new ObjectOpenCustomHashSet<>(STRATEGY);
			dynamicElements = filterConstant(Stream.of(elements), fixedElements);

			value = new MutableBoolean();
		}

		/** Copy constructor */
		private TextSetPredicate(TextExpression target, Set<CharSequence> fixedElements,
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
			return new TextSetPredicate(
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

				return new TextSetPredicate(newTarget, fixedElements, dynamicElements);
			}

			return this;
		}
	}
}
