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
package de.ims.icarus2.model.standard.driver.virtual;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.manager.ItemList;
import de.ims.icarus2.model.manifest.api.ContainerManifestBase;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.standard.members.container.AbstractImmutableContainer;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.seq.DataSequence;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 * Implements a container suitable for being a layer's root container.
 * It does not manage the storage of items directly, but rather uses an
 * implementation of {@link ItemList} (which ought to be provided at
 * construction time) to delegate all mutation and lookup operations to.
 *
 * @author Markus Gärtner
 *
 */
public class RootContainer extends AbstractImmutableContainer {

	private final Supplier<? extends ItemList> supplier;
	private volatile ItemList items;

	private final ItemLayer layer;

	public RootContainer(ItemLayer layer, Supplier<? extends ItemList> supplier) {
		requireNonNull(layer);
		requireNonNull(supplier);

		this.layer = layer;
		this.supplier = supplier;
	}

	public ItemList getItems() {
		if(items==null) {
			synchronized (supplier) {
				if(items==null) {
					items = supplier.get();
				}
			}
		}
		return items;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getId()
	 */
	@Override
	public long getId() {
		return IcarusUtils.UNSET_LONG;
	}

	/**
	 * Always returns {@code null}.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#getContainer()
	 */
	@Override
	public Container getContainer() {
		return null;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getLayer()
	 */
	@Override
	public ItemLayer getLayer() {
		return layer;
	}

	/**
	 * Returns {@link IcarusUtils#UNSET_LONG -1}.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#getIndex()
	 */
	@Override
	public long getIndex() {
		return IcarusUtils.UNSET_LONG;
	}

	/**
	 * Returns {@link IcarusUtils#UNSET_LONG -1}.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#getBeginOffset()
	 */
	@Override
	public long getBeginOffset() {
		return IcarusUtils.UNSET_LONG;
	}

	/**
	 * Returns {@link IcarusUtils#UNSET_LONG -1}.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#getEndOffset()
	 */
	@Override
	public long getEndOffset() {
		return IcarusUtils.UNSET_LONG;
	}

	/**
	 * Always returns {@code true}.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#isAlive()
	 */
	@Override
	public boolean isAlive() {
		return true;
	}

	/**
	 * Always returns {@code false}.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#isLocked()
	 */
	@Override
	public boolean isLocked() {
		return false;
	}

	/**
	 * Always returns {@code false}.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return false;
	}

	/**
	 * Delegates to the surrounding layer's {@link Layer#getCorpus()} method.
	 *
	 * @see de.ims.icarus2.model.api.members.CorpusMember#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return getLayer().getCorpus();
	}

	/**
	 * Returns the {@code ContainerType} declared in the manifest returned
	 * by {@link #getManifest()} (usually this will be  {@link ContainerType#LIST}).
	 *
	 * @see de.ims.icarus2.model.api.members.container.Container#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return getManifest().getContainerType();
	}

	/**
	 * Delegates to the surrounding layer's model by invoking its {@link ItemLayerManifestBase<?>#getRootContainerManifest()}
	 * method.
	 *
	 * @see de.ims.icarus2.model.api.members.container.Container#getManifest()
	 */
	@Override
	public ContainerManifestBase<?> getManifest() {
		return getLayer().getManifest().getRootContainerManifest().get();
	}

	/**
	 * Always returns the empty {@code DataSet}.
	 *
	 * @see de.ims.icarus2.model.api.members.container.Container#getBaseContainers()
	 */
	@Override
	public DataSet<Container> getBaseContainers() {
		return DataSet.emptySet();
	}

	/**
	 * Always returns {@code null}.
	 *
	 * @see de.ims.icarus2.model.api.members.container.Container#getBoundaryContainer()
	 */
	@Override
	public Container getBoundaryContainer() {
		return null;
	}

	/**
	 * Always returns {@code true}.
	 *
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
		return getItems().getItemCount();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#getItemAt(long)
	 */
	@Override
	public Item getItemAt(long index) {
		return getItems().getItemAt(index);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#indexOfItem(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long indexOfItem(Item item) {
		return getItems().indexOfItem(item);
	}

	public DataSequence<? extends Item> clear() {
		if(items==null) {
			return null;
		}

		return getItems().removeAllItems();
	}

	@Override
	public void addItem(long index, Item item) {
		getItems().addItem(index, item);
	}

	@Override
	public void addItems(long index, DataSequence<? extends Item> items) {
		getItems().addItems(index, items);
	}

	@Override
	public Item removeItem(long index) {
		return getItems().removeItem(index);
	}

	@Override
	public DataSequence<? extends Item> removeItems(long index0, long index1) {
		return getItems().removeItems(index0, index1);
	}

	@Override
	public void swapItems(long index0, long index1) {
		getItems().swapItems(index0, index1);
	}
}
