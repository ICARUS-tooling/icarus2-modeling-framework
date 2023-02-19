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

import static de.ims.icarus2.model.api.ModelTestUtils.mockStructure;
import static de.ims.icarus2.model.api.ModelTestUtils.stubDefaultLazyEdges;
import static de.ims.icarus2.test.TestUtils.longRange;
import static de.ims.icarus2.test.util.Pair.intChain;
import static de.ims.icarus2.test.util.Pair.pair;
import static de.ims.icarus2.test.util.Triple.triple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.api.ModelTestUtils;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.standard.members.container.ContainerEditVerifierTestBuilder;
import de.ims.icarus2.model.standard.members.container.UnrestrictedContainerEditVerifierTest;

/**
 * @author Markus Gärtner
 *
 */
@SuppressWarnings("resource")
public class UnrestrictedStructureEditVerifierTest {

	@TestFactory
	Stream<DynamicTest> testContainerVerifierNullArguments() {
		Structure structure = mockStructure(0, 0);
		UnrestrictedStructureEditVerifier verifier = new UnrestrictedStructureEditVerifier(structure);

		return ContainerEditVerifierTestBuilder.createNullArgumentsTests(verifier);
	}

	@TestFactory
	Stream<DynamicTest> testStructureVerifierNullArguments() {
		Structure structure = mockStructure(0, 0);
		UnrestrictedStructureEditVerifier verifier = new UnrestrictedStructureEditVerifier(structure);

		return new StructureEditVerifierTestBuilder(verifier)
				.failForNullMembers()
				.createTests();
	}

	@TestFactory
	Stream<DynamicTest> testStructureVerifierIllegalMemberArguments() {
		@SuppressWarnings("unchecked")
		Structure structure = mockStructure(4, pair(0, 1));
		UnrestrictedStructureEditVerifier verifier = new UnrestrictedStructureEditVerifier(structure);

		return new StructureEditVerifierTestBuilder(verifier)
				.failForIllegalMembers()
				.createTests();
	}

	@Test
	void testLifecycle() {
		Structure structure = mockStructure(0, 0);
		UnrestrictedStructureEditVerifier verifier = new UnrestrictedStructureEditVerifier(structure);

		assertEquals(structure, verifier.getSource());

		verifier.close();

		assertNull(verifier.getSource());
	}


	@TestFactory
	Stream<DynamicTest> testEmptyContainer() {
		return UnrestrictedContainerEditVerifierTest.configureBuilderEmpty(new ContainerEditVerifierTestBuilder(
					new UnrestrictedStructureEditVerifier(mockStructure(0, 0))))
				.createTests();
	}

	@TestFactory
	Stream<DynamicTest> testSmallContainerSize10() {
		return UnrestrictedContainerEditVerifierTest.configureBuilder(new ContainerEditVerifierTestBuilder(
					new UnrestrictedStructureEditVerifier(mockStructure(10, 0))))
				.createTests();
	}

	@TestFactory
	Stream<DynamicTest> testLargeContainerLongMax() {
		return UnrestrictedContainerEditVerifierTest.configureBuilder(new ContainerEditVerifierTestBuilder(
				new UnrestrictedStructureEditVerifier(mockStructure(Long.MAX_VALUE-1, 0))))
			.createTests();
	}

	@TestFactory
	Stream<DynamicTest> testEmptyStructure() {
		return configureBuilderEmpty(new StructureEditVerifierTestBuilder(
					new UnrestrictedStructureEditVerifier(mockStructure(0, 0))))
				.createTests();
	}

	/**
	 * Configures the given builder under the assumption that the
	 * underlying structure is empty, i.e. it contains exactly
	 * {@code 0} items and edges.
	 *
	 * @param builder
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static StructureEditVerifierTestBuilder configureBuilderEmpty(
			StructureEditVerifierTestBuilder builder) {
		assertTrue(builder.getVerifier().getSource().getEdgeCount()==0L);

		return builder
				.removeSingleIllegal(longRange(-1, 1))
				.removeBatchIllegal(pair(0, 0), pair(0, 1))
				.swapSingleIllegal(pair(0, 0), pair(0, 1));
	}

	private static final int ROOT = ModelTestUtils.ROOT;

	@TestFactory
	Stream<DynamicTest> testSmallStructure10() {
		return configureBuilder(new StructureEditVerifierTestBuilder(
					new UnrestrictedStructureEditVerifier(mockStructure(10, intChain(ROOT, 9)))))
				.createTests();
	}

	@TestFactory
	Stream<DynamicTest> testLargeStructureLongMax() {
		Structure structure = mockStructure(Long.MAX_VALUE-1, Long.MAX_VALUE-1);
		stubDefaultLazyEdges(structure);

		return configureBuilder(new StructureEditVerifierTestBuilder(
					new UnrestrictedStructureEditVerifier(
							structure)))
				.createTests();
	}

	/**
	 * Configures the given builder under the assumption that the
	 * underlying structure has at least 5 edges and items.
	 *
	 * @param builder
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "boxing" })
	public static StructureEditVerifierTestBuilder configureBuilder(
			StructureEditVerifierTestBuilder builder) {

		long items = builder.getVerifier().getSource().getItemCount();
		assertTrue(items>4L, "must have 5 or more elements: "+items);
		assertTrue(items<Long.MAX_VALUE, "size must be less than Long.MAX_VALUE");

		long edges = builder.getVerifier().getSource().getEdgeCount();
		assertTrue(edges>4L, "must have 5 or more edges: "+edges);
		assertTrue(edges<Long.MAX_VALUE, "edge count must be less than Long.MAX_VALUE");

		long midI = items>>>1;
		long midE = edges>>>1;

		//TODO rework test to be repeated and use random values for inner indices
//		Supplier<Long> innerE = randomizer(0L, 1L, midE, edges-1);
//		Supplier<Long> innerI = randomizer(0L, 1L, midI, items-1);


		return builder
				.addSingleLegal(pair(0L, 1L), pair(1, midI), pair(midI, midI), pair(midI, items-1))
				.addSingleIllegal(pair(0L, ROOT), pair(midI, ROOT), pair(items-1, ROOT))

				.addBatchLegalIndirect(pair(ROOT, 0L), pair(0L, 1L), pair(1, midI), pair(midI, items-1))
				.addBatchLegalIndirect(pair(0L, 1L), pair(midI, items-1))
				.addBatchLegalIndirect(pair(ROOT, 0L), pair(0L, 1), pair(1, midI), pair(midI, items-1))
				.addBatchLegalIndirect(pair(ROOT, midI), pair(ROOT, 1L), pair(1, midI), pair(ROOT, items-1))

				.addBatchIllegalIndirect(pair(0, ROOT))
				.addBatchIllegalIndirect(pair(0L, ROOT), pair(midI, ROOT))
				.addBatchIllegalIndirect(pair(0L, ROOT), pair(midI, ROOT), pair(items-1, ROOT))

				.removeSingleLegal(0, 1, midE, edges-1)
				.removeSingleIllegal(-1, edges, edges+1)
				.removeBatchLegal(pair(0L, 0L), pair(0L, 1L), pair(0, midE),
						pair(1, edges-1), pair(midE, edges-1))
				.removeBatchIllegal(pair(-1L, 0L), pair(-1, edges), pair(edges, edges), pair(midE, edges+1))
				.swapSingleLegal(pair(0L, 0L), pair(1L, 1L), pair(midE, midE),
						pair(edges-1, edges-1), pair(midE, 0), pair(edges-1, 1))
				.swapSingleIllegal(pair(-1L, 0L), pair(0, edges), pair(edges, midE))

				// For each of the picked edges try to set both its terminals to 2 "random" other spots
				.setTerminalLegalIndirect(
						triple(0, 0, true), triple(0, midI, true), triple(0, edges-1, false), triple(0, 1, false),
						triple(1, 0, true), triple(1, midI, true), triple(1, edges-1, false), triple(1, midI, false),
						triple(midE, 0, true), triple(midE, midI, true), triple(midE, edges-1, false), triple(midE, 1, false),
						triple(edges-1, 0, true), triple(edges-1, midI, true), triple(edges-1, edges-1, false), triple(edges-1, 1, false))

				;
				// old chunk
//				.addSingleIllegal(intPair(1, 2), intPair(2, ROOT))
//				.addBatchIllegalIndirect(intPair(1, 2))
//					.addBatchIllegalIndirect(intPair(1, 2), intPair(2, ROOT))
//				.removeSingleIllegal(longRange(-1, 1))
//				.removeBatchIllegal(intPair(0, 0), intPair(0, 1))
//				.swapSingleIllegal(intPair(0,  0), intPair(0, 1))
//				.setTerminalIllegalIndirect(triple(0, 2, true), triple(0, ROOT, false))
//				.createEdgeIllegalIndirect(intPair(ROOT, 1), intPair(1, 2));
	}
}
