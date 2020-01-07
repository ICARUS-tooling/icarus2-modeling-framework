/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.container;

import static de.ims.icarus2.SharedTestUtils.mockSequence;
import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.model.api.ModelTestUtils.mockEdge;
import static de.ims.icarus2.model.api.ModelTestUtils.mockStructure;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
class EdgeProxyStorageTest implements ItemStorageTest<EdgeProxyStorage> {

	@Override
	public Class<? extends EdgeProxyStorage> getTestTargetClass() {
		return EdgeProxyStorage.class;
	}

	@Override
	public EdgeProxyStorage createTestInstance(TestSettings settings) {
		return settings.process(new EdgeProxyStorage(mockStructure()));
	}

	@Override
	public ContainerType getExpectedContainerType() {
		return ContainerType.LIST;
	}

	@Nested
	class WithInstance {
		private Structure structure;
		private EdgeProxyStorage storage;

		@BeforeEach
		void setUp() {
			structure = mockStructure();
			storage = new EdgeProxyStorage(structure);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.EdgeProxyStorage#getStructure()}.
		 */
		@Test
		void testGetStructure() {
			assertSame(structure, storage.getStructure());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.EdgeProxyStorage#recycle()}.
		 */
		@Test
		void testRecycle() {
			storage.recycle();
			assertNull(storage.getStructure());
			assertFalse(storage.revive());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.EdgeProxyStorage#revive()}.
		 */
		@Test
		void testRevive() {
			assertFalse(storage.revive());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.EdgeProxyStorage#getItemCount(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testGetItemCount() {
			storage.getItemCount(null);
			verify(structure).getEdgeCount();
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.EdgeProxyStorage#getItemAt(de.ims.icarus2.model.api.members.container.Container, long)}.
		 */
		@Test
		@RandomizedTest
		void testGetItemAt(RandomGenerator rng) {
			long index = rng.randomId();
			storage.getItemAt(null, index);
			verify(structure).getEdgeAt(index);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.EdgeProxyStorage#indexOfItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@Test
		void testIndexOfItem() {
			Edge edge = mockEdge();
			storage.indexOfItem(null, edge);
			verify(structure).indexOfEdge(edge);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.EdgeProxyStorage#addItem(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@Test
		@RandomizedTest
		void testAddItem(RandomGenerator rng) {
			assertModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
					() -> storage.addItem(null, rng.randomId(), mockEdge()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.EdgeProxyStorage#addItems(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.util.collections.seq.DataSequence)}.
		 */
		@Test
		@RandomizedTest
		void testAddItems(RandomGenerator rng) {
			assertModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
					() -> storage.addItems(null, rng.randomId(), mockSequence()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.EdgeProxyStorage#removeItem(de.ims.icarus2.model.api.members.container.Container, long)}.
		 */
		@Test
		@RandomizedTest
		void testRemoveItem(RandomGenerator rng) {
			assertModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
					() -> storage.removeItem(null, rng.randomId()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.EdgeProxyStorage#removeItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
		 */
		@Test
		@RandomizedTest
		void testRemoveItems(RandomGenerator rng) {
			assertModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
					() -> storage.removeItems(null, rng.randomId(), rng.randomId()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.EdgeProxyStorage#swapItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
		 */
		@Test
		@RandomizedTest
		void testSwapItems(RandomGenerator rng) {
			assertModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
					() -> storage.swapItems(null, rng.randomId(), rng.randomId()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.EdgeProxyStorage#getBeginOffset(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testGetBeginOffset() {
			assertEquals(UNSET_LONG, storage.getBeginOffset(null));
			verify(structure, never()).getBeginOffset();
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.EdgeProxyStorage#getEndOffset(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testGetEndOffset() {
			assertEquals(UNSET_LONG, storage.getEndOffset(null));
			verify(structure, never()).getEndOffset();
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.EdgeProxyStorage#createEditVerifier(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testCreateEditVerifier() {
			assertFalse(storage.createEditVerifier(mockContainer()).isAllowEdits());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.EdgeProxyStorage#isDirty(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testIsDirty() {
			storage.isDirty(null);
			verify(structure).isDirty();
		}
	}

}
