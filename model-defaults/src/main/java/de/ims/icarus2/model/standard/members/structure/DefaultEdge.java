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
 */
package de.ims.icarus2.model.standard.members.structure;

import static de.ims.icarus2.util.Conditions.checkNotNull;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.util.mem.HeapMember;
import de.ims.icarus2.util.mem.Reference;
import de.ims.icarus2.util.mem.ReferenceType;

/**
 * Implements a simple edge. Note that this edge implementation requires
 * both terminals and a host strcture to be set at all times!
 *
 * @author Markus Gärtner
 *
 */
@HeapMember
public class DefaultEdge extends AbstractEdge {

	@Reference(ReferenceType.UPLINK)
	private Structure structure;

	public DefaultEdge(Structure structure, Item source, Item target) {
		// Rely on setStructure() doing the null check
		setStructure(structure);
		setSource(source);
		setTarget(target);
	}

	public DefaultEdge(Item source, Item target) {
		setSource(source);
		setTarget(target);
	}

	public DefaultEdge(Structure structure) {
		setStructure(structure);
	}

	/**
	 * @param structure the structure to set
	 */
	public void setStructure(Structure structure) {
		checkNotNull(structure);

		this.structure = structure;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Edge#getStructure()
	 */
	@Override
	public Structure getStructure() {
		return structure;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#isAlive()
	 */
	@Override
	public boolean isAlive() {
		return super.isAlive() && structure!=null;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#isLocked()
	 */
	@Override
	public boolean isLocked() {
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return super.isDirty() || structure==null;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		super.recycle();
		structure = null;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return super.revive() && structure!=null;
	}
}
