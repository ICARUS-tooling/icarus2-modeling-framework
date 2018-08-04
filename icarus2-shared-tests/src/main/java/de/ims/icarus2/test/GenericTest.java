/**
 *
 */
package de.ims.icarus2.test;

import java.lang.reflect.Constructor;

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
}
