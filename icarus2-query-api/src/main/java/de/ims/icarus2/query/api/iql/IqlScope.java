/**
 *
 */
package de.ims.icarus2.query.api.iql;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Markus Gärtner
 *
 */
public class IqlScope extends IqlAliasedReference {

	/**
	 * Indicates that the primary layer of this scope is meant to be
	 * used as a primary layer in the query result.
	 */
	@JsonProperty(IqlProperties.PRIMARY)
	public boolean primary;

	/**
	 * Defines the members of this scope.
	 */
	@JsonProperty(IqlProperties.LAYERS)
	public List<IqlLayer> layers = new ArrayList<>();

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.SCOPE;
	}

}
