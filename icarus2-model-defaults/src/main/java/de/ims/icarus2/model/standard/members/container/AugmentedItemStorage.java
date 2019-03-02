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

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.collections.LookupList;
import de.ims.icarus2.util.collections.seq.DataSequence;
import de.ims.icarus2.util.collections.seq.DataSequenceCollectionWrapper;
import de.ims.icarus2.util.collections.seq.ListSequence;

/**
 * Implements a container storage that uses another container's elements as a <i>base</i>
 * and augments them with additional items. To streamline management it is assumed to not
 * be allowed for the wrapping storage to forward edits to the base container and access
 * to elements is managed as follows:
 * <p>
 * <blockquote><pre>
 *       +------------------------------+---------------------+
 *       |         BASE CONTAINER       |    AUGMENTATION     |
 *       |          immutable           |      mutable        |
 *       +------------------------------+---------------------+
 *
 * Index 0                             K-1                   N-1
 * </pre></blockquote>
 * The total number of elements in this storage is {@code N} with the base container holding
 * {@code K} items. Legal index values for edit operations range from {@code K} to {@code N-1}.
 * Attempts to add, remove or move items in the index range occupied by the base container
 * will result in an exception. Note that the base container is required to be static in
 * regards to its items (see {@link #setSourceContainer(Container)})!
 * <p>
 * Note that this implementation assumes items in its augmentation list to be virtual or to not
 * contribute to the results of the {@link #getBeginOffset(Container) beginOffset} and
 * {@link #getEndOffset(Container) endOffset} methods. Therefore it does not override the
 * implementations of {@link WrappingItemStorage} for those methods which simply
 * forward the request to the base container.
 *
 * @author Markus Gärtner
 *
 */
@TestableImplementation(ItemStorage.class)
public class AugmentedItemStorage extends WrappingItemStorage {

	public static final int DEFAULT_CAPACITY = 10;

	protected final LookupList<Item> augmentation;

	public AugmentedItemStorage(Container sourceContainer) {
		this(sourceContainer, -1);
	}

	public AugmentedItemStorage(Container sourceContainer, int capacity) {
		super(sourceContainer);

		augmentation = createAugmentationBuffer(capacity);
	}

	/**
	 *
	 * @param capacity
	 * @return
	 */
	protected LookupList<Item> createAugmentationBuffer(int capacity) {
		if(capacity<0) {
			capacity = DEFAULT_CAPACITY;
		}

		return new LookupList<>(capacity);
	}

	@Override
	public void recycle() {
		super.recycle();

		augmentation.clear();
	}

	/**
	 * Unlike the super implementation this one ignores the type of its base container and
	 * always returns {@link ContainerType#LIST}.
	 *
	 * @see de.ims.icarus2.model.standard.members.container.WrappingItemStorage#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return ContainerType.LIST;
	}

	protected long getWrappedItemCount(Container context) {
		return super.getItemCount(context);
	}

	/**
	 * Returns the sum of the base containers item count and the size of this storage's
	 * internal augmentation list.
	 *
	 * @see de.ims.icarus2.model.standard.members.container.WrappingItemStorage#getItemCount(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public long getItemCount(Container context) {
		return getWrappedItemCount(context) + augmentation.size();
	}

	@Override
	public Item getItemAt(Container context, long index) {

		long wrappedCount = getWrappedItemCount(context);
		if(index<wrappedCount) {
			return super.getItemAt(context, index);
		} else {
			return augmentation.get(translateAndCheckEditIndex(context, index));
		}
	}

	@Override
	public long indexOfItem(Container context, Item item) {

		int index = augmentation.indexOf(item);
		if(index!=-1) {
			return index + getWrappedItemCount(context);
		} else {
			return super.indexOfItem(context, item);
		}
	}

	/**
	 * Translates a global index into one usable for edit operations on the internal
	 * {@link #augmentation} storage.
	 *
	 * @param context
	 * @param index
	 * @return
	 */
	protected int translateAndCheckEditIndex(Container context, long index) {
		long wrappedCount = getWrappedItemCount(context);
		if(index<wrappedCount)
			throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					"Augmented container storage cannot modify underlying container - unable to perform edit at index: "+index);

		return IcarusUtils.ensureIntegerValueRange(index-wrappedCount);
	}

	/**
	 * Returns whether the given {@code index} lies within the wrapped part of
	 * the container or
	 * @param context
	 * @param index
	 * @return
	 */
	public boolean isWrappedIndex(Container context, long index) {
		long wrappedCount = getWrappedItemCount(context);
		return index<wrappedCount;
	}

	@Override
	public void addItem(Container context, long index, Item item) {
		augmentation.add(translateAndCheckEditIndex(context, index), item);
	}

	@Override
	public void addItems(Container context, long index,
			DataSequence<? extends Item> items) {
		augmentation.addAll(IcarusUtils.ensureIntegerValueRange(index),
				new DataSequenceCollectionWrapper<>(items));
	}

	@Override
	public Item removeItem(Container context, long index) {
		return augmentation.remove(translateAndCheckEditIndex(context, index));
	}

	@Override
	public DataSequence<? extends Item> removeItems(Container context,
			long index0, long index1) {
		int idx0 = translateAndCheckEditIndex(context, index0);
		int idx1 = translateAndCheckEditIndex(context, index1);

		final List<Item> buffer = new ArrayList<>(Math.max(1, idx1-idx0+1));

		augmentation.removeAll(idx0, idx1, buffer::add);

		return new ListSequence<>(buffer);
	}

	@Override
	public void swapItems(Container context, long index0, long index1) {
		int idx0 = translateAndCheckEditIndex(context, index0);
		int idx1 = translateAndCheckEditIndex(context, index1);

		Item item0 = augmentation.get(idx0);
		Item item1 = augmentation.get(idx1);

		augmentation.set(item0, idx1);
		augmentation.set(item1, idx0);
	}

	@Override
	public ContainerEditVerifier createEditVerifier(Container context) {
		return new AugmentedContainerEditVerifier(context, this);
	}
}
