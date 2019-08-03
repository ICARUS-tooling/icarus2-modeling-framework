/**
 *
 */
package de.ims.icarus2.model.standard.members.structure.builder;

import java.util.stream.Stream;

import de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorage.CompactChainEdgeStorageLong;

/**
 * @author Markus Gärtner
 *
 */
class CompactChainEdgeStorageLongTest implements StaticChainEdgeStorageTest<CompactChainEdgeStorageLong> {

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends CompactChainEdgeStorageLong> getTestTargetClass() {
		return CompactChainEdgeStorageLong.class;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorageTest#createDefaultTestConfiguration(int)
	 */
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

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorageTest#createFromBuilder(de.ims.icarus2.model.standard.members.structure.builder.StructureBuilder)
	 */
	@Override
	public CompactChainEdgeStorageLong createFromBuilder(StructureBuilder builder) {

		builder.prepareEdgeBuffer();

		return CompactChainEdgeStorageLong.fromBuilder(builder);
	}

}
