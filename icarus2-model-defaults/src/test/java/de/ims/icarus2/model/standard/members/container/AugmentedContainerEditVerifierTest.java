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
package de.ims.icarus2.model.standard.members.container;

import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.test.util.Pair.pair;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
class AugmentedContainerEditVerifierTest {

	@TestFactory
	public Stream<DynamicTest> testNullArguments() {
		Container container = mockContainer(0);
		AugmentedItemStorage storage = mockStorage(container, 0);

		return ContainerEditVerifierTestBuilder.createNullArgumentsTests(
				new AugmentedContainerEditVerifier(container, storage));
	}

	@Test
	void testIsAllowEdits() {
		Container container = mockContainer(0);
		AugmentedItemStorage storage = mockStorage(container, 0);
		@SuppressWarnings("resource")
		AugmentedContainerEditVerifier verifier = new AugmentedContainerEditVerifier(container, storage);

		assertTrue(verifier.isAllowEdits());
	}


	@Test
	void testLifecycle() {
		Container container = mockContainer(0);
		AugmentedItemStorage storage = mockStorage(container, 0);
		@SuppressWarnings("resource")
		AugmentedContainerEditVerifier verifier = new AugmentedContainerEditVerifier(container, storage);

		assertEquals(container, verifier.getSource());
		assertEquals(storage, verifier.getStorage());

		verifier.close();

		assertNull(verifier.getSource());
		assertNull(verifier.getStorage());
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

	@SuppressWarnings({ "unchecked" })
	@TestFactory
	Stream<DynamicTest> testEmptyContainer() {
		Container container = mockContainer(0);
		AugmentedItemStorage storage = mockStorage(container, 0);
		return new ContainerEditVerifierTestBuilder(
				new AugmentedContainerEditVerifier(container, storage))
			.addSingleLegal(0)
			.addSingleIllegal(-1, 1)
			.addBatchLegal(0)
			.addBatchIllegal(-1, 1)
			.removeSingleIllegal(-1, 0, 1)
			.removeBatchIllegal(pair(0, 0), pair(1, 1), pair(0, 1))
			.swapSingleIllegal(pair(0, 0), pair(1, 1))
			.createTests();
	}

	@SuppressWarnings({ "unchecked" })
	@TestFactory
	Stream<DynamicTest> testSmallContainerSize5Aug5() {
		Container container = mockContainer(5);
		AugmentedItemStorage storage = mockStorage(container, 5);
		return new ContainerEditVerifierTestBuilder(
				new AugmentedContainerEditVerifier(container, storage))
			.addSingleLegal(5, 7, 9, 10)
			.addSingleIllegal(-1, 0, 1, 4)
			.addBatchLegal(5, 7, 9, 10)
			.addBatchIllegal(-1, 0, 1, 4)
			.removeSingleLegal(5, 7, 9)
			.removeSingleIllegal(-1, 0, 1, 4, 10)
			.removeBatchLegal(pair(5, 5), pair(9, 9), pair(5, 9), pair(7, 8))
			.removeBatchIllegal(pair(0, 0), pair(1, 1), pair(4, 4),
					pair(0, 4), pair(0, 1), pair(4, 9), pair(10, 10))
			.swapSingleLegal(pair(5, 9), pair(9, 7))
			.swapSingleIllegal(pair(0, 0), pair(1, 1), pair(4, 4),
					pair(4, 9))
			.createTests();
	}

	@SuppressWarnings({ "unchecked" })
	@TestFactory
	Stream<DynamicTest> testSmallContainerSize10NoAug() {
		Container container = mockContainer(10);
		AugmentedItemStorage storage = mockStorage(container, 0);
		return new ContainerEditVerifierTestBuilder(
				new AugmentedContainerEditVerifier(container, storage))
			.addSingleLegal(10)
			.addSingleIllegal(-1, 0, 9)
			.addBatchLegal(10)
			.addBatchIllegal(-1, 0, 9)
			.removeSingleIllegal(-1, 0, 1, 9, 10)
			.removeBatchIllegal(pair(0, 0), pair(1, 1), pair(4, 9), pair(10, 10))
			.swapSingleIllegal(pair(0, 0), pair(1, 1), pair(4, 9), pair(9, 10))
			.createTests();
	}

	@SuppressWarnings({ "unchecked" })
	@TestFactory
	Stream<DynamicTest> testLargeContainerWithAugmentation() {
		// Wrapped Size
		final long WS = Long.MAX_VALUE/2;
		// Augmentation Size
		final int AS = IcarusUtils.MAX_INTEGER_INDEX;

		Container container = mockContainer(WS);
		AugmentedItemStorage storage = mockStorage(container, AS);

		return new ContainerEditVerifierTestBuilder(
				new AugmentedContainerEditVerifier(container, storage))
			.addSingleLegal(WS, WS+AS/2, WS+AS)
			.addSingleIllegal(-1, 0, WS-1, WS+AS+1, Long.MAX_VALUE)
			.addBatchLegal(WS, WS+AS/2, WS+AS)
			.addBatchIllegal(-1, 0, WS-1, WS+AS+1, Long.MAX_VALUE)
			.removeSingleLegal(WS, WS+AS/2, WS+AS-1)
			.removeSingleIllegal(-1, 0, WS-1, WS+AS, Long.MAX_VALUE)
			.removeBatchLegal(pair(WS, WS), pair(WS+AS-1, WS+AS-1), pair(WS, WS+AS-1), pair(WS+AS/2, WS+AS-2))
			.removeBatchIllegal(pair(0L, 0L), pair(1L, 1L), pair(WS-1, WS-1),
					pair(0L, 1000L), pair(0, WS), pair(4, WS+AS), pair(WS+AS, WS+AS))
			.swapSingleLegal(pair(WS, WS+AS-1), pair(WS+AS-1, WS+1))
			.swapSingleIllegal(pair(0L, 0L), pair(1L, 1L), pair(WS/2, 4),
					pair(WS/2, WS+AS-1))
			.createTests();
	}
}
