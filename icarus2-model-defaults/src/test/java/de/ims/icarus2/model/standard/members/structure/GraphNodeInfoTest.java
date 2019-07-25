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
class GraphNodeInfoTest implements NodeInfoTest<GraphNodeInfo> {

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends GraphNodeInfo> getTestTargetClass() {
		return GraphNodeInfo.class;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public GraphNodeInfo createTestInstance(TestSettings settings) {
		return settings.process(new GraphNodeInfo());
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfoTest#getExpectedType()
	 */
	@Override
	public Type getExpectedType() {
		return Type.GRAPH;
	}
	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfoTest#getIncomingEquivalent()
	 */
	@Override
	public StructureType getIncomingEquivalent() {
		return StructureType.GRAPH;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfoTest#getOutgoingEquivalent()
	 */
	@Override
	public StructureType getOutgoingEquivalent() {
		return StructureType.GRAPH;
	}

}
