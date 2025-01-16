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
package de.ims.icarus2.model.standard.registry;

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.lang.Primitives._int;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.StampedLock;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.apiguard.Api;
import de.ims.icarus2.apiguard.Api.ApiType;
import de.ims.icarus2.apiguard.Guarded;
import de.ims.icarus2.apiguard.Guarded.MethodType;
import de.ims.icarus2.apiguard.Mandatory;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.events.CorpusLifecycleListener;
import de.ims.icarus2.model.api.io.FileManager;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.registry.CorpusMemberFactory;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.api.registry.MetadataStoragePolicy;
import de.ims.icarus2.model.api.registry.SubRegistry;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.standard.DefaultManifestRegistry;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.model.standard.corpus.DefaultCorpus;
import de.ims.icarus2.model.standard.io.DefaultFileManager;
import de.ims.icarus2.model.standard.registry.metadata.VirtualMetadataRegistry;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.AccumulatingException;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.events.EventObject;
import de.ims.icarus2.util.id.Identity;
import de.ims.icarus2.util.io.resource.FileResourceProvider;
import de.ims.icarus2.util.io.resource.ResourceProvider;

/**
 * Default implementation of the central management component.
 * <p>
 * Since it provides quite a few opportunities for customization,
 * construction is again realized via the builder pattern.
 *
 * @author Markus Gärtner
 *
 */
@TestableImplementation(CorpusManager.class)
public class DefaultCorpusManager implements CorpusManager {

	public static Builder builder() {
		return new Builder();
	}

	private final ManifestRegistry manifestRegistry;
	private final MetadataRegistry metadataRegistry;
	private final FileManager fileManager;
	private final ResourceProvider resourceProvider;

	private final BiFunction<CorpusManager, CorpusManifest, Corpus> corpusProducer;

	/**
	 * Current state of corpora. Must never contain corpora that are 'only' enabled!
	 */
	private final Map<String, CorpusManager.CorpusState> states = new HashMap<>();

	/**
	 * Mapping of corpora that are truly live (i.e. properly connected).
	 * Chosen to be a linked map as we need the order in which corpora got connected
	 * for the proper shutdown sequence.
	 */
	private final Map<String, Corpus> liveCorpora = new LinkedHashMap<>();

	/**
	 * Global lock to synchronize manipulation of corpora that are under control
	 * of this manager.
	 */
	private final StampedLock lock = new StampedLock();

	private final MetadataStoragePolicy<CorpusManifest> corpusMetadataPolicy;
	private final MetadataStoragePolicy<ContextManifest> contextMetadataPolicy;

	private final List<CorpusLifecycleListener> lifecycleListeners = new CopyOnWriteArrayList<>();

	private final Properties properties;

	protected DefaultCorpusManager(Builder builder) {
		requireNonNull(builder);

		manifestRegistry = builder.getManifestRegistry();
		metadataRegistry = builder.getMetadataRegistry();
		fileManager = builder.getFileManager();
		resourceProvider = builder.getResourceProvider();

		corpusMetadataPolicy = builder.getCorpusMetadataPolicy();
		contextMetadataPolicy = builder.getContextMetadataPolicy();

		corpusProducer = builder.getCorpusProducer();

		// Attempt to load default properties
		Properties defaultProperties = new Properties();
		URL defaultPropertiesUrl = DefaultCorpusManager.class.getResource("default-properties.ini");
		if(defaultPropertiesUrl==null)
			throw new ModelException(GlobalErrorCode.INTERNAL_ERROR, "Unable to load default properties");
		readProperties(defaultProperties, defaultPropertiesUrl);

		properties = new Properties(defaultProperties);

		if(builder.getProperties()!=null) {
			builder.getProperties().forEach(properties::setProperty);
		} else if(builder.getPropertiesFile()!=null) {
			readProperties(properties, builder.getPropertiesFile());
		} else if(builder.getPropertiesUrl()!=null) {
			readProperties(properties, builder.getPropertiesUrl());
		}
	}

	/**
	 * Reads {@link Properties} from the given {@link URL} using {@link StandardCharsets#UTF_8 UTF-8}
	 * as character encoding.
	 *
	 * @param properties
	 * @param url
	 *
	 * @throws ModelException if an {@link IOException} occurs
	 */
	private void readProperties(Properties properties, URL url) {
		try {
			properties.load(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new ModelException(GlobalErrorCode.IO_ERROR, "Failed to load properties from URL: "+url, e);
		}
	}

	/**
	 * Reads {@link Properties} from the given {@link Path} using {@link StandardCharsets#UTF_8 UTF-8}
	 * as character encoding.
	 *
	 * @param properties
	 * @param path
	 *
	 * @throws ModelException if an {@link IOException} occurs
	 */
	private void readProperties(Properties properties, Path path) {
		try {
			properties.load(Files.newBufferedReader(path, StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new ModelException(GlobalErrorCode.IO_ERROR, "Failed to load properties from file: "+path, e);
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
		requireNonNull(key);

		String value = System.getProperty(key);

		if(value==null) {
			value = properties.getProperty(key);
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

	@Override
	public CorpusMemberFactory newFactory() {
		return new DefaultCorpusMemberFactory(this);
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
		requireNonNull(manifest);
		if(manifest.getRegistry()!=manifestRegistry)
			throw new ModelException(ManifestErrorCode.MANIFEST_ERROR,
					"Foreign manifest: "+getName(manifest));

		final String id = ManifestUtils.requireId(manifest);
		CorpusManager.CorpusState state = states.get(id);

		if(state==CorpusManager.CorpusState.BAD && !allowBadState)
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					"Bad corpus: "+id);

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
		requireNonNull(manifest);
		requireNonNull(state);

		final String id = ManifestUtils.requireId(manifest);

		CorpusManager.CorpusState oldState = states.get(id);

		if(oldState==null) {
			oldState = CorpusManager.CorpusState.ENABLED;
		}

		if(!state.isValidPrecondition(oldState))
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					String.format("Corpus %s cannot be set to state %s while currently being %s",
							id, state, oldState));

		if(state==CorpusManager.CorpusState.ENABLED) {
			states.remove(id);
		} else {
			states.put(id, state);
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

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#getFileManager()
	 */
	@Override
	public FileManager getFileManager() {
		return fileManager;
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#getResourceProvider()
	 */
	@Override
	public ResourceProvider getResourceProvider() {
		return resourceProvider;
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
		requireNonNull(manifest);

		Corpus corpus = null;

		long stamp = lock.writeLockInterruptibly();
		try {
			CorpusManager.CorpusState oldState = getStateUnsafe(manifest, false);

			final String id = ManifestUtils.requireId(manifest);

			// Immediately return in case the corpus is already connected (do not notify listeners!!!)
			if(oldState==CorpusManager.CorpusState.CONNECTED) {
				return liveCorpora.get(id);
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
			liveCorpora.put(id, corpus);
			setStateUnsafe(manifest, CorpusManager.CorpusState.CONNECTED);
			fireCorpusConnected(corpus);

		} finally {
			lock.unlockWrite(stamp);
		}

		return corpus;
	}

	private Corpus createCorpusFromManifest(CorpusManifest manifest) throws InterruptedException {

		Corpus corpus = null;

		BiFunction<CorpusManager, CorpusManifest, Corpus> corpusProducer = this.corpusProducer;
		if(corpusProducer!=null) {
			// When delegating corpus creation, the producer is responsible for cloning the manifest
			corpus = corpusProducer.apply(this, manifest);
		} else {
			// Create a detached version of the manifest that uses no templating
			manifest = ManifestUtils.flattenCorpus(manifest);
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
	 * Actual instantiation will then be delegated to a new {@link DefaultCorpus.Builder}.
	 *
	 * @param manifest
	 * @return
	 * @throws InterruptedException
	 */
	protected Corpus instantiate(CorpusManifest manifest) throws InterruptedException {

		return DefaultCorpus.builder()
			.manager(this)
			.manifest(manifest)
			.metadataRegistry(new SubRegistry(metadataRegistry, ManifestUtils.requireId(manifest)))
			.build();
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#disconnect(de.ims.icarus2.model.manifest.api.CorpusManifest)
	 */
	@Override
	public void disconnect(CorpusManifest manifest) throws InterruptedException, AccumulatingException {
		requireNonNull(manifest);

		long stamp = lock.writeLockInterruptibly();
		try {
			disconnectUnsafe(manifest);
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	/**
	 * Disconnects the given corpus, but does not acquire locks. This is the responsibility
	 * of whatever code calls this method!!!
	 *
	 * @param manifest
	 * @throws InterruptedException
	 * @throws AccumulatingException
	 */
	private void disconnectUnsafe(CorpusManifest manifest) throws InterruptedException, AccumulatingException {
		// For internal sanity checks
		CorpusState oldState = getStateUnsafe(manifest, false);
		if(oldState==CorpusState.DISCONNECTING) {
			return;
		}
		if(oldState!=CorpusState.CONNECTED)
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					String.format("Cannot dosconnect manifest %s in state %s",
							getName(manifest), oldState));

		// First transition and notification
		setStateUnsafe(manifest, CorpusManager.CorpusState.DISCONNECTING);
		fireCorpusChanged(manifest);

		final String id = ManifestUtils.requireId(manifest);

		// Now destroy corpus
		Corpus corpus = liveCorpora.get(id);
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

		liveCorpora.remove(id);
		setStateUnsafe(manifest, CorpusManager.CorpusState.ENABLED);
		fireCorpusDisconnected(manifest);

	}

	/**
	 * Hook for subclasses to implement more elaborate shutdown procedures for
	 * individual corpora. The default implementation simply calls
	 * {@link Corpus#close()}.
	 */
	protected void destroy(Corpus corpus) throws InterruptedException, AccumulatingException {
		corpus.close();
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#shutdown()
	 */
	@Override
	public void shutdown() throws InterruptedException, AccumulatingException {
		long stamp = lock.writeLockInterruptibly();
		try {
			List<Corpus> pendingCorpora = new ArrayList<>(liveCorpora.values());
			if(pendingCorpora.isEmpty()) {
				return;
			}

			AccumulatingException.Buffer exceptionBuffer = new AccumulatingException.Buffer();
			for(Corpus corpus : pendingCorpora) {
				try {
					//TODO do we need to add measures for suppressing events at this stage?
					disconnectUnsafe(corpus.getManifest());
				} catch (AccumulatingException e) {
					exceptionBuffer.addExceptionsFrom(e);
				}
			}

			// Throw all exceptions combined
			if(!exceptionBuffer.isEmpty()) {
				exceptionBuffer.setFormattedMessage("Failed to shutdown %d of %d corpora",
						_int(exceptionBuffer.getExceptionCount()), _int(pendingCorpora.size()));
				throw exceptionBuffer.toException();
			}
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#getLiveCorpus(de.ims.icarus2.model.manifest.api.CorpusManifest)
	 */
	@Override
	public Corpus getLiveCorpus(CorpusManifest manifest) {
		requireNonNull(manifest);

		long stamp = lock.readLock();
		try {
			Corpus corpus = null;
			CorpusManager.CorpusState state = getStateUnsafe(manifest, true);

			final String id = ManifestUtils.requireId(manifest);

			if(state==CorpusManager.CorpusState.CONNECTED) {
				corpus = liveCorpora.get(id);
			}

			return corpus;
		} finally {
			lock.unlockRead(stamp);
		}
	}

	protected final boolean isCorpusInState(CorpusManifest corpus, CorpusManager.CorpusState state) {
		return isCorpusInState(corpus, state::equals);
	}

	protected final boolean isCorpusInState(CorpusManifest corpus, Predicate<? super CorpusManager.CorpusState> p) {
		requireNonNull(corpus);
		requireNonNull(p);

		long stamp = lock.tryOptimisticRead();
		CorpusManager.CorpusState currentState = getStateUnsafe(corpus, true);

		if(!lock.validate(stamp)) {
			stamp = lock.readLock();
			try {
				currentState = getStateUnsafe(corpus, true);
			} finally {
				lock.unlockRead(stamp);
			}
		}
		return p.test(currentState);
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
		requireNonNull(corpus);

		long stamp = lock.writeLock();
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
			lock.unlockWrite(stamp);
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#disableCorpus(de.ims.icarus2.model.manifest.api.CorpusManifest)
	 */
	@Override
	public boolean disableCorpus(CorpusManifest corpus) {
		requireNonNull(corpus);

		long stamp = lock.writeLock();
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
			lock.unlockWrite(stamp);
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#resetBadCorpus(de.ims.icarus2.model.manifest.api.CorpusManifest)
	 */
	@Override
	public boolean resetBadCorpus(CorpusManifest corpus) {
		requireNonNull(corpus);

		long stamp = lock.writeLock();
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
			lock.unlockWrite(stamp);
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#addCorpusLifecycleListener(de.ims.icarus2.model.api.events.CorpusLifecycleListener)
	 */
	@Override
	public void addCorpusLifecycleListener(CorpusLifecycleListener listener) {
		requireNonNull(listener);

		lifecycleListeners.remove(listener);
		lifecycleListeners.add(listener);
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#removeCorpusLifecycleListener(de.ims.icarus2.model.api.events.CorpusLifecycleListener)
	 */
	@Override
	public void removeCorpusLifecycleListener(CorpusLifecycleListener listener) {
		requireNonNull(listener);

		lifecycleListeners.remove(listener);
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManager#getLiveCorpora()
	 */
	@Override
	public List<CorpusManifest> getLiveCorpora() {

		long stamp = lock.readLock();
		try {
			return liveCorpora.values()
					.stream()
					.map(Corpus::getManifest)
					.collect(Collectors.toList());
		} finally {
			lock.unlockRead(stamp);
		}
	}

	@Override
	public List<CorpusManifest> getCorpora(Predicate<? super CorpusManifest> p) {
		requireNonNull(p);

		long stamp = lock.readLock();
		try {
			Collection<String> corpora = states.keySet();

			if(corpora.isEmpty()) {
				return Collections.emptyList();
			}

			List<CorpusManifest> result = null;

			int visited = 0;
			for(String id : corpora) {
				Optional<CorpusManifest> corpus = manifestRegistry.getCorpusManifest(id);
				if(corpus.isPresent() && p.test(corpus.get())) {
					// Create result buffer lazily only in case we really need it
					if(result==null) {
						result = new ArrayList<>(corpora.size()-visited);
					}
					result.add(corpus.get());
				}
				visited++;
			}

			if(result==null) {
				result = Collections.emptyList();
			}

			return result;
		} finally {
			lock.unlockRead(stamp);
		}
	}

	@Override
	public List<CorpusManifest> getCorpora(CorpusState state) {
		requireNonNull(state);

		long stamp = lock.readLock();
		try {
			//TODO currently we can't return 'enabled' manifests

			Set<Entry<String, CorpusState>> entries = states.entrySet();

			if(entries.isEmpty()) {
				return Collections.emptyList();
			}

			List<CorpusManifest> result = null;

			int visited = 0;
			for(Entry<String, CorpusState> entry : entries) {
				Optional<CorpusManifest> corpus = manifestRegistry.getCorpusManifest(entry.getKey());
				if(corpus.isPresent() && entry.getValue()==state) {
					// Create result buffer lazily only in case we really need it
					if(result==null) {
						result = new ArrayList<>(entries.size()-visited);
					}
					result.add(corpus.get());
				}
				visited++;
			}

			if(result==null) {
				result = Collections.emptyList();
			}

			return result;
		} finally {
			lock.unlockRead(stamp);
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

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	@Api(type=ApiType.BUILDER)
	public static class Builder extends AbstractBuilder<Builder, DefaultCorpusManager> {

		private MetadataStoragePolicy<CorpusManifest> corpusMetadataPolicy;
		private MetadataStoragePolicy<ContextManifest> contextMetadataPolicy;

		private ManifestRegistry manifestRegistry;
		private MetadataRegistry metadataRegistry;
		private FileManager fileManager;
		private ResourceProvider resourceProvider;

		private BiFunction<CorpusManager, CorpusManifest, Corpus> corpusProducer;

		private Map<String, String> properties;

		private URL propertiesUrl;
		private Path propertiesFile;

		protected Builder() {
			// no-op
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public MetadataStoragePolicy<CorpusManifest> getCorpusMetadataPolicy() {
			return corpusMetadataPolicy;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public MetadataStoragePolicy<ContextManifest> getContextMetadataPolicy() {
			return contextMetadataPolicy;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public ManifestRegistry getManifestRegistry() {
			return manifestRegistry;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public MetadataRegistry getMetadataRegistry() {
			return metadataRegistry;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public FileManager getFileManager() {
			return fileManager;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public BiFunction<CorpusManager, CorpusManifest, Corpus> getCorpusProducer() {
			return corpusProducer;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public Map<String, String> getProperties() {
			return properties;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public URL getPropertiesUrl() {
			return propertiesUrl;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public Path getPropertiesFile() {
			return propertiesFile;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public ResourceProvider getResourceProvider() {
			return resourceProvider;
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public Builder resourceProvider(ResourceProvider resourceProvider) {
			requireNonNull(resourceProvider);
			checkState(this.resourceProvider==null);

			this.resourceProvider = resourceProvider;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder corpusMetadataPolicy(
				MetadataStoragePolicy<CorpusManifest> corpusMetadataPolicy) {
			requireNonNull(corpusMetadataPolicy);
			checkState("Corpus metadata policy already set", this.corpusMetadataPolicy==null);

			this.corpusMetadataPolicy = corpusMetadataPolicy;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder contextMetadataPolicy(
				MetadataStoragePolicy<ContextManifest> contextMetadataPolicy) {
			requireNonNull(contextMetadataPolicy);
			checkState("Context metadata policy already set", this.contextMetadataPolicy==null);

			this.contextMetadataPolicy = contextMetadataPolicy;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public Builder manifestRegistry(ManifestRegistry manifestRegistry) {
			requireNonNull(manifestRegistry);
			checkState("Manifest registry already set", this.manifestRegistry==null);

			this.manifestRegistry = manifestRegistry;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public Builder metadataRegistry(MetadataRegistry metadataRegistry) {
			requireNonNull(metadataRegistry);
			checkState("Metadata registry already set", this.metadataRegistry==null);

			this.metadataRegistry = metadataRegistry;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public Builder fileManager(FileManager fileManager) {
			requireNonNull(fileManager);
			checkState("File manager already set", this.fileManager==null);

			this.fileManager = fileManager;

			return thisAsCast();
		}

		/**
		 * Assigns an alternative way of creating actual corpus instances when
		 * connecting to a corpus resource. Note that the {@code corpusProducer}
		 * has the responsibility to fully instantiate and link a {@link Corpus}
		 * instance based on a given {@link CorpusManifest}. This potentially
		 * includes cloning the manifest into a {@link ManifestUtils#flattenCorpus(CorpusManifest) flattened}
		 * version first.
		 *
		 * @param corpusProducer
		 * @return
		 */
		@Guarded(methodType=MethodType.BUILDER)
		public Builder corpusProducer(
				BiFunction<CorpusManager, CorpusManifest, Corpus> corpusProducer) {
			requireNonNull(corpusProducer);
			checkState("Corpus producer already set", this.corpusProducer==null);

			this.corpusProducer = corpusProducer;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder properties(Map<String, String> properties) {
			requireNonNull(properties);
			checkState("Properties already set", this.properties==null);

			this.properties = properties;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder propertiesUrl(URL propertiesUrl) {
			requireNonNull(propertiesUrl);
			checkState("Properties URL already set", this.propertiesUrl==null);

			this.propertiesUrl = propertiesUrl;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder propertiesFile(Path propertiesFile) {
			requireNonNull(propertiesFile);
			checkState("Properties file already set", this.propertiesFile==null);

			this.propertiesFile = propertiesFile;

			return thisAsCast();
		}

		/**
		 * Configures this builder to use the following implementations as instances
		 * for {@link #fileManager(FileManager) file manager}, {@link #resourceProvider(ResourceProvider)},
		 * {@link #metadataRegistry(MetadataRegistry) metadata registry}
		 * and {@link #manifestRegistry(ManifestRegistry) manifest registry}:
		 * <ul>
		 * <li>{@link DefaultFileManager} with the {@code user.dir} directory as root folder</li>
		 * <li>{@link FileResourceProvider}</li>
		 * <li>{@link VirtualMetadataRegistry}</li>
		 * <li>{@link DefaultManifestRegistry}</li>
		 * </ul>
		 */
		public Builder defaultEnvironment() {
			return fileManager(new DefaultFileManager(Paths.get(System.getProperty("user.dir"))))
					.resourceProvider(new FileResourceProvider())
					.metadataRegistry(new VirtualMetadataRegistry())
					.manifestRegistry(new DefaultManifestRegistry());
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#validate()
		 */
		@Override
		protected void validate() {
			checkState("Manifest registry missing", manifestRegistry!=null);
			checkState("Metadata registry missing", metadataRegistry!=null);
			checkState("File manager missing", fileManager!=null);
			checkState("Missing resource provider", resourceProvider!=null);
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#create()
		 */
		@Override
		protected DefaultCorpusManager create() {
			return new DefaultCorpusManager(this);
		}

	}
}
