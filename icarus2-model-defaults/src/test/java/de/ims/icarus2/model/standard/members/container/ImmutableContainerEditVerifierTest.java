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
class ImmutableContainerEditVerifierTest {

	@Test
	void testLifecycle() {
		Container container = mockContainer(0);
		@SuppressWarnings("resource")
		ImmutableContainerEditVerifier verifier = new ImmutableContainerEditVerifier(container);

		assertEquals(container, verifier.getSource());

		verifier.close();

		assertNull(verifier.getSource());
	}

	@SuppressWarnings("unchecked")
	@TestFactory
	public Stream<DynamicTest> testEmptyContainer() {
		return new ContainerEditVerifierTestBuilder(
				new ImmutableContainerEditVerifier(mockContainer(0)))
			.addSingleIllegal(-1, 1, Long.MAX_VALUE)
			.addBatchIllegal(-1, 1)
			.removeSingleIllegal(-1, 0, 1)
			.removeBatchIllegal(longPair(0, 0), longPair(1, 1), longPair(0, 1))
			.swapSingleIllegal(longPair(0, 0), longPair(1, 1))
			.createTests();
	}

	@SuppressWarnings("unchecked")
	@TestFactory
	public Stream<DynamicTest> testSmallContainerSize10() {
		return new ContainerEditVerifierTestBuilder(
				new ImmutableContainerEditVerifier(mockContainer(10)))
			.addSingleIllegal(0, 1, Long.MAX_VALUE)
			.addBatchIllegal(0, 3, 9, Long.MAX_VALUE)
			.removeSingleIllegal(-1, 0, 5, 9, Long.MAX_VALUE)
			.removeBatchIllegal(longPair(0, 0), longPair(1, 1), longPair(0, 1))
			.swapSingleIllegal(longPair(0, 0), longPair(1, 1), longPair(9, 1))
			.createTests();
	}

	@SuppressWarnings("unchecked")
	@TestFactory
	public Stream<DynamicTest> testLargeContainer() {
		return new ContainerEditVerifierTestBuilder(
				new ImmutableContainerEditVerifier(mockContainer(Long.MAX_VALUE)))
			.addSingleIllegal(-1, 1, Long.MAX_VALUE)
			.addBatchIllegal(-1, 1, Long.MAX_VALUE)
			.removeSingleIllegal(-1, 0, Long.MAX_VALUE)
			.removeBatchIllegal(longPair(0, 0), longPair(1, Long.MAX_VALUE), longPair(0, 1))
			.swapSingleIllegal(longPair(0, 0), longPair(1, Long.MAX_VALUE), longPair(Long.MAX_VALUE, 1))
			.createTests();
	}
}
