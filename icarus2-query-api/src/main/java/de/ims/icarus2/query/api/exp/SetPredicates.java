/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.query.api.exp.EvaluationUtils.castFloatingPointList;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.castIntegerList;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.castList;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.castText;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.castTextList;
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
import de.ims.icarus2.query.api.exp.Expression.ListExpression;
import de.ims.icarus2.query.api.exp.Expression.PrimitiveExpression;
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

	public static Expression<Primitive<Boolean>> in(Expression<?> query, Expression<?>...set) {
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
		} else if(TypeInfo.isText(queryType)) {
			return TextSetPredicate.of(mode, query, set);
		}

		throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
				"Unable to handle set predicate for type: "+queryType);
	}

	public static Expression<Primitive<Boolean>> allIn(ListExpression<?, ?> query, Expression<?>...set) {
		Mode mode = Mode.EXPAND_EXHAUSTIVE;
		TypeInfo queryType = query.getElementType();

		if(TypeInfo.isNumerical(queryType)) {
			if(TypeInfo.isFloatingPoint(queryType)) {
				return FloatingPointSetPredicate.of(mode, query, set);
			}
			return IntegerSetPredicate.of(mode, query, set);
		} else if(TypeInfo.isText(queryType)) {
			return TextSetPredicate.of(mode, query, set);
		}

		throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
				"Unable to handle [all in] set predicate for type: "+queryType);
	}

	public static Expression<Primitive<Boolean>> allNotIn(ListExpression<?, ?> query, Expression<?>...set) {
		Mode mode = Mode.EXPAND_EXHAUSTIVE_NEGATED;
		TypeInfo queryType = query.getElementType();

		if(TypeInfo.isNumerical(queryType)) {
			if(TypeInfo.isFloatingPoint(queryType)) {
				return FloatingPointSetPredicate.of(mode, query, set);
			}
			return IntegerSetPredicate.of(mode, query, set);
		} else if(TypeInfo.isText(queryType)) {
			return TextSetPredicate.of(mode, query, set);
		}

		throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
				"Unable to handle [all not in] set predicate for type: "+queryType);
	}

	private enum Mode {
		SINGLE,
		EXPAND,
		EXPAND_EXHAUSTIVE,
		EXPAND_EXHAUSTIVE_NEGATED,
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
	static final class IntegerSetPredicate implements Expression<Primitive<Boolean>>,
			PrimitiveExpression {

		static IntegerSetPredicate of(Mode mode, Expression<?> target, Expression<?>[] elements) {
			checkArgument("Need at least 1 element to check set containment", elements.length>0);

			LongSet fixedLongs = new LongOpenHashSet();
			List<IntegerListExpression<?>> dynamicLists = new ArrayList<>();
			List<Expression<?>> dynamicElements = new ArrayList<>();

			for (Expression<?> element : elements) {
				if(element.isInteger()) {
					if(element.isConstant()) {
						fixedLongs.add(element.computeAsLong());
					} else {
						dynamicElements.add(element);
					}
				} else if(element.isList()) {
					TypeInfo elementType = castList(element).getElementType();
					if(!TypeInfo.isInteger(elementType))
						throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
								"Not an integer list type: "+elementType);

					IntegerListExpression<?> ie = castIntegerList(element);
					if(ie.isConstant()) {
						ie.forEachInteger(fixedLongs::add);
					} else {
						dynamicLists.add(ie);
					}
				}
			}

			return new IntegerSetPredicate(mode, target, fixedLongs,
					dynamicElements.toArray(new Expression[0]),
					dynamicLists.toArray(new IntegerListExpression[0]));
		}

		/** Pre-compiled (expanded) fixed values */
		private final LongSet fixedLongs;
		/** Dynamic expressions producing integer list values */
		private final IntegerListExpression<?>[] dynamicLists;
		/** Single-value dynamic expressions */
		private final Expression<?>[] dynamicElements;
		/** Buffer for result value when using the wrapper {@link #compute()} method. */
		private final MutableBoolean value;
		/** Query value to match against */
		private final Expression<?> target;
		/** Query value to match against if mode is expanding */
		private final IntegerListExpression<?> listTarget;
		/** Indicator how to evaluate the predicate */
		private final Mode mode;

		private IntegerSetPredicate(Mode mode, Expression<?> target, LongSet fixedLongs,
				Expression<?>[] dynamicElements, IntegerListExpression<?>[] dynamicLists) {
			requireNonNull(mode);
			requireNonNull(target);

			this.mode = mode;
			switch (mode) {
			case SINGLE:
				this.target = target;
				listTarget = null;
				break;
			case EXPAND:
			case EXPAND_EXHAUSTIVE:
			case EXPAND_EXHAUSTIVE_NEGATED:
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

		@Override
		public TypeInfo getResultType() { return TypeInfo.BOOLEAN; }

		@VisibleForTesting
		LongSet getFixedLongs() { return fixedLongs; }

		@VisibleForTesting
		Expression<?>[] getDynamicElements() { return dynamicElements; }

		@VisibleForTesting
		IntegerListExpression<?>[] getDynamicLists() { return dynamicLists; }

		@Override
		public Primitive<Boolean> compute() {
			value.setBoolean(computeAsBoolean());
			return value;
		}

		private static boolean containsDynamic(long value, Expression<?>[] elements) {
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
				Expression<?>[] dynamicElements, IntegerListExpression<?>[] dynamicLists) {
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
				Expression<?>[] dynamicElements, IntegerListExpression<?>[] dynamicLists) {
			int size = target.size();
			for (int i = 0; i < size; i++) {
				long value = target.getAsLong(i);
				if(containsSingle(value, fixedLongs, dynamicElements, dynamicLists)) {
					return true;
				}
			}
			return false;
		}

		private static boolean containsNone(IntegerListExpression<?> target, LongSet fixedLongs,
				Expression<?>[] dynamicElements, IntegerListExpression<?>[] dynamicLists) {
			int size = target.size();
			for (int i = 0; i < size; i++) {
				long value = target.getAsLong(i);
				if(containsSingle(value, fixedLongs, dynamicElements, dynamicLists)) {
					return false;
				}
			}
			return true;
		}

		private static boolean containsSingle(long value, LongSet fixedLongs,
				Expression<?>[] dynamicElements, IntegerListExpression<?>[] dynamicLists) {
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
			case EXPAND_EXHAUSTIVE_NEGATED: return containsNone(listTarget, fixedLongs, dynamicElements, dynamicLists);

			default:
				throw forUnknownMode(mode);
			}
		}

		@Override
		public Expression<Primitive<Boolean>> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new IntegerSetPredicate(
					mode,
					target==null ? context.duplicate(listTarget) : context.duplicate(target),
					new LongOpenHashSet(fixedLongs),
					EvaluationUtils.duplicate(dynamicElements, context),
					EvaluationUtils.duplicate(dynamicLists, context, IntegerListExpression.class));
		}

		@Override
		public Expression<Primitive<Boolean>> optimize(EvaluationContext context) {
			requireNonNull(context);
			Expression<?> oldTarget = target==null ? listTarget : target;
			Expression<?> newTarget = oldTarget.optimize(context);

			/*
			 * If we have at least 1 constant in the current set of dynamic elements,
			 * we can shift that over to the fixed set.
			 */
			LongSet fixedLongs = new LongOpenHashSet(this.fixedLongs);
			Expression<?>[] dynamicElements = Stream.of(this.dynamicElements)
					.map(expression -> expression.optimize(context))
					.filter(ne -> {
						if(ne.isConstant()) {
							fixedLongs.add(ne.computeAsLong());
							return false;
						}
						return true;
					})
					.toArray(Expression[]::new);
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
						newTarget.computeAsLong(), fixedLongs,
						dynamicElements, dynamicLists));

				case EXPAND: return Literals.of(containsAny((IntegerListExpression<?>)newTarget,
						fixedLongs, dynamicElements, dynamicLists));
				case EXPAND_EXHAUSTIVE: return Literals.of(
						containsAll((IntegerListExpression<?>)newTarget,
								fixedLongs, dynamicElements, dynamicLists));
				case EXPAND_EXHAUSTIVE_NEGATED: return Literals.of(
						containsNone((IntegerListExpression<?>)newTarget,
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
	static final class FloatingPointSetPredicate implements Expression<Primitive<Boolean>>,
			PrimitiveExpression {

		static FloatingPointSetPredicate of(Mode mode, Expression<?> target, Expression<?>[] elements) {
			checkArgument("Need at least 1 element to check set containment", elements.length>0);

			DoubleSet fixedDoubles = new DoubleOpenHashSet();
			List<FloatingPointListExpression<?>> dynamicLists = new ArrayList<>();
			List<Expression<?>> dynamicElements = new ArrayList<>();

			for (Expression<?> element : elements) {
				if(element.isFloatingPoint()) {
					if(element.isConstant()) {
						fixedDoubles.add(element.computeAsDouble());
					} else {
						dynamicElements.add(element);
					}
				} else if(element.isList()) {
					TypeInfo elementType = castList(element).getElementType();
					if(!TypeInfo.isNumerical(elementType))
						throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
								"Not an numerical list type: "+elementType);
					if(!TypeInfo.isFloatingPoint(elementType))
						throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
								"Not a floating point type: "+elementType);

					FloatingPointListExpression<?> ie = castFloatingPointList(element);
					if(ie.isConstant()) {
						ie.forEachFloatingPoint(fixedDoubles::add);
					} else {
						dynamicLists.add(ie);
					}
				}
			}

			return new FloatingPointSetPredicate(mode, target, fixedDoubles,
					dynamicElements.toArray(new Expression[0]),
					dynamicLists.toArray(new FloatingPointListExpression[0]));
		}

		/** Pre-compiled (expanded) fixed values */
		private final DoubleSet fixedDoubles;
		/** Dynamic expressions producing integer list values */
		private final FloatingPointListExpression<?>[] dynamicLists;
		/** Single-value dynamic expressions */
		private final Expression<?>[] dynamicElements;
		/** Buffer for result value when using the wrapper {@link #compute()} method. */
		private final MutableBoolean value;
		/** Query value to match against */
		private final Expression<?> target;
		/** Query value to match against if mode is expanding */
		private final FloatingPointListExpression<?> listTarget;
		/** Indicator how to evaluate the predicate */
		private final Mode mode;

		private FloatingPointSetPredicate(Mode mode, Expression<?> target, DoubleSet fixedDoubles,
				Expression<?>[] dynamicElements, FloatingPointListExpression<?>[] dynamicLists) {
			requireNonNull(mode);
			requireNonNull(target);

			this.mode = mode;
			switch (mode) {
			case SINGLE:
				this.target = target;
				listTarget = null;
				break;
			case EXPAND:
			case EXPAND_EXHAUSTIVE:
			case EXPAND_EXHAUSTIVE_NEGATED:
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

		@Override
		public TypeInfo getResultType() { return TypeInfo.BOOLEAN; }

		@VisibleForTesting
		DoubleSet getFixedDoubles() { return fixedDoubles; }

		@VisibleForTesting
		Expression<?>[] getDynamicElements() { return dynamicElements; }

		@VisibleForTesting
		FloatingPointListExpression<?>[] getDynamicLists() { return dynamicLists; }

		@Override
		public Primitive<Boolean> compute() {
			value.setBoolean(computeAsBoolean());
			return value;
		}

		private static boolean containsDynamic(double value, Expression<?>[] elements) {
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
				Expression<?>[] dynamicElements, FloatingPointListExpression<?>[] dynamicLists) {
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
				Expression<?>[] dynamicElements, FloatingPointListExpression<?>[] dynamicLists) {
			int size = target.size();
			for (int i = 0; i < size; i++) {
				double value = target.getAsDouble(i);
				if(containsSingle(value, fixedDoubles, dynamicElements, dynamicLists)) {
					return true;
				}
			}
			return false;
		}

		private static boolean containsNone(FloatingPointListExpression<?> target, DoubleSet fixedDoubles,
				Expression<?>[] dynamicElements, FloatingPointListExpression<?>[] dynamicLists) {
			int size = target.size();
			for (int i = 0; i < size; i++) {
				double value = target.getAsDouble(i);
				if(containsSingle(value, fixedDoubles, dynamicElements, dynamicLists)) {
					return false;
				}
			}
			return true;
		}

		private static boolean containsSingle(double value, DoubleSet fixedDoubles,
				Expression<?>[] dynamicElements, FloatingPointListExpression<?>[] dynamicLists) {
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
			case EXPAND_EXHAUSTIVE_NEGATED: return containsNone(listTarget, fixedDoubles, dynamicElements, dynamicLists);

			default:
				throw forUnknownMode(mode);
			}
		}

		@Override
		public Expression<Primitive<Boolean>> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new FloatingPointSetPredicate(
					mode,
					target==null ? context.duplicate(listTarget) : context.duplicate(target),
					new DoubleOpenHashSet(fixedDoubles),
					EvaluationUtils.duplicate(dynamicElements, context),
					EvaluationUtils.duplicate(dynamicLists, context, FloatingPointListExpression.class));
		}

		@Override
		public Expression<Primitive<Boolean>> optimize(EvaluationContext context) {
			requireNonNull(context);
			Expression<?> oldTarget = target==null ? listTarget : target;
			Expression<?> newTarget = oldTarget.optimize(context);

			/*
			 * If we have at least 1 constant in the current set of dynamic elements,
			 * we can shift that over to the fixed set.
			 */
			DoubleSet fixedDoubles = new DoubleOpenHashSet(this.fixedDoubles);
			Expression<?>[] dynamicElements = Stream.of(this.dynamicElements)
					.map(expression -> expression.optimize(context))
					.filter(ne -> {
						if(ne.isConstant()) {
							fixedDoubles.add(ne.computeAsDouble());
							return false;
						}
						return true;
					})
					.toArray(Expression[]::new);
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
						newTarget.computeAsDouble(), fixedDoubles,
						dynamicElements, dynamicLists));

				case EXPAND: return Literals.of(containsAny((FloatingPointListExpression<?>)newTarget,
						fixedDoubles, dynamicElements, dynamicLists));
				case EXPAND_EXHAUSTIVE: return Literals.of(
						containsAll((FloatingPointListExpression<?>)newTarget,
								fixedDoubles, dynamicElements, dynamicLists));
				case EXPAND_EXHAUSTIVE_NEGATED: return Literals.of(
						containsNone((FloatingPointListExpression<?>)newTarget,
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
	static final class TextSetPredicate implements Expression<Primitive<Boolean>> {

		static TextSetPredicate of(Mode mode, Expression<?> target, Expression<?>[] elements) {
			checkArgument("Need at least 1 element to check set containment", elements.length>0);

			Set<CharSequence> fixedElements = new ObjectOpenCustomHashSet<>(STRATEGY);
			List<ListExpression<?, CharSequence>> dynamicLists = new ArrayList<>();
			List<Expression<CharSequence>> dynamicElements = new ArrayList<>();

			for (Expression<?> element : elements) {
				if(element.isText()) {
					Expression<CharSequence> te = castText(element);

					if(te.isConstant()) {
						fixedElements.add(te.compute());
					} else {
						dynamicElements.add(te);
					}
				} else if(element.isList()) {
					TypeInfo elementType = castList(element).getElementType();
					if(!TypeInfo.isText(elementType))
						throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
								"Not an text list type: "+elementType);

					ListExpression<?, CharSequence> ie = castTextList(element);
					if(ie.isConstant()) {
						ie.forEachItem(fixedElements::add);
					} else {
						dynamicLists.add(ie);
					}
				}
			}

			return new TextSetPredicate(mode, target, fixedElements,
					dynamicElements.toArray(new Expression[0]),
					dynamicLists.toArray(new ListExpression[0]));
		}

		private final Set<CharSequence> fixedElements;
		/** Dynamic expressions producing integer list values */
		private final ListExpression<?, CharSequence>[] dynamicLists;
		/** Single-value dynamic expressions */
		private final Expression<CharSequence>[] dynamicElements;
		/** Buffer for result value when using the wrapper {@link #compute()} method. */
		private final MutableBoolean value;
		/** Query value to match against */
		private final Expression<CharSequence> target;
		/** Query value to match against if mode is expanding */
		private final ListExpression<?, CharSequence> listTarget;
		/** Indicator how to evaluate the predicate */
		private final Mode mode;

		private static final Strategy<CharSequence> STRATEGY = new Strategy<CharSequence>() {

			@Override
			public int hashCode(CharSequence o) { return StringUtil.hash(o); }

			@Override
			public boolean equals(CharSequence a, CharSequence b) {
				return StringUtil.equals(a, b);
			}
		};

		private TextSetPredicate(Mode mode, Expression<?> target, Set<CharSequence> fixedElements,
				Expression<CharSequence>[] dynamicElements, ListExpression<?, CharSequence>[] dynamicLists) {
			requireNonNull(mode);
			requireNonNull(target);

			this.mode = mode;
			switch (mode) {
			case SINGLE:
				this.target = castText(target);
				listTarget = null;
				break;
			case EXPAND:
			case EXPAND_EXHAUSTIVE:
			case EXPAND_EXHAUSTIVE_NEGATED:
				this.target = null;
				listTarget = castTextList(target);
				break;

			default:
				throw forUnknownMode(mode);
			}

			this.fixedElements = requireNonNull(fixedElements);
			this.dynamicLists = requireNonNull(dynamicLists);
			this.dynamicElements = requireNonNull(dynamicElements);
			value = new MutableBoolean();
		}

		private QueryException forUnknownMode(Mode mode) {
			return new QueryException(GlobalErrorCode.INTERNAL_ERROR, "Unknown mode: "+mode);
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.BOOLEAN; }

		@VisibleForTesting
		Set<CharSequence> getFixedElements() { return fixedElements; }

		@VisibleForTesting
		Expression<CharSequence>[] getDynamicElements() { return dynamicElements; }

		@VisibleForTesting
		ListExpression<?, CharSequence>[] getDynamicLists() { return dynamicLists; }

		@Override
		public Primitive<Boolean> compute() {
			value.setBoolean(computeAsBoolean());
			return value;
		}

		private static boolean containsDynamic(CharSequence value, Expression<CharSequence>[] elements) {
			for (int i = 0; i < elements.length; i++) {
				if(StringUtil.equals(value, elements[i].compute())) {
					return true;
				}
			}
			return false;
		}

		private static boolean containsDynamicExpanded(CharSequence value, ListExpression<?, CharSequence>[] elements) {
			for (int i = 0; i < elements.length; i++) {
				ListProxy<CharSequence> iList = elements[i];
				for (int j = 0; j < iList.size(); j++) {
					if(StringUtil.equals(value, iList.get(j))) {
						return true;
					}
				}
			}
			return false;
		}

		private static boolean containsAll(ListExpression<?, CharSequence> target, Set<CharSequence> fixedElements,
				Expression<CharSequence>[] dynamicElements, ListExpression<?, CharSequence>[] dynamicLists) {
			int size = target.size();
			for (int i = 0; i < size; i++) {
				CharSequence value = target.get(i);
				if(!containsSingle(value, fixedElements, dynamicElements, dynamicLists)) {
					return false;
				}
			}
			return true;
		}

		private static boolean containsAny(ListExpression<?, CharSequence> target, Set<CharSequence> fixedElements,
				Expression<CharSequence>[] dynamicElements, ListExpression<?, CharSequence>[] dynamicLists) {
			int size = target.size();
			for (int i = 0; i < size; i++) {
				CharSequence value = target.get(i);
				if(containsSingle(value, fixedElements, dynamicElements, dynamicLists)) {
					return true;
				}
			}
			return false;
		}

		private static boolean containsNone(ListExpression<?, CharSequence> target, Set<CharSequence> fixedElements,
				Expression<CharSequence>[] dynamicElements, ListExpression<?, CharSequence>[] dynamicLists) {
			int size = target.size();
			for (int i = 0; i < size; i++) {
				CharSequence value = target.get(i);
				if(containsSingle(value, fixedElements, dynamicElements, dynamicLists)) {
					return false;
				}
			}
			return true;
		}

		private static boolean containsSingle(CharSequence value, Set<CharSequence> fixedElements,
				Expression<CharSequence>[] dynamicElements, ListExpression<?, CharSequence>[] dynamicLists) {
			return (!fixedElements.isEmpty() && fixedElements.contains(value))
					|| (dynamicElements.length>0 && containsDynamic(value, dynamicElements))
					|| (dynamicLists.length>0 && containsDynamicExpanded(value, dynamicLists));
		}

		@Override
		public boolean computeAsBoolean() {
			switch (mode) {
			case SINGLE: return containsSingle(target.compute(), fixedElements,
					dynamicElements, dynamicLists);

			case EXPAND: return containsAny(listTarget, fixedElements, dynamicElements, dynamicLists);
			case EXPAND_EXHAUSTIVE: return containsAll(listTarget, fixedElements, dynamicElements, dynamicLists);
			case EXPAND_EXHAUSTIVE_NEGATED: return containsNone(listTarget, fixedElements, dynamicElements, dynamicLists);

			default:
				throw forUnknownMode(mode);
			}
		}

		@Override
		public Expression<Primitive<Boolean>> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new TextSetPredicate(
					mode,
					target==null ? context.duplicate(listTarget) : context.duplicate(target),
					new ObjectOpenCustomHashSet<>(fixedElements, STRATEGY),
					EvaluationUtils.duplicate(dynamicElements, context),
					EvaluationUtils.duplicate(dynamicLists, context, ListExpression.class));
		}

		/**
		 * @see de.ims.icarus2.query.api.exp.Expression#optimize(de.ims.icarus2.query.api.exp.EvaluationContext)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Expression<Primitive<Boolean>> optimize(EvaluationContext context) {
			requireNonNull(context);
			Expression<?> oldTarget = target==null ? listTarget : target;
			Expression<?> newTarget = oldTarget.optimize(context);

			/*
			 * If we have at least 1 constant in the current set of dynamic elements,
			 * we can shift that over to the fixed set.
			 */
			Set<CharSequence> fixedElements = new ObjectOpenCustomHashSet<>(this.fixedElements, STRATEGY);
			Expression<CharSequence>[] dynamicElements = Stream.of(this.dynamicElements)
					.map(expression -> expression.optimize(context))
					.filter(ne -> {
						if(ne.isConstant()) {
							fixedElements.add(ne.compute());
							return false;
						}
						return true;
					})
					.toArray(Expression[]::new);
			ListExpression<?, CharSequence>[] dynamicLists = Stream.of(this.dynamicLists)
					.map(expression -> expression.optimize(context))
					.map(ListExpression.class::cast)
					.filter(ile -> {
						if(ile.isConstant()) {
							ile.forEachItem(item -> fixedElements.add((CharSequence) item));
							return false;
						}
						return true;
					})
					.toArray(ListExpression[]::new);

			// Special case of full constant expression -> optimize into single boolean
			if(newTarget.isConstant() && dynamicElements.length==0 && dynamicLists.length==0) {
				switch (mode) {
				case SINGLE: return Literals.of(containsSingle(
						((Expression<CharSequence>)newTarget).compute(), fixedElements,
						dynamicElements, dynamicLists));

				case EXPAND: return Literals.of(containsAny((ListExpression<?, CharSequence>)newTarget,
						fixedElements, dynamicElements, dynamicLists));
				case EXPAND_EXHAUSTIVE: return Literals.of(
						containsAll((ListExpression<?, CharSequence>)newTarget,
								fixedElements, dynamicElements, dynamicLists));
				case EXPAND_EXHAUSTIVE_NEGATED: return Literals.of(
						containsNone((ListExpression<?, CharSequence>)newTarget,
								fixedElements, dynamicElements, dynamicLists));

				default:
					throw forUnknownMode(mode);
				}
			} else if(dynamicElements.length < this.dynamicElements.length
					|| dynamicLists.length < this.dynamicLists.length) {
				return new TextSetPredicate(mode, newTarget, fixedElements, dynamicElements, dynamicLists);
			}

			return this;
		}
	}
}
