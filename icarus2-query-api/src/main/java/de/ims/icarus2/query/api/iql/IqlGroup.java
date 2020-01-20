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

import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static java.util.Objects.requireNonNull;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Markus Gärtner
 *
 */
public class IqlGroup extends IqlUnique {

	@JsonProperty(IqlProperties.GROUP_BY)
	private IqlExpression groupBy;

	@JsonProperty(IqlProperties.FILTER_ON)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<IqlExpression> filterOn = Optional.empty();

	@JsonProperty(IqlProperties.LABEL)
	private String label;

	@JsonProperty(IqlProperties.DEFAULT_VALUE)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<Object> defaultValue = Optional.empty();

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.GROUP;
	}

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlUnique#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkStringNotEmpty(label, IqlProperties.LABEL);
		checkNestedNotNull(groupBy, IqlProperties.GROUP_BY);

		checkOptionalNested(filterOn);
	}

	public IqlExpression getGroupBy() { return groupBy; }

	public Optional<IqlExpression> getFilterOn() { return filterOn; }

	public String getLabel() { return label; }

	public Optional<Object> getDefaultValue() { return defaultValue; }

	public void setGroupBy(IqlExpression groupBy) { this.groupBy = requireNonNull(groupBy); }

	public void setFilterOn(IqlExpression filterOn) { this.filterOn = Optional.of(filterOn); }

	public void setLabel(String label) { this.label = checkNotEmpty(label); }

	public void setDefaultValue(Object defaultValue) { this.defaultValue = Optional.of(defaultValue); }

}