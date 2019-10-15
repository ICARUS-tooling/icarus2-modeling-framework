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
package de.ims.icarus2.filedriver.io;

import com.google.common.annotations.VisibleForTesting;

import de.ims.icarus2.filedriver.io.BufferedIOResource.Block;
import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.strings.ToStringBuilder;
import it.unimi.dsi.fastutil.HashCommon;

/**
 * Implements a cache with a removal strategy based on the recent usage of blocks.
 * Each time a block is added or requested it gets pushed to the head of the internal
 * linked list of entries. When the cache is full and needs to make space for new entries
 * it will remove either the least or most recently used entry, depending on the chosen
 * strategy. The entry storage is built as an open hash table, making all cache operations
 * perform in constant time (with the exception of occasional rehashing when the current
 * buffer storage needs to be expanded until the specified cache capacity is reached, at
 * which point removal of previously used entries will begin).
 *
 * @author Markus Gärtner
 *
 */
public class RUBlockCache implements BlockCache {

	/** Buffer of cached blocks */
	private Entry[] table;
	/** Upper limit of allowed blocks. Purging will occur beyond that point. */
	private int capacity;
	/** Mask to use for addressing bins in the power-of-2 sized table */
	private int mask;
	/** Number of currently cached blocks */
	private int count;
	/** Number of cached blocks that will trigger rehashing */
	private int threshold;
	/** We simply stick to a default load factor */
	private static final float LOAD_FACTOR = CollectionUtils.DEFAULT_LOAD_FACTOR;

	private final boolean isLRU;

	private final Entry root;

	private static class Entry {

		/** Block id used as hash key (saved as backup measure against corrupted blocks) */
		int key;

		/** Data block */
		Block block;

		/** Link to the next entry in the hash table */
		Entry next;

		/** Next entry in the usage list or "root" */
		Entry _next;
		/** Previous entry in the usage list or "root" */
		Entry _previous;

		Entry(int key, Block block, Entry next) {
			this.key = key;
			this.block = block;
			this.next = next;
		}
	}

	/**
	 * Creates a cache implementation that will discard the least recently used block
	 * when free capacity is used up.
	 * @return
	 */
	public static RUBlockCache newLeastRecentlyUsedCache() {
		return new RUBlockCache(true);
	}

	/**
	 * Creates a cache implementation that will discard the most recently used block
	 * when free capacity is used up.
	 * @return
	 */
	public static RUBlockCache newMostRecentlyUsedCache() {
		return new RUBlockCache(false);
	}

	private RUBlockCache(boolean lru) {
		isLRU = lru;

		root = new Entry(0, null, null);
		root._next = root;
		root._previous = root;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.create(this)
			.add("capacity", capacity)
			.add("size", count)
			.add("isLRU", isLRU)
			.build();
	}

	/**
	 * Looks up the entry associated with the specified {@code id}. If such an entry
	 * could be found, it will be marked as most recently used and the {@code Block}
	 * it holds will be returned. In case no entry for the given {@code id} exists this
	 * method simply returns {@code null}.
	 *
	 * @see de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache#getBlock(int)
	 */
	@Override
	public Block getBlock(int id) {
		Entry tab[] = table;
		int index = (id & 0x7FFFFFFF) & mask;
		for (Entry e = tab[index]; e != null; e = e.next) {
			if (e.key == id) {

				// Remove entry from linked list
				e._previous._next = e._next;
				e._next._previous = e._previous;

				// Add entry to head of the list
				e._previous = root;
				e._next = root._next;
				root._next._previous = e;
				root._next = e;

				return e.block;
			}
		}
		return null;
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache#addBlock(de.ims.icarus2.filedriver.io.BufferedIOResource.Block, int)
	 */
	@Override
	public Block addBlock(Block block) {
		Entry tab[] = table;
		int id = block.getId();
		int index = (id & 0x7FFFFFFF) & mask;
		for (Entry e = tab[index]; e != null; e = e.next) {
			if (e.key == id)
				throw new IllegalStateException("Cannot add block to cache - id already in use: "+id);
		}

		Block removed = null;

		if (count >= threshold && count<capacity) {
			// Rehash the table if the threshold is exceeded
			rehash();

			tab = table;
			index = (id & 0x7FFFFFFF) & mask;
		} else if(count==capacity) {
			// Remove another entry from the table

			Entry old = findUnlockedEntry();

			if(old==null)
				throw new IllegalStateException("No unlocked entry found that could be removed to make room for new block - size: "+count);

			removed = removeBlock(old.key);
		}

		// Creates the new entry.
		Entry e = new Entry(id, block, tab[index]);
		tab[index] = e;
		count++;

		// Add entry to head of the list
		e._previous = root;
		e._next = root._next;
		root._next._previous = e;
		root._next = e;

		return removed;
	}

	/**
	 * Iterates through the linked list of entries to find an unlocked
	 * one. This can be rather expensive in case a lot of entries have
	 * been locked.
	 */
	private Entry findUnlockedEntry() {
		Entry entry = null;

		if(isLRU) {
			entry = root._previous;
			while(entry!=root && entry.block.isLocked()) {
				entry = entry._previous;
			}
		} else {
			entry = root._next;
			while(entry!=root && entry.block.isLocked()) {
				entry = entry._next;
			}
		}

		if(entry==root || entry.block.isLocked()) {
			entry = null;
		}

		return entry;
	}

	public Block removeBlock(int id) {
		Entry tab[] = table;
		int index = (id & 0x7FFFFFFF) & mask;
		for (Entry e = tab[index], prev = null; e != null; prev = e, e = e.next) {
			if (e.key == id) {
				if (prev != null) {
					prev.next = e.next;
				} else {
					tab[index] = e.next;
				}

				// Remove entry from linked list
				e._previous._next = e._next;
				e._next._previous = e._previous;

				count--;
				Block block = e.block;
				e.block = null;

				return block;
			}
		}
		return null;
	}

	private void rehash() {
		int oldCapacity = table.length;
		Entry oldMap[] = table;

		int n = HashCommon.arraySize(oldCapacity+1, LOAD_FACTOR);
		Entry newMap[] = new Entry[n];

		threshold = (int) (n * 0.75f);
		table = newMap;
		mask = n-1;

		for (int i = oldCapacity; i-- > 0;) {
			for (Entry old = oldMap[i]; old != null;) {
				Entry e = old;
				old = old.next;

				int index = (e.key & 0x7FFFFFFF) & mask;
				e.next = newMap[index];
				newMap[index] = e;
			}
		}
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache#open(int)
	 */
	@Override
	public void open(int capacity) {
		if(capacity<MIN_CAPACITY)
			throw new IllegalArgumentException("Capacity below required minimum: "+capacity);

		this.capacity = capacity;

		int n = HashCommon.arraySize(MIN_CAPACITY, LOAD_FACTOR);

		table = new Entry[n];
		threshold = (int) (n*LOAD_FACTOR);
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache#close()
	 */
	@Override
	public void close() {
		table = null;
		count = 0;
		threshold = 0;
		root._next = root._previous = root;
	}

	/** Open for package-private reading for tests */
	@VisibleForTesting
	boolean isLRU() {
		return isLRU;
	}
}
