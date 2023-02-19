/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.api.registry.LayerMemberFactory;
import de.ims.icarus2.model.standard.members.item.FixedFragment;
import de.ims.icarus2.model.standard.members.item.FixedItem;
import de.ims.icarus2.model.standard.members.structure.FixedEdge;

/**
 * An extension of the {@link DefaultLayerMemberFactory} implementation with
 * a change on the lowest level of layer members being created. This implementation
 * only creates "fixed" versions of the {@link Item}, {@link Edge} and {@link Fragment}
 * members.
 *
 * @author Markus Gärtner
 *
 */
public class FixedLayerMemberFactory extends DefaultLayerMemberFactory {

	public static final FixedLayerMemberFactory INSTANCE = new FixedLayerMemberFactory();

	/**
	 * @see de.ims.icarus2.model.standard.members.DefaultLayerMemberFactory#newItem(de.ims.icarus2.model.api.members.container.Container, long)
	 */
	@Override
	public Item newItem(Container host, long id) {
		return new FixedItem(host, id);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.DefaultLayerMemberFactory#newFragment(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public Fragment newFragment(Container host, long id, Item item) {
		throw new UnsupportedOperationException("Cannot create incomplete fixed fragment");
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.LayerMemberFactory#newFragment(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.raster.Position, de.ims.icarus2.model.api.raster.Position)
	 */
	@Override
	public Fragment newFragment(Container host, long id, Item item, Position begin, Position end) {
		return new FixedFragment(host, id, item, begin, end);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.DefaultLayerMemberFactory#newEdge(de.ims.icarus2.model.api.members.structure.Structure, long)
	 */
	@Override
	public Edge newEdge(Structure host, long id) {
		throw new UnsupportedOperationException("Cannot create incomplete fixed edge");
	}

	/**
	 * Note that contrary to the original {@link LayerMemberFactory#newEdge(Structure, long, Item, Item)}
	 * method this implementation does <b>not</b> allow the {@code source} or {@code target} terminal
	 * to be {@code null}!
	 *
	 * @see de.ims.icarus2.model.api.registry.LayerMemberFactory#newEdge(de.ims.icarus2.model.api.members.structure.Structure, long, de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public Edge newEdge(Structure host, long id, Item source, Item target) {
		return new FixedEdge(host, id, source, target);
	}
}
