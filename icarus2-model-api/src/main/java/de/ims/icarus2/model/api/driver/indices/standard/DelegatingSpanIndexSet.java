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
		requireNonNull(source);

		this.source = source;

		resetIndices();
	}

	public void setBeginIndex(int beginIndex) {
		requireNonNull(source);
		checkArgument(beginIndex>=0 && beginIndex<source.size());
		checkArgument(beginIndex<=endIndex);

		this.beginIndex = beginIndex;
	}

	public void setEndIndex(int endIndex) {
		requireNonNull(source);
		checkArgument(endIndex>=0 && endIndex<source.size());
		checkArgument(endIndex>=beginIndex);

		this.endIndex = endIndex;
	}

	public void resetIndices() {
		requireNonNull(source);

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
