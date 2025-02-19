/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives._long;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
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

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.Report;
import de.ims.icarus2.Report.ReportItem;
import de.ims.icarus2.ReportBuilder;
import de.ims.icarus2.apiguard.Api;
import de.ims.icarus2.apiguard.Api.ApiType;
import de.ims.icarus2.apiguard.Guarded;
import de.ims.icarus2.apiguard.Guarded.MethodType;
import de.ims.icarus2.apiguard.Mandatory;
import de.ims.icarus2.filedriver.Converter.ConverterProperty;
import de.ims.icarus2.filedriver.Converter.LoadResult;
import de.ims.icarus2.filedriver.FileDataStates.ChunkIndexInfo;
import de.ims.icarus2.filedriver.FileDataStates.FileInfo;
import de.ims.icarus2.filedriver.FileDataStates.LayerInfo;
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
import de.ims.icarus2.model.api.io.FileManager;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec;
import de.ims.icarus2.model.manifest.api.ImplementationLoader;
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
import de.ims.icarus2.model.standard.driver.BufferedItemManager.TrackingMode;
import de.ims.icarus2.model.standard.driver.ChunkConsumer;
import de.ims.icarus2.model.standard.driver.ChunkInfoBuilder;
import de.ims.icarus2.model.standard.driver.mods.LoggingModuleMonitor;
import de.ims.icarus2.model.standard.driver.mods.ModuleManager;
import de.ims.icarus2.model.standard.driver.mods.SimpleModuleMonitor;
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
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;


/**
 * @author Markus Gärtner
 *
 */
public class FileDriver extends AbstractDriver {

	public static Builder builder() {
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

	private final ModuleSpec converterSpec;

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

		converterSpec = getManifest().getModuleSpec(FileDriverUtils.PROPERTY_CONVERTER)
				.orElseThrow(ManifestException.missing(getManifest(), "converter"));
	}

	/**
	 * @see de.ims.icarus2.model.standard.driver.AbstractDriver#prepareModuleLoader(de.ims.icarus2.model.manifest.api.ImplementationLoader)
	 */
	@Override
	protected void prepareModuleLoader(ImplementationLoader<?> loader) {
		loader.environment(FileDriver.class, this);
	}

	public Converter getConverter() {
		ModuleManager modules = getModuleManager();
		return (Converter) modules.getModule(converterSpec).orElseThrow(
				ModelException.create(ModelErrorCode.DRIVER_ERROR, "Converter not prepared yet"));
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

		for(LayerManifest<?> layer : manifest.getLayerManifests(ModelUtils::isAnyItemLayer)) {
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
		LayerInfo info = null;

		if(isReady()) {
			info = getFileStates().getLayerInfo(layer);
		}

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
		BufferedItemManager.Builder builder = BufferedItemManager.builder();

		// Honor switch to disable tracking altogether
		TrackingMode trackingMode = TrackingMode.PRIMARY_ONLY;
		if(OptionKey.DISABLE_TRACKING.<Boolean>getValue(getManifest()).orElse(Boolean.FALSE).booleanValue()) {
			trackingMode = TrackingMode.NEVER;
		}

		for(LayerManifest<?> layerManifest : getContext().getManifest().getLayerManifests(ManifestUtils::isAnyItemLayerManifest)) {
			ItemLayerManifestBase<?> itemLayerManifest = (ItemLayerManifestBase<?>) layerManifest;
			//TODO add options to activate recycling and pooling of items
			long layerSize = getItemCount(itemLayerManifest);

			// Restrict capacity to 100 millions for now
			if(layerSize>0) {
				@PreliminaryValue
				int defaultMinCapacity = 100_000_000;
				int capacity = (int)Math.min(defaultMinCapacity , layerSize);
				builder.addBuffer(itemLayerManifest, trackingMode, capacity);
			} else {
				builder.addBuffer(itemLayerManifest, trackingMode);
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

			// Creates context, mappings and modules
			super.doConnect();

			// We sync as early as possible to allow fail-fast in case of metadata issues
			states.syncFrom(getMetadataRegistry());

			// Now make sure all modules are loaded and prepared properly
			SimpleModuleMonitor monitor = new SimpleModuleMonitor(
					new LoggingModuleMonitor(log, true, m -> getName(getModuleManager().getManifest(m))));
			getModuleManager().loadAllModules(monitor);
			if(monitor.hasFailedModules())
				throw new ModelException(ModelErrorCode.DRIVER_MODULE_LOADING, "Failed to load modules: "+monitor.getFailedModules().toString());

			monitor.reset();
			getModuleManager().prepareAllModules(monitor);
			if(monitor.hasFailedModules())
				throw new ModelException(ModelErrorCode.DRIVER_MODULE_PREPARATION, "Module preparation failed: "+monitor.getFailedModules().toString());

			final List<PreparationStep> steps = computeStepList();
			final int stepCount = steps.size();

			ReportBuilder<ReportItem> reportBuilder = ReportBuilder.builder(getManifest());
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
				} catch(IcarusApiException e) {
					// Redundant error handling: Collect error for report AND send directly to logger
					reportBuilder.addError(ModelErrorCode.DRIVER_ERROR,
							"Preparation step {} of {} failed: {} - {}",
							_int(i+1), _int(stepCount), step.name(), e);

					log.error("Preparation step {} of {} failed: {}",
							_int(i+1), _int(stepCount), step.name(), e);
					valid = false;
				}

				//TODO catch+rethrow InterruptedException from step and do some cleanup

				if(!valid) {
					driverIsValid = false;
					break;
				}
			}

			// Set global flag to mark driver unusable (report will contain errors, so we throw an exception further down)
			if(!driverIsValid) {
				states.getGlobalInfo().setFlag(ElementFlag.UNUSABLE);
			}

			// All preparation steps done, now we can sync back the (new) metadata
			states.syncTo(getMetadataRegistry());

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
				throw new IcarusApiException(GlobalErrorCode.IO_ERROR, "Failed to load complete file resources", e);
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

			// Now make sure all modules are prepared properly
			SimpleModuleMonitor monitor = new SimpleModuleMonitor(
					new LoggingModuleMonitor(log, true, m -> getName(getModuleManager().getManifest(m))));
			getModuleManager().resetAllModules(monitor);

			// Modules might have stored new metadata, so sync it
			getFileStates().syncTo(metadataRegistry);

			// Shut down our storage (this should only be in-memory cleanup)
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

            if (info.getLastIndex(layer) < index)
                low = mid + 1;
            else if (info.getFirstIndex(layer) > index)
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
		final boolean useChunkIndex;
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
			useChunkIndex = chunkIndexStorage!=null;
		} finally {
			globalLock.unlock();
		}

		getFileStates().forEachLayerInfo(info -> info.setUseChunkIndex(useChunkIndex));

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
			long totalSize = getFileStates().getGlobalInfo().getSize();

			if(totalSize>=fileSizeThreshold) {
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
		String key = FileDriverUtils.PROPERTY_CHUNKING_FILE_SIZE_THRESHOLD;
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
		ChunkIndexStorage.Builder builder = ChunkIndexStorage.builder();

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

	protected @Nullable ChunkIndex createChunkIndex(LayerGroupManifest groupManifest) {
		ItemLayerManifestBase<?> layerManifest = groupManifest.getPrimaryLayerManifest()
				.orElseThrow(ManifestException.missing(getManifest(), "resolvable primary layer"));
		LayerInfo layerInfo = getFileStates().getLayerInfo(layerManifest);

		// First check if we should skip the specified group

		// Honor metadata information about skipping chunking for this layer!
		if(!layerInfo.isUseChunkIndex()) {
			return null;
		}

		ChunkIndexInfo chunkIndexInfo = getFileStates().getChunkIndexInfo(layerManifest);

		// Fetch combined size of all files
		long totalFileSize =  getFileStates().getGlobalInfo().getSize();

		//*******************************************
		//  Physical file of chunk index
		//*******************************************
		Path path = chunkIndexInfo.getPath();

		if(path==null) {

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
			String filename = ManifestUtils.requireId(layerManifest)+FileDriverUtils.CHUNK_INDEX_FILE_ENDING;
			path = folder.get().resolve(filename);

			// Make sure to persist the file path to metadata for future lookups
			chunkIndexInfo.setPath(path);
		}

		checkState("Failed to obtain valid file for chunk index: "+layerManifest, path!=null);

		//*******************************************
		//  ValueType for chunk entries
		//*******************************************
		IndexValueType valueType = chunkIndexInfo.getValueType();
		if(valueType==null) {

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
					if(estimatedChunkCount>0 && estimatedChunkCount<Integer.MAX_VALUE && getCorpus().getManifest().isEditable()) {
						estimatedChunkCount <<= 2;
					}

					valueType = IndexValueType.forValue(estimatedChunkCount);
				}
			}

			// If we couldn't make a valid estimation, go use LONG as fallback
			if(valueType==null) {
				valueType = IndexValueType.LONG;
			}

			chunkIndexInfo.setValueType(valueType);
		}

		checkState("Failed to obtain value type for chunk index: "+layerManifest, valueType!=null);

		//*******************************************
		//  Size of block cache
		//*******************************************
		int cacheSize = chunkIndexInfo.getCacheSize();

		if(cacheSize==UNSET_INT) {
			// Fetch default value from global config
			String key = FileDriverUtils.PROPERTY_CHUNKING_CACHE_SIZE;
			String value = getCorpus().getManager().getProperty(key);

			if(value!=null && !value.isEmpty()) {
				cacheSize = Integer.parseInt(value);
			} else {
				// Very conservative cache size as fallback
				cacheSize = BlockCache.MIN_CAPACITY;
			}

			chunkIndexInfo.setCacheSize(cacheSize);
		}

		checkState("Failed to obtain cache size for chunk index: "+layerManifest, cacheSize>=0);

		//*******************************************
		//  Size of blocks for buffering in 2^blockPower frames
		//*******************************************
		int blockPower = chunkIndexInfo.getBlockPower();

		if(blockPower==UNSET_INT) {
			// Fetch default block power from global config
			String key = FileDriverUtils.PROPERTY_CHUNKING_BLOCK_POWER;
			String value = getCorpus().getManager().getProperty(key);

			if(value!=null && !value.isEmpty()) {
				blockPower = Integer.parseInt(value);
			} else {
				// 4096 frames per block
				//TODO maybe have some smarter way of deciding upon this value?
				blockPower = 12;
			}

			chunkIndexInfo.setBlockPower(blockPower);
		}

		checkState("Failed to obtain block power for chunk index: "+layerManifest, blockPower>0);

		//*******************************************
		//  BlockCache used for buffering
		//*******************************************
		BlockCache blockCache = null;
		String blockCacheHint = chunkIndexInfo.getBlockCache();

		if(blockCacheHint==null) {
			// Per default we'll always use a cache with least-recently-used purging policy
			blockCacheHint = FileDriverUtils.HINT_LRU_CACHE;

			chunkIndexInfo.setBlockCache(blockCacheHint);
		}
		blockCache = FileDriverUtils.toBlockCache(blockCacheHint);

		checkState("Failed to obtain block cache for chunk index: "+layerManifest, blockCache!=null);

		// Finally throw all the settings into a builder and let it assemble the actual chunk index instance
		return DefaultChunkIndex.builder()
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
		int converterEstimation = value!=null ? value.intValue() : UNSET_INT;
		if(converterEstimation!=UNSET_INT) {
			return converterEstimation;
		}

		// Check driver's settings next
		//TODO


		// Check metadata
		//TODO

		return UNSET_INT;
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
			@Nullable Consumer<ChunkInfo> action) throws InterruptedException, IcarusApiException {
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

		/*
		 *  Delegate loading.
		 *  We do not forward the consumer action for publishing since that is only
		 *  required when actually loading chunks from a backend storage!
		 */
		SingleThreadedItemLoader singleThreadedItemLoader = new SingleThreadedItemLoader(indices, getLayerBuffer(layer), action);

		// Potentially long running part
		singleThreadedItemLoader.execute();

		long loadedItems = 0L;

		// Only in case of missing items do we need to start mapping and/or accessing underlying resources
		if(singleThreadedItemLoader.getMissingIndicesCount()>0L) {
			IndexSet[] missingIndices = singleThreadedItemLoader.getMissingIndices();

			// If needed map to primary layer indices
			IndexSet[] indicesToLoad = missingIndices;
			if(mappingRequired) {
				indicesToLoad = mapIndices(layer.getManifest(), primaryLayer.getManifest(), missingIndices);
			}

			// Delegate to chunk aware method to do the actual I/O stuff
			try {
				// We only forward the original action if we did not have to translate indices
				loadedItems = loadPrimaryLayer(indicesToLoad, primaryLayer, mappingRequired ? null : action);
			} catch (IOException e) {
				throw new ModelException(getCorpus(), GlobalErrorCode.IO_ERROR,
						"Failed loading chunks for layer: "+ModelUtils.getUniqueId(primaryLayer));
			}

			// When everything's done, load and publish the items in correct layer (in case of mapping)
			if(mappingRequired) {
				singleThreadedItemLoader.loadMissingIndices();
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
		private IndexSet[] missingIndices;

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

				allIndicesMissing = missingIndicesCount==requestedItemCount;

				// Make sure to tell the publisher we're done, so it can wrap up pending chunk information
				publisher.flush();
			}
		}

		public void loadMissingIndices() {
			// This might force creation of missing indices array
			getMissingIndices();

			checkState("No missing indices defined", missingIndices!=null);


			OfLong it = IndexUtils.asIterator(missingIndices);

			buffer.load(it, this::onLoadedMissingItem, this::onRepeatedlyMissingItem);

			publisher.flush();
		}

		/**
		 * Called only for missing items by the {@link BufferedItemManager.LayerBuffer}
		 */
		private void onMissingItem(long index) {
			missingIndicesCount++;

			if(missingIndicesBuffer==null) {
				IndexCollectorFactory factory = new IndexCollectorFactory();
				factory.totalSizeLimit(requestedItemCount);
				factory.inputSorted(inputSorted);

				missingIndicesBuffer = factory.create();
			}

			missingIndicesBuffer.add(index);
		}

		private void onAvailableItem(Item item, long index) {
			ChunkState chunkState = ChunkState.forItem(item);
			if(chunkState!=ChunkState.CORRUPTED) {
				availableIndicesCount++;
			}
			publisher.accept(index, item, chunkState);
		}

		private void onLoadedMissingItem(Item item, long index) {
			publisher.accept(index, item, ChunkState.forItem(item));
		}

		private void onRepeatedlyMissingItem(long index) {
			throw new ModelException(ModelErrorCode.DRIVER_INDEX_ERROR, String.format(
					"Item at index %d not available after internal loading", _long(index)));
		}

		public @Nullable IndexSet[] getMissingIndices() {
			// Shortcut in case of empty buffer: just forward the raw input indices
			if(allIndicesMissing) {
				return indices;
			}

			if(missingIndicesBuffer==null) {
				return null;
			}

			if(missingIndices==null) {
				missingIndices = missingIndicesBuffer.build();
			}

			return missingIndices;
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
			@Nullable Consumer<ChunkInfo> action) throws IOException, InterruptedException, IcarusApiException {
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
	public long loadFile(int fileIndex, @Nullable Consumer<ChunkInfo> action)
			throws IOException, InterruptedException, IcarusApiException {

		LockableFileObject fileObject = getFileObject(fileIndex);
		StampedLock lock = fileObject.getLock();
		FileInfo fileInfo = getFileStates().getFileInfo(fileIndex);

		long stamp = lock.readLock();
		try {

			if(!fileInfo.isValid())
				throw new ModelException(ModelErrorCode.DRIVER_ERROR,
						"Cannot attempt to load from invalid file at index "+fileIndex+" ["+fileInfo.getPath()+"] - state="+fileInfo.states2String());

			if(fileInfo.isFlagSet(ElementFlag.PARTIALLY_LOADED))
				throw new ModelException(ModelErrorCode.DRIVER_ERROR,
						"Cannot attempt to completely load already partially loaded file at index "+fileIndex+" ["+fileInfo.getPath()+"]");

			if(fileInfo.isFlagSet(ElementFlag.LOADED))
				throw new ModelException(ModelErrorCode.DRIVER_ERROR,
						"File already completely loaded for index "+fileIndex+" ["+fileInfo.getPath()+"]");

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

			// All good till here, mark file as loaded
			fileInfo.setFlag(ElementFlag.LOADED);

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
	 * Attempts to scan all files comprising the resources of this driver.
	 *
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws IcarusApiException
	 */
	public boolean scanAllFiles() throws IOException, InterruptedException, IcarusApiException {
		ResourceSet dataFiles = getDataFiles();

		int fileCount = dataFiles.getResourceCount();

		for(int fileIndex = 0; fileIndex < fileCount; fileIndex++) {

			boolean success = scanFile(fileIndex);
			if(!success) {
				return false;
			}
		}

		return true;
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
	protected ChunkConsumer createPublisher(@Nullable Consumer<ChunkInfo> action, int bufferSize) {
		ChunkConsumer consumer = noOpChunkConsumer;

		if(action!=null) {
			if(bufferSize<=0) {
				bufferSize = DEFAULT_CHUNK_INFO_SIZE;
			}
			ChunkInfoBuilder buffer = ChunkInfoBuilder.builder(100);
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
	@Api(type=ApiType.BUILDER)
	public static class Builder extends DriverBuilder<Builder, FileDriver> {
		private ResourceSet dataFiles;
		private MetadataRegistry metadataRegistry;
		private ResourceProvider resourceProvider;

		protected Builder() {
			// no-op
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public Builder resourceProvider(ResourceProvider resourceProvider) {
			requireNonNull(resourceProvider);
			checkState(this.resourceProvider==null);

			this.resourceProvider = resourceProvider;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public ResourceProvider getResourceProvider() {
			return resourceProvider;
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public Builder metadataRegistry(MetadataRegistry metadataRegistry) {
			requireNonNull(metadataRegistry);
			checkState(this.metadataRegistry==null);

			this.metadataRegistry = metadataRegistry;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public MetadataRegistry getMetadataRegistry() {
			return metadataRegistry;
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public Builder dataFiles(ResourceSet dataFiles) {
			requireNonNull(dataFiles);
			checkState(this.dataFiles==null);

			this.dataFiles = dataFiles;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
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

		/**
		 * Disables tracking of item use which in turn would enable the driver to
		 * unload any corpus members that are no longer in active use.
		 */
		DISABLE_TRACKING("disableTracking", ValueType.BOOLEAN),
		;

		private final String key;
		private final ValueType type;

		private OptionKey(String key, ValueType type) {
			this.key = FileDriverUtils.SHARED_PROPERTY_PREFIX+'.'+key;
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
		 * @param reportBuilder
		 * @param env
		 * @return {@code true} iff this step executed successfully
		 * @throws Exception
		 */
		boolean apply(FileDriver driver, ReportBuilder<ReportItem> reportBuilder, Options env) throws IcarusApiException, InterruptedException;

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
