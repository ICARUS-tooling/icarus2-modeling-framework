/**
 *
 */
package de.ims.icarus2.query.api.iql;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Markus Gärtner
 *
 */
public class IqlData extends IqlUnique {

	@JsonProperty(IqlProperties.CONTENT)
	public String content;

	@JsonProperty(IqlProperties.VARIABLE)
	public String variable;

	@JsonProperty(IqlProperties.CHECKSUM)
	public String checksum;

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.DATA;
	}
}
