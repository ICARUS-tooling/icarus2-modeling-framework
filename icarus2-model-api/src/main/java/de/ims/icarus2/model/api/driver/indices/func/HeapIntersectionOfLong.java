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
package de.ims.icarus2.model.api.driver.indices.func;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.stream.Stream;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.MappedMinHeap.MappedLongMinHeap;

/**
 *
 *
 * @author Markus Gärtner
 *
 */
public class HeapIntersectionOfLong implements PrimitiveIterator.OfLong {

	public static HeapIntersectionOfLong fromArrays(long[]...arrays) {
		requireNonNull(arrays);
		checkArgument(arrays.length>2);

		return new HeapIntersectionOfLong(Stream.of(arrays)
				.map(array -> Arrays.stream(array).iterator())
				.toArray(OfLong[]::new));
	}

	public static HeapIntersectionOfLong fromIndices(IndexSet...indices) {
		requireNonNull(indices);
		checkArgument(indices.length>2);

		return new HeapIntersectionOfLong(Stream.of(indices)
				.map(IndexSet::iterator)
				.toArray(OfLong[]::new));
	}

	public static HeapIntersectionOfLong fromIndices(Collection<IndexSet> indices) {
		requireNonNull(indices);
		checkArgument(indices.size()>2);

		return new HeapIntersectionOfLong(indices.stream()
				.map(IndexSet::iterator)
				.toArray(OfLong[]::new));
	}

	// Min-heap of the last returned values of each stream
	private final MappedLongMinHeap<PrimitiveIterator.OfLong> heap;
	// Buffer for storing streams while checking for a common value
	private final PrimitiveIterator.OfLong[] buffer;

	private long value = IcarusUtils.UNSET_LONG;

	public HeapIntersectionOfLong(PrimitiveIterator.OfLong[] sources) {
		requireNonNull(sources);
		checkArgument(sources.length>2);

		buffer = new PrimitiveIterator.OfLong[sources.length];
		heap = new MappedLongMinHeap<>(sources.length);

		for(int i=0; i<sources.length; i++) {
			OfLong source = sources[i];
			if(source.hasNext()) {
				heap.push(source, source.nextLong());
			}
		}
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {

		// If required prepare next value to be returned
		if(value==IcarusUtils.UNSET_LONG && !heap.isEmpty()) {

			boolean continueSearch = true;

			while(continueSearch) {
				// Read and remember current min value
				long val = heap.peekValue();
				buffer[0] = heap.pop();

				// Check whether all sources share that min value
				int idx = 1;
				while(!heap.isEmpty() && heap.peekValue()==val) {
					buffer[idx++] = heap.pop();
				}

				// All sources share same value -> save it and mark end of search
				if(idx==buffer.length) {
					value = val;
					continueSearch = false;
				}

				// Fill sources back into heap
				for(int i=0; i<idx; i++) {
					OfLong source = buffer[i];
					if(source.hasNext()) {
						heap.push(source, source.nextLong());
					} else {
						/*
						 *  As soon as a source runs out of values, break entire operation:
						 *
						 *  Clearing the heap entirely will cause the outer check to always fall
						 *  and therefore offers a cheap way out.
						 */
						heap.clear();
						continueSearch = false;
					}
				}
			}
		}

		return value!=IcarusUtils.UNSET_LONG;
	}

	/**
	 * @see java.util.PrimitiveIterator.OfLong#nextLong()
	 */
	@Override
	public long nextLong() {
		if (value==IcarusUtils.UNSET_LONG)
			throw new NoSuchElementException();

		long result = value;
		value = IcarusUtils.UNSET_LONG;
		return result;
	}

}
