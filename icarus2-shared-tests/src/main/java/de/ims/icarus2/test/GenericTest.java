/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.test;

import static de.ims.icarus2.test.TestUtils.abort;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface GenericTest<T extends Object> extends TargetedTest<T> {

	/**
	 * Accesses the {@link #getTestTargetClass() class under test} and calls
	 * its default no-arguments constructor via {@link Class#newInstance()}.
	 *
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Provider
	default T createNoArgs() {
		try {
			return (T) getTestTargetClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException("Unable to call default no-args constructor", e);
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
	@SuppressWarnings("unchecked")
	@Provider
	default T create(Class<?>[] signature, Object...values) throws Exception {
		Class<?> clazz = getTestTargetClass();

		Constructor<?> constructor = clazz.getConstructor(signature);

		return (T) constructor.newInstance(values);
	}

	@Test
	default void verifyCorrectTestType() {
		assertEquals(getTestTargetClass(), create().getClass());
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
		abort();
	}
}
