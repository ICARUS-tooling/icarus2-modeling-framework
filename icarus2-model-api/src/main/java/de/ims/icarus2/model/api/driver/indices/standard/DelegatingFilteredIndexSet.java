/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;

/**
 * Models a window to another index set (the {@code source}) that can be discontinuous.
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
