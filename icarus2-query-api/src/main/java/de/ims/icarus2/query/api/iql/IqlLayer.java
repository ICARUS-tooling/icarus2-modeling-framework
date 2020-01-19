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
public class IqlLayer extends IqlAliasedReference {

	/**
	 * Indicates that this layer is meant to be used as a primary layer
	 * in the query result.
	 */
	@JsonProperty(IqlProperties.PRIMARY)
	@JsonInclude(Include.NON_DEFAULT)
	private boolean primary = false;

	/**
	 * When this layer definition is used inside a {@link IqlScope}, effectively
	 * adds the entire member-subgraph of this layer to the scope.
	 *
	 * Note: This property is redundant when the layer is part of the regular
	 * {@link IqlQuery#layers} declaration, as in that case all member subgraphs
	 * for each layer are already being added to the global scope!
	 */
	@JsonProperty(IqlProperties.ALL_MEMBERS)
	@JsonInclude(Include.NON_DEFAULT)
	private boolean allMembers = false;

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.LAYER;
	}

	public boolean isPrimary() { return primary; }

	public boolean isAllMembers() { return allMembers; }

	public void setPrimary(boolean primary) { this.primary = primary; }

	public void setAllMembers(boolean allMembers) { this.allMembers = allMembers; }

}
