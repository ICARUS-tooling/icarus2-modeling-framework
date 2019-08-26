/**
 *
 */
package de.ims.icarus2.model.api.driver.indices.func;

import static de.ims.icarus2.model.api.ModelTestUtils.assertOverflow;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.test.TestUtils.randomLongs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator.OfLong;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * @author Markus Gärtner
 *
 */
class LongToIntIteratorTest {

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.func.LongToIntIterator#LongToIntIterator(java.util.PrimitiveIterator.OfLong)}.
	 */
	@Test
	void testLongToIntIterator() {
		assertNotNull(new LongToIntIterator(mock(OfLong.class)));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.func.LongToIntIterator#LongToIntIterator(java.util.PrimitiveIterator.OfLong)}.
	 */
	@Test
	void testLongToIntIteratorNull() {
		assertNPE(() -> new LongToIntIterator(null));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.func.LongToIntIterator#hasNext()}.
	 */
	@Test
	void testHasNextEmpty() {
		assertFalse(new LongToIntIterator(LongStream.empty().iterator()).hasNext());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.func.LongToIntIterator#hasNext()}.
	 */
	@Test
	void testHasNext() {
		LongToIntIterator it = new LongToIntIterator(LongStream.of(random().nextLong()).iterator());
		assertTrue(it.hasNext());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.func.LongToIntIterator#nextInt()}.
	 */
	@Test
	void testNextInt() {
		long[] values = randomLongs(random(10, 100), Integer.MIN_VALUE, Integer.MAX_VALUE);
		LongToIntIterator it = new LongToIntIterator(LongStream.of(values).iterator());

		for (int i = 0; i < values.length; i++) {
			assertTrue(it.hasNext());
			assertEquals(values[i], it.nextInt());
		}

		assertFalse(it.hasNext());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.func.LongToIntIterator#nextInt()}.
	 */
	@ParameterizedTest
	@ValueSource(longs = {Integer.MAX_VALUE+1L, Long.MAX_VALUE, Long.MIN_VALUE})
	void testNextIntOverflow(long value) {
		LongToIntIterator it = new LongToIntIterator(LongStream.of(value).iterator());
		assertTrue(it.hasNext());
		assertOverflow(() -> it.nextInt());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.func.LongToIntIterator#nextInt()}.
	 */
	@Test
	void testNextInt_NSE() {
		assertThrows(NoSuchElementException.class,
				() -> new LongToIntIterator(LongStream.empty().iterator()).nextInt());
	}

}
