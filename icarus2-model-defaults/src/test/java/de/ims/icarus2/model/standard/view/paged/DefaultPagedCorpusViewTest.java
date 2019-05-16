/**
 *
 */
package de.ims.icarus2.model.standard.view.paged;

import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.MANIFEST_FACTORY;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.registry.CorpusMemberFactory;
import de.ims.icarus2.model.api.view.Scope;
import de.ims.icarus2.model.api.view.paged.PagedCorpusViewTest;
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
import de.ims.icarus2.util.AccessMode;

/**
 * @author Markus Gärtner
 *
 */
class DefaultPagedCorpusViewTest implements PagedCorpusViewTest<DefaultPagedCorpusView> {

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
	 * @see de.ims.icarus2.model.api.view.CorpusViewTest#getSupportedAccessModes()
	 */
	@Override
	public Set<AccessMode> getSupportedAccessModes() {
		return EnumSet.allOf(AccessMode.class);
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends DefaultPagedCorpusView> getTestTargetClass() {
		return DefaultPagedCorpusView.class;
	}

	/**
	 * @see de.ims.icarus2.util.PartTest#createEnvironment()
	 */
	@SuppressWarnings("resource")
	@Override
	public Corpus createEnvironment() {
		return DefaultCorpus.newBuilder()
				.manifest(corpusManifest)
				.metadataRegistry(new VirtualMetadataRegistry())
				.manager(corpusManager)
				.build();
	}

	/**
	 * @see de.ims.icarus2.model.api.view.paged.PagedCorpusViewTest#createView(de.ims.icarus2.model.api.corpus.Corpus, de.ims.icarus2.util.AccessMode, int, de.ims.icarus2.model.api.driver.indices.IndexSet[])
	 */
	@Override
	public DefaultPagedCorpusView createView(Corpus corpus, AccessMode accessMode,
			int pageSize, IndexSet... indices) {
		assertTrue(indices.length>0, "Must define _some_ indices for testing");

		long size = IndexUtils.max(indices);

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
		Scope scope = corpus.createCompleteScope();
		ItemLayer layer = scope.getPrimaryLayer();

		itemLayerManager.clear();
		itemLayerManager.addLayer(layer);

		// Takes care of only filling the manager if size is positive
		if(size<10_000) {
			for (int i = 0; i < size; i++) {
				itemLayerManager.addItem(layer, mockItem());
			}
		}

		return DefaultPagedCorpusView.newBuilder()
				.accessMode(accessMode)
				.scope(scope)
				.itemLayerManager(itemLayerManager)
				.indices(indices)
				.pageSize(pageSize)
				.build();
	}

}