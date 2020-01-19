/**
 *
 */
package de.ims.icarus2.query.api.iql;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Markus Gärtner
 *
 */
public class IqlProperty implements IqlQueryElement {

	/**
	 * Mandatory key used to identify this property or switch
	 */
	@JsonProperty(IqlProperties.KEY)
	public String key;

	/**
	 * Value to set for this property if it is not a switch.
	 * Can be either a String or any primitive value.
	 */
	@JsonProperty(IqlProperties.VALUE)
	public Object value;

	@Override
	public IqlType getType() {
		return IqlType.PROPERTY;
	}
}
