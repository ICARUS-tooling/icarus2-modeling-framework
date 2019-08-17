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
package de.ims.icarus2.filedriver;

import static de.ims.icarus2.model.api.driver.indices.IndexUtils.checkNonEmpty;
import static de.ims.icarus2.model.util.ModelUtils.getName;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.lang.Primitives._int;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PrimitiveIterator.OfLong;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.Report;
import de.ims.icarus2.Report.ReportItem;
import de.ims.icarus2.ReportBuilder;
import de.ims.icarus2.filedriver.Converter.ConverterProperty;
import de.ims.icarus2.filedriver.Converter.LoadResult;
import de.ims.icarus2.filedriver.FileDataStates.ElementInfo;
import de.ims.icarus2.filedriver.FileDataStates.FileInfo;
import de.ims.icarus2.filedriver.FileDataStates.LayerInfo;
import de.ims.icarus2.filedriver.FileDriverMetadata.ChunkIndexKey;
import de.ims.icarus2.filedriver.FileDriverMetadata.DriverKey;
import de.ims.icarus2.filedriver.FileDriverMetadata.FileKey;
import de.ims.icarus2.filedriver.FileDriverMetadata.ItemLayerKey;
import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;
import de.ims.icarus2.filedriver.io.sets.ResourceSet;
import de.ims.icarus2.filedriver.mapping.AbstractStoredMapping;
import de.ims.icarus2.filedriver.mapping.DefaultMappingFactory;
import de.ims.icarus2.filedriver.mapping.MappingFactory;
import de.ims.icarus2.filedriver.mapping.chunks.ChunkIndex;
import de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexStorage;
import de.ims.icarus2.filedriver.mapping.chunks.DefaultChunkIndex;
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
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.registry.CorpusMemberFactory;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.driver.AbstractDriver;
import de.ims.icarus2.model.standard.driver.BufferedItemManager;
import de.ims.icarus2.model.standard.driver.BufferedItemManager.LayerBuffer;
import de.ims.icarus2.model.standard.driver.ChunkConsumer;
import de.ims.icarus2.model.standard.driver.ChunkInfoBuilder;
import de.ims.icarus2.model.util.Graph;
import de.ims.icarus2.model.util.Graph.TraversalPolicy;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.annotations.PreliminaryValue;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.io.resource.FileResource;
import de.ims.icarus2.util.io.resource.IOResource;
import de.ims.icarus2.util.io.resource.ResourceProvider;
import de.ims.icarus2.util.lang.Lazy;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;


/**
 * @author Markus Gärtner
 *
 */
public class FileDriver extends AbstractDriver {

	public static Builder newBuilder() {
		return new Builder();
	}

	private static final int DEFAULT_CHUNK_INFO_SIZE = 200;

	/**
	 * Registry for storing metadata info like number of items in each layer,
	 * availability of chunk indices for certain files, etc...
	 */
	private final MetadataRegistry metadataRegistry;

	/**
	 * Storage for our model instances
	 */
	protected volatile BufferedItemManager content;

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
	protected final ResourceSet dataFiles;

	/**
	 * Abstraction for the actual file system
	 */
	protected final ResourceProvider resourceProvider;

	/**
	 * Locks for individual files.
	 * <p>
	 * Will be populated lazily when actually needed.
	 */
	private final Int2ObjectMap<LockableFileObject> fileObjects;

	private static Logger log = LoggerFactory.getLogger(FileDriver.class);

	/**
	 * @param manifest
	 * @param corpus
	 * @throws ModelException
	 */
	protected FileDriver(Builder builder) {
		super(builder);

		metadataRegistry = builder.getMetadataRegistry();
		dataFiles = builder.getDataFiles();
		resourceProvider = builder.getResourceProvider();

		fileObjects = new Int2ObjectOpenHashMap<>(dataFiles.getResourceCount());

		converter = Lazy.create(this::createConverter, true);
	}

	protected Converter createConverter() {

		// Fetch the (hopefully) single module manifest describing our converter to be used
		Set<ModuleManifest> converterManifests = getManifest().getModuleManifests("converter");

		if(converterManifests.isEmpty())
			throw new ModelException(ModelErrorCode.DRIVER_ERROR,
					"No converter modules declared in driver manifest: "+getName(getManifest()));
		if(converterManifests.size()>1)
			throw new ModelException(ModelErrorCode.DRIVER_ERROR,
					"Too many converter modules declared in driver manifest: "+getName(getManifest()));

		ModuleManifest manifest = converterManifests.iterator().next();

		final CorpusMemberFactory factory = getCorpus().getManager().newFactory();

		Converter converter = factory.newImplementationLoader()
				.manifest(manifest.getImplementationManifest().get())
				.environment(this)
				.message("Converter for driver "+getName(getManifest()))
				.instantiate(Converter.class);


		//DEBUG
//		TableSchema tableSchema = (TableSchema) getManifest().getPropertyValue("tableSchema");
//
//		Converter converter = new TableConverter(tableSchema); //TODO
//		converter.init(this);

		converter.addNotify(this);

		return converter;
	}

	public Converter getConverter() {
		return converter.value();
	}

	public final LockableFileObject getFileObject(int fileIndex) {
		IOResource file = dataFiles.getResourceAt(fileIndex);
		if(file==null)
			throw new ModelException(ModelErrorCode.DRIVER_ERROR,
					"Could not find a valid file to fetch lock for at index "+fileIndex);

		LockableFileObject fileObject = fileObjects.get(fileIndex);
		if(fileObject==null) {
			synchronized (fileObjects) {
				fileObject = fileObjects.get(fileIndex);
				if(fileObject==null) {
					fileObject = new LockableFileObject(file, fileIndex, new StampedLock());
					fileObjects.put(fileIndex, fileObject);
				}
			}
		}

		return fileObject;
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

		for(LayerManifest<?> layer : manifest.getLayerManifests(ModelUtils::isItemLayer)) {
			//TODO access states and create optimized layers?
		}

		return options;
	}

	public ResourceSet getDataFiles() {
		return dataFiles;
	}

	public MetadataRegistry getMetadataRegistry() {
		return metadataRegistry;
	}

	/**
	 * Returns the lookup structure for all kinds of metadata
	 *
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
	 * @return the resource provider associated with this driver
	 */
	public ResourceProvider getResourceProvider() {
		return resourceProvider;
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
		BiFunction<ItemLayerManifestBase<?>, ItemLayerManifestBase<?>, Mapping> fallback = getMappingFallback();
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
	 * Default implementation delegates directly to
	 * {@link FileDriverUtils#createMapping(MappingFactory, MappingManifest, MetadataRegistry, Options)}.
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
				if(m instanceof AbstractStoredMapping) {
					AbstractStoredMapping mapping = (AbstractStoredMapping) m;
					try {
						mapping.delete();
					} catch (IOException e) {
						log.error("Failed to delete mapping storage", e);
					} finally {
						//TODO clean up metadata associated with this mapping!
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
		action.accept(getConverter());
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
	 * Generates a lookup key using {@link ItemLayerKey#ITEMS#getKey(ItemLayerManifestBase<?>)}. This key
	 * is then used to query the underlying {@link MetadataRegistry} for the mapped
	 * {@link MetadataRegistry#getLongValue(String, long) long value}, supplying {@value IcarusUtils#UNSET_LONG}
	 * as default value for the case that no matching entry was found.
	 *
	 * TODO refresh description
	 *
	 * @see de.ims.icarus2.model.api.driver.Driver#getItemCount(de.ims.icarus2.model.manifest.api.ItemLayerManifestBase<?>)
	 * @see ItemLayerKey#ITEMS
	 */
	@Override
	public long getItemCount(ItemLayerManifestBase<?> layer) {
		checkReady();

		LayerInfo info = getFileStates().getLayerInfo(layer);

		return info==null ? IcarusUtils.UNSET_LONG : info.getSize();
	}

	public BufferedItemManager.LayerBuffer getLayerBuffer(ItemLayer layer) {
		checkConnected();

		if(content==null) {
			//TODO shift initialization to one of the connection substeps?
			synchronized (this) {
				if(content==null) {
					content = createBufferedItemManager();
				}
			}
		}

		return content.getBuffer(layer);
	}

	protected BufferedItemManager createBufferedItemManager() {
		BufferedItemManager.Builder builder = BufferedItemManager.newBuilder();

		for(LayerManifest<?> layerManifest : getContext().getManifest().getLayerManifests(ManifestUtils::isItemLayerManifest)) {
			ItemLayerManifestBase<?> itemLayerManifest = (ItemLayerManifestBase<?>) layerManifest;
			//TODO add options to activate recycling and pooling of items
			long layerSize = getItemCount(itemLayerManifest);

			// Restrict capacity to 100 millions for now
			if(layerSize>0) {
				@PreliminaryValue
				int defaultMinCapacity = 100_000_000;
				int capacity = (int)Math.min(defaultMinCapacity , layerSize);
				builder.addBuffer(itemLayerManifest, capacity);
			} else {
				builder.addBuffer(itemLayerManifest);
			}
		}

		return builder.build();
	}

	public ChunkIndex getChunkIndex(ItemLayer layer) {
		checkConnected();

		return chunkIndexStorage.getChunkIndex(layer);
	}

	@Override
	public void addItem(ItemLayer layer, Item item, long index) {
		checkConnected();
		checkEditable();

		getLayerBuffer(layer).add(item, index);

		//TODO refresh index mapping
	}

	@Override
	public void removeItem(ItemLayer layer, Item item, long index) {
		checkConnected();
		checkEditable();

		Item removedItem = getLayerBuffer(layer).remove(index);

		//TODO refresh index mapping
	}

	@Override
	public void moveItem(ItemLayer layer, Item item, long fromIndex, long toIndex) {
		checkConnected();
		checkEditable();

		//TODO refresh content of LayerBuffer for given layer and then schedule update for index mappers?
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemLayerManager#getItem(de.ims.icarus2.model.api.layer.ItemLayer, long)
	 */
	@Override
	public Item getItem(ItemLayer layer, long index) {
		checkConnected();

		return getLayerBuffer(layer).fetch(index);
	}

	private List<PreparationStep> computeStepList() {

		// Fetch customized steps to be performed next
		Graph<PreparationStep> stepGraph = Graph.genericGraphFromCollection(PreparationStep.class,
				getPreparationSteps(), PreparationStep::getPreconditions, Graph.acceptAll());

		LazyCollection<PreparationStep> result = LazyCollection.lazyList();

		// Post-order traversal makes sure each step is added AFTER its preconditions
		stepGraph.walkTree(TraversalPolicy.POST_ORDER, stepGraph.getRoots(), result);

		return result.getAsList();
	}

	/**
	 * This implementation initializes the internal {@link FileDataStates} storage
	 * before calling the super method.
	 * After that, a customizable series of {@link PreparationStep preparation steps} is performed
	 * to allow for a more flexible initialization.
	 * @throws IcarusApiException
	 *
	 * @see de.ims.icarus2.model.standard.driver.AbstractDriver#doConnect()
	 * @see #getPreparationSteps()
	 */
	@Override
	protected void doConnect() throws InterruptedException, IcarusApiException {

		MetadataRegistry metadataRegistry = getMetadataRegistry();

		metadataRegistry.beginUpdate();
		try {
			// Slight violation of the super contract since we set this up before delegating to super method!
			states = new FileDataStates(this);

			// Creates context and mappings
			super.doConnect();

			final List<PreparationStep> steps = computeStepList();
			final int stepCount = steps.size();

			ReportBuilder<ReportItem> reportBuilder = ReportBuilder.newBuilder(getManifest());
			reportBuilder.addInfo("Connecting to corpus");

			boolean driverIsValid = true;

			// Environmental lookup
			Options env = new Options();
			//TODO maybe fill this with internal information not directly accessible via public instance methods

			for(int i=0; i<stepCount; i++) {

				checkInterrupted();

				PreparationStep step = steps.get(i);

				log.info("Performing preparation step {} of {}: {}",
						_int(i+1), _int(stepCount), step.name());

				boolean valid = true;

				try {
					valid = step.apply(this, reportBuilder, env);
				} catch(Exception e) {
					// Redundant error handling: Collect error for report AND send directly to logger
					reportBuilder.addError(ModelErrorCode.DRIVER_ERROR,
							"Preparation step {} of {} failed: {} - {}",
							_int(i+1), _int(stepCount), step.name(), e);

					log.error("Preparation step {} of {} failed: {}",
							_int(i+1), _int(stepCount), step.name(), e);
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

			reportBuilder.addInfo("Finished connecting to corpus");

			Report<ReportItem> report = reportBuilder.build();

			// If we encountered real errors throw an exception, otherwise just delegate to logging
			if(report.hasErrors()) {
				throw new ModelException(getCorpus(), ModelErrorCode.DRIVER_CONNECTION, report.toString("Connecting driver failed"));
			} else if(!report.isEmpty()) {
				log.info(report.toString());
			}

		} finally {
			metadataRegistry.endUpdate();
		}
	}

	/**
	 * @throws IcarusApiException
	 * @throws InterruptedException
	 * @see de.ims.icarus2.model.standard.driver.AbstractDriver#afterConnect()
	 */
	@Override
	protected void afterConnect() throws IcarusApiException, InterruptedException {

		// Check if we should directly load all the files
		if(OptionKey.LOAD_ON_CONNECT.<Boolean>getValue(getManifest()).orElse(Boolean.FALSE).booleanValue()) {
			try {
				loadAllFiles(null);
			} catch(IOException e) {
				throw new IcarusApiException(GlobalErrorCode.IO_ERROR, "Failed to load complete file resources");
			}
		}
	}

	protected void checkInterrupted() throws InterruptedException {
		if(Thread.interrupted())
			throw new InterruptedException();
	}

	protected Set<PreparationStep> getPreparationSteps() {
		Set<PreparationStep> result = new ReferenceOpenHashSet<>();
		CollectionUtils.feedItems(result, StandardPreparationSteps.values());
		return result;
	}

	/**
	 * Calls the super method and then closes the internal {@link MetadataRegistry}
	 * and discards the current set of {@link FileDataStates states}.
	 * @throws IcarusApiException
	 *
	 * @see de.ims.icarus2.model.standard.driver.AbstractDriver#doDisconnect()
	 */
	@Override
	protected void doDisconnect() throws InterruptedException, IcarusApiException {

		@SuppressWarnings("resource")
		MetadataRegistry metadataRegistry = getMetadataRegistry();

		metadataRegistry.beginUpdate();
		try {
			// Mandatory delegation to super method before we attempt any cleanup work ourselves
			super.doDisconnect();

			//TODO persist current file states in metadata registry

			// Only attempt to close converter if we actually used it
			if(converter.created()) {
				Converter converter = getConverter();
				try {
					converter.removeNotify(this);
				} catch (Exception e) {
					log.error("Attempt to close converter failed", e);
				}
			}

			// Shut down our storage
			if(content!=null) {
				try {
					content.close();
				} catch(Exception e) {
					log.error("Error during shutdown of layer buffers", e);
				}
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
	private int findFileForIndex(long index, ItemLayerManifestBase<?> layer, int min, int max) {
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
	 * @throws IcarusApiException
	 * @throws Exception
	 */
	public boolean scanFile(int fileIndex) throws IOException, InterruptedException, IcarusApiException {

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


		LockableFileObject fileObject = getFileObject(fileIndex);
		StampedLock lock = fileObject.getLock();

		Report<ReportItem> report = null;

		long stamp = lock.readLock();
		try {
			report = getConverter().scanFile(fileIndex);
		} finally {
			lock.unlockRead(stamp);
		}

		//TODO process report
		if(report.hasErrors()) {
			log.error("Scan of file {} finished with errors:\n{}", _int(fileIndex+1), report);
		} else if(report.hasWarnings()) {
			log.warn("Scan of file {} finished with warnings:\n{}", _int(fileIndex+1), report);
		}
		//DEBUG

		return !report.hasErrors();
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
		if(fileSizeThreshold!=IcarusUtils.UNSET_LONG) {
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
			return IcarusUtils.UNSET_LONG;
		}

		long threshold = IcarusUtils.UNSET_LONG;

		try {
			threshold = Long.parseLong(value);
		} catch(NumberFormatException e) {
			log.warn("Error while reading property '{1}' from global settings - value '{2}' was expected to be a long integer", key, value, e);
		}

		// Collapse all "invalid" values into the same "ignore" value
		if(threshold<=0) {
			threshold = IcarusUtils.UNSET_LONG;
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

		ContextManifest contextManifest = ManifestUtils.requireHost(getManifest());
		ChunkIndexStorage.Builder builder = new ChunkIndexStorage.Builder();

		// Traverse layer groups and create a chunk index for each group's primary layer
		contextManifest.forEachGroupManifest(manifest -> {

			ChunkIndex chunkIndex = createChunkIndex(manifest);


			// Only skips a layer/group if explicitly set so
			if(chunkIndex!=null) {
				builder.add(manifest.getPrimaryLayerManifest()
						.orElseThrow(ManifestException.missing(getManifest(), "resolvable primary layer")), chunkIndex);
			}
		});

		return builder.build();
	}

	protected ChunkIndex createChunkIndex(LayerGroupManifest groupManifest) {
		ItemLayerManifestBase<?> layerManifest = groupManifest.getPrimaryLayerManifest()
				.orElseThrow(ManifestException.missing(getManifest(), "resolvable primary layer"));
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
		//  Physical file of chunk index
		//*******************************************
		Path path = null;
		String pathKey = ChunkIndexKey.PATH.getKey(layerManifest);
		String savedPath = metadataRegistry.getValue(pathKey);

		if(savedPath==null) {

			// Check driver settings first
			Optional<Path> folder = OptionKey.CHUNK_INDICES_FOLDER.getValue(getManifest());

			if(!folder.isPresent()) {
				// Try to find a suitable location for the chunk index
				FileManager fileManager = getCorpus().getManager().getFileManager();

				// If at this point we have no valid file manager there is no way of finding a suitable file location
				if(fileManager==null) {
					return null;
				}

				folder = Optional.of(fileManager.getCorpusFolder(getCorpus().getManifest()));
			}

			// Use a file named after the layer itself inside whatever folder we should use
			String filename = layerManifest.getId()+FileDriverUtils.CHUNK_INDEX_FILE_ENDING;
			path = folder.get().resolve(filename);
			savedPath = path.toString();

			// Make sure to persist the file file to metadata for future lookups
			metadataRegistry.setValue(pathKey, savedPath);
		} else {
			path = Paths.get(savedPath);
		}

		checkState("Failed to obtain valid file file for chunk index: "+layerManifest, path!=null);

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
					 *  For editable corpora only:
					 *
					 *  As a safety measure we assume that our guess only accounts for 25%
					 *  of the potential total size the actual scanning method will encounter.
					 *
					 *  This means that for estimated counts that are close to value type boundaries
					 *  we will most likely shift into the next higher value space and potentially
					 *  "waste" some space.
					 */
					if(estimatedChunkCount<Integer.MAX_VALUE && getCorpus().getManifest().isEditable()) {
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
		int cacheSize = metadataRegistry.getIntValue(cacheSizeKey, IcarusUtils.UNSET_INT);

		if(cacheSize==IcarusUtils.UNSET_INT) {
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
		int blockPower = metadataRegistry.getIntValue(blockPowerKey, IcarusUtils.UNSET_INT);

		if(blockPower==IcarusUtils.UNSET_INT) {
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
		return DefaultChunkIndex.newBuilder()
				.resourceSet(getDataFiles())
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
		String key = groupManifest.getId().orElseThrow(ManifestException.missing(groupManifest, "id"));
		Integer value = (Integer) lookup.get(key);
		int converterEstimation = value!=null ? value.intValue() : IcarusUtils.UNSET_INT;
		if(converterEstimation!=IcarusUtils.UNSET_INT) {
			return converterEstimation;
		}

		// Check driver's settings next
		//TODO


		// Check metadata
		//TODO

		return IcarusUtils.UNSET_INT;
	}

	/**
	 * Translates the indices to indices in the surrounding layer group in case the specified
	 * layer is not the respective primary layer and then delegates to an internal
	 * {@link #loadPrimaryLayer(IndexSet[], ItemLayer, Consumer) load} method.
	 * @throws IcarusApiException
	 *
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemLayerManager#load(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.layer.ItemLayer, java.util.function.Consumer)
	 */
	@Override
	public long load(IndexSet[] indices, ItemLayer layer,
			Consumer<ChunkInfo> action) throws InterruptedException, IcarusApiException {
		requireNonNull(indices);
		requireNonNull(layer);

		checkNonEmpty(indices);

		checkConnected();
		checkReady();

		/*
		 * HOWTO:
		 *
		 * - Map indices to primary layer if required
		 * - Delegate to SingleThreadedItemLoader which in turn delegates to LayerBuffer.load()
		 * - Any existing chunks in the primary layer will directly be published to the action callback
		 * - Indices not yet mapped to a live item then get sent to loadPrimaryLayer()
		 */

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
		SingleThreadedItemLoader singleThreadedItemLoader = new SingleThreadedItemLoader(indices, getLayerBuffer(primaryLayer), null);

		// Potentially long running part
		singleThreadedItemLoader.execute();

		long loadedItems = 0L;

		// Only in case of missing items do we need to start mapping and/or accessing underlying resources
		if(singleThreadedItemLoader.getMissingIndicesCount()>0L) {
			IndexSet[] missingIndices = singleThreadedItemLoader.getMissingIndices();

			// Delegate to chunk aware method to do the actual I/O stuff
			try {
				loadedItems = loadPrimaryLayer(missingIndices, primaryLayer, action);
			} catch (IOException e) {
				throw new ModelException(getCorpus(), GlobalErrorCode.IO_ERROR,
						"Failed loading chunks for layer: "+ModelUtils.getUniqueId(primaryLayer));
			}
		}

		return loadedItems;
	}

	/**
	 * Not meant to be used by multiple threads!
	 *
	 * @author Markus Gärtner
	 *
	 */
	protected class SingleThreadedItemLoader {

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
		private IndexSetBuilder missingIndicesBuffer;

		private boolean allIndicesMissing = false;

		private long missingIndicesCount = 0L;
		private long availableIndicesCount = 0L;

		public SingleThreadedItemLoader(IndexSet[] indices, BufferedItemManager.LayerBuffer buffer, Consumer<ChunkInfo> action) {
			this.indices = indices;
			this.buffer = buffer;

			requestedItemCount = IndexUtils.count(indices);
			inputSorted = IndexUtils.isSorted(indices);

			int recommendedBufferSize = (int) Math.min(DEFAULT_CHUNK_INFO_SIZE, requestedItemCount);
			publisher = createPublisher(action, recommendedBufferSize);
		}

		public void execute() {
			if(buffer.isEmpty()) {
				allIndicesMissing = true;
				missingIndicesCount = requestedItemCount;
			} else {

				OfLong it = IndexUtils.asIterator(indices);

				buffer.load(it, this::onAvailableItem, this::onMissingItem);

				// Make sure to tell the publisher we're done, so it can wrap up pending chunk information
				publisher.flush();
			}
		}

		/**
		 * Called only for missing items by the {@link BufferedItemManager.LayerBuffer}
		 */
		public void onMissingItem(long index) {
			missingIndicesCount++;

			if(missingIndicesBuffer==null) {
				IndexCollectorFactory factory = new IndexCollectorFactory();
				factory.totalSizeLimit(requestedItemCount);
				factory.inputSorted(inputSorted);

				missingIndicesBuffer = factory.create();
			}

			missingIndicesBuffer.add(index);
		}

		public void onAvailableItem(Item item, long index) {
			ChunkState chunkState = ChunkState.CORRUPTED;
			if(item.isUsable()) {
				availableIndicesCount++;
			}
			publisher.accept(index, item, chunkState);
		}

		public IndexSet[] getMissingIndices() {

			// Shortcut in case of empty buffer: just forward the raw input indices
			if(allIndicesMissing) {
				return indices;
			}

			return missingIndicesBuffer==null ? null : missingIndicesBuffer.build();
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
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemLayerManager#release(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.layer.ItemLayer)
	 */
	@Override
	public void release(IndexSet[] indices, ItemLayer layer)
			throws InterruptedException {
		requireNonNull(indices);
		requireNonNull(layer);

		checkNonEmpty(indices);

		checkConnected();
		checkReady();

		/*
		 * HOWTO:
		 *
		 * - Map indices to primary layer if required
		 * - Provide indices to LayerBuffer.release() and let it take care of also releasing content
		 */

		final LayerGroup group = layer.getLayerGroup();
		final ItemLayer primaryLayer = group.getPrimaryLayer();
		final boolean mappingRequired = layer!=primaryLayer;

		if(mappingRequired) {
			indices = mapIndices(layer.getManifest(), primaryLayer.getManifest(), indices);
		}

		getLayerBuffer(primaryLayer).release(IndexUtils.asIterator(indices));
	}

	protected long loadPrimaryLayer(IndexSet[] indices, ItemLayer layer,
			Consumer<ChunkInfo> action) throws IOException, InterruptedException, IcarusApiException {
		checkConnected();
		checkReady();

		// If the driver supports chunking and has created a chunk index, then use the easy way
		if(hasChunkIndex()) {
			return loadChunks(layer, indices, action);
		}

		// No chunk index available, proceed with loading the respective files
		int fileCount = dataFiles.getResourceCount();

		if(fileCount==1) {
			// Trivial situation: single file corpora mean we just have to read it in one go

			return loadFile(0, action);
		}

		// Now actually access metadata to find out which files to load

		long minIndex = IndexUtils.firstIndex(indices);
		long maxIndex = IndexUtils.lastIndex(indices);

		// Find first and last file to contain the indices

		int firstFile = findFileForIndex(minIndex, layer.getManifest(), 0, fileCount);
		if(firstFile<0)
			throw new ModelException(ModelErrorCode.DRIVER_METADATA_CORRUPTED,
					"Could not find file index for item index "+minIndex+" in layer "+ModelUtils.getUniqueId(layer));
		int lastFile = findFileForIndex(maxIndex, layer.getManifest(), firstFile, fileCount);
		if(lastFile<0)
			throw new ModelException(ModelErrorCode.DRIVER_METADATA_CORRUPTED,
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

			loadedItems += loadFile(fileIndex, action);
		}

		return loadedItems;
	}

	/**
	 * Loads chunks of data from the underlying file(s). It is the method's responsibility to
	 * query the chunk index and verify for each chunk of data that it is indeed required to be
	 * loaded.
	 *
	 * @param layer the primary layer to load chunks for
	 * @param indices
	 * @param action
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected long loadChunks(ItemLayer layer, IndexSet[] indices,
			Consumer<ChunkInfo> action) throws IOException,
			InterruptedException {
		requireNonNull(indices);
		requireNonNull(layer);

		checkConnected();
		checkReady();

		if(!hasChunkIndex())
			throw new ModelException(getCorpus(), GlobalErrorCode.ILLEGAL_STATE,
					"Cannot attempt to load chunks from files without a chunk index storage");

		/*
		 * HOWTO:
		 *
		 * - Ensure sorted order of indices
		 * - Iterate indices and access chunk index
		 */


		// Now start actual loading process
		ChunkIndex chunkIndex = getChunkIndex(layer);

		int recommendedBufferSize = (int) Math.min(DEFAULT_CHUNK_INFO_SIZE, IndexUtils.count(indices));
		ChunkConsumer consumer = createPublisher(action, recommendedBufferSize);

		LayerBuffer layerBuffer = getLayerBuffer(layer);

		//FIXME
		return IcarusUtils.UNSET_LONG;
	}

	/**
	 * Loads the entire content of the file specified by {@code fileIndex} and transforms
	 * it into the appropriate model representation.
	 *
	 * @param fileIndex
	 * @throws IOException
	 * @throws IcarusApiException
	 * @throws ModelException in case the specified file has already been (partially) loaded
	 */
	public long loadFile(int fileIndex, Consumer<ChunkInfo> action)
			throws IOException, InterruptedException, IcarusApiException {

		LockableFileObject fileObject = getFileObject(fileIndex);
		StampedLock lock = fileObject.getLock();
		FileInfo fileInfo = getFileStates().getFileInfo(fileIndex);

		long stamp = lock.readLock();
		try {

			if(!fileInfo.isValid())
				throw new ModelException(ModelErrorCode.DRIVER_ERROR,
						"Cannot attempt to load from invalid file at index "+fileIndex+" - state="+fileInfo.states2String());

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
			long expectedItemCount = fileInfo.getItemCount(
					(ItemLayerManifestBase<?>) getManifest().getContextManifest()
						.flatMap(ContextManifest::getPrimaryLayerManifest)
						.flatMap(TargetLayerManifest::getResolvedLayerManifest)
						.orElseThrow(ManifestException.missing(getManifest(), "resolvable primary layer")));

			// Now start actual loading process
			int recommendedBufferSize = (int) Math.min(DEFAULT_CHUNK_INFO_SIZE, expectedItemCount);
			ChunkConsumer consumer = createPublisher(action, recommendedBufferSize);

			// Delegate to converter to do the I/O work
			LoadResult loadResult = getConverter().loadFile(fileIndex, consumer);

			// Ensure pending information gets published
			consumer.flush();

			// Verify that content meets expectations from previous scans and also contains no corrupted chunks

			boolean contentIsValid = loadResult.chunkCount(ChunkState.CORRUPTED)==0L;

			if(expectedItemCount!=IcarusUtils.UNSET_LONG && loadResult.loadedChunkCount()!=expectedItemCount) {
				contentIsValid = false;
			}

			long loadedChunkCount = loadResult.loadedChunkCount();

			// Only if entire content is valid do we allow it to be published
			if(contentIsValid) {
				loadResult.publish();
			} else {
				fileInfo.setFlag(ElementFlag.CORRUPTED);
				loadResult.discard();
				//TODO delete annotations (use AnnotationStorage.removeAllValues(Supplier source)
				loadedChunkCount = 0L;
			}

			return loadedChunkCount;

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
	 * @throws IcarusApiException
	 */
	public void loadAllFiles(Consumer<ChunkInfo> action) throws IOException, InterruptedException, IcarusApiException {
		ResourceSet dataFiles = getDataFiles();

		int fileCount = dataFiles.getResourceCount();

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
	 * Returns the {@link Charset} specified by the {@link OptionKey#ENCODING} option.
	 * If no such option has been set in this driver's {@link #getManifest() manifest}
	 * the default {@link StandardCharsets#UTF_8 UTF-8} charset will be returned.
	 *
	 * @return
	 */
	public Charset getEncoding() {

		return OptionKey.ENCODING.<String>getValue(getManifest())
				.map(Charset::forName)
				.orElse(StandardCharsets.UTF_8);
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
			requireNonNull(buffer);
			requireNonNull(action);

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
	public static class Builder extends DriverBuilder<Builder, FileDriver> {
		private ResourceSet dataFiles;
		private MetadataRegistry metadataRegistry;
		private ResourceProvider resourceProvider;

		protected Builder() {
			// no-op
		}

		public Builder resourceProvider(ResourceProvider resourceProvider) {
			requireNonNull(resourceProvider);
			checkState(this.resourceProvider==null);

			this.resourceProvider = resourceProvider;

			return thisAsCast();
		}

		public ResourceProvider getResourceProvider() {
			return resourceProvider;
		}

		public Builder metadataRegistry(MetadataRegistry metadataRegistry) {
			requireNonNull(metadataRegistry);
			checkState(this.metadataRegistry==null);

			this.metadataRegistry = metadataRegistry;

			return thisAsCast();
		}

		public MetadataRegistry getMetadataRegistry() {
			return metadataRegistry;
		}

		public Builder dataFiles(ResourceSet dataFiles) {
			requireNonNull(dataFiles);
			checkState(this.dataFiles==null);

			this.dataFiles = dataFiles;

			return thisAsCast();
		}

		public ResourceSet getDataFiles() {
			return dataFiles;
		}

		@Override
		protected void validate() {
			super.validate();

			checkState("Missing data file set", dataFiles!=null);
			checkState("Missing metadata registry", metadataRegistry!=null);
			checkState("Missing resource provider", resourceProvider!=null);

			//TODO
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#create()
		 */
		@Override
		protected FileDriver create() {
			return new FileDriver(this);
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
		 * Name of a {@link Charset} specifying the character encoding of the files
		 * containing the data for this driver.
		 * <p>
		 * Note that if no encoding is specified the driver will use the default
		 * {@link StandardCharsets#UTF_8 UTF-8} encoding.
		 */
		ENCODING("encoding", ValueType.STRING),

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

		public <V extends Object> Optional<V> getValue(ModuleManifest manifest) {
			return manifest.getProperty(key).flatMap(this::checkAndGetValue);
		}

		public <V extends Object> Optional<V> getValue(DriverManifest manifest) {
			return manifest.getProperty(key).flatMap(this::checkAndGetValue);
		}

		private <V extends Object> Optional<V> checkAndGetValue(MemberManifest.Property property) {
			if(property.getValueType()!=type)
				throw new ModelException(ModelErrorCode.DRIVER_ERROR,
						Messages.mismatch(
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
		 * Perform whatever kind of operation the step models.
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

	public static class LockableFileObject {
		private final IOResource file;
		private final int fileIndex;
		private final StampedLock lock;

		/**
		 * @param file
		 * @param fileIndex
		 * @param lock
		 */
		public LockableFileObject(IOResource file, int fileIndex, StampedLock lock) {
			requireNonNull(file);
			requireNonNull(lock);
			checkArgument(fileIndex>=0);

			this.file = file;
			this.fileIndex = fileIndex;
			this.lock = lock;
		}

		public IOResource getResource() {
			return file;
		}

		public int getFileIndex() {
			return fileIndex;
		}

		/**
		 * Returns the raw lock used for this file resource.
		 * We provide full access to the lock in order to allow subclasses the
		 * decision to exploit all facets of the {@link StampedLock} implementation,
		 * e.g. the option to use {@link StampedLock#tryOptimisticRead() optimistic reads}
		 * to greatly reduce locking overhead.
		 *
		 * @return
		 */
		public StampedLock getLock() {
			return lock;
		}

	}
}
