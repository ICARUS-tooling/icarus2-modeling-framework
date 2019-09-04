/**
 *
 */
package de.ims.icarus2.util.collections;

import static de.ims.icarus2.test.TestUtils.assertCollectionEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface IterableTest<T, S extends Iterable<T>> {

	@Provider
	S createEmpty();

	@Provider
	S createFilled(@SuppressWarnings("unchecked") T...items);

	T[] randomContent();

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.seq.DataSequence#iterator()}.
	 */
	@Test
	default void testIterator() {
		T[] items = randomContent();
		S seq = createFilled(items);
		Iterator<T> it = seq.iterator();
		for (int i = 0; i < items.length; i++) {
			assertTrue(it.hasNext());
			assertSame(items[i], it.next());
		}
		assertFalse(it.hasNext());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.seq.DataSequence#iterator()}.
	 */
	@Test
	default void testIteratorEmpty() {
		assertFalse(createEmpty().iterator().hasNext());
		assertThrows(NoSuchElementException.class, () -> createEmpty().iterator().next());
	}

	/**
	 * Test method for {@link java.lang.Iterable#forEach(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEach() {
		T[] items = randomContent();
		S seq = createFilled(items);
		List<T> tmp = new ArrayList<>();
		seq.forEach(tmp::add);
		assertCollectionEquals(tmp, items);
	}

	/**
	 * Test method for {@link java.lang.Iterable#forEach(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachEmpty() {
		Consumer<T> action = mock(Consumer.class);
		createEmpty().forEach(action);
		verify(action, never()).accept(any());
	}

	/**
	 * Test method for {@link java.lang.Iterable#spliterator()}.
	 */
	@Test
	default void testSpliterator() {
		T[] items = randomContent();
		S seq = createFilled(items);
		assertArrayEquals(items, StreamSupport.stream(seq.spliterator(), false).toArray());
	}

}
