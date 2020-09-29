/**
 *
 */
package de.ims.icarus2.test.util.convert;

import java.lang.reflect.Array;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

import de.ims.icarus2.test.annotations.ConvertAsArray;

/**
 * @author Markus Gärtner
 *
 */
public class ArrayConverter implements ArgumentConverter {

	/**
	 * @see org.junit.jupiter.params.converter.ArgumentConverter#convert(java.lang.Object, org.junit.jupiter.api.extension.ParameterContext)
	 */
	@Override
	public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
		ConvertAsArray info = context.findAnnotation(ConvertAsArray.class).orElseThrow(
				() -> new ArgumentConversionException("Cannot convert array without "
						+ "de.ims.icarus2.test.annotations.ConvertAsArray annotation!"));

		final Class<?> targetType = context.getParameter().getType();

		if(!targetType.isArray())
			throw new ArgumentConversionException("Not an array type parameter: "+targetType);

		final Class<?> componentType = targetType.getComponentType();

		if(!String.class.isInstance(source))
			throw new ArgumentConversionException("Not a String source: "+source);

		String payload = (String)source;

		if(payload==null || payload.isEmpty() || payload.equals(info.empty())) {
			return Array.newInstance(componentType, 0);
		}
		payload = payload.trim();

		int left = 0;
		if(!info.open().isEmpty()) {
			if(!payload.startsWith(info.open()))
				throw new ArgumentConversionException(String.format("Expected %s to start with %s",
						payload, info.open()));
			left += info.open().length();
		}

		int right = payload.length();
		if(!info.close().isEmpty()) {
			if(!payload.endsWith(info.close()))
				throw new ArgumentConversionException(String.format("Expected %s to end with %s",
						payload, info.close()));
			right -= info.close().length();
		}

		payload = payload.substring(left, right);

		String[] items = payload.split(info.delimiter());

		ComponentConverter converter;
		try {
			converter = info.componentConverter().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ArgumentConversionException("Failed to instantiate component converter", e);
		}

		Object result = Array.newInstance(componentType, items.length);

		for (int i = 0; i < items.length; i++) {
			Array.set(result, i, converter.convert(items[i], context, componentType));
		}

		return result;
	}

}
