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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;

import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.MutablePrimitives.Primitive;
import de.ims.icarus2.util.collections.ArrayUtils;

/**
 * @author Markus Gärtner
 *
 */
public class LogicalOperators {

	public static Expression<Primitive<Boolean>> conjunction(
			Expression<?>[] elements, boolean earlyExit) {
		return new Conjunction(elements, earlyExit);
	}

	public static Expression<Primitive<Boolean>> disjunction(
			Expression<?>[] elements, boolean earlyExit) {
		return new Disjunction(elements, earlyExit);
	}

	static abstract class LogicalOperator implements Expression<Primitive<Boolean>> {
		protected final Expression<?>[] elements;
		protected final MutableBoolean value;
		protected final boolean earlyExit;

		protected LogicalOperator(Expression<?>[] elements, boolean earlyExit) {
			this.elements = requireNonNull(elements);
			checkArgument("Elements count must be greater than 1", elements.length>1);
			this.value = new MutableBoolean();
			this.earlyExit = earlyExit;
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.BOOLEAN; }

		@Override
		public Primitive<Boolean> compute() {
			value.setBoolean(computeAsBoolean());
			return value;
		}
	}

	static final class Conjunction extends LogicalOperator {

		Conjunction(Expression<?>[] elements, boolean earlyExit) {
			super(elements, earlyExit);
		}

		@Override
		public boolean computeAsBoolean() {
			boolean result = true;
			for (int i = 0; i < elements.length; i++) {
				result &= elements[i].computeAsBoolean();
				if(earlyExit && !result) {
					break;
				}
			}
			return result;
		}

		@Override
		public Expression<Primitive<Boolean>> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new Conjunction(EvaluationUtils.duplicate(elements, context), earlyExit);
		}

		@Override
		public Expression<Primitive<Boolean>> optimize(EvaluationContext context) {
			requireNonNull(context);
			MutableBoolean constantFalse = new MutableBoolean(false);
			Expression<?>[] newElements = Stream.of(elements)
					.map(exp -> exp.optimize(context))
					.filter(exp -> {
						if(exp.isConstant()) {
							if(!exp.computeAsBoolean()) {
								constantFalse.setBoolean(true);
							}
							return false;
						}
						return true;
					}).toArray(Expression[]::new);

			// Collapse entire term if we can and are allowed to do so
			if(newElements.length==0 || (constantFalse.booleanValue() && earlyExit)) {
				return Literals.of(!constantFalse.booleanValue());
			}

			if(ArrayUtils.containsSame(elements, newElements)) {
				// Nothing changed
				return this;
			}

			return new Conjunction(newElements, earlyExit);
		}
	}

	static final class Disjunction extends LogicalOperator {

		Disjunction(Expression<?>[] elements, boolean earlyExit) {
			super(elements, earlyExit);
		}

		@Override
		public boolean computeAsBoolean() {
			boolean result = false;
			for (int i = 0; i < elements.length; i++) {
				result |= elements[i].computeAsBoolean();
				if(earlyExit && result) {
					break;
				}
			}
			return result;
		}

		@Override
		public Expression<Primitive<Boolean>> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return new Disjunction(EvaluationUtils.duplicate(elements, context), earlyExit);
		}

		@Override
		public Expression<Primitive<Boolean>> optimize(EvaluationContext context) {
			requireNonNull(context);
			MutableBoolean constantTrue = new MutableBoolean(false);
			Expression<?>[] newElements = Stream.of(elements)
					.map(exp -> exp.optimize(context))
					.filter(exp -> {
						if(exp.isConstant()) {
							if(exp.computeAsBoolean()) {
								constantTrue.setBoolean(true);
							}
							return false;
						}
						return true;
					}).toArray(Expression[]::new);

			// Collapse entire term if we can and are allowed to do so
			if(newElements.length==0 || (constantTrue.booleanValue() && earlyExit)) {
				return Literals.of(constantTrue.booleanValue());
			}

			if(ArrayUtils.containsSame(elements, newElements)) {
				// Nothing changed
				return this;
			}

			return new Disjunction(newElements, earlyExit);
		}
	}
}
