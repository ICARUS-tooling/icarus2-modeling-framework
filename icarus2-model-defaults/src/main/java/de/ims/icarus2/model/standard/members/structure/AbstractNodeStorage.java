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

import static de.ims.icarus2.model.api.members.MemberUtils.checkHostStructure;
import static de.ims.icarus2.model.util.ModelUtils.getName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.members.structure.StructureEditVerifier;
import de.ims.icarus2.model.manifest.api.StructureFlag;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.LookupList;
import de.ims.icarus2.util.collections.seq.DataSequence;
import de.ims.icarus2.util.collections.seq.ListSequence;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * Implements a an {@link EdgeStorage} that stores all the actual structural information
 * in {@link NodeInfo} objects associated with each {@link Item node} in the {@link Structure}.
 *
 *
 * @author Markus Gärtner
 *
 * @param <N> type of the {@link NodeInfo} objects stored
 * @param <E> type of edges stored
 *
 */
public abstract class AbstractNodeStorage<N extends NodeInfo, E extends Edge> implements EdgeStorage {

	public static final int DEFAULT_CAPACITY = 10;

	protected RootItem<E> root;

	protected final LookupList<E> edges;
	protected final Map<Item, N> nodesData;

	public AbstractNodeStorage() {
		this(DEFAULT_CAPACITY);
	}

	public AbstractNodeStorage(int capacity) {
		edges = createEdgesLookup(capacity);
		nodesData = createNodesDataMap(capacity);
	}

	protected RootItem<E> createRootItem(Structure context) {
		return RootItem.forStructure(context);
	}

	protected LookupList<E> createEdgesLookup(int capacity) {
		if(capacity<0) {
			capacity = DEFAULT_CAPACITY;
		}

		return new LookupList<>(capacity);
	}

	protected Map<Item, N> createNodesDataMap(int capacity) {
		if(capacity<0) {
			capacity = DEFAULT_CAPACITY;
		}

		return new Object2ObjectOpenHashMap<>(capacity);
	}

	/**
	 * Reports the storage as being dirty when its manifest does not allow partial structures
	 * and the number of stored {@code NodeInfo} objects does not match the number of available
	 * nodes (not including the virtual root node).
	 *
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#isDirty(de.ims.icarus2.model.api.members.structure.Structure)
	 */
	@Override
	public boolean isDirty(Structure context) {
		return !context.getManifest().isStructureFlagSet(StructureFlag.PARTIAL)
				&& context.getItemCount()!=nodesData.size();
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#isRoot(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public boolean isRoot(Structure context, Item node) {
		return node==root;
	}

	/**
	 * Empty and delete the root item, clear edge list
	 * and node data lookup.
	 *
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		root.removeAllEdges();
		root = null;
		//TODO theoretically we have a problem here, as both storages below only ever grow
		edges.clear();
		nodesData.clear();
	}

	@Override
	public boolean revive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addNotify(Structure context) {
		root = createRootItem(context);
	}

	@Override
	public void removeNotify(Structure context) {
		root.removeAllEdges();
		root = null;
	}

	@Override
	public long getEdgeCount(Structure context) {
		return edges.size();
	}

	@Override
	public Edge getEdgeAt(Structure context, long index) {
		return edges.get(IcarusUtils.ensureIntegerValueRange(index));
	}

	@Override
	public long indexOfEdge(Structure context, Edge edge) {
		return edges.indexOf(edge);
	}

	protected NodeInfo getNodeInfo(Item node) {
		return node==root ? root : nodesData.get(node);
	}

	protected abstract N createNodeInfo(Item node);

	protected NodeInfo ensureNodeInfo(Item item) {
		if(item==root) {
			return root;
		}

		N info = nodesData.get(item);
		if(info == null) {
			info = createNodeInfo(item);
			nodesData.put(item, info);
		}

		return info;
	}

	public void prepareNode(Item node) {
		ensureNodeInfo(node);
	}

	@Override
	public long getEdgeCount(Structure context, Item node) {

		NodeInfo info = getNodeInfo(node);

		return info==null ? 0L : info.edgeCount();
	}

	@Override
	public long getEdgeCount(Structure context, Item node, boolean isSource) {

		NodeInfo info = getNodeInfo(node);

		return info==null ? 0L : info.edgeCount(!isSource);
	}

	@Override
	public Edge getEdgeAt(Structure context, Item node, long index,
			boolean isSource) {

		NodeInfo info = getNodeInfo(node);

		return info==null ? null : info.edgeAt(index, !isSource);
	}

	@Override
	public Item getVirtualRoot(Structure context) {
		return root;
	}

	/**
	 * Callback method for subclasses
	 */
	protected void invalidate() {
		// for subclasses
	}

	// MODIFICATION METHODS

	@Override
	public long addEdge(Structure context, long index, Edge edge) {
		checkHostStructure(edge, context);

		@SuppressWarnings("unchecked")
		E e = (E)edge;

		if(edges.contains(e))
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					"Edge already present: "+getName(edge));

		edges.add(IcarusUtils.ensureIntegerValueRange(index), e);

		// Map source node
		NodeInfo sourceInfo = ensureNodeInfo(edge.getSource());
		sourceInfo.addEdge(edge, false);

		// Map target node
		NodeInfo targetInfo = ensureNodeInfo(edge.getTarget());
		targetInfo.addEdge(edge, true);

		invalidate();

		return index;
	}

	@Override
	public void addEdges(Structure context, long index,
			DataSequence<? extends Edge> edges) {

		int size = IcarusUtils.ensureIntegerValueRange(edges.entryCount());

		for(int i=0; i<size; i++) {
			addEdge(context, index+i, edges.elementAt(i));
		}
	}

	@Override
	public Edge removeEdge(Structure context, long index) {

		E edge = edges.remove(IcarusUtils.ensureIntegerValueRange(index));

		// Unmap source
		NodeInfo sourceInfo = getNodeInfo(edge.getSource());
		if(sourceInfo!=null) {
			sourceInfo.removeEdge(edge, false);
			if(sourceInfo.edgeCount()==0) {
				removeNodeInfo(edge.getSource(), sourceInfo);
			}
		}

		// Unmap target node
		NodeInfo targetInfo = getNodeInfo(edge.getTarget());
		if(targetInfo!=null) {
			targetInfo.removeEdge(edge, true);
			if(targetInfo.edgeCount()==0) {
				removeNodeInfo(edge.getTarget(), targetInfo);
			}
		}

		invalidate();

		return edge;
	}

	/**
	 * Hook for subclasses to allow for keeping of unused {@code NodeInfo}
	 * instances if required.
	 */
	protected void removeNodeInfo(Item item, NodeInfo info) {
		nodesData.remove(item);
	}

	@Override
	public DataSequence<? extends Edge> removeEdges(Structure context,
			long index0, long index1) {

		int idx0 = IcarusUtils.ensureIntegerValueRange(index0);
		int idx1 = IcarusUtils.ensureIntegerValueRange(index1);

		List<Edge> buffer = new ArrayList<>(Math.max(1, idx1-idx0+1));

		for(int idx=idx0; idx<idx1; idx++) {
			buffer.add(removeEdge(context, idx));
		}

		return new ListSequence<>(buffer);
	}

	@Override
	public void swapEdges(Structure context, long index0, long index1) {
		int idx0 = IcarusUtils.ensureIntegerValueRange(index0);
		int idx1 = IcarusUtils.ensureIntegerValueRange(index1);

		E edge0 = edges.get(idx0);
		E edge1 = edges.get(idx1);

		edges.set(idx1, edge0);
		edges.set(idx0, edge1);

		invalidate();
	}

	@Override
	public void setTerminal(Structure context, Edge edge, Item item,
			boolean isSource) {

		// Old states
		NodeInfo sourceInfo = getNodeInfo(edge.getSource());
		NodeInfo targetInfo = getNodeInfo(edge.getTarget());
		NodeInfo terminalInfo = getNodeInfo(item);

		// Reassign links

		if(isSource) {
			sourceInfo.removeEdge(edge, false);
			terminalInfo.addEdge(edge, false);
			edge.setSource(item);
		} else {
			targetInfo.removeEdge(edge, true);
			terminalInfo.addEdge(edge, true);
			edge.setTarget(item);
		}

		invalidate();
	}

	@Override
	public Edge newEdge(Structure context, Item source, Item target) {
		return new DefaultEdge(context, source, target);
	}

	private <T extends Object> T signalUnsupportedOperation(Structure context) {
		throw new ModelException(context.getCorpus(), GlobalErrorCode.UNSUPPORTED_OPERATION,
				"Specialized tree operations not supported");
	}

	@Override
	public Item getParent(Structure context, Item node) {
		return signalUnsupportedOperation(context);
	}

	@SuppressWarnings("boxing")
	@Override
	public long indexOfChild(Structure context, Item child) {
		return signalUnsupportedOperation(context);
	}

	@Override
	public Item getSiblingAt(Structure context, Item child, long offset) {
		return signalUnsupportedOperation(context);
	}

	@SuppressWarnings("boxing")
	@Override
	public long getHeight(Structure context, Item node) {
		return signalUnsupportedOperation(context);
	}

	@SuppressWarnings("boxing")
	@Override
	public long getDepth(Structure context, Item node) {
		return signalUnsupportedOperation(context);
	}

	@SuppressWarnings("boxing")
	@Override
	public long getDescendantCount(Structure context, Item parent) {
		return signalUnsupportedOperation(context);
	}

	@Override
	public StructureEditVerifier createEditVerifier(Structure context,
			ContainerEditVerifier containerEditVerifier) {
		return new CompoundStructureEditVerifier(containerEditVerifier);
	}
}
