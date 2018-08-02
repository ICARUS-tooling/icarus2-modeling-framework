/**
 *
 */
package de.ims.icarus2.test.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Disabled;

/**
 * @author Markus Gärtner
 *
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
@Disabled("soon™")
public @interface PostponedTest {
	/**
	 * Reason for postponing, if given
	 * @return
	 */
	String value() default "";
}
