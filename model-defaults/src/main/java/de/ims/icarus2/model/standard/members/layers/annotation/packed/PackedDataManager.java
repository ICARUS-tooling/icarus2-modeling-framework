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
package de.ims.icarus2.model.standard.members.layers.annotation.packed;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.Part;
import de.ims.icarus2.util.mem.ByteAllocator;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class PackedDataManager<E extends Item> implements Part<PackedDataDescriptor> {

	private static final Logger log = LoggerFactory.getLogger(PackedDataManager.class);

	private static final int NO_MAPPING_VALUE = IcarusUtils.UNSET_INT;

	/**
	 * Flag to indicate whether or not the chunk addressing should
	 * use weak references to {@link Item items} as keys for the mapping.
	 */
	private final boolean weakKeys;

	/**
	 * Hint on the initial capacity of the chunk address mapping structrue.
	 */
	private final int initialCapacity;

	/**
	 * Flag to indicate whether the manager is allowed to cram
	 * multiple boolean values into a single byte.
	 */
	private final boolean allowBitPacking;

	/**
	 * Flag to indicate whether or not this manager supports
	 * dynamic add and remove of new annotation slots.
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
	 */
	private transient Object2IntMap<E> chunkAddresses;

	/**
	 * Lock for accessing the raw annotation storage and chunk mapping.
	 */
	private final StampedLock lock = new StampedLock();

	private final Supplier<ByteAllocator> storageSource;

	/**
	 * The lookup structure for defining
	 */
	private PackageInfoLookup packageInfoLookup;

	public PackedDataManager(boolean weakKeys, int initialCapacity, Supplier<ByteAllocator> storageSource) {
		this.weakKeys = weakKeys;
		this.initialCapacity = initialCapacity;

		this.storageSource = requireNonNull(storageSource);
	}

	public boolean isWeakKeys() {
		return weakKeys;
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
	public void addNotify(PackedDataDescriptor owner) {
		if(useCounter.getAndIncrement()==0) {
			chunkAddresses = buildMap();

			rawStorage = storageSource.get();

			cursor = rawStorage.newCursor();
		}

		//TODO
	}

	/**
	 * Releases the mapping facility, the raw data storage and
	 * the navigation cursor for that storage if this manager has
	 * no more {@link AnnotationStorage} instances linked to it.
	 *
	 * @see de.ims.icarus2.util.Part#removeNotify(java.lang.Object)
	 */
	@Override
	public void removeNotify(PackedDataDescriptor owner) {
		if(useCounter.decrementAndGet()==0) {
			chunkAddresses.clear();
			chunkAddresses = null;

			rawStorage.clear();
			rawStorage = null;

			cursor.clear();
			cursor = null;
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
			log.info("No implementation for weak keys available so far");
		}

		Object2IntMap<E> result = new Object2IntOpenHashMap<>(1000); //FIXME needs some mechanism to properly estimate initial capacity!
		result.defaultReturnValue(NO_MAPPING_VALUE);

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

			isNewItem = id==NO_MAPPING_VALUE;

			if(isNewItem) {
				id = rawStorage.alloc();

				chunkAddresses.put(item, id);
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

		boolean wasKnownItem;

		long stamp = lock.writeLock();
		try {
			wasKnownItem = chunkAddresses.removeInt(item)!=NO_MAPPING_VALUE;
		} finally {
			lock.unlockWrite(stamp);
		}

		return wasKnownItem;
	}

	public boolean isRegistered(E item) {
		requireNonNull(item);

		long stamp = lock.tryOptimisticRead();
		boolean registered = chunkAddresses.containsKey(item);
		if(!lock.validate(stamp)) {
			lock.readLock();
			try {
				registered = chunkAddresses.containsKey(item);
			} finally {
				lock.unlockRead(stamp);
			}
		}

		return registered;
	}

	/**
	 * Buffer for reading byte chunks from the
	 * underlying raw storage. Per default this
	 * buffer is large enough to hold the byte
	 * sequence needed to store a single {@code long}
	 * value.
	 */
	private byte[] buffer = new byte[Long.BYTES];

	/**
	 * Reads {@code n} bytes from the active {@link ByteAllocator.Cursor}
	 * into the internal {@link #buffer}.
	 *
	 * @param n
	 */
	private void loadBuffer(int offset, int n) {
		cursor.readBytes(offset, buffer, n);
	}

	private PackageInfo getPackageInfo(Handle handle) {
		return packageInfoLookup.forHandle(handle);
	}

	private int getChunkAddress(E item) {
		int id = chunkAddresses.getInt(item);

		/*
		 *  SPECIAL NOTE: We shouldn't throw an exception here since
		 *  it would break our optimistic locking:
		 *
		 *  I
		 */


//		if(id==NO_MAPPING_VALUE)
//			throw new ModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
//					"Unregistered item: "+ModelUtils.toString(item));

		return id;
	}

	public int getInteger(E item, Handle handle) {

		PackageInfo packageInfo;
		int id;

		long stamp = lock.tryOptimisticRead();

		// Try optimistically
		packageInfo = getPackageInfo(handle);
		id = getChunkAddress(item);

		// Do a real locking in case we encountered modification
		if(!lock.validate(stamp)) {
			stamp = lock.readLock();
			try {
				packageInfo = getPackageInfo(handle);
				id = getChunkAddress(item);
			} finally {
				lock.unlockRead(stamp);
			}
		}

		// Now
	}

	public static final class Handle {

	}

	/**
	 * Central storage of all the important information about a single
	 * byte package, used for the lookup table.
	 *
	 * @author Markus Gärtner
	 *
	 */
	private static final class PackageInfo {

		/**
		 * Annotation key used for accessing this info
		 */
		private final String key;

		/**
		 * Position in the lookup table
		 */
		private final int index;

		/**
		 * The converter used to translate between raw byte data
		 * and the actual annotation types.
		 */
		private final BytePackConverter converter;

		private final Object converterContext;

		/**
		 * @param key
		 * @param index
		 * @param converter
		 */
		public PackageInfo(String key, int index, BytePackConverter converter) {
			checkArgument("Index must be positive", index>=0);

			this.key = requireNonNull(key);
			this.index = index;
			this.converter = requireNonNull(converter);
		}
	}

	/**
	 * Lookup structure for information on individual annotation keys.
	 *
	 * @author Markus Gärtner
	 *
	 */
	private static class PackageInfoLookup {
		private final Object2IntMap<Handle> indexMap = new Object2IntOpenHashMap<>();
		private final List<PackageInfo> packageInfos = new ArrayList<>();

		PackageInfoLookup() {
			indexMap.defaultReturnValue(NO_MAPPING_VALUE);
		}

		public PackageInfo forHandle(Handle handle) {
			int index = indexMap.getInt(handle);
			if(index==NO_MAPPING_VALUE)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Unknown handle: "+handle);
			return packageInfos.get(index);
		}

		public PackageInfo forIndex(int index) {
			return packageInfos.get(index);
		}
	}
}
