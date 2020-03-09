/**
 *
 */
package de.ims.icarus2.query.api.eval;

import static de.ims.icarus2.query.api.eval.EvaluationUtils.checkBooleanType;
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
					condition.duplicate(context),
					option1.duplicate(context),
					option2.duplicate(context));
		}

		@SuppressWarnings("unchecked")
		@Override
		public Expression<Object> optimize(EvaluationContext context) {

			Expression<?> newCondition = condition.optimize(context);
			Expression<?> newOption1 = option1.optimize(context);
			Expression<?> newOption2 = option2.optimize(context);

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
