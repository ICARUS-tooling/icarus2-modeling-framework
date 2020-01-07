/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.test.func.TriConsumer;
import de.ims.icarus2.util.collections.MappedMinHeap.MappedDoubleMinHeap;
import de.ims.icarus2.util.collections.MappedMinHeap.MappedIntMinHeap;
import de.ims.icarus2.util.collections.MappedMinHeap.MappedLongMinHeap;
import de.ims.icarus2.util.collections.MappedMinHeap.MappedObjectMinHeap;

/**
 * @author Markus Gärtner
 *
 */
class MappedMinHeapTest {

	/**
	 *
	 * @param <H> type of the heap under test
	 * @param <T> type of elements stored in the heap
	 * @param <V> type of payload associated with each element
	 */
	private static <H extends MappedMinHeap<V>, T, V> void testHeap(
			H heap,
			TriConsumer<H, V, T> push,
			Function<H, V> pop,
			Function<H, T> peekValue,
			Comparator<T> comparator,
			IntFunction<V> payloadGen,
			@SuppressWarnings("unchecked") T...values) {

		T[] mins = values.clone();
		Arrays.sort(mins, comparator);

		Map<T, V> map = new HashMap<>();
		for (int i = 0; i < mins.length; i++) {
			map.put(mins[i], payloadGen.apply(i));
		}

		// Sanity check for our test inputs
		assertEquals(values.length, mins.length);

		int size = values.length;

		// Assume initially empty heap
		assertTrue(heap.isEmpty());

		// Add all supplied values
		for(int i=0; i<size; i++) {
			push.accept(heap, map.get(values[i]), values[i]);
		}

		assertFalse(heap.isEmpty());

		// Now remove all of them again
		for(int i=0; i<size; i++) {
			T minPeek = peekValue.apply(heap);
			V payloadPeek = heap.peekObject();
			assertEquals(map.get(minPeek), payloadPeek);

			V payload = pop.apply(heap);
			assertEquals(payloadPeek, payload);

			if(i==size-1) {
				assertTrue(heap.isEmpty());
			} else {
				assertEquals(mins[i], minPeek);
			}
		}
	}

	@SuppressWarnings("boxing")
	@TestFactory
	List<DynamicTest> testIntMinHeap() throws Exception {
		return Arrays.asList(
				dynamicTest("sorted", () -> testHeap(new MappedIntMinHeap<String>(20),
						MappedIntMinHeap::push,
						MappedIntMinHeap::pop,
						MappedIntMinHeap::peekValue,
						Integer::compare,
						i -> "val_"+i,
						1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
				dynamicTest("random", () -> testHeap(new MappedIntMinHeap<String>(20),
						MappedIntMinHeap::push,
						MappedIntMinHeap::pop,
						MappedIntMinHeap::peekValue,
						Integer::compare,
						i -> "val_"+i,
						-1,  4,  2, 600,  0,  2, -5,  8, -999)),
				dynamicTest("negative", () -> testHeap(new MappedIntMinHeap<String>(20),
						MappedIntMinHeap::push,
						MappedIntMinHeap::pop,
						MappedIntMinHeap::peekValue,
						Integer::compare,
						i -> "val_"+i,
						-1, -2, -4, -3, -7, -999, -5, -3))
				);
	}

	@SuppressWarnings("boxing")
	@TestFactory
	List<DynamicTest> testLongMinHeap() throws Exception {
		return Arrays.asList(
				dynamicTest("sorted", () -> testHeap(new MappedLongMinHeap<String>(20),
						MappedLongMinHeap::push,
						MappedLongMinHeap::pop,
						MappedLongMinHeap::peekValue,
						Long::compare,
						i -> "val_"+i,
						1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)),
				dynamicTest("random", () -> testHeap(new MappedLongMinHeap<String>(20),
						MappedLongMinHeap::push,
						MappedLongMinHeap::pop,
						MappedLongMinHeap::peekValue,
						Long::compare,
						i -> "val_"+i,
						-1L,  4L,  2L, 600L,  0L,  2L, -5L,  8L, -999L)),
				dynamicTest("negative", () -> testHeap(new MappedLongMinHeap<String>(20),
						MappedLongMinHeap::push,
						MappedLongMinHeap::pop,
						MappedLongMinHeap::peekValue,
						Long::compare,
						i -> "val_"+i,
						-1L, -2L, -4L, -3L, -7L, -999L, -5L, -3L))
				);
	}

	@SuppressWarnings("boxing")
	@TestFactory
	List<DynamicTest> testDoubleMinHeap() throws Exception {
		return Arrays.asList(
				dynamicTest("sorted", () -> testHeap(new MappedDoubleMinHeap<String>(20),
						MappedDoubleMinHeap::push,
						MappedDoubleMinHeap::pop,
						MappedDoubleMinHeap::peekValue,
						Double::compare,
						i -> "val_"+i,
						0.1, 1.2, 2.3, 3.4, 4.5, 5.6, 6.7, 7.8, 8.9, 9.001)),
				dynamicTest("random", () -> testHeap(new MappedDoubleMinHeap<String>(20),
						MappedDoubleMinHeap::push,
						MappedDoubleMinHeap::pop,
						MappedDoubleMinHeap::peekValue,
						Double::compare,
						i -> "val_"+i,
						-1.1,  4.5,  2.3, 600.006,  0.0,  2.3, -5.4,  8.9, -999.111)),
				dynamicTest("negative", () -> testHeap(new MappedDoubleMinHeap<String>(20),
						MappedDoubleMinHeap::push,
						MappedDoubleMinHeap::pop,
						MappedDoubleMinHeap::peekValue,
						Double::compare,
						i -> "val_"+i,
						-1.1, -2.3, -4.3, -3.4, -7.8, -999.111, -5.4, -3.2))
				);
	}

	@SuppressWarnings("boxing")
	@TestFactory
	List<DynamicTest> testObjectMinHeap() throws Exception {
		return Arrays.asList(
				dynamicTest("sorted", () -> testHeap(
						new MappedObjectMinHeap<Integer, String>(20),
						MappedObjectMinHeap::push,
						MappedObjectMinHeap::pop,
						MappedObjectMinHeap::peekValue,
						Integer::compare,
						i -> "val_"+i,
						1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
				dynamicTest("random", () -> testHeap(
						new MappedObjectMinHeap<Integer, String>(20),
						MappedObjectMinHeap::push,
						MappedObjectMinHeap::pop,
						MappedObjectMinHeap::peekValue,
						Integer::compare,
						i -> "val_"+i,
						-1,  4,  2, 600,  0,  2, -5,  8, -999)),
				dynamicTest("negative", () -> testHeap(
						new MappedObjectMinHeap<Integer, String>(20),
						MappedObjectMinHeap::push,
						MappedObjectMinHeap::pop,
						MappedObjectMinHeap::peekValue,
						Integer::compare,
						i -> "val_"+i,
						-1, -2, -4, -3, -7, -999, -5, -3))
				);
	}
}
