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
class ImmutableContainerEditVerifierTest implements ContainerEditVerifierTest<ImmutableContainerEditVerifier> {

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifierTest#createContainerEditVerifier(de.ims.icarus2.test.TestSettings, de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public ImmutableContainerEditVerifier createContainerEditVerifier(TestSettings settings, Container container) {
		return new ImmutableContainerEditVerifier(container);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifierTest#testEmptyContainer()
	 */
	@SuppressWarnings("unchecked")
	@Override
	@TestFactory
	public Stream<DynamicTest> testEmptyContainer() {
		return new ContainerEditVerifierTestSpec(
				new ImmutableContainerEditVerifier(mockContainer(0)))
			.addSingleIllegal(-1, 1, Long.MAX_VALUE)
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
				new ImmutableContainerEditVerifier(mockContainer(10)))
			.addSingleIllegal(0, 1, Long.MAX_VALUE)
			.addBatchIllegal(0, 3, 9, Long.MAX_VALUE)
			.removeSingleIllegal(-1, 0, 5, 9, Long.MAX_VALUE)
			.removeBatchIllegal(longPair(0, 0), longPair(1, 1), longPair(0, 1))
			.moveSingleIllegal(longPair(0, 0), longPair(1, 1), longPair(9, 1))
			.test();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifierTest#testLargeContainer()
	 */
	@SuppressWarnings("unchecked")
	@Override
	@TestFactory
	public Stream<DynamicTest> testLargeContainer() {
		return new ContainerEditVerifierTestSpec(
				new ImmutableContainerEditVerifier(mockContainer(Long.MAX_VALUE)))
			.addSingleIllegal(-1, 1, Long.MAX_VALUE)
			.addBatchIllegal(-1, 1, Long.MAX_VALUE)
			.removeSingleIllegal(-1, 0, Long.MAX_VALUE)
			.removeBatchIllegal(longPair(0, 0), longPair(1, Long.MAX_VALUE), longPair(0, 1))
			.moveSingleIllegal(longPair(0, 0), longPair(1, Long.MAX_VALUE), longPair(Long.MAX_VALUE, 1))
			.test();
	}
}
