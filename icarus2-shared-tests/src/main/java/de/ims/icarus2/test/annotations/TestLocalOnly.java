/**
 *
 */
package de.ims.icarus2.test.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

/**
 * Marker interface to run a test only in the local
 * environment and not on the CI server.
 *
 * @author Markus Gärtner
 *
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
@Test
@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
public @interface TestLocalOnly {
	// marker interface
}
