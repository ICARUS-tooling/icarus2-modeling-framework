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

 * $Revision: 421 $
 * $Date: 2015-08-06 11:54:09 +0200 (Do, 06 Aug 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/structure/builder/StaticChainEdgeStorage.java $
 *
 * $LastChangedDate: 2015-08-06 11:54:09 +0200 (Do, 06 Aug 2015) $
 * $LastChangedRevision: 421 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members.structure.builder;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.standard.members.structure.RootItem;
import de.ims.icarus2.util.collections.LookupList;

/**
 * @author Markus Gärtner
 * @version $Id: StaticChainEdgeStorage.java 421 2015-08-06 09:54:09Z mcgaerty $
 *
 */
public abstract class StaticChainEdgeStorage extends AbstractStaticEdgeStorage<RootItem<?>> {

	/**
	 * Break even point at which the complete and sparse chain storage implementations
	 * take up the same amount of memory. Below that threshold the sparse version is
	 * more efficient.
	 */
	private static double LOAD_THRESHOLD = -1;

	static boolean isSparse(StructureBuilder builder) {
		if(LOAD_THRESHOLD<0) {
			/*
			 * B_f = bytes required for a full entry in the partial chain implementation
			 * B_e = bytes required for an empty entry in the partial chain implementation
			 * B_d = bytes required for the default full implementation
			 *
			 * x = load factor [0..1]
			 *
			 * 	=>	x*B_f + (1-x)*B_e = B_d
			 *  => x = (B_d - B_e)/(B_f - B_e)
			 */

			double bytesFullEntry = LargeThinChainEdgeStorage.USED_ENTRY_SIZE;
			double bytesEmptyEntry = LargeThinChainEdgeStorage.UNUSED_ENTRY_SIZE;
			double bytesDefaultEntry = LargeCompleteChainEdgeStorage.ENTRY_SIZE;

			LOAD_THRESHOLD = (bytesDefaultEntry-bytesEmptyEntry) / (bytesFullEntry - bytesEmptyEntry);
		}

		double nodeCount = builder.getNodeCount();
		double edgeCount = builder.getEdgeCount();

		return (edgeCount/nodeCount) < LOAD_THRESHOLD;
	}

	public static StaticChainEdgeStorage fromBuilder(StructureBuilder builder) {
		EdgeBuffer edgeBuffer = builder.edgeBuffer();

		if(edgeBuffer.getMaxIncoming()>1)
			throw new IllegalArgumentException(
					"Cannot build chain storage from buffer - maximum incoming edge count exceeded: "+edgeBuffer.getMaxIncoming());
		if(edgeBuffer.getMaxOutgoing()>1)
			throw new IllegalArgumentException(
					"Cannot build chain storage from buffer - maximum outgoing edge count exceeded: "+edgeBuffer.getMaxOutgoing());

		int nodeCount = builder.getNodeCount();

		if(nodeCount<=CompactChainEdgeStorageInt.MAX_NODE_COUNT) {
			return CompactChainEdgeStorageInt.fromBuilder(builder);
		} else if(nodeCount<=CompactChainEdgeStorageLong.MAX_NODE_COUNT) {
			return CompactChainEdgeStorageLong.fromBuilder(builder);
		} else if(isSparse(builder)) {
			return LargeThinChainEdgeStorage.fromBuilder(builder);
		} else {
			return LargeCompleteChainEdgeStorage.fromBuilder(builder);
		}
	}

	StaticChainEdgeStorage(RootItem<Edge> root, LookupList<Edge> edges) {
		super(root, edges);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getStructureType()
	 */
	@Override
	public StructureType getStructureType() {
		return StructureType.CHAIN;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getSiblingAt(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, long)
	 */
	@Override
	public Item getSiblingAt(Structure context, Item child, long offset) {
		throw new ModelException(ModelErrorCode.UNSUPPORTED_OPERATION, "Nodes in CHAIN structure can't have siblings");
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getParent(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public Item getParent(Structure context, Item node) {
		if(getEdgeCount(context, node, false) > 0) {
			return getEdgeAt(context, node, 0, false).getSource();
		}
		return null;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#indexOfChild(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long indexOfChild(Structure context, Item child) {
		if(getEdgeCount(context, child, false) > 0) {
			return 0;
		}
		return NO_INDEX;
	}

	/**
	 * A very compact chain implementation that uses a single integer field per node to store
	 * the indices of the incoming and outgoing edge as well as the height and depth of the
	 * node itself. Due to this shared use of a 32 bit storage, this implementation can only
	 * address a maximum of {@value #MAX_NODE_COUNT} items/edges.
	 *
	 * @author Markus Gärtner
	 * @version $Id: StaticChainEdgeStorage.java 421 2015-08-06 09:54:09Z mcgaerty $
	 *
	 */
	public static class CompactChainEdgeStorageInt extends StaticChainEdgeStorage {

		public static final int MAX_NODE_COUNT = (1 << 8)-1;

		public static CompactChainEdgeStorageInt fromBuilder(StructureBuilder builder) {
			int nodeCount = builder.getNodeCount();
			if(nodeCount>MAX_NODE_COUNT)
				throw new IllegalArgumentException("Builder contains too many nodes for this implementation: "+nodeCount);

			final EdgeBuffer edgeBuffer = builder.edgeBuffer();

			final RootItem<Edge> root = builder.root();

			final LookupList<Edge> edges = new LookupList<>(builder.edges());

			final int[] chainData = new int[nodeCount+1];

			for(int i=0; i<nodeCount; i++) {
				Item node = builder.getNodeAt(i);

				int height = edgeBuffer.getHeight(node);
				int depth = edgeBuffer.getDepth(node)+1;
				int incoming = 0;
				int outgoing = 0;

				if(edgeBuffer.getEdgeCount(node, false)>0) {
					incoming = edges.indexOf(edgeBuffer.getEdgeAt(node, 0, false))+1;
				}

				if(edgeBuffer.getEdgeCount(node, true)>0) {
					outgoing = edges.indexOf(edgeBuffer.getEdgeAt(node, 0, true))+1;
				}

				int data = 0;
				data |= (incoming);
				data |= (outgoing << 8);
				data |= (height <<16);
				data |= (depth <<24);

				chainData[i+1] = data;
			}

			int rootData = 0;
			rootData |= (edgeBuffer.getHeight(root) << 16);
			rootData |= (edgeBuffer.getDescendantsCount(root) << 24);

			chainData[0] = rootData;

			return new CompactChainEdgeStorageInt(root, edges, chainData);
		}

		// 8-bit mask for values ranging from 0 to 255
		private static final int MASK = 0xFF;

		private static int extract(int data, int shifts) {
			return (data >>> shifts) & MASK;
		}

		private static int incoming(int data) {
			return extract(data, 0)-1;
		}

		private static int outgoing(int data) {
			return extract(data, 8)-1;
		}

		private static int height(int data) {
			return extract(data, 16);
		}

		private static int depth(int data) {
			return extract(data, 24)-1;
		}

		private static int descendants(int data) {
			return extract(data, 24);
		}

		/*
		 *  8 Bits for incoming, outgoing, height and depth each
		 *
		 *  bits 00 - 07	index of incoming edge +1 or 0 if no incoming edge
		 *  bits 08 - 15	index of outgoing edge +1 or 0 if no incoming edge
		 *  bits 16 - 23	height of node
		 *  bits 24 - 31	depth of node +1 or 0 if node not connected to virtual root
		 */
		private final int[] chainData;

		/**
		 * @param root
		 * @param edges
		 */
		CompactChainEdgeStorageInt(RootItem<Edge> root, LookupList<Edge> edges, int[] chainData) {
			super(root, edges);

			if (chainData == null)
				throw new NullPointerException("Invalid chainData");

			this.chainData = chainData;
		}

		private int localIndex(Structure context, Item node) {
			return ensureIntegerValueRange(context.indexOfItem(node))+1;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, boolean)
		 */
		@Override
		public long getEdgeCount(Structure context, Item node, boolean isSource) {
			if(node==root) {
				return root.edgeCount(!isSource);
			} else {
				int data = chainData[localIndex(context, node)];
				if(isSource && outgoing(data)>=0) {
					return 1;
				} else if(!isSource && incoming(data)>=0) {
					return 1;
				} else {
					return 0;
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
				int data = chainData[localIndex(context, node)];
				if(isSource) {
					return getEdgeAt(context, outgoing(data));
				} else {
					return getEdgeAt(context, incoming(data));
				}
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getHeight(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public long getHeight(Structure context, Item node) {
			if(node==root) {
				return height(chainData[0]);
			} else {
				int data = chainData[localIndex(context, node)];
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
				int data = chainData[localIndex(context, node)];
				return depth(data);
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getDescendantCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public long getDescendantCount(Structure context, Item parent) {
			if(parent==root) {
				return descendants(chainData[0]);
			} else {
				int data = chainData[localIndex(context, parent)];
				return height(data);
			}
		}
	}

	/**
	 * A very compact chain implementation that uses a single long field per node to store
	 * the indices of the incoming and outgoing edge as well as the height and depth of the
	 * node itself. Due to this shared use of a 64 bit storage, this implementation can only
	 * address a maximum of {@value #MAX_NODE_COUNT} items/edges.
	 *
	 * @author Markus Gärtner
	 * @version $Id: StaticChainEdgeStorage.java 421 2015-08-06 09:54:09Z mcgaerty $
	 *
	 */
	public static class CompactChainEdgeStorageLong extends StaticChainEdgeStorage {

		public static final int MAX_NODE_COUNT = (1 << 16)-1;

		public static CompactChainEdgeStorageLong fromBuilder(StructureBuilder builder) {
			int nodeCount = builder.getNodeCount();
			if(nodeCount>MAX_NODE_COUNT)
				throw new IllegalArgumentException("Builder contains too many nodes for this implementation: "+nodeCount);

			final EdgeBuffer edgeBuffer = builder.edgeBuffer();

			final RootItem<Edge> root = builder.root();

			final LookupList<Edge> edges = new LookupList<>(builder.edges());

			final long[] chainData = new long[nodeCount+1];

			for(int i=0; i<nodeCount; i++) {
				Item node = builder.getNodeAt(i);

				long height = edgeBuffer.getHeight(node);
				long depth = edgeBuffer.getDepth(node)+1;
				long incoming = 0;
				long outgoing = 0;

				if(edgeBuffer.getEdgeCount(node, false)>0) {
					incoming = edges.indexOf(edgeBuffer.getEdgeAt(node, 0, false))+1;
				}

				if(edgeBuffer.getEdgeCount(node, true)>0) {
					outgoing = edges.indexOf(edgeBuffer.getEdgeAt(node, 0, true))+1;
				}

				long data = 0;
				data |= (incoming);
				data |= (outgoing << 16);
				data |= (height << 32);
				data |= (depth << 48);

				chainData[i+1] = data;
			}

			long rootData = 0;
			rootData |= ((long)edgeBuffer.getHeight(root) << 32);
			rootData |= ((long)edgeBuffer.getDescendantsCount(root) << 48);

			chainData[0] = rootData;

			return new CompactChainEdgeStorageLong(root, edges, chainData);
		}

		// 8-bit mask for values ranging from 0 to 255
		private static final long MASK = 0xFFFF;

		private static int extract(long data, int shifts) {
			return (int) ((data >>> shifts) & MASK);
		}

		private static int incoming(long data) {
			return extract(data, 0)-1;
		}

		private static int outgoing(long data) {
			return extract(data, 16)-1;
		}

		private static int height(long data) {
			return extract(data, 32);
		}

		private static int depth(long data) {
			return extract(data, 48)-1;
		}

		private static int descendants(long data) {
			return extract(data, 48);
		}

		/*
		 *  16 Bits for incoming, outgoing, height and depth each
		 *
		 *  bits 00 - 15	index of incoming edge +1 or 0 if no incoming edge
		 *  bits 16 - 31	index of outgoing edge +1 or 0 if no incoming edge
		 *  bits 32 - 47	height of node
		 *  bits 48 - 63	depth of node +1 or 0 if node not connected to virtual root
		 */
		private final long[] chainData;

		/**
		 * @param root
		 * @param edges
		 */
		CompactChainEdgeStorageLong(RootItem<Edge> root, LookupList<Edge> edges, long[] chainData) {
			super(root, edges);

			if (chainData == null)
				throw new NullPointerException("Invalid chainData");

			this.chainData = chainData;
		}

		private int localIndex(Structure context, Item node) {
			return ensureIntegerValueRange(context.indexOfItem(node))+1;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, boolean)
		 */
		@Override
		public long getEdgeCount(Structure context, Item node, boolean isSource) {
			if(node==root) {
				return root.edgeCount(!isSource);
			} else {
				long data = chainData[localIndex(context, node)];
				if(isSource && outgoing(data)>=0) {
					return 1;
				} else if(!isSource && incoming(data)>=0) {
					return 1;
				} else {
					return 0;
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
				long data = chainData[localIndex(context, node)];
				if(isSource) {
					return getEdgeAt(context, outgoing(data));
				} else {
					return getEdgeAt(context, incoming(data));
				}
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getHeight(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public long getHeight(Structure context, Item node) {
			if(node==root) {
				return height(chainData[0]);
			} else {
				long data = chainData[localIndex(context, node)];
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
				long data = chainData[localIndex(context, node)];
				return depth(data);
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getDescendantCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public long getDescendantCount(Structure context, Item parent) {
			if(parent==root) {
				return descendants(chainData[0]);
			} else {
				long data = chainData[localIndex(context, parent)];
				return height(data);
			}
		}
	}

	public static class LargeCompleteChainEdgeStorage extends StaticChainEdgeStorage {

		/**
		 * Bytes per entry (2 * long)
		 */
		public static final int ENTRY_SIZE = 2*8;

		public static LargeCompleteChainEdgeStorage fromBuilder(StructureBuilder builder) {
			int nodeCount = builder.getNodeCount();

			final EdgeBuffer edgeBuffer = builder.edgeBuffer();

			final RootItem<Edge> root = builder.root();

			final LookupList<Edge> edges = new LookupList<>(builder.edges());

			final long[] linkData = new long[nodeCount+1];
			final long[] sizeData = new long[nodeCount+1];

			for(int i=0; i<nodeCount; i++) {
				Item node = builder.getNodeAt(i);

				long height = edgeBuffer.getHeight(node);
				long depth = edgeBuffer.getDepth(node)+1;
				long incoming = 0;
				long outgoing = 0;

				if(edgeBuffer.getEdgeCount(node, false)>0) {
					incoming = edges.indexOf(edgeBuffer.getEdgeAt(node, 0, false));
				}

				if(edgeBuffer.getEdgeCount(node, true)>0) {
					outgoing = edges.indexOf(edgeBuffer.getEdgeAt(node, 0, true));
				}

				long data = 0;
				data |= (incoming);
				data |= (outgoing << 32);

				linkData[i+1] = data;

				data = 0;
				data |= (height);
				data |= (depth << 32);

				sizeData[i+1] = data;
			}

			long rootData = 0;
			rootData |= ((long)edgeBuffer.getHeight(root));
			rootData |= ((long)edgeBuffer.getDescendantsCount(root) << 32);

			sizeData[0] = rootData;

			return new LargeCompleteChainEdgeStorage(root, edges, linkData, sizeData);
		}

		private static int incoming(long data) {
			return (int) (data);
		}

		private static int outgoing(long data) {
			return (int) (data >>> 32);
		}

		private static int height(long data) {
			return (int) (data);
		}

		private static int depth(long data) {
			return (int) (data >>> 32);
		}

		private static int descendants(long data) {
			return (int) (data >>> 32);
		}

		/*
		 *  32 Bits for incoming and outgoing each
		 *
		 *  bits 00 - 31	index of incoming edge -1 or 0 if no incoming edge
		 *  bits 32 - 63	index of outgoing edge -1 or 0 if no incoming edge
		 */
		private final long[] linkData;

		/*
		 *  32 Bits for height and depth each
		 *
		 *  bits 0 - 31		height of node
		 *  bits 32 - 63	depth of node -1 or 0 if node not connected to virtual root
		 */
		private final long[] sizeData;

		LargeCompleteChainEdgeStorage(RootItem<Edge> root, LookupList<Edge> edges, long[] linkData, long[] sizeData) {
			super(root, edges);

			if (linkData == null)
				throw new NullPointerException("Invalid linkData");
			if (sizeData == null)
				throw new NullPointerException("Invalid sizeData");

			this.linkData = linkData;
			this.sizeData = sizeData;
		}

		private int localIndex(Structure context, Item node) {
			return ensureIntegerValueRange(context.indexOfItem(node))+1;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, boolean)
		 */
		@Override
		public long getEdgeCount(Structure context, Item node, boolean isSource) {
			if(node==root) {
				return root.edgeCount(!isSource);
			} else {
				long data = linkData[localIndex(context, node)];
				if(isSource && outgoing(data)>=0) {
					return 1;
				} else if(!isSource && incoming(data)>=0) {
					return 1;
				} else {
					return 0;
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
				long data = linkData[localIndex(context, node)];
				if(isSource) {
					return getEdgeAt(context, outgoing(data));
				} else {
					return getEdgeAt(context, incoming(data));
				}
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getHeight(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public long getHeight(Structure context, Item node) {
			if(node==root) {
				return height(sizeData[0]);
			} else {
				long data = sizeData[localIndex(context, node)];
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
				long data = sizeData[localIndex(context, node)];
				return depth(data);
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getDescendantCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public long getDescendantCount(Structure context, Item parent) {
			if(parent==root) {
				return descendants(sizeData[0]);
			} else {
				long data = sizeData[localIndex(context, parent)];
				return height(data);
			}
		}

	}

	public static class LargeThinChainEdgeStorage extends StaticChainEdgeStorage {

		public static final int UNUSED_ENTRY_SIZE;
		public static final int USED_ENTRY_SIZE;
		static {

			int size = 8 + 4*4;

			int pointerSize = 4;

			String arch = System.getProperty("sun.arch.data.model");
			if(arch.contains("64")) {
				pointerSize = 8;
			}

			UNUSED_ENTRY_SIZE = pointerSize;
			USED_ENTRY_SIZE = size + pointerSize;
		}

		public static final int MAX_NODE_COUNT = (1 << 8)-1;

		public static LargeThinChainEdgeStorage fromBuilder(StructureBuilder builder) {
			int nodeCount = builder.getNodeCount();
			if(nodeCount>MAX_NODE_COUNT)
				throw new IllegalArgumentException("Builder contains too many nodes for this implementation: "+nodeCount);

			final EdgeBuffer edgeBuffer = builder.edgeBuffer();

			final RootItem<Edge> root = builder.root();

			final LookupList<Edge> edges = new LookupList<>(builder.edges());

			final NodeInfo[] chainData = new NodeInfo[nodeCount+1];

			for(int i=0; i<nodeCount; i++) {
				Item node = builder.getNodeAt(i);

				int height = edgeBuffer.getHeight(node);
				int depth = edgeBuffer.getDepth(node);

				if(height==0 && depth==-1) {
					continue;
				}

				int incoming = -1;
				int outgoing = -1;

				if(edgeBuffer.getEdgeCount(node, false)>0) {
					incoming = edges.indexOf(edgeBuffer.getEdgeAt(node, 0, false));
				}

				if(edgeBuffer.getEdgeCount(node, true)>0) {
					outgoing = edges.indexOf(edgeBuffer.getEdgeAt(node, 0, true));
				}

				chainData[i+1] = new NodeInfo(incoming, outgoing, height, depth);
			}

			chainData[0] = new NodeInfo(-1, -1, edgeBuffer.getHeight(root), edgeBuffer.getDescendantsCount(root));

			return new LargeThinChainEdgeStorage(root, edges, chainData);
		}

		private final NodeInfo[] chainData;

		LargeThinChainEdgeStorage(RootItem<Edge> root, LookupList<Edge> edges, NodeInfo[] chainData) {
			super(root, edges);

			if (chainData == null)
				throw new NullPointerException("Invalid chainData");

			this.chainData = chainData;
		}

		private int localIndex(Structure context, Item node) {
			return ensureIntegerValueRange(context.indexOfItem(node))+1;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, boolean)
		 */
		@Override
		public long getEdgeCount(Structure context, Item node, boolean isSource) {
			if(node==root) {
				return root.edgeCount(!isSource);
			} else {
				NodeInfo data = chainData[localIndex(context, node)];
				if(isSource && data.outgoing>=0) {
					return 1;
				} else if(!isSource && data.incoming>=0) {
					return 1;
				} else {
					return 0;
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
				NodeInfo data = chainData[localIndex(context, node)];
				if(isSource) {
					return getEdgeAt(context, data.outgoing);
				} else {
					return getEdgeAt(context, data.incoming);
				}
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getHeight(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public long getHeight(Structure context, Item node) {
			if(node==root) {
				return chainData[0].height;
			} else {
				return chainData[localIndex(context, node)].height;
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
				return chainData[localIndex(context, node)].depth;
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getDescendantCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public long getDescendantCount(Structure context, Item parent) {
			if(parent==root) {
				return chainData[0].depth;
			} else {
				return chainData[localIndex(context, parent)].height;
			}
		}

	}

	private static class NodeInfo {

		int incoming, outgoing, height, depth;

		NodeInfo(int incoming, int outgoing, int height, int depth) {
			this.incoming = incoming;
			this.outgoing = outgoing;
			this.height = height;
			this.depth = depth;
		}
	}
}
