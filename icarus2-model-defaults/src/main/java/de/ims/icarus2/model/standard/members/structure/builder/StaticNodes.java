/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;

import java.util.Arrays;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.members.structure.builder.EdgeBuffer.NodeInfo;
import de.ims.icarus2.util.MutablePrimitives.MutableInteger;
import de.ims.icarus2.util.collections.LookupList;

/**
 * Provides a collection of specialized node implementations for memory and access
 * efficient edge storage and lookup.
 *
 *
 * @author Markus Gärtner
 *
 */
public class StaticNodes {

	public static final Node EMPTY_NODE = new Node();

	private static int _parent(Item item, EdgeBuffer edgeBuffer, LookupList<Edge> edges) {
		int inCount = edgeBuffer.getEdgeCount(item, false);
		if(inCount>1)
			throw new IllegalArgumentException("Given node has too many incoming edges for a tree: "+getName(item));

		int parent = UNSET_INT;

		if(inCount>0) {
			parent = edges.indexOf(edgeBuffer.getEdgeAt(item, 0, false));
		}

		return parent;
	}

	private static int _in(NodeInfo info, LookupList<Edge> edges, int index) {
		return edges.indexOf(info.incomingAt(index));
	}

	private static int _out(NodeInfo info, LookupList<Edge> edges, int index) {
		return edges.indexOf(info.outgoingAt(index));
	}

	private static int[] _edges(NodeInfo info, LookupList<Edge> edges, int inCount, int outCount) {
		int[] result = new int[inCount+outCount];
		MutableInteger counter = new MutableInteger(0);
		info.forEachEdge(e -> {result[counter.getAndIncrement()] = edges.indexOf(e);}, inCount>0, outCount>0);

		return result;
	}

	public static Node createNode(Item item, EdgeBuffer edgeBuffer, LookupList<Edge> edges,
			boolean forceTreeProperties) {
		NodeInfo info = edgeBuffer.getInfo(item);

		if(info==null || !info.hasEdges()) {
			// Return null so that client code can decide whether or not to use the EMPTY_NODE dummy or do something else
			return null;
		}

		final int inCount = edgeBuffer.getEdgeCount(item, false);
//		if(inCount==0) {
//			//TODO maybe throw an error indicating that we have an illegal root node?
//			return null;
//		}
		if(inCount>1 && forceTreeProperties)
			throw new IllegalArgumentException("Given node has too many incoming edges for a tree: "+getName(item));

		final int outCount = edgeBuffer.getEdgeCount(item, true);
		final int parent = _parent(item, edgeBuffer, edges);
		final int depth = edgeBuffer.getDepth(item);
		final int height = edgeBuffer.getHeight(item);
		final int descendants = edgeBuffer.getDescendantsCount(item);

		Node node = null;

		if(inCount<=1) {
			// GENERAL TREE STYLE NODES

			switch (outCount) {
			case 0: // TREE LEAF
				node = new TreeLeaf(parent, depth);
				break;

			case 1: // CHAIN ELEMENT
				int child = _out(info, edges, 0);
				node = new ChainNode(parent, depth, child, height, descendants);
				break;

			case 2: // BINARY TREE ELEMENT
				int left = _out(info, edges, 0);
				int right = _out(info, edges, 1);
				node = new BinaryTreeNode(parent, depth, left, right, height, descendants);
				break;

			default:
				// TREE NODE
				int[] children = _edges(info, edges, 0, outCount);
				node = new CompactTreeNode(parent, depth, children, height, descendants);
				break;
			}
		} else {
			// GENERAL GRAPH STYLE NODES
			int[] combinedEdges = _edges(info, edges, inCount, outCount);
			node = new GraphNode(combinedEdges, inCount);
		}

		return node;
	}

	/**
	 * Models an arbitrary node in a structure with only read access.
	 * Note that basically all methods are optional and allowed to return
	 * their respective default values. Returned indices always refer to
	 * the order of edges in the {@link EdgeBuffer buffer} used at construction
	 * time.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class Node {
		public int incomingEdgeCount() {
			return 0;
		}

		public int outgoingEdgeCount() {
			return 0;
		}

		public int indexOfIncomingEdge(int edge) {
			return UNSET_INT;
		}

		public int indexOfOutgoingEdge(int edge) {
			return UNSET_INT;
		}

		public int incomingEdgeAt(int index) {
			throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS, "No incoming edges");
		}

		public int outgoingEdgeAt(int index) {
			throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS, "No outgoing edges");
		}

		public int height() {
			return 0;
		}

		public int depth() {
			return UNSET_INT;
		}

		public int descendants() {
			return 0;
		}
	}

	public static class TreeLeaf extends Node {
		private final int parent, depth;

		public TreeLeaf(int parent, int depth) {
			this.parent = parent;
			this.depth = depth;
		}

		@Override
		public int incomingEdgeCount() {
			return parent==UNSET_INT ? 0 : 1;
		}

		@Override
		public int indexOfIncomingEdge(int edge) {
			return (parent!=UNSET_INT && edge==parent) ? 0 : UNSET_INT;
		}

		@Override
		public int incomingEdgeAt(int index) {
			if(parent==UNSET_INT)
				throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS, "No incoming edges");
			if(index!=0)
				throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS, "Only one incoming edge - invalid index: "+index);
			return parent;
		}

		@Override
		public int depth() {
			return depth;
		}
	}

	public abstract static class IntermediateNode extends TreeLeaf {

		private final int height, descendants;

		public IntermediateNode(int parent, int depth, int height, int descendants) {
			super(parent, depth);

			this.height = height;
			this.descendants = descendants;
		}

		@Override
		public int height() {
			return height;
		}

		@Override
		public int descendants() {
			return descendants;
		}

	}

	public static class RootNode extends Node {

		private final int height, descendants;

		public RootNode(int height, int descendants) {

			this.height = height;
			this.descendants = descendants;
		}

		@Override
		public int height() {
			return height;
		}

		@Override
		public int descendants() {
			return descendants;
		}

	}

	public static class ChainNode extends IntermediateNode {

		private final int child;

		public ChainNode(int parent, int depth, int child, int height, int descendants) {
			super(parent, depth, height, descendants);

			this.child = child;
		}

		@Override
		public int outgoingEdgeCount() {
			return 1;
		}

		@Override
		public int indexOfOutgoingEdge(int edge) {
			return edge==child ? 0 : UNSET_INT;
		}

		@Override
		public int outgoingEdgeAt(int index) {
			if(index!=0)
				throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS, "Only 1 incoming edge - invalid index: "+index);

			return child;
		}

	}

	public static class BinaryTreeNode extends IntermediateNode {

		private final int left, right;


		public BinaryTreeNode(int parent, int depth, int[] edges, int height, int descendants) {
			this(parent, depth, edges[0], edges[1], height, descendants);
		}

		public BinaryTreeNode(int parent, int depth, int left, int right, int height, int descendants) {
			super(parent, depth, height, descendants);

			this.left = left;
			this.right = right;
		}

		@Override
		public int outgoingEdgeCount() {
			return 2;
		}

		@Override
		public int indexOfOutgoingEdge(int edge) {
			if(edge==left) {
				return 0;
			} else if(edge==right) {
				return 1;
			} else {
				return UNSET_INT;
			}
		}

		@Override
		public int outgoingEdgeAt(int index) {
			if(index<0 || index>1)
				throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS, "Only 2 incoming edges - invalid index: "+index);

			return index==0 ? left : right;
		}
	}

	public static class CompactTreeNode extends IntermediateNode {

		private final int[] edges;

		public CompactTreeNode(int parent, int depth, int[] edges, int height, int descendants) {
			super(parent, depth, height, descendants);

			Arrays.sort(edges);

			this.edges = edges;
		}

		@Override
		public int outgoingEdgeCount() {
			return edges.length;
		}

		@Override
		public int indexOfOutgoingEdge(int edge) {
			int index = Arrays.binarySearch(edges, edge);
			return index<0 ? UNSET_INT : index;
		}

		@Override
		public int outgoingEdgeAt(int index) {
			if(index<0 || index>=edges.length)
				throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
						Messages.indexOutOfBounds(null, 0, edges.length-1, index));

			return edges[index];
		}
	}

	public static class GraphNode extends Node {

		private final int inCount;
		private final int[] edges;

		public GraphNode(int[] edges, int inCount) {

			Arrays.sort(edges, 0, inCount);
			Arrays.sort(edges, inCount, edges.length);

			this.edges = edges;
			this.inCount = inCount;
		}

		@Override
		public int incomingEdgeCount() {
			return inCount;
		}

		@Override
		public int outgoingEdgeCount() {
			return edges.length-inCount;
		}

		@Override
		public int indexOfIncomingEdge(int edge) {
			int index = Arrays.binarySearch(edges, 0, inCount, edge);
			return index<0 ? UNSET_INT : index;
		}

		@Override
		public int indexOfOutgoingEdge(int edge) {
			int index = Arrays.binarySearch(edges, inCount, edges.length, edge);
			return index<0 ? UNSET_INT : index;
		}

		@Override
		public int incomingEdgeAt(int index) {
			checkArgument("Index of incoming edge out of bounds: "+index, index<inCount);
			return edges[index];
		}

		@Override
		public int outgoingEdgeAt(int index) {
			checkArgument("Index of outgoing edge out of bounds: "+index, index<edges.length-inCount);
			return edges[inCount+index];
		}
	}
}
