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

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Markus Gärtner
 *
 */
public class IqlLane extends IqlAliasedReference {

	//TODO document that getName() will return the name of the layer (alias) to which this lane is bound

	/**
	 * Special name reserved for signaling that a lane is introduced as a proxy,
	 * i.e. the original query did not contain a lane statement, but only the
	 * element and/or constraints.
	 */
	//TODO need a better mechanism for indicating proxy state
	public static final String PROXY_NAME = "lane_proxy";

	@JsonProperty(value=IqlTags.LANE_TYPE, required=true)
	private LaneType laneType;

	@JsonProperty(value=IqlTags.ELEMENT, required=true)
	private IqlElement element;

	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkNotNull(laneType, IqlTags.LANE_TYPE);
		checkNestedNotNull(element, IqlTags.ELEMENT);
	}

	@Override
	public IqlType getType() { return IqlType.LANE; }

	public LaneType getLaneType() { return laneType; }

	public IqlElement getElement() { return element; }


	public void setLaneType(LaneType laneType) { this.laneType = requireNonNull(laneType); }

	public void setElement(IqlElement element) { this.element = requireNonNull(element); }

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
}
