/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.collections.seq.DataSequence;
import de.ims.icarus2.util.collections.seq.SingletonSequence;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(ItemStorage.class)
public class SingletonItemStorage implements ItemStorage {

	protected Item singleton;

	/**
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		singleton = null;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return true;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#addNotify(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public void addNotify(@Nullable Container context) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#removeNotify(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public void removeNotify(@Nullable Container context) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return ContainerType.SINGLETON;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getItemCount(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public long getItemCount(Container context) {
		requireNonNull(context);
		return singleton==null ? 0L : 1L;
	}

	protected final void checkIndex(long index) {
		if(index!=0L)
			throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					"Singleton container storage can only accept 0 as index value - got "+index);
	}

	protected final void checkNonEmpty(ModelErrorCode errorCode) {
		if(isEmpty())
			throw new ModelException(errorCode,
					"Singletong container storage is empty");
	}

	protected final void checkEmpty(ModelErrorCode errorCode) {
		if(!isEmpty())
			throw new ModelException(errorCode,
					"Singletong container storage is not empty");
	}

	public boolean isEmpty() {
		return singleton==null;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getItemAt(de.ims.icarus2.model.api.members.container.Container, long)
	 */
	@Override
	public Item getItemAt(@Nullable Container context, long index) {
		checkNonEmpty(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS);
		checkIndex(index);

		return singleton;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#indexOfItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long indexOfItem(@Nullable Container context, Item item) {
		requireNonNull(item);
		return (singleton!=null && singleton==item) ? 0L : IcarusUtils.UNSET_LONG;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#addItem(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public void addItem(@Nullable Container context, long index, Item item) {
		requireNonNull(item);
		checkEmpty(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS);
		checkIndex(index);

		singleton = item;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#addItems(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.util.collections.seq.DataSequence)
	 */
	@Override
	public void addItems(@Nullable Container context, long index,
			DataSequence<? extends Item> items) {
		requireNonNull(items);
		checkEmpty(ModelErrorCode.MODEL_INVALID_REQUEST);
		if(items.entryCount()>1)
			throw new ModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
					"Cannot add more than 1 element - attempted to add "+items.entryCount());
		checkIndex(index);

		singleton = items.elementAt(0L);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#removeItem(de.ims.icarus2.model.api.members.container.Container, long)
	 */
	@Override
	public Item removeItem(@Nullable Container context, long index) {
		checkNonEmpty(ModelErrorCode.MODEL_INVALID_REQUEST);
		checkIndex(index);

		Item result = singleton;
		singleton = null;

		return result;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#removeItems(de.ims.icarus2.model.api.members.container.Container, long, long)
	 */
	@Override
	public DataSequence<? extends Item> removeItems(@Nullable Container context,
			long index0, long index1) {
		checkNonEmpty(ModelErrorCode.MODEL_INVALID_REQUEST);
		checkIndex(index0);
		checkIndex(index1);
		if(index1-index0>0)
			throw new ModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
					"Cannot remove more than 1 element from singletong container storage");

		return new SingletonSequence<>(removeItem(context, 0L));
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#swapItems(de.ims.icarus2.model.api.members.container.Container, long, long)
	 */
	@Override
	public void swapItems(@Nullable Container context, long index0, long index1) {
		throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
				"Cannot move items in singletong container storage");
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getBeginOffset(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public long getBeginOffset(@Nullable Container context) {
		return isEmpty() ? IcarusUtils.UNSET_LONG : singleton.getBeginOffset();
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getEndOffset(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public long getEndOffset(@Nullable Container context) {
		return isEmpty() ? IcarusUtils.UNSET_LONG : singleton.getEndOffset();
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#createEditVerifier(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public ContainerEditVerifier createEditVerifier(Container context) {
		return new SingletonContainerEditVerifier(context);
	}

	@Override
	public boolean isDirty(@Nullable Container context) {
		return false;
	}
}
