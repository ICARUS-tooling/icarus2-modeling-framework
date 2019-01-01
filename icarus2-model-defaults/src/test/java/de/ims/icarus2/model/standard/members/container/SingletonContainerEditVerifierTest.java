/**
 *
 */
package de.ims.icarus2.model.standard.members.container;

import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.test.util.Pair.longPair;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifierTest;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class SingletonContainerEditVerifierTest implements ContainerEditVerifierTest<SingletonContainerEditVerifier> {

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifierTest#createContainerEditVerifier(de.ims.icarus2.test.TestSettings, de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public SingletonContainerEditVerifier createContainerEditVerifier(TestSettings settings, Container container) {
		return new SingletonContainerEditVerifier(container);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifierTest#testEmptyContainer()
	 */
	@SuppressWarnings("unchecked")
	@Override
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
			.test();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifierTest#testSmallContainer()
	 */
	@SuppressWarnings("unchecked")
	@Override
	@TestFactory
	public Stream<DynamicTest> testSmallContainer() {
		return new ContainerEditVerifierTestSpec(
				new SingletonContainerEditVerifier(mockContainer(1)))
			.addSingleIllegal(-1, 0, 1)
			.addBatchIllegal(-1, 0, 1)
			.removeSingleLegal(0)
			.removeSingleIllegal(-1, 1)
			.removeBatchLegal(longPair(0, 0))
			.removeBatchIllegal(longPair(1, 1), longPair(0, 1))
			.moveSingleIllegal(longPair(0, 0), longPair(1, 1))
			.test();
	}

	/**
	 * Not an actual test method, since cases are covered by {@link #testSmallContainer()}.
	 * Not marked as {@link TestFactory}!
	 *
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifierTest#testLargeContainer()
	 */
	@Override
	public Stream<DynamicTest> testLargeContainer() {
		// Nothing to do here, all cases covered by testSmallContainer();
		return Stream.of();
	}

}
