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

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a type, method or constructor as unguarded, so that
 * the api guard framework should ignore it in automatic testing.
 *
 * @author Markus Gärtner
 *
 */
@Documented
@Inherited
@Retention(RUNTIME)
@Target({ TYPE, METHOD, CONSTRUCTOR })
public @interface Unguarded {

	/**
	 * Returns the reason for marking the type, method or constructor
	 * as unguarded.
	 */
	String value();

	/**
	 * A usable reason for marking methods to be excluded from automatic
	 * argument testing due to them not being supported in the implementation
	 * under test.
	 * <p>
	 * The basic assumption is that (optional) methods which aren't supported
	 * in an implementation will not perform any parameter validation, but
	 * instead simply throw the appropriate exception to indicate that they
	 * are not supported. We leave the specifics of that exception contract
	 * to the respective test suites.
	 */
	public static final String UNSUPPORTED = "Method not supported";
}
