/**
 *
 */
package de.ims.icarus2.model.api.driver.indices.func;

import static de.ims.icarus2.model.api.ModelTestUtils.assertIndicesEqualsExact;
import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.set;
import static de.ims.icarus2.model.api.ModelTestUtils.sorted;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.EMPTY_SET;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.test.TestUtils.randomId;
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
	void mergeEmpty() {
		assertMerge(EMPTY_SET, Stream.generate(() -> EMPTY_SET)
				.limit(random(3, 10))
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
	Stream<DynamicTest> singularDisjoint() {
		int count = random(4, 10);
		return IntStream.range(0, count)
				.mapToObj(index -> dynamicTest(String.valueOf(index), () -> {
					long value = randomId();
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
