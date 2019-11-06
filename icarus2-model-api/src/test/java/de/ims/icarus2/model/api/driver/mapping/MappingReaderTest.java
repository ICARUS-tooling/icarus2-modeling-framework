/**
 *
 */
package de.ims.icarus2.model.api.driver.mapping;

import de.ims.icarus2.model.api.io.SynchronizedAccessorTest;

/**
 * @author Markus Gärtner
 *
 */
public interface MappingReaderTest extends SynchronizedAccessorTest<Mapping, MappingReader> {
	/**
	 * @see de.ims.icarus2.model.api.io.SynchronizedAccessorTest#createAccessor(java.lang.Object)
	 */
	@Override
	default MappingReader createAccessor(Mapping source) {
		return source.newReader();
	}
}
