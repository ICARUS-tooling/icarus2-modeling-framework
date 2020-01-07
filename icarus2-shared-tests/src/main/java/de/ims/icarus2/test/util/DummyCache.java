/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.test.util;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.opentest4j.TestAbortedException;

import de.ims.icarus2.apiguard.EnumType;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.guard.GuardException;

/**
 * @author Markus Gärtner
 *
 */
public class DummyCache<C extends DummyCache<C, T>, T> {


	private static final Map<Class<?>, Object> sharedDummies;

	static {
		Map<Class<?>, Object> map = new HashMap<>();

		// Primitives and their wrappers

		map.put(Byte.TYPE, Byte.valueOf((byte) (Byte.MAX_VALUE/2)));
		map.put(Byte.class, Byte.valueOf((byte) (Byte.MAX_VALUE/2)));

		map.put(Short.TYPE, Short.valueOf((short) (Short.MAX_VALUE/2)));
		map.put(Short.class, Short.valueOf((short) (Short.MAX_VALUE/2)));

		map.put(Integer.TYPE, Integer.valueOf(Integer.MAX_VALUE/2));
		map.put(Integer.class, Integer.valueOf(Integer.MAX_VALUE/2));

		map.put(Character.TYPE, Character.valueOf((char) (Character.MAX_VALUE/2)));
		map.put(Character.class, Character.valueOf((char) (Character.MAX_VALUE/2)));

		map.put(Long.TYPE, Long.valueOf(Long.MAX_VALUE/2));
		map.put(Long.class, Long.valueOf(Long.MAX_VALUE/2));

		map.put(Float.TYPE, Float.valueOf(Float.MAX_VALUE));
		map.put(Float.class, Float.valueOf(Float.MAX_VALUE));

		map.put(Double.TYPE, Double.valueOf(Double.MAX_VALUE));
		map.put(Double.class, Double.valueOf(Double.MAX_VALUE));

		map.put(Boolean.TYPE, Boolean.TRUE);
		map.put(Boolean.class, Boolean.TRUE);

		// Known final classes

		map.put(String.class, "test");
		map.put(URL.class, TestUtils.TEST_URL);
		map.put(Class.class, TestUtils.class);

		sharedDummies = Collections.unmodifiableMap(map);
	}


	private final Map<Class<?>, Function<T, ?>> dummyFactories = new HashMap<>();

	@SuppressWarnings("unchecked")
	protected C self() {
		return (C) this;
	}

	public <V> DummyCache<C, T> parameterResolver(Class<V> paramClass,
			Function<T, V> resolver) {
		requireNonNull(paramClass);
		requireNonNull(resolver);

		dummyFactories.put(paramClass, resolver);

		return self();
	}

	public Map<Class<?>, Function<T, ?>> getDummyFactories() {
		return Collections.unmodifiableMap(dummyFactories);
	}

	/**
	 * Resolves a non-null test value for the given {@code class}.
	 *
	 * @param clazz
	 * @return
	 */
	public Object createDummy(T instance, Class<?> clazz) {
		Object value = null;

		Function<T, ?> factory = dummyFactories.get(clazz);
		if(factory!=null) {
			value = factory.apply(instance);
		}

		if(value==null) {
			value = getDefaultDummy(clazz);
		}
		if(value==null) {
			value = createDummy(clazz);
		}
		return value;
	}

	/**
	 * Fetches and returns an entry from the map of default
	 * values for parameter testing based on the given {@code class}.
	 *
	 * @param clazz
	 * @return
	 */
	private Object getDefaultDummy(Class<?> clazz) {
		return sharedDummies.get(clazz);
	}

	/**
	 *
	 * @param clazz
	 * @return
	 */
	private Object createDummy(Class<?> clazz) {

		if(clazz.isEnum()) {
			// Assumes that the enum actually has values defined
			return clazz.getEnumConstants()[0];
		} else if(clazz.isArray()) {
			return Array.newInstance(clazz.getComponentType(), 0);
		}

		EnumType enumType = clazz.getAnnotation(EnumType.class);
		if(enumType!=null) {
			return fetchEnumValue(clazz, enumType);
		}

		if(Modifier.isFinal(clazz.getModifiers()))
			throw new GuardException("Cannot mock final class: "+clazz);

		/*
		 *  We could check for a no-args constructor and use it,
		 *  but most likely this would just be prone to injecting
		 *  possible issues into test code where they are not expected.
		 *
		 *  So per default we will just mock every type that we
		 *  cannot provide a simpler solution for...
		 */

		return mock(clazz);
	}

	private Object fetchEnumValue(Class<?> clazz, EnumType annotation) {
		String methodName = annotation.method();
		String key = annotation.key();

		assertNotNull(methodName);

		try {
			if(key==null || key.isEmpty()) {
				Object enums = clazz.getMethod(methodName).invoke(null);
				assertNotNull(enums);

				if(enums.getClass().isArray()) {
					return Array.get(enums, 0);
				} else if(enums instanceof Collection) {
					return ((Collection<?>)enums).iterator().next();
				} else
					throw new TestAbortedException("Unsupported return type of enum retreival method: "+methodName);
			}

			Object value = clazz.getMethod(methodName, String.class)
					.invoke(null, key);
			assertNotNull(value);
			return value;

		} catch (NoSuchMethodException | SecurityException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new TestAbortedException("Failed to obtain enum constants", e);
		}
	}
}
