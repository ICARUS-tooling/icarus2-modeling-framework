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
package de.ims.icarus2.model.standard.view.streamed;

import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.MANIFEST_FACTORY;
import static de.ims.icarus2.test.util.Triple.triple;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.manager.ItemLayerManager;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.registry.CorpusMemberFactory;
import de.ims.icarus2.model.api.view.Scope;
import de.ims.icarus2.model.api.view.streamed.StreamOption;
import de.ims.icarus2.model.api.view.streamed.StreamedCorpusViewTest;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.ImplementationLoader;
import de.ims.icarus2.model.manifest.util.ManifestBuilder;
import de.ims.icarus2.model.standard.corpus.DefaultCorpus;
import de.ims.icarus2.model.standard.driver.virtual.VirtualDriver;
import de.ims.icarus2.model.standard.driver.virtual.VirtualItemLayerManager;
import de.ims.icarus2.model.standard.registry.DefaultCorpusMemberFactory;
import de.ims.icarus2.model.standard.registry.metadata.VirtualMetadataRegistry;
import de.ims.icarus2.model.standard.util.DefaultImplementationLoader;
import de.ims.icarus2.model.standard.view.streamed.DefaultStreamedCorpusView.Builder;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.util.Triple;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.BuilderTest;

/**
 * @author Markus Gärtner
 *
 */
class DefaultStreamedCorpusViewTest implements StreamedCorpusViewTest<DefaultStreamedCorpusView> {

	private CorpusManager corpusManager;
	private CorpusManifest corpusManifest;
	private VirtualItemLayerManager itemLayerManager;

	@BeforeEach
	void setUp() {
		try(ManifestBuilder builder = new ManifestBuilder(MANIFEST_FACTORY)) {
			corpusManifest = createDefaultCorpusManifest();

			corpusManager = createDefaultCorpusManager(corpusManifest);
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.view.streamed.StreamedCorpusViewTest#getSupportedOptions()
	 */
	@Override
	public Set<StreamOption> getSupportedOptions() {
		return EnumSet.allOf(StreamOption.class);
	}

	/**
	 * @see de.ims.icarus2.util.PartTest#createEnvironment()
	 */
	@Override
	public Corpus createEnvironment() {
		return DefaultCorpus.builder()
				.manifest(corpusManifest)
				.metadataRegistry(new VirtualMetadataRegistry())
				.manager(corpusManager)
				.build();
	}

	/**
	 * @see de.ims.icarus2.model.api.view.CorpusViewTest#getSupportedAccessModes()
	 */
	@Override
	public Set<AccessMode> getSupportedAccessModes() {
		return EnumSet.of(AccessMode.READ, AccessMode.READ_WRITE);
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends DefaultStreamedCorpusView> getTestTargetClass() {
		return DefaultStreamedCorpusView.class;
	}

	/**
	 * @see de.ims.icarus2.model.api.view.streamed.StreamedCorpusViewTest#createView(de.ims.icarus2.model.api.corpus.Corpus, de.ims.icarus2.util.AccessMode, long)
	 */
	@Override
	public DefaultStreamedCorpusView createView(Corpus corpus, AccessMode accessMode,
			long size, int capacity) {

		itemLayerManager = new VirtualItemLayerManager();

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
									VirtualDriver.builder()
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
		Scope scope = corpus.createCompleteScope();
		ItemLayer layer = scope.getPrimaryLayer();

		itemLayerManager.clear();
		itemLayerManager.addLayer(layer);

		// Takes care of only filling the manager if size is positive and sensible
		assumeTrue(size<=10_000, "Larger size would cause too many item mocks to be created");
		for (int i = 0; i < size; i++) {
			itemLayerManager.addItem(layer, mockItem());
		}

		if(capacity==UNSET_LONG) {
			capacity = 100;
		}
		if(size==UNSET_LONG) {
			size = 1000;
		}
		if(capacity>size) {
			capacity = (int) (size/10);
		}

		return DefaultStreamedCorpusView.builder()
				.accessMode(accessMode)
				.bufferCapacity(capacity)
				.scope(scope)
				.itemLayerManager(itemLayerManager)
				.withAllOptions()
				.build();
	}

	/**
	 * @see de.ims.icarus2.model.api.view.streamed.StreamedCorpusViewTest#getRawItemStream()
	 */
	@Override
	public Stream<Item> getRawItemStream() {

		ItemLayer layer = itemLayerManager.getItemLayers().get(0);

		return itemLayerManager.getRootContainer(layer).elements();
	}

	/**
	 * @see de.ims.icarus2.model.api.view.streamed.StreamedCorpusViewTest#getItemLayerManager()
	 */
	@Override
	public ItemLayerManager getItemLayerManager() {
		return itemLayerManager;
	}

	@Nested
	class ForBuilder implements BuilderTest<DefaultStreamedCorpusView, DefaultStreamedCorpusView.Builder> {

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
			return settings.process(DefaultStreamedCorpusView.builder());
		}

		/**
		 * @see de.ims.icarus2.util.BuilderTest#invalidOps()
		 */
		@Override
		public List<Triple<String, Class<? extends Throwable>, Consumer<? super Builder>>> invalidOps() {
			return list(
					triple("zero buffer capacity", IllegalArgumentException.class, b -> b.bufferCapacity(0)),
					triple("negative buffer capacity", IllegalArgumentException.class, b -> b.bufferCapacity(-1234)),
					triple("empty options array", IllegalArgumentException.class, b -> b.streamOptions())
			);
		}

		@Test
		void testWithAllOptions() {
			Builder builder = create();
			StreamOption[] options = StreamOption.values();

			builder.withAllOptions();
			Set<StreamOption> actual = builder.getStreamOptions();

			for (StreamOption option : options) {
				if(builder.isOptionSupported(option)) {
					assertThat(actual).as("Missing supported option").contains(option);
				} else {
					assertThat(actual).as("Option not supposed to be supported").doesNotContain(option);
				}
			}
		}

		@TestFactory
		Stream<DynamicTest> testStreamOptions() {
			return Stream.of(StreamOption.values())
					.map(option -> dynamicTest(option.name(), () -> {
						Builder builder = create();
						builder.streamOptions(option);
						if(builder.isOptionSupported(option)) {
							assertThat(builder.getStreamOptions()).as("Missing supported option").containsOnly(option);
						} else {
							assertThat(builder.getStreamOptions()).as("Option not supposed to be supported").doesNotContain(option);
						}
					}));
		}
	}
}
