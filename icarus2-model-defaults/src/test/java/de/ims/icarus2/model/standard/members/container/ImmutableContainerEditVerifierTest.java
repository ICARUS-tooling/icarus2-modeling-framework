/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.api.members.container.Container;

/**
 * @author Markus Gärtner
 *
 */
public class ImmutableContainerEditVerifierTest {

	@TestFactory
	Stream<DynamicTest> testNullArguments() {
		Container container = mockContainer(0);
		ImmutableContainerEditVerifier verifier = new ImmutableContainerEditVerifier(container);

		return ContainerEditVerifierTestBuilder.createNullArgumentsTests(verifier);
	}

	@Test
	void testIsAllowEdits() {
		Container container = mockContainer(0);
		@SuppressWarnings("resource")
		ImmutableContainerEditVerifier verifier = new ImmutableContainerEditVerifier(container);

		assertFalse(verifier.isAllowEdits());
	}

	@Test
	void testLifecycle() {
		Container container = mockContainer(0);
		@SuppressWarnings("resource")
		ImmutableContainerEditVerifier verifier = new ImmutableContainerEditVerifier(container);

		assertEquals(container, verifier.getSource());

		verifier.close();

		assertNull(verifier.getSource());
	}

	@TestFactory
	Stream<DynamicTest> testEmptyContainer() {
		return configureBuilderEmpty(new ContainerEditVerifierTestBuilder(
				new ImmutableContainerEditVerifier(mockContainer(0))))
				.createTests();
	}

	@SuppressWarnings("unchecked")
	public static ContainerEditVerifierTestBuilder configureBuilderEmpty(
			ContainerEditVerifierTestBuilder builder) {
		assertTrue(builder.getVerifier().getSource().getItemCount()==0L);

		return builder
			.addSingleIllegal(0, 1, Long.MAX_VALUE)
			.addBatchIllegal(0, 3, 9, Long.MAX_VALUE)
			.removeSingleIllegal(-1, 0, 5, 9, Long.MAX_VALUE)
			.removeBatchIllegal(pair(0, 0), pair(1, 1), pair(0, 1))
			.swapSingleIllegal(pair(0, 0), pair(1, 1), pair(9, 1));
	}

	@TestFactory
	Stream<DynamicTest> testSmallContainerSize10() {
		return configureBuilder(new ContainerEditVerifierTestBuilder(
				new ImmutableContainerEditVerifier(mockContainer(10))))
			.createTests();
	}

	@TestFactory
	Stream<DynamicTest> testLargeContainer() {
		return configureBuilder(new ContainerEditVerifierTestBuilder(
				new ImmutableContainerEditVerifier(mockContainer(Long.MAX_VALUE))))
			.createTests();
	}

	@SuppressWarnings("unchecked")
	public static ContainerEditVerifierTestBuilder configureBuilder(
			ContainerEditVerifierTestBuilder builder) {
		long size = builder.getVerifier().getSource().getItemCount();
		assertTrue(size>3);

		long mid = size>>>1;

		return builder
			.addSingleIllegal(0, 1, size)
			.addBatchIllegal(0, mid, size-1, size)
			.removeSingleIllegal(-1, 0, mid, size-1, size)
			.removeBatchIllegal(pair(0L, 0L), pair(1, size), pair(0L, 1L))
			.swapSingleIllegal(pair(0L, 0L), pair(1, size), pair(size, 1));
	}
}
