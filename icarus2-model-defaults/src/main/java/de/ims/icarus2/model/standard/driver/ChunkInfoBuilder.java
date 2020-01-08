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
package de.ims.icarus2.model.standard.driver;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus2.model.api.driver.ChunkInfo;
import de.ims.icarus2.model.api.driver.ChunkState;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
public class ChunkInfoBuilder {

	public static final int DEFAULT_CAPACITY = 100;

	public static ChunkInfoBuilder newEmptyBuilder() {
		return new ChunkInfoBuilder(new EmptyChunkInfo());
	}

	public static ChunkInfoBuilder builder() {
		return builder(DEFAULT_CAPACITY);
	}

	public static ChunkInfoBuilder builder(int capacity) {
		return new ChunkInfoBuilder(new BufferedChunkStorage(capacity));
	}

	private final ChunkStorage storage;

	ChunkInfoBuilder(ChunkStorage storage) {
		this.storage = storage;
	}

	public boolean add(long index, Item item, ChunkState state) {
		return storage.addChunk(index, item, state);
	}

	public boolean addNoIndex(Item item, ChunkState state) {
		return storage.addChunk(IcarusUtils.UNSET_LONG, item, state);
	}

	public boolean addCorrupted(Item item) {
		return storage.addChunk(IcarusUtils.UNSET_LONG, item, ChunkState.CORRUPTED);
	}

	public boolean addCorrupted(long index, Item item) {
		return storage.addChunk(index, item, ChunkState.CORRUPTED);
	}

	public boolean addCorrupted(long index) {
		return storage.addChunk(index, null, ChunkState.CORRUPTED);
	}

	public boolean addValid(Item item) {
		return storage.addChunk(IcarusUtils.UNSET_LONG, item, ChunkState.VALID);
	}

	public boolean addValid(long index, Item item) {
		return storage.addChunk(index, item, ChunkState.VALID);
	}

	public boolean addValid(long index) {
		return storage.addChunk(index, null, ChunkState.VALID);
	}

	public boolean addModified(Item item) {
		return storage.addChunk(IcarusUtils.UNSET_LONG, item, ChunkState.MODIFIED);
	}

	public boolean addModified(long index, Item item) {
		return storage.addChunk(index, item, ChunkState.MODIFIED);
	}

	public boolean addModified(long index) {
		return storage.addChunk(index, null, ChunkState.MODIFIED);
	}

	/**
	 * Returns the current state of the internal {@link ChunkInfo}.
	 * Note that this method does <b>not</b> reset this builder and
	 * therefore can be used multiple times with the same result.
	 *
	 * @return
	 */
	public ChunkInfo build() {
		return storage;
	}

	public void reset() {
		storage.clear();
	}

	public boolean isEmpty() {
		return storage.chunkCount()==0;
	}

	public int size() {
		return storage.chunkCount();
	}

	/**
	 * Helper interface for
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface ChunkStorage extends ChunkInfo {
		boolean addChunk(long index, Item item, ChunkState state);

		void clear();
	}

	public static class EmptyChunkInfo implements ChunkStorage {

		/**
		 * @see de.ims.icarus2.model.api.driver.ChunkInfo#chunkCount()
		 */
		@Override
		public int chunkCount() {
			return 0;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.ChunkInfo#getIndex(int)
		 */
		@Override
		public long getIndex(int index) {
			throw new IndexOutOfBoundsException();
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.ChunkInfo#getItem(int)
		 */
		@Override
		public Item getItem(int index) {
			throw new IndexOutOfBoundsException();
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.ChunkInfo#getState(int)
		 */
		@Override
		public ChunkState getState(int index) {
			throw new IndexOutOfBoundsException();
		}

		/**
		 * Does nothing.
		 *
		 * @see de.ims.icarus2.model.standard.driver.ChunkInfoBuilder.ChunkStorage#addChunk(long, de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.driver.ChunkState)
		 */
		@Override
		public boolean addChunk(long index, Item item, ChunkState state) {
			return false;
		}

		@Override
		public void clear() {
			// no-op
		}

	}

	public static class ChunkEntry {
		private final long index;
		private final Item item;
		private final byte state;

		private static final ChunkState[] states = ChunkState.values();

		public ChunkEntry(long index, Item item, ChunkState state) {
			this.index = index;
			this.item = item;
			this.state = (byte) state.ordinal();
		}

		public long getIndex() {
			return index;
		}

		public Item getItem() {
			return item;
		}

		public ChunkState getState() {
			return states[state];
		}
	}

	public static class BufferedChunkStorage implements ChunkStorage {

		private final List<ChunkEntry> entries;
		private final int capacity;

		public BufferedChunkStorage(int capacity) {
			entries = new ArrayList<>(capacity+2);
			this.capacity = capacity;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.ChunkInfo#chunkCount()
		 */
		@Override
		public int chunkCount() {
			return entries.size();
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.ChunkInfo#getIndex(int)
		 */
		@Override
		public long getIndex(int index) {
			return entries.get(index).getIndex();
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.ChunkInfo#getItem(int)
		 */
		@Override
		public Item getItem(int index) {
			return entries.get(index).getItem();
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.ChunkInfo#getState(int)
		 */
		@Override
		public ChunkState getState(int index) {
			return entries.get(index).getState();
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.ChunkInfoBuilder.ChunkStorage#addChunk(long, de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.driver.ChunkState)
		 */
		@Override
		public boolean addChunk(long index, Item item, ChunkState state) {
			entries.add(new ChunkEntry(index, item, state));

			return entries.size()>=capacity;
		}

		@Override
		public void clear() {
			entries.clear();
		}

	}
}
