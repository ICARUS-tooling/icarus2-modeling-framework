/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import java.util.function.Function;

/**
 * @author Markus Gärtner
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ToStringField {

	/**
	 * The label under which the field should show up
	 * in the string representation of the object. If the
	 * returned value is the empty string, the original
	 * name of the field will be used.
	 */
	String label() default DEFAULT_LABEL;

	/**
	 * Format string to be used to transfer the field value
	 * into a a string. If the returned value is the empty
	 * string, no special formatting will be done.
	 */
	String format() default DEFAULT_FORMAT;

	/**
	 * Class of dedicated formatter to be used to transform
	 * the field's value into a string. Must implement {@link Function}
	 * and be able to handle the type of the field's class.
	 * The parameterized signature of the functional interface
	 * is {@code Function<Object,String>}.
	 * If the returned value is {@code Object.class}, no special
	 * formatting will be done.
	 */
	Class<?> formatter() default Object.class;

	public static final String DEFAULT_LABEL = "";
	public static final String DEFAULT_FORMAT = "";
	public static final Class<?> DEFAULT_FORMATTER = Object.class;
}
