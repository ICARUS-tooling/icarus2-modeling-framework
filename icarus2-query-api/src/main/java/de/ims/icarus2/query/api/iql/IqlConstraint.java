/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public abstract class IqlConstraint extends IqlUnique {

	@JsonProperty(IqlTags.SOLVED)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<Boolean> solved = Optional.empty();

	@JsonProperty(IqlTags.SOLVED_AS)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<Boolean> solvedAs = Optional.empty();

	public boolean isSolved() { return solved.orElse(Boolean.FALSE).booleanValue(); }

	public boolean isSolvedAs() { return solvedAs.orElse(Boolean.FALSE).booleanValue(); }

	public void setSolved(boolean solved) { this.solved = Optional.ofNullable(setOrFallback(solved, false)); }

	public void setSolvedAs(boolean solvedAs) { this.solvedAs = Optional.ofNullable(setOrFallback(solvedAs, false)); }

	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkCondition(!solvedAs.isPresent() || solved.isPresent(), IqlTags.SOLVED_AS,
				"cannot give 'solvedAs' value if constraint is not marked 'solved'");
	}

	public static class IqlPredicate extends IqlConstraint {

		@JsonProperty(value=IqlTags.EXPRESSION, required=true)
		private IqlExpression expression;

		@Override
		public IqlType getType() { return IqlType.PREDICATE; }

		public IqlExpression getExpression() { return expression; }

		public void setExpression(IqlExpression expression) { this.expression = requireNonNull(expression); }

		@Override
		public void checkIntegrity() {
			super.checkIntegrity();
			checkNestedNotNull(expression, IqlTags.EXPRESSION);
		}
	}

	public static class IqlTerm extends IqlConstraint {

		@JsonProperty(value=IqlTags.ITEMS, required=true)
		private final List<IqlConstraint> items = new ArrayList<>();

		@JsonProperty(value=IqlTags.OPERATION, required=true)
		private BooleanOperation operation;

		@Override
		public IqlType getType() { return IqlType.TERM; }

		public List<IqlConstraint> getItems() { return CollectionUtils.unmodifiableListProxy(items); }

		public BooleanOperation getOperation() { return operation; }

		public void addItem(IqlConstraint item) { items.add(requireNonNull(item)); }

		public void setOperation(BooleanOperation operation) { this.operation = requireNonNull(operation); }

		@Override
		public void checkIntegrity() {
			super.checkIntegrity();
			checkNotNull(operation, IqlTags.OPERATION);
			checkCollectionNotEmpty(items, "items");
		}
	}

	public enum BooleanOperation {
		CONJUNCTION("conjunction"),
		DISJUNCTION("disjunction"),
		;

		private final String label;

		private BooleanOperation(String label) { this.label = label; }

		@JsonValue
		public String getLabel() { return label; }
	}
}
