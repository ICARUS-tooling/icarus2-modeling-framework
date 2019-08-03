/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;

import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.util.IcarusUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * Buffer implementation for the construction of {@link Structure} instances.
 *
 *
 * @author Markus Gärtner
 *
 */
public class EdgeBuffer {

	private static final int UNSET_INT = IcarusUtils.UNSET_INT;
	private static final int VISITED_INT = UNSET_INT-1;

	private final Map<Item, NodeInfo> data = new Object2ObjectOpenHashMap<>(200);

	private int maxHeight = UNSET_INT;
	private int maxDepth = UNSET_INT;
	private int maxDescendants = UNSET_INT;

	private int maxIncoming = UNSET_INT;
	private int minIncoming = UNSET_INT;
	private int maxOutgoing = UNSET_INT;
	private int minOutgoing = UNSET_INT;

	private boolean metadataComputed = false;

	private final Stack<NodeInfo> pool = new Stack<>();

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

	public void computeMetaData(Item rootCandidate) {
		computeMetaData(Collections.singleton(rootCandidate));
	}

	public void computeMetaData(Item...rootCandidates) {
		computeMetaData(Arrays.asList(rootCandidates));
	}

	/**
	 * Computes meta data information for the structure represented by this buffer using
	 * the given collection of root candidates to start with. If the collection is {@code null}
	 * or empty, then all the nodes stored in this buffer are considered as potential root
	 * candidates and tested for having an incoming edge count of {@code 0}.
	 *
	 * @param rootCandidates
	 */
	public void computeMetaData(Collection<? extends Item> rootCandidates) {
		checkState("Metadata already computed", !metadataComputed);
		checkState("Missing virtual root node", root!=null);

		if(rootCandidates==null || rootCandidates.isEmpty()) {
			rootCandidates = data.keySet();
		}

		maxHeight = 0;
		maxDescendants = 0;
		maxDepth = 0;

		maxIncoming = 0;
		maxOutgoing = 0;

		minIncoming = Integer.MAX_VALUE;
		maxIncoming = Integer.MIN_VALUE;

		for(Item root : rootCandidates) {
			NodeInfo info = getInfo(root, false);
			if(info==null || !info.incoming.isEmpty()) {
				continue;
			}

			info = visitAndCompute(root, root==this.root ? 0 : -1);

			maxHeight = Math.max(maxHeight, info.height);
			maxDescendants = Math.max(maxDescendants, info.descendants);
		}

		metadataComputed = true;
	}

	private NodeInfo visitAndCompute(Item node, int depth) {
		NodeInfo info = getInfo(node, false);
		checkState("Missing info for node: "+node, node!=null);
		checkState("Cycle detected at node: "+node, info.depth==UNSET_INT);

		List<Edge> edges = info.outgoing;

		info.depth = depth;

		int height = 0;
		int descendants = edges.size();

		if(!edges.isEmpty()) {

			for(Edge edge : edges) {
				NodeInfo targetInfo = visitAndCompute(edge.getTarget(), depth==-1 ? -1 : depth+1);
				descendants += targetInfo.descendants;
				height = Math.max(height, targetInfo.height);
			}

			// Include own level in maxHeight value
			height++;
		} else if(depth!=-1) {
			maxDepth = Math.max(maxDepth, depth);
		}

		info.descendants = descendants;
		info.height = height;

		if(node!=root) {
			//TODO maybe merge some of the minmaxing with above loop?
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
		}

		return info;
	}

	public static class NodeInfo {
		private final List<Edge> outgoing = new ArrayList<>();
		private final List<Edge> incoming = new ArrayList<>();

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
			height = UNSET_INT;
			depth = UNSET_INT;
			descendants = UNSET_INT;
		}

		public boolean hasEdges() {
			return !incoming.isEmpty() && !outgoing.isEmpty();
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
