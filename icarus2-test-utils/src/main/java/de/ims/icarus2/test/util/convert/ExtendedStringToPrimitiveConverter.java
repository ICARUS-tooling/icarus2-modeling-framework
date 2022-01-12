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
package de.ims.icarus2.test.util.convert;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.DefaultArgumentConverter;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * Implements a special {@link ArgumentConverter} that converts
 * {@code String} objects into primitives. In addition to the normal
 * conversion performed by {@link DefaultArgumentConverter} it also
 * recognizes string literals such as "max" or "Integer.MAX_VALUE" to
 * stand for their respective value type's extreme values.
 *
 * @author Markus Gärtner
 *
 */
@SuppressWarnings("boxing")
public class ExtendedStringToPrimitiveConverter extends SimpleArgumentConverter {

	private static final Map<Class<?>, Function<String, ?>> CONVERTERS;
	static {
		Map<Class<?>, Function<String, ?>> converters = new HashMap<>();
		converters.put(Boolean.class, Boolean::valueOf);
		converters.put(Character.class, source -> {
			Preconditions.condition(source.length() == 1, () -> "String must have length of 1: " + source);
			return Character.valueOf(source.charAt(0));
		});
		converters.put(Byte.TYPE, new Converter(Byte::valueOf)
				.map("max", Byte.MAX_VALUE)
				.map("Byte.MAX_VALUE", Byte.MAX_VALUE)
				.map("min", Byte.MIN_VALUE)
				.map("Byte.MIN_VALUE", Byte.MIN_VALUE));
		converters.put(Short.TYPE, new Converter(Short::valueOf)
				.map("max", Short.MAX_VALUE)
				.map("Short.MAX_VALUE", Short.MAX_VALUE)
				.map("min", Short.MIN_VALUE)
				.map("Short.MIN_VALUE", Short.MIN_VALUE));
		converters.put(Integer.TYPE, new Converter(Integer::valueOf)
				.map("max", Integer.MAX_VALUE)
				.map("Integer.MAX_VALUE", Integer.MAX_VALUE)
				.map("min", Integer.MIN_VALUE)
				.map("Integer.MIN_VALUE", Integer.MIN_VALUE));
		converters.put(Long.TYPE, new Converter(Long::valueOf)
				.map("max", Long.MAX_VALUE)
				.map("Long.MAX_VALUE", Long.MAX_VALUE)
				.map("min", Long.MIN_VALUE)
				.map("Long.MIN_VALUE", Long.MIN_VALUE));
		converters.put(Float.TYPE, new Converter(Float::valueOf)
				.map("max", Float.MAX_VALUE)
				.map("Float.MAX_VALUE", Float.MAX_VALUE)
				.map("min", Float.MAX_VALUE)
				.map("-Float.MAX_VALUE", Float.MAX_VALUE));
		converters.put(Double.TYPE, new Converter(Double::valueOf)
				.map("max", Double.MAX_VALUE)
				.map("Double.MAX_VALUE", Double.MAX_VALUE)
				.map("min", -Double.MAX_VALUE)
				.map("-Double.MAX_VALUE", Double.MAX_VALUE));
		CONVERTERS = unmodifiableMap(converters);
	}

	private static class Converter implements Function<String, Object> {

		private final Map<String, Object> namedValues = new HashMap<>();
		private final Function<String, ?> fallback;

		public Converter(Function<String, ?> fallback) {
			this.fallback = requireNonNull(fallback);
		}

		@Override
		public Object apply(String t) {
			Object result = namedValues.get(t);
			if(result==null) {
				result = fallback.apply(t);
			}
			return result;
		}

		public Converter map(String s, Object value) {
			assertFalse(namedValues.containsKey(s), "Duplicate mapping attempt for "+s);
			namedValues.put(requireNonNull(s), requireNonNull(value));
			return this;
		}
	}

	@Override
	public Object convert(Object source, Class<?> targetType) {
		assertTrue(targetType.isPrimitive());
		if (source == null)
			throw new ArgumentConversionException(
				"Cannot convert null to primitive value of type " + targetType.getName());

		if (ReflectionUtils.isAssignableTo(source, targetType)) {
			return source;
		}

		return CONVERTERS.get(targetType).apply(String.valueOf(source));
	}
}
