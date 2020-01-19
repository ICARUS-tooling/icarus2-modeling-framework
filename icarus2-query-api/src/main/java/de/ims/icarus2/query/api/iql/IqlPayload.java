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
				"must either define a global 'csontraint' or 'elements'");

		checkCollection(bindings);
		checkOptionalNested(constraint);
		checkCollection(elements);
	}

	public QueryType getQueryType() { return queryType; }

	public List<IqlBinding> getBindings() { return CollectionUtils.unmodifiableListProxy(bindings); }

	public Optional<IqlConstraint> getConstraint() { return constraint; }

	public List<IqlElement> getElements() { return CollectionUtils.unmodifiableListProxy(elements); }

	public void setQueryType(QueryType queryType) { this.queryType = requireNonNull(queryType); }

	public void addBinding(IqlBinding binding) { bindings.add(requireNonNull(binding)); }

	public void setConstraint(IqlConstraint constraint) { this.constraint = Optional.of(constraint); }

	public void addElement(IqlElement element) { elements.add(requireNonNull(element)); }

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
