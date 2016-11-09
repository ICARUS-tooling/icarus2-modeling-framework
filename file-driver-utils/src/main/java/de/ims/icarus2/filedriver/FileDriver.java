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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.PrimitiveIterator.OfLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.Report;
import de.ims.icarus2.Report.ReportItem;
import de.ims.icarus2.ReportBuilder;
import de.ims.icarus2.filedriver.Converter.ConverterProperty;
import de.ims.icarus2.filedriver.FileDataStates.ElementInfo;
import de.ims.icarus2.filedriver.FileDataStates.FileInfo;
import de.ims.icarus2.filedriver.FileDataStates.LayerInfo;
import de.ims.icarus2.filedriver.FileDriverMetadata.ChunkIndexKey;
import de.ims.icarus2.filedriver.FileDriverMetadata.DriverKey;
import de.ims.icarus2.filedriver.FileDriverMetadata.FileKey;
import de.ims.icarus2.filedriver.FileDriverMetadata.ItemLayerKey;
import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;
import de.ims.icarus2.filedriver.io.sets.FileSet;
import de.ims.icarus2.filedriver.mapping.AbstractStoredMapping;
import de.ims.icarus2.filedriver.mapping.DefaultMappingFactory;
import de.ims.icarus2.filedriver.mapping.MappingFactory;
import de.ims.icarus2.filedriver.mapping.chunks.ChunkIndex;
import de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexStorage;
import de.ims.icarus2.filedriver.mapping.chunks.DefaultChunkIndex;
import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.ChunkInfo;
import de.ims.icarus2.model.api.driver.ChunkState;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory;
import de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.IndexSetBuilder;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingStorage;
import de.ims.icarus2.model.api.driver.mods.DriverModule;
import de.ims.icarus2.model.api.io.FileManager;
import de.ims.icarus2.model.api.io.resources.FileResource;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.driver.AbstractDriver;
import de.ims.icarus2.model.standard.driver.BufferedItemManager;
import de.ims.icarus2.model.standard.driver.ChunkConsumer;
import de.ims.icarus2.model.standard.driver.ChunkInfoBuilder;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.classes.Lazy;


/**
 * @author Markus Gärtner
 *
 */
public class FileDriver extends AbstractDriver {

	private static final int DEFAULT_CHUNK_INFO_SIZE = 200;

	/**
	 * Registry for storing metadata info like number of items in each layer,
	 * availability of chunk indices for certain files, etc...
	 */
	private final MetadataRegistry metadataRegistry;

	/**
	 * Storage for our model instances
	 */
	protected BufferedItemManager content; //FIXME needs initialization in one of the connection substeps

	/**
	 * Runtime states for files and associated resources.
	 */
	private FileDataStates states;

	/**
	 * Our bridge between physical format and the model instances we manage
	 */
	protected final Lazy<Converter> converter;

	/**
	 * Chunk indices for the primary layers in each layer group of the host driver.
	 * This is an optional feature and connector implementations are free to decide on their
	 * own whether or not it makes sense for a certain data set to map chunks at all
	 * or just do a "read all" operation when asked to load items from it.
	 */
	private volatile ChunkIndexStorage chunkIndexStorage;

	/**
	 * Switch to indicate once we made our first attempt at creating chunk indices
	 * after it has been deemed necessary to actually use them. This prevents
	 * repeated attempts and allows treating a missing {@link ChunkIndexStorage}
	 * as actual error.
	 */
	private volatile boolean chunkIndexCreationAttempted = false;

	/**
	 * Set of corpus files that contain the data for this driver
	 */
	protected final FileSet dataFiles;

	/**
	 * Locks for individual files.
	 * <p>
	 * Will be populated lazily when actually needed.
	 */
	private final Int2ObjectMap<StampedLock> fileLocks;

	private static Logger log = LoggerFactory.getLogger(FileDriver.class);

	/**
	 * @param manifest
	 * @param corpus
	 * @throws ModelException
	 */
	protected FileDriver(FileDriverBuilder<?> builder) {
		super(builder);

		metadataRegistry = builder.getMetadataRegistry();
		dataFiles = builder.getDataFiles();

		fileLocks = new Int2ObjectOpenHashMap<>(dataFiles.getFileCount());

		converter = Lazy.create(this::createConverter, true);
	}

	protected Converter createConverter() {

		Converter converter = null; //TODO
		converter.init(this);

		return converter;
	}

	public Converter getConverter() {
		return converter.value();
	}

	protected final StampedLock getLockForFile(int fileIndex) {
		Path file = dataFiles.getFileAt(fileIndex);
		if(file==null)
			throw new ModelException(ModelErrorCode.DRIVER_ERROR,
					"Could not find a valid file to fetch lock for at index "+fileIndex);

		StampedLock lock = fileLocks.get(fileIndex);
		if(lock==null) {
			synchronized (fileLocks) {
				lock = fileLocks.get(fileIndex);
				if(lock==null) {
					lock = new StampedLock();
					fileLocks.put(fileIndex, lock);
				}
			}
		}

		return lock;
	}

	@Override
	public String toString() {
		return getClass().getName()+"@FileDriver["+getManifest().getId()+"]";
	}

	/**
	 *
	 *
	 * @see de.ims.icarus2.model.standard.driver.AbstractDriver#createCustomLayers(de.ims.icarus2.model.manifest.api.ContextManifest)
	 */
	@Override
	protected Options createCustomLayers(ContextManifest manifest) {

		Options options = new Options();

		for(LayerManifest layer : manifest.getLayerManifests(ModelUtils::isItemLayer)) {
			//TODO access states and create optimized layers?
		}

		return options;
	}

	public FileSet getDataFiles() {
		return dataFiles;
	}

	public MetadataRegistry getMetadataRegistry() {
		return metadataRegistry;
	}

	/**
	 * Should only be used by modules of the driver or other code that takes part in the
	 * data preparation process!
	 *
	 * @return
	 */
	public FileDataStates getFileStates() {
		checkConnected();
		return states;
	}

	/**
	 * Creates the basic mappings for this driver implementation.
	 * <p>
	 * The default implementation just traverses all mapping manifests declared for this driver
	 * and delegates to the internal {@link #defaultCreateMapping(MappingFactory, MappingManifest, Options, boolean)}
	 * with an {@link Options} instance that contains the original mapping in case a reverse mapping
	 * is to be created and which is otherwise empty. If subclasses wish to customize the generation
	 * process they should either override the above mentioned default method to
	 * <p>
	 * If a subclass wants to more closely control the creation of mappings, it should override
	 * this method and create every mapping instance either by using a {@code MappingFactory} object
	 * and customize the generation by supplying appropriate {@code options} or by manually
	 * instantiating the mappings.
	 *
	 * @param manifest
	 * @return
	 */
	@Override
	protected MappingStorage createMappings() {

		DriverManifest manifest = getManifest();
		MappingStorage.Builder builder = new MappingStorage.Builder();

		// Allow for subclasses to provide a fallback function
		BiFunction<ItemLayerManifest, ItemLayerManifest, Mapping> fallback = getMappingFallback();
		if(fallback!=null) {
			builder.fallback(fallback);
		}

		MappingFactory mappingFactory = new DefaultMappingFactory(this);

		manifest.forEachMappingManifest(m -> {
			Options options = new Options();

			Mapping mapping = defaultCreateMapping(mappingFactory, m, options);
			builder.addMapping(mapping);
		});

		return builder.build();
	}

	/**
	 * Default implementation delegates directly to {@link FileDriverUtils#createMapping(MappingFactory, MappingManifest, MetadataRegistry, Options)}.
	 *
	 */
	protected Mapping defaultCreateMapping(MappingFactory mappingFactory, MappingManifest mappingManifest, Options options) {
		return FileDriverUtils.createMapping(mappingFactory, mappingManifest, metadataRegistry, options);
	}

	public void resetMappings() {

		Lock lock = getGlobalLock();

		lock.lock();

		try {
			getMappings().forEachMapping(m -> {

				// Try to delete all "stored mappings"
				if(AbstractStoredMapping.class.isInstance(m)) {
					AbstractStoredMapping mapping = (AbstractStoredMapping) m;
					try {
						mapping.delete();
					} catch (Exception e) {
						log.error("Failed to delete mapping storage", e);
					}
				}
			});
		} finally {
			lock.unlock();
		}
	}

	protected ChunkIndexStorage getChunkIndexStorage() {
		ChunkIndexStorage result = chunkIndexStorage;
		boolean creationAttempted = chunkIndexCreationAttempted;

		if(result==null && creationAttempted)
			throw new ModelException(getCorpus(), GlobalErrorCode.ILLEGAL_STATE,
					"Previous attempt to create chunk index storage failed - no storage available");

		return result;
	}

	@Override
	public void forEachModule(Consumer<? super DriverModule> action) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.Driver#getItemCount(de.ims.icarus2.model.api.layer.ItemLayer)
	 */
	@Override
	public long getItemCount(ItemLayer layer) {
		checkConnected();

		return getItemCount(layer.getManifest());
	}

	/**
	 * Generates a lookup key using {@link ItemLayerKey#ITEMS#getKey(ItemLayerManifest)}. This key
	 * is then used to query the underlying {@link MetadataRegistry} for the mapped
	 * {@link MetadataRegistry#getLongValue(String, long) long value}, supplying {@value ModelConstants#NO_INDEX}
	 * as default value for the case that no matching entry was found.
	 *
	 * TODO refresh description
	 *
	 * @see de.ims.icarus2.model.api.driver.Driver#getItemCount(de.ims.icarus2.model.manifest.api.ItemLayerManifest)
	 * @see ItemLayerKey#ITEMS
	 */
	@Override
	public long getItemCount(ItemLayerManifest layer) {
		checkReady();

		LayerInfo info = getFileStates().getLayerInfo(layer);

		return info==null ? NO_INDEX : info.getSize();
	}

	public BufferedItemManager.LayerBuffer getLayerBuffer(ItemLayer layer) {
		checkConnected();

		return content.getBuffer(layer);
	}

	@Override
	public void addItem(ItemLayer layer, Item item, long index) {
		checkConnected();

		// TODO Auto-generated method stub
		super.addItem(layer, item, index);
	}

	@Override
	public void removeItem(ItemLayer layer, Item item, long index) {
		checkConnected();

		// TODO Auto-generated method stub
		super.removeItem(layer, item, index);
	}

	@Override
	public void moveItem(ItemLayer layer, Item item, long fromIndex, long toIndex) {
		checkConnected();

		// TODO Auto-generated method stub
		super.moveItem(layer, item, fromIndex, toIndex);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.ItemLayerManager#getItem(de.ims.icarus2.model.api.layer.ItemLayer, long)
	 */
	@Override
	public Item getItem(ItemLayer layer, long index) {
		checkConnected();

		return getLayerBuffer(layer).fetch(index);
	}

	/**
	 * This implementation initializes the internal {@link FileDataStates} storage
	 * before calling the super method.
	 * After that, a customizable series of {@link PreparationStep preparation steps} is performed
	 * to allow for a more flexible initialization.
	 *
	 * @see de.ims.icarus2.model.standard.driver.AbstractDriver#doConnect()
	 * @see #getPreparationSteps()
	 */
	@Override
	protected void doConnect() throws InterruptedException {

		MetadataRegistry metadataRegistry = getMetadataRegistry();

		metadataRegistry.beginUpdate();
		try {
			// Slight violation of the super contract since we set this up before delegating to super method!
			states = new FileDataStates(this);

			// Creates context and mappings
			super.doConnect();

			// Fetch customized steps to be performed next
			//FIXME move this over to a DAG style execution order making sure of the getPreconditions() result of each step
			final PreparationStep[] steps = getPreparationSteps();
			ReportBuilder<ReportItem> reportBuilder = ReportBuilder.newBuilder(getManifest());
			reportBuilder.addInfo("Connecting to corpus");

			boolean driverIsValid = true;

			// Environmental lookup
			Options env = new Options();
			//TODO maybe fill this with internal information not directly accessible via public instance methods

			for(int i=0; i<steps.length; i++) {

				checkInterrupted();

				PreparationStep step = steps[i];

				boolean valid = true;

				try {
					valid = step.apply(this, reportBuilder, env);
				} catch(Exception e) {
					reportBuilder.addError(ModelErrorCode.DRIVER_ERROR,
							"Preparation step {} of {} failed: {}", Integer.valueOf(i+1),
							Integer.valueOf(steps.length), step.name(), e);
					valid = false;
				}

				if(!valid) {
					driverIsValid = false;
					break;
				}
			}

			if(!driverIsValid) {
				states.getGlobalInfo().setFlag(ElementFlag.UNUSABLE);
			}


			log.info(reportBuilder.build().toString());

		} finally {
			metadataRegistry.endUpdate();
		}
	}

	protected void checkInterrupted() throws InterruptedException {
		if(Thread.interrupted())
			throw new InterruptedException();
	}

	protected PreparationStep[] getPreparationSteps() {
		return StandardPreparationSteps.values();
	}

	/**
	 * Calls the super method and then closes the internal {@link MetadataRegistry}
	 * and discards the current set of {@link FileDataStates states}.
	 *
	 * @see de.ims.icarus2.model.standard.driver.AbstractDriver#doDisconnect()
	 */
	@Override
	protected void doDisconnect() throws InterruptedException {

		@SuppressWarnings("resource")
		MetadataRegistry metadataRegistry = getMetadataRegistry();

		metadataRegistry.beginUpdate();
		try {
			super.doDisconnect();

			//TODO shut down converter and persist current filestates in metadata registry

			try {
				content.close();
			} catch(Exception e) {
				log.error("Error during shutdown of layer buffer", e);
			}

			states = null;
		} finally {
			metadataRegistry.endUpdate();
			metadataRegistry.close();
		}
	}

	public boolean hasChunkIndex() {
		checkConnected();
		return chunkIndexStorage!=null;
	}

	/**
	 * Helper method to find the index of a file that is known to contain the
	 * specified item index of a given layer.
	 *
	 * @param index
	 * @param layer
	 * @param min first fileIndex to look at (inclusive)
	 * @param max last fileIndex to look at (exclusive)
	 * @return
	 *
	 * @see Arrays#binarySearch(long[], int, int, long)
	 */
	private int findFile(long index, ItemLayerManifest layer, int min, int max) {
        int low = min;
        int high = max - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
			FileInfo info = states.getFileInfo(mid);

            if (info.getEndIndex(layer) < index)
                low = mid + 1;
            else if (info.getBeginIndex(layer) > index)
                high = mid - 1;
            else
                return mid; // file found
        }
        return -(low + 1);  // file not found.
	}

	/**
	 * Scans the file specified by the given {@code fileIndex} and stores
	 * the acquired metadata.
	 * <p>
	 * Per default this method will be called in the order of their index for all
	 * files that have not been previously marked as {@link FileKey#SCANNED scanned}
	 * in the metadata. As a result of this strategy, each invocation of this method
	 * is guaranteed to have full access to all collected metadata of previously
	 * scanned files. This means that all index values to be used as begin indices
	 * for the first item of each layer in the file will be available via the
	 * respective {@link FileInfo} object.
	 *
	 * @param fileIndex
	 * @throws Exception
	 */
	public void scanFile(int fileIndex) throws IOException, InterruptedException {

		/*
		 *  Start by checking whether or not we should use a chunk index.
		 *  Normally this will be determined primarily by looking at the
		 *  total combined size of all the resource files.
		 *
		 *  If there are no chunk indices present and we need them -> create a new storage
		 */
		Lock globalLock = getGlobalLock();
		globalLock.lock();
		try {
			if(chunkIndexStorage==null && !chunkIndexCreationAttempted && isChunkIndicesRequired()) {
				chunkIndexCreationAttempted = true;

				// Create new storage, verify it and then assign as new global
				ChunkIndexStorage newStorage = createChunkIndices();
				if(newStorage==null)
					throw new ModelException(GlobalErrorCode.INTERNAL_ERROR,
							"Created storage for chunk indices must not be null!");

				chunkIndexStorage = newStorage;
			}
		} finally {
			globalLock.unlock();
		}

		scanFileSynchronized(fileIndex);
	}

	/**
	 * Asks the {@link #getConverter() converter} first if it {@link Converter#isChunkIndexSupported() supports}
	 * chunk indexing and if so, continues with the following:
	 * <br>
	 * Lookup metadata information about all the resource files and decide whether
	 * or not the driver should use chunk indices.
	 *
	 * @return
	 */
	protected boolean isChunkIndicesRequired() {

		// Check first if converter even supports chunking
		Converter converter = getConverter();
		if(!((Boolean)converter.getPropertyValue(ConverterProperty.CHUNK_INDEX_SUPPORTED)).booleanValue()) {
			return false;
		}

		// Query global settings for file size threshold
		long fileSizeThreshold = getFileSizeThresholdForChunking();
		if(fileSizeThreshold!=-1L) {
			long totalSize = 0L;

			ElementInfo globalInfo = getFileStates().getGlobalInfo();
			String savedTotalSize = globalInfo.getProperty(DriverKey.SIZE.getKey());
			if(savedTotalSize!=null) { //TODO maybe if there's no value stored we should traverse file metadata to compute total size?
				totalSize = Long.parseLong(savedTotalSize);
			}

			if(totalSize!=0L && totalSize>=fileSizeThreshold) {
				return true;
			}
		}

		//TODO access driver settings

		//TODO access metadata and check total file size against threshold or manual user override in settings

		return false;
	}

	/**
	 * Fetches or computes the (total) file size for which chunking should be employed.
	 * A return value of {@code -1} means that no such threshold exists and the driver
	 * should use other means of deciding upon that matter.
	 *
	 * @return
	 */
	protected long getFileSizeThresholdForChunking() {
		String key = FileDriverUtils.FILE_SIZE_THRESHOLD_FOR_CHUNKING_PROPERTY;
		String value = getCorpus().getManager().getProperty(key);

		if(value==null || value.isEmpty()) {
			return -1L;
		}

		long threshold = -1L;

		try {
			threshold = Long.parseLong(value);
		} catch(NumberFormatException e) {
			log.warn("Error while reading property '{1}' from global settings - value '{2}' was expected to be a long integer", key, value, e);
		}

		// Collapse all "invalid" values into the same "ignore" value
		if(threshold<=0) {
			threshold = -1;
		}

		return threshold;
	}

	/**
	 *
	 *
	 * @return
	 */
	protected ChunkIndexStorage createChunkIndices() {

		// Allow converter to have a shot at deciding upon chunk index implementations first
		Converter converter = getConverter();
		ChunkIndexStorage result = converter.createChunkIndices();
		if(result!=null) {
			return result;
		}

		ContextManifest contextManifest = getManifest().getContextManifest();
		ChunkIndexStorage.Builder builder = new ChunkIndexStorage.Builder();

		// Traverse layer groups and create a chunk index for each group's primary layer
		contextManifest.forEachGroupManifest(manifest -> {

			ChunkIndex chunkIndex = createChunkIndex(manifest);


			// Only skips a layer/group if explicitly set so
			if(chunkIndex!=null) {
				builder.add(manifest.getPrimaryLayerManifest(), chunkIndex);
			}
		});

		return builder.build();
	}

	protected ChunkIndex createChunkIndex(LayerGroupManifest groupManifest) {
		ItemLayerManifest layerManifest = groupManifest.getPrimaryLayerManifest();
		MetadataRegistry metadataRegistry = getMetadataRegistry();

		// First check if we should skip the specified group

		String useChunkIndexKey = ItemLayerKey.USE_CHUNK_INDEX.getKey(layerManifest);
		/*
		 *  We use "true" as default value to catch an explicitly saved value
		 *  of "false". This way if no entry exists for this key we automatically
		 *  continue to creating the respective metadata later in this method.
		 */
		boolean savedUseChunkIndex = metadataRegistry.getBooleanValue(useChunkIndexKey, true);

		// Honor metadata information about skipping chunking for this layer!
		if(!savedUseChunkIndex) {
			return null;
		}

		// Fetch combined size of all files
		long totalFileSize = 0L;
		ElementInfo globalInfo = getFileStates().getGlobalInfo();
		String savedTotalSize = globalInfo.getProperty(DriverKey.SIZE.getKey());
		if(savedTotalSize!=null) {
			totalFileSize = Long.parseLong(savedTotalSize);
		}


		//*******************************************
		//  Physical path of chunk index
		//*******************************************
		Path path = null;
		String pathKey = ChunkIndexKey.PATH.getKey(layerManifest);
		String savedPath = metadataRegistry.getValue(pathKey);

		if(savedPath==null) {

			// Check driver settings first
			Path folder = (Path) OptionKey.CHUNK_INDICES_FOLDER.getValue(getManifest());

			if(folder==null) {
				// Try to find a suitable location for the chunk index
				FileManager fileManager = getCorpus().getManager().getFileManager();

				// If at this point we have no valid file manager there is no way of finding a suitable file location
				if(fileManager==null) {
					return null;
				}

				folder = fileManager.getCorpusFolder(getCorpus().getManifest());
			}

			// Use a file named after the layer itself inside whatever folder we should use
			String filename = layerManifest.getId()+FileDriverUtils.CHUNK_INDEX_FILE_ENDING;
			path = folder.resolve(filename);
			savedPath = path.toString();

			// Make sure to persist the file path to metadata for future lookups
			metadataRegistry.setValue(pathKey, savedPath);
		} else {
			path = Paths.get(savedPath);
		}

		checkState("Failed to obtain valid file path for chunk index: "+layerManifest, path!=null);

		//*******************************************
		//  ValueType for chunk entries
		//*******************************************
		IndexValueType valueType = null;
		String valueTypeKey = ChunkIndexKey.VALUE_TYPE.getKey(layerManifest);
		String savedValueType = metadataRegistry.getValue(valueTypeKey);

		if(savedValueType==null) {

			// If possible try to estimate the required value type
			if(totalFileSize!=0L) {
				int estimatedChunkSize = getEstimatedChunkSizeForGroup(groupManifest);

				if(estimatedChunkSize>0) {
					long estimatedChunkCount = (long) Math.ceil((double)totalFileSize/(double)estimatedChunkSize);

					// If something went wrong with the estimation we just assume the worst case scenario
					if(estimatedChunkCount<=0) {
						estimatedChunkCount = Long.MAX_VALUE;
					}

					/*
					 *  As a safety measure we assume that our guess only accounts for 25%
					 *  of the potential total size the actual scanning method will encounter.
					 *
					 *  This means that for estimated counts that are close to value type boundaries
					 *  we will most likely shift into the next higher value space and potentially
					 *  "waste" some space.
					 */
					if(estimatedChunkCount<Integer.MAX_VALUE) {
						estimatedChunkCount <<= 2;
					}

					valueType = IndexValueType.forValue(estimatedChunkCount);
				}
			}

			// If we couldn't make a valid estimation, go use LONG as fallback
			if(valueType==null) {
				valueType = IndexValueType.LONG;
			}

			savedValueType = valueType.getStringValue();

			metadataRegistry.setValue(valueTypeKey, savedValueType);
		} else {
			valueType = FileDriverUtils.toValueType(savedValueType);
		}

		checkState("Failed to obtain value type for chunk index: "+layerManifest, valueType!=null);

		//*******************************************
		//  Size of block cache
		//*******************************************
		String cacheSizeKey = ChunkIndexKey.CACHE_SIZE.getKey(layerManifest);
		int cacheSize = metadataRegistry.getIntValue(cacheSizeKey, -1);

		if(cacheSize==-1) {
			// Fetch default value from global config
			String key = FileDriverUtils.CACHE_SIZE_FOR_CHUNKING_PROPERTY;
			String value = getCorpus().getManager().getProperty(key);

			if(value!=null && !value.isEmpty()) {
				cacheSize = Integer.parseInt(value);
			} else {
				// Very conservative cache size as fallback
				cacheSize = 10;
			}

			metadataRegistry.setIntValue(cacheSizeKey, cacheSize);
		}

		checkState("Failed to obtain cache size for chunk index: "+layerManifest, cacheSize>=0);

		//*******************************************
		//  Size of blocks for buffering in 2^blockPower frames
		//*******************************************
		String blockPowerKey = ChunkIndexKey.BLOCK_POWER.getKey(layerManifest);
		int blockPower = metadataRegistry.getIntValue(blockPowerKey, -1);

		if(blockPower==-1) {
			// Fetch default block power from global config
			String key = FileDriverUtils.BLOCK_POWER_FOR_CHUNKING_PROPERTY;
			String value = getCorpus().getManager().getProperty(key);

			if(value!=null && !value.isEmpty()) {
				blockPower = Integer.parseInt(value);
			} else {
				// 4096 frames per block
				//TODO maybe have some smarter way of deciding upon this value?
				blockPower = 12;
			}

			metadataRegistry.setIntValue(blockPowerKey, blockPower);
		}

		checkState("Failed to obtain block power for chunk index: "+layerManifest, blockPower>0);

		//*******************************************
		//  BlockCache used for buffering
		//*******************************************
		BlockCache blockCache = null;
		String blockCackeKey = ChunkIndexKey.BLOCK_CACHE.getKey(layerManifest);
		String savedBlockCache = metadataRegistry.getValue(blockCackeKey);

		if(savedBlockCache==null) {
			// Per default we'll always use a cache with least-recently-used purging policy
			savedBlockCache = FileDriverUtils.HINT_LRU_CACHE;

			metadataRegistry.setValue(blockCackeKey, savedBlockCache);
		}
		blockCache = FileDriverUtils.toBlockCache(savedBlockCache);

		checkState("Failed to obtain block cache for chunk index: "+layerManifest, blockCache!=null);

		// Finally throw all the settings into a builder and let it assemble the actual chunk index instance
		return DefaultChunkIndex.builder()
				.fileSet(getDataFiles())
				.indexValueType(valueType)
				.resource(new FileResource(path))
				.blockCache(blockCache)
				.cacheSize(cacheSize)
				.blockPower(blockPower)
				.build();
	}

	protected int getEstimatedChunkSizeForGroup(LayerGroupManifest groupManifest) {

		// Check converter's estimation first
		Converter converter = getConverter();
		@SuppressWarnings("rawtypes")
		Map lookup = (Map) converter.getPropertyValue(ConverterProperty.EXTIMATED_CHUNK_SIZES);
		String key = groupManifest.getId()+FileDriverUtils.ESTIMATED_CHUNK_SIZE_SUFFIX;
		Integer value = (Integer) lookup.get(key);
		int converterEstimation = value!=null ? value.intValue() : -1;

		// Check driver's settings next


		// Check metadata
	}

	/**
	 * Wraps the {@link Converter#scanFile(int, ChunkIndexStorage)} call into a
	 * synchronized block based on the file's {@link #getReadLockForFile(int) read lock}.
	 *
	 * @see de.ims.icarus2.filedriver.FileDriver#scanFileSynchronized(int)
	 */
	protected Report<ReportItem> scanFileSynchronized(int fileIndex) throws IOException,
			InterruptedException {

		StampedLock lock = getLockForFile(fileIndex);

		long stamp = lock.readLock();
		try {
			return getConverter().scanFile(fileIndex, chunkIndexStorage);
		} finally {
			lock.unlockRead(stamp);
		}
	}

	/**
	 * Translates the indices to indices in the surrounding layer group in case the specified
	 * layer is not the respective primary layer and then delegates to an internal
	 * {@link #loadPrimaryLayer(IndexSet[], ItemLayer, Consumer) load} method.
	 *
	 * @see de.ims.icarus2.model.api.members.item.ItemLayerManager#load(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.layer.ItemLayer, java.util.function.Consumer)
	 */
	@Override
	public long load(IndexSet[] indices, ItemLayer layer,
			Consumer<ChunkInfo> action) throws InterruptedException {
		checkNotNull(indices);
		checkNotNull(layer);

		checkConnected();
		checkReady();

		Lock globalLock = getGlobalLock();
		globalLock.lock();
		try {

			final LayerGroup group = layer.getLayerGroup();
			final ItemLayer primaryLayer = group.getPrimaryLayer();
			final boolean mappingRequired = layer!=primaryLayer;

			if(mappingRequired) {
				indices = mapIndices(layer.getManifest(), primaryLayer.getManifest(), indices);
			}

			/*
			 *  Delegate loading.
			 *  We do not forward the consumer action for publishing since that is only
			 *  required when actually loading chunks from a backend storage!
			 */
			Loader loader = new Loader(indices, getLayerBuffer(primaryLayer), null);

			// Potentially long running part
			loader.execute();

			// Only in case of missing items do we need to start mapping and/or accessing underlying resources
			if(loader.getMissingIndicesCount()>0L) {
				IndexSet[] missingIndices = loader.getMissingIndices();

				// Delegate to chunk aware method to do the actual I/O stuff
				return loadPrimaryLayer(missingIndices, primaryLayer, action);
			} else {
				// Nothing to be loaded
				return 0L;
			}
		} finally {
			globalLock.unlock();
		}
	}

	/**
	 * Not meant to be used by multiple threads!
	 *
	 * @author Markus Gärtner
	 *
	 */
	protected class Loader {

		private final IndexSet[] indices;

		/**
		 * Hint for lazy construction of buffers.
		 * Meant to represent the total number of index values in the original request.
		 */
		private final long requestedItemCount;

		private final boolean inputSorted;

		/**
		 * Callback for successfully loaded chunks.
		 */
		private final ChunkConsumer publisher;

		/**
		 * Actual storage to query
		 */
		private final BufferedItemManager.LayerBuffer buffer;

		/**
		 * Storage for index values of chunks that need to be loaded
		 */
		private IndexSetBuilder missingIndices;

		private long missingIndicesCount = 0L;
		private long availableIndicesCount = 0L;

		public Loader(IndexSet[] indices, BufferedItemManager.LayerBuffer buffer, Consumer<ChunkInfo> action) {
			this.indices = indices;
			this.buffer = buffer;

			requestedItemCount = IndexUtils.count(indices);
			inputSorted = IndexUtils.isSorted(indices);

			int recommendedBufferSize = (int) Math.min(DEFAULT_CHUNK_INFO_SIZE, requestedItemCount);
			publisher = createPublisher(action, recommendedBufferSize);
		}

		public void execute() {
			OfLong it = IndexSet.asIterator(indices);

			buffer.load(it, this::onAvailableItem, this::onMissingItem);
		}

		/**
		 * Called only for missing items by the {@link BufferedItemManager.LayerBuffer}
		 */
		public void onMissingItem(long index) {
			missingIndicesCount++;

			if(missingIndices==null) {
				IndexCollectorFactory factory = new IndexCollectorFactory();
				factory.totalSizeLimit(requestedItemCount);
				factory.inputSorted(inputSorted);

				missingIndices = factory.create();
			}

			missingIndices.add(index);
		}

		public void onAvailableItem(Item item, long index) {
			ChunkState chunkState = ChunkState.CORRUPTED;
			if(item.isUsable()) {
				availableIndicesCount++;
			}
			publisher.accept(index, item, chunkState);
		}

		public IndexSet[] getMissingIndices() {
			return missingIndices==null ? null : missingIndices.build();
		}

		public long getRequestedItemCount() {
			return requestedItemCount;
		}

		public boolean isInputSorted() {
			return inputSorted;
		}

		public long getMissingIndicesCount() {
			return missingIndicesCount;
		}

		public long getAvailableIndicesCount() {
			return availableIndicesCount;
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.ItemLayerManager#release(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.layer.ItemLayer)
	 */
	@Override
	public void release(IndexSet[] indices, ItemLayer layer)
			throws InterruptedException {
		checkNotNull(indices);
		checkNotNull(layer);

		checkConnected();
		checkReady();

		Lock globalLock = getGlobalLock();
		globalLock.lock();
		try {

			final LayerGroup group = layer.getLayerGroup();
			final ItemLayer primaryLayer = group.getPrimaryLayer();
			final boolean mappingRequired = layer!=primaryLayer;

			if(mappingRequired) {
				indices = mapIndices(layer.getManifest(), primaryLayer.getManifest(), indices);
			}
		} finally {
			globalLock.unlock();
		}
	}

	protected long loadPrimaryLayer(IndexSet[] indices, ItemLayer layer,
			Consumer<ChunkInfo> action) throws InterruptedException {
		checkConnected();
		checkReady();

		// If the driver supports chunking and has created a chunk index, then use the easy path
		if(hasChunkIndex()) {
			try {
				return loadChunks(layer, indices, action);
			} catch (IOException e) {
				throw new ModelException(ModelErrorCode.DRIVER_ERROR,
						"Failed to delegate loading of data chunks in layer "+ModelUtils.getUniqueId(layer)+" to connector", e);
			}
		} else {
			// No chunk index available, proceed with loading the respective files
			int fileCount = dataFiles.getFileCount();

			long minIndex = IndexUtils.firstIndex(indices);
			long maxIndex = IndexUtils.lastIndex(indices);

			// Find first and last file to contain the indices

			int firstFile = findFile(minIndex, layer.getManifest(), 0, fileCount);
			if(firstFile<0)
				throw new ModelException(ModelErrorCode.DRIVER_METADATA,
						"Could not find file index for item index "+minIndex+" in layer "+ModelUtils.getUniqueId(layer));
			int lastFile = findFile(maxIndex, layer.getManifest(), firstFile, fileCount);
			if(lastFile<0)
				throw new ModelException(ModelErrorCode.DRIVER_METADATA,
						"Could not find file index for item index "+maxIndex+" in layer "+ModelUtils.getUniqueId(layer));

			long loadedItems = 0L;

			for(int fileIndex = firstFile; fileIndex<=lastFile; fileIndex++) {

				FileInfo fileInfo = states.getFileInfo(fileIndex);
				if(!fileInfo.isValid())
					throw new ModelException(ModelErrorCode.DRIVER_ERROR,
							"Cannot attempt to load from invalid file at index "+fileIndex);

				if(fileInfo.isFlagSet(ElementFlag.PARTIALLY_LOADED))
					throw new ModelException(ModelErrorCode.DRIVER_ERROR,
							"Cannot attempt to completely load already partially loaded file at index "+fileIndex);

				if(fileInfo.isFlagSet(ElementFlag.LOADED)) {
					// Skip file entirely (no need to publish chunk info in this case either)
					continue;
				}

				try {
					loadedItems += loadFile(fileIndex, action);
				} catch (IOException e) {
					throw new ModelException(ModelErrorCode.DRIVER_ERROR,
							"Failed to delegate loading of data chunks in file at index "+fileIndex+" to connector", e);
				}
			}


			return loadedItems;
		}
	}

	/**
	 * Loads chunks of data from the underlying file(s). It is the method's responsibility to
	 * query the chunk index and verify for each chunk of data that it is indeed required to be
	 * loaded.
	 *
	 * @param layer
	 * @param indices
	 * @param action
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected long loadChunks(ItemLayer layer, IndexSet[] indices,
			Consumer<ChunkInfo> action) throws IOException,
			InterruptedException {


		StampedLock lock = getLockForFile(fileIndex);
		FileInfo fileInfo = getFileStates().getFileInfo(fileIndex);

		long stamp = lock.readLock();
		try {

		} finally {
			lock.unlockRead(stamp);
		}

		//TODO check whether we

		// TODO Auto-generated method stub
		return getConverter().loadChunks(layer, IndexSet.asIterator(indices), chunkIndexStorage, action);
	}

	/**
	 * Loads the entire content of the file specified by {@code fileIndex} and transforms
	 * it into the appropriate model representation.
	 *
	 * @param fileIndex
	 * @throws IOException
	 * @throws ModelException in case the specified file has already been (partially) loaded
	 */
	public long loadFile(int fileIndex, Consumer<ChunkInfo> action)
			throws IOException, InterruptedException {

		StampedLock lock = getLockForFile(fileIndex);
		FileInfo fileInfo = getFileStates().getFileInfo(fileIndex);

		long stamp = lock.readLock();
		try {

			if(!fileInfo.isValid())
				throw new ModelException(ModelErrorCode.DRIVER_ERROR,
						"Cannot attempt to load from invalid file at index "+fileIndex);

			if(fileInfo.isFlagSet(ElementFlag.PARTIALLY_LOADED))
				throw new ModelException(ModelErrorCode.DRIVER_ERROR,
						"Cannot attempt to completely load already partially loaded file at index "+fileIndex);

			if(fileInfo.isFlagSet(ElementFlag.LOADED))
				throw new ModelException(ModelErrorCode.DRIVER_ERROR,
						"File already completely loaded for index "+fileIndex);

			/*
			 * Converter.loadFile() returns number of loaded elements in context's primary layer,
			 * so we grab those values here and use it for consistency checking.
			 */
			long expectedItemCount = fileInfo.getItemCount(getManifest().getContextManifest().getPrimaryLayerManifest());

			// Now start actual loading process
			int recommendedBufferSize = (int) Math.min(DEFAULT_CHUNK_INFO_SIZE, expectedItemCount);
			ChunkConsumer consumer = createPublisher(action, recommendedBufferSize);

			// Delegate to converter to do the I/O work
			long loadedItemCount = getConverter().loadFile(fileIndex, consumer);

			// Ensure pending information gets published
			consumer.flush();

			// Verify that content meets expectations from previous scans

			if(loadedItemCount!=expectedItemCount) {
				fileInfo.setFlag(ElementFlag.CORRUPTED);
				//TODO implement a way of discarding elements loaded during this process (maybe with a special ItemLayerManager? )
			}

			return loadedItemCount;

		} catch (IOException e) {
			fileInfo.setFlag(ElementFlag.UNUSABLE);
			throw e;
		} catch (InterruptedException e) {
			//TODO in theory interrupting the loading should be a legal operation?
			fileInfo.setFlag(ElementFlag.UNUSABLE);
			throw e;
		} finally {
			lock.unlockRead(stamp);
		}
	}

	/**
	 * Attempts to load all files comprising the resources of this driver.
	 *
	 * @param action
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void loadAllFiles(Consumer<ChunkInfo> action) throws IOException, InterruptedException {
		FileSet dataFiles = getDataFiles();

		int fileCount = dataFiles.getFileCount();

		for(int fileIndex = 0; fileIndex < fileCount; fileIndex++) {

			FileInfo fileInfo = getFileStates().getFileInfo(fileIndex);

			if(!fileInfo.isFlagSet(ElementFlag.SCANNED))
				throw new ModelException(ModelErrorCode.DRIVER_ERROR,
						"Unscanned file at index "+fileIndex);

			if(!fileInfo.isFlagSet(ElementFlag.LOADED)) {
				loadFile(fileIndex, action);
			}
		}

	}

	/**
	 * Creates a {@link ChunkConsumer} that's suitable for buffering information about
	 * individual chunks during a loading process and publishing them to the given
	 * {@link Consumer} in the form of {@link ChunkInfo} objects.
	 * <p>
	 * This implementation either returns an "empty" consumer in case the {@code action}
	 * argument is {@code null} or an instance of {@link BufferedChunkInfoPublisher} with
	 * the supplied {@code bufferSize} as capacity. If the {@code bufferSize} parameter
	 * is {@code 0} or negative it will use a default fallback capacity of {@code 100}.
	 * <p>
	 * Subclasses are free to decide on other capacity values or publisher implementations
	 * if they so wish.
	 *
	 * @param action
	 * @param bufferSize
	 * @return
	 */
	protected ChunkConsumer createPublisher(Consumer<ChunkInfo> action, int bufferSize) {
		ChunkConsumer consumer = noOpChunkConsumer;

		if(action!=null) {
			if(bufferSize<=0) {
				bufferSize = DEFAULT_CHUNK_INFO_SIZE;
			}
			ChunkInfoBuilder buffer = ChunkInfoBuilder.newBuilder(100);
			consumer = new BufferedChunkInfoPublisher(buffer, action);
		}

		return consumer;
	}


	/**
	 * ChunkConsumer implementation that does nothing.
	 */
	static final ChunkConsumer noOpChunkConsumer = new ChunkConsumer() {

		@Override
		public final void accept(long index, Item item, ChunkState state) {
			// no-op
		}
	};

	/**
	 * Bridge between generation of individual chunk information via a {@link ChunkConsumer} and
	 * the batch-like notification mechanism via {@link Consumer} when fed {@link ChunkInfo} objects.
	 * <p>
	 * This implementation will {@link #accept(long, Item, ChunkState) consume} individual chunk information
	 * and buffer it in an instance of {@link ChunkInfoBuilder}. Once the buffer is full it will then
	 * publish the collected {@link ChunkInfo} to the {@link Consumer action} supplied at construction
	 * time.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class BufferedChunkInfoPublisher implements ChunkConsumer {

		private final ChunkInfoBuilder buffer;
		private final Consumer<ChunkInfo> action;


		/**
		 * @param buffer
		 * @param action
		 */
		public BufferedChunkInfoPublisher(ChunkInfoBuilder buffer,
				Consumer<ChunkInfo> action) {
			checkNotNull(buffer);
			checkNotNull(action);

			this.buffer = buffer;
			this.action = action;
		}


		/**
		 * Adds the given chunk information to the buffer and if it is full,
		 * publishes its content to the saved action.
		 *
		 * @see de.ims.icarus2.model.standard.driver.ChunkConsumer#accept(long, de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.driver.ChunkState)
		 */
		@Override
		public void accept(long index, Item item, ChunkState state) {
			if(buffer.add(index, item, state)) {
				publish();
			}
		}

		private void publish() {
			try {
				action.accept(buffer.build());
			} finally {
				buffer.reset();
			}
		}

		@Override
		public void flush() {
			if(!buffer.isEmpty()) {
				publish();
			}
		}
	}


	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public abstract static class FileDriverBuilder<B extends FileDriverBuilder<B>> extends DriverBuilder<B, FileDriver> {
		private FileSet dataFiles;
		private MetadataRegistry metadataRegistry;

		public FileDriverBuilder<B> metadataRegistry(MetadataRegistry metadataRegistry) {
			checkNotNull(metadataRegistry);
			checkState(this.metadataRegistry==null);

			this.metadataRegistry = metadataRegistry;

			return thisAsCast();
		}

		public MetadataRegistry getMetadataRegistry() {
			return metadataRegistry;
		}

		public FileDriverBuilder<B> dataFiles(FileSet dataFiles) {
			checkNotNull(dataFiles);
			checkState(this.dataFiles==null);

			this.dataFiles = dataFiles;

			return thisAsCast();
		}

		public FileSet getDataFiles() {
			return dataFiles;
		}

		@Override
		protected void validate() {
			super.validate();

			checkState("Missing data file set", dataFiles!=null);
			checkState("Missing metadata registry", metadataRegistry!=null);

			//TODO
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#create()
		 */
		@Override
		protected FileDriver create() {
			throw new ModelException(GlobalErrorCode.NOT_IMPLEMENTED,
					"Cannot create abstract file driver - must override method in subclass or specify driver constructor!");
		}
	}

	/**
	 * Defines a set of default keys used to customize options for a file connector
	 * (e.g. the behavior in case of corrupted metadata, etc...)
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static enum OptionKey {


		//TODO
		CHUNK_INDICES_FOLDER("chunkIndicesFolder", ValueType.FILE),

		/**
		 * User defined hint whether to immediately load the underlying resources
		 * when the driver is connecting.
		 */
		LOAD_ON_CONNECT("loadOnConnect", ValueType.BOOLEAN),
		;

		private final String key;
		private final ValueType type;

		private OptionKey(String key, ValueType type) {
			this.key = key;
			this.type = type;
		}

		public String getKey() {
			return key;
		}

		public ValueType getType() {
			return type;
		}

		public Object getValue(ModuleManifest manifest) {
			return checkAndGetValue(manifest.getProperty(key));
		}

		public Object getValue(DriverManifest manifest) {
			return checkAndGetValue(manifest.getProperty(key));
		}

		private Object checkAndGetValue(MemberManifest.Property property) {
			if(property.getValueType()!=type)
				throw new ModelException(ModelErrorCode.DRIVER_ERROR,
						Messages.mismatchMessage(
								"Incompatible property value type in manifest for key: "+key,
								type, property.getValueType()));

			return property.getValue();
		}
	}

	/**
	 * Models a single step in the preparation process of a file driver.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface PreparationStep {

		/**
		 * Perform whatever kind of operation hte step models.
		 *
		 * @param driver
		 * @param env
		 * @return
		 * @throws Exception
		 */
		boolean apply(FileDriver driver, ReportBuilder<ReportItem> reportBuilder, Options env) throws Exception;

		Collection<? extends PreparationStep> getPreconditions();


		String name();

		/**
		 * Optional method to perform cleanup work in case the connection process
		 * failed and some steps need to release resources, etc...
		 */
		default void cleanup() {
			// no-op
		}
	}
}
