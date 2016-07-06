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
 *
 */
package de.ims.icarus2.model.standard.corpus;

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static de.ims.icarus2.model.util.ModelUtils.getUniqueId;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotNull;
import static de.ims.icarus2.util.Conditions.checkState;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.TCustomHashSet;
import gnu.trove.set.hash.THashSet;
import gnu.trove.strategy.IdentityHashingStrategy;

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

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Context.VirtualContext;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.corpus.CorpusAccessMode;
import de.ims.icarus2.model.api.corpus.CorpusOption;
import de.ims.icarus2.model.api.corpus.CorpusView;
import de.ims.icarus2.model.api.corpus.Scope;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.edit.CorpusEditManager;
import de.ims.icarus2.model.api.edit.CorpusUndoManager;
import de.ims.icarus2.model.api.events.CorpusEventManager;
import de.ims.icarus2.model.api.events.CorpusListener;
import de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.meta.MetaData;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.registry.CorpusMemberFactory;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.ImplementationLoader;
import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.model.standard.members.container.AbstractImmutableContainer;
import de.ims.icarus2.model.standard.registry.ContextFactory;
import de.ims.icarus2.model.standard.view.DefaultCorpusView;
import de.ims.icarus2.model.standard.view.DefaultCorpusView.CorpusViewBuilder;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.AccumulatingException;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.collections.set.DataSet;
import de.ims.icarus2.util.data.ContentType;
import de.ims.icarus2.util.events.EventListener;
import de.ims.icarus2.util.events.EventObject;
import de.ims.icarus2.util.events.Events;

/**
 * Implements a corpus that manages its contents in a lazy way. {@link Context} instances
 * and their {@link Driver drivers} will be created and linked when they are first being used.
 * <p>
 * When a corpus consists of multiple contexts and there exist dependencies between several
 * contexts, then creation and initialization of context instances will be performed in such a
 * way, that a context gets fully initialized only <b>after</b> all the other contexts it is
 * depending on are created and connected with their respective drivers.
 * Note that the actual instantiation and linking of contexts, {@link LayerGroup groups} and
 * {@link Layer layers} is delegated to a new {@link ContextFactory} object for each context.
 * This factory implementation honors the ability of drivers to provide custom implementations
 * of groups or layers (or {@link AnnotationStorage annotation storages}) if they do wish so.
 * <p>
 * Due to the lazy creation of both contexts and drivers it is perfectly legal to encounter a
 * driver whose context has not yet been created. Instantiation of contexts and their drivers
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
public class DefaultCorpus implements Corpus {

	private final CorpusManager manager;
	private final CorpusManifest manifest;
	private final MetadataRegistry metadataRegistry;

	private final CorpusEventManager corpusEventManager = new CorpusEventManager(this);
	private final CorpusEditManager editModel = new CorpusEditManager(this);
	private final CorpusUndoManager undoManager = new CorpusUndoManager(this);


	private final Lock lock = new ReentrantLock();

	// All contained layers, not including the overlay layer!
	private final List<Layer> layers = new ArrayList<>();
	private final Map<String, Layer> layerLookup = new THashMap<>();

	private final OverlayLayer overlayLayer;
	private final OverlayContainer overlayContainer;

	private final ManifestTracker manifestTracker;

	private final ContextProxy rootContext;
	/**
	 *  Proxies for all custom contexts, preserves insertion order.
	 *
	 *  NOTE:
	 *  The content of this map is effectively static, since it will be
	 *  created and populated at construction time of this corpus. Therefore
	 *  no synchronization is required when accessing it!
	 */
	private final Map<String, ContextProxy> contexts = new LinkedHashMap<>();
	private final Map<Layer, MetaDataStorage> metaDataStorages = new THashMap<>();
	private final Map<String, VirtualContext> virtualContexts = new LinkedHashMap<>();

	private final ViewStorage viewStorage = new ViewStorage();

	private AtomicBoolean closing = new AtomicBoolean(false);

	protected DefaultCorpus(CorpusBuilder builder) {
		checkNotNull(builder);

		this.manager = builder.getManager();
		this.manifest = builder.getManifest();
		this.metadataRegistry = builder.getMetadataRegistry();

		overlayLayer = new OverlayLayer();
		overlayContainer = new OverlayContainer();

		ContextProxy rootContext = null;

		for(ContextManifest context : manifest.getCustomContextManifests()) {

			ContextProxy proxy = new ContextProxy(context);
			contexts.put(context.getId(), proxy);

			if(manifest.isRootContext(context)) {
				rootContext = proxy;
			}
		}

		if(rootContext==null)
			throw new ModelException(ManifestErrorCode.MANIFEST_CORRUPTED_STATE,
					"No root context declared for corpus: "+getName(manifest));

		this.rootContext = rootContext;

		manifestTracker = new ManifestTracker();
		manifest.getRegistry().addListener(Events.ADDED, manifestTracker);
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
	public CorpusView createView(Scope scope, IndexSet[] indices,
			CorpusAccessMode mode, Options options) throws InterruptedException {

		checkNotNull(scope);
		checkNotNull(mode);

		checkArgument("Scope refers to foreign corpus", scope.getCorpus()==this);

		if(options==null) {
			options = Options.emptyOptions;
		}

		ItemLayer primaryLayer = scope.getPrimaryLayer();
		Driver driver = primaryLayer.getContext().getDriver();

		if(indices==null) {
			long itemCount = driver.getItemCount(primaryLayer);

			if(itemCount==ModelConstants.NO_INDEX)
				throw new ModelException(this, ModelErrorCode.DRIVER_METADATA,
						"Cannot create default index set for entire primary layer, since driver has no metadata for its size");

			indices = IndexUtils.wrap(0L, itemCount-1);
		}

		int pageSize = options.getInteger(CorpusOption.PARAM_VIEW_PAGE_SIZE, -1);

		if(pageSize==-1) {
			pageSize = CorpusOption.DEFAULT_VIEW_PAGE_SIZE;
		}

		// TODO determine page size and fetch the correct ItemLayerManager, then create DefaultCorpusView instance
		return new CorpusViewBuilder()
			.accessMode(mode)
			.scope(scope)
			.indices(indices)
			.itemLayerManager(driver)
			.pageSize(pageSize)
			.build();
	}

	@Override
	public void forEachView(Consumer<? super CorpusView> action) {
		synchronized (viewStorage) {
			viewStorage.forEachView(action);
		}
	}

	private ContextProxy getContextProxy(String id) {
		checkNotNull(id);

		ContextProxy proxy = null;

		if(rootContext.getId().equals(id)) {
			proxy = rootContext;
		} else {
			proxy = contexts.get(id);
		}

		if(proxy==null)
			throw new ModelException(this, ManifestErrorCode.MANIFEST_UNKNOWN_ID,
					"Unknown context id: "+id);

		return proxy;
	}

	private void addContext(ContextManifest context) {
		checkNotNull(context);

		Lock lock = getLock();

		lock.lock();
		try {
			synchronized (contexts) {
				String id = context.getId();
				if(contexts.containsKey(id))
					throw new ModelException(this, ManifestErrorCode.MANIFEST_DUPLICATE_ID,
							"Duplicate context: "+id);

				contexts.put(id, new ContextProxy(context));
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

			// Close views first
			try {
				viewStorage.close();
			} catch (AccumulatingException e) {
				/*
				 *  Anti-pattern: destroys info from the caught AccumulatingException itself.
				 *  This is acceptable since this is the only code that uses the above close() method
				 *  and we are only interested in the wrapped exceptions!
				 */
				buffer.addExceptionsFrom(e);
			}

			// Close contexts and their drivers now
			for(ContextProxy proxy : contexts.values()) {
				try {
					proxy.close();
				} catch (InterruptedException e) {
					throw e;
				} catch (Exception e) {
					buffer.addException(e);
				}
			}

			try {
				rootContext.close();
			} catch (InterruptedException e) {
				throw e;
			} catch (Exception e) {
				buffer.addException(e);
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
	 * @see de.ims.icarus2.model.api.corpus.Corpus#getUndoManager()
	 */
	@Override
	public CorpusUndoManager getUndoManager() {
		return undoManager;
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.Corpus#getRootContext()
	 */
	@Override
	public Context getRootContext() {
		return rootContext.getContext();
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

	@Override
	public void forEachContext(Consumer<? super Context> action) {
		checkNotNull(action);
		synchronized (contexts) {
			contexts.values().forEach(c -> action.accept(c.getContext()));
		}
	}

	@Override
	public void addVirtualContext(VirtualContext context) {
		checkNotNull(context);

		synchronized (virtualContexts) {
			String id = context.getManifest().getId();

			if(virtualContexts.containsKey(id))
				throw new ModelException(this, ManifestErrorCode.MANIFEST_DUPLICATE_ID,
						"Duplicate context id: "+id);

			virtualContexts.put(id, context);

			corpusEventManager.fireContextAdded(context);
		}
	}

	@Override
	public void removeVirtualContext(VirtualContext context) {
		checkNotNull(context);

		synchronized (virtualContexts) {
			String id = context.getManifest().getId();

			if(!virtualContexts.remove(id, context))
				throw new ModelException(this, GlobalErrorCode.INVALID_INPUT,
						"Unknown context: "+id);

			corpusEventManager.fireContextRemoved(context);
		}
	}

	@Override
	public VirtualContext getVirtualContext(String id) {
		checkNotNull(id);

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
		checkNotNull(action);
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
		String id = context.getManifest().getId();

		if(id.equals(rootContext.getId())) {
			return true;
		}

		synchronized (contexts) {
			if(contexts.containsKey(id)) {
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
		checkNotNull(layer);

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

	private final class ManifestTracker implements EventListener {

		/**
		 * @see de.ims.icarus2.util.events.EventListener#invoke(java.lang.Object, de.ims.icarus2.util.events.EventObject)
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
	 * Storage class for currently active corpus views.
	 * All method in this class should be called under synchronization
	 * on the current instance.
	 *
	 * @author Markus Gärtner
	 *
	 */
	private final class ViewStorage implements ChangeListener {
		private CorpusView currentWriteView;
		private final Set<CorpusView> readViews = new TCustomHashSet<>(IdentityHashingStrategy.INSTANCE);

		public boolean canOpen(CorpusAccessMode accessMode) {
			return !accessMode.isWrite() || currentWriteView==null;
		}

		public void addView(CorpusView view) {
			CorpusAccessMode accessMode = view.getAccessMode();
			Corpus corpus = DefaultCorpus.this;

			// Sanity check
			if(accessMode.isWrite() && currentWriteView!=null)
				throw new ModelException(corpus, ModelErrorCode.VIEW_ERROR,
						"Cannot hold open more than 1 writing view for corpus: "+getName(corpus));

			// Now add and register listener
			if(view instanceof DefaultCorpusView) {
				((DefaultCorpusView)view).addChangeListener(this);
			}

			if(accessMode.isWrite()) {
				currentWriteView = view;
			} else {
				readViews.add(view);
			}
		}

		public Set<CorpusView> getViews() {
			LazyCollection<CorpusView> snapshot = LazyCollection.lazySet();

			snapshot.addAll(readViews);
			if(currentWriteView!=null) {
				snapshot.add(currentWriteView);
			}

			return snapshot.getAsSet();
		}

		public void close() throws InterruptedException, AccumulatingException {
			Set<CorpusView> snapshot = getViews();

			if(snapshot.isEmpty()) {
				return;
			}

			AccumulatingException.Buffer buffer = new AccumulatingException.Buffer();

			for(CorpusView view : snapshot) {
				try {
					view.close();
					view.removeNotify(DefaultCorpus.this);
				} catch(InterruptedException e) {
					// Need to catch and re-throw here to allow the more general catch phrase below
					throw e;
				} catch(Exception e) {
					buffer.addException(e);
				}
			}

			if(!buffer.isEmpty()) {
				buffer.setFormattedMessage("Failed to close %d views");
				throw new AccumulatingException(buffer);
			}
		}

		public void forEachView(Consumer<? super CorpusView> action) {
			if(currentWriteView!=null) {
				action.accept(currentWriteView);
			}

			readViews.forEach(action);
		}

		private void removeView(CorpusView view) {
			view.removeChangeListener(this);

			if(view==currentWriteView) {
				currentWriteView = null;
			} else {
				readViews.remove(view);
			}
		}

		/**
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			CorpusView view = (CorpusView) e.getSource();
			if(!view.isActive()) {
				synchronized (this) {
					removeView(view);
				}
			}
		}
	}

	private final class ContextProxy {
		private final ContextManifest manifest;
		private Driver driver;

		public ContextProxy(ContextManifest manifest) {
			checkNotNull(manifest);

			this.manifest = manifest;
		}

		public String getId() {
			return manifest.getId();
		}

		public ContextManifest getManifest() {
			return manifest;
		}

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
							.manifest(manifest.getDriverManifest().getImplementationManifest())
							.environment(corpus)
							.message("Driver for context '"+getName(manifest)+"'")
							.instantiate(Driver.class);

					driver.connect(corpus);

					corpusEventManager.fireContextAdded(driver.getContext());
				} catch (InterruptedException e) {
					throw new ModelException(corpus, ModelErrorCode.DRIVER_CONNECTION,
							"Initalization of driver cancelled by user: "+getName(manifest), e);
				} finally {
					lock.unlock();
				}
			}

			return driver;
		}

		public synchronized void close() throws InterruptedException {

			if(driver!=null) {

				final Corpus corpus = DefaultCorpus.this;

				Context context = driver.getContext();

				driver.disconnect(corpus);

				corpusEventManager.fireContextRemoved(context);
			}
		}
	}

	private static class MetaDataStorage {
		private final Map<ContentType, Set<MetaData>> content = new HashMap<>();

		private Set<MetaData> getEntry(ContentType type, boolean createIfMissing) {
			Set<MetaData> result = content.get(type);

			if(result==null && createIfMissing) {
				result = new THashSet<>();
				content.put(type, result);
			}

			return result;
		}

		public boolean isEmpty() {
			return content.isEmpty();
		}

		public void add(ContentType type, MetaData data) {
			checkNotNull(type);
			checkNotNull(data);

			getEntry(type, true).add(data);
		}

		public void remove(ContentType type, MetaData data) {
			checkNotNull(type);
			checkNotNull(data);

			Set<MetaData> entry = getEntry(type, false);
			if(entry!=null) {
				entry.remove(data);
			}
		}

		public void forEachEntry(Layer layer, BiPredicate<Layer, ContentType> filter, Consumer<MetaData> action) {
			checkNotNull(filter);
			checkNotNull(action);

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

//		//TODO call uid creation at this point or do it lazily?
//		private final int uid = getManifest().getRegistry().createUID();

		/**
		 * @see de.ims.icarus2.model.api.layer.Layer#getName()
		 */
		@Override
		public String getName() {
			return getCorpus().getManifest().getName()+" Overlay Layer"; //$NON-NLS-1$
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
		public ItemLayerManifest getManifest() {
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

//		@Override
//		public int getUID() {
//			return uid;
//		}

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
			return -1;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#getEndOffset()
		 */
		@Override
		public long getEndOffset() {
			return -1;
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
			return -1;
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
			checkNotNull(item);

			synchronized (layers) {
				for(int i=layers.size(); --i>=0;) {
					if(layers.get(i).getItemProxy()==item) {
						return i;
					}
				}
			}

			return NO_INDEX;
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

	public static class CorpusBuilder extends AbstractBuilder<CorpusBuilder, Corpus> {
		private CorpusManager manager;
		private CorpusManifest manifest;
		private MetadataRegistry metadataRegistry;

		public CorpusBuilder manager(CorpusManager manager) {
			checkNotNull(manager);
			checkState(this.manager==null);

			this.manager = manager;

			return thisAsCast();
		}

		public CorpusManager getManager() {
			return manager;
		}

		public CorpusBuilder manifest(CorpusManifest manifest) {
			checkNotNull(manifest);
			checkState(this.manifest==null);

			this.manifest = manifest;

			return thisAsCast();
		}

		public CorpusManifest getManifest() {
			return manifest;
		}

		public CorpusBuilder metadataRegistry(MetadataRegistry metadataRegistry) {
			checkNotNull(metadataRegistry);
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
