/**
 *
 */
package de.ims.icarus2.test.random;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.commons.util.ReflectionUtils.isPrivate;
import static org.junit.platform.commons.util.ReflectionUtils.makeAccessible;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode;

import de.ims.icarus2.test.annotations.Seed;

/**
 * @author Markus Gärtner
 *
 */
public class Randomized implements BeforeAllCallback, BeforeEachCallback, ParameterResolver {

	/** Test seed derived from this class' name */
	static final long TEST_SEED = computeSeed(Randomized.class.getName());

	/**
	 * @see org.junit.jupiter.api.extension.ParameterResolver#supportsParameter(org.junit.jupiter.api.extension.ParameterContext, org.junit.jupiter.api.extension.ExtensionContext)
	 */
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return (parameterContext.getParameter().getType() == RandomSource.class);
	}

	/**
	 * @see org.junit.jupiter.api.extension.ParameterResolver#resolveParameter(org.junit.jupiter.api.extension.ParameterContext, org.junit.jupiter.api.extension.ExtensionContext)
	 */
	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		Class<?> parameterType = parameterContext.getParameter().getType();
		assertSupportedType("parameter", parameterType);

		return createRandom(extensionContext, extractSeed(parameterContext, extensionContext));
	}

	private void assertSupportedType(String target, Class<?> type) {
		if (type != RandomSource.class) {
			throw new ExtensionConfigurationException("Can only resolve " + target + " of type "
					+ RandomSource.class.getName() + " but was: " + type.getName());
		}
	}

	private Seed extractSeed(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Seed seed = parameterContext.findAnnotation(Seed.class).orElse(null);
		if(seed==null) {
			seed = AnnotationUtils.findAnnotation(extensionContext.getRequiredTestMethod(), Seed.class).orElse(null);
		}
		return seed;
	}

	private static long computeSeed(String s) {
		int len = s.length();
		assertTrue(len>=10, "Textual source of seed too small (needs at least 10 characters): "+s);
		// adapted from String.hashCode()
		long h = 1125899906842597L; // prime

		for (int i = 0; i < len; i++) {
			h = 31 * h + s.charAt(i);
		}
		return h;
	}

	private static long computeSeed(Seed seed) {
		long s = seed.value();
		if(s==-1L) {
			String seedSource = seed.seedSource();
			assertNotEquals("", seedSource, "No valid seed specified");
			s = computeSeed(seedSource);
		}
		return s;
	}

	private static RandomSource createRandom(ExtensionContext context, Seed seed) {
		long randomSeed;

		if(seed!=null) {
			randomSeed = computeSeed(seed);
		} else {
			String seedSource = context.getTestMethod()
					.map(Method::getName)
					.orElseGet(() -> context.getRequiredTestInstance().getClass().getName());
			randomSeed = computeSeed(seedSource);
		}

		return new RandomSource(randomSeed);
	}

	/**
	 * @see org.junit.jupiter.api.extension.BeforeEachCallback#beforeEach(org.junit.jupiter.api.extension.ExtensionContext)
	 */
	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		injectFields(context, context.getRequiredTestInstance(), ReflectionUtils::isNotStatic);
	}

	/**
	 * @see org.junit.jupiter.api.extension.BeforeAllCallback#beforeAll(org.junit.jupiter.api.extension.ExtensionContext)
	 */
	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		injectFields(context, null, ReflectionUtils::isStatic);
	}

	private Seed extractSeed(Class<?> testClass, Field field) {
		Seed seed = AnnotationUtils.findAnnotation(field, Seed.class).orElse(null);

		if(seed==null) {
			seed = AnnotationUtils.findAnnotation(testClass, Seed.class).orElse(null);
		}

		return seed;
	}

	private void injectFields(ExtensionContext context, Object testInstance, Predicate<Field> predicate) {
		ReflectionUtils.findFields(context.getRequiredTestClass(),
				predicate.and(field -> field.getType()==RandomSource.class),
				HierarchyTraversalMode.TOP_DOWN).forEach(field -> {
					assertValidFieldCandidate(field);
					try {
						makeAccessible(field).set(testInstance, createRandom(context,
								extractSeed(context.getRequiredTestClass(), field)));
					} catch (Throwable t) {
						ExceptionUtils.throwAsUncheckedException(t);
					}
				});
	}

	private void assertValidFieldCandidate(Field field) {
		assertSupportedType("field", field.getType());
		if (isPrivate(field)) {
			throw new ExtensionConfigurationException(RandomSource.class.getName()+" field [" + field + "] must not be private.");
		}
	}

}
