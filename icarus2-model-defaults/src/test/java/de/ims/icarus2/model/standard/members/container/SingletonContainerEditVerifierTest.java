/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.container;

import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.test.util.Pair.pair;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.api.members.container.Container;

/**
 * @author Markus Gärtner
 *
 */
class SingletonContainerEditVerifierTest {

	@TestFactory
	Stream<DynamicTest> testNullArguments() {
		Container container = mockContainer(0);
		SingletonContainerEditVerifier verifier = new SingletonContainerEditVerifier(container);

		return ContainerEditVerifierTestBuilder.createNullArgumentsTests(verifier);
	}

	@Test
	void testLifecycle() {
		Container container = mockContainer(0);
		@SuppressWarnings("resource")
		SingletonContainerEditVerifier verifier = new SingletonContainerEditVerifier(container);

		assertEquals(container, verifier.getSource());

		verifier.close();

		assertNull(verifier.getSource());
	}

	@SuppressWarnings({ "unchecked" })
	@TestFactory
	Stream<DynamicTest> testEmptyContainer() {
		return new ContainerEditVerifierTestBuilder(
				new SingletonContainerEditVerifier(mockContainer(0)))
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
	Stream<DynamicTest> testFullContainer() {
		return new ContainerEditVerifierTestBuilder(
				new SingletonContainerEditVerifier(mockContainer(1)))
			.addSingleIllegal(-1, 0, 1)
			.addBatchIllegal(-1, 0, 1)
			.removeSingleLegal(0)
			.removeSingleIllegal(-1, 1)
			.removeBatchLegal(pair(0, 0))
			.removeBatchIllegal(pair(1, 1), pair(0, 1))
			.swapSingleIllegal(pair(0, 0), pair(1, 1))
			.createTests();
	}

}
