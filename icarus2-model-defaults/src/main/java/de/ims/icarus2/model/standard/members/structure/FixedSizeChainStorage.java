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
import static de.ims.icarus2.model.api.members.MemberUtils.checkNoLoopsStructure;
import static de.ims.icarus2.model.api.members.MemberUtils.checkNonEmptyContainer;
import static de.ims.icarus2.model.api.members.MemberUtils.checkNonPartialStructure;
import static de.ims.icarus2.model.api.members.MemberUtils.checkStaticContainer;
import static de.ims.icarus2.model.util.ModelUtils.getName;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.members.structure.StructureEditVerifier;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.members.container.ImmutableContainerEditVerifier;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.collections.seq.DataSequence;
import de.ims.icarus2.util.collections.seq.ListSequence;

/**
 * A specialized chain storage that links a fixed collection of items
 * in a complete chain (or collection of complete chains). This means that
 * after initial construction this storage is guaranteed to hold exactly
 * {@code N} edges, where {@code N} is the number of items in the host structure.
 * <p>
 * This implementation stores edges in a simply array of length {@code 2*N} and
 * the following properties:
 * <ul>
 * <li>The <b>incoming</b> edge for the item located at index {@code i} in the structure's
 * node collection will always be at index {@code 2*i} in the edge array</li>
 * <li>The <b>outgoing</b> edge for the item located at index {@code i} in the structure's
 * node collection will always be at index {@code 2*i + 1} in the edge array</li>
 * <li>The {@code i}th edge of the chain will be stored at index {@code i*2}, which will also
 * be the location of the incoming edge for the {@code i}th node</li>
 * <li>For index lookup of a given edge the index is the position of that edge's target terminal
 * in the node collection of the host structure</li>
 * </ul>
 * <p>
 *
 * @author Markus Gärtner
 *
 */
@TestableImplementation(EdgeStorage.class)
public class FixedSizeChainStorage implements EdgeStorage {

	protected RootItem<Edge> root;

	/**
	 * For item on position {@code i} in target container, incoming edge
	 * will be at position {@code 2*i} in this array and outgoing edge will
	 * be at position {@code 2*i+1},
	 */
	protected Edge[] edges;

	/**
	 * For item on position {@code i} in target container, depth value will
	 * be at position {@code 2*i} in this array and height value will be at
	 * position {@code 2*i+1},
	 */
	protected int[] heightAndDepthValues;
	protected int edgeCount;
	protected boolean heightInfoValid = false;

	public FixedSizeChainStorage() {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#addNotify(de.ims.icarus2.model.api.members.structure.Structure)
	 */
	@Override
	public void addNotify(Structure context) {
		checkStaticContainer(context);
		checkNonEmptyContainer(context);
		checkNonPartialStructure(context);
		checkNoLoopsStructure(context);

		int itemCount = IcarusUtils.ensureIntegerValueRange(context.getItemCount());
		int capacity = IcarusUtils.ensureIntegerValueRange(itemCount*2L);

		if(edges==null || edges.length<capacity) {
			edges = new Edge[capacity];
		} else {
			Arrays.fill(edges, null);
		}

		if(heightAndDepthValues==null || heightAndDepthValues.length<capacity) {
			heightAndDepthValues = new int[capacity];
		} else {
			Arrays.fill(heightAndDepthValues, -1);
		}

		root = createRootItem(context);
		edgeCount = 0;
	}

	protected RootItem<Edge> createRootItem(Structure context) {
		return RootItem.forStructure(context);
	}

	/**
	 * Returns {@code true} iff the number of edges added to this
	 * storage equals the number of nodes in the underlying container.
	 * @return
	 */
	public boolean isCompleteChain() {
		return edgeCount==(edges.length>>>1);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#removeNotify(de.ims.icarus2.model.api.members.structure.Structure)
	 */
	@Override
	public void removeNotify(Structure context) {
		root.removeAllEdges();
		root = null;
		Arrays.fill(edges, null);
		Arrays.fill(heightAndDepthValues, -1);
		edgeCount= 0;
		heightInfoValid = false;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getStructureType()
	 */
	@Override
	public StructureType getStructureType() {
		return StructureType.CHAIN;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure)
	 */
	@Override
	public long getEdgeCount(Structure context) {
//		checkChainConsistency();

		return edgeCount;
	}

	/**
	 * Returns the incoming edge for the {@code index}'th node in the structure
	 * if the structure is complete or the n'th edge in the order of the edges'
	 * terminals if the structure is not complete.
	 *
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getEdgeAt(de.ims.icarus2.model.api.members.structure.Structure, long)
	 */
	@Override
	public Edge getEdgeAt(Structure context, long index) {
//		checkChainConsistency();
		if(index>=edgeCount)
			throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					Messages.outOfBounds("Invalid edge index", index, 0, edgeCount));

		/*
		 * For a complete chain (or complete collection of subchains)
		 * we can directly perform a lookup on the edge array.
		 * Otherwise we need to iterate all the added edges (costly operation).
		 */
		if(isCompleteChain()) {
			return edges[IcarusUtils.ensureIntegerValueRange(index<<1)];
		}

		// Run a local iterative search for the edge
		int remaining = IcarusUtils.ensureIntegerValueRange(index);
		int idx = 0;
		do {
			if(edges[idx]!=null) {
				remaining--;
			}
			if(remaining>=0) {
				idx+=2;
			}
		} while(remaining>=0);
		return edges[idx];
	}

	protected void invalidateHeightAndDepth() {
		heightInfoValid = false;
	}

	protected void validateHeightAndDepth(Structure context) {
		if(!heightInfoValid) {
			refreshHeights(context);

			heightInfoValid = true;
		}
	}

	/**
	 * Converts the index of the given node in the host structure
	 * into the index in the local buffer array. The returned value
	 * will be the index of the incoming edge for the specified
	 * node. To get the index for the outgoing edge the value simply
	 * needs to be incremented by 1.
	 */
	protected int localIndexForNode(Structure context, Item node) {
		long nodeIndex = context.indexOfItem(node);
		if(nodeIndex==IcarusUtils.UNSET_LONG)
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					"Supplied item is not a legal node in the structure of this storage: "+getName(node));

		// Double index to get to correct position in our internal storages
		return IcarusUtils.ensureIntegerValueRange(nodeIndex)<<1;
	}

	protected void refreshHeights(Structure context) {
//		checkChainConsistency();

		Arrays.fill(heightAndDepthValues, -1);

		heightAndDepthValues[0] = 0;

		for(int i=0; i<root.getEdgeCount(); i++) {
			Edge edge = root.getEdgeAt(i);
			int localIndex = localIndexForNode(context, edge.getTarget());
			refreshHeight0(context, edge, localIndex, 1);
		}
	}

	//TODO rework to not use recursion as for big structures we might easily reach the stack size limit
	protected void refreshHeight0(Structure context, Edge edge, int localIndex, int depth) {

		Edge successor = getOutgoingEdge(context, edge.getTarget());

		heightAndDepthValues[localIndex] = depth;

		if(successor==null) {
			heightAndDepthValues[localIndex+1] = 0;
		} else {
			int targetIndex = localIndexForNode(context, successor.getTarget());

			refreshHeight0(context, successor, targetIndex, depth+1);

			int height = heightAndDepthValues[targetIndex]+1;

			heightAndDepthValues[localIndex+1] = height;
			heightAndDepthValues[0] = Math.max(height, heightAndDepthValues[0]);
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#indexOfEdge(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Edge)
	 */
	@Override
	public long indexOfEdge(Structure context, Edge edge) {
//		checkChainConsistency();
		checkHostStructure(edge, context);
		requireNonNull(edge.getTarget(), "This implementation relies on target nodes of edges for storage");

		if(isCompleteChain()) {
			int index = IcarusUtils.ensureIntegerValueRange(context.indexOfItem(edge.getTarget()));
			return edges[IcarusUtils.ensureIntegerValueRange(index<<1)]==edge ? index : IcarusUtils.UNSET_LONG;
		}

		// Run a local iterative search for the edge index
		int remaining = edgeCount;
		int idx = 0;
		while(remaining>0) {
			Edge current = edges[idx];
			if(current!=null) {
				if(current==edge) {
					return idx>>>1;
				}

				remaining--;
			}
			idx += 2;
		}
		return IcarusUtils.UNSET_LONG;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long getEdgeCount(Structure context, Item node) {
//		checkChainConsistency();

		if(node==root) {
			return root.getEdgeCount();
		}

		int index = localIndexForNode(context, node);

		int count = 0;
		if(edges[index]!=null) {
			count++;
		}
		if(edges[index+1]!=null) {
			count++;
		}

		return count;
	}

	protected void setAsSource(Structure context, Item node, Edge edge) {
		int localIndex = localIndexForNode(context, node);
		edges[localIndex+1] = edge;
	}

	protected void setAsTarget(Structure context, Item node, Edge edge) {
		int localIndex = localIndexForNode(context, node);
		edges[localIndex] = edge;
	}

	protected Edge getAsSource(Structure context, Item node) {
		int localIndex = localIndexForNode(context, node);
		return edges[localIndex+1];
	}

	protected Edge getAsTarget(Structure context, Item node) {
		int localIndex = localIndexForNode(context, node);
		return edges[localIndex];
	}

	/**
	 * Returns the outgoing edge (if present) for the given {@code node} which must not be
	 * the {@link #getVirtualRoot(Structure) root node}.
	 *
	 * @param context
	 * @param node
	 * @return
	 */
	public Edge getOutgoingEdge(Structure context, Item node) {
//		checkChainConsistency();

		if(node==root)
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					"This method is not designed for use with the root node");

		return getAsSource(context, node);
	}

	/**
	 * Returns the incoming edge (if present) for the given {@code node} which must not be
	 * the {@link #getVirtualRoot(Structure) root node}.
	 *
	 * @param context
	 * @param node
	 * @return
	 */
	public Edge getIncomingEdge(Structure context, Item node) {
//		checkChainConsistency();

		if(node==root)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Virtual root node cannot have a parent");

		return getAsTarget(context, node);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, boolean)
	 */
	@Override
	public long getEdgeCount(Structure context, Item node, boolean isSource) {
//		checkChainConsistency();

		return getUncheckedEdgeCount(context, node, isSource);
	}

	/**
	 * Helper method to fetch the edge count for the specified {@code node}.
	 *
	 * @param context
	 * @param node
	 * @param isSource
	 * @return
	 */
	long getUncheckedEdgeCount(Structure context, Item node, boolean isSource) {

		if(node==root) {
			return isSource ? root.getEdgeCount() : 0;
		}

		int index = localIndexForNode(context, node);

		if(isSource) {
			index++;
		}

		return edges[index]==null ? 0L : 1L;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getEdgeAt(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, long, boolean)
	 */
	@Override
	public Edge getEdgeAt(Structure context, Item node, long index, boolean isSource) {
//		checkChainConsistency();

		int idx = IcarusUtils.ensureIntegerValueRange(index);

		if(node==root) {
			if(isSource) {
				return root.getEdgeAt(idx);
			}

			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Virtual root node cannot have incoming edges");
		}

		int localIndex = localIndexForNode(context, node);
		if(isSource) {
			localIndex++;
		}

		Edge edge = edges[localIndex];

		if(edge==null || index>0L)
			throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					"Edge index out of bounds: "+index);

		return edge;
	}

//	public boolean hasEdgeAt(Structure context, long index) {
//		int localIndex = IcarusUtils.ensureIntegerValueRange(index)<<1;
//		return edges[localIndex] != null;
//	}

	public boolean containsEdge(Structure context, Edge edge) {
		int localIndex = localIndexForNode(context, edge.getTarget());
		return edges[localIndex] != edge;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getVirtualRoot(de.ims.icarus2.model.api.members.structure.Structure)
	 */
	@Override
	public Item getVirtualRoot(Structure context) {
		return root;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#isRoot(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public boolean isRoot(Structure context, Item node) {
		return getParent(context, node) == root;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getParent(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public Item getParent(Structure context, Item node) {
		return getIncomingEdge(context, node).getSource();
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#indexOfChild(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long indexOfChild(Structure context, Item child) {
		Edge edge = getIncomingEdge(context, child);
		Item parent = edge.getSource();

		if(parent==root) {
			return root.indexOfEdge(edge);
		}

		return 0L;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getSiblingAt(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, long)
	 */
	@Override
	public Item getSiblingAt(Structure context, Item child, long offset) {
		throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
				"Nodes in a chain cannot have siblings");
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getHeight(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long getHeight(Structure context, Item node) {
//		checkChainConsistency();

		validateHeightAndDepth(context);

		int targetIndex = localIndexForNode(context, node);

		return heightAndDepthValues[targetIndex+1];
	}

	@Override
	public long getDepth(Structure context, Item node) {
//		checkChainConsistency();

		validateHeightAndDepth(context);

		int targetIndex = localIndexForNode(context, node);

		return heightAndDepthValues[targetIndex];
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getDescendantCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long getDescendantCount(Structure context, Item parent) {
		if(parent==root) {
//			checkChainConsistency();
			// Easy job here: all nodes are descendants of the root node
			return edgeCount;
		}

		return getHeight(context, parent);
	}

	// MODIFIER METHODS

	public int addEdge(Structure context, Edge edge) {
		checkHostStructure(edge, context);

		Item source = edge.getSource();
		if(source==root) {
			root.addEdge(edge);
		} else {
			int sourceIndex = localIndexForNode(context, source) + 1;
			if(edges[sourceIndex]!=null)
				throw new ModelException(ModelErrorCode.MODEL_CORRUPTED_STATE,
						"Outgoing edge for node "+getName(source)+" already set - cannot add edge "+getName(edge));

			edges[sourceIndex] = edge;
		}

		Item target = edge.getTarget();
		int targetIndex = localIndexForNode(context, target);
		if(edges[targetIndex]!=null)
			throw new ModelException(ModelErrorCode.MODEL_CORRUPTED_STATE,
					"Outgoing edge for node "+getName(target)+" already set - cannot add edge "+getName(edge));

		edges[targetIndex] = edge;

		// Maintenance stuff
		edgeCount++;
		invalidateHeightAndDepth();

		// Return the actual insertion index
		return targetIndex>>1;
	}

	/**
	 * Ignores the {@code index} parameter completely and forwards the actual
	 * workload to {@link #addEdge(Structure, Edge)}.
	 *
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#addEdge(de.ims.icarus2.model.api.members.structure.Structure, long, de.ims.icarus2.model.api.members.item.Edge)
	 * @see #addEdge(Structure, Edge)
	 */
	@Override
	public long addEdge(Structure context, long index, Edge edge) {
		return addEdge(context, edge);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#addEdges(de.ims.icarus2.model.api.members.structure.Structure, long, de.ims.icarus2.util.collections.seq.DataSequence)
	 */
	@Override
	public void addEdges(Structure context, long index,
			DataSequence<? extends Edge> edges) {

		int size = IcarusUtils.ensureIntegerValueRange(edges.entryCount());
		for(int i = 0; i<size; i++) {
			addEdge(context, edges.elementAt(i));
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#removeEdge(de.ims.icarus2.model.api.members.structure.Structure, long)
	 */
	@Override
	public Edge removeEdge(Structure context, long index) {

		int localIndex = IcarusUtils.ensureIntegerValueRange(index)<<1;

		Edge edge = edges[localIndex];

		if(edge==null)
			throw new ModelException(ModelErrorCode.MODEL_MISSING_MEMBER,
					"No edge stored for index "+index);

		Item source = edge.getSource();

		if(source==root) {
			root.removeEdge(edge);
		} else {
			setAsSource(context, source, null);
		}

		edges[localIndex] = null;

		// Maintenance stuff
		edgeCount--;
		invalidateHeightAndDepth();

		return edge;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#removeEdges(de.ims.icarus2.model.api.members.structure.Structure, long, long)
	 */
	@Override
	public DataSequence<? extends Edge> removeEdges(Structure context,
			long index0, long index1) {

		if(edgeCount==0) {
			return DataSequence.emptySequence();
		}

		int idx0 = IcarusUtils.ensureIntegerValueRange(index0);
		int idx1 = IcarusUtils.ensureIntegerValueRange(index1);

		int bufferSize = Math.min(edgeCount, idx1-idx0+1);

		List<Edge> buffer = new ArrayList<>(bufferSize);

		int removedCount = 0;

		for(int idx = idx0; idx<=idx1; idx++) {

			int localIndex = idx<<1;

			Edge edge = edges[localIndex];

			if(edge==null) {
				continue;
			}

			Item source = edge.getSource();

			if(source==root) {
				root.removeEdge(edge);
			} else {
				setAsSource(context, source, null);
			}

			edges[localIndex] = null;

			removedCount++;
		}

		// Maintenance stuff
		edgeCount -= removedCount;
		invalidateHeightAndDepth();

		return new ListSequence<>(buffer);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#swapEdges(de.ims.icarus2.model.api.members.structure.Structure, long, long)
	 */
	@Override
	public void swapEdges(Structure context, long index0, long index1) {
		throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Order of edges is fixed in this implementation");
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#setTerminal(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Edge, de.ims.icarus2.model.api.members.item.Item, boolean)
	 */
	@Override
	public void setTerminal(Structure context, Edge edge, Item terminal,
			boolean isSource) {
		checkHostStructure(edge, context);

		if(edge.getTerminal(isSource)==terminal) {
			return;
		}

		if(isSource) {
			if(terminal==root) {
				edge.setSource(terminal);
				root.addEdge(edge);
			} else {
				Edge existing = getAsSource(context, terminal);
				if(existing!=null && existing!=edge)
					throw new ModelException(ModelErrorCode.MODEL_CORRUPTED_STATE,
							"Desired source terminal for edge already has an outgoing edge assigned: "+getName(existing));

				Item oldSource = edge.getSource();
				edge.setSource(terminal);

				setAsSource(context, terminal, edge);
				setAsSource(context, oldSource, null);
			}
		} else {
			if(terminal==root) {
				throw new ModelException(GlobalErrorCode.INVALID_INPUT,
						"Virtual root node cannot have incoming edges");
			}

			Edge existing = getAsTarget(context, terminal);
			if(existing!=null && existing!=edge)
				throw new ModelException(ModelErrorCode.MODEL_CORRUPTED_STATE,
						"Desired target terminal for edge already has an incoming edge assigned: "+getName(existing));

			Item oldTarget = edge.getTarget();
			edge.setTarget(terminal);

			setAsTarget(context, terminal, edge);
			setAsTarget(context, oldTarget, null);
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#newEdge(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public Edge newEdge(Structure context, Item source, Item target) {
		return new DefaultEdge(context, source, target);
	}

	@Override
	public void recycle() {
		if(root!=null) {
			root.removeAllEdges();
			root = null;
		}
		if(edges!=null) {
			Arrays.fill(edges, null);
		}
		if(heightAndDepthValues!=null) {
			Arrays.fill(heightAndDepthValues, -1);
		}
		edgeCount= 0;
		heightInfoValid = false;
	}

	@Override
	public boolean revive() {
		return root!=null && edges!=null && heightAndDepthValues!=null;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#createEditVerifier(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.container.ContainerEditVerifier)
	 */
	@Override
	public StructureEditVerifier createEditVerifier(Structure context,
			ContainerEditVerifier containerEditVerifier) {
		return new FixedSizeChainEditVerifier(this, context);
	}

	@Override
	public boolean isDirty(Structure context) {
		return !isCompleteChain();
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	@TestableImplementation(StructureEditVerifier.class)
	public static class FixedSizeChainEditVerifier extends ImmutableContainerEditVerifier implements StructureEditVerifier {

		private FixedSizeChainStorage storage;

		/**
		 * @param containerEditVerifier
		 */
		public FixedSizeChainEditVerifier(
				FixedSizeChainStorage storage,
				Structure source) {
			super(source);

			this.storage = requireNonNull(storage);
		}

		@Override
		public void close() {
			super.close();

			storage = null;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.structure.StructureEditVerifier#getSource()
		 */
		@Override
		public Structure getSource() {
			return (Structure) super.getSource();
		}

		protected boolean isValidEdgeRemoveIndex(long index) {
			return index>=0L && index<getSource().getEdgeCount();
		}

		/**
		 * @see de.ims.icarus2.model.api.members.structure.StructureEditVerifier#canAddEdge(de.ims.icarus2.model.api.members.item.Edge)
		 */
		@Override
		public boolean canAddEdge(Edge edge) {
			StructureEditVerifier.checkEdgeForAdd(getSource(), edge);

			final Structure structure = getSource();
			final Item source = edge.getSource();
			final Item target = edge.getTarget();

			final Item root = storage.getVirtualRoot(structure);

			return target!=root
					&& (source==root || storage.getUncheckedEdgeCount(structure, source, true)==0L)
					&& storage.getUncheckedEdgeCount(structure, target, false)==0L;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.structure.StructureEditVerifier#canAddEdges(de.ims.icarus2.util.collections.seq.DataSequence)
		 */
		@Override
		public boolean canAddEdges(DataSequence<? extends Edge> edges) {
			requireNonNull(edges);

//			if(!isValidEdgeAddIndex(index)) {
//				return false;
//			}

			boolean canAdd = true;

			for(Edge edge : edges) {
				if(!canAddEdge(edge)) {
					canAdd = false;
				}
			}

			return canAdd;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.structure.StructureEditVerifier#canRemoveEdge(long)
		 */
		@Override
		public boolean canRemoveEdge(long index) {
			return isValidEdgeRemoveIndex(index);
		}

		/**
		 * @see de.ims.icarus2.model.api.members.structure.StructureEditVerifier#canRemoveEdges(long, long)
		 */
		@Override
		public boolean canRemoveEdges(long index0, long index1) {
			return isValidEdgeRemoveIndex(index0)
					&& isValidEdgeRemoveIndex(index1)
					&& index0<=index1;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.structure.StructureEditVerifier#canSwapEdges(long, long)
		 */
		@Override
		public boolean canSwapEdges(long index0, long index1) {
			return false;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.structure.StructureEditVerifier#canSetTerminal(de.ims.icarus2.model.api.members.item.Edge, de.ims.icarus2.model.api.members.item.Item, boolean)
		 */
		@Override
		public boolean canSetTerminal(Edge edge, Item terminal, boolean isSource) {
			StructureEditVerifier.checkEdgeForTerminalChange(getSource(), edge, terminal);

			final Structure structure = getSource();

			// Early fail if no actual change
			if(edge.getTerminal(isSource)==terminal) {
				return false;
			}

			// No loops allowed
			if(edge.getTerminal(!isSource)==terminal) {
				return false;
			}

			final Item root = structure.getVirtualRoot();

			boolean isRoot = root==terminal;

			return (isSource && (isRoot || storage.getUncheckedEdgeCount(structure, terminal, true)==0L))
					|| (!isSource && !isRoot && storage.getUncheckedEdgeCount(structure, terminal, false)==0L);
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
}
