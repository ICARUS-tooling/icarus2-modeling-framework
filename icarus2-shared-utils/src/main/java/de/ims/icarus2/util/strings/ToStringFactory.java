/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.strings;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;

/**
 * Utility class to allow for simple automatic creation of
 * the results of {@link Object#toString()} methods. This
 * class is primarily intended for types that include a lot
 * of fields carrying various states in their {@code toString()}
 * methods.
 * <p>
 * The implementation is not optimized for performance, as
 * {@code toString()} methods are expected to be for logging
 * and/or debugging purposes mainly.
 *
 * @author Markus Gärtner
 *
 */
public class ToStringFactory<T> {

	public static <T> ToStringFactory<T> forClass(Class<T> clazz) {
		requireNonNull(clazz);

		Map<String, FieldHandler> handlers = new HashMap<>();
		String label = null;
		BracketStyle brackets = null;

		Class<?> classToCheck = clazz;

		while(classToCheck != null) {
			ToStringRoot anno = classToCheck.getAnnotation(ToStringRoot.class);

			// Make sure we only consider annotated classes
			if(anno==null) {
				if(classToCheck==clazz)
					throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT,
							"Class must be annotated with ToStringRoot: "+clazz);
				break;
			}

			if(label==null) {
				label = def(anno.label(), ToStringRoot.DEFAULT_LABEL);
			}

			if(brackets==null) {
				brackets = def(anno.brackets(), ToStringRoot.DEFAULT_BRACKET_STYLE);
			}

			boolean complete = anno.complete();

			for(Field field : classToCheck.getDeclaredFields()) {
				if(Modifier.isStatic(field.getModifiers())) {
					continue;
				}

				String key = field.getName();

				// First come - first serve
				if(handlers.containsKey(key)) {
					continue;
				}

				ToStringField fieldAnno = field.getAnnotation(ToStringField.class);
				if(fieldAnno!=null) {
					handlers.put(key, FieldHandler.forAnnotatedField(field, fieldAnno));
				} else if(complete) {
					handlers.put(key, FieldHandler.forField(field));
				}
			}

			classToCheck = anno.inherit() ? clazz.getSuperclass() : null;
		}

		if(handlers.isEmpty())
			throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT,
					"Given class declares no local or inherited fields for toString() construction: "+clazz);

		FieldHandler[] fieldHandlers = handlers.values().toArray(new FieldHandler[handlers.size()]);
		Arrays.sort(fieldHandlers);

		if(label==null) {
			label = clazz.getSimpleName();
		}

		if(brackets==null) {
			brackets = ToStringRoot.DEFAULT_BRACKET_STYLE;
		}

		return new ToStringFactory<>(fieldHandlers, label, brackets);
	}

	private static <T> T def(T value, T defaultValue) {
		if(value!=null && value.equals(defaultValue)) {
			value = null;
		}
		return value;
	}

	private final FieldHandler[] handlers;
	private final String label;
	private final BracketStyle brackets;

	/**
	 * @param handlers
	 * @param label
	 * @param brackets
	 */
	public ToStringFactory(FieldHandler[] handlers, String label, BracketStyle brackets) {
		this.handlers = requireNonNull(handlers);
		this.label = requireNonNull(label);
		this.brackets = requireNonNull(brackets);
	}

	public String toString(T target) {
		requireNonNull(target);

		StringBuilder sb = new StringBuilder(handlers.length * 20);

		sb.append(label).append('@').append(brackets.openingBracket);

		for (int i = 0; i < handlers.length; i++) {
			if(i>0) {
				sb.append(", ");
			}
			FieldHandler handler = handlers[i];
			sb.append(handler.getLabel()).append('=').append(handler.getValue(target));
		}

		sb.append(brackets.closingBracket);

		return sb.toString();
	}

	private static class FieldHandler implements Comparable<FieldHandler> {

		@SuppressWarnings("unchecked")
		static FieldHandler forAnnotatedField(Field field, ToStringField annotation) {
			Function<Object, String> formatter = null;
			if(annotation.formatter()!=ToStringField.DEFAULT_FORMATTER) {
				try {
					formatter = (Function<Object, String>) annotation.formatter().newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					throw new IcarusRuntimeException(GlobalErrorCode.INSTANTIATION_ERROR,
							"Failed to instantiate formatter: "+annotation.formatter(), e);
				}
			}

			String label = def(annotation.label(), ToStringField.DEFAULT_LABEL);

			String format = def(annotation.format(), ToStringField.DEFAULT_FORMAT);

			if(label==null) {
				label = field.getName();
			}

			return new FieldHandler(field, label, format, formatter);
		}

		static FieldHandler forField(Field field) {
			return new FieldHandler(field, field.getName(), null, null);
		}

		private final Field field;
		private final Function<Object, String> formatter;
		private final String label;
		private final String format;

		/**
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(FieldHandler other) {
			return label.compareTo(other.label);
		}

		/**
		 * @param field
		 * @param label
		 * @param format
		 * @param formatter
		 */
		private FieldHandler(Field field, String label, String format, Function<Object, String> formatter) {
			this.field = requireNonNull(field);
			this.label = requireNonNull(label);
			this.format = format;
			this.formatter = formatter;
		}

		String getLabel() {
			return label;
		}

		String getValue(Object target) {
			Object value;
			try {
				value = field.get(target);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new IcarusRuntimeException(GlobalErrorCode.DELEGATION_FAILED,
						"Unable to obtain value for field: "+field.toGenericString(), e);
			}

			if(value==null) {
				return "null";
			}

			if(formatter!=null) {
				return formatter.apply(value);
			}

			if(format!=null) {
				return String.format(format, value);
			}

			return value.toString();
		}
	}
}
