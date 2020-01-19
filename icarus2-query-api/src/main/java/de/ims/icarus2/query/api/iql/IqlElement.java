/**
 *
 */
package de.ims.icarus2.query.api.iql;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Markus Gärtner
 *
 */
public abstract class IqlElement extends IqlUnique {

	@JsonProperty(IqlProperties.LABEL)
	public String label;

	@JsonProperty(IqlProperties.CONSTRAINT)
	public IqlConstraint constraint;

	public static class IqlNode extends IqlElement {

		@JsonProperty(IqlProperties.QUANTIFIERS)
		public List<IqlQuantifier> quantifiers;

		/**
		 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
		 */
		@Override
		public IqlType getType() {
			return IqlType.NODE;
		}
	}

	public static class IqlTreeNode extends IqlNode {

		@JsonProperty(IqlProperties.CHILDREN)
		public List<IqlTreeNode> children = new ArrayList<>();

		/**
		 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
		 */
		@Override
		public IqlType getType() {
			return IqlType.TREE_NODE;
		}
	}

	public static class IqlEdge extends IqlElement {

		@JsonProperty(IqlProperties.SOURCE)
		public IqlNode source;

		@JsonProperty(IqlProperties.TARGET)
		public IqlNode target;

		@JsonProperty(IqlProperties.EDGE_TYPE)
		public EdgeType edgeType;

		/**
		 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
		 */
		@Override
		public IqlType getType() {
			return IqlType.EDGE;
		}
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
