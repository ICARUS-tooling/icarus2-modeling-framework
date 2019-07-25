/**
 *
 */
package de.ims.icarus2.model.standard.members;

import de.ims.icarus2.model.api.registry.LayerMemberFactoryTest;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class DefaultLayerMemberFactoryTest implements LayerMemberFactoryTest<DefaultLayerMemberFactory> {

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends DefaultLayerMemberFactory> getTestTargetClass() {
		return DefaultLayerMemberFactory.class;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public DefaultLayerMemberFactory createTestInstance(TestSettings settings) {
		return settings.process(new DefaultLayerMemberFactory());
	}

}
