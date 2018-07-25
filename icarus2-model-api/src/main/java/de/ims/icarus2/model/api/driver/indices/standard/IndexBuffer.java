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

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Array;
import java.util.List;
import java.util.PrimitiveIterator.OfInt;
import java.util.PrimitiveIterator.OfLong;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.IntToLongFunction;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexCollector;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.ArrayUtils;

/**
 * Implements a modifiable {@link IndexSet} with a fixed size buffer that can
 * be filled using various version of the {@link #add(long) add} method. Additionally
 * it implements the specialized {@link LongConsumer} and {@link IntConsumer} interfaces
 * so that it can be filled via a stream or similar data source.
 * <p>
 * Not thread safe!
 *
 * @author Markus Gärtner
 *
 */
public class IndexBuffer implements IndexSet, IndexCollector {

	private final Object buffer;
	private final IndexValueType valueType;
	private int size;

	private boolean sorted = true;

	public IndexBuffer(int bufferSize) {
		this(IndexValueType.LONG, bufferSize);
	}

	public IndexBuffer(IndexValueType valueType, int bufferSize) {
		requireNonNull(valueType);

		if(bufferSize<1)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Buffer size must not be less than 1: "+bufferSize);
		if(bufferSize>IcarusUtils.MAX_INTEGER_INDEX)
			throw new ModelException(GlobalErrorCode.INDEX_OVERFLOW, "Buffer size exceeds allowed limit for integer index values: "+bufferSize);

		this.valueType = valueType;
		buffer = valueType.newArray(bufferSize);
		size = 0;
	}

	@Override
	public IndexValueType getIndexValueType() {
		return valueType;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#size()
	 */
	@Override
	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size==0;
	}

	public void clear() {
		size = 0;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#externalize()
	 */
	@Override
	public IndexSet externalize() {
		return this;
	}

	public IndexSet snapshot() {

		return isEmpty() ? null : subSet(0, size-1);
	}

	public int remaining() {
		return Array.getLength(buffer)-size;
	}

	protected void checkCapacity(int requiredSlots) {
		int capacity = Array.getLength(buffer);
		if(capacity-size < requiredSlots)
			throw new ModelException(GlobalErrorCode.INDEX_OVERFLOW,
					"Unable to fit in "+requiredSlots+" more slots - max size: "+capacity);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#indexAt(int)
	 */
	@Override
	public long indexAt(int index) {
		return valueType.get(buffer, index);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#firstIndex()
	 */
	@Override
	public long firstIndex() {
		return size>0 ? valueType.get(buffer, 0) : IcarusUtils.UNSET_LONG;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#lastIndex()
	 */
	@Override
	public long lastIndex() {
		return size>0 ? valueType.get(buffer, size-1) : IcarusUtils.UNSET_LONG;
	}

	@Override
	public void export(int beginIndex, int endIndex, byte[] buffer, int offset) {
		valueType.copyTo(this.buffer, beginIndex, buffer, offset, endIndex-beginIndex);
	}

	@Override
	public void export(int beginIndex, int endIndex, short[] buffer, int offset) {
		valueType.copyTo(this.buffer, beginIndex, buffer, offset, endIndex-beginIndex);
	}

	@Override
	public void export(int beginIndex, int endIndex, int[] buffer, int offset) {
		valueType.copyTo(this.buffer, beginIndex, buffer, offset, endIndex-beginIndex);
	}

	@Override
	public void export(int beginIndex, int endIndex, long[] buffer, int offset) {
		valueType.copyTo(this.buffer, beginIndex, buffer, offset, endIndex-beginIndex);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#subSet(int, int)
	 */
	@Override
	public IndexSet subSet(int fromIndex, int toIndex) {
		if(fromIndex>=size || toIndex>=size)
			throw new IndexOutOfBoundsException();

		return new ArrayIndexSet(getIndexValueType(), buffer, fromIndex, toIndex, isSorted());
	}

	@Override
	public boolean sort() {
		if(!sorted) {
			valueType.sort(buffer, 0, size);
			sorted = true;
		}
		return true;
	}

	@Override
	public boolean isSorted() {
		return sorted;
	}

	@Override
	public void add(long index) {
		checkCapacity(1);

		sorted = sorted && lastIndex()<=index;
		valueType.set(buffer, size, index);

		size++;
	}

	@Override
	public void add(long from, long to) {
		int length = IcarusUtils.ensureIntegerValueRange(to-from+1);
		checkCapacity(length);

		sorted = sorted && lastIndex()<=from;
		valueType.copyFrom(i -> from+i, 0, buffer, size, length);

		sorted = false;
		size += length;
	}

	public void add(IntSupplier supplier) {
		add(supplier.getAsInt());
	}

	public void add(LongSupplier supplier) {
		add(supplier.getAsLong());
	}

	public void add(byte[] indices) {
		add(indices, 0, indices.length);
	}

	public void add(byte[] indices, int offset, int length) {
		checkCapacity(length);

		if(sorted) {
			sorted = sorted && lastIndex()<=indices[offset] && ArrayUtils.isSorted(indices, offset, offset+length);
		}
		valueType.copyFrom(indices, offset, buffer, size, length);

		size += length;
	}

	public void add(short[] indices) {
		add(indices, 0, indices.length);
	}

	public void add(short[] indices, int offset, int length) {
		checkCapacity(length);

		if(sorted) {
			sorted = sorted && lastIndex()<=indices[offset] && ArrayUtils.isSorted(indices, offset, offset+length);
		}
		valueType.copyFrom(indices, offset, buffer, size, length);

		size += length;
	}

	public void add(int[] indices) {
		add(indices, 0, indices.length);
	}

	public void add(int[] indices, int offset, int length) {
		checkCapacity(length);

		if(sorted) {
			sorted = sorted && lastIndex()<=indices[offset] && ArrayUtils.isSorted(indices, offset, offset+length);
		}
		valueType.copyFrom(indices, offset, buffer, size, length);

		size += length;
	}

	public void add(long[] indices) {
		add(indices, 0, indices.length);
	}

	public void add(long[] indices, int offset, int length) {
		checkCapacity(length);

		if(sorted) {
			sorted = sorted && lastIndex()<=indices[offset] && ArrayUtils.isSorted(indices, offset, offset+length);
		}
		valueType.copyFrom(indices, offset, buffer, size, length);

		size += length;
	}

	@Override
	public void add(IndexSet indices) {
		add(indices, 0, indices.size());
	}

	public void add(IndexSet indices, int beginIndex) {
		add(indices, beginIndex, indices.size());
	}

	/**
     * Adds index values from the specified {@code IndexSet}. The
     * interval begins at the specified {@code beginIndex} and
     * extends to the value at index {@code endIndex - 1}.
     * Thus the length of the added interval is {@code endIndex-beginIndex}.
	 *
	 * @param indices the {@link IndexSet} that acts as source of new index values
	 * @param beginIndex position of the first index value to copy from the source {@code indices} (inclusive)
	 * @param endIndex position of the last index value to copy from the source {@code indices} (exclusive)
	 */
	public void add(IndexSet indices, int beginIndex, int endIndex) {
		int addCount = endIndex-beginIndex;
		checkCapacity(addCount);

		if(sorted) {
			sorted = sorted && lastIndex()<=indices.indexAt(beginIndex) && IndexUtils.isSorted(indices, beginIndex, endIndex);
		}

		switch (valueType) {
		case BYTE:
			indices.export(beginIndex, endIndex, (byte[])buffer, size);
			break;
		case SHORT:
			indices.export(beginIndex, endIndex, (short[])buffer, size);
			break;
		case INTEGER:
			indices.export(beginIndex, endIndex, (int[])buffer, size);
			break;
		case LONG:
			indices.export(beginIndex, endIndex, (long[])buffer, size);
			break;

		default:
			throw new IllegalStateException();
		}

		size += addCount;
	}

	@Override
	public void add(IndexSet[] indices) {
		for(IndexSet set : indices) {
			add(set);
		}
	}

	public void add(Item item) {
		add(item.getIndex());
	}

	public void add(Supplier<? extends Item> supplier) {
		add(supplier.get());
	}

	public void add(Item[] items) {
		add(items, 0, items.length);
	}

	public void add(Item[] items, int offset, int length) {
		checkCapacity(length);
		IntToLongFunction src;

		if(sorted) {
			src = i -> {
				long index = items[i].getIndex();
				sorted = sorted && lastIndex()<=index;
				return index;
			};
		} else {
			src = i -> items[i].getIndex();
		}
		valueType.copyFrom(src, offset, buffer, size, length);

		size += length;
	}

	public void add(List<? extends Item> items) {
		add(items, 0, items.size());
	}

	public void add(List<? extends Item> items, int beginIndex) {
		add(items, beginIndex, items.size());
	}

	public void add(List<? extends Item> items, int beginIndex, int endIndex) {
		int addCount = endIndex-beginIndex;
		checkCapacity(addCount);

		IntToLongFunction src;
		if(sorted) {
			src = i -> {
				long index = items.get(i).getIndex();
				sorted = sorted && lastIndex()<=index;
				return index;
			};
		} else {
			src = i -> items.get(i).getIndex();
		}
		valueType.copyFrom(src, beginIndex, buffer, size, addCount);

		size += addCount;
	}

	@Override
	public void add(OfLong source) {
		source.forEachRemaining(this);
	}

	@Override
	public void add(OfInt source) {
		source.forEachRemaining(this);
	}

	public void add(LongStream stream) {
		stream.forEachOrdered(this);
	}

	public void add(IntStream stream) {
		stream.forEachOrdered(this);
	}

	/**
	 * @see java.util.function.IntConsumer#accept(int)
	 */
	@Override
	public void accept(int value) {
		add(value);
	}

	/**
	 * @see java.util.function.LongConsumer#accept(long)
	 */
	@Override
	public void accept(long value) {
		add(value);
	}
}
