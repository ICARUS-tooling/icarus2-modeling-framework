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
package de.ims.icarus2.filedriver.mapping.chunks;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotNull;
import static de.ims.icarus2.util.Conditions.checkState;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.filedriver.io.BufferedIOResource;
import de.ims.icarus2.filedriver.io.sets.FileSet;
import de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;

/**
 *
 *
 * @author Markus Gärtner
 *
 */
public class DefaultChunkIndex extends BufferedIOResource implements ChunkIndex {

	private static final Logger log = LoggerFactory
			.getLogger(DefaultChunkIndex.class);

	private final FileSet fileSet;
	private final ChunkArrays.ArrayAdapter arrayAdapter;
	private final int blockPower;
	private final int entriesPerBlock;
	private final int blockMask;

	private static final int DEFAULT_BLOCK_POWER = 12;

	protected DefaultChunkIndex(Builder builder) {
		super(builder);

		FileSet fileSet = builder.getFileSet();
		this.fileSet = fileSet;

		ArrayAdapter arrayAdapter = builder.getArrayAdapter();
		if(arrayAdapter==null) {
			arrayAdapter = createAdapter(builder.getIndexValueType(), fileSet);
		}

		this.arrayAdapter = arrayAdapter;

		int blockPower = builder.getBlockPower();
		this.blockPower = blockPower;
		blockMask = (1<<blockPower)-1;
		entriesPerBlock = 1<<blockPower;

		setBytesPerBlock(entriesPerBlock * arrayAdapter.chunkSize());
	}

	protected ArrayAdapter createAdapter(IndexValueType valueType, FileSet fileSet) {
		if(valueType==null) {
			valueType = IndexValueType.LONG;
		}

		int fileCount = fileSet.getFileCount();

		if(fileCount>1) {
			IndexValueType fileValueType = IndexValueType.forValue(fileCount);
			return ChunkArrays.createFileAdapter(valueType, fileValueType);
		} else {
			return ChunkArrays.createBasicAdapter(valueType);
		}
	}

	/**
	 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkIndex#getFileSet()
	 */
	@Override
	public FileSet getFileSet() {
		return fileSet;
	}

	/**
	 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkIndex#newReader()
	 */
	@Override
	public ChunkIndexReader newReader() {
		return this.new Reader();
	}

	/**
	 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkIndex#newWriter()
	 */
	@Override
	public ChunkIndexWriter newWriter() {
		return this.new Writer();
	}

	public long getEntryCount() {
		try {
			return getResource().size()/arrayAdapter.chunkSize();
		} catch (IOException e) {
			log.error("Unable to read resource size: {}", getResource(), e); //$NON-NLS-1$

			return 0;
		}
	}

	protected ArrayAdapter getAdapter() {
		return arrayAdapter;
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.BufferedIOResource#read(java.lang.Object, java.nio.ByteBuffer)
	 */
	@Override
	protected int read(Object target, ByteBuffer buffer) throws IOException {
		int length = buffer.remaining()/arrayAdapter.chunkSize();
		arrayAdapter.read(target, buffer, 0, length);
		return length;
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.BufferedIOResource#write(java.lang.Object, java.nio.ByteBuffer, int)
	 */
	@Override
	protected void write(Object source, ByteBuffer buffer, int size) throws IOException {
		arrayAdapter.write(source, buffer, 0, size);
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.BufferedIOResource#newBlockData()
	 */
	@Override
	protected Object newBlockData() {
		return arrayAdapter.createBuffer(getBytesPerBlock());
	}

	private int id(long index) {
		return (int) (index>>blockPower);
	}

	private int localIndex(long index) {
		return (int)(index & blockMask);
//		return (int)(index & (blockPower-1));
	}

	protected int getFileId(long index) {
		int id = id(index);
		int localIndex = localIndex(index);

		Block block = getBlock(id, false);
		return block==null ? -1 : arrayAdapter.getFileId(block.getData(), localIndex);
	}

	protected long getBeginOffset(long index) {
		int id = id(index);
		int localIndex = localIndex(index);

		Block block = getBlock(id, false);
		return block==null ? NO_INDEX : arrayAdapter.getBeginOffset(block.getData(), localIndex);
	}

	protected long getEndOffset(long index) {
		int id = id(index);
		int localIndex = localIndex(index);

		Block block = getBlock(id, false);
		return block==null ? NO_INDEX : arrayAdapter.getEndOffset(block.getData(), localIndex);
	}

	protected int setFileId(long index, int fileId) {
		int id = id(index);
		int localIndex = localIndex(index);

		Block block = getBlock(id, true);
		int result = arrayAdapter.setFileId(block.getData(), localIndex, fileId);
		lockBlock(id, block);

		return result;
	}

	protected long setBeginOffset(long index, long offset) {
		int id = id(index);
		int localIndex = localIndex(index);

		Block block = getBlock(id, true);
		long result = arrayAdapter.setBeginOffset(block.getData(), localIndex, offset);
		lockBlock(id, block);

		return result;
	}

	protected long setEndOffset(long index, long offset) {
		int id = id(index);
		int localIndex = localIndex(index);

		Block block = getBlock(id, true);
		long result = arrayAdapter.setEndOffset(block.getData(), localIndex, offset);
		lockBlock(id, block);

		return result;
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	private class Reader extends ReadAccessor<ChunkIndex> implements ChunkIndexReader {

		private Reader() {
			incrementUseCount();
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexReader#getEntryCount()
		 */
		@Override
		public long getEntryCount() {
			return DefaultChunkIndex.this.getEntryCount();
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexReader#getFileId(long)
		 */
		@Override
		public int getFileId(long index) {
			return DefaultChunkIndex.this.getFileId(index);
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexReader#getBeginOffset(long)
		 */
		@Override
		public long getBeginOffset(long index) {
			return DefaultChunkIndex.this.getBeginOffset(index);
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexReader#getEndOffset(long)
		 */
		@Override
		public long getEndOffset(long index) {
			return DefaultChunkIndex.this.getEndOffset(index);
		}

	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	private class Writer extends WriteAccessor<ChunkIndex> implements ChunkIndexWriter {

		private Writer() {
			incrementUseCount();
		}

		/**
		 * @see java.io.Flushable#flush()
		 */
		@Override
		public void flush() throws IOException {
			DefaultChunkIndex.this.flush();
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexWriter#setFileId(long, int)
		 */
		@Override
		public int setFileId(long index, int fileId) {
			return DefaultChunkIndex.this.setFileId(index, fileId);
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexWriter#setBeginOffset(long, long)
		 */
		@Override
		public long setBeginOffset(long index, long offset) {
			return DefaultChunkIndex.this.setBeginOffset(index, offset);
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexWriter#setEndOffset(long, long)
		 */
		@Override
		public long setEndOffset(long index, long offset) {
			return DefaultChunkIndex.this.setEndOffset(index, offset);
		}

	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class Builder extends BufferedIOResourceBuilder<Builder> {
		private FileSet fileSet;
		private IndexValueType indexValueType;
		private Integer blockPower;
		private ChunkArrays.ArrayAdapter arrayAdapter;

		public FileSet getFileSet() {
			return fileSet;
		}

		public Builder fileSet(FileSet fileSet) {
			checkNotNull(fileSet);
			checkState(this.fileSet==null);

			this.fileSet = fileSet;

			return thisAsCast();
		}

		public ChunkArrays.ArrayAdapter getArrayAdapter() {
			return arrayAdapter;
		}

		public Builder arrayAdapter(ChunkArrays.ArrayAdapter arrayAdapter) {
			checkNotNull(arrayAdapter);
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

			checkState("Missing file set", fileSet!=null);

			//TODO
		}

		public DefaultChunkIndex build() {
			validate();

			return new DefaultChunkIndex(this);
		}
	}
}
