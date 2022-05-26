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
package de.ims.icarus2.model.standard.view.streamed;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives._long;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.ChunkInfo;
import de.ims.icarus2.model.api.driver.ChunkState;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.standard.FixedSingletonIndexSet;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.manager.ItemLayerManager;
import de.ims.icarus2.model.manifest.util.Messages;

/**
 * Implements the actual buffering and streaming logic for
 * the {@link DefaultStreamedCorpusView}.
 * <p>
 * This implementation blocks the {@link #advance()} method whenever
 * the current buffer is empty and reloading is required.
 *
 * @author Markus Gärtner
 *
 */
@NotThreadSafe
public class ItemStreamBuffer {

	private static final Logger log = LoggerFactory.getLogger(ItemStreamBuffer.class);

	private static final int BROKEN_OR_EOS = -2;

	private final List<Item> buffer;
	private final ItemLayerManager itemLayerManager;
	private final ItemLayer layer;
	private final int capacity;

	private IndexSet indices = null;

	// state
	private int cursor = UNSET_INT;
	private int mark = UNSET_INT;

	public ItemStreamBuffer(ItemLayerManager itemLayerManager, ItemLayer layer, int capacity) {
		requireNonNull(itemLayerManager);
		requireNonNull(layer);
		checkArgument(capacity>0);

		this.itemLayerManager = itemLayerManager;
		this.layer = layer;
		this.capacity = capacity;
		buffer = new ArrayList<>(capacity);
	}

	ItemLayerManager getItemLayerManager() {
		return itemLayerManager;
	}

	ItemLayer getLayer() {
		return layer;
	}

	int getCapacity() {
		return capacity;
	}

	private IndexSet nextIndices() {
		long begin = indices==null ? 0 : indices.lastIndex()+1;
		long end = begin+capacity-1;
		return makeIndices(begin, end);
	}

	/**
	 * Turns a span of indices into a proper {@link IndexSet} by also taking
	 * the following restrictions into account:
	 * <ul>
	 * <li>If the {@code end} index is smaller than the
	 * {@link ItemLayerManager#getItemCount(ItemLayer) size} of the primary layer,
	 * that size will be used as upper bound</li>
	 * <li>If after above refactoring the chunk would have an effective size of {@code 0},
	 * {@code null} is returned</li>
	 * <li>If {@code end >= begin}, creates a {@link IndexUtils#span(long, long) span}</li>
	 * </ul>
	 * @param begin
	 * @param end
	 * @return
	 */
	private @Nullable IndexSet makeIndices(long begin, long end) {
		long max = itemLayerManager.getItemCount(layer);
		if(max!=UNSET_LONG) {
			end = Math.min(end, max-1);
		}
		if(end-begin+1 <=0) {
			return null;
		}
		return IndexUtils.span(begin, end);
	}

	/**
	 * Clears all state and loads items denoted by {@link #indices} from driver
	 */
	private void reload() {
		buffer.clear();
		cursor = BROKEN_OR_EOS; // this signals "stream broken" if anything goes bad
		mark = UNSET_INT;

		if(itemLayerManager.hasItems(layer)) {
			try {
				int expectedSize = indices.size();
				// This implementation performs synchronous loading
				itemLayerManager.load(IndexUtils.wrap(indices), layer, this::addItems);
				int actualSize = buffer.size();

				if(actualSize<expectedSize)
					throw new ModelException(ModelErrorCode.MODEL_CORRUPTED_STATE,
							Messages.mismatch("At least one item has been reported to be corrupted while reloading buffer",
									_int(expectedSize), _int(actualSize)));

				// Only if everything went well do we allow a legal cursor again
				cursor = 0;
			} catch (InterruptedException | IcarusApiException e) {
				throw new ModelException(ModelErrorCode.STREAM_ERROR,
						String.format("Failed to load new items into buffer [%d-%d]",
								_long(indices.firstIndex()), _long(indices.lastIndex())), e);
			}
		}
	}

	/**
	 * Adds all non corrupted items from given chunk.
	 * @param chunkInfo
	 */
	private void addItems(ChunkInfo chunkInfo) {
		final int size = chunkInfo.chunkCount();
		for(int i=0; i<size; i++) {
			if(chunkInfo.getState(i)!=ChunkState.CORRUPTED) {
				buffer.add(chunkInfo.getItem(i));
			}
		}
	}

	private void release(IndexSet...indices) {
		try {
			itemLayerManager.release(indices, layer);
		} catch (InterruptedException | IcarusApiException e) {
			throw new ModelException(ModelErrorCode.STREAM_ERROR,
					String.format("Failed to release items: %s",
							Arrays.toString(indices)), e);
		}
	}

	public boolean advance() {
		// EOS or stream broken
		if(cursor==BROKEN_OR_EOS) {
			return false;
		}
		// Advance cursor
		cursor++;
		assert cursor>UNSET_INT;
		// End of buffer reached -> reload
		if(cursor>buffer.size()-1) {
			if(indices!=null) {
				release(indices);
			}
			indices = nextIndices();
			if(indices==null) {
				cursor = BROKEN_OR_EOS;
				return false;
			}
			reload();
			if(cursor<0) {
				return false;
			}
		}

		return true;
	}

	public Item currentItem() {
		return cursor<0 ? null : buffer.get(cursor);
	}

	public boolean hasItem() {
		return cursor>UNSET_INT && !buffer.isEmpty();
	}

	public void mark() {
		checkState(hasItem());
		mark = cursor;
	}

	public boolean hasMark() {
		return mark!=UNSET_INT;
	}

	public void clearMark() {
		mark = UNSET_INT;
	}

	public void reset() {
		checkState(hasMark());
		cursor = mark;
		mark = UNSET_INT;
	}

	public boolean wouldInvalidateMark() {
		return mark!=UNSET_INT && cursor+1 > buffer.size()-1;
	}

	/**
	 * Reduce the buffer to the currently shown item
	 * and release all other data. Also discards the mark.
	 */
	public void flush() {
		mark = UNSET_INT;

		Item item = hasItem() ? currentItem() : null;

		buffer.clear();

		// Current item must be preserved when flushing!!!
		if(item!=null) {
			// Release everything around our current item
			int size = indices.size();
			if(cursor==0) {
				release(indices.subSet(1, size-1));
			} else if(cursor==size-1) {
				release(indices.subSet(0, size-2));
			} else {
				release(indices.subSet(0, cursor-1),
						indices.subSet(cursor+1, size-1));
			}
			// Now reset indices to the current cursor
			indices = new FixedSingletonIndexSet(indices.indexAt(cursor));
			buffer.add(item);
			cursor = 0;
		} else {
			release(indices);
		}
	}

	public void skip(long n) {
		long current = hasItem() ? cursor : 0;
		long target = Math.addExact(current, n);
		if(target<capacity) {
			// Lucky us, we can keep the current buffer AND the mark
			cursor = (int) target;
		} else {
			if(indices!=null) {
				release(indices);
			}
			/*
			 *  Target outside current buffer, so reload the chunk after that.
			 *  We do a reload here since in case there are valid items after
			 *  skipping is done, the current cursor has to point to the first
			 *  valid one again.
			 */
			indices = makeIndices(target, target+capacity-1);
			reload();
		}
	}

	public void close() {
		buffer.clear();
		indices = null;
		mark = UNSET_INT;
		cursor = UNSET_INT;
	}
}
