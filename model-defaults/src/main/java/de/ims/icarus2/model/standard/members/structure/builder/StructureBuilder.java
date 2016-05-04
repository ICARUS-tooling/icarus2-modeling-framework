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

 * $Revision: 457 $
 * $Date: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/structure/builder/StructureBuilder.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members.structure.builder;

import static de.ims.icarus2.model.standard.util.CorpusUtils.isVirtual;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotNull;
import static de.ims.icarus2.util.Conditions.checkState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.standard.members.container.ItemStorage;
import de.ims.icarus2.model.standard.members.item.DefaultItem;
import de.ims.icarus2.model.standard.members.structure.DefaultEdge;
import de.ims.icarus2.model.standard.members.structure.DefaultStructure;
import de.ims.icarus2.model.standard.members.structure.EdgeStorage;
import de.ims.icarus2.model.standard.members.structure.EmptyEdgeStorage;
import de.ims.icarus2.model.standard.members.structure.RootItem;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.collections.DataSet;

/**
 * @author Markus Gärtner
 * @version $Id: StructureBuilder.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
public class StructureBuilder {

	public static StructureBuilder newBuilder(StructureManifest manifest) {
		checkNotNull(manifest);

		StructureBuilder builder = new StructureBuilder(manifest);

		return builder;
	}

	private StructureBuilder(StructureManifest manifest) {
		this.manifest = manifest;

		edgeBuffer = new EdgeBuffer();
	}

	// Default settings

	private static final int DEFAULT_NODE_CAPACITY = 200;
	private static final int DEFAULT_EDGE_CAPACITY = 200;
	private static final SortType DEFAULT_NODE_SORT_TYPE = SortType.NATURAL;
	private static final SortType DEFAULT_EDGE_SORT_TYPE = SortType.NATURAL;

	// Static settings

	private final StructureManifest manifest;

	private boolean storeOffsets;
	private boolean mayHaveMultiRoots;

	// Modifiable settings

	private int nodeCapacity = -1;
	private int edgeCapacity = -1;

	private SortType nodeSortType = null;
	private SortType edgeSortType = null;

	private Comparator<? super Item> nodeSorter = null;
	private Comparator<? super Edge> edgeSorter = null;

	// Buffer

	private List<Item> nodes;
	private List<Edge> edges;

	private ItemStorage itemStorage;
	private EdgeStorage edgeStorage;

	private DataSet<Container> baseContainers;
	private Container boundaryContainer;

	private Structure proxyStructure;

	private RootItem<?> root;

	private final EdgeBuffer edgeBuffer;

	private StaticStructure currentStructure;

	private boolean augmented = false;

	@Override
	public String toString() {
		return new StringBuilder()
		.append("[StructureBuilder")
		.append(" storeOffsets=").append(storeOffsets)
		.append(" mayHaveMultiRoots=").append(mayHaveMultiRoots)
		.append(" nodeCapacity=").append(nodeCapacity)
		.append(" edgeCapacity=").append(edgeCapacity)
		.append(" nodeSortType=").append(nodeSortType)
		.append(" edgeSortType=").append(edgeSortType)
		.append(" nodeCount=").append(nodes==null ? 0 : nodes.size())
		.append(" edgeCount=").append(nodes==null ? 0 : edges.size())
		.append(" maxHeight=").append(edgeBuffer.getMaxHeight())
		.append(" maxDepth=").append(edgeBuffer.getMaxDepth())
		.append(" maxDescendantCount=").append(edgeBuffer.getMaxDescendantsCount())
		.append(" minIncoming=").append(edgeBuffer.getMinIncoming())
		.append(" maxIncoming=").append(edgeBuffer.getMaxIncoming())
		.append(" minOutgoing=").append(edgeBuffer.getMinOutgoing())
		.append(" maxPutgoing=").append(edgeBuffer.getMaxOutgoing())
		.append(']')
		.toString();
	};

	List<Item> nodes() {
		if(nodes==null) {
			nodes = new ArrayList<>(getNodeCapacity());
		}

		return nodes;
	}

	List<Edge> edges() {
		if(edges==null) {
			edges = new ArrayList<>(getEdgeCapacity());
		}

		return edges;
	}

	@SuppressWarnings("unchecked")
	<R extends RootItem<?>> R root() {
		if(root==null) {
			root = RootItem.forManifest(manifest);
		}

		return (R) root;
	}

	DataSet<Container> baseContainers() {
		return baseContainers==null ? DataSet.emptySet() : baseContainers;
	}

	Container boundaryContainer() {
		return boundaryContainer;
	}

	EdgeBuffer edgeBuffer() {
		return edgeBuffer;
	}

	Structure proxyStructure() {
		if(proxyStructure==null) {
			proxyStructure = new DefaultStructure() {

				@Override
				public ContainerType getContainerType() {
					return manifest.getContainerType();
				}

				@Override
				public StructureType getStructureType() {
					return manifest.getStructureType();
				}

				@Override
				public StructureManifest getManifest() {
					return manifest;
				}

				@Override
				public DataSet<Container> getBaseContainers() {
					return baseContainers();
				}

				@Override
				public Container getBoundaryContainer() {
					return boundaryContainer();
				}

			};
		}

		return proxyStructure;
	}

	StaticStructure currentStructure() {
		if(currentStructure==null) {
			currentStructure = new StaticStructure();
		}

		return currentStructure;
	}

	//**********************************
	//    CONFIGURATION METHODS
	//**********************************

	public StructureBuilder nodeCapacity(int capacity) {
		checkState(nodeCapacity==-1);
		checkArgument(capacity>0);

		nodeCapacity = capacity;

		return this;
	}

	int getNodeCapacity() {
		return nodeCapacity==-1 ? DEFAULT_NODE_CAPACITY : nodeCapacity;
	}

	public StructureBuilder edgeCapacity(int capacity) {
		checkState(edgeCapacity==-1);
		checkArgument(capacity>0);

		edgeCapacity = capacity;

		return this;

	}

	int getEdgeCapacity() {
		return edgeCapacity==-1 ? DEFAULT_EDGE_CAPACITY : edgeCapacity;
	}

	public StructureBuilder sortingNodes(SortType sortType) {
		checkState(nodeSortType==null);
		checkNotNull(sortType);

		nodeSortType = sortType;

		return this;
	}

	SortType getNodeSortType() {
		return nodeSortType==null ? DEFAULT_NODE_SORT_TYPE : nodeSortType;
	}

	public StructureBuilder customNodeSorter(Comparator<? super Item> sorter) {
		checkState(nodeSorter==null);
		checkNotNull(sorter);

		nodeSorter = sorter;

		return this;
	}

	Comparator<? super Item> getNodeSorter() {
		return nodeSorter;
	}

	public StructureBuilder sortingEdges(SortType sortType) {
		checkState(edgeSortType==null);
		checkNotNull(sortType);

		edgeSortType = sortType;

		return this;
	}

	SortType getEdgeSortType() {
		return edgeSortType==null ? DEFAULT_EDGE_SORT_TYPE : edgeSortType;
	}

	public StructureBuilder customEdgeSorter(Comparator<? super Edge> sorter) {
		checkState(edgeSorter==null);
		checkNotNull(sorter);

		edgeSorter = sorter;

		return this;
	}

	Comparator<? super Edge> getEdgeSorter() {
		return edgeSorter;
	}

	public StructureBuilder augmented(boolean augmented) {
		this.augmented = augmented;

		return this;
	}

	//**********************************
	//    CONSTRUCTION METHODS
	//**********************************

	public <R extends RootItem<?>> void setRoot(R item) {
		checkState(itemStorage == null);
		checkState(root == null);
		checkNotNull(item);

		root = item;
	}

	public void addNode(Item item) {
		checkState(itemStorage == null);
		checkNotNull(item);

		augmented |= isVirtual(item);

		nodes().add(item);
	}

	public <I extends Item> void addNodes(I[] items) {
		checkState(itemStorage == null);
		checkNotNull(items);

		if(!augmented) {
			for(int i=0; i<items.length && !augmented; i++) {
				augmented |= isVirtual(items[i]);
			}
		}

		CollectionUtils.feedItems(nodes(), items);
	}

	public <I extends Item> void addNodes(Collection<I> items) {
		checkState(itemStorage == null);
		checkNotNull(items);

		if(!augmented) {
			for(Iterator<I> it = items.iterator(); it.hasNext() && !augmented;) {
				augmented |= isVirtual(it.next());
			}
		}

		nodes().addAll(items);
	}

	public void addNodes(Container container) {
		checkState(itemStorage == null);
		checkNotNull(container);

		int size = ensureIntegerValueRange(container.getItemCount());
		List<Item> nodes = nodes();

		for(int i=0; i<size; i++) {
			Item item = container.getItemAt(i);

			augmented |= isVirtual(item);
			nodes.add(item);
		}
	}

	public void addEdge(Edge edge) {
		checkState(edgeStorage == null);
		checkNotNull(edge);

		edges().add(edge);
	}

	public <E extends Edge> void addEdges(E[] edges) {
		checkState(edgeStorage == null);
		checkNotNull(edges);

		CollectionUtils.feedItems(edges(), edges);
	}

	public <E extends Edge> void addEdges(Collection<E> edges) {
		checkState(edgeStorage == null);
		checkNotNull(edges);

		edges().addAll(edges);
	}

	public void addEdges(Structure structure) {
		checkState(itemStorage == null);
		checkNotNull(structure);

		int size = ensureIntegerValueRange(structure.getEdgeCount());
		List<Edge> nodes = edges();

		for(int i=0; i<size; i++) {
			nodes.add(structure.getEdgeAt(i));
		}
	}

	public void setBaseContainers(DataSet<Container> baseContainers) {
		checkState(this.baseContainers == null);
		checkNotNull(baseContainers);

		this.baseContainers = baseContainers;
	}

	public void setBoundaryContainer(Container boundaryContainer) {
		checkState(this.boundaryContainer == null);
		checkNotNull(boundaryContainer);

		this.boundaryContainer = boundaryContainer;
	}

	public void clearNodes() {
		nodes().clear();
	}

	public void clearEdges() {
		edges().clear();
	}

	/**
	 * Directly sets the {@code ItemStorage} instance to be used for the current
	 * building process.
	 * <p>
	 * Attention: This method does <b>not</b> refresh the builders internal <i>augmented</i>
	 * flag and virtual root node!
	 * When directly defining an {@code ItemStorage} to be used, that flag must be
	 * set {@link #augmented(boolean) manually} and the virtual root node has to be defined
	 * {@link #setRoot(RootItem) separately} <b>before</b> setting the storage!
	 *
	 * @throws IllegalStateException in case there are already items present in the buffer
	 */
	public void setNodes(ItemStorage itemStorage) {
		checkState(nodes().isEmpty());
		checkState(root!=null);
		checkState(this.itemStorage == null);
		checkNotNull(itemStorage);

		this.itemStorage = itemStorage;
	}

	/**
	 * Directly sets the {@code EdgeStorage} instance to be used for the current
	 * building process.
	 *
	 * @throws IllegalStateException in case there are already edges present in the buffer
	 */
	public void setEdges(EdgeStorage edgeStorage) {
		checkState(edges().isEmpty());
		checkState(this.edgeStorage == null);
		checkNotNull(edgeStorage);

		this.edgeStorage = edgeStorage;
	}

	//**********************************
	//    CREATION METHODS
	//**********************************

	public DefaultItem newNode() {
		return new DefaultItem();
	}

	public DefaultEdge newEdge(Item source, Item target) {
		return new DefaultEdge(currentStructure(), source, target);
	}

	//**********************************
	//    INSPECTION METHODS
	//**********************************

	public int getEdgeCount() {
		return edgeStorage==null ? edges().size() : ensureIntegerValueRange(edgeStorage.getEdgeCount(proxyStructure()));
	}

	public Edge getEdgeAt(int index) {
		return edgeStorage==null ? edges().get(index) : edgeStorage.getEdgeAt(proxyStructure(), index);
	}

	public int getNodeCount() {
		return itemStorage==null ? nodes().size() : ensureIntegerValueRange(itemStorage.getItemCount(proxyStructure()));
	}

	public Item getNodeAt(int index) {
		return itemStorage==null ? nodes().get(index) : itemStorage.getItemAt(proxyStructure(), index);
	}

	public Structure getCurrentStructure() {
		return currentStructure();
	}

	//**********************************
	//    CONTROL METHODS
	//**********************************

	public void reset() {
		nodes().clear();
		edges().clear();

		edgeBuffer.reset();

		itemStorage = null;
		edgeStorage = null;

		baseContainers = null;
		boundaryContainer = null;

		currentStructure = null;
		root = null;

		augmented = false;
	}

	/**
	 * Builds a new {@link Structure} from the current content of this
	 * builder's buffer and then resets the builder for the next building
	 * process.
	 */
	public Structure build() {
		ItemStorage itemStorage = this.itemStorage;
		if(itemStorage==null) {
			itemStorage = createItemStorage();
		}

		EdgeStorage edgeStorage = this.edgeStorage;
		if(edgeStorage==null) {
			edgeStorage = createEdgeStorage(itemStorage);
		}

		Container boundaryContainer = boundaryContainer();
		DataSet<Container> baseContainers = baseContainers();

		StaticStructure structure = currentStructure();

		structure.setBaseContainers(baseContainers);
		structure.setBoundaryContainer(boundaryContainer);
		structure.setNodes(itemStorage);
		structure.setEdges(edgeStorage);

		// Final linking
		RootItem<?> root = root();

		checkState(root==structure.getVirtualRoot());

		root.setStructure(structure);
		structure.setAugmented(augmented);

		// Clear buffer for next building process
		reset();

		return structure;
	}

	private ItemStorage createItemStorage() {

		List<Item> nodes = nodes();
		checkState(!nodes.isEmpty());

		Comparator<? super Item> sorter = getNodeSortType();

		if(sorter==SortType.CUSTOM) {
			checkState(nodeSorter!=null);
			sorter = nodeSorter;
		} else if(sorter==SortType.NONE) {
			sorter = null;
		}

		if(sorter!=null) {
			Collections.sort(nodes, sorter);
		}

		ItemStorage itemStorage = null;

		if(nodes.size()<=StaticArrayItemStorage.MAX_SIZE
				&& sorter==SortType.NATURAL) {
			itemStorage = new StaticArrayItemStorage(nodes);
		} else {

			Item beginItem = null;
			Item endItem = null;

			if(storeOffsets) {
				for(Item item : nodes) {
					if(beginItem==null || item.getBeginOffset()<beginItem.getBeginOffset()) {
						beginItem = item;
					}
					if(endItem==null || item.getBeginOffset()>endItem.getEndOffset()) {
						endItem = item;
					}
				}
			}

			itemStorage = new StaticListItemStorage(nodes, beginItem, endItem);
		}

		return itemStorage;
	}

	private void prepareEdgeBuffer() {

		RootItem<?> root = root();

		edgeBuffer.setRoot(root);
		edgeBuffer.add(edges);

		Collection<Item> roots = null;

		if(!mayHaveMultiRoots) {
			roots = Collections.singleton(root);
		}

		edgeBuffer.computeMetaData(roots);
	}

	private EdgeStorage createEdgeStorage(ItemStorage itemStorage) {

		List<Edge> edges = edges();
		checkState(!edges.isEmpty());

		Comparator<? super Edge> sorter = getEdgeSortType();

		if(sorter==SortType.CUSTOM) {
			checkState(edgeSorter!=null);
			sorter = edgeSorter;
		} else if(sorter==SortType.NONE) {
			sorter = null;
		}

		if(sorter!=null) {
			Collections.sort(edges, sorter);
		}

		EdgeStorage edgeStorage = null;

		StructureType declaredType = manifest.getStructureType();

		/*
		 *  If the manifest declared a type that allows the possibility of a
		 *  more optimized representation, let the buffer decide the actual
		 *  type to be used based on collected meta data (max number of incoming
		 *  or outgoing edges for example).
		 */
		if(declaredType!=StructureType.SET) {
			prepareEdgeBuffer();
			declaredType = edgeBuffer.getStructureType();
		}

		switch (declaredType) {

		case SET: {
			edgeStorage = new EmptyEdgeStorage();
		} break;

		case CHAIN: {
			edgeStorage = StaticChainEdgeStorage.fromBuilder(this);
		} break;

		case TREE: {
			edgeStorage = StaticTreeEdgeStorage.fromBuilder(this);
		} break;

		//TODO other structure types!!!

		default:
			throw new IllegalStateException("Structure type not yet supported by builder: "+manifest.getStructureType());
		}

		return edgeStorage;
	}

	static int compare0(Item m1, Item m2) {
		long result = m1.getBeginOffset()-m2.getBeginOffset();

		if(result==0) {
			result = m1.getEndOffset()-m2.getEndOffset();
		}

		return (int) result;
	}

	public enum SortType implements Comparator<Item> {
		NONE {

			@Override
			public int compare(Item o1, Item o2) {
				throw new UnsupportedOperationException();
			}
		},
		NATURAL {

			@Override
			public int compare(Item m1, Item m2) {
				return compare0(m1, m2);
			}
		},
		NATURAL_REVERSE {

			@Override
			public int compare(Item m1, Item m2) {
				return compare0(m2, m1);
			}
		},
		CUSTOM {

			@Override
			public int compare(Item o1, Item o2) {
				throw new UnsupportedOperationException("Must provide custiom implementation for sorter");
			}
		},
		;
	}

	public interface BuilderObserver {
		void structureBuilt(Structure structure);
	}

	public static class BuilderStats {
		private final Set<StructureType> types = EnumSet.noneOf(StructureType.class);

		void addStructure(Structure structure) {

			StructureType type = structure.getStructureType();

			types.add(type);
		}
	}

	public static class TypeStats {
		int count;
		Stats size, incoming, outgoing, height, depth, descendants;

		//TODO calc methods + public getters
	}

	public static class Stats {
		int min, max;
		double avg;

		//TODO public getters
	}
}
