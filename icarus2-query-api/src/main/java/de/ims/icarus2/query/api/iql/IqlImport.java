/**
 *
 */
package de.ims.icarus2.query.api.iql;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Markus Gärtner
 *
 */
public class IqlImport extends IqlAliasedReference {

	@JsonProperty(IqlProperties.OPTIONAL)
	@JsonInclude(Include.NON_DEFAULT)
	private boolean optional = false;

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.IMPORT;
	}

	public boolean isOptional() { return optional; }

	public void setOptional(boolean optional) { this.optional = optional; }

}
