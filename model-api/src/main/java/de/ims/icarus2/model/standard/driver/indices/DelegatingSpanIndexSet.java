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

 * $Revision: 442 $
 * $Date: 2016-01-07 10:59:40 +0100 (Do, 07 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/driver/indices/DelegatingSpanIndexSet.java $
 *
 * $LastChangedDate: 2016-01-07 10:59:40 +0100 (Do, 07 Jan 2016) $
 * $LastChangedRevision: 442 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.driver.indices;

import static de.ims.icarus2.model.util.Conditions.checkArgument;
import static de.ims.icarus2.model.util.Conditions.checkNotNull;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;

/**
 * Models a window to another index set (the {@code source}). The content of this index set
 * represents a continuous slice of the {@code source}, defined by a {@link #getBeginIndex() begin}
 * and {@link #getEndIndex() end} index. Both of those window boundaries can be changed at any time,
 * making this a very flexible way of looking at sub-sections of another index set.
 *
 * @author Markus Gärtner
 * @version $Id: DelegatingSpanIndexSet.java 442 2016-01-07 09:59:40Z mcgaerty $
 *
 */
public class DelegatingSpanIndexSet implements IndexSet {

	private IndexSet source;
	private int beginIndex, endIndex;

	public DelegatingSpanIndexSet(IndexSet source) {
		setSource(source);
	}

	public DelegatingSpanIndexSet(IndexSet source, int beginIndex, int endIndex) {
		setSource(source);
		setBeginIndex(beginIndex);
		setEndIndex(endIndex);
	}

	public IndexSet getSource() {
		return source;
	}

	public int getBeginIndex() {
		return beginIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public void setSource(IndexSet source) {
		checkNotNull(source);

		this.source = source;

		resetIndices();
	}

	public void setBeginIndex(int beginIndex) {
		checkNotNull(source);
		checkArgument(beginIndex>=0 && beginIndex<source.size());
		checkArgument(beginIndex<=endIndex);

		this.beginIndex = beginIndex;
	}

	public void setEndIndex(int endIndex) {
		checkNotNull(source);
		checkArgument(endIndex>=0 && endIndex<source.size());
		checkArgument(endIndex>=beginIndex);

		this.endIndex = endIndex;
	}

	public void resetIndices() {
		checkNotNull(source);

		beginIndex = 0;
		endIndex = source.size()-1;
	}

	protected void checkIndex(int index) {
		if(index<0 || index>=size())
			throw new IndexOutOfBoundsException();
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#size()
	 */
	@Override
	public int size() {
		return endIndex-beginIndex+1;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#indexAt(int)
	 */
	@Override
	public long indexAt(int index) {
		checkIndex(index);
		return source.indexAt(beginIndex+index);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#firstIndex()
	 */
	@Override
	public long firstIndex() {
		return source.indexAt(beginIndex);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#lastIndex()
	 */
	@Override
	public long lastIndex() {
		return source.indexAt(endIndex);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#getIndexValueType()
	 */
	@Override
	public IndexValueType getIndexValueType() {
		return source.getIndexValueType();
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#subSet(int, int)
	 */
	@Override
	public IndexSet subSet(int fromIndex, int toIndex) {
		checkArgument(fromIndex<=toIndex);
		checkIndex(fromIndex);
		checkIndex(toIndex);

		// Translate indices, so we can use the original source index set!
		fromIndex = beginIndex+fromIndex;
		toIndex = beginIndex+toIndex;

		return new DelegatingSpanIndexSet(source, fromIndex, toIndex);
	}

	/**
	 * This implementation delegates to the underlying index set and calls its
	 * {@link IndexSet#subSet(int, int)} method with the current boundary values.
	 * This is so that the returned {@code IndexSet} represents a persistent snapshot
	 * of the underlying set even when the boundary values get changed afterwards.
	 *
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#externalize()
	 */
	@Override
	public IndexSet externalize() {
		return source.subSet(beginIndex, endIndex);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#isSorted()
	 */
	@Override
	public boolean isSorted() {
		return source.isSorted();
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#sort()
	 */
	@Override
	public boolean sort() {
		return source.sort();
	}
}
