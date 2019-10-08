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
package de.ims.icarus2.filedriver.mapping.chunks;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.io.BufferedIOResource;
import de.ims.icarus2.filedriver.io.BufferedIOResource.Block;
import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;
import de.ims.icarus2.filedriver.io.BufferedIOResource.PayloadConverter;
import de.ims.icarus2.filedriver.io.BufferedIOResource.ReadWriteAccessor;
import de.ims.icarus2.filedriver.io.sets.ResourceSet;
import de.ims.icarus2.filedriver.mapping.AbstractStoredMapping;
import de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.io.SynchronizedAccessor;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.io.resource.IOResource;

/**
 *
 *
 * @author Markus Gärtner
 *
 */
public class DefaultChunkIndex implements ChunkIndex {

	private static final int DEFAULT_BLOCK_POWER = 12;

	public static final int DEFAULT_CACHE_SIZE = 100;

	private static final Logger log = LoggerFactory
			.getLogger(DefaultChunkIndex.class);

	private final ResourceSet resourceSet;
	private final ChunkArrays.ArrayAdapter arrayAdapter;
	private final int blockPower;
	private final int blockMask;
	private final BufferedIOResource resource;

	public static Builder builder() {
		return new Builder();
	}

	protected DefaultChunkIndex(Builder builder) {

		ResourceSet resourceSet = builder.getFileSet();
		this.resourceSet = resourceSet;

		ArrayAdapter arrayAdapter = builder.getArrayAdapter();
		if(arrayAdapter==null) {
			arrayAdapter = createAdapter(builder.getIndexValueType(), resourceSet);
		}

		this.arrayAdapter = arrayAdapter;

		int blockPower = builder.getBlockPower();
		this.blockPower = blockPower;
		blockMask = (1<<blockPower)-1;

		int entriesPerBlock = 1<<blockPower;

		resource = BufferedIOResource.builder()
			.cacheSize(builder.getCacheSize())
			.blockCache(builder.getBlockCache())
			.resource(builder.getResource())
			.bytesPerBlock(entriesPerBlock*arrayAdapter.chunkSize())
			.payloadConverter(new PayloadConverterImpl())
			.build();
	}

	protected ArrayAdapter createAdapter(IndexValueType valueType, ResourceSet resourceSet) {
		if(valueType==null) {
			valueType = IndexValueType.LONG;
		}

		int fileCount = resourceSet.getResourceCount();

		if(fileCount>1) {
			IndexValueType fileValueType = IndexValueType.forValue(fileCount);
			return ChunkArrays.createFileAdapter(valueType, fileValueType);
		}

		return ChunkArrays.createBasicAdapter(valueType);
	}

	public BufferedIOResource getBufferedResource() {
		return resource;
	}

	/**
	 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkIndex#getFileSet()
	 */
	@Override
	public ResourceSet getFileSet() {
		return resourceSet;
	}

	@Override
	public ChunkIndexCursor newCursor(boolean readOnly) {
		return this.new Cursor(readOnly);
	}

	protected Block getBlock(int id, boolean writeAccess) {
		return resource.getBlock(id, writeAccess);
	}

	protected ArrayAdapter getAdapter() {
		return arrayAdapter;
	}

	private int id(long index) {
		return (int) (index>>blockPower);
	}

	private int localIndex(long index) {
		return (int)(index & blockMask);
	}

	private long getEntryCount() {
		try {
			IOResource resource = getBufferedResource().getResource();
			return resource.size()/getAdapter().chunkSize();
		} catch (IOException e) {
			log.error("Unable to read resource size: {}", getBufferedResource(), e);

			return 0L;
		}
	}

	/**
	 * Direct bridge to the internal {@link ArrayAdapter} of the surrounding
	 * {@link DefaultChunkIndex} instance.
	 *
	 * @author Markus Gärtner
	 *
	 */
	private class PayloadConverterImpl implements PayloadConverter {

		@Override
		public int read(Object target, ByteBuffer buffer) throws IOException {
			int length = buffer.remaining()/getAdapter().chunkSize();
			getAdapter().read(target, buffer, 0, length);
			return length;
		}

		@Override
		public void write(Object source, ByteBuffer buffer, int size) throws IOException {
			getAdapter().write(source, buffer, 0, size);
		}

		@Override
		public Object newBlockData(int bytesPerBlock) {
			return getAdapter().createBuffer(bytesPerBlock);
		}

	}

	protected class ResourceAccessor implements SynchronizedAccessor<ChunkIndex> {

		protected final ReadWriteAccessor delegateAccessor;

		/**
		 * Creates an accessor that wraps around the main resource as
		 * returned by {@link AbstractStoredMapping#getBufferedResource()}
		 *
		 * @param readOnly
		 */
		protected ResourceAccessor(boolean readOnly) {
			this(DefaultChunkIndex.this.getBufferedResource(), readOnly);
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
		public ChunkIndex getSource() {
			return DefaultChunkIndex.this;
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

		protected Block getBlock(int id, boolean writeAccess) {
			return delegateAccessor.getSource().getBlock(id, writeAccess);
		}

		protected void lockBlock(int id, Block block) {
			delegateAccessor.getSource().lockBlock(id, block);
		}
	}

	private class Cursor extends ResourceAccessor implements ChunkIndexCursor {

		private int id = IcarusUtils.UNSET_INT;
		private int localIndex = IcarusUtils.UNSET_INT;
		private Block block = null;

		private Cursor(boolean readOnly) {
			super(readOnly);
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexCursor#isReadOnly()
		 */
		@Override
		public boolean isReadOnly() {
			return delegateAccessor.isReadOnly();
		}

		/**
		 * @see java.io.Flushable#flush()
		 */
		@Override
		public void flush() throws IOException {
			getBufferedResource().flush();
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexCursor#getEntryCount()
		 */
		@Override
		public long getEntryCount() {
			return DefaultChunkIndex.this.getEntryCount();
		}

		/**
		 * Calculates and caches id of the required {@link BufferedIOResource.Block block}
		 * and the offset for the frame within this block that's denoted by {@code index}.
		 * Note that this method will only attempt to access the backing cache if the
		 * required chunk data is stored outside the block referenced on the last call.
		 *
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexCursor#moveTo(long)
		 */
		@Override
		public boolean moveTo(long index) {

			localIndex = localIndex(index);

			// Only delegate to cache lookup if we need a new block
			int newId = id(index);
			if(newId!=id) {
				id = newId;
				block = getBlock(id, !isReadOnly());
			}

			return block!=null;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexCursor#getFileId()
		 */
		@Override
		public int getFileId() {
			return block==null ? -1 : arrayAdapter.getFileId(block.getData(), localIndex);
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexCursor#getBeginOffset()
		 */
		@Override
		public long getBeginOffset() {
			return block==null ? IcarusUtils.UNSET_LONG : arrayAdapter.getBeginOffset(block.getData(), localIndex);
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexCursor#getEndOffset()
		 */
		@Override
		public long getEndOffset() {
			return block==null ? IcarusUtils.UNSET_LONG : arrayAdapter.getEndOffset(block.getData(), localIndex);
		}

		private void checkWriteAccess() {
			if(isReadOnly())
				throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
						"Write operations not supported on read-only cursor");
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexCursor#setFileId(long, int)
		 */
		@Override
		public int setFileId(long index, int fileId) {
			checkWriteAccess();

			int result = arrayAdapter.setFileId(block.getData(), localIndex, fileId);
			lockBlock(id, block);

			return result;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexCursor#setBeginOffset(long, long)
		 */
		@Override
		public long setBeginOffset(long index, long offset) {
			checkWriteAccess();

			long result = arrayAdapter.setBeginOffset(block.getData(), localIndex, offset);
			lockBlock(id, block);

			return result;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexCursor#setEndOffset(long, long)
		 */
		@Override
		public long setEndOffset(long index, long offset) {
			checkWriteAccess();

			long result = arrayAdapter.setEndOffset(block.getData(), localIndex, offset);
			lockBlock(id, block);

			return result;
		}

	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class Builder extends AbstractBuilder<Builder, DefaultChunkIndex> {
		private ResourceSet resourceSet;
		private IndexValueType indexValueType;
		private Integer blockPower;
		private ChunkArrays.ArrayAdapter arrayAdapter;
		private Integer cacheSize;
		private IOResource resource;
		private BlockCache blockCache;

		protected Builder() {
			// no-op
		}

		public Builder cacheSize(int cacheSize) {
			checkArgument(cacheSize>0);
			checkState(this.cacheSize==null);

			this.cacheSize = Integer.valueOf(cacheSize);

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

		public int getCacheSize() {
			return cacheSize==null ? DEFAULT_CACHE_SIZE : cacheSize.intValue();
		}

		public IOResource getResource() {
			return resource;
		}

		public BlockCache getBlockCache() {
			return blockCache;
		}

		public ResourceSet getFileSet() {
			return resourceSet;
		}

		public Builder resourceSet(ResourceSet resourceSet) {
			requireNonNull(resourceSet);
			checkState(this.resourceSet==null);

			this.resourceSet = resourceSet;

			return thisAsCast();
		}

		public ChunkArrays.ArrayAdapter getArrayAdapter() {
			return arrayAdapter;
		}

		public Builder arrayAdapter(ChunkArrays.ArrayAdapter arrayAdapter) {
			requireNonNull(arrayAdapter);
			checkState(this.arrayAdapter==null);

			this.arrayAdapter = arrayAdapter;

			return thisAsCast();
		}

		public int getBlockPower() {
			return blockPower==null ? DEFAULT_BLOCK_POWER : blockPower.intValue();
		}

		public Builder blockPower(int blockPower) {
			checkArgument(blockPower>0);
			checkState(this.blockPower==null);

			this.blockPower = Integer.valueOf(blockPower);

			return thisAsCast();
		}

		public IndexValueType getIndexValueType() {
			return indexValueType;
		}

		public Builder indexValueType(IndexValueType indexValueType) {
			checkState(this.indexValueType==null);

			this.indexValueType = indexValueType;

			return thisAsCast();
		}

		@Override
		protected void validate() {
			super.validate();

			checkState("Missing file set", resourceSet!=null);
			checkState("Missing resource", resource!=null);
			checkState("Missing block cache", blockCache!=null);

			//TODO
		}

		@Override
		protected DefaultChunkIndex create() {
			return new DefaultChunkIndex(this);
		}
	}
}
