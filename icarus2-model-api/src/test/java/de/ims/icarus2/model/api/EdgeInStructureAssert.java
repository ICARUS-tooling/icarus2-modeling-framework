/**
 *
 */
package de.ims.icarus2.model.api;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.structure.Structure;

/**
 * @author Markus Gärtner
 *
 */
public class EdgeInStructureAssert extends AbstractEdgeAssert<EdgeInStructureAssert> {

	private final Structure structure;

	public EdgeInStructureAssert(Structure structure, Edge actual) {
		super(actual, EdgeInStructureAssert.class);
		this.structure = requireNonNull(structure);
	}

	public ItemInStructureAssert source() {
		sourceIsNotNull();
		return new ItemInStructureAssert(structure, actual.getSource());
	}

	public ItemInStructureAssert target() {
		targetIsNotNull();
		return new ItemInStructureAssert(structure, actual.getTarget());
	}

	//TODO
}
