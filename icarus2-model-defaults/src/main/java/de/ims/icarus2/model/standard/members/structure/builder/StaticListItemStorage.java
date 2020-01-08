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
package de.ims.icarus2.model.standard.members.structure.builder;

import static java.util.Objects.requireNonNull;

import java.util.Collection;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.standard.members.container.AbstractImmutableItemStorage;
import de.ims.icarus2.model.standard.members.container.ItemStorage;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.collections.LookupList;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(ItemStorage.class)
public class StaticListItemStorage extends AbstractImmutableItemStorage {

	private final LookupList<Item> items;

	private final Item beginItem, endItem;

	/**
	 *
	 * @param items
	 * @param beginItem
	 * @param endItem
	 *
	 * @throws ModelException of type {@link ModelErrorCode#MODEL_ILLEGAL_MEMBER}
	 * if wither {@code beginItem} or {@code endItem} are not part of the {@code items}
	 * array.
	 */
	public StaticListItemStorage(Collection<Item> items,
			@Nullable Item beginItem, @Nullable Item endItem) {
		this.items = new LookupList<>(items);

		if(beginItem!=null && !this.items.contains(beginItem))
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					"Given beginItem is not part of the list");
		if(endItem!=null && !this.items.contains(endItem))
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					"Given endItem is not part of the list");

		this.beginItem = beginItem;
		this.endItem = endItem;
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
		return ContainerType.LIST;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getItemCount(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public long getItemCount(@Nullable Container context) {
		return items.size();
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getItemAt(de.ims.icarus2.model.api.members.container.Container, long)
	 */
	@Override
	public Item getItemAt(@Nullable Container context, long index) {
		return items.get(IcarusUtils.ensureIntegerValueRange(index));
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#indexOfItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long indexOfItem(@Nullable Container context, Item item) {
		requireNonNull(item);
		return items.indexOf(item);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getBeginOffset(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public long getBeginOffset(@Nullable Container context) {
		return beginItem==null ? IcarusUtils.UNSET_LONG : beginItem.getBeginOffset();
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getEndOffset(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public long getEndOffset(@Nullable Container context) {
		return endItem==null ? IcarusUtils.UNSET_LONG : endItem.getEndOffset();
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
 				"Cannot recycle final item storage");
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return false;
	}

}
