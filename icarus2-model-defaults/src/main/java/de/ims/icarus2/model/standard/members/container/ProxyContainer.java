/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.container;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 * @author Markus Gärtner
 *
 */
public class ProxyContainer extends AbstractImmutableContainer {

	private final ItemLayer layer;

	/**
	 * @param layer
	 */
	public ProxyContainer(ItemLayer layer) {
		requireNonNull(layer);

		this.layer = layer;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getLayer()
	 */
	@Override
	public ItemLayer getLayer() {
		return layer;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#isProxy()
	 */
	@Override
	public final boolean isProxy() {
		return true;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return ContainerType.PROXY;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#getManifest()
	 */
	@Override
	public ContainerManifest getManifest() {
		return null;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#getBaseContainers()
	 */
	@Override
	public DataSet<Container> getBaseContainers() {
		return DataSet.emptySet();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#getBoundaryContainer()
	 */
	@Override
	public Container getBoundaryContainer() {
		return null;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#isItemsComplete()
	 */
	@Override
	public boolean isItemsComplete() {
		return true;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#getItemCount()
	 */
	@Override
	public long getItemCount() {
		return 0;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#getItemAt(long)
	 */
	@Override
	public Item getItemAt(long index) {
		throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Immutable proxy");
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#indexOfItem(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long indexOfItem(Item item) {
		throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Immutable proxy");
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getContainer()
	 */
	@Override
	public Container getContainer() {
		throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Immutable proxy");
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getIndex()
	 */
	@Override
	public long getIndex() {
		return IcarusUtils.UNSET_LONG;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getId()
	 */
	@Override
	public long getId() {
		return IcarusUtils.UNSET_LONG;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getBeginOffset()
	 */
	@Override
	public long getBeginOffset() {
		return IcarusUtils.UNSET_LONG;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getEndOffset()
	 */
	@Override
	public long getEndOffset() {
		return IcarusUtils.UNSET_LONG;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#isAlive()
	 */
	@Override
	public boolean isAlive() {
		return true;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#isLocked()
	 */
	@Override
	public boolean isLocked() {
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.CorpusMember#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return getLayer().getCorpus();
	}

}
