/**
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
package de.ims.icarus2.model.standard.members.structure;

import static de.ims.icarus2.model.api.ModelTestUtils.mockStructure;
import static de.ims.icarus2.test.TestUtils.longRange;
import static de.ims.icarus2.test.util.Pair.intChain;
import static de.ims.icarus2.test.util.Pair.pair;
import static de.ims.icarus2.test.util.Triple.triple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.standard.members.container.ContainerEditVerifierTestBuilder;
import de.ims.icarus2.model.standard.members.container.ImmutableContainerEditVerifierTest;

/**
 * @author Markus Gärtner
 *
 */
@SuppressWarnings("resource")
class ImmutableStructureEditVerifierTest {

	private static final int ROOT = -1;

	@Test
	void testIsAllowEdits() {
		Structure structure = mockStructure(0, 0);
		ImmutableStructureEditVerifier verifier = new ImmutableStructureEditVerifier(structure);

		assertFalse(verifier.isAllowEdits());
	}

	@TestFactory
	Stream<DynamicTest> testNullArguments() {
		Structure structure = mockStructure(0, 0);
		ImmutableStructureEditVerifier verifier = new ImmutableStructureEditVerifier(structure);

		return new StructureEditVerifierTestBuilder(verifier)
				.failForNullMembers()
				.createTests();
	}

	@SuppressWarnings("unchecked")
	@TestFactory
	Stream<DynamicTest> testIllegalMemberArguments() {
		Structure structure = mockStructure(10, pair(1, 2));
		ImmutableStructureEditVerifier verifier = new ImmutableStructureEditVerifier(structure);

		return new StructureEditVerifierTestBuilder(verifier)
				.failForIllegalMembers()
				.createTests();
	}

	@TestFactory
	Stream<DynamicTest> testContainerEditVerifierEmpty() {
		Structure structure = mockStructure(0, 0);
		ImmutableStructureEditVerifier verifier = new ImmutableStructureEditVerifier(structure);

		return ImmutableContainerEditVerifierTest.configureBuilderEmpty(
				new ContainerEditVerifierTestBuilder(verifier))
				.createTests();
	}

	@TestFactory
	Stream<DynamicTest> testContainerEditVerifierFull() {
		Structure structure = mockStructure(10, intChain(ROOT, 9));
		ImmutableStructureEditVerifier verifier = new ImmutableStructureEditVerifier(structure);

		return ImmutableContainerEditVerifierTest.configureBuilder(
				new ContainerEditVerifierTestBuilder(verifier))
				.createTests();
	}

	@TestFactory
	Stream<DynamicTest> testStructureEditVerifierEmpty() {
		Structure structure = mockStructure(0, 0);
		ImmutableStructureEditVerifier verifier = new ImmutableStructureEditVerifier(structure);

		return configureBuilderEmpty(new StructureEditVerifierTestBuilder(verifier))
				.createTests();
	}

	@SuppressWarnings("unchecked")
	public static StructureEditVerifierTestBuilder configureBuilderEmpty(
			StructureEditVerifierTestBuilder builder) {
		assertTrue(builder.getVerifier().getSource().getEdgeCount()==0L);

		return builder
				.removeSingleIllegal(longRange(-1, 1))
				.removeBatchIllegal(pair(0, 0), pair(0, 1))
				.swapSingleIllegal(pair(0, 0), pair(0, 1));
	}

	@TestFactory
	Stream<DynamicTest> testStructureEditVerifierFull() {
		@SuppressWarnings("unchecked")
		Structure structure = mockStructure(10, pair(0, 1));
		ImmutableStructureEditVerifier verifier = new ImmutableStructureEditVerifier(structure);

		return configureBuilder(new StructureEditVerifierTestBuilder(verifier))
				.createTests();
	}

	@SuppressWarnings({ "unchecked", "boxing" })
	public static StructureEditVerifierTestBuilder configureBuilder(
			StructureEditVerifierTestBuilder builder) {

		Structure structure = builder.getVerifier().getSource();
		assertTrue(structure.getItemCount()>3);
		assertTrue(structure.getEdgeCount()>0);

		return builder
				.addSingleIllegal(pair(1, 2), pair(2, ROOT))
				.addBatchIllegalIndirect(pair(1, 2))
					.addBatchIllegalIndirect(pair(1, 2), pair(2, ROOT))
				.removeSingleIllegal(longRange(-1, 1))
				.removeBatchIllegal(pair(0, 0), pair(0, 1))
				.swapSingleIllegal(pair(0,  0), pair(0, 1))
				.setTerminalIllegalIndirect(triple(0, 2, true), triple(0, ROOT, false))
				.createEdgeIllegalIndirect(pair(ROOT, 1), pair(1, 2));
	}
}
