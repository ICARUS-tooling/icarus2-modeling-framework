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
package de.ims.icarus2.util;

import javax.swing.Icon;

import de.ims.icarus2.util.id.Identity;

/**
 * @author Markus Gärtner
 *
 */
public enum EditOperation implements Identity {

	/**
	 * Describes the adding of a new element either
	 * at the end of a collection or as its first
	 * element. This is essentially the operation of
	 * appending an element to either the head or tail
	 * of a list.
	 */
	ADD("Add", "Add an element before the first or after the last position"), //$NON-NLS-1$

	/**
	 * Adding an element is supported at every random
	 * position within the collection.
	 */
	ADD_RANDOM("Add (Random Access)", "Add an element at an arbitrary position"), //$NON-NLS-1$

	/**
	 * Removal of an element is only possible on one of
	 * the two ends of a the list.
	 */
	REMOVE("Remove", "Remove an element from the first or last position"), //$NON-NLS-1$

	/**
	 * Any element in the list can be removed at any time
	 */
	REMOVE_RANDOM("Remove (Random Access)", "Remove an element from an arbitrary position"), //$NON-NLS-1$

	/**
	 * All elements can be removed with one atomic operation
	 */
	CLEAR("Clear", "Clear the entire storage (removes all elements)"), //$NON-NLS-1$

	/**
	 * An element can be moved within the collection between
	 * random positions.
	 */
	MOVE("Move", "Change the position of an element within the storage"), //$NON-NLS-1$

	/**
	 * A special kind of operation only affecting edges.
	 * Allows to change the source or target terminal of an edge.
	 */
	LINK("Link", "Link two nodes in a structure storage by a new edge "); //$NON-NLS-1$

	private final String name, description;

	private EditOperation(String name, String description) {
		this.name = name;
		this.description = description;
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return name();
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return null;
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return this;
	}
}
