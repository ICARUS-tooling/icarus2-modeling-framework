/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.util.collections.MinHeap.DoubleMinHeap;
import de.ims.icarus2.util.collections.MinHeap.IntMinHeap;
import de.ims.icarus2.util.collections.MinHeap.LongMinHeap;
import de.ims.icarus2.util.collections.MinHeap.ObjectMinHeap;

/**
 * @author Markus Gärtner
 *
 */
public class MinHeapTest {

	@Test
	public void testIntMinHeap() throws Exception {

		IntMinHeap heap = new IntMinHeap(20);

		// Basic test with sorted input
		int[] values = ArrayUtils.fillAscending(new int[10]);

		// Test with random input
		values = new int[] {-1,  4,  2, 600,  0,  2, -5,  8, -999};
		test(heap, values);

		// Test with negative input only
		values = new int[] {-1, -2, -4, -3, -7, -999, -5, -3};
		test(heap, values);
	}

	private static void test(IntMinHeap heap, int[] values) {

		int[] mins = values.clone();
		Arrays.sort(mins);

		// Sanity check for our test inputs
		assertEquals(values.length, mins.length);

		int size = values.length;

		// Assume initially empty heap
		assertTrue(heap.isEmpty());

		// Add all supplied values
		for(int i=0; i<size; i++) {
			heap.push(values[i]);
		}

		// Now remove all of them again
		for(int i=0; i<size; i++) {
			int min = heap.pop();

			if(i==size-1) {
				assertTrue(heap.isEmpty());
			} else {
				assertEquals(mins[i], min);
			}
		}

	}

	@Test
	public void testLongMinHeap() throws Exception {

		LongMinHeap heap = new LongMinHeap(20);

		// Basic test with sorted input
		long[] values = ArrayUtils.fillAscending(new long[10]);

		// Test with random input
		values = new long[] {-1,  4,  2, 600,  0,  2, -5,  8, -999};
		test(heap, values);

		// Test with negative input only
		values = new long[] {-1, -2, -4, -3, -7, -999, -5, -3};
		test(heap, values);
	}

	private static void test(LongMinHeap heap, long[] values) {

		long[] mins = values.clone();
		Arrays.sort(mins);

		// Sanity check for our test inputs
		assertEquals(values.length, mins.length);

		int size = values.length;

		// Assume initially empty heap
		assertTrue(heap.isEmpty());

		// Add all supplied values
		for(int i=0; i<size; i++) {
			heap.push(values[i]);
		}

		// Now remove all of them again
		for(int i=0; i<size; i++) {
			long min = heap.pop();

			if(i==size-1) {
				assertTrue(heap.isEmpty());
			} else {
				assertEquals(mins[i], min);
			}
		}

	}

	@Test
	public void testDoubleMinHeap() throws Exception {

		DoubleMinHeap heap = new DoubleMinHeap(20);

		// Basic test with sorted input
		double[] values = {0.1, 1.2, 2.3, 3.4, 4.5, 5.6, 6.7, 7.8, 8.9, 9.001};

		// Test with random input
		values = new double[] {-1.1,  4.5,  2.3, 600.006,  0.0,  2.3, -5.4,  8.9, -999.111};
		test(heap, values);

		// Test with negative input only
		values = new double[] {-1.1, -2.3, -4.3, -3.4, -7.8, -999.111, -5.4, -3.2};
		test(heap, values);
	}

	private static void test(DoubleMinHeap heap, double[] values) {

		double[] mins = values.clone();
		Arrays.sort(mins);

		// Sanity check for our test inputs
		assertEquals(values.length, mins.length);

		int size = values.length;

		// Assume initially empty heap
		assertTrue(heap.isEmpty());

		// Add all supplied values
		for(int i=0; i<size; i++) {
			heap.push(values[i]);
		}

		// Now remove all of them again
		for(int i=0; i<size; i++) {
			double min = heap.pop();

			if(i==size-1) {
				assertTrue(heap.isEmpty());
			} else {
				assertEquals(mins[i], min, 0.0001);
			}
		}

	}

	@SuppressWarnings("boxing")
	@Test
	public void testObjectMinHeap() throws Exception {

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

		ObjectMinHeap<Integer> heap = new ObjectMinHeap<>(20);

		// Basic test with sorted input
		Integer[] values = ArrayUtils.fillAscending(new Integer[10]);

		// Test with random input
		values = new Integer[] {-1,  4,  2, 600,  0,  2, -5,  8, -999};
		test(heap, values);

		// Test with negative input only
		values = new Integer[] {-1, -2, -4, -3, -7, -999, -5, -3};
		test(heap, values);
	}

	private static void test(ObjectMinHeap<Integer> heap, Integer[] values) {

		Integer[] mins = values.clone();
		Arrays.sort(mins);

		// Sanity check for our test inputs
		assertEquals(values.length, mins.length);

		int size = values.length;

		// Assume initially empty heap
		assertTrue(heap.isEmpty());

		// Add all supplied values
		for(int i=0; i<size; i++) {
			heap.push(values[i]);
		}

		// Now remove all of them again
		for(int i=0; i<size; i++) {
			Integer min = heap.pop();

			if(i==size-1) {
				assertTrue(heap.isEmpty());
			} else {
				assertEquals(mins[i], min);
			}
		}

	}
}
