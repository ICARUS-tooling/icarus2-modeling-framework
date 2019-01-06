/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.registry.LayerMemberFactory;
import de.ims.icarus2.model.manifest.api.ContainerManifestBase;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.standard.driver.cache.TrackedMember.TrackedContainer;
import de.ims.icarus2.model.standard.driver.cache.TrackedMember.TrackedItem;
import de.ims.icarus2.model.standard.driver.cache.TrackedMember.TrackedStructure;
import de.ims.icarus2.model.standard.members.container.DefaultContainer;
import de.ims.icarus2.model.standard.members.container.ItemStorage;
import de.ims.icarus2.model.standard.members.item.DefaultFragment;
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
	public Item newItem(Container host, long id) {
		DefaultItem item = host.isProxy() ? new TrackedItem() : new DefaultItem();

		item.setContainer(host);
		item.setId(id);

		return item;
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.LayerMemberFactory#newEdge(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public Edge newEdge(Structure host, long id) {
		//FIXME ignores id
		return new DefaultEdge(host);
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.LayerMemberFactory#newFragment(de.ims.icarus2.model.api.members.container.Container, long)
	 */
	@Override
	public Fragment newFragment(Container host, long id, Item item) {
		DefaultFragment fragment = new DefaultFragment();

		fragment.setItem(item);
		fragment.setContainer(host);
		fragment.setId(id);

		return fragment;
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.LayerMemberFactory#newContainer(ContainerManifestBase, Container, long)
	 */
	@Override
	public Container newContainer(ContainerManifestBase<?> manifest, Container host, long id) {
		ItemStorage itemStorage = createItemStorage(manifest);

		DefaultContainer container = host.isProxy() ? new TrackedContainer() : new DefaultContainer();
		container.setId(id);
		container.setContainer(host);
		container.setItemStorage(itemStorage);

		return container;
	}

	protected ItemStorage createItemStorage(ContainerManifestBase<?> manifest) {
		//TODO implement
		throw new ModelException(GlobalErrorCode.NOT_IMPLEMENTED, "TODO");
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.LayerMemberFactory#newStructure(de.ims.icarus2.model.manifest.api.StructureManifest, de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public Structure newStructure(StructureManifest manifest, Container host, long id) {
		ItemStorage itemStorage = createItemStorage(manifest);
		EdgeStorage edgeStorage = createEdgeStorage(manifest);

		DefaultStructure structure = host.isProxy() ? new TrackedStructure() : new DefaultStructure();
		structure.setId(id);
		structure.setContainer(host);
		structure.setItemStorage(itemStorage);
		structure.setEdgeStorage(edgeStorage);

		return structure;
	}

	protected EdgeStorage createEdgeStorage(StructureManifest manifest) {
		//TODO implement
		throw new ModelException(GlobalErrorCode.NOT_IMPLEMENTED, "TODO");
	}
}
