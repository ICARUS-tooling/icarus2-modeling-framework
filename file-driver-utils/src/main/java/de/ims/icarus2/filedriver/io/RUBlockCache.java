/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 442 $
 * $Date: 2016-01-07 10:59:40 +0100 (Do, 07 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/driver/io/RUBlockCache.java $
 *
 * $LastChangedDate: 2016-01-07 10:59:40 +0100 (Do, 07 Jan 2016) $
 * $LastChangedRevision: 442 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.filedriver.io;

import de.ims.icarus2.filedriver.io.BufferedIOResource.Block;
import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;

/**
 * Implements a cache with a removal strategy based on the recent usage of blocks.
 * Each time a block is added or requested it gets pushed to the head of the internal
 * linked list of entries. When the cache is full and needs to make space for new entries
 * it will remove either the least or most recently used entry, depending on the chosen
 * strategy. The entry storage is built as a hash table, making all cache operations
 * perform in constant time (with the exception of occasional rehashing when the current
 * buffer storage needs to be expanded until the specified cache capacity is reached, at
 * which point removal of previously used entries will begin).
 *
 * @author Markus Gärtner
 * @version $Id: RUBlockCache.java 442 2016-01-07 09:59:40Z mcgaerty $
 *
 */
public class RUBlockCache implements BlockCache {

	private Entry[] table;
	private int capacity;
	private int count;
	private int threshold; //FIXME init capacity, load factor etc..!!!

	private final boolean isLRU;

	private final Entry root;

	private static class Entry {

		// Block id used as hash key
		int key;

		// Data block
		Block block;

		// Link to the next entry in the hash table
		Entry next;

		// Links for the linked list
		Entry _next, _previous;

		Entry(int key, Block block, Entry next) {
			this.key = key;
			this.block = block;
			this.next = next;
		}
	}

	public static RUBlockCache newLeastRecentlyUsedCache() {
		return new RUBlockCache(true);
	}

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
		return new StringBuilder()
		.append(getClass().getName())
		.append("[ capacity=").append(capacity)
		.append(" size=").append(count)
		.append(" isLRU=").append(isLRU)
		.append(']')
		.toString();
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
		int index = (id & 0x7FFFFFFF) % tab.length;
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
	public Block addBlock(Block block, int id) {
		Entry tab[] = table;
		int index = (id & 0x7FFFFFFF) % tab.length;
		for (Entry e = tab[index]; e != null; e = e.next) {
			if (e.key == id)
				throw new IllegalStateException("Cannot add block to cache - id already in use: "+id); //$NON-NLS-1$
		}

		Block removed = null;

		if (count >= threshold && count<capacity) {
			// Rehash the table if the threshold is exceeded
			rehash();

			tab = table;
			index = (id & 0x7FFFFFFF) % tab.length;
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
		int index = (id & 0x7FFFFFFF) % tab.length;
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

		int newCapacity = Math.min(capacity, (oldCapacity * 2) + 1);
		Entry newMap[] = new Entry[newCapacity];

		threshold = (int) (newCapacity * 0.75f);
		table = newMap;

		for (int i = oldCapacity; i-- > 0;) {
			for (Entry old = oldMap[i]; old != null;) {
				Entry e = old;
				old = old.next;

				int index = (e.key & 0x7FFFFFFF) % newCapacity;
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
			throw new IllegalArgumentException("Capacity below required minimum: "+capacity); //$NON-NLS-1$

		this.capacity = capacity;

		int size = Math.min(MIN_CAPACITY, capacity);

		table = new Entry[size];
		threshold = (int) (size*0.75f);
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache#close()
	 */
	@Override
	public void close() {
		table = null;
		count = 0;
		threshold = 0;
	}
}
