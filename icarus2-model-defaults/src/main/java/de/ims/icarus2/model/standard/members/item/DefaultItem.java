/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import de.ims.icarus2.util.IcarusUtils;
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
	private long id = IcarusUtils.UNSET_LONG;

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
		id = IcarusUtils.UNSET_LONG;
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
		checkState("Id already set", this.id==IcarusUtils.UNSET_LONG);

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
