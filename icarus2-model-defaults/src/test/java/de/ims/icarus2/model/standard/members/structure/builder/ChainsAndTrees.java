/**
 *
 */
package de.ims.icarus2.model.standard.members.structure.builder;

import static de.ims.icarus2.model.api.ModelTestUtils.mockEdge;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.api.ModelTestUtils.mockStructure;
import static de.ims.icarus2.model.api.ModelTestUtils.stubId;
import static de.ims.icarus2.model.api.ModelTestUtils.stubIndex;
import static de.ims.icarus2.test.TestUtils.assertMock;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.standard.members.container.ItemStorage;
import de.ims.icarus2.model.standard.members.item.DefaultItem;
import de.ims.icarus2.model.standard.members.structure.DefaultEdge;
import de.ims.icarus2.model.standard.members.structure.DefaultStructure;
import de.ims.icarus2.model.standard.members.structure.RootItem;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.util.tree.Tree;

/**
 * @author Markus Gärtner
 *
 */
public class ChainsAndTrees {


	static int randomSize() {
		return random(50, 100);
	}

	static PrimitiveIterator.OfInt randomIndices(int spectrum, int size) {
		int[] source = new int[spectrum];
		for (int i = 0; i < source.length; i++) {
			source[i] = i;
		}

		for (int i = 0; i < source.length; i++) {
			int x = TestUtils.random(0, spectrum);
			int tmp = source[i];
			source[i] = source[x];
			source[x] = tmp;
		}

		return IntStream.of(source).limit(size).iterator();
	}

	private static void fillChain(ChainConfig config, PrimitiveIterator.OfInt nodes,
			int offset, int nodeCount, int rootIndex) {
		assertTrue(nodeCount>0);
		assertNull(config.edges[offset]);

		// Special treatment of "head" of the chain
		int previous = nodes.nextInt();
		config.edges[offset] = config.edge(null, config.nodes[previous]);
		config.rootEdges[rootIndex] = config.edges[offset];
		config.incoming[previous] = config.edges[offset];
		config.depths[previous] = 1;
		config.heights[previous] = config.descendants[previous] = nodeCount-1;

		// Now randomize the next size-1 elements
		for (int i = 1; i < nodeCount; i++) {
			assertNull(config.edges[offset+i]);

			int next = nodes.nextInt();
			Edge edge = config.edge(config.nodes[previous], config.nodes[next]);
			config.edges[offset+i] = edge;
			config.outgoing[previous] = edge;
			config.incoming[next] = edge;

			config.heights[next] = config.descendants[next] = nodeCount-i-1;
			config.depths[next] = i+1;

			previous = next;
		}
	}

	@SuppressWarnings("boxing")
	static ChainConfig singleChain(int size, double fraction) {
		checkArgument(fraction<=1.0);
		int part = (int) (size * fraction);

		ChainConfig chainConfig = ChainConfig.basic(size, part, 1);
		chainConfig.label = String.format("single chain - %.0f%% full", fraction*100);
		chainConfig.defaultStructure();

		fillChain(chainConfig, randomIndices(size, part), 0, part, 0);

		return chainConfig;
	}

	@SuppressWarnings("boxing")
	static ChainConfig multiChain(int size, double fraction) {
		checkArgument(fraction<=1.0);
		int part = (int) (size * fraction);
		int chainCount = random(2, 6);

		ChainConfig chainConfig = ChainConfig.basic(size, part, chainCount);
		chainConfig.label = String.format("multi chain - %.0f%% full", fraction*100);


		chainConfig.defaultStructure();
		chainConfig.multiRoot = true;

		PrimitiveIterator.OfInt nodes = randomIndices(size, part);

		int remaining = part;
		for (int i = 0; i < chainCount; i++) {
			int chainSize = i==chainCount-1 ? remaining : random(1, remaining-chainCount+i+1);
			fillChain(chainConfig, nodes, part-remaining, chainSize, i);
			remaining -= chainSize;
		}
		assertEquals(0, remaining);

		return chainConfig;
	}

	private static class Payload {
		Item node = null;
		int height = UNSET_INT;
		int depth = UNSET_INT;
		int descendants = UNSET_INT;

		public Payload(Item node, int height, int descendants) {
			this.node = requireNonNull(node);
			this.height = height;
			this.descendants = descendants;
		}
	}

	/*
	 * Tree constraints:
	 * - virtual nodes can only have virtual nodes as parent
	 * -
	 */

	private static void fillTree(TreeConfig config, PrimitiveIterator.OfInt nodes,
			int offset, int nodeCount, int rootIndex) {
		assertTrue(nodeCount>0);
		assertNull(config.edges[offset]);

		// Special treatment of "head" of the chain
		int previous = nodes.nextInt();
		config.edges[offset] = config.edge(null, config.nodes[previous]);
		config.rootEdges[rootIndex] = config.edges[offset];
		config.incoming[previous] = config.edges[offset];
		config.depths[previous] = 1;
		config.heights[previous] = config.descendants[previous] = nodeCount-1;

		// Now randomize the next size-1 elements
		for (int i = 1; i < nodeCount; i++) {
			assertNull(config.edges[offset+i]);

			int next = nodes.nextInt();
			Edge edge = config.edge(config.nodes[previous], config.nodes[next]);
			config.edges[offset+i] = edge;
			config.outgoing[previous] = edge;
			config.incoming[next] = edge;

			config.heights[next] = config.descendants[next] = nodeCount-i-1;
			config.depths[next] = i+1;

			previous = next;
		}
	}

	@SuppressWarnings("boxing")
	static TreeConfig singleTree(int size, double fraction, int maxHeight,
			int maxBranching, boolean allowVirtualNodes) {
		checkArgument(fraction<=1.0);
		int part = (int) (size * fraction);

		TreeConfig treeConfig = TreeConfig.basic(size, part, 1);
		treeConfig.label = String.format("single tree - %.0f%% full", fraction*100);
		treeConfig.defaultStructure();
		treeConfig.virtualNodes = new Item[0];

		fillTree(treeConfig, randomIndices(size, part), 0, part, 0);

		return treeConfig;
	}

	static final int MAX_STUBBED_SIZE = 100;

	static abstract class Config<C extends Config<C>> {

		/** Human readable label for displaying the created test */
		String label;
		/** All nodes */
		Item[] nodes;
		/** Raw edges, all non-null */
		Edge[] edges;
		/**
		 * Edges to designated roots, minimum of 1.
		 * Note that due to the fact that the virtual root node isn't available
		 * by the time this config gets constructed, we need to lazily add the
		 * source terminals for all these root edges!
		 */
		Edge[] rootEdges;

		/** Optional structure to be used for calls on the storage under test */
		Structure structure = null;

		boolean multiRoot = false;

		final int size;

		private Config(int size) {
			this.size = size;
		}

		@SuppressWarnings("unchecked")
		protected C thisAsCast() {
			return (C) this;
		}

		C validate() {
			assertNotNull(label);
			assertNotNull(nodes);
			assertNotNull(edges);
			assertNotNull(rootEdges);
			return thisAsCast();
		}

		@SuppressWarnings("unchecked")
		void finalizeRootEdges(@SuppressWarnings("rawtypes") RootItem root) {

			for(int i = 0; i<rootEdges.length; i++) {
				Edge rootEdge = rootEdges[i];
				if(size<=MAX_STUBBED_SIZE) {
					assertMock(rootEdge);
					when(rootEdge.getSource()).thenReturn(root);
				} else {
					rootEdge.setTerminal(root, true);
				}
				root.addEdge(rootEdge);
			}
		}

		@SuppressWarnings("boxing")
		void defaultStructure() {
			if(size<=MAX_STUBBED_SIZE) {
				structure = mockStructure();
				for (int i = 0; i < nodes.length; i++) {
					when(structure.indexOfItem(nodes[i])).thenReturn(Long.valueOf(i));
				}
			} else {
				ItemStorage itemStorage = new StaticListItemStorage(
						Arrays.asList(nodes), null, null);
				DefaultStructure structure = new DefaultStructure();
				StructureManifest manifest = mock(StructureManifest.class);
				when(manifest.getContainerType()).thenReturn(ContainerType.LIST);
				structure.setManifest(manifest);
				structure.setItemStorage(itemStorage);
				this.structure = structure;
			}
		}

		Item item(int index) {
			if(size<=MAX_STUBBED_SIZE) {
				return stubId(stubIndex(mockItem(), index), index);
			}

			return new DefaultItem() {
				@Override
				public String toString() { return "item_"+index; }
				@Override
				public long getId() { return index; }
				@Override
				public long getIndex() { return index; }
			};
		}

		Edge edge(Item source, Item target) {
			requireNonNull(target);

			if(size<=MAX_STUBBED_SIZE) {
				Edge edge = mockEdge(source, target);
				if(source==null) {
					doReturn("root->"+target).when(edge).toString();
				} else {
					doReturn(source+"->"+target).when(edge).toString();
				}
				return edge;
			}

			DefaultEdge edge = new DefaultEdge();
			edge.setTerminal(source, true);
			edge.setTerminal(target, false);
			return edge;
		}

		protected void initDefault(int nodeCount, int edgeCount, int rootEdgeCount) {
			nodes = IntStream.range(0, nodeCount)
					.mapToObj(this::item)
					.toArray(Item[]::new);
			edges = new Edge[edgeCount];
			rootEdges = new Edge[rootEdgeCount];
		}
	}

	public static class ChainConfig extends Config<ChainConfig> {

		/** For every node, lists the incoming edge or null */
		Edge[] incoming;
		/** For every node other than the virtual root, lists the outgoing edge or null */
		Edge[] outgoing;

		int[] heights;
		int[] depths;
		int[] descendants;

		public ChainConfig(int size) {
			super(size);
		}

		@Override
		ChainConfig validate() {
			super.validate();
			assertNotNull(incoming);
			assertNotNull(outgoing);
			return this;
		}

		static ChainConfig basic(int nodeCount, int edgeCount, int rootEdgeCount) {
			ChainConfig chainConfig = new ChainConfig(nodeCount);
			chainConfig.initDefault(nodeCount, edgeCount, rootEdgeCount);

			chainConfig.incoming = new Edge[nodeCount];
			chainConfig.outgoing = new Edge[nodeCount];

			chainConfig.heights = new int[nodeCount];
			chainConfig.depths = new int[nodeCount];
			chainConfig.descendants = new int[nodeCount];

			Arrays.fill(chainConfig.heights, UNSET_INT);
			Arrays.fill(chainConfig.depths, UNSET_INT);
			Arrays.fill(chainConfig.descendants, UNSET_INT);

			return chainConfig;
		}
	}

	public static class TreeConfig extends Config<TreeConfig> {

		/** The raw tree with metadata as payload */
		Tree<Payload> tree;

		/**
		 * Wrappers for all the nodes (normal and virtual).
		 * Normal nodes are first, then followed by optional
		 * virtual nodes i nthe array.
		 */
		Tree<Payload>[] treeNodes;

		/** Optional virtual nodes */
		Item[] virtualNodes;

		public TreeConfig(int size) {
			super(size);
		}

		@Override
		TreeConfig validate() {
			super.validate();
			assertNotNull(tree);
			return this;
		}

		@SuppressWarnings("unchecked")
		static TreeConfig basic(int nodeCount, int edgeCount, int rootEdgeCount) {
			TreeConfig treeConfig = new TreeConfig(nodeCount);
			treeConfig.initDefault(nodeCount, edgeCount, rootEdgeCount);

			treeConfig.tree = Tree.root();
			treeConfig.treeNodes = new Tree[edgeCount];

			return treeConfig;
		}
	}
}
