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
package de.ims.icarus2.model.standard.members.structure;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.standard.members.item.DefaultItem;
import de.ims.icarus2.util.Recyclable;
import de.ims.icarus2.util.mem.Assessable;
import de.ims.icarus2.util.mem.Reference;

/**
 * @author Markus Gärtner
 *
 */
@Assessable
public class DefaultEdge extends DefaultItem implements Edge, Recyclable {

	@Reference
	private Item source;
	@Reference
	private Item target;

	public DefaultEdge() {
		// no-op
	}

	public DefaultEdge(Structure structure) {
		setStructure(structure);
	}

	public DefaultEdge(Structure structure, Item source, Item target) {
		setStructure(structure);
		setSource(source);
		setTarget(target);
	}

	@Override
	public Structure getStructure() {
		return getContainer();
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.item.DefaultItem#getContainer()
	 */
	@Override
	public Structure getContainer() {
		return (Structure) super.getContainer();
	}

	@Override
	public Item getTerminal(boolean isSource) {
		return isSource ? source : target;
	}

	@Override
	public boolean isLoop() {
		return source!=null && source==target;
	}

	@Override
	public void setTerminal(Item item, boolean isSource) {
		if(isSource) {
			source = item;
		} else {
			target = item;
		}
	}

	@Override
	public void setLocked(boolean locked) {
		// no-op
	}

	@Override
	public void setId(long id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setContainer(Container container) {
		super.setContainer((Structure)container);
	}

	public void setStructure(Structure structure) {
		setContainer(structure);
	}

	/**
	 * @param source the source to set
	 */
	@Override
	public void setSource(Item source) {
		requireNonNull(source);
		this.source = source;
	}

	/**
	 * @param target the target to set
	 */
	@Override
	public void setTarget(Item target) {
		requireNonNull(target);
		this.target = target;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getBeginOffset()
	 */
	@Override
	public long getBeginOffset() {
		return (source==null || target==null) ? UNSET_LONG : Math.min(source.getBeginOffset(), target.getBeginOffset());
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getEndOffset()
	 */
	@Override
	public long getEndOffset() {
		return (source==null || target==null) ? UNSET_LONG : Math.min(source.getEndOffset(), target.getEndOffset());
	}

	/**
	 * @see de.ims.icarus2.model.api.members.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.EDGE;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Edge#getSource()
	 */
	@Override
	public Item getSource() {
		return source;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Edge#getTarget()
	 */
	@Override
	public Item getTarget() {
		return target;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getIndex()
	 */
	@Override
	public long getIndex() {
		return UNSET_LONG;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#isAlive()
	 */
	@Override
	public boolean isAlive() {
		return super.isAlive() && source!=null && target!=null;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#isLocked()
	 */
	@Override
	public boolean isLocked() {
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return super.isDirty() || source==null || target==null;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		super.recycle();
		source = target = null;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return super.revive() && source!=null && target!=null;
	}
}
