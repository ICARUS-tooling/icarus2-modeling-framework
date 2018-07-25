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
package de.ims.icarus2.model.api.registry;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.api.raster.Rasterizer;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.StructureManifest;

/**
 * A factory for creating {@link Item items}, {@link Edge edges} and
 * various {@link Container containers} to host those elements.
 * <p>
 * Note that the creation of {@link Position} instances to address
 * boundaries of {@link Fragment fragments} is <b>not</i> part of this
 * factory since they are covered by the appropriate {@link Rasterizer}.
 *
 * @author Markus Gärtner
 *
 */
public interface LayerMemberFactory {

	/**
	 * Creates a new general container that is linked to the given {@code host} container
	 * using the given {@code manifest} as hint for optimizing the implementation.
	 * If the {@code host} is a {@link Container#isProxy() proxy} container than the
	 * returned type will be an implementation suitable for being a {@link Item#isTopLevel() top-level}
	 * member.
	 *
	 * @param manifest
	 * @param host
	 * @param id
	 * @return
	 */
	Container newContainer(ContainerManifest manifest, Container host, long id);

	/**
	 * Creates a new general structure that is linked to the given {@code host} container
	 * using the given {@code manifest} as hint for optimizing the implementation.
	 * If the {@code host} is a {@link Container#isProxy() proxy} container than the
	 * returned type will be an implementation suitable for being a {@link Item#isTopLevel() top-level}
	 * member.
	 *
	 * @param manifest
	 * @param host
	 * @param id
	 * @return
	 */
	Structure newStructure(StructureManifest manifest, Container host, long id);

	/**
	 * Creates a new general item that is linked to the given {@code host} container.
	 * If the {@code host} is a {@link Container#isProxy() proxy} container than the
	 * returned type will be an implementation suitable for being a {@link Item#isTopLevel() top-level}
	 * member.
	 *
	 * @param host
	 * @param id
	 * @return
	 */
	Item newItem(Container host, long id);

	/**
	 * Creates a new edge that is linked to the given {@code host} structure.
	 *
	 * @param host
	 * @param id
	 * @return
	 */
	Edge newEdge(Structure host, long id);

	default Edge newEdge(Structure host, long id, Item source, Item target) {
		Edge edge = newEdge(host, id);
		edge.setSource(source);
		edge.setTarget(target);
		return edge;
	}

	/**
	 * Creates a new fragment that is linked to the specified {@code host} container.
	 *
	 * @param host
	 * @param begin
	 * @param end
	 * @return
	 */
	Fragment newFragment(Container host, long id, Item item);

	default Fragment newFragment(Container host, long id, Item item, Position begin, Position end) {
		Fragment fragment = newFragment(host, id, item);
		fragment.setFragmentBegin(begin);
		fragment.setFragmentEnd(end);
		return fragment;
	}
}
