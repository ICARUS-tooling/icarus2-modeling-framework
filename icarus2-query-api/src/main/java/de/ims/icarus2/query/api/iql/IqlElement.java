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

import de.ims.icarus2.query.api.iql.IqlQuantifier.QuantifierType;
import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public abstract class IqlElement extends IqlUnique {

	@JsonProperty(IqlProperties.CONSUMED)
	@JsonInclude(Include.NON_DEFAULT)
	private boolean consumed = false;

	public boolean isConsumed() { return consumed; }

	public void setConsumed(boolean consumed) { this.consumed = consumed; }

	public static abstract class IqlProperElement extends IqlElement {

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

		@Override
		public void checkIntegrity() {
			super.checkIntegrity();

			checkOptionalStringNotEmpty(label, IqlProperties.LABEL);
			checkOptionalNested(constraint);
		}

	}

	private static boolean isExistentiallyQuantified0(List<IqlQuantifier> quantifiers) {
		if(quantifiers.isEmpty()) {
			return true;
		}
		for(IqlQuantifier quantifier : quantifiers) {
			if(quantifier.isExistentiallyQuantified()) {
				return true;
			}
		}
		return false;
	}

	private static boolean isUniversallyQuantified0(List<IqlQuantifier> quantifiers) {
		return quantifiers.size()==1
				&& quantifiers.get(0).getQuantifierType()==QuantifierType.ALL;
	}

	private static boolean isExistentiallyNegated0(List<IqlQuantifier> quantifiers) {
		return quantifiers.size()==1
				&& quantifiers.get(0).isExistentiallyNegated();
	}

	public static class IqlStructure extends IqlElement {

		@JsonProperty(value=IqlProperties.ELEMENTS, required=true)
		@JsonInclude(Include.NON_EMPTY)
		private final List<IqlElement> elements = new ArrayList<>();

		@Override
		public IqlType getType() { return IqlType.STRUCTURE; }

		@Override
		public void checkIntegrity() {
			super.checkIntegrity();

			checkCollectionNotEmpty(elements, IqlProperties.ELEMENTS);
		}

		public List<IqlElement> getElements() { return elements; }

		public void addElement(IqlElement element) { elements.add(requireNonNull(element)); }

		public void forEachElement(Consumer<? super IqlElement> action) { elements.forEach(requireNonNull(action)); }
	}

	public static class IqlElementGrouping extends IqlElement {

		@JsonProperty(IqlProperties.QUANTIFIERS)
		@JsonInclude(Include.NON_EMPTY)
		private final List<IqlQuantifier> quantifiers = new ArrayList<>();

		@JsonProperty(value=IqlProperties.ELEMENTS, required=true)
		@JsonInclude(Include.NON_EMPTY)
		private final List<IqlElement> elements = new ArrayList<>();

		@Override
		public IqlType getType() { return IqlType.ELEMENT_GROUPING; }

		@Override
		public void checkIntegrity() {
			super.checkIntegrity();

			checkCollection(quantifiers);
			checkCollectionNotEmpty(elements, IqlProperties.ELEMENTS);
		}

		public List<IqlElement> getElements() { return elements; }

		public List<IqlQuantifier> getQuantifiers() { return CollectionUtils.unmodifiableListProxy(quantifiers); }

		public void addQuantifier(IqlQuantifier quantifier) { quantifiers.add(requireNonNull(quantifier)); }

		public void forEachQuantifier(Consumer<? super IqlQuantifier> action) { quantifiers.forEach(requireNonNull(action)); }

		public void addElement(IqlElement element) { elements.add(requireNonNull(element)); }

		public void forEachElement(Consumer<? super IqlElement> action) { elements.forEach(requireNonNull(action)); }


		// utility

		public boolean hasQuantifiers() { return !quantifiers.isEmpty(); }

		public boolean isExistentiallyQuantified() { return isExistentiallyQuantified0(quantifiers); }

		public boolean isUniversallyQuantified() { return isUniversallyQuantified0(quantifiers); }

		public boolean isExistentiallyNegated() { return isExistentiallyNegated0(quantifiers); }
	}

	public static class IqlNode extends IqlProperElement {

		@JsonProperty(IqlProperties.QUANTIFIERS)
		@JsonInclude(Include.NON_EMPTY)
		private final List<IqlQuantifier> quantifiers = new ArrayList<>();

		@Override
		public IqlType getType() { return IqlType.NODE; }

		@Override
		public void checkIntegrity() {
			super.checkIntegrity();

			checkCollection(quantifiers);
		}

		public List<IqlQuantifier> getQuantifiers() { return CollectionUtils.unmodifiableListProxy(quantifiers); }

		public void addQuantifier(IqlQuantifier quantifier) { quantifiers.add(requireNonNull(quantifier)); }

		public void forEachQuantifier(Consumer<? super IqlQuantifier> action) { quantifiers.forEach(requireNonNull(action)); }

		// utility

		public boolean hasQuantifiers() { return !quantifiers.isEmpty(); }

		public boolean isExistentiallyQuantified() { return isExistentiallyQuantified0(quantifiers); }

		public boolean isUniversallyQuantified() { return isUniversallyQuantified0(quantifiers); }

		public boolean isExistentiallyNegated() { return isExistentiallyNegated0(quantifiers); }
	}

	public static class IqlElementSet extends IqlElement {

		@JsonProperty(IqlProperties.CHILDREN)
		@JsonInclude(Include.NON_EMPTY)
		private final List<IqlElement> nodes = new ArrayList<>();

		@JsonProperty(IqlProperties.NODE_ARRANGEMENT)
		@JsonInclude(Include.NON_DEFAULT)
		private NodeArrangement nodeArrangement = NodeArrangement.UNSPECIFIED;

		@Override
		public IqlType getType() { return IqlType.ELEMENT_SET; }

		@Override
		public void checkIntegrity() {
			super.checkIntegrity();

			checkCollection(nodes);
		}

		public List<IqlElement> getNodes() { return CollectionUtils.unmodifiableListProxy(nodes); }

		public NodeArrangement getNodeArrangement() { return nodeArrangement; }


		public void addNode(IqlElement child) { nodes.add(requireNonNull(child)); }

		public void setNodeArrangement(NodeArrangement nodeArrangement) { this.nodeArrangement = requireNonNull(nodeArrangement); }

		public void forEachNode(Consumer<? super IqlElement> action) { nodes.forEach(requireNonNull(action)); }

	}

	/**
	 * Implementation note: we use {@link IqlStructure} as child type so that
	 * {@link IqlElementDisjunction} and {@link IqlElementSet} are also allowed.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class IqlTreeNode extends IqlNode {

		/**
		 * Children of this tree node, can either be a single (tree)node,
		 * an IqlElementSet or an IqlElementDisjunction.
		 */
		@JsonProperty(IqlProperties.CHILDREN)
		@JsonInclude(Include.NON_ABSENT)
		private Optional<IqlStructure> children = Optional.empty();

		@Override
		public IqlType getType() { return IqlType.TREE_NODE; }

		public Optional<IqlStructure> getChildren() { return children; }

		public void setChildren(IqlStructure children) { this.children = Optional.of(children); }
	}

	public static class IqlEdge extends IqlProperElement {

		@JsonProperty(value=IqlProperties.SOURCE, required=true)
		private IqlNode source;

		@JsonProperty(value=IqlProperties.TARGET, required=true)
		private IqlNode target;

		@JsonProperty(value=IqlProperties.EDGE_TYPE, required=true)
		private EdgeType edgeType;

		@Override
		public IqlType getType() { return IqlType.EDGE; }

		@Override
		public void checkIntegrity() {
			super.checkIntegrity();
			checkNestedNotNull(source, IqlProperties.SOURCE);
			checkNestedNotNull(target, IqlProperties.TARGET);
			checkNotNull(edgeType, IqlProperties.EDGE_TYPE);
//			checkCondition(source.getQuantifiers().isEmpty() || target.getQuantifiers().isEmpty(),
//					"source/target", "Redundant quantifiers on edge - only one of 'source' or 'target' nodes may be assigned a quantifier!");
			//TODO having the quantifier check here causes test issues. is it ok to do that check only in the QueryProcessor
		}

		public IqlNode getSource() { return source; }

		public IqlNode getTarget() { return target; }

		public EdgeType getEdgeType() { return edgeType; }

		public void setSource(IqlNode source) { this.source = requireNonNull(source); }

		public void setTarget(IqlNode target) { this.target = requireNonNull(target); }

		public void setEdgeType(EdgeType edgeType) { this.edgeType = requireNonNull(edgeType); }

		// utility

		private List<IqlQuantifier> quantifiers() {
			if(source.hasQuantifiers()) {
				return source.getQuantifiers();
			}

			return target.getQuantifiers();
		}

		public List<IqlQuantifier> getQuantifiers() { return quantifiers(); }

		public void forEachQuantifier(Consumer<? super IqlQuantifier> action) { quantifiers().forEach(requireNonNull(action)); }

		public boolean hasQuantifiers() {
			return source.hasQuantifiers() || target.hasQuantifiers();
		}

		public boolean isExistentiallyQuantified() {
			if(!hasQuantifiers()) {
				return true;
			}
			for(IqlQuantifier quantifier : quantifiers()) {
				if(quantifier.isExistentiallyQuantified()) {
					return true;
				}
			}
			return false;
		}

		public boolean isUniversallyQuantified() {
			List<IqlQuantifier> quantifiers = quantifiers();
			return quantifiers.size()==1
					&& quantifiers.get(0).getQuantifierType()==QuantifierType.ALL;
		}

		public boolean isNegated() {
			List<IqlQuantifier> quantifiers = quantifiers();
			return quantifiers.size()==1
					&& quantifiers.get(0).isExistentiallyNegated();
		}
	}

	public static class IqlElementDisjunction extends IqlElement {

		@JsonProperty(value=IqlProperties.ALTERNATIVES, required=true)
		private final List<IqlElement> alternatives = new ArrayList<>();

		@Override
		public IqlType getType() { return IqlType.ELEMENT_DISJUNCTION; }

		@Override
		public void checkIntegrity() {
			super.checkIntegrity();
			checkCondition(alternatives.size()>1, "alternatives", "Must have at least 2 alternatives");

			for(int i=0; i<alternatives.size(); i++) {
				checkNestedNotNull(alternatives.get(i), "alternatives["+i+"]");
			}
		}

		public int getAlternativesCount() { return alternatives.size(); }

		public IqlElement getAlternative(int index) { return alternatives.get(index); }

		public void addAlternative(IqlElement alternative) {
			alternatives.add(requireNonNull(alternative));
		}
	}

	public enum EdgeType {
		UNDIRECTED("simple"),
		UNIDIRECTIONAL("one-way"),
		BIDIRECTIONAL("two-way"),
		;

		private final String label;

		private EdgeType(String label) { this.label = label; }

		@JsonValue
		public String getLabel() { return label; }
	}
}
