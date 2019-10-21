/**
 *
 */
package de.ims.icarus2.filedriver.mapping;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;
import de.ims.icarus2.model.api.driver.mapping.MappingTest.Config;
import de.ims.icarus2.util.io.resource.IOResource;

/**
 * @author Markus Gärtner
 *
 */
class AbstractStoredMappingTest {

	interface BuilderTests {

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.AbstractStoredMapping#getBufferedResource()}.
		 */
		@Test
		default void testGetBufferedResource() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.AbstractStoredMapping#getDriver()}.
		 */
		@Test
		default void testGetDriver() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.AbstractStoredMapping#getSourceLayer()}.
		 */
		@Test
		default void testGetSourceLayer() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.AbstractStoredMapping#getTargetLayer()}.
		 */
		@Test
		default void testGetTargetLayer() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.AbstractStoredMapping#getManifest()}.
		 */
		@Test
		default void testGetManifest() {
			fail("Not yet implemented"); // TODO
		}

	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.mapping.AbstractStoredMapping#delete()}.
	 */
	@Test
	void testDelete() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.mapping.AbstractStoredMapping#optimize()}.
	 */
	@Test
	void testOptimize() {
		fail("Not yet implemented"); // TODO
	}

	public abstract static class AbstractConfig<M extends AbstractStoredMapping> extends Config<M> {
		public BlockCache blockCache;
		public int cacheSize;
		public IOResource resource;
	}
}
