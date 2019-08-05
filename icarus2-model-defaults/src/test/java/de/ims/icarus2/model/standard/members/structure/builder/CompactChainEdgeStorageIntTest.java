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
import de.ims.icarus2.test.annotations.TestLocalOnly;

/**
 * @author Markus Gärtner
 *
 */
class CompactChainEdgeStorageIntTest implements StaticChainEdgeStorageTest<CompactChainEdgeStorageInt>{

	@Override
	public Class<? extends CompactChainEdgeStorageInt> getTestTargetClass() {
		return CompactChainEdgeStorageInt.class;
	}

	@Override
	public ChainsAndTrees.ChainConfig createDefaultTestConfiguration(int size) {
		return ChainsAndTrees.singleChain(size, 1.0);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorageTest#createTestConfigurations()
	 */
	@Override
	public Stream<ChainsAndTrees.ChainConfig> createTestConfigurations() {
		return Stream.of(
				ChainsAndTrees.singleChain(ChainsAndTrees.randomSize(), 1.0),
				ChainsAndTrees.multiChain(ChainsAndTrees.randomSize(), 1.0),
				ChainsAndTrees.singleChain(ChainsAndTrees.randomSize(), 0.25),
				ChainsAndTrees.multiChain(ChainsAndTrees.randomSize(), 0.25)
				);
	}

	@Override
	public CompactChainEdgeStorageInt createFromBuilder(StructureBuilder builder) {
		return CompactChainEdgeStorageInt.fromBuilder(builder);
	}

	@Nested
	class EdgeCases {

		@Test
		@TestLocalOnly
		void testMaxChainSize() {
			int size = CompactChainEdgeStorageInt.MAX_NODE_COUNT;
			ChainsAndTrees.ChainConfig chainConfig = ChainsAndTrees.singleChain(size, 1.0);
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
