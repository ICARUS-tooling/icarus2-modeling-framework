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

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public class IqlPayload extends AbstractIqlQueryElement {

	@JsonProperty(IqlProperties.QUERY_TYPE)
	private QueryType queryType;

	/**
	 * All the bindings to be usable for this query, if defined.
	 */
	@JsonProperty(IqlProperties.BINDINGS)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlBinding> bindings = new ArrayList<>();

	// Either plain or global constraints
	@JsonProperty(IqlProperties.CONSTRAINT)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<IqlConstraint> constraint = Optional.empty();

	@JsonProperty(IqlProperties.ELEMENTS)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlElement> elements = new ArrayList<>();

	@JsonProperty(IqlProperties.ALIGNED)
	@JsonInclude(Include.NON_DEFAULT)
	private boolean aligned = false;

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.PAYLOAD;
	}

	/**
	 * @see de.ims.icarus2.query.api.iql.AbstractIqlQueryElement#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkNotNull(queryType, IqlProperties.QUERY_TYPE);
		checkCondition(!(constraint==null && elements.isEmpty()), "constraint/elements",
				"must either define a global 'constraint' or 'elements'");

		//TODO 'aligned' flag only supported for tree or graph statement

		checkCollection(bindings);
		checkOptionalNested(constraint);
		checkCollection(elements);
	}

	public QueryType getQueryType() { return queryType; }

	public List<IqlBinding> getBindings() { return CollectionUtils.unmodifiableListProxy(bindings); }

	public Optional<IqlConstraint> getConstraint() { return constraint; }

	public List<IqlElement> getElements() { return CollectionUtils.unmodifiableListProxy(elements); }

	public boolean isAligned() { return aligned; }


	public void setQueryType(QueryType queryType) { this.queryType = requireNonNull(queryType); }

	public void addBinding(IqlBinding binding) { bindings.add(requireNonNull(binding)); }

	public void setConstraint(IqlConstraint constraint) { this.constraint = Optional.of(constraint); }

	public void addElement(IqlElement element) { elements.add(requireNonNull(element)); }

	public void setAligned(boolean aligned) { this.aligned = aligned; }

	public enum QueryType {
		/**
		 * Return all elements of the corpus for specified primary layer.
		 * Cannot have any constraints defined at all!
		 */
		ALL("all", false, false, false),
		/** Define basic constraints for bound members */
		PLAIN("plain", false, false, false),
		/** Define sequential flat nodes */
		SEQUENCE("sequence", true, false, false),
		/** Define structural constraints via nested tree nodes */
		TREE("tree", true, true, false),
		/**
		 * Use full power of graph-based constraints via definition
		 * of arbitrary nodes and edges.
		 */
		GRAPH("graph", true, false, true),
		;

		private final String label;
		private final boolean allowNodes, allowChildren, allowEdges;

		private QueryType(String label, boolean allowNodes, boolean allowChildren, boolean allowEdges) {
			this.label = label;
			this.allowNodes = allowNodes;
			this.allowChildren = allowChildren;
			this.allowEdges = allowEdges;
		}

		@JsonValue
		public String getLabel() {
			return label;
		}

		public boolean isAllowNodes() { return allowNodes; }

		public boolean isAllowChildren() { return allowChildren; }

		public boolean isAllowEdges() { return allowEdges; }
	}
}
