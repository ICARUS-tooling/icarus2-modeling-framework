/**
 *
 */
package de.ims.icarus2.test.util;

import java.io.Closeable;

/**
 * Root interface to be used for any class that acts as a configuration container
 * for creating test instances. Due to the tendency of using stream operations to
 * turn those configurations into actual test instances there is a certain danger
 * of causing memory congestion or leaks due to large number of objects and/or mocks
 * being kept alive in the stream structure containing the configurations.
 * As a precaution test suites using this kind of approach should make sure to
 * always {@link #close() clean-up} all the configurations as soon as their respective
 * tests completed (ideally in a try-finally construct).
 *
 * @author Markus Gärtner
 *
 */
public interface TestConfig extends Closeable {

	/**
	 * Release any stored objects that could cause memory congestion or leaks.
	 * @see java.io.Closeable#close()
	 */
	@Override
	void close();
}
