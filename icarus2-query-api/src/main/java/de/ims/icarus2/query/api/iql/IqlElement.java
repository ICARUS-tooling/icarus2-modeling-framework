/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public abstract class IqlElement extends IqlUnique {

	@JsonProperty(IqlTags.CONSUMED)
	@JsonInclude(Include.NON_DEFAULT)
	private boolean consumed = false;

	/** SIgnals whether or not the element has been consumed during a matching process. */
	public boolean isConsumed() { return consumed; }

	public void setConsumed(boolean consumed) { this.consumed = consumed; }

	/** Indicates the minimal size in nodes that the element can cover, not including quantification. */
	public abstract int length();

	public static abstract class IqlProperElement extends IqlElement {
		//TODO add MAPPING_ID field for assigning the ids used for mapping in Match  results

		@JsonProperty(IqlTags.MAPPING_ID)
		@JsonInclude(Include.NON_DEFAULT)
		private int mappingId = UNSET_INT;

		@JsonProperty(IqlTags.LABEL)
		@JsonInclude(Include.NON_ABSENT)
		private Optional<String> label = Optional.empty();

		@JsonProperty(IqlTags.CONSTRAINT)
		@JsonInclude(Include.NON_ABSENT)
		private Optional<IqlConstraint> constraint = Optional.empty();

		public int getMappingId() { return mappingId; }

		public Optional<String> getLabel() { return label; }

		public Optional<IqlConstraint> getConstraint() { return constraint; }

		public void setLabel(String label) { this.label = Optional.of(checkNotEmpty(label)); }

		public void setConstraint(IqlConstraint constraint) { this.constraint = Optional.of(constraint); }

		public void setMappingId(int mappingId) { this.mappingId = mappingId; }

		@Override
		public void checkIntegrity() {
			super.checkIntegrity();

			// Mapping id can be undefined, in which case the engine will assign it
//			checkCondition(mappingId!=UNSET_INT, IqlTags.MAPPING_ID, "Mapping id not set");
			checkOptionalStringNotEmpty(label, IqlTags.LABEL);
			checkOptionalNested(constraint);
		}

	}

	/**
	 * Acts as a scope around a {@link IqlElement element} to
	 * provide quantification or simply grouping them together for surrounding
	 * query features.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class IqlGrouping extends IqlElement implements IqlQuantifier.Quantifiable {

		@JsonProperty(IqlTags.QUANTIFIERS)
		@JsonInclude(Include.NON_EMPTY)
		private final List<IqlQuantifier> quantifiers = new ArrayList<>();

		@JsonProperty(value=IqlTags.ELEMENT, required=true)
		private IqlElement element;

		@Override
		public IqlType getType() { return IqlType.GROUPING; }

		@Override
		public void checkIntegrity() {
			super.checkIntegrity();

			checkCollection(quantifiers);
			checkNotNull(element, IqlTags.ELEMENT);
		}

		@Override
		public int length() { return element.length(); }

		public IqlElement getElement() { return element; }

		@Override
		public List<IqlQuantifier> getQuantifiers() { return CollectionUtils.unmodifiableListProxy(quantifiers); }

		@Override
		public void addQuantifier(IqlQuantifier quantifier) { quantifiers.add(requireNonNull(quantifier)); }

		@Override
		public void forEachQuantifier(Consumer<? super IqlQuantifier> action) { quantifiers.forEach(requireNonNull(action)); }

		public void setElement(IqlElement element) { this.element = requireNonNull(element); }

		// utility

		@Override
		public boolean hasQuantifiers() { return !quantifiers.isEmpty(); }
	}

	/**
	 * Plain or graph node with quantification and markers.
	 * Inherits label and local constraints.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class IqlNode extends IqlProperElement implements IqlQuantifier.Quantifiable  {

		@JsonProperty(IqlTags.QUANTIFIERS)
		@JsonInclude(Include.NON_EMPTY)
		private final List<IqlQuantifier> quantifiers = new ArrayList<>();

		@JsonProperty(IqlTags.MARKER)
		@JsonInclude(Include.NON_ABSENT)
		private Optional<IqlMarker> marker = Optional.empty();

		@Override
		public IqlType getType() { return IqlType.NODE; }

		@Override
		public void checkIntegrity() {
			super.checkIntegrity();

			checkCollection(quantifiers);
		}

		@Override
		public int length() { return 1; }

		public Optional<IqlMarker> getMarker() { return marker; }

		@Override
		public List<IqlQuantifier> getQuantifiers() { return CollectionUtils.unmodifiableListProxy(quantifiers); }

		public void setMarker(IqlMarker marker) { this.marker = Optional.of(marker); }

		@Override
		public void addQuantifier(IqlQuantifier quantifier) { quantifiers.add(requireNonNull(quantifier)); }

		@Override
		public void forEachQuantifier(Consumer<? super IqlQuantifier> action) { quantifiers.forEach(requireNonNull(action)); }

		// utility

		@Override
		public boolean hasQuantifiers() { return !quantifiers.isEmpty(); }
	}

	/**
	 * Orders a collection of nodes according to a specific arrangement.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class IqlSequence extends IqlElement {

		@JsonProperty(IqlTags.ELEMENTS)
		@JsonInclude(Include.NON_EMPTY)
		private final List<IqlElement> elements = new ArrayList<>();

		@JsonProperty(IqlTags.ARRANGEMENTS)
		@JsonInclude(Include.NON_EMPTY)
		private Set<NodeArrangement> nodeArrangements = new HashSet<>();

		@Override
		public IqlType getType() { return IqlType.SEQUENCE; }

		@Override
		public void checkIntegrity() {
			super.checkIntegrity();

			checkCollectionNotEmpty(elements, IqlTags.ELEMENTS);
		}

		@Override
		public int length() { return elements.size(); }

		public List<IqlElement> getElements() { return CollectionUtils.unmodifiableListProxy(elements); }

		public Set<NodeArrangement> getArrangements() { return CollectionUtils.unmodifiableSetProxy(nodeArrangements); }


		public void addElement(IqlElement child) { elements.add(requireNonNull(child)); }

		public void addArrangement(NodeArrangement nodeArrangement) { nodeArrangements.add(requireNonNull(nodeArrangement)); }

		public void forEachElement(Consumer<? super IqlElement> action) { elements.forEach(requireNonNull(action)); }

		public boolean hasArrangement(NodeArrangement arrangement) { return nodeArrangements.contains(arrangement); }
	}

	/**
	 * Adds the ability of nesting to {@link IqlNode}.
	 *
	 * Implementation note: we use {@link IqlElement} as child type so that
	 * {@link IqlElementDisjunction} and {@link IqlSequence} are also allowed.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class IqlTreeNode extends IqlNode {

		/**
		 * Children of this tree node, can either be a single (tree)node,
		 * an IqlSequence or an IqlElementDisjunction.
		 */
		@JsonProperty(IqlTags.CHILDREN)
		@JsonInclude(Include.NON_ABSENT)
		private Optional<IqlElement> children = Optional.empty();

		@Override
		public void checkIntegrity() {
			super.checkIntegrity();
			checkOptionalNested(children);
		}

		//TODO add cached size information and override length() method

		@Override
		public IqlType getType() { return IqlType.TREE_NODE; }

		public Optional<IqlElement> getChildren() { return children; }

		public void setChildren(IqlElement children) { this.children = Optional.of(children); }
	}

	/**
	 * Connection between a source and target node with a specified connection type.
	 * Inherits label and local constraints.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class IqlEdge extends IqlProperElement implements IqlQuantifier.Quantifiable {

		@JsonProperty(value=IqlTags.SOURCE, required=true)
		private IqlNode source;

		@JsonProperty(value=IqlTags.TARGET, required=true)
		private IqlNode target;

		@JsonProperty(value=IqlTags.EDGE_TYPE, required=true)
		private EdgeType edgeType;

		@Override
		public IqlType getType() { return IqlType.EDGE; }

		@Override
		public void checkIntegrity() {
			super.checkIntegrity();
			checkNestedNotNull(source, IqlTags.SOURCE);
			checkNestedNotNull(target, IqlTags.TARGET);
			checkNotNull(edgeType, IqlTags.EDGE_TYPE);
//			checkCondition(source.getQuantifiers().isEmpty() || target.getQuantifiers().isEmpty(),
//					"source/target", "Redundant quantifiers on edge - only one of 'source' or 'target' nodes may be assigned a quantifier!");
			//TODO having the quantifier check here causes test issues. is it ok to do that check only in the QueryProcessor
		}

		@Override
		public int length() { return source.length() + target.length(); }

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

		@Override
		public List<IqlQuantifier> getQuantifiers() { return quantifiers(); }

		@Override
		public void forEachQuantifier(Consumer<? super IqlQuantifier> action) { quantifiers().forEach(requireNonNull(action)); }

		@Override
		public boolean hasQuantifiers() {
			return source.hasQuantifiers() || target.hasQuantifiers();
		}

		@Override
		public void addQuantifier(IqlQuantifier quantifier) {
			throw new QueryException(QueryErrorCode.INCORRECT_USE,
					"Cannot directly add quantifier to "+getClass().getSimpleName()
					+" - use one of source or target to attach quantifiers!");
		}
	}

	/**
	 * Logical connective to disjunctively group a collection of {@link IqlElement element}.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class IqlElementDisjunction extends IqlElement {

		@JsonProperty(value=IqlTags.ALTERNATIVES, required=true)
		private final List<IqlElement> alternatives = new ArrayList<>();

		private transient int size = UNSET_INT;

		@Override
		public IqlType getType() { return IqlType.DISJUNCTION; }

		@Override
		public void checkIntegrity() {
			super.checkIntegrity();
			checkCondition(alternatives.size()>1, IqlTags.ALTERNATIVES, "Must have at least 2 alternatives");

			for(int i=0; i<alternatives.size(); i++) {
				checkNestedNotNull(alternatives.get(i), "alternatives["+i+"]");
			}
		}

		@Override
		public int length() {
			if(size==UNSET_INT && !alternatives.isEmpty()) {
				int tmp = Integer.MAX_VALUE;
				for(IqlElement element : alternatives) {
					tmp = Math.min(tmp, element.length());
				}
				size = tmp;
			}
			return size==UNSET_INT ? 0 : size;
		}

		public List<IqlElement> getAlternatives() { return CollectionUtils.unmodifiableListProxy(alternatives); }

		public void addAlternative(IqlElement alternative) {
			alternatives.add(requireNonNull(alternative));
			size = UNSET_INT;
		}
	}

//	public enum IqlElementType {
//		GROUPING,
//		NODE,
//		SEQUENCE,
//		TREE_NODE,
//		EDGE,
//		DISJUNCTION,
//		;
//	}

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
