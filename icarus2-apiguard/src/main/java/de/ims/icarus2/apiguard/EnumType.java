/**
 *
 */
package de.ims.icarus2.apiguard;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marker annotation to signal that a type represents an
 * enum-like collection of objects.
 *
 * @author Markus Gärtner
 *
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface EnumType {

	/**
	 * Returns the name of the public static method within the
	 * annotated type that returns the "enum-like" collection of
	 * objects. The return type can be an array or a collection,
	 * but it must never be {@code null}.
	 *
	 * @return
	 */
	String method();

	/**
	 * Provides a key that should be passed to the {@link #method() lookup method}
	 * to fetch a default value for this enum type.
	 *
	 * @return
	 */
	String key() default "";
}
