/**
 *
 */
package de.ims.icarus2.model.standard.members.container;

import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.test.util.Pair.longPair;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifierTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
class AugmentedContainerEditVerifierTest implements ContainerEditVerifierTest<AugmentedContainerEditVerifier> {

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifierTest#createContainerEditVerifier(de.ims.icarus2.test.TestSettings, de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public AugmentedContainerEditVerifier createContainerEditVerifier(TestSettings settings, Container container) {
		AugmentedItemStorage storage = mockStorage(container, 0);

		return new AugmentedContainerEditVerifier(container, storage);
	}

	@SuppressWarnings("boxing")
	private static AugmentedItemStorage mockStorage(Container source, int augmentation) {
		requireNonNull(source);
		checkArgument(augmentation>=0);

		AugmentedItemStorage storage = mock(AugmentedItemStorage.class);

		final long wrappedCount = source.getItemCount();

		when(storage.isWrappedIndex(any(), anyLong())).thenAnswer(invocation -> {
			long index = invocation.getArgument(1);
			return index<wrappedCount;
		});

		when(storage.getWrappedItemCount(any())).thenReturn(wrappedCount);
		when(storage.getItemCount(any())).thenReturn(wrappedCount+augmentation);

		return storage;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifierTest#testEmptyContainer()
	 */
	@SuppressWarnings("unchecked")
	@Override
	@TestFactory
	public Stream<DynamicTest> testEmptyContainer() {
		Container container = mockContainer(0);
		AugmentedItemStorage storage = mockStorage(container, 0);
		return new ContainerEditVerifierTestSpec(
				new AugmentedContainerEditVerifier(container, storage))
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
	@DisplayName("testSmallContainer() [wrapped=5, augment=5]")
	public Stream<DynamicTest> testSmallContainer() {
		Container container = mockContainer(5);
		AugmentedItemStorage storage = mockStorage(container, 5);
		return new ContainerEditVerifierTestSpec(
				new AugmentedContainerEditVerifier(container, storage))
			.addSingleLegal(5, 7, 9, 10)
			.addSingleIllegal(-1, 0, 1, 4)
			.addBatchLegal(5, 7, 9, 10)
			.addBatchIllegal(-1, 0, 1, 4)
			.removeSingleLegal(5, 7, 9)
			.removeSingleIllegal(-1, 0, 1, 4, 10)
			.removeBatchLegal(longPair(5, 5), longPair(9, 9), longPair(5, 9), longPair(7, 8))
			.removeBatchIllegal(longPair(0, 0), longPair(1, 1), longPair(4, 4),
					longPair(0, 4), longPair(0, 1), longPair(4, 9), longPair(10, 10))
			.moveSingleLegal(longPair(5, 9), longPair(9, 7))
			.moveSingleIllegal(longPair(0, 0), longPair(1, 1), longPair(4, 4),
					longPair(4, 9))
			.test();
	}

	@SuppressWarnings("unchecked")
	@TestFactory
	@DisplayName("testSmallContainer2() [wrapped=10, augment=0]")
	public Stream<DynamicTest> testSmallContainer2() {
		Container container = mockContainer(10);
		AugmentedItemStorage storage = mockStorage(container, 0);
		return new ContainerEditVerifierTestSpec(
				new AugmentedContainerEditVerifier(container, storage))
			.addSingleLegal(10)
			.addSingleIllegal(-1, 0, 9)
			.addBatchLegal(10)
			.addBatchIllegal(-1, 0, 9)
			.removeSingleIllegal(-1, 0, 1, 9, 10)
			.removeBatchIllegal(longPair(0, 0), longPair(1, 1), longPair(4, 9), longPair(10, 10))
			.moveSingleIllegal(longPair(0, 0), longPair(1, 1), longPair(4, 9), longPair(9, 10))
			.test();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifierTest#testLargeContainer()
	 */
	@SuppressWarnings("unchecked")
	@Override
	@TestFactory
	@DisplayName("testLargeContainer() [wrapped=Long.MAX_VALUE/2, augment=MAX_INDEX]")
	public Stream<DynamicTest> testLargeContainer() {
		// Wrapped Size
		final long WS = Long.MAX_VALUE/2;
		// Augmentation Size
		final int AS = IcarusUtils.MAX_INTEGER_INDEX;

		Container container = mockContainer(WS);
		AugmentedItemStorage storage = mockStorage(container, AS);

		return new ContainerEditVerifierTestSpec(
				new AugmentedContainerEditVerifier(container, storage))
			.addSingleLegal(WS, WS+AS/2, WS+AS)
			.addSingleIllegal(-1, 0, WS-1, WS+AS+1, Long.MAX_VALUE)
			.addBatchLegal(WS, WS+AS/2, WS+AS)
			.addBatchIllegal(-1, 0, WS-1, WS+AS+1, Long.MAX_VALUE)
			.removeSingleLegal(WS, WS+AS/2, WS+AS-1)
			.removeSingleIllegal(-1, 0, WS-1, WS+AS, Long.MAX_VALUE)
			.removeBatchLegal(longPair(WS, WS), longPair(WS+AS-1, WS+AS-1), longPair(WS, WS+AS-1), longPair(WS+AS/2, WS+AS-2))
			.removeBatchIllegal(longPair(0, 0), longPair(1, 1), longPair(WS-1, WS-1),
					longPair(0, 1000), longPair(0, WS), longPair(4, WS+AS), longPair(WS+AS, WS+AS))
			.moveSingleLegal(longPair(WS, WS+AS-1), longPair(WS+AS-1, WS+1))
			.moveSingleIllegal(longPair(0, 0), longPair(1, 1), longPair(WS/2, 4),
					longPair(WS/2, WS+AS-1))
			.test();
	}
}
