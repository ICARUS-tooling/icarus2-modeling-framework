/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.collections;

import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.IntBinaryOperator;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.LongBinaryOperator;

import javax.annotation.Nullable;

import de.ims.icarus2.util.lang.Primitives;

/**
 * @author Markus Gärtner
 *
 */
public class ArrayUtils {

	@SafeVarargs
	public static <T> T[] array(T...values) {
		return values;
	}

	/**
	 * Returns {@code true} iff both arrays have the same length and for every index
	 * contain the exact same object, determined by reference equality.
	 * @param a
	 * @param b
	 * @return
	 */
	public static <T> boolean containsSame(T[] a, T[] b) {
		if(a.length!=b.length) {
			return false;
		}
		for (int i = 0; i < b.length; i++) {
			if(a[i] != b[i]) {
				return false;
			}
		}
		return true;
	}

	public static <T extends Object> boolean contains(@Nullable T[] array, @Nullable T target) {
		return indexOf(array, target)!=-1;
	}

	public static <T extends Object> int indexOf(@Nullable T[] array, @Nullable T target) {
		if(array!=null) {
			if(target==null) {
				for(int i=0; i<array.length; i++) {
					if(array[i]==null) {
						return i;
					}
				}
			} else {
				for(int i=0; i<array.length; i++) {
					if(target.equals(array[i])) {
						return i;
					}
				}
			}
		}
		return UNSET_INT;
	}

	public static int indexOf(long[] a, long v) {
		for(int i=0; i<a.length; i++) {
			if(a[i]==v)
				return i;
		}
		return UNSET_INT;
	}

	public static int indexOf(int[] a, int v) {
		for(int i=0; i<a.length; i++) {
			if(a[i]==v)
				return i;
		}
		return UNSET_INT;
	}

	public static int min(int...values) {
		/*if(values.length<2)
			throw new IllegalArgumentException();*/

		int min = Integer.MAX_VALUE;

		for(int val : values)
			if(val<min)
				min = val;

		return min;
	}

	public static int max(int...values) {
		/*if(values.length<2)
			throw new IllegalArgumentException();*/

		int max = Integer.MIN_VALUE;

		for(int val : values)
			if(val>max)
				max = val;

		return max;
	}

	public static <T> void swap(T[] array, int index0, int index1) {
		T item0 = array[index0];
		T item1 = array[index1];
		array[index0] = item1;
		array[index1] = item0;
	}

	public static void swap(int[] array, int index0, int index1) {
		int item0 = array[index0];
		int item1 = array[index1];
		array[index0] = item1;
		array[index1] = item0;
	}

	public static void swap(long[] array, int index0, int index1) {
		long item0 = array[index0];
		long item1 = array[index1];
		array[index0] = item1;
		array[index1] = item0;
	}

	public static int[] fillAscending(int[] a) {
		for(int i=0; i<a.length; i++)
			a[i] = i;

		return a;
	}

	public static int[] fillAscending(int[] a, int start) {
		for(int i=0; i<a.length; i++)
			a[i] = start+i;

		return a;
	}

	public static long[] fillAscending(long[] a) {
		for(int i=0; i<a.length; i++)
			a[i] = i;

		return a;
	}

	public static long[] fillAscending(long[] a, long start) {
		for(int i=0; i<a.length; i++)
			a[i] = start+i;

		return a;
	}

	public static Integer[] fillAscending(Integer[] a) {
		for(int i=0; i<a.length; i++)
			a[i] = Integer.valueOf(i);

		return a;
	}

	public static boolean isAscending(int[] a) {
		for(int i=0; i<a.length; i++)
			if(a[i]!=i)
				return false;

		return true;
	}

	public static void permutate(int[] a, int[] permutation) {
		if(a.length<permutation.length)
			throw new IllegalArgumentException();

		int[] tmp = new int[permutation.length];
		for(int i=0; i<permutation.length; i++) {
			if(permutation[i]>=permutation.length)
				throw new IllegalArgumentException();

			tmp[i] = a[permutation[i]];
		}

		System.arraycopy(tmp, 0, a, 0, permutation.length);
	}

	@SuppressWarnings("unchecked")
	public static<T extends Object> void permutate(T[] a, int[] permutation) {
		if(a.length<permutation.length)
			throw new IllegalArgumentException();

	    T[] tmp = (T[])Array.newInstance(
	    		a.getClass().getComponentType(), permutation.length);

		for(int i=0; i<permutation.length; i++) {
			if(permutation[i]>=permutation.length)
				throw new IllegalArgumentException();
			tmp[i] = a[permutation[i]];
		}

		System.arraycopy(tmp, 0, a, 0, permutation.length);
	}

	public static int[] reverse(int[] array) {
		reverse(array, 0, array.length);
		return array;
	}

	public static void reverse(int[] array, int offset, int length) {
		requireNonNull(array);

		if(length==-1)
			length = array.length;

		length = Math.min(length, array.length-offset);

		int tmp, flipIndex;
		int steps = length/2;
		for(int i = 0; i<steps; i++) {
			flipIndex = offset+length-i-1;
			tmp = array[offset+i];
			array[offset+i] = array[flipIndex];
			array[flipIndex] = tmp;
		}
	}

	public static long[] reverse(long[] array) {
		reverse(array, 0, array.length);
		return array;
	}

	public static void reverse(long[] array, int offset, int length) {
		requireNonNull(array);

		if(length==-1)
			length = array.length;

		length = Math.min(length, array.length-offset);

		long tmp;
		int flipIndex;
		int steps = length/2;
		for(int i = 0; i<steps; i++) {
			flipIndex = offset+length-i-1;
			tmp = array[offset+i];
			array[offset+i] = array[flipIndex];
			array[flipIndex] = tmp;
		}
	}

	public static int count(boolean[] a, boolean value) {
		int count = 0;

		for(boolean val : a)
			if(val==value)
				count++;

		return count;
	}

	public static void reverse(Object[] array, int offset, int length) {
		requireNonNull(array);

		if(length==-1)
			length = array.length;

		length = Math.min(length, array.length-offset);

		int flipIndex;
		Object tmp;
		int steps = length/2;
		for(int i = 0; i<steps; i++) {
			flipIndex = offset+length-i-1;
			tmp = array[offset+i];
			array[offset+i] = array[flipIndex];
			array[flipIndex] = tmp;
		}
	}

	public static boolean isUniform(Object[] list) {
		Object prev = null;
		for(Object item : list) {
			if(item==null)
				throw new IllegalArgumentException("Null not supported by uniformity check!"); //$NON-NLS-1$

			if(prev!=null && !prev.equals(item)) {
				return false;
			}

			prev = item;
		}
		return true;
	}

	public static int hashCode(int[] source) {
		int hc = 1;
		for(int i : source) {
			hc *= (i+1);
		}
		return hc;
	}

	@SafeVarargs
	public static <T extends Object> Set<T> asSet(T...items) {
		int size = items==null ? 0 : items.length;
		Set<T> set = new HashSet<>(size);
		CollectionUtils.feedItems(set, items);
		return set;
	}

	@SafeVarargs
	public static <T extends Object> List<T> asList(T...items) {
		int size = items==null ? 0 : items.length;
		List<T> list = new ArrayList<>(size);
		CollectionUtils.feedItems(list, items);
		return list;
	}

	@SafeVarargs
	public static <T extends Object> Stack<T> asStack(T...items) {
		Stack<T> list = new Stack<>();
		CollectionUtils.feedItems(list, items);
		return list;
	}

	@SuppressWarnings("unchecked")
	public static <K extends Object, V extends Object> Map<K, V> asMap(Object...items) {
		int size = items==null ? 0 : items.length;
		Map<Object, Object> map = new HashMap<>(Math.min(10, size/2));

		if(items!=null) {
			for(int i = 0, len = items.length-1; i<len; i+=2) {
				map.put(items[i], items[i+1]);
			}
		}

		return (Map<K, V>) map;
	}

	@SuppressWarnings("unchecked")
	public static <K extends Object, V extends Object> Map<K, V> asLinkedMap(Object...items) {
		int size = items==null ? 0 : items.length;
		Map<Object, Object> map = new LinkedHashMap<>(Math.min(10, size/2));

		if(items!=null) {
			for(int i = 0, len = items.length-1; i<len; i+=2) {
				map.put(items[i], items[i+1]);
			}
		}

		return (Map<K, V>) map;
	}

	// ARRAY SORT CHECKS

	/**
	 * Checks whether a given section of the given array is sorted
	 *
	 * @param a array for which to check a specified section
	 * @param from first index to check (inclusive)
	 * @param to last index to check (exclusive)
	 * @return {@code true} iff the specified section of the array is sorted
	 */
	public static boolean isSorted(int[] a, int from, int to) {
		for(int i=from+1; i<to; i++) {
			if(a[i-1]>a[i]) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks whether a given section of the given array is sorted
	 *
	 * @param a array for which to check a specified section
	 * @param from first index to check (inclusive)
	 * @param to last index to check (exclusive)
	 * @return {@code true} iff the specified section of the array is sorted
	 */
	public static boolean isSorted(long[] a, int from, int to) {
		for(int i=from+1; i<to; i++) {
			if(a[i-1]>a[i]) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks whether a given section of the given array is sorted
	 *
	 * @param a array for which to check a specified section
	 * @param from first index to check (inclusive)
	 * @param to last index to check (exclusive)
	 * @return {@code true} iff the specified section of the array is sorted
	 */
	public static boolean isSorted(short[] a, int from, int to) {
		for(int i=from+1; i<to; i++) {
			if(a[i-1]>a[i]) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks whether a given section of the given array is sorted
	 *
	 * @param a array for which to check a specified section
	 * @param from first index to check (inclusive)
	 * @param to last index to check (exclusive)
	 * @return {@code true} iff the specified section of the array is sorted
	 */
	public static boolean isSorted(byte[] a, int from, int to) {
		for(int i=from+1; i<to; i++) {
			if(a[i-1]>a[i]) {
				return false;
			}
		}

		return true;
	}

	// CONVERSION CHECKS



	// ARRAY COPY BYTE

	public static void arrayCopy(byte[] src, int srcPos, short[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = src[srcPos+i];
		}
	}

	public static void arrayCopy(byte[] src, int srcPos, int[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = src[srcPos+i];
		}
	}

	public static void arrayCopy(byte[] src, int srcPos, long[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = src[srcPos+i];
		}
	}

	public static void arrayCopy(byte[] src, int srcPos, IntBinaryOperator dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest.applyAsInt(destPos+i, src[srcPos+i]);
		}
	}

	public static void arrayCopy(byte[] src, int srcPos, LongBinaryOperator dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest.applyAsLong(destPos+i, src[srcPos+i]);
		}
	}

	// ARRAY COPY SHORT

	public static void arrayCopy(short[] src, int srcPos, byte[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = Primitives.strictToByte(src[srcPos+i]);
		}
	}

	public static void arrayCopy(short[] src, int srcPos, int[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = src[srcPos+i];
		}
	}

	public static void arrayCopy(short[] src, int srcPos, long[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = src[srcPos+i];
		}
	}

	public static void arrayCopy(short[] src, int srcPos, IntBinaryOperator dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest.applyAsInt(destPos+i, src[srcPos+i]);
		}
	}

	public static void arrayCopy(short[] src, int srcPos, LongBinaryOperator dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest.applyAsLong(destPos+i, src[srcPos+i]);
		}
	}

	// ARRAY COPY INT

	public static void arrayCopy(int[] src, int srcPos, byte[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = Primitives.strictToByte(src[srcPos+i]);
		}
	}

	public static void arrayCopy(int[] src, int srcPos, short[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = Primitives.strictToShort(src[srcPos+i]);
		}
	}

	public static void arrayCopy(int[] src, int srcPos, long[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = src[srcPos+i];
		}
	}

	public static void arrayCopy(int[] src, int srcPos, IntBinaryOperator dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest.applyAsInt(destPos+i, src[srcPos+i]);
		}
	}

	public static void arrayCopy(int[] src, int srcPos, LongBinaryOperator dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest.applyAsLong(destPos+i, src[srcPos+i]);
		}
	}

	// ARRAY COPY LONG

	public static void arrayCopy(long[] src, int srcPos, byte[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = Primitives.strictToByte(src[srcPos+i]);
		}
	}

	public static void arrayCopy(long[] src, int srcPos, short[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = Primitives.strictToShort(src[srcPos+i]);
		}
	}

	public static void arrayCopy(long[] src, int srcPos, int[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = Primitives.strictToInt(src[srcPos+i]);
		}
	}

	public static void arrayCopy(long[] src, int srcPos, LongBinaryOperator dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest.applyAsLong(destPos+i, src[srcPos+i]);
		}
	}

	// LAMBDA COPY INT

	public static void arrayCopy(IntUnaryOperator src, int srcPos, byte[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = Primitives.strictToByte(src.applyAsInt(srcPos+i));
		}
	}

	public static void arrayCopy(IntUnaryOperator src, int srcPos, short[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = Primitives.strictToShort(src.applyAsInt(srcPos+i));
		}
	}

	public static void arrayCopy(IntUnaryOperator src, int srcPos, long[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = src.applyAsInt(srcPos+i);
		}
	}

	public static void arrayCopy(IntUnaryOperator src, int srcPos, IntBinaryOperator dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest.applyAsInt(destPos+i, src.applyAsInt(srcPos+i));
		}
	}

	// LAMBDA COPY LONG

	public static void arrayCopy(IntToLongFunction src, int srcPos, byte[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = Primitives.strictToByte(src.applyAsLong(srcPos+i));
		}
	}

	public static void arrayCopy(IntToLongFunction src, int srcPos, short[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = Primitives.strictToShort(src.applyAsLong(srcPos+i));
		}
	}

	public static void arrayCopy(IntToLongFunction src, int srcPos, int[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = Primitives.strictToInt(src.applyAsLong(srcPos+i));
		}
	}

	public static void arrayCopy(IntToLongFunction src, int srcPos, long[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = src.applyAsLong(srcPos+i);
		}
	}

	public static void arrayCopy(IntToLongFunction src, int srcPos, LongBinaryOperator dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest.applyAsLong(destPos+i, src.applyAsLong(srcPos+i));
		}
	}

}
