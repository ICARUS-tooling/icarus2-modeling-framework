/**
 *
 */
package de.ims.icarus2.model.api.driver.indices.func;

import static de.ims.icarus2.model.api.ModelTestUtils.set;
import static de.ims.icarus2.test.TestUtils.assertIAE;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.test.TestUtils.random;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator.OfLong;
import java.util.function.LongConsumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

/**
 * @author Markus Gärtner
 *
 */
class HeapMergeOfLongTest {

	@Test
	void constructor() {
		assertNotNull(new HeapMergeOfLong(new OfLong[] {
				mock(OfLong.class), mock(OfLong.class), mock(OfLong.class)}));
	}

	@Test
	void constructorNull() {
		assertNPE(() -> new HeapMergeOfLong(null));
	}

	@Test
	void constructorEmpty() {
		assertIAE(() -> new HeapMergeOfLong(new OfLong[0]));
	}

	@Test
	void constructorInsufficient() {
		assertIAE(() -> new HeapMergeOfLong(new OfLong[] {
				mock(OfLong.class), mock(OfLong.class)}));
	}

	private static long[] merge(LongStream...streams) {
		OfLong[] iterators = Stream.of(streams)
				.map(LongStream::iterator)
				.toArray(OfLong[]::new);
		return merge(new HeapMergeOfLong(iterators));
	}

	private static long[] merge(HeapMergeOfLong merger) {
		LongList tmp = new LongArrayList();
		merger.forEachRemaining((LongConsumer)tmp::add);
		assertFalse(merger.hasNext());
		assertThrows(NoSuchElementException.class, () -> merger.nextLong());
		return tmp.toLongArray();
	}

	@Test
	void empty() {
		assertArrayEquals(new long[0], merge(
				LongStream.of(),
				LongStream.of(),
				LongStream.of(),
				LongStream.of()
		));
	}

	@TestFactory
	Stream<DynamicTest> singular() {
		return IntStream.range(0, 4)
				.mapToObj(index -> dynamicTest(String.valueOf(index), () -> {
					LongStream[] streams = Stream.generate(LongStream::empty)
							.limit(4)
							.toArray(LongStream[]::new);
					long value = random().nextLong();
					streams[index] = LongStream.of(value);

					assertArrayEquals(new long[] {value}, merge(streams));
				}));
	}

	@Test
	void disjointStreams() {
		assertArrayEquals(LongStream.rangeClosed(1, 9).toArray(), merge(
				LongStream.of(1, 2),
				LongStream.of(5, 6),
				LongStream.of(3, 4),
				LongStream.of(7, 8, 9)
		));
	}

	@Test
	void overlappingStreams() {
		assertArrayEquals(new long[] {
				1, 1, 2, 3, 3, 4, 5, 5, 6, 7, 8, 8, 9, 9
		}, merge(
				LongStream.of(1, 2, 5, 8),
				LongStream.of(5, 6, 9),
				LongStream.of(1, 3, 4),
				LongStream.of(3, 7, 8, 9)
		));
	}

	@Test
	void overlappingArrays() {
		assertArrayEquals(new long[] {
				1, 1, 2, 3, 3, 4, 5, 5, 6, 7, 8, 8, 9, 9
		}, merge(HeapMergeOfLong.fromArrays(
				new long[] {1, 2, 5, 8},
				new long[] {5, 6, 9},
				new long[] {1, 3, 4},
				new long[] {3, 7, 8, 9}
		)));
	}

	@Test
	void overlappingIndexSetArray() {
		assertArrayEquals(new long[] {
				1, 1, 2, 3, 3, 4, 5, 5, 6, 7, 8, 8, 9, 9
		}, merge(HeapMergeOfLong.fromIndices(
				set(1, 2, 5, 8),
				set(5, 6, 9),
				set(1, 3, 4),
				set(3, 7, 8, 9)
		)));
	}

	@Test
	void overlappingIndexSetCollection() {
		assertArrayEquals(new long[] {
				1, 1, 2, 3, 3, 4, 5, 5, 6, 7, 8, 8, 9, 9
		}, merge(HeapMergeOfLong.fromIndices(Arrays.asList(
				set(1, 2, 5, 8),
				set(5, 6, 9),
				set(1, 3, 4),
				set(3, 7, 8, 9)
		))));
	}
}
