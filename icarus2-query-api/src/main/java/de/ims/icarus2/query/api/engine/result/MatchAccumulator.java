/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.query.api.engine.result;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;

import java.util.List;

import de.ims.icarus2.query.api.engine.QueryUtils;
import de.ims.icarus2.util.annotations.PreliminaryValue;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Implements a buffer for virtually unlimited {@link Match} instances
 * that can be traversed by a cursor-style interface.
 *
 * @author Markus Gärtner
 *
 */
public class MatchAccumulator implements MatchSource, MatchSink {

	@PreliminaryValue
	public static final int DEFAULT_INITIAL_BLOCK_SIZE = QueryUtils.BUFFER_STARTSIZE;

	@PreliminaryValue
	public static final int DEFAULT_MAX_BLOCK_SIZE = 1<<25;

	private final List<Entry> entries = new ObjectArrayList<>();
	private final List<Block> blocks = new ObjectArrayList<>();

	private int count = 0;
	private int activeBlock = 0;
	private int nextOffset = 0;
	private int cursor = UNSET_INT;

	/** Initial size of buffer arrays inside {@link Block} instances */
	private final int initialBlockSize;
	/** Maximum size of buffer arrays inside {@link Block} instances */
	private final int maxBlockSize;

	public MatchAccumulator() {
		this(DEFAULT_INITIAL_BLOCK_SIZE, DEFAULT_MAX_BLOCK_SIZE);
	}

	public MatchAccumulator(int initialBlockSize, int maxBlockSize) {
		checkArgument("initial block size must be positive", initialBlockSize>0);
		checkArgument("max block size must be positive", maxBlockSize>0);

		this.initialBlockSize = initialBlockSize;
		this.maxBlockSize = maxBlockSize;
	}

	public void reset() {
		count = 0;
		activeBlock = 0;
		nextOffset = 0;
		cursor = UNSET_INT;
	}

	public void close() {
		reset();
		entries.clear();
		blocks.clear();
	}

	private Entry createEntry() {
		Entry entry = new Entry();
		entries.add(entry);
		return entry;
	}

	private Entry nextEntry() {
		if(count>=entries.size()) {
			createEntry();
		}
		return entries.get(count);
	}

	private Block createBlock() {
		Block block = new Block(initialBlockSize);
		blocks.add(block);
		return block;
	}

	private Block prepareBlock(int size) {
		Block block;
		for(;;) {
			if(activeBlock>=blocks.size()) {
				createBlock();
			}

			block = blocks.get(activeBlock);
			// Check if we have enough capacity directly
			if(block.hasCapacity(nextOffset+size)) {
				break;
			}
			// Attempt to enlarge the block
			if(block.grow(nextOffset+size, maxBlockSize, nextOffset)) {
				break;
			}

			// Go to new block and start over from 0
			activeBlock++;
			nextOffset = 0;
		}
		return block;
	}

	/**
	 * Adds a new entry to this buffer and moves the cursor so that it point to the new entry.
	 * @see de.ims.icarus2.query.api.engine.result.MatchSink#consume(long, int, int, int[], int[])
	 */
	@Override
	public void consume(long index, int offset, int size, int[] m_node, int[] m_index) {
		Entry entry = nextEntry();
		Block block = prepareBlock(size);

		System.arraycopy(m_node, offset, block.m_nodes, nextOffset, size);
		System.arraycopy(m_index, offset, block.m_indices, nextOffset, size);

		entry.size = size;
		entry.index = index;
		entry.offset = nextOffset;
		entry.block = activeBlock;

		// Move to the new entry, this way LaneBridge does not have to do extra maintenance work
		cursor = count;
		count++;
	}

	@Override
	public Match toMatch() {
		checkState("Cursor not at a valid position", cursor!=UNSET_INT);
		Entry entry = entries.get(cursor);
		Block block = blocks.get(entry.block);
		return DefaultMatch.of(entry.index, entry.offset, entry.size, block.m_nodes, block.m_indices);
	}

	@Override
	public void drainTo(MatchSink sink) {
		checkState("Cursor not at a valid position", cursor!=UNSET_INT);
		Entry entry = entries.get(cursor);
		Block block = blocks.get(entry.block);
		sink.consume(entry.index, entry.offset, entry.size, block.m_nodes, block.m_indices);
	}

	public int getMatchCount() {
		return count;
	}

	public void goToMatch(int index) {
		if(index<0 || index >=count)
			throw new ArrayIndexOutOfBoundsException(index);
		cursor = index;
	}

	private static class Entry {
		long index;
		int offset;
		int size;
		int block;
	}

	private static class Block {
		private int[] m_nodes;
		private int[] m_indices;

		Block(int initialSize) {
			m_nodes = new int[initialSize];
			m_indices = new int[initialSize];
		}

		boolean hasCapacity(int capacity) {
			return m_nodes.length>=capacity;
		}

		boolean grow(int capacity, int limit, int preserve) {
			if(capacity>=limit) {
				return false;
			}
			m_nodes = IntArrays.grow(m_nodes, capacity, preserve);
			m_indices = IntArrays.grow(m_indices, capacity, preserve);
			return true;
		}
	}
}
