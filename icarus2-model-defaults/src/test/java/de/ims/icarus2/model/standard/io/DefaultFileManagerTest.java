/**
 *
 */
package de.ims.icarus2.model.standard.io;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.io.FileManagerTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.OverrideTest;

/**
 * @author Markus Gärtner
 *
 */
class DefaultFileManagerTest implements FileManagerTest<DefaultFileManager> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends DefaultFileManager> getTestTargetClass() {
		return DefaultFileManager.class;
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public DefaultFileManager createTestInstance(TestSettings settings) {
		return new DefaultFileManager(Paths.get("."));
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#testMandatoryConstructors()
	 */
	@Override
	@OverrideTest
	@Test
	public void testMandatoryConstructors() throws Exception {
		create();
	}
}
