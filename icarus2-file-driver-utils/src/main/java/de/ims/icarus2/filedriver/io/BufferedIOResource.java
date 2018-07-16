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
package de.ims.icarus2.filedriver.io;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
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
import de.ims.icarus2.model.api.io.resources.IOResource;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.IcarusUtils;
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

	public static Builder newBuilder() {
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
	private int lastReturnedBlockId = -1;

	public BufferedIOResource(IOResource resource, PayloadConverter payloadConverter, BlockCache cache, int cacheSize, int bytesPerBlock) {
		requireNonNull(resource);
		requireNonNull(payloadConverter);
		requireNonNull(cache);
		checkArgument(cacheSize>=0);

		this.resource = resource;
		this.payloadConverter = payloadConverter;
		this.cache = cache;
		this.cacheSize = cacheSize;
		this.bytesPerBlock = bytesPerBlock;
	}

	protected BufferedIOResource(Builder builder) {
		// TODO maybe redundant call or should leave it here for safety?
		builder.validate();

		this.resource = builder.getResource();
		this.payloadConverter = builder.getPayloadConverter();
		this.cache = builder.getBlockCache();
		this.cacheSize = builder.getCacheSize();
		this.bytesPerBlock = builder.getBytesPerBlock();
	}

	protected StampedLock getLock() {
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

	public int getCacheSize() {
		return cacheSize;
	}

	public long getBlockCount() throws IOException {
		return resource.size()/bytesPerBlock;
	}

	public final void delete() throws IOException {
		close();
		resource.delete();
	}

	public final void lockBlock(int id, Block block) {
		changedBlocks.add(id);
		block.lock();
	}

	public final void refreshBlockSize(Block block, int size) {
		if(size<0 || size>block.getSize()+1)
			throw new ModelException(ModelErrorCode.DRIVER_INDEX_WRITE_VIOLATION,
					"Entry index out of boundy for block: "+size+" - expected non negative value up to "+(block.getSize()+1)); //$NON-NLS-1$ //$NON-NLS-2$
		block.setSize(size);
	}

	public final Block getBlock(int id, boolean writeAccess) {
		if(id==lastReturnedBlockId && lastReturnedBlock!=null) {
			return lastReturnedBlock;
		}

		Block block = cache.getBlock(id);

		if(block==null) {

			// Automatic flushing if cache gets stale
			if(changedBlocks.size()>(cacheSize>>1)) {
				try {
					flush();
				} catch (IOException e) {
					throw new ModelException(ModelErrorCode.DRIVER_INDEX_IO,
							"Failed to automatically flush index changes", e); //$NON-NLS-1$
				}
			}

			// Byte offset of the beginning of the block to be read
			long offset = id*(long)bytesPerBlock;

			try {
				boolean exists = resource.size()>offset;

				// We can abort lookup if our desired offset is outside the channel bounds
				// and all we want to do is read data
				if(!exists && !writeAccess) {
					return null;
				}

				if(tmpBlock==null || tmpBlock.isLocked()) { // Technically it should never happen to have a block locked at this point
					tmpBlock = new Block(payloadConverter.newBlockData(bytesPerBlock));
				}

				block = tmpBlock;

				if(exists && !readBlock(block, offset)) {
					return null;
				}

				tmpBlock = cache.addBlock(block, id);
			} catch(IOException e) {
				throw new ModelException(ModelErrorCode.DRIVER_INDEX_IO,
						"Failed to read block "+id+" in resource "+resource, e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		lastReturnedBlock = block;
		lastReturnedBlockId = id;

		return block;
	}

	private boolean readBlock(Block block, long offset) throws IOException {
		try(SeekableByteChannel channel = resource.getReadChannel()) {

			// Read data from channel
			channel.position(offset);
			int bytesRead = channel.read(buffer);

			if(bytesRead==-1) {
				return false;
			}

			// Read entries from buffer
			int entriesRead = payloadConverter.read(block.data, buffer);

			// Save number of entries read
			//TODO evaluate if setting them even in case of empty block is desired
			block.setSize(Math.max(entriesRead, 0));

			return entriesRead>0;
		}
	}

	private boolean writeBlock(Block block, long offset) throws IOException {
		try(SeekableByteChannel channel = resource.getWriteChannel()) {

			// Write data to buffer
			payloadConverter.write(block.getData(), buffer, block.getSize());

			// Copy buffer to channel
			channel.position(offset);
			channel.write(buffer);

			return true;
		}
	}

	public void flush() throws IOException {

		for(IntIterator it = changedBlocks.iterator(); it.hasNext(); ) {
			int id = it.nextInt();

			Block block = cache.getBlock(id);
			if(block==null)
				throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Missing block previously marked as locked: "+id); //$NON-NLS-1$

			long offset = id*(long)bytesPerBlock;

			writeBlock(block, offset);

			it.remove();
			block.unlock();
		}
	}

	public final void incrementUseCount() {
		// Previous use count
		int count = useCount.getAndIncrement();

		if(count==0) {
			try {
				open();
			} catch (IOException e) {
				throw new ModelException(ModelErrorCode.DRIVER_INDEX_IO,
						"Failed to open managed resource", e); //$NON-NLS-1$
			}
		}
	}

	protected void open() throws IOException {
		if(bytesPerBlock<0)
			throw new IllegalStateException("No block size defined - cannot allocate buffer"); //$NON-NLS-1$

		//TODO add sanity check for upper limit of buffer size!!!

		buffer = ByteBuffer.allocateDirect(bytesPerBlock);

		cache.open(cacheSize);
		resource.prepare();
	}

	public final void decrementUseCount() {
		// New use count
		int count = useCount.decrementAndGet();

		if(count==0) {
			try {
				close();
			} catch (IOException e) {
				throw new ModelException(ModelErrorCode.DRIVER_INDEX_IO,
						"Failed to close managed resource", e); //$NON-NLS-1$
			}
		}
	}

	protected void close() throws IOException {

		//TODO having a corrupted accessor prevent resource from closing might pose problems?
		if(useCount.get()>0)
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					"Cannot close resource while there are still accessors using it");

		try {
			flush();
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
	 */
	public ReadWriteAccessor newAccessor(boolean readOnly) {
		return this.new ReadWriteAccessor(readOnly);
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

		void setSize(int size) {
			this.size = size;
		}

		void setData(Object data) {
			this.data = data;
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
	public interface BlockCache {

		public static final int MIN_CAPACITY = 100;

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
		 * @param block The block that was pushed out of the cache or {@code null}
		 * @param id
		 * @return the block that got removed from the cache in case the cache was full
		 * prior to calling this method.
		 * @throws IllegalStateException if the supplied block is already present in the cache
		 */
		Block addBlock(Block block, int id);

		/**
		 * Initialize storage and allocate basic resources.
		 */
		void open(int capacity);

		/**
		 * Discard any stored data and invalidate cache until
		 * {@link #open()} gets called.
		 */
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
	public class ReadWriteAccessor implements SynchronizedAccessor<BufferedIOResource> {

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
