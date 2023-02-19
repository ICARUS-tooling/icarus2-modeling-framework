/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.members.structure.StructureEditVerifier;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(StructureEditVerifier.class)
public class CompoundStructureEditVerifier implements StructureEditVerifier {

	private ContainerEditVerifier containerEditVerifier;

	public CompoundStructureEditVerifier(ContainerEditVerifier containerEditVerifier) {
		 requireNonNull(containerEditVerifier);

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

	// BEGIN DELEGATED METHODS

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifier#canAddItem(long, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public boolean canAddItem(long index, Item item) {
		return containerEditVerifier.canAddItem(index, item);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifier#canAddItems(long, de.ims.icarus2.util.collections.seq.DataSequence)
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
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifier#canSwapItems(long, long)
	 */
	@Override
	public boolean canSwapItems(long index0, long index1) {
		return containerEditVerifier.canSwapItems(index0, index1);
	}

	// END DELEGATED METHODS

	/**
	 * @see de.ims.icarus2.model.api.members.structure.StructureEditVerifier#getSource()
	 */
	@Override
	public Structure getSource() {
		return containerEditVerifier==null ? null : (Structure) containerEditVerifier.getSource();
	}

	protected boolean isValidRemoveEdgeIndex(long index) {
		return index>=0L && index<getSource().getEdgeCount();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.StructureEditVerifier#canAddEdge(de.ims.icarus2.model.api.members.item.Edge)
	 */
	@Override
	public boolean canAddEdge(Edge edge) {
		StructureEditVerifier.checkEdgeForAdd(getSource(), edge);

		return true;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.StructureEditVerifier#canAddEdges(de.ims.icarus2.util.collections.seq.DataSequence)
	 */
	@Override
	public boolean canAddEdges(DataSequence<? extends Edge> edges) {
		requireNonNull(edges);

		edges.forEach(this::canAddEdge); //TODO not very pretty, as the exception will be thrown inside the internal iteration code

		return true;
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
	 * @see de.ims.icarus2.model.api.members.structure.StructureEditVerifier#canSwapEdges(long, long)
	 */
	@Override
	public boolean canSwapEdges(long index0, long index1) {
		return index0!=index1
				&& isValidRemoveEdgeIndex(index0)
				&& isValidRemoveEdgeIndex(index1);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.StructureEditVerifier#canSetTerminal(de.ims.icarus2.model.api.members.item.Edge, de.ims.icarus2.model.api.members.item.Item, boolean)
	 */
	@Override
	public boolean canSetTerminal(Edge edge, Item terminal, boolean isSource) {
		StructureEditVerifier.checkEdgeForTerminalChange(getSource(), edge, terminal);

		return isSource || terminal!=getSource().getVirtualRoot();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.StructureEditVerifier#canCreateEdge(de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public boolean canCreateEdge(Item source, Item target) {
		StructureEditVerifier.checkNodesForEdgeCreation(getSource(), source, target);

		return target!=getSource().getVirtualRoot();
	}
}
