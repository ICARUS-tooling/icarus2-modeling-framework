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
package de.ims.icarus2.test.util;

import java.io.Closeable;

/**
 * Root interface to be used for any class that acts as a configuration container
 * for creating test instances. Due to the tendency of using stream operations to
 * turn those configurations into actual test instances there is a certain danger
 * of causing memory congestion or leaks due to large number of objects and/or mocks
 * being kept alive in the stream structure containing the configurations.
 * As a precaution test suites using this kind of approach should make sure to
 * always {@link #close() clean-up} all the configurations as soon as their respective
 * tests completed (ideally in a try-finally construct).
 *
 * @author Markus Gärtner
 *
 */
public interface TestConfig extends Closeable {

	/**
	 * Release any stored objects that could cause memory congestion or leaks.
	 * @see java.io.Closeable#close()
	 */
	@Override
	void close();
}
