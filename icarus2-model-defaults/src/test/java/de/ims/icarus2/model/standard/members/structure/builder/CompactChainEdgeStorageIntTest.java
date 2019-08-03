/**
 *
 */
package de.ims.icarus2.model.standard.members.structure.builder;

import java.util.stream.Stream;

import de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorage.CompactChainEdgeStorageInt;

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
	public Config createDefaultTestConfiguration(int size) {
		return Chains.singleChain(size, 1.0);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorageTest#createTestConfigurations()
	 */
	@Override
	public Stream<Config> createTestConfigurations() {
		return Stream.of(
				Chains.singleChain(Chains.randomSize(), 1.0),
				Chains.multiChain(Chains.randomSize(), 1.0),
				Chains.singleChain(Chains.randomSize(), 0.25),
				Chains.multiChain(Chains.randomSize(), 0.25)
				);
	}

	@Override
	public CompactChainEdgeStorageInt createFromBuilder(StructureBuilder builder) {

		builder.prepareEdgeBuffer();

		return CompactChainEdgeStorageInt.fromBuilder(builder);
	}

}
