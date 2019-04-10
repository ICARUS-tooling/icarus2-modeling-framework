/**
 *
 */
package de.ims.icarus2.test.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as being a builder implementation to produce
 * tests for the specified {@link #value() target class}.
 *
 * @author Markus Gärtner
 *
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface TestBuilder {
	Class<?> value();
}
