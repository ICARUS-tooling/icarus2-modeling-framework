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
package de.ims.icarus2.filedriver.io;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.io.SynchronizedAccessor;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.Stats;
import de.ims.icarus2.util.io.resource.IOResource;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Models an I/O resource that reads from a single physical source and provides synchronized
 * accessors for both read and write operations. Data is read in blocks of a fixed
 * size, the value of which is determined at construction time by the respective
 * subclass. Blocks are then cached in a cache implementation also provided at
 * construction time.
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
public class BufferedIOResource {

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Maximum size of supported I/O objects, limited to 32 Gigabytes
	 */
	public static final long MAX_CHANNEL_SIZE = 1024L * 1024L * 1024L * 32L;

	/**
	 * Minimum size of cache blocks in bytes. Chosen to be {@value #MIN_BLOCK_SIZE}
	 * so that a channel within the size limitations of {@link #MAX_CHANNEL_SIZE} can still
	 * be addressed on the block level by means of integer values.
	 */
	public static final long MIN_BLOCK_SIZE = MAX_CHANNEL_SIZE/IcarusUtils.MAX_INTEGER_INDEX;

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

	public BufferedIOResource(IOResource resource, PayloadConverter payloadConverter,
			BlockCache cache, int cacheSize, int bytesPerBlock) {
		requireNonNull(resource);
		requireNonNull(payloadConverter);
		requireNonNull(cache);
		checkArgument(cacheSize>=0);

		this.resource = resource;
		this.payloadConverter = payloadConverter;
		this.cache = cache;
		this.cacheSize = cacheSize;
		this.bytesPerBlock = bytesPerBlock;
		stats = null;
	}

	private BufferedIOResource(Builder builder) {
		builder.validate();

		this.resource = builder.getResource();
		this.payloadConverter = builder.getPayloadConverter();
		this.cache = builder.getBlockCache();
		this.cacheSize = builder.getCacheSize();
		this.bytesPerBlock = builder.getBytesPerBlock();
		stats = builder.isCollectStats() ? new Stats<>(StatField.class) : null;
	}

	private StampedLock getLock() {
		return lock;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder()
		.append(getClass().getName())
		.append("[useCount=").append(useCount.get())
		.append(" bytesPerBlock=").append(bytesPerBlock)
		.append(" cacheSize=").append(cacheSize)
		.append(" bufferSize=").append(buffer==null ? -1 : buffer.capacity())
		.append(" pendingBlockCount==").append(changedBlocks.size())
		.append(" resource=").append(resource)
		.append(" payloadConverter=").append(payloadConverter)
		.append(" cache=").append(cache);

		return sb.append(']').toString();
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
	 *
	 * @throws IOException
	 */
	public void delete() throws IOException {
		syncWrite(() -> {
			closeUnsafe();
			resource.delete();
		});
	}

	private boolean readBlockUnsafe(Block block, long offset) throws IOException {
		try(SeekableByteChannel channel = resource.getReadChannel()) {

			// Read data from channel
			channel.position(offset);
			int bytesRead = channel.read(buffer);

			if(bytesRead==-1) {
				return false;
			}

			// Read entries from buffer
			int entriesRead = payloadConverter.read(block.data, buffer);

			// Save number of entries read (needed in case we're filling a recycled block)
			block.setSize(Math.max(entriesRead, 0));

			return entriesRead>0;
		}
	}

	private boolean writeBlockUnsafe(Block block, long offset) throws IOException {
		try(SeekableByteChannel channel = resource.getWriteChannel()) {

			// Write data to buffer
			payloadConverter.write(block.getData(), buffer, block.getSize());

			// Copy buffer to channel
			channel.position(offset);
			channel.write(buffer);

			return true;
		}
	}

	/**
	 * Stores all pending changes in the underlying resource.
	 *
	 * @throws IOException
	 */
	public void flush() throws IOException {
		syncWrite(this::flushUnsafe);
	}

	private void flushUnsafe() throws IOException {
		for(IntIterator it = changedBlocks.iterator(); it.hasNext(); ) {
			int id = it.nextInt();

			Block block = cache.getBlock(id);
			if(block==null)
				throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Missing block previously marked as locked: "+id);

			long offset = id*(long)bytesPerBlock;

			writeBlockUnsafe(block, offset);

			it.remove();
			block.unlock();
		}
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
		if(bytesPerBlock<0)
			throw new IllegalStateException("No block size defined - cannot allocate buffer");

		buffer = ByteBuffer.allocateDirect(bytesPerBlock);

		cache.open(cacheSize);
		resource.prepare();
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
		READ,
		WRITE,
		READ_ACCESSOR,
		WRITE_ACCESSOR,
		CACHE_MISS,
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
			this.data = data;
		}

		/**
		 * @return the number of <i>data points</i> currently stored in this block.
		 */
		public int getSize() {
			return size;
		}

		/**
		 * @return stored data, the type of which is depending on the implementation of the
		 * hosting {@link BufferedIOResource}.
		 */
		public Object getData() {
			return data;
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
	public final class ReadWriteAccessor implements SynchronizedAccessor<BufferedIOResource> {

		private long stamp;
		private final boolean readOnly;

		public ReadWriteAccessor(boolean readOnly) {
			this.readOnly = readOnly;

			incrementUseCount();
		}

		public boolean isReadOnly() {
			return readOnly;
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
				stamp = getLock().readLock();
			} else {
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

			if(readOnly) {
				getLock().unlockRead(stamp);
			} else {
				getLock().unlockWrite(stamp);
			}
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#close()
		 */
		@Override
		public void close() {
			decrementUseCount();
		}

		public void lockBlock(Block block) {
			changedBlocks.add(block.getId());
			block.lock();
		}

		/**
		 * Fetches a block from the cache, loading a new block if required.
		 * Note that this method will temporarily elevate the lock held by this
		 * accessor to a full write lock if needed (this is the case if this
		 * accessor only holds a read lock, but the method has to load a new block
		 * and therefore structurally modify internal buffer data).
		 * @param id
		 * @return
		 */
		public Block getBlock(int id) {
			// CHeap effort at MRU caching
			Block lastBlock = lastReturnedBlock;
			if(lastBlock!=null && lastBlock.getId()==id) {
				return lastBlock;
			}

			Block block = cache.getBlock(id);

			if(block==null) {
				// Upgrade the lock to write lock if needed
				stamp = getLock().tryConvertToWriteLock(stamp);
				try {
					// Automatic flushing if cache gets stale
					if(changedBlocks.size()>(cacheSize>>1)) {
						try {
							flushUnsafe();
						} catch (IOException e) {
							throw new ModelException(ModelErrorCode.DRIVER_INDEX_IO,
									"Failed to automatically flush index changes", e);
						}
					}

					// Byte offset of the beginning of the block to be read
					long offset = id*(long)bytesPerBlock;

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
	 *
	 * @author Markus Gärtner
	 *
	 * @param <B>
	 */
	public static class Builder extends AbstractBuilder<Builder, BufferedIOResource> {
		private int cacheSize;
		private int bytesPerBlock;
		private IOResource resource;
		private BlockCache blockCache;
		private PayloadConverter payloadConverter;

		private Boolean collectStats;

		protected Builder() {
			// no-op
		}

		public Builder cacheSize(int cacheSize) {
			checkArgument(cacheSize>=0);
			checkState(this.cacheSize==0);

			this.cacheSize = cacheSize;

			return thisAsCast();
		}

		public Builder bytesPerBlock(int bytesPerBlock) {
			checkArgument(bytesPerBlock>=0);
			checkState(this.bytesPerBlock==0);

			this.bytesPerBlock = bytesPerBlock;

			return thisAsCast();
		}

		public Builder resource(IOResource resource) {
			requireNonNull(resource);
			checkState(this.resource==null);

			this.resource = resource;

			return thisAsCast();
		}

		public Builder blockCache(BlockCache blockCache) {
			requireNonNull(blockCache);
			checkState(this.blockCache==null);

			this.blockCache = blockCache;

			return thisAsCast();
		}

		public Builder payloadConverter(PayloadConverter payloadConverter) {
			requireNonNull(payloadConverter);
			checkState(this.payloadConverter==null);

			this.payloadConverter = payloadConverter;

			return thisAsCast();
		}

		public Builder collectStats(boolean collectStats) {
			checkState("Flag 'collectStats' already set", this.collectStats==null);

			this.collectStats = Boolean.valueOf(collectStats);

			return thisAsCast();
		}

		public int getCacheSize() {
			return cacheSize;
		}

		public int getBytesPerBlock() {
			return bytesPerBlock;
		}

		public IOResource getResource() {
			return resource;
		}

		public BlockCache getBlockCache() {
			return blockCache;
		}

		public PayloadConverter getPayloadConverter() {
			return payloadConverter;
		}

		public boolean isCollectStats() {
			return collectStats==null ? false : collectStats.booleanValue();
		}

		@Override
		protected void validate() {
			checkState("Missing resource", resource!=null);
			checkState("Missing block cache", blockCache!=null);
			checkState("Missing payload converter", payloadConverter!=null);
			checkState("Negative cache size", cacheSize>=0);
			checkState("Bytes per block too small: "+bytesPerBlock, bytesPerBlock>=MIN_BLOCK_SIZE);
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
