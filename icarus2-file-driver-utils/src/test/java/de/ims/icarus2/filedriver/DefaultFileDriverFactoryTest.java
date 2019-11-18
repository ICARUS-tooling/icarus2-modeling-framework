/**
 *
 */
package de.ims.icarus2.filedriver;

import de.ims.icarus2.model.manifest.api.FactoryTest;

/**
 * @author Markus Gärtner
 *
 */
class DefaultFileDriverFactoryTest implements FactoryTest<DefaultFileDriverFactory, FileDriver> {

	@Override
	public Class<?> getTestTargetClass() {
		return DefaultFileDriverFactory.class;
	}

}
