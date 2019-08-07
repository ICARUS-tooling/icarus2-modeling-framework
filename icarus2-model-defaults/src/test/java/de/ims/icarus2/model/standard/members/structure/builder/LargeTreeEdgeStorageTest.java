/**
 *
 */
package de.ims.icarus2.model.standard.members.structure.builder;

import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;

import de.ims.icarus2.model.standard.members.structure.builder.ChainsAndTrees.TreeConfig;
import de.ims.icarus2.model.standard.members.structure.builder.StaticTreeEdgeStorage.LargeTreeEdgeStorage;

/**
 * @author Markus Gärtner
 *
 */
class LargeTreeEdgeStorageTest implements StaticTreeEdgeStorageTest<LargeTreeEdgeStorage> {

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends LargeTreeEdgeStorage> getTestTargetClass() {
		return LargeTreeEdgeStorage.class;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.builder.StaticTreeEdgeStorageTest#createDefaultTestConfiguration(int)
	 */
	@Override
	public TreeConfig createDefaultTestConfiguration(int size) {
		return ChainsAndTrees.singleTree(size, 1.0, size/3, UNSET_INT);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.builder.StaticTreeEdgeStorageTest#createFromBuilder(de.ims.icarus2.model.standard.members.structure.builder.StructureBuilder)
	 */
	@Override
	public LargeTreeEdgeStorage createFromBuilder(StructureBuilder builder) {
		return LargeTreeEdgeStorage.fromBuilder(builder);
	}

}
