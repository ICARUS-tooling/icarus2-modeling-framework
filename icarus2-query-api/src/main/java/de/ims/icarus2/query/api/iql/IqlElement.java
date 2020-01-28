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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

		/**
		 * @see de.ims.icarus2.query.api.iql.IqlUnique#checkIntegrity()
		 */
		@Override
		public void checkIntegrity() {
			super.checkIntegrity();

			checkOptionalStringNotEmpty(label, IqlProperties.LABEL);
			checkOptionalNested(constraint);
		}

	}

	public static class IqlNode extends IqlProperElement {

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

		// utility

		@JsonIgnore
		public boolean hasQuantifiers() {
			return !quantifiers.isEmpty();
		}

		@JsonIgnore
		public boolean isExistentiallyQuantified() {
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

		@JsonIgnore
		public boolean isUniversallyQuantified() {
			return quantifiers.size()==1
					&& quantifiers.get(0).getQuantifierType()==QuantifierType.ALL;
		}

		@JsonIgnore
		public boolean isNegated() {
			return quantifiers.size()==1
					&& quantifiers.get(0).isExistentiallyNegated();
		}
	}

	/**
	 * Implementation note: we use {@link IqlElement} as child type so that
	 * {@link IqlElementDisjunction} is also allowed.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class IqlTreeNode extends IqlNode {

		@JsonProperty(IqlProperties.CHILDREN)
		@JsonInclude(Include.NON_EMPTY)
		private List<IqlElement> children = new ArrayList<>();

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

		public List<IqlElement> getChildren() { return CollectionUtils.unmodifiableListProxy(children); }

		public void addChild(IqlElement child) { children.add(requireNonNull(child)); }

		public void forEachChild(Consumer<? super IqlElement> action) { children.forEach(requireNonNull(action)); }
	}

	public static class IqlEdge extends IqlProperElement {

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

		@JsonIgnore
		private List<IqlQuantifier> quantifiers() {
			if(source.hasQuantifiers()) {
				return source.getQuantifiers();
			}

			return target.getQuantifiers();
		}

		@JsonIgnore
		public List<IqlQuantifier> getQuantifiers() { return quantifiers(); }

		public void forEachQuantifier(Consumer<? super IqlQuantifier> action) { quantifiers().forEach(requireNonNull(action)); }

		@JsonIgnore
		public boolean hasQuantifiers() {
			return source.hasQuantifiers() || target.hasQuantifiers();
		}

		@JsonIgnore
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

		@JsonIgnore
		public boolean isUniversallyQuantified() {
			List<IqlQuantifier> quantifiers = quantifiers();
			return quantifiers.size()==1
					&& quantifiers.get(0).getQuantifierType()==QuantifierType.ALL;
		}

		@JsonIgnore
		public boolean isNegated() {
			List<IqlQuantifier> quantifiers = quantifiers();
			return quantifiers.size()==1
					&& quantifiers.get(0).isExistentiallyNegated();
		}
	}

	public static class IqlElementDisjunction extends IqlElement {

		@JsonProperty(IqlProperties.ALTERNATIVES)
		private final List<List<IqlElement>> alternatives = new ArrayList<>();

		/**
		 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
		 */
		@Override
		public IqlType getType() {
			return IqlType.ELEMENT_DISJUNCTION;
		}

		/**
		 * @see de.ims.icarus2.query.api.iql.IqlUnique#checkIntegrity()
		 */
		@Override
		public void checkIntegrity() {
			super.checkIntegrity();
			checkCondition(alternatives.size()>1, "alternatives", "Must have at least 2 alternatives");

			for(int i=0; i<alternatives.size(); i++) {
				checkCollectionNotEmpty(alternatives.get(i), "alternatives["+i+"]");
			}
		}

		public int getAlternativesCount() { return alternatives.size(); }

		public List<IqlElement> getAlternative(int index) { return CollectionUtils.unmodifiableListProxy(alternatives.get(index)); }

		public void addAlternative(List<? extends IqlElement> items) {
			requireNonNull(items);
			checkArgument("Alternative must not be empty", !items.isEmpty());

			alternatives.add(new ArrayList<>(items));
		}
	}

	public enum EdgeType {
		UNDIRECTED("simple"),
		UNIDIRECTIONAL("one-way"),
		BIDIRECTIONAL("two-way"),
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
