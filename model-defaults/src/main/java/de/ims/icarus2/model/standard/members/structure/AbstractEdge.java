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

import static de.ims.icarus2.util.Conditions.checkNotNull;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.Recyclable;
import de.ims.icarus2.util.mem.HeapMember;
import de.ims.icarus2.util.mem.Reference;

/**
 * @author Markus Gärtner
 *
 */
@HeapMember
public abstract class AbstractEdge implements Edge, Recyclable {

	@Reference
	private Item source;
	@Reference
	private Item target;


	/**
	 * @param source the source to set
	 */
	@Override
	public void setSource(Item source) {
		checkNotNull(source);
		this.source = source;
	}

	/**
	 * @param target the target to set
	 */
	@Override
	public void setTarget(Item target) {
		checkNotNull(target);
		this.target = target;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getBeginOffset()
	 */
	@Override
	public long getBeginOffset() {
		return source==null ? NO_INDEX : source.getBeginOffset();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getEndOffset()
	 */
	@Override
	public long getEndOffset() {
		return target==null ? NO_INDEX : target.getBeginOffset();
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
	 * @see de.ims.icarus2.model.api.members.item.Item#getLayer()
	 */
	@Override
	public ItemLayer getLayer() {
		return getStructure().getLayer();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.CorpusMember#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return getStructure().getCorpus();
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
		return NO_INDEX;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#setIndex(long)
	 */
	@Override
	public void setIndex(long newIndex) {
		throw new UnsupportedOperationException("Edges cannot have index values assigned"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#isAlive()
	 */
	@Override
	public boolean isAlive() {
		return source!=null && target!=null;
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
		return source==null || target==null;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		source = target = null;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return source!=null && target!=null;
	}
}
