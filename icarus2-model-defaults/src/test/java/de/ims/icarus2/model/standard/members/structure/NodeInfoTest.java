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

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.mockEdge;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.test.ApiGuardedTest;

/**
 * @author Markus Gärtner
 *
 */
public interface NodeInfoTest<I extends NodeInfo> extends ApiGuardedTest<I> {

	/**
	 * Returns the structure type that adequately describes the
	 * constraints for incoming edges on the node info under test.
	 */
	StructureType getIncomingEquivalent();

	/**
	 * Returns the structure type that adequately describes the
	 * constraints for outgoing edges on the node info under test.
	 */
	StructureType getOutgoingEquivalent();

	NodeInfo.Type getExpectedType();

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.NodeInfo#edgeCount()}.
	 */
	@TestFactory
	default List<DynamicTest> testEdgeCount() {
		List<DynamicTest> tests = new ArrayList<>();

		tests.add(dynamicTest("empty", () -> assertEquals(0, create().edgeCount())));

		StructureType in = getIncomingEquivalent();
		StructureType out = getOutgoingEquivalent();

		int limitIn = in.getIncomingEdgeLimit();
		int limitOut = out.getOutgoingEdgeLimit();

		// Only test by adding incoming edges
		if(limitIn==UNSET_INT) {
			tests.add(dynamicTest("in: unlimited - random", () -> {
				I info = create();
				int count = NodeTests.fillRandom(info, true);

				assertEquals(count, info.edgeCount());
			}));
		} else if(limitIn>0) {
			tests.add(dynamicTest("in: fixed limit - "+limitIn, () -> {
				I info = create();
				NodeTests.fill(info, limitIn, true);
				assertEquals(limitIn, info.edgeCount());
			}));
		}

		// Only test by adding outgoing edges
		if(limitOut==UNSET_INT) {
			tests.add(dynamicTest("out: unlimited - random", () -> {
				I info = create();
				int count = NodeTests.fillRandom(info, false);

				assertEquals(count, info.edgeCount());
			}));
		} else if(limitOut>0) {
			tests.add(dynamicTest("out: fixed limit - "+limitOut, () -> {
				I info = create();
				NodeTests.fill(info, limitOut, false);
				assertEquals(limitOut, info.edgeCount());
			}));
		}

		// Test by adding combinations of edges
		if(limitIn==UNSET_INT && limitOut==UNSET_INT) {
			tests.add(dynamicTest("both: unlimited - random", () -> {
				I info = create();
				int countIn = NodeTests.fillRandom(info, true);
				int countOut = NodeTests.fillRandom(info, false);

				assertEquals(countIn+countOut, info.edgeCount());
			}));
		} else if(limitIn>0 && limitOut>0) {
			tests.add(dynamicTest("both: fixed limits", () -> {
				I info = create();
				NodeTests.fill(info, limitIn, true);
				NodeTests.fill(info, limitOut, false);
				assertEquals(limitIn+limitOut, info.edgeCount());
			}));
		} else if(limitIn>0 & limitOut==UNSET_INT) {
			tests.add(dynamicTest("both: in-limited out-unbound", () -> {
				I info = create();
				NodeTests.fill(info, limitIn, true);
				int countOut = NodeTests.fillRandom(info, false);
				assertEquals(limitIn+countOut, info.edgeCount());
			}));
		} else if(limitIn==UNSET_INT & limitOut>0) {
			tests.add(dynamicTest("both: in-limited out-unbound", () -> {
				I info = create();
				int countIn = NodeTests.fillRandom(info, true);
				NodeTests.fill(info, limitOut, false);
				assertEquals(countIn+limitOut, info.edgeCount());
			}));
		}

		return tests;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.NodeInfo#edgeCount(boolean)}.
	 */
	@TestFactory
	default List<DynamicTest> testEdgeCountBoolean() {
		List<DynamicTest> tests = new ArrayList<>();

		tests.add(dynamicTest("empty [in]", () -> assertEquals(0, create().edgeCount(true))));
		tests.add(dynamicTest("empty [out]", () -> assertEquals(0, create().edgeCount(false))));

		StructureType in = getIncomingEquivalent();
		StructureType out = getOutgoingEquivalent();

		int limitIn = in.getIncomingEdgeLimit();
		int limitOut = out.getOutgoingEdgeLimit();

		// Only test by adding incoming edges
		if(limitIn==UNSET_INT) {
			tests.add(dynamicTest("in: unlimited - random", () -> {
				I info = create();
				int count = NodeTests.fillRandom(info, true);

				assertEquals(count, info.edgeCount(true));
				assertEquals(0, info.edgeCount(false));
			}));
		} else if(limitIn>0) {
			tests.add(dynamicTest("in: fixed limit - "+limitIn, () -> {
				I info = create();
				NodeTests.fill(info, limitIn, true);
				assertEquals(limitIn, info.edgeCount(true));
				assertEquals(0, info.edgeCount(false));
			}));
		}

		// Only test by adding outgoing edges
		if(limitOut==UNSET_INT) {
			tests.add(dynamicTest("out: unlimited - random", () -> {
				I info = create();
				int count = NodeTests.fillRandom(info, false);

				assertEquals(count, info.edgeCount(false));
				assertEquals(0, info.edgeCount(true));
			}));
		} else if(limitOut>0) {
			tests.add(dynamicTest("out: fixed limit - "+limitOut, () -> {
				I info = create();
				NodeTests.fill(info, limitOut, false);
				assertEquals(limitOut, info.edgeCount(false));
				assertEquals(0, info.edgeCount(true));
			}));
		}

		// Test by adding combinations of edges
		if(limitIn==UNSET_INT && limitOut==UNSET_INT) {
			tests.add(dynamicTest("both: unlimited - random", () -> {
				I info = create();
				int countIn = NodeTests.fillRandom(info, true);
				int countOut = NodeTests.fillRandom(info, false);

				assertEquals(countIn, info.edgeCount(true));
				assertEquals(countOut, info.edgeCount(false));
			}));
		} else if(limitIn>0 && limitOut>0) {
			tests.add(dynamicTest("both: fixed limits", () -> {
				I info = create();
				NodeTests.fill(info, limitIn, true);
				NodeTests.fill(info, limitOut, false);
				assertEquals(limitIn, info.edgeCount(true));
				assertEquals(limitOut, info.edgeCount(false));
			}));
		} else if(limitIn>0 & limitOut==UNSET_INT) {
			tests.add(dynamicTest("both: in-limited out-unbound", () -> {
				I info = create();
				NodeTests.fill(info, limitIn, true);
				int countOut = NodeTests.fillRandom(info, false);
				assertEquals(limitIn, info.edgeCount(true));
				assertEquals(countOut, info.edgeCount(false));
			}));
		} else if(limitIn==UNSET_INT & limitOut>0) {
			tests.add(dynamicTest("both: in-limited out-unbound", () -> {
				I info = create();
				int countIn = NodeTests.fillRandom(info, true);
				NodeTests.fill(info, limitOut, false);
				assertEquals(countIn, info.edgeCount(true));
				assertEquals(limitOut, info.edgeCount(false));
			}));
		}

		return tests;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.NodeInfo#edgeAt(long, boolean)}.
	 */
	@TestFactory
	default List<DynamicTest> testEdgeAt() {
		List<DynamicTest> tests = new ArrayList<>();

		StructureType in = getIncomingEquivalent();
		StructureType out = getOutgoingEquivalent();

		int limitIn = in.getIncomingEdgeLimit();
		int limitOut = out.getOutgoingEdgeLimit();

		// Only test by adding incoming edges
		if(limitIn==UNSET_INT) {
			tests.add(dynamicTest("in: unlimited - random", () -> {
				NodeTests.fillAndAssert(create(), -1, true);
			}));
		} else if(limitIn>0) {
			tests.add(dynamicTest("in: fixed limit - "+limitIn, () -> {
				NodeTests.fillAndAssert(create(), limitIn, true);
			}));
		}

		// Only test by adding outgoing edges
		if(limitOut==UNSET_INT) {
			tests.add(dynamicTest("out: unlimited - random", () -> {
				NodeTests.fillAndAssert(create(), -1, false);
			}));
		} else if(limitOut>0) {
			tests.add(dynamicTest("out: fixed limit - "+limitOut, () -> {
				NodeTests.fillAndAssert(create(), limitOut, false);
			}));
		}

		// Test by adding combinations of edges
		if(limitIn==UNSET_INT && limitOut==UNSET_INT) {
			tests.add(dynamicTest("both: unlimited - random", () -> {
				NodeTests.fillAndAssert(create(), -1, -1);
			}));
		} else if(limitIn>0 && limitOut>0) {
			tests.add(dynamicTest("both: fixed limits", () -> {
				NodeTests.fillAndAssert(create(), limitIn, limitOut);
			}));
		} else if(limitIn>0 & limitOut==UNSET_INT) {
			tests.add(dynamicTest("both: in-limited out-unbound", () -> {
				NodeTests.fillAndAssert(create(), limitIn, -1);
			}));
		} else if(limitIn==UNSET_INT & limitOut>0) {
			tests.add(dynamicTest("both: in-limited out-unbound", () -> {
				NodeTests.fillAndAssert(create(), -1, limitOut);
			}));
		}

		return tests;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.NodeInfo#addEdge(de.ims.icarus2.model.api.members.item.Edge, boolean)}.
	 */
	@TestFactory
	default List<DynamicTest> testAddEdge() {
		List<DynamicTest> tests = new ArrayList<>();

		StructureType in = getIncomingEquivalent();
		StructureType out = getOutgoingEquivalent();

		int limitIn = in.getIncomingEdgeLimit();
		int limitOut = out.getOutgoingEdgeLimit();

		// Only test by adding incoming edges
		if(limitIn==UNSET_INT) {
			tests.add(dynamicTest("in: unlimited - random", () -> {
				NodeTests.fillRandom(create(), true);
			}));
		} else if(limitIn>0) {
			tests.add(dynamicTest("in: fixed limit - "+limitIn, () -> {
				NodeTests.fill(create(), limitIn, true);
			}));
			tests.add(dynamicTest("in: fixed limit [ME] - "+limitIn, () -> {
				I info = create();
				NodeTests.fill(info, limitIn, true);

				assertModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
						() -> info.addEdge(mockEdge(), true));
			}));
		} else {
			tests.add(dynamicTest("in: none [ME]", () -> {
				assertModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
						() -> create().addEdge(mockEdge(), true));
			}));
		}

		// Only test by adding outgoing edges
		if(limitOut==UNSET_INT) {
			tests.add(dynamicTest("out: unlimited - random", () -> {
				NodeTests.fillRandom(create(), false);
			}));
		} else if(limitOut>0) {
			tests.add(dynamicTest("out: fixed limit - "+limitOut, () -> {
				NodeTests.fill(create(), limitOut, false);
			}));
			tests.add(dynamicTest("out: fixed limit [ME] - "+limitOut, () -> {
				I info = create();
				NodeTests.fill(info, limitOut, false);

				assertModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
						() -> info.addEdge(mockEdge(), false));
			}));
		} else {
			tests.add(dynamicTest("out: none [ME]", () -> {
				I info = create();
				assertModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
						() -> info.addEdge(mockEdge(), false));
			}));
		}

		// Test by adding combinations of edges
		if(limitIn==UNSET_INT && limitOut==UNSET_INT) {
			tests.add(dynamicTest("both: unlimited - random", () -> {
				I info = create();
				NodeTests.fillRandom(info, true);
				NodeTests.fillRandom(info, false);
			}));
		} else if(limitIn>0 && limitOut>0) {
			tests.add(dynamicTest("both: fixed limits", () -> {
				I info = create();
				NodeTests.fill(info, limitIn, true);
				NodeTests.fill(info, limitOut, false);
				assertModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
						() -> info.addEdge(mockEdge(), true));
				assertModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
						() -> info.addEdge(mockEdge(), false));
			}));
		} else if(limitIn>0 & limitOut==UNSET_INT) {
			tests.add(dynamicTest("both: in-limited out-unbound", () -> {
				I info = create();
				NodeTests.fill(info, limitIn, true);
				NodeTests.fillRandom(info, false);
				assertModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
						() -> info.addEdge(mockEdge(), true));
			}));
		} else if(limitIn==UNSET_INT & limitOut>0) {
			tests.add(dynamicTest("both: in-limited out-unbound", () -> {
				I info = create();
				NodeTests.fillRandom(info, true);
				NodeTests.fill(info, limitOut, false);
				assertModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
						() -> info.addEdge(mockEdge(), false));
			}));
		}

		return tests;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.NodeInfo#removeEdge(de.ims.icarus2.model.api.members.item.Edge, boolean)}.
	 */
	@TestFactory
	default List<DynamicTest> testRemoveEdge() {
		List<DynamicTest> tests = new ArrayList<>();

		StructureType in = getIncomingEquivalent();
		StructureType out = getOutgoingEquivalent();

		int limitIn = in.getIncomingEdgeLimit();
		int limitOut = out.getOutgoingEdgeLimit();

		// Only test by adding incoming edges
		if(limitIn==UNSET_INT) {
			tests.add(dynamicTest("in: unlimited - random", () -> {
				NodeTests.fillRemoveAndAssert(create(), -1, true);
			}));
		} else if(limitIn>0) {
			tests.add(dynamicTest("in: fixed limit - "+limitIn, () -> {
				NodeTests.fillRemoveAndAssert(create(), limitIn, true);
			}));
		}

		if(limitIn!=0) {
			tests.add(dynamicTest("in: empty [ME]", () -> {
				assertModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
						() -> create().removeEdge(mockEdge(), true));
			}));
		}

		// Only test by adding outgoing edges
		if(limitOut==UNSET_INT) {
			tests.add(dynamicTest("out: unlimited - random", () -> {
				NodeTests.fillRemoveAndAssert(create(), -1, false);
			}));
		} else if(limitOut>0) {
			tests.add(dynamicTest("out: fixed limit - "+limitOut, () -> {
				NodeTests.fillRemoveAndAssert(create(), limitIn, false);
			}));
		}

		if(limitOut!=0) {
			tests.add(dynamicTest("out: empty [ME]", () -> {
				assertModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
						() -> create().removeEdge(mockEdge(), false));
			}));
		}

		// Test by adding combinations of edges
		if(limitIn==UNSET_INT && limitOut==UNSET_INT) {
			tests.add(dynamicTest("both: unlimited - random", () -> {
				NodeTests.fillRemoveAndAssert(create(), -1, -1);
			}));
		} else if(limitIn>0 && limitOut>0) {
			tests.add(dynamicTest("both: fixed limits", () -> {
				NodeTests.fillRemoveAndAssert(create(), limitIn, limitOut);
			}));
		} else if(limitIn>0 & limitOut==UNSET_INT) {
			tests.add(dynamicTest("both: in-limited out-unbound", () -> {
				NodeTests.fillRemoveAndAssert(create(), limitIn, -1);
			}));
		} else if(limitIn==UNSET_INT & limitOut>0) {
			tests.add(dynamicTest("both: in-limited out-unbound", () -> {
				NodeTests.fillRemoveAndAssert(create(), -1, limitOut);
			}));
		}

		return tests;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.NodeInfo#getType()}.
	 */
	@Test
	default void testGetType() {
		assertEquals(getExpectedType(), create().getType());
	}

}
