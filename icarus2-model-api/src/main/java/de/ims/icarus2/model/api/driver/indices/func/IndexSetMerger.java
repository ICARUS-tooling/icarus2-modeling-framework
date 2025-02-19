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
package de.ims.icarus2.model.api.driver.indices.func;

import java.util.PrimitiveIterator.OfLong;
import java.util.function.LongConsumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer;
import de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory;
import de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.IndexSetBuilder;
import de.ims.icarus2.util.IcarusUtils;

/**
 * This class is not thread-safe!
 *
 * @author Markus Gärtner
 *
 */
public class IndexSetMerger extends AbstractIndexSetProcessor<IndexSetMerger> {

	public IndexSetMerger() {
		super(true);
		estimatedResultSize = 0;
	}

	@Override
	protected void refreshEstimatedResultSize(IndexSet indexSet) {
		estimatedResultSize += indexSet.size();
	}

	/**
	 * Tries to merge all the {@link IndexSet indices} set as input
	 * for this merger into a single output {@link IndexSet}.
	 *
	 * @return
	 *
	 * @throws ModelException of type {@link GlobalErrorCode#ILLEGAL_STATE} in case
	 * the estimated result size exceeds the limit of {@value IcarusUtils#MAX_INTEGER_INDEX}
	 */
	public IndexSet mergeAllToSingle() {
		if(buffer.isEmpty() || estimatedResultSize<=0) {
			return IndexUtils.EMPTY_SET;
		} else if(buffer.size()==1) {
			return buffer.get(0);
		}

		if(estimatedResultSize>IcarusUtils.MAX_INTEGER_INDEX)
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					"Estimated size of merged indices exceeds array limit: "+estimatedResultSize);

		IndexValueType valueType = IndexUtils.getDominantType(buffer);

		// Ensure enough buffer space for the case of disjoint sets
		IndexBuffer result = new IndexBuffer(valueType, (int) estimatedResultSize);

		mergeAll(result);

		assert result.isSorted();

		return result;
	}

	public IndexSet[] mergeAllToArray() {
		if(buffer.isEmpty()) {
			return IndexUtils.EMPTY;
		} else if(buffer.size()==1) {
			return IndexUtils.wrap(buffer.get(0));
		}

		/*
		 *  Short-cut for maximum efficiency.
		 */
		if(estimatedResultSize<=IcarusUtils.MAX_INTEGER_INDEX) {
			return IndexUtils.wrap(mergeAllToSingle());
		}

		/*
		 *  Little bit of cheap optimization here. Picking
		 *  a small value type here can drastically reduce
		 *  memory consumption.
		 */
		IndexValueType valueType = IndexUtils.getDominantType(buffer);

		IndexSetBuilder builder = new IndexCollectorFactory()
				.totalSizeLimit(estimatedResultSize)
				.valueType(valueType)
				.inputSorted(true)
				.create();

		mergeAll(builder);

		return builder.build();
	}

	public void mergeAll(LongConsumer consumer) {

		// Filters merged input stream against duplicates
		LongConsumer filter = filteredConsumer(consumer);

		// Create merged stream of all the sources
		OfLong mergedRawIterator = mergedIterator();

		// Collect all distinct values
		mergedRawIterator.forEachRemaining(filter);

	}

	/**
	 * Creates an iterator that traverses the merged output in
	 * sorted order.
	 * <p>
	 * In case we only need to merge 2 input streams the simple
	 * {@link DualMergeOfLong} is used, otherwise this implementation
	 * delegates to {@link HeapMergeOfLong}.
	 *
	 * @return
	 */
	private OfLong mergedIterator() {
		// Buffer size guaranteed to be 2 or more
		if(buffer.size()==2) {
			OfLong left = buffer.get(0).iterator();
			OfLong right = buffer.get(1).iterator();

			return new DualMergeOfLong(left, right);
		}

		return HeapMergeOfLong.fromIndices(buffer);
	}

	/**
	 * Wraps the given consumer to create a filter against duplicates.
	 * @param consumer
	 * @return
	 */
	private LongConsumer filteredConsumer(LongConsumer consumer) {
		return new DuplicateFilter(consumer);
	}
}
