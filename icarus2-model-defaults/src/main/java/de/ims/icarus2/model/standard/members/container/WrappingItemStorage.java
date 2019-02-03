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
import de.ims.icarus2.model.api.members.MemberUtils;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * Implements a container storage that forwards all read operations to a designated
 * <i>base</i> container and blocks all edit attempts with an exception.
 *
 * @author Markus Gärtner
 *
 */
public class WrappingItemStorage implements ItemStorage {

	protected Container sourceContainer;

	public WrappingItemStorage(Container sourceContainer) {
		setSourceContainer(sourceContainer);
	}

	public Container getSourceContainer() {
		return sourceContainer;
	}

	public void setSourceContainer(Container sourceContainer) {
		requireNonNull(sourceContainer);

		// Implementation specific restriction: static target layer
		MemberUtils.checkStaticContainer(sourceContainer);

		this.sourceContainer = sourceContainer;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		sourceContainer = null;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return sourceContainer!=null;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#addNotify(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public void addNotify(Container context) throws ModelException {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#removeNotify(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public void removeNotify(Container context) throws ModelException {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return sourceContainer.getContainerType();
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getItemCount(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public long getItemCount(Container context) {
		return sourceContainer.getItemCount();
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getItemAt(de.ims.icarus2.model.api.members.container.Container, long)
	 */
	@Override
	public Item getItemAt(Container context, long index) {
		return sourceContainer.getItemAt(index);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#indexOfItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long indexOfItem(Container context, Item item) {
		return sourceContainer.indexOfItem(item);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#addItem(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public void addItem(Container context, long index, Item item) {
		throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Cannot add item to static source container");
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#addItems(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.util.collections.seq.DataSequence)
	 */
	@Override
	public void addItems(Container context, long index,
			DataSequence<? extends Item> items) {
		throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Cannot add items to static source container");
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#removeItem(de.ims.icarus2.model.api.members.container.Container, long)
	 */
	@Override
	public Item removeItem(Container context, long index) {
		throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Cannot remove item from static source container");
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#removeItems(de.ims.icarus2.model.api.members.container.Container, long, long)
	 */
	@Override
	public DataSequence<? extends Item> removeItems(Container context,
			long index0, long index1) {
		throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Cannot remove items from static source container");
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#swapItems(de.ims.icarus2.model.api.members.container.Container, long, long)
	 */
	@Override
	public void swapItems(Container context, long index0, long index1) {
		throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Cannot move items in static source container");
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getBeginOffset(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public long getBeginOffset(Container context) {
		return sourceContainer.getBeginOffset();
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getEndOffset(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public long getEndOffset(Container context) {
		return sourceContainer.getEndOffset();
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#createEditVerifier(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public ContainerEditVerifier createEditVerifier(Container context) {
		return new ImmutableContainerEditVerifier(context);
	}

	@Override
	public boolean isDirty(Container context) {
		return sourceContainer.isDirty();
	}

}
