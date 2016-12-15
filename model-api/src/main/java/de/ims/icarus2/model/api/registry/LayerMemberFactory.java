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
}
