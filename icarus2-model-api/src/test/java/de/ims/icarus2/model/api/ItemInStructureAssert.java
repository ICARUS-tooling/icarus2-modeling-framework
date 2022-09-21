/**
 *
 */
package de.ims.icarus2.model.api;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;

/**
 * @author Markus Gärtner
 *
 */
public class ItemInStructureAssert extends AbstractItemAssert<ItemInStructureAssert, Item> {

	private final Structure structure;

	public ItemInStructureAssert(Structure structure, Item actual) {
		super(actual, ItemInStructureAssert.class);
		this.structure = requireNonNull(structure);
	}

	//TODO
}
