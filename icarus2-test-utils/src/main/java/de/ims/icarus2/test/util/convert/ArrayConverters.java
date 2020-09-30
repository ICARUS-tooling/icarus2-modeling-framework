/**
 *
 */
package de.ims.icarus2.test.util.convert;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.platform.commons.util.AnnotationUtils;

import de.ims.icarus2.test.annotations.ArrayArg;
import de.ims.icarus2.test.annotations.ArrayFormat;

/**
 * @author Markus Gärtner
 *
 */
public class ArrayConverters {

	static abstract class ArrayConverterBase implements ArgumentConverter {

		@ArrayFormat
		private static final Object _annoCatch = new Object();

		private static final ArrayFormat defaultFormat;
		static {
			AnnotatedElement element;
			try {
				element = ArrayConverterBase.class.getDeclaredField("_annoCatch");
			} catch (NoSuchFieldException | SecurityException e) {
				throw new InternalError(e);
			}
			defaultFormat = AnnotationUtils.findAnnotation(element, ArrayFormat.class)
					.orElseThrow(() -> new InternalError("Missing our default annotation field"));
		}

		protected abstract Object convert(String[] items, ParameterContext context,
				Class<?> componentType) throws ArgumentConversionException;

		/**
		 * @see org.junit.jupiter.params.converter.ArgumentConverter#convert(java.lang.Object, org.junit.jupiter.api.extension.ParameterContext)
		 */
		@Override
		public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
			final ArrayFormat format = context.findAnnotation(ArrayFormat.class).orElse(defaultFormat);

			if(!String.class.isInstance(source))
				throw new ArgumentConversionException("Not a String source: "+source);

			final Class<?> targetType = context.getParameter().getType();

			if(!targetType.isArray())
				throw new ArgumentConversionException("Not an array type parameter: "+targetType);

			final Class<?> componentType = targetType.getComponentType();

			String payload = (String)source;

			if(payload==null || payload.isEmpty() || payload.equals(format.empty())) {
				return Array.newInstance(componentType, 0);
			}
			payload = payload.trim();

			int left = 0;
			if(!format.open().isEmpty()) {
				if(!payload.startsWith(format.open()))
					throw new ArgumentConversionException(String.format("Expected %s to start with %s",
							payload, format.open()));
				left += format.open().length();
			}

			int right = payload.length();
			if(!format.close().isEmpty()) {
				if(!payload.endsWith(format.close()))
					throw new ArgumentConversionException(String.format("Expected %s to end with %s",
							payload, format.close()));
				right -= format.close().length();
			}

			payload = payload.substring(left, right);

			final String[] items = payload.split(format.delimiter());

			return convert(items, context, componentType);
		}

	}

	public static class GenericArrayConverter extends ArrayConverterBase {

		@Override
		protected Object convert(String[] items, ParameterContext context,
				Class<?> componentType)
				throws ArgumentConversionException {

			final ArrayArg info = context.findAnnotation(ArrayArg.class).orElseThrow(
					() -> new ArgumentConversionException("Cannot convert array without "
							+ "de.ims.icarus2.test.annotations.ArrayArg annotation!"));

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

	public static class IntegerArrayConverter extends ArrayConverterBase {

		@Override
		protected Object convert(String[] items, ParameterContext context, Class<?> componentType)
				throws ArgumentConversionException {
			return Stream.of(items).mapToInt(Integer::parseInt).toArray();
		}
	}

	//TODO add other converter implementations as the need arises
}
