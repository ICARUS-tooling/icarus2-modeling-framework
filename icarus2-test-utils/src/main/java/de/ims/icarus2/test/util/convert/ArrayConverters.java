/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *
 */
package de.ims.icarus2.test.util.convert;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.platform.commons.util.AnnotationUtils;

import de.ims.icarus2.test.annotations.ArrayArg;
import de.ims.icarus2.test.annotations.ArrayFormat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
public class ArrayConverters {

	@ArrayFormat
	private static final Object _annoCatch = new Object();

	private static final ArrayFormat defaultFormat;
	static {
		AnnotatedElement element;
		try {
			element = ArrayConverters.class.getDeclaredField("_annoCatch");
		} catch (NoSuchFieldException | SecurityException e) {
			throw new InternalError(e);
		}
		defaultFormat = AnnotationUtils.findAnnotation(element, ArrayFormat.class)
				.orElseThrow(() -> new InternalError("Missing our default annotation field"));
	}

	public static ArrayFormat defaultFormat() { return defaultFormat; }


	public static abstract class ArrayConverterBase implements ArgumentConverter {

		protected abstract Object convert(String[] items, ParameterContext context,
				Class<?> componentType) throws ArgumentConversionException;

		@Override
		public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
			final ArrayFormat format = context.findAnnotation(ArrayFormat.class).orElse(defaultFormat());

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

	public static abstract class MatrixConverterBase implements ArgumentConverter {

		protected abstract Object convertElement(String raw, ParameterContext context) throws ArgumentConversionException;

		@Override
		public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
			final ArrayFormat format = context.findAnnotation(ArrayFormat.class).orElse(defaultFormat());

			if(!String.class.isInstance(source))
				throw new ArgumentConversionException("Not a String source: "+source);

			final Class<?> targetType = context.getParameter().getType();

			if(!targetType.isArray())
				throw new ArgumentConversionException("Not an array type parameter: "+targetType);

			final Class<?> componentType = targetType.getComponentType();
			final String empty = format.empty();

			String payload = (String)source;

			if(payload==null || payload.isEmpty() || payload.equals(empty)) {
				return Array.newInstance(componentType, 0);
			}
			payload = payload.trim();

			final List<Class<?>> types = new ObjectArrayList<>();
			{
				Class<?> arrayType = targetType;
				while(arrayType.isArray()) {
					types.add(arrayType);
					arrayType = arrayType.getComponentType();
				}
			}

			final int dimensions = types.size();
			final char open = fetch(format.open(), "opening");
			final char close = fetch(format.close(), "closing");
			final char del = fetch(format.delimiter(), "delimiter");

			final List<Object>[] buffer = IntStream.range(0, dimensions)
					.mapToObj(i -> new ObjectArrayList<>())
					.toArray(List[]::new);
			StringBuilder sb = new StringBuilder();

			Object result = null;
			int depth = -1;

			//TODO handle empty sub-arrays ("-" marker)

			for (int i = 0; i < payload.length(); i++) {
				char c = payload.charAt(i);
				if(Character.isWhitespace(c)) {
					// Ignore whitespaces entirely
					continue;
				} else if(c==open) {
					// Begin new array
					//TODO should we do a sanity check against unfinished content here?
					depth++;
				} else if(c==close || c==del) {
					// Finish element
					if(sb.length()>0) {
						String content = sb.toString().trim();
						if(!content.isEmpty() && !content.equals(format.empty())) {
							buffer[depth].add(convertElement(content, context));
						}
						sb.setLength(0);
					}
					if(c==close) {
						// Finish array
						List<Object> list = buffer[depth];
						Object array = Array.newInstance(types.get(depth).getComponentType(), list.size());
						for (int j = 0; j < list.size(); j++) {
							Array.set(array, j, list.get(j));
						}
						list.clear();
						if(--depth<0) {
							result = array;
						} else {
							buffer[depth].add(array);
						}
					}
//				} else if(empty.length()==1 && empty.charAt(0)==c) {
//					// Empty sub-array
//					//TODO creates element of wrong dimension?
//					Object array = Array.newInstance(types.get(depth).getComponentType(), 0);
//					buffer[depth].add(array);
				} else {
					sb.append(c);
				}
			}

			if(result==null)
				throw new ArgumentConversionException("No valid array/matrix");

			return result;
		}

		private char fetch(String s, String field) {
			if(s.isEmpty())
				throw new ArgumentConversionException("Symbol for array declaration must not be empty: '"+field+"'");
			if(s.length()>1)
				throw new ArgumentConversionException("Illegal array '"+field+"' symbol: "+s);
			return s.charAt(0);
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

	public static class StringArrayConverter extends ArrayConverterBase {

		@Override
		protected Object convert(String[] items, ParameterContext context,
				Class<?> componentType)
				throws ArgumentConversionException {
			return items;
		}

	}

	public static class StringMatrixConverter extends MatrixConverterBase {

		@Override
		protected Object convertElement(String raw, ParameterContext context) throws ArgumentConversionException {
			return raw;
		}

	}

	public static class IntegerArrayConverter extends ArrayConverterBase {

		@Override
		protected Object convert(String[] items, ParameterContext context, Class<?> componentType)
				throws ArgumentConversionException {
			return Stream.of(items).map(String::trim).mapToInt(Integer::parseInt).toArray();
		}
	}

	public static class LongArrayConverter extends ArrayConverterBase {

		@Override
		protected Object convert(String[] items, ParameterContext context, Class<?> componentType)
				throws ArgumentConversionException {
			return Stream.of(items).map(String::trim).mapToLong(Long::parseLong).toArray();
		}
	}

	public static class IntegerMatrixConverter extends MatrixConverterBase {

		@Override
		protected Object convertElement(String raw, ParameterContext context) throws ArgumentConversionException {
			return Integer.valueOf(raw.trim());
		}

	}

	public static class LongMatrixConverter extends MatrixConverterBase {

		@Override
		protected Object convertElement(String raw, ParameterContext context) throws ArgumentConversionException {
			return Long.valueOf(raw.trim());
		}

	}

	//TODO add other converter implementations as the need arises
}
