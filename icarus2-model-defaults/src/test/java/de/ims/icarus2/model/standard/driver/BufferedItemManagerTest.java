/**
 *
 */
package de.ims.icarus2.model.standard.driver;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.standard.driver.BufferedItemManager.Builder;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.util.BuilderTest;

/**
 * @author Markus Gärtner
 *
 */
class BufferedItemManagerTest {

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.driver.BufferedItemManager#builder()}.
	 */
	@Test
	void testBuilder() {
		assertThat(BufferedItemManager.builder()).isNotNull();
	}

	//TODO add tests for the main implementation

	@Nested
	class ForBuilder implements BuilderTest<BufferedItemManager, BufferedItemManager.Builder> {

		/**
		 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
		 */
		@Override
		public Class<?> getTestTargetClass() {
			return Builder.class;
		}

		/**
		 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
		 */
		@Override
		public Builder createTestInstance(TestSettings settings) {
			return settings.process(BufferedItemManager.builder());
		}

		//TODO add tests for the cumulative setters
	}
}
