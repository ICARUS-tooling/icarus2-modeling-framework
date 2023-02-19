/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.filedriver.io;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.lang.Primitives._byte;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static java.util.Objects.requireNonNull;

import java.io.Flushable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.apiguard.Api;
import de.ims.icarus2.apiguard.Api.ApiType;
import de.ims.icarus2.apiguard.Guarded;
import de.ims.icarus2.apiguard.Guarded.MethodType;
import de.ims.icarus2.apiguard.Mandatory;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.io.SynchronizedAccessor;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.Stats;
import de.ims.icarus2.util.io.resource.IOResource;
import de.ims.icarus2.util.strings.ToStringBuilder;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Models an I/O resource that reads from a single physical source and provides synchronized
 * accessors for both read and write operations. Data is read in blocks of a fixed
 * size, the value of which is determined at construction time by the respective
 * subclass. Blocks are then cached in a cache implementation also provided at
 * construction time. The data source may also contain an optional header section of fixed
 * length that can be read from and written to by the {@link #newAccessor(boolean) accessors}
 * of the resource.
 * <p>
 * Synchronization in this class is not performed on a <i>per method</i> basis,
 * but rather as batches of operations surrounded by the appropriate synchronization
 * commands. All of the accessors ({@link ReadAccessor read} and {@link WriteAccessor write})
 * provided by this class have their {@link SynchronizedAccessor#begin() begin} and
 * {@link SynchronizedAccessor#end() end} methods linked to the shared lock of the file
 * resource. Beginning a batch operation by invoking {@link SynchronizedAccessor#begin() begin},
 * the calling thread effectively locks the respective lock and releases it with
 * {@link SynchronizedAccessor#end() end}.
 * <p>
 * <pre>
 * Reader reader = ...	// obtain reader
 * reader.begin();
 * try {
 *     // do something with reader
 *     ...
 * } finally {
 *     reader.end();
 * }
 * </pre>
 *
 * <p>
 * Note that there is no dedicated method for shutting down the resource. When the <i>use count</i> of
 * a managed resource reaches {@code 0} after {@link #decrementUseCount() decrementing it} the
 * resource will automatically flush pending changes and close caches and buffers.
 * {@link #incrementUseCount() Incrementing} the counter again (e.g. by requesting an accessor for
 * read or write operations) will then reopen those caches and buffers.
 * <p>
 * It is important to keep in mind that this implementation is <b>not</b> suited for arbitrary data,
 * but only for resources which follow a frame format with fixed frame size. Any change in the placement of
 * individual data frames in the physical resource would invalidate the entire access mechanism implemented
 * in this class!
 *
 * @author Markus Gärtner
 *
 */
public class BufferedIOResource implements Flushable {

	private static final Logger log = LoggerFactory.getLogger(BufferedIOResource.class);

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Maximum size of supported I/O objects, limited to 32 Gigabytes
	 */
	public static final long MAX_CHANNEL_SIZE = 1024L * 1024L * 1024L * 32L;

	/**
	 * Minimum size of cache blocks in bytes. Chosen to be {@code 16}
	 * so that a channel within the size limitations of {@link #MAX_CHANNEL_SIZE} can still
	 * be addressed on the block level by means of integer values.
	 */
	public static final int MIN_BLOCK_SIZE = strictToInt(MAX_CHANNEL_SIZE/IcarusUtils.MAX_INTEGER_INDEX);

	/**
	 * Originally was {@link ReentrantReadWriteLock} but the stamped locking version should
	 * provide better throughput and we also don't really need the reentrant capability.
	 */
	private final StampedLock lock = new StampedLock();

	/**
	 * Number of concurrently active accessors
	 */
	private final AtomicInteger useCount = new AtomicInteger();

	/**
	 * Actual resource to fetch physical data from
	 */
	private final IOResource resource;

	/**
	 * Intermediate buffer of blocks of physical data in respective internal representation
	 */
	private final BlockCache cache;

	/**
	 * Lookup for ids of blocks that have been modified.
	 */
	private final IntSet changedBlocks = new IntOpenHashSet(100);

	/**
	 * Physical size of a block to be read into cache
	 */
	private final int bytesPerBlock;

	/**
	 *  Size of cache upon which stale entries can be removed
	 */
	private final int cacheSize;

	/**
	 * Buffer for I/O operations on open channels from the underlying {@link IOResource}
	 */
	private ByteBuffer buffer;

	/**
	 * External processor to convert back and forth between binary form ({@link ByteBuffer})
	 * and the payload stored inside {@link Block} objects.
	 */
	private final PayloadConverter payloadConverter;

	/**
	 * Buffered instance to prevent unnecessary creation of new blocks.
	 * <p>
	 * When cache grows full or stale and old entries need to be purged, this field stores
	 * the result of a call to {@link BlockCache#addBlock(Block, int)}. When a cache miss
	 * occurs and a new block needs to be read from the underlying {@link IOResource} this
	 * buffered block will be used before falling back to creating a fresh new block object.
	 */
	private Block tmpBlock;

	private Block lastReturnedBlock = null;

	private final Stats<StatField> stats;

	private final Header header;

	private BufferedIOResource(Builder builder) {
		builder.validate();

		this.resource = builder.getResource();
		this.payloadConverter = builder.getPayloadConverter();
		this.cache = builder.getBlockCache();
		this.cacheSize = builder.getCacheSize();
		this.header = builder.getHeader();
		this.bytesPerBlock = builder.getBytesPerBlock();
		stats = builder.isCollectStats() ? new Stats<>(StatField.class) : null;
	}

	private StampedLock getLock() {
		return lock;
	}

	private void record(StatField field) {
		if(stats!=null) {
			stats.count(field);
		}
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

	@Override
	public String toString() {
		return ToStringBuilder.create(this)
		.add("useCount", useCount.get())
		.add("bytesPerBlock", bytesPerBlock)
		.add("cacheSize=", cacheSize)
		.add("bufferSize", buffer==null ? -1 : buffer.capacity())
		.add("pendingBlockCount", changedBlocks.size())
		.add("resource", resource)
		.add("payloadConverter", payloadConverter)
		.add("cache", cache)
		.build();
	}

	public IOResource getResource() {
		return resource;
	}

	/**
	 * @return the bytesPerBlock
	 */
	public int getBytesPerBlock() {
		return bytesPerBlock;
	}

	public int getUseCount() {
		return useCount.get();
	}

	public boolean isUsed() {
		return useCount.get()>0;
	}

	public int getCacheSize() {
		return cacheSize;
	}

	/**
	 * Tries to close down and then delete the underlying {@link #getResource() resource}.
	 * This method will fail in case there are still {@link ReadWriteAccessor accessors}
	 * active that potentially hold locks on this resource.
	 * <p>
	 * Note that this method must <b>not</b> be called from inside a {@link ReadWriteAccessor}'s
	 * {@link ReadWriteAccessor#begin() begin} ... {@link ReadWriteAccessor#end() end}
	 * code block!
	 *
	 * @throws IOException
	 */
	public void delete() throws IOException {
		syncWrite(() -> {
			closeUnsafe();
			resource.delete();
		});
	}

	/** Must be called under write lock */
	private boolean readBlockUnsafe(Block block, long offset) throws IOException {
		try(SeekableByteChannel channel = resource.getReadChannel()) {

			// Read data from channel
			channel.position(offset);
			buffer.clear();
			int bytesRead = channel.read(buffer);

			if(bytesRead==-1) {
				return false;
			}
			buffer.flip();

			// Read entries from buffer
			int entriesRead = payloadConverter.read(block.data, buffer);

			// Save number of entries read (needed in case we're filling a recycled block)
			block.setSize(Math.max(entriesRead, 0));

			return entriesRead>0;
		}
	}

	public Header getHeader() {
		return header;
	}

	private void writeHeaderUnsafe() throws IOException {
		if(header==null) {
			return;
		}

		ByteBuffer bb = ByteBuffer.allocate(headerBytes());
		try(SeekableByteChannel channel = resource.getReadChannel()) {
			header.save(bb);
			bb.flip();
			channel.position(0L);
			channel.write(bb);
		}
	}

	private void readHeaderUnsafe() throws IOException {
		if(header==null) {
			return;
		}

		ByteBuffer bb = ByteBuffer.allocate(headerBytes());
		try(SeekableByteChannel channel = resource.getReadChannel()) {
			channel.position(0L);
			channel.read(bb);
			bb.flip();

			// Only attempt to load if there is data available
			if(bb.hasRemaining()) {
				header.load(bb);
			}
		}
	}

	/**
	 * Returns whether or not there are blocks marked for serialization that haven't
	 * yet been flushed. This method is mainly intended as an additional tool to verify
	 * if flushing is required and/or if a performed call to {@link #flush()} actually
	 * covered all the pending blocks. No synchronization is done when fetching the result
	 * for this method, so client code is responsible for making sure that no structural
	 * modification (e.g. {@link ReadWriteAccessor#lockBlock(Block, int) locking} new blocks)
	 * happens concurrently!
	 *
	 * @return
	 */
	public boolean hasLockedBlocks() {
		return !changedBlocks.isEmpty();
	}

	/**
	 * Stores all pending changes in the underlying resource.
	 * <p>
	 * Note that this method must <b>not</b> be called from inside a {@link ReadWriteAccessor}'s
	 * {@link ReadWriteAccessor#begin() begin} ... {@link ReadWriteAccessor#end() end}
	 * code block!
	 *
	 * @throws IOException
	 */
	@Override
	public void flush() throws IOException {
		syncWrite(this::flushUnsafe);
	}

	private void flushUnsafe() throws IOException {
		if(!changedBlocks.isEmpty()) {
			record(StatField.FLUSH);
			try(SeekableByteChannel channel = resource.getWriteChannel()) {

				for(IntIterator it = changedBlocks.iterator(); it.hasNext(); ) {
					int id = it.nextInt();

					Block block = cache.getBlock(id);
					if(block==null)
						throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
								"Missing block previously marked as locked: "+id);

					long offset = offsetForBlock(id);


					// Write data to buffer
					buffer.clear();
					payloadConverter.write(block.getData(), buffer, block.getSize());
					buffer.flip();

					// Copy buffer to channel
					channel.position(offset);
					channel.write(buffer);

					it.remove();
					block.unlock();
				}
			}
		}

		writeHeaderUnsafe();
	}

	/**
	 * Executes the given {@code task} under write lock and forwards any occurring
	 *  {@link IOException} to the caller.
	 * @param task
	 * @throws IOException
	 */
	private void syncWrite(Task task) throws IOException {
		long stamp = getLock().writeLock();
		try {
			task.execute();
		} finally {
			getLock().unlockWrite(stamp);
		}
	}

	private void incrementUseCount() {
		// Previous use count
		int count = useCount.getAndIncrement();

		if(count==0) {
			try {
				syncWrite(this::openUnsafe);
			} catch (IOException e) {
				throw new ModelException(ModelErrorCode.DRIVER_INDEX_IO,
						"Failed to open managed resource", e);
			}
		}
	}

	private void openUnsafe() throws IOException {
		buffer = ByteBuffer.allocateDirect(bytesPerBlock);

		cache.open(cacheSize);
		resource.prepare();

		readHeaderUnsafe();
	}

	private void decrementUseCount() {
		// New use count
		int count = useCount.decrementAndGet();

		if(count==0) {
			try {
				syncWrite(this::closeUnsafe);
			} catch (IOException e) {
				throw new ModelException(ModelErrorCode.DRIVER_INDEX_IO,
						"Failed to close managed resource", e);
			}
		}
	}

	private void closeUnsafe() throws IOException {

		//TODO having a corrupted accessor prevent resource from closing might pose problems?
		if(useCount.get()>0)
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					"Cannot close resource while there are still accessors using it");

		try {
			flushUnsafe();
		} finally {
			buffer = null;
			tmpBlock = null;
			cache.close();
		}
	}

	private int headerBytes() {
		return header==null ? 0 : header.sizeInBytes();
	}

	private long offsetForBlock(int id) {
		return id*(long)bytesPerBlock + headerBytes();
	}

	/**
	 * Marks the given {@code block} as changed so that its content will be
	 * persisted when {@link BufferedIOResource#flush() flushing} the surrounding
	 * resource or when enough blocks get marked to warrant automatic flushing.
	 * <p>
	 * This method must be called inside a {@link #begin()} ... {@link #end()} code
	 * block and requires an accessor with write access!
	 * @param block the {@link Block} that was modified and should be marked for flushing
	 * @param size the portion of the block that has been modified. This information is
	 * used to update the internal {@link Block#getSize() size} information of the block.
	 */
	private void lockBlockUnsafe(Block block, int size) {
		int id = block.getId();
		if(id<0)
			throw new ModelException(ModelErrorCode.MODEL_CORRUPTED_STATE,
					"Cannot mark block for serialization - invalid id: "+id);

		record(StatField.BLOCK_MARK);

		if(block.isLocked()) {
			block.updateSize(size);
			return;
		}

		// Automatic flushing if cache gets crowded with locked blocks
		if(changedBlocks.size()>(cacheSize>>1)) {
			try {
				flushUnsafe();
			} catch (IOException e) {
				throw new ModelException(ModelErrorCode.DRIVER_INDEX_IO,
						"Failed to automatically flush index changes", e);
			}
		}

		block.lock();
		block.updateSize(size);

		// Ensure we have the block in cache (locking it will keep it there).
		cache.addBlock(block);
		changedBlocks.add(id);
	}

	/**
	 * Returns a new accessor for managing synchronization with this resource.
	 * Depending on the {@code readOnly} parameter the returned accessor will
	 * lock the resource either for writing or only read mode.
	 *
	 * @param readOnly
	 * @return
	 *
	 * @throws ModelException if an attempt is made to create a write accessor
	 * for a read-only resource
	 */
	public ReadWriteAccessor newAccessor(boolean readOnly) {
		if(!readOnly && !resource.getAccessMode().isWrite())
			throw new ModelException(GlobalErrorCode.NO_WRITE_ACCESS,
					"Cannot create write accessor for read-only resource");
		return this.new ReadWriteAccessor(readOnly);
	}

	@FunctionalInterface
	private interface Task {
		void execute() throws IOException;
	}

	public static enum StatField {
		READ_LOCK,
		WRITE_LOCK,
		READ_ACCESSOR,
		WRITE_ACCESSOR,
		FLUSH,
		BLOCK_LOOKUP,
		LAST_HIT,
		CACHE_MISS,
		BLOCK_MARK,
		;
	}

	public interface PayloadConverter {

		/**
		 * Write content of payload {@code source} into given {@link ByteBuffer buffer}.
		 * The method should write exactly {@code length} units from the payload object.
		 *
		 * @param source
		 * @param buffer
		 * @param length
		 * @throws IOException
		 */
		void write(Object source, ByteBuffer buffer, int length) throws IOException;

		/**
		 * Read the content of the given {@link ByteBuffer buffer} into provided
		 * payload {@code target} and return the number of units that got stored
		 * in payload object as a result of this method call.
		 * <p>
		 * Note that the meaning of "unit" here is implementation specific:
		 * An index that stores spans as tuples of values might consider a single
		 * span as unit, whereas an implementation that stores a huge list of
		 * parameter values will treat every single value as a separate unit!
		 *
		 * @param target
		 * @param buffer
		 * @return
		 * @throws IOException
		 */
		int read(Object target, ByteBuffer buffer) throws IOException;

		/**
		 * Creates a new payload object to be used inside a {@link Block}
		 * @return
		 */
		Object newBlockData(int bytesPerBlock);

	}

	/**
	 * A lockable chunk of raw data with a variable size.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static final class Block {

		// To check if a block differs from default
		private int size = 0;
		// Storage data
		private Object data;
		// Flag to prevent removal from cache
		private boolean locked;

		private int id = UNSET_INT;

		Block(Object data) {
			setData(data);
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return ToStringBuilder.create(this)
					.add("id", id)
					.add("size", size)
					.add("locked", locked)
					.add("data", data)
					.build();
		}

		/**
		 * @return the number of <i>data points</i> currently stored in this block.
		 */
		public int getSize() {
			return size;
		}

		/**
		 * Returns the payload of this block casted to the desired type.
		 *
		 * @return stored data, the type of which is depending on the implementation of the
		 * hosting {@link BufferedIOResource}.
		 */
		@SuppressWarnings("unchecked")
		public <T> T getData() {
			return (T) data;
		}

		/**
		 * @return {@code true} iff the block's data has been modified and locked to prevent
		 * the cache from discarding it before changes are made persistent to the underlying
		 * {@link IOResource} instance.
		 */
		public boolean isLocked() {
			return locked;
		}

		/**
		 * @return the id
		 */
		public int getId() {
			return id;
		}

		void setSize(int size) {
			this.size = size;
		}

		void updateSize(int size) {
			if(size>this.size) {
				this.size = size;
			}
		}

		void setData(Object data) {
			this.data = data;
		}

		void setId(int id) {
			this.id = id;
		}

		void lock() {
			locked = true;
		}

		void unlock() {
			locked = false;
		}
	}

	/**
	 * Caching strategy and storage for holding loaded {@link Block blocks} in memory.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface BlockCache extends AutoCloseable {

		public static final int MIN_CAPACITY = 32;

		/**
		 * Lookup the block stored for the specified {@code id}. If the cache
		 * does not contain such a block, return {@code null}.
		 *
		 * @param id
		 * @return
		 */
		Block getBlock(int id);

		/**
		 * Add the given {@code block} to the cache using the specified
		 * {@code id}. In case another block gets removed due the cache being full
		 * it should be returned.
		 *
		 * @param block the block to be added to the cache
		 * @param id
		 * @return the block that got removed from the cache in case the cache was full
		 * prior to calling this method or {@code null}.
		 * @throws IllegalStateException if the supplied block is already present in the cache
		 */
		Block addBlock(Block block);

		/**
		 * Initialize storage and allocate basic resources.
		 * The supplied {@code capacity} is meant as an upper limit of the size
		 * of the cache. Upon reaching this limit, the cache should start discarding
		 * unneeded entries when running out of space.
		 */
		void open(int capacity);

		/**
		 * Discard any stored data and invalidate cache until
		 * {@link #openUnsafe()} gets called.
		 */
		@Override
		void close();
	}


	/**
	 * Provides a basic implementation for synchronized read or write access to the data in
	 * the hosting {@link BufferedIOResource}. Creating an instance of this accessor
	 * will automatically increment the resource's use counter and closing it will
	 * then again decrement the counter.
	 *
	 * @author Markus Gärtner
	 *
	 * @param <T> The source type of the accessor
	 */
	public final class ReadWriteAccessor implements SynchronizedAccessor<BufferedIOResource>, Flushable {

		private long stamp;
		private final boolean readOnly;

		public ReadWriteAccessor(boolean readOnly) {
			this.readOnly = readOnly;

			incrementUseCount();
		}

		public boolean isReadOnly() {
			return readOnly;
		}

		private void checkWriteAccess() {
			if(isReadOnly())
				throw new ModelException(GlobalErrorCode.NO_WRITE_ACCESS,
						"Accessor is read-only, cannot structurally modify underlying buffer!");
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#getSource()
		 */
		@Override
		public BufferedIOResource getSource() {
			return BufferedIOResource.this;
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#begin()
		 */
		@Override
		public void begin() {
			if(readOnly) {
				record(StatField.READ_LOCK);
				stamp = getLock().readLock();
			} else {
				record(StatField.WRITE_LOCK);
				stamp = getLock().writeLock();
			}
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#end()
		 */
		@Override
		public void end() {
			long stamp = this.stamp;
			this.stamp = 0L;
			getLock().unlock(stamp);
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#close()
		 */
		@Override
		public void close() {
			decrementUseCount();
		}

		/**
		 * Same as {@link BufferedIOResource#flush()} but safe to call from inside a
		 * {@link #begin()} ... {@link #end()} code block.
		 * @throws IOException if an error occured while writing the block contents.
		 */
		@Override
		public void flush() throws IOException {
			checkWriteAccess();
			flushUnsafe();
		}

		/**
		 * Marks the given {@code block} as changed so that its content will be
		 * persisted when {@link BufferedIOResource#flush() flushing} the surrounding
		 * resource or when enough blocks get marked to warrant automatic flushing.
		 * <p>
		 * This method must be called inside a {@link #begin()} ... {@link #end()} code
		 * block and requires an accessor with write access!
		 * @param block the {@link Block} that was modified and should be marked for flushing
		 * @param size the portion of the block that has been modified. This information is
		 * used to update the internal {@link Block#getSize() size} information of the block.
		 */
		public void lockBlock(Block block, int size) {
			checkWriteAccess();
			lockBlockUnsafe(block, size);
		}

		/**
		 * Fetches a block from the cache, loading a new block if required.
		 * Note that this method will temporarily elevate the lock held by this
		 * accessor to a full write lock if needed (this is the case if this
		 * accessor only holds a read lock, but the method has to load a new block
		 * and therefore structurally modify internal buffer data).
		 * <p>
		 * This method must be called inside a {@link #begin()} ... {@link #end()} block!
		 *
		 * @param id
		 * @return
		 */
		public Block getBlock(int id) {
			record(StatField.BLOCK_LOOKUP);
			// Cheap effort at MRU caching
			Block lastBlock = lastReturnedBlock;
			if(lastBlock!=null && lastBlock.getId()==id) {
				record(StatField.LAST_HIT);
				return lastBlock;
			}

			Block block = cache.getBlock(id);

			if(block==null) {
				record(StatField.CACHE_MISS);
				// Upgrade the lock to write lock if needed
				stamp = getLock().tryConvertToWriteLock(stamp);
				try {
					// Byte offset of the beginning of the block to be read
					long offset = offsetForBlock(id);

					try {
						boolean exists = resource.size()>offset;

						// We can abort lookup if our desired offset is outside the channel bounds
						// and all we want to do is read data
						if(!exists && readOnly) {
							return null;
						}

						if(tmpBlock==null || tmpBlock.isLocked()) { // Technically it should never happen to have a block locked at this point
							tmpBlock = new Block(payloadConverter.newBlockData(bytesPerBlock));
						}

						block = tmpBlock;

						// Empty data -> bail
						if(exists && !readBlockUnsafe(block, offset)) {
							return null;
						}

						// Properly loaded data -> update id and cache it
						block.setId(id);
						tmpBlock = cache.addBlock(block);
					} catch(IOException e) {
						throw new ModelException(ModelErrorCode.DRIVER_INDEX_IO,
								"Failed to read block "+id+" in resource "+resource, e); //$NON-NLS-2$
					}
				} finally {
					// If this accessor is read-only, downgrade the lock again
					if(readOnly) {
						stamp = getLock().tryConvertToReadLock(stamp);
					}
				}
			}

			lastReturnedBlock = block;

			return block;
		}
	}

	/**
	 * Models interaction with the optional header section.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static abstract class Header {

		private final int sizeInBytes;

		public Header(int size) {
			checkArgument("Header size must be positive", size>0);
			this.sizeInBytes = size;
		}

		/** Returns the total size in bytes reserved by the header */
		public int sizeInBytes() {
			return sizeInBytes;
		}

		//TODO serialization

		protected abstract void load(ByteBuffer source);
		protected abstract void save(ByteBuffer target);
	}

	public static class SimpleHeader extends Header {

		/** We reserve slightly more space than needed to keep it extensible */
		public static final int RESERVED_SIZE = 64;

		private static final byte VERSION = 1;

		private static final int USED_SIZE = Byte.BYTES+5*Long.BYTES;

		static {
			assert USED_SIZE<=RESERVED_SIZE;
		}

		/** Number of entries */
		private long size = 0;
		private final Range used = new Range();
		private final Range target = new Range();

		public SimpleHeader() {
			super(RESERVED_SIZE);
		}

		protected byte version() {
			return VERSION;
		}

		/**
		 * @see de.ims.icarus2.filedriver.io.BufferedIOResource.Header#load(java.nio.ByteBuffer)
		 */
		@Override
		protected void load(ByteBuffer source) {
			byte version = source.get();
			// We're supposed to be backwards compatible, so only check if the stored data is too new
			if(version>version()) {
				log.warn("Incompatible header version detected: current version is %d, stored version %d",
						_byte(version()), _byte(version));
			}
			size = source.getLong();
			used.set(source.getLong(), source.getLong());
			target.set(source.getLong(), source.getLong());
		}

		/**
		 * @see de.ims.icarus2.filedriver.io.BufferedIOResource.Header#save(java.nio.ByteBuffer)
		 */
		@Override
		protected void save(ByteBuffer dest) {
			dest.put(VERSION);
			dest.putLong(size);
			dest.putLong(used.getMin());
			dest.putLong(used.getMax());
			dest.putLong(target.getMin());
			dest.putLong(target.getMax());
		}

		// LOOKUP BORDERS
		public long getSmallestUsedIndex() { return used.getMin(); }
		public long getLargestUsedIndex() { return used.getMax(); }
		public long getSmallestTargetIndex() { return target.getMin(); }
		public long getLargestTargetIndex() { return target.getMax(); }

		// UPDATE RANGES
		/** Update range of used source indices */
		public void updateUsedIndex(long value) { used.update(value); }
		/** Update range of used target indices */
		public void updateTargetIndex(long value) { target.update(value); }
		/** Increase size by {@code 1} */
		public void growSize() { size++; }

		// CHECK USAGE
		public boolean isUsedIndex(long value) { return used.contains(value); }
		public boolean isUsedTarget(long value) { return target.contains(value); }

		// FETCH LIMITED RANGES
		/** Calculates and returns the intersection of given span and stored range of soruce indices */
		public Range getUsedIndices(long fromIndex, long toIndex) {
			Range range = new Range().set(fromIndex, toIndex);
			range.limit(used);
			return range;
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 * @param <B>
	 */
	@Api(type = ApiType.BUILDER)
	public static class Builder extends AbstractBuilder<Builder, BufferedIOResource> {
		private int cacheSize = 0;
		private int bytesPerBlock = 0;
		private IOResource resource;
		private BlockCache blockCache;
		private PayloadConverter payloadConverter;
		private Header header;

		private Boolean collectStats;

		protected Builder() {
			// no-op
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public Builder cacheSize(int cacheSize) {
			checkArgument("Cache size msut be positive", cacheSize>0);
			checkState(this.cacheSize==0);

			this.cacheSize = cacheSize;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public Builder bytesPerBlock(int bytesPerBlock) {
			checkArgument("Bytes per block must be positive", bytesPerBlock>=0);
			checkArgument("Bytes per block too small: "+bytesPerBlock+" minimum: "+MIN_BLOCK_SIZE,
					bytesPerBlock>=MIN_BLOCK_SIZE);
			checkState(this.bytesPerBlock==0);

			this.bytesPerBlock = bytesPerBlock;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder header(Header header) {
			requireNonNull(header);
			checkState(this.header==null);

			this.header = header;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public Builder resource(IOResource resource) {
			requireNonNull(resource);
			checkState(this.resource==null);

			this.resource = resource;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public Builder blockCache(BlockCache blockCache) {
			requireNonNull(blockCache);
			checkState(this.blockCache==null);

			this.blockCache = blockCache;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public Builder payloadConverter(PayloadConverter payloadConverter) {
			requireNonNull(payloadConverter);
			checkState(this.payloadConverter==null);

			this.payloadConverter = payloadConverter;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder collectStats(boolean collectStats) {
			checkState("Flag 'collectStats' already set", this.collectStats==null);

			this.collectStats = Boolean.valueOf(collectStats);

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.GETTER, defaultValue="0")
		public int getCacheSize() {
			return cacheSize;
		}

		@Guarded(methodType=MethodType.GETTER, defaultValue="0")
		public int getBytesPerBlock() {
			return bytesPerBlock;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public IOResource getResource() {
			return resource;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public BlockCache getBlockCache() {
			return blockCache;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public PayloadConverter getPayloadConverter() {
			return payloadConverter;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public Header getHeader() {
			return header;
		}

		@Guarded(methodType=MethodType.GETTER, defaultValue="false")
		public boolean isCollectStats() {
			return collectStats==null ? false : collectStats.booleanValue();
		}

		@Override
		protected void validate() {
			checkState("Missing resource", resource!=null);
			checkState("Missing block cache", blockCache!=null);
			checkState("Missing payload converter", payloadConverter!=null);
			checkState("Missing cache size", cacheSize>0);
			checkState("Missing bytes per block value", bytesPerBlock>0);
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#create()
		 */
		@Override
		protected BufferedIOResource create() {
			return new BufferedIOResource(this);
		}
	}
}
