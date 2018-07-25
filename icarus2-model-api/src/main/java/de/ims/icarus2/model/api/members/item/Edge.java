/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
public interface Edge extends Item {

	/**
	 * Returns the host structure of this edge. Note that this method should
	 * return the same object as {@link #getContainer()}!
	 *
	 * @return
	 */
	Structure getStructure();

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getContainer()
	 */
	@Override
	default Container getContainer() {
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

	Item getSource();

	Item getTarget();

	default Item getTerminal(boolean isSource) {
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

	void setSource(Item item);

	void setTarget(Item item);

	default void setTerminal(Item item, boolean isSource) {
		if(isSource) {
			setSource(item);
		} else {
			setTarget(item);
		}
	}
}
