/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.util.collections.LazyCollection;

/**
 * @author Markus Gärtner
 *
 */
public interface LayerManifestTest<M extends LayerManifest> extends MemberManifestTest<M> {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#getContextManifest()}.
	 */
	@Test
	default void testGetContextManifest() {
		assertNotNull(createUnlocked().getContextManifest());
		assertNull(createTemplate().getContextManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#getGroupManifest()}.
	 */
	@Test
	default void testGetGroupManifest() {
		assertNotNull(createUnlocked().getGroupManifest());
		assertNull(createTemplate().getGroupManifest());
	}

	public static LayerType mockLayerType(String id) {
		LayerType type = mock(LayerType.class);
		when(type.getId()).thenReturn(id);
		return type;
	}

	/**
	 * Creates a wrapper around {@link LayerManifest#setLayerTypeId(String)} to turn it into
	 * a {@link BiConsumer} that takes a {@link LayerManifest} and {@link LayerType} and
	 * internally accessing the underlying mocked {@link ManifestRegistry} to ensure the
	 * given {@link LayerType} is {@link ManifestRegistry#getLayerType(String) available}.
	 *
	 * @return
	 */
	public static <M extends LayerManifest> BiConsumer<M, LayerType> inject_setLayerTypeId() {
		// Ensure our mocked LayerType is available from the registry
		return (m, type) -> {
			ManifestRegistry mockedRegistry = TestUtils.assertMock(m.getRegistry());
			String id = type.getId();
			when(mockedRegistry.getLayerType(id)).thenReturn(type);

			m.setLayerTypeId(id);
		};
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#getLayerType()}.
	 */
	@Test
	default void testGetLayerType() {
		assertDerivativeGetter(mockLayerType("type1"), mockLayerType("type2"), null,
				LayerManifest::getLayerType, inject_setLayerTypeId());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#isLocalLayerType()}.
	 */
	@Test
	default void testIsLocalLayerType() {
		assertDerivativeIsLocal(mockLayerType("type1"), mockLayerType("type2"),
				LayerManifest::isLocalLayerType, inject_setLayerTypeId());
	}

	/**
	 * Creates a wrapper around {@link LayerManifest#forEachBaseLayerManifest(Consumer)} that
	 * provides a {@code forEach} signature for {@code String} values instead of
	 * {@link TargetLayerManifest}. This way it can be used together with the modifier methods
	 * such as {@link LayerManifest#addBaseLayerId(String)} for testing.
	 *
	 * @return
	 */
	public static <M extends LayerManifest, C extends Consumer<String>>
			Function<M, Consumer<C>> inject_forEachBaseLayerManifest(boolean localOnly) {
		return m -> LazyCollection.<String>lazyList()
				.addFromForEach(localOnly ? m::forEachLocalBaseLayerManifest
						: m::forEachBaseLayerManifest, t -> ((TargetLayerManifest)t).getLayerId())
				::forEach;
	}

	public static <M extends LayerManifest> BiConsumer<M, String> inject_addBaseLayerManifest() {
		return (m, id) -> {
			LayerManifest target = mock(LayerManifest.class);
			ContextManifest contextManifest = TestUtils.assertMock(m.getContextManifest());

			when(contextManifest.getLayerManifest(id)).thenReturn(target);

			TargetLayerManifest targetLayerManifest = m.addBaseLayerId(id);
			assertNotNull(targetLayerManifest);
			assertEquals(id, targetLayerManifest.getLayerId());
			assertSame(m, targetLayerManifest.getLayerManifest());
			assertSame(m, targetLayerManifest.getHost());

			assertSame(target, targetLayerManifest.getResolvedLayerManifest());
		};
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#forEachBaseLayerManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachBaseLayerManifest() {
		assertDerivativeForEach("layer", "layer", inject_forEachBaseLayerManifest(false), inject_addBaseLayerManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#forEachLocalBaseLayerManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLocalBaseLayerManifest() {
		assertDerivativeForEach("layer", "layer", inject_forEachBaseLayerManifest(true), inject_addBaseLayerManifest());
	}

	/**
	 * Creates a wrapper around {@link LayerManifest#getBaseLayerManifests()} that returns a list
	 * of ids instead of the raw layer manifests.
	 *
	 * @return
	 */
	public static <M extends LayerManifest> Function<M, List<String>> transform_getBaseLayerManifests(boolean localOnly) {
		return m -> {
			return LazyCollection.<String>lazyList()
					.addAll(localOnly ? m.getLocalBaseLayerManifests()
							: m.getBaseLayerManifests(), t -> t.getLayerId())
					.getAsList();
		};
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#getBaseLayerManifests()}.
	 */
	@Test
	default void testGetBaseLayerManifests() {
		assertDerivativeAccumulativeGetter("layer", "layer",
				transform_getBaseLayerManifests(false), inject_addBaseLayerManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#getLocalBaseLayerManifests()}.
	 */
	@Test
	default void testGetLocalBaseLayerManifests() {
		assertDerivativeAccumulativeGetter("layer", "layer",
				transform_getBaseLayerManifests(true), inject_addBaseLayerManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#setLayerTypeId(java.lang.String)}.
	 */
	@Test
	default void testSetLayerTypeId() {
		assertLockableSetter(LayerManifest::setLayerTypeId, ManifestTestUtils.getLegalIdValues(),
				true, ManifestTestUtils.getIllegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#addBaseLayerId(java.lang.String)}.
	 */
	@Test
	default void testAddBaseLayerId() {
		assertLockableAccumulativeAdd(inject_addBaseLayerManifest(),
				ManifestTestUtils.getIllegalIdValues(), true, true, ManifestTestUtils.getLegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#removeBaseLayerId(java.lang.String)}.
	 */
	@Test
	default void testRemoveBaseLayerId() {
		assertLockableAccumulativeRemove(LayerManifest::addBaseLayerId,
				LayerManifest::removeBaseLayerId, transform_getBaseLayerManifests(false), true, true,
				"layer1", "layer2", "layer3");
	}

}
