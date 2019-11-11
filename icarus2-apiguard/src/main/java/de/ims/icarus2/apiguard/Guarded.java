/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * and setter for a given property, the {@link #value() value} of this
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
	String value() default "";

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
		 * Annotated method is intended to act as a setter.
		 */
		SETTER,
		/**
		 * Annotated method is a special setter for chained builder calls and following very
		 * specific conventions:
		 * <ul>
		 * <li>It can be chained, i.e. its return value is the exact object it was invoked on.</li>
		 * <li>It takes a single argument.</li>
		 * <li>It does not accept a {@code null} argument.</li>
		 * <li>Invoking a builder method more than once will result in an {@link IllegalStateException}
		 * being thrown.</li>
		 * <li>Every builder method is supposed to have a matching getter method that either returns
		 * the value specified in a previous builder call or a property-specific default value. This
		 * getter method must be named {@code [is|get]Xxxxx} for a builder {@code xxxxx}.</li>
		 * </ul>
		 */
		BUILDER,
		/**
		 * Annotated method is intended to act as a getter.
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
