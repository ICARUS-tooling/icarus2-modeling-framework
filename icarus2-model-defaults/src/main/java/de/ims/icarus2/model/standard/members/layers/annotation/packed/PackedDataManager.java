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
package de.ims.icarus2.model.standard.members.layers.annotation.packed;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.Part;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.collections.LazyMap;
import de.ims.icarus2.util.collections.LookupList;
import de.ims.icarus2.util.mem.ByteAllocator;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * @author Markus Gärtner
 *
 * @param <E> type of elements that are used for mapping to byte chunks
 * @param <O> type of the owning context
 */
public class PackedDataManager<E extends Object, O extends Object> implements Part<O> {

	public static <E, O> Builder<E, O> builder() {
		return new Builder<>();
	}

	private static final Logger log = LoggerFactory.getLogger(PackedDataManager.class);

	/**
	 * Flag to indicate whether or not the chunk addressing should
	 * use weak references to {@link Item items} as keys for the mapping.
	 */
	private final boolean weakKeys;

	/**
	 * Hint on the initial capacity of the chunk address mapping structure.
	 */
	private final int initialCapacity;

	/**
	 * Flag to indicate whether the manager is allowed to cram
	 * multiple boolean values into a single byte.
	 */
	private final boolean allowBitPacking;

	/**
	 * Flag to indicate whether or not this manager supports
	 * dynamic add and remove of new annotation slots while
	 * the manager is active, i.e. when the underlying
	 * byte storage is live.
	 * <p>
	 * Note that this setting has serious influence on the
	 * expected performance! Modifying the current chunk
	 * composition results in a potentially very expensive
	 * duplication and reconstruction of the underlying byte
	 * storage and can block access for an extended period
	 * of time..
	 */
	private final boolean allowDynamicChunkComposition;

	/**
	 * Keeps track of the number of annotation storages using this manager.
	 * Allows lazy creation of the actual storage and to release the entire
	 * data once it is no longer needed.
	 */
	private final AtomicInteger useCounter = new AtomicInteger(0);

	/**
	 * Contains the actual raw data.
	 */
	private transient ByteAllocator rawStorage;

	/**
	 * For more efficient interaction with the raw storage
	 * in terms of locality.
	 */
	private transient ByteAllocator.Cursor cursor;

	/**
	 * Maps individual items to their associated chunks of
	 * allocated data in the {@link #rawStorage raw storage}.
	 * <p>
	 * Value semantics:
	 * The stored index value based on an original {@code x} is:
	 * <ul>
	 * <li>{@code x} if the index has a valid value</li>
	 * <li>{@code -x-2} if the index is unused, i.e. the registered item has no annotation yes</li>
	 * </ul>
	 */
	private transient Object2IntMap<E> chunkAddresses;

	/**
	 * Lock for accessing the raw annotation storage and chunk mapping.
	 */
	private final StampedLock lock = new StampedLock();

	/** Delegates construction of the storage construction */
	private final IntFunction<ByteAllocator> storageSource;

	/**
	 * The lookup structure for defining raw package handlers.
	 * To actually support growing and shrinking the underlying storage,
	 * we have to guarantee the following properties:
	 * <ul>
	 * <li>Handles will be registered and unregistered in groups</li>
	 * <li>All handles using full bytes to store data appear in the same order
	 * as their offsets in the storage</li>
	 * <li>Bit packing will always happen at the end of a group of handles</li>
	 * <li>After removing a group of handles, the highest offset for any remaining
	 * handle decides whether or not the storage can get shrunk.</li>
	 * </ul>
	 */
	private LookupList<PackageHandle> packageHandles = new LookupList<>();

	protected PackedDataManager(Builder<E,O> builder) {
		requireNonNull(builder);

		initialCapacity = builder.getInitialCapacity();
		storageSource = builder.getStorageSource();
		weakKeys = builder.isWeakKeys();
		allowBitPacking = builder.isAllowBitPacking();
		allowDynamicChunkComposition = builder.isAllowDynamicChunkComposition();

		packageHandles.addAll(builder.getHandles());
	}

	public void registerHandles(Collection<PackageHandle> handles) {
		requireNonNull(handles);
		checkState("Manager does not allow dynamic chunk composition", allowDynamicChunkComposition);
		checkArgument("No handles to register", !handles.isEmpty());

		int oldHandleCount = packageHandles.size();
		for(PackageHandle handle : handles) {
			if(packageHandles.contains(handle))
				throw new IllegalArgumentException("Duplicate handle: "+handle);
		}
		packageHandles.addAll(handles);

		if(rawStorage!=null) {
			int oldSlotSize = rawStorage.getSlotSize();
			int bytesIncrement = updateHandles(oldHandleCount, oldSlotSize);

			int newSlotSize = oldSlotSize + bytesIncrement;

			rawStorage.adjustSlotSize(newSlotSize);
		}
		// If no storage exists yet, we don't need to do anything
	}

	public void unregisterHandles(Collection<PackageHandle> handles) {
		requireNonNull(handles);
		checkState("Manager does not allow dynamic chunk composition", allowDynamicChunkComposition);
		checkArgument("No handles to unregister", !handles.isEmpty());

		for(PackageHandle handle : handles) {
			if(!packageHandles.contains(handle))
				throw new IllegalArgumentException("Unknown handle: "+handle);
		}
		packageHandles.removeAll(handles);


		if(rawStorage!=null) {
			if(packageHandles.isEmpty()) {
				rawStorage.clear();
			} else {
				int oldSlotSize = rawStorage.getSlotSize();

				// Figure out the minimum space we need
				int requiredSize = 0;
				for(PackageHandle handle : packageHandles) {
					int size = handle.getConverter().sizeInBytes();
					if(size==0) {
						size = handle.getConverter().sizeInBits()/8 + 1;
					}
					requiredSize = Math.max(requiredSize, handle.getOffset()+size+1);
				}

				// Only if we can actually safe space are we eating the resize cost
				if(requiredSize<oldSlotSize) {
					rawStorage.adjustSlotSize(requiredSize);
				}
			}
		}
		// If no storage exists yet, we don't need to do anything
	}

	/**
	 * Looks up the {@link PackageHandle handle} associated with the
	 * given {@code source} (by reference identity).
	 * <p>
	 * If no handle is available for the source, this method will
	 * return {@code null}.
	 *
	 * @param source
	 * @return
	 */
	public PackageHandle lookupHandle(Object source) {
		requireNonNull(source);

		for(PackageHandle handle : packageHandles) {
			if(source == handle.source) {
				return handle;
			}
		}

		return null;
	}

	/**
	 * Looks up all the {@link PackageHandle handles} associated with the specified
	 * {@code sources}.
	 *
	 * @param sources
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> Map<T, PackageHandle> lookupHandles(Set<T> sources) {
		requireNonNull(sources);
		checkArgument("Set of sources to look up must not be empty", !sources.isEmpty());

		LazyMap<T, PackageHandle> result = LazyMap.lazyHashMap(sources.size());

		for(PackageHandle handle : packageHandles) {
			if(sources.contains(handle.source)) {
				result.add((T) handle.source, handle);
			}
		}

		return result.getAsMap();
	}

	public boolean isWeakKeys() {
		return weakKeys;
	}

	/**
	 * Refresh the handles registered so far (set their indices, offsets
	 * and bit addresses) and return the total size in bytes required per
	 * slot.
	 *
	 * @param startOffset first byte available within a slot
	 * @param startIndex index of first handler to update
	 * @return
	 */
	private int updateHandles(int startOffset, int startIndex) {
		// Required size in full bytes
		int size = 0;

		Deque<PackageHandle> bitHandles = new ArrayDeque<>();

		int offset = 0;

		for(int i=startOffset; i<packageHandles.size(); i++) {
			PackageHandle handle = packageHandles.get(i);
			handle.setIndex(i);

			BytePackConverter converter = handle.converter;

			if(converter.sizeInBytes()==0) {
				if(!allowBitPacking)
					throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "No bit packing allowed");
				bitHandles.add(handle);
			} else {
				size += converter.sizeInBytes();

				handle.setOffset(offset);
				offset += converter.sizeInBytes();
			}
		}

		// Now package bit-based handles
		if(!bitHandles.isEmpty()) {
			// Total bytes used for bit-packaging
			int bytes = 1;
			// Bits within current byte already used for handles
			int bits = 0;

			while(!bitHandles.isEmpty()) {
				PackageHandle handle = bitHandles.remove();
				int bitSize = handle.converter.sizeInBits();
				if(bits+bitSize<=Byte.SIZE) {
					handle.setOffset(offset);
					handle.setBit(bits);
					bits += bitSize;
				} else {
					// Wait till we have space somewhere
					bits = 0;
					bytes++;
					offset++;
					bitHandles.offerLast(handle);
				}
			}

			size += bytes;
		}

		return size;
	}

	/**
	 * Initializes the following components if the method is invoked
	 * for the first time after the manager has been created or after
	 * it has been reset:
	 * <ul>
	 * <li>The mapping facility for translating items to their chunk index values.</li>
	 * <li>The raw byte storage containing all the actual data.</li>
	 * <li>The cursor used to navigate the raw storage.</li>
	 * </ul>
	 *
	 * @see de.ims.icarus2.util.Part#addNotify(java.lang.Object)
	 */
	@Override
	public void addNotify(@Nullable O owner) {
		if(useCounter.getAndIncrement()==0) {
			long stamp = lock.writeLock();
			try {
				chunkAddresses = buildMap();

				// Initialize a new storage based on our current chunk size
				rawStorage = storageSource.apply(updateHandles(0, 0));

				cursor = rawStorage.newCursor();
			} finally {
				lock.unlockWrite(stamp);
			}
		}
	}

	/**
	 * Releases the mapping facility, the raw data storage and
	 * the navigation cursor for that storage if this manager has
	 * no more {@code owner} instances linked to it.
	 *
	 * @see de.ims.icarus2.util.Part#removeNotify(java.lang.Object)
	 */
	@Override
	public void removeNotify(@Nullable O owner) {

		if(useCounter.decrementAndGet()==0) {
			long stamp = lock.writeLock();
			try {
				chunkAddresses.clear();
				chunkAddresses = null;

				rawStorage.clear();
				rawStorage = null;

				cursor.clear();
				cursor = null;
			} finally {
				lock.unlockWrite(stamp);
			}
		}
	}

	/**
	 * @see de.ims.icarus2.util.Part#isAdded()
	 */
	@Override
	public boolean isAdded() {
		return useCounter.get()>0;
	}

	protected Object2IntMap<E> buildMap() {
		if(isWeakKeys()) {
			log.info("No implementation for weak keys available yet - defaulting to regular map implementation");
		}

		Object2IntMap<E> result = new Object2IntOpenHashMap<>(initialCapacity);
		result.defaultReturnValue(UNSET_INT);

		return result;
	}

	// Item management

	/**
	 * Reserves a chunk of byte buffer for the specified {@code item}
	 * if it hasn't been registered already.
	 *
	 * @param item the item for which a chunk of byte buffer should be reserved
	 * @return {@code true} iff the item hasn't been registered already
	 */
	public boolean register(E item) {
		requireNonNull(item);

		boolean isNewItem;

		long stamp = lock.writeLock();
		try {
			int id = chunkAddresses.getInt(item);

			isNewItem = id==UNSET_INT;

			if(isNewItem) {
				id = rawStorage.alloc();

				chunkAddresses.put(item, toggleId(id));
			}
		} finally {
			lock.unlockWrite(stamp);
		}

		return isNewItem;
	}

	/**
	 * Releases the reserved chunk of byte buffer for the specified
	 * {@code item} if there has been one reserved previously.
	 *
	 * @param item
	 * @return
	 */
	public boolean unregister(E item) {
		requireNonNull(item);

		boolean wasKnownItem = false;

		long stamp = lock.writeLock();
		try {
			int id = chunkAddresses.removeInt(item);

			if(id!=UNSET_INT) {
				rawStorage.free(asWriteId(id));
				wasKnownItem = true;
			}
		} finally {
			lock.unlockWrite(stamp);
		}

		return wasKnownItem;
	}

	/**
	 * Releases the reserved chunks of byte buffer for elements
	 * returned by the given {@link Supplier}. The method returns
	 * the total number of successfully unregistered elements.
	 *
	 * @param item
	 * @return
	 */
	public int unregister(Supplier<? extends E> source) {
		requireNonNull(source);

		int removedItems = 0;

		long stamp = lock.writeLock();
		try {
			E item;

			// Continue as long as we get new items supplied
			while((item = source.get()) !=null) {
				int id = chunkAddresses.removeInt(item);

				// If item had a valid address, deallocate and count
				if(id!=UNSET_INT) {
					rawStorage.free(asWriteId(id));
					removedItems++;
				}
			}
		} finally {
			lock.unlockWrite(stamp);
		}

		return removedItems;
	}

	public boolean isRegistered(E item) {
		requireNonNull(item);

		boolean registered = false;

		// Try optimistically first
		long stamp = lock.tryOptimisticRead();
		if(stamp!=0L) {
			registered = chunkAddresses.containsKey(item);
		}

		// Run a real lock if needed
		if(!lock.validate(stamp)) {
			stamp = lock.readLock();
			try {
				registered = chunkAddresses.containsKey(item);
			} finally {
				lock.unlockRead(stamp);
			}
		}

		return registered;
	}

	/**
	 * Clears (i.e. sets to the respective {@code noEntryValue})
	 * all the annotations defined by the {@code handles} array
	 * for as long as the specified supplier produces entries
	 * that are different to {@code null}.
	 * <p>
	 * Ignores all provided items that are not mapped to an
	 * actual chunk id in this manager.
	 *
	 * @param ids
	 * @param handles
	 */
	public void clear(Supplier<? extends E> items, PackageHandle[] handles) {
		requireNonNull(items);
		requireNonNull(handles);
		checkArgument("Empty handles array", handles.length>0);

		long stamp = lock.writeLock();
		try {
			E item;
			while((item = items.get()) != null) {
				// We ignore all items that have no annotations assigned to them anyway
				if(prepareCursor(chunkAddressForRead(item))) {
					clearCurrent(handles);
				}
			}
		} finally {
			lock.unlockWrite(stamp);
		}
	}

//	/**
//	 * Clears (i.e. sets to the respective {@code noEntryValue})
//	 * all the annotations defined by the {@code handles} array
//	 * for as long as the specified supplier produces {@code ids}
//	 * that are different to {@link IcarusUtils#UNSET_INT -1}.
//	 *
//	 * @param ids
//	 * @param handles
//	 */
//	public void clear(IntSupplier ids, PackageHandle[] handles) {
//		requireNonNull(ids);
//		requireNonNull(handles);
//		checkArgument("Empty handles array", handles.length>0);
//
//		long stamp = lock.writeLock();
//		try {
//			int id;
//			while((id = ids.getAsInt()) != UNSET_INT) {
//				cursor.moveTo(id);
//				clearCurrent(handles);
//			}
//		} finally {
//			lock.unlockWrite(stamp);
//		}
//	}

	/**
	 * Clears (i.e. sets to the respective {@code noEntryValue})
	 * all the annotations defined by the {@code handles} array
	 * for all currently registered items.
	 *
	 * @param handles
	 */
	public void clear(PackageHandle[] handles) {
		requireNonNull(handles);
		checkArgument("Empty handles array", handles.length>0);

		long stamp = lock.writeLock();
		try {
			for(IntIterator it = chunkAddresses.values().iterator(); it.hasNext();) {
				int id = it.nextInt();
				if(id>UNSET_INT) {
					cursor.moveTo(id);
					clearCurrent(handles);
				}
			}
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	/**
	 * Sets the byte chunks for the specified
	 * handles to their respective noEntryValue
	 * representations.
	 * <p>
	 * Must be called under write lock.
	 */
	private void clearCurrent(PackageHandle[] handles) {

		for(int i=handles.length-1; i>=0; i--) {
			PackageHandle handle = handles[i];
			handle.converter.setValue(handle, cursor, handle.noEntryValue);
		}
	}

	private static int toggleId(int id) {
		return -id-2;
	}

	private static int asWriteId(int id) {
		return id<0 ? toggleId(id) : id;
	}

	private int chunkAddressForRead(E item) {
		return chunkAddresses.getInt(item);
	}

	private int chunkAddressForWrite(E item) {
		int id = chunkAddresses.getInt(item);
		return asWriteId(id);
	}

	private void markUsed(E item, int id) {
		chunkAddresses.put(item, asWriteId(id));
	}

	/**
	 * Shift cursor to the address of given item.
	 *
	 * @param item
	 * @return {@code true} iff given item had a valid address
	 */
	private boolean prepareCursor(int id) {
		boolean validId =  id>UNSET_INT;

		if(validId) {
			cursor.moveTo(id);
		}

		return validId;
	}

	// GetXXX methods

	/**
	 * Reads the stored annotation value specified by the given {@code handle}
	 * for the selected {@code item} as a {@code boolean}. This method internally
	 * calls the associated {@link BytePackConverter converter} and ensures
	 * proper synchronization.
	 *
	 * @param item target of the annotation
	 * @param handle the specification which annotation to access
	 * @return the annotation for {@code item} specified by {@code handle} as an {@code boolean}
	 *
	 * @see BytePackConverter#getBoolean(PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor)
	 */
	public boolean getBoolean(E item, PackageHandle handle) {

		boolean result = ((Boolean)handle.noEntryValue).booleanValue();

		long stamp = lock.tryOptimisticRead();

		if(stamp!=0L) {
			// Try optimistically
			if(prepareCursor(chunkAddressForRead(item))) {
				result = handle.converter.getBoolean(handle, cursor);
			}
		}

		// Do a real locking in case we encountered parallel modifications
		if(!lock.validate(stamp)) {
			stamp = lock.readLock();
			try {
				if(prepareCursor(chunkAddressForRead(item))) {
					result = handle.converter.getBoolean(handle, cursor);
				}
			} finally {
				lock.unlockRead(stamp);
			}
		}

		return result;
	}

	/**
	 * Reads the stored annotation value specified by the given {@code handle}
	 * for the selected {@code item} as an {@code int}. This method internally
	 * calls the associated {@link BytePackConverter converter} and ensures
	 * proper synchronization.
	 *
	 * @param item target of the annotation
	 * @param handle the specification which annotation to access
	 * @return the annotation for {@code item} specified by {@code handle} as an {@code int}
	 *
	 * @see BytePackConverter#getInteger(PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor)
	 */
	public int getInteger(E item, PackageHandle handle) {
		requireNonNull(item);

		int result = ((Integer)handle.noEntryValue).intValue();

		long stamp = lock.tryOptimisticRead();

		if(stamp!=0L) {
			// Try optimistically
			if(prepareCursor(chunkAddressForRead(item))) {
				result = handle.converter.getInteger(handle, cursor);
			}
		}

		// Do a real locking in case we encountered parallel modifications
		if(!lock.validate(stamp)) {
			stamp = lock.readLock();
			try {
				if(prepareCursor(chunkAddressForRead(item))) {
					result = handle.converter.getInteger(handle, cursor);
				}
			} finally {
				lock.unlockRead(stamp);
			}
		}

		return result;
	}

	/**
	 * Reads the stored annotation value specified by the given {@code handle}
	 * for the selected {@code item} as a {@code long}. This method internally
	 * calls the associated {@link BytePackConverter converter} and ensures
	 * proper synchronization.
	 *
	 * @param item target of the annotation
	 * @param handle the specification which annotation to access
	 * @return the annotation for {@code item} specified by {@code handle} as a {@code long}
	 *
	 * @see BytePackConverter#getLong(PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor)
	 */
	public long getLong(E item, PackageHandle handle) {
		requireNonNull(item);

		long result = ((Long)handle.noEntryValue).longValue();

		long stamp = lock.tryOptimisticRead();

		if(stamp!=0L) {
			// Try optimistically
			if(prepareCursor(chunkAddressForRead(item))) {
				result = handle.converter.getLong(handle, cursor);
			}
		}

		// Do a real locking in case we encountered parallel modifications
		if(!lock.validate(stamp)) {
			stamp = lock.readLock();
			try {
				if(prepareCursor(chunkAddressForRead(item))) {
					result = handle.converter.getLong(handle, cursor);
				}
			} finally {
				lock.unlockRead(stamp);
			}
		}

		return result;
	}

	/**
	 * Reads the stored annotation value specified by the given {@code handle}
	 * for the selected {@code item} as a {@code float}. This method internally
	 * calls the associated {@link BytePackConverter converter} and ensures
	 * proper synchronization.
	 *
	 * @param item target of the annotation
	 * @param handle the specification which annotation to access
	 * @return the annotation for {@code item} specified by {@code handle} as a {@code float}
	 *
	 * @see BytePackConverter#getFloat(PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor)
	 */
	public float getFloat(E item, PackageHandle handle) {
		requireNonNull(item);

		float result = ((Float)handle.noEntryValue).floatValue();

		long stamp = lock.tryOptimisticRead();

		if(stamp!=0L) {
			// Try optimistically
			if(prepareCursor(chunkAddressForRead(item))) {
				result = handle.converter.getFloat(handle, cursor);
			}
		}

		// Do a real locking in case we encountered parallel modifications
		if(!lock.validate(stamp)) {
			stamp = lock.readLock();
			try {
				if(prepareCursor(chunkAddressForRead(item))) {
					result = handle.converter.getFloat(handle, cursor);
				}
			} finally {
				lock.unlockRead(stamp);
			}
		}

		return result;
	}

	/**
	 * Reads the stored annotation value specified by the given {@code handle}
	 * for the selected {@code item} as a {@code double}. This method internally
	 * calls the associated {@link BytePackConverter converter} and ensures
	 * proper synchronization.
	 *
	 * @param item target of the annotation
	 * @param handle the specification which annotation to access
	 * @return the annotation for {@code item} specified by {@code handle} as a {@code double}
	 *
	 * @see BytePackConverter#getDouble(PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor)
	 */
	public double getDouble(E item, PackageHandle handle) {
		requireNonNull(item);

		double result = ((Double)handle.noEntryValue).doubleValue();

		long stamp = lock.tryOptimisticRead();

		if(stamp!=0L) {
			// Try optimistically
			if(prepareCursor(chunkAddressForRead(item))) {
				result = handle.converter.getDouble(handle, cursor);
			}
		}

		// Do a real locking in case we encountered parallel modifications
		if(!lock.validate(stamp)) {
			stamp = lock.readLock();
			try {
				if(prepareCursor(chunkAddressForRead(item))) {
					result = handle.converter.getDouble(handle, cursor);
				}
			} finally {
				lock.unlockRead(stamp);
			}
		}

		return result;
	}

	/**
	 * Reads the stored annotation value specified by the given {@code handle}
	 * for the selected {@code item}. This method internally
	 * calls the associated {@link BytePackConverter converter} and ensures
	 * proper synchronization.
	 *
	 * @param item target of the annotation
	 * @param handle the specification which annotation to access
	 * @return the annotation for {@code item} specified by {@code handle}
	 *
	 * @see BytePackConverter#getValue(PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor)
	 */
	public Object getValue(E item, PackageHandle handle) {
		requireNonNull(item);

		Object result = handle.noEntryValue;

		long stamp = lock.tryOptimisticRead();

		if(stamp!=0L) {
			// Try optimistically
			if(prepareCursor(chunkAddressForRead(item))) {
				result = handle.converter.getValue(handle, cursor);
			}
		}

		// Do a real locking in case we encountered parallel modifications
		if(!lock.validate(stamp)) {
			stamp = lock.readLock();
			try {
				if(prepareCursor(chunkAddressForRead(item))) {
					result = handle.converter.getValue(handle, cursor);
				}
			} finally {
				lock.unlockRead(stamp);
			}
		}

		return result;
	}

	/**
	 * Reads the stored annotation value specified by the given {@code handle}
	 * for the selected {@code item}. This method internally
	 * calls the associated {@link BytePackConverter converter} and ensures
	 * proper synchronization.
	 *
	 * @param item target of the annotation
	 * @param handle the specification which annotation to access
	 * @return the annotation for {@code item} specified by {@code handle}
	 *
	 * @see BytePackConverter#getValue(PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor)
	 */
	public String getString(E item, PackageHandle handle) {
		requireNonNull(item);

		String result = (String) handle.noEntryValue;

		long stamp = lock.tryOptimisticRead();

		if(stamp!=0L) {
			// Try optimistically
			if(prepareCursor(chunkAddressForRead(item))) {
				result = handle.converter.getString(handle, cursor);
			}
		}

		// Do a real locking in case we encountered parallel modifications
		if(!lock.validate(stamp)) {
			stamp = lock.readLock();
			try {
				if(prepareCursor(chunkAddressForRead(item))) {
					result = handle.converter.getString(handle, cursor);
				}
			} finally {
				lock.unlockRead(stamp);
			}
		}

		return result;
	}

	// SetXXX methods

	/**
	 * Changes the stored annotation value specified by the given {@code handle}
	 * for the selected {@code item}. This method internally
	 * calls the associated {@link BytePackConverter converter} and ensures
	 * proper synchronization.
	 *
	 * @param item target of the annotation
	 * @param handle the specification which annotation to access
	 * @param value the new annotation value to use
	 *
	 * @see BytePackConverter#setBoolean(PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor, boolean)
	 */
	public void setBoolean(E item, PackageHandle handle, boolean value) {
		requireNonNull(item);
		requireNonNull(handle);

		long stamp = lock.writeLock();
		try {
			int id = chunkAddressForWrite(item);
			if(prepareCursor(id)) {
				handle.converter.setBoolean(handle, cursor, value);
				markUsed(item, id);
			}
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	/**
	 * Changes the stored annotation value specified by the given {@code handle}
	 * for the selected {@code item}. This method internally
	 * calls the associated {@link BytePackConverter converter} and ensures
	 * proper synchronization.
	 *
	 * @param item target of the annotation
	 * @param handle the specification which annotation to access
	 * @param value the new annotation value to use
	 *
	 * @see BytePackConverter#setInteger(PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor, int)
	 */
	public void setInteger(E item, PackageHandle handle, int value) {
		requireNonNull(item);
		requireNonNull(handle);

		long stamp = lock.writeLock();
		try {
			int id = chunkAddressForWrite(item);
			if(prepareCursor(id)) {
				handle.converter.setInteger(handle, cursor, value);
				markUsed(item, id);
			}
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	/**
	 * Changes the stored annotation value specified by the given {@code handle}
	 * for the selected {@code item}. This method internally
	 * calls the associated {@link BytePackConverter converter} and ensures
	 * proper synchronization.
	 *
	 * @param item target of the annotation
	 * @param handle the specification which annotation to access
	 * @param value the new annotation value to use
	 *
	 * @see BytePackConverter#setLong(PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor, long)
	 */
	public void setLong(E item, PackageHandle handle, long value) {
		requireNonNull(item);
		requireNonNull(handle);

		long stamp = lock.writeLock();
		try {
			int id = chunkAddressForWrite(item);
			if(prepareCursor(id)) {
				handle.converter.setLong(handle, cursor, value);
				markUsed(item, id);
			}
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	/**
	 * Changes the stored annotation value specified by the given {@code handle}
	 * for the selected {@code item}. This method internally
	 * calls the associated {@link BytePackConverter converter} and ensures
	 * proper synchronization.
	 *
	 * @param item target of the annotation
	 * @param handle the specification which annotation to access
	 * @param value the new annotation value to use
	 *
	 * @see BytePackConverter#setFloat(PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor, float)
	 */
	public void setFloat(E item, PackageHandle handle, float value) {
		requireNonNull(item);
		requireNonNull(handle);

		long stamp = lock.writeLock();
		try {
			int id = chunkAddressForWrite(item);
			if(prepareCursor(id)) {
				handle.converter.setFloat(handle, cursor, value);
				markUsed(item, id);
			}
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	/**
	 * Changes the stored annotation value specified by the given {@code handle}
	 * for the selected {@code item}. This method internally
	 * calls the associated {@link BytePackConverter converter} and ensures
	 * proper synchronization.
	 *
	 * @param item target of the annotation
	 * @param handle the specification which annotation to access
	 * @param value the new annotation value to use
	 *
	 * @see BytePackConverter#setDouble(PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor, double)
	 */
	public void setDouble(E item, PackageHandle handle, double value) {
		requireNonNull(item);
		requireNonNull(handle);

		long stamp = lock.writeLock();
		try {
			int id = chunkAddressForWrite(item);
			if(prepareCursor(id)) {
				handle.converter.setDouble(handle, cursor, value);
				markUsed(item, id);
			}
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	/**
	 * Changes the stored annotation value specified by the given {@code handle}
	 * for the selected {@code item}. This method internally
	 * calls the associated {@link BytePackConverter converter} and ensures
	 * proper synchronization.
	 *
	 * @param item target of the annotation
	 * @param handle the specification which annotation to access
	 * @param value the new annotation value to use
	 *
	 * @see BytePackConverter#setValue(PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor, Object)
	 */
	public void setValue(E item, PackageHandle handle, @Nullable Object value) {
		requireNonNull(item);
		requireNonNull(handle);

		if(value==null) {
			value = handle.noEntryValue;
		}

		long stamp = lock.writeLock();
		try {
			int id = chunkAddressForWrite(item);
			if(prepareCursor(id)) {
				handle.converter.setValue(handle, cursor, value);
				markUsed(item, id);
			}
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	/**
	 * Changes the stored annotation value specified by the given {@code handle}
	 * for the selected {@code item}. This method internally
	 * calls the associated {@link BytePackConverter converter} and ensures
	 * proper synchronization.
	 *
	 * @param item target of the annotation
	 * @param handle the specification which annotation to access
	 * @param value the new annotation value to use
	 *
	 * @see BytePackConverter#setValue(PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor, Object)
	 */
	public void setString(E item, PackageHandle handle, @Nullable String value) {
		requireNonNull(item);
		requireNonNull(handle);

		if(value==null) {
			value = (String) handle.noEntryValue;
		}

		long stamp = lock.writeLock();
		try {
			int id = chunkAddressForWrite(item);
			if(prepareCursor(id)) {
				handle.converter.setString(handle, cursor, value);
				markUsed(item, id);
			}
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	// Utility

	/**
	 * Returns whether or not this manager contains any annotation values
	 * available. This method uses a somewhat simplistic approach and
	 * assumes that any registered item is meant to have at least one
	 * annotation associated with it.
	 * Therefore this implementation only checks whether or not the
	 * underlying lookup structure for mapping items to chunk ids
	 * contains at least one entry.
	 *
	 * @return
	 */
	public boolean hasValues() {
		return !chunkAddresses.isEmpty();
	}

	/**
	 * Forwards to the {@link #isRegistered(Item)} method to simply
	 * check if the given {@link Item} is known to this manager.
	 * Currently this method does not perform any real checks on the
	 * actual annotation values associated with the item!
	 *
	 * @param item
	 * @return
	 */
	public boolean hasValues(E item) {
		return isRegistered(item);
	}

	/**
	 * Checks whether the specified {@code item} has at least one
	 * valid annotation for any of the given {@link PackageHandle handles}.
	 * The optional {@code action} callback is used to report all the
	 * individual handles that were found to have annotations for
	 * the given {@code item}.
	 *
	 * @param item
	 * @param handles
	 * @param action
	 * @return
	 */
	public boolean collectUsedHandles(E item, Collection<PackageHandle> handles,
			@Nullable Consumer<? super PackageHandle> action) {
		requireNonNull(item);
		requireNonNull(handles);

		boolean result = false;

		/*
		 *  Using lazy collection can prevent necessity of creating real buffer.
		 *
		 *  IMPLEMENTATION NOTE:
		 *  We need this buffering due to optimistic locking approach. If we
		 *  optimistically call the original action callback and then realize
		 *  acquisition of the lock happened halfway through, we need to run
		 *  the same query again under a full read lock. This way we would
		 *  incorrectly report outdated/duplicate data to the callback.
		 */
		LazyCollection<PackageHandle> buffer = LazyCollection.lazySet();

		Consumer<PackageHandle> collector = action==null ? null : buffer;

		// Try to optimistically collect the information
		long stamp = lock.tryOptimisticRead();
		if(stamp!=0L) {
			result = collectUsedHandlesUnsafe(item, handles, collector);
		}

		// If we failed, go and properly lock before trying again
		if(!lock.validate(stamp)) {
			stamp = lock.readLock();
			try {
				// Make sure the buffer doesn't hold duplicates or stale information
				buffer.clear();
				result = collectUsedHandlesUnsafe(item, handles, collector);
			} finally {
				lock.unlockRead(stamp);
			}
		}

		// Don't forget to actually report the handles for which we found annotation values
		if(result && action!=null) {
			buffer.forEach(action);
		}

		return result;
	}

	private boolean collectUsedHandlesUnsafe(E item, Collection<PackageHandle> handles,
			Consumer<? super PackageHandle> action) {
		boolean result = false;

		// Move to data chunk and then go through all the specified handles
		if(prepareCursor(chunkAddressForRead(item))) {
			for(PackageHandle handle : handles) {
				// Fetch actual and the "default" value
				Object value = handle.converter.getValue(handle, cursor);
				Object noEntryValue = handle.noEntryValue;

				// If current value is different to default, report it
				if(!handle.converter.equal(value, noEntryValue)) {
					result = true;
					if(action!=null) {
						action.accept(handle);
					}
				}
			}
		}

		return result;
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 * @param <E>
	 */
	public static class Builder<E extends Object, O extends Object> extends AbstractBuilder<Builder<E,O>, PackedDataManager<E,O>> {

		private IntFunction<ByteAllocator> storageSource;

		private Integer initialCapacity;

		private Boolean allowBitPacking;

		private Boolean allowDynamicChunkComposition;

		private Boolean weakKeys;

		private Set<PackageHandle> handles = new ObjectOpenHashSet<>();

		public Builder<E,O> storageSource(IntFunction<ByteAllocator> storageSource) {
			requireNonNull(storageSource);
			checkState("Storage source already set", this.storageSource==null);

			this.storageSource = storageSource;

			return thisAsCast();
		}

		public IntFunction<ByteAllocator> getStorageSource() {
			return storageSource;
		}

		public Builder<E,O> initialCapacity(int initialCapacity) {
			checkArgument("Initial capacity must be greater than 0", initialCapacity>0);
			checkState("Initial capacity already set", this.initialCapacity==null);

			this.initialCapacity = Integer.valueOf(initialCapacity);

			return thisAsCast();
		}

		public Builder<E,O> allowBitPacking(boolean allowBitPacking) {
			checkState("Flag 'allowBitPacking' already set", this.allowBitPacking==null);

			this.allowBitPacking = Boolean.valueOf(allowBitPacking);

			return thisAsCast();
		}

		public Builder<E,O> allowDynamicChunkComposition(boolean allowDynamicChunkComposition) {
			checkState("Flag 'allowDynamicChunkComposition' already set", this.allowDynamicChunkComposition==null);

			this.allowDynamicChunkComposition = Boolean.valueOf(allowDynamicChunkComposition);

			return thisAsCast();
		}

		public Builder<E,O> weakKeys(boolean weakKeys) {
			checkState("Flag 'weakKeys' already set", this.weakKeys==null);

			this.weakKeys = Boolean.valueOf(weakKeys);

			return thisAsCast();
		}

		public int getInitialCapacity() {
			return initialCapacity==null ? 0 : initialCapacity.intValue();
		}

		public boolean isAllowBitPacking() {
			return allowBitPacking==null ? false : allowBitPacking.booleanValue();
		}

		public boolean isAllowDynamicChunkComposition() {
			return allowDynamicChunkComposition==null ? false : allowDynamicChunkComposition.booleanValue();
		}

		public boolean isWeakKeys() {
			return weakKeys==null ? false : weakKeys.booleanValue();
		}

		public Builder<E,O> addHandles(PackageHandle...handles) {
			requireNonNull(handles);
			checkArgument("Handles array must not be empty", handles.length>0);

			for(PackageHandle handle : handles) {
				if(!this.handles.add(handle))
					throw new ModelException(GlobalErrorCode.INVALID_INPUT,
							"Duplicate handle for "+handle.getSource());
			}

			return thisAsCast();
		}

		public Builder<E,O> addHandles(Collection<PackageHandle> handles) {
			requireNonNull(handles);
			checkArgument("Handles array must not be empty", handles.size()>0);

			for(PackageHandle handle : handles) {
				if(!this.handles.add(handle))
					throw new ModelException(GlobalErrorCode.INVALID_INPUT,
							"Duplicate handle for "+handle.getSource());
			}

			return thisAsCast();
		}

		public Set<PackageHandle> getHandles() {
			return Collections.unmodifiableSet(handles);
		}

		/**
		 * Sets the storage source to one that always returns a {@link ByteAllocator}
		 * with a chunk capacity of {@code 2^10} slots.
		 */
		public Builder<E, O> defaultStorageSource() {
			return storageSource(slotSize -> new ByteAllocator(slotSize, 10));
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#validate()
		 */
		@Override
		protected void validate() {
			super.validate();

			checkState("Missing storage source", storageSource!=null);
			checkState("Missing initial capacity", initialCapacity!=null);

			checkState("Must either provide initial package handles or allow dynamic registration",
					isAllowDynamicChunkComposition() || !handles.isEmpty());
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#create()
		 */
		@Override
		protected PackedDataManager<E,O> create() {
			return new PackedDataManager<>(this);
		}

	}
}
