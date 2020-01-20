/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalLong;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public class IqlResult extends AbstractIqlQueryElement {

	@JsonProperty(value=IqlProperties.RESULT_TYPES)
	private final Set<ResultType> resultTypes = new HashSet<>();

	/**
	 * The processed result instructions if
	 * {@link #rawResultInstructions} was set.
	 */
	@JsonProperty(IqlProperties.PROCESSED_RESULT_INSTRUCTIONS)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlResultInstruction> processedResultInstructions = new ArrayList<>();

	@JsonProperty(IqlProperties.LIMIT)
	@JsonInclude(Include.NON_ABSENT)
	private OptionalLong limit = OptionalLong.empty();

	@JsonProperty(IqlProperties.SORTINGS)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlSorting> sortings = new ArrayList<>();

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.RESULT;
	}

	/**
	 * @see de.ims.icarus2.query.api.iql.AbstractIqlQueryElement#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkCondition(!resultTypes.isEmpty(), "resultTypes", "Must define at elast 1 result type");

		checkCollection(processedResultInstructions);
		checkCollection(sortings);
	}

	public List<IqlResultInstruction> getProcessedResultInstructions() { return CollectionUtils.unmodifiableListProxy(processedResultInstructions); }

	public Set<ResultType> getResultTypes() { return CollectionUtils.unmodifiableSetProxy(resultTypes); }

	public OptionalLong getLimit() { return limit; }

	public List<IqlSorting> getSortings() { return CollectionUtils.unmodifiableListProxy(sortings); }

	public void setLimit(long limit) { checkArgument(limit>0); this.limit = OptionalLong.of(limit); }

	public void addResultInstruction(IqlResultInstruction instruction) { processedResultInstructions.add(requireNonNull(instruction)); }

	public void addResultType(ResultType resultType) { resultTypes.add(requireNonNull(resultType)); }

	public void addSorting(IqlSorting sorting) { sortings.add(requireNonNull(sorting)); }

	public enum ResultType {

		KWIC("kwic", "Simple 'keyword in context' result info with customizable window size"),
		CUSTOM("custom", "Only the user defined result scheme is to be used"),
		;

		private final String label, description;

		private ResultType(String label, String description) {
			this.label = label;
			this.description = description;
		}

		@JsonValue
		public String getLabel() {
			return label;
		}

		public String getDescription() {
			return description;
		}
	}
}
