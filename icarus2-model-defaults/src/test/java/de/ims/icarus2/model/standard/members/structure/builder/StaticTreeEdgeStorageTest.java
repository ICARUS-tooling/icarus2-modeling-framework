/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.mockEdge;
import static de.ims.icarus2.test.TestUtils.assertCollectionEmpty;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.StructureFlag;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.standard.members.structure.ImmutableEdgeStorageTest;
import de.ims.icarus2.model.standard.members.structure.builder.ChainsAndTrees.Payload;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.tree.Tree;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * @author Markus Gärtner
 *
 */
interface StaticTreeEdgeStorageTest<T extends StaticTreeEdgeStorage> extends ImmutableEdgeStorageTest<T> {

	/**
	 * Create a configurations for testing the basic
	 * structure methods common to all tree implementations.
	 */
	ChainsAndTrees.TreeConfig createDefaultTestConfiguration(int size);

	public static ChainsAndTrees.TreeConfig defaultCreateRandomTestConfiguration(RandomGenerator rng, int size) {
		return ChainsAndTrees.singleTree(rng, size, 1.0, size/3, UNSET_INT);
	}

	/**
	 * Creates a variety of configurations to be tested.
	 * Returned stream must not be empty.
	 *
	 * @see #defaultCreateRandomTestConfigurations(RandomGenerator)
	 */
	Stream<ChainsAndTrees.TreeConfig> createTestConfigurations();

	public static Stream<ChainsAndTrees.TreeConfig> defaultCreateRandomTestConfigurations(RandomGenerator rng) {
		return Stream.of(
				ChainsAndTrees.singleTree(rng, ChainsAndTrees.randomSize(rng), 1.0, 1, UNSET_INT), // full chain
				ChainsAndTrees.singleTree(rng, ChainsAndTrees.randomSize(rng), 1.0, 2, UNSET_INT), // binary tree
				ChainsAndTrees.singleTree(rng, ChainsAndTrees.randomSize(rng), 1.0, UNSET_INT, UNSET_INT), // full random tree
				ChainsAndTrees.singleTree(rng, ChainsAndTrees.randomSize(rng), 0.5, UNSET_INT, UNSET_INT) // sparse random tree
		);
	}

	@Provider
	T createFromBuilder(StructureBuilder builder);

	/**
	 * Create a manifest usable for testing, which should provide the following
	 * content appropriate for the structure implementation under test:<br>
	 * <ul>
	 * <li>{@link Structure#getContainerType() container type}</li>
	 * <li>{@link Structure#getStructureType() structure type}</li>
	 * </ul>
	 */
	@SuppressWarnings("boxing")
	default StructureManifest createManifest(ChainsAndTrees.TreeConfig treeConfig) {
		StructureManifest manifest = mock(StructureManifest.class);
		when(manifest.getContainerType()).thenReturn(ContainerType.LIST);
		when(manifest.getStructureType()).thenReturn(StructureType.TREE);

		when(manifest.isStructureFlagSet(StructureFlag.MULTI_ROOT)).thenReturn(treeConfig.multiRoot);

		return manifest;
	}

	default StructureBuilder toBuilder(ChainsAndTrees.TreeConfig treeConfig) {
		treeConfig.validate();
		StructureBuilder builder = StructureBuilder.builder(createManifest(treeConfig));

		// Adjust source terminal of all root edges to the virtual root node
		treeConfig.finalizeRootEdges(builder.getRoot());

		builder.addNodes(treeConfig.nodes);
		builder.addEdges(treeConfig.edges);

		return builder;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default T createTestInstance(TestSettings settings) {
		ChainsAndTrees.TreeConfig treeConfig = createDefaultTestConfiguration(10);
		return settings.process(createFromBuilder(toBuilder(treeConfig)));
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorageTest#getExpectedStructureType()
	 */
	@Override
	default StructureType getExpectedStructureType() {
		return StructureType.TREE;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticTreeEdgeStorage#getSiblingAt(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, long)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetSiblingAt() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					T tree = createFromBuilder(toBuilder(config));
					for (int i = 0; i < config.nodes.length; i++) {
						Item node = config.nodes[i];
						Tree<Payload> parent = config.node(node)
								.map(Tree::parent)
								.orElse(null);

						if(parent==null) {
							assertModelException(GlobalErrorCode.INVALID_INPUT,
									() -> tree.getSiblingAt(config.structure, node, 0L));
						} else {
							assertFalse(parent.isChildless());
							int nodeCount = parent.childCount();

							// Cache all siblings (in order of appearance)
							Item[] nodes = new Item[nodeCount];
							for (int j = 0; j < nodes.length; j++) {
								nodes[j] = tree.getEdgeAt(config.structure,
										parent.getData().node, j, true).getTarget();
							}
							int ownIndex = IntStream.range(0, nodes.length)
									.filter(idx -> nodes[idx]==node)
									.findFirst()
									.orElseThrow(AssertionError::new);

							// Check boundary violations
							assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
									() -> tree.getSiblingAt(config.structure, node, -ownIndex-1));
							assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
									() -> tree.getSiblingAt(config.structure, node, nodeCount-ownIndex));

							// Ensure the node itself can be obtained
							assertSame(node, tree.getSiblingAt(config.structure, node, 0L));

							// Check left area
							for (int offset = 1; offset <= ownIndex; offset++) {
								assertSame(nodes[ownIndex-offset],
										tree.getSiblingAt(config.structure, node, -offset));
							}

							// Check right area
							for (int offset = 1; offset < nodeCount-ownIndex; offset++) {
								assertSame(nodes[ownIndex+offset],
										tree.getSiblingAt(config.structure, node, offset));
							}
						}
					}

					// Root has no parent
					assertNull(tree.getParent(config.structure,
							tree.getVirtualRoot(config.structure)));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticTreeEdgeStorage#getParent(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetParent() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					T tree = createFromBuilder(toBuilder(config));

					for (int i = 0; i < config.nodes.length; i++) {
						Item node = config.nodes[i];
						Item expected = config.node(node)
								.map(Tree::parent)
								.map(Tree::getData)
								.map(p -> p.node)
								.orElse(null);
						Item parent = tree.getParent(config.structure, node);
						if(expected==null) {
							assertNull(parent, "Unexpected parent for "+node);
						} else {
							assertSame(expected, parent);
						}
					}

					// Root has no parent
					assertNull(tree.getParent(config.structure,
							tree.getVirtualRoot(config.structure)));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticTreeEdgeStorage#indexOfChild(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIndexOfChild() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					T tree = createFromBuilder(toBuilder(config));

					for (int i = 0; i < config.nodes.length; i++) {
						Item node = config.nodes[i];
						Item parent = config.node(node)
								.map(Tree::parent)
								.map(Tree::getData)
								.map(p -> p.node)
								.orElse(null);

						if(parent==null) {
							assertModelException(GlobalErrorCode.INVALID_INPUT,
									() -> tree.indexOfChild(config.structure, node));
						} else {
							long index = tree.indexOfChild(config.structure, node);
							assertTrue(index != UNSET_LONG);

							assertSame(node, tree.getEdgeAt(
									config.structure, parent, index, true).getTarget());
						}
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticTreeEdgeStorage#indexOfChild(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIndexOfChildRoot() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					T tree = createFromBuilder(toBuilder(config));

					Item root = tree.getVirtualRoot(config.structure);

					// Root is not a child of any node
					assertModelException(GlobalErrorCode.INVALID_INPUT,
							() -> tree.indexOfChild(config.structure, root));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.AbstractStaticEdgeStorage#isRoot(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default Stream<DynamicTest> testIsRootOnNodes() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					T tree = createFromBuilder(toBuilder(config));

					Set<Item> roots = Stream.of(config.rootEdges)
							.map(Edge::getTarget)
							.collect(Collectors.toSet());

					for (int i = 0; i < config.nodes.length; i++) {
						Item node = config.nodes[i];
						assertEquals(roots.contains(node), tree.isRoot(config.structure, node));
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.AbstractStaticEdgeStorage#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetEdgeCountStructure() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					T tree = createFromBuilder(toBuilder(config));
					assertEquals(config.edges.length, tree.getEdgeCount(config.structure));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.AbstractStaticEdgeStorage#getEdgeAt(de.ims.icarus2.model.api.members.structure.Structure, long)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetEdgeAtStructureLong() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					T tree = createFromBuilder(toBuilder(config));
					// We're not expecting a predetermined order of the edges
					Set<Edge> edges = set(config.edges);
					for (int i = 0; i < tree.getEdgeCount(config.structure); i++) {
						assertTrue(edges.remove(tree.getEdgeAt(config.structure, i)));
					}
					assertCollectionEmpty(edges);
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.AbstractStaticEdgeStorage#indexOfEdge(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Edge)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIndexOfEdgeStructureEdge() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					T tree = createFromBuilder(toBuilder(config));

					for (int i = 0; i < config.edges.length; i++) {
						Edge edge = tree.getEdgeAt(i);
						assertEquals(i, tree.indexOfEdge(config.structure, edge));
					}

					// Ensure that foreign edges aren't recognized
					assertEquals(UNSET_LONG, tree.indexOfEdge(config.structure, mockEdge()));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.AbstractStaticEdgeStorage#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetEdgeCountStructureItem() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					T tree = createFromBuilder(toBuilder(config));

					for (int i = 0; i < config.nodes.length; i++) {
						Item node = config.nodes[i];
						Tree<Payload> t = config.node(node).orElse(null);
						long expected = 0;
						if(t!=null) {
							expected += t.childCount();
							if(t.parent()!=null) {
								expected ++;
							}
						}

						long count = tree.getEdgeCount(config.structure, node);
						assertEquals(expected, count, "Edge count mismatch at index "+i);
					}

					// Verify number of root edges
					Item root = tree.getVirtualRoot(config.structure);
					assertEquals(config.rootEdges.length, tree.getEdgeCount(config.structure, root));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticTreeEdgeStorage#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, boolean)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetEdgeCountStructureItemBoolean() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					T tree = createFromBuilder(toBuilder(config));

					for (int i = 0; i < config.nodes.length; i++) {
						Item node = config.nodes[i];
						Tree<Payload> t = config.node(node).orElse(null);

						assertEquals(t==null ? 0 : t.childCount(),
								tree.getEdgeCount(config.structure, node, true),
								"Outgoing edge count mismatch at index "+i);
						assertEquals(t==null || t.parent()==null ? 0 : 1,
								tree.getEdgeCount(config.structure, node, false),
								"Incoming edge count mismatch at index "+i);
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticTreeEdgeStorage(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, boolean)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetEdgeCountStructureItemBooleanRoot() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					T tree = createFromBuilder(toBuilder(config));

					Item root = tree.getVirtualRoot(config.structure);
					assertEquals(config.rootEdges.length, tree.getEdgeCount(config.structure, root, true));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticTreeEdgeStorage#getEdgeAt(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, long, boolean)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetEdgeAtStructureItemLongBoolean() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					T tree = createFromBuilder(toBuilder(config));

					for (int i = 0; i < config.nodes.length; i++) {
						Item node = config.nodes[i];
						Tree<Payload> t = config.node(node).orElse(null);

						// Verify incoming edge
						if(t!=null && t.parent()!=null) {
							Edge incoming = config.edges[t.getData().edgeIndex];
							assertSame(incoming, tree.getEdgeAt(config.structure, node, 0, false));
						} else {
							assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
									() -> tree.getEdgeAt(config.structure, node, 0, false));
						}

						// Verify outgoing edges
						if(t!=null && !t.isChildless()) {
							int childCount = t.childCount();
							// Cache all expected edges
							Set<Edge> edges = new ObjectOpenHashSet<>();
							for (int j = 0; j < childCount; j++) {
								edges.add(config.edges[t.childAt(j).getData().edgeIndex]);
							}

							for (int j = 0; j < childCount; j++) {
								Edge edge = tree.getEdgeAt(config.structure, node, j, true);
								assertNotNull(edge);
								assertTrue(edges.contains(edge),
										"Unexpected edge for node "+node+": "+edge);
							}
						} else {
							assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
									() -> tree.getEdgeAt(config.structure, node, 0, true));
						}
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticTreeEdgeStorage#getEdgeAt(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, long, boolean)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetEdgeAtStructureItemLongBooleanRoot() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					T tree = createFromBuilder(toBuilder(config));

					Item root = tree.getVirtualRoot(config.structure);
					Set<Edge> edges = set(config.rootEdges);
					for (int i = 0; i < config.rootEdges.length; i++) {
						assertTrue(edges.contains(tree.getEdgeAt(config.structure, root, i, true)));
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticTreeEdgeStorage#getHeight(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetHeight() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					T tree = createFromBuilder(toBuilder(config));
					for (int i = 0; i < config.nodes.length; i++) {
						Item item = config.nodes[i];
						Payload payload = config.payload(item);
						long height = tree.getHeight(config.structure, config.nodes[i]);
						if(payload==null) {
							assertEquals(0, height, "Unexpected height for index "+i);
						} else {
							assertEquals(payload.height, height, "Mismatch on height for index "+i);
						}
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticTreeEdgeStorage#getHeight(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetHeightRoot() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					T tree = createFromBuilder(toBuilder(config));

					Item root = tree.getVirtualRoot(config.structure);
					assertEquals(config.payload(root).height, tree.getHeight(config.structure, root));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticTreeEdgeStorage#getDepth(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetDepth() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					T tree = createFromBuilder(toBuilder(config));
					for (int i = 0; i < config.nodes.length; i++) {
						Item item = config.nodes[i];
						Payload payload = config.payload(item);
						long depth = tree.getDepth(config.structure, config.nodes[i]);
						if(payload==null) {
							assertEquals(UNSET_LONG, depth, "Unexpected depth for index "+i);
						} else {
							assertEquals(payload.depth, depth, "Mismatch on depths for index "+i);
						}
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticTreeEdgeStorage#getDepth(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetDepthRoot() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					T tree = createFromBuilder(toBuilder(config));

					Item root = tree.getVirtualRoot(config.structure);
					assertEquals(0L, tree.getDepth(config.structure, root));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticTreeEdgeStorage#getDescendantCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetDescendantCount() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					T tree = createFromBuilder(toBuilder(config));
					for (int i = 0; i < config.nodes.length; i++) {
						Item item = config.nodes[i];
						Payload payload = config.payload(item);
						long count = tree.getDescendantCount(config.structure, config.nodes[i]);
						if(payload==null) {
							assertEquals(0, count, "Unexpected descendant count for index "+i);
						} else {
							assertEquals(payload.descendants, count, "Mismatch on descendant count for index "+i);
						}
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticTreeEdgeStorage#getDescendantCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetDescendantCountRoot() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					T tree = createFromBuilder(toBuilder(config));

					Item root = tree.getVirtualRoot(config.structure);
					// 1 descendant per edge in a tree
					assertEquals(config.edges.length, tree.getDescendantCount(config.structure, root));
				}));
	}
}
