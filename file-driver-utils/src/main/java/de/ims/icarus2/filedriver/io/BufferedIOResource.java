/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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
import static de.ims.icarus2.util.Conditions.checkNotNull;
import static de.ims.icarus2.util.Conditions.checkState;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.io.SynchronizedAccessor;
import de.ims.icarus2.model.api.io.resources.IOResource;
import de.ims.icarus2.util.IcarusUtils;

/**
 * Models an I/O resource that reads from a single source and provides synchronized
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
 *     Reader reader = ...	// obtain reader
 *     reader.begin();
 *     try {
 *         // do something with reader
 *         ...
 *     } finally {
 *         reader.end();
 *     }
 * </pre>
 *
 * <p>
 * Note that there is no dedicated method for shutting down the resource. When the <i>use count</i>
 * a managed resource reaches {@code 0} after {@link #decrementUseCount() decrementing it} the
 * resource will automatically flush pending changes and close caches and buffers.
 * {@link #incrementUseCount() Incrementing} the counter again (e.g. by requesting an accessor for
 * read or write operations) will then reopen those caches and buffers.
 *
 * @author Markus Gärtner
 *
 */
public abstract class BufferedIOResource {

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

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final AtomicInteger useCount = new AtomicInteger();

	private final IOResource resource;

	private final BlockCache cache;

	private final TIntSet changedBlocks = new TIntHashSet();

	private int bytesPerBlock = -1;

	// Size of cache upon which stale entries can be removed
	private final int cacheSize;

	private ByteBuffer buffer;
	private Block tmpBlock;

	protected BufferedIOResource(IOResource resource, BlockCache cache, int cacheSize) {
		checkNotNull(resource);
		checkNotNull(cache);
		checkArgument(cacheSize>=0);

		this.resource = resource;
		this.cache = cache;
		this.cacheSize = cacheSize;
	}

	protected BufferedIOResource(BufferedIOResourceBuilder<?> builder) {
		// TODO maybe redundant call or should leave for safety?
		builder.validate();

		this.resource = builder.getResource();
		this.cache = builder.getBlockCache();
		this.cacheSize = builder.getCacheSize();
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
		.append(" cache=").append(cache);

		toString(sb);

		return sb.append(']').toString();
	}

	protected void toString(StringBuilder sb) {
		// for subclasses
	}

	protected void setBytesPerBlock(int bytesPerBlock) {
		// Ensure we never have to worry about block id values
		// exceeding Integer.MAX_VALUE
		if(bytesPerBlock<MIN_BLOCK_SIZE)
			throw new IllegalArgumentException("Invalid block size: "+bytesPerBlock+" - minimum size is "+MIN_BLOCK_SIZE); //$NON-NLS-1$ //$NON-NLS-2$

		this.bytesPerBlock = bytesPerBlock;
	}

	public IOResource getResource() {
		return resource;
	}

	public final Lock getReadLock() {
		return lock.readLock();
	}

	public final Lock getWriteLock() {
		return lock.writeLock();
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

	protected final void lockBlock(int id, Block block) {
		changedBlocks.add(id);
		block.lock();
	}

	protected final void refreshBlockSize(Block block, int size) {
		if(size<0 || size>block.getSize()+1)
			throw new ModelException(ModelErrorCode.DRIVER_INDEX_WRITE_VIOLATION,
					"Entry index out of boundy for block: "+size+" - expected non negative value up to "+(block.getSize()+1)); //$NON-NLS-1$ //$NON-NLS-2$
		block.setSize(size);
	}

	protected final Block getBlock(int id, boolean writeAccess) {
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

				if(tmpBlock==null) {
					tmpBlock = new Block(newBlockData());
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
			int entriesRead = read(block.data, buffer);

			// Save number of entries read
			//TODO evaluate if setting them even in case of empty block is desired
			block.setSize(Math.max(entriesRead, 0));

			return entriesRead>0;
		}
	}

	private boolean writeBlock(Block block, long offset) throws IOException {
		try(SeekableByteChannel channel = resource.getWriteChannel()) {

			// Write data to buffer
			write(block.getData(), buffer, block.getSize());

			// Copy buffer to channel
			channel.position(offset);
			channel.write(buffer);

			return true;
		}
	}

	protected abstract void write(Object source, ByteBuffer buffer, int length) throws IOException;

	protected abstract int read(Object target, ByteBuffer buffer) throws IOException;

	public void flush() throws IOException {
		for(TIntIterator it = changedBlocks.iterator(); it.hasNext(); ) {
			int id = it.next();

			Block block = cache.getBlock(id);
			if(block==null)
				throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Missing block marked as locked: "+id); //$NON-NLS-1$

			long offset = id*(long)bytesPerBlock;

			writeBlock(block, offset);

			it.remove();
			block.unlock();
		}
	}

	protected abstract Object newBlockData();

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
	 * Provides a basic implementation for synchronized read access to the data in
	 * the hosting {@link BufferedIOResource}. Creating an instance of this accessor
	 * will automatically increment the resource's use counter and closing it will
	 * then again decrement the counter.
	 *
	 * @author Markus Gärtner
	 *
	 * @param <T> The source type of the accessor
	 */
	protected class ReadAccessor<T extends Object> implements SynchronizedAccessor<T> {

		protected ReadAccessor() {
			incrementUseCount();
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#getSource()
		 */
		@SuppressWarnings("unchecked")
		@Override
		public T getSource() {
			return (T) BufferedIOResource.this;
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#begin()
		 */
		@Override
		public void begin() {
			getReadLock().lock();
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#end()
		 */
		@Override
		public void end() {
			getReadLock().unlock();
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
	 * Provides a basic implementation for synchronized write access to the data in
	 * the hosting {@link BufferedIOResource}. Creating an instance of this accessor
	 * will automatically increment the resource's use counter and closing it will
	 * then again decrement the counter.
	 *
	 * @author Markus Gärtner
	 *
	 * @param <T> The source type of the accessor
	 */
	protected class WriteAccessor<T extends Object> implements SynchronizedAccessor<T> {

		protected WriteAccessor() {
			incrementUseCount();
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#getSource()
		 */
		@SuppressWarnings("unchecked")
		@Override
		public T getSource() {
			return (T) BufferedIOResource.this;
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#begin()
		 */
		@Override
		public void begin() {
			getWriteLock().lock();
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#end()
		 */
		@Override
		public void end() {
			getWriteLock().unlock();
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
	public static abstract class BufferedIOResourceBuilder<B extends BufferedIOResourceBuilder<B>> {
		private int cacheSize;
		private IOResource resource;
		private BlockCache blockCache;

		@SuppressWarnings("unchecked")
		protected B thisAsCast() {
			return (B)this;
		}

		public B cacheSize(int cacheSize) {
			checkArgument(cacheSize>=0);
			checkState(this.cacheSize==0);

			this.cacheSize = cacheSize;

			return thisAsCast();
		}

		public B resource(IOResource resource) {
			checkNotNull(resource);
			checkState(this.resource==null);

			this.resource = resource;

			return thisAsCast();
		}

		public B blockCache(BlockCache blockCache) {
			checkNotNull(blockCache);
			checkState(this.blockCache==null);

			this.blockCache = blockCache;

			return thisAsCast();
		}

		public int getCacheSize() {
			return cacheSize;
		}

		public IOResource getResource() {
			return resource;
		}

		public BlockCache getBlockCache() {
			return blockCache;
		}

		protected void validate() {
			checkState("Missing resource", resource!=null);
			checkState("Missing block cache", blockCache!=null);
			checkState("Negative cache size", cacheSize>=0);
		}
	}
}
