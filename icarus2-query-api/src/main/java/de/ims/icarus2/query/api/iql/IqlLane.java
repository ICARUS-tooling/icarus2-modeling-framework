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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.EnumSet;
import java.util.OptionalLong;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.annotations.VisibleForTesting;

import de.ims.icarus2.util.LazyStore;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.strings.StringResource;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

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

	@JsonProperty(value=IqlTags.LIMIT)
	@JsonInclude(Include.NON_ABSENT)
	private OptionalLong limit = OptionalLong.empty();

	@JsonProperty(value=IqlTags.MATCH_FLAG)
	@JsonInclude(Include.NON_EMPTY)
	private final EnumSet<IqlLane.MatchFlag> flags = EnumSet.noneOf(IqlLane.MatchFlag.class);

	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkNotNull(laneType, IqlTags.LANE_TYPE);
		checkNestedNotNull(element, IqlTags.ELEMENT);

		//TODO do we want to enforce the rules given by lane type here already?

		for(IqlLane.MatchFlag flag : flags) {
			for(IqlLane.MatchFlag excluded : flag.getExcluded()) {
				checkCondition(!flags.contains(excluded), IqlTags.MATCH_FLAG,
						String.format("Flag %s excluded by %s", excluded, flag));
			}
		}
	}

	@Override
	public IqlType getType() { return IqlType.LANE; }

	public LaneType getLaneType() { return laneType; }

	public IqlElement getElement() { return element; }

	public OptionalLong getLimit() { return limit; }

	public Set<IqlLane.MatchFlag> getFlags() { return CollectionUtils.unmodifiableSetProxy(flags); }

	public boolean isFlagSet(IqlLane.MatchFlag flag) { return flags.contains(requireNonNull(flag)); }


	public void setLaneType(LaneType laneType) { this.laneType = requireNonNull(laneType); }

	public void setElement(IqlElement element) { this.element = requireNonNull(element); }

	public void setFlag(IqlLane.MatchFlag flag) { flags.add(requireNonNull(flag)); }

	public void unsetFlag(IqlLane.MatchFlag flag) { flags.remove(requireNonNull(flag)); }

	@VisibleForTesting
	void unsetAllFlags() { flags.clear(); }

	public void setLimit(long limit) {
		checkArgument("Limit must be positive", limit>0);
		this.limit = OptionalLong.of(limit);
	}

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

	public enum MatchFlag implements StringResource {
		/** Matches must not share elements in their mappings */
		DISJOINT("disjoint"),
		/** Matches must not horizontally overlap */
		CONSECUTIVE("consecutive"),
		/** Search is meant to start at root nodes */
		ROOTED("rooted"),
		/** Set top-level scan direction to reverse */
		REVERSE("reverse"),
		;

		private final String label;

		private Set<MatchFlag> excluded = Collections.emptySet();

		private MatchFlag(String label) { this.label = label; }

		private void exclude(MatchFlag...flags) {
			Set<MatchFlag> set = new ObjectOpenHashSet<>(flags.length);
			CollectionUtils.feedItems(set, flags);
			excluded = Collections.unmodifiableSet(set);
		}

		@Override
		public String getStringValue() { return label; }

		@JsonValue
		public String getLabel() { return label; }

		public Set<MatchFlag> getExcluded() { return excluded; }

		private static final LazyStore<MatchFlag, String> store =
				LazyStore.forStringResource(MatchFlag.class, true);

		public static MatchFlag parse(String s) {
			return store.lookup(s);
		}

		static {
			DISJOINT.exclude(CONSECUTIVE, ROOTED);
			CONSECUTIVE.exclude(ROOTED, DISJOINT);
			ROOTED.exclude(DISJOINT, CONSECUTIVE);
		}
	}
}
