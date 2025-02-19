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
package de.ims.icarus2.model.api.driver.indices.standard;

import static de.ims.icarus2.model.api.driver.indices.IndexUtils.checkIndex;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.function.LongBinaryOperator;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.util.function.LongIntOperator;

/**
 * An {@link IndexSet} solely based on a {@link LongBinaryOperator} that is used
 * to produce the actual index value for a given combination of {@code offset}
 * and {@code index}.
 *
 * @author Markus Gärtner
 *
 */
public class VirtualIndexSet implements IndexSet {

	/**
	 * Offset to be added to index values passed to the
	 * {@link #indexAt(int)} method before forwarding
	 * to the {@link #func} instance.
	 */
	protected final int offset;

	/**
	 * THe index value to start the calculation from
	 */
	protected final long start;

	/**
	 * Upper limit for index values passed to the
	 * {@link #indexAt(int)} method.
	 */
	protected final int size;

	protected final boolean sorted;

	protected final LongIntOperator func;

	protected final IndexValueType valueType;

	public VirtualIndexSet(long start, int size, IndexValueType valueType,
			LongIntOperator func, boolean sorted) {
		this(start, 0, size, valueType, func, sorted);
	}

	protected VirtualIndexSet(long start, int offset,
			int size, IndexValueType valueType,
			LongIntOperator func, boolean sorted) {
		checkArgument("Offset must be >= 0", offset>=0);
		checkArgument("Size must be >=0 || UNKNOWN_SIZE", size==IndexSet.UNKNOWN_SIZE || size>=0);

		this.offset = offset;
		this.start = start;
		this.size = size;
		this.sorted = sorted;
		this.valueType = requireNonNull(valueType);
		this.func = requireNonNull(func);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#size()
	 */
	@Override
	public int size() {
		return size;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#indexAt(int)
	 */
	@Override
	public long indexAt(int index) {
		checkIndex(this, index);
		return valueType.checkValue(func.applyAsLong(start, offset+index));
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#getIndexValueType()
	 */
	@Override
	public IndexValueType getIndexValueType() {
		return valueType;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#isSorted()
	 */
	@Override
	public boolean isSorted() {
		return sorted;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#sort()
	 */
	@Override
	public boolean sort() {
		return sorted;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#subSet(int, int)
	 */
	@Override
	public IndexSet subSet(int fromIndex, int toIndex) {
		checkArgument(fromIndex<=toIndex);
		return new VirtualIndexSet(start, offset+fromIndex, toIndex-fromIndex+1,
				valueType, func, sorted);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#externalize()
	 */
	@Override
	public IndexSet externalize() {
		return this;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return IndexUtils.toString(this);
	}
}
