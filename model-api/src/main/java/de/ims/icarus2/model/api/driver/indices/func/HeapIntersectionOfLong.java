/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 387 $
 * $Date: 2015-04-22 00:17:30 +0200 (Mi, 22 Apr 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/driver/indices/func/HeapIntersectionOfLong.java $
 *
 * $LastChangedDate: 2015-04-22 00:17:30 +0200 (Mi, 22 Apr 2015) $
 * $LastChangedRevision: 387 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.driver.indices.func;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.util.collections.MinHeap.LongMinHeap;

/**
 *
 *
 * @author Markus Gärtner
 * @version $Id: HeapIntersectionOfLong.java 387 2015-04-21 22:17:30Z mcgaerty $
 *
 */
public class HeapIntersectionOfLong implements PrimitiveIterator.OfLong, ModelConstants {

	public static HeapIntersectionOfLong fromArrays(long[]...arrays) {
		checkNotNull(arrays);
		checkArgument(arrays.length>2);

		OfLong[] sources = new OfLong[arrays.length];

		for(int i=0; i<arrays.length; i++) {
			sources[i] = Arrays.stream(arrays[i]).iterator();
		}

		return new HeapIntersectionOfLong(sources);
	}

	public static HeapIntersectionOfLong fromIndices(IndexSet...indices) {
		checkNotNull(indices);
		checkArgument(indices.length>2);

		OfLong[] sources = new OfLong[indices.length];

		for(int i=0; i<indices.length; i++) {
			sources[i] = indices[i].iterator();
		}

		return new HeapIntersectionOfLong(sources);
	}

	public static HeapIntersectionOfLong fromIndices(Collection<IndexSet> indices) {
		checkNotNull(indices);
		checkArgument(indices.size()>2);

		OfLong[] sources = new OfLong[indices.size()];

		int index = 0;
		for(IndexSet set : indices) {
			sources[index++] = set.iterator();
		}

		return new HeapIntersectionOfLong(sources);
	}

	// Min-heap of the last returned values of each stream
	private final LongMinHeap<PrimitiveIterator.OfLong> heap;
	// Buffer for storing streams while checking for a common value
	private final PrimitiveIterator.OfLong[] buffer;

	private long value = NO_INDEX;

	public HeapIntersectionOfLong(PrimitiveIterator.OfLong[] sources) {
		checkNotNull(sources);

		buffer = new PrimitiveIterator.OfLong[sources.length];
		heap = new LongMinHeap<>(sources.length);

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
		if(value==NO_INDEX && !heap.isEmpty()) {

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

		return value!=NO_INDEX;
	}

	/**
	 * @see java.util.PrimitiveIterator.OfLong#nextLong()
	 */
	@Override
	public long nextLong() {
		if (value==NO_INDEX)
			throw new NoSuchElementException();

		long result = value;
		value = NO_INDEX;
		return result;
	}

}
