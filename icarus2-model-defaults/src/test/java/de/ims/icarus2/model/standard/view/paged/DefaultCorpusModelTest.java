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
/**
 *
 */
package de.ims.icarus2.model.standard.view.paged;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.edit.change.AtomicChange;
import de.ims.icarus2.model.api.members.item.manager.ItemLayerManager;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.registry.CorpusMemberFactory;
import de.ims.icarus2.model.api.view.paged.CorpusModelTest;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.ImplementationLoader;
import de.ims.icarus2.model.standard.corpus.DefaultCorpus;
import de.ims.icarus2.model.standard.driver.virtual.VirtualDriver;
import de.ims.icarus2.model.standard.registry.DefaultCorpusMemberFactory;
import de.ims.icarus2.model.standard.registry.metadata.VirtualMetadataRegistry;
import de.ims.icarus2.model.standard.util.DefaultImplementationLoader;
import de.ims.icarus2.util.AccessMode;

/**
 * @author Markus Gärtner
 *
 */
class DefaultCorpusModelTest implements CorpusModelTest<DefaultCorpusModel> {

	/**
	 * Causes the underlying {@link PageControl} to either load or close the
	 * current page to invoke publication of a change event on this model.
	 *
	 * @see de.ims.icarus2.util.ChangeableTest#invokeChange(de.ims.icarus2.util.Changeable)
	 */
	@Override
	public void invokeChange(DefaultCorpusModel model) {
		PageControl control = model.getView().getPageControl();
		if(control.isPageLoaded()) {
			try {
				control.closePage();
			} catch (InterruptedException e) {
				throw new AssertionError("not supposed to happen!", e);
			}
		} else {
			try {
				control.load();
			} catch (InterruptedException e) {
				throw new AssertionError("not supposed to happen!", e);
			}
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.view.paged.CorpusModelTest#getSupportedAccessModes()
	 */
	@Override
	public Set<AccessMode> getSupportedAccessModes() {
		return EnumSet.allOf(AccessMode.class);
	}

	/**
	 * @see de.ims.icarus2.model.api.view.paged.CorpusModelTest#createView(de.ims.icarus2.util.AccessMode, de.ims.icarus2.model.api.members.item.manager.ItemLayerManager, int)
	 */
	@SuppressWarnings("resource")
	@Override
	public PagedCorpusView createView(AccessMode accessMode,
			ItemLayerManager itemLayerManager, int pageSize, IndexSet...indices) {


		CorpusManifest corpusManifest = createDefaultCorpusManifest();
		CorpusManager corpusManager = createDefaultCorpusManager(corpusManifest);

		Corpus corpus = DefaultCorpus.newBuilder()
				.manager(corpusManager)
				.metadataRegistry(new VirtualMetadataRegistry())
				.manifest(corpusManifest)
				.build();

		final DriverManifest driverManifest = corpusManifest.getContextManifest("context").
				flatMap(ContextManifest::getDriverManifest)
				.get();

		CorpusMemberFactory corpusMemberFactory = new DefaultCorpusMemberFactory(corpusManager) {
			@SuppressWarnings("serial")
			@Override
			public ImplementationLoader<?> newImplementationLoader() {
				return new DefaultImplementationLoader(corpusManager) {
					@Override
					public <T> T instantiate(Class<T> resultClass) {
						if(Driver.class.equals(resultClass)) {
							return resultClass.cast(
									VirtualDriver.newBuilder()
									.itemLayerManager(itemLayerManager)
									.manifest(driverManifest)
									.build());
						}

						return super.instantiate(resultClass);
					}
				};
			}
		};
		when(corpusManager.newFactory()).thenReturn(corpusMemberFactory);

		return DefaultPagedCorpusView.newBuilder()
				.accessMode(accessMode)
				.indices(indices)
				.itemLayerManager(itemLayerManager)
				.scope(corpus.createCompleteScope())
				.pageSize(pageSize)
				.build();
	}

	@Override
	public DefaultCorpusModel createModel(PagedCorpusView view,
			ItemLayerManager itemLayerManager, Consumer<AtomicChange> changeHandler) {

		DefaultCorpusModel model = DefaultCorpusModel.newBuilder()
				.accessMode(view.getAccessMode())
				.itemLayerManager(itemLayerManager)
				.changeHandler(changeHandler)
				.build();

		model.addNotify(view);

		return model;
	}

	/**
	 * @see de.ims.icarus2.util.PartTest#createUnadded()
	 */
	@Override
	public DefaultCorpusModel createUnadded() {

		return DefaultCorpusModel.newBuilder()
				.accessMode(AccessMode.READ_WRITE)
				.itemLayerManager(mock(ItemLayerManager.class))
				.build();
	}
}
