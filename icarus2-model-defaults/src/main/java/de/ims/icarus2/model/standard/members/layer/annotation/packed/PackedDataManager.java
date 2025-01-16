/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.layer.annotation.packed;

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
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.apiguard.Api;
import de.ims.icarus2.apiguard.Api.ApiType;
import de.ims.icarus2.apiguard.Guarded;
import de.ims.icarus2.apiguard.Guarded.MethodType;
import de.ims.icarus2.apiguard.Mandatory;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.Part;
import de.ims.icarus2.util.Stats;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.collections.LazyMap;
import de.ims.icarus2.util.collections.LookupList;
import de.ims.icarus2.util.concurrent.CloseableThreadLocal;
import de.ims.icarus2.util.mem.ByteAllocator;
import de.ims.icarus2.util.mem.ByteAllocator.Cursor;
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
	 * Flag to indicate that any item for which a value is
	 * written into storage should first be registered
	 * automatically in the same atomic operation if has
	 * not been so previously.
	 */
	private final boolean autoRegister;

	/**
	 * Flag to signal that any time we encounter an item in a {@code getXXX} method
	 * that has not yet been registered or been written to, we should throw an
	 * exception.
	 */
	private final boolean failForUnwritten;

	private final int optimisticAttempts = 3; //TODO make customizable

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
	 * Maps individual items to their associated chunks of
	 * allocated data in the {@link #rawStorage raw storage}.
	 * <p>
	 * Value semantics:
	 * The stored index value based on an original {@code x} for a given {@code item} is:
	 * <ul>
	 * <li>{@code x} if the {@code item} has a valid value stored</li>
	 * <li>{@code -1} if {@code item} is not registered yet</li>
	 * <li>{@code -x-2} if the index is unused, i.e. the registered item has no value stored yes</li>
	 * </ul>
	 */
	private transient Object2IntMap<E> chunkAddresses;

	/**
	 * Lock for accessing the raw annotation storage and chunk mapping.
	 */
	private final StampedLock lock = new StampedLock();

	/** Delegates construction of the storage construction */
	private final IntFunction<ByteAllocator> storageSource;

	private final Stats<StatField> stats;

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

	private CloseableThreadLocal<ReadWriteProxy> readWriteProxy;

	protected PackedDataManager(Builder<E,O> builder) {
		requireNonNull(builder);

		initialCapacity = builder.getInitialCapacity();
		storageSource = builder.getStorageSource();
		weakKeys = builder.isWeakKeys();
		allowBitPacking = builder.isAllowBitPacking();
		allowDynamicChunkComposition = builder.isAllowDynamicChunkComposition();
		autoRegister = builder.isAutoRegister();
		failForUnwritten = builder.isFailForUnwritten();
		stats = builder.isCollectStats() ? new Stats<>(StatField.class) : null;

		readWriteProxy = CloseableThreadLocal.withInitial(
				() -> new ReadWriteProxy(rawStorage.newCursor()));

		packageHandles.addAll(builder.getHandles());
	}

	public boolean isAllowBitPacking() { return allowBitPacking; }

	public boolean isAllowDynamicChunkComposition() { return allowDynamicChunkComposition; }

	public boolean isAutoRegister() { return autoRegister; }

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
				int requiredSlotSize = updateHandles(0, 0);
				// Make sure we honor the minimum slot size, even if this means wasting memory
				requiredSlotSize = Math.max(requiredSlotSize, ByteAllocator.MIN_SLOT_SIZE);
				rawStorage = storageSource.apply(requiredSlotSize);
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

				readWriteProxy.close();
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

	//TODO rework the (un)registration process for items so that the manager keeps a usage counter

	/**
	 * Reserves a chunk of byte buffer for the specified {@code item}
	 * if it hasn't been registered already.
	 *
	 * @param item the item for which a chunk of byte buffer should be reserved
	 * @return {@code true} iff the item hasn't been registered already
	 */
	public boolean register(E item) {
		requireNonNull(item);

		long stamp = lock.writeLock();
		try {
			return registerUnsafe(item);
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	private boolean registerUnsafe(E item) {
		int id = chunkAddresses.getInt(item);

		boolean isNewItem = id==UNSET_INT;

		if(isNewItem) {
			id = rawStorage.alloc();
			if(id<0)
				throw new ModelException(ModelErrorCode.MODEL_CORRUPTED_STATE,
						"Failed to allocate slots for item: "+item);

			chunkAddresses.put(item, toggleId(id));
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

		ReadWriteProxy proxy = syncRead(item,
				(i, id, p) -> p.booleanValue = id!=UNSET_INT);
		return proxy.opSuccess && proxy.booleanValue;
	}

	public boolean isUsed(E item) {
		requireNonNull(item);

		// Our supplied lambda will only be called if the item is marked _written_
		ReadWriteProxy proxy = syncRead(item, (i, id, p) -> p.booleanValue = isMarkedWritten(id));
		return proxy.opSuccess && proxy.booleanValue;
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
			ReadWriteProxy proxy = getProxy();
			E item;
			while((item = items.get()) != null) {
				// We ignore all items that have no annotations assigned to them anyway
				int id = chunkAddressForRead(item);
				if(isMarkedWritten(id)) {
					proxy.cursor.moveTo(id);
					clearContent(handles, proxy);
				}
			}
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	/**
	 * Clears (i.e. sets to the respective {@code noEntryValue})
	 * all the annotations defined by the {@code handles} array
	 * for all currently registered items.
	 * <p>
	 * Note that this can be a rather costly operation.
	 *
	 * @param handles
	 */
	public void clear(PackageHandle[] handles) {
		requireNonNull(handles);
		checkArgument("Empty handles array", handles.length>0);

		long stamp = lock.writeLock();
		try {
			ReadWriteProxy proxy  = getProxy();
			for(IntIterator it = chunkAddresses.values().iterator(); it.hasNext();) {
				int id = it.nextInt();
				if(isMarkedWritten(id)) {
					proxy.cursor.moveTo(id);
					clearContent(handles, proxy);
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
	private void clearContent(PackageHandle[] handles, ReadWriteProxy proxy) {

		for(int i=handles.length-1; i>=0; i--) {
			PackageHandle handle = handles[i];
			handle.converter.setValue(handle, proxy.cursor, handle.noEntryValue);
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

	private boolean isMarkedWritten(int id) {
		return id>UNSET_INT;
	}

	private void record(StatField field) {
		if(stats!=null) {
			stats.count(field);
		}
	}

//	private void recordWrite() {
//		if(stats!=null) stats.count(StatField.WRITE);
//	}
//
//	private void recordOptimisticRead() {
//		if(stats!=null) stats.count(StatField.READ_OPTIMISTIC);
//	}
//
//	private void recordCompromisedRead() {
//		if(stats!=null) stats.count(StatField.READ_COMPROMISED);
//	}
//
//	private void recordLockedRead() {
//		if(stats!=null) stats.count(StatField.READ_LOCKED);
//	}
//
//	private void recordReadMiss() {
//		if(stats!=null) stats.count(StatField.READ_MISS);
//	}

	private void handleUnwritten(E item) {
		if(failForUnwritten) {
			throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE,
					"Given item has not been written to before: "+item);
		}

		record(StatField.READ_MISS);
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
	 * @see BytePackConverter#getBoolean(PackageHandle, ByteAllocator, int)
	 */
	public boolean getBoolean(E item, PackageHandle handle) {
		ReadWriteProxy proxy = syncRead(item, handle,
				(h, p) -> p.booleanValue = h.converter.getBoolean(h, p.cursor));
		return proxy.opSuccess ? proxy.booleanValue : ((Boolean)handle.noEntryValue).booleanValue();
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
	 * @see BytePackConverter#getInteger(PackageHandle, ByteAllocator, int)
	 */
	public int getInteger(E item, PackageHandle handle) {
		ReadWriteProxy proxy = syncRead(item, handle,
				(h, p) -> p.intValue = h.converter.getInteger(h, p.cursor));
		return proxy.opSuccess ? proxy.intValue : ((Integer)handle.noEntryValue).intValue();
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
	 * @see BytePackConverter#getLong(PackageHandle, ByteAllocator, int)
	 */
	public long getLong(E item, PackageHandle handle) {
		ReadWriteProxy proxy = syncRead(item, handle,
				(h, p) -> p.longValue = h.converter.getLong(h, p.cursor));
		return proxy.opSuccess ? proxy.longValue : ((Long)handle.noEntryValue).longValue();
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
	 * @see BytePackConverter#getFloat(PackageHandle, ByteAllocator, int)
	 */
	public float getFloat(E item, PackageHandle handle) {
		ReadWriteProxy proxy = syncRead(item, handle,
				(h, p) -> p.floatValue = h.converter.getFloat(h, p.cursor));
		return proxy.opSuccess ? proxy.floatValue : ((Float)handle.noEntryValue).floatValue();
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
	 * @see BytePackConverter#getDouble(PackageHandle, ByteAllocator, int)
	 */
	public double getDouble(E item, PackageHandle handle) {
		ReadWriteProxy proxy = syncRead(item, handle,
				(h, p) -> p.doubleValue = h.converter.getDouble(h, p.cursor));
		return proxy.opSuccess ? proxy.doubleValue : ((Double)handle.noEntryValue).doubleValue();
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
	 * @see BytePackConverter#getValue(PackageHandle, ByteAllocator, int)
	 */
	public Object getValue(E item, PackageHandle handle) {
		ReadWriteProxy proxy = syncRead(item, handle,
				(h, p) -> p.value = h.converter.getValue(h, p.cursor));
		return proxy.opSuccess && proxy.value!=null ? proxy.value : handle.getNoEntryValue();
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
	 * @see BytePackConverter#getValue(PackageHandle, ByteAllocator, int)
	 */
	public String getString(E item, PackageHandle handle) {
		ReadWriteProxy proxy = syncRead(item, handle,
				(h, p) -> p.value = h.converter.getString(h, p.cursor));
		return (String)(proxy.opSuccess && proxy.value!=null ? proxy.value : handle.getNoEntryValue());
	}

	// SetXXX methods

	private ReadWriteProxy getProxy() {
		return readWriteProxy.get();
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
	 * @see BytePackConverter#setBoolean(PackageHandle, ByteAllocator, int, boolean)
	 */
	public void setBoolean(E item, PackageHandle handle, boolean value) {
		ReadWriteProxy proxy = getProxy();
		proxy.booleanValue = value;
		syncWrite(item, handle, proxy, (h, p) -> h.converter.setBoolean(h, p.cursor, p.booleanValue));
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
	 * @see BytePackConverter#setInteger(PackageHandle, ByteAllocator, int, int)
	 */
	public void setInteger(E item, PackageHandle handle, int value) {
		ReadWriteProxy proxy = getProxy();
		proxy.intValue = value;
		syncWrite(item, handle, proxy, (h, p) -> h.converter.setInteger(h, p.cursor, p.intValue));
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
	 * @see BytePackConverter#setLong(PackageHandle, ByteAllocator, int, long)
	 */
	public void setLong(E item, PackageHandle handle, long value) {
		ReadWriteProxy proxy = getProxy();
		proxy.longValue = value;
		syncWrite(item, handle, proxy, (h, p) -> h.converter.setLong(h, p.cursor, p.longValue));
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
	 * @see BytePackConverter#setFloat(PackageHandle, ByteAllocator, int, float)
	 */
	public void setFloat(E item, PackageHandle handle, float value) {
		ReadWriteProxy proxy = getProxy();
		proxy.floatValue = value;
		syncWrite(item, handle, proxy, (h, p) -> h.converter.setFloat(h, p.cursor, p.floatValue));
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
	 * @see BytePackConverter#setDouble(PackageHandle, ByteAllocator, int, double)
	 */
	public void setDouble(E item, PackageHandle handle, double value) {
		ReadWriteProxy proxy = getProxy();
		proxy.doubleValue = value;
		syncWrite(item, handle, proxy, (h, p) -> h.converter.setDouble(h, p.cursor, p.doubleValue));
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
	 * @see BytePackConverter#setValue(PackageHandle, ByteAllocator, int, Object)
	 */
	public void setValue(E item, PackageHandle handle, @Nullable Object value) {
		if(value==null) {
			value = handle.noEntryValue;
		}
		ReadWriteProxy proxy = getProxy();
		proxy.value = value;
		syncWrite(item, handle, proxy, (h, p) -> h.converter.setValue(h, p.cursor, p.value));
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
	 * @see BytePackConverter#setValue(PackageHandle, ByteAllocator, int, Object)
	 */
	public void setString(E item, PackageHandle handle, @Nullable String value) {
		if(value==null) {
			value = (String) handle.noEntryValue;
		}
		ReadWriteProxy proxy = getProxy();
		proxy.value = value;
		syncWrite(item, handle, proxy, (h, p) -> h.converter.setString(h, p.cursor, (String) p.value));
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

		ReadWriteProxy proxy = syncRead(item, (i, id, p) -> {
			if(isMarkedWritten(id)) {
				// Make sure the buffer doesn't hold duplicates or stale information
				buffer.clear();
				p.cursor.moveTo(id);
				p.booleanValue = collectUsedHandlesUnsafe(i, handles, collector);
			} else {
				p.booleanValue = false;
			}
		});

		boolean result = proxy.opSuccess && proxy.booleanValue;

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
		int id = chunkAddressForRead(item);
		if(isMarkedWritten(id)) {
			ReadWriteProxy proxy = getProxy();
			proxy.cursor.moveTo(id);
			for(PackageHandle handle : handles) {
				// Fetch actual and the "default" value
				Object value = handle.converter.getValue(handle, proxy.cursor);
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

	public boolean isCollectStats() {
		return stats!=null;
	}

	private void checkCollectStats() {
		checkState("Not configured to record statistics", stats!=null);
	}

	public Stats<StatField> resetStats() {
		checkCollectStats();
		Stats<StatField> result = stats.clone();
		stats.reset();
		return result;
	}

	public Stats<StatField> getStats() {
		checkCollectStats();
		return stats.clone();
	}

	@FunctionalInterface
	private interface Task<E> {
		void execute(E item, int id, ReadWriteProxy proxy);
	}

	@FunctionalInterface
	private interface ReadTask {
		void execute(PackageHandle handle, ReadWriteProxy proxy);
	}

	@FunctionalInterface
	private interface WriteTask {
		void execute(PackageHandle handle, ReadWriteProxy proxy);
	}

	private void syncWrite(E item, PackageHandle handle, ReadWriteProxy proxy, WriteTask task) {
		requireNonNull(item);
		requireNonNull(handle);

		long stamp = lock.writeLock();
		try {
			if(autoRegister) {
				registerUnsafe(item);
			}
			int id = chunkAddressForWrite(item);
			proxy.cursor.moveTo(id);
			task.execute(handle, proxy);
			markUsed(item, id);
			record(StatField.WRITE);
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	private ReadWriteProxy syncRead(E item, PackageHandle handle, ReadTask task) {
		requireNonNull(item);
		requireNonNull(handle);
		// "task" is given by our methods and not forwarded user argument, so don't check it

		int id;
		ReadWriteProxy proxy = readWriteProxy.get();
		proxy.opSuccess = false;

		long stamp;
		int attempts = optimisticAttempts;

		// Try optimistically
		while(attempts-->0) {
			stamp = lock.tryOptimisticRead();
			if(stamp!=0L) {
				try {
					id = chunkAddressForRead(item);
					if(isMarkedWritten(id)) {
						proxy.cursor.moveTo(id);
						task.execute(handle, proxy);
						proxy.opSuccess = true;
						record(StatField.READ_OPTIMISTIC);
					} else {
						handleUnwritten(item);
					}
					if(lock.validate(stamp)) {
						// All good, we got valid data, so exit out
						return proxy;
					}
				} catch(RuntimeException e) {
					record(StatField.READ_COMPROMISED);
					// ignore exception, but switch to non-optimistic locking
					break;
				}
			}
		}

		proxy.opSuccess = false;

		// At this point we exhausted our optimistic attempts -> upgrade to real lock
		stamp = lock.readLock();
		try {
			id = chunkAddressForRead(item);
			if(isMarkedWritten(id)) {
				proxy.cursor.moveTo(id);
				task.execute(handle, proxy);
				proxy.opSuccess = true;
				record(StatField.READ_LOCKED);
			} else {
				handleUnwritten(item);
			}
			return proxy;
		} finally {
			lock.unlockRead(stamp);
		}
	}

	private ReadWriteProxy syncRead(E item, Task<E> task) {
		requireNonNull(item);

		ReadWriteProxy proxy = readWriteProxy.get();
		proxy.opSuccess = false;

		long stamp;
		int attempts = optimisticAttempts;

		// Try optimistically
		while(attempts-->0) {
			stamp = lock.tryOptimisticRead();
			if(stamp!=0L) {
				try {
					task.execute(item, chunkAddressForRead(item), proxy);
					proxy.opSuccess = true;
					record(StatField.READ_OPTIMISTIC);
					if(lock.validate(stamp)) {
						// All good, we got valid data, so exit out
						return proxy;
					}
				} catch(RuntimeException e) {
					record(StatField.READ_COMPROMISED);
					// ignore exception, but switch to non-optimistic locking
					break;
				}
			}
		}

		proxy.opSuccess = false;

		// At this point we exhausted our optimistic attempts -> upgrade to real lock
		stamp = lock.readLock();
		try {
			task.execute(item, chunkAddressForRead(item), proxy);
			proxy.opSuccess = true;
			record(StatField.READ_LOCKED);
			return proxy;
		} finally {
			lock.unlockRead(stamp);
		}
	}

	private static class ReadWriteProxy implements AutoCloseable {

		/** Cursor for interacting with the data storage */
		Cursor cursor;

		ReadWriteProxy(Cursor cursor) {
			this.cursor = requireNonNull(cursor);
		}

		@Override
		public void close() {
			cursor.clear();
			cursor = null;
			value = null;
		}

		/** Flag for read operations to signal that a valid value has been found */
		boolean opSuccess;

		// Buffers for transporting values between lambda layers
		Object value;
		int intValue;
		long longValue;
		float floatValue;
		double doubleValue;
		boolean booleanValue;
	}

	public static enum StatField {
		/** Full lock write operation */
		WRITE,
		/** Read operation with optimistic locking */
		READ_OPTIMISTIC,
		/** Read operation with full read lock */
		READ_LOCKED,
		/** Fail of an optimistic read due to error in underlying system */
		READ_COMPROMISED,
		/** Item not registered or not written */
		READ_MISS,
		;
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 * @param <E>
	 */
	@Api(type=ApiType.BUILDER)
	public static class Builder<E, O> extends AbstractBuilder<Builder<E,O>, PackedDataManager<E,O>> {

		private IntFunction<ByteAllocator> storageSource;

		private Integer initialCapacity;

		private Boolean allowBitPacking;

		private Boolean allowDynamicChunkComposition;

		private Boolean weakKeys;

		private Boolean autoRegister;

		private Boolean collectStats;

		private Boolean failForUnwritten;

		private Set<PackageHandle> handles = new ObjectOpenHashSet<>();

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public Builder<E,O> storageSource(IntFunction<ByteAllocator> storageSource) {
			requireNonNull(storageSource);
			checkState("Storage source already set", this.storageSource==null);

			this.storageSource = storageSource;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public IntFunction<ByteAllocator> getStorageSource() {
			return storageSource;
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public Builder<E,O> initialCapacity(int initialCapacity) {
			checkArgument("Initial capacity must be greater than 0", initialCapacity>0);
			checkState("Initial capacity already set", this.initialCapacity==null);

			this.initialCapacity = Integer.valueOf(initialCapacity);

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder<E,O> allowBitPacking(boolean allowBitPacking) {
			checkState("Flag 'allowBitPacking' already set", this.allowBitPacking==null);

			this.allowBitPacking = Boolean.valueOf(allowBitPacking);

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder<E,O> allowDynamicChunkComposition(boolean allowDynamicChunkComposition) {
			checkState("Flag 'allowDynamicChunkComposition' already set", this.allowDynamicChunkComposition==null);

			this.allowDynamicChunkComposition = Boolean.valueOf(allowDynamicChunkComposition);

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder<E,O> weakKeys(boolean weakKeys) {
			checkState("Flag 'weakKeys' already set", this.weakKeys==null);

			this.weakKeys = Boolean.valueOf(weakKeys);

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder<E,O> autoRegister(boolean autoRegister) {
			checkState("Flag 'autoRegister' already set", this.autoRegister==null);

			this.autoRegister = Boolean.valueOf(autoRegister);

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder<E,O> collectStats(boolean collectStats) {
			checkState("Flag 'collectStats' already set", this.collectStats==null);

			this.collectStats = Boolean.valueOf(collectStats);

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder<E,O> failForUnwritten(boolean failForUnwritten) {
			checkState("Flag 'failForUnwritten' already set", this.failForUnwritten==null);

			this.failForUnwritten = Boolean.valueOf(failForUnwritten);

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.GETTER, defaultValue="-1")
		public int getInitialCapacity() {
			return initialCapacity==null ? UNSET_INT : initialCapacity.intValue();
		}

		@Guarded(methodType=MethodType.GETTER, defaultValue="false")
		public boolean isAllowBitPacking() {
			return allowBitPacking==null ? false : allowBitPacking.booleanValue();
		}

		@Guarded(methodType=MethodType.GETTER, defaultValue="false")
		public boolean isAllowDynamicChunkComposition() {
			return allowDynamicChunkComposition==null ? false : allowDynamicChunkComposition.booleanValue();
		}

		@Guarded(methodType=MethodType.GETTER, defaultValue="false")
		public boolean isWeakKeys() {
			return weakKeys==null ? false : weakKeys.booleanValue();
		}

		@Guarded(methodType=MethodType.GETTER, defaultValue="false")
		public boolean isAutoRegister() {
			return autoRegister==null ? false : autoRegister.booleanValue();
		}

		@Guarded(methodType=MethodType.GETTER, defaultValue="false")
		public boolean isCollectStats() {
			return collectStats==null ? false : collectStats.booleanValue();
		}

		@Guarded(methodType=MethodType.GETTER, defaultValue="false")
		public boolean isFailForUnwritten() {
			return failForUnwritten==null ? false : failForUnwritten.booleanValue();
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
			checkArgument("Handles collection must not be empty", handles.size()>0);

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
