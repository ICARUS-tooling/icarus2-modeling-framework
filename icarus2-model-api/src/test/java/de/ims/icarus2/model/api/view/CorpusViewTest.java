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
package de.ims.icarus2.model.api.view;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.MANIFEST_FACTORY;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.util.ManifestBuilder;
import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.util.AccessMode;

/**
 * @author Markus Gärtner
 *
 */
public interface CorpusViewTest<V extends CorpusView> extends GenericTest<V> {

	/**
	 * Create a view with specified {@link AccessMode} that is of arbitrary size.
	 * @param accessMode
	 * @return
	 */
	@Provider
	V createForAccessMode(AccessMode accessMode);

	/**
	 * Create a view with read access that is of the given size.
	 * @param size
	 * @return
	 */
	@Provider
	V createForSize(long size);

	Set<AccessMode> getSupportedAccessModes();

	/**
	 * Creates a flat corpus with a single item layer.
	 * @return
	 */
	default CorpusManifest createDefaultCorpusManifest() {

		try(ManifestBuilder builder = new ManifestBuilder(MANIFEST_FACTORY)) {
			return builder.create(CorpusManifest.class, "corpus")
					.addRootContextManifest(builder.create(ContextManifest.class, "context", "corpus")
							.setPrimaryLayerId("tokens")
							.setDriverManifest(builder.create(DriverManifest.class, "driver", "context")
									.setImplementationManifest(builder.live(Driver.class)))
							.addLayerGroup(builder.create(LayerGroupManifest.class, "group", "context")
									.setPrimaryLayerId("tokens")
									.setIndependent(true)
									.addLayerManifest(builder.create(ItemLayerManifest.class, "tokens", "group"))));
		}
	}

	/**
	 * Constructs a mock of {@link CorpusManager} that allows testing creation of live corpora
	 * based on the specified {@link CorpusManifest}.
	 *
	 * @param corpusManifest
	 * @return
	 */
	@SuppressWarnings("boxing")
	default CorpusManager createDefaultCorpusManager(CorpusManifest corpusManifest) {

		CorpusManager corpusManager = mock(CorpusManager.class);
		when(corpusManager.isCorpusConnected(eq(corpusManifest))).thenReturn(Boolean.TRUE);
		when(corpusManager.isCorpusEnabled(eq(corpusManifest))).thenReturn(Boolean.TRUE);
		when(corpusManager.getImplementationClassLoader(any())).thenReturn(getClass().getClassLoader());

		return corpusManager;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.CorpusView#getCorpus()}.
	 */
	@Test
	default void testGetCorpus() {
		assertNotNull(create().getCorpus());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.CorpusView#getScope()}.
	 */
	@Test
	default void testGetScope() {
		assertNotNull(create().getScope());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.CorpusView#getAccessMode()}.
	 */
	@TestFactory
	default Stream<DynamicTest>  testGetAccessMode() {
		Set<AccessMode> supportedModes = getSupportedAccessModes();
		assertThat(supportedModes).isNotEmpty();

		return Stream.concat(
				supportedModes.stream()
					.map(mode -> dynamicTest(mode.name(), () -> {
						V view = createForAccessMode(mode);
						assertNotNull(view.getAccessMode());
						if(mode.isRead()) {
							assertTrue(view.getAccessMode().isRead(), "View must be readable");
						}
						if(mode.isWrite()) {
							assertTrue(view.getAccessMode().isWrite(), "View must be writable");
						}
					})),
				Stream.of(AccessMode.values())
					.filter(mode -> !supportedModes.contains(mode))
					.map(mode -> dynamicTest(mode.name()+" (unsupported)", () -> {
						assertModelException(GlobalErrorCode.INVALID_INPUT,
								() -> createForAccessMode(mode));
					}))
				);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.CorpusView#getSize()}.
	 */
	@ParameterizedTest
	@ValueSource(longs = {UNSET_LONG, 100, 10_000, 100_000, 100_000_000})
	default void testGetSize(long size) {
		long expectedSize = size==UNSET_LONG ? 0L : size;
		assertEquals(expectedSize, createForSize(size).getSize());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.CorpusView#fetchPrimaryLayer()}.
	 */
	@Test
	default void testFetchPrimaryLayer() {
		V view = create();
		Scope scope = view.getScope();

		assertSame(scope.getPrimaryLayer(), view.fetchPrimaryLayer());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.CorpusView#fetchLayer(java.lang.String)}.
	 */
	@Test
	default void testFetchLayerString() {
		V view = create();
		Scope scope = view.getScope();
		for(Layer layer : scope.getLayers()) {
			assertSame(layer, view.fetchLayer(layer.getManifest().getUniqueId()));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.CorpusView#fetchLayer(java.lang.String, java.lang.String)}.
	 */
	@Test
	default void testFetchLayerStringString() {
		V view = create();
		Scope scope = view.getScope();
		for(Layer layer : scope.getLayers()) {
			Context context = layer.getContext();
			assertSame(layer, view.fetchLayer(
					context.getManifest().getId().get(),
					layer.getManifest().getId().get()));
		}
	}

}
