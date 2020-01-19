/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.ims.icarus2.query.api.iql.IqlReference.IqlMember;
import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public class IqlBinding extends AbstractIqlQueryElement {

	/**
	 * Enforces that the bound member references in this binding do
	 * NOT match the same target. Depending on the localConstraint used in the query, this
	 * might be redundant (e.g. when using the member references as identifiers for tree nodes
	 * who already are structurally distinct), but can still be used to make that fact explicit.
	 */
	@JsonProperty(IqlProperties.DISTINCT)
	@JsonInclude(Include.NON_DEFAULT)
	private boolean distinct = false;

	/**
	 * Reference to the layer the members should be bound to.
	 */
	@JsonProperty(IqlProperties.TARGET)
	private String target;

	/**
	 * List of the actual member variables that should be bound
	 * to the specified {@link #target target layer}.
	 */
	@JsonProperty(IqlProperties.MEMBERS)
	private final List<IqlMember> members = new ArrayList<>();

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.BINDING;
	}

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkStringNotEmpty(target, IqlProperties.TARGET);
		checkCollectionNotEmpty(members, IqlProperties.MEMBERS);
	}

	public boolean isDistinct() { return distinct; }

	public String getTarget() { return target; }

	public List<IqlMember> getMembers() { return CollectionUtils.unmodifiableListProxy(members); }

	public void forEachMember(Consumer<? super IqlMember> action) { members.forEach(requireNonNull(action)); }

	public void setDistinct(boolean distinct) { this.distinct = distinct; }

	public void setTarget(String target) { this.target = checkNotEmpty(target); }

	public void addMember(IqlMember member) { members.add(requireNonNull(member)); }
}
