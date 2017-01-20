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
package de.ims.icarus2.filedriver.mapping.chunks;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.nio.ByteBuffer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;

/**
 * @author Markus Gärtner
 *
 */
public class ChunkArrays {

//	private static final int SHORT_BYTES = 2;
//	private static final int INT_BYTES = 4;
//	private static final int LONG_BYTES = 8;

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface ArrayAdapter {

		Object createBuffer(int byteCount);

		int bufferSize(Object buffer);

		void read(Object target, ByteBuffer buffer, int offset, int length);

		void write(Object source, ByteBuffer buffer, int offset, int length);

		int getFileId(Object buffer, int index);

		long getBeginOffset(Object buffer, int index);

		long getEndOffset(Object buffer, int index);

		int setFileId(Object buffer, int index, int fileId);

		long setBeginOffset(Object buffer, int index, long offset);

		long setEndOffset(Object buffer, int index, long offset);

		/**
		 * Returns the byte size of a single entry. This information
		 * is used to create {@code ByteBuffer} objects of adequate size
		 * for reading and writing.
		 * @return
		 */
		int chunkSize();
	}

	public static ArrayAdapter createBasicAdapter(IndexValueType valueType) {
		return new DefaultAdapter(valueType);
	}

	public static ArrayAdapter createFileAdapter(IndexValueType valueType, IndexValueType fileValueType) {
		return new DefaultFileAdapter(valueType, fileValueType);
	}

//	public static ArrayAdapter createIntAdapter() {
//		return new IntAdapter();
//	}
//
//	public static ArrayAdapter createIntFileAdapter() {
//		return new IntFileAdapter();
//	}
//
//	public static ArrayAdapter createLongAdapter() {
//		return new LongAdapter();
//	}
//
//	public static ArrayAdapter createLongFileAdapter() {
//		return new LongFileAdapter();
//	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class DefaultAdapter implements ArrayAdapter {
		private final IndexValueType valueType;

		public DefaultAdapter(IndexValueType valueType) {
			requireNonNull(valueType);
			this.valueType = valueType;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter#chunkSize()
		 */
		@Override
		public int chunkSize() {
			return valueType.bytesPerValue()*2;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter#createBuffer(int)
		 */
		@Override
		public Object createBuffer(int byteCount) {
			return valueType.newArray(byteCount/valueType.bytesPerValue());
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter#bufferSize(java.lang.Object)
		 */
		@Override
		public int bufferSize(Object buffer) {
			return valueType.length(buffer)>>1;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter#read(java.lang.Object, java.nio.ByteBuffer)
		 */
		@Override
		public void read(Object target, ByteBuffer buffer, int offset, int length) {
			int index = offset<<1;
			while(length-->0) {
				valueType.set(target, index++, valueType.get(buffer));
				valueType.set(target, index++, valueType.get(buffer));
			}
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter#write(java.lang.Object, java.nio.ByteBuffer)
		 */
		@Override
		public void write(Object source, ByteBuffer buffer, int offset, int length) {
			int index = offset<<1;
			while(length-->0) {
				valueType.set(buffer, valueType.get(source, index++));
				valueType.set(buffer, valueType.get(source, index++));
			}
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter#getFileId(java.lang.Object, int)
		 */
		@Override
		public int getFileId(Object buffer, int index) {
			return 0;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter#getBeginOffset(java.lang.Object, int)
		 */
		@Override
		public long getBeginOffset(Object buffer, int index) {
			return valueType.get(buffer, index<<1);
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter#getEndOffset(java.lang.Object, int)
		 */
		@Override
		public long getEndOffset(Object buffer, int index) {
			return valueType.get(buffer, (index<<1)+1);
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter#setFileId(java.lang.Object, int, int)
		 */
		@Override
		public int setFileId(Object buffer, int index, int fileId) {
			throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
					"Adapter implementation does not support mapping to multiple files");
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter#setBeginOffset(java.lang.Object, int, long)
		 */
		@Override
		public long setBeginOffset(Object buffer, int index, long offset) {
			index = index<<1;

			long result = valueType.get(buffer, index);
			valueType.set(buffer, index, offset);

			return result;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter#setEndOffset(java.lang.Object, int, long)
		 */
		@Override
		public long setEndOffset(Object buffer, int index, long offset) {
			index = (index<<1)+1;

			long result = valueType.get(buffer, index);
			valueType.set(buffer, index, offset);

			return result;
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class DefaultFileAdapter implements ArrayAdapter {
		// Specifies representation of buffer array in memory
		private final IndexValueType valueType;
		// Specifies byte size of file id in data
		private final IndexValueType fileValueType;

		public DefaultFileAdapter(IndexValueType valueType, IndexValueType fileValueType) {
			requireNonNull(valueType);
			requireNonNull(fileValueType);
			checkArgument("Value space for file id is restricted to int: "+fileValueType,
					fileValueType.compareTo(IndexValueType.INTEGER)>0);

			//FIXME make sure valueType is the one with greater value space (otherwise we could lose data when reading fileId into buffer)

			this.valueType = valueType;
			this.fileValueType = fileValueType;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter#chunkSize()
		 */
		@Override
		public int chunkSize() {
			return valueType.bytesPerValue()*3;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter#createBuffer(int)
		 */
		@Override
		public Object createBuffer(int byteCount) {
			return valueType.newArray(byteCount/valueType.bytesPerValue());
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter#bufferSize(java.lang.Object)
		 */
		@Override
		public int bufferSize(Object buffer) {
			return valueType.length(buffer)/3;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter#read(java.lang.Object, java.nio.ByteBuffer)
		 */
		@Override
		public void read(Object target, ByteBuffer buffer, int offset, int length) {
			int index = offset*3;
			while(length-->0) {
				valueType.set(target, index++, fileValueType.get(buffer));
				valueType.set(target, index++, valueType.get(buffer));
				valueType.set(target, index++, valueType.get(buffer));
			}
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter#write(java.lang.Object, java.nio.ByteBuffer)
		 */
		@Override
		public void write(Object source, ByteBuffer buffer, int offset, int length) {
			int index = offset*3;
			while(length-->0) {
				fileValueType.set(buffer, valueType.get(source, index++));
				valueType.set(buffer, valueType.get(source, index++));
				valueType.set(buffer, valueType.get(source, index++));
			}
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter#getFileId(java.lang.Object, int)
		 */
		@Override
		public int getFileId(Object buffer, int index) {
			return (int) valueType.get(buffer, index*3);
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter#getBeginOffset(java.lang.Object, int)
		 */
		@Override
		public long getBeginOffset(Object buffer, int index) {
			return valueType.get(buffer, (index*3)+1);
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter#getEndOffset(java.lang.Object, int)
		 */
		@Override
		public long getEndOffset(Object buffer, int index) {
			return valueType.get(buffer, (index*3)+2);
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter#setFileId(java.lang.Object, int, int)
		 */
		@Override
		public int setFileId(Object buffer, int index, int fileId) {
			index = index*3;

			int result = (int) valueType.get(buffer, index);
			valueType.set(buffer, index, fileId);

			return result;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter#setBeginOffset(java.lang.Object, int, long)
		 */
		@Override
		public long setBeginOffset(Object buffer, int index, long offset) {
			index = (index*3)+1;

			long result = valueType.get(buffer, index);
			valueType.set(buffer, index, offset);

			return result;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.chunks.ChunkArrays.ArrayAdapter#setEndOffset(java.lang.Object, int, long)
		 */
		@Override
		public long setEndOffset(Object buffer, int index, long offset) {
			index = (index*3)+2;

			long result = valueType.get(buffer, index);
			valueType.set(buffer, index, offset);

			return result;
		}
	}
}
