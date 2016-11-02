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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractIndexSetProcessor {

	protected final List<IndexSet> buffer = new ArrayList<>();

	protected long estimatedResultSize;

	/**
	 * Called whenever a new {@link IndexSet} is added to this processor so that
	 * subclasses can adjust the prediction about required buffer sizes for output
	 * data.
	 *
	 * @param indexSet
	 */
	protected abstract void refreshEstimatedResultSize(IndexSet indexSet);

	/**
	 * Signals whether or not the input for this processor has to be sorted.
	 * Used in all the {@code add} methods to determine if a check is
	 * required.
	 * <p>
	 * The default implementation returns {@code true}
	 *
	 * @return
	 */
	protected boolean isRequiresSortedInput() {
		return true;
	}

	protected void checkNewIndexSet(IndexSet set) {
		if(set.size()==IndexSet.UNKNOWN_SIZE)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Unable to process index set of unknown size");

		if(isRequiresSortedInput()) {
			IndexUtils.checkSorted(set);
		}
	}

	public void add(IndexSet...indices) {
		for(IndexSet set : indices) {
			checkNewIndexSet(set);
			buffer.add(set);
			refreshEstimatedResultSize(set);
		}
	}

	public void add(Collection<? extends IndexSet> indices) {
		for(IndexSet set : indices) {
			checkNewIndexSet(set);
			buffer.add(set);
			refreshEstimatedResultSize(set);
		}
	}

	public long getEstimatedResultSize() {
		return estimatedResultSize;
	}

}
