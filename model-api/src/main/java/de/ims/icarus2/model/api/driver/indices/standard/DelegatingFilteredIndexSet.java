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
package de.ims.icarus2.model.api.driver.indices.standard;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;

/**
 * Models a window to another index set (the {@code source}). The content of this index set
 * represents a continuous slice of the {@code source}, defined by a {@link #getBeginIndex() begin}
 * and {@link #getEndIndex() end} index. Both of those window boundaries can be changed at any time,
 * making this a very flexible way of looking at sub-sections of another index set.
 *
 * @author Markus Gärtner
 *
 */
public class DelegatingFilteredIndexSet implements IndexSet {

	private IndexSet source;
	private int[] filter;

	public DelegatingFilteredIndexSet(IndexSet source) {
		setSource(source);
	}

	public DelegatingFilteredIndexSet(IndexSet source, int[] filter) {
		setSource(source);
		setFilter(filter);
	}

	public IndexSet getSource() {
		return source;
	}

	public int[] getFilter() {
		return filter;
	}

	public void setFilter(int[] filter) {
		this.filter = filter;
	}

	public void setSource(IndexSet source) {
		requireNonNull(source);

		this.source = source;
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
		return filter==null ? source.size() : filter.length;
	}

	private int translateIndex(int index) {
		return filter==null ? index : filter[index];
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#indexAt(int)
	 */
	@Override
	public long indexAt(int index) {
		checkIndex(index);
		return source.indexAt(translateIndex(index));
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#firstIndex()
	 */
	@Override
	public long firstIndex() {
		return source.indexAt(translateIndex(0));
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#lastIndex()
	 */
	@Override
	public long lastIndex() {
		return source.indexAt(translateIndex(size()-1));
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

		if(filter==null) {
			return source.subSet(fromIndex, toIndex);
		} else {
			int[] subFilter = Arrays.copyOfRange(filter, fromIndex, toIndex+1);
			return new DelegatingFilteredIndexSet(source, subFilter);
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#externalize()
	 */
	@Override
	public IndexSet externalize() {
		if(filter==null) {
			return source.externalize();
		} else {
			return new DelegatingFilteredIndexSet(source, filter.clone());
		}
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
