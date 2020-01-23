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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalLong;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public class IqlResult extends AbstractIqlQueryElement {

	@JsonProperty(value=IqlProperties.RESULT_TYPES)
	private final Set<ResultType> resultTypes = new HashSet<>();

	/**
	 * The processed result instructions if
	 * {@link #rawResultInstructions} was set.
	 */
	@JsonProperty(IqlProperties.RESULT_INSTRUCTIONS)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlResultInstruction> resultInstructions = new ArrayList<>();

	@JsonProperty(IqlProperties.LIMIT)
	@JsonInclude(Include.NON_ABSENT)
	private OptionalLong limit = OptionalLong.empty();

	@JsonProperty(IqlProperties.PERCENT)
	@JsonInclude(Include.NON_DEFAULT)
	private boolean percent = false;

	@JsonProperty(IqlProperties.SORTINGS)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlSorting> sortings = new ArrayList<>();

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.RESULT;
	}

	/**
	 * @see de.ims.icarus2.query.api.iql.AbstractIqlQueryElement#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkCondition(!resultTypes.isEmpty(), "resultTypes", "Must define at elast 1 result type");
		checkCondition(!percent || (limit.isPresent() && limit.getAsLong()>0 && limit.getAsLong()<100),
				"percent", "When using 'percent' flag, 'limit' must be between 0 and 100 (both ends exclusive)");

		checkCollection(resultInstructions);
		checkCollection(sortings);
	}

	public List<IqlResultInstruction> getResultInstructions() { return CollectionUtils.unmodifiableListProxy(resultInstructions); }

	public Set<ResultType> getResultTypes() { return CollectionUtils.unmodifiableSetProxy(resultTypes); }

	public OptionalLong getLimit() { return limit; }

	public boolean isPercent() { return percent; }

	public List<IqlSorting> getSortings() { return CollectionUtils.unmodifiableListProxy(sortings); }

	public void setLimit(long limit) { checkArgument(limit>0); this.limit = OptionalLong.of(limit); }

	public void setPercent(boolean percent) { this.percent = percent; }

	public void addResultInstruction(IqlResultInstruction instruction) { resultInstructions.add(requireNonNull(instruction)); }

	public void addResultType(ResultType resultType) { resultTypes.add(requireNonNull(resultType)); }

	public void addSorting(IqlSorting sorting) { sortings.add(requireNonNull(sorting)); }

	public enum ResultType {

		KWIC("kwic", "Simple 'keyword in context' result info with customizable window size"),
		CUSTOM("custom", "Only the user defined result scheme is to be used"),
		;

		private final String label, description;

		private ResultType(String label, String description) {
			this.label = label;
			this.description = description;
		}

		@JsonValue
		public String getLabel() {
			return label;
		}

		public String getDescription() {
			return description;
		}
	}
}
