/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.test.guard;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.function.ThrowingConsumer;

import de.ims.icarus2.test.reflect.MethodCache;
import de.ims.icarus2.test.util.DummyCache;

/**
 * @author Markus Gärtner
 *
 */
abstract class Guardian<T> {

//	private final Map<Class<?>, Function<T, ?>> parameterResolvers;

	private final Map<String, Object> defaultReturnValues;
	private final DummyCache<?, T> dummyCache;

	protected Guardian(ApiGuard<T> apiGuard) {
		requireNonNull(apiGuard);

		dummyCache = apiGuard;
		defaultReturnValues = requireNonNull(apiGuard.getDefaultReturnValues());
	}

	final Object getDefaultReturnValue(String property, Class<?> returnType) {
		return Optional.ofNullable(defaultReturnValues.get(property))
				.orElseThrow(() -> new IllegalStateException(
						"No valid default return value defined for property '"+property+"'"));
	}

	private BitSet findNullableParameters(Executable executable, MethodCache methodCache) {
		Annotation[][] annotations = executable.getParameterAnnotations();

		BitSet set = new BitSet(annotations.length);

		for (int i = 0; i < annotations.length; i++) {
			for (int j = 0; j < annotations[i].length; j++) {
				if(annotations[i][j].annotationType().equals(Nullable.class)) {
					set.set(i);
				}
			}
		}

		if(methodCache!=null) {
			for (int i = 0; i < executable.getParameterCount(); i++) {
				if(!set.get(i) && methodCache.hasParameterAnnotation(Nullable.class, i)) {
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

	protected Object resolveParameter(T instance, Class<?> clazz) {
		return dummyCache.createDummy(instance, clazz);
	}

	Collection<ParamConfig> variateNullParameter(T instance,
			Executable executable, MethodCache methodCache) {

		final int paramCount = executable.getParameterCount();

		final BitSet nullables = findNullableParameters(executable, methodCache);

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
			original[i] = resolveParameter(instance, types[i]);
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

	URI sourceUriFor(Executable executable) {
//		try {
//			String s = "classpath:///";
//			s += executable.getDeclaringClass().getSimpleName().replace('.', '/');
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

	static void assertReflectionNPE(ThrowingCallable callable) {
		assertThatExceptionOfType(InvocationTargetException.class)
			.isThrownBy(callable)
			.withCauseExactlyInstanceOf(NullPointerException.class);
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
