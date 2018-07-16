/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * $Revision: 457 $
 *
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
