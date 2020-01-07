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
/**
 *
 */
package de.ims.icarus2.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
public interface CloneableTest<C extends Cloneable> extends GenericTest<C> {

	Function<C,Object> cloneFunction();

	/**
	 * Test case for {@link Object#clone()}
	 */
	@Test
	default void testClone() {
		C original = create();

		Object clone = cloneFunction().apply(original);

		assertNotNull(clone);

		assertEquals(original.getClass(), clone.getClass());

		assertCloneContentEquals(original, clone);
	}

	/**
	 * Hook for subclassed tests to do some assertions on the result of a
	 * cloning operation.
	 *
	 * @param original
	 * @param clone
	 */
	default void assertCloneContentEquals(C original, Object clone) {
		// no-op
	}
}
