/**
 *
 */
package de.ims.icarus2.test.guard;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.opentest4j.TestAbortedException;

import de.ims.icarus2.apiguard.EnumType;

/**
 * @author Markus Gärtner
 *
 */
abstract class Guardian {


	private static final Map<Class<?>, Object> sharedDummies;

	static {
		Map<Class<?>, Object> map = new HashMap<>();

		// Primitives and their wrappers

		map.put(Byte.TYPE, Byte.valueOf(Byte.MAX_VALUE));
		map.put(Byte.class, Byte.valueOf(Byte.MAX_VALUE));

		map.put(Short.TYPE, Short.valueOf(Short.MAX_VALUE));
		map.put(Short.class, Short.valueOf(Short.MAX_VALUE));

		map.put(Integer.TYPE, Integer.valueOf(Integer.MAX_VALUE));
		map.put(Integer.class, Integer.valueOf(Integer.MAX_VALUE));

		map.put(Character.TYPE, Character.valueOf(Character.MAX_VALUE));
		map.put(Character.class, Character.valueOf(Character.MAX_VALUE));

		map.put(Long.TYPE, Long.valueOf(Long.MAX_VALUE));
		map.put(Long.class, Long.valueOf(Long.MAX_VALUE));

		map.put(Float.TYPE, Float.valueOf(Float.MAX_VALUE));
		map.put(Float.class, Float.valueOf(Float.MAX_VALUE));

		map.put(Double.TYPE, Double.valueOf(Double.MAX_VALUE));
		map.put(Double.class, Double.valueOf(Double.MAX_VALUE));

		map.put(Boolean.TYPE, Boolean.TRUE);
		map.put(Boolean.class, Boolean.TRUE);

		// Known final classes

		map.put(String.class, "test");

		sharedDummies = Collections.unmodifiableMap(map);
	}

	private Map<Class<?>, Supplier<?>> paramFallbacks; //TODO use this for creating specialized parameters

	/**
	 * Fetches and returns an entry from the map of default
	 * values for parameter testing based on the given {@code class}.
	 *
	 * @param clazz
	 * @return
	 */
	final Object getDefaultValue(Class<?> clazz) {
		return sharedDummies.get(clazz);
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
			} else {
				Object value = clazz.getMethod(methodName, String.class)
						.invoke(null, key);
				assertNotNull(value);
				return value;
			}

		} catch (NoSuchMethodException | SecurityException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new TestAbortedException("Failed to obtain enum constants", e);
		}
	}

	/**
	 *
	 * @param clazz
	 * @return
	 */
	Object createParameter(Class<?> clazz) {
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

	/**
	 * Resolves a non-null test value for the given {@code class}.
	 *
	 * @param clazz
	 * @return
	 */
	Object resolveParameter(Class<?> clazz) {
		Object defaultValue = getDefaultValue(clazz);
		if(defaultValue==null) {
			defaultValue = createParameter(clazz);
		}
		return defaultValue;
	}

	private BitSet findNullableParameters(Executable executable) {
		Annotation[][] annotations = executable.getParameterAnnotations();

		BitSet set = new BitSet(annotations.length);

		for (int i = 0; i < annotations.length; i++) {
			for (int j = 0; j < annotations[i].length; j++) {
				if(annotations[i][j].annotationType().equals(Nullable.class)) {
					set.set(i);
				}
			}
		}

		return set;
	}

	private boolean isNullAssignable(Class<?> clazz) {
		return !clazz.isPrimitive();
	}

	private String label(Class<?> clazz) {
		return clazz.getSimpleName();
	}

	Collection<ParamConfig> variateNullParameter(Executable executable) {

		final int paramCount = executable.getParameterCount();

		final BitSet nullables = findNullableParameters(executable);

		// In case all parameters are nullable we don't really have anything to do
		if(nullables.cardinality()==paramCount) {
			return Collections.emptyList();
		}

		final Object[] original = new Object[paramCount];
		final String[] labels = new String[paramCount];

		@SuppressWarnings("rawtypes")
		final Class[] types = executable.getParameterTypes();

		assumeTrue(original.length==types.length);

		for (int i = 0; i < original.length; i++) {
			original[i] = resolveParameter(types[i]);
			labels[i] = label(types[i]);
		}

		List<ParamConfig> result = new ArrayList<>(paramCount);

		for (int i = 0; i < original.length; i++) {

			// Ignore nullable parameters
			if(nullables.get(i)) {
				continue;
			}

			// Also ignore parameters that cannot be assigned null (e.g. primitives)
			if(!isNullAssignable(types[i])) {
				continue;
			}

			// Ensure an interference-free copy of the parameters
			Object[] params = original.clone();
			params[i] = null;

			String[] tmp = labels.clone();
			tmp[i] = "<null>"+tmp[i];
			String label = "("+String.join(", ", tmp)+")";

			result.add(new ParamConfig(params, i, types[i], label));
		}

		return result;
	}

	URI sourceUriFor(Constructor<?> constructor) {
//		try {
//			String s = "classpath:///";
//			s += constructor.getDeclaringClass().getSimpleName().replace('.', '/');
//			return new URI(s);
//		} catch (URISyntaxException e) {
//			throw new TestAbortedException("Failed to create test source URI", e);
//		}
		return null;
	}

	abstract DynamicNode createTests(TestReporter testReporter);

	static DynamicTest createNullTest(ParamConfig config, ThrowingConsumer<Object[]> executor) {
		return DynamicTest.dynamicTest(config.displayName(), null,
				() -> assertReflectionNPE(
						() -> executor.accept(config.params)));
	}

	static void assertReflectionNPE(org.junit.jupiter.api.function.Executable executable) {
		InvocationTargetException exp = assertThrows(InvocationTargetException.class, executable);
		Throwable cause = exp.getCause();
		assertNotNull(cause);
		assertTrue(cause instanceof NullPointerException, "unexpected exception: "+cause);
	}

	static class ParamConfig {
		public final Object[] params;
		public final int nullIndex;
		public final Class<?> paramClass;
		public final String label;

		public ParamConfig(Object[] params, int nullIndex, Class<?> paramClass, String label) {
			this.params = requireNonNull(params);
			this.nullIndex = nullIndex;
			this.paramClass = requireNonNull(paramClass);
			this.label = requireNonNull(label);
		}

		public String displayName() {
			return String.format("[%d] %s",
					Integer.valueOf(nullIndex),
					label);
		}
	}
}
