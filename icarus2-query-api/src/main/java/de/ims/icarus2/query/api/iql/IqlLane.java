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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public class IqlLane extends IqlNamedReference {

	/**
	 * Special name reserved for signaling that a lane is introduced as a proxy,
	 * i.e. the original query did not contain a lane statement, but only the
	 * elements and/or constraints.
	 */
	//TODO need a better mechanism for indicating proxy state
	public static final String PROXY_NAME = "lane_proxy";

	@JsonProperty(value=IqlProperties.LANE_TYPE, required=true)
	private LaneType laneType;

	@JsonProperty(value=IqlProperties.ELEMENTS, required=true)
	private final List<IqlElement> elements = new ArrayList<>();

	@JsonProperty(IqlProperties.NODE_ARRANGEMENT)
	@JsonInclude(Include.NON_DEFAULT)
	private NodeArrangement nodeArrangement = NodeArrangement.UNSPECIFIED;

	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkNotNull(laneType, IqlProperties.LANE_TYPE);
		//TODO 'nodeArrangement' flag only supported for tree or graph statement

		checkCollectionNotEmpty(elements, IqlProperties.ELEMENTS);
	}

	@Override
	public IqlType getType() { return IqlType.LANE; }

	public LaneType getLaneType() { return laneType; }

	public List<IqlElement> getElements() { return CollectionUtils.unmodifiableListProxy(elements); }

	public NodeArrangement getNodeArrangement() { return nodeArrangement; }


	public void setLaneType(LaneType laneType) { this.laneType = requireNonNull(laneType); }

	public void addElement(IqlElement element) { elements.add(requireNonNull(element)); }

	public void setNodeArrangement(NodeArrangement nodeArrangement) { this.nodeArrangement = requireNonNull(nodeArrangement); }

	/** Returns {@code true} iff this lane has been assigned the {@link #PROXY_NAME proxy name} */
	public boolean isProxy() {
		return PROXY_NAME.equals(getName());
	}

	public enum LaneType {
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

		private LaneType(String label, boolean allowNodes, boolean allowChildren, boolean allowEdges) {
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

	public enum NodeArrangement {
		UNSPECIFIED("unspecified"),
		ORDERED("ordered"),
		ADJACENT("adjacent")
		;

		private final String label;

		private NodeArrangement(String label) { this.label = label; }

		@JsonValue
		public String getLabel() { return label; }
	}
}
