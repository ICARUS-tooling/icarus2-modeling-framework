/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.filedriver;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.filedriver.FileDataStates.FileInfo;
import de.ims.icarus2.filedriver.FileDriver.LockableFileObject;
import de.ims.icarus2.filedriver.FileDriverMetadata.ChunkIndexKey;
import de.ims.icarus2.filedriver.FileDriverMetadata.ContainerKey;
import de.ims.icarus2.filedriver.mapping.chunks.ChunkIndex;
import de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexCursor;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.ChunkState;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.mods.ModuleMonitor;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache;
import de.ims.icarus2.model.standard.driver.ChunkConsumer;
import de.ims.icarus2.model.standard.driver.mods.AbstractDriverModule;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.AccumulatingException;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.io.IOUtil;
import de.ims.icarus2.util.nio.SubChannel;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractConverter extends AbstractDriverModule implements Converter {

	private static Logger log = LoggerFactory.getLogger(AbstractConverter.class);

	private final Int2ObjectMap<DelegatingCursor<?>> activeCursors = new Int2ObjectOpenHashMap<>();

//	/**
//	 * If subclasses wish to override this method they should ensure to make
//	 * a call to {@code super.init()} <b>before</b> any other initialization work.
//	 *
//	 * @see de.ims.icarus2.filedriver.Converter#init(de.ims.icarus2.filedriver.FileDriver)
//	 */
//	@Override
//	public void init(FileDriver driver) {
//		requireNonNull(driver);
//
//		checkState("Driver already set", this.driver==null);
//
//		this.driver = driver;
//	}

//	protected static void checkInterrupted() throws InterruptedException {
//		if(Thread.currentThread().isInterrupted())
//			throw new InterruptedException();
//	}

	@Override
	protected boolean doPrepare(ModuleMonitor monitor) throws InterruptedException {
		// Nothing to do here
		return true;
	}

	@Override
	protected boolean doReset(ModuleMonitor monitor) throws InterruptedException {
		// TODO Nothing to do here
		return true;
	}

	@Override
	protected void doCancel() {
		// no-op
	}

//	@Override
//	public FileDriver getFileDriver() {
//		FileDriver driver = this.driver;
//
//		if(driver==null)
//			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
//					"Converter not yet initialized or already closed again");
//
//		return driver;
//	}

	/**
	 * @see de.ims.icarus2.util.AbstractPart#addNotify(java.lang.Object)
	 */
	@Override
	public void addNotify(Driver owner) {
		checkArgument("Can only be added to a FileDriver instance", FileDriver.class.isInstance(owner));

		super.addNotify(owner);
	}

	/**
	 * If subclasses wish to override this method they should ensure to make
	 * a call to {@code super.removeNotify(Driver)} <b>after</b> any other cleanup work.
	 *
	 * @see de.ims.icarus2.util.AbstractPart#removeNotify(java.lang.Object)
	 */
	@Override
	public void removeNotify(Driver owner) {

		// Close cursors first so we avoid race conditions against other processes adding items to caches
		try {
			closeAllCursors();
		} catch (AccumulatingException e) {
			throw new ModelException(ModelErrorCode.DRIVER_ERROR, "Failed to close remaining cursors", e);
		} finally {
			super.removeNotify(owner);
		}

	}

	/**
	 * @see de.ims.icarus2.model.standard.driver.mods.AbstractDriverModule#getDriver()
	 */
	@Override
	public FileDriver getDriver() {
		return (FileDriver) super.getDriver();
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
	public DelegatingCursor<?> getCursor(int fileIndex, ItemLayer layer) throws IOException {
		if(!layer.isPrimaryLayer())
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Cannot create cursor - not a priamry layer: "+ModelUtils.getUniqueId(layer));

		if(!getDriver().hasChunkIndex()) {
			return null;
		}

		if(activeCursors.containsKey(fileIndex))
			throw new ModelException(ModelErrorCode.DRIVER_ERROR,
					"Duplicate attempt to acquire cursor for file at index: "+fileIndex);

		DelegatingCursor<?> cursor = createDelegatingCursor(fileIndex, layer);

		if(cursor!=null) {
			// Open now so we have the I/O initialization work out of the way
			cursor.open();
		}

		return cursor;
	}

	/**
	 * Hook for subclasses to create custom cursors.
	 *
	 * @param fileIndex
	 * @param layer
	 * @return
	 */
	@Nullable
	protected abstract DelegatingCursor<?> createDelegatingCursor(int fileIndex, ItemLayer layer);

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
	protected void cursorOpened(DelegatingCursor<?> cursor) {
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
	protected void cursorClosed(DelegatingCursor<?> cursor) {
		activeCursors.remove(cursor.getFile().getFileIndex());
	}

	/**
	 * Reads the content of a {@link DelegatingCursor cursor} and constructs a single item.
	 *
	 * @param stream
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws IcarusApiException
	 */
	protected abstract Item readItemFromCursor(DelegatingCursor<?> cursor)
			throws IOException, InterruptedException, IcarusApiException;

	/**
	 * Returns a recommended buffer size when reading in chunks of data block-wise.
	 * The returned value will either be the {@link IOUtil#DEFAULT_BUFFER_SIZE default buffer size}
	 * or the length of the largest chunk in the data if such information has been
	 * stored in the metadata available to the surrounding {@link FileDriver driver}.
	 */
	protected int getRecommendedByteBufferSize(ItemLayerManifestBase<?> layerManifest) {

		// Determine good buffer size for the block-wise stream
		int bufferSize = IOUtil.DEFAULT_BUFFER_SIZE;
		MetadataRegistry metadataRegistry = getDriver().getMetadataRegistry();
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
	protected int getRecommendedIndexBufferSize(ItemLayerManifestBase<?> sourceLayer) {
		int bufferSize  = -1;
		MetadataRegistry metadataRegistry = getDriver().getMetadataRegistry();
		String savedMaxSize = metadataRegistry.getValue(ContainerKey.MAX_ITEM_COUNT.getKey(sourceLayer, 0));
		if(savedMaxSize!=null) {
			bufferSize = Integer.parseInt(savedMaxSize);
		}

		if(bufferSize<0)
			throw new ModelException(ModelErrorCode.DRIVER_METADATA_MISSING,
					"Missing information on maximum container size for layer: "+ManifestUtils.getUniqueId(sourceLayer));

		return bufferSize;
	}

	protected long getStartingIndex(ItemLayer layer, int fileIndex, ReadMode mode) {
		requireNonNull(layer);
		requireNonNull(mode);
		checkArgument("Chunking not supported for general supplier generation", mode!=ReadMode.CHUNK);

		if(mode==ReadMode.FILE) {
			FileInfo info = getDriver().getFileStates().getFileInfo(fileIndex);
			return info.getFirstIndex(layer.getManifest());
		} else if(fileIndex==0) {
			return 0;
		} else {
			FileInfo info = getDriver().getFileStates().getFileInfo(fileIndex-1);
			return info.getLastIndex(layer.getManifest()) + 1;
		}
	}

	/**
	 * Implements a cursor that only manages the positioning of the chunk window
	 * over the physical file resource and {@link AbstractConverter#readItemFromCursor(DelegatingCursor) delegates}
	 * the actual conversion work to the host converter.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public abstract static class DelegatingCursor<C extends AbstractConverter> implements Cursor<C> {

		protected final AbstractConverter converter;

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

		protected DelegatingCursor(CursorBuilder<?,?> builder) {
			requireNonNull(builder);

			converter = builder.getConverter();
			file = builder.getFile();
			layer = builder.getPrimaryLayer();
			loadResult = builder.getLoadResult();
			chunkIndex = builder.getChunkIndex();
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
			getConverter().cursorOpened(this);

			// Only if all configuration steps went well actually set the cursor operational
			open = true;
		}

		@Override
		public LoadResult getResult() {
			// Tell load() method to reset the result the next time it is called
			doReset = true;
			return loadResult;
		}

		@Override
		@SuppressWarnings("unchecked")
		public C getConverter() {
			return (C) converter;
		}

		/**
		 * @throws IcarusApiException
		 * @see de.ims.icarus2.filedriver.Converter.Cursor#load(long)
		 *
		 * @throws IllegalStateException if this cursor is not open
		 */
		@Override
		public Item load(long index) throws IOException, InterruptedException, IcarusApiException {
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
				result = getConverter().readItemFromCursor(this);

				// Push info into result
				loadResult.accept(index, result, ChunkState.forItem(result));
			}

			return result;
		}

		protected void doClose() throws IOException {
			// No checked exception, so let's close this first
			chunkIndexCursor.close();
			loadResult.close();

			currentIndex = IcarusUtils.UNSET_LONG;

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
				getConverter().cursorClosed(this);
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

	public static abstract class CursorBuilder<B extends CursorBuilder<B, C>, C extends DelegatingCursor<?>> extends AbstractBuilder<B, C> {

		private LockableFileObject file;
		private ItemLayer primaryLayer;
		private DynamicLoadResult loadResult;
		private ChunkIndex chunkIndex;

		private final AbstractConverter converter;

		protected CursorBuilder(AbstractConverter converter) {
			this.converter = requireNonNull(converter);
		}

		public B file(LockableFileObject file) {
			requireNonNull(file);
			checkState("File already set", this.file==null);

			this.file = file;

			return thisAsCast();
		}

		public B primaryLayer(ItemLayer primaryLayer) {
			requireNonNull(primaryLayer);
			checkState("Primary layer already set", this.primaryLayer==null);

			this.primaryLayer = primaryLayer;

			return thisAsCast();
		}

		public B loadResult(DynamicLoadResult loadResult) {
			requireNonNull(loadResult);
			checkState("Load result already set", this.loadResult==null);

			this.loadResult = loadResult;

			return thisAsCast();
		}

		public B chunkIndex(ChunkIndex chunkIndex) {
			requireNonNull(chunkIndex);
			checkState("Chunk index already set", this.chunkIndex==null);

			this.chunkIndex = chunkIndex;

			return thisAsCast();
		}

		public LockableFileObject getFile() {
			checkState("No file defined", file!=null);

			return file;
		}

		public ItemLayer getPrimaryLayer() {
			checkState("No primary layer defined", primaryLayer!=null);

			return primaryLayer;
		}

		public DynamicLoadResult getLoadResult() {
			checkState("No load result defined", loadResult!=null);

			return loadResult;
		}

		public AbstractConverter getConverter() {
			return converter;
		}

		public ChunkIndex getChunkIndex() {
			checkState("No chunk index defined", chunkIndex!=null);

			return chunkIndex;
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#validate()
		 */
		@Override
		protected void validate() {
			checkState("No file defined", file!=null);
			checkState("No primary layer defined", primaryLayer!=null);
			checkState("No load result defined", loadResult!=null);
			checkState("No chunk index defined", chunkIndex!=null);
		}
	}

	/**
	 * Extends to the basic {@link LoadResult} to be re-usable.
	 *
	 * @author Markus Gärtner
	 *
	 */
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

			// Commit first
			caches.forEach(c -> adder.add(c.commit()));
			// Then clear all caches
			caches.forEach(InputCache::reset);

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
