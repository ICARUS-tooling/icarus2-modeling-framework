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
package de.ims.icarus2.model.standard.members.layer;

import static de.ims.icarus2.test.TestUtils.assertCollectionEquals;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.view.Scope;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
class LayerCacheTest implements GenericTest<LayerCache> {

	@Override
	public Class<? extends LayerCache> getTestTargetClass() {
		return LayerCache.class;
	}

	@Override
	public LayerCache createTestInstance(TestSettings settings) {
		return settings.process(LayerCache.fromLayers(
				mockLayer(12345)));
	}

	@SuppressWarnings("unchecked")
	private Layer mockLayer(int uid) {
		return mockLayer(Layer.class, LayerManifest.class, uid);
	}

	@SuppressWarnings({ "unchecked", "boxing" })
	private <L extends Layer, M extends LayerManifest<M>>
			L mockLayer(Class<L> layerClass, Class<M> manifestClass, int uid) {
		@SuppressWarnings("rawtypes")
		LayerManifest manifest = mock(manifestClass);
		when(manifest.getUID()).thenReturn(uid);
		L layer = mock(layerClass);
		when(layer.getManifest()).thenReturn(manifest);
		return layer;
	}

	private Layer[] randomLayers(int[] uids) {
		return IntStream.of(uids)
				.mapToObj(this::mockLayer)
				.toArray(Layer[]::new);
	}

	@Nested
	class Factory {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.LayerCache#fromLayers(de.ims.icarus2.model.api.layer.Layer[])}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {1, 10, 100})
		@RandomizedTest
		void testFromLayers(int size, RandomGenerator rng) {
			Layer[] layers = randomLayers(rng.randomInts(size, 0, Integer.MAX_VALUE));
			LayerCache cache = LayerCache.fromLayers(layers);
			assertCollectionEquals(cache.layerCollection(), layers);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.LayerCache#fromCollection(java.util.Collection)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {1, 10, 100})
		@RandomizedTest
		void testFromCollection(int size, RandomGenerator rng) {
			Layer[] layers = randomLayers(rng.randomInts(size, 0, Integer.MAX_VALUE));
			LayerCache cache = LayerCache.fromCollection(set(layers));
			assertCollectionEquals(cache.layerCollection(), layers);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.LayerCache#fromScope(de.ims.icarus2.model.api.view.Scope)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {1, 10, 100})
		@RandomizedTest
		void testFromScope(int size, RandomGenerator rng) {
			Layer[] layers = randomLayers(rng.randomInts(size, 0, Integer.MAX_VALUE/2));
			int primaryUid = Integer.MAX_VALUE-1;
			ItemLayer primaryLayer = mockLayer(ItemLayer.class,
					ItemLayerManifest.class, primaryUid);
			Scope scope = mock(Scope.class);
			when(scope.getLayers()).thenReturn(list(layers));
			when(scope.getPrimaryLayer()).thenReturn(primaryLayer);

			LayerCache cache = LayerCache.fromScope(scope);

			for(Layer layer : layers) {
				assertTrue(cache.contains(layer));
			}
			assertSame(primaryLayer, cache.getLayer(primaryUid));
		}

	}

	@Nested
	@RandomizedTest
	class Filled {

		private int[] uids;
		private Layer[] layers;
		private LayerCache cache;

		@BeforeEach
		void setUp(RandomGenerator rng) {
			int size = rng.random(5, 10);
			uids = rng.randomInts(size, 0, Integer.MAX_VALUE/2);
			layers = randomLayers(uids);
			cache = LayerCache.fromLayers(layers);
		}

		@AfterEach
		void tearDown() {
			uids = null;
			layers = null;
			cache = null;
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.LayerCache#getLayer(int)}.
		 */
		@Test
		void testGetLayer() {
			for (int i = 0; i < uids.length; i++) {
				assertSame(layers[i], cache.getLayer(uids[i]));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.LayerCache#contains(de.ims.icarus2.model.api.layer.Layer)}.
		 */
		@Test
		void testContains() {
			for(Layer layer : layers) {
				assertTrue(cache.contains(layer));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.LayerCache#contains(de.ims.icarus2.model.api.layer.Layer)}.
		 */
		@Test
		void testContainsForeign() {
			assertFalse(cache.contains(mockLayer(Integer.MAX_VALUE-1)));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.LayerCache#layerCollection()}.
		 */
		@Test
		void testLayerCollection() {
			assertCollectionEquals(cache.layerCollection(), layers);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.LayerCache#layers()}.
		 */
		@Test
		void testLayers() {
			assertCollectionEquals(set(cache.layers()), layers);
		}

	}

}
