/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 */
package de.ims.icarus2.model.api.driver.indices.func;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.util.collections.MappedMinHeap.MappedLongMinHeap;

/**
 * Implements the <i>merge</i> operation for iterators over long index values.
 * The implementation itself is again a long iterator and uses an internal
 * {@link MappedLongMinHeap heap} to store the current minimal values of each input
 * iterator, requiring the inputs (from whatever source) to be in <b>sorted order</b>!
 *
 * @author Markus Gärtner
 *
 */
public class HeapMergeOfLong implements PrimitiveIterator.OfLong {

	public static HeapMergeOfLong fromArrays(long[]...arrays) {
		requireNonNull(arrays);
		checkArgument(arrays.length>2);

		OfLong[] sources = new OfLong[arrays.length];

		for(int i=0; i<arrays.length; i++) {
			sources[i] = Arrays.stream(arrays[i]).iterator();
		}

		return new HeapMergeOfLong(sources);
	}

	public static HeapMergeOfLong fromIndices(IndexSet...indices) {
		requireNonNull(indices);
		checkArgument(indices.length>2);

		OfLong[] sources = new OfLong[indices.length];

		for(int i=0; i<indices.length; i++) {
			sources[i] = indices[i].iterator();
		}

		return new HeapMergeOfLong(sources);
	}

	public static HeapMergeOfLong fromIndices(Collection<IndexSet> indices) {
		requireNonNull(indices);
		checkArgument(indices.size()>2);

		OfLong[] sources = new OfLong[indices.size()];

		int index = 0;
		for(IndexSet set : indices) {
			sources[index++] = set.iterator();
		}

		return new HeapMergeOfLong(sources);
	}

	private final MappedLongMinHeap<PrimitiveIterator.OfLong> heap;

	public HeapMergeOfLong(PrimitiveIterator.OfLong[] sources) {
		requireNonNull(sources);

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
		return !heap.isEmpty();
	}

	/**
	 * @see java.util.PrimitiveIterator.OfLong#nextLong()
	 */
	@Override
	public long nextLong() {
		if (heap.isEmpty())
			throw new NoSuchElementException();

		// "Peek" result value and corresponding source
		long value = heap.peekValue();
		PrimitiveIterator.OfLong source = heap.peekObject();

		// Now remove root
		heap.pop();

		// Add replacement value from used input stream if possible
		if(source.hasNext()) {
			heap.push(source, source.nextLong());
		}

		return value;
	}
}