/**
 *
 */
package de.ims.icarus2.model.standard.members.structure.builder;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.assertUnsupportedOperation;
import static de.ims.icarus2.model.api.ModelTestUtils.mockEdge;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.api.ModelTestUtils.stubId;
import static de.ims.icarus2.model.api.ModelTestUtils.stubIndex;
import static de.ims.icarus2.test.TestUtils.assertListEquals;
import static de.ims.icarus2.test.TestUtils.assertMock;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.IcarusUtils.ensureIntegerValueRange;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.standard.members.structure.ImmutableEdgeStorageTest;
import de.ims.icarus2.model.standard.members.structure.RootItem;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
interface StaticChainEdgeStorageTest<C extends StaticChainEdgeStorage> extends ImmutableEdgeStorageTest<C> {

	/**
	 * Create a configurations for testing the basic
	 * structure methods common to all chain implementations.
	 */
	Config createDefaultTestConfiguration(int size);

	/**
	 * Creates a variety of configurations to be tested.
	 * Returned stream must not be empty. Default implementation
	 * returns {@link #createDefaultTestConfiguration()}.
	 */
	default Stream<Config> createTestConfigurations() {
		return Stream.of(createDefaultTestConfiguration(randomSize()));
	}

	@Provider
	C createFromBuilder(StructureBuilder builder);

	/**
	 * Create a manifest usable for testing, which should provide the following
	 * content appropriate for the structure implementation under test:<br>
	 * <ul>
	 * <li>{@link Structure#getContainerType() container type}</li>
	 * <li>{@link Structure#getStructureType() structure type}</li>
	 * </ul>
	 */
	default StructureManifest createManifest() {
		StructureManifest manifest = mock(StructureManifest.class);
		when(manifest.getContainerType()).thenReturn(ContainerType.LIST);
		when(manifest.getStructureType()).thenReturn(StructureType.CHAIN);
		return manifest;
	}

	@SuppressWarnings("unchecked")
	default StructureBuilder toBuilder(Config config) {
		config.validate();
		StructureBuilder builder = StructureBuilder.newBuilder(createManifest());

		// Adjust source terminal of all root edges to the virtual root node
		@SuppressWarnings("rawtypes")
		RootItem root = builder.getRoot();
		for(int i = 0; i<config.rootEdges.length; i++) {
			Edge rootEdge = config.rootEdges[i];
			assertMock(rootEdge);
			when(rootEdge.getSource()).thenReturn(root);
			root.addEdge(rootEdge);
		}

		builder.addNodes(config.nodes);
		builder.addEdges(config.edges);

		return builder;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default C createTestInstance(TestSettings settings) {
		Config config = createDefaultTestConfiguration(10);
		return settings.process(createFromBuilder(toBuilder(config)));
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorageTest#getExpectedStructureType()
	 */
	@Override
	default StructureType getExpectedStructureType() {
		return StructureType.CHAIN;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorage#getSiblingAt(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, long)}.
	 */
	@Test
	default void testGetSiblingAt() {
		assertUnsupportedOperation(() -> create().getSiblingAt(null, mockItem(), 0L));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorage#getParent(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetParent() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					C chain = createFromBuilder(toBuilder(config));
					for (int i = 0; i < config.nodes.length; i++) {
						Item node = config.nodes[i];
						Edge edge = config.incoming[i];
						Item parent = chain.getParent(config.structure, node);
						if(edge==null) {
							assertNull(parent);
						} else {
							assertSame(edge.getSource(), parent);
						}
					}
				}));

	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorage#indexOfChild(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIndexOfChild() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					C chain = createFromBuilder(toBuilder(config));

					for (int i = 0; i < config.nodes.length; i++) {
						Item node = config.nodes[i];
						Edge edge = config.incoming[i];
						long index = chain.indexOfChild(config.structure, node);
						if(edge==null) {
							assertEquals(UNSET_LONG, index);
						} else {
							assertEquals(0L, index);
						}
					}
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
					C chain = createFromBuilder(toBuilder(config));

					Set<Item> roots = Stream.of(config.rootEdges)
							.map(Edge::getTarget)
							.collect(Collectors.toSet());

					for (int i = 0; i < config.nodes.length; i++) {
						Item node = config.nodes[i];
						assertEquals(roots.contains(node), chain.isRoot(config.structure, node));
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
					C chain = createFromBuilder(toBuilder(config));
					assertEquals(config.edges.length, chain.getEdgeCount(config.structure));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.AbstractStaticEdgeStorage#getEdgeAt(de.ims.icarus2.model.api.members.structure.Structure, long)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default Stream<DynamicTest> testGetEdgeAtStructureLong() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					C chain = createFromBuilder(toBuilder(config));
					assertListEquals(chain,
							c -> ensureIntegerValueRange(c.getEdgeCount(config.structure)),
							(c, i) -> c.getEdgeAt(config.structure, i),
							config.edges);
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.AbstractStaticEdgeStorage#indexOfEdge(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Edge)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIndexOfEdgeStructureEdge() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					C chain = createFromBuilder(toBuilder(config));

					for (int i = 0; i < config.edges.length; i++) {
						assertEquals(i, chain.indexOfEdge(config.structure, config.edges[i]));
					}

					// Ensure that foreign edges aren't recognized
					assertEquals(UNSET_LONG, chain.indexOfEdge(config.structure, mockEdge()));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.AbstractStaticEdgeStorage#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetEdgeCountStructureItem() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					C chain = createFromBuilder(toBuilder(config));

					for (int i = 0; i < config.nodes.length; i++) {
						Item node = config.nodes[i];
						long expected = 0;
						if(config.incoming[i]!=null) expected++;
						if(config.outgoing[i]!=null) expected++;

						long count = chain.getEdgeCount(config.structure, node);
						assertEquals(expected, count, "Edge count mismatch at index "+i);
					}

					Item root = chain.getVirtualRoot(config.structure);
					assertEquals(config.rootEdges.length, chain.getEdgeCount(config.structure, root));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorage.CompactChainEdgeStorageInt#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, boolean)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetEdgeCountStructureItemBoolean() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					C chain = createFromBuilder(toBuilder(config));

					for (int i = 0; i < config.nodes.length; i++) {
						Item node = config.nodes[i];

						assertEquals(config.outgoing[i]==null ? 0 : 1,
								chain.getEdgeCount(config.structure, node, true),
								"Outgoing edge count mismatch at index "+i);
						assertEquals(config.incoming[i]==null ? 0 : 1,
								chain.getEdgeCount(config.structure, node, false),
								"Incoming edge count mismatch at index "+i);
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorage.CompactChainEdgeStorageInt#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, boolean)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetEdgeCountStructureItemBooleanRoot() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					C chain = createFromBuilder(toBuilder(config));

					Item root = chain.getVirtualRoot(config.structure);
					assertEquals(config.rootEdges.length, chain.getEdgeCount(config.structure, root, true));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorage.CompactChainEdgeStorageInt#getEdgeAt(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, long, boolean)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetEdgeAtStructureItemLongBoolean() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					C chain = createFromBuilder(toBuilder(config));

					for (int i = 0; i < config.nodes.length; i++) {
						Item node = config.nodes[i];
						Edge incoming = config.incoming[i];
						Edge outgoing = config.outgoing[i];

						if(incoming!=null) {
							assertSame(incoming, chain.getEdgeAt(config.structure, node, 0, false));
						} else {
							assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
									() -> chain.getEdgeAt(config.structure, node, 0, false));
						}

						if(outgoing!=null) {
							assertSame(outgoing, chain.getEdgeAt(config.structure, node, 0, true));
						} else {
							assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
									() -> chain.getEdgeAt(config.structure, node, 0, true));
						}
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorage.CompactChainEdgeStorageInt#getEdgeAt(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, long, boolean)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetEdgeAtStructureItemLongBooleanRoot() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					C chain = createFromBuilder(toBuilder(config));

					Item root = chain.getVirtualRoot(config.structure);
					for (int i = 0; i < config.rootEdges.length; i++) {
						assertSame(config.rootEdges[i], chain.getEdgeAt(config.structure, root, i, true));
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorage.CompactChainEdgeStorageInt#getHeight(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetHeight() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					C chain = createFromBuilder(toBuilder(config));
					for (int i = 0; i < config.nodes.length; i++) {
						assertEquals(config.heights[i],
								chain.getHeight(config.structure, config.nodes[i]));
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorage.CompactChainEdgeStorageInt#getHeight(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetHeightRoot() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					C chain = createFromBuilder(toBuilder(config));

					long height = IntStream.of(config.heights).max().orElse(UNSET_INT);
					if(height!=UNSET_LONG) {
						height++;
					}

					Item root = chain.getVirtualRoot(config.structure);
					assertEquals(height, chain.getHeight(config.structure, root));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorage.CompactChainEdgeStorageInt#getDepth(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetDepth() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					C chain = createFromBuilder(toBuilder(config));
					for (int i = 0; i < config.nodes.length; i++) {
						assertEquals(config.depths[i],
								chain.getDepth(config.structure, config.nodes[i]),
								"Mismatch on depths for index "+i);
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorage.CompactChainEdgeStorageInt#getDepth(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetDepthRoot() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					C chain = createFromBuilder(toBuilder(config));

					Item root = chain.getVirtualRoot(config.structure);
					assertEquals(0L, chain.getDepth(config.structure, root));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorage.CompactChainEdgeStorageInt#getDescendantCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetDescendantCount() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					C chain = createFromBuilder(toBuilder(config));
					for (int i = 0; i < config.nodes.length; i++) {
						assertEquals(config.descendants[i],
								chain.getDescendantCount(config.structure, config.nodes[i]));
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorage.CompactChainEdgeStorageInt#getDescendantCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetDescendantCountRoot() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					C chain = createFromBuilder(toBuilder(config));

					Item root = chain.getVirtualRoot(config.structure);
					assertEquals(config.nodes.length, chain.getDescendantCount(config.structure, root));
				}));
	}

	default int randomSize() {
		return 4; //random(10, 20);
	}

	static class Config {
		/** Human readable label for displaying the created test */
		String label;
		/** All nodes */
		Item[] nodes;
		/**
		 * Edges to designated roots, minimum of 1.
		 * Note that due to the fact that the virtual root node isn't available
		 * by the time this config gets constructed, we need to lazily add the
		 * source terminals for all these root edges!
		 */
		Edge[] rootEdges;
		/** Raw edges, all non-null */
		Edge[] edges;
		/** For every node, lists the incoming edge or null */
		Edge[] incoming;
		/** For every node other than the virtual root, lists the outgoing edge or null */
		Edge[] outgoing;

		int[] heights;
		int[] depths;
		int[] descendants;

		/** Optional structure to be used for calls on the storage under test */
		Structure structure = null;

		private Config validate() {
			assertNotNull(label);
			assertNotNull(nodes);
			assertNotNull(rootEdges);
			assertNotNull(edges);
			assertNotNull(incoming);
			assertNotNull(outgoing);
			return this;
		}

		static Config basic(int size) {
			Config config = new Config();
			config.nodes = IntStream.range(0, size)
					.mapToObj(i -> stubId(stubIndex(mockItem(), i), i))
					.toArray(Item[]::new);
			config.incoming = new Edge[size];
			config.outgoing = new Edge[size];
			config.heights = new int[size];
			config.depths = new int[size];
			config.descendants = new int[size];
			return config;
		}
	}
}
