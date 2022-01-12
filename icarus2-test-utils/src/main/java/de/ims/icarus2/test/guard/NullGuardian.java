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
package de.ims.icarus2.test.guard;

import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestReporter;

import de.ims.icarus2.apiguard.Unguarded;
import de.ims.icarus2.test.reflect.ClassCache;
import de.ims.icarus2.test.reflect.MethodCache;
import de.ims.icarus2.test.reflect.RefUtils;

/**
 * @author Markus Gärtner
 *
 */
public class NullGuardian<T> extends Guardian<T> {

	private final ClassCache<T> classCache;

	private final boolean forceAccessible;

	private Supplier<? extends T> creator;

	private static final Set<String> methodBlacklist = new HashSet<>();
	static {
		Collections.addAll(methodBlacklist, "equals");
	}

	private static final String[] packageBlacklist = {
			"com.oracle.",
			"com.sun.",
			"java.",
			"jdk",
			"sun.",
			"javax."
	};

	static Predicate<? super Method> createMethodFilter(boolean includeNonPublic) {
		return (m) -> {
			boolean isStatic = Modifier.isStatic(m.getModifiers()); // must not be static
			boolean isObjMethod = m.getDeclaringClass()==Object.class; // ignore all original Object methods
			boolean hasNoParams = m.getParameterCount()==0;

			// Early filtering of unfit methods before we access the parameter array
			if(isStatic || isObjMethod || hasNoParams) {
				return false;
			}

			if(includeNonPublic) {
				if(Modifier.isPrivate(m.getModifiers())) {
					return false;
				}
			} else if(!Modifier.isPublic(m.getModifiers())) {
				return false;
			}

			if(methodBlacklist.contains(m.getName())) {
				return false;
			}

			String pck = m.getDeclaringClass().getName();
			for(String prefix : packageBlacklist) {
				if(pck.startsWith(prefix)) {
					return false;
				}
			}

			// Check if we have at least 1 non.primitive parameter
			for(Class<?> paramClass : m.getParameterTypes()) {
				if(!paramClass.isPrimitive()) {
					return true;
				}
			}

			// All parameters are primitives
			return false;
		};
	}

	/**
	 * @param apiGuard
	 */
	public NullGuardian(ApiGuard<T> apiGuard) {
		super(apiGuard);

		creator = apiGuard.instanceCreator(false);
		forceAccessible = apiGuard.isForceAccessible();

		classCache = ClassCache.<T>builder()
				.targetClass(targetClass)
				.methodFilter(createMethodFilter(apiGuard.isNullGuardNonPublic()))
//				.log(System.out::println)
				.build();
	}

	/**
	 * @see de.ims.icarus2.test.guard.Guardian#createTests(org.junit.jupiter.api.TestReporter)
	 */
	@Override
	Stream<DynamicNode> createTests(TestReporter testReporter) {
		Collection<MethodCache> methodCaches = classCache.getMethodCaches();
		List<DynamicNode> tests = methodCaches.stream()
//				.filter(c -> c.getMethod().isAccessible())
				.filter(c -> !c.hasAnnotation(Unguarded.class))
				.sorted((m1, m2) -> m1.getMethod().getName().compareTo(m2.getMethod().getName()))
				.map(this::createTestsForMethod)
				.collect(Collectors.toCollection(ArrayList::new));
		String displayName = String.format("Null-Guard [%d/%d]",
				Integer.valueOf(tests.size()),
				Integer.valueOf(methodCaches.size()));

		return Stream.of(dynamicContainer(displayName, tests));
	}

	private DynamicNode createTestsForMethod(MethodCache methodCache) {
		Method method = methodCache.getMethod();
		if(forceAccessible) {
			method.setAccessible(true);
		}

		T instance = creator.get(); // we use the same instance for an entire sequence
		Collection<ParamConfig> variations = variateNullParameter(instance, method, methodCache);
		String baseLabel = RefUtils.toSimpleString(method);

		if(variations.isEmpty()) {
			return dynamicContainer(baseLabel+" - no null-guarded arguments",
					Collections.emptyList());
		}

		return dynamicContainer(
				baseLabel+" ["+variations.size()+" null-guarded arguments]",
				sourceUriFor(method),
				variations.stream().map(config ->
					createNullTest(config, args -> {
						method.invoke(instance, args);
					}))) ;
	}

}
