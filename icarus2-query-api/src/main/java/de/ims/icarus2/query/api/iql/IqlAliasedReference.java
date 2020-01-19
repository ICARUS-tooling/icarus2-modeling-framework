/**
 *
 */
package de.ims.icarus2.query.api.iql;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Markus Gärtner
 *
 */
public abstract class IqlAliasedReference extends IqlUnique {

	@JsonProperty(IqlProperties.NAME)
	public String name;

	@JsonProperty(IqlProperties.ALIAS)
	public String alias;
}
