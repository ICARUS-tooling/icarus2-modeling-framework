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
import static de.ims.icarus2.util.Conditions.checkNotNull;
import static de.ims.icarus2.util.Conditions.checkState;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.FileDriverMetadata.ChunkIndexKey;
import de.ims.icarus2.filedriver.FileDriverMetadata.ContainerKey;
import de.ims.icarus2.filedriver.mapping.chunks.ChunkIndex;
import de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexCursor;
import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingReader;
import de.ims.icarus2.model.api.driver.mapping.RequestSettings;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.registry.LayerMemberFactory;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;
import de.ims.icarus2.model.manifest.api.MappingManifest.Relation;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.driver.BufferedItemManager;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.ArrayUtils;
import de.ims.icarus2.util.collections.set.DataSet;
import de.ims.icarus2.util.io.IOUtil;
import de.ims.icarus2.util.nio.SubChannel;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractConverter implements Converter {

	private FileDriver driver;

	private LayerMemberFactory memberFactory;

	private final Int2ObjectMap<Cursor> activeCursors = new Int2ObjectOpenHashMap<>();

	private final Reference2ObjectMap<ItemLayer, BufferedItemManager.InputCache> caches = new Reference2ObjectOpenHashMap<>();

	private static Logger log = LoggerFactory.getLogger(AbstractConverter.class);

	protected BufferedItemManager.InputCache getCacheForLayer(ItemLayer layer) {
		BufferedItemManager.InputCache cache = caches.get(layer);
		if(cache==null) {
			cache = getFileDriver().getLayerBuffer(layer).newCache();
			caches.put(layer, cache);
		}
		return cache;
	}

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
		memberFactory = driver.newMemberFactory();
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

	protected LayerMemberFactory getMemberFactory() {
		return memberFactory;
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

		discardAllCaches();
		closeAllCursors();
	}

	protected void discardAllCaches() {
		//TODO
	}

	protected void closeAllCursors() {
		//TODO
	}

	/**
	 * Default implementation creates and {@link DelegatingCursor#open() opens} a
	 * new instance of {@link DelegatingCursor}.
	 *
	 * @see de.ims.icarus2.filedriver.Converter#getCursor(int, de.ims.icarus2.model.api.layer.ItemLayer, de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexStorage)
	 */
	@Override
	public Cursor getCursor(int fileIndex, ItemLayer layer) throws IOException {
		if(!layer.isPrimaryLayer())
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Cannot create cursor - not a priamry layer: "+ModelUtils.getUniqueId(layer));

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
	 * Reads the content of a {@link DelegatingCursor cursor} and constructs a single item.
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
					"Missing information on maximum container size for layer: "+ModelUtils.getUniqueId(sourceLayer));

		return bufferSize;
	}

	/**
	 * Creates a dynamic {@link IndexSourceLookup} that can be used for the random-access
	 * style operations of a {@link Cursor} object.
	 *
	 * @param group
	 * @return
	 */
	protected IndexSourceLookup createIndexSourcesForCursor(LayerGroup group) {
		checkNotNull(group);

		List<IndexSource> indexSources = new ArrayList<>();

		final ItemLayer primaryLayer = group.getPrimaryLayer();
		indexSources.add(new RootIndexSource(primaryLayer.getManifest()));
		collectIndexSources0(primaryLayer, indexSources);

		IndexSourceLookup lookup = new IndexSourceLookup(indexSources.size());

		return lookup;
	}

	/**
	 * Traverses the given layer's list of {@link Layer#getBaseLayers() base layers}
	 * and for each such layer that is located within the same group as the given layer
	 * itself creates a new {@link MappedIndexSource}. Will throw an exception if the
	 * surrounding driver fails to provide the necessary {@link Mapping} for this combination
	 * of layers.
	 * <p>
	 * Collection of index sources is performed in a recursive depth-first traversal.
	 *
	 * @param parentLayer
	 * @throws ModelException if the host driver cannot provide a required {@link Mapping}
	 */
	private void collectIndexSources0(ItemLayer parentLayer, List<IndexSource> storage) {
		DataSet<ItemLayer> baseLayers = parentLayer.getBaseLayers();
		for(ItemLayer baseLayer : baseLayers) {

			// Ignore base layers outside our "private" groups
			if(baseLayer.getLayerGroup()!=parentLayer.getLayerGroup()) {
				continue;
			}

			// Fetch required mapping and complain of missing
			Mapping mapping = getFileDriver().getMapping(parentLayer, baseLayer);
			if(mapping==null)
				throw new ModelException(ModelErrorCode.DRIVER_ERROR,
						"Missing mapping from "+ModelUtils.getUniqueId(parentLayer)+" to "+ModelUtils.getUniqueId(baseLayer));

			/*
			 *  NOTE: this fetches the largest container size for the parent layer.
			 *  This might be significantly higher than the buffer sizer we need for
			 *  the given combination (parentLayer -> baseLayer) but is an easy upper
			 *  boundary and the overhead should be manageable.
			 */
			int bufferSize = getRecommendedIndexBufferSize(parentLayer.getManifest());

			storage.add(new MappedIndexSource(mapping, bufferSize));

			// Recursion if base layer has additional dependencies
			if(!baseLayer.getBaseLayers().isEmpty()) {
				collectIndexSources0(baseLayer, storage);
			}
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
	protected class DelegatingCursor implements Cursor {

		/**
		 * Integer index of the file to be loaded
		 */
		protected final int fileIndex;

		/**
		 * Primary layer of group for which data chunks should be loaded
		 */
		protected final ItemLayer layer;

		/**
		 * Physical resource location
		 */
		protected final Path file;

		/**
		 * Chunk index for accessing physical data chunks, provided by host driver
		 */
		protected final ChunkIndex chunkIndex;

		public DelegatingCursor(int fileIndex, ItemLayer primaryLayer) {
			this.fileIndex = fileIndex;
			this.layer = primaryLayer;

			file = getFileForIndex(fileIndex);
			chunkIndex = getFileDriver().getChunkIndex(primaryLayer);
		}

		protected ChunkIndexCursor chunkIndexCursor;
		protected FileChannel fileChannel;
		protected final SubChannel blockChannel = new SubChannel();
		protected IndexSourceLookup indexSourceLookup;

		private volatile boolean open = false;

		/**
		 * Performs the following I/O related initialization steps:
		 * <ol>
		 * <li>{@link FileChannel#open(Path, java.nio.file.OpenOption...) Opening} of the underlying file channel</li>
		 * <li>Linking of the block-wise {@link SubChannel} to the open {@link FileChannel}</li>
		 * <li>{@link ChunkIndex#newCursor(boolean) Acquisition} of a read-only {@link ChunkIndexCursor}</li>
		 * <li>Creation of a new {@link IndexSourceLookup} ({@link AbstractConverter#createIndexSourcesForCursor(LayerGroup) delegated} to the host converter)</li>
		 * </ol>
		 *
		 * Finally the host converter is {@link AbstractConverter#cursorOpened(int, de.ims.icarus2.filedriver.Converter.Cursor) opened}
		 * to give it a chance for additional setup work.
		 *
		 * @throws IOException
		 */
		public void open() throws IOException {

			// Access raw input
			fileChannel = FileChannel.open(file, StandardOpenOption.READ);
			blockChannel.setSource(fileChannel);

			// Read-only access to the chunk index
			chunkIndexCursor = chunkIndex.newCursor(true);

			// Let converter customize the index sources for this cursor
			indexSourceLookup = createIndexSourcesForCursor(layer.getLayerGroup());

			// Inform converter (will be skipped in case of any exception)
			cursorOpened(fileIndex, this);

			// Only if all configuration steps went well actually set the cursor operational
			open = true;
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

			Item result = null;

			// Fetch location of chunk and only attempt to load data if chunk cursor has a valid location
			if(chunkIndexCursor.moveTo(index)) {
				long beginOffset = chunkIndexCursor.getBeginOffset();
				long endOffset = chunkIndexCursor.getEndOffset();
				long length = endOffset-beginOffset+1;

				// Reposition our channel window
				blockChannel.setOffsets(beginOffset, length);

				// Refresh root of index lookup to point to our current index
				indexSourceLookup.getIndexSource(0).reset(index);

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
				// No checked exception, so let's close this first
				chunkIndexCursor.close();

				// This might fail due to I/O stuff beyond our control
				fileChannel.close();

				//TODO do other cleanup work

			} finally {
				open = false;

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

		public IndexSourceLookup getIndexSourceLookup() {
			return indexSourceLookup;
		}

		public final FileChannel getFileChannel() {
			return fileChannel;
		}

		public final SubChannel getBlockChannel() {
			return blockChannel;
		}
	}

	/**
	 * Bridge to
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class IndexSourceLookup implements ModelConstants, AutoCloseable {

		/**
		 *
		 */
		private final IndexSource[] indexSources;

		/**
		 * Quick lookup storing the {@link LayerManifest#getUID() uid}s
		 * of source and target layer for the mapping at each level.
		 */
		private final int[] keys;

		public IndexSourceLookup(int depths) {
			checkArgument(depths>0);

			indexSources = new IndexSource[depths];
			keys = new int[depths];
		}

		public IndexSourceLookup(List<IndexSource> sources) {
			checkNotNull(sources);
			checkArgument(!sources.isEmpty());

			int size = sources.size();

			indexSources = new IndexSource[size];
			keys = new int[size];

			for(int i=0; i<size; i++) {
				configureLevel(i, sources.get(i));
			}
		}

		public IndexSource getIndexSource(int level) {
			return indexSources[level];
		}

		public void configureLevel(int level, IndexSource indexSource) {
			checkState("Level already configured: "+level, indexSources[level]==null);

			indexSources[level] = indexSource;
			keys[level] = indexSource.getLayerManifest().getUID();
		}

		/**
		 * @see java.lang.AutoCloseable#close()
		 */
		@Override
		public void close() {
			for(IndexSource indexSource : indexSources) {
				indexSource.close();
			}
		}

		public int getLevel(Mapping mapping) {
			int index = ArrayUtils.indexOf(keys, mapping.getTargetLayer().getUID());
			if(index<0)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Index source not available in this lookup: "+mapping.getManifest());
			return index;
		}

		public int getLevel(ItemLayerManifest targetLayer) {
			int index = ArrayUtils.indexOf(keys, targetLayer.getUID());
			if(index<0)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Index source not available in this lookup: "+targetLayer);
			return index;
		}
	}

	/**
	 * Generic interface for obtaining values for an item's {@link Item#getIndex() index}.
	 * Those values can come either from an existing {@link Mapping} implementation or a
	 * continuous stream of {@code long} values.
	 *
	 * This class exists as an intermediate abstraction layer so that converter
	 * implementations can unify the actual I/O work without worrying how to fetch index
	 * values for the newly created instances of model members.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static abstract class IndexSource implements AutoCloseable, ModelConstants {

		private final ItemLayerManifest layerManifest;

		protected IndexSource(ItemLayerManifest layerManifest) {
			checkNotNull(layerManifest);

			this.layerManifest = layerManifest;
		}

		/**
		 * Returns the {@code layer} for which this {@link IndexSource} produces values.
		 * @return
		 */
		public final ItemLayerManifest getLayerManifest() {
			return layerManifest;
		}

		/**
		 * Prepares this source to return index values for the specified {@code hostIndex}.
		 * Note that an implementation is not required to actually use this host index. For
		 * example a basic index source that provides the original index values for the
		 * scanning process of a file resource will simply return natural numbers in ascending
		 * order, completely ignoring the host indices.
		 * <p>
		 * It is advised for implementations to store the {@code hostIndex} used for the last
		 * call to this method in order to prevent unnecessary work in case the same {@link IndexSource}
		 * is used for multiple tasks.
		 *
		 * @param hostIndex
		 * @throws InterruptedException
		 */
		public abstract void reset(long hostIndex) throws InterruptedException;

		/**
		 * Release internal resources held by this index source.
		 *
		 * @see java.lang.AutoCloseable#close()
		 */
		@Override
		public abstract void close();

		/**
		 * Returns the number of index values currently available from this source.
		 * This is used by converter implementations to determine the required size
		 * of buffer objects when reading in data.
		 * <p>
		 * A return value of {@code -1} indicates that the number of index values
		 * is not known or that it is unlimited.
		 *
		 * @return
		 */
		public abstract long size();

		/**
		 * Returns the index value at the specified position.
		 *
		 * @param index
		 * @return
		 */
		public abstract long indexAt(long index);
	}

	public static class RootIndexSource extends IndexSource {

		private long index;

		/**
		 * @param layerManifest
		 */
		public RootIndexSource(ItemLayerManifest layerManifest) {
			super(layerManifest);
		}

		/**
		 * @see de.ims.icarus2.filedriver.AbstractConverter.IndexSource#reset(long)
		 */
		@Override
		public void reset(long hostIndex) {
			index = hostIndex;
		}

		/**
		 * @see de.ims.icarus2.filedriver.AbstractConverter.IndexSource#close()
		 */
		@Override
		public void close() {
			index = NO_INDEX;
		}

		/**
		 * @see de.ims.icarus2.filedriver.AbstractConverter.IndexSource#size()
		 */
		@Override
		public long size() {
			return index==NO_INDEX ? 0 : 1;
		}

		/**
		 * @see de.ims.icarus2.filedriver.AbstractConverter.IndexSource#indexAt(long)
		 */
		@Override
		public long indexAt(long index) {
			checkArgument(index==0);
			return this.index;
		}

	}

	/**
	 * Implements an {@link IndexSource} that produces a continuous stream of index values
	 * starting from a specified {@code begin index}. If can have an optional {@code length}
	 * defined which acts as a limit for arguments passed to the {@link #indexAt(long)} method.
	 * If no length is defined at construction time this source will produce up to
	 * {@link Long#MAX_VALUE} distinct index values.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class ContinuousIndexSource extends IndexSource {

		private final long beginIndex;
		private final long length;

		public ContinuousIndexSource(ItemLayerManifest layerManifest) {
			this(layerManifest, 0L, NO_INDEX);
		}

		public ContinuousIndexSource(ItemLayerManifest layerManifest, long beginIndex) {
			this(layerManifest, beginIndex, NO_INDEX);
		}

		public ContinuousIndexSource(ItemLayerManifest layerManifest, long beginIndex, long length) {
			super(layerManifest);

			checkArgument(beginIndex>=0L);

			this.beginIndex = beginIndex;
			this.length = length;
		}

		/**
		 * @see de.ims.icarus2.filedriver.AbstractConverter.IndexSource#reset(long)
		 */
		@Override
		public void reset(long hostIndex) {
			// no-op
		}

		/**
		 * @see de.ims.icarus2.filedriver.AbstractConverter.IndexSource#close()
		 */
		@Override
		public void close() {
			// no-op
		}

		/**
		 * @see de.ims.icarus2.filedriver.AbstractConverter.IndexSource#size()
		 */
		@Override
		public long size() {
			return length;
		}

		/**
		 * @see de.ims.icarus2.filedriver.AbstractConverter.IndexSource#indexAt(long)
		 */
		@Override
		public long indexAt(long index) {
			checkArgument(index>=0L);
			checkArgument(length==NO_INDEX || index<length);

			long result = beginIndex+index;
			// Overflow protection
			if(result<0L)
				throw new ModelException(GlobalErrorCode.INDEX_OVERFLOW,
						String.format("Long.MAX_VALUE overflow: beginIndex=%d index=%d", Long.valueOf(beginIndex), Long.valueOf(index)));
			return result;
		}

	}

	public static class MappedIndexSource extends IndexSource {

		private final Mapping mapping;
		private final boolean useSpanMapping;

		// SPAN SUPPORT
		private long spanBegin = NO_INDEX;
		private long spanEnd = NO_INDEX;

		// GENERAL MAPPING SUPPORT
		private final MappingReader mappingReader;
		private final IndexBuffer buffer;

		/**
		 * @param layerManifest
		 */
		public MappedIndexSource(Mapping mapping, int bufferSize) {
			super(mapping.getTargetLayer());

			MappingManifest mappingManifest = mapping.getManifest();
			checkArgument("Mapping relation no supported: "+mappingManifest.getRelation(),
					mappingManifest.getRelation()==Relation.ONE_TO_ONE || mappingManifest.getRelation()==Relation.ONE_TO_MANY);

			checkArgument(bufferSize>0);

			this.mapping = mapping;

			/*
			 * Important optimization step is to determine whether we can use a span-based
			 * mapping approach.
			 */

			MappingReader mappingReader = null;
			IndexBuffer buffer = null;

			useSpanMapping = (mappingManifest.getRelation()==Relation.ONE_TO_ONE
					|| mappingManifest.getCoverage()==Coverage.TOTAL_MONOTONIC);

			if(!useSpanMapping) {
				mappingReader = mapping.newReader();
				buffer = new IndexBuffer(bufferSize);
			}

			this.mappingReader = mappingReader;
			this.buffer = buffer;
		}

		/**
		 * @return the mapping
		 */
		public Mapping getMapping() {
			return mapping;
		}

		/**
		 * @throws InterruptedException
		 * @see de.ims.icarus2.filedriver.AbstractConverter.IndexSource#reset(long)
		 */
		@Override
		public void reset(long hostIndex) throws InterruptedException {
			try {
				mappingReader.begin();

				if(useSpanMapping) {
					spanBegin = mappingReader.getBeginIndex(hostIndex, RequestSettings.emptySettings);
					spanEnd = mappingReader.getEndIndex(hostIndex, RequestSettings.emptySettings);
				} else {
					buffer.clear();
					mappingReader.lookup(hostIndex, buffer, RequestSettings.emptySettings);
				}
			} finally {
				mappingReader.end();
			}
		}

		/**
		 * @see de.ims.icarus2.filedriver.AbstractConverter.IndexSource#close()
		 */
		@Override
		public void close() {
			if(mappingReader!=null) {
				mappingReader.close();
			}
		}

		/**
		 * @see de.ims.icarus2.filedriver.AbstractConverter.IndexSource#size()
		 */
		@Override
		public long size() {
			return useSpanMapping ? (spanEnd-spanBegin+1) : buffer.size();
		}

		/**
		 * @see de.ims.icarus2.filedriver.AbstractConverter.IndexSource#indexAt(long)
		 */
		@Override
		public long indexAt(long index) {
			if(useSpanMapping) {
				long result = spanBegin+index;
				if(result>spanEnd)
					throw new ModelException(GlobalErrorCode.INVALID_INPUT,
							Messages.indexOutOfBoundsMessage("MappedIndexSource.indexAt(int)", 0, size(), index));
				return result;
			} else {
				return buffer.indexAt(IcarusUtils.ensureIntegerValueRange(index));
			}
		}

	}
}
