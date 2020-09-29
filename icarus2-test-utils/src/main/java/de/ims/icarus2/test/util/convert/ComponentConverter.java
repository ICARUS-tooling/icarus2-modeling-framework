/**
 *
 */
package de.ims.icarus2.test.util.convert;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;

/**
 * @author Markus Gärtner
 *
 */
public interface ComponentConverter {

	Object convert(Object source, ParameterContext context, Class<?> componentType)
			throws ArgumentConversionException;
}
