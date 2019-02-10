/**
 *
 */
package de.ims.icarus2.test.guard;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;

import java.lang.reflect.Constructor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestReporter;

/**
 * @author Markus Gärtner
 *
 */
class ConstructorGuardian extends Guardian {

	private final Class<?> targetClass;

	public ConstructorGuardian(Class<?> targetClass) {
		this.targetClass = requireNonNull(targetClass);
	}

	/**
	 * @see de.ims.icarus2.test.guard.Guardian#createTests()
	 */
	@Override
	DynamicNode createTests(TestReporter testReporter) {
		return dynamicContainer("Constructors",
				Stream.of(targetClass.getConstructors())
				.filter(c -> c.getParameterCount()>0)
				.map(this::createTestsForConstructor)
				.collect(Collectors.toList()));
	}

	private DynamicNode createTestsForConstructor(Constructor<?> constructor) {
		return dynamicContainer(
				constructor.toGenericString(),
				sourceUriFor(constructor),
				variateNullParameter(constructor).stream().map(config ->
					createNullTest(config, constructor::newInstance)));
	}
}
