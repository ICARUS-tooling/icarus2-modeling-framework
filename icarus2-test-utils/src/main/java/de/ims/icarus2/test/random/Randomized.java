/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.test.random;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.platform.commons.util.ReflectionUtils.isPrivate;
import static org.junit.platform.commons.util.ReflectionUtils.makeAccessible;

import java.lang.reflect.Field;
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
 * Provides and injects instances of {@link RandomGenerator}.
 * Will automatically generate those instances for any declared parameters
 * and/or field in designated test classes or test methods.
 *
 * @author Markus Gärtner
 *
 */
public class Randomized implements BeforeAllCallback, BeforeEachCallback, ParameterResolver {

	public static final String SEED_PROPERTY = "de.ims.icarus2.seed";

	/** Test seed derived from this class' name */
	static final long TEST_SEED = RandomGenerator.forClass(Randomized.class).getSeed();

	private static Long tryResolveSeed() {
		String seed = System.getProperty(SEED_PROPERTY);
		return seed==null ? null : Long.valueOf(seed);
	}

	/**
	 * @see org.junit.jupiter.api.extension.ParameterResolver#supportsParameter(org.junit.jupiter.api.extension.ParameterContext, org.junit.jupiter.api.extension.ExtensionContext)
	 */
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return (parameterContext.getParameter().getType() == RandomGenerator.class);
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
		if (type != RandomGenerator.class) {
			throw new ExtensionConfigurationException("Can only resolve " + target + " of type "
					+ RandomGenerator.class.getName() + " but was: " + type.getName());
		}
	}

	private Seed extractSeed(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Seed seed = parameterContext.findAnnotation(Seed.class).orElse(null);
		if(seed==null) {
			seed = AnnotationUtils.findAnnotation(extensionContext.getRequiredTestMethod(), Seed.class).orElse(null);
		}
		return seed;
	}

	private static RandomGenerator createRandom(ExtensionContext context, Seed seed) {
		Long sharedSeed = tryResolveSeed();
		if(sharedSeed!=null) {
			return RandomGenerator.forSeed(sharedSeed.longValue());
		}

		if(seed!=null) {
			long s = seed.value();
			if(s!=-1L) {
				return RandomGenerator.forSeed(s);
			}

			String seedSource = seed.seedSource();
			assertNotEquals("", seedSource, "No valid seed specified");
			return RandomGenerator.forString(seedSource);
		}

		return context.getTestMethod()
					.map(RandomGenerator::forExecutable)
					.orElseGet(() -> RandomGenerator.forClass(context.getRequiredTestClass()));
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
				predicate.and(field -> field.getType()==RandomGenerator.class),
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
			throw new ExtensionConfigurationException(RandomGenerator.class.getName()+" field [" + field + "] must not be private.");
		}
	}

}
