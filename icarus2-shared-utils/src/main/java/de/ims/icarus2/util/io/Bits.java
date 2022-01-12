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
package de.ims.icarus2.util.io;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

/**
 * Utility class for reading and writing various numerical data types
 * from and to a byte storage in the form of byte arrays.
 * <p>
 * In the future this is also where other common bit and byte related
 * methods are collected.
 *
 * @author Markus Gärtner
 *
 */
public final class Bits {

	private Bits() {
		// no instantiation
	}

	// DYNAMIC SIZE

	public static long readNBytes(byte[] array, int offset, int n) {
		requireNonNull(array);
		checkArgument(n>0 && offset>=0 && offset+n<=array.length);

		long v = 0L;

		while(n-->0) {
			v <<= 8;
			v |= (array[offset++] & 0xff);
		}

		return v;
	}

	public static void writeNBytes(byte[] array, int offset, long value, int n) {
		requireNonNull(array);
		checkArgument(n>0 && offset>=0 && offset+n<=array.length);

		while(--n>=0) {
			array[offset+n] = (byte) (value & 0xFF);
			value >>= 8;
		}
	}

	// SHORT

	/**
	 * Construct a short value by combining the two {@code byte}
	 * arguments as follows:
	 * <table border="1">
	 * <tr><th>Bits</th><th>Argument</th></tr>
	 * <tr><td>0-7</td><td>{@code b0}</td></tr>
	 * <tr><td>8-15</td><td>{@code b1}</td></tr>
	 * </table>
	 *
	 * @param b0 the first (low) 8 bits
	 * @param b1 the second (high) 8 bits
	 * @return the result of combining the two {@code byte} values into a short
	 */
	public static short makeShort(byte b0, byte b1) {
		return	(short) (((b1 & 0xff) <<  8) |
	            (b0 & 0xff));
	}

	/**
	 * Reads bytes from the given {@code array} starting at position
	 * {@code offset} and combines them into a short value by calling
	 * {@link #makeShort(byte, byte)}.
	 * <table border="1">
	 * <tr><th>Index</th><th>Bits</th></tr>
	 * <tr><td>{@code offset}</td><td>0-7</td></tr>
	 * <tr><td>{@code offset+1}</td><td>8-15</td></tr>
	 * </table>
	 *
	 * @param array
	 * @param offset
	 * @return
	 *
	 * @see #makeShort(byte, byte)
	 */
	public static short readShort(byte[] array, int offset) {
		checkArgument(offset>=0 && offset+1<array.length);
		return makeShort(array[offset], array[offset+1]);
	}

	/**
	 * Splits a given short value into {@code byte} chunks and stores
	 * them in the specified {@code array} starting from position {@code offset}.
	 * <table border="1">
	 * <tr><th>Index</th><th>Bits</th></tr>
	 * <tr><td>{@code offset}</td><td>0-7</td></tr>
	 * <tr><td>{@code offset+1}</td><td>8-15</td></tr>
	 * </table>
	 *
	 * @param array
	 * @param offset
	 * @param value
	 */
	public static void writeShort(byte[] array, int offset, short value) {
		checkArgument(offset>=0 && offset+1<array.length);
		array[offset  ] = (byte) ((value >>>  0) & 0xFF);
		array[offset+1] = (byte) ((value >>>  8) & 0xFF);
	}

	// INTEGER

	/**
	 * Construct an integer value by combining the four {@code byte}
	 * arguments as follows:
	 * <table border="1">
	 * <tr><th>Bits</th><th>Argument</th></tr>
	 * <tr><td>0-7</td><td>{@code b0}</td></tr>
	 * <tr><td>8-15</td><td>{@code b1}</td></tr>
	 * <tr><td>16-23</td><td>{@code b2}</td></tr>
	 * <tr><td>24-31</td><td>{@code b3}</td></tr>
	 * </table>
	 *
	 * @param b0
	 * @param b1
	 * @param b2
	 * @param b3
	 * @return
	 */
	public static int makeInt(byte b0, byte b1, byte b2, byte b3) {
		return	(((b3      ) << 24) |
	            ((b2 & 0xff) << 16) |
	            ((b1 & 0xff) <<  8) |
	            ((b0 & 0xff)      ));
	}

	/**
	 * Reads bytes from the given {@code array} starting at position
	 * {@code offset} and combines them into an integer value by calling
	 * {@link #makeInt(byte, byte, byte, byte)}.
	 * <table border="1">
	 * <tr><th>Index</th><th>Bits</th></tr>
	 * <tr><td>{@code offset}</td><td>0-7</td></tr>
	 * <tr><td>{@code offset+1}</td><td>8-15</td></tr>
	 * <tr><td>{@code offset+2}</td><td>16-23</td></tr>
	 * <tr><td>{@code offset+3}</td><td>24-31</td></tr>
	 * </table>
	 *
	 * @param array
	 * @param offset
	 * @return
	 *
	 * @see #makeInt(byte, byte, byte, byte)
	 */
	public static int readInt(byte[] array, int offset) {
		checkArgument(offset>=0 && offset+3<array.length);
		return makeInt(array[offset], array[offset+1],
				array[offset+2], array[offset+3]);
	}

	/**
	 * Splits a given integer value into {@code byte} chunks and stores
	 * them in the specified {@code array} starting from position {@code offset}.
	 * <table border="1">
	 * <tr><th>Index</th><th>Bits</th></tr>
	 * <tr><td>{@code offset}</td><td>0-7</td></tr>
	 * <tr><td>{@code offset+1}</td><td>8-15</td></tr>
	 * <tr><td>{@code offset+2}</td><td>16-23</td></tr>
	 * <tr><td>{@code offset+3}</td><td>24-31</td></tr>
	 * </table>
	 *
	 * @param array
	 * @param offset
	 * @param value
	 */
	public static void writeInt(byte[] array, int offset, int value) {
		checkArgument(offset>=0 && offset+3<array.length);
		array[offset  ] = (byte) ((value >>>  0) & 0xFF);
		array[offset+1] = (byte) ((value >>>  8) & 0xFF);
		array[offset+2] = (byte) ((value >>> 16) & 0xFF);
		array[offset+3] = (byte) ((value >>> 24) & 0xFF);
	}

	// LONG

	/**
	 * Construct a long value by combining the eight {@code byte}
	 * arguments as follows:
	 * <table border="1">
	 * <tr><th>Bits</th><th>Argument</th></tr>
	 * <tr><td>0-7</td><td>{@code b0}</td></tr>
	 * <tr><td>8-15</td><td>{@code b1}</td></tr>
	 * <tr><td>16-23</td><td>{@code b2}</td></tr>
	 * <tr><td>24-31</td><td>{@code b3}</td></tr>
	 * <tr><td>32-39</td><td>{@code b4}</td></tr>
	 * <tr><td>40-47</td><td>{@code b5}</td></tr>
	 * <tr><td>48-55</td><td>{@code b6}</td></tr>
	 * <tr><td>56-63</td><td>{@code b7}</td></tr>
	 * </table>
	 *
	 * @param b0
	 * @param b1
	 * @param b2
	 * @param b3
	 * @param b4
	 * @param b5
	 * @param b6
	 * @param b7
	 * @return
	 */
	public static long makeLong(byte b0, byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7) {
		return ((((long)b7       ) << 56) |
	            (((long)b6 & 0xff) << 48) |
	            (((long)b5 & 0xff) << 40) |
	            (((long)b4 & 0xff) << 32) |
	            (((long)b3 & 0xff) << 24) |
	            (((long)b2 & 0xff) << 16) |
	            (((long)b1 & 0xff) <<  8) |
	            (((long)b0 & 0xff)      ));
	}

	/**
	 * Reads bytes from the given {@code array} starting at position
	 * {@code offset} and combines them into a long value by calling
	 * {@link #makeLong(byte, byte, byte, byte, byte, byte, byte, byte)}.
	 * <table border="1">
	 * <tr><th>Index</th><th>Bits</th></tr>
	 * <tr><td>{@code offset}</td><td>0-7</td></tr>
	 * <tr><td>{@code offset+1}</td><td>8-15</td></tr>
	 * <tr><td>{@code offset+2}</td><td>16-23</td></tr>
	 * <tr><td>{@code offset+3}</td><td>24-31</td></tr>
	 * <tr><td>{@code offset+4}</td><td>32-39</td></tr>
	 * <tr><td>{@code offset+5}</td><td>40-47</td></tr>
	 * <tr><td>{@code offset+6}</td><td>48-55</td></tr>
	 * <tr><td>{@code offset+7}</td><td>56-63</td></tr>
	 * </table>
	 *
	 * @param array
	 * @param offset
	 * @return
	 *
	 * @see #makeLong(byte, byte, byte, byte, byte, byte, byte, byte)
	 */
	public static long readLong(byte[] array, int offset) {
		checkArgument(offset>=0 && offset+7<array.length);
		return makeLong(array[offset], array[offset+1], array[offset+2], array[offset+3],
				array[offset+4], array[offset+5], array[offset+6], array[offset+7]);
	}

	/**
	 * Splits a given long value into {@code byte} chunks and stores
	 * them in the specified {@code array} starting from position {@code offset}.
	 * <table border="1">
	 * <tr><th>Index</th><th>Bits</th></tr>
	 * <tr><td>{@code offset}</td><td>0-7</td></tr>
	 * <tr><td>{@code offset+1}</td><td>8-15</td></tr>
	 * <tr><td>{@code offset+2}</td><td>16-23</td></tr>
	 * <tr><td>{@code offset+3}</td><td>24-31</td></tr>
	 * <tr><td>{@code offset+4}</td><td>32-39</td></tr>
	 * <tr><td>{@code offset+5}</td><td>40-47</td></tr>
	 * <tr><td>{@code offset+6}</td><td>48-55</td></tr>
	 * <tr><td>{@code offset+7}</td><td>56-63</td></tr>
	 * </table>
	 *
	 * @param array
	 * @param offset
	 * @param value
	 */
	public static void writeLong(byte[] array, int offset, long value) {
		checkArgument(offset>=0 && offset+7<array.length);
		array[offset  ] = (byte) ((value >>>  0) & 0xFF);
		array[offset+1] = (byte) ((value >>>  8) & 0xFF);
		array[offset+2] = (byte) ((value >>> 16) & 0xFF);
		array[offset+3] = (byte) ((value >>> 24) & 0xFF);
		array[offset+4] = (byte) ((value >>> 32) & 0xFF);
		array[offset+5] = (byte) ((value >>> 40) & 0xFF);
		array[offset+6] = (byte) ((value >>> 48) & 0xFF);
		array[offset+7] = (byte) ((value >>> 56) & 0xFF);
	}

	// EXTRACTION

	private static final long[] _masks = {
			0xff,
			0xffff,
			0xffffff,
			0xffffffffL,
			0xffffffffffL,
			0xffffffffffffL,
			0xffffffffffffffL,
			0xffffffffffffffffL,
	};

	public static long extractNBytes(long value, int n) {
		checkArgument(n>0 && n<9);
		return (value & _masks[n-1]);
	}

	public static byte extractByte(long value, int n) {
		checkArgument(n>=0 && n<8);
		return (byte)((value>>(n*8)) & 0xff);
	}
}
