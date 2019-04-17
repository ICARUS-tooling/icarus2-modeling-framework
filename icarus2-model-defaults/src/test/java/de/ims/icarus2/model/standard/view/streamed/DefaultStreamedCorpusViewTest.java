/**
 *
 */
package de.ims.icarus2.model.standard.view.streamed;

import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.MANIFEST_FACTORY;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.registry.CorpusMemberFactory;
import de.ims.icarus2.model.api.view.Scope;
import de.ims.icarus2.model.api.view.streamed.StreamedCorpusViewTest;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.ImplementationLoader;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
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
class DefaultStreamedCorpusViewTest implements StreamedCorpusViewTest<DefaultStreamedCorpusView> {

	private CorpusManager corpusManager;
	private CorpusMemberFactory corpusMemberFactory;
	private CorpusManifest corpusManifest;
	private Corpus corpus;
	private VirtualItemLayerManager itemLayerManager;

	@SuppressWarnings({ "resource", "boxing", "serial" })
	@BeforeEach
	void setUp() {
		try(ManifestBuilder builder = new ManifestBuilder(MANIFEST_FACTORY)) {
			corpusManifest = builder.create(CorpusManifest.class, "corpus")
					.addRootContextManifest(builder.create(ContextManifest.class, "context", "corpus")
							.setPrimaryLayerId("tokens")
							.setDriverManifest(builder.create(DriverManifest.class, "driver", "context")
									.setImplementationManifest(builder.live(VirtualDriver.class)))
							.addLayerGroup(builder.create(LayerGroupManifest.class, "group", "context")
									.setPrimaryLayerId("tokens")
									.setIndependent(true)
									.addLayerManifest(builder.create(ItemLayerManifest.class, "tokens", "group"))));

			corpusManager = mock(CorpusManager.class);
			when(corpusManager.isCorpusConnected(eq(corpusManifest))).thenReturn(Boolean.TRUE);
			when(corpusManager.isCorpusEnabled(eq(corpusManifest))).thenReturn(Boolean.TRUE);
			when(corpusManager.getImplementationClassLoader(any())).thenReturn(getClass().getClassLoader());

			itemLayerManager = new VirtualItemLayerManager();

			VirtualDriver driver = VirtualDriver.newBuilder()
					.itemLayerManager(itemLayerManager)
					.manifest(builder.fetch("driver"))
					.build();

			DefaultImplementationLoader implementationLoader = new DefaultImplementationLoader(corpusManager) {
				@SuppressWarnings("unchecked")
				@Override
				public <T> T instantiate(Class<T> resultClass) {
					if(Driver.class.equals(resultClass)) {
						return (T) driver;
					}

					return super.instantiate(resultClass);
				}
			};

			corpusMemberFactory = new DefaultCorpusMemberFactory(corpusManager) {
				@Override
				public ImplementationLoader<?> newImplementationLoader() {
					return implementationLoader;
				}
			};
			when(corpusManager.newFactory()).thenReturn(corpusMemberFactory);

			corpus = DefaultCorpus.newBuilder()
					.manifest(corpusManifest)
					.metadataRegistry(new VirtualMetadataRegistry())
					.manager(corpusManager)
					.build();
		}
	}

	/**
	 * @see de.ims.icarus2.util.PartTest#createEnvironment()
	 */
	@Override
	public Corpus createEnvironment() {
		return corpus;
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
	public DefaultStreamedCorpusView createView(Corpus corpus, AccessMode accessMode, long size) {
		Scope scope = corpus.createCompleteScope();
		ItemLayer layer = scope.getPrimaryLayer();

		itemLayerManager.clear();
		itemLayerManager.addLayer(layer);

		// Takes care of only filling the manager if size is positive
		assumeTrue(size<10_000_000); // guard against creating giant amounts of mocks
		for (int i = 0; i < size; i++) {
			itemLayerManager.addItem(layer, mockItem());
		}

		return DefaultStreamedCorpusView.builder()
				.accessMode(accessMode)
				.bufferCapacity((int)Math.min(100, size==UNSET_LONG ? 100 : size/10))
				.scope(scope)
				.itemLayerManager(itemLayerManager)
				.build();
	}
}
