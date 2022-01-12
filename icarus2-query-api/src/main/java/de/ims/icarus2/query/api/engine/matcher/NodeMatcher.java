/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.engine.matcher;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.query.api.exp.Assignable;
import de.ims.icarus2.query.api.exp.Expression;

/**
 * Matches a single node in the structural constraints of a query
 * against potential target items.
 *
 * @author Markus Gärtner
 *
 */
public class NodeMatcher extends AbstractMatcher<Item> {

	/** Storage to put new items for evaluation */
	private final Assignable<? extends Item> element;
	/** Constraint to be evaluated */
	private final Expression<?> constraint;

	public NodeMatcher(int id, Assignable<? extends Item> element, Expression<?> constraint) {
		super(id);

		requireNonNull(element);
		requireNonNull(constraint);

		checkArgument("Constraint must be of type BOOLEAN", constraint.isBoolean());

		this.constraint = constraint;
		this.element = element;
	}

	@Override
	public boolean matches(long index, Item target) {
		requireNonNull(target);

		// Prepare context for current target item (cheap op)
		element.assign(target);

		// Now evaluate constraint (expensive op)
		boolean result = constraint.computeAsBoolean();

		// Don't keep a reference to target item around!!
		element.clear();

		return result;
	}
}
