/**
 *
 */
package de.ims.icarus2.test;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
public interface GenericTest<T extends Object> {

	Class<? extends T> getTestTargetClass();

	default T createNoArgs() throws Exception {
		return getTestTargetClass().newInstance();
	}

	default T create(Class<?>[] signature, Object...values) throws Exception {
		Class<? extends T> clazz = getTestTargetClass();

		Constructor<? extends T> constructor = clazz.getConstructor(signature);

		return constructor.newInstance(values);
	}

	/**
	 * This is a utility test method that tries to call all the
	 * obligatory constructors for implementations of a certain interface.
	 * Usually a test specification (interface) wishing to add additional
	 * constructors to this test should simply override the method. But
	 * if a deviation from constructor rules imposed by a super interface
	 * is desired the method can be overriden without a call to the super
	 * implementation.
	 *
	 * @throws Exception
	 */
	@Test
	default void testMandatoryConstructors() throws Exception {
		// no-op
	}
}
