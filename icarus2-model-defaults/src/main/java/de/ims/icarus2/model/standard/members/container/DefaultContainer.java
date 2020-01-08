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
package de.ims.icarus2.model.standard.members.container;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.apiguard.Unguarded;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerFlag;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerManifestBase;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.members.MemberFlags;
import de.ims.icarus2.model.standard.members.item.DefaultItem;
import de.ims.icarus2.util.Recyclable;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.collections.seq.DataSequence;
import de.ims.icarus2.util.collections.set.DataSet;
import de.ims.icarus2.util.mem.Assessable;
import de.ims.icarus2.util.mem.Link;
import de.ims.icarus2.util.mem.Reference;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(Container.class)
@Assessable
public class DefaultContainer extends DefaultItem implements Container, Recyclable {

	@Link
	private ItemStorage itemStorage;

	@Reference
	private Container boundaryContainer;

	@Link
	private DataSet<Container> baseContainers = DataSet.emptySet();

	@Link
	private ContainerManifestBase<?> manifest;

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

	/**
	 * Returns {@code true} if either the super method reports the {@link MemberFlags#isItemDirty(int) dirty flag}
	 * to be set or if the underlying {@link #getItemStorage() item-storage} reports a dirty state.
	 *
	 * @see de.ims.icarus2.model.standard.members.item.DefaultItem#isDirty()
	 */
	@Unguarded("Requires access to a valid ItemStorage instance")
	@Override
	public boolean isDirty() {
		return super.isDirty() || itemStorage().isDirty(this);
	}

	@Override
	public ContainerEditVerifier createEditVerifier() {
		if(!getManifest().isContainerFlagSet(ContainerFlag.NON_STATIC)) {
			return new ImmutableContainerEditVerifier(this);
		}

		return itemStorage().createEditVerifier(this);
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
	public @Nullable ContainerManifestBase<?> getManifest() {
		return manifest;
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
	 * @see de.ims.icarus2.model.api.members.container.Container#swapItems(long, long)
	 */
	@Override
	public void swapItems(long index0, long index1) {
		itemStorage().swapItems(this, index0, index1);
	}

	@Override
	public boolean isItemsComplete() {
		return isFlagSet(MemberFlags.ITEMS_COMPLETE);
	}

	public void setItemsComplete(boolean complete) {
		setFlag(MemberFlags.ITEMS_COMPLETE, complete);
	}

	public @Nullable ItemStorage getItemStorage() {
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
					Messages.mismatch("Incompatible container types", requiredType, givenType));
	}

	@Unguarded("Requires access to a manifest")
	public void setItemStorage(@Nullable ItemStorage itemStorage) {
		checkItemStorage(itemStorage);

		clearItemStorage();

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

	protected void setManifest0(ContainerManifestBase<?> manifest) {
		this.manifest = requireNonNull(manifest);
	}

	protected void checkManifest(ContainerManifestBase<?> manifest) {
		if(!ContainerManifest.class.isInstance(manifest))
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, Messages.mismatch(
					"Invalid manifest type", ContainerManifest.class, manifest.getClass()));
	}

	/**
	 * @param manifest the manifest to set
	 */
	public void setManifest(ContainerManifestBase<?> manifest) {
		requireNonNull(manifest);
		checkManifest(manifest);
		this.manifest = manifest;
	}

	public void setBoundaryContainer(@Nullable Container boundary) {
		this.boundaryContainer = boundary;
	}

	public void setBaseContainers(DataSet<Container> baseContainers) {
		this.baseContainers = requireNonNull(baseContainers);
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		super.recycle();

		clearItemStorage();
		baseContainers = DataSet.emptySet();
		boundaryContainer = null;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return super.revive() && itemStorage!=null; // previously checked for baseContainers!=null, but that field never gets null
	}
}
