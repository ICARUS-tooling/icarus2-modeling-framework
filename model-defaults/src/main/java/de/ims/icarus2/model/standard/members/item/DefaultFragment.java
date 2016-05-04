/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 398 $
 * $Date: 2015-05-29 11:29:49 +0200 (Fr, 29 Mai 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/item/DefaultFragment.java $
 *
 * $LastChangedDate: 2015-05-29 11:29:49 +0200 (Fr, 29 Mai 2015) $
 * $LastChangedRevision: 398 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members.item;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.FragmentLayer;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.mem.HeapMember;
import de.ims.icarus2.util.mem.Reference;
import de.ims.icarus2.util.mem.ReferenceType;

/**
 * @author Markus Gärtner
 * @version $Id: DefaultFragment.java 398 2015-05-29 09:29:49Z mcgaerty $
 *
 */
@HeapMember
public class DefaultFragment extends AbstractItem implements Fragment {

	@Reference(ReferenceType.UPLINK)
	private Item item;
	@Reference(ReferenceType.DOWNLINK)
	private Position fragmentBegin;
	@Reference(ReferenceType.DOWNLINK)
	private Position fragmentEnd;

	/**
	 * @param item the item to set
	 */
	public void setItem(Item item) {
		if (item == null)
			throw new NullPointerException("Invalid item"); //$NON-NLS-1$
		this.item = item;
	}

	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} else if(obj instanceof Fragment) {
			Fragment other = (Fragment) obj;
			return item==other.getItem()
					&& fragmentBegin.equals(other.getFragmentBegin())
					&& fragmentEnd.equals(other.getFragmentEnd());
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return fragmentBegin.hashCode()*fragmentEnd.hashCode()+1;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getBeginOffset()
	 */
	@Override
	public long getBeginOffset() {
		return item.getBeginOffset();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getEndOffset()
	 */
	@Override
	public long getEndOffset() {
		return item.getEndOffset();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.FRAGMENT;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Fragment#getItem()
	 */
	@Override
	public Item getItem() {
		return item;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.CorpusMember#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return getContainer().getCorpus();
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		super.recycle();

		item = null;
		fragmentBegin = fragmentEnd = null;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return super.revive() && item!=null
				&& fragmentBegin!=null && fragmentEnd!=null;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Fragment#getLayer()
	 */
	@Override
	public FragmentLayer getLayer() {
		return (FragmentLayer) getContainer().getLayer();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Fragment#getFragmentBegin()
	 */
	@Override
	public Position getFragmentBegin() {
		return fragmentBegin;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Fragment#getFragmentEnd()
	 */
	@Override
	public Position getFragmentEnd() {
		return fragmentEnd;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Fragment#setFragmentBegin(de.ims.icarus2.model.api.raster.Position)
	 */
	@Override
	public void setFragmentBegin(Position begin) {
		if (begin == null)
			throw new NullPointerException("Invalid begin");  //$NON-NLS-1$

		ModelUtils.checkFragmentPositions(this, begin, null);

		fragmentBegin = begin;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Fragment#setFragmentEnd(de.ims.icarus2.model.api.raster.Position)
	 */
	@Override
	public void setFragmentEnd(Position end) {
		if (end == null)
			throw new NullPointerException("Invalid end");  //$NON-NLS-1$

		fragmentEnd = end;
	}

	public void setSpan(Position begin, Position end) {
		if (begin == null)
			throw new NullPointerException("Invalid begin");  //$NON-NLS-1$
		if (end == null)
			throw new NullPointerException("Invalid end");  //$NON-NLS-1$

		ModelUtils.checkFragmentPositions(this, begin, end);

		fragmentBegin = begin;
		fragmentEnd = end;
	}
}
