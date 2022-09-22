/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
		assertThat(merge(
				LongStream.of(),
				LongStream.of(),
				LongStream.of(),
				LongStream.of()
		)).isEmpty();
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

					assertThat(merge(streams)).containsExactly(value);
				}));
	}

	@Test
	void disjointStreams() {
		assertThat(merge(
				LongStream.of(1, 2),
				LongStream.of(5, 6),
				LongStream.of(3, 4),
				LongStream.of(7, 8, 9)
		)).containsExactly(LongStream.rangeClosed(1, 9).toArray());
	}

	@Test
	void overlappingStreams() {
		assertThat(merge(
				LongStream.of(1, 2, 5, 8),
				LongStream.of(5, 6, 9),
				LongStream.of(1, 3, 4),
				LongStream.of(3, 7, 8, 9)
		)).containsExactly(1, 1, 2, 3, 3, 4, 5, 5, 6, 7, 8, 8, 9, 9);
	}

	@Test
	void overlappingArrays() {
		assertThat(merge(HeapMergeOfLong.fromArrays(
				new long[] {1, 2, 5, 8},
				new long[] {5, 6, 9},
				new long[] {1, 3, 4},
				new long[] {3, 7, 8, 9}
		))).containsExactly(1, 1, 2, 3, 3, 4, 5, 5, 6, 7, 8, 8, 9, 9);
	}

	@Test
	void overlappingIndexSetArray() {
		assertThat(merge(HeapMergeOfLong.fromIndices(
				set(1, 2, 5, 8),
				set(5, 6, 9),
				set(1, 3, 4),
				set(3, 7, 8, 9)
		))).containsExactly(1, 1, 2, 3, 3, 4, 5, 5, 6, 7, 8, 8, 9, 9);
	}

	@Test
	void overlappingIndexSetCollection() {
		assertThat(merge(HeapMergeOfLong.fromIndices(Arrays.asList(
				set(1, 2, 5, 8),
				set(5, 6, 9),
				set(1, 3, 4),
				set(3, 7, 8, 9)
		)))).containsExactly(1, 1, 2, 3, 3, 4, 5, 5, 6, 7, 8, 8, 9, 9);
	}
}
