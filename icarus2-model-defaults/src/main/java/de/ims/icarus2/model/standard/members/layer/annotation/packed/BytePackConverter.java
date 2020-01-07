/**
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
package de.ims.icarus2.model.standard.members.layer.annotation.packed;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.util.io.IOUtil;
import de.ims.icarus2.util.mem.ByteAllocator.Cursor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public abstract class BytePackConverter implements Closeable {

	private static final Map<ValueType, BytePackConverter> sharedConverters
		= new Object2ObjectOpenHashMap<>();
	static {
		sharedConverters.put(ValueType.BOOLEAN, new BooleanConverter());
		sharedConverters.put(ValueType.INTEGER, new IntConverter());
		sharedConverters.put(ValueType.LONG, new LongConverter());
		sharedConverters.put(ValueType.FLOAT, new FloatConverter());
		sharedConverters.put(ValueType.DOUBLE, new DoubleConverter());
	}

	/**
	 * Returns a (potentially shared) instance of a converter suitable for the
	 * given {@link ValueType}.
	 *
	 * @param type
	 * @param allowBitPacking
	 * @return
	 *
	 * @throws ModelException if no suitable converter could be found
	 */
	public static BytePackConverter forPrimitiveType(ValueType type, boolean allowBitPacking) {
		checkArgument("Must be a primitive value type: "+type, type.isPrimitiveType());

		if(allowBitPacking && type==ValueType.BOOLEAN) {
			return new BitwiseBooleanConverter();
		}

		BytePackConverter converter = sharedConverters.get(type);
		if(converter==null)
			throw new ModelException(GlobalErrorCode.UNKNOWN_ENUM, "Unknown primitive type: "+type);

		return converter;
	}

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
	 * <p>
	 * This method is only supported when {@link #sizeInBytes()}
	 * returns {@code 0};
	 *
	 * @return
	 */
	public int sizeInBits() {
		throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Not a bit-based converter!");
	}

	/**
	 * Creates required utility objects that cannot be shared by
	 * multiple converter instances.
	 * @return
	 */
	public Object createContext() {
		return null;
	}

	private ModelException forUnsupportedType(ValueType valueType) {
		return new ModelException(ManifestErrorCode.MANIFEST_TYPE_CAST,
				Messages.mismatch("Cannot convert", getValueType(), valueType.getName()));
	}

	// Read method

	public boolean getBoolean(PackageHandle handle, Cursor cursor) {
		throw forUnsupportedType(ValueType.BOOLEAN);
	}

	public int getInteger(PackageHandle handle, Cursor cursor) {
		throw forUnsupportedType(ValueType.INTEGER);
	}

	public long getLong(PackageHandle handle, Cursor cursor) {
		throw forUnsupportedType(ValueType.LONG);
	}

	public float getFloat(PackageHandle handle, Cursor cursor) {
		throw forUnsupportedType(ValueType.FLOAT);
	}

	public double getDouble(PackageHandle handle, Cursor cursor) {
		throw forUnsupportedType(ValueType.DOUBLE);
	}

	public Object getValue(PackageHandle handle, Cursor cursor) {
		throw forUnsupportedType(ValueType.CUSTOM);
	}

	public String getString(PackageHandle handle, Cursor cursor) {
		throw forUnsupportedType(ValueType.STRING);
	}

	// Write methods

	public void setBoolean(PackageHandle handle, Cursor cursor, boolean value) {
		throw forUnsupportedType(ValueType.BOOLEAN);
	}

	public void setInteger(PackageHandle handle, Cursor cursor, int value) {
		throw forUnsupportedType(ValueType.INTEGER);
	}

	public void setLong(PackageHandle handle, Cursor cursor, long value) {
		throw forUnsupportedType(ValueType.LONG);
	}

	public void setFloat(PackageHandle handle, Cursor cursor, float value) {
		throw forUnsupportedType(ValueType.FLOAT);
	}

	public void setDouble(PackageHandle handle, Cursor cursor, double value) {
		throw forUnsupportedType(ValueType.DOUBLE);
	}

	public void setValue(PackageHandle handle, Cursor cursor, Object value) {
		throw forUnsupportedType(ValueType.CUSTOM);
	}

	public void setString(PackageHandle handle, Cursor cursor, String value) {
		throw forUnsupportedType(ValueType.STRING);
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
		return Objects.equals(v1, v2);
	}

	/**
	 * Default implementation does nothing.
	 *
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() {
		// no-op
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	public static final class BitwiseBooleanConverter extends BytePackConverter {

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#getValueType()
		 */
		@Override
		public ValueType getValueType() {
			return ValueType.BOOLEAN;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#sizeInBytes()
		 */
		@Override
		public int sizeInBytes() {
			return 0;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#sizeInBits()
		 */
		@Override
		public int sizeInBits() {
			return 1;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#getBoolean(de.ims.icarus2.util.mem.ByteAllocator.Cursor)
		 */
		@Override
		public boolean getBoolean(PackageHandle handle, Cursor cursor) {
			byte mask = (byte) ((1<<handle.getBit()) & 0xff);
			return (cursor.getByte(handle.getOffset()) & mask) == mask;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#setBoolean(de.ims.icarus2.util.mem.ByteAllocator.Cursor, boolean)
		 */
		@Override
		public void setBoolean(PackageHandle handle, Cursor cursor, boolean value) {
			int offset = handle.getOffset();
			byte block = cursor.getByte(offset);

			byte mask = (byte) ((1<<handle.getBit()) & 0xff);
			if(value) {
				block |= mask;
			} else {
				block &= ~mask;
			}

			cursor.setByte(offset, block);
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#setValue(de.ims.icarus2.model.standard.members.layer.annotation.packed.PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor, java.lang.Object)
		 */
		@Override
		public void setValue(PackageHandle handle, Cursor cursor, Object value) {
			setBoolean(handle, cursor, ((Boolean)value).booleanValue());
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#getValue(de.ims.icarus2.model.standard.members.layer.annotation.packed.PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor)
		 */
		@Override
		public Object getValue(PackageHandle handle, Cursor cursor) {
			return Boolean.valueOf(getBoolean(handle, cursor));
		}
	}

	public static final class BooleanConverter extends BytePackConverter {

		private static final byte TRUE = 0b01;
		private static final byte FALSE = 0b00;

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#getValueType()
		 */
		@Override
		public ValueType getValueType() {
			return ValueType.BOOLEAN;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#sizeInBytes()
		 */
		@Override
		public int sizeInBytes() {
			return Byte.BYTES;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#getBoolean(de.ims.icarus2.util.mem.ByteAllocator.Cursor)
		 */
		@Override
		public boolean getBoolean(PackageHandle handle, Cursor cursor) {
			return cursor.getByte(handle.getOffset())==TRUE;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#setBoolean(de.ims.icarus2.util.mem.ByteAllocator.Cursor, boolean)
		 */
		@Override
		public void setBoolean(PackageHandle handle, Cursor cursor, boolean value) {
			byte flag = value ? TRUE : FALSE;
			cursor.setByte(handle.getOffset(), flag);
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#setValue(de.ims.icarus2.model.standard.members.layer.annotation.packed.PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor, java.lang.Object)
		 */
		@Override
		public void setValue(PackageHandle handle, Cursor cursor, Object value) {
			setBoolean(handle, cursor, ((Boolean)value).booleanValue());
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#getValue(de.ims.icarus2.model.standard.members.layer.annotation.packed.PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor)
		 */
		@Override
		public Object getValue(PackageHandle handle, Cursor cursor) {
			return Boolean.valueOf(getBoolean(handle, cursor));
		}
	}

	public static final class IntConverter extends BytePackConverter {

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#getValueType()
		 */
		@Override
		public ValueType getValueType() {
			return ValueType.INTEGER;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#sizeInBytes()
		 */
		@Override
		public int sizeInBytes() {
			return Integer.BYTES;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#getInteger(de.ims.icarus2.util.mem.ByteAllocator.Cursor)
		 */
		@Override
		public int getInteger(PackageHandle handle, Cursor cursor) {
			return cursor.getInt(handle.getOffset());
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#setInteger(de.ims.icarus2.util.mem.ByteAllocator.Cursor, int)
		 */
		@Override
		public void setInteger(PackageHandle handle, Cursor cursor, int value) {
			cursor.setInt(handle.getOffset(), value);
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#setValue(de.ims.icarus2.model.standard.members.layer.annotation.packed.PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor, java.lang.Object)
		 */
		@Override
		public void setValue(PackageHandle handle, Cursor cursor, Object value) {
			setInteger(handle, cursor, ((Number)value).intValue());
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#getValue(de.ims.icarus2.model.standard.members.layer.annotation.packed.PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor)
		 */
		@Override
		public Object getValue(PackageHandle handle, Cursor cursor) {
			return Integer.valueOf(getInteger(handle, cursor));
		}
	}

	public static final class LongConverter extends BytePackConverter {

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#getValueType()
		 */
		@Override
		public ValueType getValueType() {
			return ValueType.LONG;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#sizeInBytes()
		 */
		@Override
		public int sizeInBytes() {
			return Long.BYTES;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#getLong(de.ims.icarus2.util.mem.ByteAllocator.Cursor)
		 */
		@Override
		public long getLong(PackageHandle handle, Cursor cursor) {
			return cursor.getLong(handle.getOffset());
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#setLong(de.ims.icarus2.util.mem.ByteAllocator.Cursor, long)
		 */
		@Override
		public void setLong(PackageHandle handle, Cursor cursor, long value) {
			cursor.setLong(handle.getOffset(), value);
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#setValue(de.ims.icarus2.model.standard.members.layer.annotation.packed.PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor, java.lang.Object)
		 */
		@Override
		public void setValue(PackageHandle handle, Cursor cursor, Object value) {
			setLong(handle, cursor, ((Number)value).longValue());
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#getValue(de.ims.icarus2.model.standard.members.layer.annotation.packed.PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor)
		 */
		@Override
		public Object getValue(PackageHandle handle, Cursor cursor) {
			return Long.valueOf(getLong(handle, cursor));
		}
	}

	public static final class FloatConverter extends BytePackConverter {

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#getValueType()
		 */
		@Override
		public ValueType getValueType() {
			return ValueType.FLOAT;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#sizeInBytes()
		 */
		@Override
		public int sizeInBytes() {
			return Integer.BYTES;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#getFloat(de.ims.icarus2.util.mem.ByteAllocator.Cursor)
		 */
		@Override
		public float getFloat(PackageHandle handle, Cursor cursor) {
			return Float.intBitsToFloat(cursor.getInt(handle.getOffset()));
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#setFloat(de.ims.icarus2.util.mem.ByteAllocator.Cursor, float)
		 */
		@Override
		public void setFloat(PackageHandle handle, Cursor cursor, float value) {
			cursor.setInt(handle.getOffset(), Float.floatToIntBits(value));
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#setValue(de.ims.icarus2.model.standard.members.layer.annotation.packed.PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor, java.lang.Object)
		 */
		@Override
		public void setValue(PackageHandle handle, Cursor cursor, Object value) {
			setFloat(handle, cursor, ((Number)value).floatValue());
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#getValue(de.ims.icarus2.model.standard.members.layer.annotation.packed.PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor)
		 */
		@Override
		public Object getValue(PackageHandle handle, Cursor cursor) {
			return Float.valueOf(getFloat(handle, cursor));
		}
	}

	public static final class DoubleConverter extends BytePackConverter {

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#getValueType()
		 */
		@Override
		public ValueType getValueType() {
			return ValueType.DOUBLE;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#sizeInBytes()
		 */
		@Override
		public int sizeInBytes() {
			return Long.BYTES;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#getDouble(de.ims.icarus2.util.mem.ByteAllocator.Cursor)
		 */
		@Override
		public double getDouble(PackageHandle handle, Cursor cursor) {
			return Double.longBitsToDouble(cursor.getLong(handle.getOffset()));
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#setDouble(de.ims.icarus2.util.mem.ByteAllocator.Cursor, double)
		 */
		@Override
		public void setDouble(PackageHandle handle, Cursor cursor, double value) {
			cursor.setLong(handle.getOffset(), Double.doubleToLongBits(value));
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#setValue(de.ims.icarus2.model.standard.members.layer.annotation.packed.PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor, java.lang.Object)
		 */
		@Override
		public void setValue(PackageHandle handle, Cursor cursor, Object value) {
			setDouble(handle, cursor, ((Number)value).doubleValue());
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#getValue(de.ims.icarus2.model.standard.members.layer.annotation.packed.PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor)
		 */
		@Override
		public Object getValue(PackageHandle handle, Cursor cursor) {
			return Double.valueOf(getDouble(handle, cursor));
		}
	}

	public static class SubstitutingConverterInt<T extends Object> extends BytePackConverter {

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
		public SubstitutingConverterInt(ValueType valueType, int bytes, ToIntFunction<T> substitution,
				IntFunction<T> resubstitution) {
			checkArgument("Byte size must be between 1 and "+Integer.BYTES+", inclusively",
					bytes>0 && bytes<=Integer.BYTES);

			this.valueType = requireNonNull(valueType);
			this.bytes = bytes;
			this.substitution = requireNonNull(substitution);
			this.resubstitution = requireNonNull(resubstitution);
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#getValueType()
		 */
		@Override
		public ValueType getValueType() {
			return valueType;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#sizeInBytes()
		 */
		@Override
		public int sizeInBytes() {
			return bytes;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#getValue(de.ims.icarus2.util.mem.ByteAllocator.Cursor)
		 */
		@Override
		public Object getValue(PackageHandle handle, Cursor cursor) {
			return resubstitution.apply((int)cursor.getNBytes(handle.getOffset(), bytes));
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#getString(de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor)
		 */
		@Override
		public String getString(PackageHandle handle, Cursor cursor) {
			valueType.checkType(String.class);
			return (String) getValue(handle, cursor);
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#setValue(de.ims.icarus2.util.mem.ByteAllocator.Cursor, java.lang.Object)
		 */
		@Override
		public void setValue(PackageHandle handle, Cursor cursor, Object value) {
			@SuppressWarnings("unchecked")
			int index = substitution.applyAsInt((T) value);
			cursor.setNBytes(handle.getOffset(), index, bytes);
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#setString(de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle, de.ims.icarus2.util.mem.ByteAllocator.Cursor, java.lang.String)
		 */
		@Override
		public void setString(PackageHandle handle, Cursor cursor, String value) {
			valueType.checkType(String.class);
			setValue(handle, cursor, value);
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#close()
		 */
		@Override
		public void close() {
			if(substitution instanceof Closeable) {
				IOUtil.closeSilently((Closeable) substitution);
			}

			if(resubstitution instanceof Closeable) {
				IOUtil.closeSilently((Closeable) resubstitution);
			}
		}
	}
}
