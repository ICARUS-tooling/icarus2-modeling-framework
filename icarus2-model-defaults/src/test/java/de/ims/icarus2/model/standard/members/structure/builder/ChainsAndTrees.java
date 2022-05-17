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

import static de.ims.icarus2.model.api.ModelTestUtils.mockEdge;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.api.ModelTestUtils.mockStructure;
import static de.ims.icarus2.model.api.ModelTestUtils.stubId;
import static de.ims.icarus2.model.api.ModelTestUtils.stubIndex;
import static de.ims.icarus2.test.TestUtils.assertMock;
import static de.ims.icarus2.test.TestUtils.displayString;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.MutablePrimitives.MutableInteger;
import de.ims.icarus2.util.strings.BracketStyle;
import de.ims.icarus2.util.tree.Tree;
import de.ims.icarus2.util.tree.TreeUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class ChainsAndTrees {

	/** Produce a random value for total size of a structure */
	static int randomSize(RandomGenerator rng) {
		return rng.random(50, 100);
	}

	/** Produce a suitable random value for number of parallel structures */
	static int randomMulti(RandomGenerator rng, int size) {
		return size <=2 ? 1 : rng.random(2, Math.min(size, 6));
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
	static ChainConfig singleChain(RandomGenerator rng, int size, double fraction) {
		checkArgument(fraction<=1.0);
		int part = (int) (size * fraction);

		ChainConfig chainConfig = ChainConfig.basic(size, part, 1);
		chainConfig.label = String.format("single chain - %.0f%% full", fraction*100);
		chainConfig.defaultStructure();

		fillChain(chainConfig, rng.randomIndices(size, part), 0, part, 0);

		return chainConfig;
	}

	@SuppressWarnings("boxing")
	static ChainConfig multiChain(RandomGenerator rng, int size, double fraction) {
		checkArgument(fraction<=1.0);
		int part = (int) (size * fraction);
		int chainCount = randomMulti(rng, part);

		ChainConfig chainConfig = ChainConfig.basic(size, part, chainCount);
		chainConfig.label = String.format("multi chain - %.0f%% full", fraction*100);


		chainConfig.defaultStructure();
		chainConfig.multiRoot = true;

		PrimitiveIterator.OfInt nodes = rng.randomIndices(size, part);

		int remaining = part;
		for (int i = 0; i < chainCount; i++) {
			int chainSize = i==chainCount-1 ? remaining : rng.random(1, remaining-chainCount+i+1);
			fillChain(chainConfig, nodes, part-remaining, chainSize, i);
			remaining -= chainSize;
		}
		assertEquals(0, remaining);

		return chainConfig;
	}

	static class Payload {
		Item node = null;
		int edgeIndex = UNSET_INT; // Index of edge from parent in edge array of config
		int height = UNSET_INT;
		int depth = UNSET_INT;
		int descendants = UNSET_INT;

		public Payload(Item node, int height, int depth, int descendants) {
			this.node = requireNonNull(node);
			this.height = height;
			this.depth = depth;
			this.descendants = descendants;
		}
	}

	/*
	 * Tree constraints:
	 * - virtual nodes can only have virtual nodes as parent
	 * -
	 */

	private static void fillTree(RandomGenerator rng, TreeConfig config, PrimitiveIterator.OfInt nodes,
			int offset, int nodeCount, int rootIndex,
			int maxHeight, int maxBranching) {
		assertTrue(nodeCount>0);

		// Nodes that haven't violated the maxBranching and maxHeight (inverse) constraints yet
		List<Tree<Payload>> legalParents = new ArrayList<>();

		// Random root first, as it needs to be connected to the existing "master root" in config
		Tree<Payload> root = config.tree.newChild(new Payload(
				config.nodes[nodes.nextInt()], UNSET_INT, 1, UNSET_INT)); // depth=1, since we're below the root node
		legalParents.add(root);

		for (int i = 1; i < nodeCount; i++) {
			// Pick random node
			Item item = config.nodes[nodes.nextInt()];

			// Pick random parent
			int parentIndex = rng.random(0, legalParents.size());
			Tree<Payload> parent = legalParents.get(parentIndex);

			// Attach new node to parent
			Tree<Payload> node = parent.newChild(new Payload(
					item, UNSET_INT, parent.getData().depth+1, UNSET_INT));

			// Honor constraints
			if((maxBranching!=UNSET_INT && parent.childCount()>=maxBranching) ||
					(maxHeight!=UNSET_INT && node.getData().depth>maxHeight)) {
				legalParents.remove(parentIndex);
			}

			// Add only after we maybe removed a node to potentially prevent growing the list
			legalParents.add(node);
		}

		// Now finalize metadata and put nodes into config array
		createEdges(config, root, offset, rootIndex);
	}

	private static void createEdges(TreeConfig config, Tree<Payload> root,
			int offset, int rootIndex) {

		MutableInteger index = new MutableInteger(offset);

		/*
		 *  We only run a post-order traversal to bottom-up propagate
		 *  the descendants count and height values.
		 */
		TreeUtils.traversePostOrder(root, node -> {
			Payload payload = node.getData();
			if(node.isChildless()) {
				payload.height = payload.descendants = 0; // Leaf node
			} else {
				payload.descendants = node.childCount();
				for (int i = 0; i < node.childCount(); i++) {
					Payload p = node.childAt(i).getData();
					payload.descendants += p.descendants;
					payload.height = Math.max(payload.height, p.height+1);
				}
			}

			int idx = index.getAndIncrement();
			config.treeNodes.put(payload.node, node);

			Payload p = node.parent().getData();
			Edge edge;
			if(p==null) {
				edge = config.edge(null, payload.node);
				config.rootEdges[rootIndex] = edge;
			} else {
				edge = config.edge(p.node, payload.node);
			}
			config.edges[offset+idx] = edge;
			payload.edgeIndex = offset+idx;
		});
	}

	@SuppressWarnings("boxing")
	static TreeConfig singleTree(RandomGenerator rng, int size, double fraction, int maxHeight,
			int maxBranching) {
		checkArgument(fraction<=1.0);
		int part = (int) (size * fraction);

		TreeConfig treeConfig = TreeConfig.basic(size, part, 1);
		treeConfig.label = String.format("single tree - %s nodes, %s edges, %.0f%% filled",
				displayString(size), displayString(part), fraction*100);
		treeConfig.defaultStructure();

		fillTree(rng, treeConfig, rng.randomIndices(size, part), 0, part, 0,
				maxHeight, maxBranching);

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
			assertNotNull(edges);
			assertNotNull(rootEdges);
			assertNotNull(label);
			assertNotNull(nodes);
			return thisAsCast();
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

		void finalizeRootEdges(RootItem<?> root) {

			for(int i = 0; i<rootEdges.length; i++) {
				Edge rootEdge = rootEdges[i];
				if(size<=MAX_STUBBED_SIZE) {
					assertMock(rootEdge);
					when(rootEdge.getSource()).thenReturn(root);
				} else {
					rootEdge.setTerminal(root, true);
				}
				// No need to hard-link the edge, the builder will do this
//				root.addEdge(rootEdge);
			}
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
		 */
		Map<Item,Tree<Payload>> treeNodes;

		public TreeConfig(int size) {
			super(size);
		}

		@Override
		TreeConfig validate() {
			super.validate();
			assertNotNull(tree);
			return this;
		}

		@Override
		void finalizeRootEdges(RootItem root) {
			super.finalizeRootEdges(root);

			// Adjust root info
			Payload p = new Payload(root, UNSET_INT, 0, tree.childCount());

			for (int i = 0; i < tree.childCount(); i++) {
				Tree<Payload> child = tree.childAt(i);
				p.descendants += child.getData().descendants;
				p.height = Math.max(p.height, child.getData().height+1);
			}

			tree.setData(p);
			treeNodes.put(root, tree);
		}

		Payload payload(Item item) {
			Tree<Payload> node = treeNodes.get(item);
			return node==null ? null : node.getData();
		}

		Optional<Tree<Payload>> node(Item item) {
			return Optional.ofNullable(treeNodes.get(item));
		}

		void dump(String msg) {
			System.out.println(msg+": "+TreeUtils.toString(tree, BracketStyle.SQUARE,
					p -> p.node.toString()));
		}

		static TreeConfig basic(int nodeCount, int edgeCount, int rootEdgeCount) {
			TreeConfig treeConfig = new TreeConfig(nodeCount);
			treeConfig.initDefault(nodeCount, edgeCount, rootEdgeCount);

			treeConfig.tree = Tree.newRoot();
			treeConfig.treeNodes = new Object2ObjectOpenHashMap<>(edgeCount);

			return treeConfig;
		}
	}
}
