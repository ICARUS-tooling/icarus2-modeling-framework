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

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;

/**
 * @author Markus Gärtner
 *
 */
public class IqlQuantifier extends AbstractIqlQueryElement {

	@JsonProperty(IqlProperties.QUANTIFIER_TYPE)
	private QuantifierType quantifierType;

	@JsonProperty(IqlProperties.VALUE)
	@JsonInclude(Include.NON_DEFAULT)
	private Integer value;

	@JsonProperty(IqlProperties.LOWER_BOUND)
	@JsonInclude(Include.NON_DEFAULT)
	private Integer lowerBound;

	@JsonProperty(IqlProperties.UPPER_BOUND)
	@JsonInclude(Include.NON_DEFAULT)
	private Integer upperBound;

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.QUANTIFIER;
	}

	/**
	 * @see de.ims.icarus2.query.api.iql.AbstractIqlQueryElement#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkNotNull(quantifierType, IqlProperties.QUANTIFIER_TYPE);
		switch (quantifierType) {
		case EXACT:
		case AT_LEAST:
		case AT_MOST:
			checkNotNull(value, IqlProperties.VALUE);
			break;
		case RANGE:
			checkNotNull(lowerBound, IqlProperties.LOWER_BOUND);
			checkNotNull(upperBound, IqlProperties.UPPER_BOUND);
			break;
		default:
			throw new IcarusRuntimeException(GlobalErrorCode.INTERNAL_ERROR,
					"Unknown quantifier type: "+quantifierType);
		}
	}

	private Integer _set(int val) {
		checkArgument(val>=0);
		return Integer.valueOf(val);
	}

	public QuantifierType getQuantifierType() { return quantifierType; }

	public Optional<Integer> getValue() { return Optional.ofNullable(value); }

	public Optional<Integer> getLowerBound() { return Optional.ofNullable(lowerBound); }

	public Optional<Integer> getUpperBound() { return Optional.ofNullable(upperBound); }

	public void setQuantifierType(QuantifierType quantifierType) { this.quantifierType = requireNonNull(quantifierType); }

	public void setValue(int value) { this.value = _set(value); }

	public void setLowerBound(int lowerBound) { this.lowerBound = _set(lowerBound); }

	public void setUpperBound(int upperBound) { this.upperBound = _set(upperBound); }

	public enum QuantifierType {
		EXACT("exact"),
		AT_MOST("atMost"),
		AT_LEAST("atLeast"),
		RANGE("range"),
		;

		private final String label;

		private QuantifierType(String label) {
			this.label = label;
		}

		@JsonValue
		public String getLabel() {
			return label;
		}
	}
}
