/**
 *
 */
package de.ims.icarus2.model.standard.members.item;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;

/**
 * @author Markus Gärtner
 *
 */
public class FixedItem implements Item {

	private final Container container;
	private final long id;

	public FixedItem(Container container, long id) {
		this.container = requireNonNull(container);
		this.id = id;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.ITEM;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getContainer()
	 */
	@Override
	public Container getContainer() {
		return container;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getIndex()
	 */
	@Override
	public long getIndex() { return id; }

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getId()
	 */
	@Override
	public long getId() { return id; }

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#isAlive()
	 */
	@Override
	public boolean isAlive() { return true; }

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#isLocked()
	 */
	@Override
	public boolean isLocked() { return false; }

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#isDirty()
	 */
	@Override
	public boolean isDirty() { return false; }

}
