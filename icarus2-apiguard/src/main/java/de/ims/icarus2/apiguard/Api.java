/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.apiguard;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a class as belonging to one of the public APIs of the
 * ICARUS framework. Those classes are expected to adhere to
 * certain easily testable design patterns, so the testing framework
 * can create default tests for them.
 *
 * @author Markus Gärtner
 *
 */
@Inherited
@Documented
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Api {

	ApiType type() default ApiType.GENERAL;

	public enum ApiType {
		GENERAL,
		BUILDER,
		;
	}
}
