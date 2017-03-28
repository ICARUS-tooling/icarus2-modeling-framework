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

import static de.ims.icarus2.model.util.ModelUtils.getName;

import java.util.Arrays;

import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.members.structure.RootItem;
import de.ims.icarus2.model.standard.members.structure.builder.StaticNodes.Node;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.LookupList;

/**
 * @author Markus Gärtner
 *
 */
public abstract class StaticTreeEdgeStorage extends AbstractStaticEdgeStorage<RootItem<?>> {

	public static StaticTreeEdgeStorage fromBuilder(StructureBuilder builder) {
		EdgeBuffer edgeBuffer = builder.edgeBuffer();

		if(edgeBuffer.getMaxIncoming()>1)
			throw new IllegalArgumentException(
					"Cannot build tree storage from buffer - maximum incoming edge count exceeded: "+edgeBuffer.getMaxIncoming());

		if(CompactTreeEdgeStorageInt.isCompatible(builder)) {
			return CompactTreeEdgeStorageInt.fromBuilder(builder);
		} else if(CompactTreeEdgeStorageLong.isCompatible(builder)) {
			return CompactTreeEdgeStorageLong.fromBuilder(builder);
		} else {
			throw new IllegalStateException("Missing implementation for structure data: "+builder);
		}
	}

	StaticTreeEdgeStorage(RootItem<Edge> root, LookupList<Edge> edges) {
		super(root, edges);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getStructureType()
	 */
	@Override
	public StructureType getStructureType() {
		return StructureType.TREE;
	}

	/**
	 * Compact tree storage that stores general information about nodes in an integer array
	 * and the actual information about edges for a node in a global byte array. The byte
	 * array allows addressing of 255 edges with a single entry and requires 2+n bytes per
	 * node where n is the number of outgoing edges for that node. The 2 static bytes are
	 * the global index of the incoming edge and the number of outgoing edges for the node
	 * (this value determines the number of subsequent bytes allocated for the node in the
	 * byte array).
	 * <p>
	 * Since height, depth, descendant count and the pointer to the section in the byte array
	 * are all packed together into a single integer value, the 32 bit integer space is divided as
	 * follows:
	 * The first 10 bit are used for the pointer to the byte array (this needed to be more than
	 * 8 bit, since the byte array might exceed the address space of a single byte in length).
	 * Then there are 7 bits for height and depth of the node each (which restricts the maximum
	 * height a node can have to {@value #MAX_HEIGHT} and the maximum depth to {@value #MAX_DEPTH}).
	 * The last 8 bits are reserved for the descendant count of the node, allowing for a single
	 * node to span the entire tree if necessary).
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class CompactTreeEdgeStorageInt extends StaticTreeEdgeStorage {

		public static final int MAX_NODE_COUNT = (1 << 8)-1;
		public static final int MAX_EDGE_COUNT = (1 << 8)-1;
		public static final int MAX_HEIGHT = (1 << 7)-1;
		public static final int MAX_DEPTH = (1 << 7)-2;

		static boolean isCompatible(StructureBuilder builder) {
			if(builder.getNodeCount()>MAX_NODE_COUNT) {
				return false;
			}

			if(builder.getEdgeCount()>MAX_EDGE_COUNT) {
				return false;
			}

			final EdgeBuffer edgeBuffer = builder.edgeBuffer();

			if(edgeBuffer.getMaxHeight()>MAX_HEIGHT) {
				return false;
			}

			if(edgeBuffer.getMaxDepth()>MAX_DEPTH) {
				return false;
			}

			return true;
		}

		public static CompactTreeEdgeStorageInt fromBuilder(StructureBuilder builder) {
			int nodeCount = builder.getNodeCount();
			if(nodeCount>MAX_NODE_COUNT)
				throw new IllegalArgumentException("Builder contains too many nodes for this implementation: "+nodeCount);

			int edgeCount = builder.getEdgeCount();
			if(edgeCount>MAX_EDGE_COUNT)
				throw new IllegalArgumentException("Builder contains too many edges for this implementation: "+edgeCount);


			final EdgeBuffer edgeBuffer = builder.edgeBuffer();

			if(edgeBuffer.getMaxHeight()>MAX_HEIGHT)
				throw new IllegalArgumentException("Tree exceeds maximum height for this implementation: "+edgeBuffer.getMaxHeight());
			if(edgeBuffer.getMaxDepth()>MAX_DEPTH)
				throw new IllegalArgumentException("Tree exceeds maximum depth for this implementation: "+edgeBuffer.getMaxDepth());

			final RootItem<Edge> root = builder.getRoot();

			final LookupList<Edge> edges = new LookupList<>(builder.edges());

			final int[] treeData = new int[nodeCount+1];
			final byte[] edgeData = new byte[2*nodeCount+edgeCount];

			int pointer = 0;

			for(int i=0; i<nodeCount; i++) {
				Item node = builder.getNodeAt(i);

				// Tree treeData
				int height = edgeBuffer.getHeight(node);
				int depth = edgeBuffer.getDepth(node)+1;
				int descendants = edgeBuffer.getDescendantsCount(node);

				int data = merge(pointer, height, depth, descendants);

				treeData[i+1] = data;

				// Edge treeData

				int incoming = -1;

				if(edgeBuffer.getEdgeCount(node, false)>0) {
					incoming = edges.indexOf(edgeBuffer.getEdgeAt(node, 0, false))+1;
				}

				edgeData[pointer] = toByte(incoming);

				int outgoingCount = edgeBuffer.getEdgeCount(node, true);

				edgeData[pointer+1] = toByte(outgoingCount);

				if(outgoingCount>0) {
					for(int idx = 0; idx<outgoingCount; idx++) {
						int outgoing = edges.indexOf(edgeBuffer.getEdgeAt(node, 0, true));
						edgeData[pointer+1+idx] = toByte(outgoing);
					}

					/*
					 * TODO maybe take 1 bit from the treeData entry and convert it to a 'sorted' flag,
					 * allowing to sort lazily when actually needed? Problem with this idea would be that
					 * the order reported by getEdgeAt() might be inconsistent over time.
					 */
					Arrays.sort(edgeData, pointer+2, pointer+2+outgoingCount);
				}

				pointer += 2+outgoingCount;
			}

			int rootData = merge(0, edgeBuffer.getHeight(root), 0, edgeBuffer.getDescendantsCount(root));

			treeData[0] = rootData;

			return new CompactTreeEdgeStorageInt(root, edges, treeData, edgeData);
		}

		private static int merge(int pointer, int height, int depth, int descendants) {
			return (pointer) | (height << 10) | (depth << 17) | (descendants << 24);
		}

		// 7,8 and 10 bit masks
		private static final int MASK_7 = 0x7F;
		private static final int MASK_8 = 0xFF;
		private static final int MASK_10 = 0x3FF;

		private static int extract(int data, int shifts, int mask) {
			return (data >>> shifts) & mask;
		}

		private static int pointer(int data) {
			return extract(data, 0, MASK_10);
		}

		private static int height(int data) {
			return extract(data, 10, MASK_7);
		}

		private static int depth(int data) {
			return extract(data, 17, MASK_7)-1;
		}

		private static int descendants(int data) {
			return extract(data, 24, MASK_8);
		}

		private static final byte OFFSET = Byte.MAX_VALUE;

		private static byte toByte(int value) {
			return (byte) (value-OFFSET);
		}

		private static int toInt(byte value) {
			return (int)value+OFFSET;
		}

		/*
		 *  10 Bits for section pointer, 7 bits for height and depth each
		 *  and 8 bits for descendants count
		 *
		 *  bits 00 - 09	index of section in edge array -1 or 0 if no edges
		 *  bits 10 - 16	height of node
		 *  bits 17 - 23	depth of node -1 or 0 if node not connected to virtual root
		 *  bits 24 - 31	descendant count of node
		 */
		private final int[] treeData;

		/*
		 * (2*n)+m bytes where n is the number of nodes not including the virtual root node
		 * and m the number of edges in the structure.
		 * Each node has a section of 2+x elements where x is the number of outgoing edges.
		 * The first element holds the index of the incoming edge +1 or 0 if the node has no
		 * incoming edge, the second element the number of edges that follow and the other
		 * elements each store the index of the respective edge in the global edge list.
		 */
		private final byte[] edgeData;

		CompactTreeEdgeStorageInt(RootItem<Edge> root, LookupList<Edge> edges,
				int[] treeData, byte[] edgeData) {
			super(root, edges);

			this.treeData = treeData;
			this.edgeData = edgeData;
		}

		private int localIndex(Structure context, Item node) {
			return IcarusUtils.ensureIntegerValueRange(context.indexOfItem(node))+1;
		}

		private int outgoing(int data, int index) {
			int pointer = pointer(data);
			int count = toInt(edgeData[pointer+1]);
			if(index<0 || index>=count)
				throw new IndexOutOfBoundsException(Messages.indexOutOfBoundsMessage(null, 0, count-1, index));

			return toInt(edgeData[pointer+1+index]);
		}

		private int outgoingCount(int data) {
			int pointer = pointer(data);
			return toInt(edgeData[pointer+1]);
		}

		private int incoming(int data) {
			int pointer = pointer(data);
			return toInt(edgeData[pointer])-1;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, boolean)
		 */
		@Override
		public long getEdgeCount(Structure context, Item node, boolean isSource) {
			if(node==root) {
				return root.edgeCount(!isSource);
			} else {
				int data = treeData[localIndex(context, node)];
				if(isSource) {
					return outgoingCount(data);
				} else {
					return incoming(data)>=0 ? 1 : 0;
				}
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getEdgeAt(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, long, boolean)
		 */
		@Override
		public Edge getEdgeAt(Structure context, Item node, long index,
				boolean isSource) {
			if(node==root) {
				return root.edgeAt(index, !isSource);
			} else {
				int data = treeData[localIndex(context, node)];
				if(isSource) {
					return getEdgeAt(outgoing(data, IcarusUtils.ensureIntegerValueRange(index)));
				} else {
					return getEdgeAt(incoming(data));
				}
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getHeight(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public long getHeight(Structure context, Item node) {
			if(node==root) {
				return height(treeData[0]);
			} else {
				int data = treeData[localIndex(context, node)];
				return height(data);
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getDepth(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public long getDepth(Structure context, Item node) {
			if(node==root) {
				return 0;
			} else {
				int data = treeData[localIndex(context, node)];
				return depth(data);
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getDescendantCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public long getDescendantCount(Structure context, Item parent) {
			if(parent==root) {
				return descendants(treeData[0]);
			} else {
				int data = treeData[localIndex(context, parent)];
				return descendants(data);
			}
		}

		private Edge incomingEdge(Structure context, Item node) {
			if(node==root) {
				return null;
			} else {
				int data = treeData[localIndex(context, node)];
				int incoming = incoming(data);

				if(incoming>=0) {
					return getEdgeAt(incoming);
				} else {
					return null;
				}
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getParent(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public Item getParent(Structure context, Item node) {
			Edge incomingEdge = incomingEdge(context, node);
			return incomingEdge==null ? null : incomingEdge.getSource();
		}

		private int indexOfEdge(Structure context, Item parent, Edge edge) {
			int data = treeData[localIndex(context, parent)];
			int pointer = pointer(data);
			byte gloablIndex = toByte(indexOfEdge(edge));

			int fromIndex = pointer+2;
			int toIndex = fromIndex+toInt(edgeData[pointer+1]);

			int index = Arrays.binarySearch(edgeData, fromIndex, toIndex, gloablIndex);
			if(index<0)
				throw new ModelException(ModelErrorCode.MODEL_CORRUPTED_STATE,
						"Edge not present in edge storage of source node: "+getName(edge));

			return index;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#indexOfChild(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public long indexOfChild(Structure context, Item child) {
			Edge incomingEdge = incomingEdge(context, child);
			if(incomingEdge==null) {
				return UNSET_LONG;
			} else if(incomingEdge.getSource()==root) {
				return root.indexOfEdge(incomingEdge);
			} else {
				return indexOfEdge(context, incomingEdge.getSource(), incomingEdge);
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getSiblingAt(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, long)
		 */
		@Override
		public Item getSiblingAt(Structure context, Item child, long offset) {
			int delta = IcarusUtils.ensureIntegerValueRange(offset);

			Edge incomingEdge = incomingEdge(context, child);
			if(incomingEdge==null) {
				return null;
			} else if(incomingEdge.getSource()==root) {
				int index = root.indexOfEdge(incomingEdge);
				return root.edgeAt(index+delta, false).getTarget();
			} else {
				int index = indexOfEdge(context, incomingEdge.getSource(), incomingEdge);
				return getEdgeAt(context, incomingEdge.getSource(), index+delta, false).getTarget();
			}
		}

	}

	/**
	 * Compact tree storage that stores general information about nodes in a long integer array
	 * and the actual information about edges for a node in a global short array. The short
	 * array allows addressing of {@value #MAX_EDGE_COUNT} edges with a single entry and
	 * requires 2+n short entries per node where n is the number of outgoing edges for that
	 * node. The 2 static short values are the global index of the incoming edge and the
	 * number of outgoing edges for the node (this value determines the number of subsequent
	 * entries allocated for the node in the short array).
	 * <p>
	 * Since height, depth, descendant count and the pointer to the section in the short array
	 * are all packed together into a single long value, the 64 bit long space is divided as
	 * follows:
	 * The first 18 bit are used for the pointer to the short array (this needed to be more than
	 * 16 bit, since the short array might exceed the address space of a single short in length).
	 * Then there are 15 bits for height and depth of the node each (which restricts the maximum
	 * height a node can have to {@value #MAX_HEIGHT} and the maximum depth to {@value #MAX_DEPTH}).
	 * The last 16 bits are reserved for the descendant count of the node, allowing for a single
	 * node to span the entire tree if necessary).
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class CompactTreeEdgeStorageLong extends StaticTreeEdgeStorage {

		public static final int MAX_NODE_COUNT = (1 << 16)-1;
		public static final int MAX_EDGE_COUNT = (1 << 18)-1;
		public static final int MAX_HEIGHT = (1 << 15)-1;

		/**
		 * No connection to root means {@code -1}.
		 */
		public static final int MAX_DEPTH = (1 << 15)-2;

		static boolean isCompatible(StructureBuilder builder) {
			if(builder.getNodeCount()>MAX_NODE_COUNT) {
				return false;
			}

			if(builder.getEdgeCount()>MAX_EDGE_COUNT) {
				return false;
			}

			final EdgeBuffer edgeBuffer = builder.edgeBuffer();

			if(edgeBuffer.getMaxHeight()>MAX_HEIGHT) {
				return false;
			}

			if(edgeBuffer.getMaxDepth()>MAX_DEPTH) {
				return false;
			}

			return true;
		}

		public static CompactTreeEdgeStorageLong fromBuilder(StructureBuilder builder) {
			int nodeCount = builder.getNodeCount();
			if(nodeCount>MAX_NODE_COUNT)
				throw new IllegalArgumentException("Builder contains too many nodes for this implementation: "+nodeCount);

			int edgeCount = builder.getEdgeCount();
			if(edgeCount>MAX_EDGE_COUNT)
				throw new IllegalArgumentException("Builder contains too many edges for this implementation: "+edgeCount);


			final EdgeBuffer edgeBuffer = builder.edgeBuffer();

			if(edgeBuffer.getMaxHeight()>MAX_HEIGHT)
				throw new IllegalArgumentException("Tree exceeds maximum height for this implementation: "+edgeBuffer.getMaxHeight());
			if(edgeBuffer.getMaxDepth()>MAX_DEPTH)
				throw new IllegalArgumentException("Tree exceeds maximum depth for this implementation: "+edgeBuffer.getMaxDepth());

			final RootItem<Edge> root = builder.getRoot();

			final LookupList<Edge> edges = new LookupList<>(builder.edges());

			final long[] treeData = new long[nodeCount+1];
			final short[] edgeData = new short[2*nodeCount+edgeCount];

			int pointer = 0;

			for(int i=0; i<nodeCount; i++) {
				Item node = builder.getNodeAt(i);

				// Tree treeData
				long height = edgeBuffer.getHeight(node);
				long depth = edgeBuffer.getDepth(node)+1;
				long descendants = edgeBuffer.getDescendantsCount(node);

				long data = merge(pointer, height, depth, descendants);

				treeData[i+1] = data;

				// Edge treeData

				int incoming = -1;

				if(edgeBuffer.getEdgeCount(node, false)>0) {
					incoming = edges.indexOf(edgeBuffer.getEdgeAt(node, 0, false))+1;
				}

				edgeData[pointer] = toShort(incoming);

				int outgoingCount = edgeBuffer.getEdgeCount(node, true);

				edgeData[pointer+1] = toShort(outgoingCount);

				if(outgoingCount>0) {
					for(int idx = 0; idx<outgoingCount; idx++) {
						int outgoing = edges.indexOf(edgeBuffer.getEdgeAt(node, 0, true));
						edgeData[pointer+1+idx] = toShort(outgoing);
					}

					/*
					 * TODO maybe take 1 bit from the treeData entry and convert it to a 'sorted' flag,
					 * allowing to sort lazily when actually needed? Problem with this idea would be that
					 * the order reported by getEdgeAt() might be inconsistent over time.
					 */
					Arrays.sort(edgeData, pointer+2, pointer+2+outgoingCount);
				}

				pointer += 2+outgoingCount;
			}

			long rootData = merge(0L, edgeBuffer.getHeight(root), 0L, edgeBuffer.getDescendantsCount(root));

			treeData[0] = rootData;

			return new CompactTreeEdgeStorageLong(root, edges, treeData, edgeData);
		}

		private static long merge(long pointer, long height, long depth, long descendants) {
			return (pointer) | (height << 18) | (depth << 33) | (descendants << 48);
		}

		// 15,16 and 18 bit masks
		private static final long MASK_15 = 0x7FFF;
		private static final long MASK_16 = 0xFFFF;
		private static final long MASK_18 = 0x3FFFF;

		private static int extract(long data, int shifts, long mask) {
			return (int)((data >>> shifts) & mask);
		}

		private static int pointer(long data) {
			return extract(data, 0, MASK_18);
		}

		private static int height(long data) {
			return extract(data, 18, MASK_15);
		}

		private static int depth(long data) {
			return extract(data, 33, MASK_15)-1;
		}

		private static int descendants(long data) {
			return extract(data, 48, MASK_16);
		}

		private static final short OFFSET = Short.MAX_VALUE;

		private static short toShort(int value) {
			return (short) (value-OFFSET);
		}

		private static int toInt(short value) {
			return (int)value+OFFSET;
		}

		/*
		 *  18 Bits for section pointer, 15 bits for height and depth each
		 *  and 16 bits for descendants count
		 *
		 *  bits 00 - 17	index of section in edge array -1 or 0 if no edges
		 *  bits 18 - 32	height of node
		 *  bits 33 - 47	depth of node -1 or 0 if node not connected to virtual root
		 *  bits 48 - 63	descendant count of node
		 */
		private final long[] treeData;

		/*
		 * (2*n)+m short where n is the number of nodes not including the virtual root node
		 * and m the number of edges in the structure.
		 * Each node has a section of 2+x elements where x is the number of outgoing edges.
		 * The first element holds the index of the incoming edge +1 or 0 if the node has no
		 * incoming edge, the second element the number of edges that follow and the other
		 * elements each store the index of the respective edge in the global edge list.
		 */
		private final short[] edgeData;

		CompactTreeEdgeStorageLong(RootItem<Edge> root, LookupList<Edge> edges,
				long[] treeData, short[] edgeData) {
			super(root, edges);

			this.treeData = treeData;
			this.edgeData = edgeData;
		}

		private int localIndex(Structure context, Item node) {
			return IcarusUtils.ensureIntegerValueRange(context.indexOfItem(node))+1;
		}

		private int outgoing(long data, int index) {
			int pointer = pointer(data);
			int count = toInt(edgeData[pointer+1]);
			if(index<0 || index>=count)
				throw new IndexOutOfBoundsException(Messages.indexOutOfBoundsMessage(null, 0, count-1, index));

			return toInt(edgeData[pointer+1+index]);
		}

		private int outgoingCount(long data) {
			int pointer = pointer(data);
			return toInt(edgeData[pointer+1]);
		}

		private int incoming(long data) {
			int pointer = pointer(data);
			return toInt(edgeData[pointer])-1;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, boolean)
		 */
		@Override
		public long getEdgeCount(Structure context, Item node, boolean isSource) {
			if(node==root) {
				return root.edgeCount(!isSource);
			} else {
				long data = treeData[localIndex(context, node)];
				if(isSource) {
					return outgoingCount(data);
				} else {
					return incoming(data)>=0 ? 1 : 0;
				}
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getEdgeAt(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, long, boolean)
		 */
		@Override
		public Edge getEdgeAt(Structure context, Item node, long index,
				boolean isSource) {
			if(node==root) {
				return root.edgeAt(index, !isSource);
			} else {
				long data = treeData[localIndex(context, node)];
				if(isSource) {
					return getEdgeAt(outgoing(data, IcarusUtils.ensureIntegerValueRange(index)));
				} else {
					return getEdgeAt(incoming(data));
				}
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getHeight(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public long getHeight(Structure context, Item node) {
			if(node==root) {
				return height(treeData[0]);
			} else {
				long data = treeData[localIndex(context, node)];
				return height(data);
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getDepth(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public long getDepth(Structure context, Item node) {
			if(node==root) {
				return 0;
			} else {
				long data = treeData[localIndex(context, node)];
				return depth(data);
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getDescendantCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public long getDescendantCount(Structure context, Item parent) {
			if(parent==root) {
				return descendants(treeData[0]);
			} else {
				long data = treeData[localIndex(context, parent)];
				return descendants(data);
			}
		}

		private Edge incomingEdge(Structure context, Item node) {
			if(node==root) {
				return null;
			} else {
				long data = treeData[localIndex(context, node)];
				int incoming = incoming(data);

				if(incoming>=0) {
					return getEdgeAt(incoming);
				} else {
					return null;
				}
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getParent(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public Item getParent(Structure context, Item node) {
			Edge incomingEdge = incomingEdge(context, node);
			return incomingEdge==null ? null : incomingEdge.getSource();
		}

		private int indexOfEdge(Structure context, Item parent, Edge edge) {
			long data = treeData[localIndex(context, parent)];
			int pointer = pointer(data);
			short gloablIndex = toShort(indexOfEdge(edge));

			int fromIndex = pointer+2;
			int toIndex = fromIndex+toInt(edgeData[pointer+1]);

			int index = Arrays.binarySearch(edgeData, fromIndex, toIndex, gloablIndex);
			if(index<0)
				throw new ModelException(ModelErrorCode.MODEL_CORRUPTED_STATE,
						"Edge not present in edge storage of source node: "+getName(edge));

			return index;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#indexOfChild(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public long indexOfChild(Structure context, Item child) {
			Edge incomingEdge = incomingEdge(context, child);
			if(incomingEdge==null) {
				return UNSET_LONG;
			} else if(incomingEdge.getSource()==root) {
				return root.indexOfEdge(incomingEdge);
			} else {
				return indexOfEdge(context, incomingEdge.getSource(), incomingEdge);
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getSiblingAt(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, long)
		 */
		@Override
		public Item getSiblingAt(Structure context, Item child, long offset) {
			int delta = IcarusUtils.ensureIntegerValueRange(offset);

			Edge incomingEdge = incomingEdge(context, child);
			if(incomingEdge==null) {
				return null;
			} else if(incomingEdge.getSource()==root) {
				int index = root.indexOfEdge(incomingEdge);
				return root.edgeAt(index+delta, false).getTarget();
			} else {
				int index = indexOfEdge(context, incomingEdge.getSource(), incomingEdge);
				return getEdgeAt(context, incomingEdge.getSource(), index+delta, false).getTarget();
			}
		}

	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class LargeTreeEdgeStorage extends StaticTreeEdgeStorage {

		private static int NO_INDEX = (int) ModelConstants.UNSET_LONG;

		public static LargeTreeEdgeStorage fromBuilder(StructureBuilder builder) {
			final EdgeBuffer edgeBuffer = builder.edgeBuffer();
			final RootItem<Edge> root = builder.getRoot();
			final LookupList<Edge> edges = new LookupList<>(builder.edges());
			final int nodeCount = builder.getNodeCount();

			final Node[] treeData = new Node[nodeCount+1];

			for(int i=0; i<nodeCount; i++) {
				Item node = builder.getNodeAt(i);

				treeData[i+1] = StaticNodes.createNode(node, edgeBuffer, edges, true);
			}

			treeData[0] = new StaticNodes.RootNode(
					edgeBuffer.getHeight(root),
					edgeBuffer.getDescendantsCount(root));

			return new LargeTreeEdgeStorage(root, edges, treeData);
		}

		private final Node[] treeData;

		/**
		 * @param root
		 * @param edges
		 */
		LargeTreeEdgeStorage(RootItem<Edge> root, LookupList<Edge> edges, Node[] treeData) {
			super(root, edges);

			this.treeData = treeData;
		}

		private int localIndex(Structure context, Item node) {
			long index = context.indexOfItem(node);
			if(index==NO_INDEX)
				throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER, "Given node is not a member of this tree: "+getName(node));

			return IcarusUtils.ensureIntegerValueRange(index)+1;
		}

		private Node getData(Structure context, Item node) {
			return treeData[localIndex(context, node)];
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, boolean)
		 */
		@Override
		public long getEdgeCount(Structure context, Item node, boolean isSource) {
			if(node==root) {
				return root.edgeCount(!isSource);
			} else {
				Node data = getData(context, node);
				if(data==null) {
					return 0L;
				}

				return isSource ? data.outgoingEdgeCount() : data.incomingEdgeCount();
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getEdgeAt(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, long, boolean)
		 */
		@Override
		public Edge getEdgeAt(Structure context, Item node, long index,
				boolean isSource) {
			if(node==root) {
				return root.edgeAt(index, !isSource);
			} else {
				int idx = IcarusUtils.ensureIntegerValueRange(index);
				Node data = getData(context, node);
				if(data==null)
					throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS, "No edge for index: "+idx);

				return edges.get(isSource ? data.outgoingEdgeAt(idx) : data.incomingEdgeAt(idx));
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getHeight(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public long getHeight(Structure context, Item node) {
			if(node==root) {
				return treeData[0].height();
			} else {
				Node data = getData(context, node);
				return data==null ? 0 : data.height();
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getDepth(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public long getDepth(Structure context, Item node) {
			if(node==root) {
				return 0;
			} else {
				Node data = getData(context, node);
				return data==null ? NO_INDEX : data.depth();
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getDescendantCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public long getDescendantCount(Structure context, Item parent) {
			if(parent==root) {
				return treeData[0].descendants();
			} else {
				Node data = getData(context, parent);
				return data==null ? 0 : data.descendants();
			}
		}

		private int incomingEdge(Structure context, Item node) {
			if(node==root) {
				return NO_INDEX;
			} else {
				Node data = getData(context, node);
				return (data==null || data.incomingEdgeCount()==0) ? NO_INDEX : data.incomingEdgeAt(0);
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getParent(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public Item getParent(Structure context, Item node) {
			int edgeIndex = incomingEdge(context, node);
			return edgeIndex==NO_INDEX ? null : edges.get(edgeIndex).getSource();
		}

		private int indexOfEdge(Structure context, Item parent, int edgeIndex) {
			Node data = getData(context, parent);

			return data.indexOfOutgoingEdge(edgeIndex);
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#indexOfChild(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public long indexOfChild(Structure context, Item child) {
			int edgeIndex = incomingEdge(context, child);

			if(edgeIndex==NO_INDEX) {
				return NO_INDEX;
			}

			Edge edge = edges.get(edgeIndex);

			if(edge.getSource()==root) {
				return root.indexOfEdge(edge);
			} else {
				return indexOfEdge(context, edge.getSource(), edgeIndex);
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getSiblingAt(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, long)
		 */
		@Override
		public Item getSiblingAt(Structure context, Item child, long offset) {
			int delta = IcarusUtils.ensureIntegerValueRange(offset);
			int edgeIndex = incomingEdge(context, child);

			if(edgeIndex==NO_INDEX) {
				return null;
			}

			Edge edge = edges.get(edgeIndex);

			if(edge.getSource()==root) {
				int index = root.indexOfEdge(edge);
				return root.edgeAt(index+delta, false).getTarget();
			} else {
				int index = indexOfEdge(context, edge.getSource(), edgeIndex);
				return getEdgeAt(context, edge.getSource(), index+delta, false).getTarget();
			}
		}
	}
}
