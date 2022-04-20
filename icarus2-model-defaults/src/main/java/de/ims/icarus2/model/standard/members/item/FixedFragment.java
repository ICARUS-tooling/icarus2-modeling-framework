/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.model.standard.members.item;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.layer.FragmentLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.raster.Position;

/**
 * @author Markus Gärtner
 *
 */
public class FixedFragment extends FixedItem implements Fragment {

	private final Item item;
	private final Position begin, end;

	/**
	 * @param container
	 * @param id
	 */
	public FixedFragment(Container container, long id, Item item, Position begin, Position end) {
		super(container, id);

		this.item = requireNonNull(item);
		this.begin = requireNonNull(begin);
		this.end = requireNonNull(end);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Fragment#getLayer()
	 */
	@Override
	public FragmentLayer getLayer() {
		return (FragmentLayer) getContainer().getLayer();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Fragment#getItem()
	 */
	@Override
	public Item getItem() { return item; }

	/**
	 * @see de.ims.icarus2.model.api.members.item.Fragment#getFragmentBegin()
	 */
	@Override
	public Position getFragmentBegin() { return begin; }

	/**
	 * @see de.ims.icarus2.model.api.members.item.Fragment#getFragmentEnd()
	 */
	@Override
	public Position getFragmentEnd() { return end; }

	/**
	 * @see de.ims.icarus2.model.api.members.item.Fragment#setFragmentBegin(de.ims.icarus2.model.api.raster.Position)
	 */
	@Override
	public void setFragmentBegin(Position position) {
		throw new UnsupportedOperationException("Immutable fragement");
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Fragment#setFragmentEnd(de.ims.icarus2.model.api.raster.Position)
	 */
	@Override
	public void setFragmentEnd(Position position) {
		throw new UnsupportedOperationException("Immutable fragement");
	}

}
