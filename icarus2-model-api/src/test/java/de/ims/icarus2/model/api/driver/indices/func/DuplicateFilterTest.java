/**
 *
 */
package de.ims.icarus2.model.api.driver.indices.func;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.util.collections.ArrayUtils.fillAscending;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.function.LongConsumer;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.GlobalErrorCode;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

/**
 * @author Markus Gärtner
 *
 */
class DuplicateFilterTest {

	@Test
	void constructor() {
		assertNotNull(new DuplicateFilter(mock(LongConsumer.class)));
	}

	@Test
	void constructorNull() {
		assertNPE(() -> new DuplicateFilter(null));
	}

	private long[] filter(long...values) {
		LongList tmp = new LongArrayList();
		LongStream.of(values).forEach(new DuplicateFilter(tmp::add));
		return tmp.toLongArray();
	}

	@Test
	void empty() {
		assertEquals(0, filter().length);
	}

	@Test
	void singularValues() {
		assertArrayEquals(new long[] {1}, filter(1, 1, 1, 1, 1));
	}

	@Test
	void sortedUnique() {
		long[] values = fillAscending(new long[random(10, 100)], random(0, Long.MAX_VALUE/2));
		assertArrayEquals(values, filter(values));
	}

	@Test
	void unsorted() {
		assertModelException(GlobalErrorCode.INVALID_INPUT,
				() -> filter(1, 2, 3, 5, 4, 6, 7, 8));
	}
}
