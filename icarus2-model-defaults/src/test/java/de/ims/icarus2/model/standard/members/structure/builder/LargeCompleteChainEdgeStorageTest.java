/**
 *
 */
package de.ims.icarus2.model.standard.members.structure.builder;

import java.util.stream.Stream;

import de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorage.LargeCompleteChainEdgeStorage;

/**
 * @author Markus Gärtner
 *
 */
class LargeCompleteChainEdgeStorageTest implements StaticChainEdgeStorageTest<LargeCompleteChainEdgeStorage> {

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends LargeCompleteChainEdgeStorage> getTestTargetClass() {
		return LargeCompleteChainEdgeStorage.class;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorageTest#createDefaultTestConfiguration(int)
	 */
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

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorageTest#createFromBuilder(de.ims.icarus2.model.standard.members.structure.builder.StructureBuilder)
	 */
	@Override
	public LargeCompleteChainEdgeStorage createFromBuilder(StructureBuilder builder) {
		return LargeCompleteChainEdgeStorage.fromBuilder(builder);
	}
}
