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
package de.ims.icarus2.test.annotations;

import static de.ims.icarus2.test.TestTags.LOCAL;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

/**
 * Marker interface to run a test only in the local
 * environment and not on the CI server.
 *
 * @author Markus Gärtner
 *
 */
@Documented
@Retention(RUNTIME)
@Target({METHOD, TYPE})
@Tag(LOCAL)
@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
public @interface DisabledOnCi {
	/** Reason given for documentation purposes */
	String value() default "";
}
