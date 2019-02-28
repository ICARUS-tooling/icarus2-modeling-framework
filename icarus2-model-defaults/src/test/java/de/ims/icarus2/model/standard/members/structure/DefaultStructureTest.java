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
package de.ims.icarus2.model.standard.members.structure;

import static de.ims.icarus2.SharedTestUtils.mockSequence;
import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.model.api.ModelTestUtils.mockEdge;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.test.TestTags.RANDOMIZED;
import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.test.TestUtils.assertFlagGetter;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.test.TestUtils.assertSetter;
import static de.ims.icarus2.test.TestUtils.filledArray;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.test.TestUtils.randomLongPair;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.members.structure.StructureEditVerifier;
import de.ims.icarus2.model.api.members.structure.StructureTest;
import de.ims.icarus2.model.manifest.api.ContainerFlag;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.StructureFlag;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.standard.members.container.ItemStorage;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.util.collections.seq.DataSequence;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 * @author Markus Gärtner
 *
 */
class DefaultStructureTest implements StructureTest<Structure> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends Structure> getTestTargetClass() {
		return DefaultStructure.class;
	}

	@Nested
	class Constructors {

		@Test
		void testNoArgsConstructor() {
			new DefaultStructure();
		}

		@Test
		void testHostConstructor() {
			new DefaultStructure(mockContainer());
		}

		@Test
		void testHostConstructorNull() {
			assertNPE(() -> new DefaultStructure(null));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#DefaultStructure(de.ims.icarus2.model.standard.members.container.ItemStorage, de.ims.icarus2.model.standard.members.structure.EdgeStorage)}.
		 */
		@Test
		void testNullStorages() {
			new DefaultStructure(mockContainer(), null, null);
		}
	}

	@Nested
	class WithBareInstance {
		private DefaultStructure instance;


		@BeforeEach
		void setUp() {
			instance = new DefaultStructure();
		}


		@AfterEach
		void tearDown() {
			instance = null;
		}

		/**
		 * TEst method for {@link DefaultStructure#getMemberType()}
		 */
		@Test
		void testGetMemberType() {
			assertEquals(MemberType.STRUCTURE, instance.getMemberType());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getManifest()}.
		 */
		@Test
		void testGetManifestIllegalState() {
			assertThrows(IllegalStateException.class, () -> instance.getManifest());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#isEdgesComplete()}.
		 */
		@Test
		void testIsEdgesComplete() {
			assertFlagGetter(instance, Boolean.FALSE,
					DefaultStructure::isEdgesComplete,
					DefaultStructure::setEdgesComplete);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#setEdgesComplete(boolean)}.
		 */
		@Test
		void testSetEdgesComplete() {
			assertSetter(instance, DefaultStructure::setItemsComplete);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#isAugmented()}.
		 */
		@Test
		void testIsAugmented() {
			assertFlagGetter(instance, Boolean.FALSE,
					DefaultStructure::isAugmented,
					DefaultStructure::setAugmented);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#setAugmented(boolean)}.
		 */
		@Test
		void testSetAugmented() {
			assertSetter(instance, DefaultStructure::setAugmented);
		}

		@Nested
		class ExpectingIllegalState {

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getInfo()}.
			 */
			@Test
			void testGetInfo() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE, () -> instance.getInfo());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getVirtualRoot()}.
			 */
			@Test
			void testGetVirtualRoot() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE, () -> instance.getVirtualRoot());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#edgeStorage()}.
			 */
			@Test
			void testEdgeStorage() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.edgeStorage());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getEdgeCount()}.
			 */
			@Test
			void testGetEdgeCount() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.getEdgeCount());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getEdgeAt(long)}.
			 */
			@Test
			void testGetEdgeAtLong() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.getEdgeAt(1L));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#indexOfEdge(de.ims.icarus2.model.api.members.item.Edge)}.
			 */
			@Test
			void testIndexOfEdge() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.indexOfEdge(mockEdge()));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getEdgeCount(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testGetEdgeCountItem() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.getEdgeCount(mockItem()));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getEdgeCount(de.ims.icarus2.model.api.members.item.Item, boolean)}.
			 */
			@Test
			void testGetEdgeCountItemBoolean() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.getEdgeCount(mockItem(), false));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getEdgeAt(de.ims.icarus2.model.api.members.item.Item, long, boolean)}.
			 */
			@Test
			void testGetEdgeAtItemLongBoolean() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.getEdgeAt(mockItem(), 1L, false));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#isRoot(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testIsRoot() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.isRoot(mockItem()));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getParent(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testGetParent() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.getParent(mockItem()));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#indexOfChild(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testIndexOfChild() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.indexOfChild(mockItem()));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getSiblingAt(de.ims.icarus2.model.api.members.item.Item, long)}.
			 */
			@Test
			void testGetSiblingAt() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.getSiblingAt(mockItem(), 1L));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getHeight(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testGetHeight() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.getHeight(mockItem()));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getDepth(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testGetDepth() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.getDepth(mockItem()));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getDescendantCount(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testGetDescendantCount() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.getDescendantCount(mockItem()));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#addEdge(long, de.ims.icarus2.model.api.members.item.Edge)}.
			 */
			@Test
			void testAddEdge() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.addEdge(1L, mockEdge()));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#addEdges(long, de.ims.icarus2.util.collections.seq.DataSequence)}.
			 */
			@Test
			void testAddEdges() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.addEdges(1L, mockSequence()));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#removeEdge(long)}.
			 */
			@Test
			void testRemoveEdge() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.removeEdge(1L));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#removeEdges(long, long)}.
			 */
			@Test
			void testRemoveEdges() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.removeEdges(1L, 2L));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#swapEdges(long, long)}.
			 */
			@Test
			void testSwapEdges() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.swapEdges(1L, 2l));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#setTerminal(de.ims.icarus2.model.api.members.item.Edge, de.ims.icarus2.model.api.members.item.Item, boolean)}.
			 */
			@Test
			void testSetTerminal() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.setTerminal(mockEdge(), mockItem(), false));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#newEdge(de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testNewEdge() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.newEdge(mockItem(), mockItem()));
			}
		}
	}

	@Nested
	class WithManifestEnvironment {
		StructureManifest manifest;
		Container host;
		ItemStorage itemStorage;
		EdgeStorage edgeStorage;

		DefaultStructure instance;

		@BeforeEach
		void setUp() {
			manifest = mock(StructureManifest.class);
			when(manifest.getContainerType()).thenReturn(ContainerType.LIST);
			when(manifest.getStructureType()).thenReturn(StructureType.CHAIN);

			itemStorage = mock(ItemStorage.class);
			when(itemStorage.getContainerType()).thenReturn(ContainerType.LIST);

			edgeStorage = mock(EdgeStorage.class);
			when(edgeStorage.getStructureType()).thenReturn(StructureType.CHAIN);

			host = mockContainer();
			when((StructureManifest)host.getManifest()).thenReturn(manifest);
		}

		@AfterEach
		void tearDown() {
			manifest = null;
			host = null;
			itemStorage = null;
			instance = null;
		}

	}

	@Nested
	class WithCustomSetup extends WithManifestEnvironment {
		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#recycle()}.
		 */
		@Test
		void testRecycle() {
			instance = new DefaultStructure(host);

			DataSet<Container> baseContainers = mock(DataSet.class);

			instance.setBaseContainers(baseContainers);
			instance.setBoundaryContainer(mockContainer());
			instance.setItemStorage(itemStorage);
			instance.setEdgeStorage(edgeStorage);

			instance.recycle();

			assertNull(instance.getBoundaryContainer());
			assertNull(instance.getItemStorage());
			assertNull(instance.getEdgeStorage());
			assertNotSame(baseContainers, instance.getBaseContainers());
			assertTrue(instance.getBaseContainers().isEmpty());

			verify(itemStorage).removeNotify(eq(instance));
			verify(edgeStorage).removeNotify(eq(instance));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#setEdgeStorage(de.ims.icarus2.model.standard.members.structure.EdgeStorage)}.
		 */
		@Test
		void testSetEdgeStorage() {
			instance = new DefaultStructure(host);

			assertSetter(instance, DefaultStructure::setEdgeStorage, edgeStorage, NO_NPE_CHECK, NO_CHECK);

			verify(edgeStorage).addNotify(eq(instance));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#checkEdgeStorage(de.ims.icarus2.model.standard.members.structure.EdgeStorage)}.
		 */
		@TestFactory
		Stream<DynamicTest> testCheckEdgeStorage() {
			return Stream.of(StructureType.values())
					.flatMap(typeFromManifest -> Stream.of(typeFromManifest.getCompatibleTypes())
							.map(typeFromStorage -> dynamicTest(
									String.format("manifest=%s, storage=%s",
											typeFromManifest, typeFromStorage), () -> {

								instance = new DefaultStructure(host);

								when(manifest.getStructureType()).thenReturn(typeFromManifest);
								when(edgeStorage.getStructureType()).thenReturn(typeFromStorage);

								instance.checkEdgeStorage(edgeStorage);
							})));
		}
	}

	@Nested
	class WithPresetStorage extends WithManifestEnvironment {

		@Override
		@BeforeEach
		void setUp() {
			super.setUp();
			instance = new DefaultStructure(host, itemStorage, edgeStorage);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#isDirty()}.
		 */
		@SuppressWarnings("boxing")
		@Test
		void testIsDirty() {
			when(itemStorage.isDirty(any())).thenReturn(false);

			// Base state
			assertFalse(instance.isDirty());

			// Assert honoring of manual flag
			instance.setDirty(true);
			assertTrue(instance.isDirty());

			// Assert honoring of item storage
			instance.setDirty(false);
			when(itemStorage.isDirty(any())).thenReturn(true);
			assertTrue(instance.isDirty());

			// Assert honoring of item storage
			when(itemStorage.isDirty(any())).thenReturn(false);
			when(edgeStorage.isDirty(any())).thenReturn(true);
			assertTrue(instance.isDirty());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getManifest()}.
		 */
		@Test
		void testGetManifest() {
			assertSame(manifest, instance.getManifest());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#edgeStorage()}.
		 */
		@Test
		void testEdgeStorage() {
			assertSame(edgeStorage, instance.edgeStorage());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getInfo()}.
		 */
		@Test
		void testGetInfoEmpty() {
			assertNull(instance.getInfo());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getVirtualRoot()}.
		 */
		@Test
		void testGetVirtualRoot() {
			Item root = mockItem();
			when(edgeStorage.getVirtualRoot(eq(instance))).thenReturn(root);

			assertEquals(root, instance.getVirtualRoot());

			verify(edgeStorage).getVirtualRoot(eq(instance));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getStructureType()}.
		 */
		@TestFactory
		Stream<DynamicTest> testGetStructureType() {
			return Stream.of(StructureType.values())
					.map(structureType -> dynamicTest(structureType.name(), () -> {
						when(edgeStorage.getStructureType()).thenReturn(structureType);
						assertEquals(structureType, instance.getStructureType());
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#clearEdgeStorage()}.
		 */
		@Test
		void testClearEdgeStorage() {
			instance.clearEdgeStorage();;

			assertNull(instance.getEdgeStorage());

			verify(edgeStorage).removeNotify(eq(instance));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#createEditVerifier()}.
		 */
		@SuppressWarnings("boxing")
		@Test
		void testCreateEditVerifier() {
			when(manifest.isContainerFlagSet(any())).thenReturn(Boolean.FALSE);
			when(manifest.isStructureFlagSet(any())).thenReturn(Boolean.FALSE);

			ContainerEditVerifier containerEditVerifier = mock(ContainerEditVerifier.class);

			StructureEditVerifier verifierForStatic = instance.createEditVerifier();
			assertNotNull(verifierForStatic);
			assertFalse(verifierForStatic.isAllowEdits());

			// Make sure we have control over the container edit verifier
			when(manifest.isContainerFlagSet(eq(ContainerFlag.NON_STATIC))).thenReturn(Boolean.TRUE);
			when(itemStorage.createEditVerifier(eq(instance))).thenReturn(containerEditVerifier);

			// Assert mirroring of the basic edit flag
			StructureEditVerifier verifierForMutableItems = instance.createEditVerifier();
			when(containerEditVerifier.isAllowEdits()).thenReturn(Boolean.TRUE);
			assertTrue(verifierForMutableItems.isAllowEdits());
			when(containerEditVerifier.isAllowEdits()).thenReturn(Boolean.FALSE);
			assertFalse(verifierForMutableItems.isAllowEdits());

			// Now allow for fully mutable items and edges
			when(manifest.isStructureFlagSet(eq(StructureFlag.NON_STATIC))).thenReturn(Boolean.TRUE);
			StructureEditVerifier verifier = mock(StructureEditVerifier.class);
			when(edgeStorage.createEditVerifier(eq(instance), eq(containerEditVerifier))).thenReturn(verifier);
			StructureEditVerifier verifierForEditable = instance.createEditVerifier();
			assertSame(verifier, verifierForEditable);
			verify(edgeStorage).createEditVerifier(eq(instance), eq(containerEditVerifier));
		}

		@Nested
		class ExpectStorageDelegation {

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getEdgeCount()}.
			 */
			@SuppressWarnings("boxing")
			@Test
			void testGetEdgeCount() {
				when(edgeStorage.getEdgeCount(eq(instance))).thenReturn(0L, 1L, Long.MAX_VALUE);

				assertEquals(0L, instance.getEdgeCount());
				assertEquals(1L, instance.getEdgeCount());
				assertEquals(Long.MAX_VALUE, instance.getEdgeCount());

				verify(edgeStorage, times(3)).getEdgeCount(eq(instance));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getEdgeAt(long)}.
			 */
			@Test
			@Tag(RANDOMIZED)
			void testGetEdgeAtLong() {
				Edge[] edges = filledArray(RUNS, Edge.class);
				long[] indices = random().longs(edges.length, 0, Long.MAX_VALUE).toArray();

				IntStream.range(0, edges.length).forEach(
						idx -> when(edgeStorage.getEdgeAt(eq(instance), eq(indices[idx])))
							.thenReturn(edges[idx]));

				IntStream.range(0, edges.length).forEach(
						idx -> assertEquals(edges[idx], instance.getEdgeAt(indices[idx])));


				IntStream.range(0, edges.length).forEach(
						idx -> verify(edgeStorage).getEdgeAt(eq(instance), eq(indices[idx])));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#indexOfEdge(de.ims.icarus2.model.api.members.item.Edge)}.
			 */
			@SuppressWarnings("boxing")
			@Test
			@Tag(RANDOMIZED)
			void testIndexOfEdge() {
				Edge[] edges = filledArray(10, Edge.class);
				long[] indices = random().longs(edges.length, 0, Long.MAX_VALUE).toArray();

				IntStream.range(0, edges.length).forEach(
						idx -> when(edgeStorage.indexOfEdge(eq(instance), eq(edges[idx])))
							.thenReturn(indices[idx]));

				IntStream.range(0, edges.length).forEach(
						idx -> assertEquals(indices[idx], instance.indexOfEdge(edges[idx])));

				IntStream.range(0, edges.length).forEach(
						idx -> verify(edgeStorage).indexOfEdge(eq(instance), eq(edges[idx])));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getEdgeCount(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@SuppressWarnings("boxing")
			@Test
			@Tag(RANDOMIZED)
			void testGetEdgeCountItem() {
				Item[] items = filledArray(10, Item.class);
				long[] counts = random().longs(RUNS, 0, Long.MAX_VALUE).toArray();

				IntStream.range(0, RUNS).forEach(
						idx -> when(edgeStorage.getEdgeCount(eq(instance), eq(items[idx])))
							.thenReturn(counts[idx]));

				IntStream.range(0, RUNS).forEach(
						idx -> assertEquals(counts[idx], instance.getEdgeCount(items[idx])));

				IntStream.range(0, RUNS).forEach(
						idx -> verify(edgeStorage).getEdgeCount(eq(instance), eq(items[idx])));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getEdgeCount(de.ims.icarus2.model.api.members.item.Item, boolean)}.
			 */
			@SuppressWarnings("boxing")
			@Test
			@Tag(RANDOMIZED)
			void testGetEdgeCountItemBoolean() {
				Item[] items = filledArray(RUNS, Item.class);
				long[] counts = random().longs(RUNS, 0, Long.MAX_VALUE).toArray();
				boolean isSource = random().nextBoolean();

				IntStream.range(0, RUNS).forEach(
						idx -> when(edgeStorage.getEdgeCount(
								eq(instance), eq(items[idx]), eq(isSource)))
							.thenReturn(counts[idx]));

				IntStream.range(0, RUNS).forEach(
						idx -> assertEquals(counts[idx],
								instance.getEdgeCount(items[idx], isSource)));

				IntStream.range(0, RUNS).forEach(
						idx -> verify(edgeStorage).getEdgeCount(
								eq(instance), eq(items[idx]), eq(isSource)));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getEdgeAt(de.ims.icarus2.model.api.members.item.Item, long, boolean)}.
			 */
			@Test
			@Tag(RANDOMIZED)
			void testGetEdgeAtItemLongBoolean() {
				Item[] items = filledArray(RUNS, Item.class);
				Edge[] edges = filledArray(RUNS, Edge.class);
				long[] indices = random().longs(RUNS, 0, Long.MAX_VALUE).toArray();
				boolean isSource = random().nextBoolean();

				IntStream.range(0, RUNS).forEach(
						idx -> when(edgeStorage.getEdgeAt(
								eq(instance), eq(items[idx]), eq(indices[idx]), eq(isSource)))
							.thenReturn(edges[idx]));

				IntStream.range(0, RUNS).forEach(
						idx -> assertEquals(edges[idx],
								instance.getEdgeAt(items[idx], indices[idx], isSource)));

				IntStream.range(0, RUNS).forEach(
						idx -> verify(edgeStorage).getEdgeAt(
								eq(instance), eq(items[idx]), eq(indices[idx]), eq(isSource)));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#isRoot(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@SuppressWarnings("boxing")
			@Test
			void testIsRoot() {
				Item root = mockItem();
				Item notRoot = mockItem();

				when(edgeStorage.isRoot(eq(instance), eq(root))).thenReturn(Boolean.TRUE);
				when(edgeStorage.isRoot(eq(instance), eq(notRoot))).thenReturn(Boolean.FALSE);

				assertTrue(instance.isRoot(root));
				assertFalse(instance.isRoot(notRoot));

				verify(edgeStorage).isRoot(eq(instance), eq(root));
				verify(edgeStorage).isRoot(eq(instance), eq(notRoot));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getParent(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testGetParent() {
				Item[] items = filledArray(RUNS, Item.class);
				Item[] parents = filledArray(RUNS, Item.class);

				IntStream.range(0, RUNS).forEach(
						idx -> when(edgeStorage.getParent(
								eq(instance), eq(items[idx])))
							.thenReturn(parents[idx]));

				IntStream.range(0, RUNS).forEach(
						idx -> assertEquals(parents[idx],
								instance.getParent(items[idx])));

				IntStream.range(0, RUNS).forEach(
						idx -> verify(edgeStorage).getParent(
								eq(instance), eq(items[idx])));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#indexOfChild(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@SuppressWarnings("boxing")
			@Test
			@Tag(RANDOMIZED)
			void testIndexOfChild() {
				Item[] items = filledArray(RUNS, Item.class);
				long[] indices = random().longs(RUNS, 0, Long.MAX_VALUE).toArray();

				IntStream.range(0, RUNS).forEach(
						idx -> when(edgeStorage.indexOfChild(
								eq(instance), eq(items[idx])))
							.thenReturn(indices[idx]));

				IntStream.range(0, RUNS).forEach(
						idx -> assertEquals(indices[idx],
								instance.indexOfChild(items[idx])));

				IntStream.range(0, RUNS).forEach(
						idx -> verify(edgeStorage).indexOfChild(
								eq(instance), eq(items[idx])));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getSiblingAt(de.ims.icarus2.model.api.members.item.Item, long)}.
			 */
			@Test
			@Tag(RANDOMIZED)
			void testGetSiblingAt() {
				Item[] items = filledArray(RUNS, Item.class);
				long[] indices = random().longs(RUNS).toArray();
				Item[] siblings = filledArray(RUNS, Item.class);

				IntStream.range(0, RUNS).forEach(
						idx -> when(edgeStorage.getSiblingAt(
								eq(instance), eq(items[idx]), eq(indices[idx])))
							.thenReturn(siblings[idx]));

				IntStream.range(0, RUNS).forEach(
						idx -> assertEquals(siblings[idx],
								instance.getSiblingAt(items[idx], indices[idx])));

				IntStream.range(0, RUNS).forEach(
						idx -> verify(edgeStorage).getSiblingAt(
								eq(instance), eq(items[idx]), eq(indices[idx])));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getHeight(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@SuppressWarnings("boxing")
			@Test
			@Tag(RANDOMIZED)
			void testGetHeight() {
				Item[] items = filledArray(RUNS, Item.class);
				long[] heights = random().longs(RUNS).toArray();

				IntStream.range(0, RUNS).forEach(
						idx -> when(edgeStorage.getHeight(
								eq(instance), eq(items[idx])))
							.thenReturn(heights[idx]));

				IntStream.range(0, RUNS).forEach(
						idx -> assertEquals(heights[idx],
								instance.getHeight(items[idx])));

				IntStream.range(0, RUNS).forEach(
						idx -> verify(edgeStorage).getHeight(
								eq(instance), eq(items[idx])));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getDepth(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@SuppressWarnings("boxing")
			@Test
			@Tag(RANDOMIZED)
			void testGetDepth() {
				Item[] items = filledArray(RUNS, Item.class);
				long[] depths = random().longs(RUNS).toArray();

				IntStream.range(0, RUNS).forEach(
						idx -> when(edgeStorage.getDepth(
								eq(instance), eq(items[idx])))
							.thenReturn(depths[idx]));

				IntStream.range(0, RUNS).forEach(
						idx -> assertEquals(depths[idx],
								instance.getDepth(items[idx])));

				IntStream.range(0, RUNS).forEach(
						idx -> verify(edgeStorage).getDepth(
								eq(instance), eq(items[idx])));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#getDescendantCount(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@SuppressWarnings("boxing")
			@Test
			@Tag(RANDOMIZED)
			void testGetDescendantCount() {
				Item[] items = filledArray(RUNS, Item.class);
				long[] counts = random().longs(RUNS).toArray();

				IntStream.range(0, RUNS).forEach(
						idx -> when(edgeStorage.getDescendantCount(
								eq(instance), eq(items[idx])))
							.thenReturn(counts[idx]));

				IntStream.range(0, RUNS).forEach(
						idx -> assertEquals(counts[idx],
								instance.getDescendantCount(items[idx])));

				IntStream.range(0, RUNS).forEach(
						idx -> verify(edgeStorage).getDescendantCount(
								eq(instance), eq(items[idx])));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#addEdge(long, de.ims.icarus2.model.api.members.item.Edge)}.
			 */
			@Test
			@Tag(RANDOMIZED)
			void testAddEdge() {
				Edge[] edges = filledArray(RUNS, Edge.class);
				long[] indices = random().longs(RUNS, 0, Long.MAX_VALUE).toArray();

				IntStream.range(0, RUNS).forEach(
						idx -> instance.addEdge(indices[idx], edges[idx]));

				IntStream.range(0, RUNS).forEach(
						idx -> verify(edgeStorage).addEdge(eq(instance), eq(indices[idx]), eq(edges[idx])));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#addEdges(long, de.ims.icarus2.util.collections.seq.DataSequence)}.
			 */
			@SuppressWarnings("unchecked")
			@Test
			@Tag(RANDOMIZED)
			void testAddEdges() {
				@SuppressWarnings("rawtypes")
				DataSequence[] edges = filledArray(RUNS, DataSequence.class);
				long[] indices = random().longs(RUNS, 0, Long.MAX_VALUE).toArray();

				IntStream.range(0, RUNS).forEach(
						idx -> instance.addEdges(indices[idx], edges[idx]));

				IntStream.range(0, RUNS).forEach(
						idx -> verify(edgeStorage).addEdges(eq(instance), eq(indices[idx]), eq(edges[idx])));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#removeEdge(long)}.
			 */
			@Test
			void testRemoveEdge() {
				long[] indices = random().longs(RUNS, 0, Long.MAX_VALUE).toArray();

				LongStream.of(indices).forEach(instance::removeEdge);

				LongStream.of(indices).forEach(index -> verify(edgeStorage).removeEdge(eq(instance), eq(index)));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#removeEdges(long, long)}.
			 */
			@SuppressWarnings({ "unchecked", "boxing" })
			@Test
			void testRemoveEdges() {
				@SuppressWarnings("rawtypes")
				Pair[] indices = {
					randomLongPair(0, Long.MAX_VALUE),
					randomLongPair(0, Long.MAX_VALUE),
					randomLongPair(0, Long.MAX_VALUE),
				};
				@SuppressWarnings("rawtypes")
				DataSequence[] edges = filledArray(indices.length, DataSequence.class);
				IntStream.range(0, indices.length).forEach(
						idx -> {
							Pair<Long, Long> p = indices[idx];
							when(edgeStorage.removeEdges(eq(instance), eq(p.first), eq(p.second)))
								.thenReturn(edges[idx]);
							});
				IntStream.range(0, indices.length).forEach(
						idx -> {
							Pair<Long, Long> p = indices[idx];
							assertEquals(edges[idx], instance.removeEdges(p.first, p.second));
							});

				IntStream.range(0, indices.length).forEach(
						idx -> {
							Pair<Long, Long> p = indices[idx];
							verify(edgeStorage).removeEdges(eq(instance), eq(p.first), eq(p.second));
						});
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#swapEdges(long, long)}.
			 */
			@SuppressWarnings("boxing")
			@Test
			void testSwapEdges() {
				@SuppressWarnings("rawtypes")
				Pair[] indices = {
					randomLongPair(0, Long.MAX_VALUE),
					randomLongPair(0, Long.MAX_VALUE),
					randomLongPair(0, Long.MAX_VALUE),
					randomLongPair(0, Long.MAX_VALUE),
				};
				IntStream.range(0, indices.length).forEach(
						idx -> {
							@SuppressWarnings("unchecked")
							Pair<Long, Long> p = indices[idx];
							instance.swapEdges(p.first, p.second);
							});

				IntStream.range(0, indices.length).forEach(
						idx -> {
							@SuppressWarnings("unchecked")
							Pair<Long, Long> p = indices[idx];
							verify(edgeStorage).swapEdges(eq(instance), eq(p.first), eq(p.second));
						});
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#setTerminal(de.ims.icarus2.model.api.members.item.Edge, de.ims.icarus2.model.api.members.item.Item, boolean)}.
			 */
			@RepeatedTest(value=5)
			void testSetTerminal() {
				Item[] items = filledArray(RUNS, Item.class);
				Edge[] edges = filledArray(RUNS, Edge.class);
				boolean isSource = random().nextBoolean();

				IntStream.range(0, RUNS).forEach(
						idx -> instance.setTerminal(edges[idx], items[idx], isSource));

				IntStream.range(0, RUNS).forEach(
						idx -> verify(edgeStorage).setTerminal(eq(instance), eq(edges[idx]),
								eq(items[idx]), eq(isSource)));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultStructure#newEdge(de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testNewEdge() {
				Item[] sources = filledArray(RUNS, Item.class);
				Item[] targets = filledArray(RUNS, Item.class);

				IntStream.range(0, RUNS).forEach(
						idx -> instance.newEdge(sources[idx], targets[idx]));

				IntStream.range(0, RUNS).forEach(
						idx -> verify(edgeStorage).newEdge(eq(instance),
								eq(sources[idx]), eq(targets[idx])));
			}
		}
	}

}
