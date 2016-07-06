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
 */
package de.ims.icarus2.filedriver.mapping;

import java.nio.ByteBuffer;

import de.ims.icarus2.model.api.driver.indices.IndexValueType;

/**
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

	private IndexBlockStorage(IndexValueType valueType) {
		this.valueType = valueType;
	}

	private final IndexValueType valueType;

	public abstract void read(Object target, ByteBuffer buffer, int offset, int length);

	public abstract void write(Object source, ByteBuffer buffer, int offset, int length);

	public int entrySize() {
		return valueType.bytesPerValue();
	}

	public int spanSize() {
		return valueType.bytesPerValue()<<1;
	}

	public int entryCount(Object buffer) {
		return valueType.length(buffer);
	}

	public int spanCount(Object buffer) {
		return valueType.length(buffer)>>1;
	}

	/**
	 * Create a new buffer array of given size {@code byteCount}.
	 */
	public Object createBuffer(int byteCount) {
		int size = byteCount/valueType.bytesPerValue();
		Object buffer = valueType.newArray(size);
		valueType.fill(buffer, -1, 0, size);

		return buffer;
	}

	/**
	 * Searches the given storage data for a specified {@code value}, delegating
	 * to the underlying {@link IndexValueType value type's}
	 * {@link IndexValueType#binarySearch(Object, long, int, int) search} method.
	 *
	 *
	 * @param source
	 * @param from first index to search (inclusive)
	 * @param to last index to search (exclusive)
	 * @param value
	 * @return
	 */
	public int findSorted(Object source, int from, int to, long value) {
		return Math.max(NOT_FOUND, valueType.binarySearch(source, value, from, to));
	}

	/**
	 * Searches the given storage data for a specified {@code value}
	 *
	 * @param source
	 * @param from first index to search (inclusive)
	 * @param to last index to search (exclusive)
	 * @param value
	 * @return
	 */
	public int find(Object source, int from, int to, long value) {

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
	 *
	 * @param source
	 * @param from first index to check (inclusive)
	 * @param to last index to check (exclusive)
	 * @param value
	 * @return
	 */
	public int findSortedSpan(Object source, int from, int to, long value) {

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
	 * Searches the backing array for a given {@code value}. Unlike the regular
	 * {@link #find(Object, int, int, long) search} method this one assumes that
	 * the array contains span definitions, i.e. its content is arranged as
	 * 2-tupels, each denoting begin and end of a span.
	 *
	 * @param source
	 * @param from first index to check (inclusive)
	 * @param to last index to check (exclusive)
	 * @param value
	 * @return
	 */
	public int findSpan(Object source, int from, int to, long value) {

        for(int i=from; i<to; i++) {
        	int pos = i<<1;
        	if(valueType.get(source, pos)<=value && valueType.get(source, pos+1)>=value) {
        		return i;
        	}
        }

        return NOT_FOUND;
	}

	public long getEntry(Object source, int index) {
		return valueType.get(source, index);
	}

	public long getSpanBegin(Object source, int index) {
		return valueType.get(source, index<<1);
	}

	public long getSpanEnd(Object source, int index) {
		return valueType.get(source, (index<<1)+1);
	}

	public long setEntry(Object source, int index, long value) {
		long current = valueType.get(source, index);
		valueType.set(source, index, value);
		return current;
	}

	public long setSpanBegin(Object source, int index, long value) {
		index = index<<1;
		long current = valueType.get(source, index);
		valueType.set(source, index, value);
		return current;
	}

	public long setSpanEnd(Object source, int index, long value) {
		index = (index<<1)+1;
		long current = valueType.get(source, index);
		valueType.set(source, index, value);
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
