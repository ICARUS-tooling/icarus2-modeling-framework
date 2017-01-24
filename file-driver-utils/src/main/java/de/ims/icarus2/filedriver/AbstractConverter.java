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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.FileDriver.LockableFileObject;
import de.ims.icarus2.filedriver.FileDriverMetadata.ChunkIndexKey;
import de.ims.icarus2.filedriver.FileDriverMetadata.ContainerKey;
import de.ims.icarus2.filedriver.mapping.chunks.ChunkIndex;
import de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexCursor;
import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.ChunkState;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.model.standard.driver.BufferedItemManager;
import de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache;
import de.ims.icarus2.model.standard.driver.ChunkConsumer;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.AccumulatingException;
import de.ims.icarus2.util.collections.Pool;
import de.ims.icarus2.util.io.IOUtil;
import de.ims.icarus2.util.nio.SubChannel;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractConverter implements Converter {

	private FileDriver driver;

	private final Int2ObjectMap<DelegatingCursor> activeCursors = new Int2ObjectOpenHashMap<>();

	private final Reference2ObjectMap<ItemLayer, Pool<BufferedItemManager.InputCache>> cachePools = new Reference2ObjectOpenHashMap<>();

	private static Logger log = LoggerFactory.getLogger(AbstractConverter.class);

	protected Pool<BufferedItemManager.InputCache> getCachePoolForLayer(ItemLayer layer) {
		Pool<BufferedItemManager.InputCache> pool = cachePools.get(layer);
		if(pool==null) {
			pool = new Pool<>(getFileDriver().getLayerBuffer(layer)::newCache);
			cachePools.put(layer, pool);
		}
		return pool;
	}

	/**
	 * If subclasses wish to override this method they should ensure to make
	 * a call to {@code super.init()} <b>before</b> any other initialization work.
	 *
	 * @see de.ims.icarus2.filedriver.Converter#init(de.ims.icarus2.filedriver.FileDriver)
	 */
	@Override
	public void init(FileDriver driver) {
		requireNonNull(driver);

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
	public void close() throws AccumulatingException {
		driver = null;

		// Close cursors first so we avoid race conditions against other processes adding items to caches
		closeAllCursors();
	}

	protected void closeAllCursors() throws AccumulatingException {
		AccumulatingException.Buffer exceptionBuffer = new AccumulatingException.Buffer();

		activeCursors.values().forEach(cursor -> {
			try {
				cursor.close();
			} catch (IOException e) {
				exceptionBuffer.addException(e);
				log.error("Failed to close cursor for file '{1}'", cursor.getFile().getResource(), e);
			}
		});

		if(!exceptionBuffer.isEmpty()) {
			exceptionBuffer.setFormattedMessage("Failed to close %d cursors");
			throw exceptionBuffer.toException();
		}
	}

	/**
	 * Default implementation creates and {@link DelegatingCursor#open() opens} a
	 * new instance of {@link DelegatingCursor}.
	 *
	 * @see de.ims.icarus2.filedriver.Converter#getCursor(int, de.ims.icarus2.model.api.layer.ItemLayer, de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexStorage)
	 */
	@Override
	public DelegatingCursor getCursor(int fileIndex, ItemLayer layer) throws IOException {
		if(!layer.isPrimaryLayer())
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Cannot create cursor - not a priamry layer: "+ModelUtils.getUniqueId(layer));

		if(!driver.hasChunkIndex()) {
			return null;
		}

		if(activeCursors.containsKey(fileIndex))
			throw new ModelException(ModelErrorCode.DRIVER_ERROR,
					"Duplicate attempt to acquire cursor for file at index: "+fileIndex);

		DelegatingCursor cursor = createDelegatingCursor(fileIndex, layer);

		// Open now so we have the I/O initialization work out of the way
		cursor.open();

		return cursor;
	}

	/**
	 * Hook for subclasses to create custom cursors.
	 *
	 * @param fileIndex
	 * @param layer
	 * @return
	 */
	protected abstract DelegatingCursor createDelegatingCursor(int fileIndex, ItemLayer layer);

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
	protected void cursorOpened(DelegatingCursor cursor) {
		activeCursors.put(cursor.getFile().getFileIndex(), cursor);
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
	protected void cursorClosed(DelegatingCursor cursor) {
		activeCursors.remove(cursor.getFile().getFileIndex());
	}

	/**
	 * Reads the content of a {@link DelegatingCursor cursor} and constructs a single item.
	 *
	 * @param stream
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected abstract Item readItemFromCursor(DelegatingCursor cursor) throws IOException, InterruptedException;

	/**
	 * Returns a recommended buffer size when reading in chunks of data block-wise.
	 * The returned value will either be the {@link IOUtil#DEFAULT_BUFFER_SIZE default buffer size}
	 * or the length of the largest chunk in the data if such information has been
	 * stored in the metadata available to the surrounding {@link FileDriver driver}.
	 */
	protected int getRecommendedByteBufferSize(ItemLayerManifest layerManifest) {

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
	 * Returns a buffer size that is sufficient to host elements of any {@link Container}
	 * hosted within the given {@code sourceLayer}. This assumes that that the layer does
	 * indeed host container objects and that it has been previously scanned.
	 *
	 * @param sourceLayer
	 * @return
	 * @throws ModelException if there is no exploitable metadata on the maximum size of containers for the given layer
	 */
	protected int getRecommendedIndexBufferSize(ItemLayerManifest sourceLayer) {
		int bufferSize  = -1;
		MetadataRegistry metadataRegistry = getFileDriver().getMetadataRegistry();
		String savedMaxSize = metadataRegistry.getValue(ContainerKey.MAX_ITEM_COUNT.getKey(sourceLayer, 0));
		if(savedMaxSize!=null) {
			bufferSize = Integer.parseInt(savedMaxSize);
		}

		if(bufferSize<0)
			throw new ModelException(ModelErrorCode.DRIVER_METADATA_MISSING,
					"Missing information on maximum container size for layer: "+ManifestUtils.getUniqueId(sourceLayer));

		return bufferSize;
	}

	/**
	 * Implements a cursor that only manages the positioning of the chunk window
	 * over the physical file resource and {@link AbstractConverter#readItemFromCursor(DelegatingCursor) delegates}
	 * the actual conversion work to the host converter.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public class DelegatingCursor implements Cursor, ModelConstants {

		/**
		 * Primary layer of group for which data chunks should be loaded
		 */
		protected final ItemLayer layer;

		/**
		 * Physical resource location, index and shared lock
		 * bundled into one object.
		 */
		protected final LockableFileObject file;

		/**
		 * Chunk index for accessing physical data chunks, provided by host driver
		 */
		protected final ChunkIndex chunkIndex;

		protected final DynamicLoadResult loadResult;

		public DelegatingCursor(LockableFileObject file, ItemLayer primaryLayer, DynamicLoadResult loadResult) {
			requireNonNull(file);
			requireNonNull(primaryLayer);
			requireNonNull(loadResult);

			this.file = file;
			this.layer = primaryLayer;
			this.loadResult = loadResult;

			chunkIndex = getFileDriver().getChunkIndex(primaryLayer);
		}

		private ChunkIndexCursor chunkIndexCursor;
		private SeekableByteChannel sourceChannel;
		private final SubChannel blockChannel = new SubChannel();
		private long currentIndex;

		/**
		 * Hint for the {@link #load(long)} method to reset the loadResult instance before
		 * actually loading a new item.
		 */
		private boolean doReset = false;

		private volatile boolean open = false;

		/**
		 * Performs the following I/O related initialization steps:
		 * <ol>
		 * <li>{@link FileChannel#open(Path, java.nio.file.OpenOption...) Opening} of the underlying file channel</li>
		 * <li>Linking of the block-wise {@link SubChannel} to the open {@link FileChannel}</li>
		 * <li>{@link ChunkIndex#newCursor(boolean) Acquisition} of a read-only {@link ChunkIndexCursor}</li>
		 * </ol>
		 *
		 * Finally the host converter is {@link AbstractConverter#cursorOpened(int, de.ims.icarus2.filedriver.Converter.Cursor) opened}
		 * to give it a chance for additional setup work.
		 *
		 * @throws IOException
		 */
		public void open() throws IOException {

			// Access raw input
			sourceChannel = file.getResource().getReadChannel();
			blockChannel.setSource(sourceChannel);

			// Read-only access to the chunk index
			chunkIndexCursor = chunkIndex.newCursor(true);

			// Inform converter (will be skipped in case of any exception)
			cursorOpened(this);

			// Only if all configuration steps went well actually set the cursor operational
			open = true;
		}

		@Override
		public LoadResult getResult() {
			// Tell load() method to reset the result the next time it is called
			doReset = true;
			return loadResult;
		}

		/**
		 * @see de.ims.icarus2.filedriver.Converter.Cursor#load(long)
		 *
		 * @throws IllegalStateException if this cursor is not open
		 */
		@Override
		public Item load(long index) throws IOException, InterruptedException {
			checkState(open);
			checkInterrupted();

			// Wipe state of result
			if(doReset) {
				loadResult.reset();
				doReset = false;
			}

			Item result = null;

			// Fetch location of chunk and only attempt to load data if chunk cursor has a valid location
			if(chunkIndexCursor.moveTo(index)) {
				long beginOffset = chunkIndexCursor.getBeginOffset();
				long endOffset = chunkIndexCursor.getEndOffset();
				long length = endOffset-beginOffset+1;

				// Reposition our channel window
				blockChannel.setOffsets(beginOffset, length);

				currentIndex = index;

				// Delegate reading to converter
				result = readItemFromCursor(this);

				// Push info into result
				loadResult.accept(index, result, ChunkState.forItem(result));
			}

			return result;
		}

		protected void doClose() throws IOException {
			// No checked exception, so let's close this first
			chunkIndexCursor.close();
			loadResult.close();

			currentIndex = NO_INDEX;

			// This might fail due to I/O stuff beyond our control
			sourceChannel.close();

			//TODO do other cleanup work
		}

		/**
		 * @see de.ims.icarus2.filedriver.Converter.Cursor#close()
		 */
		@Override
		public void close() throws IOException {

			try {
				doClose();
			} finally {
				open = false;

				/*
				 *  Finally notify converter (we need to do this here in a finally
				 *  block to ensure proper removal of "old" cursor instances)
				 */
				cursorClosed(this);
			}
		}

		public final LockableFileObject getFile() {
			return file;
		}

		public final ItemLayer getLayer() {
			return layer;
		}

		public final ChunkIndex getChunkIndex() {
			return chunkIndex;
		}

		public final ChunkIndexCursor getChunkIndexCursor() {
			return chunkIndexCursor;
		}

		public final SeekableByteChannel getSourceChannel() {
			return sourceChannel;
		}

		public final SubChannel getBlockChannel() {
			return blockChannel;
		}

		public long getCurrentIndex() {
			return currentIndex;
		}
	}

	public interface DynamicLoadResult extends LoadResult, ChunkConsumer {

		/**
		 * Resets the internal buffer of this result so that it will
		 * subsequently report {@code 0} for all count related methods
		 * unless new chunks are {@link #accept(long, Item, ChunkState) consumed}.
		 */
		void reset();
	}

	protected static class SimpleLoadResult implements DynamicLoadResult {

		private long validChunkCount = 0L;
		private long modifiedChunkCount = 0L;
		private long corruptedChunkCount = 0L;

		private final List<InputCache> caches = new ArrayList<>();
		private boolean published = false;
		private boolean discarded = false;

		public SimpleLoadResult(Collection<InputCache> caches) {
			requireNonNull(caches);
			checkArgument(!caches.isEmpty());

			this.caches.addAll(caches);
		}

		/**
		 * Creates an "empty" load result, i.e. one that is not linked to any
		 * caches and therefore suitable for the scanning phase of a converter.
		 */
		public SimpleLoadResult() {
			// no-op
		}

		private void checkStillValid() {
			checkState("Result already published", !published);
			checkState("Result already discarded", !discarded);
		}

		/**
		 * @see de.ims.icarus2.filedriver.Converter.LoadResult#publish()
		 */
		@Override
		public long publish() {
			checkStillValid();
			published = true;
			LongAdder adder = new LongAdder();

			caches.forEach(c -> adder.add(c.commit()));

			return adder.longValue();
		}

		/**
		 * @see de.ims.icarus2.filedriver.Converter.LoadResult#discard()
		 */
		@Override
		public long discard() {
			checkStillValid();
			discarded = true;
			LongAdder adder = new LongAdder();

			caches.forEach(c -> adder.add(c.discard()));

			return adder.longValue();
		}

		/**
		 * @see de.ims.icarus2.filedriver.Converter.LoadResult#loadedChunkCount()
		 */
		@Override
		public long loadedChunkCount() {
			return validChunkCount+modifiedChunkCount+corruptedChunkCount;
		}

		/**
		 * @see de.ims.icarus2.filedriver.Converter.LoadResult#chunkCount(de.ims.icarus2.model.api.driver.ChunkState)
		 */
		@Override
		public long chunkCount(ChunkState state) {
			switch (state) {
			case VALID: return validChunkCount;
			case MODIFIED: return modifiedChunkCount;
			case CORRUPTED: return corruptedChunkCount;

			default:
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Unknown chunk state: "+state);
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.ChunkConsumer#accept(long, de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.driver.ChunkState)
		 */
		@Override
		public void accept(long index, Item item, ChunkState state) {
			switch (state) {
			case VALID: validChunkCount++; break;
			case MODIFIED: modifiedChunkCount++; break;
			case CORRUPTED: corruptedChunkCount++; break;

			default:
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Unknown chunk state: "+state);
			}
		}

		/**
		 * @see java.lang.AutoCloseable#close()
		 */
		@Override
		public void close() {
			caches.forEach(InputCache::discard);
			caches.clear();
		}

		/**
		 * @see de.ims.icarus2.filedriver.AbstractConverter.DynamicLoadResult#reset()
		 */
		@Override
		public void reset() {
			caches.forEach(InputCache::discard);
			validChunkCount = modifiedChunkCount = corruptedChunkCount = 0L;
			discarded = published = false;
		}
	}
}