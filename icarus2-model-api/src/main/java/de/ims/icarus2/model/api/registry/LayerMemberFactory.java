/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import javax.annotation.Nullable;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.api.raster.Rasterizer;
import de.ims.icarus2.model.manifest.api.ContainerManifestBase;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 * A factory for creating {@link Item items}, {@link Edge edges} and
 * various {@link Container containers} to host those elements.
 * <p>
 * Note that the creation of {@link Position} instances to address
 * boundaries of {@link Fragment fragments} is <b>not</i> part of this
 * factory since they are covered by the appropriate {@link Rasterizer} implementation!
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
	 * @param manifest manifest describing the container to be created
	 * @param host container that should own the new container
	 * @param baseContainers optional set of containers the newly created
	 * container should rely on
	 * @param boundaryContainer optional container that provides the ultimate
	 * bounds for any legal item that can be part of the newly created container
	 * @param id {@link Item#getId() id} of the new container
	 * @return a new container suitable for the specification from the given manifest
	 */
	Container newContainer(ContainerManifestBase<?> manifest, Container host,
			@Nullable DataSet<Container> baseContainers,
			@Nullable Container boundaryContainer, long id);

	/**
	 * Creates a new general structure that is linked to the given {@code host} container
	 * using the given {@code manifest} as hint for optimizing the implementation.
	 * If the {@code host} is a {@link Container#isProxy() proxy} container than the
	 * returned type will be an implementation suitable for being a {@link Item#isTopLevel() top-level}
	 * member.
	 *
	 * @param manifest manifest describing the structure to be created
	 * @param host container that should own the new structure
	 * @param baseContainers optional set of containers the newly created
	 * structure should rely on (for recruiting nodes)
	 * @param boundaryContainer optional container that provides the ultimate
	 * bounds for any legal item that can be part of the newly created structure
	 * @param id {@link Item#getId() id} of the new structure
	 * @return a new structure suitable for the specification from the given manifest
	 */
	Structure newStructure(StructureManifest manifest, Container host,
			@Nullable DataSet<Container> baseContainers,
			@Nullable Container boundaryContainer, long id);

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

	default Edge newEdge(Structure host, long id, @Nullable Item source, @Nullable Item target) {
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
