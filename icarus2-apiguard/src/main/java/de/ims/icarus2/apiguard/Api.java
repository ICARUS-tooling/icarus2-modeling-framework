/**
 *
 */
package de.ims.icarus2.apiguard;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a class as belonging to one of the public APIs of the
 * ICARUS framework. Those classes are expected to adhere to
 * certain easily testable design patterns, so the testing framework
 * can create default tests for them.
 *
 * @author Markus Gärtner
 *
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Api {
	// marker annotation
}
