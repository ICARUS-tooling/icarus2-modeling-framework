/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
public class UnrestrictedContainerEditVerifierTest {

	@TestFactory
	Stream<DynamicTest> testNullArguments() {
		Container container = mockContainer(0);
		UnrestrictedContainerEditVerifier verifier = new UnrestrictedContainerEditVerifier(container);

		return ContainerEditVerifierTestBuilder.createNullArgumentsTests(verifier);
	}

	@Test
	void testLifecycle() {
		Container container = mockContainer(0);
		@SuppressWarnings("resource")
		UnrestrictedContainerEditVerifier verifier = new UnrestrictedContainerEditVerifier(container);

		assertEquals(container, verifier.getSource());

		verifier.close();

		assertNull(verifier.getSource());
	}

	@TestFactory
	Stream<DynamicTest> testEmptyContainer() {
		return configureBuilderEmpty(new ContainerEditVerifierTestBuilder(
					new UnrestrictedContainerEditVerifier(mockContainer(0))))
				.createTests();
	}

	/**
	 * Configures the given builder under the assumption that the
	 * underlying container is empty, i.e. it contains exactly
	 * {@code 0} items.
	 *
	 * @param builder
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static ContainerEditVerifierTestBuilder configureBuilderEmpty(
			ContainerEditVerifierTestBuilder builder) {
		assertTrue(builder.getVerifier().getSource().getItemCount()==0L);

		return builder
				.addSingleLegal(0)
				.addSingleIllegal(-1, 1, Long.MAX_VALUE)
				.addBatchLegal(0)
				.addBatchIllegal(-1, 1)
				.removeSingleIllegal(-1, 0, 1)
				.removeBatchIllegal(pair(0, 0), pair(1, 1), pair(0, 1))
				.swapSingleIllegal(pair(0, 0), pair(-1, 0), pair(0, 1));
	}

	@TestFactory
	Stream<DynamicTest> testSmallContainerSize10() {
		return configureBuilder(new ContainerEditVerifierTestBuilder(
						new UnrestrictedContainerEditVerifier(mockContainer(10))))
				.createTests();
	}

	@TestFactory
	Stream<DynamicTest> testLargeContainerLongMax() {
		return configureBuilder(new ContainerEditVerifierTestBuilder(
						new UnrestrictedContainerEditVerifier(mockContainer(Long.MAX_VALUE-1))))
				.createTests();
	}

	/**
	 * Configures the given builder under the assumption that the
	 * underlying container has at least 5 elements.
	 *
	 * @param builder
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static ContainerEditVerifierTestBuilder configureBuilder(
			ContainerEditVerifierTestBuilder builder) {

		long size = builder.getVerifier().getSource().getItemCount();
		assertTrue(size>4L, "must have 5 or more elements: "+size);
		assertTrue(size<Long.MAX_VALUE, "size must be less than Long.MAX_VALUE");

		long mid = size>>>1;

		return builder
				.addSingleLegal(0, 1, mid, size-1, size)
				.addSingleIllegal(-1, size+1)
				.addBatchLegal(0, 1, mid, size-1, size)
				.addBatchIllegal(-1, size+1)
				.removeSingleLegal(0, 1, mid, size-2, size-1)
				.removeSingleIllegal(-1, size, size+1)
				.removeBatchLegal(pair(0L, 0L), pair(size-1, size-1),
						pair(0, size-1), pair(1, mid), pair(mid, size-1))
				.removeBatchIllegal(pair(0, size), pair(1, size+1), pair(-1, mid), pair(mid, 1))
				.swapSingleLegal(pair(0L, 0L), pair(size-1, size-1), pair(size-1, 0), pair(1, mid))
				.swapSingleIllegal(pair(-1L, 1L), pair(size-2, size), pair(0, size+1));
	}
}
