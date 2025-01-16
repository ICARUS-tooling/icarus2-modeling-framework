/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.test.contracts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.Testable;

/**
 * Provides default tests for the {@link Object#equals(Object)} contract.
 *
 * @author Markus Gärtner
 *
 * @param <T> type of class under test
 */
public interface EqualsContract<T> extends Testable<T> {

	/**
	 * Creates a testable instance that is supposed to be different to
	 * the given {@code source}.
	 * <p>
	 * The default implementation assumes that every call to {@link #create()}
	 * produces a different result and as such delegates to that method.
	 *
	 * @return
	 */
	default T createUnequal(T source) {
		return create();
	}

	/**
	 * Creates a testable instance that is supposed to be equal to the
	 * given {@code source}.
	 * <p>
	 * Note that for types that rely on the default implementation of the
	 * {@link Object#equals(Object)} method it is acceptable to return the
	 * {@code source} argument itself.
	 *
	 * @return
	 */
	T createEqual(T source);

    @Test
    default void valueEqualsItself() {
        T value = create();
        assertEquals(value, value);
    }

    @Test
    default void equalsSymmetric() {
        T value = create();
        T other = createEqual(value);
        assertEquals(value, other);
        assertEquals(other, value);
    }

    @Test
    default void equalsTransitive() {
        T value = create();
        T other1 = createEqual(value);
        T other2 = createEqual(other1);
        assertEquals(value, other1);
        assertEquals(other1, other2);
        assertEquals(value, other2);
    }

    @Test
    default void doesNotEqualTransitiveXXO() {
        T value = create();
        T other1 = createEqual(value);
        T other2 = createUnequal(other1);
        assertEquals(value, other1);
        assertNotEquals(other1, other2);
        assertNotEquals(value, other2);
    }

    @Test
    default void doesNotEqualTransitiveXOO() {
        T value = create();
        T other1 = createUnequal(value);
        T other2 = createEqual(other1);
        assertNotEquals(value, other1);
        assertEquals(other1, other2);
        assertNotEquals(value, other2);
    }

    @Test
    default void valueDoesNotEqualNull() {
        T value = create();
        assertFalse(value.equals(null));
    }

    @Test
    default void valueDoesNotEqualDifferentValue() {
        T value = create();
        T differentValue = createUnequal(value);
        assertNotEquals(value, differentValue);
        assertNotEquals(differentValue, value);
    }
}
