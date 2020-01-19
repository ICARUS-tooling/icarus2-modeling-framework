/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static de.ims.icarus2.util.Conditions.checkNotEmpty;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Markus Gärtner
 *
 */
public class IqlProperty extends AbstractIqlQueryElement {

	/**
	 * Mandatory key used to identify this property or switch
	 */
	@JsonProperty(IqlProperties.KEY)
	private String key;

	/**
	 * Value to set for this property if it is not a switch.
	 * Can be either a String or any primitive value.
	 */
	@JsonProperty(IqlProperties.VALUE)
	private Optional<Object> value = Optional.empty();

	@Override
	public IqlType getType() {
		return IqlType.PROPERTY;
	}

	/**
	 * @see de.ims.icarus2.query.api.iql.AbstractIqlQueryElement#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkStringNotEmpty(key, IqlProperties.KEY);
	}

	public String getKey() { return key; }

	public Optional<Object> getValue() { return value; }

	public void setKey(String key) { this.key = checkNotEmpty(key); }

	public void setValue(Object value) { this.value = Optional.of(value); }
}
