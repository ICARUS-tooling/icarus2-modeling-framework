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

import static de.ims.icarus2.model.api.ModelTestUtils.assertOverflow;
import static de.ims.icarus2.test.TestUtils.assertNPE;
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

import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

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
	@RandomizedTest
	void testHasNext(RandomGenerator rand) {
		Long2IntIterator it = new Long2IntIterator(LongStream.of(rand.nextLong()).iterator());
		assertTrue(it.hasNext());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.func.Long2IntIterator#nextInt()}.
	 */
	@Test
	@RandomizedTest
	void testNextInt(RandomGenerator rand) {
		long[] values = rand.randomLongs(rand.random(10, 100), Integer.MIN_VALUE, Integer.MAX_VALUE);
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
