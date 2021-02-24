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
package de.ims.icarus2.model.api.members.item;

import javax.annotation.Nullable;

import de.ims.icarus2.apiguard.Api;
import de.ims.icarus2.apiguard.Unguarded;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.structure.Structure;

/**
 * Specifies a member of a {@code Structure} object. In addition to being
 * a simple {@link Item}, an {@code Edge} consists of a {@code source}
 * and {@code target} item.
 *
 * @author Markus Gärtner
 *
 */
@Api
public interface Edge extends Item {

	/**
	 * Returns the host structure of this edge. Note that this method should
	 * return the same object as {@link #getContainer()}!
	 *
	 * @return
	 */
	@Nullable Structure getStructure();

	/**
	 * {@inheritDoc}
	 * <p>
	 * In addition to the directly set flag value an edge will only be considered
	 * alive if both its terminals are {@code non-null}.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#isAlive()
	 */
	@Unguarded("Liveness depends on the values of terminals")
	@Override
	boolean isAlive();

	/**
	 * An edge cannot be locked individually, so this method must always return {@code false}.
	 * <p>
	 * We do not declare a default implementation here, as the
	 * expected override hierarchy passes by the {@link Item} class which
	 * allows locking. Therefore every {@link Edge} implementation has to
	 * explicitly override this method.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#isLocked()
	 */
	@Unguarded("Edges are never locked, so return value is constant")
	@Override
	boolean isLocked();

	/**
	 * {@inheritDoc}
	 * <p>
	 * In addition to the directly set flag value an edge will automatically be
	 * considered dirty as soon as at least one of its terminals is {@code null}.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#isDirty()
	 */
	@Unguarded("Dirtyness depends on the values of terminals")
	@Override
	boolean isDirty();

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getContainer()
	 */
	@Override
	default @Nullable Container getContainer() {
		return getStructure();
	}

	/**
	 * Always returns {@code false}.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#isTopLevel()
	 */
	@Override
	default boolean isTopLevel() {
		return false;
	}

	@Nullable Item getSource();

	@Nullable Item getTarget();

	default @Nullable Item getTerminal(boolean isSource) {
		return isSource ? getSource() : getTarget();
	}

	/**
	 * Returns whether or not this edge describes a close loop,
	 * meaning that {@link #getSource() source} and {@link #getTarget() target}
	 * node are the same.
	 *
	 * @return
	 */
	default boolean isLoop() {
		return getSource()==getTarget();
	}

	// Modification methods

	void setSource(@Nullable Item item);

	void setTarget(@Nullable Item item);

	default void setTerminal(@Nullable Item item, boolean isSource) {
		if(isSource) {
			setSource(item);
		} else {
			setTarget(item);
		}
	}
}
