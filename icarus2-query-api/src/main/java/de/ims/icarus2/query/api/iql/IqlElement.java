/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import java.util.Optional;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public abstract class IqlElement extends IqlUnique {

	@JsonProperty(IqlProperties.LABEL)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<String> label = Optional.empty();

	@JsonProperty(IqlProperties.CONSTRAINT)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<IqlConstraint> constraint = Optional.empty();

	public Optional<String> getLabel() { return label; }

	public Optional<IqlConstraint> getConstraint() { return constraint; }

	public void setLabel(String label) { this.label = Optional.of(checkNotEmpty(label)); }

	public void setConstraint(IqlConstraint constraint) { this.constraint = Optional.of(constraint); }

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlUnique#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		super.checkIntegrity();

		checkOptionalStringNotEmpty(label, IqlProperties.LABEL);
		checkOptionalNested(constraint);
	}

	public static class IqlNode extends IqlElement {

		@JsonProperty(IqlProperties.QUANTIFIERS)
		@JsonInclude(Include.NON_EMPTY)
		private final List<IqlQuantifier> quantifiers = new ArrayList<>();

		/**
		 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
		 */
		@Override
		public IqlType getType() {
			return IqlType.NODE;
		}

		/**
		 * @see de.ims.icarus2.query.api.iql.IqlElement#checkIntegrity()
		 */
		@Override
		public void checkIntegrity() {
			super.checkIntegrity();

			checkCollection(quantifiers);
		}

		public List<IqlQuantifier> getQuantifiers() { return CollectionUtils.unmodifiableListProxy(quantifiers); }

		public void addQuantifier(IqlQuantifier quantifier) { quantifiers.add(requireNonNull(quantifier)); }

		public void forEachQuantifier(Consumer<? super IqlQuantifier> action) { quantifiers.forEach(requireNonNull(action)); }
	}

	public static class IqlTreeNode extends IqlNode {

		@JsonProperty(IqlProperties.CHILDREN)
		@JsonInclude(Include.NON_EMPTY)
		private List<IqlTreeNode> children = new ArrayList<>();

		/**
		 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
		 */
		@Override
		public IqlType getType() {
			return IqlType.TREE_NODE;
		}

		/**
		 * @see de.ims.icarus2.query.api.iql.IqlElement.IqlNode#checkIntegrity()
		 */
		@Override
		public void checkIntegrity() {
			super.checkIntegrity();

			checkCollection(children);
		}

		public List<IqlTreeNode> getChildren() { return CollectionUtils.unmodifiableListProxy(children); }

		public void addChild(IqlTreeNode child) { children.add(requireNonNull(child)); }

		public void forEachChild(Consumer<? super IqlTreeNode> action) { children.forEach(requireNonNull(action)); }
	}

	public static class IqlEdge extends IqlElement {

		@JsonProperty(IqlProperties.SOURCE)
		private IqlNode source;

		@JsonProperty(IqlProperties.TARGET)
		private IqlNode target;

		@JsonProperty(IqlProperties.EDGE_TYPE)
		private EdgeType edgeType;

		/**
		 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
		 */
		@Override
		public IqlType getType() {
			return IqlType.EDGE;
		}

		/**
		 * @see de.ims.icarus2.query.api.iql.IqlUnique#checkIntegrity()
		 */
		@Override
		public void checkIntegrity() {
			super.checkIntegrity();
			checkNestedNotNull(source, IqlProperties.SOURCE);
			checkNestedNotNull(target, IqlProperties.TARGET);
			checkNotNull(edgeType, IqlProperties.EDGE_TYPE);
		}

		public IqlNode getSource() { return source; }

		public IqlNode getTarget() { return target; }

		public EdgeType getEdgeType() { return edgeType; }

		public void setSource(IqlNode source) { this.source = requireNonNull(source); }

		public void setTarget(IqlNode target) { this.target = requireNonNull(target); }

		public void setEdgeType(EdgeType edgeType) { this.edgeType = requireNonNull(edgeType); }
	}

	public enum EdgeType {
		UNDIRECTED("simple"),
		UNIDIRECTIONAL("uni"),
		BIDIRECTIONAL("bi"),
		;

		private final String label;

		private EdgeType(String label) {
			this.label = label;
		}

		@JsonValue
		public String getLabel() {
			return label;
		}
	}
}
