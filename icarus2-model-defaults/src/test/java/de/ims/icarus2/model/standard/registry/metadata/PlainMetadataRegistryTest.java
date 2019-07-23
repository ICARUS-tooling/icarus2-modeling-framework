/**
 *
 */
package de.ims.icarus2.model.standard.registry.metadata;

import de.ims.icarus2.model.api.registry.MetadataRegistryTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.util.io.resource.VirtualIOResource;

/**
 * @author Markus Gärtner
 *
 */
class PlainMetadataRegistryTest implements MetadataRegistryTest<PlainMetadataRegistry> {

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends PlainMetadataRegistry> getTestTargetClass() {
		return PlainMetadataRegistry.class;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public PlainMetadataRegistry createTestInstance(TestSettings settings) {
		return settings.process(new PlainMetadataRegistry(new VirtualIOResource()));
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistryTest#createReadingCopy(de.ims.icarus2.model.api.registry.MetadataRegistry)
	 */
	@Override
	public PlainMetadataRegistry createReadingCopy(PlainMetadataRegistry original) {
		return new PlainMetadataRegistry(original.getResource());
	}
}
