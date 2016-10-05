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

import static de.ims.icarus2.util.Conditions.checkNotNull;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.standard.members.MemberFlags;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.Recyclable;
import de.ims.icarus2.util.mem.HeapMember;
import de.ims.icarus2.util.mem.Primitive;
import de.ims.icarus2.util.mem.Reference;
import de.ims.icarus2.util.mem.ReferenceType;

/**
 * An abstract base implementation for common {@link Item} functionality.
 * The only fields defined in this class are a pointer to the host
 * {@link Container}, the {@link #getIndex() index} value and an integer
 * bit storage for flags.
 * <p>
 * Subclasses will only have to implement the {@link #getMemberType()} method
 * to return the appropriate type and the two offset related methods.
 *
 * @author Markus Gärtner
 *
 */
@HeapMember
public abstract class AbstractItem implements Item, Recyclable {

	@Primitive
	protected long index = NO_INDEX;
	@Reference(ReferenceType.UPLINK)
	protected Container container;

	@Primitive
	protected int flags = MemberFlags.EMPTY_FLAGS;

	protected AbstractItem() {
		// no-op
	}

	protected AbstractItem(Container host) {
		setContainer(host);
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		index = NO_INDEX;
		container = null;
		flags = MemberFlags.EMPTY_FLAGS;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return true;
	}

	/**
	 * @param container the container to set
	 */
	public void setContainer(Container container) {
		checkNotNull(container);
		this.container = container;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.CorpusMember#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return getContainer().getCorpus();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getContainer()
	 */
	@Override
	public Container getContainer() {
		return container;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getLayer()
	 */
	@Override
	public ItemLayer getLayer() {
		return getContainer().getLayer();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ModelUtils.toString(this);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getIndex()
	 */
	@Override
	public long getIndex() {
		return index;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#setIndex(long)
	 */
	@Override
	public void setIndex(long newIndex) {
		if(newIndex==index) {
			return;
		}

		if(newIndex<NO_INDEX)
			throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS, "Index must be greater or equal to -1: "+newIndex); //$NON-NLS-1$

		this.index = newIndex;
	}

	@Override
	public boolean isAlive() {
		return MemberFlags.isItemAlive(flags);
	}

	@Override
	public boolean isLocked() {
		return MemberFlags.isItemLocked(flags);
	}

	@Override
	public boolean isDirty() {
		return MemberFlags.isItemDirty(flags);
	}

	public void setAlive(boolean alive) {
		flags = MemberFlags.setItemAlive(flags, alive);
	}

	public void setLocked(boolean locked) {
		flags = MemberFlags.setItemLocked(flags, locked);
	}

	public void setDirty(boolean dirty) {
		flags = MemberFlags.setItemDirty(flags, dirty);
	}
}
