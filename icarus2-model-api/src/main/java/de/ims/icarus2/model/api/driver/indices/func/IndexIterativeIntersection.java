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

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.function.LongConsumer;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer;


/**
 * Calculates the intersection of a collection of sorted {@link IndexSet}
 * instances.
 * <p>
 * This class is not thread-safe!
 *
 * @author Markus Gärtner
 *
 */
public class IndexIterativeIntersection extends AbstractIndexSetProcessor {

	public IndexIterativeIntersection() {
		super(true);
		estimatedResultSize = Long.MAX_VALUE;
	}

	public IndexIterativeIntersection(IndexSet...indices) {
		this();
		add(indices);
	}

	public IndexIterativeIntersection(Collection<? extends IndexSet> indices) {
		this();
		add(indices);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.func.AbstractIndexSetProcessor#refreshEstimatedResultSize(de.ims.icarus2.model.api.driver.indices.IndexSet)
	 */
	@Override
	protected void refreshEstimatedResultSize(IndexSet indexSet) {
		estimatedResultSize = Math.min(estimatedResultSize, indexSet.size());
	}

	/**
	 * Returns the result of intersecting all the {@link IndexSet}
	 * instances that have been {@link #add(Collection) added} to
	 * this processor so far.
	 * @return
	 */
	public IndexSet intersectAll() {
		if(buffer.isEmpty()) {
			return IndexUtils.EMPTY_SET;
		} else if(buffer.size()==1) {
			return buffer.get(0);
		}

		IndexValueType valueType = IndexUtils.getDominantType(buffer);

		// We hold 2 buffers so we can swap them during iterations once they are used
		// Size is guaranteed to be within integer range due to it being the minimum
		// of all
		int bufferSize = (int) estimatedResultSize;
		IndexBuffer tmp1 = new IndexBuffer(valueType, bufferSize);
		IndexBuffer tmp2 = new IndexBuffer(valueType, bufferSize);

		IndexBuffer activeBuffer = tmp1;
		IndexBuffer result = null;

		/*
		 *  Create sorted array of input sets (in ascending order of size).
		 *  This way chances are higher to approach the exit condition earlier
		 *  if no intersection exists.
		 */
		buffer.sort(IndexSet.INDEX_SET_SIZE_SORTER);

		// Intersect sets iteratively, using the result of previous intersection if possible
		for(int i=1; i<buffer.size(); i++) {

			activeBuffer.clear();

			// As soon as an intersection is reported empty, break up
			if(!intersect(buffer.get(i-1), buffer.get(i), activeBuffer)) {
				return IndexUtils.EMPTY_SET;
			}

			// Store resulting intersection at current position in array
			result = activeBuffer;
			buffer.set(i, activeBuffer);
			// Swap buffer to the one used before
			activeBuffer = (activeBuffer==tmp1) ? tmp2 : tmp1;
		}

		return result;
	}

	/**
	 * Creates the intersection of two {@link IndexSet} instances and saves the result in
	 * the specified {@link LongConsumer consumer}. Returns {@code true} only if there exists a
	 * valid intersection of {@code set1} and {@code set2} (i.e. the {@code consumer} will
	 * hold at least one index value common to both input sets).
	 * <p>
	 * Set to private so we don't have to perform additional checks against unsorted index sets
	 * or those of unknown size.
	 *
	 * @param set1 first set of the intersection
	 * @param set2 second set of the intersection
	 * @param buffer collector to send individuals values that are contained in the intersection
	 * @return {@code true} iff the intersection of the two sets is not empty.
	 */
	private static boolean intersect(IndexSet set1, IndexSet set2, LongConsumer consumer) {
		requireNonNull(set1);
		requireNonNull(set2);
		requireNonNull(consumer);

		long max1 = set1.lastIndex();
		long max2 = set2.lastIndex();

		// Disjoint sets => break directly
		if(set1.firstIndex()>max2 || max1<set2.firstIndex()) {
			return false;
		}

		int i = 0;
		int j = 0;
		int m = set1.size();
		int n = set2.size();

		int count = 0;

		while (i < m && j < n) {
			long v1 = set1.indexAt(i);
			long v2 = set2.indexAt(j);

			// If moved out of max range for one set, stop iteration
			if(v1>max2 || v2>max1) {
				break;
			}

			if(v1 < v2) {
				i++;
			} else if(v2 > v1) {
				j++;
			} else {
				consumer.accept(v1);
				count++;

				i++;
				j++;
			}
		}

		return count>0;
	}
}
