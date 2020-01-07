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
package de.ims.icarus2.test.contracts;

import static de.ims.icarus2.test.TestUtils.assertNPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.Testable;
import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface ComparableContract<T extends Comparable<T>> extends Testable<T> {

	/**
	 * Creates a test instance that is supposed to be smaller than
	 * the result of {@link #create()} wrt {@link Comparable#compareTo(Object)}
	 *
	 * @return
	 */
	@Provider
    T createSmaller();

    @Test
    default void expectNPE() {
    	assertNPE(() -> create().compareTo(null));
    }

    @Test
    default void returnsZeroWhenComparedToItself() {
        T value = create();
        assertEquals(0, value.compareTo(value));
    }

    @Test
    default void returnsPositiveNumberWhenComparedToSmallerValue() {
        T value = create();
        T smallerValue = createSmaller();
        assertTrue(value.compareTo(smallerValue) > 0);
    }

    @Test
    default void returnsNegativeNumberWhenComparedToLargerValue() {
        T value = create();
        T smallerValue = createSmaller();
        assertTrue(smallerValue.compareTo(value) < 0);
    }

}
