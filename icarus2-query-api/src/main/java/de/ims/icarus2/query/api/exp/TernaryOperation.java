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

import static de.ims.icarus2.query.api.exp.EvaluationUtils.checkBooleanType;
import static java.util.Objects.requireNonNull;


/**
 * @author Markus Gärtner
 *
 */
public class TernaryOperation {

	public static Expression<?> of(TypeInfo type, Expression<?> condition, Expression<?> option1, Expression<?> option2) {
		checkBooleanType(condition);

		return new TernaryOp(type, condition, option1, option2);
	}

	static final class TernaryOp implements Expression<Object> {
		private final Expression<?> condition;
		private final Expression<?> option1, option2;
		private final TypeInfo type;

		TernaryOp(TypeInfo type, Expression<?> condition, Expression<?> option1, Expression<?> option2) {
			this.type = requireNonNull(type);
			this.condition = requireNonNull(condition);
			this.option1 = requireNonNull(option1);
			this.option2 = requireNonNull(option2);
		}

		@Override
		public TypeInfo getResultType() { return type; }

		private Expression<?> choose() {
			return condition.computeAsBoolean() ? option1 : option2;
		}

		@Override
		public Object compute() {
			return choose().compute();
		}

		@Override
		public long computeAsLong() {
			return choose().computeAsLong();
		}

		@Override
		public int computeAsInt() {
			return choose().computeAsInt();
		}

		@Override
		public double computeAsDouble() {
			return choose().computeAsDouble();
		}

		@Override
		public boolean computeAsBoolean() {
			return choose().computeAsBoolean();
		}

		@Override
		public Expression<Object> duplicate(EvaluationContext context) {
			return new TernaryOp(type,
					context.duplicate(condition),
					context.duplicate(option1),
					context.duplicate(option2));
		}

		@SuppressWarnings("unchecked")
		@Override
		public Expression<Object> optimize(EvaluationContext context) {

			Expression<?> newCondition = context.optimize(condition);
			Expression<?> newOption1 = context.optimize(option1);
			Expression<?> newOption2 = context.optimize(option2);

			// Jackpot : fixed choice
			if(newCondition.isConstant()) {
				Expression<?> fixedOption = newCondition.computeAsBoolean() ? newOption1 : newOption2;
				return (Expression<Object>)fixedOption;
			}

			// If anything has changed, propagate the optimization effect
			if(newCondition!=condition || newOption1!=option1 || newOption2!=option2) {
				return new TernaryOp(type, newCondition, newOption1, newOption2);
			}

			// Nothing changed
			return this;
		}
	}
}
