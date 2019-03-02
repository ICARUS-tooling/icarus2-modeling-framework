/**
 *
 */
package de.ims.icarus2.util.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks methods or fields that provide access to an objects
 * environments.
 *
 * @author Markus Gärtner
 *
 */
@Documented
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface ProvidesEnvironment {

	Class<?> value();
}
