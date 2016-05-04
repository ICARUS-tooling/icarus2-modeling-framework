/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 422 $
 * $Date: 2015-08-19 15:38:58 +0200 (Mi, 19 Aug 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/structure/builder/StaticNodes.java $
 *
 * $LastChangedDate: 2015-08-19 15:38:58 +0200 (Mi, 19 Aug 2015) $
 * $LastChangedRevision: 422 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members.structure.builder;

import static de.ims.icarus2.model.util.CorpusUtils.getName;
import static de.ims.icarus2.util.Conditions.checkArgument;

import java.util.Arrays;

import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.standard.members.structure.builder.EdgeBuffer.NodeInfo;
import de.ims.icarus2.util.MutablePrimitives.MutableInteger;
import de.ims.icarus2.util.collections.LookupList;

/**
 * Provides a collection of specialized node implementations for memory and access
 * efficient edge storage and lookup.
 *
 *
 * @author Markus Gärtner
 * @version $Id: StaticNodes.java 422 2015-08-19 13:38:58Z mcgaerty $
 *
 */
public class StaticNodes {

	private static final int NO_INDEX = (int) ModelConstants.NO_INDEX;
	private static final int NO_DEPTH = -1;

	public static final Node EMPTY_NODE = new Node();

	private static final MutableInteger counter = new MutableInteger();

	private static int _parent(Item item, EdgeBuffer edgeBuffer, LookupList<Edge> edges) {
		int inCount = edgeBuffer.getEdgeCount(item, false);
		if(inCount>1)
			throw new IllegalArgumentException("Given node has too many incoming edges for a tree: "+getName(item));

		int parent = NO_INDEX;

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

		counter.setInt(0);

		info.forEachEdge(e -> {result[counter.increment()] = edges.indexOf(e);}, inCount>0, outCount>0);

		return result;
	}

	public static Node createNode(Item item, EdgeBuffer edgeBuffer, LookupList<Edge> edges, boolean forceTreeProperties) {
		NodeInfo info = edgeBuffer.getInfo(item);

		if(info==null || !info.hasEdges()) {
			// Return null so that client code can decide whether or not to use the EMPTY_NODE dummy or do something else
			return null;
		}

		final int inCount = edgeBuffer.getEdgeCount(item, false);
		if(inCount==0) {
			//TODO maybe throw an error indicating that we have an illegal root node?
			return null;
		}
		if(inCount>1 && forceTreeProperties)
			throw new IllegalArgumentException("Given node has too many incoming edges for a tree: "+getName(item));

		final int outCount = edgeBuffer.getEdgeCount(item, true);
		final int parent = _parent(item, edgeBuffer, edges);
		final int depth = edgeBuffer.getDepth(item);
		final int height = edgeBuffer.getHeight(item);
		final int descendants = edgeBuffer.getDescendantsCount(item);

		Node node = null;

		if(inCount==1) {
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
	 * @version $Id: StaticNodes.java 422 2015-08-19 13:38:58Z mcgaerty $
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
			return NO_INDEX;
		}

		public int indexOfOutgoingEdge(int edge) {
			return NO_INDEX;
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
			return NO_DEPTH;
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
		public int indexOfIncomingEdge(int edge) {
			return (parent!=NO_INDEX && edge==parent) ? 0 : NO_INDEX;
		}

		@Override
		public int incomingEdgeAt(int index) {
			if(parent==NO_INDEX)
				throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS, "No incoming edges");
			if(index!=0)
				throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS, "Only one incoming edge - invalid index: "+index);
			return parent;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.builder.StaticNodes.Node#depth()
		 */
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
			return edge==child ? 0 : NO_INDEX;
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
				return NO_INDEX;
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
			return index<0 ? NO_INDEX : index;
		}

		@Override
		public int outgoingEdgeAt(int index) {
			return edges[index];
		}
	}

	public static class GraphNode extends Node {

		private final int inCount;
		private final int[] edges;

		public GraphNode(int[] edges, int inCount) {

			Arrays.sort(edges);

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
			return index<0 ? NO_INDEX : index;
		}

		@Override
		public int indexOfOutgoingEdge(int edge) {
			int index = Arrays.binarySearch(edges, inCount, edges.length, edge);
			return index<0 ? NO_INDEX : index;
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
