/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import de.ims.icarus2.model.api.layer.FragmentLayer;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.mem.Assessable;
import de.ims.icarus2.util.mem.Reference;
import de.ims.icarus2.util.mem.ReferenceType;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(Fragment.class)
@Assessable
public class DefaultFragment extends DefaultItem implements Fragment {

	@Reference(ReferenceType.UPLINK)
	private Item item;
	@Reference(ReferenceType.DOWNLINK)
	private Position fragmentBegin;
	@Reference(ReferenceType.DOWNLINK)
	private Position fragmentEnd;

	public DefaultFragment() {
		// no-op
	}

	public DefaultFragment(Container container) {
		super(container);
	}

	public DefaultFragment(Container container, long id) {
		super(container, id);
	}

	public DefaultFragment(Container container, long id, Item item) {
		this(container, id);

		setItem(item);
	}

	public DefaultFragment(Container container, long id, Item item,
			Position fragmentBegin, Position fragmentEnd) {
		this(container, id, item);

		requireNonNull(fragmentBegin);
		requireNonNull(fragmentEnd);

		ModelUtils.checkFragmentPositions(this, fragmentBegin, fragmentEnd);

		this.fragmentBegin = fragmentBegin;
		this.fragmentEnd = fragmentEnd;
	}

	/**
	 * @param item the item to set
	 */
	public void setItem(Item item) {
		this.item = requireNonNull(item);
	}

//	@Override
//	public boolean equals(Object obj) {
//		if(this==obj) {
//			return true;
//		} else if(obj instanceof Fragment) {
//			Fragment other = (Fragment) obj;
//			return item==other.getItem()
//					&& fragmentBegin.equals(other.getFragmentBegin())
//					&& fragmentEnd.equals(other.getFragmentEnd());
//		}
//		return false;
//	}

//	/**
//	 * @see java.lang.Object#hashCode()
//	 */
//	@Override
//	public int hashCode() {
//		return fragmentBegin.hashCode()*fragmentEnd.hashCode()+1;
//	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getBeginOffset()
	 */
	@Override
	public long getBeginOffset() {
		checkState(item!=null);
		return item.getBeginOffset();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getEndOffset()
	 */
	@Override
	public long getEndOffset() {
		checkState(item!=null);
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
		return (FragmentLayer) expectContainer().getLayer();
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
		requireNonNull(begin);
		ModelUtils.checkFragmentPositions(this, begin, fragmentEnd);

		fragmentBegin = begin;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Fragment#setFragmentEnd(de.ims.icarus2.model.api.raster.Position)
	 */
	@Override
	public void setFragmentEnd(Position end) {
		requireNonNull(end);
		ModelUtils.checkFragmentPositions(this, fragmentBegin, end);

		fragmentEnd = end;
	}

	public void setSpan(Position begin, Position end) {
		requireNonNull(begin);
		requireNonNull(end);

		ModelUtils.checkFragmentPositions(this, begin, end);

		fragmentBegin = begin;
		fragmentEnd = end;
	}
}
