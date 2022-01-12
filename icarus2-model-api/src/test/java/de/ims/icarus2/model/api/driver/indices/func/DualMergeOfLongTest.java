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

import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.lang.Primitives._int;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator.OfLong;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

import de.ims.icarus2.test.annotations.DisabledOnCi;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

/**
 * @author Markus Gärtner
 *
 */
class DualMergeOfLongTest {

	@Test
	void constructor() {
		assertNotNull(new DualMergeOfLong(mock(OfLong.class), mock(OfLong.class)));
	}

	@Test
	void constructorNull() {
		assertNPE(() -> new DualMergeOfLong(null, mock(OfLong.class)));
		assertNPE(() -> new DualMergeOfLong(mock(OfLong.class), null));
	}

	private long[] merge(LongStream left, LongStream right) {
		LongList tmp = new LongArrayList();
		DualMergeOfLong merger = new DualMergeOfLong(left.iterator(), right.iterator());
		merger.forEachRemaining((LongConsumer)tmp::add);
		assertFalse(merger.hasNext());
		assertThrows(NoSuchElementException.class, () -> merger.nextLong());
		return tmp.toLongArray();
	}

	@Test
	void leftEmpty() {
		assertArrayEquals(new long[] {1, 2, 3, 4, 5},
				merge(
				LongStream.of(),
				LongStream.of(1, 2, 3, 4, 5)
		));
	}

	@Test
	void rightEmpty() {
		assertArrayEquals(new long[] {1, 2, 3, 4, 5},
				merge(
				LongStream.of(1, 2, 3, 4, 5),
				LongStream.of()
		));
	}

	@Test
	void empty() {
		assertArrayEquals(new long[] {}, merge(
				LongStream.of(),
				LongStream.of()));
	}

	@Test
	void twoSplitMerge1() {
		assertArrayEquals(new long[] {1, 2, 3, 4, 5},
				merge(
				LongStream.of(1, 2, 3),
				LongStream.of(4, 5)
		));
	}

	@Test
	void twoSplitMerge2() {
		assertArrayEquals(new long[] {1, 2, 3, 4, 5},
				merge(
				LongStream.of(4, 5),
				LongStream.of(1, 2, 3)
		));
	}

	@Test
	void mixedMergeUnique() {
		assertArrayEquals(new long[] {1, 2, 3, 4, 5},
				merge(
				LongStream.of(1, 3, 5),
				LongStream.of(2, 4)
		));
	}

	@Test
	void mixedMergeDoubles() {
		assertArrayEquals(new long[] {1, 2, 3, 3, 4, 4, 5},
				merge(
				LongStream.of(1, 3, 4, 5),
				LongStream.of(2, 3, 4)
		));
	}

	@Test
	@DisabledOnCi
	@RandomizedTest
	void large(TestReporter reporter, RandomGenerator rand) {
		Instant start = Instant.now();
		int sizeLeft = 100_000_000;
		int sizeRight = 30_000_000;
		OfLong left = LongStream.iterate(rand.random(Long.MIN_VALUE, Integer.MAX_VALUE),
					v -> v+1_000_000)
				.limit(sizeLeft)
				.iterator();
		OfLong right = LongStream.iterate(rand.random(Long.MIN_VALUE, Integer.MAX_VALUE),
					v -> v+10_000_000)
				.limit(sizeRight)
				.iterator();

		Instant streamsDone = Instant.now();
		reporter.publishEntry(String.format("Streams constructed for %d and %d entries: %s",
				_int(sizeLeft), _int(sizeRight), Duration.between(start, streamsDone)));

		DualMergeOfLong merger = new DualMergeOfLong(left, right);

		int count = 0;
		long previous = UNSET_LONG;
		while(merger.hasNext()) {
			long value = merger.nextLong();
			if(count>0) {
				assertTrue(value>=previous);
			}
			previous = value;
			count++;
		}
		assertEquals(sizeLeft+sizeRight, count);
		Instant end = Instant.now();
		reporter.publishEntry("Done after "+Duration.between(streamsDone, end));
	}

	@Test
	void nextLongEmpty() {
		assertThrows(NoSuchElementException.class,
				() -> new DualMergeOfLong(LongStream.empty().iterator(),
						LongStream.empty().iterator()).nextLong());
	}

}
