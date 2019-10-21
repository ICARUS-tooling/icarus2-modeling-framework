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
package de.ims.icarus2.filedriver.mapping;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.ByteBuffer;

import de.ims.icarus2.filedriver.io.BufferedIOResource;
import de.ims.icarus2.filedriver.io.BufferedIOResource.Block;
import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;
import de.ims.icarus2.filedriver.io.BufferedIOResource.PayloadConverter;
import de.ims.icarus2.filedriver.io.BufferedIOResource.ReadWriteAccessor;
import de.ims.icarus2.filedriver.mapping.AbstractVirtualMapping.AbstractMappingBuilder;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingWriter;
import de.ims.icarus2.model.api.driver.mapping.WritableMapping;
import de.ims.icarus2.model.api.io.SynchronizedAccessor;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.util.io.resource.IOResource;

/**
 * Abstract base class for {@code Mapping} implementations that store mapping data.
 *
 *
 * @author Markus Gärtner
 *
 */
public abstract class AbstractStoredMapping implements WritableMapping {

	public static final int DEFAULT_CACHE_SIZE = 100;

	private final Driver driver;
	private final MappingManifest manifest;
	private final ItemLayerManifestBase<?> sourceLayer;
	private final ItemLayerManifestBase<?> targetLayer;
	private final BufferedIOResource resource;

	protected AbstractStoredMapping(AbstractStoredMappingBuilder<?,?> builder) {

		driver = builder.getDriver();
		manifest = builder.getManifest();
		sourceLayer = builder.getSourceLayer();
		targetLayer = builder.getTargetLayer();

		resource = requireNonNull(builder.createBufferedIOResource());
	}

	public BufferedIOResource getBufferedResource() {
		return resource;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(100);

		sb.append(getClass().getName()).append('@');
		sb.append("[id=").append(manifest.getId());
		sb.append(" sourceLayer=").append(sourceLayer.getId());
		sb.append(" targetLayer=").append(targetLayer.getId());

		toString(sb);

		sb.append(']');

		return sb.toString();
	}

	protected abstract void toString(StringBuilder sb);

	protected static void checkInterrupted() throws InterruptedException {
		if(Thread.interrupted())
			throw new InterruptedException();
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.mapping.Mapping#getDriver()
	 */
	@Override
	public Driver getDriver() {
		return driver;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.mapping.Mapping#getSourceLayer()
	 */
	@Override
	public ItemLayerManifestBase<?> getSourceLayer() {
		return sourceLayer;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.mapping.Mapping#getTargetLayer()
	 */
	@Override
	public ItemLayerManifestBase<?> getTargetLayer() {
		return targetLayer;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.mapping.Mapping#getManifest()
	 */
	@Override
	public MappingManifest getManifest() {
		return manifest;
	}

	/**
	 * Returns an accessor for write operations on this index.
	 *
	 * @return
	 */
	@Override
	public abstract MappingWriter newWriter();

	/**
	 * The default implementation does nothing.
	 *
	 * @see de.ims.icarus2.model.api.driver.mapping.Mapping#close()
	 */
	@Override
	public void close() {
		// no-op
	}

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

	protected class ResourceAccessor implements SynchronizedAccessor<Mapping> {

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
			delegateAccessor = resource.newAccessor(readOnly);
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#getSource()
		 */
		@Override
		public Mapping getSource() {
			return AbstractStoredMapping.this;
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#begin()
		 */
		@Override
		public void begin() {
			delegateAccessor.begin();
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#end()
		 */
		@Override
		public void end() {
			delegateAccessor.end();
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessor#close()
		 */
		@Override
		public void close() {
			delegateAccessor.close();
		}

		Block getBlock(int id) {
			return delegateAccessor.getBlock(id);
		}

		void lockBlock(Block block, int index) {
			delegateAccessor.lockBlock(block, index+1);
		}
	}

	public static class PayloadConverterImpl implements PayloadConverter {

		private final IndexBlockStorage blockStorage;

		/**
		 * @param blockStorage
		 */
		public PayloadConverterImpl(IndexBlockStorage blockStorage) {
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
			int length = buffer.remaining()/blockStorage.spanSize();
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
	public static abstract class AbstractStoredMappingBuilder<B extends AbstractMappingBuilder<B, M>, M extends Mapping>
			extends AbstractMappingBuilder<B, M> {
		private Integer cacheSize;
		private IOResource resource;
		private BlockCache blockCache;

		protected AbstractStoredMappingBuilder() {
			// no-op
		}

		public B cacheSize(int cacheSize) {
			checkArgument(cacheSize>0);
			checkState(this.cacheSize==null);

			this.cacheSize = Integer.valueOf(cacheSize);

			return thisAsCast();
		}

		public B resource(IOResource resource) {
			requireNonNull(resource);
			checkState(this.resource==null);

			this.resource = resource;

			return thisAsCast();
		}

		public B blockCache(BlockCache blockCache) {
			requireNonNull(blockCache);
			checkState(this.blockCache==null);

			this.blockCache = blockCache;

			return thisAsCast();
		}

		public int getCacheSize() {
			return cacheSize==null ? DEFAULT_CACHE_SIZE : cacheSize.intValue();
		}

		public IOResource getResource() {
			return resource;
		}

		public BlockCache getBlockCache() {
			return blockCache;
		}

		public abstract BufferedIOResource createBufferedIOResource();

		@Override
		protected void validate() {
			super.validate();
			checkState("Missing resource", resource!=null);
			checkState("Missing block cache", blockCache!=null);
		}
	}
}
