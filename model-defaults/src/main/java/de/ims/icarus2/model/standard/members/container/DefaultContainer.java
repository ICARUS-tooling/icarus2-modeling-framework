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
package de.ims.icarus2.model.standard.members.container;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerFlag;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.members.MemberFlags;
import de.ims.icarus2.model.standard.members.item.AbstractItem;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.Recyclable;
import de.ims.icarus2.util.collections.seq.DataSequence;
import de.ims.icarus2.util.collections.set.DataSet;
import de.ims.icarus2.util.mem.Link;
import de.ims.icarus2.util.mem.Reference;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultContainer extends AbstractItem implements Container, Recyclable {

	@Link
	protected ItemStorage itemStorage;

	@Reference
	protected Container boundaryContainer;

	@Link
	protected DataSet<Container> baseContainers = DataSet.emptySet();

	public DefaultContainer() {
		// no-op
	}

	/**
	 * Method for accessing the internal item storage in a situation where it
	 * is expected to not be {@code null}.
	 * This method will throw an exception if the storage currently is {@code null}.
	 *
	 * @return
	 *
	 * @throws ModelException in case the internal item storage is {@code null}
	 */
	protected ItemStorage itemStorage() {
		if(itemStorage==null)
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "No item storage set");
		return itemStorage;
	}

	@Override
	public boolean isDirty() {
		return super.isDirty() || itemStorage().isDirty(this);
	}

	@Override
	public ContainerEditVerifier createEditVerifier() {
		if(!getManifest().isContainerFlagSet(ContainerFlag.NON_STATIC)) {
			return new ImmutableContainerEditVerifier(this);
		} else {
			return itemStorage().createEditVerifier(this);
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getBeginOffset()
	 */
	@Override
	public long getBeginOffset() {
		return itemStorage().getBeginOffset(this);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getEndOffset()
	 */
	@Override
	public long getEndOffset() {
		return itemStorage().getEndOffset(this);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.CONTAINER;
	}

	/**
	 * Implementation note:<br>
	 * This method returns the {@link ContainerType} of the internal
	 * {@link ItemStorage} implementation currently in use,
	 * <b>not</b> the type defined in the manifest for this container!
	 * However, it is ensured upon setting a new storage object
	 * that its container type is {@link ContainerType#isCompatibleWith(ContainerType) compatible}
	 * with the one specified in the manifest. The potentially more restrictive
	 * container type returned by this method can be used by client code
	 * to optimize operations and/or visualizations.
	 *
	 * @see de.ims.icarus2.model.api.members.container.Container#getContainerType()
	 * @see ContainerManifest#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return itemStorage().getContainerType();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#getManifest()
	 */
	@Override
	public ContainerManifest getManifest() {
		return ModelUtils.getContainerManifest(this);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#getBaseContainers()
	 */
	@Override
	public DataSet<Container> getBaseContainers() {
		return baseContainers;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#getBoundaryContainer()
	 */
	@Override
	public Container getBoundaryContainer() {
		return boundaryContainer;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#getItemCount()
	 */
	@Override
	public long getItemCount() {
		return itemStorage().getItemCount(this);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#getItemAt(long)
	 */
	@Override
	public Item getItemAt(long index) {
		return itemStorage().getItemAt(this, index);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#indexOfItem(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long indexOfItem(Item item) {
		return itemStorage().indexOfItem(this, item);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#removeItem(long)
	 */
	@Override
	public Item removeItem(long index) {
		return itemStorage().removeItem(this, index);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#removeItems(long, long)
	 */
	@Override
	public DataSequence<? extends Item> removeItems(long index0, long index1) {
		return itemStorage().removeItems(this, index0, index1);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#addItem(long, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public void addItem(long index, Item item) {
		itemStorage().addItem(this, index, item);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#addItems(long, de.ims.icarus2.util.collections.seq.DataSequence)
	 */
	@Override
	public void addItems(long index, DataSequence<? extends Item> items) {
		itemStorage().addItems(this, index, items);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#moveItem(long, long)
	 */
	@Override
	public void moveItem(long index0, long index1) {
		itemStorage().moveItem(this, index0, index1);
	}

	@Override
	public boolean isItemsComplete() {
		return MemberFlags.isItemsComplete(flags);
	}

	public void setItemsComplete(boolean complete) {
		flags = MemberFlags.setItemsComplete(flags, complete);
	}

	public ItemStorage getItemStorage() {
		return itemStorage;
	}

	protected void checkItemStorage(ItemStorage itemStorage) {
		if (itemStorage == null) {
			return;
		}

		ContainerType requiredType = getManifest().getContainerType();
		ContainerType givenType = itemStorage.getContainerType();

		if(!requiredType.isCompatibleWith(givenType))
			throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
					Messages.mismatchMessage("Incompatible container types", requiredType, givenType));
	}

	public void setItemStorage(ItemStorage itemStorage) {
		checkItemStorage(itemStorage);

		if(this.itemStorage!=null) {
			this.itemStorage.removeNotify(this);
		}

		this.itemStorage = itemStorage;

		if(this.itemStorage!=null) {
			this.itemStorage.addNotify(this);
		}
	}

	public void clearItemStorage() {
		if(itemStorage!=null) {
			itemStorage.removeNotify(this);
		}

		itemStorage = null;
	}

	public void setBoundaryContainer(Container boundary) {
		this.boundaryContainer = boundary;
	}

	public void setBaseContainers(DataSet<Container> baseContainers) {
		if (baseContainers == null)
			throw new NullPointerException("Invalid baseContainers");

		this.baseContainers = baseContainers;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		super.recycle();

		itemStorage = null;
		baseContainers = DataSet.emptySet();
		boundaryContainer = null;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return super.revive() && itemStorage!=null && baseContainers!=null;
	}
}
