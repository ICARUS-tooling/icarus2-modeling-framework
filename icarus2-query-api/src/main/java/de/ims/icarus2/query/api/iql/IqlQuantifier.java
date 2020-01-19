/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Markus Gärtner
 *
 */
public class IqlQuantifier extends AbstractIqlQueryElement {

	@JsonProperty(IqlProperties.QUANTIFIER_TYPE)
	private QuantifierType quantifierType;

	@JsonProperty(IqlProperties.VALUE)
	@JsonInclude(Include.NON_DEFAULT)
	private int value = UNSET_INT;

	@JsonProperty(IqlProperties.LOWER_BOUND)
	@JsonInclude(Include.NON_DEFAULT)
	private int lowerBound = UNSET_INT;

	@JsonProperty(IqlProperties.UPPER_BOUND)
	@JsonInclude(Include.NON_DEFAULT)
	private int upperBound = UNSET_INT;

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
	}

	private int _set(int val) {
		checkArgument(val>=0);
		return val;
	}

	public QuantifierType getQuantifierType() { return quantifierType; }

	public int getValue() { return value; }

	public int getLowerBound() { return lowerBound; }

	public int getUpperBound() { return upperBound; }

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
