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
package de.ims.icarus2.model.standard.corpus;

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static de.ims.icarus2.model.util.ModelUtils.getUniqueId;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.lang.Primitives._int;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.apiguard.Api;
import de.ims.icarus2.apiguard.Api.ApiType;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Context.VirtualContext;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.corpus.CorpusOption;
import de.ims.icarus2.model.api.corpus.GenerationControl;
import de.ims.icarus2.model.api.corpus.OwnableCorpusPart;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.id.IdManager;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.edit.CorpusEditManager;
import de.ims.icarus2.model.api.edit.CorpusUndoManager;
import de.ims.icarus2.model.api.events.CorpusEventManager;
import de.ims.icarus2.model.api.events.CorpusListener;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.meta.MetaData;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.registry.CorpusMemberFactory;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.api.view.Scope;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView;
import de.ims.icarus2.model.api.view.streamed.StreamOption;
import de.ims.icarus2.model.api.view.streamed.StreamedCorpusView;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.ImplementationLoader;
import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.model.standard.members.container.AbstractImmutableContainer;
import de.ims.icarus2.model.standard.members.container.ProxyContainer;
import de.ims.icarus2.model.standard.registry.ContextFactory;
import de.ims.icarus2.model.standard.view.paged.DefaultPagedCorpusView;
import de.ims.icarus2.model.standard.view.streamed.DefaultStreamedCorpusView;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.AccumulatingException;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.collections.set.DataSet;
import de.ims.icarus2.util.data.ContentType;
import de.ims.icarus2.util.events.EventObject;
import de.ims.icarus2.util.events.Events;
import de.ims.icarus2.util.events.SimpleEventListener;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

/**
 * Implements a corpus that manages its contents in a lazy way. {@link Context} instances
 * and their {@link Driver drivers} will be created and linked when they are first being used.
 * <p>
 * When a corpus consists of multiple customContexts and there exist dependencies between several
 * customContexts, then creation and initialization of context instances will be performed in such a
 * way, that a context gets fully initialized only <b>after</b> all the other customContexts it is
 * depending on are created and connected with their respective drivers.
 * Note that the actual instantiation and linking of customContexts, {@link LayerGroup groups} and
 * {@link Layer layers} is delegated to a new {@link ContextFactory} object for each context.
 * This factory implementation honors the ability of drivers to provide custom implementations
 * of groups or layers (or {@link AnnotationStorage annotation storages}) if they do wish so.
 * <p>
 * Due to the lazy creation of both customContexts and drivers it is perfectly legal to encounter a
 * driver whose context has not yet been created. Instantiation of customContexts and their drivers
 * is done as follows:
 * <p>
 * {@code Context} instances are created by delegating to a {@link CorpusMemberFactory} obtained
 * from the host {@link CorpusManager manager} and calling the
 * {@link CorpusMemberFactory#createContext(Corpus, ContextManifest, Options)} method with the
 * {@code corpus} argument being this corpus instance.
 * For {@code Drivers} the process is a bit more complicated. First the same factory as above
 * will be used to fetch a new {@link ImplementationLoader} which will then set up with additional
 * settings, before calling its {@link ImplementationLoader#instantiate(Class)} method with
 * {@link Driver} as the desired result class. Note that both {@link ImplementationLoader#corpus(Corpus) corpus}
 * and {@link ImplementationLoader#environment(Object) environment} of the loader will be set to
 * this corpus instance. This way a potentially used {@link ImplementationManifest.Factory factory}
 * has full access to all information this corpus can provide.
 *
 * @author Markus Gärtner
 *
 */
@TestableImplementation(Corpus.class)
public class DefaultCorpus implements Corpus {

	private static final Logger log = LoggerFactory.getLogger(DefaultCorpus.class);

	public static Builder builder() {
		return new Builder();
	}

	private final CorpusManager manager;
	private final CorpusManifest manifest;
	private final MetadataRegistry metadataRegistry;

	private final CorpusEventManager corpusEventManager = new CorpusEventManager(this);
	private final CorpusEditManager editModel = new CorpusEditManager(this);
	private final CorpusUndoManager undoManager = new CorpusUndoManager(this);
	private final GenerationControl generationControl;


	private final Lock lock = new ReentrantLock();

	// All contained layers, not including the overlay layer!
	private final List<Layer> layers = new ArrayList<>();
	private final Map<String, Layer> layerLookup = new Object2ObjectOpenHashMap<>();

	private final OverlayLayer overlayLayer;
	private final OverlayContainer overlayContainer;

	private final ManifestTracker manifestTracker;

	/**
	 * Proxies for all root contexts in insertion order.
	 */
	private final Map<String, ContextProxy> rootContexts = new LinkedHashMap<>();
	/**
	 *  Proxies for all custom customContexts, preserves insertion order.
	 *
	 *  NOTE:
	 *  The content of this map is effectively static, since it will be
	 *  created and populated at construction time of this corpus. Therefore
	 *  no synchronization is required when accessing it!
	 */
	private final Map<String, ContextProxy> customContexts = new LinkedHashMap<>();
	private final Map<Layer, MetaDataStorage> metaDataStorages = new Object2ObjectOpenHashMap<>();
	private final Map<String, VirtualContext> virtualContexts = new LinkedHashMap<>();

	private final CorpusPartStorage corpusPartStorage = new CorpusPartStorage();

	private AtomicBoolean closing = new AtomicBoolean(false);

	protected DefaultCorpus(Builder builder) {
		requireNonNull(builder);

		manager = builder.getManager();
		manifest = builder.getManifest();

		metadataRegistry = builder.getMetadataRegistry();
		metadataRegistry.open();

		overlayLayer = new OverlayLayer();
		overlayContainer = new OverlayContainer();

		for(ContextManifest context : manifest.getCustomContextManifests()) {

			ContextProxy proxy = new ContextProxy(context);
			customContexts.put(ManifestUtils.requireId(context), proxy);
		}

		for(ContextManifest context : manifest.getRootContextManifests()) {

			ContextProxy proxy = new ContextProxy(context);
			rootContexts.put(ManifestUtils.requireId(context), proxy);
		}

		if(rootContexts.isEmpty())
			throw new ModelException(ManifestErrorCode.MANIFEST_CORRUPTED_STATE,
					"No root context declared for corpus: "+getName(manifest));

		manifestTracker = new ManifestTracker();
		manifest.getRegistry().addListener(manifestTracker, Events.ADDED);

		if(manifest.isEditable()) {
			generationControl = DefaultGenerationControl.builder()
					//FIXME properly setup the builder
					.build();
		} else {
			generationControl = new ImmutableGenerationControl(this);
		}
	}

	@Override
	public CorpusManager getManager() {
		return manager;
	}

	@Override
	public MetadataRegistry getMetadataRegistry() {
		return metadataRegistry;
	}

	public boolean isActive() {
		return !closing.get() && manager.isCorpusConnected(manifest);
	}

	@Override
	public PagedCorpusView createView(Scope scope, IndexSet[] indices,
			AccessMode mode, Options options) throws InterruptedException {

		requireNonNull(scope);
		requireNonNull(mode);

		checkArgument("Scope refers to foreign corpus", scope.getCorpus()==this);

		if(options==null) {
			options = Options.NONE;
		}

		ItemLayer primaryLayer = scope.getPrimaryLayer();
		Driver driver = primaryLayer.getContext().getDriver();

		if(indices==null) {
			long itemCount = driver.getItemCount(primaryLayer);

			if(itemCount==IcarusUtils.UNSET_LONG)
				throw new ModelException(this, ModelErrorCode.DRIVER_METADATA_MISSING,
						"Cannot create default index set for entire primary layer, since driver has no metadata for its size");

			indices = IndexUtils.wrapSpan(0L, itemCount-1);
		}

		int pageSize = options.getInteger(CorpusOption.PARAM_VIEW_PAGE_SIZE, UNSET_INT);

		if(pageSize==UNSET_INT) {
			//TODO introduce mechanism to fetch global settings from central CorpusManager
			pageSize = CorpusOption.DEFAULT_VIEW_PAGE_SIZE;
		}

		synchronized (corpusPartStorage) {
			if(!corpusPartStorage.canOpen(mode))
				throw new ModelException(this, ModelErrorCode.VIEW_ALREADY_OPENED,
						"Cannot open another view in mode: "+mode);

			PagedCorpusView view = DefaultPagedCorpusView.builder()
				.accessMode(mode)
				.scope(scope)
				.indices(indices)
				.itemLayerManager(driver)
				.pageSize(pageSize)
				.build();

			corpusPartStorage.addPart(view, mode);

			return view;
		}
	}

	@Override
	public void forEachView(Consumer<? super PagedCorpusView> action) {
		synchronized (corpusPartStorage) {
			corpusPartStorage.forEachPart(part -> {
				if(part instanceof PagedCorpusView) {
					action.accept((PagedCorpusView) part);
				}
			});
		}
	}

	/**
	 * Creates a new stream that traverses the entirety of this corpus.
	 * <p>
	 * If no {@link StreamOption stream options} are defined, this implementation
	 * will by default enable {@link StreamOption#ALLOW_MARK} and {@link StreamOption#ALLOW_SKIP}.
	 *
	 * @see de.ims.icarus2.model.api.corpus.Corpus#createStream(de.ims.icarus2.model.api.view.Scope, de.ims.icarus2.util.AccessMode, de.ims.icarus2.util.Options)
	 */
	@Override
	public StreamedCorpusView createStream(Scope scope, AccessMode mode,
			Options options, StreamOption...streamOptions) throws InterruptedException {

		requireNonNull(scope);
		requireNonNull(mode);
		requireNonNull(streamOptions);

		checkArgument("Scope refers to foreign corpus", scope.getCorpus()==this);
		checkArgument("Mode must be readable", mode.isRead());

		if(options==null) {
			options = Options.NONE;
		}

		if(streamOptions.length==0) {
			streamOptions = new StreamOption[] {
					StreamOption.ALLOW_MARK,
					StreamOption.ALLOW_SKIP,
			};
		}

		ItemLayer primaryLayer = scope.getPrimaryLayer();
		Driver driver = primaryLayer.getContext().getDriver();

		synchronized (corpusPartStorage) {
			if(!corpusPartStorage.canOpen(mode))
				throw new ModelException(this, ModelErrorCode.VIEW_ALREADY_OPENED,
						"Cannot open another subcorpus in mode: "+mode);

			//TODO actually build the stream
			StreamedCorpusView stream = DefaultStreamedCorpusView.builder()
					.accessMode(mode)
					.scope(scope)
					.streamOptions(streamOptions)
					.itemLayerManager(driver)
					.build();

			corpusPartStorage.addPart(stream, mode);

			return stream;
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Corpus#forEachStream(java.util.function.Consumer)
	 */
	@Override
	public void forEachStream(Consumer<? super StreamedCorpusView> action) {
		synchronized (corpusPartStorage) {
			corpusPartStorage.forEachPart(part -> {
				if(part instanceof StreamedCorpusView) {
					action.accept((StreamedCorpusView) part);
				}
			});
		}
	}

	private ContextProxy getContextProxy(String id) {
		requireNonNull(id);

		ContextProxy proxy = rootContexts.get(id);

		if(proxy==null) {
			proxy = customContexts.get(id);
		}

		if(proxy==null)
			throw new ModelException(this, ManifestErrorCode.MANIFEST_UNKNOWN_ID,
					"Unknown context id: "+id);

		return proxy;
	}

	private void addContext(ContextManifest context) {
		requireNonNull(context);

		Lock lock = getLock();

		lock.lock();
		try {
			synchronized (customContexts) {
				String id = ManifestUtils.requireId(context);
				if(customContexts.containsKey(id))
					throw new ModelException(this, ManifestErrorCode.MANIFEST_DUPLICATE_ID,
							"Duplicate context: "+id);

				customContexts.put(id, new ContextProxy(context));
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Context getContext(String id) {
		return getContextProxy(id).getContext();
	}

	@Override
	public Driver getDriver(String id) {
		return getContextProxy(id).getDriver();
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Corpus#getLayer(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <L extends Layer> L getLayer(String qualifiedLayerId, boolean nativeOnly) {
		String layerId = ManifestUtils.extractElementId(qualifiedLayerId);
		Context context = null;

		String contextId = ManifestUtils.extractHostId(qualifiedLayerId);
		if(contextId!=null) {
			context = getContext(contextId);
		}

		if(context==null) {
			context = getRootContext();
		}

		Layer layer = nativeOnly ?
				context.getNativeLayer(layerId) : context.getLayer(layerId);

		return (L) layer;
	}

	//TODO somewhere say a few words about the design decision of using InterruptedException as indicator for user originated cancellation
	@Override
	public void close() throws AccumulatingException, InterruptedException {
		Lock lock = getLock();

		lock.lock();
		try {
			if(!closing.compareAndSet(false, true))
				throw new ModelException(this, GlobalErrorCode.ILLEGAL_STATE,
						"Corpus already closing: "+getName(this));

			AccumulatingException.Buffer buffer = new AccumulatingException.Buffer();

			// Ensure we wrap the entire corpus shutdown into a single metadata registry transaction
			try(MetadataRegistry registry = getMetadataRegistry()) {
				registry.beginUpdate();

				// Close open views and streams first
				try {
					corpusPartStorage.close();
				} catch (AccumulatingException e) {
					/*
					 *  Anti-pattern: destroys info from the caught AccumulatingException itself.
					 *  This is acceptable since this is the only code that uses the above close() method
					 *  and we are only interested in the wrapped exceptions!
					 */
					buffer.addExceptionsFrom(e);
				}

				// Close virtual contexts and their drivers now
				for(VirtualContext context : virtualContexts.values()) {
					try {
						context.removeNotify(this);
					} catch (Exception e) {
						buffer.addException(e);
					}
				}

				// Close custom contexts and their drivers now
				for(ContextProxy proxy : customContexts.values()) {
					try {
						proxy.close();
					} catch (InterruptedException e) {
						log.warn("Corpus shutdown  disrupted", e);
					} catch (IcarusApiException e) {
						buffer.addException(e);
					}
				}

				// Close root contexts and their drivers now
				for(ContextProxy proxy : rootContexts.values()) {
					try {
						proxy.close();
					} catch (InterruptedException e) {
						log.warn("Corpus shutdown  disrupted", e);
					} catch (Exception e) {
						buffer.addException(e);
					}
				}

				// Now end the metadata transaction and ensure it gets saved
				try {
					registry.endUpdate();
				} catch(Exception e) {
					buffer.addException(e);
				}
			}

			if(!buffer.isEmpty()) {
				buffer.setFormattedMessage("%d error during closing of corpus '%s'", getName(this));
				throw new AccumulatingException(buffer);
			}
		} finally {
			// Make sure we don't receive any more events from the registry!
			manifest.getRegistry().removeListener(manifestTracker);
			lock.unlock();
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Corpus#getLock()
	 */
	@Override
	public Lock getLock() {
		return lock;
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Corpus#getEditManager()
	 */
	@Override
	public CorpusEditManager getEditManager() {
		return editModel;
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Corpus#getGenerationControl()
	 */
	@Override
	public GenerationControl getGenerationControl() {
		return generationControl;
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Corpus#getUndoManager()
	 */
	@Override
	public CorpusUndoManager getUndoManager() {
		return undoManager;
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Corpus#addCorpusListener(de.ims.icarus2.model.api.events.CorpusListener)
	 */
	@Override
	public void addCorpusListener(CorpusListener listener) {
		corpusEventManager.addCorpusListener(listener);
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Corpus#removeCorpusListener(de.ims.icarus2.model.api.events.CorpusListener)
	 */
	@Override
	public void removeCorpusListener(CorpusListener listener) {
		corpusEventManager.removeCorpusListener(listener);
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Corpus#getManifest()
	 */
	@Override
	public CorpusManifest getManifest() {
		return manifest;
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Corpus#forEachRootContext(java.util.function.Consumer)
	 */
	@Override
	public void forEachRootContext(Consumer<? super Context> action) {
		requireNonNull(action);
		synchronized (rootContexts) {
			rootContexts.values().forEach(c -> action.accept(c.getContext()));
		}
	}

	@Override
	public void forEachCustomContext(Consumer<? super Context> action) {
		requireNonNull(action);
		synchronized (customContexts) {
			customContexts.values().forEach(c -> action.accept(c.getContext()));
		}
	}

	@Override
	public void addVirtualContext(VirtualContext context) {
		requireNonNull(context);

		synchronized (virtualContexts) {
			String id = ManifestUtils.requireId(context.getManifest());

			if(virtualContexts.containsKey(id))
				throw new ModelException(this, ManifestErrorCode.MANIFEST_DUPLICATE_ID,
						"Duplicate context id: "+id);

			virtualContexts.put(id, context);

			corpusEventManager.fireContextAdded(context);
		}
	}

	@Override
	public void removeVirtualContext(VirtualContext context) {
		requireNonNull(context);

		synchronized (virtualContexts) {
			String id = ManifestUtils.requireId(context.getManifest());

			if(!virtualContexts.remove(id, context))
				throw new ModelException(this, GlobalErrorCode.INVALID_INPUT,
						"Unknown context: "+id);

			corpusEventManager.fireContextRemoved(context);
		}
	}

	@Override
	public VirtualContext getVirtualContext(String id) {
		requireNonNull(id);

		synchronized (virtualContexts) {
			VirtualContext context = virtualContexts.get(id);

			if(context==null)
				throw new ModelException(this, ManifestErrorCode.MANIFEST_UNKNOWN_ID,
						"No such context: "+id);

			return context;
		}
	}

	@Override
	public void forEachVirtualContext(Consumer<? super VirtualContext> action) {
		requireNonNull(action);
		synchronized (virtualContexts) {
			virtualContexts.values().forEach(action);
		}
	}

	/**
	 * To be called under 'layers' lock!
	 *
	 * @param layer
	 * @return
	 */
	private boolean isKnownLayer(String id, Layer layer) {

		Layer savedLayer = layerLookup.get(id);

		if(savedLayer==null) {
			return false;
		} else if(savedLayer==layer) {
			return true;
		} else
			throw new ModelException(this, ManifestErrorCode.MANIFEST_DUPLICATE_ID,
					"Duplicate layers for id: "+id);
	}

	private boolean isKnownContext(Context context) {
		String id = ManifestUtils.requireId(context.getManifest());

		synchronized (rootContexts) {
			if(rootContexts.containsKey(id)) {
				return true;
			}
		}

		synchronized (customContexts) {
			if(customContexts.containsKey(id)) {
				return true;
			}
		}

		synchronized (virtualContexts) {
			return virtualContexts.containsKey(id);
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Corpus#addLayer(de.ims.icarus2.model.api.layer.Layer)
	 */
	@Override
	public void addLayer(Layer layer) {
		String id = getUniqueId(layer);
		if(!isKnownContext(layer.getContext()))
			throw new ModelException(this, GlobalErrorCode.INVALID_INPUT,
					"Context for layer is unknown to this corpus: "+id);

		synchronized (layers) {
			if(isKnownLayer(id, layer))
				throw new ModelException(this, GlobalErrorCode.INVALID_INPUT,
						"Layer already present: "+id);

			layers.add(layer);
			layerLookup.put(id, layer);

			corpusEventManager.fireLayerAdded(layer);
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Corpus#removeLayer(de.ims.icarus2.model.api.layer.Layer)
	 */
	@Override
	public void removeLayer(Layer layer) {
		synchronized (layers) {
			String id = getUniqueId(layer);

			if(!isKnownLayer(id, layer))
				throw new ModelException(this, GlobalErrorCode.INVALID_INPUT,
						"Layer not present: "+id);

			layers.remove(layer);
			layerLookup.remove(id);

			corpusEventManager.fireLayerRemoved(layer);
		}
	}

	/**
	 * To be called under lock on 'metaDataStorages' field
	 */
	private MetaDataStorage getMetaDataStorage(Layer layer, boolean createIfMissing) {
		requireNonNull(layer);

		MetaDataStorage storage = metaDataStorages.get(layer);

		if(storage==null && createIfMissing) {
			storage = new MetaDataStorage();
			metaDataStorages.put(layer, storage);
		}

		return storage;
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Corpus#addMetaData(de.ims.icarus2.util.data.ContentType, de.ims.icarus2.model.api.layer.Layer, java.lang.Object)
	 */
	@Override
	public void addMetaData(ContentType type, Layer layer, MetaData data) {
		synchronized (metaDataStorages) {
			getMetaDataStorage(layer, true).add(type, data);
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Corpus#removeMetaData(de.ims.icarus2.util.data.ContentType, de.ims.icarus2.model.api.layer.Layer, java.lang.Object)
	 */
	@Override
	public void removeMetaData(ContentType type, Layer layer, MetaData data) {

		synchronized (metaDataStorages) {
			MetaDataStorage storage = getMetaDataStorage(layer, false);
			if(storage==null) {
				return;
			}

			storage.remove(type, data);

			if(storage.isEmpty()) {
				metaDataStorages.remove(layer);
			}
		}
	}

	@Override
	public void forEachMetaData(BiPredicate<Layer, ContentType> filter,
			Consumer<MetaData> action) {
		synchronized (metaDataStorages) {
			for(Map.Entry<Layer, MetaDataStorage> entry : metaDataStorages.entrySet()) {
				MetaDataStorage storage = entry.getValue();

				storage.forEachEntry(entry.getKey(), filter, action);
			}
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Corpus#getOverlayLayer()
	 */
	@Override
	public ItemLayer getOverlayLayer() {
		return overlayLayer;
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Corpus#getOverlayContainer()
	 */
	@Override
	public Container getOverlayContainer() {
		return overlayContainer;
	}

	private final class ManifestTracker implements SimpleEventListener {

		/**
		 * @see de.ims.icarus2.util.events.SimpleEventListener#invoke(java.lang.Object, de.ims.icarus2.util.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			if(event.isPropertiesDefined("corpus", "context")) {
				CorpusManifest corpus = (CorpusManifest) event.getProperty("corpus");

				// Ignore all foreign corpora!
				if(corpus!=getManifest()) {
					return;
				}

				ContextManifest context = (ContextManifest) event.getProperty("context");

				addContext(context);
			}
		}

	}

	/**
	 * Storage class for currently active corpus parts.
	 * All method in this class should be called under synchronization
	 * on the current instance.
	 *
	 * @author Markus Gärtner
	 *
	 */
	private final class CorpusPartStorage implements ChangeListener {
		private OwnableCorpusPart currentWritePart;
		private final Set<OwnableCorpusPart> readParts = new ReferenceOpenHashSet<>();

		private boolean canOpen(AccessMode accessMode) {
			return !accessMode.isWrite() || currentWritePart==null;
		}

		private void addPart(OwnableCorpusPart part, AccessMode accessMode) {
			Corpus corpus = DefaultCorpus.this;

			// Sanity check
			if(!canOpen(accessMode))
				throw new ModelException(corpus, ModelErrorCode.VIEW_ERROR,
						"Cannot hold open more than 1 writing view or stream for corpus: "+getName(corpus));

			// Now add and register listener
			part.addChangeListener(this);

			if(accessMode.isWrite()) {
				currentWritePart = part;
			} else {
				readParts.add(part);
			}

			part.addNotify(corpus);
		}

		private Set<OwnableCorpusPart> getParts() {
			LazyCollection<OwnableCorpusPart> snapshot = LazyCollection.lazySet();

			snapshot.addAll(readParts);
			if(currentWritePart!=null) {
				snapshot.add(currentWritePart);
			}

			return snapshot.getAsSet();
		}

		/**
		 * @throws InterruptedException if the process got cancelled
		 * @throws AccumulatingException if closing of at least one corpus part failed
		 */
		private void close() throws InterruptedException, AccumulatingException {
			Set<OwnableCorpusPart> snapshot = getParts();

			if(snapshot.isEmpty()) {
				return;
			}

			AccumulatingException.Buffer buffer = new AccumulatingException.Buffer();

			for(OwnableCorpusPart part : snapshot) {
				try {
					part.close();
				} catch(Exception e) {
					buffer.addException(e);
				} finally {
					// Make sure we unregister from the corpus part
					part.removeNotify(DefaultCorpus.this);
				}
			}

			if(!buffer.isEmpty()) {
				buffer.setFormattedMessage("Failed to close %d parts",
						_int(buffer.getExceptionCount()));
				throw new AccumulatingException(buffer);
			}
		}

		private void forEachPart(Consumer<? super OwnableCorpusPart> action) {
			if(currentWritePart!=null) {
				action.accept(currentWritePart);
			}

			readParts.forEach(action);
		}

		private void removePart(OwnableCorpusPart part) {
			part.removeChangeListener(this);

			if(part==currentWritePart) {
				currentWritePart = null;
			} else {
				readParts.remove(part);
			}
		}

		/**
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			PagedCorpusView view = (PagedCorpusView) e.getSource();
			if(!view.isActive()) {
				synchronized (this) {
					removePart(view);
				}
			}
		}
	}

	private final class ContextProxy {
		private final ContextManifest contextManifest;
		private Driver driver;

		public ContextProxy(ContextManifest manifest) {
			requireNonNull(manifest);

			this.contextManifest = manifest;
		}

		public ContextManifest getManifest() {
			return contextManifest;
		}

		/**
		 * Throws {@link ModelException} with code {@link GlobalErrorCode#ILLEGAL_STATE}
		 * if the corpus is not marked {@link DefaultCorpus#isActive() active}.
		 */
		private void checkActive() {
			if(!isActive())
				throw new ModelException(DefaultCorpus.this, GlobalErrorCode.ILLEGAL_STATE,
						"Corpus is not active");
		}

		public Context getContext() {
			return getDriver().getContext();
		}

		public synchronized Driver getDriver() {
			if(driver==null) {

				Lock lock = getLock();

				final Corpus corpus = DefaultCorpus.this;

				lock.lock();
				try {
					checkActive();
					final CorpusMemberFactory factory = corpus.getManager().newFactory();

					driver = factory.newImplementationLoader()
							.manifest(getManifest().getDriverManifest()
									.flatMap(DriverManifest::getImplementationManifest)
									.get())
							.environment(corpus)
							.message("Driver for context '"+getName(contextManifest)+"'")
							.instantiate(Driver.class);

					driver.connect(corpus);

					corpusEventManager.fireContextAdded(driver.getContext());
				} catch (InterruptedException e) {
					throw new ModelException(corpus, ModelErrorCode.DRIVER_CONNECTION,
							"Initalization of driver cancelled by user: "+getName(contextManifest), e);
				} catch (IcarusApiException e) {
					throw new ModelException(corpus, ModelErrorCode.DRIVER_CONNECTION,
							"Initalization of driver failed: "+getName(contextManifest), e);
				} finally {
					lock.unlock();
				}
			}

			return driver;
		}

		public synchronized void close() throws InterruptedException, IcarusApiException {

			if(driver!=null) {

				final Corpus corpus = DefaultCorpus.this;

				Context context = driver.getContext();

				// Let the driver do its work
				driver.disconnect(corpus);

				// Notify context
				context.removeNotify(corpus);

				// Finally let the rest of the world know
				corpusEventManager.fireContextRemoved(context);
			}
		}
	}

	private static class MetaDataStorage {
		private final Map<ContentType, Set<MetaData>> content = new HashMap<>();

		private Set<MetaData> getEntry(ContentType type, boolean createIfMissing) {
			Set<MetaData> result = content.get(type);

			if(result==null && createIfMissing) {
				result = new ObjectOpenHashSet<>();
				content.put(type, result);
			}

			return result;
		}

		public boolean isEmpty() {
			return content.isEmpty();
		}

		public void add(ContentType type, MetaData data) {
			requireNonNull(type);
			requireNonNull(data);

			getEntry(type, true).add(data);
		}

		public void remove(ContentType type, MetaData data) {
			requireNonNull(type);
			requireNonNull(data);

			Set<MetaData> entry = getEntry(type, false);
			if(entry!=null) {
				entry.remove(data);
			}
		}

		public void forEachEntry(Layer layer, BiPredicate<Layer, ContentType> filter, Consumer<MetaData> action) {
			requireNonNull(filter);
			requireNonNull(action);

			if(content.isEmpty()) {
				return;
			}

			for(Map.Entry<ContentType, Set<MetaData>> entry : content.entrySet()) {
				for(MetaData data : entry.getValue()) {
					if(filter.test(layer, entry.getKey())) {
						action.accept(data);
					}
				}
			}
		}
	}

	private class OverlayLayer implements ItemLayer {

		private final Container proxyContainer;

		OverlayLayer() {
			proxyContainer = new ProxyContainer(this);
		}

		/**
		 * @see de.ims.icarus2.model.api.layer.ItemLayer#getProxyContainer()
		 */
		@Override
		public Container getProxyContainer() {
			return proxyContainer;
		}

		/**
		 * @see de.ims.icarus2.model.api.layer.ItemLayer#getIdManager()
		 */
		@Override
		public IdManager getIdManager() {
			return null;
		}

		/**
		 * @see de.ims.icarus2.model.api.layer.Layer#getName()
		 */
		@Override
		public String getName() {
			return ModelUtils.getName(this)+" Overlay Layer"; //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus2.model.api.layer.Layer#getItemProxy()
		 */
		@Override
		public Item getItemProxy() {
			// Not supported by this layer!
			return null;
		}

		/**
		 * @see de.ims.icarus2.model.api.layer.Layer#getContext()
		 */
		@Override
		public Context getContext() {
			return null;
		}

		/**
		 * @see de.ims.icarus2.model.api.layer.Layer#getBaseLayer()
		 */
		@Override
		public DataSet<ItemLayer> getBaseLayers() {
			return DataSet.emptySet();
		}

		@Override
		public void addNotify(LayerGroup owner) {
			throw new ModelException(getCorpus(), GlobalErrorCode.UNSUPPORTED_OPERATION,
					"Cannot add dummy layer to group");
		}

		@Override
		public void removeNotify(LayerGroup owner) {
			throw new ModelException(getCorpus(), GlobalErrorCode.UNSUPPORTED_OPERATION,
					"Cannot remove dummy layer from group");
		}

		@Override
		public boolean isAdded() {
			return true;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.CorpusMember#getCorpus()
		 */
		@Override
		public Corpus getCorpus() {
			return DefaultCorpus.this;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.CorpusMember#getMemberType()
		 */
		@Override
		public MemberType getMemberType() {
			return MemberType.LAYER;
		}

		/**
		 * @see de.ims.icarus2.model.api.layer.ItemLayer#getManifest()
		 */
		@Override
		public ItemLayerManifestBase<?> getManifest() {
			//TODO re-evaluate if null is a good return value here!!!
			return null;
		}

		/**
		 * No boundary on overlay layer!
		 * @see de.ims.icarus2.model.api.layer.ItemLayer#getBoundaryLayer()
		 */
		@Override
		public ItemLayer getBoundaryLayer() {
			return null;
		}

		@Override
		public ItemLayer getFoundationLayer() {
			return null;
		}

		@Override
		public void setBaseLayers(DataSet<ItemLayer> baseLayers) {
			throw new ModelException(getCorpus(), GlobalErrorCode.UNSUPPORTED_OPERATION, "Overlay layer is immutable");
		}

		@Override
		public void setBoundaryLayer(ItemLayer layer) {
			throw new ModelException(getCorpus(), GlobalErrorCode.UNSUPPORTED_OPERATION, "Overlay layer is immutable");
		}

		@Override
		public void setFoundationLayer(ItemLayer layer) {
			throw new ModelException(getCorpus(), GlobalErrorCode.UNSUPPORTED_OPERATION, "Overlay layer is immutable");
		}

		/**
		 * The artificial overlay layer is so far the only layer that is allowed to be
		 * without a hosting layer group!
		 *
		 * FIXME: re-evaluate that situation and maybe introduce a dummy group
		 *
		 * @see de.ims.icarus2.model.api.layer.Layer#getLayerGroup()
		 */
		@Override
		public LayerGroup getLayerGroup() {
			return null;
		}

	}


	private class OverlayContainer extends AbstractImmutableContainer {

		private DefaultCorpus getCorpus0() {
			return DefaultCorpus.this;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#getContainer()
		 */
		@Override
		public Container getContainer() {
			return null;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#getLayer()
		 */
		@Override
		public ItemLayer getLayer() {
			return getCorpus0().overlayLayer;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#getBeginOffset()
		 */
		@Override
		public long getBeginOffset() {
			return IcarusUtils.UNSET_LONG;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#getEndOffset()
		 */
		@Override
		public long getEndOffset() {
			return IcarusUtils.UNSET_LONG;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.CorpusMember#getCorpus()
		 */
		@Override
		public Corpus getCorpus() {
			return getCorpus0();
		}

		/**
		 * @see de.ims.icarus2.model.api.members.container.Container#getContainerType()
		 */
		@Override
		public ContainerType getContainerType() {
			return ContainerType.LIST;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.container.Container#getManifest()
		 */
		@Override
		public ContainerManifest getManifest() {
			return null;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.container.Container#getItemCount()
		 */
		@Override
		public long getItemCount() {
			synchronized (layers) {
				return layers.size();
			}
		}

		/**
		 * @see de.ims.icarus2.model.api.members.container.Container#getItemAt(long)
		 */
		@Override
		public Item getItemAt(long index) {
			synchronized (layers) {
				return layers.get(IcarusUtils.ensureIntegerValueRange(index)).getItemProxy();
			}
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#getIndex()
		 */
		@Override
		public long getIndex() {
			return IcarusUtils.UNSET_LONG;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#getId()
		 */
		@Override
		public long getId() {
			return IcarusUtils.UNSET_LONG;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.container.Container#getBaseContainers()
		 */
		@Override
		public DataSet<Container> getBaseContainers() {
			return null;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.container.Container#getBoundaryContainer()
		 */
		@Override
		public Container getBoundaryContainer() {
			return null;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.container.Container#indexOfItem(de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public long indexOfItem(Item item) {
			requireNonNull(item);

			synchronized (layers) {
				for(int i=layers.size(); --i>=0;) {
					if(layers.get(i).getItemProxy()==item) {
						return i;
					}
				}
			}

			return IcarusUtils.UNSET_LONG;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.container.Container#isItemsComplete()
		 */
		@Override
		public boolean isItemsComplete() {
			return true;
		}

		/**
		 * Returns {@code true} in case the surrounding corpus is currently connected.
		 *
		 * @see de.ims.icarus2.model.api.members.item.Item#isAlive()
		 */
		@Override
		public boolean isAlive() {
			return manager.isCorpusConnected(manifest);
		}

		/**
		 * Returns {@code true} if the surrounding corpus is currently in the process if
		 * being connected or disconnected.
		 *
		 * @see de.ims.icarus2.model.api.members.item.Item#isLocked()
		 */
		@Override
		public boolean isLocked() {
			return manager.isCorpusConnecting(manifest)
					|| manager.isCorpusDisconnecting(manifest);
		}

		/**
		 * Returns {@code true} if the surrounding corpus has been marked as
		 * {@link CorpusManager#isBadCorpus(CorpusManifest) bad}.
		 *
		 * @see de.ims.icarus2.model.api.members.item.Item#isDirty()
		 */
		@Override
		public boolean isDirty() {
			return manager.isBadCorpus(manifest);
		}
	}

	@Api(type=ApiType.BUILDER) //TODO add builder annotations and edge case tests
	public static class Builder extends AbstractBuilder<Builder, Corpus> {
		private CorpusManager manager;
		private CorpusManifest manifest;
		private MetadataRegistry metadataRegistry;

		protected Builder() {
			// no-op
		}

		public Builder manager(CorpusManager manager) {
			requireNonNull(manager);
			checkState(this.manager==null);

			this.manager = manager;

			return thisAsCast();
		}

		public CorpusManager getManager() {
			return manager;
		}

		public Builder manifest(CorpusManifest manifest) {
			requireNonNull(manifest);
			checkState(this.manifest==null);

			this.manifest = manifest;

			return thisAsCast();
		}

		public CorpusManifest getManifest() {
			return manifest;
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

		@Override
		protected void validate() {
			checkState("Missing manager", manager!=null);
			checkState("Missing manifest", manifest!=null);
			checkState("Missing metadata registry", metadataRegistry!=null);
		}

		@Override
		public DefaultCorpus create() {
			return new DefaultCorpus(this);
		}
	}
}
