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
package de.ims.icarus2.model.standard.members;

import javax.annotation.Nullable;

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
import de.ims.icarus2.model.standard.members.container.ListItemStorageInt;
import de.ims.icarus2.model.standard.members.container.SingletonItemStorage;
import de.ims.icarus2.model.standard.members.container.SpanItemStorage;
import de.ims.icarus2.model.standard.members.item.DefaultFragment;
import de.ims.icarus2.model.standard.members.item.DefaultItem;
import de.ims.icarus2.model.standard.members.structure.ChainEdgeStorage;
import de.ims.icarus2.model.standard.members.structure.DefaultEdge;
import de.ims.icarus2.model.standard.members.structure.DefaultStructure;
import de.ims.icarus2.model.standard.members.structure.EdgeStorage;
import de.ims.icarus2.model.standard.members.structure.EmptyEdgeStorage;
import de.ims.icarus2.model.standard.members.structure.GraphEdgeStorage;
import de.ims.icarus2.model.standard.members.structure.TreeEdgeStorage;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(LayerMemberFactory.class)
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

	private <C extends DefaultContainer> void applyOptionalFeatures(C container,
			DataSet<Container> baseContainers, Container boundaryContainer) {

		if(baseContainers!=null) {
			container.setBaseContainers(baseContainers);
		}

		if(boundaryContainer!=null) {
			container.setBoundaryContainer(boundaryContainer);
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.LayerMemberFactory#newContainer(ContainerManifestBase, Container, long)
	 */
	@Override
	public Container newContainer(ContainerManifestBase<?> manifest, Container host,
			@Nullable DataSet<Container> baseContainers,
			@Nullable Container boundaryContainer, long id) {
		ItemStorage itemStorage = createItemStorage(manifest);

		DefaultContainer container = host.isProxy() ? new TrackedContainer() : new DefaultContainer();
		container.setManifest(manifest);
		applyOptionalFeatures(container, baseContainers, boundaryContainer);
		container.setId(id);
		container.setContainer(host);
		container.setItemStorage(itemStorage);

		return container;
	}

	protected ItemStorage createItemStorage(ContainerManifestBase<?> manifest) {
		switch (manifest.getContainerType()) {
		case SINGLETON:
			return new SingletonItemStorage();

		case LIST:
			return new ListItemStorageInt();

		case SPAN:
			return new SpanItemStorage();

		default:
			throw new ModelException(GlobalErrorCode.UNKNOWN_ENUM,
					"Unknown container type: "+manifest.getContainerType());
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.LayerMemberFactory#newStructure(de.ims.icarus2.model.manifest.api.StructureManifest, de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public Structure newStructure(StructureManifest manifest, Container host,
			@Nullable DataSet<Container> baseContainers,
			@Nullable Container boundaryContainer, long id) {
		ItemStorage itemStorage = createItemStorage(manifest);
		EdgeStorage edgeStorage = createEdgeStorage(manifest);

		DefaultStructure structure = host.isProxy() ? new TrackedStructure() : new DefaultStructure();
		structure.setManifest(manifest);
		applyOptionalFeatures(structure, baseContainers, boundaryContainer);
		structure.setId(id);
		structure.setContainer(host);
		structure.setItemStorage(itemStorage);
		structure.setEdgeStorage(edgeStorage);

		return structure;
	}

	protected EdgeStorage createEdgeStorage(StructureManifest manifest) {
		switch (manifest.getStructureType()) {
		case CHAIN: return new ChainEdgeStorage();
		case TREE: return new TreeEdgeStorage();
		case SET: return new EmptyEdgeStorage();
		case GRAPH: return new GraphEdgeStorage();

		default:
			throw new ModelException(GlobalErrorCode.UNKNOWN_ENUM,
					"Unknown container type: "+manifest.getContainerType());
		}
	}
}
