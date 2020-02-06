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

import java.util.stream.Stream;

import de.ims.icarus2.query.api.eval.Expression.BooleanExpression;
import de.ims.icarus2.query.api.eval.Expression.NumericalExpression;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.MutablePrimitives.Primitive;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

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
			NumericalExpression numQuery = (NumericalExpression)query;
			if(numQuery.isFPE()) {
				//TODO
			} else {
				return new FlatIntegerSetPredicate(numQuery, EvaluationUtils.ensureInteger(set));
			}
		}
		//TODO
		throw new UnsupportedOperationException();
	}


	// expects 'query' to be a list expression
	private static BooleanExpression allIn(Expression<?> query, Expression<?>...set) {
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
		private final LongSet fixedElements;
		private final NumericalExpression[] dynamicElements;
		private final MutableBoolean value;

		private final NumericalExpression target;

		FlatIntegerSetPredicate(NumericalExpression target, NumericalExpression[] elements) {
			requireNonNull(elements);
			this.target = requireNonNull(target);
			checkArgument("Need at least 1 element to check set containment", elements.length>0);
			fixedElements = new LongOpenHashSet();

			dynamicElements = filterConstant(Stream.of(elements), fixedElements);

			value = new MutableBoolean();
		}

		/** Copy constructor */
		private FlatIntegerSetPredicate(NumericalExpression target, LongSet fixedElements,
				NumericalExpression[] dynamicElements) {
			this.target = requireNonNull(target);
			this.fixedElements = requireNonNull(fixedElements);
			this.dynamicElements = requireNonNull(dynamicElements);
			value = new MutableBoolean();
		}

		private static NumericalExpression[] filterConstant(Stream<NumericalExpression> elements,
				LongSet fixedElements) {
			return elements
					// Collect all the constant entries directly into the fixed set for lookup
					.filter(element -> {
						boolean constant = element.isConstant();
						if(constant) {
							fixedElements.add(element.computeAsLong());
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

		private static boolean containsDynamic(long value, NumericalExpression[] elements) {
			for (int i = 0; i < elements.length; i++) {
				if(elements[i].computeAsLong()==value) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean computeAsBoolean() {
			long value = target.computeAsLong();
			return (!fixedElements.isEmpty() && fixedElements.contains(value))
					|| (dynamicElements.length>0 && containsDynamic(value, dynamicElements));
		}

		@Override
		public Expression<Primitive<Boolean>> duplicate(EvaluationContext context) {
			return new FlatIntegerSetPredicate(
					(NumericalExpression)target.duplicate(context),
					new LongOpenHashSet(fixedElements),
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
				return Literals.of(fixedElements.contains(newTarget.computeAsLong()));
			}

			/*
			 * If we have at least 1 constant in the current set of dynamic elements,
			 * we can shift that over to the fixed set.
			 */
			LongSet fixedElements = new LongOpenHashSet(this.fixedElements);
			NumericalExpression[] dynamicElements = filterConstant(
					Stream.of(this.dynamicElements)
						.map(expression -> expression.optimize(context))
						.map(NumericalExpression.class::cast),
					fixedElements);

			if(dynamicElements.length<this.dynamicElements.length) {

				// Special case of full constant expression -> optimize into single boolean
				if(newTarget.isConstant() && dynamicElements.length==0) {
					return Literals.of(fixedElements.contains(newTarget.computeAsLong()));
				}

				return new FlatIntegerSetPredicate(newTarget, fixedElements, dynamicElements);
			}

			return this;
		}
	}
}
