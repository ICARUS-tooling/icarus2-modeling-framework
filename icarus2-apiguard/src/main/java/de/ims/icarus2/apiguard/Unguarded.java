/**
 *
 */
package de.ims.icarus2.apiguard;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a type, method or constructor as unguarded, so that
 * the api guard framework should ignore it in automatic testing.
 *
 * @author Markus Gärtner
 *
 */
@Documented
@Inherited
@Retention(RUNTIME)
@Target({ TYPE, METHOD, CONSTRUCTOR })
public @interface Unguarded {
	// marker annotation
}
