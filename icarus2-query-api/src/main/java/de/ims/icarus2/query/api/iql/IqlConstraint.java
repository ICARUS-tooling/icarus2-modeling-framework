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
package de.ims.icarus2.query.api.iql;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Markus Gärtner
 *
 */
public abstract class IqlConstraint extends IqlUnique {

	@JsonProperty(IqlProperties.SOLVED)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<Boolean> solved = Optional.empty();

	@JsonProperty(IqlProperties.SOLVED_AS)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<Boolean> solvedAs = Optional.empty();

	public boolean isSolved() { return solved.orElse(Boolean.FALSE).booleanValue(); }

	public boolean isSolvedAs() { return solvedAs.orElse(Boolean.FALSE).booleanValue(); }

	public void setSolved(boolean solved) { this.solved = Optional.ofNullable(setOrFallback(solved, false)); }

	public void setSolvedAs(boolean solvedAs) { this.solvedAs = Optional.ofNullable(setOrFallback(solvedAs, false)); }

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlUnique#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkCondition(!solvedAs.isPresent() || solved.isPresent(), IqlProperties.SOLVED_AS,
				"cannot give 'solvedAs' value if constraint is not marked 'solved'");
	}

	public static class IqlPredicate extends IqlConstraint {

		@JsonProperty(IqlProperties.EXPRESSION)
		private IqlExpression expression;

		/**
		 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
		 */
		@Override
		public IqlType getType() {
			return IqlType.PREDICATE;
		}

		public IqlExpression getExpression() { return expression; }

		public void setExpression(IqlExpression expression) { this.expression = requireNonNull(expression); }

		/**
		 * @see de.ims.icarus2.query.api.iql.IqlConstraint#checkIntegrity()
		 */
		@Override
		public void checkIntegrity() {
			super.checkIntegrity();
			checkNestedNotNull(expression, IqlProperties.EXPRESSION);
		}
	}

	public static class IqlTerm extends IqlConstraint {

		@JsonProperty(IqlProperties.LEFT)
		private IqlConstraint left;

		@JsonProperty(IqlProperties.RIGHT)
		private IqlConstraint right;

		@JsonProperty(IqlProperties.OPERATION)
		private BooleanOperation operation;

		/**
		 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
		 */
		@Override
		public IqlType getType() {
			return IqlType.TERM;
		}

		public IqlConstraint getLeft() { return left; }

		public IqlConstraint getRight() { return right; }

		public BooleanOperation getOperation() { return operation; }

		public void setLeft(IqlConstraint left) { this.left = requireNonNull(left); }

		public void setRight(IqlConstraint right) { this.right = requireNonNull(right); }

		public void setOperation(BooleanOperation operation) { this.operation = requireNonNull(operation); }

		/**
		 * @see de.ims.icarus2.query.api.iql.IqlConstraint#checkIntegrity()
		 */
		@Override
		public void checkIntegrity() {
			super.checkIntegrity();
			checkNotNull(operation, IqlProperties.OPERATION);
			checkNestedNotNull(left, IqlProperties.LEFT);
			checkNestedNotNull(right, IqlProperties.RIGHT);
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
