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

import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public abstract class IqlMarker extends AbstractIqlQueryElement {

	public static class IqlMarkerCall extends IqlMarker {

		@JsonProperty(value=IqlTags.NAME, required=true)
		@JsonInclude(Include.NON_EMPTY)
		private String name;

		@JsonProperty(IqlTags.ARGUMENTS)
		@JsonInclude(Include.NON_DEFAULT)
		private Number[] arguments = EMPTY;

		private static final Number[] EMPTY = {};

		@Override
		public IqlType getType() { return IqlType.MARKER_CALL; }

		@Override
		public void checkIntegrity() {
			super.checkIntegrity();

			checkStringNotEmpty(name, IqlTags.NAME);
		}

		public String getName() { return name; }

		public int getArgumentCount() { return arguments.length; }

		public Object getArgument(int index) { return arguments[index]; }

		public void setName(String name) { this.name = checkNotEmpty(name); }

		public void setArguments(Number[] arguments) { this.arguments = requireNonNull(arguments).clone(); }

	}

	public static class IqlMarkerExpression extends IqlMarker {

		public static IqlMarkerExpression and(Iterable<? extends IqlMarker> items) {
			IqlMarkerExpression exp = new IqlMarkerExpression();
			exp.setExpressionType(MarkerExpressionType.CONJUNCTION);
			items.forEach(exp::addItem);
			return exp;
		}

		public static IqlMarkerExpression or(Iterable<? extends IqlMarker> items) {
			IqlMarkerExpression exp = new IqlMarkerExpression();
			exp.setExpressionType(MarkerExpressionType.DISJUNCTION);
			items.forEach(exp::addItem);
			return exp;
		}

		@JsonProperty(IqlTags.ITEMS)
		@JsonInclude(Include.NON_EMPTY)
		private final List<IqlMarker> items = new ArrayList<>();

		@JsonProperty(value=IqlTags.EXPRESSION_TYPE, required=true)
		private MarkerExpressionType expressionType;

		@Override
		public IqlType getType() { return IqlType.MARKER_EXPRESSION; }

		@Override
		public void checkIntegrity() {
			super.checkIntegrity();
			checkCondition(items.size()>1, IqlTags.ITEMS, "Must have at least 2 items");

			checkCollection(items);
			checkNotNull(expressionType, IqlTags.EXPRESSION_TYPE);
		}

		public List<IqlMarker> getItems() { return CollectionUtils.unmodifiableListProxy(items); }

		public MarkerExpressionType getExpressionType() { return expressionType; }

		public void setExpressionType(MarkerExpressionType expressionType) { this.expressionType = requireNonNull(expressionType); }

		public void addItem(IqlMarker item) { items.add(requireNonNull(item)); }

		public void forEachItem(Consumer<? super IqlMarker> action) { items.forEach(action); }
	}

	public static enum MarkerExpressionType {
		CONJUNCTION("conjunction"),
		DISJUNCTION("disjunction"),
		;

		private final String label;
		private MarkerExpressionType(String label) {
			this.label = checkNotEmpty(label);
		}

		@JsonValue
		public String getLabel() {
			return label;
		}
	}
}
