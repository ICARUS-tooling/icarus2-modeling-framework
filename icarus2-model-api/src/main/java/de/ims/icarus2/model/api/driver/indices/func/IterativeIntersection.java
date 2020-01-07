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
public class IterativeIntersection extends AbstractIndexSetProcessor<IterativeIntersection> {

	public IterativeIntersection() {
		super(true);
		estimatedResultSize = Long.MAX_VALUE;
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
		if(buffer.isEmpty() || estimatedResultSize<=0) {
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
		assert buffer.get(0).size()<=buffer.get(buffer.size()-1).size();

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

		assert set1!=set2;
		assert set1!=consumer;
		assert set2!=consumer;


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
			} else if(v2 < v1) {
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
