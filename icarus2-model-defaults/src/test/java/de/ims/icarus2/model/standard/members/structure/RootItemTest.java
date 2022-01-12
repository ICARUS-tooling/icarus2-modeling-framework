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
package de.ims.icarus2.model.standard.members.structure;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.mockEdge;
import static de.ims.icarus2.model.api.ModelTestUtils.mockStructure;
import static de.ims.icarus2.test.TestUtils.MAX_INTEGER_INDEX;
import static de.ims.icarus2.test.TestUtils.NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.assertIOOB;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.test.TestUtils.assertRestrictedSetter;
import static de.ims.icarus2.test.TestUtils.filledArray;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.StructureFlag;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.standard.members.structure.RootItem.EmptyRootItem;
import de.ims.icarus2.model.standard.members.structure.RootItem.MultiEdgeRootItem;
import de.ims.icarus2.model.standard.members.structure.RootItem.SingleEdgeRootItem;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
class RootItemTest {

	@Nested
	class FactoryMethods {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#forStructure(de.ims.icarus2.model.api.members.structure.Structure)}.
		 */
		@Test
		@Disabled("method under test is just a delegate to the manifest-based method variant")
		void testForStructure() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#forManifest(de.ims.icarus2.model.manifest.api.StructureManifest)}.
		 */
		@SuppressWarnings("boxing")
		@Test
		void testForManifest() {
			StructureManifest manifest = mock(StructureManifest.class);

			RootItem<Edge> singleEdgeRootItem = RootItem.forManifest(manifest);
			assertNotNull(singleEdgeRootItem);

			when(manifest.isStructureFlagSet(eq(StructureFlag.MULTI_ROOT))).thenReturn(Boolean.TRUE);
			RootItem<Edge> multiEdgeRootItem = RootItem.forManifest(manifest);
			assertNotNull(multiEdgeRootItem);

			assertNotSame(singleEdgeRootItem, multiEdgeRootItem);
		}
	}

	class ForSharedMethods<R extends RootItem<Edge>> {

		R instance;

		Structure structure;

		@BeforeEach
		void setUp() {
			structure = mockStructure();
		}

		@AfterEach
		void tearDown() {
			instance = null;
			structure = null;
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#setStructure(de.ims.icarus2.model.api.members.structure.Structure)}.
		 */
		@Test
		void testSetStructure() {
			assumeTrue(instance!=null);

			assertRestrictedSetter(instance, RootItem::setStructure,
					structure, mockStructure(), NPE_CHECK,
					(exec, msg) -> assertModelException(
							ModelErrorCode.MODEL_ILLEGAL_LINKING, exec, msg));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#isTopLevel()}.
		 */
		@Test
		void testIsTopLevel() {
			assumeTrue(instance!=null);

			assertFalse(instance.isTopLevel());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#getMemberType()}.
		 */
		@Test
		void testGetMemberType() {
			assumeTrue(instance!=null);

			assertEquals(MemberType.ITEM, instance.getMemberType());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#getContainer()}.
		 */
		@Test
		void testGetContainer() {
			assumeTrue(instance!=null);

			assertNull(instance.getContainer());

			instance.setStructure(structure);
			assertSame(structure, instance.getContainer());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#getIndex()}.
		 */
		@Test
		void testGetIndex() {
			assumeTrue(instance!=null);

			assertEquals(IcarusUtils.UNSET_LONG, instance.getIndex());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#getId()}.
		 */
		@Test
		void testGetId() {
			assumeTrue(instance!=null);

			assertEquals(IcarusUtils.UNSET_LONG, instance.getId());
		}

		@Nested
		class ForIllegalIncomingEdges {

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#edgeCount(boolean)}.
			 */
			@Test
			void testEdgeCountBoolean() {
				assumeTrue(instance!=null);

				assertModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
						() -> instance.edgeCount(true));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#edgeAt(long, boolean)}.
			 */
			@Test
			void testEdgeAt() {
				assumeTrue(instance!=null);

				assertModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
						() -> instance.edgeAt(0L, true));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#addEdge(de.ims.icarus2.model.api.members.item.Edge, boolean)}.
			 */
			@Test
			void testAddEdgeEdgeBoolean() {
				assumeTrue(instance!=null);

				assertModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
						() -> instance.addEdge(mockEdge(), true));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#removeEdge(de.ims.icarus2.model.api.members.item.Edge, boolean)}.
			 */
			@Test
			void testRemoveEdgeEdgeBoolean() {
				assumeTrue(instance!=null);

				assertModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
						() -> instance.removeEdge(mockEdge(), true));
			}
		}

		@Nested
		class WithOwner {

			@BeforeEach
			void setUp() {
				instance.setStructure(structure);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#isAlive()}.
			 */
			@SuppressWarnings("boxing")
			@Test
			void testIsAlive() {
				assumeTrue(instance!=null);

				when(structure.isAlive()).thenReturn(Boolean.TRUE, Boolean.FALSE);

				assertEquals(Boolean.TRUE, instance.isAlive());
				assertEquals(Boolean.FALSE, instance.isAlive());

				verify(structure, times(2)).isAlive();
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#isLocked()}.
			 */
			@SuppressWarnings("boxing")
			@Test
			void testIsLocked() {
				assumeTrue(instance!=null);

				when(structure.isLocked()).thenReturn(Boolean.TRUE, Boolean.FALSE);

				assertEquals(Boolean.TRUE, instance.isLocked());
				assertEquals(Boolean.FALSE, instance.isLocked());

				verify(structure, times(2)).isLocked();
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#isDirty()}.
			 */
			@SuppressWarnings("boxing")
			@Test
			void testIsDirty() {
				assumeTrue(instance!=null);

				when(structure.isDirty()).thenReturn(Boolean.TRUE, Boolean.FALSE);

				assertEquals(Boolean.TRUE, instance.isDirty());
				assertEquals(Boolean.FALSE, instance.isDirty());

				verify(structure, times(2)).isDirty();
			}
		}
	}

	@Nested
	class ForEmptyRootItem extends ForSharedMethods<EmptyRootItem<Edge>> {

		@Override
		@BeforeEach
		void setUp() {
			super.setUp();
			instance = new EmptyRootItem<>();
		}

		@Nested
		class Constructors {

			@Test
			void testNoArgsConstructor() {
				EmptyRootItem<Edge> item = new EmptyRootItem<>();
				assertNull(item.getContainer());
			}

			@Test
			void testHostConstructor() {
				Structure structure = mockStructure();
				EmptyRootItem<Edge> item = new EmptyRootItem<>(structure);
				assertSame(structure, item.getContainer());
			}

			@Test
			void testHostConstructorNull() {
				assertNPE(() -> new EmptyRootItem<>(null));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#getEdgeCount()}.
		 */
		@Test
		void testGetEdgeCount() {
			assertEquals(0, instance.getEdgeCount());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#edgeCount()}.
		 */
		@Test
		void testEdgeCountConsistency() {
			assertEquals(instance.getEdgeCount(), instance.edgeCount());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#getEdgeAt(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, 1, MAX_INTEGER_INDEX})
		void testGetEdgeAt(int index) {
			assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					() -> instance.getEdgeAt(index));
			assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					() -> instance.edgeAt(index, false));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#addEdge(de.ims.icarus2.model.api.members.item.Edge)}.
		 */
		@Test
		void testAddEdgeE() {
			assertModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					() -> instance.addEdge(mockEdge()));
			assertModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					() -> instance.addEdge(mockEdge(), false));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#removeEdge(de.ims.icarus2.model.api.members.item.Edge)}.
		 */
		@Test
		void testRemoveEdgeE() {
			assertModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					() -> instance.removeEdge(mockEdge()));
			assertModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					() -> instance.removeEdge(mockEdge(), false));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#indexOfEdge(de.ims.icarus2.model.api.members.item.Edge)}.
		 */
		@Test
		void testIndexOfEdge() {
			assertEquals(UNSET_INT, instance.indexOfEdge(mockEdge()));
		}
	}

	@Nested
	class ForSingleEdgeRootItem extends ForSharedMethods<SingleEdgeRootItem<Edge>> {

		@Override
		@BeforeEach
		void setUp() {
			super.setUp();
			instance = new SingleEdgeRootItem<>();
		}

		@Nested
		class Constructors {

			@Test
			void testNoArgsConstructor() {
				SingleEdgeRootItem<Edge> item = new SingleEdgeRootItem<>();
				assertNull(item.getContainer());
			}

			@Test
			void testHostConstructor() {
				Structure structure = mockStructure();
				SingleEdgeRootItem<Edge> item = new SingleEdgeRootItem<>(structure);
				assertSame(structure, item.getContainer());
			}

			@Test
			void testHostConstructorNull() {
				assertNPE(() -> new SingleEdgeRootItem<>(null));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#getEdgeCount()}.
		 */
		@Test
		void testGetEdgeCount() {
			assertEquals(0, instance.getEdgeCount());

			instance.addEdge(mockEdge());

			assertEquals(1, instance.getEdgeCount());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#edgeCount()}.
		 */
		@Test
		void testEdgeCountConsistency() {
			assertEquals(instance.getEdgeCount(), instance.edgeCount());

			instance.addEdge(mockEdge());

			assertEquals(instance.getEdgeCount(), instance.edgeCount());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#getEdgeAt(int)}.
		 */
		@Test
		void testGetEdgeAt() {
			assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					() -> instance.getEdgeAt(0));

			// Add a single edge and verify
			Edge edge = mockEdge();
			instance.addEdge(edge);
			assertSame(edge, instance.getEdgeAt(0));

			// Verify that intended illegal indices don't work
			IntStream.of(-1, 1, 2, Integer.MAX_VALUE)
				.forEach(index -> assertModelException(
						ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
						() -> instance.getEdgeAt(index)));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#addEdge(de.ims.icarus2.model.api.members.item.Edge)}.
		 */
		@Test
		void testAddEdgeE() {
			assertNPE(() -> instance.addEdge(null));
			assertNPE(() -> instance.addEdge(null, false));

			// Must succeed
			instance.addEdge(mockEdge()); //TODO should we also use the delegate method?

			// Singleton root item can't handle more than 1 edge
			assertModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
					() -> instance.addEdge(mockEdge()));
			assertModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
					() -> instance.addEdge(mockEdge(), false));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#removeEdge(de.ims.icarus2.model.api.members.item.Edge)}.
		 */
		@Test
		void testRemoveEdgeE() {
			assertNPE(() -> instance.removeEdge(null));
			assertNPE(() -> instance.removeEdge(null, false));

			assertModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					() -> instance.removeEdge(mockEdge()));
			assertModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					() -> instance.removeEdge(mockEdge(), false));

			Edge edge = mockEdge();

			instance.addEdge(edge);
			instance.removeEdge(edge);

			instance.addEdge(edge, false);
			instance.removeEdge(edge, false);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#indexOfEdge(de.ims.icarus2.model.api.members.item.Edge)}.
		 */
		@Test
		void testIndexOfEdge() {
			assertEquals(UNSET_INT, instance.indexOfEdge(mockEdge()));

			Edge edge = mockEdge();
			instance.addEdge(edge);

			assertEquals(UNSET_INT, instance.indexOfEdge(mockEdge()));

			assertEquals(0, instance.indexOfEdge(edge));
		}
	}

	@Nested
	class ForMultiEdgeRootItem extends ForSharedMethods<MultiEdgeRootItem<Edge>> {

		final int RUNS = 10;

		@Override
		@BeforeEach
		void setUp() {
			super.setUp();
			instance = new MultiEdgeRootItem<>();
		}

		@Nested
		class Constructors {

			@Test
			void testNoArgsConstructor() {
				MultiEdgeRootItem<Edge> item = new MultiEdgeRootItem<>();
				assertNull(item.getContainer());
			}

			@Test
			void testHostConstructor() {
				Structure structure = mockStructure();
				MultiEdgeRootItem<Edge> item1 = new MultiEdgeRootItem<>(structure);
				assertSame(structure, item1.getContainer());

				MultiEdgeRootItem<Edge> item2 = new MultiEdgeRootItem<>(structure, 10);
				assertSame(structure, item2.getContainer());
			}

			@ParameterizedTest
			@ValueSource(ints = {-2, 0})
			void testHostConstructorIllegalCapacity(int capacity) {
				assertThrows(IllegalArgumentException.class,
						() -> new MultiEdgeRootItem<>(capacity));
				assertThrows(IllegalArgumentException.class,
						() -> new MultiEdgeRootItem<>(mockStructure(), capacity));
			}

			@Test
			void testHostConstructorsNull() {
				assertNPE(() -> new MultiEdgeRootItem<>(null));
				assertNPE(() -> new MultiEdgeRootItem<>(null, 10));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#getEdgeCount()}.
		 *
		 * Consistency between the 2 edge count methods is also tested.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicTest> testGetEdgeCount(RandomGenerator rng) {
			return rng.ints(RUNS, 0, 20)
					.mapToObj(count -> dynamicTest(String.valueOf(count), () -> {
						instance.removeAllEdges();
						IntStream.range(0, count).forEach(_x -> instance.addEdge(mockEdge()));
						assertEquals(count, instance.getEdgeCount());
						assertEquals(count, instance.edgeCount());
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#getEdgeAt(int)}.
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#addEdge(de.ims.icarus2.model.api.members.item.Edge)}.
		 */
		@Test
		void testGetEdgeAt() {
			Edge[] edges = filledArray(RUNS, Edge.class);

			Stream.of(edges).forEach(instance::addEdge);

			IntStream.range(0, RUNS).forEach(index ->
				assertEquals(edges[index], instance.getEdgeAt(index)));
			IntStream.range(0, RUNS).forEach(index ->
				assertEquals(edges[index], instance.edgeAt(index, false)));

			IntStream.of(-1, RUNS+1).forEach(index ->
				assertIOOB(() -> instance.getEdgeAt(index)));

			IntStream.of(-1, RUNS+1).forEach(index ->
				assertIOOB(() -> instance.edgeAt(index, false)));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#addEdge(de.ims.icarus2.model.api.members.item.Edge)}.
		 */
		@Test
		void testAddEdgeE() {
			assertNPE(() -> instance.addEdge(null));
			assertNPE(() -> instance.addEdge(null, false));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#removeEdge(de.ims.icarus2.model.api.members.item.Edge)}.
		 */
		@Test
		void testRemoveEdgeE() {
			assertNPE(() -> instance.removeEdge(null));
			assertNPE(() -> instance.removeEdge(null, false));
			Edge[] edges = filledArray(RUNS, Edge.class);

			Stream.of(edges).forEach(instance::addEdge);

			assertModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					() -> instance.removeEdge(mockEdge()));
			assertModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					() -> instance.removeEdge(mockEdge(), false));

			IntStream.rangeClosed(1, RUNS).forEach(idx -> {
				instance.removeEdge(edges[idx-1]);
				assertEquals(RUNS-idx, instance.getEdgeCount());
			});
		}

		/**
		 * Test method for {@link MultiEdgeRootItem#removeAllEdges()}.
		 */
		@Test
		@RandomizedTest
		void testRemoveAllEdges(RandomGenerator rng) {
			IntStream.range(10, rng.nextInt(10)+20).forEach(
					_x -> instance.addEdge(mockEdge()));

			assertTrue(instance.getEdgeCount()>0);

			instance.removeAllEdges();

			assertEquals(0, instance.getEdgeCount());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.RootItem#indexOfEdge(de.ims.icarus2.model.api.members.item.Edge)}.
		 */
		@Test
		void testIndexOfEdge() {
			assertNPE(() -> instance.indexOfEdge(null));

			Edge[] edges = filledArray(RUNS, Edge.class);

			Stream.of(edges).forEach(instance::addEdge);

			IntStream.range(0, RUNS).forEach(index ->
				assertEquals(index, instance.indexOfEdge(edges[index])));
		}
	}

}
