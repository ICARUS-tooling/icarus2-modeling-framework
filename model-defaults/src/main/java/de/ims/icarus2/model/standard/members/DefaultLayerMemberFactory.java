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
package de.ims.icarus2.model.standard.members;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.registry.LayerMemberFactory;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.standard.members.container.DefaultContainer;
import de.ims.icarus2.model.standard.members.container.ItemStorage;
import de.ims.icarus2.model.standard.members.item.DefaultItem;
import de.ims.icarus2.model.standard.members.structure.DefaultEdge;
import de.ims.icarus2.model.standard.members.structure.DefaultStructure;
import de.ims.icarus2.model.standard.members.structure.EdgeStorage;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultLayerMemberFactory implements LayerMemberFactory {

	/**
	 * @see de.ims.icarus2.model.api.registry.LayerMemberFactory#newItem(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public Item newItem(Container host) {
		return new DefaultItem(host);
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.LayerMemberFactory#newEdge(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public Edge newEdge(Structure host) {
		return new DefaultEdge(host);
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.LayerMemberFactory#newContainer(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.manifest.api.ContainerManifest)
	 */
	@Override
	public Container newContainer(Container host, ContainerManifest manifest) {
		ItemStorage itemStorage = createItemStorage(manifest);

		return new DefaultContainer(host, itemStorage);
	}

	protected ItemStorage createItemStorage(ContainerManifest manifest) {
		throw new ModelException(GlobalErrorCode.NOT_IMPLEMENTED, "TODO");
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.LayerMemberFactory#newStructure(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.manifest.api.StructureManifest)
	 */
	@Override
	public Structure newStructure(Container host, StructureManifest manifest) {
		ItemStorage itemStorage = createItemStorage(manifest);
		EdgeStorage edgeStorage = createEdgeStorage(manifest);

		return new DefaultStructure(host, itemStorage, edgeStorage);
	}

	protected EdgeStorage createEdgeStorage(StructureManifest manifest) {
		throw new ModelException(GlobalErrorCode.NOT_IMPLEMENTED, "TODO");
	}
}
