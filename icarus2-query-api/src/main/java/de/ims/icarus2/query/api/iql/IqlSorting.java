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
public class IqlSorting extends AbstractIqlQueryElement {
	//TODO do we need to make this unique?

	@JsonProperty(value=IqlTags.EXPRESSION, required=true)
	private IqlExpression expression;

	@JsonProperty(value=IqlTags.ORDER, required=true)
	private Order order;

	@Override
	public IqlType getType() { return IqlType.SORTING; }

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
