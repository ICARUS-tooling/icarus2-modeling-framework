/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.NO_NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.assertGetter;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.test.TestUtils.assertSetter;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.members.item.Edge;

/**
 * @author Markus Gärtner
 *
 */
class ChainNodeInfoTest {

	ChainNodeInfo instance;

	@BeforeEach
	void setUp() {
		instance = new ChainNodeInfo();
	}

	@AfterEach
	void tearDown() {
		instance = null;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.ChainNodeInfo#getType()}.
	 */
	@Test
	void testGetType() {
		assertEquals(NodeInfo.Type.CHAIN, instance.getType());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.ChainNodeInfo#setIn(de.ims.icarus2.model.api.members.item.Edge)}.
	 */
	@Test
	void testSetIn() {
		assertSetter(instance,
				ChainNodeInfo::setIn,
				mockEdge(),
				NO_NPE_CHECK, NO_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.ChainNodeInfo#setOut(de.ims.icarus2.model.api.members.item.Edge)}.
	 */
	@Test
	void testSetOut() {
		assertSetter(instance,
				ChainNodeInfo::setOut,
				mockEdge(),
				NO_NPE_CHECK, NO_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.ChainNodeInfo#getIn()}.
	 */
	@Test
	void testGetIn() {
		assertGetter(instance,
				mockEdge(), mockEdge(),
				NO_DEFAULT(),
				ChainNodeInfo::getIn,
				ChainNodeInfo::setIn);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.ChainNodeInfo#getOut()}.
	 */
	@Test
	void testGetOut() {
		assertGetter(instance,
				mockEdge(), mockEdge(),
				NO_DEFAULT(),
				ChainNodeInfo::getOut,
				ChainNodeInfo::setOut);
	}

	@Nested
	class WithTerminals {

		Edge incoming, outgoing;

		@BeforeEach
		void setUp() {
			incoming = mockEdge();
			outgoing = mockEdge();
		}

		@AfterEach
		void tearDown() {
			incoming = outgoing = null;
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.ChainNodeInfo#edgeCount()}.
		 */
		@Test
		void testEdgeCount() {
			assertEquals(0, instance.edgeCount());

			instance.setIn(incoming);
			assertEquals(1, instance.edgeCount());

			instance.setOut(outgoing);
			assertEquals(2, instance.edgeCount());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.ChainNodeInfo#edgeCount(boolean)}.
		 */
		@Test
		void testEdgeCountBoolean() {
			assertEquals(0, instance.edgeCount(true));
			assertEquals(0, instance.edgeCount(false));

			instance.setIn(incoming);
			assertEquals(1, instance.edgeCount(true));
			assertEquals(0, instance.edgeCount(false));

			instance.setOut(outgoing);
			assertEquals(1, instance.edgeCount(true));
			assertEquals(1, instance.edgeCount(false));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.ChainNodeInfo#edgeAt(long, boolean)}.
		 */
		@Test
		void testEdgeAtOutOfBounds() {
			assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					() -> instance.edgeAt(0, true));
			assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					() -> instance.edgeAt(0, false));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.ChainNodeInfo#addEdge(de.ims.icarus2.model.api.members.item.Edge, boolean)}.
		 */
		@Test
		void testAddEdge() {
			assertNPE(() -> instance.addEdge(null, true));
			assertNPE(() -> instance.addEdge(null, false));

			instance.addEdge(incoming, true);

			assertModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
					() -> instance.addEdge(mockEdge(), true));

			instance.addEdge(outgoing, false);

			assertModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
					() -> instance.addEdge(mockEdge(), false));

			assertSame(incoming, instance.getIn());
			assertSame(outgoing, instance.getOut());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.ChainNodeInfo#removeEdge(de.ims.icarus2.model.api.members.item.Edge, boolean)}.
		 */
		@Test
		void testRemoveEdge() {
			assertNPE(() -> instance.removeEdge(null, true));
			assertNPE(() -> instance.removeEdge(null, false));

			assertModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					() -> instance.removeEdge(incoming, true));
			assertModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					() -> instance.removeEdge(outgoing, false));

			instance.addEdge(incoming, true);
			instance.addEdge(outgoing, false);

			assertModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					() -> instance.removeEdge(mockEdge(), true));
			assertModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					() -> instance.removeEdge(mockEdge(), false));

			instance.removeEdge(incoming, true);
			assertNull(instance.getIn());

			instance.removeEdge(outgoing, false);
			assertNull(instance.getOut());
		}

		@Nested
		class WithPresetTerminals {

			@BeforeEach
			void setUp() {
				instance.setIn(incoming);
				instance.setOut(outgoing);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.ChainNodeInfo#edgeCount()}.
			 */
			@Test
			void testEdgeCount() {
				assertEquals(2, instance.edgeCount());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.ChainNodeInfo#edgeCount(boolean)}.
			 */
			@Test
			void testEdgeCountBoolean() {
				assertEquals(1, instance.edgeCount(true));
				assertEquals(1, instance.edgeCount(false));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.ChainNodeInfo#edgeAt(long, boolean)}.
			 */
			@Test
			void testEdgeAt() {
				assertSame(incoming, instance.edgeAt(0, true));
				assertSame(outgoing, instance.edgeAt(0, false));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.ChainNodeInfo#edgeAt(long, boolean)}.
			 */
			@ParameterizedTest
			@ValueSource(longs = {UNSET_LONG, 1, 2})
			void testEdgeAtOutOfBounds(long index) {
				assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
						() -> instance.edgeAt(index, true));
				assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
						() -> instance.edgeAt(index, false));
			}
		}
	}

}
