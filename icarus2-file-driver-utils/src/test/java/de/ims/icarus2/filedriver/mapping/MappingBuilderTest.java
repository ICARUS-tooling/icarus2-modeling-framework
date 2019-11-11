/**
 *
 */
package de.ims.icarus2.filedriver.mapping;

import de.ims.icarus2.filedriver.mapping.AbstractVirtualMapping.AbstractMappingBuilder;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.util.BuilderTest;

/**
 * @author Markus Gärtner
 *
 */
public interface MappingBuilderTest<M extends Mapping, B extends AbstractMappingBuilder<B, M>>
		extends BuilderTest<M, B> {

	// no special configuration on this level needed
}
