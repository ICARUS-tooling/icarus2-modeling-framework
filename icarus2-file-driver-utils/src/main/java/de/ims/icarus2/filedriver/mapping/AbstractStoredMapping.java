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
package de.ims.icarus2.filedriver.mapping;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.annotation.Nullable;

import de.ims.icarus2.apiguard.Guarded;
import de.ims.icarus2.apiguard.Guarded.MethodType;
import de.ims.icarus2.apiguard.Mandatory;
import de.ims.icarus2.filedriver.io.BufferedIOResource;
import de.ims.icarus2.filedriver.io.BufferedIOResource.Block;
import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;
import de.ims.icarus2.filedriver.io.BufferedIOResource.Header;
import de.ims.icarus2.filedriver.io.BufferedIOResource.PayloadConverter;
import de.ims.icarus2.filedriver.io.BufferedIOResource.ReadWriteAccessor;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingWriter;
import de.ims.icarus2.model.api.driver.mapping.WritableMapping;
import de.ims.icarus2.model.api.io.SynchronizedAccessor;
import de.ims.icarus2.util.io.resource.IOResource;

/**
 * Abstract base class for {@code Mapping} implementations that store mapping data.
 *
 *
 * @author Markus Gärtner
 *
 */
public abstract class AbstractStoredMapping<H extends Header>
		extends AbstractVirtualMapping implements WritableMapping {

	public static final int DEFAULT_CACHE_SIZE = 100;

	private final BufferedIOResource resource;
	private final H header;

	@SuppressWarnings("unchecked")
	protected AbstractStoredMapping(AbstractStoredMappingBuilder<?,?> builder) {
		super(builder);

		resource = requireNonNull(builder.createBufferedIOResource());
		header = (H) requireNonNull(resource.getHeader());
	}

	public BufferedIOResource getBufferedResource() {
		return resource;
	}

	protected static void checkInterrupted() throws InterruptedException {
		if(Thread.interrupted())
			throw new InterruptedException();
	}

	/**
	 * Returns an accessor for write operations on this index.
	 *
	 * @return
	 */
	@Override
	public abstract MappingWriter newWriter();

	public void delete() throws IOException {
		resource.delete();
	}

	/**
	 * Allows subclasses to perform compression or other means of
	 * storage optimization. This method should only be called once
	 * an index has been completely filled with mapping data!
	 * <p>
	 * It is advised for subclasses that override this method that
	 * they implement the optimization step in a such a way that
	 * aborting the process does not invalidate the current data
	 * stored in the mapping.
	 *
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void optimize() throws IOException, InterruptedException {
		// for subclasses
	}

	protected final H getHeader() {
		return header;
	}

	protected class ResourceAccessor<M extends Mapping> implements SynchronizedAccessor<M> {

		protected final ReadWriteAccessor delegateAccessor;

		/**
		 * Creates an accessor that wraps around the main resource as
		 * returned by {@link AbstractStoredMapping#getBufferedResource()}
		 *
		 * @param readOnly
		 */
		protected ResourceAccessor(boolean readOnly) {
			this(AbstractStoredMapping.this.getBufferedResource(), readOnly);
		}

		/**
		 * Creates an accessor that wraps around the provided {@link BufferedIOResource resource}.
		 * This constructor is useful if a mapping implementations needs to store data in different
		 * resources.
		 *
		 * @param resource
		 * @param readOnly
		 */
		protected ResourceAccessor(BufferedIOResource resource, boolean readOnly) {
			delegateAccessor = requireNonNull(resource.newAccessor(readOnly));
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#getSource()
		 */
		@SuppressWarnings("unchecked")
		@Override
		public M getSource() {
			return (M) AbstractStoredMapping.this;
		}

		/**
		 * Allows subclasses to perform actions or acquire resources before the
		 * actual {@link #begin()} work is done.
		 */
		protected void beginHook() {
			//no -op
		}

		/**
		 * Calls {@link #beginHook()} and then uses the associated {@link ReadWriteAccessor}
		 * to acquire the actual lock. Note that in case the {@link #beginHook()} calls fails
		 * the lock will never be acquired!
		 *
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#begin()
		 */
		@Override
		public final void begin() {
			beginHook();
			delegateAccessor.begin();
		}

		/**
		 * Allows subclasses to perform actions or release resources after the
		 * actual {@link #end()} work is done.
		 */
		protected void endHook() {
			//no -op
		}

		/**
		 * Releases the lock held by the associated {@link ReadWriteAccessor} and then
		 * allows subclasses to perform cleanup work in the {@link #endHook()} method.
		 * Note that {@link #endHook()} will be called even if the internal lock release
		 * fails, potentially suppressing an exception.
		 *
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#end()
		 */
		@Override
		public final void end() {
			try {
				delegateAccessor.end();
			} finally {
				endHook();
			}
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#close()
		 */
		@Override
		public final void close() {
			delegateAccessor.close();
		}

		protected final Block getBlock(int id) {
			return delegateAccessor.getBlock(id);
		}

		protected final void lockBlock(Block block, int index) {
			delegateAccessor.lockBlock(block, index+1);
		}

		protected final H getHeader() {
			return header;
		}
	}

	public static class SpanConverter implements PayloadConverter {

		private final IndexBlockStorage blockStorage;

		/**
		 * @param blockStorage
		 */
		public SpanConverter(IndexBlockStorage blockStorage) {
			requireNonNull(blockStorage);

			this.blockStorage = blockStorage;
		}

		@Override
		public void write(Object source, ByteBuffer buffer, int length)
				throws IOException {
			blockStorage.write(source, buffer, 0, length<<1);
		}

		@Override
		public int read(Object target, ByteBuffer buffer) throws IOException {
			int length = buffer.remaining()/blockStorage.spanSize();
			blockStorage.read(target, buffer, 0, length<<1);
			return length;
		}

		@Override
		public Object newBlockData(int bytesPerBlock) {
			return blockStorage.createBuffer(bytesPerBlock);
		}

		public IndexBlockStorage getBlockStorage() {
			return blockStorage;
		}
	}

	public static class ValueConverter implements PayloadConverter {

		private final IndexBlockStorage blockStorage;

		/**
		 * @param blockStorage
		 */
		public ValueConverter(IndexBlockStorage blockStorage) {
			requireNonNull(blockStorage);

			this.blockStorage = blockStorage;
		}

		@Override
		public void write(Object source, ByteBuffer buffer, int length)
				throws IOException {
			blockStorage.write(source, buffer, 0, length);
		}

		@Override
		public int read(Object target, ByteBuffer buffer) throws IOException {
			int length = buffer.remaining()/blockStorage.entrySize();
			blockStorage.read(target, buffer, 0, length);
			return length;
		}

		@Override
		public Object newBlockData(int bytesPerBlock) {
			return blockStorage.createBuffer(bytesPerBlock);
		}

		public IndexBlockStorage getBlockStorage() {
			return blockStorage;
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 * @param <B>
	 * @param <M>
	 */
	public static abstract class AbstractStoredMappingBuilder<B extends AbstractStoredMappingBuilder<B, M>,
				M extends AbstractStoredMapping<?>>
			extends AbstractMappingBuilder<B, M> {
		private Integer cacheSize;
		private IOResource resource;
		private BlockCache blockCache;

		protected AbstractStoredMappingBuilder() {
			// no-op
		}

		@Guarded(methodType=MethodType.BUILDER)
		public B cacheSize(int cacheSize) {
			checkArgument(cacheSize>0);
			checkState(this.cacheSize==null);

			this.cacheSize = Integer.valueOf(cacheSize);

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public B resource(IOResource resource) {
			requireNonNull(resource);
			checkState(this.resource==null);

			this.resource = resource;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public B blockCache(BlockCache blockCache) {
			requireNonNull(blockCache);
			checkState(this.blockCache==null);

			this.blockCache = blockCache;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.GETTER, defaultValue="100")
		public int getCacheSize() {
			return cacheSize==null ? DEFAULT_CACHE_SIZE : cacheSize.intValue();
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

		/** Create a backing resource for the mapping, never {@code null} */
		public abstract BufferedIOResource createBufferedIOResource();

		@Override
		protected void validate() {
			super.validate();
			checkState("Missing resource", resource!=null);
			checkState("Missing block cache", blockCache!=null);
		}
	}
}
