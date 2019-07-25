/**
 *
 */
package de.ims.icarus2.model.standard.members.structure;

import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.StructureType;

/**
 * @author Markus Gärtner
 *
 */
public class ChainEdgeStorage extends AbstractNodeStorage<ChainNodeInfo, Edge> {

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#getStructureType()
	 */
	@Override
	public StructureType getStructureType() {
		return StructureType.CHAIN;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.AbstractNodeStorage#createNodeInfo(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	protected ChainNodeInfo createNodeInfo(Item node) {
		return new ChainNodeInfo();
	}

}
