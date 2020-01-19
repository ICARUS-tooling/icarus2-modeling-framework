/**
 *
 */
package de.ims.icarus2.query.api.iql;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Markus Gärtner
 *
 */
public abstract class IqlConstraint extends IqlUnique {

	@JsonProperty(IqlProperties.SOLVED)
	public boolean solved;

	@JsonProperty(IqlProperties.SOLVED_AS)
	public boolean solvedAs;

	public static class IqlPredicate extends IqlConstraint {

		@JsonProperty(IqlProperties.EXPRESSION)
		public IqlExpression expression;

		/**
		 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
		 */
		@Override
		public IqlType getType() {
			return IqlType.PREDICATE;
		}
	}

	public static class IqlTerm extends IqlConstraint {

		@JsonProperty(IqlProperties.LEFT)
		public IqlConstraint left;

		@JsonProperty(IqlProperties.RIGHT)
		public IqlConstraint right;

		@JsonProperty(IqlProperties.OPERATION)
		public BooleanOperation operation;

		/**
		 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
		 */
		@Override
		public IqlType getType() {
			return IqlType.TERM;
		}
	}

	public enum BooleanOperation {
		CONJUNCTION("and"),
		DISJUNCTION("or"),
		;

		private final String label;

		private BooleanOperation(String label) {
			this.label = label;
		}

		@JsonValue
		public String getLabel() {
			return label;
		}
	}
}
