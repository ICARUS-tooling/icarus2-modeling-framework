/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 440 $
 * $Date: 2015-12-18 14:36:38 +0100 (Fr, 18 Dez 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/driver/file/FileDriver.java $
 *
 * $LastChangedDate: 2015-12-18 14:36:38 +0100 (Fr, 18 Dez 2015) $
 * $LastChangedRevision: 440 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.filedriver;

import static de.ims.icarus2.util.Conditions.checkNotNull;
import static de.ims.icarus2.util.Conditions.checkState;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.FileDriverStates.FileInfo;
import de.ims.icarus2.filedriver.FileMetadata.FileKey;
import de.ims.icarus2.filedriver.FileMetadata.ItemLayerKey;
import de.ims.icarus2.filedriver.FileMetadata.MappingKey;
import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;
import de.ims.icarus2.filedriver.io.RUBlockCache;
import de.ims.icarus2.filedriver.io.UnlimitedBlockCache;
import de.ims.icarus2.filedriver.io.sets.FileSet;
import de.ims.icarus2.filedriver.mapping.AbstractStoredMapping;
import de.ims.icarus2.filedriver.mapping.DefaultMappingFactory;
import de.ims.icarus2.filedriver.mapping.DefaultMappingFactory.Property;
import de.ims.icarus2.filedriver.mapping.MappingFactory;
import de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexStorage;
import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.ChunkInfo;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingStorage;
import de.ims.icarus2.model.api.driver.mods.DriverModule;
import de.ims.icarus2.model.api.io.resources.FileResource;
import de.ims.icarus2.model.api.io.resources.IOResource;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.ItemLayerManager;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.driver.AbstractDriver;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.Options;


/**
 * @author Markus Gärtner
 * @version $Id: FileDriver.java 440 2015-12-18 13:36:38Z mcgaerty $
 *
 */
public abstract class FileDriver extends AbstractDriver {

	/**
	 * Registry for storing metadata info like number of items in each layer,
	 * availability of chunk indices for certain files, etc...
	 */
	private final MetadataRegistry metadataRegistry;

	private ItemLayerManager itemManager;

	private FileDriverStates states;

	/**
	 * Chunk indices for the primary layers in each layer group of the host driver.
	 * This is an optional feature and connector implementations are free to decide on their
	 * own whether or not it makes sense for a certain data set to map chunks at all
	 * or just do a "read all" operation when asked to load items from it.
	 */
	protected volatile ChunkIndexStorage chunkIndexStorage;

	/**
	 * Set of corpus files that contain the data for this driver
	 */
	protected final FileSet dataFiles;

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

	public FileDriverStates getStates() {
		checkConnected();
		return states;
	}

	public MetadataRegistry getMetadataRegistry() {
		return metadataRegistry;
	}

	public Logger getLogger() {
		return log;
	}

	/**
	 * Should only be used by modules of the driver or other code that takes part in the
	 * data preparation process!
	 *
	 * @return
	 */
	public FileDriverStates getFileDriverStates() {
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
		BiFunction<ItemLayerManifest, ItemLayerManifest, Mapping> fallback = createMappingFallback();
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

	public static final String HINT_LRU_CACHE = "LRU";
	public static final String HINT_MRU_CACHE = "MRU";
	public static final String HINT_UNLIMITED_CACHE = "UNLIMITED";

	protected static BlockCache toBlockCache(String s) {
		if(s==null) {
			return null;
		}

		if(HINT_LRU_CACHE.equals(s)) {
			return RUBlockCache.newLeastRecentlyUsedCache();
		} else if(HINT_MRU_CACHE.equals(s)) {
			return RUBlockCache.newMostRecentlyUsedCache();
		} else if(HINT_UNLIMITED_CACHE.equals(s)) {
			return new UnlimitedBlockCache();
		} else {
			try {
				return (BlockCache) Class.forName(s).newInstance();
			} catch (InstantiationException | IllegalAccessException
					| ClassNotFoundException e) {
				throw new ModelException(ManifestErrorCode.IMPLEMENTATION_ERROR,
						"Unable to instantiate block cache: "+s, e);
			}
		}
	}

	protected static Integer toInteger(int value) {
		return value==-1 ? null : Integer.valueOf(value);
	}

	protected static Long toLong(long value) {
		return value==NO_INDEX ? null : Long.valueOf(value);
	}

	protected static IndexValueType toValueType(String s) {
		if(s==null) {
			return null;
		}

		return IndexValueType.valueOf(s);
	}

	protected static IOResource toResource(String s) {
		if(s==null) {
			return null;
		}

		return new FileResource(Paths.get(s));
	}

	/**
	 * Enhanced version of the super method. This implementation augments the given {@link Options options} with values for the
	 * following properties:
	 * <ul>
	 * <li>{@link Property#BLOCK_CACHE}</li>
	 * <li>{@link Property#CACHE_SIZE}</li>
	 * <li>{@link Property#BLOCK_POWER}</li>
	 * <li>{@link Property#GROUP_POWER}</li>
	 * <li>{@link Property#VALUE_TYPE}</li>
	 * <li>{@link Property#RESOURCE}</li>
	 * </ul>
	 *
	 * The values are created by querying the {@link MetadataRegistry} of this driver for a set of {@link MappingKey mapping-keys}
	 * and processing those stored values. The augmented options are then passed to the super method to do the actual instantiation
	 * and verification work.
	 *
	 * @param mappingFactory
	 * @param mappingManifest
	 * @param options
	 * @return
	 *
	 * @see #toBlockCache(String)
	 * @see #toInteger(int)
	 * @see #toResource(String)
	 * @see #toValueType(String)
	 */
	protected Mapping defaultCreateMapping(MappingFactory mappingFactory, MappingManifest mappingManifest, Options options) {
		// Populate options

		ContextManifest contextManifest = getManifest().getContextManifest();

		ItemLayerManifest source = (ItemLayerManifest) contextManifest.getLayerManifest(mappingManifest.getSourceLayerId());
		ItemLayerManifest target = (ItemLayerManifest) contextManifest.getLayerManifest(mappingManifest.getTargetLayerId());

		options.put(Property.BLOCK_CACHE.key(), toBlockCache(metadataRegistry.getValue(MappingKey.BLOCK_CACHE.getKey(source, target))));
		options.put(Property.CACHE_SIZE.key(), toInteger(metadataRegistry.getIntValue(MappingKey.CACHE_SIZE.getKey(source, target), -1)));
		options.put(Property.BLOCK_POWER.key(), toInteger(metadataRegistry.getIntValue(MappingKey.BLOCK_POWER.getKey(source, target), -1)));
		options.put(Property.GROUP_POWER.key(), toInteger(metadataRegistry.getIntValue(MappingKey.GROUP_POWER.getKey(source, target), -1)));
		options.put(Property.VALUE_TYPE.key(), toValueType(metadataRegistry.getValue(MappingKey.VALUE_TYPE.getKey(source, target))));
		options.put(Property.RESOURCE.key(), toResource(metadataRegistry.getValue(MappingKey.PATH.getKey(source, target))));

		// Populate options further

		Mapping mapping = mappingFactory.createMapping(mappingManifest, options);

		//TODO do verification?

		return mapping;
	}

	public void resetMappings() {

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
		// TODO access local cache, if no data available there, delegate to the manifest based method
		return 0;
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

		return states.getLayerInfo(layer).getSize();
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
		return itemManager.getItem(layer, index);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.ItemLayerManager#release(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.layer.ItemLayer)
	 */
	@Override
	public void release(IndexSet[] indices, ItemLayer layer)
			throws InterruptedException {
		itemManager.release(indices, layer);
	}

	/**
	 * This implementation initializes the internal {@link FileDriverStates} storage
	 * before calling the super method.
	 * In addition a customizable series of {@link PreparationStep preparation steps} is performed
	 * to allow for a more flexible initialization.
	 *
	 * @see de.ims.icarus2.model.standard.driver.AbstractDriver#doConnect()
	 * @see #getPreparationSteps()
	 */
	@Override
	protected void doConnect() throws InterruptedException {

		states = new FileDriverStates(this);

		// Creates context and mappings
		super.doConnect();

		final PreparationStep[] steps = getPreparationSteps();

		boolean driverIsValid = true;
		Options env = new Options();

		for(int i=0; i<steps.length; i++) {

			checkInterrupted();

			PreparationStep step = steps[i];

			boolean valid = true;

			try {
				valid = step.apply(this, env);
			} catch(Exception e) {
				log.error("Preparation step {} of {} failed: {}", i+1, steps.length, step.name(), e);
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
	 * and discards the current set of {@link FileDriverStates states}.
	 *
	 * @see de.ims.icarus2.model.standard.driver.AbstractDriver#doDisconnect()
	 */
	@Override
	protected void doDisconnect() throws InterruptedException {
		super.doDisconnect();

		metadataRegistry.close();

		states = null;
	}

	public boolean hasChunkIndex() {
		checkConnected();
		return chunkIndexStorage!=null;
	}

	/**
	 * @see de.ims.icarus2.model.standard.driver.AbstractDriver#loadPrimaryLayer(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.layer.ItemLayer, java.util.function.Consumer)
	 */
	@Override
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
			// No chunk index available, proceed with loading the respective file
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
					//TODO do we need to supply the already loaded chunks from that file to the 'action' argument?
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
                return mid; // key found
        }
        return -(low + 1);  // key not found.
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
	public abstract void scanFile(int fileIndex) throws IOException, InterruptedException;

	/**
	 * Loads the entire content of the file specified by {@code fileIndex} and transforms
	 * it into the appropriate model representation.
	 *
	 * @param fileIndex
	 * @throws IOException
	 * @throws ModelException in case the specified file has already been loaded
	 */
	public abstract long loadFile(int fileIndex, Consumer<ChunkInfo> action) throws IOException, InterruptedException;

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
	public abstract long loadChunks(ItemLayer layer, IndexSet[] indices, Consumer<ChunkInfo> action) throws IOException, InterruptedException;

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id: FileDriver.java 440 2015-12-18 13:36:38Z mcgaerty $
	 *
	 */
	public abstract static class FileDriverBuilder<B extends FileDriverBuilder<B>> extends DriverBuilder<B, FileDriver> {
		private FileSet dataFiles;
		private MetadataRegistry metadataRegistry;

		public FileDriverBuilder metadataRegistry(MetadataRegistry metadataRegistry) {
			checkNotNull(metadataRegistry);
			checkState(this.metadataRegistry==null);

			this.metadataRegistry = metadataRegistry;

			return thisAsCast();
		}

		public MetadataRegistry getMetadataRegistry() {
			return metadataRegistry;
		}

		public FileDriverBuilder dataFiles(FileSet dataFiles) {
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
	 * @version $Id: FileDriver.java 440 2015-12-18 13:36:38Z mcgaerty $
	 *
	 */
	public static enum OptionKey {

		//TODO
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
			MemberManifest.Property property = manifest.getProperty(key);
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
	 * @version $Id: FileDriver.java 440 2015-12-18 13:36:38Z mcgaerty $
	 *
	 */
	public interface PreparationStep {
		boolean apply(FileDriver driver, Options env) throws Exception;
		String name();
	}
}
