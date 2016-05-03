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

 * $Revision: 400 $
 * $Date: 2015-05-29 15:06:46 +0200 (Fr, 29 Mai 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.core/source/de/ims/icarus2/util/collections/ArrayUtils.java $
 *
 * $LastChangedDate: 2015-05-29 15:06:46 +0200 (Fr, 29 Mai 2015) $
 * $LastChangedRevision: 400 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.util.collections;

import static de.ims.icarus2.util.Conditions.checkNotNull;

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

/**
 * @author Markus Gärtner
 * @version $Id: ArrayUtils.java 400 2015-05-29 13:06:46Z mcgaerty $
 *
 */
public class ArrayUtils {

	public static <T extends Object> boolean contains(T[] array, T target) {
		return indexOf(array, target)!=-1;
	}

	public static <T extends Object> int indexOf(T[] array, T target) {
		//FIXME change implementation to use Arrays.binarySearch() <-- not possible for unsorted arrays!!!

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
		return -1;
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

	public static void fillAscending(int[] a) {
		for(int i=0; i<a.length; i++)
			a[i] = i;
	}

	public static void fillAscending(Integer[] a) {
		for(int i=0; i<a.length; i++)
			a[i] = i;
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

	public static void reverse(int[] array, int offset, int length) {
		checkNotNull(array);

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

	public static int count(boolean[] a, boolean value) {
		int count = 0;

		for(boolean val : a)
			if(val==value)
				count++;

		return count;
	}

	public static void reverse(Object[] array, int offset, int length) {
		checkNotNull(array);

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

	public static Map<Object, Object> asMap(Object...items) {
		int size = items==null ? 0 : items.length;
		Map<Object, Object> map = new HashMap<>(Math.min(10, size/2));

		if(items!=null) {
			for(int i = 0, len = items.length-1; i<len; i+=2) {
				map.put(items[i], items[i+1]);
			}
		}

		return map;
	}

	public static Map<Object, Object> asLinkedMap(Object...items) {
		int size = items==null ? 0 : items.length;
		Map<Object, Object> map = new LinkedHashMap<>(Math.min(10, size/2));

		if(items!=null) {
			for(int i = 0, len = items.length-1; i<len; i+=2) {
				map.put(items[i], items[i+1]);
			}
		}

		return map;
	}

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
			dest.applyAsInt(destPos, src[srcPos+i]);
		}
	}

	public static void arrayCopy(byte[] src, int srcPos, LongBinaryOperator dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest.applyAsLong(destPos, src[srcPos+i]);
		}
	}

	// ARRAY COPY SHORT

	public static void arrayCopy(short[] src, int srcPos, byte[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = (byte) src[srcPos+i];
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
			dest.applyAsInt(destPos, src[srcPos+i]);
		}
	}

	public static void arrayCopy(short[] src, int srcPos, LongBinaryOperator dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest.applyAsLong(destPos, src[srcPos+i]);
		}
	}

	// ARRAY COPY INT

	public static void arrayCopy(int[] src, int srcPos, byte[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = (byte) src[srcPos+i];
		}
	}

	public static void arrayCopy(int[] src, int srcPos, short[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = (short) src[srcPos+i];
		}
	}

	public static void arrayCopy(int[] src, int srcPos, long[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = src[srcPos+i];
		}
	}

	public static void arrayCopy(int[] src, int srcPos, IntBinaryOperator dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest.applyAsInt(destPos, src[srcPos+i]);
		}
	}

	public static void arrayCopy(int[] src, int srcPos, LongBinaryOperator dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest.applyAsLong(destPos, src[srcPos+i]);
		}
	}

	// ARRAY COPY LONG

	public static void arrayCopy(long[] src, int srcPos, byte[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = (byte) src[srcPos+i];
		}
	}

	public static void arrayCopy(long[] src, int srcPos, short[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = (short) src[srcPos+i];
		}
	}

	public static void arrayCopy(long[] src, int srcPos, int[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = (int) src[srcPos+i];
		}
	}

	public static void arrayCopy(long[] src, int srcPos, LongBinaryOperator dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest.applyAsLong(destPos, src[srcPos+i]);
		}
	}

	// LAMBDA COPY INT

	public static void arrayCopy(IntUnaryOperator src, int srcPos, byte[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = (byte) src.applyAsInt(srcPos+i);
		}
	}

	public static void arrayCopy(IntUnaryOperator src, int srcPos, short[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = (short) src.applyAsInt(srcPos+i);
		}
	}

	public static void arrayCopy(IntUnaryOperator src, int srcPos, long[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = src.applyAsInt(srcPos+i);
		}
	}

	public static void arrayCopy(IntUnaryOperator src, int srcPos, IntBinaryOperator dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest.applyAsInt(destPos, src.applyAsInt(srcPos+i));
		}
	}

	// LAMBDA COPY LONG

	public static void arrayCopy(IntToLongFunction src, int srcPos, byte[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = (byte) src.applyAsLong(srcPos+i);
		}
	}

	public static void arrayCopy(IntToLongFunction src, int srcPos, short[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = (short) src.applyAsLong(srcPos+i);
		}
	}

	public static void arrayCopy(IntToLongFunction src, int srcPos, int[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = (int) src.applyAsLong(srcPos+i);
		}
	}

	public static void arrayCopy(IntToLongFunction src, int srcPos, long[] dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest[destPos+i] = src.applyAsLong(srcPos+i);
		}
	}

	public static void arrayCopy(IntToLongFunction src, int srcPos, LongBinaryOperator dest, int destPos, int length) {
		for(int i=0; i<length; i++) {
			dest.applyAsLong(destPos, src.applyAsLong(srcPos+i));
		}
	}

}
