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

	/** Min-heap of the last returned values of each stream */
	private final MappedLongMinHeap<Entry> heap;
	/** Buffer for storing streams while checking for a common value */
	private final Entry[] buffer;

	/** Value to be returned on invocation of nextLong() */
	private long value;
	/** Flag to signal whether there's an actual value to be returned */
	private boolean hasValue;

	public HeapIntersectionOfLong(PrimitiveIterator.OfLong[] sources) {
		requireNonNull(sources);
		checkArgument(sources.length>2);

		buffer = new Entry[sources.length];
		heap = new MappedLongMinHeap<>(sources.length);

		// Create the buffer structure, and only keep streams that are not empty
		for(int i=0; i<sources.length; i++) {
			OfLong source = sources[i];
			Entry entry = new Entry(source);
			buffer[i] = entry;
			if(entry.advance()) {
				heap.push(entry, entry.head);
			}
		}
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {

		// If required prepare next value to be returned
		if(!hasValue && !heap.isEmpty()) {

			boolean continueSearch = heap.size()==buffer.length;

			boolean eos = false;

			while(continueSearch) {
				// Read and remember current min value
				long val = heap.peekValue();

				// Check whether all other sources share that min value
				int matchingStreams = 0;

				while(!heap.isEmpty() && heap.peekValue()==val) {
					Entry entry = heap.pop();
					matchingStreams++;

					if(entry.advance()) {
						// Keep pushing new values into heap as long as we're still searching
						heap.push(entry, entry.head);
					} else {
						/*
						 * Stop global search, but still continue checking for the current
						 * value. Mark end-of-stream being reached, so we can cleanup the
						 * heap at end of search.
						 */
						continueSearch = false;
						eos = true;
					}
				}

				// All sources share same value -> save it and mark end of search
				if(matchingStreams==buffer.length) {
					value = val;
					continueSearch = false;
					hasValue = true;
				}
			}

			/*
			 *  If we encountered the end of at least one source stream,
			 *  we can completely stop future checks, as there won't be
			 *  any more common values.
			 */
			if(eos) {
				heap.clear();
			}
		}

		return hasValue;
	}

	/**
	 * @see java.util.PrimitiveIterator.OfLong#nextLong()
	 */
	@Override
	public long nextLong() {
		if (!hasValue)
			throw new NoSuchElementException();

		// Signal hasNext() to search for next value
		hasValue = false;
		return value;
	}

	private static class Entry {
		private final OfLong source;
		private long head;
		private boolean hasHead;

		private Entry(OfLong source) {
			this.source = requireNonNull(source);
		}

		/**
		 * Try to fetch the next value while also eliminating
		 * duplicates.
		 */
		private boolean advance() {
			boolean hadHead = hasHead;
			long previousHead = head;
			hasHead = false;

			while(source.hasNext()) {
				head = source.nextLong();
				if(!hadHead || head!=previousHead) {
					hasHead = true;
					break;
				}
			}

			return hasHead;
		}
	}
}
