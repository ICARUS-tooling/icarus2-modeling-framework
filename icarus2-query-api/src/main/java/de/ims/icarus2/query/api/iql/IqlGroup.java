/**
 *
 */
package de.ims.icarus2.query.api.iql;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Markus Gärtner
 *
 */
public class IqlGroup extends IqlUnique {

	@JsonProperty(IqlProperties.GROUP_BY)
	public IqlExpression groupBy;

	@JsonProperty(IqlProperties.FILTER_ON)
	public IqlExpression filterOn;

	@JsonProperty(IqlProperties.LABEL)
	public String label;

	@JsonProperty(IqlProperties.DEFAULT_VALUE)
	public Object defaultValue;

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.GROUP;
	}

}
