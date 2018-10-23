/**
 *
 */
package de.ims.icarus2.test.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marker annotation to signal that a test case is to be considered
 * outdated wrt the current state of the test suite.
 *
 * @author Markus Gärtner
 *
 */
@Documented
@Retention(SOURCE)
@Target(TYPE)
public @interface OldTests {

	// marker annotation
}
