/**
 *
 */
package de.ims.icarus2.util.collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.test.annotations.LongArrayArg;

/**
 * @author Markus Gärtner
 *
 */
class BlockingLongBatchQueueTest {

	@Nested
	class ForConstructor {

	}

	@Nested
	class ForInstance {

		private BlockingLongBatchQueue instance;

		@BeforeEach
		void setUp() {
			instance = new BlockingLongBatchQueue(4);
		}

		void assertQueue(int count, int putIndex, int takeIndex, long...items) {
			assertThat(instance.count).isEqualTo(count);
			assertThat(instance.putIndex).isEqualTo(putIndex);
			assertThat(instance.takeIndex).isEqualTo(takeIndex);
			assertThat(instance.items).startsWith(items);
		}

		@ParameterizedTest
		@ValueSource(longs = {1, 11, 999_999_999, Long.MIN_VALUE, Long.MAX_VALUE})
		void testWriteSingle(long value) throws InterruptedException {
			instance.write(value);
			assertQueue(1, 1, 0, value);
		}

		@ParameterizedTest
		@CsvSource({
			"'{1, 2}'",
			"'{1, 2, 3}'",
			"'{3, 2, 1, 0}'",
			"'{1, 1}'",
		})
		void testWriteSequence(@LongArrayArg long[] values) throws InterruptedException {
			for(long value : values) {
				instance.write(value);
			}
			assertQueue(values.length, values.length, 0, values);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.BlockingLongBatchQueue#read(long[])}.
	 */
	@Test
	void testRead() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.BlockingLongBatchQueue#write(long[])}.
	 */
	@Test
	void testWrite() {
		fail("Not yet implemented"); // TODO
	}

}
