/**
 *
 */
package de.ims.icarus2.model.standard.view.streamed;

import static de.ims.icarus2.model.api.driver.indices.IndexUtils.span;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.lang.Primitives._long;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

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
import de.ims.icarus2.model.api.driver.indices.standard.SingletonIndexSet;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.manager.ItemLayerManager;

/**
 * Implements the actual buffering and streaming logic for
 * the {@link DefaultStreamedCorpusView}.
 *
 * @author Markus Gärtner
 *
 */
@NotThreadSafe
public class ItemStreamBuffer {

	private static final Logger log = LoggerFactory.getLogger(ItemStreamBuffer.class);

	private final List<Item> buffer;
	private final ItemLayerManager itemLayerManager;
	private final ItemLayer layer;
	private final int capacity;

	private IndexSet indices = null;

	// state
	private int cursor = 0;
	private int mark = UNSET_INT;

	/** Used only for making sure we don't mess up loading new data */
	private final Object lock = new Object();

	public ItemStreamBuffer(ItemLayerManager itemLayerManager, ItemLayer layer, int capacity) {
		requireNonNull(itemLayerManager);
		requireNonNull(layer);
		checkArgument(capacity>0);

		this.itemLayerManager = itemLayerManager;
		this.layer = layer;
		this.capacity = capacity;
		buffer = new ArrayList<>(capacity);
	}

	private IndexSet nextIndices() {
		long begin = indices==null ? 0 : indices.lastIndex()+1;
		long end = begin+capacity-1;
		long max = itemLayerManager.getItemCount(layer);
		if(max!=UNSET_LONG) {
			end = Math.min(end, max);
		}
		if(end-begin+1 <=0) {
			return null;
		}
		return span(begin, end);
	}

	/**
	 * Clears all state and loads items denoted by {@link #indices} from driver
	 */
	private void reload() {
		synchronized (lock) {
			buffer.clear();
			cursor = UNSET_INT;
			mark = UNSET_INT;

			try {
				int expectedSize = indices.size();
				itemLayerManager.load(IndexUtils.wrap(indices), layer, this::addItems);
				int actualSize = buffer.size();

				if(actualSize<expectedSize)
					throw new ModelException(ModelErrorCode.MODEL_CORRUPTED_STATE,
							"At least one item has been reported to be corrupted while reloading buffer");

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
		for(int i=0; i<chunkInfo.chunkCount(); i++) {
			if(chunkInfo.getState(i)!=ChunkState.CORRUPTED) {
				buffer.add(chunkInfo.getItem(i));
			}
		}
	}

	private boolean isEosOrBroken() {
		return indices==null || cursor==UNSET_INT;
	}

	public boolean advance() {
		// EOS or stream broken
		if(isEosOrBroken()) {
			return false;
		}
		// Advance cursor
		cursor++;
		// End of buffer reached -> reload
		if(cursor>=buffer.size()-1) {
			indices = nextIndices();
			reload();
			if(cursor==UNSET_INT) {
				return false;
			}
		}

		return true;
	}

	public Item currentItem() {
		return cursor==UNSET_INT ? null : buffer.get(cursor);
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
		return cursor+1 >= buffer.size()-1;
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
			indices = new SingletonIndexSet(indices.indexAt(cursor));
			buffer.add(item);
			cursor = 0;
		}
	}

	public void skip(long n) {
		long current = hasItem() ? cursor : 0;
		long target = Math.addExact(current, n);
		if(target<capacity) {
			// Lucky us, we can keep the current buffer AND the mark
			cursor = (int) target;
		} else {
			/*
			 *  Target outside current buffer, so discard mark and prepare to
			 *  load a new chunk from driver.
			 */
			mark = UNSET_INT;
		}
	}

	public void close() {
		buffer.clear();
		indices = null;
		mark = UNSET_INT;
		cursor = UNSET_INT;
	}
}
