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
	public Config createDefaultTestConfiguration(int size) {
		return Chains.singleChain(size, 0.2);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorageTest#createTestConfigurations()
	 */
	@Override
	public Stream<Config> createTestConfigurations() {
		return Stream.of(
				Chains.singleChain(Chains.randomSize(), 1.0),
				Chains.multiChain(Chains.randomSize(), 1.0),
				Chains.singleChain(Chains.randomSize(), 0.20),
				Chains.multiChain(Chains.randomSize(), 0.20)
				);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorageTest#createFromBuilder(de.ims.icarus2.model.standard.members.structure.builder.StructureBuilder)
	 */
	@Override
	public LargeSparseChainEdgeStorage createFromBuilder(StructureBuilder builder) {

		builder.prepareEdgeBuffer();

		return LargeSparseChainEdgeStorage.fromBuilder(builder);
	}

}
