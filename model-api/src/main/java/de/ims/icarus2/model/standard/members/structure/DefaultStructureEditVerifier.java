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

 * $Revision: 457 $
 * $Date: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/structure/DefaultStructureEditVerifier.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members.structure;

import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.members.structure.StructureEditVerifier;
import de.ims.icarus2.model.standard.members.container.DefaultContainerEditVerifier;
import de.ims.icarus2.model.util.DataSequence;

/**
 * @author Markus Gärtner
 * @version $Id: DefaultStructureEditVerifier.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
public class DefaultStructureEditVerifier extends DefaultContainerEditVerifier implements StructureEditVerifier {

	public DefaultStructureEditVerifier(Structure source) {
		super(source);
	}

	protected boolean isValidAddEdgeIndex(long index) {
		return index>0L && index<=getSource().getEdgeCount();
	}

	protected boolean isValidRemoveEdgeIndex(long index) {
		return index>0L && index<getSource().getEdgeCount();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.StructureEditVerifier#getSource()
	 */
	@Override
	public Structure getSource() {
		return (Structure) super.getSource();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.StructureEditVerifier#canAddEdge(long, de.ims.icarus2.model.api.members.item.Edge)
	 */
	@Override
	public boolean canAddEdge(long index, Edge edge) {
		return edge!=null && isValidAddEdgeIndex(index);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.StructureEditVerifier#canAddEdges(long, de.ims.icarus2.model.util.DataSequence)
	 */
	@Override
	public boolean canAddEdges(long index, DataSequence<? extends Edge> edges) {
		return edges!=null && isValidAddEdgeIndex(index);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.StructureEditVerifier#canRemoveEdge(long)
	 */
	@Override
	public boolean canRemoveEdge(long index) {
		return isValidRemoveEdgeIndex(index);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.StructureEditVerifier#canRemoveEdges(long, long)
	 */
	@Override
	public boolean canRemoveEdges(long index0, long index1) {
		return isValidRemoveEdgeIndex(index0) && isValidRemoveEdgeIndex(index1) && index0<=index1;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.StructureEditVerifier#canMoveEdge(long, long)
	 */
	@Override
	public boolean canMoveEdge(long index0, long index1) {
		return isValidRemoveEdgeIndex(index0) && isValidRemoveEdgeIndex(index1);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.StructureEditVerifier#canSetTerminal(de.ims.icarus2.model.api.members.item.Edge, de.ims.icarus2.model.api.members.item.Item, boolean)
	 */
	@Override
	public boolean canSetTerminal(Edge edge, Item terminal, boolean isSource) {
		return !(terminal==getSource().getVirtualRoot() && !isSource);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.StructureEditVerifier#canCreateEdge(de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public boolean canCreateEdge(Item source, Item target) {
		return true;
	}

}
