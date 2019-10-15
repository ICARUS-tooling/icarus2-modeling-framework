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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestReporter;
import org.opentest4j.TestAbortedException;

import de.ims.icarus2.apiguard.Api;
import de.ims.icarus2.apiguard.Unguarded;
import de.ims.icarus2.test.util.DummyCache;

/**
 * @author Markus Gärtner
 *
 */
public class ApiGuard<T> extends DummyCache<ApiGuard<T>, T> {

	/**
	 * Class for which we want guarding tests to be created for
	 */
	private final Class<T> targetClass;

	/**
	 * If the class defines no no-args constructor, we require
	 * external help in constructing the test instance.
	 */
	private Supplier<? extends T> noArgsFallback;

	/**
	 * If a class requires complex setup for its test instances,
	 * we cannot use the default no-args constructor fallback
	 * and should be supplied a helper implementation!
	 */
	private Supplier<? extends T> constructorOverride;

	/**
	 * Signals that the api guard should also construct tests that
	 * test simple getters and setters of properties.
	 */
	private Boolean testProperties;

	/**
	 * Signals that a blanket test should be run to guard every
	 * method with non-primitive arguments against passing
	 * {@code null} values.
	 */
	private Boolean nullGuard;

	/**
	 * Signals that a Guardian responsible for testing property
	 * methods should automatically detect the methods to be tested.
	 */
	private Boolean detectUnmarkedMethods;

	/**
	 * Signals that the Guardian responsible for testing property
	 * methods is to use strict filtering when searching for setter
	 * and getter methods. Strict filtering means that only methods
	 * that follow the property naming convention and begin with either
	 * of {@code [set,get,is]} are considered.
	 */
	private Boolean strictNameFilter;

	private final List<Guardian<T>> guardians = new ArrayList<>(3);

	private final Map<String, Object> defaultReturnValues = new HashMap<>();

	public ApiGuard(Class<T> targetClass) {
		this.targetClass = requireNonNull(targetClass);
		checkGuardableClass(targetClass);
	}

	private void checkGuardableClass(Class<?> clazz) {

		assertFalse(clazz.isInterface(), "Test class must not be an interface");
		assertFalse(Modifier.isAbstract(clazz.getModifiers()), "Test class must not be abstract");
		assertFalse(clazz.isArray(), "Test class must not be an array");
		assertFalse(clazz.isAnnotation(), "Test class must not be an annotation");
		assertFalse(clazz.isAnonymousClass(), "Test class must not be anonymous");
		assertFalse(clazz.isPrimitive(), "Test class must not be primitive");
	}

	@Override
	protected ApiGuard<T> self() {
		return this;
	}

	/**
	 * @return the targetClass
	 */
	public Class<T> getTargetClass() {
		return targetClass;
	}

	public ApiGuard<T> noArgsFallback(Supplier<? extends T> noArgsFallback) {
		assertNull(this.noArgsFallback, "noArgsFallback already set");
		this.noArgsFallback = requireNonNull(noArgsFallback);
		return self();
	}

	public ApiGuard<T> constructorOverride(Supplier<? extends T> constructorOverride) {
		assertNull(this.constructorOverride, "constructorOverride already set");
		this.constructorOverride = requireNonNull(constructorOverride);
		return self();
	}

	public ApiGuard<T> testProperties(boolean testProperties) {
		assertNull(this.testProperties, "testProperties already set");
		this.testProperties = Boolean.valueOf(testProperties);
		return self();
	}

	public ApiGuard<T> nullGuard(boolean nullGuard) {
		assertNull(this.nullGuard, "nullGuard already set");
		this.nullGuard = Boolean.valueOf(nullGuard);
		return self();
	}

	public ApiGuard<T> detectUnmarkedMethods(boolean detectUnmarkedMethods) {
		assertNull(this.detectUnmarkedMethods, "detectUnmarkedMethods already set");
		this.detectUnmarkedMethods = Boolean.valueOf(detectUnmarkedMethods);
		return self();
	}

	public ApiGuard<T> strictNameFilter(boolean strictNameFilter) {
		assertNull(this.strictNameFilter, "strictNameFilter already set");
		this.strictNameFilter = Boolean.valueOf(strictNameFilter);
		return self();
	}

	/**
	 * Registers a new default vlaue for the given {@code property} or removes
	 * and previously registered one if the {@code value} parameter is {@code null}.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public ApiGuard<T> defaultReturnValue(String property, Object value) {
		requireNonNull(property);

		if(value==null) {
			defaultReturnValues.remove(property);
		} else {
			defaultReturnValues.put(property, value);
		}

		return self();
	}

	public static boolean isApi(Class<?> clazz) {
		if(clazz.isAnnotationPresent(Api.class)) {
			return true;
		}

		Stack<Class<?>> pending = new Stack<>();
		pending.push(clazz);

		while(!pending.empty()) {
			Class<?> c = pending.pop();
			if(c==Object.class) {
				continue;
			}
			if(c.isAnnotationPresent(Api.class)) {
				return true;
			}

			Class<?> p = c.getSuperclass();
			if(p!=null) {
				pending.push(p);
			}

			Collections.addAll(pending, c.getInterfaces());
		}

		return false;
	}

	/**
	 * If the {@link #getTargetClass() target class} is marked with the
	 * {@link Api} annotation.
	 * @return
	 */
	public ApiGuard<T> testPropertiesIfApi() {
		if(isApi(targetClass)) {
			testProperties(true);
		}
		return self();
	}

	public boolean isTestProperties() {
		return testProperties!=null && testProperties.booleanValue();
	}

	public boolean isNullGuard() {
		return nullGuard!=null && nullGuard.booleanValue();
	}

	public boolean isDetectUnmarkedMethods() {
		return detectUnmarkedMethods!=null && detectUnmarkedMethods.booleanValue();
	}

	public boolean isStrictNameFilter() {
		return strictNameFilter!=null && strictNameFilter.booleanValue();
	}

	public Map<String, Object> getDefaultReturnValues() {
		return Collections.unmodifiableMap(defaultReturnValues);
	}

	public Supplier<? extends T> instanceCreator(boolean allowBlank) {
		if(constructorOverride!=null && !allowBlank) {
			return constructorOverride;
		}

		Constructor<T> noArgsConstructor = null;
		try {
			noArgsConstructor = targetClass.getConstructor();
		} catch (NoSuchMethodException e) {
			if(noArgsFallback==null)
				throw new TestAbortedException("Unable to instantiate "+targetClass);
		}

		Supplier<? extends T> creator = noArgsFallback;

		if(noArgsConstructor!=null) {
			final Constructor<T> constructor = noArgsConstructor;
			creator = () -> {
				try {
					return constructor.newInstance();
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					throw new TestAbortedException("Failed to instantiate "+targetClass, e);
				}
			};
		}

		return creator;
	}

	public Stream<DynamicNode> createTests(TestReporter testReporter) {

		if(targetClass.isAnnotationPresent(Unguarded.class)) {
			return Stream.of(dynamicContainer("unguarded class", Collections.emptyList()));
		}

		guardians.add(new ConstructorGuardian<>(this));

		if(isTestProperties()) {
			guardians.add(new PropertyGuardian<>(this));
		}

		if(isNullGuard()) {
			guardians.add(new NullGuardian<>(this));
		}

		return guardians.stream()
				.map(guardian -> guardian.createTests(testReporter));
	}
}
