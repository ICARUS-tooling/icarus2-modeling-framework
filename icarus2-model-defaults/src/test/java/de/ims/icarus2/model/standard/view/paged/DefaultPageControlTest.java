/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.view.paged;

import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.test.TestUtils.filledArray;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Nested;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.item.manager.ItemLayerManager;
import de.ims.icarus2.model.api.view.Scope;
import de.ims.icarus2.model.api.view.paged.PageControlTest;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.standard.driver.virtual.VirtualItemLayerManager;
import de.ims.icarus2.model.standard.view.paged.DefaultPageControl.Builder;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.guard.ApiGuard;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.test.util.Triple;
import de.ims.icarus2.util.BuilderTest;

/**
 * @author Markus Gärtner
 *
 */
class DefaultPageControlTest implements PageControlTest<DefaultPageControl> {

	@SuppressWarnings("boxing")
	@Override
	public PagedCorpusView createView(boolean active) {
		ItemLayerManifest layerManifest = mock(ItemLayerManifest.class);
		when(layerManifest.getUID()).thenReturn(Integer.valueOf(1));

		ItemLayer primaryLayer = mock(ItemLayer.class);
		when((ItemLayerManifest)primaryLayer.getManifest()).thenReturn(layerManifest);

		Scope scope = mock(Scope.class);
		when(scope.getPrimaryLayer()).thenReturn(primaryLayer);

		PagedCorpusView view = mock(PagedCorpusView.class);
		when(view.getScope()).thenReturn(scope);
		when(view.isActive()).thenReturn(Boolean.valueOf(active));

		return view;
	}

	/**
	 * @see de.ims.icarus2.model.api.view.paged.PageControlTest#createFilled(int, de.ims.icarus2.model.api.driver.indices.IndexSet[])
	 */
	@Override
	public DefaultPageControl createFilled(PagedCorpusView view, int pageSize, IndexSet... indices) {
		long size = IndexUtils.max(indices);

		assumeTrue(size<100_000, "Requested size would cause too many mocked items to be created: "+size);

		ItemLayer layer = view.getScope().getPrimaryLayer();

		VirtualItemLayerManager itemLayerManager = new VirtualItemLayerManager();
		itemLayerManager.addLayer(layer);

		for (int i = 0; i < size+1; i++) {
			itemLayerManager.addItem(layer, mockItem());
		}

		DefaultPageControl control = DefaultPageControl.builder()
				.pageSize(pageSize)
				.indices(indices)
				.itemLayerManager(itemLayerManager)
				.build();

		control.addNotify(view);

		return control;
	}

	/**
	 * @see de.ims.icarus2.model.api.view.paged.PageControlTest#createUnadded()
	 */
	@Override
	public DefaultPageControl createUnadded() {
		return DefaultPageControl.builder()
				.pageSize(DEFAULT_PAGE_SIZE)
				.indices(DEFAULT_INDICES)
				.itemLayerManager(mock(ItemLayerManager.class))
				.build();
	}

	@Nested
	class ForBuilder implements BuilderTest<DefaultPageControl, DefaultPageControl.Builder> {

		/**
		 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
		 */
		@Override
		public Class<?> getTestTargetClass() {
			return Builder.class;
		}

		/**
		 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
		 */
		@Override
		public Builder createTestInstance(TestSettings settings) {
			return settings.process(DefaultPageControl.builder());
		}

		/**
		 * @see de.ims.icarus2.test.ApiGuardedTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
		 */
		@Override
		public void configureApiGuard(ApiGuard<Builder> apiGuard) {
			BuilderTest.super.configureApiGuard(apiGuard);

			apiGuard.parameterResolver(IndexSet[].class, b -> new IndexSet[2]);
		}

		/**
		 * @see de.ims.icarus2.util.BuilderTest#invalidOps()
		 */
		@Override
		public List<Triple<String, Class<? extends Throwable>, Consumer<? super Builder>>> invalidOps() {
			return list(
					Triple.triple("zero pageSize", IllegalArgumentException.class, b -> b.pageSize(0)),
					Triple.triple("negative pageSize", IllegalArgumentException.class, b -> b.pageSize(-1244)),

					Triple.triple("zero indexCacheSize", IllegalArgumentException.class, b -> b.indexCacheSize(0)),
					Triple.triple("negative indexCacheSize", IllegalArgumentException.class, b -> b.indexCacheSize(-1244)),

					Triple.triple("empty indices", IllegalArgumentException.class, b -> b.indices(new IndexSet[0]))
			);
		}

		/**
		 * @see de.ims.icarus2.util.BuilderTest#invalidConfigurations()
		 */
		@Override
		public List<Pair<String, Consumer<? super Builder>>> invalidConfigurations() {
			return list(
					Pair.pair("missing pageIndexBuffer and indices+pageSize", b -> b.itemLayerManager(mock(ItemLayerManager.class))),
					Pair.pair("missing pageIndexBuffer or pageSize",
							b -> b.itemLayerManager(mock(ItemLayerManager.class)).indices(filledArray(4, IndexSet.class))),
					Pair.pair("missing pageIndexBuffer or indices",
							b -> b.itemLayerManager(mock(ItemLayerManager.class)).pageSize(10))
			);
		}
	}
}
