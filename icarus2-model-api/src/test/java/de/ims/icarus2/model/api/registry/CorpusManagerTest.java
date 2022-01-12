/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.registry;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.stubId;
import static de.ims.icarus2.test.TestUtils.assertCollectionEmpty;
import static de.ims.icarus2.test.TestUtils.assertCollectionEquals;
import static de.ims.icarus2.test.TestUtils.assertMock;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.test.TestUtils.settings;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.events.CorpusLifecycleListener;
import de.ims.icarus2.model.api.registry.CorpusManager.CorpusState;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.PostponedTest;
import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface CorpusManagerTest<M extends CorpusManager> extends GenericTest<M> {

	@Provider
	M createCustomManager(BiFunction<CorpusManager, CorpusManifest, Corpus> corpusProducer, TestSettings settings);

	public static final BiFunction<CorpusManager, CorpusManifest, Corpus> DEFAULT_PRODUCER
			= (manager, manifest) -> {
		assertMock(manifest);

		Corpus corpus = mock(Corpus.class);
		when(corpus.getManifest()).thenReturn(manifest);
		when(corpus.getManager()).thenReturn(manager);

		return corpus;
	};

	public static final BiFunction<CorpusManager, CorpusManifest, Corpus> NULL_PRODUCER
			= (manager, manifest) -> {
		assertMock(manifest);

		return null;
	};

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#getProperty(java.lang.String)}.
	 */
	@Test
	default void testGetProperty() {
		assertNPE(() -> create().getProperty(null));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#getManifestRegistry()}.
	 */
	@Test
	default void testGetManifestRegistry() {
		assertNotNull(create().getManifestRegistry());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#getMetadataRegistry()}.
	 */
	@Test
	default void testGetMetadataRegistry() {
		assertNotNull(create().getMetadataRegistry());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#getFileManager()}.
	 */
	@Test
	default void testGetFileManager() {
		assertNotNull(create().getFileManager());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#getResourceProvider()}.
	 */
	@Test
	default void testGetResourceProvider() {
		assertNotNull(create().getResourceProvider());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#getCorpusMetadataPolicy()}.
	 */
	@Test
	default void testGetCorpusMetadataPolicy() {
		assertNotNull(create().getCorpusMetadataPolicy());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#getContextMetadataPolicy()}.
	 */
	@Test
	default void testGetContextMetadataPolicy() {
		assertNotNull(create().getContextMetadataPolicy());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#newFactory()}.
	 */
	@Test
	default void testNewFactory() {
		assertNotNull(create().newFactory());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#connect(de.ims.icarus2.model.manifest.api.CorpusManifest)}.
	 */
	@Test
	default void testConnect() throws Exception {
		M manager = createCustomManager(DEFAULT_PRODUCER, settings());

		assertNPE(() -> manager.connect(null));

		CorpusManifest manifest = mockManifest(manager, "corpus1");
		Corpus corpus = manager.connect(manifest);

		assertNotNull(corpus);
		assertSame(manifest, corpus.getManifest());
		assertSame(manager, corpus.getManager());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#getLiveCorpus(de.ims.icarus2.model.manifest.api.CorpusManifest)}.
	 */
	@Test
	default void testGetLiveCorpus() throws Exception {
		M manager = createCustomManager(DEFAULT_PRODUCER, settings());
		CorpusManifest manifest = mockManifest(manager, "corpus1");
		Corpus corpus = manager.connect(manifest);

		assertSame(corpus, manager.getLiveCorpus(manifest));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#disconnect(de.ims.icarus2.model.manifest.api.CorpusManifest)}.
	 */
	@Test
	default void testDisconnect() throws Exception {
		M manager = createCustomManager(DEFAULT_PRODUCER, settings());

		assertNPE(() -> manager.disconnect(null));

		CorpusManifest manifest = mockManifest(manager, "corpus1");

		assertModelException(GlobalErrorCode.ILLEGAL_STATE,
				() -> manager.disconnect(manifest));

		assertNotNull(manager.connect(manifest));
		manager.disconnect(manifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#shutdown()}.
	 */
	@Test
	default void testShutdown() throws Exception {
		M manager = createCustomManager(DEFAULT_PRODUCER, settings());
		manager.connect(mockManifest(manager, "corpus1"));
		manager.connect(mockManifest(manager, "corpus2"));
		manager.connect(mockManifest(manager, "corpus3"));

		manager.shutdown();

		assertCollectionEmpty(manager.getLiveCorpora());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#isCorpusConnected(de.ims.icarus2.model.manifest.api.CorpusManifest)}.
	 */
	@Test
	default void testIsCorpusConnected() throws Exception {
		M manager = createCustomManager(DEFAULT_PRODUCER, settings());

		assertNPE(() -> manager.isCorpusConnected(null));

		CorpusManifest manifest = mockManifest(manager, "corpus1");

		assertFalse(manager.isCorpusConnected(manifest));
		assertNotNull(manager.connect(manifest));
		assertTrue(manager.isCorpusConnected(manifest));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#isCorpusConnecting(de.ims.icarus2.model.manifest.api.CorpusManifest)}.
	 */
	@Test
	default void testIsCorpusConnecting() throws Exception {
		M manager = createCustomManager(DEFAULT_PRODUCER, settings());

		assertNPE(() -> manager.isCorpusConnecting(null));

		CorpusManifest manifest = mockManifest(manager, "corpus1");

		/*
		 * A corpus is only in the CONNECTING state while it gets constructed,
		 * we therefore need to construct a controlled concurrent test scenario.
		 */
		//TODO complete test
		assertFalse(manager.isCorpusConnecting(manifest));
		assertNotNull(manager.connect(manifest));
		assertFalse(manager.isCorpusConnecting(manifest));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#isCorpusDisconnecting(de.ims.icarus2.model.manifest.api.CorpusManifest)}.
	 */
	@Test
	default void testIsCorpusDisconnecting() throws Exception {
		M manager = createCustomManager(DEFAULT_PRODUCER, settings());

		assertNPE(() -> manager.isCorpusDisconnecting(null));

		CorpusManifest manifest = mockManifest(manager, "corpus1");

		/*
		 * A corpus is only in the DISCONNECTING state while it gets deconstructed,
		 * we therefore need to construct a controlled concurrent test scenario.
		 */
		//TODO complete test
		assertFalse(manager.isCorpusDisconnecting(manifest));
		assertNotNull(manager.connect(manifest));
		assertFalse(manager.isCorpusDisconnecting(manifest));
		manager.disconnect(manifest);
		assertFalse(manager.isCorpusDisconnecting(manifest));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#isCorpusEnabled(de.ims.icarus2.model.manifest.api.CorpusManifest)}.
	 */
	@Test
	default void testIsCorpusEnabled() {
		M manager = create();

		assertNPE(() -> manager.isCorpusEnabled(null));

		CorpusManifest manifest = mockManifest(manager, "corpus1");

		assertTrue(manager.isCorpusEnabled(manifest));

		assertTrue(manager.disableCorpus(manifest));
		assertFalse(manager.isCorpusEnabled(manifest));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#isBadCorpus(de.ims.icarus2.model.manifest.api.CorpusManifest)}.
	 */
	@Test
	default void testIsBadCorpus() {
		M manager = createCustomManager(NULL_PRODUCER, settings());

		assertNPE(() -> manager.isBadCorpus(null));

		CorpusManifest manifest = mockManifest(manager, "corpus1");

		// Initially no bad corpora
		assertFalse(manager.isBadCorpus(manifest));

		assertModelException(GlobalErrorCode.DELEGATION_FAILED,
				() -> manager.connect(manifest));

		assertTrue(manager.isBadCorpus(manifest));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#enableCorpus(de.ims.icarus2.model.manifest.api.CorpusManifest)}.
	 */
	@Test
	default void testEnableCorpus() {
		M manager = createCustomManager(DEFAULT_PRODUCER, settings());

		assertNPE(() -> manager.enableCorpus(null));

		CorpusManifest manifest = mockManifest(manager, "corpus1");

		// Initially corpus should already be enabled
		assertFalse(manager.enableCorpus(manifest));

		// Need to disable first before we can properly enable
		assertTrue(manager.disableCorpus(manifest));
		assertTrue(manager.enableCorpus(manifest));
		// Return value should change as result of repeated call
		assertFalse(manager.enableCorpus(manifest));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#disableCorpus(de.ims.icarus2.model.manifest.api.CorpusManifest)}.
	 */
	@Test
	default void testDisableCorpus() {
		M manager = createCustomManager(DEFAULT_PRODUCER, settings());

		assertNPE(() -> manager.disableCorpus(null));

		CorpusManifest manifest = mockManifest(manager, "corpus1");

		assertTrue(manager.disableCorpus(manifest));
		// Return value should change as result of repeated call
		assertFalse(manager.disableCorpus(manifest));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#resetBadCorpus(de.ims.icarus2.model.manifest.api.CorpusManifest)}.
	 */
	@Test
	default void testResetBadCorpus() {
		M manager = createCustomManager(NULL_PRODUCER, settings());
		CorpusManifest manifest = mockManifest(manager, "corpus1");

		// Initially no bad corpora
		assertFalse(manager.resetBadCorpus(manifest));

		assertModelException(GlobalErrorCode.DELEGATION_FAILED,
				() -> manager.connect(manifest));

		assertTrue(manager.resetBadCorpus(manifest));
		// Change of return value on repeated call
		assertFalse(manager.resetBadCorpus(manifest));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#addCorpusLifecycleListener(de.ims.icarus2.model.api.events.CorpusLifecycleListener)}.
	 */
	@Test
	default void testAddCorpusLifecycleListener() {
		M manager = create();

		assertNPE(() -> manager.addCorpusLifecycleListener(null));

		manager.addCorpusLifecycleListener(mock(CorpusLifecycleListener.class));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#removeCorpusLifecycleListener(de.ims.icarus2.model.api.events.CorpusLifecycleListener)}.
	 */
	@Test
	default void testRemoveCorpusLifecycleListener() {
		M manager = create();

		assertNPE(() -> manager.removeCorpusLifecycleListener(null));

		manager.removeCorpusLifecycleListener(mock(CorpusLifecycleListener.class));
	}

	default CorpusManifest mockManifest(M manager, String id) {
		CorpusManifest manifest = stubId(mockTypedManifest(ManifestType.CORPUS_MANIFEST), id);
		when(manifest.getRegistry()).thenReturn(manager.getManifestRegistry());
		return manifest;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#getLiveCorpora()}.
	 */
	@Test
	default void testGetLiveCorpora() throws Exception {
		M manager = createCustomManager(DEFAULT_PRODUCER, settings());

		// Initially no live corpora
		assertCollectionEmpty(manager.getLiveCorpora());

		// Now initiate a dummy
		CorpusManifest manifest = mockManifest(manager, "corpus1");
		assertNotNull(manager.connect(manifest));

		assertCollectionEquals(manager.getLiveCorpora(), manifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#getCorpora(java.util.function.Predicate)}.
	 */
	@Test
	default void testGetCorporaPredicateOfQsuperCorpusManifest() throws Exception {
		M manager = createCustomManager(DEFAULT_PRODUCER, settings());

		assertNPE(() -> manager.getCorpora((Predicate<? super CorpusManifest>)null));

		CorpusManifest manifest1 = mockManifest(manager, "corpus1");
		CorpusManifest manifest2 = mockManifest(manager, "corpus2a");
		CorpusManifest manifest3 = mockManifest(manager, "corpus2b");

		manager.connect(manifest1);
		manager.connect(manifest2);
		manager.connect(manifest3);

		assertCollectionEmpty(manager.getCorpora(m -> false));
		assertCollectionEquals(manager.getCorpora(m -> true), manifest1, manifest2, manifest3);
		assertCollectionEquals(manager.getCorpora(m -> m.getId().get().startsWith("corpus2")),
				manifest2, manifest3);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#getCorpora(de.ims.icarus2.model.api.registry.CorpusManager.CorpusState)}.
	 */
	@Test
	default void testGetCorporaCorpusState() throws Exception {
		BiFunction<CorpusManager, CorpusManifest, Corpus> producer = (manager, manifest) -> {
			if("bad".equals(manifest.getId().orElse(null))) {
				return NULL_PRODUCER.apply(manager, manifest);
			}
			return DEFAULT_PRODUCER.apply(manager, manifest);
		};
		M manager = createCustomManager(producer, settings());

		assertNPE(() -> manager.getCorpora((CorpusState)null));

		for(CorpusState state : CorpusState.values()) {
			assertCollectionEmpty(manager.getCorpora(state));
		}

		CorpusManifest connectedManifest = mockManifest(manager, "connected");
		CorpusManifest disconenctedManifest = mockManifest(manager, "disconnected");
		CorpusManifest badManifest = mockManifest(manager, "bad");
		CorpusManifest disabledManifest = mockManifest(manager, "disabled");

		assertNotNull(manager.connect(connectedManifest));

		assertNotNull(manager.connect(disconenctedManifest));
		manager.disconnect(disconenctedManifest);

		assertModelException(GlobalErrorCode.DELEGATION_FAILED,
				() -> manager.connect(badManifest));

		assertTrue(manager.disableCorpus(disabledManifest));

		/*
		 * Corpora in the 'conencting' or 'disconnecting' state cannot be
		 * queried without a concurrent testing scenario.
		 */
		assertCollectionEquals(manager.getCorpora(CorpusState.CONNECTED), connectedManifest);
		assertCollectionEquals(manager.getCorpora(CorpusState.BAD), badManifest);
		assertCollectionEquals(manager.getCorpora(CorpusState.DISABLED), disabledManifest);
		//TODO actually test for mere 'enabled' (which also includes the disconnected ones) corpora
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#getConnectedCorpora()}.
	 */
	@Test
	default void testGetConnectedCorpora() throws Exception {
		M manager = createCustomManager(DEFAULT_PRODUCER, settings());

		// Initially no live corpora
		assertCollectionEmpty(manager.getConnectedCorpora());

		// Now initiate a dummy
		CorpusManifest manifest = mockManifest(manager, "corpus1");
		assertNotNull(manager.connect(manifest));

		assertCollectionEquals(manager.getConnectedCorpora(), manifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#getConnectingCorpora()}.
	 */
	@Test
	@PostponedTest("need to devise a good concurrent test scenario for (dis)connecting state")
	default void testGetConnectingCorpora() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#getDisconnectingCorpora()}.
	 */
	@Test
	@PostponedTest("need to devise a good concurrent test scenario for (dis)connecting state")
	default void testGetDisconnectingCorpora() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#getBadCorpora()}.
	 */
	@Test
	default void testGetBadCorpora() {
		M manager = createCustomManager(NULL_PRODUCER, settings());
		CorpusManifest manifest = mockManifest(manager, "corpus1");

		// Initially no bad corpora
		assertCollectionEmpty(manager.getBadCorpora());

		assertModelException(GlobalErrorCode.DELEGATION_FAILED,
				() -> manager.connect(manifest));

		assertCollectionEquals(manager.getBadCorpora(), manifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#getDisabledCorpora()}.
	 */
	@Test
	default void testGetDisabledCorpora() {
		M manager = createCustomManager(DEFAULT_PRODUCER, settings());
		CorpusManifest manifest = mockManifest(manager, "corpus1");

		// Initially no live corpora
		assertCollectionEmpty(manager.getDisabledCorpora());

		// Now disable some corpus
		assertTrue(manager.disableCorpus(manifest));

		assertCollectionEquals(manager.getDisabledCorpora(), manifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#getImplementationClassLoader(de.ims.icarus2.model.manifest.api.ImplementationManifest)}.
	 */
	@Test
	default void testGetImplementationClassLoader() {
		ManifestLocation location = mock(ManifestLocation.class);
		when(location.getClassLoader()).thenReturn(getClass().getClassLoader());
		ImplementationManifest manifest = mock(ImplementationManifest.class);
		when(manifest.getManifestLocation()).thenReturn(location);

		assertNotNull(create().getImplementationClassLoader(manifest));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#availableExtensions(java.lang.String)}.
	 */
	@Test
	@PostponedTest("Need to rethink the plugin mechanisms")
	default void testAvailableExtensions() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#resolveExtension(java.lang.String)}.
	 */
	@Test
	@PostponedTest("Need to rethink the plugin mechanisms")
	default void testResolveExtension() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#getPluginClassLoader(java.lang.String)}.
	 */
	@Test
	@PostponedTest("Need to rethink the plugin mechanisms")
	default void testGetPluginClassLoader() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.CorpusManager#getExtensionIdentity(java.lang.String)}.
	 */
	@Test
	@PostponedTest("Need to rethink the plugin mechanisms")
	default void testGetExtensionIdentity() {
		fail("Not yet implemented"); // TODO
	}

}
