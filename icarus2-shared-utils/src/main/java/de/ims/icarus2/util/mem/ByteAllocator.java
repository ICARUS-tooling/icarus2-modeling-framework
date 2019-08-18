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
package de.ims.icarus2.util.mem;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static java.util.Objects.requireNonNull;

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
 * Each buffer chunk in this list is organized in the following
 * fashion:
 * <blockquote><pre>
 * +---+-----------------------------------------------+
 * |   |+-+------+ +-+------+   +-+--------+ +-+------+|
 * | C ||M|Slot_0| |M|Slot_1|...|M|Slot_n-1| |M|Slot_n||
 * |   |+-+------+ +-+------+   +-+--------+ +-+------+|
 * +---+-----------------------------------------------+
 * </pre></blockquote>
 * Each slot holds a sequence of data bytes, preceded by a fixed-sized header {@code (M)} that
 * for live slots is a special marker value and for free slots points to the id of another free
 * slot. The buffer chunk itself has again a header section {@code (C)} that stores the total
 * number of live slots in it. This additional maintenance information is used to allow
 * for efficient trimming of the heap when entire buffer chunks are no longer needed.
 * <p>
 * {@link #alloc() Allocating} a new slot will either recycle a previously {@link #free(int) freed}
 * slot or obtain a new slot id from a monotonically increasing id generator function.
 * Note that for a live slot the slot header has no actual function. Every time a slot
 * gets allocated the usage counter of its surrounding buffer chunk is increased by {@code 1}.
 * <p>
 * {@link #free(int) Freeing} a previously allocated slot will have it record the current
 * tail pointer of the free-list in its header and store its slot id as new tail of the free-list.
 * Whenever a slot gets freed the usage counter of its surrounding buffer chunk will be decreased
 * by {@code 1}. Trailing buffer chunks with a usage counter of {@code 0} can be removed completely
 * from this heap as a result of {@link #trim() trimming}.
 * <p>
 * All read or write methods in the main class (not {@link Cursor}) will throw an {@link IllegalStateException}
 * every time an attempt is made to interact with a slot that hasn't been allocated, but whose
 * surrounding buffer chunk is available. This is to prevent accidental reading from or writing to
 * "dead" storage.
 * <p>
 * Not thread-safe!
 *
 * @author Markus Gärtner
 *
 */
public final class ByteAllocator {

	public static final int MIN_SLOT_SIZE = 2 * Integer.BYTES;

	public static final int MIN_CHUNK_POWER = 7;

	public static final int MAX_CHUNK_POWER = 17;

	private static final int SLOT_ALIVE = -2;

	/**
	 * Number of reserved bytes in each slot for maintenance
	 * information in the form of a free-list pointer. Chosen to be
	 * large enough to fit a single integer value.
	 */
	private static final int SLOT_HEADER_SIZE = Integer.BYTES;

	/**
	 * Number of reserved bytes in each buffer chunk for maintenance
	 * information. Stores the counter keeping track of how many live
	 * slots the buffer chunk contains. Chosen to hold a single
	 * integer value.
	 */
	private static final int CHUNK_HEADER_SIZE = Integer.BYTES;

	/**
	 * Raw value for bytes per slot as defined at constructor time.
	 * This value does not include the {@link #SLOT_HEADER_SIZE slot header}.
	 */
	private int rawSlotSize;

	/**
	 * Constant size of individual slots that
	 * can be allocated.
	 * <p>
	 * Value is given as number of bytes, including the
	 * {@link #SLOT_HEADER_SIZE slot header}.
	 */
	private int slotSize;

	/**
	 * Constant size of collections of slots. Calculated as
	 * {@code 2^chunkPower}.
	 * <p>
	 * Value is given as number of individual slots.
	 */
	private final int chunkSize;

	/** Exponent for chunk size calculation as given by client code */
	private final int chunkPower;

	/**
	 * Monotonically increasing ids for our slots.
	 * {@link #clear() Clearing} this heap will reset the
	 * counter to {@code 0}.
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
	 * Keeps track of the total number of currently allocated slots.
	 */
	private int slots = 0;

	/**
	 * Head pointer of the free-list.
	 * Is either {@code -1} to signal an empty list or {@code id+1}
	 * where id is the id of the first empty slot in the list.
	 */
	private int freeSlot = UNSET_INT;

	/**
	 * Creates a {@code ByteAllocator} whose chunks all contain
	 * {@code 2^chunkPower} slots of {@code slotSize} bytes each.
	 *
	 * @param slotSize size of individual slots in a chunk in bytes.
	 * Must be greater or equal to {@link #MIN_SLOT_SIZE 8}.
	 * @param chunkPower power to calculate number of slots in each
	 * chunk. Legal values range from {@link #MIN_CHUNK_POWER 7} to
	 * {@link #MAX_CHUNK_POWER 17}.
	 */
	public ByteAllocator(int slotSize, int chunkPower) {
		checkArgument("Slot size must not be less than "+MIN_SLOT_SIZE, slotSize>=MIN_SLOT_SIZE);
		checkArgument("Chunk power must be between "+MIN_CHUNK_POWER+" and "+MAX_CHUNK_POWER,
				chunkPower>=MIN_CHUNK_POWER && chunkPower<=MAX_CHUNK_POWER);

		//TODO ensure that we can't get an int overflow with chunkSize*max_list_size

		this.slotSize = slotSize+SLOT_HEADER_SIZE;
		this.chunkPower = chunkPower;

		rawSlotSize = slotSize;
		chunkSize = 1<<chunkPower;
	}

	/**
	 * Returns the number of slots per chunk in this heap as calculated
	 * at constructor time.
	 *
	 * @return the chunkSize
	 */
	public int getChunkSize() {
		return chunkSize;
	}

	/**
	 * Returns the size of each individual slot in bytes as set at constructor time.
	 * <p>
	 * Note that this does not include additional bytes reserved to store maintenance
	 * information in each slot's header section!
	 *
	 * @return the slotSize
	 */
	public int getSlotSize() {
		return rawSlotSize;
	}

	/**
	 * For slot located at index {@code id} returns the index of the
	 * buffer chunk which contains the specified data chunk.
	 * @param id
	 * @return
	 */
	private int chunkIndex(int id) {
		return id>>chunkPower;
	}

	/**
	 * Within its hosting buffer chunk returns the slot at which the data chunk
	 * identified by {@code id} is located. The returned value is a multiple of
	 * {@link #getSlotSize() slot size} and can be used readily to address bytes
	 * in the buffer array.
	 * <p>
	 * This value points to the beginning of the slot's header data, so should
	 * <b>not</b> be exposed to client code!
	 *
	 * @param id
	 * @return
	 */
	private int rawSlotIndex(int id) {
		// First slot begins after the chunk header
		return CHUNK_HEADER_SIZE + ((id & (chunkSize-1)) * slotSize);
	}

	/**
	 * Helper enum to indicate what or if growth is allowed
	 * in the context of a certain operation.
	 *
	 * @author Markus Gärtner
	 *
	 */
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
		 * Add new chunks till a given index can fit.
		 * Note that this policy can introduce a lot of unused
		 * chunks if trying to access high index values early on.
		 * @deprecated current implementation will only append, never over-generate buffer chunks!
		 */
		GROW_TO_FIT,
		;
	}

	/**
	 * Returns (and creates if necessary) the byte chunk for a given
	 * {@code chunkIndex}. The specified {@code policy} controls whether
	 * or not creation of additional buffer chunks is allowed.
	 *
	 * @param chunkIndex
	 * @param policy
	 * @return
	 *
	 * @throws IndexOutOfBoundsException if the specified {@code chunkIndex}
	 * lies
	 */
	private byte[] getChunk(int chunkIndex, GrowthPolicy policy) {
		// Grow our buffer depending on the given policy as needed
		while(policy!=GrowthPolicy.NO_GROWTH && chunkIndex>=chunks.size()) {
			chunks.add(createChunk());
			// If we only need to append we can exit after having added a single chunk
			if(policy==GrowthPolicy.APPEND) {
				break;
			}
		}
		// If invalid chunk index was requested this will fail with IndexOutOfBoundsException
		return chunks.get(chunkIndex);
	}

	private boolean isUsed(int chunkIndex) {
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		int liveCount = Bits.readInt(chunk, 0);

		return liveCount>0;
	}

	/**
	 * Create a byte chunk of appropriate size for this heap.
	 * @return
	 */
	private byte[] createChunk() {
		return new byte[(chunkSize * slotSize) + CHUNK_HEADER_SIZE];
	}

	/**
	 * Allocates a new slot and returns the id that
	 * can be used for read or write requests.
	 * <p>
	 * A return value of {@code -1} indicates a problem
	 * other than an exception that prevented successful
	 * allocation of a new data chunk.
	 *
	 * @return the id usable for addressing the allocated slot
	 * @throws IndexOutOfBoundsException if the heap space is exhausted
	 */
	public int alloc() {
		int id = UNSET_INT;

		byte[] chunk;

		int rawSlotIndex;

		if(freeSlot!=UNSET_INT) {
			// Allocate slot and re-route marker
			id = freeSlot-1;

			// Chunk has been allocated before, so access must not fail
			chunk = getChunk(chunkIndex(id), GrowthPolicy.NO_GROWTH);

			rawSlotIndex = rawSlotIndex(id);

			// New "free slot" will have been set by a previous free() call
			freeSlot = Bits.readInt(chunk, rawSlotIndex);
		} else {
			// Grab a new slot
			id = idGen.getAndIncrement();

			// Make sure we have the associated chunk in memory
			chunk = getChunk(chunkIndex(id), GrowthPolicy.APPEND);
			rawSlotIndex = rawSlotIndex(id);
		}

		// Mark slot live
		Bits.writeInt(chunk, rawSlotIndex, SLOT_ALIVE);

		// Update live counter for chunk
		int liveCount = Bits.readInt(chunk, 0);
		liveCount++;
		Bits.writeInt(chunk, 0, liveCount);

		slots++;

		return id;
	}

	/**
	 * Releases a previously allocated slot identified
	 * by the given {@code id}.
	 *
	 * @param id the slot to free
	 * @throws IndexOutOfBoundsException if {@code id} is negative or exceeds
	 * the current constraints for addressable slots
	 */
	public void free(int id) {
		if(id<0)
			throw new IndexOutOfBoundsException("Slot id must not be negative: "+id);

		byte[] chunk = getChunk(chunkIndex(id), GrowthPolicy.NO_GROWTH);

		// Keep link to previous "empty slot" intact
		Bits.writeInt(chunk, rawSlotIndex(id), freeSlot);
		// Simply mark supplied id as the next free slot
		freeSlot = id+1;

		// Update live counter for chunk
		int liveCount = Bits.readInt(chunk, 0);
		liveCount--;
		checkState("Counter must not become negative", liveCount>=0);
		Bits.writeInt(chunk, 0, liveCount);

		slots--;
	}

	/**
	 * Discards all the internal data structures and effectively
	 * deletes all active slot allocations.
	 */
	public void clear() {
		chunks.clear();
		freeSlot = UNSET_INT;
		idGen.set(0);
		slots = 0;
	}

	/**
	 * Returns the total number of currently allocated slots in this heap.
	 * @return
	 */
	public int size() {
		return slots;
	}

	/**
	 * Returns the number of chunks currently used for this heap.
	 * Note that this can be significantly higher than needed to
	 * host the current {@link #size() number of live slots}.
	 *
	 * @return
	 */
	public int chunksUsed() {
		return chunks.size();
	}

	/**
	 * Reduces the size of the internal buffer structures if possible.
	 *
	 * @return {@code true} iff trimming was possible and execution of this
	 * method allowed at least one buffer chunk to be removed.
	 */
	public boolean trim() {
		/*
		 * While the last buffer chunk in this heap is unused,
		 * discard it completely and adjust the idGen by resetting
		 * it to return a value equal to the slot capacity of the
		 * scaled-down buffer.
		 */

		boolean couldTrim = false;

		while(!chunks.isEmpty()) {
			int chunkIndex = chunks.size()-1;

			if(isUsed(chunkIndex)) {
				// If we encounter a chunk that's still in use we need to abort
				break;
			}

			// Delete the trailing chunk
			chunks.remove(chunkIndex);
			// Adjust idGen
			idGen.set(chunks.size()*getChunkSize());

			couldTrim = true;
		}

		return couldTrim;
	}

	/**
	 * Increases the space available for every slot in this storage
	 * to {@code newSlotSize}.
	 * Does nothing if there are no entries currently.
	 *
	 * @param newSlotSize
	 */
	public void adjustSlotSize(int size) {
		checkArgument("Slot size must not be less than "+MIN_SLOT_SIZE, slotSize>=MIN_SLOT_SIZE);

		//TODO guard against overflowing the address space

		boolean grow = size>rawSlotSize;

		int oldSlotSize = slotSize;
		slotSize = size + SLOT_HEADER_SIZE; // so that createChunk() uses the right values!
		rawSlotSize = size;

		// Nothing in storage
		if(chunks.isEmpty()) {
			return;
		}

		// No live slots, so just erase current storage
		if(slots<=0) {
			clear();
			return;
		}

		int copySlotSize = grow ? oldSlotSize : slotSize;

		// Chunk by chunk adjust the content
		for (int i = 0; i < chunks.size(); i++) {
			byte[] oldChunk = chunks.get(i);
			byte[] newChunk = createChunk();
			chunks.set(i, newChunk);

			// Copy over chunk header
			System.arraycopy(oldChunk, 0, newChunk, 0, CHUNK_HEADER_SIZE);

			// If chunk had no data previously and we have no free-list, we can ignore it
			if(freeSlot==UNSET_INT && Bits.readInt(newChunk, 0)<=0) {
				continue;
			}

			int idxOld = CHUNK_HEADER_SIZE;
			int idxNew = CHUNK_HEADER_SIZE;

			// Now copy over all individual slots
			for (int j = 0; j < chunkSize; j++) {
				int header = Bits.readInt(oldChunk, idxOld);
				if(header==SLOT_ALIVE) {
					System.arraycopy(oldChunk, idxOld, newChunk, idxNew, copySlotSize);
				} else if(header>0) {
					Bits.writeInt(newChunk, idxNew, header);
				}

				idxOld += oldSlotSize;
				idxNew += slotSize;
			}
		}
	}

	/**
	 * Throws {@link IndexOutOfBoundsException} if {@code id < 0 || id >= chunkSize*chunks.size()}
	 * or if {@code offset < 0 || offset >= rawSlotSize}
	 * @param id
	 * @param offset
	 */
	private void checkIdAndOffset(int id, int offset) {
		if(id<0 || id>=chunkSize*chunks.size())
			throw new IndexOutOfBoundsException("Slot id out of bounds: "+id);

		if(offset<0)
			throw new IndexOutOfBoundsException("Negative offset not legal: "+offset);
		if(offset>=rawSlotSize)
			throw new IndexOutOfBoundsException("Offset exceeds slot size: "+offset);
	}

	private void checkBytesAvailable(int offset, int n) {
		if(offset+n-1 >= rawSlotSize)
			throw new IndexOutOfBoundsException("Offset does not leave enough bytes to satisfy request: "+offset);
	}

	private void checkLiveSlot(byte[] chunk, int rawSlotIndex, int id) {
		if(Bits.readInt(chunk, rawSlotIndex)!=SLOT_ALIVE)
			throw new IllegalStateException("Designated slot is not allocated: "+id);
	}

	// GETxxx methods

	/**
	 * Reads a single byte located in slot {@code id} at specified {@code offset}.
	 *
	 * @param id the slot to fetch data for
	 * @param offset the exact location within the byte data of designated slot
	 * @return the byte value stored at the specified location
	 *
	 * @throws IndexOutOfBoundsException if the id or offset violate the current
	 * constraints of this heap
	 */
	public byte getByte(int id, int offset) {
		checkIdAndOffset(id, offset);

		int chunkIndex = chunkIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);
		int rawSlotIndex = rawSlotIndex(id);
		checkLiveSlot(chunk, rawSlotIndex, id);

		return chunk[rawSlotIndex+SLOT_HEADER_SIZE+offset];
	}

	/**
	 * Reads a sequence of up to 8 bytes located in slot {@code id}
	 * and starting at specified {@code offset} and returns them as a {@code long}.
	 *
	 * @param id the slot to fetch data for
	 * @param offset the exact location within the byte data of designated slot
	 * @param n the number of bytes to read
	 * @return the byte value stored at the specified location
	 *
	 * @throws IndexOutOfBoundsException if the id or offset violate the current
	 * constraints of this heap
	 * @throws IllegalArgumentException if {@code n} is less than {@code 1} or
	 * greater than {@code 8}
	 * @throws IndexOutOfBoundsException if {@code offset} leaves less than {@code n}
	 * bytes to be read in the slot
	 *
	 * @see Bits#readNBytes(byte[], int, int)
	 */
	public long getNBytes(int id, int offset, int n) {
		checkIdAndOffset(id, offset);
		checkArgument(n>0 && n<9);
		checkBytesAvailable(offset, n);

		int chunkIndex = chunkIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		int rawSlotIndex = rawSlotIndex(id);
		checkLiveSlot(chunk, rawSlotIndex, id);

		return Bits.readNBytes(chunk, rawSlotIndex+SLOT_HEADER_SIZE+offset, n);
	}

	/**
	 * Reads two bytes located in slot {@code id} and starting at the specified {@code offset}
	 * and returns them as a {@code short}.
	 *
	 * @param id the slot to fetch data for
	 * @param offset the exact location within the byte data of designated slot
	 * @return the combination of {@code 2} byte values stored at the specified location as a {@code short}
	 *
	 * @throws IndexOutOfBoundsException if the id or offset violate the current
	 * constraints of this heap
	 * @throws IndexOutOfBoundsException if {@code offset} leaves less than {@code 2}
	 * bytes to be read in the slot
	 *
	 * @see Bits#readShort(byte[], int)
	 */
	public short getShort(int id, int offset) {
		checkIdAndOffset(id, offset);
		checkBytesAvailable(offset, Short.BYTES);

		int chunkIndex = chunkIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		int rawSlotIndex = rawSlotIndex(id);
		checkLiveSlot(chunk, rawSlotIndex, id);

		return Bits.readShort(chunk, rawSlotIndex+SLOT_HEADER_SIZE+offset);
	}

	/**
	 * Reads four bytes located in slot {@code id} and starting at the specified {@code offset}
	 * and returns them as an {@code int}.
	 *
	 * @param id the slot to fetch data for
	 * @param offset the exact location within the byte data of designated slot
	 * @return the combination of {@code 4} byte values stored at the specified location as an {@code int}
	 *
	 * @throws IndexOutOfBoundsException if the id or offset violate the current
	 * constraints of this heap
	 * @throws IndexOutOfBoundsException if {@code offset} leaves less than {@code 4}
	 * bytes to be read in the slot
	 *
	 * @see Bits#readInt(byte[], int)
	 */
	public int getInt(int id, int offset) {
		checkIdAndOffset(id, offset);
		checkBytesAvailable(offset, Integer.BYTES);

		int chunkIndex = chunkIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		int rawSlotIndex = rawSlotIndex(id);
		checkLiveSlot(chunk, rawSlotIndex, id);

		return Bits.readInt(chunk, rawSlotIndex+SLOT_HEADER_SIZE+offset);
	}

	/**
	 * Reads eight bytes located in slot {@code id} and starting at the specified {@code offset}
	 * and returns them as a {@code long}.
	 *
	 * @param id the slot to fetch data for
	 * @param offset the exact location within the byte data of designated slot
	 * @return the combination of {@code 8} byte values stored at the specified location as a {@code long}
	 *
	 * @throws IndexOutOfBoundsException if the id or offset violate the current
	 * constraints of this heap
	 * @throws IndexOutOfBoundsException if {@code offset} leaves less than {@code 8}
	 * bytes to be read in the slot
	 *
	 * @see Bits#readLong(byte[], int)
	 */
	public long getLong(int id, int offset) {
		checkIdAndOffset(id, offset);
		checkBytesAvailable(offset, Long.BYTES);

		int chunkIndex = chunkIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		int rawSlotIndex = rawSlotIndex(id);
		checkLiveSlot(chunk, rawSlotIndex, id);

		return Bits.readLong(chunk, rawSlotIndex+SLOT_HEADER_SIZE+offset);
	}

	// SETxxx methods

	public void setByte(int id, int offset, byte value) {
		checkIdAndOffset(id, offset);

		int chunkIndex = chunkIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		int rawSlotIndex = rawSlotIndex(id);
		checkLiveSlot(chunk, rawSlotIndex, id);

		chunk[rawSlotIndex+SLOT_HEADER_SIZE+offset] = value;
	}

	public void setNBytes(int id, int offset, long value, int n) {
		checkIdAndOffset(id, offset);
		checkArgument(n>0);
		checkBytesAvailable(offset, n);

		int chunkIndex = chunkIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		int rawSlotIndex = rawSlotIndex(id);
		checkLiveSlot(chunk, rawSlotIndex, id);

		Bits.writeNBytes(chunk, rawSlotIndex+SLOT_HEADER_SIZE+offset, value, n);
	}

	public void setShort(int id, int offset, short value) {
		checkIdAndOffset(id, offset);
		checkBytesAvailable(offset, Short.BYTES);

		int chunkIndex = chunkIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		int rawSlotIndex = rawSlotIndex(id);
		checkLiveSlot(chunk, rawSlotIndex, id);

		Bits.writeShort(chunk, rawSlotIndex+SLOT_HEADER_SIZE+offset, value);
	}

	public void setInt(int id, int offset, int value) {
		checkIdAndOffset(id, offset);
		checkBytesAvailable(offset, Integer.BYTES);

		int chunkIndex = chunkIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		int rawSlotIndex = rawSlotIndex(id);
		checkLiveSlot(chunk, rawSlotIndex, id);

		Bits.writeInt(chunk, rawSlotIndex+SLOT_HEADER_SIZE+offset, value);
	}

	public void setLong(int id, int offset, long value) {
		checkIdAndOffset(id, offset);
		checkBytesAvailable(offset, Long.BYTES);

		int chunkIndex = chunkIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		int rawSlotIndex = rawSlotIndex(id);
		checkLiveSlot(chunk, rawSlotIndex, id);

		Bits.writeLong(chunk, rawSlotIndex+SLOT_HEADER_SIZE+offset, value);
	}

	// Buffer methods

	/**
	 * Copies bytes from a given array into a slot in this heap
	 *
	 * @param id the slot to write into
	 * @param offset the byte offset within the designated slot to start writing at
	 * @param source the source of bytes to copy into this heap
	 * @param n the number of bytes to copy
	 *
	 * @throws IndexOutOfBoundsException if the id or offset violate the current
	 * constraints of this heap
	 * @throws NullPointerException if the {@code source} argument is {@code null}
	 * @throws IllegalArgumentException if {@code n} is less than {@code 1} or
	 * equal or greater than the length of array {@code source}
	 * @throws IndexOutOfBoundsException if {@code offset} leaves less than {@code n}
	 * bytes to be read in the slot
	 */
	public void writeBytes(int id, int offset, byte[] source, int n) {
		checkIdAndOffset(id, offset);
		requireNonNull(source);
		checkArgument(n>0 && n<=source.length);
		checkBytesAvailable(offset, n);

		int chunkIndex = chunkIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		int rawSlotIndex = rawSlotIndex(id);
		checkLiveSlot(chunk, rawSlotIndex, id);

		System.arraycopy(source, 0, chunk, rawSlotIndex+SLOT_HEADER_SIZE+offset, n);
	}

	/**
	 * Copies bytes from this heap into a designated target array.
	 *
	 * @param id the slot to read from
	 * @param offset the byte offset within the designated slot to start reading at
	 * @param destination the destination of bytes to copy from this heap
	 * @param n the number of bytes to copy
	 *
	 * @throws IndexOutOfBoundsException if the id or offset violate the current
	 * constraints of this heap
	 * @throws NullPointerException if the {@code destination} argument is {@code null}
	 * @throws IllegalArgumentException if {@code n} is less than {@code 1} or
	 * equal or greater than the length of array {@code destination}
	 * @throws IndexOutOfBoundsException if {@code offset} leaves less than {@code n}
	 * bytes to be read in the slot
	 */
	public void readBytes(int id, int offset, byte[] destination, int n) {
		checkIdAndOffset(id, offset);
		requireNonNull(destination);
		checkArgument(n>0 && n <= destination.length);
		checkBytesAvailable(offset, n);

		int chunkIndex = chunkIndex(id);
		byte[] chunk = getChunk(chunkIndex, GrowthPolicy.NO_GROWTH);

		int rawSlotIndex = rawSlotIndex(id);
		checkLiveSlot(chunk, rawSlotIndex, id);

		System.arraycopy(chunk, rawSlotIndex+SLOT_HEADER_SIZE+offset, destination, 0, n);
	}

	public Cursor newCursor() {
		return new Cursor();
	}

	/**
	 * A convenient mechanism for moving around inside a {@link ByteAllocator}
	 * instance and to perform bulk operations on selected slots.
	 * <p>
	 * Note that neither the surrounding {@link ByteAllocator} or instances of
	 * this class are thread-safe! If the {@link ByteAllocator} for a cursor
	 * is structurally modified (e.g. by {@link ByteAllocator#free(int) freeing}
	 * or {@link ByteAllocator#trim() trimming}) most of the methods in this class
	 * will throw an {@link IllegalStateException} in case the underlying data
	 * they rely on is no longer available.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public class Cursor {
		/**
		 * Id of current slot or -1
		 */
		private int id = UNSET_INT;
		/**
		 * Begin of payload section in current slot byte data
		 */
		private int slotIndex = UNSET_INT;
		/**
		 * Buffer chunk to write into
		 */
		private byte[] chunk = null;

		Cursor() {
			// not visible for foreign code
		}

		public ByteAllocator source() {
			return ByteAllocator.this;
		}

		/**
		 * Resets the internal position and chunk buffer of this cursor.
		 * This behavior is equivalent to calling {@link #moveTo(int)}
		 * with an {@code id} argument of {@link IcarusUtils#UNSET_INT -1}.
		 */
		public void clear() {
			moveTo(UNSET_INT);
		}

		/**
		 * {@link ByteAllocator#alloc() allocates} a new slot and {@link #moveTo(int) moves}
		 * to it for immediate access.
		 * @return
		 */
		public Cursor alloc() {
			int id = source().alloc();
			return moveTo(id);
		}

		/**
		 * Moves to the chunk identified by the given {@code id}. If the
		 * parameter is {@link IcarusUtils#UNSET_INT -1}, clear the internal
		 * position and chunk data.
		 * <p>
		 * Note that this method expects the slot for {@code id} to be already
		 * {@link ByteAllocator#alloc() allocated}!
		 *
		 * @param id
		 * @return this cursor
		 *
		 * @see ByteAllocator#getChunk(int, GrowthPolicy)
		 */
		public Cursor moveTo(int id) {
			if(id<IcarusUtils.UNSET_INT || id>=size())
				throw new IndexOutOfBoundsException("Slot id out of bounds: "+id);

			this.id = id;
			if(id==IcarusUtils.UNSET_INT) {
				slotIndex = IcarusUtils.UNSET_INT;
				chunk = null;
			} else {
				chunk = getChunk(chunkIndex(id), GrowthPolicy.NO_GROWTH);

				int rawSlotIndex = rawSlotIndex(id);
				checkLiveSlot(chunk, rawSlotIndex, id);

				slotIndex = rawSlotIndex+SLOT_HEADER_SIZE;
			}
			return this;
		}

		/**
		 * Returns the id of the chunk this cursor was last {@link #moveTo(int) moved to}.
		 * A value of {@link IcarusUtils#UNSET_INT -1} indicates that this cursor hasn't
		 * been moved, has been {@link #clear() cleared} or the last chunk id passed to the
		 * {@link #moveTo(int)} method was {@link IcarusUtils#UNSET_INT -1}.
		 *
		 * @return the id
		 */
		public int getId() {
			return id;
		}

		/**
		 * Returns {@code true} iff this cursor has been {@link #moveTo(int) moved}
		 * to a valid data chunk and is ready to read from or write to the underlying
		 * byte heap.
		 *
		 * @return
		 */
		public boolean hasChunk() {
			return chunk!=null;
		}

		private void checkChunkAvailable() {
			checkState("No chunk available - use moveTo(int) to move cursor before "
					+ "interacting with underlying data", hasChunk());
		}

		private void checkOffset(int offset) {
			if(offset<0 || offset>=rawSlotSize)
				throw new IndexOutOfBoundsException("Offset value out of bounds: "+offset);
		}

		// GETxxx methods

		public byte getByte(int offset) {
			checkChunkAvailable();
			checkOffset(offset);
			return chunk[slotIndex+offset];
		}

		public long getNBytes(int offset, int n) {
			checkChunkAvailable();
			checkOffset(offset);
			checkArgument(n>0 && n<=8);
			checkBytesAvailable(offset, n);
			return Bits.readNBytes(chunk, slotIndex+offset, n);
		}

		public short getShort(int offset) {
			checkChunkAvailable();
			checkOffset(offset);
			return Bits.readShort(chunk, slotIndex+offset);
		}

		public int getInt(int offset) {
			checkChunkAvailable();
			checkOffset(offset);
			return Bits.readInt(chunk, slotIndex+offset);
		}

		public long getLong(int offset) {
			checkChunkAvailable();
			checkOffset(offset);
			return Bits.readLong(chunk, slotIndex+offset);
		}

		// SETxxx methods

		public Cursor setByte(int offset, byte value) {
			checkChunkAvailable();
			checkOffset(offset);
			checkBytesAvailable(offset, 1);
			chunk[slotIndex+offset] = value;
			return this;
		}

		public Cursor setNBytes(int offset, long value, int n) {
			checkChunkAvailable();
			checkOffset(offset);
			checkArgument(n>0 && n<=8);
			checkBytesAvailable(offset, n);
			Bits.writeNBytes(chunk, slotIndex+offset, value, n);
			return this;
		}

		public Cursor setShort(int offset, short value) {
			checkChunkAvailable();
			checkOffset(offset);
			checkBytesAvailable(offset, Short.BYTES);
			Bits.writeShort(chunk, slotIndex+offset, value);
			return this;
		}

		public Cursor setInt(int offset, int value) {
			checkChunkAvailable();
			checkOffset(offset);
			checkBytesAvailable(offset, Integer.BYTES);
			Bits.writeInt(chunk, slotIndex+offset, value);
			return this;
		}

		public Cursor setLong(int offset, long value) {
			checkChunkAvailable();
			checkOffset(offset);
			checkBytesAvailable(offset, Long.BYTES);
			Bits.writeLong(chunk, slotIndex+offset, value);
			return this;
		}

		// Buffer methods

		public Cursor writeBytes(int offset, byte[] bytes, int n) {
			checkChunkAvailable();
			requireNonNull(bytes);
			checkOffset(offset);
			checkArgument(n>0 && n<=bytes.length);
			checkBytesAvailable(offset, n);
			System.arraycopy(bytes, 0, chunk, slotIndex+offset, n);
			return this;
		}

		public Cursor readBytes(int offset, byte[] bytes, int n) {
			checkChunkAvailable();
			requireNonNull(bytes);
			checkOffset(offset);
			checkArgument(n>0 && n<=bytes.length);
			checkBytesAvailable(offset, n);
			System.arraycopy(chunk, slotIndex+offset, bytes, 0, n);
			return this;
		}
	}
}
