/**
 *
 */
package de.ims.icarus2.model.standard.members.structure;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.standard.members.item.FixedItem;

/**
 * @author Markus Gärtner
 *
 */
public class FixedEdge extends FixedItem implements Edge {

	private final Item source, target;

	public FixedEdge(Structure structure, long id, Item source, Item target) {
		super(structure, id);

		this.source = requireNonNull(source);
		this.target = requireNonNull(target);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Edge#getStructure()
	 */
	@Override
	public Structure getStructure() { return (Structure) getContainer(); }

	/**
	 * @see de.ims.icarus2.model.api.members.item.Edge#getSource()
	 */
	@Override
	public Item getSource() { return source; }

	/**
	 * @see de.ims.icarus2.model.api.members.item.Edge#getTarget()
	 */
	@Override
	public Item getTarget() { return target; }

	/**
	 * @see de.ims.icarus2.model.api.members.item.Edge#setSource(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public void setSource(Item item) {
		throw new UnsupportedOperationException("Immutable edge");
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Edge#setTarget(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public void setTarget(Item item) {
		throw new UnsupportedOperationException("Immutable edge");
	}
}
