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
package de.ims.icarus2.model.api.driver.indices;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.range;
import static de.ims.icarus2.model.api.ModelTestUtils.set;
import static de.ims.icarus2.model.api.ModelTestUtils.sorted;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.checkNonEmpty;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.checkNotNegative;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.isContinuous;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.test.annotations.PostponedTest;

/**
 * @author Markus Gärtner
 *
 */
class IndexUtilsTest {

	class DominantType {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#getDominantType(de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
		 */
		@Test
		void testGetDominantTypeIndexSetArray() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#getDominantType(de.ims.icarus2.model.api.driver.indices.IndexSet[], int, int)}.
		 */
		@Test
		void testGetDominantTypeIndexSetArrayIntInt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#getDominantType(java.util.Collection)}.
		 */
		@Test
		void testGetDominantTypeCollectionOfQextendsIndexSet() {
			fail("Not yet implemented"); // TODO
		}
	}

	class Sorting {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#isSorted(de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
		 */
		@Test
		void testIsSortedIndexSetArray() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#isSorted(de.ims.icarus2.model.api.driver.indices.IndexSet, int, int)}.
		 */
		@Test
		void testIsSortedIndexSetIntInt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#checkSorted(long, long)}.
		 */
		@Test
		void testCheckSortedLongLong() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#checkSorted(de.ims.icarus2.model.api.driver.indices.IndexSet)}.
		 */
		@Test
		void testCheckSortedIndexSet() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#checkSorted(de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
		 */
		@Test
		void testCheckSortedIndexSetArray() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#ensureSorted(de.ims.icarus2.model.api.driver.indices.IndexSet)}.
		 */
		@Test
		void testEnsureSortedIndexSet() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#ensureSorted(de.ims.icarus2.model.api.driver.indices.IndexSet, de.ims.icarus2.model.api.driver.mapping.RequestSettings)}.
		 */
		@Test
		void testEnsureSortedIndexSetRequestSettings() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#ensureSorted(de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
		 */
		@Test
		void testEnsureSortedIndexSetArray() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#ensureSorted(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.mapping.RequestSettings)}.
		 */
		@Test
		void testEnsureSortedIndexSetArrayRequestSettings() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#sort(de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
		 */
		@Test
		void testSort() {
			fail("Not yet implemented"); // TODO
		}
	}

	class CountingAndSize {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#count(de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
		 */
		@Test
		void testCountIndexSetArray() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#count(java.util.Collection)}.
		 */
		@Test
		void testCountCollectionOfQextendsIndexSet() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#minSize(de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
		 */
		@Test
		void testMinSize() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#maxSize(de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
		 */
		@Test
		void testMaxSize() {
			fail("Not yet implemented"); // TODO
		}

	}

	@Nested
	class Continuity {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#isContinuous(de.ims.icarus2.model.api.driver.indices.IndexSet)}.
		 */
		@TestFactory
		List<DynamicTest> testIsContinuousIndexSet() {
			return Arrays.asList(
					dynamicTest("empty", () -> assertFalse(isContinuous(set()))),
					// Ranges
					dynamicTest("range [1]", () -> assertTrue(isContinuous(range(1, 1)))),
					dynamicTest("range [1..10]", () -> assertTrue(isContinuous(range(1, 10)))),
					dynamicTest("range [2..9]", () -> assertTrue(isContinuous(range(2, 9)))),
					// Sorted sets
					dynamicTest("sorted [1,3,5,10]", () -> assertFalse(isContinuous(sorted(1,3,5,10)))),
					dynamicTest("sorted [2,3,4,9]", () -> assertFalse(isContinuous(sorted(2,3,4,9)))),
					// Unsorted sets
					dynamicTest("unsorted [3,2,1]", () -> assertFalse(isContinuous(set(3,2,1)))),
					dynamicTest("unsorted [9,2,7]", () -> assertFalse(isContinuous(set(9,2,7))))
			);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#isContinuous(de.ims.icarus2.model.api.driver.indices.IndexSet, int, int)}.
		 */
		@PostponedTest
		@Test
		void testIsContinuousIndexSetIntInt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#isContinuous(de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
		 */
		@PostponedTest
		@Test
		void testIsContinuousIndexSetArray() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#checkNonEmpty(de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
		 */
		@TestFactory
		List<DynamicTest> testCheckNonEmpty() {
			return Arrays.asList(
					dynamicTest("null array", () -> assertNPE(() -> checkNonEmpty(null))),
					dynamicTest("empty array", () -> assertModelException(
							GlobalErrorCode.INVALID_INPUT, () -> checkNonEmpty(new IndexSet[0]))),
					dynamicTest("empty indices", () -> assertModelException(
							GlobalErrorCode.INVALID_INPUT, () -> checkNonEmpty(
									new IndexSet[] {set()}))),
					dynamicTest("valid indices", () -> checkNonEmpty(new IndexSet[] {set(1)}))
			);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#checkNotNegative(long)}.
		 */
		@ParameterizedTest
		@ValueSource(longs = {0, 1, Long.MAX_VALUE, -1L, Long.MIN_VALUE})
		void testCheckNotNegative(long value) {
			if(value<0L) {
				assertModelException(GlobalErrorCode.INVALID_INPUT, () -> checkNotNegative(value));
			} else {
				checkNotNegative(value);
			}
		}

	}

	class Conversion {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#toIndices(java.util.Collection, boolean)}.
		 */
		@Test
		void testToIndices() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#wrap(long)}.
		 */
		@Test
		void testWrapLong() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#span(long, long)}.
		 */
		@Test
		void testSpan() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#wrapSpan(long, long)}.
		 */
		@Test
		void testWrapSpan() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#wrap(long[])}.
		 */
		@Test
		void testWrapLongArray() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#wrap(int[])}.
		 */
		@Test
		void testWrapIntArray() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#wrap(short[])}.
		 */
		@Test
		void testWrapShortArray() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#wrap(byte[])}.
		 */
		@Test
		void testWrapByteArray() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#wrap(de.ims.icarus2.model.api.driver.indices.IndexSet)}.
		 */
		@Test
		void testWrapIndexSet() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#unwrap(de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
		 */
		@Test
		void testUnwrap() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#externalize(de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
		 */
		@Test
		void testExternalize() {
			fail("Not yet implemented"); // TODO
		}

	}

	class Combine {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#combine(de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
		 */
		@Test
		void testCombineIndexSetArray() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#combine(java.util.List)}.
		 */
		@Test
		void testCombineListOfQextendsIndexSet() {
			fail("Not yet implemented"); // TODO
		}

	}

	class Merge {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#merge(de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
		 */
		@Test
		void testMergeIndexSetArray() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#merge(java.util.Collection)}.
		 */
		@Test
		void testMergeCollectionOfQextendsIndexSet() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#mergeToArray(de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
		 */
		@Test
		void testMergeToArrayIndexSetArray() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#mergeToArray(java.util.Collection)}.
		 */
		@Test
		void testMergeToArrayCollectionOfQextendsIndexSet() {
			fail("Not yet implemented"); // TODO
		}

	}

	class Intersect {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#intersect(de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
		 */
		@Test
		void testIntersectIndexSetArray() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#intersect(java.util.Collection)}.
		 */
		@Test
		void testIntersectCollectionOfQextendsIndexSet() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#intersect(long, long, long, long)}.
		 */
		@Test
		void testIntersectLongLongLongLong() {
			fail("Not yet implemented"); // TODO
		}

	}

	class TraverseAndSearch {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#forEachSpan(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.indices.IndexUtils.SpanProcedure)}.
		 */
		@Test
		void testForEachSpanIndexSetArraySpanProcedure() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#forEachSpan(de.ims.icarus2.model.api.driver.indices.IndexSet, de.ims.icarus2.model.api.driver.indices.IndexUtils.SpanProcedure)}.
		 */
		@Test
		void testForEachSpanIndexSetSpanProcedure() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#forEachPair(de.ims.icarus2.model.api.driver.indices.IndexSet, de.ims.icarus2.model.api.driver.indices.IndexSet, java.util.function.LongBinaryOperator)}.
		 */
		@Test
		void testForEachPairIndexSetIndexSetLongBinaryOperator() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#forEachPair(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.indices.IndexSet[], java.util.function.LongBinaryOperator)}.
		 */
		@Test
		void testForEachPairIndexSetArrayIndexSetArrayLongBinaryOperator() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#forEachIndex(de.ims.icarus2.model.api.driver.indices.IndexSet[], java.util.function.LongConsumer)}.
		 */
		@Test
		void testForEachIndexIndexSetArrayLongConsumer() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#forEachIndex(de.ims.icarus2.model.api.driver.indices.IndexSet[], java.util.function.IntConsumer)}.
		 */
		@Test
		void testForEachIndexIndexSetArrayIntConsumer() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#binarySearch(de.ims.icarus2.model.api.driver.indices.IndexSet, long)}.
		 */
		@Test
		void testBinarySearchIndexSetLong() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#binarySearch(de.ims.icarus2.model.api.driver.indices.IndexSet, int, int, long)}.
		 */
		@Test
		void testBinarySearchIndexSetIntIntLong() {
			fail("Not yet implemented"); // TODO
		}

	}

	class StreamsAndIterators {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#asIntStream(de.ims.icarus2.model.api.driver.indices.IndexSet)}.
		 */
		@Test
		void testAsIntStream() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#asLongStream(de.ims.icarus2.model.api.driver.indices.IndexSet)}.
		 */
		@Test
		void testAsLongStream() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#asIterator(de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
		 */
		@Test
		void testAsIteratorIndexSetArray() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#asIterator(de.ims.icarus2.model.api.driver.indices.IndexSet)}.
		 */
		@Test
		void testAsIteratorIndexSet() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#asIterator(de.ims.icarus2.model.api.driver.indices.IndexSet, int)}.
		 */
		@Test
		void testAsIteratorIndexSetInt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#asIterator(de.ims.icarus2.model.api.driver.indices.IndexSet, int, int)}.
		 */
		@Test
		void testAsIteratorIndexSetIntInt() {
			fail("Not yet implemented"); // TODO
		}

	}

	class Misc {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#firstIndex(de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
		 */
		@Test
		void testFirstIndex() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#lastIndex(de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
		 */
		@Test
		void testLastIndex() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#toString(de.ims.icarus2.model.api.driver.indices.IndexSet)}.
		 */
		@Test
		void testToStringIndexSet() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexUtils#toString(de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
		 */
		@Test
		void testToStringIndexSetArray() {
			fail("Not yet implemented"); // TODO
		}

	}

}
