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
package de.ims.icarus2.util.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.util.collections.MinHeap.DoubleMinHeap;
import de.ims.icarus2.util.collections.MinHeap.IntMinHeap;
import de.ims.icarus2.util.collections.MinHeap.LongMinHeap;
import de.ims.icarus2.util.collections.MinHeap.ObjectMinHeap;

/**
 * @author Markus Gärtner
 *
 */
public class MinHeapTest {

	/**
	 *
	 * @param <H> type of the heap under test
	 * @param <T> type of elements stored in the heap
	 */
	private static <H extends MinHeap, T> void testHeap(
			H heap,
			BiConsumer<H, T> push,
			Function<H, T> pop,
			Comparator<T> comparator,
			@SuppressWarnings("unchecked") T...values) {

		T[] mins = values.clone();
		Arrays.sort(mins, comparator);

		// Sanity check for our test inputs
		assertEquals(values.length, mins.length);

		int size = values.length;

		// Assume initially empty heap
		assertTrue(heap.isEmpty());

		// Add all supplied values
		for(int i=0; i<size; i++) {
			push.accept(heap, values[i]);
		}

		assertFalse(heap.isEmpty());

		// Now remove all of them again
		for(int i=0; i<size; i++) {
			T min = pop.apply(heap);

			if(i==size-1) {
				assertTrue(heap.isEmpty());
			} else {
				assertEquals(mins[i], min);
			}
		}
	}


	@SuppressWarnings("boxing")
	@TestFactory
	List<DynamicTest> testIntMinHeap() throws Exception {
		return Arrays.asList(
				dynamicTest("sorted", () -> testHeap(new IntMinHeap(20),
						IntMinHeap::push,
						IntMinHeap::pop,
						Integer::compare,
						1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
				dynamicTest("random", () -> testHeap(new IntMinHeap(20),
						IntMinHeap::push,
						IntMinHeap::pop,
						Integer::compare,
						-1,  4,  2, 600,  0,  2, -5,  8, -999)),
				dynamicTest("negative", () -> testHeap(new IntMinHeap(20),
						IntMinHeap::push,
						IntMinHeap::pop,
						Integer::compare,
						-1, -2, -4, -3, -7, -999, -5, -3))
				);
	}

	@SuppressWarnings("boxing")
	@TestFactory
	List<DynamicTest> testLongMinHeap() throws Exception {
		return Arrays.asList(
				dynamicTest("sorted", () -> testHeap(new LongMinHeap(20),
						LongMinHeap::push,
						LongMinHeap::pop,
						Long::compare,
						1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)),
				dynamicTest("random", () -> testHeap(new LongMinHeap(20),
						LongMinHeap::push,
						LongMinHeap::pop,
						Long::compare,
						-1L,  4L,  2L, 600L,  0L,  2L, -5L,  8L, -999L)),
				dynamicTest("negative", () -> testHeap(new LongMinHeap(20),
						LongMinHeap::push,
						LongMinHeap::pop,
						Long::compare,
						-1L, -2L, -4L, -3L, -7L, -999L, -5L, -3L))
				);
	}
	@SuppressWarnings("boxing")
	@TestFactory
	List<DynamicTest> testDoubleMinHeap() throws Exception {
		return Arrays.asList(
				dynamicTest("sorted", () -> testHeap(new DoubleMinHeap(20),
						DoubleMinHeap::push,
						DoubleMinHeap::pop,
						Double::compare,
						0.1, 1.2, 2.3, 3.4, 4.5, 5.6, 6.7, 7.8, 8.9, 9.001)),
				dynamicTest("random", () -> testHeap(new DoubleMinHeap(20),
						DoubleMinHeap::push,
						DoubleMinHeap::pop,
						Double::compare,
						-1.1,  4.5,  2.3, 600.006,  0.0,  2.3, -5.4,  8.9, -999.111)),
				dynamicTest("negative", () -> testHeap(new DoubleMinHeap(20),
						DoubleMinHeap::push,
						DoubleMinHeap::pop,
						Double::compare,
						-1.1, -2.3, -4.3, -3.4, -7.8, -999.111, -5.4, -3.2))
				);
	}

	@SuppressWarnings("boxing")
	@TestFactory
	List<DynamicTest> testObjectMinHeap() throws Exception {

		/*
		 * NOTE:
		 *
		 * We use Integer here as representative of any random
		 * Comparable. This should be enough to prove the basic
		 * correctness of the heap implementation. Otherwise we'd
		 * need to use the version that also takes a Comparator
		 * and then carefully design test sequences that honor
		 * that comparator. For simplicity reasons we refrain
		 * from that (potentially error prone) alternative and
		 * stick with a bare Integer test.
		 */
		return Arrays.asList(
				dynamicTest("sorted", () -> testHeap(new ObjectMinHeap<Integer>(20),
						ObjectMinHeap::push,
						ObjectMinHeap::pop,
						Integer::compare,
						1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
				dynamicTest("random", () -> testHeap(new ObjectMinHeap<Integer>(20),
						ObjectMinHeap::push,
						ObjectMinHeap::pop,
						Integer::compare,
						-1,  4,  2, 600,  0,  2, -5,  8, -999)),
				dynamicTest("negative", () -> testHeap(new ObjectMinHeap<Integer>(20),
						ObjectMinHeap::push,
						ObjectMinHeap::pop,
						Integer::compare,
						-1, -2, -4, -3, -7, -999, -5, -3))
				);
	}
}
