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
 * Marks a {@code getter} or {@code setter} method for a property.
 * <p>
 * The test framework will try to match getters and setters that are
 * of the form {@code getProperty} and {@code setProperty}. For boolean
 * properties the getter method can be named {@code isProperty}. For
 * cases where there is no direct relation between the naming of getter
 * and setter for a given property, the {@link #value() value} of this
 * annotation can be used to link methods by assigning them the same
 * property name.
 *
 * @author Markus Gärtner
 *
 */
@Inherited
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface Property {

	/**
	 * Name of the property this method is associated with
	 * @return
	 */
	String value() default "";
}
