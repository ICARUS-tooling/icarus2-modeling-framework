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
package de.ims.icarus2.model.api.layer;

import static de.ims.icarus2.SharedTestUtils.mockSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.PartTest;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 * @author Markus Gärtner
 *
 */
public interface LayerTest<L extends Layer, M extends Manifest> extends PartTest<LayerGroup, L>,
		ApiGuardedTest<L> {

	@Provider
	L createForManifest(M manifest);

	@Provider
	M createManifest(String name);

	@Provider
	default L createForGroup(LayerGroup group) {
		L layer = createForManifest(createManifest("test"));
		layer.addNotify(group);
		return layer;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default L createTestInstance(TestSettings settings) {
		return settings.process(createForManifest(createManifest("test")));
	}

	@Override
	default LayerGroup createEnvironment() {
		return mock(LayerGroup.class);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.Layer#getName()}.
	 */
	@Test
	@RandomizedTest
	default void testGetName(RandomGenerator rand) {
		String name = rand.randomString(20);
		M manifest = createManifest(name);
		L layer = createForManifest(manifest);
		assertEquals(name, layer.getName());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.Layer#getItemProxy()}.
	 */
	@Test
	default void testGetItemProxy() {
		assertNotNull(create().getItemProxy());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.Layer#getContext()}.
	 */
	@Test
	default void testGetContext() {
		LayerGroup group = createEnvironment();
		L layer = createForGroup(group);
		layer.getContext();
		verify(group).getContext();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.Layer#getLayerGroup()}.
	 */
	@Test
	default void testGetLayerGroup() {
		LayerGroup group = createEnvironment();
		L layer = createForGroup(group);
		assertSame(group, layer.getLayerGroup());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.Layer#getBaseLayers()}.
	 */
	@Test
	default void testGetBaseLayers() {
		assertTrue(create().getBaseLayers().isEmpty());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.Layer#getManifest()}.
	 */
	@Test
	default void testGetManifest() {
		M manifest = createManifest("test");
		L layer = createForManifest(manifest);
		assertSame(manifest, layer.getManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.Layer#setBaseLayers(de.ims.icarus2.util.collections.set.DataSet)}.
	 */
	@Test
	default void testSetBaseLayers() {
		L layer = create();
		DataSet<ItemLayer> baseLayers = mockSet(mock(ItemLayer.class));

		layer.setBaseLayers(baseLayers);

		assertSame(baseLayers, layer.getBaseLayers());
	}

}
