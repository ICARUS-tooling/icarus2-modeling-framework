/**
 *
 */
package de.ims.icarus2.apiguard;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author Markus Gärtner
 *
 */
@Inherited
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface Getter {

	/**
	 * Name of the property this method is associated with, never {@code null}.
	 * @return
	 */
	String value();
}
