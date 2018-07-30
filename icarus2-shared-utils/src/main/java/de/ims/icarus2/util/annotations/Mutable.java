/**
 *
 */
package de.ims.icarus2.util.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashMap;

/**
 * Marker annotation to signal that a class implements the
 * {@link #hashCode()} and {@link Mutable#equals(Object)}
 * methods in a way that they rely on mutable internal data.
 * This makes instances of that class unsuitable for being
 * used in hash-based data structures such as {@link HashMap}
 * without special caution.
 *
 * @author Markus Gärtner
 *
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Mutable {

	// marker annotation
}
