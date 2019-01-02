/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.members.structure;

import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * Specifies a verification mechanism that can be used to check preconditions
 * for any modification action on a structure. Each instance of such a verifier
 * is bound to a single {@link #getSource() structure} and can not be used for
 * any other.
 * <p>
 * Note that like most other parts of the lower level framework specification
 * this interface too does not make any guarantees for multi-threaded access.
 * If client code does not enforce synchronization when accessing a verifier,
 * the returned values cannot be taken as promise that the target structure will
 * actually be able to carry out a desired modification!
 *
 * @author Markus Gärtner
 *
 */
public interface StructureEditVerifier extends ContainerEditVerifier {

	@Override
	Structure getSource();

	/**
	 * Verify if the given {@code edge} can be inserted at the given {@code index}.
	 * Note that the edge's {@link Edge#getTerminal(boolean) terminals} must be
	 * items that are already present as nodes in the underlying structure!
	 * <p>
	 * Precondition check for the {@link Structure#addEdge(long, Edge)} method.
	 *
	 * @param index
	 * @param edge
	 * @return
	 */
	boolean canAddEdge(long index, Edge edge);

	/**
	 * Verify if the given {@code edges} can be inserted at the given {@code index}.
	 * Note that the {@link Edge#getTerminal(boolean) terminals} of all provided
	 * edges must be items that are already present as nodes in the underlying structure!
	 * <p>
	 * Precondition check for the {@link Structure#addEdges(long, DataSequence)} method.
	 *
	 * @param index
	 * @param edges
	 * @return
	 */
	boolean canAddEdges(long index, DataSequence<? extends Edge> edges);

	/**
	 * Verify that whichever edge is currently located at the given {@code index}
	 * can safely be removed.
	 * <p>
	 * Precondition check for the {@link Structure#removeEdge(long)} method.
	 *
	 * @param index
	 * @return
	 */
	boolean canRemoveEdge(long index);

	/**
	 * Verify that a section of the underlying structrue's edge storage can safely
	 * be removed.
	 * <p>
	 * Precondition check for the {@link Structure#removeEdges(long, long)} method.
	 *
	 * @param index0
	 * @param index1
	 * @return
	 */
	boolean canRemoveEdges(long index0, long index1);

	boolean canSwapEdges(long index0, long index1);

	boolean canSetTerminal(Edge edge, Item terminal, boolean isSource);

	boolean canCreateEdge(Item source, Item target);
}
