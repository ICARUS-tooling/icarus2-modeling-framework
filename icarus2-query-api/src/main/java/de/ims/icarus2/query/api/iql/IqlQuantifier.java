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

import java.util.Objects;
import java.util.OptionalInt;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import it.unimi.dsi.fastutil.Hash.Strategy;

/**
 * @author Markus Gärtner
 *
 */
public class IqlQuantifier extends AbstractIqlQueryElement {

	public static IqlQuantifier all() {
		IqlQuantifier q = new IqlQuantifier();
		q.setQuantifierType(QuantifierType.ALL);
		return q;
	}

	public static IqlQuantifier none() {
		IqlQuantifier q = new IqlQuantifier();
		q.setQuantifierType(QuantifierType.EXACT);
		q.setValue(0);
		return q;
	}

	public static final Strategy<IqlQuantifier> EQUALITY = new Strategy<IqlQuantifier>() {

		@Override
		public int hashCode(IqlQuantifier q) {
			return Objects.hash(q.quantifierType, q.value, q.lowerBound, q.upperBound);
		}

		@Override
		public boolean equals(IqlQuantifier q1, IqlQuantifier q2) {
			if(q1==null || q2==null) {
				return q1==q2;
			}
			return Objects.equals(q1.quantifierType, q2.quantifierType)
					&& q1.value.equals(q2.value)
					&& q1.lowerBound.equals(q2.lowerBound)
					&& q1.upperBound.equals(q2.upperBound);
		}
	};

	@JsonProperty(value=IqlProperties.QUANTIFIER_TYPE, required=true)
	private QuantifierType quantifierType;

	@JsonProperty(value=IqlProperties.QUANTIFIER_MODIFIER)
	@JsonInclude(Include.NON_DEFAULT)
	private QuantifierModifier quantifierModifier = QuantifierModifier.GREEDY;

	@JsonProperty(IqlProperties.VALUE)
	@JsonInclude(Include.NON_ABSENT)
	private OptionalInt value = OptionalInt.empty();

	@JsonProperty(IqlProperties.LOWER_BOUND)
	@JsonInclude(Include.NON_ABSENT)
	private OptionalInt lowerBound = OptionalInt.empty();

	@JsonProperty(IqlProperties.UPPER_BOUND)
	@JsonInclude(Include.NON_ABSENT)
	private OptionalInt upperBound = OptionalInt.empty();

	@Override
	public IqlType getType() { return IqlType.QUANTIFIER; }

	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkNotNull(quantifierType, IqlProperties.QUANTIFIER_TYPE);
		switch (quantifierType) {
		case ALL:
//			checkNotPresent(value, IqlProperties.VALUE);
//			checkNotPresent(lowerBound, IqlProperties.LOWER_BOUND);
//			checkNotPresent(upperBound, IqlProperties.UPPER_BOUND);
			break;
		case EXACT:
		case AT_LEAST:
		case AT_MOST:
			checkPresent(value, IqlProperties.VALUE);
//			checkNotPresent(lowerBound, IqlProperties.LOWER_BOUND);
//			checkNotPresent(upperBound, IqlProperties.UPPER_BOUND);
			break;
		case RANGE:
			checkPresent(lowerBound, IqlProperties.LOWER_BOUND);
			checkPresent(upperBound, IqlProperties.UPPER_BOUND);
//			checkNotPresent(value, IqlProperties.VALUE);
			break;
		default:
			throw new IcarusRuntimeException(GlobalErrorCode.INTERNAL_ERROR,
					"Unknown quantifier type: "+quantifierType);
		}
	}

	private OptionalInt _set(int val) {
		checkArgument(val>=0);
		return OptionalInt.of(val);
	}

	public QuantifierType getQuantifierType() { return quantifierType; }

	public QuantifierModifier getQuantifierModifier() { return quantifierModifier; }

	public OptionalInt getValue() { return value; }

	public OptionalInt getLowerBound() { return lowerBound; }

	public OptionalInt getUpperBound() { return upperBound; }

	public void setQuantifierType(QuantifierType quantifierType) { this.quantifierType = requireNonNull(quantifierType); }

	public void setQuantifierModifier(QuantifierModifier quantifierModifier) { this.quantifierModifier = requireNonNull(quantifierModifier); }

	public void setValue(int value) { this.value = _set(value); }

	public void setLowerBound(int lowerBound) { this.lowerBound = _set(lowerBound); }

	public void setUpperBound(int upperBound) { this.upperBound = _set(upperBound); }

	// Utility

	public boolean isExistentiallyQuantified() {
		return (value.isPresent() && value.getAsInt()>0)
				|| (upperBound.isPresent() && upperBound.getAsInt()>0);
	}

	public boolean isExistentiallyNegated() {
		return quantifierType==QuantifierType.EXACT && value.getAsInt()==0;
	}

	public enum QuantifierType {
		ALL("all"),
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

	public enum QuantifierModifier {
		/** Match as many as possible while still considering subsequent constraints or nodes */
		GREEDY("greedy"),
		/** Match as few as possible, considering subsequent constraints or nodes */
		RELUCTANT("reluctanct"),
		/** Match as many as possible without consideration for subsequent constraints or nodes */
		POSSESSIVE("possessive"),
		;

		private final String label;

		private QuantifierModifier(String label) {
			this.label = label;
		}

		@JsonValue
		public String getLabel() {
			return label;
		}
	}
}
