/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 457 $
 * $Date: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/driver/virtual/RootContainer.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.driver.virtual;

import static de.ims.icarus2.model.util.Conditions.checkNotNull;

import java.util.function.Supplier;

import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.manifest.ContainerManifest;
import de.ims.icarus2.model.api.manifest.ContainerType;
import de.ims.icarus2.model.api.manifest.ItemLayerManifest;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.ItemList;
import de.ims.icarus2.model.standard.members.container.AbstractImmutableContainer;
import de.ims.icarus2.model.util.DataSequence;
import de.ims.icarus2.model.util.DataSet;

/**
 * Implements a container suitable for being a layer's root container.
 * It does not manage the storage of items directly, but rather uses an
 * implementation of {@link ItemList} (which ought to be provided at
 * construction time) to delegate all mutation and lookup operations to.
 *
 * @author Markus Gärtner
 * @version $Id: RootContainer.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
public class RootContainer extends AbstractImmutableContainer {

	private final Supplier<? extends ItemList> supplier;
	private volatile ItemList items;

	private final ItemLayer layer;

	public RootContainer(ItemLayer layer, Supplier<? extends ItemList> supplier) {
		checkNotNull(layer);
		checkNotNull(supplier);

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
	 * Returns {@link ModelConstants#NO_INDEX -1}.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#getIndex()
	 */
	@Override
	public long getIndex() {
		return NO_INDEX;
	}

	/**
	 * Returns {@link ModelConstants#NO_INDEX -1}.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#getBeginOffset()
	 */
	@Override
	public long getBeginOffset() {
		return NO_INDEX;
	}

	/**
	 * Returns {@link ModelConstants#NO_INDEX -1}.
	 *
	 * @see de.ims.icarus2.model.api.members.item.Item#getEndOffset()
	 */
	@Override
	public long getEndOffset() {
		return NO_INDEX;
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
	 * Returns {@link ContainerType#LIST}.
	 *
	 * @see de.ims.icarus2.model.api.members.container.Container#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return ContainerType.LIST;
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
