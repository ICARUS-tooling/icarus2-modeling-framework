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
class JAXBMetadataRegistryTest implements MetadataRegistryTest<JAXBMetadataRegistry> {

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends JAXBMetadataRegistry> getTestTargetClass() {
		return JAXBMetadataRegistry.class;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public JAXBMetadataRegistry createTestInstance(TestSettings settings) {
		return settings.process(new JAXBMetadataRegistry(new VirtualIOResource()));
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistryTest#createReadingCopy(de.ims.icarus2.model.api.registry.MetadataRegistry)
	 */
	@Override
	public JAXBMetadataRegistry createReadingCopy(JAXBMetadataRegistry original) {
		return new JAXBMetadataRegistry(original.getResource());
	}

}
