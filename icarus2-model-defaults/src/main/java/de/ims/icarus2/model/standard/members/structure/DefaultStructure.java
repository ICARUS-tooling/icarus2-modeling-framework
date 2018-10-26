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
package de.ims.icarus2.model.standard.members.structure;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.members.structure.StructureEditVerifier;
import de.ims.icarus2.model.api.members.structure.StructureInfo;
import de.ims.icarus2.model.manifest.api.StructureFlag;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.members.MemberFlags;
import de.ims.icarus2.model.standard.members.container.DefaultContainer;
import de.ims.icarus2.model.standard.members.structure.info.StructureInfoBuilder;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultStructure extends DefaultContainer implements Structure {

	protected EdgeStorage edgeStorage;

	public DefaultStructure() {
		// no-op
	}

	/**
	 * Checks whether a valid edge storage is set and returns it.
	 * Throw an exception otherwise.
	 *
	 * @return
	 *
	 * @throws ModelException in case no edge storage is set
	 */
	protected EdgeStorage edgeStorage() {
		if(edgeStorage==null)
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "No edge storage set");
		return edgeStorage;
	}

	@Override
	public boolean isDirty() {
		return super.isDirty() || edgeStorage().isDirty(this);
	}

	protected void checkEdgeStorage(EdgeStorage edgeStorage) {
		if (edgeStorage == null) {
			return;
		}

		StructureType requiredType = getManifest().getStructureType();
		StructureType givenType = edgeStorage.getStructureType();

		if(!requiredType.isCompatibleWith(givenType))
			throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
					Messages.mismatch("Incompatible structure types", requiredType, givenType));
	}

	public void setEdgeStorage(EdgeStorage edgeStorage) {
		checkEdgeStorage(edgeStorage);

		if(this.edgeStorage!=null) {
			this.edgeStorage.removeNotify(this);
		}

		this.edgeStorage = edgeStorage;

		if(this.edgeStorage!=null) {
			this.edgeStorage.addNotify(this);
		}
	}

	public void clearEdgeStorage() {
		if(edgeStorage!=null) {
			edgeStorage.removeNotify(this);
		}

		edgeStorage = null;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getManifest()
	 */
	@Override
	public StructureManifest getManifest() {
		return (StructureManifest) super.getManifest();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getStructureType()
	 */
	@Override
	public StructureType getStructureType() {
		return edgeStorage.getStructureType();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#isEdgesComplete()
	 */
	@Override
	public boolean isEdgesComplete() {
		return isFlagSet(MemberFlags.EDGES_COMPLETE);
	}

	public void setEdgesComplete(boolean complete) {
		setFlag(MemberFlags.EDGES_COMPLETE, complete);
	}

	@Override
	public boolean isAugmented() {
		return isFlagSet(MemberFlags.STRUCTURE_AUGMENTED);
	}

	public void setAugmented(boolean augmented) {
		setFlag(MemberFlags.STRUCTURE_AUGMENTED, augmented);
	}

	@Override
	public StructureInfo getInfo() {
		return StructureInfoBuilder.createInfo(this);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getEdgeCount()
	 */
	@Override
	public long getEdgeCount() {
		return edgeStorage().getEdgeCount(this);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getEdgeAt(long)
	 */
	@Override
	public Edge getEdgeAt(long index) {
		return edgeStorage().getEdgeAt(this, index);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#indexOfEdge(de.ims.icarus2.model.api.members.item.Edge)
	 */
	@Override
	public long indexOfEdge(Edge edge) {
		return edgeStorage().indexOfEdge(this, edge);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getEdgeCount(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long getEdgeCount(Item node) {
		return edgeStorage().getEdgeCount(this, node);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getEdgeCount(de.ims.icarus2.model.api.members.item.Item, boolean)
	 */
	@Override
	public long getEdgeCount(Item node, boolean isSource) {
		return edgeStorage().getEdgeCount(this, node, isSource);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getEdgeAt(de.ims.icarus2.model.api.members.item.Item, long, boolean)
	 */
	@Override
	public Edge getEdgeAt(Item node, long index, boolean isSource) {
		return edgeStorage().getEdgeAt(this, node, index, isSource);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getVirtualRoot()
	 */
	@Override
	public Item getVirtualRoot() {
		return edgeStorage().getVirtualRoot(this);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#isRoot(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public boolean isRoot(Item node) {
		return edgeStorage().isRoot(this, node);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getParent(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public Item getParent(Item node) {
		return edgeStorage().getParent(this, node);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#indexOfChild(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long indexOfChild(Item child) {
		return edgeStorage().indexOfChild(this, child);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getSiblingAt(de.ims.icarus2.model.api.members.item.Item, long)
	 */
	@Override
	public Item getSiblingAt(Item child, long offset) {
		return edgeStorage().getSiblingAt(this, child, offset);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getHeight(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long getHeight(Item node) {
		return edgeStorage().getHeight(this, node);
	}

	@Override
	public long getDepth(Item node) {
		return edgeStorage().getDepth(this, node);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getDescendantCount(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long getDescendantCount(Item parent) {
		return edgeStorage().getDescendantCount(this, parent);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#addEdge(long, de.ims.icarus2.model.api.members.item.Edge)
	 */
	@Override
	public long addEdge(long index, Edge edge) {
		return edgeStorage().addEdge(this, index, edge);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#addEdges(long, de.ims.icarus2.util.collections.seq.DataSequence)
	 */
	@Override
	public void addEdges(long index, DataSequence<? extends Edge> edges) {
		edgeStorage().addEdges(this, index, edges);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#removeEdge(long)
	 */
	@Override
	public Edge removeEdge(long index) {
		return edgeStorage().removeEdge(this, index);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#removeEdges(long, long)
	 */
	@Override
	public DataSequence<? extends Edge> removeEdges(long index0, long index1) {
		return edgeStorage().removeEdges(this, index0, index1);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#moveEdge(long, long)
	 */
	@Override
	public void moveEdge(long index0, long index1) {
		edgeStorage().moveEdge(this, index0, index1);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#setTerminal(de.ims.icarus2.model.api.members.item.Edge, de.ims.icarus2.model.api.members.item.Item, boolean)
	 */
	@Override
	public void setTerminal(Edge edge, Item item, boolean isSource) {
		edgeStorage().setTerminal(this, edge, item, isSource);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#newEdge(de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public Edge newEdge(Item source, Item target) {
		return edgeStorage().newEdge(this, source, target);
	}

	@Override
	public StructureEditVerifier createEditVerifier() {
		/*
		 *  This implementation relies on the fact that we derive from
		 *  a Container implementation that is bound to already have the
		 *  means of creating a ContainerEditVerifier.
		 *  We will use that verifier to forward it to the StructureEditVerifier
		 *  implementation as a 'hint' on how to handle nodes.
		 */
		ContainerEditVerifier containerEditVerifier = super.createEditVerifier();

		// If edges are static don't even bother with asking the edge storage
		if(!getManifest().isStructureFlagSet(StructureFlag.NON_STATIC)) {
			return new CompoundStructureEditVerifier(containerEditVerifier){

				@Override
				protected boolean isValidAddEdgeIndex(long index) {
					return false;
				}

				@Override
				protected boolean isValidRemoveEdgeIndex(long index) {
					return false;
				}

			};
		} else {
			// Forward verifier creation to the edge storage (the container edit verifier is a 'hint' only!)
			return edgeStorage().createEditVerifier(this, containerEditVerifier);
		}
	}
}
