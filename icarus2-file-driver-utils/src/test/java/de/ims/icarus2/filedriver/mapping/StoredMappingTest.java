/**
 *
 */
package de.ims.icarus2.filedriver.mapping;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.verify;

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.filedriver.io.BufferedIOResource;
import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;
import de.ims.icarus2.model.api.driver.mapping.WritableMappingTest;
import de.ims.icarus2.util.io.resource.IOResource;

/**
 * @author Markus Gärtner
 *
 */
public interface StoredMappingTest<M extends AbstractStoredMapping<?>, C extends StoredMappingTest.AbstractConfig<M>>
		extends WritableMappingTest<M, C> {

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.mapping.AbstractStoredMapping#delete()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testDelete() {
		return configurations().map(config -> dynamicTest(config.label, () -> {
			try(M mapping = config.create()) {
				BufferedIOResource resource = mapping.getBufferedResource();
				mapping.delete();
				verify(resource).delete();
			} finally {
				config.close();
			}
		}));
	}

	public abstract static class AbstractConfig<M extends AbstractStoredMapping<?>> extends Config<M> {
		public Supplier<BlockCache> blockCacheGen;
		public int cacheSize;
		public Supplier<IOResource> resourceGen;
	}
}
