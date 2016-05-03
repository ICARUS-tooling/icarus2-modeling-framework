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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/structure/CompoundStructureEditVerifier.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members.structure;

import static de.ims.icarus2.model.util.Conditions.checkNotNull;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.members.structure.StructureEditVerifier;
import de.ims.icarus2.model.util.DataSequence;

/**
 * @author Markus Gärtner
 * @version $Id: CompoundStructureEditVerifier.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
public class CompoundStructureEditVerifier implements StructureEditVerifier {

	private ContainerEditVerifier containerEditVerifier;

	public CompoundStructureEditVerifier(ContainerEditVerifier containerEditVerifier) {
		 checkNotNull(containerEditVerifier);

		 this.containerEditVerifier = containerEditVerifier;
	}

	public ContainerEditVerifier getContainerEditVerifier() {
		return containerEditVerifier;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifier#close()
	 */
	@Override
	public void close() {
		containerEditVerifier.close();

		containerEditVerifier = null;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifier#canAddItem(long, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public boolean canAddItem(long index, Item item) {
		return containerEditVerifier.canAddItem(index, item);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifier#canAddItems(long, de.ims.icarus2.model.util.DataSequence)
	 */
	@Override
	public boolean canAddItems(long index, DataSequence<? extends Item> items) {
		return containerEditVerifier.canAddItems(index, items);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifier#canRemoveItem(long)
	 */
	@Override
	public boolean canRemoveItem(long index) {
		return containerEditVerifier.canRemoveItem(index);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifier#canRemoveItems(long, long)
	 */
	@Override
	public boolean canRemoveItems(long index0, long index1) {
		return containerEditVerifier.canRemoveItems(index0, index1);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifier#canMoveItem(long, long)
	 */
	@Override
	public boolean canMoveItem(long index0, long index1) {
		return containerEditVerifier.canMoveItem(index0, index1);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.StructureEditVerifier#getSource()
	 */
	@Override
	public Structure getSource() {
		return (Structure) containerEditVerifier.getSource();
	}

	protected boolean isValidAddEdgeIndex(long index) {
		return index>=0L && index<=getSource().getEdgeCount();
	}

	protected boolean isValidRemoveEdgeIndex(long index) {
		return index>=0L && index<getSource().getEdgeCount();
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
