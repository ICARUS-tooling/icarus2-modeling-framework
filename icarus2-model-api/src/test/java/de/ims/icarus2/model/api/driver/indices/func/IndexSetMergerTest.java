/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.model.api.driver.indices.func;

import static de.ims.icarus2.model.api.ModelTestUtils.assertIndicesEqualsExact;
import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.set;
import static de.ims.icarus2.model.api.ModelTestUtils.sorted;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.EMPTY_SET;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
class IndexSetMergerTest {

	//TODO properly test the extended merge method mergeAllToArray()

	static void assertMerge(IndexSet expected, IndexSet...sets) {
		IndexSet result = new IndexSetMerger().add(sets).mergeAllToSingle();
		assertIndicesEqualsExact(expected, result);
	}

	@Test
	void constructor() {
		assertNotNull(new IndexSetMerger());
	}

	@Test
	void unsorted() {
		assertModelException(ModelErrorCode.MODEL_UNSORTED_INDEX_SET,
				() -> assertMerge(EMPTY_SET, set(), set(), set()));
	}

	@Test
	@RandomizedTest
	void mergeEmpty(RandomGenerator rand) {
		assertMerge(EMPTY_SET, Stream.generate(() -> EMPTY_SET)
				.limit(rand.random(3, 10))
				.toArray(IndexSet[]::new));
	}

	@Test
	void mergeNone() {
		assertMerge(EMPTY_SET);
	}

	@Test
	void mergeDualEmpty() {
		assertMerge(EMPTY_SET, sorted(), sorted());
	}

	@Test
	void mergeSingleEmpty() {
		assertMerge(EMPTY_SET, sorted());
	}

	@TestFactory
	@RandomizedTest
	Stream<DynamicTest> singularDisjoint(RandomGenerator rand) {
		int count = rand.random(4, 10);
		return IntStream.range(0, count)
				.mapToObj(index -> dynamicTest(String.valueOf(index), () -> {
					long value = rand.randomId();
					IndexSet[] sets = new IndexSet[count];
					Arrays.fill(sets, EMPTY_SET);
					sets[index] = sorted(value);
					assertMerge(set(value), sets);
				}));
	}

	@Test
	void mergeDual() {
		assertMerge(set(1, 2, 3, 4), sorted(1, 2), sorted(3, 4));
	}

	@Test
	void mergeSingle() {
		assertMerge(set(1, 2), sorted(1, 2));
	}

}
