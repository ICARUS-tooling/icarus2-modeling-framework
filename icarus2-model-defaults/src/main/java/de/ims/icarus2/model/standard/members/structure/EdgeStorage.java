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
package de.ims.icarus2.model.standard.members.structure;

import javax.annotation.Nullable;

import de.ims.icarus2.apiguard.OptionalMethod;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.members.structure.StructureEditVerifier;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.util.Recyclable;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
public interface EdgeStorage extends Recyclable {

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
	@Nullable
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
	 * @see Structure#swapEdges(long, long)
	 */
	void swapEdges(Structure context, long index0, long index1);

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

	/**
	 * Indicates whether or not the structure is currently in an inconsistent state.
	 * Some {@link StructureType structure types} or structure implementations impose
	 * certain requirements, i.e. an implementation optimized for modeling fully
	 * connected {@link StructureType#CHAIN chains} is likely to report the structure
	 * to be {@code dirty} when not every node in it is part of a (sub-)chain.
	 *
	 * @param context
	 * @return
	 */
	boolean isDirty(Structure context);
}
