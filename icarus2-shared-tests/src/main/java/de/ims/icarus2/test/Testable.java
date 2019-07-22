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
package de.ims.icarus2.test;

import static de.ims.icarus2.test.TestUtils.settings;

import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface Testable<T extends Object> {

	/**
	 * Creates a fresh new test instance of type {@code T} for the
	 * current test case.
	 * <p>
	 * Note that implementations <b>must</b> ensure to call
	 * {@link TestSettings#process(Object)} on the created test
	 * instance prior to returning it!
	 *
	 * @param settings
	 * @return
	 */
	@Provider
	T createTestInstance(TestSettings settings);

	/**
	 * Shorthand method for {@link #createTestInstance(TestSettings)}
	 * with a fresh new {@link TestSettings} instance. Should only be
	 * used when test routines are not desired to be overridden by
	 * subclasses/implementations as there is no way to pass new test
	 * settings to a method that creates its test instances this way.
	 *
	 * @return
	 */
	@Provider
	default T create() {
		return createTestInstance(settings());
	}
}
