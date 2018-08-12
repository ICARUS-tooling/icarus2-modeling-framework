/**
 *
 */
package de.ims.icarus2.test.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(SOURCE)
@Target(METHOD)
/**
 * Marks a method to be  the producer of test instances
 *
 * @author Markus Gärtner
 *
 */
public @interface Provider {
	// marker annotation
}
