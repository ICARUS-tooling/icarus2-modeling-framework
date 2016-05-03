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

 * $Revision: 451 $
 * $Date: 2016-02-03 12:33:06 +0100 (Mi, 03 Feb 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/driver/AbstractDriver.java $
 *
 * $LastChangedDate: 2016-02-03 12:33:06 +0100 (Mi, 03 Feb 2016) $
 * $LastChangedRevision: 451 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.driver;

import static de.ims.icarus2.model.standard.util.CorpusUtils.getName;
import static de.ims.icarus2.model.util.Conditions.checkNotNull;
import static de.ims.icarus2.model.util.Conditions.checkState;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.driver.ChunkInfo;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.DriverListener;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingStorage;
import de.ims.icarus2.model.api.driver.mods.DriverModule;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus2.model.api.manifest.ContextManifest;
import de.ims.icarus2.model.api.manifest.DriverManifest;
import de.ims.icarus2.model.api.manifest.ImplementationLoader;
import de.ims.icarus2.model.api.manifest.DriverManifest.ModuleManifest;
import de.ims.icarus2.model.api.manifest.ItemLayerManifest;
import de.ims.icarus2.model.api.manifest.MappingManifest;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.meta.AnnotationValueDistribution;
import de.ims.icarus2.model.api.meta.AnnotationValueSet;
import de.ims.icarus2.model.io.resources.FileResource;
import de.ims.icarus2.model.io.resources.IOResource;
import de.ims.icarus2.model.registry.CorpusMemberFactory;
import de.ims.icarus2.model.standard.driver.io.BufferedIOResource.BlockCache;
import de.ims.icarus2.model.standard.driver.io.RUBlockCache;
import de.ims.icarus2.model.standard.driver.io.UnlimitedBlockCache;
import de.ims.icarus2.model.standard.driver.mapping.MappingFactory;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.Options;

/**
 *
 * For the most part not thread safe!
 *
 * @author Markus Gärtner
 * @version $Id: AbstractDriver.java 451 2016-02-03 11:33:06Z mcgaerty $
 *
 */
public abstract class AbstractDriver implements Driver {
	private final DriverManifest manifest;

	private volatile boolean dead = false;

	// Helper flag to disable connection based errors during the connect() method
	private volatile boolean allowUncheckedAccess = false;
	private final AtomicBoolean connected = new AtomicBoolean(false);
	private Corpus corpus;

	private final List<DriverListener> driverListeners = new CopyOnWriteArrayList<>();

	private Context context;

	protected final Lock lock = new ReentrantLock();

	private MappingStorage mappings;

	protected AbstractDriver(DriverBuilder<?,?> builder) {
		this(builder.getManifest());
	}

	protected AbstractDriver(DriverManifest manifest) {
		checkNotNull(manifest);

		this.manifest = manifest;
	}

	@Override
	public String toString() {
		return getClass().getName()+"Driver["+manifest.getId()+"]";
	}

	@Override
	public boolean isConnected() {
		return connected.get();
	}

	protected boolean isDead() {
		return dead;
	}

	@Override
	public void connect(Corpus target) throws InterruptedException {
		lock.lock();

		try {
			if(isDead())
				throw new ModelException(ModelErrorCode.DRIVER_CONNECTION, "Driver previously disconencted - considered dead: "+manifest.getId());
			if(isConnected())
				throw new ModelException(ModelErrorCode.DRIVER_CONNECTION, "Driver already connected: "+manifest.getId());

			this.corpus = target;

			allowUncheckedAccess = true;

			// Delegate initialization work
			doConnect();

			verifyInternals();

			connected.set(true);

			// Connect notify AFTER this driver's 'connected' flag has been set!
			context.connectNotify(this);
		} finally {
			allowUncheckedAccess = false;
			lock.unlock();
		}
	}

	protected final void setMappings(MappingStorage mappings) {
		checkNotNull(mappings);
		checkState(this.mappings==null);

		this.mappings = mappings;
	}

	protected final void setContext(Context context) {
		checkNotNull(context);
		checkState(this.context==null);

		this.context = context;
	}

	protected void verifyInternals() {

		checkState("Missing context", context!=null);
		checkState("Missing mappings", mappings!=null);
	}

	/**
	 * Performs maintenance work during the connection process after the basic internal
	 * modules like mappings and the respective context have been initialized. For virtual
	 * drivers for example, this is the place to create their content. Other implementations
	 * might use this opportunity to prepare {@link DriverModule modules} or do sanity checks
	 * on external resources.
	 * <p>
	 * Note that this method will be called <b>first</b> from {@link #connect(Corpus)}.
	 * <p>
	 * The default implementation creates the context
	 *
	 * @throws InterruptedException
	 */
	protected void doConnect() throws InterruptedException {
		setContext(createContext());
		setMappings(createMappings());
	}

	/**
	 * Creates the {@link Context} instance of this driver.
	 * <p>
	 * Note that this method should only perform initialization and proper
	 * linking of all the involved layers and such, but not {@link Context#connectNotify(Driver) connect}
	 * the context, which is the responsibility of the calling code!
	 *
	 * @see #connect(Corpus)
	 *
	 * @return
	 */
	protected Context createContext() {

		CorpusMemberFactory factory = corpus.getManager().newFactory();
		ContextManifest contextManifest = manifest.getContextManifest();

		// Allow custom layer implementations defined by the driver
		Options options = createCustomLayers(contextManifest);
		//TODO maybe introduce some general options or verify custom layers?

		return factory.createContext(corpus, contextManifest, options);
	}

	/**
	 * The default implementation does not provide custom layer implementations and therefore
	 * always returns {@code null}.
	 *
	 * @see de.ims.icarus2.model.api.driver.Driver#createCustomLayers(de.ims.icarus2.model.api.manifest.ContextManifest)
	 */
	protected Options createCustomLayers(ContextManifest manifest) {
		return null;
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
	protected MappingStorage createMappings() {

		DriverManifest manifest = getManifest();
		MappingStorage.Builder builder = new MappingStorage.Builder();

		// Allow for subclasses to provide a fallback function
		BiFunction<ItemLayerManifest, ItemLayerManifest, Mapping> fallback = createMappingFallback();
		if(fallback!=null) {
			builder.fallback(fallback);
		}

		MappingFactory mappingFactory = new MappingFactory(this);

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
				throw new ModelException(ModelErrorCode.IMPLEMENTATION_ERROR,
						"Unable to instantiate block cache: "+s);
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

	protected Mapping defaultCreateMapping(MappingFactory mappingFactory, MappingManifest mappingManifest, Options options) {
		// Populate options

		Mapping mapping = mappingFactory.createMapping(mappingManifest, options);

		//TODO do verification?

		return mapping;
	}

	protected BiFunction<ItemLayerManifest, ItemLayerManifest, Mapping> createMappingFallback() {
		return null;
	}

	@Override
	public void disconnect(Corpus target) throws InterruptedException {
		checkNotNull(target);

		lock.lock();
		try {
			checkConnected();
			if(isDead())
				throw new ModelException(ModelErrorCode.DRIVER_CONNECTION, "Driver previously disconencted - considered dead: "+manifest.getId());
			if(this.corpus!=target)
				throw new ModelException(ModelErrorCode.ILLEGAL_STATE,
						"Driver not connected to given context: "+manifest.getId());

			allowUncheckedAccess = true;

			/* Enable fail fast behavior for all parts of this driver by disabling
			 * it early. This way each attempt to access mappings or such things
			 * during the duration of this method call is bound to fail.
			 */
			connected.set(false);

			// Delegate cleanup work
			doDisconnect();

			context.disconnectNotify(this);

			// Let gc do the rest
			context = null;
			corpus = null;
			mappings = null;

			dead = true;
		} finally {
			allowUncheckedAccess = false;
			lock.unlock();
		}
	}

	/**
	 * Performs maintenance work <b>before</b> any other steps in the process of disconnecting
	 * from a corpus.
	 * <p>
	 * The default implementation just {@link #closeMappings() shuts down} all the mappings for
	 * this driver.
	 *
	 * @throws InterruptedException
	 */
	protected void doDisconnect() throws InterruptedException {
		closeMappings();
	}

	/**
	 * Iterates over all mappings this driver manages and calls {@link Mapping#close()} on them.
	 */
	protected void closeMappings() {
		mappings.forEachMapping(Mapping::close);
	}

	/**
	 * Throws {@link ModelException} with code {@link ModelErrorCode#DRIVER_CONNECTION} in case
	 * the driver is not currently connected. Does nothing if the driver is in the process
	 * of connecting and was marked to allow unchecked access to connection critical code.
	 */
	protected void checkConnected() {
		if(!allowUncheckedAccess && !isConnected())
			throw new ModelException(ModelErrorCode.DRIVER_CONNECTION, "Driver not yet connected: "+manifest.getId());
	}

	/**
	 * Throws {@link ModelException} with code {@link ModelErrorCode#DRIVER_READY} in case
	 * the driver is not currently ready.
	 */
	protected void checkReady() {
		if(!isReady())
			throw new ModelException(ModelErrorCode.DRIVER_READY, "Driver is not ready: "+manifest.getId());
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.Driver#getContext()
	 */
	@Override
	public Context getContext() {
		checkConnected();

		return context;
	}

	public Corpus getCorpus() {
		checkConnected();
		return corpus;
	}

	/**
	 *
	 * @see de.ims.icarus2.model.api.members.item.ItemLayerManager#getLayers()
	 *
	 * @throws ModelException in case the driver is currently not connected to any live context
	 */
	@Override
	public Collection<Layer> getLayers() {
		return getContext().getLayers(ItemLayer.class);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.Driver#getManifest()
	 */
	@Override
	public DriverManifest getManifest() {
		return manifest;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.Driver#getMappings()
	 */
	@Override
	public MappingStorage getMappings() {
		checkConnected();

		return mappings;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.Driver#getMapping(ItemLayerManifest, ItemLayerManifest)
	 */
	@Override
	public Mapping getMapping(ItemLayerManifest sourceLayer, ItemLayerManifest targetLayer) {
		return getMappings().getMapping(sourceLayer, targetLayer);
	}

	@Override
	public void addDriverListener(DriverListener listener) {
		checkNotNull(listener);

		if(!driverListeners.contains(listener)) {
			driverListeners.add(listener);
		}
	}

	@Override
	public void removeDriverListener(DriverListener listener) {
		checkNotNull(listener);

		driverListeners.remove(listener);
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

		final LayerGroup group = layer.getLayerGroup();

		ItemLayer primaryLayer = layer;
		IndexSet[] primaryIndices = indices;

		// Translate indices to primary layer of enclosing group if the given layer is not primary
		if(group.getPrimaryLayer()!=layer) {
			primaryLayer = group.getPrimaryLayer();

			// Delegate to default lookup
			primaryIndices = mapIndices(layer.getManifest(), primaryLayer.getManifest(), indices);
		}

		// Delegate to wrapped ItemLayerManager for actual loading
		return loadPrimaryLayer(primaryIndices, primaryLayer, action);
	}

	/**
	 * Loads data chunks for the specified primary layer. This method is not publicly exposed
	 * and only used be the general {@link #load(IndexSet[], ItemLayer, Consumer) load} method
	 * after indices have been translated properly to the respective primary layer.
	 *
	 * @param indices
	 * @param layer
	 * @param action
	 * @return
	 * @throws InterruptedException
	 */
	protected abstract long loadPrimaryLayer(IndexSet[] indices, ItemLayer layer,
			Consumer<ChunkInfo> action) throws InterruptedException;

	protected void fireChunksLoaded(ItemLayer layer, ChunkInfo info) {
		if(driverListeners.isEmpty() || info==null || info.chunkCount()==0) {
			return;
		}

		for(DriverListener listener : driverListeners) {
			listener.chunksLoaded(layer, info);
		}
	}

	protected void fireChunksSkipped(ItemLayer layer, ChunkInfo info) {
		if(driverListeners.isEmpty() || info==null || info.chunkCount()==0) {
			return;
		}

		for(DriverListener listener : driverListeners) {
			listener.chunksSkipped(layer, info);
		}
	}

	/**
	 * Instantiates a new {@link DriverModule module} for this driver based on the
	 * provided manifest. This method will first obtain a new {@link ImplementationLoader}
	 * via the {@link Corpus} it is connected with and then set it up with additional
	 * information:
	 * <br>
	 * The {@link ImplementationLoader#corpus(Corpus) corpus} will be set to the above mentioned one.
	 * The {@link ImplementationLoader#environment(Object) environment} will be set to this driver instance.
	 * <br>
	 * When calling the {@link ImplementationLoader#instantiate(Class) instantiation} method, the
	 * given {@code resultClass} argument will be passed on.
	 *
	 * @param resultClass
	 * @param manifest
	 * @return
	 */
	protected <T extends Object> T defaultInstantiateModule(Class<T> resultClass, ModuleManifest manifest) {
		checkNotNull(resultClass);
		checkNotNull(manifest);

		checkConnected();

		CorpusMemberFactory factory = corpus.getManager().newFactory();

		return factory.newImplementationLoader()
				.manifest(manifest.getImplementationManifest())
				.corpus(corpus)
				.environment(this)
				.message("Module manifest "+getName(manifest))
				.instantiate(resultClass);
	}

	/**
	 * Default implementation throws {@link ModelErrorCode#UNSUPPORTED_OPERATION ModelException}.
	 *
	 * @see de.ims.icarus2.model.api.driver.Driver#addItem(de.ims.icarus2.model.api.layer.ItemLayer, de.ims.icarus2.model.api.members.item.Item, long)
	 */
	@Override
	public void addItem(ItemLayer layer, Item item, long index) {
		throw new ModelException(ModelErrorCode.UNSUPPORTED_OPERATION,
				"Driver implementation does not support addition of items");
	}

	/**
	 * Default implementation throws {@link ModelErrorCode#UNSUPPORTED_OPERATION ModelException}.
	 *
	 * @see de.ims.icarus2.model.api.driver.Driver#removeItem(de.ims.icarus2.model.api.layer.ItemLayer, de.ims.icarus2.model.api.members.item.Item, long)
	 */
	@Override
	public void removeItem(ItemLayer layer, Item item, long index) {
		throw new ModelException(ModelErrorCode.UNSUPPORTED_OPERATION,
				"Driver implementation does not support removal of items");
	}

	/**
	 * Default implementation throws {@link ModelErrorCode#UNSUPPORTED_OPERATION ModelException}.
	 *
	 * @see de.ims.icarus2.model.api.driver.Driver#moveItem(de.ims.icarus2.model.api.layer.ItemLayer, de.ims.icarus2.model.api.members.item.Item, long, long)
	 */
	@Override
	public void moveItem(ItemLayer layer, Item item, long fromIndex,
			long toIndex) {
		throw new ModelException(ModelErrorCode.UNSUPPORTED_OPERATION,
				"Driver implementation does not support moving of items");
	}

	/**
	 * Default implementation always returns {@code false}.
	 *
	 * @see de.ims.icarus2.model.api.driver.Driver#hasPendingChanges()
	 */
	@Override
	public boolean hasPendingChanges() {
		return false;
	}

	/**
	 * Default implementation does nothing.
	 *
	 * @see de.ims.icarus2.model.api.driver.Driver#flush()
	 */
	@Override
	public void flush() {
		// no-op
	}

	/**
	 * Default implementation does nothing and returns {@code null}.
	 *
	 * @see de.ims.icarus2.model.api.driver.Driver#lookupValues(de.ims.icarus2.model.api.layer.AnnotationLayer, java.lang.String)
	 */
	@Override
	public AnnotationValueSet lookupValues(AnnotationLayerManifest layer, String key)
			throws InterruptedException {
		return null;
	}

	/**
	 * Default implementation does nothing and returns {@code null}.
	 *
	 * @see de.ims.icarus2.model.api.driver.Driver#lookupDistribution(de.ims.icarus2.model.api.layer.AnnotationLayer, java.lang.String)
	 */
	@Override
	public AnnotationValueDistribution lookupDistribution(
			AnnotationLayerManifest layer, String key) throws InterruptedException {
		return null;
	}

	public static abstract class DriverBuilder<B extends DriverBuilder<B, D>, D extends Driver> extends AbstractBuilder<B, D> {

		private DriverManifest manifest;

		public B manifest(DriverManifest manifest) {
			checkNotNull(manifest);
			checkState(this.manifest==null);

			this.manifest = manifest;

			return thisAsCast();
		}

		public DriverManifest getManifest() {
			return manifest;
		}

		@Override
		protected void validate() {
			checkState("No manifest defined", manifest!=null);
		}
	}
}
