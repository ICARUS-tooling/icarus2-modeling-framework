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
package de.ims.icarus2.model.standard.members.layers.annotation.packed;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

import com.google.common.base.Objects;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.members.layers.annotation.packed.PackedDataManager.PackageHandle;
import de.ims.icarus2.util.mem.ByteAllocator.Cursor;

/**
 * @author Markus Gärtner
 *
 */
public abstract class BytePackConverter {

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
	 * Creates required utility objects that cannot be shared by
	 * multiple converter instances.
	 * @return
	 */
	public Object createContext() {
		return null;
	}

	// Read method

	public boolean getBoolean(PackageHandle handle, Cursor cursor) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.BOOLEAN_TYPE_LABEL));
	}

	public int getInteger(PackageHandle handle, Cursor cursor) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.INTEGER_TYPE_LABEL));
	}

	public long getLong(PackageHandle handle, Cursor cursor) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.LONG_TYPE_LABEL));
	}

	public float getFloat(PackageHandle handle, Cursor cursor) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.FLOAT_TYPE_LABEL));
	}

	public double getDouble(PackageHandle handle, Cursor cursor) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.DOUBLE_TYPE_LABEL));
	}

	public Object getValue(PackageHandle handle, Cursor cursor) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.CUSTOM_TYPE_LABEL));
	}

	public String getString(PackageHandle handle, Cursor cursor) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.STRING_TYPE_LABEL));
	}

	// Write methods

	public void setBoolean(PackageHandle handle, Cursor cursor, boolean value) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.BOOLEAN_TYPE_LABEL));
	}

	public void setInteger(PackageHandle handle, Cursor cursor, int value) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.INTEGER_TYPE_LABEL));
	}

	public void setLong(PackageHandle handle, Cursor cursor, long value) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.LONG_TYPE_LABEL));
	}

	public void setFloat(PackageHandle handle, Cursor cursor, float value) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.FLOAT_TYPE_LABEL));
	}

	public void setDouble(PackageHandle handle, Cursor cursor, double value) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.DOUBLE_TYPE_LABEL));
	}

	public void setValue(PackageHandle handle, Cursor cursor, Object value) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.CUSTOM_TYPE_LABEL));
	}

	public void setString(PackageHandle handle, Cursor cursor, String value) {
		throw new ModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
				Messages.mismatchMessage("Cannot convert", getValueType(), ValueType.STRING_TYPE_LABEL));
	}

	// Utility

	/**
	 * Used to test two annotation values for equality.
	 *
	 * @param v1
	 * @param v2
	 * @return
	 */
	public boolean equal(Object v1, Object v2) {
		return Objects.equal(v1, v2);
	}

	public static final class BitwiseBooleanConverter extends BytePackConverter {

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
		 * Returns the bit index within a single byte that this
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
		public boolean getBoolean(PackageHandle handle, Cursor cursor) {
			return (cursor.getByte(handle.getOffset()) & mask) == mask;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#setBoolean(de.ims.icarus2.util.mem.ByteAllocator.Cursor, boolean)
		 */
		@Override
		public void setBoolean(PackageHandle handle, Cursor cursor, boolean value) {
			int offset = handle.getOffset();
			byte block = cursor.getByte(offset);

			if(value) {
				block |= mask;
			} else {
				block &= ~mask;
			}

			cursor.setByte(offset, block);
		}
	}

	public static final class BooleanConverter extends BytePackConverter {

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
		public boolean getBoolean(PackageHandle handle, Cursor cursor) {
			return cursor.getByte(handle.getOffset())==TRUE;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#setBoolean(de.ims.icarus2.util.mem.ByteAllocator.Cursor, boolean)
		 */
		@Override
		public void setBoolean(PackageHandle handle, Cursor cursor, boolean value) {
			byte flag = value ? TRUE : FALSE;
			cursor.setByte(handle.getOffset(), flag);
		}
	}

	public static final class IntConverter extends BytePackConverter {

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
		public int getInteger(PackageHandle handle, Cursor cursor) {
			return cursor.getInt(handle.getOffset());
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#setInteger(de.ims.icarus2.util.mem.ByteAllocator.Cursor, int)
		 */
		@Override
		public void setInteger(PackageHandle handle, Cursor cursor, int value) {
			cursor.setInt(handle.getOffset(), value);
		}
	}

	public static final class LongConverter extends BytePackConverter {

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
		public long getLong(PackageHandle handle, Cursor cursor) {
			return cursor.getLong(handle.getOffset());
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#setLong(de.ims.icarus2.util.mem.ByteAllocator.Cursor, long)
		 */
		@Override
		public void setLong(PackageHandle handle, Cursor cursor, long value) {
			cursor.setLong(handle.getOffset(), value);
		}
	}

	public static final class FloatConverter extends BytePackConverter {

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
		public float getFloat(PackageHandle handle, Cursor cursor) {
			return Float.intBitsToFloat(cursor.getInt(handle.getOffset()));
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#setFloat(de.ims.icarus2.util.mem.ByteAllocator.Cursor, float)
		 */
		@Override
		public void setFloat(PackageHandle handle, Cursor cursor, float value) {
			cursor.setInt(handle.getOffset(), Float.floatToIntBits(value));
		}
	}

	public static final class DoubleConverter extends BytePackConverter {

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
		public double getDouble(PackageHandle handle, Cursor cursor) {
			return Double.longBitsToDouble(cursor.getLong(handle.getOffset()));
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#setDouble(de.ims.icarus2.util.mem.ByteAllocator.Cursor, double)
		 */
		@Override
		public void setDouble(PackageHandle handle, Cursor cursor, double value) {
			cursor.setLong(handle.getOffset(), Double.doubleToLongBits(value));
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
		public Object getValue(PackageHandle handle, Cursor cursor) {
			return resubstitution.apply((int)cursor.getNBytes(handle.getOffset(), bytes));
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter#setValue(de.ims.icarus2.util.mem.ByteAllocator.Cursor, java.lang.Object)
		 */
		@Override
		public void setValue(PackageHandle handle, Cursor cursor, Object value) {
			@SuppressWarnings("unchecked")
			int index = substitution.applyAsInt((T) value);
			cursor.setNBytes(handle.getOffset(), index, bytes);
		}
	}
}
