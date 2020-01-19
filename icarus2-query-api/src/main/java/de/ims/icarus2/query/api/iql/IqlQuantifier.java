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
public class IqlQuantifier implements IqlQueryElement {


	@JsonProperty(IqlProperties.QUANTIFIER_TYPE)
	public QuantifierType quantifierType;

	@JsonProperty(IqlProperties.VALUE)
	public int value;

	@JsonProperty(IqlProperties.LOWER_BOUND)
	public int lowerBound;

	@JsonProperty(IqlProperties.UPPER_BOUND)
	public int upperBound;

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.QUANTIFIER;
	}

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
