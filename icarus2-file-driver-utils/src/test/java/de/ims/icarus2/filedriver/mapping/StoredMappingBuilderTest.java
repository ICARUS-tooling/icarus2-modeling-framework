/**
 *
 */
package de.ims.icarus2.filedriver.mapping;

import static de.ims.icarus2.util.lang.Primitives._int;

import de.ims.icarus2.filedriver.mapping.AbstractStoredMapping.AbstractStoredMappingBuilder;
import de.ims.icarus2.test.guard.ApiGuard;

/**
 * @author Markus Gärtner
 *
 */
public interface StoredMappingBuilderTest<M extends AbstractStoredMapping<?>, B extends AbstractStoredMappingBuilder<B, M>>
		extends MappingBuilderTest<M, B>{

	/**
	 * @see de.ims.icarus2.test.ApiGuardedTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
	 */
	@Override
	default void configureApiGuard(ApiGuard<B> apiGuard) {
		MappingBuilderTest.super.configureApiGuard(apiGuard);
		apiGuard.defaultReturnValue("cacheSize", _int(AbstractStoredMapping.DEFAULT_CACHE_SIZE));
	}
}
