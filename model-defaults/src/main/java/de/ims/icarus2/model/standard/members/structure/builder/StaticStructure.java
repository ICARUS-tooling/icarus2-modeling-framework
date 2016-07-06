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
package de.ims.icarus2.model.standard.members.structure.builder;

import static de.ims.icarus2.util.Conditions.checkNotNull;
import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.members.structure.StructureEditVerifier;
import de.ims.icarus2.model.api.members.structure.StructureInfo;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.standard.members.MemberFlags;
import de.ims.icarus2.model.standard.members.container.ItemStorage;
import de.ims.icarus2.model.standard.members.item.AbstractItem;
import de.ims.icarus2.model.standard.members.structure.EdgeStorage;
import de.ims.icarus2.model.standard.members.structure.ImmutableStructureEditVerifier;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.collections.seq.DataSequence;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 * @author Markus Gärtner
 *
 */
public class StaticStructure extends AbstractItem implements Structure {

	private ItemStorage nodes;
	private EdgeStorage edges;

	private DataSet<Container> baseContainers;
	private Container boundaryContainer;

	void setNodes(ItemStorage nodes) {
		checkNotNull(nodes);

		this.nodes = nodes;

		nodes.addNotify(this);
	}

	void setEdges(EdgeStorage edges) {
		checkNotNull(edges);

		this.edges = edges;

		edges.addNotify(this);
	}

	void setBaseContainers(DataSet<Container> baseContainers) {

		if(baseContainers==null) {
			baseContainers = DataSet.emptySet();
		}

		this.baseContainers = baseContainers;
	}

	void setBoundaryContainer(Container boundaryContainer) {
		this.boundaryContainer = boundaryContainer;
	}

	void setAugmented(boolean augmented) {
		flags = MemberFlags.setStructureAugmented(flags, augmented);
	}

	private <T extends Object> T signalUnsupportedOperation() {
		throw new ModelException(getCorpus(), GlobalErrorCode.UNSUPPORTED_OPERATION,
				"Structure is immutable");
	}

	@Override
	public boolean isAugmented() {
		return MemberFlags.isStructureAugmented(flags);
	}

	@Override
	public StructureInfo getInfo() {
		//TODO
		return null;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return nodes.getContainerType();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#getBaseContainers()
	 */
	@Override
	public DataSet<Container> getBaseContainers() {
		return baseContainers;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#getBoundaryContainer()
	 */
	@Override
	public Container getBoundaryContainer() {
		return boundaryContainer;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#isItemsComplete()
	 */
	@Override
	public boolean isItemsComplete() {
		return true;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#getItemCount()
	 */
	@Override
	public long getItemCount() {
		return nodes.getItemCount(this);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#getItemAt(long)
	 */
	@Override
	public Item getItemAt(long index) {
		return nodes.getItemAt(this, index);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#indexOfItem(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long indexOfItem(Item item) {
		return nodes.indexOfItem(this, item);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#addItem(long, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public void addItem(long index, Item item) {
		signalUnsupportedOperation();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#addItems(long, de.ims.icarus2.util.collections.seq.DataSequence)
	 */
	@Override
	public void addItems(long index, DataSequence<? extends Item> items) {
		signalUnsupportedOperation();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#removeItem(long)
	 */
	@Override
	public Item removeItem(long index) {
		return signalUnsupportedOperation();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#removeItems(long, long)
	 */
	@Override
	public DataSequence<? extends Item> removeItems(long index0, long index1) {
		return signalUnsupportedOperation();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#moveItem(long, long)
	 */
	@Override
	public void moveItem(long index0, long index1) {
		signalUnsupportedOperation();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getBeginOffset()
	 */
	@Override
	public long getBeginOffset() {
		return nodes.getBeginOffset(this);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getEndOffset()
	 */
	@Override
	public long getEndOffset() {
		return nodes.getEndOffset(this);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.STRUCTURE;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getManifest()
	 */
	@Override
	public StructureManifest getManifest() {
		return (StructureManifest) ModelUtils.getContainerManifest(this);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#createEditVerifier()
	 */
	@Override
	public StructureEditVerifier createEditVerifier() {
		return new ImmutableStructureEditVerifier(this);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getStructureType()
	 */
	@Override
	public StructureType getStructureType() {
		return edges.getStructureType();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#isEdgesComplete()
	 */
	@Override
	public boolean isEdgesComplete() {
		return true;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getEdgeCount()
	 */
	@Override
	public long getEdgeCount() {
		return edges.getEdgeCount(this);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getEdgeAt(long)
	 */
	@Override
	public Edge getEdgeAt(long index) {
		return edges.getEdgeAt(this, index);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#indexOfEdge(de.ims.icarus2.model.api.members.item.Edge)
	 */
	@Override
	public long indexOfEdge(Edge edge) {
		return edges.indexOfEdge(this, edge);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getEdgeCount(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long getEdgeCount(Item node) {
		return edges.getEdgeCount(this, node);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getEdgeCount(de.ims.icarus2.model.api.members.item.Item, boolean)
	 */
	@Override
	public long getEdgeCount(Item node, boolean isSource) {
		return edges.getEdgeCount(this, node, isSource);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getEdgeAt(de.ims.icarus2.model.api.members.item.Item, long, boolean)
	 */
	@Override
	public Edge getEdgeAt(Item node, long index, boolean isSource) {
		return edges.getEdgeAt(this, node, index, isSource);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getVirtualRoot()
	 */
	@Override
	public Item getVirtualRoot() {
		return edges.getVirtualRoot(this);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#isRoot(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public boolean isRoot(Item node) {
		return edges.isRoot(this, node);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getParent(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public Item getParent(Item node) {
		return edges.getParent(this, node);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#indexOfChild(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long indexOfChild(Item child) {
		return edges.indexOfChild(this, child);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getSiblingAt(de.ims.icarus2.model.api.members.item.Item, long)
	 */
	@Override
	public Item getSiblingAt(Item child, long offset) {
		return edges.getSiblingAt(this, child, offset);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getHeight(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long getHeight(Item node) {
		return edges.getHeight(this, node);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getDepth(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long getDepth(Item node) {
		return edges.getDepth(this, node);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#getDescendantCount(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long getDescendantCount(Item parent) {
		return edges.getDescendantCount(this, parent);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#addEdge(long, de.ims.icarus2.model.api.members.item.Edge)
	 */
	@Override
	public long addEdge(long index, Edge edge) {
		return signalUnsupportedOperation();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#addEdges(long, de.ims.icarus2.util.collections.seq.DataSequence)
	 */
	@Override
	public void addEdges(long index, DataSequence<? extends Edge> edges) {
		signalUnsupportedOperation();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#removeEdge(long)
	 */
	@Override
	public Edge removeEdge(long index) {
		return signalUnsupportedOperation();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#removeEdges(long, long)
	 */
	@Override
	public DataSequence<? extends Edge> removeEdges(long index0, long index1) {
		return signalUnsupportedOperation();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#moveEdge(long, long)
	 */
	@Override
	public void moveEdge(long index0, long index1) {
		signalUnsupportedOperation();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#setTerminal(de.ims.icarus2.model.api.members.item.Edge, de.ims.icarus2.model.api.members.item.Item, boolean)
	 */
	@Override
	public void setTerminal(Edge edge, Item item, boolean isSource) {
		signalUnsupportedOperation();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.Structure#newEdge(de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public Edge newEdge(Item source, Item target) {
		return signalUnsupportedOperation();
	}

}
