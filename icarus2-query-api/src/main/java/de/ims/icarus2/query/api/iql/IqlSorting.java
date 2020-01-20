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
public class IqlSorting extends AbstractIqlQueryElement {

	@JsonProperty(IqlProperties.EXPRESSION)
	private IqlExpression expression;

	@JsonProperty(IqlProperties.ORDER)
	private Order order;

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.SORTING;
	}

	/**
	 * @see de.ims.icarus2.query.api.iql.AbstractIqlQueryElement#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkNotNull(order, "order");
		checkNestedNotNull(expression, "expression");
	}

	public IqlExpression getExpression() { return expression; }

	public Order getOrder() { return order; }

	public void setExpression(IqlExpression expression) { this.expression = requireNonNull(expression); }

	public void setOrder(Order order) { this.order = requireNonNull(order); }

	public enum Order {
		ASCENDING("asc"),
		DESCENDING("desc"),
		;

		private final String label;

		private Order(String label) { this.label = label; }

		@JsonValue
		public String getLabel() { return label; }
	}
}
