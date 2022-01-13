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

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.apiguard.Unguarded;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.MemberUtils;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.util.SpanSequence;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * A storage implementation for {@value ContainerType#SPAN} containers that assumes the underlying
 * target container for the span elements to be static. This restriction is motivated by the way
 * this implementation stores the span <i>bounds</i>. To provide an extremely compact storage in
 * terms of memory it only holds two long values denoting the <i>bounds</i>.
 * This means that potential modifications to the target layer (e.g. adding a
 * new item within the region covered by this span) might compromise consistency (technically the
 * span would have grown, but due to it still having the same indices as <i>bounds</i> it would
 * report incorrect data).
 *
 * @author Markus Gärtner
 *
 */
@TestableImplementation(ItemStorage.class)
public class SpanItemStorage implements ItemStorage {
	protected long beginIndex = IcarusUtils.UNSET_LONG;
	protected long endIndex = IcarusUtils.UNSET_LONG;

	public SpanItemStorage() {
		// no-op
	}

	public SpanItemStorage(long beginIndex, long endIndex) {
		setSpanIndices(beginIndex, endIndex);
	}

	public SpanItemStorage(Item beginItem, Item endItem) {
		setSpanIndices(beginItem.getIndex(), endItem.getIndex());
	}

	@Override
	public void addNotify(Container context) {
		Container target = MemberUtils.checkSingleBaseContainer(context);

		MemberUtils.checkStaticContainer(target);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#removeNotify(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public void removeNotify(Container context) {
		requireNonNull(context);
	}

	protected Container target(Container context) {
		return context.getBaseContainers().entryAt(0);
	}

	protected boolean isEmpty() {
		return beginIndex==IcarusUtils.UNSET_LONG || endIndex==IcarusUtils.UNSET_LONG;
	}

	protected long beginIndex() {
		if(beginIndex==IcarusUtils.UNSET_LONG)
			throw new ModelException(GlobalErrorCode.MISSING_DATA, "Begin index not set");

		return beginIndex;
	}

	protected long endIndex() {
		if(endIndex==IcarusUtils.UNSET_LONG)
			throw new ModelException(GlobalErrorCode.MISSING_DATA, "End index not set");

		return endIndex;
	}

	protected long toTargetIndex(long index) {
		return beginIndex()+index;
	}

	protected long fromTargetIndex(long index) {
		return index-beginIndex();
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return ContainerType.SPAN;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getItemCount()
	 */
	@Override
	public long getItemCount(Container context) {
		requireNonNull(context);
		return isEmpty() ? 0 : (endIndex-beginIndex+1);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getItemAt(long)
	 */
	@Override
	public Item getItemAt(Container context, long index) {
		long size = getItemCount(context);
		if(index<0 || index>=size)
			throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					Messages.indexOutOfBounds(null, context, size, index));

		return target(context).getItemAt(beginIndex()+index);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#indexOfItem(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long indexOfItem(Container context, Item item) {
		requireNonNull(context);
		requireNonNull(item);

		if(isEmpty()) {
			return IcarusUtils.UNSET_LONG;
		}

		long targetIndex = target(context).indexOfItem(item);

		// Ensure we only consider items within our span
		if(targetIndex>=beginIndex() && targetIndex<=endIndex()) {
			// Translate index (beginIndex is definitely set here)
			targetIndex -= beginIndex;
		} else {
			targetIndex = IcarusUtils.UNSET_LONG;
		}

		return targetIndex;
	}

	protected void checkTargetItem(Container target, long targetIndex, Item item) {
		Item targetItem = target.getItemAt(targetIndex);

		if(targetItem!=item)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Given item "+item+" is not located at correct target location "+targetIndex);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#addItem(long, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public void addItem(Container context, long index, Item item) {
		requireNonNull(context);
		requireNonNull(item);

		Container target = target(context);

		if(isEmpty()) {
			beginIndex = endIndex = target.indexOfItem(item);

			if(beginIndex==IcarusUtils.UNSET_LONG)
				throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
						Messages.foreignItem(null, target, item));
		} else {
			long size = getItemCount(context);

			if(index==0L) {
				if(beginIndex==0L)
					throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
							"Begin index in target container is already 0 - cannot decrement it further");

				checkTargetItem(target, beginIndex-1, item);

				beginIndex--;
			} else if(index==size) {
				if(target.getItemCount()<=endIndex+1)
					throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
							"End index in target contianer is already at maximum size - cannot increment it further: "+endIndex);

				checkTargetItem(target, endIndex+1, item);

				endIndex++;
			} else
				throw new ModelException(GlobalErrorCode.INVALID_INPUT,
						"Can only append to beginning or end of span - index out of range: "+index);
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#addItems(long, de.ims.icarus2.util.collections.seq.DataSequence)
	 */
	@Override
	public void addItems(Container context, long index, DataSequence<? extends Item> items) {
		requireNonNull(context);
		requireNonNull(items);

		long size = getItemCount(context);
		// Begin and end of the span expressed in the target containers space
		long index0 = IcarusUtils.UNSET_LONG, index1 = IcarusUtils.UNSET_LONG;

		if(index!=0L && index!=size)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Can only append to beginning or end of span - index out of range: "+index);

		if(items instanceof SpanSequence) {
			// Special casing "native" sequences => very cheap!

			SpanSequence sequence = (SpanSequence) items;
			Container target = target(context);
			Container sequenceTarget = sequence.getTarget();

			// Ensure the span sequence operates on the same target container
			if(sequenceTarget!=target)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, Messages.foreignContainer(
						null, target, sequenceTarget));

			// Fetch interval bounds in target container's space
			index0 = sequence.getBeginIndex();
			index1 = sequence.getEndIndex();
		} else {

			// Expensive iterative approach

			long minIndex = Long.MAX_VALUE;
			long maxIndex = Long.MIN_VALUE;

			for(long i = 0; i<items.entryCount(); i++) {
				Container target = target(context);
				Item item = items.elementAt(i);

				// Potentially costly invocation
				long targetindex = target.indexOfItem(item);

				if(targetindex==IcarusUtils.UNSET_LONG)
					throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
							Messages.foreignItem(null, target, item));

				minIndex = Math.min(minIndex, targetindex);
				maxIndex = Math.max(maxIndex, targetindex);
			}

			//FIXME check if this is a real constraint of the SPAN type contract !!!
			if((maxIndex-minIndex+1) != items.entryCount())
				throw new ModelException(GlobalErrorCode.INVALID_INPUT,
						"Provided item sequenc does not represent a span - cannot append random collections of items");

			// Neither min nor max index can be -1
			index0 = minIndex;
			index1 = maxIndex;
		}


		if(index1<beginIndex-1 || index0>endIndex+1)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, Messages.nonOverlappingIntervals(
					null, beginIndex, endIndex, index0, index1));

		beginIndex = Math.min(index0, beginIndex);
		endIndex = Math.max(index1, endIndex);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#removeItem(long)
	 */
	@Override
	public Item removeItem(Container context, long index) {
		requireNonNull(context);

		if(isEmpty())
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					"Span is empty - cannot remove item at index: "+index);

		// Ensures a legal index value
		Item item = getItemAt(context, index);
		long size = getItemCount(context);

		if(index==0L) {
			beginIndex++;
		} else if(index==size-1) {
			endIndex--;
		} else
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Can only remove from beginning or end of span: "+index);

		// Mark empty if required
		if(beginIndex>endIndex) {
			beginIndex = endIndex = IcarusUtils.UNSET_LONG;
		}

		return item;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#removeItems(long, long)
	 */
	@Override
	public DataSequence<? extends Item> removeItems(Container context, long index0, long index1) {
		requireNonNull(context);

		if(isEmpty())
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					"Span is empty - cannot remove items");

		long size = getItemCount(context);
		Container target = target(context);

		SpanSequence sequence = null;

		if(index0==0L && index1<size) {
			// Remove from beginning
			sequence = new SpanSequence(target, beginIndex, index1+1);
			beginIndex += index1;
		} else if(index1==size-1 && index0>=0L) {
			// Remove from end
			sequence = new SpanSequence(target, index0, index1-index0+1);
			endIndex = beginIndex+index0-1;
		} else
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Supplied index range must denote either the beginning or end of this span: "+index0+" to "+index1);

		// Mark empty if required
		if(beginIndex>endIndex) {
			beginIndex = endIndex = IcarusUtils.UNSET_LONG;
		}

		return sequence;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#moveItem(long, long)
	 */
	@Override
	@Unguarded(Unguarded.UNSUPPORTED)
	public void swapItems(Container context, long index0, long index1) {
		throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
				"Cannot move items within a span");
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getBeginOffset()
	 */
	@Override
	public long getBeginOffset(Container context) {
		return beginIndex==IcarusUtils.UNSET_LONG ? IcarusUtils.UNSET_LONG : target(context).getItemAt(beginIndex).getBeginOffset();
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getEndOffset()
	 */
	@Override
	public long getEndOffset(Container context) {
		return endIndex==IcarusUtils.UNSET_LONG ? IcarusUtils.UNSET_LONG : target(context).getItemAt(endIndex).getEndOffset();
	}

	public long getBeginIndex() {
		return beginIndex;
	}

	public long getEndIndex() {
		return endIndex;
	}

	public void setBeginIndex(long beginIndex) {
		this.beginIndex = beginIndex;
	}

	public void setEndIndex(long endIndex) {
		this.endIndex = endIndex;
	}

	public void setSpanIndices(long beginIndex, long endIndex) {
		this.beginIndex = beginIndex;
		this.endIndex = endIndex;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		beginIndex = endIndex = IcarusUtils.UNSET_LONG;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return true;
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
		return target(context).isDirty();
	}
}
