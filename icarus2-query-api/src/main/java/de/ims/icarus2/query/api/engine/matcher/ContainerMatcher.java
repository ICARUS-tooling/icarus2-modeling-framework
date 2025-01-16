/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.query.api.exp.Assignable;
import de.ims.icarus2.query.api.exp.Expression;

/**
 * Matches a single UoI (typically a {@link Container})
 * as either filter or global constraint.
 *
 * @author Markus Gärtner
 *
 */
public class ContainerMatcher extends AbstractMatcher<Container> {

	/** Storage to put new items for evaluation */
	private final Assignable<? extends Container> element;
	/** Constraint to be evaluated */
	private final Expression<?> constraint;

	public ContainerMatcher(Assignable<? extends Container> lane, Expression<?> constraint) {
		super(UNSET_INT);

		requireNonNull(lane);
		requireNonNull(constraint);

		checkArgument("Constraint must be of type BOOLEAN", constraint.isBoolean());

		this.constraint = constraint;
		this.element = lane;
	}

	@Override
	public boolean matches(long index, Container target) {
		requireNonNull(target);

		// Prepare context for current target container (cheap op)
		element.assign(target);

		// Now evaluate constraint (expensive op)
		return constraint.computeAsBoolean();
	}

	public void reset() {
		element.clear();
	}
}
