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
interface StaticChainEdgeStorageTest<C extends StaticChainEdgeStorage>
		extends ImmutableEdgeStorageTest<C> {

	/**
	 * Create a configurations for testing the basic
	 * structure methods common to all chain implementations.
	 */
	ChainsAndTrees.ChainConfig createDefaultTestConfiguration(int size);

	/**
	 * Creates a variety of configurations to be tested.
	 * Returned stream must not be empty. Default implementation
	 * returns {@link #createDefaultTestConfiguration()}.
	 */
	Stream<ChainsAndTrees.ChainConfig> createTestConfigurations();

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
	@SuppressWarnings("boxing")
	default StructureManifest createManifest(ChainsAndTrees.ChainConfig chainConfig) {
		StructureManifest manifest = mock(StructureManifest.class);
		when(manifest.getContainerType()).thenReturn(ContainerType.LIST);
		when(manifest.getStructureType()).thenReturn(StructureType.CHAIN);

		when(manifest.isStructureFlagSet(StructureFlag.MULTI_ROOT)).thenReturn(chainConfig.multiRoot);

		return manifest;
	}

	default StructureBuilder toBuilder(ChainsAndTrees.ChainConfig chainConfig) {
		chainConfig.validate();
		StructureBuilder builder = StructureBuilder.builder(createManifest(chainConfig));
		builder.createRoot();

		// Adjust source terminal of all root edges to the virtual root node
		chainConfig.finalizeRootEdges(builder.getRoot());

		builder.addNodes(chainConfig.nodes);
		builder.addEdges(chainConfig.edges);

		return builder;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default C createTestInstance(TestSettings settings) {
		ChainsAndTrees.ChainConfig chainConfig = createDefaultTestConfiguration(10);
		return settings.process(createFromBuilder(toBuilder(chainConfig)));
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
							assertNull(parent, "Unexpected parent for "+node);
						} else {
							assertSame(edge.getSource(), parent);
						}
					}

					// Root has no parent
					assertNull(chain.getParent(config.structure,
							chain.getVirtualRoot(config.structure)));
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

					// Root is not a child of any node
					assertEquals(UNSET_LONG, chain.indexOfChild(config.structure,
							chain.getVirtualRoot(config.structure)));
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
	@TestFactory
	default Stream<DynamicTest> testGetEdgeAtStructureLong() {
		return createTestConfigurations()
				.map(config -> dynamicTest(config.label, () -> {
					C chain = createFromBuilder(toBuilder(config));
					// We're not expecting a predetermined order of the edges
					Set<Edge> edges = set(config.edges);
					for (int i = 0; i < chain.getEdgeCount(config.structure); i++) {
						assertTrue(edges.remove(chain.getEdgeAt(config.structure, i)));
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
					C chain = createFromBuilder(toBuilder(config));

					for (int i = 0; i < config.edges.length; i++) {
						Edge edge = chain.getEdgeAt(i);
						assertEquals(i, chain.indexOfEdge(config.structure, edge));
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

					// Verify number of root edges
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

					assertEquals(0L, chain.getEdgeCount(config.structure, root, false));
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
								chain.getDescendantCount(config.structure, config.nodes[i]),
								"Descendants count mismatch for "+config.nodes[i]);
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
					// 1 descendant per edge in a chain
					assertEquals(config.edges.length, chain.getDescendantCount(config.structure, root));
				}));
	}
}
