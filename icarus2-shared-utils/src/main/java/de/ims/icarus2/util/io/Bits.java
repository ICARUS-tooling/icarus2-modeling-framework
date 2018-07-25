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
package de.ims.icarus2.util.io;

/**
 * @author Markus Gärtner
 *
 */
public final class Bits {

	private Bits() {
		// no instantiation
	}

	// DYNAMIC

	public static long readNBytes(byte[] array, int offset, int n) {
		long v = 0L;

		while(n-->0) {
			v = ((v << 8) |  (array[offset++] & 0xff));
		}

		return v;
	}

	public static void writeNBytes(byte[] array, int offset, long value, int n) {
		while(--n>=0) {
			array[offset+n] = (byte) (value & 0xFF);
			value >>= 8;
		}
	}

	// SHORT

	public static short makeShort(byte b0, byte b1) {
		return	(short) (((b1 & 0xff) <<  8) |
	            (b0 & 0xff));
	}

	public static short readShort(byte[] array, int offset) {
		return makeShort(array[offset], array[offset+1]);
	}

	public static void writeShort(byte[] array, int offset, short value) {
		array[offset  ] = (byte) ((value >>>  0) & 0xFF);
		array[offset+1] = (byte) ((value >>>  8) & 0xFF);
	}

	// INTEGER

	public static int makeInt(byte b0, byte b1, byte b2, byte b3) {
		return	(((b3      ) << 24) |
	            ((b2 & 0xff) << 16) |
	            ((b1 & 0xff) <<  8) |
	            ((b0 & 0xff)      ));
	}

	public static int readInt(byte[] array, int offset) {
		return makeInt(array[offset], array[offset+1],
				array[offset+2], array[offset+3]);
	}

	public static void writeInt(byte[] array, int offset, int value) {
		array[offset  ] = (byte) ((value >>>  0) & 0xFF);
		array[offset+1] = (byte) ((value >>>  8) & 0xFF);
		array[offset+2] = (byte) ((value >>> 16) & 0xFF);
		array[offset+3] = (byte) ((value >>> 24) & 0xFF);
	}

	// LONG

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

	public static long readLong(byte[] array, int offset) {
		return makeLong(array[offset], array[offset+1], array[offset+2], array[offset+3],
				array[offset+4], array[offset+5], array[offset+6], array[offset+7]);
	}

	public static void writeLong(byte[] array, int offset, long value) {
		array[offset  ] = (byte) ((value >>>  0) & 0xFF);
		array[offset+1] = (byte) ((value >>>  8) & 0xFF);
		array[offset+2] = (byte) ((value >>> 16) & 0xFF);
		array[offset+3] = (byte) ((value >>> 24) & 0xFF);
		array[offset+4] = (byte) ((value >>> 32) & 0xFF);
		array[offset+5] = (byte) ((value >>> 40) & 0xFF);
		array[offset+6] = (byte) ((value >>> 48) & 0xFF);
		array[offset+7] = (byte) ((value >>> 56) & 0xFF);
	}
}
