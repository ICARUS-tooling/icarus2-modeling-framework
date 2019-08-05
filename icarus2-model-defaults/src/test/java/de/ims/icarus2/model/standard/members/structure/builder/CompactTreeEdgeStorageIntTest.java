/**
 *
 */
package de.ims.icarus2.model.standard.members.structure.builder;

import de.ims.icarus2.model.standard.members.structure.builder.StaticTreeEdgeStorage.CompactTreeEdgeStorageInt;

/**
 * @author Markus Gärtner
 *
 */
class CompactTreeEdgeStorageIntTest implements StaticTreeEdgeStorageTest<CompactTreeEdgeStorageInt> {

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends CompactTreeEdgeStorageInt> getTestTargetClass() {
		return CompactTreeEdgeStorageInt.class;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.builder.StaticTreeEdgeStorageTest#createDefaultTestConfiguration(int)
	 */
	@Override
	public ChainsAndTrees.TreeConfig createDefaultTestConfiguration(int size) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.builder.StaticTreeEdgeStorageTest#createFromBuilder(de.ims.icarus2.model.standard.members.structure.builder.StructureBuilder)
	 */
	@Override
	public CompactTreeEdgeStorageInt createFromBuilder(StructureBuilder builder) {
		return CompactTreeEdgeStorageInt.fromBuilder(builder);
	}

}
