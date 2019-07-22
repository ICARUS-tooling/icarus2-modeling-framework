/**
 *
 */
package de.ims.icarus2.model.standard.registry.metadata;

import de.ims.icarus2.model.api.registry.MetadataRegistryTest;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class VirtualMetadataRegistryTest implements MetadataRegistryTest<VirtualMetadataRegistry> {

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends VirtualMetadataRegistry> getTestTargetClass() {
		return VirtualMetadataRegistry.class;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public VirtualMetadataRegistry createTestInstance(TestSettings settings) {
		return settings.process(new VirtualMetadataRegistry());
	}

}
