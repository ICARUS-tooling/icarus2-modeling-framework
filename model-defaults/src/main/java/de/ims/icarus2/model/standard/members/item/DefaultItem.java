/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 */
package de.ims.icarus2.model.standard.members.item;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.standard.members.AbstractMember;
import de.ims.icarus2.model.standard.members.MemberFlags;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.Recyclable;
import de.ims.icarus2.util.mem.Assessable;
import de.ims.icarus2.util.mem.Primitive;
import de.ims.icarus2.util.mem.Reference;
import de.ims.icarus2.util.mem.ReferenceType;

/**
 * An abstract base implementation for common {@link Item} functionality.
 * The only fields defined in this class are an {@link #getIndex() index}
 * value and an integer bit storage for flags.
 * <p>
 * Subclasses will only have to implement the {@link #getMemberType()} method
 * to return the appropriate type and the two offset related methods, plus
 * decide how to link to the host container or layer.
 *
 * @author Markus Gärtner
 *
 */
@Assessable
public class DefaultItem extends AbstractMember implements Item, Item.ManagedItem, Recyclable {

	@Primitive
	private long id = UNSET_LONG;

	@Reference(ReferenceType.UPLINK)
	private Container container;

	public DefaultItem() {
		// no-op
	}

	public DefaultItem(Container container, long id) {
		setContainer(container);
		setId(id);
	}

	public DefaultItem(long id) {
		setId(id);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getId()
	 */
	@Override
	public long getId() {
		return id;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		super.recycle();
		id = UNSET_LONG;
		container = null;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getContainer()
	 */
	@Override
	public Container getContainer() {
		return container;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ModelUtils.toString(this);
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return getIndex()>-1 && getContainer()!=null;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.ITEM;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getIndex()
	 */
	@Override
	public long getIndex() {
		return getLayer().getIdManager().indexOfId(getId());
	}

	@Override
	public boolean isAlive() {
		return isFlagSet(MemberFlags.ITEM_ALIVE);
	}

	@Override
	public boolean isLocked() {
		return isFlagSet(MemberFlags.ITEM_LOCKED);
	}

	@Override
	public boolean isDirty() {
		return isFlagSet(MemberFlags.ITEM_DIRTY);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#isUsable()
	 */
	@Override
	public boolean isUsable() {
		return MemberFlags.isItemUsable(getFlags());
	}

	@Override
	public void setAlive(boolean alive) {
		setFlag(MemberFlags.ITEM_ALIVE, alive);
	}

	@Override
	public void setLocked(boolean locked) {
		setFlag(MemberFlags.ITEM_LOCKED, locked);
	}

	@Override
	public void setDirty(boolean dirty) {
		setFlag(MemberFlags.ITEM_DIRTY, dirty);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item.ManagedItem#setId(long)
	 */
	@Override
	public void setId(long id) {
		checkState("Id already set", this.id==UNSET_LONG);

		this.id = id;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item.ManagedItem#setContainer(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public void setContainer(Container container) {
		requireNonNull(container);

		this.container = container;
	}
}
