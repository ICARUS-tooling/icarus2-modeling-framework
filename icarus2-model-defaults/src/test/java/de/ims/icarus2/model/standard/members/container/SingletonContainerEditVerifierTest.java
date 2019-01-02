/**
 *
 */
package de.ims.icarus2.model.standard.members.container;

import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.test.util.Pair.longPair;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifierTestSpec;

/**
 * @author Markus Gärtner
 *
 */
class SingletonContainerEditVerifierTest {

	@Test
	void testLifecycle() {
		Container container = mockContainer(0);
		@SuppressWarnings("resource")
		SingletonContainerEditVerifier verifier = new SingletonContainerEditVerifier(container);

		assertEquals(container, verifier.getSource());

		verifier.close();

		assertNull(verifier.getSource());
	}

	@SuppressWarnings("unchecked")
	@TestFactory
	public Stream<DynamicTest> testEmptyContainer() {
		return new ContainerEditVerifierTestSpec(
				new SingletonContainerEditVerifier(mockContainer(0)))
			.addSingleLegal(0)
			.addSingleIllegal(-1, 1)
			.addBatchLegal(0)
			.addBatchIllegal(-1, 1)
			.removeSingleIllegal(-1, 0, 1)
			.removeBatchIllegal(longPair(0, 0), longPair(1, 1), longPair(0, 1))
			.moveSingleIllegal(longPair(0, 0), longPair(1, 1))
			.createTests();
	}

	@SuppressWarnings("unchecked")
	@TestFactory
	public Stream<DynamicTest> testFullContainer() {
		return new ContainerEditVerifierTestSpec(
				new SingletonContainerEditVerifier(mockContainer(1)))
			.addSingleIllegal(-1, 0, 1)
			.addBatchIllegal(-1, 0, 1)
			.removeSingleLegal(0)
			.removeSingleIllegal(-1, 1)
			.removeBatchLegal(longPair(0, 0))
			.removeBatchIllegal(longPair(1, 1), longPair(0, 1))
			.moveSingleIllegal(longPair(0, 0), longPair(1, 1))
			.createTests();
	}

}
