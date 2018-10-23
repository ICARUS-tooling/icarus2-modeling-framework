/**
 *
 */
package de.ims.icarus2.test.fail;

import static de.ims.icarus2.test.TestUtils.assertNPE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;

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
		assertClassGeneration("", className, null, exceptionClass);
		assertClassGeneration(packageName, className, null, exceptionClass);
		assertClassGeneration("", className, baseClass, exceptionClass);
		assertClassGeneration(packageName, className, baseClass, exceptionClass);
	}

	//TODO add test for other class construction method

	private void assertClassGeneration(String packageName, String className,
			Class<?> baseClass, Class<? extends Exception> exceptionClass) throws Exception {

		Class<? extends RuntimeException> generatedClass = FailOnConstructor.createClassForException(
				packageName, className, baseClass, exceptionClass);

		assertTrue(baseClass==null || baseClass.isAssignableFrom(generatedClass));

		assertThrows(exceptionClass, generatedClass::newInstance);
	}
}
