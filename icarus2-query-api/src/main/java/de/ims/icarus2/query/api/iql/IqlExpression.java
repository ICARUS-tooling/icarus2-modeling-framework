/**
 *
 */
package de.ims.icarus2.query.api.iql;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Markus Gärtner
 *
 */
public class IqlExpression implements IqlQueryElement {

	@JsonProperty(IqlProperties.CONTENT)
	public String content;

	@JsonProperty(IqlProperties.RETURN_TYPE)
	public Class<?> returnType;

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.EXPRESSION;
	}

}
