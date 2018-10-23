/**
 *
 */
package de.ims.icarus2.model.manifest.standard;

import de.ims.icarus2.model.manifest.api.HierarchyTest;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class HierarchyImplTest implements HierarchyTest<Object, HierarchyImpl<Object>> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends HierarchyImpl<Object>> getTestTargetClass() {
		return (Class<? extends HierarchyImpl<Object>>) HierarchyImpl.class;
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public HierarchyImpl<Object> createTestInstance(TestSettings settings) {
		return settings.process(new HierarchyImpl<>());
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.HierarchyTest#mockItem()
	 */
	@Override
	public Object mockItem() {
		return new Object();
	}

}
