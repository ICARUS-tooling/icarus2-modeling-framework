/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.model.standard.members.structure.builder;

import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.model.api.ModelTestUtils.mockEdge;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.assertSetter;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.test.TestUtils.randomId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.layer.StructureLayer;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.ImmutableStructureTest;
import de.ims.icarus2.model.api.members.structure.StructureEditVerifier;
import de.ims.icarus2.model.manifest.api.ContainerManifestBase;
import de.ims.icarus2.model.manifest.api.Hierarchy;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.standard.members.container.ItemStorage;
import de.ims.icarus2.model.standard.members.structure.EdgeStorage;
import de.ims.icarus2.test.annotations.PostponedTest;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 * @author Markus Gärtner
 *
 */
class StaticStructureTest implements ImmutableStructureTest<StaticStructure> {

	@Override
	public Class<? extends StaticStructure> getTestTargetClass() {
		return StaticStructure.class;
	}

	@Nested
	class WithInstance {
		private StaticStructure structure;

		@BeforeEach
		void setUp() {
			structure = new StaticStructure();
		}

		@AfterEach
		void tearDown() {
			structure = null;
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#getMemberType()}.
		 */
		@Test
		void testGetMemberType() {
			assertEquals(MemberType.STRUCTURE, structure.getMemberType());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#isAugmented()}.
		 */
		@Test
		void testIsAugmented() {
			assertFalse(structure.isAugmented());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#getInfo()}.
		 */
		@Test
		@PostponedTest("need to finish structure info specifications first")
		void testGetInfo() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#getBaseContainers()}.
		 */
		@Test
		void testGetBaseContainers() {
			assertNull(structure.getBaseContainers());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#isItemsComplete()}.
		 */
		@Test
		void testIsItemsComplete() {
			assertTrue(structure.isItemsComplete());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#getBoundaryContainer()}.
		 */
		@Test
		void testGetBoundaryContainer() {
			assertNull(structure.getBoundaryContainer());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#getManifest()}.
		 */
		@Test
		void testGetManifest() {
			StructureManifest manifest = mock(StructureManifest.class);
			Hierarchy<ContainerManifestBase<?>> hierarchy = mock(Hierarchy.class);
			when(hierarchy.atLevel(1)).then(inv -> manifest);

			StructureLayerManifest layerManifest = mock(StructureLayerManifest.class);
			when(layerManifest.getContainerHierarchy()).thenReturn(Optional.of(hierarchy));

			StructureLayer layer = mock(StructureLayer.class);
			when(layer.getManifest()).thenReturn(layerManifest);

			Container host = mockContainer();
			when(host.getLayer()).thenReturn(layer);

			structure.setContainer(host);

			assertNotNull(structure.getManifest());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#isEdgesComplete()}.
		 */
		@Test
		void testIsEdgesComplete() {
			assertTrue(structure.isEdgesComplete());
		}

		class Internals {

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#setNodes(de.ims.icarus2.model.standard.members.container.ItemStorage)}.
			 */
			@Test
			void testSetNodes() {
				assertSetter(structure,
						StaticStructure::setNodes,
						mock(ItemStorage.class),
						NO_NPE_CHECK,
						NO_CHECK);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#setEdges(de.ims.icarus2.model.standard.members.structure.EdgeStorage)}.
			 */
			@Test
			void testSetEdges() {
				assertSetter(structure,
						StaticStructure::setEdges,
						mock(EdgeStorage.class),
						NO_NPE_CHECK,
						NO_CHECK);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#setBaseContainers(de.ims.icarus2.util.collections.set.DataSet)}.
			 */
			@Test
			void testSetBaseContainers() {
				assertSetter(structure,
						StaticStructure::setBaseContainers,
						mock(DataSet.class),
						NO_NPE_CHECK,
						NO_CHECK);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#setBoundaryContainer(de.ims.icarus2.model.api.members.container.Container)}.
			 */
			@Test
			void testSetBoundaryContainer() {
				assertSetter(structure,
						StaticStructure::setBoundaryContainer,
						mock(Container.class),
						NO_NPE_CHECK,
						NO_CHECK);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#setAugmented(boolean)}.
			 */
			@Test
			void testSetAugmented() {
				assertSetter(structure, StaticStructure::setAugmented);
			}

		}

		@Nested
		class WithItemStorage {
			private ItemStorage itemStorage;

			@BeforeEach
			void setUp() {
				itemStorage = mock(ItemStorage.class);
				structure.setNodes(itemStorage);
			}

			@AfterEach
			void tearDown() {
				itemStorage = null;
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#getContainerType()}.
			 */
			@Test
			void testGetContainerType() {
				structure.getContainerType();
				verify(itemStorage).getContainerType();
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#getItemCount()}.
			 */
			@Test
			void testGetItemCount() {
				structure.getItemCount();
				verify(itemStorage).getItemCount(structure);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#getItemAt(long)}.
			 */
			@Test
			void testGetItemAt() {
				long index = randomId();
				structure.getItemAt(index);
				verify(itemStorage).getItemAt(structure, index);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#indexOfItem(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testIndexOfItem() {
				Item item = mockItem();
				structure.indexOfItem(item);
				verify(itemStorage).indexOfItem(structure, item);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#getBeginOffset()}.
			 */
			@Test
			void testGetBeginOffset() {
				structure.getBeginOffset();
				verify(itemStorage).getBeginOffset(structure);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#getEndOffset()}.
			 */
			@Test
			void testGetEndOffset() {
				structure.getEndOffset();
				verify(itemStorage).getEndOffset(structure);
			}
		}

		@Nested
		class WithEdgeStorage {
			private EdgeStorage edgeStorage;

			@BeforeEach
			void setUp() {
				edgeStorage = mock(EdgeStorage.class);
				structure.setEdges(edgeStorage);
			}

			@AfterEach
			void tearDown() {
				edgeStorage = null;
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#createEditVerifier()}.
			 */
			@Test
			void testCreateEditVerifier() {
				StructureEditVerifier verifier = structure.createEditVerifier();
				assertSame(structure, verifier.getSource());
				assertFalse(verifier.isAllowEdits());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#getStructureType()}.
			 */
			@Test
			void testGetStructureType() {
				structure.getStructureType();
				verify(edgeStorage).getStructureType();
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#getEdgeCount()}.
			 */
			@Test
			void testGetEdgeCount() {
				structure.getEdgeCount();
				verify(edgeStorage).getEdgeCount(structure);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#getEdgeAt(long)}.
			 */
			@Test
			void testGetEdgeAtLong() {
				long index = randomId();
				structure.getEdgeAt(index);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#indexOfEdge(de.ims.icarus2.model.api.members.item.Edge)}.
			 */
			@Test
			void testIndexOfEdge() {
				Edge edge = mockEdge();
				structure.indexOfEdge(edge);
				verify(edgeStorage).indexOfEdge(structure, edge);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#getEdgeCount(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testGetEdgeCountItem() {
				Item item = mockItem();
				structure.getEdgeCount(item);
				verify(edgeStorage).getEdgeCount(structure, item);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#getEdgeCount(de.ims.icarus2.model.api.members.item.Item, boolean)}.
			 */
			@Test
			void testGetEdgeCountItemBoolean() {
				Item item = mockItem();
				boolean isSource = random().nextBoolean();
				structure.getEdgeCount(item, isSource);
				verify(edgeStorage).getEdgeCount(structure, item, isSource);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#getEdgeAt(de.ims.icarus2.model.api.members.item.Item, long, boolean)}.
			 */
			@Test
			void testGetEdgeAtItemLongBoolean() {
				Item tiem = mockItem();
				long index = randomId();
				boolean isSource = random().nextBoolean();
				structure.getEdgeAt(tiem, index, isSource);
				verify(edgeStorage).getEdgeAt(structure, tiem, index, isSource);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#getVirtualRoot()}.
			 */
			@Test
			void testGetVirtualRoot() {
				structure.getVirtualRoot();
				verify(edgeStorage).getVirtualRoot(structure);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#isRoot(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testIsRoot() {
				Item item = mockItem();
				structure.isRoot(item);
				verify(edgeStorage).isRoot(structure, item);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#getParent(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testGetParent() {
				Item item = mockItem();
				structure.getParent(item);
				verify(edgeStorage).getParent(structure, item);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#indexOfChild(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testIndexOfChild() {
				Item item = mockItem();
				structure.indexOfChild(item);
				verify(edgeStorage).indexOfChild(structure, item);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#getSiblingAt(de.ims.icarus2.model.api.members.item.Item, long)}.
			 */
			@Test
			void testGetSiblingAt() {
				Item item = mockItem();
				long index = randomId();
				structure.getSiblingAt(item, index);
				verify(edgeStorage).getSiblingAt(structure, item, index);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#getHeight(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testGetHeight() {
				Item item = mockItem();
				structure.getHeight(item);
				verify(edgeStorage).getHeight(structure, item);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#getDepth(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testGetDepth() {
				Item item = mockItem();
				structure.getDepth(item);
				verify(edgeStorage).getDepth(structure, item);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.builder.StaticStructure#getDescendantCount(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testGetDescendantCount() {
				Item item = mockItem();
				structure.getDescendantCount(item);
				verify(edgeStorage).getDescendantCount(structure, item);
			}
		}
	}
}
