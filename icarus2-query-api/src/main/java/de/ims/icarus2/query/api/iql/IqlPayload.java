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
public class IqlPayload implements IqlQueryElement {

	@JsonProperty(IqlProperties.QUERY_TYPE)
	public QueryType queryType;

	/**
	 * All the bindings to be usable for this query, if defined.
	 */
	@JsonProperty(IqlProperties.BINDINGS)
	public List<IqlBinding> bindings = new ArrayList<>();

	// Either plain or global constraints
	@JsonProperty(IqlProperties.CONSTRAINT)
	public IqlConstraint constraint;

	@JsonProperty(IqlProperties.ELEMENTS)
	public List<IqlElement> elements = new ArrayList<>();

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.PAYLOAD;
	}

	public enum QueryType {
		/**
		 * Return all elements of the corpus for specified primary layer.
		 * Cannot have any constraints defined at all!
		 */
		ALL("all"),
		/** Define basic constraints for bound members */
		PLAIN("plain"),
		/** Define sequential flat nodes */
		SEQUENCE("sequence"),
		/** Define structural constraints via nested tree nodes */
		TREE("tree"),
		/**
		 * Use full power of graph-based constraints via definition
		 * of arbitrary nodes and edges.
		 */
		GRAPH("graph"),
		;

		private final String label;

		private QueryType(String label) {
			this.label = label;
		}

		@JsonValue
		public String getLabel() {
			return label;
		}
	}
}
