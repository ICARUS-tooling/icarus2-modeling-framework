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
package de.ims.icarus2.model.standard.registry;

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static de.ims.icarus2.util.Conditions.checkNotNull;
import static de.ims.icarus2.util.Conditions.checkState;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Predicate;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.events.CorpusLifecycleListener;
import de.ims.icarus2.model.api.io.FileManager;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.registry.CorpusMemberFactory;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.api.registry.MetadataStoragePolicy;
import de.ims.icarus2.model.api.registry.SubRegistry;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.standard.corpus.DefaultCorpus;
import de.ims.icarus2.util.events.EventObject;
import de.ims.icarus2.util.id.Identity;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultCorpusManager implements CorpusManager {

	private final ManifestRegistry manifestRegistry;
	private final MetadataRegistry metadataRegistry;
	private final FileManager fileManager;

	private volatile Function<CorpusManifest, Corpus> corpusProducer;

	/**
	 * Current state of corpora. Must never contain corpora that are 'only' enabled!
	 */
	private final Map<CorpusManifest, CorpusManager.CorpusState> states = new HashMap<>();

	/**
	 * Mapping of corpora that are truly live (i.e. properly connected)
	 */
	private final Map<CorpusManifest, Corpus> liveCorpora = new HashMap<>();

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	private MetadataStoragePolicy<CorpusManifest> corpusMetadataPolicy;
	private MetadataStoragePolicy<ContextManifest> contextMetadataPolicy;

	private final List<CorpusLifecycleListener> lifecycleListeners = new CopyOnWriteArrayList<>();

	private final Map<String, String> properties = new Object2ObjectOpenHashMap<>();

	//TODO add config parameter that allows passing of stuff like path to a new properties file, or a default fallback set of properties, etc...
	public DefaultCorpusManager(ManifestRegistry registry, MetadataRegistry metadataRegistry, FileManager fileManager) {
		checkNotNull(registry);
		checkNotNull(metadataRegistry);

		this.manifestRegistry = registry;
		this.metadataRegistry = metadataRegistry;
		this.fileManager = fileManager;

		// Read in default properties
		Properties defaultProperties = new Properties();
		try {
			defaultProperties.load(DefaultCorpusManager.class.getResourceAsStream("default-properties.ini"));
		} catch (IOException e) {
			throw new ModelException(GlobalErrorCode.INTERNAL_ERROR, "Failed to load default properties", e);
		}

		// Copy over default properties
		for(String key : properties.keySet()) {
			properties.put(key, defaultProperties.getProperty(key));
		}
	}

	/**
	 * This implementation first tries to fetch the {@link System#getProperty(String) system property} for the given
	 * {@code key} and if no such property has been defined accesses the internal default properties.
	 *
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String key) {
		checkNotNull(key);

		String value = System.getProperty(key);

		if(value==null) {
			value = properties.get(key);
		}

		return value;
	}

	@Override
	public Collection<String> availableExtensions(String extensionPointUid) {
		return Collections.emptySet();
	}

	@Override
	public Class<?> resolveExtension(String extensionUid) throws ClassNotFoundException {
		throw new ClassNotFoundException("Implementation does not support plugin lookup for extension: "+extensionUid);
	}

	@Override
	public ClassLoader getPluginClassLoader(String pluginUid) {
		throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
				"Implementation does not support class loader lookup for plugin: "+pluginUid);
	}

	@Override
	public Identity getExtensionIdentity(String extensionuid) {
		throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
				"Implementation does not support identity lookup for extension: "+extensionuid);
	}

	public final boolean isCurrentThreadWriting() {
		return lock.isWriteLockedByCurrentThread();
	}

	protected final void tryStartWrite() {
		if(isCurrentThreadWriting())
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					"Thread is not allowed to perform nested write actions");
	}

	protected final Lock readLock() {
		return lock.readLock();
	}

	protected final Lock writeLock() {
		return lock.writeLock();
	}

	@Override
	public CorpusMemberFactory newFactory() {
		return new DefaultCorpusMemberFactory(this);
	}

	public void setCorpusProducer(Function<CorpusManifest, Corpus> corpusProducer) {
		checkNotNull(corpusProducer);
		checkState(this.corpusProducer==null);

		this.corpusProducer = corpusProducer;
	}

	protected boolean isLegalChange(EventObject event) {
		//TODO verify that the event is not related to any live or currently changing corpus
		return true;
	}

	/**
	 * Checks that the given {@code manifest} is managed by the {@link manifestRegistry manifestRegistry}
	 * this manager is linked to and then looks up the current state of it, assigning
	 * {@link CorpusState#ENABLED enabled} in case there is no other state stored for it.
	 * <p>
	 * <b>Attention:</b> This method must be called under read lock to ensure proper synchronization!
	 *
	 * @param manifest
	 * @return
	 */
	protected final CorpusManager.CorpusState getStateUnsafe(CorpusManifest manifest, boolean allowBadState) {
		checkNotNull(manifest);
		if(manifest.getRegistry()!=manifestRegistry)
			throw new ModelException(ManifestErrorCode.MANIFEST_ERROR,
					"Foreign manifest: "+getName(manifest));

		CorpusManager.CorpusState state = states.get(manifest);

		if(state==CorpusManager.CorpusState.BAD && !allowBadState)
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					"Bad corpus: "+getName(manifest));

		if(state==null) {
			state = CorpusManager.CorpusState.ENABLED;
		}

		return state;
	}

	/**
	 *
	 * <b>Attention:</b> This method must be called under write lock to ensure proper synchronization!
	 *
	 * @param manifest
	 * @param state
	 * @return
	 */
	protected final CorpusManager.CorpusState setStateUnsafe(CorpusManifest manifest, CorpusManager.CorpusState state) {
		checkNotNull(manifest);
		checkNotNull(state);

		CorpusManager.CorpusState oldState = states.get(manifest);

		if(oldState==null) {
			oldState = CorpusManager.CorpusState.ENABLED;
		}

		if(!state.isValidPrecondition(oldState))
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					String.format("Corpus %s cannot be set to state %s while currently being %s",
							getName(manifest), state, oldState));

		if(state==CorpusManager.CorpusState.ENABLED) {
			states.remove(manifest);
		} else {
			states.put(manifest, state);
		}

		return oldState;
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#getManifestRegistry()
	 */
	@Override
	public ManifestRegistry getManifestRegistry() {
		return manifestRegistry;
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#getMetadataRegistry()
	 */
	@Override
	public MetadataRegistry getMetadataRegistry() {
		return metadataRegistry;
	}

	@Override
	public FileManager getFileManager() {
		return fileManager;
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#getCorpusMetadataPolicy()
	 */
	@Override
	public MetadataStoragePolicy<CorpusManifest> getCorpusMetadataPolicy() {
		MetadataStoragePolicy<CorpusManifest> result = corpusMetadataPolicy;

		if(result==null) {
			result = CorpusManager.super.getCorpusMetadataPolicy();
		}

		return result;
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#getContextMetadataPolicy()
	 */
	@Override
	public MetadataStoragePolicy<ContextManifest> getContextMetadataPolicy() {
		MetadataStoragePolicy<ContextManifest> result = contextMetadataPolicy;

		if(result==null) {
			result = CorpusManager.super.getContextMetadataPolicy();
		}

		return result;
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#connect(de.ims.icarus2.model.manifest.api.CorpusManifest)
	 */
	@Override
	public Corpus connect(CorpusManifest manifest) throws InterruptedException {
		checkNotNull(manifest);

		tryStartWrite();

		Corpus corpus = null;

		writeLock().lock();
		try {
			CorpusManager.CorpusState oldState = getStateUnsafe(manifest, false);

			// Immediately return in case the corpus is already connected (do not notify listeners!!!)
			if(oldState==CorpusManager.CorpusState.CONNECTED) {
				return liveCorpora.get(manifest);
			}

			// If currently disconnecting, signal the client to wait and try again later
			if(oldState==CorpusManager.CorpusState.DISCONNECTING) {
				return null;
			}

			// First transition and notification
			setStateUnsafe(manifest, CorpusManager.CorpusState.CONNECTING);
			fireCorpusChanged(manifest);

			// Now instantiate new corpus

			/*
			 * We need a special marker to track cancellation of the loading process.
			 * Since it is perfectly legal for the user to cancel loading, we must not
			 * mark the corpus as bad once that happens.
			 */
			boolean cancelled = false;

			try {
				corpus = createCorpusFromManifest(manifest);
			} catch(InterruptedException e) {
				// Mark process as cancelled and rethrow
				cancelled = true;
				throw e;
			} finally {

				// Corpus being null means the instantiation failed -> mark as bad and notify
				if(corpus==null && !cancelled) {
					setStateUnsafe(manifest, CorpusManager.CorpusState.BAD);
					fireCorpusChanged(manifest);
				}

				// In case of cancellation this is as far as execution goes, since now the InterruptedException gets passed up the stack
			}

			// Everything went smooth -> set live state and notify

			setStateUnsafe(manifest, CorpusManager.CorpusState.CONNECTED);
			fireCorpusConnected(corpus);

		} finally {
			writeLock().unlock();
		}

		return corpus;
	}

	private Corpus createCorpusFromManifest(CorpusManifest manifest) throws InterruptedException {

		Corpus corpus = null;

		Function<CorpusManifest, Corpus> corpusProducer = this.corpusProducer;
		if(corpusProducer!=null) {
			corpus = corpusProducer.apply(manifest);
		} else {
			corpus = instantiate(manifest);
		}

		if(corpus==null)
			throw new ModelException(GlobalErrorCode.DELEGATION_FAILED,
					"Corpus generation yielded null result");

		return corpus;
	}

	/**
	 * Creates a new live corpus instance based on the given {@code CorpusManifest manifest}.
	 * This method must not return {@code null}!
	 * <p>
	 * Note that this implementation uses the internal {@link MetadataRegistry} instance that
	 * was set at creation time and creates a new {@link SubRegistry sub registry} with the
	 * {@link Manifest#getId() id} of the given {@link CorpusManifest manifest} as prefix.
	 * <p>
	 * Actual instantiation will then be delegated to a new {@link DefaultCorpus.CorpusBuilder}.
	 *
	 * @param manifest
	 * @return
	 * @throws InterruptedException
	 */
	protected Corpus instantiate(CorpusManifest manifest) throws InterruptedException {

		return new DefaultCorpus.CorpusBuilder()
			.manager(this)
			.manifest(manifest)
			.metadataRegistry(new SubRegistry(metadataRegistry, manifest.getId()))
			.build();
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#disconnect(de.ims.icarus2.model.manifest.api.CorpusManifest)
	 */
	@Override
	public void disconnect(CorpusManifest manifest) throws InterruptedException {
		checkNotNull(manifest);

		tryStartWrite();

		writeLock().lock();
		try {
			// For internal sanity checks
			getStateUnsafe(manifest, false);

			// First transition and notification
			setStateUnsafe(manifest, CorpusManager.CorpusState.DISCONNECTING);
			fireCorpusChanged(manifest);

			// Now destroy corpus

			Corpus corpus = liveCorpora.get(manifest);

			try {
				destroy(corpus);
				// Reset to null is indicator for successful destruction
				corpus = null;
			} finally {

				// Corpus not being null means destruction failed -> mark as bad and notify
				if(corpus!=null) {
					setStateUnsafe(manifest, CorpusManager.CorpusState.BAD);
					fireCorpusChanged(manifest);
				}
			}

			// Everything went smooth -> remove corpus, set state and notify

			liveCorpora.remove(manifest);
			setStateUnsafe(manifest, CorpusManager.CorpusState.ENABLED);
			fireCorpusDisconnected(manifest);

		} finally {
			writeLock().unlock();
		}
	}

	protected void destroy(Corpus corpus) throws InterruptedException {
		//TODO close corpus
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#shutdown()
	 */
	@Override
	public void shutdown() throws InterruptedException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#getLiveCorpus(de.ims.icarus2.model.manifest.api.CorpusManifest)
	 */
	@Override
	public Corpus getLiveCorpus(CorpusManifest manifest) {
		checkNotNull(manifest);

		readLock().lock();
		try {
			Corpus corpus = null;
			CorpusManager.CorpusState state = getStateUnsafe(manifest, true);

			if(state==CorpusManager.CorpusState.CONNECTED) {
				corpus = liveCorpora.get(manifest);
			}

			return corpus;
		} finally {
			readLock().unlock();
		}
	}

	protected final boolean isCorpusInState(CorpusManifest corpus, CorpusManager.CorpusState state) {
		return isCorpusInState(corpus, state::equals);
	}

	protected final boolean isCorpusInState(CorpusManifest corpus, Predicate<? super CorpusManager.CorpusState> p) {
		checkNotNull(corpus);
		checkNotNull(p);

		readLock().lock();
		try {
			CorpusManager.CorpusState currentState = getStateUnsafe(corpus, true);

			return p.test(currentState);
		} finally {
			readLock().unlock();
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#isCorpusConnected(de.ims.icarus2.model.manifest.api.CorpusManifest)
	 */
	@Override
	public boolean isCorpusConnected(CorpusManifest corpus) {
		return isCorpusInState(corpus, CorpusManager.CorpusState.CONNECTED);
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#isCorpusConnecting(de.ims.icarus2.model.manifest.api.CorpusManifest)
	 */
	@Override
	public boolean isCorpusConnecting(CorpusManifest corpus) {
		return isCorpusInState(corpus, CorpusManager.CorpusState.CONNECTING);
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#isCorpusDisconnecting(de.ims.icarus2.model.manifest.api.CorpusManifest)
	 */
	@Override
	public boolean isCorpusDisconnecting(CorpusManifest corpus) {
		return isCorpusInState(corpus, CorpusManager.CorpusState.DISCONNECTING);
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#isCorpusEnabled(de.ims.icarus2.model.manifest.api.CorpusManifest)
	 */
	@Override
	public boolean isCorpusEnabled(CorpusManifest corpus) {
		return isCorpusInState(corpus, CorpusManager.CorpusState.ENABLED);
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#isBadCorpus(de.ims.icarus2.model.manifest.api.CorpusManifest)
	 */
	@Override
	public boolean isBadCorpus(CorpusManifest corpus) {
		return isCorpusInState(corpus, CorpusManager.CorpusState.BAD);
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#enableCorpus(de.ims.icarus2.model.manifest.api.CorpusManifest)
	 */
	@Override
	public boolean enableCorpus(CorpusManifest corpus) {
		tryStartWrite();

		writeLock().lock();
		try {
			// Read state, automatically applying sanity checks
			final CorpusManager.CorpusState currentState = getStateUnsafe(corpus, false);

			if(currentState==CorpusManager.CorpusState.ENABLED) {
				return false;
			}

			setStateUnsafe(corpus, CorpusManager.CorpusState.ENABLED);

			fireCorpusEnabled(corpus);

			return true;
		} finally {
			writeLock().unlock();
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#disableCorpus(de.ims.icarus2.model.manifest.api.CorpusManifest)
	 */
	@Override
	public boolean disableCorpus(CorpusManifest corpus) {
		tryStartWrite();

		writeLock().lock();
		try {
			// Read state, automatically applying sanity checks
			final CorpusManager.CorpusState currentState = getStateUnsafe(corpus, false);

			if(currentState==CorpusManager.CorpusState.DISABLED) {
				return false;
			}

			setStateUnsafe(corpus, CorpusManager.CorpusState.DISABLED);

			fireCorpusDisabled(corpus);

			return true;
		} finally {
			writeLock().unlock();
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#resetBadCorpus(de.ims.icarus2.model.manifest.api.CorpusManifest)
	 */
	@Override
	public boolean resetBadCorpus(CorpusManifest corpus) {
		tryStartWrite();

		writeLock().lock();
		try {
			// Read state, automatically applying sanity checks
			final CorpusManager.CorpusState currentState = getStateUnsafe(corpus, true);

			if(currentState==CorpusManager.CorpusState.ENABLED) {
				return false;
			}

			if(currentState!=CorpusManager.CorpusState.BAD)
				throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
						"Cannot reset corpus if not marked as 'bad': "+getName(corpus));

			setStateUnsafe(corpus, CorpusManager.CorpusState.ENABLED);

			fireCorpusChanged(corpus);

			return true;
		} finally {
			writeLock().unlock();
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#addCorpusLifecycleListener(de.ims.icarus2.model.api.events.CorpusLifecycleListener)
	 */
	@Override
	public void addCorpusLifecycleListener(CorpusLifecycleListener listener) {
		checkNotNull(listener);

		lifecycleListeners.remove(listener);
		lifecycleListeners.add(listener);
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#removeCorpusLifecycleListener(de.ims.icarus2.model.api.events.CorpusLifecycleListener)
	 */
	@Override
	public void removeCorpusLifecycleListener(CorpusLifecycleListener listener) {
		checkNotNull(listener);

		lifecycleListeners.remove(listener);
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#getLiveCorpora()
	 */
	@Override
	public Collection<CorpusManifest> getLiveCorpora() {
		readLock().lock();
		try {
			return new ArrayList<>(liveCorpora.keySet());
		} finally {
			readLock().unlock();
		}
	}

	@Override
	public Collection<CorpusManifest> getCorpora(Predicate<? super CorpusManifest> p) {
		checkNotNull(p);

		readLock().lock();
		try {
			Collection<CorpusManifest> corpora = states.keySet();

			if(corpora.isEmpty()) {
				return Collections.emptyList();
			}

			Collection<CorpusManifest> result = null;

			int visited = 0;
			for(CorpusManifest corpus : corpora) {
				if(p.test(corpus)) {
					// Create result buffer lazily only in case we really need it
					if(result==null) {
						result = new ArrayList<>(corpora.size()-visited);
					}
					result.add(corpus);
				}
				visited++;
			}

			if(result==null) {
				result = Collections.emptyList();
			}

			return result;
		} finally {
			readLock().unlock();
		}
	}

	@Override
	public Collection<CorpusManifest> getCorpora(CorpusState state) {
		checkNotNull(state);

		readLock().lock();
		try {
			Set<Entry<CorpusManifest, CorpusState>> entries = states.entrySet();

			if(entries.isEmpty()) {
				return Collections.emptyList();
			}

			Collection<CorpusManifest> result = null;

			int visited = 0;
			for(Entry<CorpusManifest, CorpusState> entry : entries) {
				if(entry.getValue()==state) {
					// Create result buffer lazily only in case we really need it
					if(result==null) {
						result = new ArrayList<>(entries.size()-visited);
					}
					result.add(entry.getKey());
				}
				visited++;
			}

			if(result==null) {
				result = Collections.emptyList();
			}

			return result;
		} finally {
			readLock().unlock();
		}
	}

	protected final void fireCorpusChanged(CorpusManifest manifest) {
		for (CorpusLifecycleListener listener : lifecycleListeners) {
			listener.corpusChanged(this, manifest);
		}
	}

	protected final void fireCorpusConnected(Corpus corpus) {
		for (CorpusLifecycleListener listener : lifecycleListeners) {
			listener.corpusConnected(this, corpus);
		}
	}

	protected final void fireCorpusDisconnected(CorpusManifest manifest) {
		for (CorpusLifecycleListener listener : lifecycleListeners) {
			listener.corpusDisconnected(this, manifest);
		}
	}

	protected final void fireCorpusEnabled(CorpusManifest manifest) {
		for (CorpusLifecycleListener listener : lifecycleListeners) {
			listener.corpusEnabled(this, manifest);
		}
	}

	protected final void fireCorpusDisabled(CorpusManifest manifest) {
		for (CorpusLifecycleListener listener : lifecycleListeners) {
			listener.corpusDisconnected(this, manifest);
		}
	}
}
