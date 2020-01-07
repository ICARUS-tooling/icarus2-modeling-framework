/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.structure.builder;

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static de.ims.icarus2.util.Conditions.checkState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.util.IcarusUtils;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Buffer implementation for the construction of {@link Structure} instances.
 *
 *
 * @author Markus Gärtner
 *
 */
public class EdgeBuffer {

	private static final int UNSET_INT = IcarusUtils.UNSET_INT;

	private final Map<Item, NodeInfo> data = new Object2ObjectOpenHashMap<>(200);

	private int maxHeight = UNSET_INT;
	private int maxDepth = UNSET_INT;
	private int maxDescendants = UNSET_INT;

	private int maxIncoming = UNSET_INT;
	private int minIncoming = UNSET_INT;
	private int maxOutgoing = UNSET_INT;
	private int minOutgoing = UNSET_INT;

	private boolean metadataComputed = false;

	private final Stack<NodeInfo> pool = new ObjectArrayList<>();

	private Item root;

	public void reset() {
		for(NodeInfo info : data.values()) {
			info.reset();

			pool.push(info);
		}
		data.clear();

		root = null;
		maxHeight = UNSET_INT;
		maxDepth = UNSET_INT;
		maxDescendants = UNSET_INT;

		maxIncoming = UNSET_INT;
		minIncoming = UNSET_INT;
		maxOutgoing = UNSET_INT;
		minOutgoing = UNSET_INT;

		metadataComputed = false;
	}

	private NodeInfo newInfo() {
		return pool.isEmpty() ? new NodeInfo() : pool.pop();
	}

	private NodeInfo getInfo(Item node, boolean createIfMissing) {
		NodeInfo info = data.get(node);
		if(info==null && createIfMissing) {
			info = newInfo();
			data.put(node, info);
		}
		return info;
	}

	public NodeInfo getInfo(Item node) {
		return data.get(node);
	}

	public void setRoot(Item root) {
		checkState("Root already set", this.root==null);

		this.root = root;
	}

	public void add(Edge edge) {
		getInfo(edge.getSource(), true).outgoing.add(edge);
		getInfo(edge.getTarget(), true).incoming.add(edge);
	}

	public void add(Collection<? extends Edge> edges) {
		for(Edge edge : edges) {
			add(edge);
		}
	}

	public <E extends Edge> void add(@SuppressWarnings("unchecked") E...edges) {
		for(Edge edge : edges) {
			add(edge);
		}
	}

	public int getEdgeCount(Item node) {
		NodeInfo info = getInfo(node, false);
		return info==null ? 0 : info.incoming.size() + info.outgoing.size();
	}

	public int getEdgeCount(Item node, boolean isSource) {
		NodeInfo info = getInfo(node, false);
		if(info==null) {
			return 0;
		}
		return isSource ? info.outgoing.size() : info.incoming.size();
	}

	public Edge getEdgeAt(Item node, int index, boolean isSource) {
		NodeInfo info = getInfo(node, false);
		if(info==null)
			throw new IndexOutOfBoundsException();

		return isSource ? info.outgoing.get(index) : info.incoming.get(index);
	}

	public void forEachEdge(Item node, Consumer<? super Edge> c, boolean incoming, boolean outgoing) {
		NodeInfo info = getInfo(node, false);
		if(info==null)
			throw new IllegalArgumentException("No info for node: "+getName(node));

		if(incoming) {
			info.incoming.forEach(c);
		}
		if(outgoing) {
			info.outgoing.forEach(c);
		}
	}

	private void checkMetadataComputed() {
		checkState("Metadata not computed yet", metadataComputed);
	}

	public int getHeight(Item node) {
		checkMetadataComputed();

		NodeInfo info = getInfo(node, false);
		return info==null ? UNSET_INT : info.height;
	}

	public int getDepth(Item node) {
		checkMetadataComputed();

		NodeInfo info = getInfo(node, false);
		return info==null ? UNSET_INT : info.depth;
	}

	public int getDescendantsCount(Item node) {
		checkMetadataComputed();

		NodeInfo info = getInfo(node, false);
		return info==null ? UNSET_INT : info.descendants;
	}

	public int getMaxHeight() {
		checkMetadataComputed();

		return maxHeight;
	}

	public int getMaxDepth() {
		checkMetadataComputed();

		return maxDepth;
	}

	public int getMaxDescendantsCount() {
		checkMetadataComputed();

		return maxDescendants;
	}

	public int getMaxIncoming() {
		checkMetadataComputed();

		return maxIncoming;
	}

	public int getMinIncoming() {
		checkMetadataComputed();

		return minIncoming;
	}

	public int getMaxOutgoing() {
		checkMetadataComputed();

		return maxOutgoing;
	}

	public int getMinOutgoing() {
		checkMetadataComputed();

		return minOutgoing;
	}

	public StructureType getStructureType() {
		checkMetadataComputed();

		if(maxIncoming==0 && maxOutgoing==0) {
			return StructureType.SET;
		} else if(maxIncoming<=1 && maxOutgoing<=1) {
			return StructureType.CHAIN;
		} else if(maxIncoming<=1) {
			return StructureType.TREE;
		} else {
			return StructureType.GRAPH;
		}
	}

	public void sortEdges(Comparator<? super Edge> c, boolean sortIncoming, boolean sortOutgoing) {
		for(NodeInfo info : data.values()) {
			if(sortIncoming && info.incoming!=null && info.incoming.size()>1) {
				Collections.sort(info.incoming, c);
			}
			if(sortOutgoing && info.outgoing!=null && info.outgoing.size()>1) {
				Collections.sort(info.outgoing, c);
			}
		}
	}

	/**
	 * Computes meta data information for the structure represented by this buffer using
	 * the given collection of root candidates to start with. If the collection is {@code null}
	 * or empty, then all the nodes stored in this buffer are considered as potential root
	 * candidates and tested for having an incoming edge count of {@code 0}.
	 *
	 * @param rootCandidates
	 */
	public void computeMetaData() {
		checkState("Metadata already computed", !metadataComputed);
		checkState("Missing virtual root node", root!=null);

		maxIncoming = 0;
		maxOutgoing = 0;

		minIncoming = Integer.MAX_VALUE;
		maxIncoming = Integer.MIN_VALUE;

		// Only relevant if we do not have a graph structure
		maxHeight = 0;
		maxDescendants = 0;
		maxDepth = 0;

		/*
		 * Reworked 2-path strategy:
		 *
		 * First path computes top-down the depth values of each node
		 * and also marks all the leaves.
		 * The second path then bottom-up computed heights and the
		 * descendants count for nodes (IFF we have a tree/chain structure only!!).
		 *
		 * As structure complexity easily can exceed stacksize limits
		 * we need to do this without recursion!
		 */

		// Nodes that are yet to be processed and marked
		Queue<Item> pendingNodes = new ArrayDeque<>();
		pendingNodes.offer(root);

		// Nodes that have been found to be leaf candidates for second path
		List<Item> leaves = new ArrayList<>();

		// Now do the first path of top-down calculations and detect leaves
		while(!pendingNodes.isEmpty()) {
			Item node = pendingNodes.poll();
			NodeInfo info = getInfo(node, false);

			if(info.visited) {
				continue;
			}

			if(node!=root) {
				int intCount = info.incoming.size();
				int outCount = info.outgoing.size();

				if(intCount!=0) {
					maxIncoming = Math.max(maxIncoming, intCount);
					minIncoming = Math.min(minIncoming, intCount);
				}

				if(outCount!=0) {
					maxOutgoing = Math.max(maxOutgoing, outCount);
					minIncoming = Math.min(minIncoming, outCount);
				}
			} else {
				// Initialize root depth
				info.depth = 0;
			}

			List<Edge> edges = info.outgoing;

			info.descendants = edges.size();

			if(edges.size()>0) {
				for(Edge edge : edges) {
					// Downwards propagation of depth values
					Item target = edge.getTarget();
					NodeInfo targetInfo = getInfo(target, false);
					targetInfo.depth = info.depth+1;
					pendingNodes.offer(target);
				}
			} else {
				// Initialize leaf height
				info.height = 0;
				leaves.add(node);
			}

			info.visited = true;
		}


		/*
		 *  Second path only if structure satisfies chain and/or tree properties.
		 *  We subsequently can stop calculating heights, depths and descendant
		 *  counts otherwise, as those properties cannot be collected for graphs.
		 */
		if(maxIncoming==1) {
			// Begin with leave nodes
			pendingNodes.addAll(leaves);
			leaves.clear();

			while(!pendingNodes.isEmpty()) {
				Item node = pendingNodes.poll();
				if(node==root) {
					continue;
				}

				NodeInfo info = getInfo(node, false);
				// Shouldn't happen, but we won't abort due to broken structures
				if(info.incoming.isEmpty()) {
					continue;
				}

				Item parent = info.incomingAt(0).getSource();
				NodeInfo parentInfo = getInfo(parent, false);

				maxDescendants = Math.max(maxDescendants, info.descendants);

				parentInfo.descendants += info.descendants;
				parentInfo.height = Math.max(parentInfo.height, info.height+1);
				parentInfo.processedChildren++;

				// Only schedule parent for processing if all its children are done
				if(parentInfo.processedChildren>=parentInfo.outgoingCount()) {
					pendingNodes.offer(parent);
				}
			}

			NodeInfo rootInfo = getInfo(root, false);
			maxDepth = rootInfo.height;
			maxHeight = rootInfo.height-1;
		}

		metadataComputed = true;
	}

	public static class NodeInfo {
		private final List<Edge> outgoing = new ArrayList<>();
		private final List<Edge> incoming = new ArrayList<>();

		/** Flag for the first path */
		private boolean visited = false;

		/** Hint for the second path */
		private int processedChildren = 0;

		/**
		 * Number of hops in the longest path from this node down to a leaf.
		 * Leaf nodes have a height of {@code 0}.
		 */
		private int height = UNSET_INT;

		/**
		 * Number of hops from this node up to the artificial root node or
		 * {@code -1} if this node is not connected to the artificial root.
		 */
		private int depth = UNSET_INT;

		/**
		 * Total number of nodes in the subtree whose root this node is.
		 */
		private int descendants = UNSET_INT;

		private void reset() {
			outgoing.clear();
			incoming.clear();
			visited = false;
			processedChildren = 0;
			height = UNSET_INT;
			depth = UNSET_INT;
			descendants = UNSET_INT;
		}

		public boolean hasEdges() {
			return !incoming.isEmpty() || !outgoing.isEmpty();
		}

		public int getHeight() {
			return height;
		}

		public int getDepth() {
			return depth;
		}

		public int getDescendants() {
			return descendants;
		}

		public Edge incomingAt(int index) {
			return incoming.get(index);
		}

		public int incomingCount() {
			return incoming.size();
		}

		public Edge outgoingAt(int index) {
			return outgoing.get(index);
		}

		public int outgoingCount() {
			return outgoing.size();
		}

		/**
		 * Applies the given {@code action} to all edges of the specified type(s).
		 * If both incoming and outgoing edges are to be included, incoming edges
		 * will be processed first!
		 *
		 * @param action
		 * @param incoming
		 * @param outgoing
		 */
		public void forEachEdge(Consumer<? super Edge> action, boolean incoming, boolean outgoing) {
			if(incoming) {
				this.incoming.forEach(action);
			}
			if(outgoing) {
				this.outgoing.forEach(action);
			}
		}

		public void forEachIncomingEdge(Consumer<? super Edge> c) {
			incoming.forEach(c);
		}

		public void forEachOutgoingEdge(Consumer<? super Edge> c) {
			outgoing.forEach(c);
		}
	}
}
