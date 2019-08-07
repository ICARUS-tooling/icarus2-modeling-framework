/**
 *
 */
package de.ims.icarus2.model.standard.members.structure.builder;

import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;

import de.ims.icarus2.model.standard.members.structure.builder.ChainsAndTrees.TreeConfig;
import de.ims.icarus2.model.standard.members.structure.builder.StaticTreeEdgeStorage.CompactTreeEdgeStorageLong;

/**
 * @author Markus Gärtner
 *
 */
class CompactTreeEdgeStorageLongTest implements StaticTreeEdgeStorageTest<CompactTreeEdgeStorageLong> {

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends CompactTreeEdgeStorageLong> getTestTargetClass() {
		return CompactTreeEdgeStorageLong.class;
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
	public CompactTreeEdgeStorageLong createFromBuilder(StructureBuilder builder) {
		return CompactTreeEdgeStorageLong.fromBuilder(builder);
	}

}
