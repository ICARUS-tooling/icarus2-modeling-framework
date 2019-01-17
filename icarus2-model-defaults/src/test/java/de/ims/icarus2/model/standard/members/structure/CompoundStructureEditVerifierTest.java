/**
 *
 */
package de.ims.icarus2.model.standard.members.structure;

import static de.ims.icarus2.model.api.ModelTestUtils.mockStructure;
import static de.ims.icarus2.model.api.ModelTestUtils.stubEdgeCount;
import static de.ims.icarus2.model.api.ModelTestUtils.stubEdges;
import static de.ims.icarus2.model.api.ModelTestUtils.stubItemCount;
import static de.ims.icarus2.model.api.ModelTestUtils.stubItems;
import static de.ims.icarus2.test.util.Pair.intPair;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.standard.members.container.ContainerEditVerifierTestBuilder;
import de.ims.icarus2.model.standard.members.container.ImmutableContainerEditVerifier;
import de.ims.icarus2.model.standard.members.container.ImmutableContainerEditVerifierTest;
import de.ims.icarus2.model.standard.members.container.UnrestrictedContainerEditVerifier;

/**
 * @author Markus Gärtner
 *
 */
class CompoundStructureEditVerifierTest {

	private Structure structure;

	private CompoundStructureEditVerifier verifier;


	@BeforeEach
	void setUp() {
		structure = mockStructure();
	}

	@AfterEach
	void tearDown() {
		structure = null;
		verifier = null;
	}

	@TestFactory
	Stream<DynamicTest> testContainerVerifierNullArguments() {
		verifier = new CompoundStructureEditVerifier(
				new UnrestrictedContainerEditVerifier(structure));

		return ContainerEditVerifierTestBuilder.createNullArgumentsTests(verifier);
	}

	@TestFactory
	Stream<DynamicTest> testStructureVerifierNullArguments() {
		verifier = new CompoundStructureEditVerifier(
				new UnrestrictedContainerEditVerifier(structure));

		return new StructureEditVerifierTestBuilder(verifier)
				.failForNullMembers()
				.createTests();
	}

	@SuppressWarnings("unchecked")
	@TestFactory
	Stream<DynamicTest> testStructureVerifierIllegalMemberArguments() {
		stubItemCount(structure, 4);
		stubItems(structure);
		stubEdgeCount(structure, 1);
		stubEdges(structure, intPair(0, 1));

		verifier = new CompoundStructureEditVerifier(
				new UnrestrictedContainerEditVerifier(structure));

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

	@Nested
	class GivenImmutableContainerEditVerifier {

		private ImmutableContainerEditVerifier oracle;

		@BeforeEach
		void setUp() {
			oracle = new ImmutableContainerEditVerifier(structure);
			verifier = new CompoundStructureEditVerifier(oracle);
		}

		@AfterEach
		void tearDown() {
			oracle = null;
		}

		@TestFactory
		Stream<DynamicTest> testEmptyContainer() {
			return ImmutableContainerEditVerifierTest.configureBuilderEmpty(
					new ContainerEditVerifierTestBuilder(verifier))
					.createTestsWithOracle(oracle);
		}

		@TestFactory
		Stream<DynamicTest> testSmallContainerSize10() {
			stubItemCount(structure, 10);
			stubItems(structure);

			return ImmutableContainerEditVerifierTest.configureBuilder(
					new ContainerEditVerifierTestBuilder(verifier))
					.createTestsWithOracle(oracle);
		}

		@TestFactory
		Stream<DynamicTest> testLargeContainer() {
			stubItemCount(structure, Long.MAX_VALUE-1);
			stubItems(structure);

			return ImmutableContainerEditVerifierTest.configureBuilder(
					new ContainerEditVerifierTestBuilder(verifier))
					.createTestsWithOracle(oracle);
		}
	}

	@Nested
	class GivenUnrestrictedContainerEditVerifier {

		private UnrestrictedContainerEditVerifier oracle;

		@BeforeEach
		void setUp() {
			oracle = new UnrestrictedContainerEditVerifier(structure);
			verifier = new CompoundStructureEditVerifier(oracle);
		}

		@AfterEach
		void tearDown() {
			oracle = null;
		}

		@TestFactory
		Stream<DynamicTest> testEmptyContainer() {
			return ImmutableContainerEditVerifierTest.configureBuilderEmpty(
					new ContainerEditVerifierTestBuilder(verifier))
					.createTestsWithOracle(oracle);
		}

		@TestFactory
		Stream<DynamicTest> testSmallContainerSize10() {
			stubItemCount(structure, 10);
			stubItems(structure);

			return ImmutableContainerEditVerifierTest.configureBuilder(
					new ContainerEditVerifierTestBuilder(verifier))
					.createTestsWithOracle(oracle);
		}

		@TestFactory
		Stream<DynamicTest> testLargeContainer() {
			stubItemCount(structure, Long.MAX_VALUE-1);
			stubItems(structure);

			return ImmutableContainerEditVerifierTest.configureBuilder(
					new ContainerEditVerifierTestBuilder(verifier))
					.createTestsWithOracle(oracle);
		}
	}
}
