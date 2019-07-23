/**
 *
 */
package de.ims.icarus2.util.io.resource;

import java.nio.file.Path;
import java.nio.file.Paths;

import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class VirtualResourceProviderTest implements ResourceProviderTest<VirtualResourceProvider> {

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends VirtualResourceProvider> getTestTargetClass() {
		return VirtualResourceProvider.class;
	}

	/**
	 * @see de.ims.icarus2.util.io.resource.ResourceProviderTest#cleanup(de.ims.icarus2.util.io.resource.ResourceProvider, java.nio.file.Path[])
	 */
	@Override
	public void cleanup(VirtualResourceProvider provider, Path... paths) {
		provider.clear();
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public VirtualResourceProvider createTestInstance(TestSettings settings) {
		return settings.process(new VirtualResourceProvider());
	}

	/**
	 * @see de.ims.icarus2.util.io.resource.ResourceProviderTest#createRoot()
	 */
	@Override
	public Path createRoot() {
		return Paths.get("root");
	}

}
