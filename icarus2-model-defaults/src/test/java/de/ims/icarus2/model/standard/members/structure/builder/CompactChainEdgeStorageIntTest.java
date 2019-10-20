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
package de.ims.icarus2.model.standard.members.structure.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorage.CompactChainEdgeStorageInt;
import de.ims.icarus2.test.annotations.DisabledOnCi;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
@RandomizedTest
class CompactChainEdgeStorageIntTest implements StaticChainEdgeStorageTest<CompactChainEdgeStorageInt>{

	RandomGenerator rng;

	@Override
	public Class<? extends CompactChainEdgeStorageInt> getTestTargetClass() {
		return CompactChainEdgeStorageInt.class;
	}

	@Override
	public ChainsAndTrees.ChainConfig createDefaultTestConfiguration(int size) {
		return ChainsAndTrees.singleChain(rng, size, 1.0);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorageTest#createTestConfigurations()
	 */
	@Override
	public Stream<ChainsAndTrees.ChainConfig> createTestConfigurations() {
		return Stream.of(
				ChainsAndTrees.singleChain(rng, ChainsAndTrees.randomSize(rng), 1.0),
				ChainsAndTrees.multiChain(rng, ChainsAndTrees.randomSize(rng), 1.0),
				ChainsAndTrees.singleChain(rng, ChainsAndTrees.randomSize(rng), 0.25),
				ChainsAndTrees.multiChain(rng, ChainsAndTrees.randomSize(rng), 0.25)
				);
	}

	@Override
	public CompactChainEdgeStorageInt createFromBuilder(StructureBuilder builder) {
		return CompactChainEdgeStorageInt.fromBuilder(builder);
	}

	@Nested
	class EdgeCases {

		@Test
		@DisabledOnCi
		@RandomizedTest
		void testMaxChainSize(RandomGenerator rng) {
			int size = CompactChainEdgeStorageInt.MAX_NODE_COUNT;
			ChainsAndTrees.ChainConfig chainConfig = ChainsAndTrees.singleChain(rng, size, 1.0);
			CompactChainEdgeStorageInt chain = createFromBuilder(toBuilder(chainConfig));
			assertEquals(size, chain.getEdgeCount(chainConfig.structure));
		}

		@SuppressWarnings("boxing")
		@Test
		void testOverflowChainSize() {
			StructureBuilder builder = mock(StructureBuilder.class);
			when(builder.getNodeCount()).thenReturn(CompactChainEdgeStorageInt.MAX_NODE_COUNT+1);
			assertThrows(IllegalArgumentException.class,
					() -> createFromBuilder(builder));
		}
	}
}
