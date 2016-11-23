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
package de.ims.icarus2.filedriver;

import static de.ims.icarus2.util.Conditions.checkNotNull;
import static de.ims.icarus2.util.Conditions.checkState;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.FileDriverMetadata.ChunkIndexKey;
import de.ims.icarus2.filedriver.mapping.chunks.ChunkIndex;
import de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexCursor;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.util.io.IOUtil;
import de.ims.icarus2.util.nio.SubChannel;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractConverter implements Converter {

	protected FileDriver driver;

	private final Int2ObjectMap<Cursor> activeCursors = new Int2ObjectOpenHashMap<>();

	/**
	 * If subclasses wish to override this method they should ensure to make
	 * a call to {@code super.init()} <b>before</b> any other initialization work.
	 *
	 * @see de.ims.icarus2.filedriver.Converter#init(de.ims.icarus2.filedriver.FileDriver)
	 */
	@Override
	public void init(FileDriver driver) {
		checkNotNull(driver);

		checkState("Driver already set", this.driver==null);

		this.driver = driver;
	}

	protected static void checkInterrupted() throws InterruptedException {
		if(Thread.currentThread().isInterrupted())
			throw new InterruptedException();
	}

	@Override
	public FileDriver getFileDriver() {
		FileDriver driver = this.driver;

		if(driver==null)
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					"Converter not yet initialized or already closed again");

		return driver;
	}

	/**
	 * If subclasses wish to override this method they should ensure to make
	 * a call to {@code super.close()} <b>after</b> any other cleanup work.
	 *
	 * @see de.ims.icarus2.filedriver.Converter#close()
	 */
	@Override
	public void close() {
		driver = null;
	}

	/**
	 * Default implementation creates and {@link DelegatingCursor#open()} a
	 * new instance of {@link DelegatingCursor}.
	 *
	 * @see de.ims.icarus2.filedriver.Converter#getCursor(int, de.ims.icarus2.model.api.layer.ItemLayer, de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexStorage)
	 */
	@Override
	public Cursor getCursor(int fileIndex, ItemLayer layer) throws IOException {
		if(!driver.hasChunkIndex()) {
			return null;
		}

		if(activeCursors.containsKey(fileIndex))
			throw new ModelException(ModelErrorCode.DRIVER_ERROR,
					"Duplicate attempt to acquire cursor for file at index: "+fileIndex);

		DelegatingCursor cursor = new DelegatingCursor(fileIndex, layer);

		// Open now so we have the I/O initialization work out of the way
		cursor.open();

		return cursor;
	}

	/**
	 * Callback for {@link DelegatingCursor} instances to signal to their
	 * respective converter that the {@link DelegatingCursor#open() opening}
	 * step has been successful.
	 * <p>
	 * If subclasses wish to override this method they should make sure to
	 * call the {@code super} method <b>after</b> performing custom work
	 * for proper nesting.
	 *
	 * @param fileIndex
	 * @param cursor
	 */
	protected void cursorOpened(int fileIndex, Cursor cursor) {
		activeCursors.put(fileIndex, cursor);
	}

	/**
	 * Callback for {@link DelegatingCursor} instances to signal to their
	 * respective converter that the {@link DelegatingCursor#close() closing}
	 * step has been ended (either successfully or with an exception).
	 * <p>
	 * If subclasses wish to override this method they should make sure to
	 * call the {@code super} method <b>before</b> performing custom work
	 * for proper nesting.
	 *
	 * @param fileIndex
	 * @param cursor
	 */
	protected void cursorClosed(int fileIndex, Cursor cursor) {
		activeCursors.remove(fileIndex);
	}

	protected Path getFileForIndex(int fileIndex) {
		return driver.getDataFiles().getFileAt(fileIndex);
	}


	/**
	 * Reads the content of a {@code DelegatingCursor cursor} and constructs a single item.
	 *
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	protected abstract Item readItemFromCursor(DelegatingCursor cursor) throws IOException;

	/**
	 * Returns a recommended buffer size when reading in chunks of data block-wise.
	 * The returned value will either be the {@link IOUtil#DEFAULT_BUFFER_SIZE default buffer size}
	 * or the length of the largest chunk in the data if such information has been
	 * stored in the metadata available to the surrounding {@link FileDriver driver}.
	 */
	protected int getRecommendedBufferSize(ItemLayerManifest layerManifest) {

		// Determine good buffer size for the block-wise stream
		int bufferSize = IOUtil.DEFAULT_BUFFER_SIZE;
		MetadataRegistry metadataRegistry = getFileDriver().getMetadataRegistry();
		String savedMaxChunkSize = metadataRegistry.getValue(ChunkIndexKey.MAX_CHUNK_SIZE.getKey(layerManifest));
		if(savedMaxChunkSize!=null) {
			int maxChunkSize = Integer.parseInt(savedMaxChunkSize);
			bufferSize = Math.max(bufferSize, maxChunkSize);
		}

		return bufferSize;
	}

	/**
	 *
	 *
	 * @author Markus Gärtner
	 *
	 */
	protected class DelegatingCursor implements Cursor {
		protected final int fileIndex;
		protected final ItemLayer layer;

		protected final Path file;
		protected final ChunkIndex chunkIndex;
		protected final Charset encoding;

		public DelegatingCursor(int fileIndex, ItemLayer layer) {
			this.fileIndex = fileIndex;
			this.layer = layer;

			file = getFileForIndex(fileIndex);
			chunkIndex = getFileDriver().getChunkIndex(layer);
			encoding = getFileDriver().getEncoding();
		}

		protected ChunkIndexCursor chunkIndexCursor;
		protected FileChannel fileChannel;
		protected final SubChannel blockChannel = new SubChannel();

		public void open() throws IOException {

			fileChannel = FileChannel.open(file, StandardOpenOption.READ);
			blockChannel.setSource(fileChannel);

			chunkIndexCursor = chunkIndex.newCursor(true);

			// Inform converter (will be skipped in case of any exception)
			cursorOpened(fileIndex, this);
		}

		/**
		 * @see de.ims.icarus2.filedriver.Converter.Cursor#load(long)
		 */
		@Override
		public Item load(long index) throws IOException, InterruptedException {
			checkInterrupted();

			Item result = null;

			// Fetch location of chunk
			if(chunkIndexCursor.moveTo(index)) {
				long beginOffset = chunkIndexCursor.getBeginOffset();
				long endOffset = chunkIndexCursor.getEndOffset();
				long length = endOffset-beginOffset+1;

				// Reposition our channel window
				blockChannel.setOffsets(beginOffset, length);

				// Delegate reading to converter
				result = readItemFromCursor(this);
			}

			return result;
		}

		/**
		 * @see de.ims.icarus2.filedriver.Converter.Cursor#close()
		 */
		@Override
		public void close() throws IOException {

			try {
				fileChannel.close();

				//TODO do other cleanup work

			} finally {
				/*
				 *  Finally notify converter (we need to do this here in a finally
				 *  block to ensure proper removal of "old" cursor instances)
				 */
				cursorClosed(fileIndex, this);
			}
		}

		public final int getFileIndex() {
			return fileIndex;
		}

		public final ItemLayer getLayer() {
			return layer;
		}

		public final Path getFile() {
			return file;
		}

		public final ChunkIndex getChunkIndex() {
			return chunkIndex;
		}

		public final ChunkIndexCursor getChunkIndexCursor() {
			return chunkIndexCursor;
		}

		public final FileChannel getFileChannel() {
			return fileChannel;
		}

		public final SubChannel getBlockChannel() {
			return blockChannel;
		}

		public final Charset getEncoding() {
			return encoding;
		}
	}
}
