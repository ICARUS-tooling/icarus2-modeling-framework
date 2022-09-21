/**
 *
 */
package de.ims.icarus2.model.api;

import de.ims.icarus2.model.api.members.structure.Structure;

/**
 * @author Markus Gärtner
 *
 */
public class StructureAssert extends AbstractContainerAssert<StructureAssert, Structure> {

	public StructureAssert(Structure actual) {
		super(actual, StructureAssert.class);
	}

	@SuppressWarnings("boxing")
	public ItemInStructureAssert node(long index) {
		isNotNull();
		if(index>=actual.getItemCount())
			throw failure("Node index %d out of bounds. Structure only holds %d nodes.", index, actual.getItemCount());
		return new ItemInStructureAssert(actual, actual.getItemAt(index));
	}

	@SuppressWarnings("boxing")
	public EdgeInStructureAssert edge(long index) {
		isNotNull();
		if(index>=actual.getEdgeCount())
			throw failure("Edge index %d out of bounds. Structure only holds %d edges.", index, actual.getEdgeCount());
		return new EdgeInStructureAssert(actual, actual.getEdgeAt(index));
	}

	//TODO
}
