/**
 *
 */
package de.ims.icarus2.util.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author Markus Gärtner
 *
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface RequiresEnvironment {

	Class<?>[] value();
}
