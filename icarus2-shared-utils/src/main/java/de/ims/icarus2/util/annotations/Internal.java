/**
 *
 */
package de.ims.icarus2.util.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(SOURCE)
@Target({METHOD,TYPE})
/**
 * Marks types or methods that are mainly intended for use by the
 * ICARUS2 framework itself, but which are publicly available.
 * <p>
 * The usage of those annotated methods or types by client code
 * is allowed but highly discouraged. Usually each such method
 * or type will provide documentation listing a preferable
 * replacement in the public API.
 *
 * @author Markus Gärtner
 *
 */
public @interface Internal {

	// marker annotation
}
