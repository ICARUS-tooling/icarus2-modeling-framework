/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
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
public class IqlQuantifier extends AbstractIqlQueryElement {

	private static final Integer UNSET = Integer.valueOf(UNSET_INT);

	@JsonProperty(IqlProperties.QUANTIFIER_TYPE)
	private QuantifierType quantifierType;

	@JsonProperty(IqlProperties.VALUE)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<Integer> value = Optional.empty();

	@JsonProperty(IqlProperties.LOWER_BOUND)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<Integer> lowerBound = Optional.empty();

	@JsonProperty(IqlProperties.UPPER_BOUND)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<Integer> upperBound = Optional.empty();

	//TODO some mechanism to exclude the values on default

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

	private int _get(Optional<Integer> val) {
		return val.orElse(UNSET).intValue();
	}

	private Optional<Integer> _set(int val) {
		checkArgument(val>=0);
		return Optional.of(Integer.valueOf(val));
	}

	public QuantifierType getQuantifierType() { return quantifierType; }

	public int getValue() { return _get(value); }

	public int getLowerBound() { return _get(lowerBound); }

	public int getUpperBound() { return _get(upperBound); }

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
