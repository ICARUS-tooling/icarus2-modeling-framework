/**
 *
 */
package de.ims.icarus2.model.standard.members.structure.builder;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.assertUnsupportedOperation;
import static de.ims.icarus2.model.api.ModelTestUtils.mockEdge;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.test.TestUtils.assertCollectionEmpty;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.StructureFlag;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.standard.members.structure.ImmutableEdgeStorageTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;

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

	/**
	 * Creates a variety of configurations to be tested.
	 * Returned stream must not be empty. Default implementation
	 * returns {@link #createDefaultTestConfiguration()}.
	 */
	default Stream<ChainsAndTrees.TreeConfig> createTestConfigurations() {
		return Stream.of(createDefaultTestConfiguration(ChainsAndTrees.randomSize()));
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
		StructureBuilder builder = StructureBuilder.newBuilder(createManifest(treeConfig));

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
	@Test
	default void testGetSiblingAt() {
		assertUnsupportedOperation(() -> create().getSiblingAt(null, mockItem(), 0L));
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
						Edge edge = config.incoming[i];
						Item parent = tree.getParent(config.structure, node);
						if(edge==null) {
							assertNull(parent, "Unexpected parent for "+node);
						} else {
							assertSame(edge.getSource(), parent);
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
						Edge edge = config.incoming[i];
						long index = tree.indexOfChild(config.structure, node);
						if(edge==null) {
							assertEquals(UNSET_LONG, index);
						} else {
							assertEquals(0L, index);
						}
					}

					// Root is not a child of any node
					assertEquals(UNSET_LONG, tree.indexOfChild(config.structure,
							tree.getVirtualRoot(config.structure)));
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
						long expected = 0;
						if(config.incoming[i]!=null) expected++;
						if(config.outgoing[i]!=null) expected++;

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

						assertEquals(config.outgoing[i]==null ? 0 : 1,
								tree.getEdgeCount(config.structure, node, true),
								"Outgoing edge count mismatch at index "+i);
						assertEquals(config.incoming[i]==null ? 0 : 1,
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
						Edge incoming = config.incoming[i];
						Edge outgoing = config.outgoing[i];

						if(incoming!=null) {
							assertSame(incoming, tree.getEdgeAt(config.structure, node, 0, false));
						} else {
							assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
									() -> tree.getEdgeAt(config.structure, node, 0, false));
						}

						if(outgoing!=null) {
							assertSame(outgoing, tree.getEdgeAt(config.structure, node, 0, true));
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
					for (int i = 0; i < config.rootEdges.length; i++) {
						assertSame(config.rootEdges[i], tree.getEdgeAt(config.structure, root, i, true));
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
						assertEquals(config.heights[i],
								tree.getHeight(config.structure, config.nodes[i]));
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

					long height = IntStream.of(config.heights).max().orElse(UNSET_INT);
					if(height!=UNSET_LONG) {
						height++;
					}

					Item root = tree.getVirtualRoot(config.structure);
					assertEquals(height, tree.getHeight(config.structure, root));
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
						assertEquals(config.depths[i],
								tree.getDepth(config.structure, config.nodes[i]),
								"Mismatch on depths for index "+i);
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
						assertEquals(config.descendants[i],
								tree.getDescendantCount(config.structure, config.nodes[i]),
								"Descendants count mismatch for "+config.nodes[i]);
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
