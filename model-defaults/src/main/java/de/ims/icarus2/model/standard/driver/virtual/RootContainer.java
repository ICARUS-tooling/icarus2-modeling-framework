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
package de.ims.icarus2.model.standard.driver.virtual;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.ItemList;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.standard.members.container.AbstractImmutableContainer;
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
		return UNSET_LONG;
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
	 * Returns {@link ModelConstants#UNSET_LONG -1}.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#getIndex()
	 */
	@Override
	public long getIndex() {
		return UNSET_LONG;
	}

	/**
	 * Returns {@link ModelConstants#UNSET_LONG -1}.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#getBeginOffset()
	 */
	@Override
	public long getBeginOffset() {
		return UNSET_LONG;
	}

	/**
	 * Returns {@link ModelConstants#UNSET_LONG -1}.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#getEndOffset()
	 */
	@Override
	public long getEndOffset() {
		return UNSET_LONG;
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
	 * Delegates to the surrounding layer's model by invoking its {@link ItemLayerManifest#getRootContainerManifest()}
	 * method.
	 *
	 * @see de.ims.icarus2.model.api.members.container.Container#getManifest()
	 */
	@Override
	public ContainerManifest getManifest() {
		return getLayer().getManifest().getRootContainerManifest();
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
	public void moveItem(long index0, long index1) {
		getItems().moveItem(index0, index1);
	}
}
