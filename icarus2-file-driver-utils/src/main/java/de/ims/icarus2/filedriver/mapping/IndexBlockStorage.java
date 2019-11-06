/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.filedriver.mapping;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;

/**
 * Implements storage solutions and protocols for array based storage of index value pairs.
 *
 * @author Markus Gärtner
 *
 */
public enum IndexBlockStorage {

	BYTE(IndexValueType.BYTE) {
		@Override
		public void read(Object target, ByteBuffer buffer, int offset,
				int length) {
			byte[] array = (byte[]) target;
			int index = offset;
			while(length-->0) {
				array[index++] = buffer.get();
			}
		}

		@Override
		public void write(Object source, ByteBuffer buffer, int offset,
				int length) {
			byte[] array = (byte[]) source;
			int index = offset;
			while(length-->0) {
				buffer.put(array[index++]);
			}
		}
	},
	SHORT(IndexValueType.SHORT) {
		@Override
		public void read(Object target, ByteBuffer buffer, int offset,
				int length) {
			short[] array = (short[]) target;
			int index = offset;
			while(length-->0) {
				array[index++] = buffer.getShort();
			}
		}

		@Override
		public void write(Object source, ByteBuffer buffer, int offset,
				int length) {
			short[] array = (short[]) source;
			int index = offset;
			while(length-->0) {
				buffer.putShort(array[index++]);
			}
		}
	},
	INTEGER(IndexValueType.INTEGER) {
		@Override
		public void read(Object target, ByteBuffer buffer, int offset,
				int length) {
			int[] array = (int[]) target;
			int index = offset;
			while(length-->0) {
				array[index++] = buffer.getInt();
			}
		}

		@Override
		public void write(Object source, ByteBuffer buffer, int offset,
				int length) {
			int[] array = (int[]) source;
			int index = offset;
			while(length-->0) {
				buffer.putInt(array[index++]);
			}
		}
	},
	LONG(IndexValueType.LONG) {
		@Override
		public void read(Object target, ByteBuffer buffer, int offset,
				int length) {
			long[] array = (long[]) target;
			int index = offset;
			while(length-->0) {
				array[index++] = buffer.getLong();
			}
		}

		@Override
		public void write(Object source, ByteBuffer buffer, int offset,
				int length) {
			long[] array = (long[]) source;
			int index = offset;
			while(length-->0) {
				buffer.putLong(array[index++]);
			}
		}
	},
	;

	private static final int NOT_FOUND = -1;

	/** The official {@code noEntryValue} to signal unused slots in the storage */
	public static final long UNUSED = 0;

	private IndexBlockStorage(IndexValueType valueType) {
		this.valueType = valueType;
	}

	private final IndexValueType valueType;

	/** Read from buffer into designated target */
	public abstract void read(Object target, ByteBuffer buffer, int offset, int length);

	/** Write from designated source into buffer */
	public abstract void write(Object source, ByteBuffer buffer, int offset, int length);

	/**
	 * @return the valueType
	 */
	public IndexValueType getValueType() {
		return valueType;
	}

	public long checkValue(long value) {
		if(value<0L)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, name()+" - Value is negative: "+value);
		if(value>maxValue())
			throw new ModelException(GlobalErrorCode.VALUE_OVERFLOW, name()+" - Value exceeds max value: "+value);
		return value;
	}

	/**
	 * Returns byte size of a single value entry
	 *
	 * @return
	 */
	public int entrySize() {
		return valueType.bytesPerValue();
	}

	/**
	 * Returns byte size of a single span entry,
	 * which is double the size of a single value entry.
	 *
	 * @return
	 */
	public int spanSize() {
		return valueType.bytesPerValue()<<1;
	}

	/**
	 * Returns number of value entries stored in the given {@code buffer},
	 * which is an array of appropriate component type for this storage's
	 * {@link #getValueType() value type}.
	 *
	 * @param buffer
	 * @return
	 */
	public int entryCount(Object buffer) {
		return valueType.length(buffer);
	}

	/**
	 * Returns number of span entries stored in the given {@code buffer},
	 * which is an array of appropriate component type for this storage's
	 * {@link #getValueType() value type}.
	 *
	 * @param buffer
	 * @return
	 */
	public int spanCount(Object buffer) {
		return valueType.length(buffer)>>1;
	}

	/**
	 * Create a new buffer array of given size {@code byteCount}.
	 */
	public Object createBuffer(int byteCount) {
		int size = byteCount/valueType.bytesPerValue();
		Object buffer = valueType.newArray(size);
//		valueType.fill(buffer, -1, 0, size);

		assert Array.getLength(buffer)==size;

		return buffer;
	}

	public long maxValue() {
		return valueType.maxValue()-1;
	}

	// TRANSLATION

	public long valueToStorage(long value) {
		assert value>=0;
		assert value<=maxValue();
		return value + 1;
	}

	public long storageToValue(long storedValue) {
		assert storedValue>=0;
		assert storedValue<=maxValue()+1;
		return storedValue - 1;
	}

	/**
	 * Searches the given storage data for a specified {@code value}, delegating
	 * to the underlying {@link IndexValueType value type's}
	 * {@link IndexValueType#binarySearch(Object, long, int, int) search} method.
	 * <p>
	 * If the given {@code value} is not found this method will return {@code -1}
	 *
	 * @param source
	 * @param from first index to search (inclusive)
	 * @param to last index to search (exclusive)
	 * @param value
	 * @return
	 */
	public int findSorted(Object source, int from, int to, long value) {
		value = valueToStorage(value);
		return Math.max(NOT_FOUND, valueType.binarySearch(source, value, from, to));
	}

	/**
	 * Searches the given storage data for a specified {@code value}, using a modified
	 * binary search strategy to navigate the partially filled {@code source} array.
	 * <p>
	 * If the given {@code value} is not found this method will return {@code -1}
	 *
	 * @param source
	 * @param from first index to search (inclusive)
	 * @param to last index to search (exclusive)
	 * @param value
	 * @return
	 */
	public int sparseFindSorted(Object source, int from, int to, long value) {

		value = valueToStorage(value);

        int low = from;
        int high = to-1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
			long midVal = valueType.get(source, mid);

			if (midVal==UNUSED) {
				// Unused mid
				long lowVal = valueType.get(source, low);
				if(lowVal==UNUSED || lowVal<value)
					low++;
				else if(lowVal>value)
					return NOT_FOUND;
				else
					return low;
			} else if (midVal>value)
            	// Continue on left area
            	high = mid - 1;
            else if (midVal<value)
            	// Continue on right area
            	low = mid + 1;
            else
                return mid; // span found
        }

        return NOT_FOUND;  // span not found.
	}

	/**
	 * Searches the given storage data for a specified {@code value} and returns
	 * the index location or {@code -1} if the {@code value} couldn't be found.
	 *
	 * @param source
	 * @param from first index to search (inclusive)
	 * @param to last index to search (exclusive)
	 * @param value
	 * @return
	 */
	public int find(Object source, int from, int to, long value) {

		value = valueToStorage(value);

        for(int i=from; i<to; i++) {
        	if(valueType.get(source, i)==value) {
        		return i;
        	}
        }

        return NOT_FOUND;
	}

	/**
	 * Searches the backing array for a given {@code value}. Unlike the regular
	 * {@link #find(Object, int, int, long) search} method this one assumes that
	 * the array contains span definitions, i.e. its content is arranged as
	 * 2-tupels, each denoting begin and end of a span.
	 * <p>
	 * Note that this method requires disjoint spans to be stored in the array
	 * in order to work properly!!
	 *
	 * @param source
	 * @param from first index to check (inclusive)
	 * @param to last index to check (exclusive)
	 * @param value
	 * @return
	 */
	public int findSortedSpan(Object source, int from, int to, long value) {

		value = valueToStorage(value);

        int low = from;
        int high = to-1;

        while (low <= high) {
            int mid = (low + high) >>> 1;

            if (valueType.get(source, mid<<1)>value)
            	// Continue on left area
            	high = mid - 1;
            else if (valueType.get(source, (mid<<1)+1)<value)
            	// Continue on right area
            	low = mid + 1;
            else
                return mid; // span found
        }

        return NOT_FOUND;  // span not found.
	}

	/**
	 * Searches the backing span array for a given {@code value}. This method behaves
	 * similar to {@link #sparseFindSorted(Object, int, int, long)} but also assumes
	 * the array to ontain span definitions.
	 * <p>
	 * Note that this method requires disjoint spans to be stored in the array
	 * in order to work properly!!
	 *
	 * @param source
	 * @param from first index to check (inclusive)
	 * @param to last index to check (exclusive)
	 * @param value
	 * @return
	 *
	 * @see #findSortedSpan(Object, int, int, long)
	 * @see #sparseFindSorted(Object, int, int, long)
	 */
	public int sparseFindSortedSpan(Object source, int from, int to, long value) {

		value = valueToStorage(value);

        int low = from;
        int high = to-1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
        	long spanLow = valueType.get(source, mid<<1);

	        if (spanLow==UNUSED) {
				// Unused span
				long left = valueType.get(source, low<<1);
				long right = valueType.get(source, (low<<1)+1);
				if(left==UNUSED || right<value)
					low++;
				else if(left>value)
					return NOT_FOUND;
				else
					return low;
			} else if (spanLow>value)
            	// Continue on left area
            	high = mid - 1;
            else if (valueType.get(source, (mid<<1)+1)<value)
            	// Continue on right area
            	low = mid + 1;
            else
                return mid; // span found
        }

        return NOT_FOUND;  // span not found.
	}

	/**
	 * Searches the backing array for a given {@code value}. Unlike the regular
	 * {@link #find(Object, int, int, long) search} method this one assumes that
	 * the array contains span definitions, i.e. its content is arranged as
	 * 2-tupels, each denoting begin and end of a span.
	 * <p>
	 * This method naively iterates the spans in the backing array and returns the
	 * first one that actually covers the specified value.
	 *
	 * @param source
	 * @param from first index to check (inclusive)
	 * @param to last index to check (exclusive)
	 * @param value
	 * @return
	 */
	public int findSpan(Object source, int from, int to, long value) {

		value = valueToStorage(value);

        for(int i=from; i<to; i++) {
        	int pos = i<<1;
        	if(valueType.get(source, pos)<=value && valueType.get(source, pos+1)>=value) {
        		return i;
        	}
        }

        return NOT_FOUND;
	}

	// READ

	public long getEntry(Object source, int index) {
		return storageToValue(valueType.get(source, index));
	}

	public long getSpanBegin(Object source, int index) {
		return storageToValue(valueType.get(source, index<<1));
	}

	public long getSpanEnd(Object source, int index) {
		return storageToValue(valueType.get(source, (index<<1)+1));
	}

	// WRITE

	public long setEntry(Object source, int index, long value) {
		long current = storageToValue(valueType.get(source, index));
		valueType.set(source, index, valueToStorage(value));
		return current;
	}

	public long setSpanBegin(Object source, int index, long value) {
		index = index<<1;
		long current = storageToValue(valueType.get(source, index));
		valueType.set(source, index, valueToStorage(value));
		return current;
	}

	public long setSpanEnd(Object source, int index, long value) {
		index = (index<<1)+1;
		long current = storageToValue(valueType.get(source, index));
		valueType.set(source, index, valueToStorage(value));
		return current;
	}

	public static IndexBlockStorage forValueType(IndexValueType valueType) {
		switch (valueType) {
		case BYTE: return BYTE;
		case SHORT: return SHORT;
		case INTEGER: return INTEGER;
		case LONG: return LONG;

		default:
			throw new IllegalStateException();
		}
	}
}
