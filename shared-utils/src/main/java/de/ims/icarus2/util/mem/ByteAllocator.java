/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
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
 */
package de.ims.icarus2.util.mem;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.ims.icarus2.util.io.Bits;

/**
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

	private byte[] getChunk(int chunkIndex) {
		// Assumption: we can only grow 1 chunk at a time,
		// so any other chunk index will yield an exception.
		if(chunkIndex==chunks.size()) {
			chunks.add(createChunk());
		}
		return chunks.get(chunkIndex);
	}

	private byte[] createChunk() {
		return new byte[chunkSize * slotSize];
	}

	private int readMarker(int id) {
		int chunkIndex = chunkIndex(id);
		int slotIndex = slotIndex(id);

		byte[] chunk = getChunk(chunkIndex);

		return Bits.readInt(chunk, slotIndex);
	}

	private void writeMarker(int id, int marker) {
		int chunkIndex = chunkIndex(id);
		int slotIndex = slotIndex(id);

		byte[] chunk = getChunk(chunkIndex);

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
			getChunk(chunkIndex(id));
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

//	// GETxxx methods
//
//	public byte getByte(int id, int offset) {
//		int chunkIndex = chunkIndex(id);
//		int slotIndex = slotIndex(id);
//		byte[] chunk = getChunk(chunkIndex);
//
//		return chunk[slotIndex+offset];
//	}
//
//	public long getNBytes(int id, int offset, int n) {
//		int chunkIndex = chunkIndex(id);
//		int slotIndex = slotIndex(id);
//		byte[] chunk = getChunk(chunkIndex);
//
//		return Bits.readNBytes(chunk, slotIndex+offset, n);
//	}
//
//	public short getShort(int id, int offset) {
//		int chunkIndex = chunkIndex(id);
//		int slotIndex = slotIndex(id);
//		byte[] chunk = getChunk(chunkIndex);
//
//		return Bits.readShort(chunk, slotIndex+offset);
//	}
//
//	public int getInt(int id, int offset) {
//		int chunkIndex = chunkIndex(id);
//		int slotIndex = slotIndex(id);
//		byte[] chunk = getChunk(chunkIndex);
//
//		return Bits.readInt(chunk, slotIndex+offset);
//	}
//
//	public long getLong(int id, int offset) {
//		int chunkIndex = chunkIndex(id);
//		int slotIndex = slotIndex(id);
//		byte[] chunk = getChunk(chunkIndex);
//
//		return Bits.readLong(chunk, slotIndex+offset);
//	}
//
//	// SETxxx methods
//
//	public void setByte(int id, int offset, byte value) {
//		int chunkIndex = chunkIndex(id);
//		int slotIndex = slotIndex(id);
//		byte[] chunk = getChunk(chunkIndex);
//
//		chunk[slotIndex+offset] = value;
//	}
//
//	public void setNBytes(int id, int offset, long value, int n) {
//		int chunkIndex = chunkIndex(id);
//		int slotIndex = slotIndex(id);
//		byte[] chunk = getChunk(chunkIndex);
//
//		Bits.writeNBytes(chunk, slotIndex+offset, value, n);
//	}
//
//	public void setShort(int id, int offset, short value) {
//		int chunkIndex = chunkIndex(id);
//		int slotIndex = slotIndex(id);
//		byte[] chunk = getChunk(chunkIndex);
//
//		Bits.writeShort(chunk, slotIndex+offset, value);
//	}
//
//	public void setInt(int id, int offset, int value) {
//		int chunkIndex = chunkIndex(id);
//		int slotIndex = slotIndex(id);
//		byte[] chunk = getChunk(chunkIndex);
//
//		Bits.writeInt(chunk, slotIndex+offset, value);
//	}
//
//	public void setLong(int id, int offset, long value) {
//		int chunkIndex = chunkIndex(id);
//		int slotIndex = slotIndex(id);
//		byte[] chunk = getChunk(chunkIndex);
//
//		Bits.writeLong(chunk, slotIndex+offset, value);
//	}

	// Buffer methods

	public void writeBytes(int id, int offset, byte[] bytes, int n) {
		int chunkIndex = chunkIndex(id);
		int slotIndex = slotIndex(id);
		byte[] chunk = getChunk(chunkIndex);

		System.arraycopy(bytes, 0, chunk, slotIndex+offset, n);
	}

	public void readBytes(int id, int offset, byte[] bytes, int n) {
		int chunkIndex = chunkIndex(id);
		int slotIndex = slotIndex(id);
		byte[] chunk = getChunk(chunkIndex);

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
			id = -1;
			slotIndex = -1;
			chunk = null;
		}

		public Cursor moveTo(int id) {
			this.id = id;
			slotIndex = slotIndex(id);
			chunk = getChunk(chunkIndex(id));
			return this;
		}

		/**
		 * @return the id
		 */
		public int getId() {
			return id;
		}

		private void checkChunkAvailable() {
			checkState("No chunk available - use moveTo(int) to move cursor", chunk!=null);
		}

//		// GETxxx methods
//
//		public byte getByte(int offset) {
//			checkChunkAvailable();
//			return chunk[slotIndex+offset];
//		}
//
//		public long getNBytes(int offset, int n) {
//			checkChunkAvailable();
//			return Bits.readNBytes(chunk, slotIndex+offset, n);
//		}
//
//		public short getShort(int offset) {
//			checkChunkAvailable();
//			return Bits.readShort(chunk, slotIndex+offset);
//		}
//
//		public int getInt(int offset) {
//			checkChunkAvailable();
//			return Bits.readInt(chunk, slotIndex+offset);
//		}
//
//		public long getLong(int offset) {
//			checkChunkAvailable();
//			return Bits.readLong(chunk, slotIndex+offset);
//		}
//
//		// SETxxx methods
//
//		public void setByte(int offset, byte value) {
//			checkChunkAvailable();
//			chunk[slotIndex+offset] = value;
//		}
//
//		public void setNBytes(int offset, long value, int n) {
//			checkChunkAvailable();
//			Bits.writeNBytes(chunk, slotIndex+offset, value, n);
//		}
//
//		public void setShort(int offset, short value) {
//			checkChunkAvailable();
//			Bits.writeShort(chunk, slotIndex+offset, value);
//		}
//
//		public void setInt(int offset, int value) {
//			checkChunkAvailable();
//			Bits.writeInt(chunk, slotIndex+offset, value);
//		}
//
//		public void setLong(int offset, long value) {
//			checkChunkAvailable();
//			Bits.writeLong(chunk, slotIndex+offset, value);
//		}

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
