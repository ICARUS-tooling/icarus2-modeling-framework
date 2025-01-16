/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.test.fail;

import static de.ims.icarus2.test.TestUtils.assertNPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
class FailOnConstructorTest {

	final Class<? extends RuntimeException> exceptionClass = UnsupportedOperationException.class;
	final String className = "BoundToFail";
	final String packageName = "com.my.project";
	final Class<?> baseClass = Point.class;

	/**
	 * Test method for {@link de.ims.icarus2.test.fail.FailOnConstructor#createClassForException(java.lang.String, java.lang.String, java.lang.Class, java.lang.Class)}.
	 * @throws Exception
	 */
	@Test
	void testCreateClassForException() throws Exception {

		// Only baseClass can be null
		assertNPE(() -> FailOnConstructor.createClassForException(null, className, baseClass, exceptionClass));
		assertNPE(() -> FailOnConstructor.createClassForException(packageName, null, baseClass, exceptionClass));
		assertNPE(() -> FailOnConstructor.createClassForException(packageName, className, baseClass, null));

		// Test all possible combinations of set and unset parameters
		// (pretty limited since we can't pass empty package name)
		assertClassGeneration(packageName, className, null, exceptionClass);
		assertClassGeneration(packageName, className, baseClass, exceptionClass);
	}

	//TODO add test for other class construction method

	private void assertClassGeneration(String packageName, String className,
			Class<?> baseClass, Class<? extends Exception> exceptionClass) throws Exception {

		Class<? extends RuntimeException> generatedClass = FailOnConstructor.createClassForException(
				packageName, className, baseClass, exceptionClass);

		assertTrue(baseClass==null || baseClass.isAssignableFrom(generatedClass));

		InvocationTargetException exp = assertThrows(InvocationTargetException.class,
				generatedClass.getDeclaredConstructor()::newInstance);
		assertThat(exp.getCause()).isExactlyInstanceOf(exceptionClass);
	}
}
