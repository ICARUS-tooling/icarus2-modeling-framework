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
package de.ims.icarus2.model.standard.view.paged;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl;
import de.ims.icarus2.model.manifest.util.Messages;

/**
 * Implements a buffer that holds a collection of raw {@link IndexSet} instances and
 * transforms them into new sets based on a fixed paging policy. Creation of those
 * {@code pages} is done on demand and the buffer itself does not cache them, nor store any
 * reference to them. So if client code such as a {@link PageControl} implementation wishes
 * caching to be employed then it will have to do it itself.
 *
 * @author Markus Gärtner
 *
 */
public class PageIndexBuffer {

	/**
	 * Max size of a single returned {@link IndexSet}
	 */
	private final int pageSize;

	/**
	 * Number of pages the input indices can be split into.
	 * Calculated at construction time
	 */
	private final int pageCount;

	/**
	 * Total number of index values available
	 */
	private final long size;

	/**
	 * Source collection of {@link IndexSet} instances
	 */
	private final IndexSet[] indices;

	/**
	 * Accumulated size informations for all the given index sets
	 */
	private final long[] offsets;

	public PageIndexBuffer(IndexSet[] indices, int pageSize) {
		requireNonNull(indices);

		if(indices.length==0)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Indices array is empty");
		if(pageSize<=0)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Page size must not be zero or negative: "+pageSize);

		this.indices = indices;
		this.pageSize = pageSize;

		offsets = new long[indices.length];

		long size = 0L;

		for(int i=0; i<indices.length; i++) {
			offsets[i] = size;
			size += indices[i].size();
		}

		this.size = size;

		// Calculate page count without floating point conversion
		long pageCount = size/pageSize;
		if(pageCount*pageSize<size) {
			pageCount++;
		}

		this.pageCount = (int)pageCount;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getPageCount() {
		return pageCount;
	}

	/**
	 * Returns the total number of index values available in this buffer
	 * @return
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Collects all index values for the page at {@code pageIndex} and wraps them
	 * into a freshly created {@link IndexSet};
	 *
	 * @param pageIndex
	 * @return
	 */
	public IndexSet createPage(int pageIndex) {
		if(pageIndex<0 || pageIndex>=pageCount)
			throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					Messages.outOfBoundsMessage(null, pageIndex, 0, pageCount-1));

		// If it's only a single output page just aggregate all the source indices
		if(pageCount==1) {
			return IndexUtils.combine(indices);
		}

		int bufferSize = 0;

		if(pageIndex==pageCount-1) {
			bufferSize = (int) (size % pageSize);
		}
		if(bufferSize==0) {
			bufferSize = pageSize;
		}

		long firstOffset = pageIndex*(long)pageSize;
		long lastOffset = firstOffset+bufferSize;

		// Search for the first raw IndexSet to use
		int firstSetIndex = Arrays.binarySearch(offsets, firstOffset);
		if(firstSetIndex<0) {
			/*
			 * (-(insertion_point) - 1) is returned. Insertion_point is the
			 * first index holding an offset greater than 'firstOffset', so we
			 * need the previous index.
			 */
			firstSetIndex = -(firstSetIndex+1) - 1;
		}

		/*
		 * Search for the last raw IndexSet to use.
		 * Make use of the fact that we already searched for the first index
		 */
		int lastSetIndex = Arrays.binarySearch(offsets, firstSetIndex, offsets.length, lastOffset);
		if(lastSetIndex<0) {
			lastSetIndex = -(lastSetIndex+1) - 1;
		}
		if(lastSetIndex==indices.length) {
			lastSetIndex = indices.length-1;
		}

		IndexValueType valueType = IndexUtils.getDominantType(indices, firstSetIndex, lastSetIndex+1);

		IndexBuffer buffer = new IndexBuffer(valueType, bufferSize);

		if(firstSetIndex==lastSetIndex) {
			IndexSet set = indices[firstSetIndex];
			buffer.add(set,
					(int)(firstOffset-offsets[firstSetIndex]),
					(int)(lastOffset-offsets[firstSetIndex]+1));

		} else {

			// Add part of first set
			IndexSet firstSet = indices[firstSetIndex];
			buffer.add(firstSet, (int)(firstOffset-offsets[firstSetIndex]));

			// Add intermediate sets

			for(int i=firstSetIndex+1; i<lastSetIndex; i++) {
				buffer.add(indices[i]);
			}

			// Add part of last set
			IndexSet lastSet = indices[lastSetIndex];
			buffer.add(lastSet, 0, (int)(lastOffset-offsets[lastSetIndex]+1));
		}

		//TODO maybe add sanity check comparing buffer's size with calculated size of the requested page?

		return buffer;
	}
}
