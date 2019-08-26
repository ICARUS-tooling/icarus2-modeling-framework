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
class Long2IntIteratorTest {

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.func.Long2IntIterator#LongToIntIterator(java.util.PrimitiveIterator.OfLong)}.
	 */
	@Test
	void testLongToIntIterator() {
		assertNotNull(new Long2IntIterator(mock(OfLong.class)));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.func.Long2IntIterator#LongToIntIterator(java.util.PrimitiveIterator.OfLong)}.
	 */
	@Test
	void testLongToIntIteratorNull() {
		assertNPE(() -> new Long2IntIterator(null));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.func.Long2IntIterator#hasNext()}.
	 */
	@Test
	void testHasNextEmpty() {
		assertFalse(new Long2IntIterator(LongStream.empty().iterator()).hasNext());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.func.Long2IntIterator#hasNext()}.
	 */
	@Test
	void testHasNext() {
		Long2IntIterator it = new Long2IntIterator(LongStream.of(random().nextLong()).iterator());
		assertTrue(it.hasNext());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.func.Long2IntIterator#nextInt()}.
	 */
	@Test
	void testNextInt() {
		long[] values = randomLongs(random(10, 100), Integer.MIN_VALUE, Integer.MAX_VALUE);
		Long2IntIterator it = new Long2IntIterator(LongStream.of(values).iterator());

		for (int i = 0; i < values.length; i++) {
			assertTrue(it.hasNext());
			assertEquals(values[i], it.nextInt());
		}

		assertFalse(it.hasNext());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.func.Long2IntIterator#nextInt()}.
	 */
	@ParameterizedTest
	@ValueSource(longs = {Integer.MAX_VALUE+1L, Long.MAX_VALUE, Long.MIN_VALUE})
	void testNextIntOverflow(long value) {
		Long2IntIterator it = new Long2IntIterator(LongStream.of(value).iterator());
		assertTrue(it.hasNext());
		assertOverflow(() -> it.nextInt());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.func.Long2IntIterator#nextInt()}.
	 */
	@Test
	void testNextInt_NSE() {
		assertThrows(NoSuchElementException.class,
				() -> new Long2IntIterator(LongStream.empty().iterator()).nextInt());
	}

}
