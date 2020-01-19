/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static java.util.Objects.requireNonNull;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Markus Gärtner
 *
 */
public class IqlGroup extends IqlUnique {

	@JsonProperty(IqlProperties.GROUP_BY)
	private IqlExpression groupBy;

	@JsonProperty(IqlProperties.FILTER_ON)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<IqlExpression> filterOn = Optional.empty();

	@JsonProperty(IqlProperties.LABEL)
	private String label;

	@JsonProperty(IqlProperties.DEFAULT_VALUE)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<Object> defaultValue = Optional.empty();

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.GROUP;
	}

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlUnique#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkStringNotEmpty(label, IqlProperties.LABEL);
		checkNestedNotNull(groupBy, IqlProperties.GROUP_BY);

		checkOptionalNested(filterOn);
	}

	public IqlExpression getGroupBy() { return groupBy; }

	public Optional<IqlExpression> getFilterOn() { return filterOn; }

	public String getLabel() { return label; }

	public Optional<Object> getDefaultValue() { return defaultValue; }

	public void setGroupBy(IqlExpression groupBy) { this.groupBy = requireNonNull(groupBy); }

	public void setFilterOn(IqlExpression filterOn) { this.filterOn = Optional.of(filterOn); }

	public void setLabel(String label) { this.label = checkNotEmpty(label); }

	public void setDefaultValue(Object defaultValue) { this.defaultValue = Optional.of(defaultValue); }

}
