/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 400 $
 * $Date: 2015-05-29 15:06:46 +0200 (Fr, 29 Mai 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/edit/EditOperation.java $
 *
 * $LastChangedDate: 2015-05-29 15:06:46 +0200 (Fr, 29 Mai 2015) $
 * $LastChangedRevision: 400 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.util;

import javax.swing.Icon;

import de.ims.icarus2.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id: EditOperation.java 400 2015-05-29 13:06:46Z mcgaerty $
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
