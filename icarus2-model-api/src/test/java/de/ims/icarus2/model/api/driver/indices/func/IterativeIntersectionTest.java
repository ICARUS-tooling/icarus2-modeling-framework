/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.model.api.ModelTestUtils.assertIndicesEqualsExact;
import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelTestUtils;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet;
import de.ims.icarus2.util.collections.ArrayUtils;

/**
 * @author Markus Gärtner
 *
 */
class IterativeIntersectionTest {

	@Test
	void constructor() {
		assertNotNull(new IterativeIntersection());
	}

	@Test
	void unsortedInput() {
		assertModelException(ModelErrorCode.MODEL_UNSORTED_INDEX_SET,
				() -> new IterativeIntersection()
					.add(ModelTestUtils.set(1)).intersectAll());
	}

	private void assertIntersection(IndexSet expected, IndexSet...sets) {
		IndexSet result = new IterativeIntersection().add(sets).intersectAll();
		assertIndicesEqualsExact(expected, result);
	}

	private static IndexSet set(long...values) {
		assertTrue(ArrayUtils.isSorted(values, 0, values.length));
		return new ArrayIndexSet(IndexValueType.LONG, values, true);
	}

	@Test
	void equalSizedOverlapping() {
		assertIntersection(set(2, 9),
				set(1, 2,       5,       8, 9),
				set(   2,       5, 6, 7,    9),
				set(1, 2, 3, 4,             9),
				set(   2, 3,    5,       8, 9)
		);
	}

	@Test
	void disjointSets() {
		assertIntersection(IndexUtils.EMPTY_SET,
				set(1, 2),
				set(            5, 6, 7,    9),
				set(1, 2, 3, 4,             9),
				set(   2, 3,    5,       8, 9)
		);
	}

	@Test
	void differentSizedOverlapping() {
		assertIntersection(set(2, 5, 9),
				set(1, 2,       5,       8, 9, 11, 12, 13, 14, 15),
				set(   2,       5, 6, 7,    9, 21, 22, 23),
				set(1, 2, 3, 4, 5, 6,       9),
				set(   2, 3,    5,       8, 9)
		);
	}

	@Test
	void pyramid() {
		assertIntersection(set(1),
				set(1, 2, 3, 4, 5),
				set(1, 2, 3, 4),
				set(1, 2, 3),
				set(1, 2),
				set(1)
		);
	}

	@Test
	void withEmpty() {
		assertIntersection(IndexUtils.EMPTY_SET,
				set(1, 2, 3, 4, 5),
				set(1, 2, 3, 4),
				IndexUtils.EMPTY_SET,
				set(1, 2),
				set(1)
		);
	}

}
