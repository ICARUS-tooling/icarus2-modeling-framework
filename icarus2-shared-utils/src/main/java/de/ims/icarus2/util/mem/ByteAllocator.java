/**
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
package de.ims.icarus2.util.mem;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.MutablePrimitives.MutableInteger;
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
public final class ByteAllocator implements AutoCloseable {

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
	 * Raw value for bytes per slot as defined at constructor time.
	 * This value does not include the {@link #SLOT_HEADER_SIZE slot header}.
	 */
	private volatile int rawSlotSize;

	/**
	 * Constant size of individual slots that
	 * can be allocated.
	 * <p>
	 * Value is given as number of bytes, including the
	 * {@link #SLOT_HEADER_SIZE slot header}.
	 */
	private volatile int slotSize;

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
	private volatile Chunk[] chunks;

	/** Number of currently used chunks in the {@link #chunks} array */
	private volatile int chunkCount = 0;

	/**
	 * Keeps track of the total number of currently allocated slots.
	 */
	private volatile int slots = 0;

	/**
	 * Head pointer of the free-list.
	 * Is either {@code -1} to signal an empty list or {@code id+1}
	 * where id is the id of the first empty slot in the list.
	 */
	private volatile int freeSlot = UNSET_INT;

	private final Sync sync;

	public ByteAllocator(int slotSize, int chunkPower) {
		this(slotSize, chunkPower, LockType.NONE);
	}

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
	public ByteAllocator(int slotSize, int chunkPower, LockType lockType) {
		requireNonNull(lockType);
		checkArgument("Slot size must not be less than "+MIN_SLOT_SIZE, slotSize>=MIN_SLOT_SIZE);
		checkArgument("Chunk power must be between "+MIN_CHUNK_POWER+" and "+MAX_CHUNK_POWER,
				chunkPower>=MIN_CHUNK_POWER && chunkPower<=MAX_CHUNK_POWER);

		Sync sync;
		switch (lockType) {
		case NONE: sync = new Unsafe(); break;
		case NATIVE: sync = new Native(); break;
		case OPTIMISTIC: sync = new Optimistic(3); break;
		case HIGHLY_OPTIMISTIC: sync = new Optimistic(10); break;

		default:
			throw new IllegalArgumentException("Unknown lock type: "+lockType);
		}
		this.sync = sync;

		//TODO ensure that we can't get an int overflow with chunkSize*max_list_size

		this.slotSize = slotSize+SLOT_HEADER_SIZE;
		this.chunkPower = chunkPower;

		rawSlotSize = slotSize;
		chunkSize = 1<<chunkPower;

		chunks = new Chunk[10]; //TODO make customizable or find a good default
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
		return ((id & (chunkSize-1)) * slotSize);
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
	 * <p>
	 * If the given policy allows growing the internal chunks buffer, this
	 * method <b>must</b> be called under sync!!
	 *
	 * @param chunkIndex
	 * @param policy
	 * @return
	 *
	 * @throws IndexOutOfBoundsException if the specified {@code chunkIndex}
	 * lies outside {@code 0<= x <chunkCount} and the given {@code policy} does
	 * not support growths.
	 */
	private Chunk getChunkUnsafe(int chunkIndex, GrowthPolicy policy) {
		Chunk[] chunks = this.chunks;
		int chunkCount = this.chunkCount;
		if(policy!=GrowthPolicy.NO_GROWTH && chunkIndex>=chunkCount) {
			// Grow our buffer depending on the given policy as needed
			if(chunkIndex>=chunks.length) {
				int newSize = Math.max(chunks.length*2, chunkIndex);
				if(newSize<=0)
					throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE, "Size overflow");
				chunks = Arrays.copyOf(chunks, newSize);
			}
			while(chunkIndex>=chunkCount) {
				chunks[chunkCount++] = new Chunk(chunkSize * slotSize);
				// If we only need to append we can exit after having added a single chunk
				if(policy==GrowthPolicy.APPEND) {
					break;
				}
			}
			this.chunks = chunks;
			this.chunkCount = chunkCount;
		}
		// If invalid chunk index was requested this will fail with IndexOutOfBoundsException
		return chunks[chunkIndex];
	}

	private boolean isUsed(int chunkIndex) {
		Chunk chunk = getChunkUnsafe(chunkIndex, GrowthPolicy.NO_GROWTH);
		return chunk!= null && !chunk.dead && chunk.liveSlots>0;
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
		MutableInteger result = new MutableInteger();
		sync.syncGlobal(() -> {
			int id = UNSET_INT;
			Chunk chunk;
			int rawSlotIndex;

			if(freeSlot!=UNSET_INT) {
				// Allocate slot and re-route marker
				id = freeSlot-1;

				// Chunk has been allocated before, so access must not fail
				chunk = getChunkUnsafe(chunkIndex(id), GrowthPolicy.NO_GROWTH);

				rawSlotIndex = rawSlotIndex(id);

				// New "free slot" will have been set by a previous free() call
				freeSlot = Bits.readInt(chunk.data, rawSlotIndex);
			} else {
				// Grab a new slot
				id = idGen.getAndIncrement();

				// Make sure we have the associated chunk in memory
				chunk = getChunkUnsafe(chunkIndex(id), GrowthPolicy.APPEND);
				rawSlotIndex = rawSlotIndex(id);
			}

			// Mark slot live
			Bits.writeInt(chunk.data, rawSlotIndex, SLOT_ALIVE);

			// Update live counter for chunk
			chunk.liveSlots++;

			slots++;
			result.setInt(id);
		});

		return result.intValue();
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

		sync.syncGlobal(() -> {
			sync.syncWrite(id, 0, 0, (chunk, index, v) -> {
				// Keep link to previous "empty slot" intact
				Bits.writeInt(chunk.data, rawSlotIndex(id), freeSlot);
				// Simply mark supplied id as the next free slot
				freeSlot = id+1;

				// Update live counter for chunk
				chunk.liveSlots--;
				checkState("Counter must not become negative", chunk.liveSlots>=0);
			});

			slots--;
		});
	}

	/**
	 * Discards all the internal data structures and effectively
	 * deletes all active slot allocations.
	 */
	public void clear() {
		sync.syncGlobal(() -> {
			for (int i = 0; i < chunkCount; i++) {
				chunks[i].dead = true;
				chunks[i] = null;
			}
			freeSlot = UNSET_INT;
			chunkCount = 0;
			idGen.set(0);
			slots = 0;
		});
	}

	/**
	 * Defaults to {@link #clear()}.
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() {
		clear();
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
		return chunkCount;
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

		MutableBoolean couldTrim = new MutableBoolean(false);

		sync.syncGlobal(() -> {
			while(chunkCount>0) {
				int chunkIndex = chunkCount-1;

				if(isUsed(chunkIndex)) {
					// If we encounter a chunk that's still in use we need to abort
					break;
				}

				// Delete the trailing chunk
				Chunk chunk = chunks[--chunkCount];
				chunk.dead = true;
				chunks[chunkCount] = null;
				// Adjust idGen
				idGen.set(chunkCount*getChunkSize());

				couldTrim.setBoolean(true);
			}
		});

		return couldTrim.booleanValue();
	}

	/**
	 * Increases the space available for every slot in this storage
	 * to {@code newSlotSize}.
	 * Does nothing if there are no entries currently.
	 * <p>
	 * This method should be used with great care and only when the caller
	 * can guarantee that there are no concurrent read or write operations
	 * being performed, as it manipulates internal fields that are generally
	 * not guarded against concurrent modification!
	 *
	 * @param newSlotSize
	 */
	public void adjustSlotSize(int size) {
		checkArgument("Slot size must not be less than "+MIN_SLOT_SIZE, size>=MIN_SLOT_SIZE);

		//TODO guard against overflowing the address space

		sync.syncGlobal(() -> {
			boolean grow = size>rawSlotSize;

			int oldSlotSize = slotSize;
			int newSlotSize = size + SLOT_HEADER_SIZE;

			slotSize = newSlotSize;
			rawSlotSize = size;

			// Nothing in storage
			if(chunkCount==0) {
				return;
			}

			// No live slots, so just erase current storage
			if(slots<=0) {
				clear();
				return;
			}

			int copySlotSize = grow ? oldSlotSize : newSlotSize;

			// Chunk by chunk adjust the content
			for (int i = 0; i < chunkCount; i++) {
				// Dummy id to make the syncWrite call fetch the right chunk
				int id = i*oldSlotSize;
				sync.syncWrite(id, 0, 0, (chunk, index, v) -> {
					chunk.dead = true;
					Chunk newChunk = new Chunk(chunkSize * newSlotSize);
					chunks[chunkIndex(id)] = newChunk;

					// Copy over chunk header data
					newChunk.liveSlots = chunk.liveSlots;

					// If chunk had no data previously and we have no free-list, we can ignore it
					if(freeSlot==UNSET_INT && newChunk.liveSlots<=0) {
						return;
					}

					int idxOld = 0;
					int idxNew = 0;

					// Now copy over all individual slots
					for (int j = 0; j < chunkSize; j++) {
						int header = Bits.readInt(chunk.data, idxOld);
						if(header==SLOT_ALIVE) {
							System.arraycopy(chunk.data, idxOld, newChunk.data, idxNew, copySlotSize);
						} else if(header>0) {
							Bits.writeInt(newChunk.data, idxNew, header);
						}

						idxOld += oldSlotSize;
						idxNew += slotSize;
					}
				});
			}
		});
	}

	/**
	 * Throws {@link IndexOutOfBoundsException} if {@code id < 0 || id >= chunkSize*chunkCount}
	 * or if {@code offset < 0 || offset >= rawSlotSize}
	 * @param id
	 * @param offset
	 */
	private void checkIdAndOffset(int id, int offset) {
		if(id<0 || id>=chunkSize*chunkCount)
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

	private void checkLiveSlot(Chunk chunk, int rawSlotIndex, int id) {
		if(chunk.dead)
			throw new IllegalStateException("Designated slot's host chunk alread marked as dead: "+id);
		if(Bits.readInt(chunk.data, rawSlotIndex)!=SLOT_ALIVE)
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
		return (byte) sync.syncRead(id, offset, (chunk, index) -> chunk.data[index]);
	}

	private static final ReadTask[] nBytesReaders = new ReadTask[9];
	private static final WriteTask[] nBytesWriters = new WriteTask[9];
	static {
		for (int i = 1; i < nBytesReaders.length; i++) {
			int slot = i;
			nBytesReaders[i] = (chunk, index) -> Bits.readNBytes(chunk.data, index, slot);
			nBytesWriters[i] = (chunk, index, v) -> Bits.writeNBytes(chunk.data, index, v, slot);
		}
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
		checkArgument(n>0 && n<9);
		checkBytesAvailable(offset, n);

		return sync.syncRead(id, offset, nBytesReaders[n]);
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
		checkBytesAvailable(offset, Short.BYTES);

		return (short) sync.syncRead(id, offset, (chunk, index) -> Bits.readShort(chunk.data, index));
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
		checkBytesAvailable(offset, Integer.BYTES);

		return (int) sync.syncRead(id, offset, (chunk, index) -> Bits.readInt(chunk.data, index));
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
		checkBytesAvailable(offset, Long.BYTES);

		return sync.syncRead(id, offset, (chunk, index) -> Bits.readLong(chunk.data, index));
	}

	// SETxxx methods

	public void setByte(int id, int offset, byte value) {
		checkIdAndOffset(id, offset);

		sync.syncWrite(id, offset, value,
				(chunk, index, v) -> chunk.data[index] = (byte)v);
	}

	public void setNBytes(int id, int offset, long value, int n) {
		checkArgument(n>0);
		checkBytesAvailable(offset, n);

		sync.syncWrite(id, offset, value, nBytesWriters[n]);
	}

	public void setShort(int id, int offset, short value) {
		checkBytesAvailable(offset, Short.BYTES);

		sync.syncWrite(id, offset, value,
				(chunk, index, v) -> Bits.writeShort(chunk.data, index, (short) v));
	}

	public void setInt(int id, int offset, int value) {
		checkBytesAvailable(offset, Integer.BYTES);

		sync.syncWrite(id, offset, value,
				(chunk, index, v) -> Bits.writeInt(chunk.data, index, (int) v));
	}

	public void setLong(int id, int offset, long value) {
		checkBytesAvailable(offset, Long.BYTES);

		sync.syncWrite(id, offset, value,
				(chunk, index, v) -> Bits.writeLong(chunk.data, index, v));
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
		requireNonNull(source);
		checkArgument(n>0 && n<=source.length);
		checkBytesAvailable(offset, n);

		sync.syncWrite(id, offset, 0, (chunk, index, v) ->
			System.arraycopy(source, 0, chunk.data, index, n));
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
		requireNonNull(destination);
		checkArgument(n>0 && n <= destination.length);
		checkBytesAvailable(offset, n);

		sync.syncRead(id, offset, (chunk, index) -> {
			System.arraycopy(chunk.data, index, destination, 0, n);
			return UNSET_LONG;
		});
	}

	public Cursor newCursor() {
		return new Cursor();
	}

	/**
	 * A convenient mechanism for moving around inside a {@link ByteAllocator}
	 * instance and to perform bulk operations on selected slots.
	 * <p>
	 * Note that cursor instances are designed to be used on a per-thread level!
	 * While the underlying read and write operations are thread-safe,
	 * {@link #moveTo(int) moving} a cursor isn't.
	 *
	 * If the {@link ByteAllocator} for a cursor
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
		private int rawSlotIndex = UNSET_INT;
		/**
		 * Buffer chunk to write into
		 */
		private Chunk chunk = null;

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
		 * <p>
		 * Note that allocating and moving are separate atomic operations.
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
		 * @see ByteAllocator#getChunkUnsafe(int, GrowthPolicy)
		 */
		public Cursor moveTo(int id) {
			if(id<IcarusUtils.UNSET_INT || id>=size())
				throw new IndexOutOfBoundsException("Slot id out of bounds: "+id);

			this.id = id;
			if(id==IcarusUtils.UNSET_INT) {
				rawSlotIndex = IcarusUtils.UNSET_INT;
				chunk = null;
			} else {
				chunk = getChunkUnsafe(chunkIndex(id), GrowthPolicy.NO_GROWTH);

				rawSlotIndex = rawSlotIndex(id);
				checkLiveSlot(chunk, rawSlotIndex, id);
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
			return (byte) sync.syncRead(this, offset, (chunk, index) -> chunk.data[index]);
		}

		public long getNBytes(int offset, int n) {
			checkChunkAvailable();
			checkOffset(offset);
			checkArgument(n>0 && n<=8);
			checkBytesAvailable(offset, n);

			return sync.syncRead(this, offset, nBytesReaders[n]);
		}

		public short getShort(int offset) {
			checkChunkAvailable();
			checkOffset(offset);
			return (short) sync.syncRead(this, offset, (chunk, index) ->
					Bits.readShort(chunk.data, index));
		}

		public int getInt(int offset) {
			checkChunkAvailable();
			checkOffset(offset);
			return (int) sync.syncRead(this, offset, (chunk, index) ->
				Bits.readInt(chunk.data, index));
		}

		public long getLong(int offset) {
			checkChunkAvailable();
			checkOffset(offset);
			return sync.syncRead(this, offset, (chunk, index) ->
				Bits.readLong(chunk.data, index));
		}

		// SETxxx methods

		public Cursor setByte(int offset, byte value) {
			checkChunkAvailable();
			checkOffset(offset);
			checkBytesAvailable(offset, 1);
			sync.syncWrite(this, offset, value, (chunk, index, v) -> chunk.data[index] = value);
			return this;
		}

		public Cursor setNBytes(int offset, long value, int n) {
			checkChunkAvailable();
			checkOffset(offset);
			checkArgument(n>0 && n<=8);
			checkBytesAvailable(offset, n);
			sync.syncWrite(this, offset, value, nBytesWriters[n]);
			return this;
		}

		public Cursor setShort(int offset, short value) {
			checkChunkAvailable();
			checkOffset(offset);
			checkBytesAvailable(offset, Short.BYTES);
			sync.syncWrite(this, offset, value, (chunk, index, v) ->
				Bits.writeShort(chunk.data, index, (short)v));
			return this;
		}

		public Cursor setInt(int offset, int value) {
			checkChunkAvailable();
			checkOffset(offset);
			checkBytesAvailable(offset, Integer.BYTES);
			sync.syncWrite(this, offset, value, (chunk, index, v) ->
				Bits.writeInt(chunk.data, index, (int)v));
			return this;
		}

		public Cursor setLong(int offset, long value) {
			checkChunkAvailable();
			checkOffset(offset);
			checkBytesAvailable(offset, Long.BYTES);
			sync.syncWrite(this, offset, value, (chunk, index, v) ->
				Bits.writeLong(chunk.data, index, v));
			return this;
		}

		// Buffer methods

		public Cursor writeBytes(int offset, byte[] bytes, int n) {
			checkChunkAvailable();
			requireNonNull(bytes);
			checkOffset(offset);
			checkArgument(n>0 && n<=bytes.length);
			checkBytesAvailable(offset, n);
			sync.syncWrite(this, offset, 0, (chunk, index, v) ->
				System.arraycopy(bytes, 0, chunk.data, index, n));
			return this;
		}

		public Cursor readBytes(int offset, byte[] bytes, int n) {
			checkChunkAvailable();
			requireNonNull(bytes);
			checkOffset(offset);
			checkArgument(n>0 && n<=bytes.length);
			checkBytesAvailable(offset, n);
			sync.syncRead(this, offset, (chunk, index) -> {
				System.arraycopy(chunk.data, index, bytes, 0, n);
				return UNSET_LONG;
			});
			return this;
		}
	}

	/**
	 * Data storage for a single chunk. This class extends {@link StampedLock} as a side
	 * effect of one of the supported {@link Sync} implementations. Since instances
	 * of this class are never exposed to client code and the {@code StampedLock} class is
	 * rather light-weight, this shouldn't create any issues.
	 *
	 * @author Markus Gärtner
	 *
	 */
	@SuppressWarnings("serial")
	private static class Chunk extends StampedLock {
		/** The actual slots */
		private final byte[] data;
		/** The current number of slots that are actually used */
		private volatile int liveSlots = 0;
		/** Switch to signal that this chunk should no longer be used */
		private volatile boolean dead = false;
		Chunk(int size) {
			data = new byte[size];
		}
	}

	@FunctionalInterface
	interface Task {
		void execute();
	}

	@FunctionalInterface
	interface WriteTask {
		void execute(Chunk chunk, int index, long value);
	}

	@FunctionalInterface
	interface ReadTask {
		long execute(Chunk chunk, int index);
	}

	public enum LockType {
		/** NO locking at all */
		NONE,
		/** Use native {@code synchronized} keyword for locking on global and chunk objects */
		NATIVE,
//		/** Use {@link ReentrantLock} or similar implementation to synchronize everything. */
//		LOCK,
		/** Use {@code synchronized} for global and {@link StampedLock} for chunk-level locking */
		OPTIMISTIC,
		/** Same as {@link #OPTIMISTIC} but with a higher threshold of failed optimistic attempts */
		HIGHLY_OPTIMISTIC,
		;
	}

	interface Sync {
		void syncGlobal(Task task);

		void syncWrite(int id, int offset, long value, WriteTask task);
		long syncRead(int id, int offset, ReadTask task);

		void syncWrite(Cursor cursor, int offset, long value, WriteTask task);
		long syncRead(Cursor cursor, int offset, ReadTask task);
	}

	/**
	 * Implements a strategy without any actual locking.
	 *
	 * @author Markus Gärtner
	 *
	 */
	class Unsafe implements Sync {

		@Override
		public void syncGlobal(Task task) {
			task.execute();
		}

		@Override
		public void syncWrite(int id, int offset, long value, WriteTask task) {
			checkIdAndOffset(id, offset);
			int chunkIndex = chunkIndex(id);
			int rawSlotIndex = rawSlotIndex(id);
			Chunk chunk = getChunkUnsafe(chunkIndex, GrowthPolicy.NO_GROWTH);
			checkLiveSlot(chunk, rawSlotIndex, id);
			task.execute(chunk, rawSlotIndex+SLOT_HEADER_SIZE+offset, value);
		}

		@Override
		public long syncRead(int id, int offset, ReadTask task) {
			checkIdAndOffset(id, offset);
			int chunkIndex = chunkIndex(id);
			int rawSlotIndex = rawSlotIndex(id);
			Chunk chunk = getChunkUnsafe(chunkIndex, GrowthPolicy.NO_GROWTH);
			checkLiveSlot(chunk, rawSlotIndex, id);
			return task.execute(chunk, rawSlotIndex+SLOT_HEADER_SIZE+offset);
		}

		@Override
		public void syncWrite(Cursor cursor, int offset, long value, WriteTask task) {
			checkLiveSlot(cursor.chunk, cursor.rawSlotIndex, cursor.id);
			task.execute(cursor.chunk, cursor.rawSlotIndex+SLOT_HEADER_SIZE+offset, value);
		}

		@Override
		public long syncRead(Cursor cursor, int offset, ReadTask task) {
			checkLiveSlot(cursor.chunk, cursor.rawSlotIndex, cursor.id);
			return task.execute(cursor.chunk, cursor.rawSlotIndex+SLOT_HEADER_SIZE+offset);
		}
	}

	/**
	 * Implements a strategy that does native locking via {@code synchronized}
	 * on global scale and relies on the {@link StampedLock} mechanics for
	 * synchronizing read and write operations on individual chunks.
	 * <p>
	 * Note that this implementation does <b>not</b> synchronize the read access
	 * to global fields in the read and write sync calls!!
	 *
	 * @author Markus Gärtner
	 *
	 */
	class Optimistic implements Sync {

		private final Object globalLock = new Object();

		/** Max optimistic repetitions before upgrading to full lock */
		private final int optimisticAttempts;

		public Optimistic(int optimisticAttempts) {
			checkArgument("Number of optimistic attempts must be greater than 0: "+optimisticAttempts, optimisticAttempts>0);
			this.optimisticAttempts = optimisticAttempts;
		}

		@Override
		public void syncGlobal(Task task) {
			synchronized (globalLock) {
				task.execute();
			}
		}

		@Override
		public void syncWrite(int id, int offset, long value, WriteTask task) {
			checkIdAndOffset(id, offset);
			int chunkIndex = chunkIndex(id);
			int rawSlotIndex = rawSlotIndex(id);
			Chunk chunk = getChunkUnsafe(chunkIndex, GrowthPolicy.NO_GROWTH);
			long stamp = chunk.writeLock();
			try {
				checkLiveSlot(chunk, rawSlotIndex, id);
				task.execute(chunk, rawSlotIndex+SLOT_HEADER_SIZE+offset, value);
			} finally {
				chunk.unlockWrite(stamp);
			}
		}

		@Override
		public long syncRead(int id, int offset, ReadTask task) {
			checkIdAndOffset(id, offset);
			int chunkIndex = chunkIndex(id);
			int rawSlotIndex = rawSlotIndex(id);
			Chunk chunk = getChunkUnsafe(chunkIndex, GrowthPolicy.NO_GROWTH);
			int index = rawSlotIndex+SLOT_HEADER_SIZE+offset;

			long stamp;
			int attempts = optimisticAttempts;

			while(attempts>0) {
				stamp = chunk.tryOptimisticRead();
				if(stamp!=0L) {
					checkLiveSlot(chunk, rawSlotIndex, id);
					long value = task.execute(chunk, index);
					if(chunk.validate(stamp)) {
						// All good, we got valid data, so exit out
						return value;
					}
				}
			}

			// At this point we exhausted our optimistic attempts -> upgrade to real lock
			stamp = chunk.readLock();
			try {
				checkLiveSlot(chunk, rawSlotIndex, id);
				return task.execute(chunk, index);
			} finally {
				chunk.unlockRead(stamp);
			}
		}

		@Override
		public void syncWrite(Cursor cursor, int offset, long value, WriteTask task) {
			Chunk chunk = cursor.chunk;
			long stamp = chunk.writeLock();
			try {
				checkLiveSlot(chunk, cursor.rawSlotIndex, cursor.id);
				task.execute(cursor.chunk, cursor.rawSlotIndex+SLOT_HEADER_SIZE+offset, value);
			} finally {
				chunk.unlockWrite(stamp);
			}
		}

		@Override
		public long syncRead(Cursor cursor, int offset, ReadTask task) {
			checkLiveSlot(cursor.chunk, cursor.rawSlotIndex, cursor.id);

			Chunk chunk = cursor.chunk;
			long stamp;
			int attempts = optimisticAttempts;

			while(attempts>0) {
				stamp = chunk.tryOptimisticRead();
				if(stamp!=0L) {
					checkLiveSlot(chunk, cursor.rawSlotIndex, cursor.id);
					long value = task.execute(cursor.chunk, cursor.rawSlotIndex+SLOT_HEADER_SIZE+offset);
					if(chunk.validate(stamp)) {
						// All good, we got valid data, so exit out
						return value;
					}
				}
			}

			// At this point we exhausted our optimistic attempts -> upgrade to real lock
			stamp = chunk.readLock();
			try {
				checkLiveSlot(chunk, cursor.rawSlotIndex, cursor.id);
				return task.execute(cursor.chunk, cursor.rawSlotIndex+SLOT_HEADER_SIZE+offset);
			} finally {
				chunk.unlockRead(stamp);
			}
		}
	}

	/**
	 * Implements a strategy based on the built-in {@code synchronized} keyword.
	 * Calls to {@link #syncGlobal(Task)} are synchronized on an internal lock object.
	 * Calls to all the other read and write methods are synchronized on the affected
	 * {@link Chunk} object, so as to reduce the scope of locking to a minimum and
	 * improve interference between operations on different chunks in the heap.
	 *
	 * @author Markus Gärtner
	 *
	 */
	class Native implements Sync {
		private final Object globalLock = new Object();

		@Override
		public void syncGlobal(Task task) {
			synchronized (globalLock) {
				task.execute();
			}
		}

		@Override
		public void syncWrite(int id, int offset, long value, WriteTask task) {
			int chunkIndex, rawSlotIndex;
			Chunk chunk;

			synchronized (globalLock) {
				checkIdAndOffset(id, offset);
				chunkIndex = chunkIndex(id);
				rawSlotIndex = rawSlotIndex(id);
				chunk = getChunkUnsafe(chunkIndex, GrowthPolicy.NO_GROWTH);
			}

			synchronized (chunk) {
				checkLiveSlot(chunk, rawSlotIndex, id);
				task.execute(chunk, rawSlotIndex+SLOT_HEADER_SIZE+offset, value);
			}
		}

		@Override
		public long syncRead(int id, int offset, ReadTask task) {
			int chunkIndex, rawSlotIndex;
			Chunk chunk;

			synchronized (globalLock) {
				checkIdAndOffset(id, offset);
				chunkIndex = chunkIndex(id);
				rawSlotIndex = rawSlotIndex(id);
				chunk = getChunkUnsafe(chunkIndex, GrowthPolicy.NO_GROWTH);
			}

			synchronized (chunk) {
				checkLiveSlot(chunk, rawSlotIndex, id);
				return task.execute(chunk, rawSlotIndex+SLOT_HEADER_SIZE+offset);
			}
		}

		@Override
		public void syncWrite(Cursor cursor, int offset, long value, WriteTask task) {
			synchronized (cursor.chunk) {
				checkLiveSlot(cursor.chunk, cursor.rawSlotIndex, cursor.id);
				task.execute(cursor.chunk, cursor.rawSlotIndex+SLOT_HEADER_SIZE+offset, value);
			}
		}

		@Override
		public long syncRead(Cursor cursor, int offset, ReadTask task) {
			synchronized (cursor.chunk) {
				checkLiveSlot(cursor.chunk, cursor.rawSlotIndex, cursor.id);
				return task.execute(cursor.chunk, cursor.rawSlotIndex+SLOT_HEADER_SIZE+offset);
			}
		}
	}
}
