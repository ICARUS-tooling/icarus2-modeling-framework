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

import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.members.structure.StructureEditVerifier;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.util.Recyclable;
import de.ims.icarus2.util.annotations.OptionalMethod;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
public interface EdgeStorage extends ModelConstants, Recyclable {

	void addNotify(Structure context);

	void removeNotify(Structure context);

	/**
	 * @see Structure#getStructureType()
	 */
	StructureType getStructureType();

	/**
	 * @see Structure#getEdgeCount()
	 */
	long getEdgeCount(Structure context);

	/**
	 * @see Structure#getEdgeAt(long)
	 */
	Edge getEdgeAt(Structure context, long index);


	/**
	 * @see Structure#indexOfEdge(Edge)
	 */
	long indexOfEdge(Structure context, Edge edge);

	/**
	 * @see Structure#getEdgeCount(Item)
	 */
	long getEdgeCount(Structure context, Item node);

	/**
	 * @see Structure#getEdgeAt(Item, long, boolean)
	 */
	long getEdgeCount(Structure context, Item node, boolean isSource);

	/**
	 * @see Structure#getEdgeAt(Item, long, boolean)
	 */
	Edge getEdgeAt(Structure context, Item node, long index, boolean isSource);

	/**
	 * @see Structure#getVirtualRoot()
	 */
	Item getVirtualRoot(Structure context);

	/**
	 * @see Structure#isRoot(Item)
	 */
	boolean isRoot(Structure context, Item node);

	// TREE METHODS

	/**
	 * @see Structure#getParent(Item)
	 */
	@OptionalMethod
	Item getParent(Structure context, Item node);

	/**
	 * @see Structure#indexOfChild(Item)
	 */
	@OptionalMethod
	long indexOfChild(Structure context, Item child);

	/**
	 * @see Structure#getSiblingAt(Item, long)
	 */
	@OptionalMethod
	Item getSiblingAt(Structure context, Item child, long offset);

	/**
	 * @see Structure#getHeight(Item)
	 */
	@OptionalMethod
	long getHeight(Structure context, Item node);

	/**
	 * @see Structure#getDepth(Item)
	 */
	@OptionalMethod
	long getDepth(Structure context, Item node);

	/**
	 * @see Structure#getDescendantCount(Item)
	 */
	@OptionalMethod
	long getDescendantCount(Structure context, Item parent);

	// EDIT METHODS

	/**
	 * @see Structure#addEdge(long, Edge)
	 */
	long addEdge(Structure context, long index, Edge edge);

	/**
	 * @see Structure#addEdges(long, DataSequence)
	 */
	void addEdges(Structure context, long index, DataSequence<? extends Edge> edges);

	/**
	 * @see Structure#removeEdge(long)
	 */
	Edge removeEdge(Structure context, long index);

	/**
	 * @see Structure#removeEdges(long, long)
	 */
	DataSequence<? extends Edge> removeEdges(Structure context, long index0, long index1);

	/**
	 * @see Structure#moveEdge(long, long)
	 */
	void moveEdge(Structure context, long index0, long index1);

	/**
	 * @see Structure#setTerminal(Edge, Item, boolean)
	 */
	void setTerminal(Structure context, Edge edge, Item item, boolean isSource);

	/**
	 * @see Structure#newEdge(Item, Item)
	 */
	Edge newEdge(Structure context, Item source, Item target);

	/**
	 * Creates a new {@code StructureEditVerifier} for the host structure. The provided
	 * {@link ContainerEditVerifier} instance is merely a hint for the edge storage and
	 * in no way obligatory to use! If the edge storage poses its own restrictions on the
	 * nodes it holds edges for then it might as well provide a specialized edit verifier
	 * for both edges and nodes.
	 *
	 * @see Structure#createEditVerifier()
	 */
	StructureEditVerifier createEditVerifier(Structure context, ContainerEditVerifier containerEditVerifier);

	boolean isDirty(Structure context);
}
