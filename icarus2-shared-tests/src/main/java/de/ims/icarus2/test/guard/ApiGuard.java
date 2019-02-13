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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestReporter;
import org.opentest4j.TestAbortedException;

import de.ims.icarus2.apiguard.Api;
import de.ims.icarus2.apiguard.Unguarded;

/**
 * @author Markus Gärtner
 *
 */
public class ApiGuard<T> {

	/**
	 * Class for which we want guarding tests to be created for
	 */
	private final Class<T> targetClass;

	/**
	 * If the class defines no no-args constructor, we require
	 * external help in constructing the
	 */
	private Supplier<? extends T> noArgsFallback;

	/**
	 * Signals that the api guard should also construct tests that
	 * test simple getters and setters of properties.
	 */
	private Boolean testProperties;

	/**
	 * Signals that a Guardian responsible for testing property
	 * methods should automatically detect the methods to be tested.
	 */
	private Boolean detectUnmarkedMethods;

	private final List<Guardian<T>> guardians = new ArrayList<>(3);

	private final Map<Class<?>, Function<T, ?>> parameterResolvers = new HashMap<>();

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

	public ApiGuard<T> testProperties(boolean testProperties) {
		assertNull(this.testProperties, "testProperties already set");
		this.testProperties = Boolean.valueOf(testProperties);
		return self();
	}

	public ApiGuard<T> detectUnmarkedMethods(boolean detectUnmarkedMethods) {
		assertNull(this.detectUnmarkedMethods, "detectUnmarkedMethods already set");
		this.detectUnmarkedMethods = Boolean.valueOf(detectUnmarkedMethods);
		return self();
	}

	public <V> ApiGuard<T> parameterResolver(Class<V> paramClass,
			Function<T, V> resolver) {
		requireNonNull(paramClass);
		requireNonNull(resolver);

		parameterResolvers.put(paramClass, resolver);

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

	public boolean isDetectUnmarkedMethods() {
		return detectUnmarkedMethods!=null && detectUnmarkedMethods.booleanValue();
	}

	public Map<Class<?>, Function<T, ?>> getParameterResolvers() {
		return Collections.unmodifiableMap(parameterResolvers);
	}

	public Supplier<? extends T> instanceCreator() {
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

		return guardians.stream()
				.map(guardian -> guardian.createTests(testReporter));
	}
}
