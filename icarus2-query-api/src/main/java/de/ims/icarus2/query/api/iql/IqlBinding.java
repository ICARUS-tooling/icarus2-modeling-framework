/**
 *
 */
package de.ims.icarus2.query.api.iql;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.ims.icarus2.query.api.iql.IqlReference.IqlMember;

/**
 * @author Markus Gärtner
 *
 */
public class IqlBinding implements IqlQueryElement {

	/**
	 * Enforces that the bound member references in this binding do
	 * NOT match the same target. Depending on the localConstraint used in the query, this
	 * might be redundant (e.g. when using the member references as identifiers for tree nodes
	 * who already are structurally distinct), but can still be used to make that fact explicit.
	 */
	@JsonProperty(IqlProperties.DISTINCT)
	public boolean distinct;

	/**
	 * Reference to the layer the members should be bound to.
	 */
	@JsonProperty(IqlProperties.TARGET)
	public String target;

	/**
	 * List of the actual member variables that should be bound
	 * to the specified {@link #target target layer}.
	 */
	@JsonProperty(IqlProperties.MEMBERS)
	public List<IqlMember> members = new ArrayList<>();

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.BINDING;
	}
}
