/**
 *
 */
package de.ims.icarus2.apiguard;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a {@code getter} or {@code setter} method for a property.
 * <p>
 * The test framework will try to match getters and setters that are
 * of the form {@code getProperty} and {@code setProperty}. For boolean
 * properties the getter method can be named {@code isProperty}. For
 * cases where there is no direct relation between the naming of getter
 * and setter for a given property, the {@link #name() value} of this
 * annotation can be used to link methods by assigning them the same
 * property name.
 *
 * @author Markus Gärtner
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface Guarded {

	/**
	 * Name of the property this method is associated with.
	 * An empty string indicates that the framework should
	 * extract the associated property automatically from
	 * the annotated method's name.
	 *
	 * @return
	 */
	String name() default "";

	/**
	 * Signals that the method should not be called more than once with
	 * valid parameters.
	 * <p>
	 * Only relevant if {@link #methodType()} is {@link Type#SETTER}.
	 * @return
	 */
	boolean restricted() default false;

	/**
	 * String representation of the default value for this getter method.
	 * Only applicable if the return type of the annotated method is
	 * primitive.
	 * <p>
	 * Only relevant if {@link #methodType()} is {@link Type#GETTER}.
	 *
	 * @return
	 */
	String defaultValue() default "";

	MethodType methodType() default MethodType.AUTO_DETECT;

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	enum MethodType {
		/**
		 * Annotated method is intended to act as a setter
		 */
		SETTER,
		/**
		 * Annotated method is intended to act as a getter
		 */
		GETTER,

		/**
		 * The actual type of the annotated method should be
		 * detected automatically.
		 */
		AUTO_DETECT,
		;
	}
}
