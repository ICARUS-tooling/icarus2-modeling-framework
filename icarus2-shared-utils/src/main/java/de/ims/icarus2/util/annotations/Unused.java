/**
 *
 */
package de.ims.icarus2.util.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(SOURCE)
@Target({ TYPE, FIELD, METHOD, PARAMETER, LOCAL_VARIABLE })
/**
 *  Marker annotation to signal that something is not (currently) in use
 *
 * @author Markus
 *
 */
public @interface Unused {
	// Marker annotation
}
