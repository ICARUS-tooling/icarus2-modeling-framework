/**
 *
 */
package de.ims.icarus2.query.api.iql;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Markus Gärtner
 *
 */
public class IqlLayer extends IqlAliasedReference {

	/**
	 * Indicates that this layer is meant to be used as a primary layer
	 * in the query result.
	 */
	@JsonProperty(IqlProperties.PRIMARY)
	public boolean primary;

	/**
	 * When this layer definition is used inside a {@link IqlScope}, effectively
	 * adds the entire member-subgraph of this layer to the scope.
	 *
	 * Note: This property is redundant when the layer is part of the regular
	 * {@link IqlQuery#layers} declaration, as in that case all member subgraphs
	 * for each layer are already being added to the global scope!
	 */
	@JsonProperty(IqlProperties.ALL_MEMBERS)
	public boolean allMembers;

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.LAYER;
	}

}
