/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.mem;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.io.Bits;

/**
 * Models a byte heap with fixed block size.
 * This implementation uses a free-list to keep track
 * of unused entries after they have been {@link #free(int) released}.
 * <p>
 * Not thread-safe!
 *
 * @author Markus Gärtner
 *
 */
public class ByteAllocator {

	public static final int MIN_SLOT_SIZE = 2 * Integer.BYTES;

	public static final int MIN_CHUNK_POWER = 7;

	public static final int MAX_CHUNK_POWER = 17;

	/**
	 * Constant size of individual slots that
	 * can be allocated.
	 * <p>
	 * Value is given as number of bytes.
	 */
	private final int slotSize;

	/**
	 * Constant size of collections of slots.
	 * <p>
	 * Value is given as number of individual slots.
	 */
	private final int chunkSize;

	private final int chunkPower;

	/**
	 * Monotonically increasing ids for our slots
	 */
	private final AtomicInteger idGen = new AtomicInteger(0);

	/**
	 * Growing storage for groups of slots.
	 * Note that currently we have no mechanism in place to
	 * dynamically shrink the buffer if entire chunks are
	 * no longer needed.
	 */
	private final List<byte[]> chunks = new ArrayList<>();

	/**
	 *
	 */
	private volatile int freeSlot = -1;

	public ByteAllocator(int slotSize, int chunkPower) {
		checkArgument("Slot size must not be less than "+MIN_SLOT_SIZE, slotSize>=MIN_SLOT_SIZE);
		checkArgument("Chunk power must be between "+MIN_CHUNK_POWER+" and "+MAX_CHUNK_POWER,
				chunkPower>=MIN_CHUNK_POWER && chunkPower<=MAX_CHUNK_POWER);

		this.slotSize = slotSize;
		this.chunkPower = chunkPower;
		this.chunkSize = 2<<chunkPower;
	}

	private int chunkIndex(int id) {
		return id>>chunkPower;
	}

	private int slotIndex(int id) {
		return (id%chunkSize) * slotSize;
	}

	private enum GrowthPolicy {
		/**
		 * No new chunks are to be added
		 */
		NO_GROWTH,
		/**
		 * Append a single chunk if needed
		 */
		APPEND,
		/**
		 * Add new chunks till a given index can fit
		 */
		GROW_TO_FIT,
		;
	}

	private byte[] getChunk(int chunkIndex, GrowthPolicy policy) {
		// Grow our buffer depending on the given policy as needed
		while(chunkIndex>=chunks.size() && policy!=GrowthPolicy.NO_GROWTH) {
			chunks.add(createChunk());
			if(policy==GrowthPolicy.APPEND) {
				break;
			}
		}
		return chunks.get(chunkIndex);
	}

	private byte[] createChunk() {
		return new byte[chunkSize * slotSize];
	}

	private int readMarker(int id) {
		int chunkIndex = chunkIndex(id);
		int slotIndex = slotIndex(id);

		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		return Bits.readInt(chunk, slotIndex);
	}

	private void writeMarker(int id, int marker) {
		int chunkIndex = chunkIndex(id);
		int slotIndex = slotIndex(id);

		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		Bits.writeInt(chunk, slotIndex, marker);
	}

	/**
	 * Allocates a new slot and returns the id that
	 * can be used for read or write requests.
	 * <p>
	 * A return value of {@code -1} indicates a problem
	 * other than an exception that prevented successful
	 * allocation of a new data chunk.
	 *
	 * @return
	 */
	public int alloc() {
		int id = -1;

		if(freeSlot!=-1) {
			// Allocate slot and re-route marker
			id = freeSlot;

			// New "free slot" will have been set by a previous free() call
			freeSlot = readMarker(freeSlot);
		} else {
			// Grab a new slot
			id = idGen.getAndIncrement();

			// Make sure we have the associated chunk in memory
			getChunk(chunkIndex(id), GrowthPolicy.APPEND);
		}

		return id;
	}

	/**
	 * Releases a previously allocated slot identified
	 * by the given {@code id}.
	 *
	 * @param id
	 */
	public void free(int id) {
		// Keep link to previous "empty slot" intact
		writeMarker(id, freeSlot);
		// Simply mark supplied id as the next free slot
		freeSlot = id;
	}

	/**
	 * Discards all the internal data structures and effectively
	 * deletes all active slot allocations.
	 */
	public void clear() {
		chunks.clear();
		freeSlot = -1;
	}

	/**
	 * Reduces the size of the internal buffer structures if possible.
	 */
	public void trim() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	// GETxxx methods

	public byte getByte(int id, int offset) {
		int chunkIndex = chunkIndex(id);
		int slotIndex = slotIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		return chunk[slotIndex+offset];
	}

	public long getNBytes(int id, int offset, int n) {
		int chunkIndex = chunkIndex(id);
		int slotIndex = slotIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		return Bits.readNBytes(chunk, slotIndex+offset, n);
	}

	public short getShort(int id, int offset) {
		int chunkIndex = chunkIndex(id);
		int slotIndex = slotIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		return Bits.readShort(chunk, slotIndex+offset);
	}

	public int getInt(int id, int offset) {
		int chunkIndex = chunkIndex(id);
		int slotIndex = slotIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		return Bits.readInt(chunk, slotIndex+offset);
	}

	public long getLong(int id, int offset) {
		int chunkIndex = chunkIndex(id);
		int slotIndex = slotIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		return Bits.readLong(chunk, slotIndex+offset);
	}

	// SETxxx methods

	public void setByte(int id, int offset, byte value) {
		int chunkIndex = chunkIndex(id);
		int slotIndex = slotIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		chunk[slotIndex+offset] = value;
	}

	public void setNBytes(int id, int offset, long value, int n) {
		int chunkIndex = chunkIndex(id);
		int slotIndex = slotIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		Bits.writeNBytes(chunk, slotIndex+offset, value, n);
	}

	public void setShort(int id, int offset, short value) {
		int chunkIndex = chunkIndex(id);
		int slotIndex = slotIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		Bits.writeShort(chunk, slotIndex+offset, value);
	}

	public void setInt(int id, int offset, int value) {
		int chunkIndex = chunkIndex(id);
		int slotIndex = slotIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		Bits.writeInt(chunk, slotIndex+offset, value);
	}

	public void setLong(int id, int offset, long value) {
		int chunkIndex = chunkIndex(id);
		int slotIndex = slotIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		Bits.writeLong(chunk, slotIndex+offset, value);
	}

	// Buffer methods

	public void writeBytes(int id, int offset, byte[] bytes, int n) {
		int chunkIndex = chunkIndex(id);
		int slotIndex = slotIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		System.arraycopy(bytes, 0, chunk, slotIndex+offset, n);
	}

	public void readBytes(int id, int offset, byte[] bytes, int n) {
		int chunkIndex = chunkIndex(id);
		int slotIndex = slotIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		System.arraycopy(chunk, slotIndex+offset, bytes, 0, n);
	}

	public Cursor newCursor() {
		return new Cursor();
	}

	/**
	 * A convenient mechanism for moving around inside a {@link ByteAllocator}
	 * instance and to perform bulk operations on selected slots.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public class Cursor {
		private int id;
		private int slotIndex;
		private byte[] chunk;

		Cursor() {
			// not visible for foreign code
		}

		public void clear() {
			id = IcarusUtils.UNSET_INT;
			slotIndex = IcarusUtils.UNSET_INT;
			chunk = null;
		}

		public Cursor moveTo(int id) {
			this.id = id;
			if(id==IcarusUtils.UNSET_INT) {
				slotIndex = IcarusUtils.UNSET_INT;
				chunk = null;
			} else {
				slotIndex = slotIndex(id);
				chunk = getChunk(chunkIndex(id), GrowthPolicy.NO_GROWTH);
			}
			return this;
		}

		/**
		 * @return the id
		 */
		public int getId() {
			return id;
		}

		public boolean hasChunk() {
			return chunk!=null;
		}

		private void checkChunkAvailable() {
			checkState("No chunk available - use moveTo(int) to move cursor", hasChunk());
		}

		// GETxxx methods

		public byte getByte(int offset) {
			checkChunkAvailable();
			return chunk[slotIndex+offset];
		}

		public long getNBytes(int offset, int n) {
			checkChunkAvailable();
			return Bits.readNBytes(chunk, slotIndex+offset, n);
		}

		public short getShort(int offset) {
			checkChunkAvailable();
			return Bits.readShort(chunk, slotIndex+offset);
		}

		public int getInt(int offset) {
			checkChunkAvailable();
			return Bits.readInt(chunk, slotIndex+offset);
		}

		public long getLong(int offset) {
			checkChunkAvailable();
			return Bits.readLong(chunk, slotIndex+offset);
		}

		// SETxxx methods

		public void setByte(int offset, byte value) {
			checkChunkAvailable();
			chunk[slotIndex+offset] = value;
		}

		public void setNBytes(int offset, long value, int n) {
			checkChunkAvailable();
			Bits.writeNBytes(chunk, slotIndex+offset, value, n);
		}

		public void setShort(int offset, short value) {
			checkChunkAvailable();
			Bits.writeShort(chunk, slotIndex+offset, value);
		}

		public void setInt(int offset, int value) {
			checkChunkAvailable();
			Bits.writeInt(chunk, slotIndex+offset, value);
		}

		public void setLong(int offset, long value) {
			checkChunkAvailable();
			Bits.writeLong(chunk, slotIndex+offset, value);
		}

		// Buffer methods

		public void writeBytes(int offset, byte[] bytes, int n) {
			checkChunkAvailable();
			System.arraycopy(bytes, 0, chunk, slotIndex+offset, n);
		}

		public void readBytes(int offset, byte[] bytes, int n) {
			checkChunkAvailable();
			System.arraycopy(chunk, slotIndex+offset, bytes, 0, n);
		}
	}
}
