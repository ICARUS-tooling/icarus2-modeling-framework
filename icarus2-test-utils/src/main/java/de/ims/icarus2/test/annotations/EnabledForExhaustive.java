/*
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
package de.ims.icarus2.test.annotations;

import static de.ims.icarus2.test.TestTags.LOCAL;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Declares a test method or class to only be enabled if the
 * {@code de.ims.icarus2.test.exhaustive} property is defined and evaluates
 * to {@code true}.
 * This is intended to be used for every expensive tests that exhaustively
 * explore a large value space and only need to be performed in case of
 * substantial changes to their respective units under test.
 *
 * @author Markus Gärtner
 *
 */
@Documented
@Retention(RUNTIME)
@Target({METHOD, TYPE})
@Tag(LOCAL)
@EnabledIfEnvironmentVariable(named="de.ims.icarus2.test.exhaustive", matches="true")
public @interface EnabledForExhaustive {
	// Marker annotation
}
