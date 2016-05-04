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

 * $Revision: 419 $
 * $Date: 2015-07-23 22:36:36 +0200 (Do, 23 Jul 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/driver/indices/func/IndexSetMerger.java $
 *
 * $LastChangedDate: 2015-07-23 22:36:36 +0200 (Do, 23 Jul 2015) $
 * $LastChangedRevision: 419 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.driver.indices.func;

import java.util.Collection;
import java.util.PrimitiveIterator.OfLong;
import java.util.function.LongConsumer;

import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;

/**
 * This class is not thread-safe!
 *
 * @author Markus Gärtner
 * @version $Id: IndexSetMerger.java 419 2015-07-23 20:36:36Z mcgaerty $
 *
 */
public class IndexSetMerger extends AbstractIndexSetProcessor implements ModelConstants {

	public IndexSetMerger() {
		estimatedResultSize = 0;
	}

	public IndexSetMerger(IndexSet...indices) {
		this();
		add(indices);
	}

	public IndexSetMerger(Collection<? extends IndexSet> indices) {
		this();
		add(indices);
	}

	@Override
	protected void refreshEstimatedResultSize(IndexSet indexSet) {
		estimatedResultSize += indexSet.size();
	}

	public IndexSet mergeAllToSingle() {
		if(buffer.isEmpty()) {
			return null;
		} else if(buffer.size()==1) {
			return buffer.get(0);
		}

		if(estimatedResultSize>MAX_INTEGER_INDEX)
			throw new ModelException(ModelErrorCode.ILLEGAL_STATE,
					"Estimated size of merged indices exceeds array limit: "+estimatedResultSize);

		// Create merged stream of all the sources
		OfLong mergedRawIterator = mergedIterator();

		IndexValueType valueType = IndexUtils.getDominantType(buffer);

		// Ensure enough buffer space for the case of disjoint sets
		IndexBuffer result = new IndexBuffer(valueType, (int) estimatedResultSize);

		// Filters merged input stream against duplicates
		LongConsumer filter = filteredConsumer(result);

		// Collect all distinct values
		mergedRawIterator.forEachRemaining(filter);

		return result;
	}

	public IndexSet[] mergeAllToArray() {
		if(buffer.isEmpty()) {
			return null;
		} else if(buffer.size()==1) {
			return IndexUtils.wrap(buffer.get(0));
		}

		if(estimatedResultSize<=MAX_INTEGER_INDEX) {
			return IndexUtils.wrap(mergeAllToSingle());
		}

		// Create merged stream of all the sources
		OfLong mergedRawIterator = mergedIterator();

		IndexValueType valueType = IndexUtils.getDominantType(buffer);

		IndexSetBuilder builder = new IndexCollectorFactory()
				.totalSizeLimit(estimatedResultSize)
				.valueType(valueType)
				.inputSorted(true)
				.create();

		// Filters merged input stream against duplicates
		LongConsumer filter = filteredConsumer(builder);

		// Collect all distinct values
		mergedRawIterator.forEachRemaining(filter);

		return builder.build();
	}

	private OfLong mergedIterator() {
		// Buffer size guaranteed to be 2 or more
		if(buffer.size()==2) {
			OfLong left = buffer.get(0).iterator();
			OfLong right = buffer.get(1).iterator();

			return new DualMergeOfLong(left, right);
		} else {
			return HeapMergeOfLong.fromIndices(buffer);
		}
	}

	private LongConsumer filteredConsumer(LongConsumer consumer) {
		return new DuplicateFilter(consumer);
	}
}
