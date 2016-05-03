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

 * $Revision: 457 $
 * $Date: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/driver/indices/IndexValueType.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.driver.indices;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.IntToLongFunction;
import java.util.function.LongBinaryOperator;

import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.util.collections.ArrayUtils;

/**
 *
 * @author Markus Gärtner
 * @version $Id: IndexValueType.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
public enum IndexValueType {

	BYTE(Byte.TYPE, byte[].class) {
		@Override
		public Object newArray(int bufferSize) {
			return new byte[bufferSize];
		}

		@Override
		public long get(Object array, int index) {
			return ((byte[])array)[index];
		}

		@Override
		public void set(Object array, int index, long value) {
			((byte[])array)[index] = (byte) value;
		}

		@Override
		public Object copyOf(Object array, int srcPos, int length) {
			return Arrays.copyOfRange((byte[])array, srcPos, srcPos+length);
		}

		@Override
		public void copyTo(Object array, int srcPos, byte[] destArray,
				int destPos, int length) {
			System.arraycopy((byte[])array, srcPos, destArray, destPos, length);
		}

		@Override
		public void copyTo(Object array, int srcPos, short[] destArray,
				int destPos, int length) {
			ArrayUtils.arrayCopy((byte[]) array, srcPos, destArray, destPos, length);
		}

		@Override
		public void copyTo(Object array, int srcPos, int[] destArray,
				int destPos, int length) {
			ArrayUtils.arrayCopy((byte[]) array, srcPos, destArray, destPos, length);
		}

		@Override
		public void copyTo(Object array, int srcPos, long[] destArray,
				int destPos, int length) {
			ArrayUtils.arrayCopy((byte[]) array, srcPos, destArray, destPos, length);
		}

		@Override
		public void copyFrom(byte[] srcArray, int srcPos, Object array,
				int destPos, int length) {
			System.arraycopy(srcArray, srcPos, (byte[])array, destPos, length);
		}

		@Override
		public void copyFrom(short[] srcArray, int srcPos, Object array,
				int destPos, int length) {
			ArrayUtils.arrayCopy(srcArray, srcPos, (byte[])array, destPos, length);
		}

		@Override
		public void copyFrom(int[] srcArray, int srcPos, Object array,
				int destPos, int length) {
			ArrayUtils.arrayCopy(srcArray, srcPos, (byte[])array, destPos, length);
		}

		@Override
		public void copyFrom(long[] srcArray, int srcPos, Object array,
				int destPos, int length) {
			ArrayUtils.arrayCopy(srcArray, srcPos, (byte[])array, destPos, length);
		}

		@Override
		public void sort(Object array, int fromIndex, int toIndex) {
			Arrays.sort((byte[])array, fromIndex, toIndex);
		}

		@Override
		public void copyTo(Object array, int srcPos, LongBinaryOperator dest,
				int destPos, int length) {
			ArrayUtils.arrayCopy((byte[])array, srcPos, dest, destPos, length);
		}

		@Override
		public void copyFrom(IntToLongFunction src, int srcPos, Object array,
				int destPos, int length) {
			ArrayUtils.arrayCopy(src, srcPos, (byte[])array, destPos, length);
		}

		@Override
		public int bytesPerValue() {
			return Byte.BYTES;
		}

		@Override
		public int length(Object array) {
			return ((byte[])array).length;
		}

		@Override
		public void fill(Object array, long value, int fromIndex, int toIndex) {
			Arrays.fill((byte[])array, fromIndex, toIndex, (byte) value);
		}

		@Override
		public int binarySearch(Object array, long value, int fromIndex,
				int toIndex) {
			return Arrays.binarySearch((byte[])array, fromIndex, toIndex, (byte) value);
		}

		@Override
		public long get(ByteBuffer buffer) {
			return buffer.get();
		}

		@Override
		public void set(ByteBuffer buffer, long value) {
			buffer.put((byte)value);
		}

		@Override
		public long maxValue() {
			return Byte.MAX_VALUE;
		}
	},
	SHORT(Short.TYPE, short[].class) {
		@Override
		public Object newArray(int bufferSize) {
			return new short[bufferSize];
		}

		@Override
		public long get(Object array, int index) {
			return ((short[])array)[index];
		}

		@Override
		public void set(Object array, int index, long value) {
			((short[])array)[index] = (short) value;
		}

		@Override
		public Object copyOf(Object array, int srcPos, int length) {
			return Arrays.copyOfRange((short[])array, srcPos, srcPos+length);
		}

		@Override
		public void copyTo(Object array, int srcPos, byte[] destArray,
				int destPos, int length) {
			ArrayUtils.arrayCopy((short[]) array, srcPos, destArray, destPos, length);
		}

		@Override
		public void copyTo(Object array, int srcPos, short[] destArray,
				int destPos, int length) {
			System.arraycopy((short[])array, srcPos, destArray, destPos, length);
		}

		@Override
		public void copyTo(Object array, int srcPos, int[] destArray,
				int destPos, int length) {
			ArrayUtils.arrayCopy((short[]) array, srcPos, destArray, destPos, length);
		}

		@Override
		public void copyTo(Object array, int srcPos, long[] destArray,
				int destPos, int length) {
			ArrayUtils.arrayCopy((short[]) array, srcPos, destArray, destPos, length);
		}

		@Override
		public void copyFrom(byte[] srcArray, int srcPos, Object array,
				int destPos, int length) {
			ArrayUtils.arrayCopy(srcArray, srcPos, (short[])array, destPos, length);
		}

		@Override
		public void copyFrom(short[] srcArray, int srcPos, Object array,
				int destPos, int length) {
			System.arraycopy(srcArray, srcPos, (short[])array, destPos, length);
		}

		@Override
		public void copyFrom(int[] srcArray, int srcPos, Object array,
				int destPos, int length) {
			ArrayUtils.arrayCopy(srcArray, srcPos, (short[])array, destPos, length);
		}

		@Override
		public void copyFrom(long[] srcArray, int srcPos, Object array,
				int destPos, int length) {
			ArrayUtils.arrayCopy(srcArray, srcPos, (short[])array, destPos, length);
		}

		@Override
		public void sort(Object array, int fromIndex, int toIndex) {
			Arrays.sort((short[])array, fromIndex, toIndex);
		}

		@Override
		public void copyTo(Object array, int srcPos, LongBinaryOperator dest,
				int destPos, int length) {
			ArrayUtils.arrayCopy((short[])array, srcPos, dest, destPos, length);
		}

		@Override
		public void copyFrom(IntToLongFunction src, int srcPos, Object array,
				int destPos, int length) {
			ArrayUtils.arrayCopy(src, srcPos, (short[])array, destPos, length);
		}

		@Override
		public int bytesPerValue() {
			return Short.BYTES;
		}

		@Override
		public int length(Object array) {
			return ((short[])array).length;
		}

		@Override
		public void fill(Object array, long value, int fromIndex, int toIndex) {
			Arrays.fill((short[])array, fromIndex, toIndex, (short) value);
		}

		@Override
		public int binarySearch(Object array, long value, int fromIndex,
				int toIndex) {
			return Arrays.binarySearch((short[])array, fromIndex, toIndex, (short) value);
		}

		@Override
		public long get(ByteBuffer buffer) {
			return buffer.getShort();
		}

		@Override
		public void set(ByteBuffer buffer, long value) {
			buffer.putShort((short)value);
		}

		@Override
		public long maxValue() {
			return Short.MAX_VALUE;
		}
	},
	INTEGER(Integer.TYPE, int[].class) {

		@Override
		public Object newArray(int bufferSize) {
			return new int[bufferSize];
		}

		@Override
		public long get(Object array, int index) {
			return ((int[])array)[index];
		}

		@Override
		public void set(Object array, int index, long value) {
			((int[])array)[index] = (int) value;
		}

		@Override
		public Object copyOf(Object array, int srcPos, int length) {
			return Arrays.copyOfRange((int[])array, srcPos, srcPos+length);
		}

		@Override
		public void copyTo(Object array, int srcPos, byte[] destArray,
				int destPos, int length) {
			ArrayUtils.arrayCopy((int[]) array, srcPos, destArray, destPos, length);
		}

		@Override
		public void copyTo(Object array, int srcPos, short[] destArray,
				int destPos, int length) {
			ArrayUtils.arrayCopy((int[]) array, srcPos, destArray, destPos, length);
		}

		@Override
		public void copyTo(Object array, int srcPos, int[] destArray,
				int destPos, int length) {
			System.arraycopy((int[])array, srcPos, destArray, destPos, length);
		}

		@Override
		public void copyTo(Object array, int srcPos, long[] destArray,
				int destPos, int length) {
			ArrayUtils.arrayCopy((int[]) array, srcPos, destArray, destPos, length);
		}

		@Override
		public void copyFrom(byte[] srcArray, int srcPos, Object array,
				int destPos, int length) {
			ArrayUtils.arrayCopy(srcArray, srcPos, (int[])array, destPos, length);
		}

		@Override
		public void copyFrom(short[] srcArray, int srcPos, Object array,
				int destPos, int length) {
			ArrayUtils.arrayCopy(srcArray, srcPos, (int[])array, destPos, length);
		}

		@Override
		public void copyFrom(int[] srcArray, int srcPos, Object array,
				int destPos, int length) {
			System.arraycopy(srcArray, srcPos, (int[])array, destPos, length);
		}

		@Override
		public void copyFrom(long[] srcArray, int srcPos, Object array,
				int destPos, int length) {
			ArrayUtils.arrayCopy(srcArray, srcPos, (int[])array, destPos, length);
		}

		@Override
		public void sort(Object array, int fromIndex, int toIndex) {
			Arrays.sort((int[])array, fromIndex, toIndex);
		}

		@Override
		public void copyTo(Object array, int srcPos, LongBinaryOperator dest,
				int destPos, int length) {
			ArrayUtils.arrayCopy((int[])array, srcPos, dest, destPos, length);
		}

		@Override
		public void copyFrom(IntToLongFunction src, int srcPos, Object array,
				int destPos, int length) {
			ArrayUtils.arrayCopy(src, srcPos, (int[])array, destPos, length);
		}

		@Override
		public int bytesPerValue() {
			return Integer.BYTES;
		}

		@Override
		public int length(Object array) {
			return ((int[])array).length;
		}

		@Override
		public void fill(Object array, long value, int fromIndex, int toIndex) {
			Arrays.fill((int[])array, fromIndex, toIndex, (int) value);
		}

		@Override
		public int binarySearch(Object array, long value, int fromIndex,
				int toIndex) {
			return Arrays.binarySearch((int[])array, fromIndex, toIndex, (int) value);
		}

		@Override
		public long get(ByteBuffer buffer) {
			return buffer.getInt();
		}

		@Override
		public void set(ByteBuffer buffer, long value) {
			buffer.putInt((int)value);
		}

		@Override
		public long maxValue() {
			return Integer.MAX_VALUE;
		}
	},
	LONG(Long.TYPE, long[].class) {
		@Override
		public Object newArray(int bufferSize) {
			return new long[bufferSize];
		}

		@Override
		public long get(Object array, int index) {
			return ((long[])array)[index];
		}

		@Override
		public void set(Object array, int index, long value) {
			((long[])array)[index] = value;
		}

		@Override
		public Object copyOf(Object array, int srcPos, int length) {
			return Arrays.copyOfRange((long[])array, srcPos, srcPos+length);
		}

		@Override
		public void copyTo(Object array, int srcPos, byte[] destArray,
				int destPos, int length) {
			ArrayUtils.arrayCopy((long[]) array, srcPos, destArray, destPos, length);
		}

		@Override
		public void copyTo(Object array, int srcPos, short[] destArray,
				int destPos, int length) {
			ArrayUtils.arrayCopy((long[]) array, srcPos, destArray, destPos, length);
		}

		@Override
		public void copyTo(Object array, int srcPos, int[] destArray,
				int destPos, int length) {
			ArrayUtils.arrayCopy((long[]) array, srcPos, destArray, destPos, length);
		}

		@Override
		public void copyTo(Object array, int srcPos, long[] destArray,
				int destPos, int length) {
			System.arraycopy(array, srcPos, destArray, destPos, length);
		}

		@Override
		public void copyFrom(byte[] srcArray, int srcPos, Object array,
				int destPos, int length) {
			ArrayUtils.arrayCopy(srcArray, srcPos, (long[])array, destPos, length);
		}

		@Override
		public void copyFrom(short[] srcArray, int srcPos, Object array,
				int destPos, int length) {
			ArrayUtils.arrayCopy(srcArray, srcPos, (long[])array, destPos, length);
		}

		@Override
		public void copyFrom(int[] srcArray, int srcPos, Object array,
				int destPos, int length) {
			ArrayUtils.arrayCopy(srcArray, srcPos, (long[])array, destPos, length);
		}

		@Override
		public void copyFrom(long[] srcArray, int srcPos, Object array,
				int destPos, int length) {
			System.arraycopy(srcArray, srcPos, (long[])array, destPos, length);
		}

		@Override
		public void sort(Object array, int fromIndex, int toIndex) {
			Arrays.sort((long[])array, fromIndex, toIndex);
		}

		@Override
		public void copyTo(Object array, int srcPos, LongBinaryOperator dest,
				int destPos, int length) {
			ArrayUtils.arrayCopy((long[])array, srcPos, dest, destPos, length);
		}

		@Override
		public void copyFrom(IntToLongFunction src, int srcPos, Object array,
				int destPos, int length) {
			ArrayUtils.arrayCopy(src, srcPos, (long[])array, destPos, length);
		}

		@Override
		public int bytesPerValue() {
			return Long.BYTES;
		}

		@Override
		public int length(Object array) {
			return ((long[])array).length;
		}

		@Override
		public void fill(Object array, long value, int fromIndex, int toIndex) {
			Arrays.fill((long[])array, fromIndex, toIndex, value);
		}

		@Override
		public int binarySearch(Object array, long value, int fromIndex,
				int toIndex) {
			return Arrays.binarySearch((long[])array, fromIndex, toIndex, value);
		}

		@Override
		public long get(ByteBuffer buffer) {
			return buffer.getLong();
		}

		@Override
		public void set(ByteBuffer buffer, long value) {
			buffer.putLong(value);
		}

		@Override
		public long maxValue() {
			return Long.MAX_VALUE;
		}
	},
	;

	private final Class<?> valueClass, arrayClass;

	private IndexValueType(Class<?> valueClass, Class<?> arrayClass) {
		this.valueClass = valueClass;
		this.arrayClass = arrayClass;
	}

	public Class<?> getValueClass() {
		return valueClass;
	}

	public Class<?> getArrayClass() {
		return arrayClass;
	}

	public boolean isValidBuffer(Object buffer) {
		return arrayClass.isAssignableFrom(buffer.getClass());
	}

	public void checkBuffer(Object buffer) {
		if(!isValidBuffer(buffer))
			throw new IllegalArgumentException("Not a valid array type for "+this+": "+buffer.getClass());
	}

	public abstract long maxValue();

	public long checkValue(long value) {
		if(value>=maxValue())
			throw new ModelException(ModelErrorCode.INVALID_INPUT, name()+" - Value exceeds max value: "+value);
		return value;
	}

	public abstract int bytesPerValue();

	/**
	 * Returns {@code true} if the given type's value space is less or equal to the
	 * one specified by this {@code IndexValueType}.
	 *
	 * @param other
	 * @return
	 */
	public boolean isValidSubstitute(IndexValueType other) {
		return other.ordinal()<=ordinal();
	}

	// Instantiation methods
	public abstract Object newArray(int bufferSize);

	// Read/Write methods
	public abstract long get(Object array, int index);
	public abstract void set(Object array, int index, long value);

	public abstract int length(Object array);

	/**
	 * Fills the backing array with the specified {@code value}.
	 *
	 * @param array
	 * @param value
	 * @param fromIndex first index to fill (inclusive)
	 * @param toIndex last index to fill (exclusive)
	 */
	public abstract void fill(Object array, long value, int fromIndex, int toIndex);

	public void fill(Object array, long value) {
		fill(array, value, 0, length(array));
	}

	/**
	 * Runs a binary search on the backing array
	 *
	 * @param array
	 * @param value
	 * @param fromIndex first index to search (inclusive)
	 * @param toIndex last index to search (exclusive)
	 * @return
	 *
	 * @see Arrays#binarySearch(byte[], int, int, byte)
	 */
	public abstract int binarySearch(Object array, long value, int fromIndex, int toIndex);

	/**
	 *
	 * @param array
	 * @param fromIndex first index to sort (inclusive)
	 * @param toIndex last index to sort (exclusive)
	 */
	public abstract void sort(Object array, int fromIndex, int toIndex);

	// ByteBuffer methods

	public abstract long get(ByteBuffer buffer);
	public abstract void set(ByteBuffer buffer, long value);

	// Copy methods
	/**
	 *
	 * @param array source array
	 * @param srcPos begin index of the range to be copied
	 * @param length length of the range to be copied
	 * @return a new array that is a copy of the specified range of argument {@code array}
	 */
	public abstract Object copyOf(Object array, int srcPos, int length);

	/**
	 *
	 * @param array source array to copy data from
	 * @param srcPos position of the first element in the source array to be copied
	 * @param destArray target array to copy data into
	 * @param destPos position in the target array to place the first copied element into
	 * @param length number of elements to be copied
	 */
	public abstract void copyTo(Object array, int srcPos, byte[] destArray, int destPos, int length);

	/**
	 *
	 * @param array source array to copy data from
	 * @param srcPos position of the first element in the source array to be copied
	 * @param destArray target array to copy data into
	 * @param destPos position in the target array to place the first copied element into
	 * @param length number of elements to be copied
	 */
	public abstract void copyTo(Object array, int srcPos, short[] destArray, int destPos, int length);

	/**
	 *
	 * @param array source array to copy data from
	 * @param srcPos position of the first element in the source array to be copied
	 * @param destArray target array to copy data into
	 * @param destPos position in the target array to place the first copied element into
	 * @param length number of elements to be copied
	 */
	public abstract void copyTo(Object array, int srcPos, int[] destArray, int destPos, int length);

	/**
	 *
	 * @param array source array to copy data from
	 * @param srcPos position of the first element in the source array to be copied
	 * @param destArray target array to copy data into
	 * @param destPos position in the target array to place the first copied element into
	 * @param length number of elements to be copied
	 */
	public abstract void copyTo(Object array, int srcPos, long[] destArray, int destPos, int length);

	/**
	 *
	 * @param array source array to copy data from
	 * @param srcPos position of the first element in the source array to be copied
	 * @param dest virtual target array to copy data into (the return value of the {@link LongBinaryOperator#applyAsLong(long, long) set}
	 * method is ignored, the first argument provided will be the index and the second the value to set at that index)
	 * @param destPos position in the target array to place the first copied element into
	 * @param length number of elements to be copied
	 */
	public abstract void copyTo(Object array, int srcPos, LongBinaryOperator dest, int destPos, int length);

	/**
	 *
	 * @param srcArray source array to copy data from
	 * @param srcPos position of the first element in the source array to copy
	 * @param array target array to copy data into
	 * @param destPos position in the target array to place the first copied element into
	 * @param length number of elements to be copied
	 */
	public abstract void copyFrom(byte[] srcArray, int srcPos, Object array, int destPos, int length);

	/**
	 *
	 * @param srcArray source array to copy data from
	 * @param srcPos position of the first element in the source array to copy
	 * @param array target array to copy data into
	 * @param destPos position in the target array to place the first copied element into
	 * @param length number of elements to be copied
	 */
	public abstract void copyFrom(short[] srcArray, int srcPos, Object array, int destPos, int length);

	/**
	 *
	 * @param srcArray source array to copy data from
	 * @param srcPos position of the first element in the source array to copy
	 * @param array target array to copy data into
	 * @param destPos position in the target array to place the first copied element into
	 * @param length number of elements to be copied
	 */
	public abstract void copyFrom(int[] srcArray, int srcPos, Object array, int destPos, int length);

	/**
	 *
	 * @param srcArray source array to copy data from
	 * @param srcPos position of the first element in the source array to copy
	 * @param array target array to copy data into
	 * @param destPos position in the target array to place the first copied element into
	 * @param length number of elements to be copied
	 */
	public abstract void copyFrom(long[] srcArray, int srcPos, Object array, int destPos, int length);

	/**
	 *
	 * @param src source virtual source array
	 * @param srcPos position of the first element in the source array to copy
	 * @param array target array to copy data into
	 * @param destPos position in the target array to place the first copied element into
	 * @param length number of elements to be copied
	 */
	public abstract void copyFrom(IntToLongFunction src, int srcPos, Object array, int destPos, int length);

	public static IndexValueType forValue(long value) {
		if(value==ModelConstants.NO_INDEX) {
			return null;
		}

		if(value<=Byte.MAX_VALUE) {
			return BYTE;
		} else if(value<=Short.MAX_VALUE) {
			return SHORT;
		} else if(value<=Integer.MAX_VALUE) {
			return INTEGER;
		} else {
			return LONG;
		}
	}

	private static final IndexValueType[] _values = values();

	public static IndexValueType forArray(Object array) {
		Class<?> clazz = array.getClass();

		for(IndexValueType type : _values) {
			if(type.arrayClass==clazz) {
				return type;
			}
		}

		throw new IllegalArgumentException("Not a valid index array class: "+clazz);
	}
}
