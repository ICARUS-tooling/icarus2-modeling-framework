/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.driver.indices.standard;

import static de.ims.icarus2.model.api.driver.indices.IndexUtils.checkIndex;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.checkRangeInclusive;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;

import javax.annotation.Nullable;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.util.collections.ArrayUtils;

/**
 * Models a window to another index set (the {@code source}) that can be discontinuous.
 *
 * @author Markus Gärtner
 *
 */
public class DelegatingFilteredIndexSet implements IndexSet {

	private IndexSet source;
	private int[] filter;
	private boolean filterSorted = false;

	public DelegatingFilteredIndexSet(IndexSet source) {
		setSource(source);
	}

	public DelegatingFilteredIndexSet(IndexSet source, @Nullable int[] filter) {
		setSource(source);
		setFilter(filter);
	}

	public IndexSet getSource() {
		return source;
	}

	//TODO do we really want to allow this?
	public int[] getFilter() {
		return filter;
	}

	public void setFilter(@Nullable int[] filter) {
		this.filter = filter;
		filterSorted = filter==null ? true : ArrayUtils.isSorted(filter, 0, filter.length);
	}

	public void setSource(IndexSet source) {
		requireNonNull(source);

		this.source = source;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#size()
	 */
	@Override
	public int size() {
		return filter==null ? source.size() : filter.length;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return filter==null ? source.isEmpty() : filter.length==0;
	}

	private int translateIndex(int index) {
		return filter==null ? index : filter[index];
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#indexAt(int)
	 */
	@Override
	public long indexAt(int index) {
		checkIndex(this, index);
		return source.indexAt(translateIndex(index));
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#firstIndex()
	 */
	@Override
	public long firstIndex() {
		if(filter==null) {
			return source.firstIndex();
		}
		return filter.length==0 ? UNSET_LONG : source.indexAt(translateIndex(0));
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#lastIndex()
	 */
	@Override
	public long lastIndex() {
		if(filter==null) {
			return source.lastIndex();
		}
		return filter.length==0 ? UNSET_LONG : source.indexAt(translateIndex(size()-1));
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
		checkRangeInclusive(this, fromIndex, toIndex);

		if(filter==null) {
			return source.subSet(fromIndex, toIndex);
		}

		int[] subFilter = Arrays.copyOfRange(filter, fromIndex, toIndex+1);
		return new DelegatingFilteredIndexSet(source, subFilter);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#externalize()
	 */
	@Override
	public IndexSet externalize() {
		if(filter==null) {
			return source.externalize();
		}

		return new DelegatingFilteredIndexSet(source, filter.clone());
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#isSorted()
	 */
	@Override
	public boolean isSorted() {
		return (source.isSorted() && filterSorted) || (filter!=null && filter.length==1);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#sort()
	 */
	@Override
	public boolean sort() {
		// If filter isn't sorted or null, we don't need to bother with sorting at all
		return filterSorted && source.sort();
	}
}
