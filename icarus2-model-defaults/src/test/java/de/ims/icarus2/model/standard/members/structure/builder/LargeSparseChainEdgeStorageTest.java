/**
 *
 */
package de.ims.icarus2.model.standard.members.structure.builder;

import java.util.stream.Stream;

import de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorage.LargeSparseChainEdgeStorage;

/**
 * @author Markus Gärtner
 *
 */
class LargeSparseChainEdgeStorageTest implements StaticChainEdgeStorageTest<LargeSparseChainEdgeStorage> {

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends LargeSparseChainEdgeStorage> getTestTargetClass() {
		return LargeSparseChainEdgeStorage.class;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorageTest#createDefaultTestConfiguration(int)
	 */
	@Override
	public ChainsAndTrees.ChainConfig createDefaultTestConfiguration(int size) {
		return ChainsAndTrees.singleChain(size, 0.2);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorageTest#createTestConfigurations()
	 */
	@Override
	public Stream<ChainsAndTrees.ChainConfig> createTestConfigurations() {
		return Stream.of(
				ChainsAndTrees.singleChain(ChainsAndTrees.randomSize(), 1.0),
				ChainsAndTrees.multiChain(ChainsAndTrees.randomSize(), 1.0),
				ChainsAndTrees.singleChain(ChainsAndTrees.randomSize(), 0.20),
				ChainsAndTrees.multiChain(ChainsAndTrees.randomSize(), 0.20)
				);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorageTest#createFromBuilder(de.ims.icarus2.model.standard.members.structure.builder.StructureBuilder)
	 */
	@Override
	public LargeSparseChainEdgeStorage createFromBuilder(StructureBuilder builder) {
		return LargeSparseChainEdgeStorage.fromBuilder(builder);
	}

}
