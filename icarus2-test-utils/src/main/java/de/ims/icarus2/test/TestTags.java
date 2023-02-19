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
package de.ims.icarus2.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Markus Gärtner
 *
 */
public final class TestTags {

	/**
	 * Marks tests that are meant to explore framework features for learning.
	 */
	public static final String LEARNER = "learner";

	/**
	 * Marks tests that could potentially take a very long time.
	 */
	public static final String SLOW = "slow";

	/**
	 * Marks tests that are meant to only be run on the CI server.
	 */
	public static final String CI = "ci";

	/**
	 * Marks tests that are meant to <b>not</b> be run on the CI server.
	 */
	public static final String LOCAL = "local";

	/**
	 * Marks tests that rely on randomly generated input.
	 */
	public static final String RANDOMIZED = "randomized";

	/**
	 * Marks collections of tests that are generated completely
	 * automatic and to a large extent outside the control of
	 * the individual test classes.
	 */
	public static final String AUTOMATIC = "automatic";


	/**
	 * Marks tests that intend not to participate in the regular
	 * test lifecycle. The associated {@link BeforeEach} and
	 * {@link AfterEach} methods are expected to honor this and
	 * to ignore the marked tests for initialization or cleanup.
	 * <p>
	 * In a way this is an anti-pattern for proper JUnit setup,
	 * but in very rare cases can prevent overhead in larger
	 * test classes. Use sparsely!!
	 */
	public static final String STANDALONE = "standalone";

	/**
	 * Marks tests that want their {@link BeforeEach} method to
	 * shuffle the underlying data.
	 */
	public static final String SHUFFLE = "shuffle";

	/**
	 * Marks tests intended to test the thread-safety of an implementation.
	 * While it is impossible due to JVM threading to reliably guarantee
	 * thread-safety based on runtime samples, simulating high concurrency
	 * might at least help spot some more obvious concurrency flaws in the code.
	 * <p>
	 * Note that concurrency tests are likely to also qualify for the
	 * {@link #SLOW} tag due to the necessity of creating substantial load.
	 * As such they should <b>not</b> be included when running on CI environments.
	 */
	public static final String CONCURRENT = "concurrent";
}
