/**
 *
 */
package de.ims.icarus2.test.guard;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestReporter;
import org.opentest4j.TestAbortedException;

import de.ims.icarus2.apiguard.Api;

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
	//TODO use this flag to actually buibld tests
	private Boolean testProperties;

	private final List<Guardian> guardians = new ArrayList<>(3);

	public ApiGuard(Class<T> targetClass) {
		this.targetClass = requireNonNull(targetClass);
		checkGuardableClass(targetClass);

		guardians.add(new ConstructorGuardian(targetClass));
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

	//-------------------------------------
	// Code taken from:
	// https://stackoverflow.com/questions/10082619/how-do-java-
	// method-annotations-work-in-conjunction-with-method-overriding

	//TODO optimize solution

	public static <A extends Annotation> A getInheritedAnnotation(
			Class<A> annotationClass, AnnotatedElement element) {
		A annotation = element.getAnnotation(annotationClass);
		if (annotation == null && element instanceof Method)
			annotation = getOverriddenAnnotation(annotationClass, (Method) element);
		return annotation;
	}

	private static <A extends Annotation> A getOverriddenAnnotation(Class<A> annotationClass, Method method) {
		final Class<?> methodClass = method.getDeclaringClass();
		final String name = method.getName();
		final Class<?>[] params = method.getParameterTypes();

		// prioritize all superclasses over all interfaces
		final Class<?> superclass = methodClass.getSuperclass();
		if (superclass != null) {
			final A annotation = getOverriddenAnnotationFrom(annotationClass, superclass, name, params);
			if (annotation != null)
				return annotation;
		}

		// depth-first search over interface hierarchy
		for (final Class<?> intf : methodClass.getInterfaces()) {
			final A annotation = getOverriddenAnnotationFrom(annotationClass, intf, name, params);
			if (annotation != null)
				return annotation;
		}

		return null;
	}

	private static <A extends Annotation> A getOverriddenAnnotationFrom(Class<A> annotationClass, Class<?> searchClass,
			String name, Class<?>[] params) {
		try {
			final Method method = searchClass.getMethod(name, params);
			final A annotation = method.getAnnotation(annotationClass);
			if (annotation != null)
				return annotation;
			return getOverriddenAnnotation(annotationClass, method);
		} catch (final NoSuchMethodException e) {
			return null;
		}
	}

	// END COPY

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

	private Supplier<? extends T> instanceCreator() {
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

		if(isTestProperties()) {
			guardians.add(new PropertyGuardian<>(targetClass, instanceCreator()));
		}

		return guardians.stream()
				.map(guardian -> guardian.createTests(testReporter));
	}
}
