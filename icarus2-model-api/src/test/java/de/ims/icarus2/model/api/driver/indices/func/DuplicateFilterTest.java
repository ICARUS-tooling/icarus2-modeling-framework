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

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.util.collections.ArrayUtils.fillAscending;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.function.LongConsumer;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
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
		assertThat(filter(1, 1, 1, 1, 1)).containsExactly(1);
	}

	@Test
	@RandomizedTest
	void sortedUnique(RandomGenerator rand) {
		long[] values = fillAscending(new long[rand.random(10, 100)], rand.random(0, Long.MAX_VALUE/2));
		assertThat(filter(values)).containsExactly(values);
	}

	@Test
	void unsorted() {
		assertModelException(GlobalErrorCode.INVALID_INPUT,
				() -> filter(1, 2, 3, 5, 4, 6, 7, 8));
	}
}
