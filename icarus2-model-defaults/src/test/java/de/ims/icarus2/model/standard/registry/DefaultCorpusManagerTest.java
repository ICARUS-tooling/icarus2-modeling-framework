/**
 *
 */
package de.ims.icarus2.model.standard.registry;

import java.nio.file.Paths;
import java.util.function.BiFunction;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.registry.CorpusManagerTest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.standard.DefaultManifestRegistry;
import de.ims.icarus2.model.standard.io.DefaultFileManager;
import de.ims.icarus2.model.standard.registry.metadata.VirtualMetadataRegistry;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.util.io.resource.VirtualResourceProvider;

/**
 * @author Markus Gärtner
 *
 */
class DefaultCorpusManagerTest implements CorpusManagerTest<DefaultCorpusManager> {

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends DefaultCorpusManager> getTestTargetClass() {
		return DefaultCorpusManager.class;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public DefaultCorpusManager createTestInstance(TestSettings settings) {
		return settings.process(DefaultCorpusManager.newBuilder()
				.fileManager(new DefaultFileManager(Paths.get(".")))
				.resourceProvider(new VirtualResourceProvider())
				.manifestRegistry(new DefaultManifestRegistry())
				.metadataRegistry(new VirtualMetadataRegistry())
				.build());
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManagerTest#createWithCustomProducer(java.util.function.Function)
	 */
	@Override
	public DefaultCorpusManager createCustomManager(BiFunction<CorpusManager, CorpusManifest, Corpus> corpusProducer, TestSettings settings) {
		return settings.process(DefaultCorpusManager.newBuilder()
				.fileManager(new DefaultFileManager(Paths.get(".")))
				.resourceProvider(new VirtualResourceProvider())
				.manifestRegistry(new DefaultManifestRegistry())
				.metadataRegistry(new VirtualMetadataRegistry())
				.corpusProducer(corpusProducer)
				.build());
	}

}
