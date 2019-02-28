/**
 *
 */
package de.ims.icarus2.test.guard;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestReporter;

import de.ims.icarus2.test.reflect.RefUtils;

/**
 * @author Markus Gärtner
 *
 */
class ConstructorGuardian<T> extends Guardian<T> {

	private final Class<?> targetClass;

	public ConstructorGuardian(ApiGuard<T> apiGuard) {
		super(apiGuard);
		this.targetClass = requireNonNull(apiGuard.getTargetClass());
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
		Collection<ParamConfig> variations = variateNullParameter(null, constructor);
		String baseLabel = RefUtils.toSimpleString(constructor);

		if(variations.isEmpty()) {
			return dynamicContainer(baseLabel+" - no null-guarded arguments", Collections.emptyList());
		}

		return dynamicContainer(
				baseLabel+" ["+variations.size()+" null-guarded arguments]",
				sourceUriFor(constructor),
				variations.stream().map(config ->
					createNullTest(config, constructor::newInstance)));
	}
}
