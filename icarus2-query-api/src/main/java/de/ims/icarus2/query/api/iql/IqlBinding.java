/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import com.fasterxml.jackson.annotation.JsonValue;

import de.ims.icarus2.query.api.exp.TypeInfo;
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
	@JsonProperty(IqlTags.DISTINCT)
	@JsonInclude(Include.NON_DEFAULT)
	private boolean distinct = false;

	/**
	 * Reference to the layer the members should be bound to.
	 */
	@JsonProperty(value=IqlTags.TARGET, required=true)
	private String target;

	/**
	 * Declares this binding to target edges in the specified structure.
	 * <p>
	 * Note that the {@link #getTarget()} of this binding must resolve to
	 * a structure layer in order for this to work!
	 */
	@JsonProperty(IqlTags.EDGES)
	@JsonInclude(Include.NON_DEFAULT)
	private boolean edges = false;

	/**
	 * List of the actual member variables that should be bound
	 * to the specified {@link #target target layer}.
	 */
	@JsonProperty(value=IqlTags.MEMBERS, required=true)
	private final List<IqlReference> members = new ArrayList<>();

	@Override
	public IqlType getType() { return IqlType.BINDING; }

	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkStringNotEmpty(target, IqlTags.TARGET);
		checkCollectionNotEmpty(members, IqlTags.MEMBERS);
	}

	public boolean isDistinct() { return distinct; }

	public boolean isEdges() { return edges; }

	public String getTarget() { return target; }

	public List<IqlReference> getMembers() { return CollectionUtils.unmodifiableListProxy(members); }

	public void forEachMember(Consumer<? super IqlReference> action) { members.forEach(requireNonNull(action)); }

	public void setDistinct(boolean distinct) { this.distinct = distinct; }

	public void setEdges(boolean edges) { this.edges = edges; }

	public void setTarget(String target) { this.target = checkNotEmpty(target); }

	public void addMember(IqlReference member) { members.add(requireNonNull(member)); }

	@Deprecated
	public enum MemberType {
		ITEM("item", TypeInfo.ITEM),
		EDGE("edge", TypeInfo.EDGE),
		FRAGMENT("fragment", TypeInfo.FRAGMENT),
		CONTAINER("container", TypeInfo.CONTAINER),
		STRUCTURE("structure", TypeInfo.STRUCTURE),
		;

		private final TypeInfo type;
		private final String label;

		private MemberType(String label, TypeInfo type) {
			this.type = requireNonNull(type);
			this.label = requireNonNull(label);
		}

		@JsonValue
		public String getLabel() { return label; }

		public TypeInfo getType() { return type; }
	}
}
