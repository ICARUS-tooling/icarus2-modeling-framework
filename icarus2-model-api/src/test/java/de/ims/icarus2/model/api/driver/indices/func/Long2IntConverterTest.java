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

import static de.ims.icarus2.model.api.ModelTestUtils.assertOverflow;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.function.IntConsumer;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
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
	@RandomizedTest
	void valid(RandomGenerator rand) {
		long[] values = rand.randomLongs(rand.random(10, 100), Integer.MIN_VALUE, Integer.MAX_VALUE);
		IntList tmp = new IntArrayList();

		LongStream.of(values).forEach(new Long2IntConverter(tmp::add));

		assertEquals(values.length, tmp.size());
		for (int i = 0; i < values.length; i++) {
			assertEquals(values[i], tmp.getInt(i));
		}
	}
}
