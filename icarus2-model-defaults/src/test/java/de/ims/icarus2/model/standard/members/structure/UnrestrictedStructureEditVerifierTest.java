/**
 *
 */
package de.ims.icarus2.model.standard.members.structure;

import static de.ims.icarus2.model.api.ModelTestUtils.mockStructure;
import static de.ims.icarus2.model.api.ModelTestUtils.stubDefaultLazyEdges;
import static de.ims.icarus2.test.TestUtils.longRange;
import static de.ims.icarus2.test.util.Pair.intChain;
import static de.ims.icarus2.test.util.Pair.intPair;
import static de.ims.icarus2.test.util.Pair.longPair;
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
		Structure structure = mockStructure(4, intPair(0, 1));
		UnrestrictedStructureEditVerifier verifier = new UnrestrictedStructureEditVerifier(structure);

		return new StructureEditVerifierTestBuilder(verifier)
				.failForIllegalMembers()
				.createTests();
	}

	@Test
	void testLifecycle() {
		Structure structure = mockStructure(0, 0);
		@SuppressWarnings("resource")
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
				.removeBatchIllegal(intPair(0, 0), intPair(0, 1))
				.swapSingleIllegal(intPair(0, 0), intPair(0, 1));
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
				.addSingleLegal(longPair(0, 1), longPair(1, midI), longPair(midI, midI), longPair(midI, items-1))
				.addSingleIllegal(longPair(0, ROOT), longPair(midI, ROOT), longPair(items-1, ROOT))

				.addBatchLegalIndirect(longPair(ROOT, 0), longPair(0, 1), longPair(1, midI), longPair(midI, items-1))
				.addBatchLegalIndirect(longPair(0, 1), longPair(midI, items-1))
				.addBatchLegalIndirect(longPair(ROOT, 0), longPair(0, 1), longPair(1, midI), longPair(midI, items-1))
				.addBatchLegalIndirect(longPair(ROOT, midI), longPair(ROOT, 1), longPair(1, midI), longPair(ROOT, items-1))

				.addBatchIllegalIndirect(longPair(0, ROOT))
				.addBatchIllegalIndirect(longPair(0, ROOT), longPair(midI, ROOT))
				.addBatchIllegalIndirect(longPair(0, ROOT), longPair(midI, ROOT), longPair(items-1, ROOT))

				.removeSingleLegal(0, 1, midE, edges-1)
				.removeSingleIllegal(-1, edges, edges+1)
				.removeBatchLegal(longPair(0, 0), longPair(0, 1), longPair(0, midE),
						longPair(1, edges-1), longPair(midE, edges-1))
				.removeBatchIllegal(longPair(-1, 0), longPair(-1, edges), longPair(edges, edges), longPair(midE, edges+1))
				.swapSingleLegal(longPair(0, 0), longPair(1, 1), longPair(midE, midE),
						longPair(edges-1, edges-1), longPair(midE, 0), longPair(edges-1, 1))
				.swapSingleIllegal(longPair(-1, 0), longPair(0, edges), longPair(edges, midE))

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
