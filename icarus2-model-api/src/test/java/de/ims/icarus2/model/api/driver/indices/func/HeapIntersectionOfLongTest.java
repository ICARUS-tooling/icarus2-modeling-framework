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
package de.ims.icarus2.model.api.driver.indices.func;

import static de.ims.icarus2.model.api.ModelTestUtils.set;
import static de.ims.icarus2.test.TestUtils.assertIAE;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static org.assertj.core.api.Assertions.assertThat;
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

import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

/**
 * @author Markus Gärtner
 *
 */
class HeapIntersectionOfLongTest {

	@Test
	void constructor() {
		assertNotNull(new HeapIntersectionOfLong(new OfLong[] {
				mock(OfLong.class), mock(OfLong.class), mock(OfLong.class)}));
	}

	@Test
	void constructorNull() {
		assertNPE(() -> new HeapIntersectionOfLong(null));
	}

	@Test
	void constructorEmpty() {
		assertIAE(() -> new HeapIntersectionOfLong(new OfLong[0]));
	}

	@Test
	void constructorInsufficient() {
		assertIAE(() -> new HeapIntersectionOfLong(new OfLong[] {
				mock(OfLong.class), mock(OfLong.class)}));
	}

	private static long[] intersect(LongStream...streams) {
		OfLong[] iterators = Stream.of(streams)
				.map(LongStream::iterator)
				.toArray(OfLong[]::new);
		return intersect(new HeapIntersectionOfLong(iterators));
	}

	private static long[] intersect(HeapIntersectionOfLong merger) {
		LongList tmp = new LongArrayList();
		merger.forEachRemaining((LongConsumer)tmp::add);
		assertFalse(merger.hasNext());
		assertThrows(NoSuchElementException.class, () -> merger.nextLong());
		return tmp.toLongArray();
	}

	@Test
	void empty() {
		assertArrayEquals(new long[0], intersect(
				LongStream.of(),
				LongStream.of(),
				LongStream.of(),
				LongStream.of()
		));
	}

	@TestFactory
	@RandomizedTest
	Stream<DynamicTest> singular(RandomGenerator rand) {
		return IntStream.range(0, 4)
				.mapToObj(index -> dynamicTest(String.valueOf(index), () -> {
					LongStream[] streams = Stream.generate(LongStream::empty)
							.limit(4)
							.toArray(LongStream[]::new);
					long value = rand.nextLong();
					streams[index] = LongStream.of(value);

					assertArrayEquals(new long[0], intersect(streams));
				}));
	}

	@Test
	@RandomizedTest
	void shared(RandomGenerator rand) {
		long value = rand.nextLong();
		assertThat(intersect(
				LongStream.of(value),
				LongStream.of(value),
				LongStream.of(value),
				LongStream.of(value)
		)).containsExactly(value);
	}

	@Test
	void disjointStreams() {
		assertThat(intersect(
				LongStream.of(1, 2),
				LongStream.of(5, 6),
				LongStream.of(3, 4),
				LongStream.of(7, 8, 9)
		)).isEmpty();
	}

	@Test
	void overlappingStreams() {
		assertThat(intersect(
				LongStream.of(1, 2,       5,       8, 9),
				LongStream.of(   2,       5, 6, 7,    9),
				LongStream.of(1, 2, 3, 4,             9),
				LongStream.of(   2, 3,    5,       8, 9)
		)).containsExactly(2, 9);
	}

	@Test
	void overlappingArrays() {
		assertThat(intersect(HeapIntersectionOfLong.fromArrays(
				new long[] {1, 2,       5,       8, 9},
				new long[] {   2,       5, 6, 7,    9},
				new long[] {1, 2, 3, 4,             9},
				new long[] {   2, 3,    5,       8, 9}
		))).containsExactly(2, 9);
	}

	@Test
	void overlappingIndexSetArray() {
		assertThat(intersect(HeapIntersectionOfLong.fromIndices(
				set(1, 2,       5,       8, 9),
				set(   2,       5, 6, 7,    9),
				set(1, 2, 3, 4,             9),
				set(   2, 3,    5,       8, 9)
		))).containsExactly(2, 9);
	}

	@Test
	void overlappingIndexSetCollection() {
		assertThat(intersect(HeapIntersectionOfLong.fromIndices(Arrays.asList(
				set(1, 2,       5,       8, 9),
				set(   2,       5, 6, 7,    9),
				set(1, 2, 3, 4,             9),
				set(   2, 3,    5,       8, 9)
		)))).containsExactly(2, 9);
	}
}
