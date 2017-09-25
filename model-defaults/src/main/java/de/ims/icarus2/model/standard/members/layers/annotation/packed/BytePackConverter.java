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
package de.ims.icarus2.model.standard.members.layers.annotation.packed;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.mem.ByteAllocator.Cursor;

/**
 * @author Markus Gärtner
 *
 */
public abstract class BytePackConverter {

	private int offset = IcarusUtils.UNSET_INT;

	public abstract ValueType getValueType();

	/**
	 * Returns the number of bytes this pack needs.
	 * If a converter uses individual bits that amount
	 * to less than {@code 1} total byte this method
	 * should return {@code 0}.
	 *
	 * @return
	 */
	public abstract int sizeInBytes();

	/**
	 * Returns the total number of bits required to
	 * represent the data this converter handles.
	 *
	 * @return
	 */
	public int sizeInBits() {
		return sizeInBytes()<<3;
	}

	/**
	 * Returns the byte offset this converter is reading data
	 * from inside a chunk of bytes.
	 *
	 * @return
	 */
	public int getOffset() {
		checkState("Offset not set", offset!=IcarusUtils.UNSET_INT);
		return offset;
	}

	public void setOffsett(int offset) {
		checkArgument("Offset must be 0 or greater", offset>=0);
		this.offset = offset;
	}

	/**
	 * Creates required utility objects that cannot be shared by
	 * multiple converter instances.
	 * @return
	 */
	public Object createContext() {
		return null;
	}

	public Object getValue(long raw) {

	}

	// Read method

	public boolean getBoolean(Cursor cursor) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.BOOLEAN_TYPE_LABEL));
	}

	public int getInteger(Cursor cursor) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.INTEGER_TYPE_LABEL));
	}

	public long getLong(Cursor cursor) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.LONG_TYPE_LABEL));
	}

	public float getFloat(Cursor cursor) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.FLOAT_TYPE_LABEL));
	}

	public double getDouble(Cursor cursor) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.DOUBLE_TYPE_LABEL));
	}

	public Object getValue(Cursor cursor) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.CUSTOM_TYPE_LABEL));
	}

	public String getString(Cursor cursor) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.STRING_TYPE_LABEL));
	}

	// Write methods

	public void setBoolean(Cursor cursor, boolean value) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.BOOLEAN_TYPE_LABEL));
	}

	public void setInteger(Cursor cursor, int value) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.INTEGER_TYPE_LABEL));
	}

	public void setLong(Cursor cursor, long value) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.LONG_TYPE_LABEL));
	}

	public void setFloat(Cursor cursor, float value) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.FLOAT_TYPE_LABEL));
	}

	public void setDouble(Cursor cursor, double value) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.DOUBLE_TYPE_LABEL));
	}

	public void setValue(Cursor cursor, Object value) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.CUSTOM_TYPE_LABEL));
	}

	public void setString(Cursor cursor, String value) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.STRING_TYPE_LABEL));
	}


	public static class BitwiseBooleanConverter extends BytePackConverter {

		private final byte mask;

		/**
		 * The bit slot within a storage byte, allowed
		 * value {@code 0-7}.
		 */
		private final int bit;

		public BitwiseBooleanConverter(int bit) {
			checkArgument("Bit index must be between 0 and 7, inclusively.", bit>=0 && bit<=7);

			this.bit = bit;
			mask = (byte) ((1<<bit) & 0xFF);
		}

		/**
		 * Returns the bit index within a 32-bit integer that this
		 * converter is using.
		 *
		 * @return the bit
		 */
		public int getBit() {
			return bit;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#getValueType()
		 */
		@Override
		public ValueType getValueType() {
			return ValueType.BOOLEAN;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#sizeInBytes()
		 */
		@Override
		public int sizeInBytes() {
			return 0;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#sizeInBits()
		 */
		@Override
		public int sizeInBits() {
			return 1;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#getBoolean(de.ims.icarus2.util.mem.ByteAllocator.Cursor)
		 */
		@Override
		public boolean getBoolean(Cursor cursor) {
			return (cursor.getByte(getOffset()) & mask) == mask;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#setBoolean(de.ims.icarus2.util.mem.ByteAllocator.Cursor, boolean)
		 */
		@Override
		public void setBoolean(Cursor cursor, boolean value) {
			int offset = getOffset();
			byte block = cursor.getByte(offset);

			if(value) {
				block |= mask;
			} else {
				block &= ~mask;
			}

			cursor.setByte(offset, block);
		}
	}

	public static class BooleanConverter extends BytePackConverter {

		private static final byte TRUE = 0b01;
		private static final byte FALSE = 0b00;

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#getValueType()
		 */
		@Override
		public ValueType getValueType() {
			return ValueType.BOOLEAN;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#sizeInBytes()
		 */
		@Override
		public int sizeInBytes() {
			return Byte.BYTES;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#getBoolean(de.ims.icarus2.util.mem.ByteAllocator.Cursor)
		 */
		@Override
		public boolean getBoolean(Cursor cursor) {
			return cursor.getByte(getOffset())==TRUE;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#setBoolean(de.ims.icarus2.util.mem.ByteAllocator.Cursor, boolean)
		 */
		@Override
		public void setBoolean(Cursor cursor, boolean value) {
			byte flag = value ? TRUE : FALSE;
			cursor.setByte(getOffset(), flag);
		}
	}

	public static class IntConverter extends BytePackConverter {

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#getValueType()
		 */
		@Override
		public ValueType getValueType() {
			return ValueType.INTEGER;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#sizeInBytes()
		 */
		@Override
		public int sizeInBytes() {
			return Integer.BYTES;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#getInteger(de.ims.icarus2.util.mem.ByteAllocator.Cursor)
		 */
		@Override
		public int getInteger(Cursor cursor) {
			return cursor.getInt(getOffset());
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#setInteger(de.ims.icarus2.util.mem.ByteAllocator.Cursor, int)
		 */
		@Override
		public void setInteger(Cursor cursor, int value) {
			cursor.setInt(getOffset(), value);
		}
	}

	public static class LongConverter extends BytePackConverter {

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#getValueType()
		 */
		@Override
		public ValueType getValueType() {
			return ValueType.LONG;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#sizeInBytes()
		 */
		@Override
		public int sizeInBytes() {
			return Long.BYTES;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#getLong(de.ims.icarus2.util.mem.ByteAllocator.Cursor)
		 */
		@Override
		public long getLong(Cursor cursor) {
			return cursor.getLong(getOffset());
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#setLong(de.ims.icarus2.util.mem.ByteAllocator.Cursor, long)
		 */
		@Override
		public void setLong(Cursor cursor, long value) {
			cursor.setLong(getOffset(), value);
		}
	}

	public static class FloatConverter extends BytePackConverter {

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#getValueType()
		 */
		@Override
		public ValueType getValueType() {
			return ValueType.FLOAT;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#sizeInBytes()
		 */
		@Override
		public int sizeInBytes() {
			return Float.BYTES;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#getFloat(de.ims.icarus2.util.mem.ByteAllocator.Cursor)
		 */
		@Override
		public float getFloat(Cursor cursor) {
			return Float.intBitsToFloat(cursor.getInt(getOffset()));
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#setFloat(de.ims.icarus2.util.mem.ByteAllocator.Cursor, float)
		 */
		@Override
		public void setFloat(Cursor cursor, float value) {
			cursor.setInt(getOffset(), Float.floatToIntBits(value));
		}
	}

	public static class DoubleConverter extends BytePackConverter {

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#getValueType()
		 */
		@Override
		public ValueType getValueType() {
			return ValueType.DOUBLE;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#sizeInBytes()
		 */
		@Override
		public int sizeInBytes() {
			return Double.BYTES;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#getDouble(de.ims.icarus2.util.mem.ByteAllocator.Cursor)
		 */
		@Override
		public double getDouble(Cursor cursor) {
			return Double.longBitsToDouble(cursor.getLong(getOffset()));
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#setDouble(de.ims.icarus2.util.mem.ByteAllocator.Cursor, double)
		 */
		@Override
		public void setDouble(Cursor cursor, double value) {
			cursor.setLong(getOffset(), Double.doubleToLongBits(value));
		}
	}

	public static class SubstitutingConverter<T extends Object> extends BytePackConverter {

		private final IntFunction<T> resubstitution;
		private final ToIntFunction<T> substitution;

		private final ValueType valueType;

		private final int bytes;

		/**
		 * @param valueType the value space for this converter
		 * @param bytes the number of bytes used for storing the substituted value
		 * @param substitution the function to transform actual annotation values
		 * into substituted numerical values.
		 * @param resubstitution the function to reverse the substitution process
		 */
		public SubstitutingConverter(ValueType valueType, int bytes, ToIntFunction<T> substitution,
				IntFunction<T> resubstitution) {
			checkArgument("Byte size must be between 1 and "+Integer.BYTES+", inclusively",
					bytes>0 && bytes<=Integer.BYTES);

			this.valueType = requireNonNull(valueType);
			this.bytes = bytes;
			this.substitution = requireNonNull(substitution);
			this.resubstitution = requireNonNull(resubstitution);
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#getValueType()
		 */
		@Override
		public ValueType getValueType() {
			return valueType;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#sizeInBytes()
		 */
		@Override
		public int sizeInBytes() {
			return bytes;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#getValue(de.ims.icarus2.util.mem.ByteAllocator.Cursor)
		 */
		@Override
		public Object getValue(Cursor cursor) {
			return resubstitution.apply((int)cursor.getNBytes(getOffset(), bytes));
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#setValue(de.ims.icarus2.util.mem.ByteAllocator.Cursor, java.lang.Object)
		 */
		@Override
		public void setValue(Cursor cursor, Object value) {
			@SuppressWarnings("unchecked")
			int index = substitution.applyAsInt((T) value);
			cursor.setNBytes(getOffset(), index, bytes);
		}
	}
}
