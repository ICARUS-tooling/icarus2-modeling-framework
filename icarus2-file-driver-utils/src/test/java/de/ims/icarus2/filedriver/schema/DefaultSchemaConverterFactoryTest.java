/**
 *
 */
package de.ims.icarus2.filedriver.schema;

import de.ims.icarus2.filedriver.Converter;
import de.ims.icarus2.model.manifest.api.FactoryTest;

/**
 * @author Markus Gärtner
 *
 */
class DefaultSchemaConverterFactoryTest implements FactoryTest<DefaultSchemaConverterFactory, Converter> {

	@Override
	public Class<?> getTestTargetClass() {
		return DefaultSchemaConverterFactory.class;
	}

	//TODO create actual tests for some edge cases
}
