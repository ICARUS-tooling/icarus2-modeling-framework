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
