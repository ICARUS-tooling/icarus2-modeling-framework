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
package de.ims.icarus2.model.api.driver.indices.standard;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Array;
import java.util.PrimitiveIterator.OfInt;
import java.util.PrimitiveIterator.OfLong;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.shorts.ShortList;

/**
 * @author Markus Gärtner
 *
 */
public class ArrayIndexSet implements IndexSet {

	private final Object indices;
	private final IndexValueType valueType;
	private final int fromIndex, toIndex;

	private boolean sorted = false;

	public ArrayIndexSet(Object indices) {
		this(IndexValueType.forArray(indices), indices, 0, -1);
	}

	public ArrayIndexSet(IndexValueType valueType, Object indices) {
		this(valueType, indices, 0, -1);
	}

	public ArrayIndexSet(IndexValueType valueType, Object indices, boolean isSorted) {
		this(valueType, indices, 0, -1, isSorted);
	}

	public ArrayIndexSet(IndexValueType valueType, Object indices, int numIndices) {
		this(valueType, indices, 0, numIndices-1);
	}

	public ArrayIndexSet(IndexValueType valueType, Object indices, int fromIndex, int toIndex) {
		this(valueType, indices, fromIndex, toIndex, false);
	}

	public ArrayIndexSet(IndexValueType valueType, Object indices, int fromIndex, int toIndex, boolean isSorted) {
		requireNonNull(valueType);
		requireNonNull(indices);

		valueType.checkBuffer(indices);
		int size = valueType.length(indices);

		checkArgument("Indices empty", size>0);

		checkArgument(fromIndex>=0);
		checkArgument(toIndex<size);

		if(toIndex<0) {
			toIndex = size-1;
		}

		checkArgument(fromIndex<=toIndex);

		this.valueType = valueType;
		this.indices = indices;
		this.fromIndex = fromIndex;
		this.toIndex = toIndex;
		this.sorted = isSorted;
	}

	@Override
	public IndexValueType getIndexValueType() {
		return valueType;
	}

	@Override
	public boolean isSorted() {
		return sorted;
	}

	@Override
	public boolean sort() {
		if(!sorted) {
			valueType.sort(indices, fromIndex, toIndex);
			sorted = true;
		}
		return true;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#getSize()
	 */
	@Override
	public int size() {
		return toIndex-fromIndex+1;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#indexAt(int)
	 */
	@Override
	public long indexAt(int index) {
		return valueType.get(indices, fromIndex+index);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#firstIndex()
	 */
	@Override
	public long firstIndex() {
		return valueType.get(indices, fromIndex);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#lastIndex()
	 */
	@Override
	public long lastIndex() {
		return valueType.get(indices, toIndex);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#externalize()
	 */
	@Override
	public IndexSet externalize() {
		if(fromIndex==0 && toIndex==Array.getLength(indices)-1) {
			return this;
		}

		Object newIndices = valueType.copyOf(indices, fromIndex, size());

		return new ArrayIndexSet(valueType, newIndices, sorted);
	}

	@Override
	public void export(int beginIndex, int endIndex, byte[] buffer, int offset) {
		valueType.copyTo(indices, beginIndex, buffer, offset, endIndex-beginIndex);
	}

	@Override
	public void export(int beginIndex, int endIndex, short[] buffer, int offset) {
		valueType.copyTo(indices, beginIndex, buffer, offset, endIndex-beginIndex);
	}

	@Override
	public void export(int beginIndex, int endIndex, int[] buffer, int offset) {
		valueType.copyTo(indices, beginIndex, buffer, offset, endIndex-beginIndex);
	}

	@Override
	public void export(int beginIndex, int endIndex, long[] buffer, int offset) {
		valueType.copyTo(indices, beginIndex, buffer, offset, endIndex-beginIndex);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#subSet(int, int)
	 */
	@Override
	public IndexSet subSet(int fromIndex, int toIndex) {
		if(sorted) {
			return new ArrayIndexSet(valueType, indices, this.fromIndex+fromIndex, this.fromIndex+toIndex, true);
		} else {
			return copyOf(this, fromIndex, toIndex);
		}
	}

	public static ArrayIndexSet copyOf(IndexSet set) {
		return copyOf(set, 0, set.size());
	}

	/**
	 * Creates a copy of the specified range within the given target {@code IndexSet}. The copy is backed
	 * by a new array.
	 *
	 * @param set
	 * @param beginIndex first index position in the target index set to copy (inclusive)
	 * @param endIndex last index position in the target index set to copy (inclusive)
	 * @return
	 */
	public static ArrayIndexSet copyOf(IndexSet set, int beginIndex, int endIndex) {
		requireNonNull(set);
		checkArgument(beginIndex>=0);
		checkArgument(beginIndex<=endIndex);
		checkArgument(endIndex<set.size());

		int size = endIndex-beginIndex+1;
		IndexValueType valueType = set.getIndexValueType();
		Object buffer = valueType.newArray(size);

		int pointer = 0;
		for(int i=beginIndex; i<=endIndex; i++) {
			valueType.set(buffer, pointer++, set.indexAt(i));
		}

		return new ArrayIndexSet(valueType, buffer, set.isSorted());
	}

	public static ArrayIndexSet copyOf(ByteList source) {
		requireNonNull(source);

		IndexValueType valueType = IndexValueType.BYTE;
		Object indices = source.toArray();

		return new ArrayIndexSet(valueType, indices, false);
	}

	public static ArrayIndexSet copyOf(ShortList source) {
		requireNonNull(source);

		IndexValueType valueType = IndexValueType.SHORT;
		Object indices = source.toArray();

		return new ArrayIndexSet(valueType, indices, false);
	}

	public static ArrayIndexSet copyOf(IntList source) {
		requireNonNull(source);

		IndexValueType valueType = IndexValueType.INTEGER;
		Object indices = source.toArray();

		return new ArrayIndexSet(valueType, indices, false);
	}

	public static ArrayIndexSet copyOf(LongList source) {
		requireNonNull(source);

		IndexValueType valueType = IndexValueType.LONG;
		Object indices = source.toArray();

		return new ArrayIndexSet(valueType, indices, false);
	}

	public static ArrayIndexSet fromIterator(OfInt source) {
		requireNonNull(source);

		IntList indices = new IntArrayList(1000); //TODO flexible buffer size!!!
		boolean sorted = true;
		int previousValue = -1;
		while(source.hasNext()) {
			int value = source.nextInt();

			if(previousValue!=-1 && value<previousValue) {
				sorted = false;
			}

			indices.add(value);
			previousValue = value;
		}

		return new ArrayIndexSet(IndexValueType.INTEGER, indices.toArray(), sorted);
	}

	public static ArrayIndexSet fromIterator(OfLong source) {
		requireNonNull(source);

		LongList indices = new LongArrayList(1000); //TODO flexible buffer size!!!
		boolean sorted = true;
		long previousValue = -1L;
		while(source.hasNext()) {
			long value = source.nextLong();

			if(previousValue!=-1L && value<previousValue) {
				sorted = false;
			}

			indices.add(value);
			previousValue = value;
		}

		return new ArrayIndexSet(IndexValueType.LONG, indices.toArray(), sorted);
	}
}
