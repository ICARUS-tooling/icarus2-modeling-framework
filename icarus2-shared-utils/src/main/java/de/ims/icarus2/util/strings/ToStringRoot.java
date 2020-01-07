/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.strings;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a type for support of automatic construction
 * of {@link Object#toString()} values.
 *
 * @author Markus Gärtner
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ToStringRoot {

	/**
	 * Allows to decide whether or not fields from
	 * super classes should be included. Note that
	 * for inheritance to work, the super class must
	 * also have the {@link ToStringRoot} annotation.
	 */
	boolean inherit() default DEFAULT_INHERIT;

	/**
	 * Signals whether all fields declared in this class
	 * should be included in the toString() construction.
	 * Note that any field annotated with {@link ToStringField}
	 * will still have all the settings declared with that a
	 * annotation honored, but this flag also allows to
	 * reduce the annotation overhead.
	 */
	boolean complete() default DEFAULT_COMPLETE;

	/**
	 * Defines the main label for the class. An empty
	 * string is a signal that the short form of the
	 * class name should be used.
	 */
	String label() default DEFAULT_LABEL;

	/**
	 * Style to be used for the surrounding brackets
	 */
	BracketStyle brackets() default BracketStyle.SQUARE;

	public static final BracketStyle DEFAULT_BRACKET_STYLE = BracketStyle.SQUARE;
	public static final boolean DEFAULT_INHERIT = true;
	public static final boolean DEFAULT_COMPLETE = false;
	public static final String DEFAULT_LABEL = "";
}
