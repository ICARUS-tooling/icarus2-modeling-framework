/**
 *
 */
package de.ims.icarus2.test;

import static de.ims.icarus2.test.TestUtils.settings;

import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface Testable<T extends Object> {

	@Provider
	T createTestInstance(TestSettings settings);

	/**
	 * Shorthand method for {@link #createTestInstance(TestSettings)}
	 * with a fresh new {@link TestSettings} instance. Should only be
	 * used when test routines are not desired to be overridden by
	 * subclasses/implementations as there is no way to pass new test
	 * settings to a method that creates its test instances this way.
	 *
	 * @return
	 */
	@Provider
	default T create() {
		return createTestInstance(settings());
	}
}
