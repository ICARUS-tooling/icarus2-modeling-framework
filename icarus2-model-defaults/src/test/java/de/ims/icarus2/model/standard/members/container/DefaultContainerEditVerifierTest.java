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
import static de.ims.icarus2.test.util.Pair.longPair;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifierTestBuilder;

/**
 * @author Markus Gärtner
 *
 */
class DefaultContainerEditVerifierTest {

	@Test
	void testLifecycle() {
		Container container = mockContainer(0);
		@SuppressWarnings("resource")
		DefaultContainerEditVerifier verifier = new DefaultContainerEditVerifier(container);

		assertEquals(container, verifier.getSource());

		verifier.close();

		assertNull(verifier.getSource());
	}

	@SuppressWarnings("unchecked")
	@TestFactory
	public Stream<DynamicTest> testEmptyContainer() {
		return new ContainerEditVerifierTestBuilder(
					new DefaultContainerEditVerifier(mockContainer(0)))
				.addSingleLegal(0)
				.addSingleIllegal(-1, 1, Long.MAX_VALUE)
				.addBatchLegal(0)
				.addBatchIllegal(-1, 1)
				.removeSingleIllegal(-1, 0, 1)
				.removeBatchIllegal(longPair(0, 0), longPair(1, 1), longPair(0, 1))
				.swapSingleIllegal(longPair(0, 0), longPair(-1, 0), longPair(0, 1))
				.createTests();
	}

	@SuppressWarnings("unchecked")
	@TestFactory
	public Stream<DynamicTest> testSmallContainerSize10() {
		return new ContainerEditVerifierTestBuilder(
						new DefaultContainerEditVerifier(mockContainer(10)))
				.addSingleLegal(0, 1, 5, 8, 9, 10)
				.addSingleIllegal(-1, 11, Long.MAX_VALUE)
				.addBatchLegal(0, 1, 5, 8, 9, 10)
				.addBatchIllegal(-1, 11)
				.removeSingleLegal(0, 1, 5, 8, 9)
				.removeSingleIllegal(-1, 10, 11)
				.removeBatchLegal(longPair(0, 0), longPair(9, 9), longPair(0, 9), longPair(2, 7))
				.removeBatchIllegal(longPair(0, 10), longPair(2, 11), longPair(-1, 5), longPair(5, 3))
				.swapSingleLegal(longPair(0, 0), longPair(9, 9), longPair(9, 0), longPair(2, 6))
				.swapSingleIllegal(longPair(-1, 1), longPair(8, 10), longPair(0, 12))
				.createTests();
	}

	@SuppressWarnings("unchecked")
	@TestFactory
	public Stream<DynamicTest> testLargeContainer() {
		return new ContainerEditVerifierTestBuilder(
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
				.swapSingleLegal(longPair(0, 0), longPair(10_000_000, 10_000_000_000L),
						longPair(Long.MAX_VALUE-2, 0))
				.swapSingleIllegal(longPair(-1, 1),
						longPair(Long.MAX_VALUE-2, Long.MAX_VALUE))
				.createTests();
	}
}
