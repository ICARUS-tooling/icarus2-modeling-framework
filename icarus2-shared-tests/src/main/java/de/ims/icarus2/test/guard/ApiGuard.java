/**
 *
 */
package de.ims.icarus2.test.guard;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestReporter;

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

	/**
	 * If the {@link #getTargetClass() target class} is marked with the
	 * {@link Api} annotation.
	 * @return
	 */
	public ApiGuard<T> testPropertiesIfApi() {
		if(targetClass.getAnnotation(Api.class)!=null) {
			testProperties(true);
		}
		return self();
	}

	public boolean isTestProperties() {
		return testProperties!=null && testProperties.booleanValue();
	}

	public Stream<DynamicNode> createTests(TestReporter testReporter) {

		if(isTestProperties()) {
			guardians.add(new PropertyGuardian(targetClass));
		}

		return guardians.stream()
				.map(guardian -> guardian.createTests(testReporter));
	}
}
