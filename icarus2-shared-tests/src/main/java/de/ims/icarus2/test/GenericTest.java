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
package de.ims.icarus2.test;

import static de.ims.icarus2.test.TestTags.AUTOMATIC;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Constructor;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestReporter;
import org.opentest4j.TestAbortedException;

import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.test.guard.ApiGuard;

/**
 * @author Markus Gärtner
 *
 */
public interface GenericTest<T extends Object> extends Testable<T> {

	Class<? extends T> getTestTargetClass();

	/**
	 * Accesses the {@link #getTestTargetClass() class under test} and calls
	 * its default no-arguments constructor via {@link Class#newInstance()}.
	 *
	 * @return
	 * @throws Exception
	 */
	@Provider
	default T createNoArgs() {
		try {
			return getTestTargetClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new TestAbortedException("Unable to call default no-args constructor", e);
		}
	}

	/**
	 * Accesses the {@link #getTestTargetClass() class under test} and fetches and calls
	 * the constructor matching the specified {@code signature}.
	 *
	 * @param signature
	 * @param values
	 * @return
	 * @throws Exception
	 */
	@Provider
	default T create(Class<?>[] signature, Object...values) throws Exception {
		Class<? extends T> clazz = getTestTargetClass();

		Constructor<? extends T> constructor = clazz.getConstructor(signature);

		return constructor.newInstance(values);
	}

	@Test
	default void verifyCorrectTestType() {
		assertEquals(getTestTargetClass(), create().getClass());
	}

	@SuppressWarnings("unchecked")
	@TestFactory
	@Tag(AUTOMATIC)
	@DisplayName("ApiGuard")
	default Stream<DynamicNode> guardApi(TestReporter testReporter) {
		return new ApiGuard<T>((Class<T>)getTestTargetClass())
				.testPropertiesIfApi()
				.noArgsFallback(this::create)
				.createTests(testReporter);
	}

	/**
	 * This is a utility test method that tries to call all the
	 * obligatory constructors for implementations of a certain interface.
	 * Usually a test specification (interface) wishing to add additional
	 * constructors to this test should simply override the method. But
	 * if a deviation from constructor rules imposed by a super interface
	 * is desired the method can be overridden without a call to the super
	 * implementation.
	 *
	 * @throws Exception
	 */
	@Test
	default void testMandatoryConstructors() throws Exception {
		// no-op
	}
}
