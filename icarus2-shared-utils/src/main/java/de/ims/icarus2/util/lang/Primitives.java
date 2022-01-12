/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.lang;

import java.util.IdentityHashMap;
import java.util.Map;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;

/**
 * @author Markus Gärtner
 *
 */
public class Primitives {

	private static final Map<Class<?>, Class<?>> primitiveWrapperLookup = new IdentityHashMap<>(9);
	private static final Map<Class<?>, Class<?>> primitiveWrappers = new IdentityHashMap<>(9);

	static {
		primitiveWrapperLookup.put(Boolean.class, boolean.class);
		primitiveWrapperLookup.put(Character.class, char.class);
		primitiveWrapperLookup.put(Byte.class, byte.class);
		primitiveWrapperLookup.put(Short.class, short.class);
		primitiveWrapperLookup.put(Integer.class, int.class);
		primitiveWrapperLookup.put(Long.class, long.class);
		primitiveWrapperLookup.put(Float.class, float.class);
		primitiveWrapperLookup.put(Double.class, double.class);
		primitiveWrapperLookup.put(Void.class, void.class);
	}

	static {
		primitiveWrappers.put(boolean.class, Boolean.class);
		primitiveWrappers.put(char.class, Character.class);
		primitiveWrappers.put(byte.class, Byte.class);
		primitiveWrappers.put(short.class, Short.class);
		primitiveWrappers.put(int.class, Integer.class);
		primitiveWrappers.put(long.class, Long.class);
		primitiveWrappers.put(float.class, Float.class);
		primitiveWrappers.put(double.class, Double.class);
		primitiveWrappers.put(void.class, Void.class);
	}

	/**
	 * Unwraps wrapper types to their primitive type definition.
	 *
	 * @param clazz
	 * @return
	 */
	public static Class<?> unwrap(Class<?> clazz) {
		Class<?> primitiveClass = primitiveWrapperLookup.get(clazz);
		return primitiveClass==null ? clazz : primitiveClass;
	}

	/**
	 * Returns the wrapper type for a given class if it is a primitive type.
	 *
	 * @param clazz
	 * @return
	 */
	public static Class<?> wrap(Class<?> clazz) {
		return clazz.isPrimitive() ? primitiveWrappers.get(clazz) : clazz;
	}

	/**
	 * Returns whether or not the given class is one of the wrapper classes for
	 * primitives like {@link Integer}, etc...
	 *
	 * @param clazz
	 * @return
	 */
	public static <T extends Object> boolean isPrimitiveWrapperClass(Class<T> clazz) {
		return clazz==Long.class || clazz==Integer.class
				|| clazz==Short.class || clazz==Byte.class
				|| clazz==Float.class || clazz==Double.class
				|| clazz==Void.class || clazz==Character.class
				|| clazz==Boolean.class;
	}

	//TODO add documentation for all the cast and boxing methods!

	public static int unbox(Integer value) {
		return value==null ? 0 : value.intValue();
	}

	public static long unbox(Long value) {
		return value==null ? 0L : value.intValue();
	}

	public static double unbox(Double value) {
		return value==null ? 0D : value.doubleValue();
	}

	public static float unbox(Float value) {
		return value==null ? 0F : value.floatValue();
	}

	public static short unbox(Short value) {
		return value==null ? 0 : value.shortValue();
	}

	public static byte unbox(Byte value) {
		return value==null ? 0 : value.byteValue();
	}

	public static boolean unbox(Boolean value) {
		return value==null ? false : value.booleanValue();
	}

	public static char unbox(Character value) {
		return value==null ? 0 : value.charValue();
	}

	public static Integer _int(int value) {
		return Integer.valueOf(value);
	}

	public static Long _long(long value) {
		return Long.valueOf(value);
	}

	public static Double _double(double value) {
		return Double.valueOf(value);
	}

	public static Float _float(float value) {
		return Float.valueOf(value);
	}

	public static Short _short(short value) {
		return Short.valueOf(value);
	}

	public static Byte _byte(byte value) {
		return Byte.valueOf(value);
	}

	public static Boolean _boolean(boolean value) {
		return Boolean.valueOf(value);
	}

	public static Character _char(char value) {
		return Character.valueOf(value);
	}

	private static IcarusRuntimeException forOverflow(String type, long value) {
		return new IcarusRuntimeException(GlobalErrorCode.VALUE_OVERFLOW,
				String.format("Given value is overflowing %s space: %d", type, _long(value)));
	}

	public static byte strictToByte(short v) {
		if(v<Byte.MIN_VALUE || v>Byte.MAX_VALUE)
			throw forOverflow("byte", v);
		return (byte)v;
	}

	public static byte strictToByte(int v) {
		if(v<Byte.MIN_VALUE || v>Byte.MAX_VALUE)
			throw forOverflow("byte", v);
		return (byte)v;
	}

	public static byte strictToByte(long v) {
		if(v<Byte.MIN_VALUE || v>Byte.MAX_VALUE)
			throw forOverflow("byte", v);
		return (byte)v;
	}

	public static short strictToShort(int v) {
		if(v<Short.MIN_VALUE || v>Short.MAX_VALUE)
			throw forOverflow("short", v);
		return (short)v;
	}

	public static short strictToShort(long v) {
		if(v<Short.MIN_VALUE || v>Short.MAX_VALUE)
			throw forOverflow("short", v);
		return (short)v;
	}

	public static char strictToChar(int v) {
		if(v<Character.MIN_VALUE || v>Character.MAX_VALUE)
			throw forOverflow("char", v);
		return (char)v;
	}

	public static char strictToChar(long v) {
		if(v<Character.MIN_VALUE || v>Character.MAX_VALUE)
			throw forOverflow("char", v);
		return (char)v;
	}

	public static int strictToInt(long v) {
		if(v<Integer.MIN_VALUE || v>Integer.MAX_VALUE)
			throw forOverflow("int", v);
		return (int)v;
	}

}
