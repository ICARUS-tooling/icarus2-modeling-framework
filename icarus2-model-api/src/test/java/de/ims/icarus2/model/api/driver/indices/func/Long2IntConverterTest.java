/**
 *
 */
package de.ims.icarus2.model.api.driver.indices.func;

import static de.ims.icarus2.model.api.ModelTestUtils.assertOverflow;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.test.TestUtils.randomLongs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.function.IntConsumer;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * @author Markus Gärtner
 *
 */
class Long2IntConverterTest {

	@Test
	void constructor() {
		assertNotNull(new Long2IntConverter(mock(IntConsumer.class)));
	}

	@Test
	void constructorNull() {
		assertNPE(() -> new Long2IntConverter(null));
	}

	@ParameterizedTest
	@ValueSource(longs = {Long.MIN_VALUE, Integer.MIN_VALUE-1L,
			Integer.MAX_VALUE+1L, Long.MAX_VALUE})
	void overflow(long value) {
		assertOverflow(() -> new Long2IntConverter(mock(IntConsumer.class)).accept(value));
	}

	@Test
	void valid() {
		long[] values = randomLongs(random(10, 100), Integer.MIN_VALUE, Integer.MAX_VALUE);
		IntList tmp = new IntArrayList();

		LongStream.of(values).forEach(new Long2IntConverter(tmp::add));

		assertEquals(values.length, tmp.size());
		for (int i = 0; i < values.length; i++) {
			assertEquals(values[i], tmp.getInt(i));
		}
	}
}
