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
			v <<= 8;
			v |= array[offset++];
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
