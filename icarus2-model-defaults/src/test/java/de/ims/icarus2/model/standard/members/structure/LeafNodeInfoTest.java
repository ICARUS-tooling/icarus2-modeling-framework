/**
 *
 */
package de.ims.icarus2.model.standard.members.structure;

import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.standard.members.structure.NodeInfo.Type;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class LeafNodeInfoTest implements NodeInfoTest<LeafNodeInfo> {

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends LeafNodeInfo> getTestTargetClass() {
		return LeafNodeInfo.class;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public LeafNodeInfo createTestInstance(TestSettings settings) {
		return settings.process(new LeafNodeInfo());
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfoTest#getExpectedType()
	 */
	@Override
	public Type getExpectedType() {
		return Type.LEAF;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfoTest#getIncomingEquivalent()
	 */
	@Override
	public StructureType getIncomingEquivalent() {
		return StructureType.CHAIN;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfoTest#getOutgoingEquivalent()
	 */
	@Override
	public StructureType getOutgoingEquivalent() {
		return StructureType.SET;
	}

}
