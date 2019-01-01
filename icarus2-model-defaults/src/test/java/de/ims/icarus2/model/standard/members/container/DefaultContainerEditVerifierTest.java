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
class DefaultContainerEditVerifierTest implements ContainerEditVerifierTest<DefaultContainerEditVerifier> {

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifierTest#createContainerEditVerifier(de.ims.icarus2.test.TestSettings, de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public DefaultContainerEditVerifier createContainerEditVerifier(TestSettings settings, Container container) {
		return new DefaultContainerEditVerifier(container);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifierTest#testEmptyContainer()
	 */
	@SuppressWarnings("unchecked")
	@Override
	@TestFactory
	public Stream<DynamicTest> testEmptyContainer() {
		return new ContainerEditVerifierTestSpec(
					new DefaultContainerEditVerifier(mockContainer(0)))
				.addSingleLegal(0)
				.addSingleIllegal(-1, 1, Long.MAX_VALUE)
				.addBatchLegal(0)
				.addBatchIllegal(-1, 1)
				.removeSingleIllegal(-1, 0, 1)
				.removeBatchIllegal(longPair(0, 0), longPair(1, 1), longPair(0, 1))
				.moveSingleIllegal(longPair(0, 0), longPair(-1, 0), longPair(0, 1))
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
						new DefaultContainerEditVerifier(mockContainer(10)))
				.addSingleLegal(0, 1, 5, 8, 9, 10)
				.addSingleIllegal(-1, 11, Long.MAX_VALUE)
				.addBatchLegal(0, 1, 5, 8, 9, 10)
				.addBatchIllegal(-1, 11)
				.removeSingleLegal(0, 1, 5, 8, 9)
				.removeSingleIllegal(-1, 10, 11)
				.removeBatchLegal(longPair(0, 0), longPair(9, 9), longPair(0, 9), longPair(2, 7))
				.removeBatchIllegal(longPair(0, 10), longPair(2, 11), longPair(-1, 5), longPair(5, 3))
				.moveSingleLegal(longPair(0, 0), longPair(9, 9), longPair(9, 0), longPair(2, 6))
				.moveSingleIllegal(longPair(-1, 1), longPair(8, 10), longPair(0, 12))
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
						new DefaultContainerEditVerifier(mockContainer(Long.MAX_VALUE-1)))
				.addSingleLegal(0, Long.MAX_VALUE-1)
				.addSingleIllegal(-1, Long.MAX_VALUE)
				.addBatchLegal(0, Long.MAX_VALUE-1)
				.addBatchIllegal(-1, Long.MAX_VALUE)
				.removeSingleLegal(0, Long.MAX_VALUE-2)
				.removeSingleIllegal(-1, Long.MAX_VALUE-1)
				.removeBatchLegal(longPair(0, 0), longPair(0, Long.MAX_VALUE-2),
						longPair(10_000_000L, 10_000_000_000L))
				.removeBatchIllegal(longPair(0, Long.MAX_VALUE-1),
						longPair(10_000_000_000L, Long.MAX_VALUE),
						longPair(-1, 10_000_000),
						longPair(Long.MAX_VALUE-2, 10_000_000))
				.moveSingleLegal(longPair(0, 0), longPair(10_000_000, 10_000_000_000L),
						longPair(Long.MAX_VALUE-2, 0))
				.moveSingleIllegal(longPair(-1, 1),
						longPair(Long.MAX_VALUE-2, Long.MAX_VALUE))
				.test();
	}
}
