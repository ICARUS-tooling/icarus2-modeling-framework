/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static de.ims.icarus2.util.Conditions.checkNotEmpty;
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
public class IqlLane extends IqlUnique {

	@JsonProperty(IqlProperties.LANE_TYPE)
	private LaneType laneType;

	@JsonProperty(IqlProperties.NAME)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<String> name = Optional.empty();

	@JsonProperty(IqlProperties.ELEMENTS)
	private final List<IqlElement> elements = new ArrayList<>();

	@JsonProperty(IqlProperties.ALIGNED)
	@JsonInclude(Include.NON_DEFAULT)
	private boolean aligned = false;

	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkNotNull(laneType, IqlProperties.LANE_TYPE);
		checkOptionalStringNotEmpty(name, IqlProperties.NAME);
		//TODO 'aligned' flag only supported for tree or graph statement

		checkCollectionNotEmpty(elements, IqlProperties.ELEMENTS);
	}

	@Override
	public IqlType getType() { return IqlType.LANE; }

	public LaneType getLaneType() { return laneType; }

	public Optional<String> getName() { return name; }

	public List<IqlElement> getElements() { return CollectionUtils.unmodifiableListProxy(elements); }

	public boolean isAligned() { return aligned; }


	public void setLaneType(LaneType laneType) { this.laneType = requireNonNull(laneType); }

	public void setName(String name) { this.name = Optional.of(checkNotEmpty(name)); }

	public void addElement(IqlElement element) { elements.add(requireNonNull(element)); }

	public void setAligned(boolean aligned) { this.aligned = aligned; }

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
