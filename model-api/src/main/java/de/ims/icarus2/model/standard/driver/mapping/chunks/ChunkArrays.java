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

 * $Revision: 398 $
 * $Date: 2015-05-29 11:29:49 +0200 (Fr, 29 Mai 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/driver/mapping/chunks/ChunkArrays.java $
 *
 * $LastChangedDate: 2015-05-29 11:29:49 +0200 (Fr, 29 Mai 2015) $
 * $LastChangedRevision: 398 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.driver.mapping.chunks;

import static de.ims.icarus2.model.util.Conditions.checkArgument;
import static de.ims.icarus2.model.util.Conditions.checkNotNull;

import java.nio.ByteBuffer;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;

/**
 * @author Markus Gärtner
 * @version $Id: ChunkArrays.java 398 2015-05-29 09:29:49Z mcgaerty $
 *
 */
public class ChunkArrays {

//	private static final int SHORT_BYTES = 2;
//	private static final int INT_BYTES = 4;
//	private static final int LONG_BYTES = 8;

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

	public static class DefaultAdapter implements ArrayAdapter {
		private final IndexValueType valueType;

		public DefaultAdapter(IndexValueType valueType) {
			checkNotNull(valueType);
			this.valueType = valueType;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#chunkSize()
		 */
		@Override
		public int chunkSize() {
			return valueType.bytesPerValue()*2;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#createBuffer(int)
		 */
		@Override
		public Object createBuffer(int byteCount) {
			return valueType.newArray(byteCount/valueType.bytesPerValue());
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#bufferSize(java.lang.Object)
		 */
		@Override
		public int bufferSize(Object buffer) {
			return valueType.length(buffer)>>1;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#read(java.lang.Object, java.nio.ByteBuffer)
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
		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#write(java.lang.Object, java.nio.ByteBuffer)
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
		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#getFileId(java.lang.Object, int)
		 */
		@Override
		public int getFileId(Object buffer, int index) {
			return 0;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#getBeginOffset(java.lang.Object, int)
		 */
		@Override
		public long getBeginOffset(Object buffer, int index) {
			return valueType.get(buffer, index<<1);
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#getEndOffset(java.lang.Object, int)
		 */
		@Override
		public long getEndOffset(Object buffer, int index) {
			return valueType.get(buffer, (index<<1)+1);
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#setFileId(java.lang.Object, int, int)
		 */
		@Override
		public int setFileId(Object buffer, int index, int fileId) {
			throw new ModelException(ModelErrorCode.UNSUPPORTED_OPERATION,
					"Adapter implementation does not support mapping to multiple files");
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#setBeginOffset(java.lang.Object, int, long)
		 */
		@Override
		public long setBeginOffset(Object buffer, int index, long offset) {
			index = index<<1;

			long result = valueType.get(buffer, index);
			valueType.set(buffer, index, offset);

			return result;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#setEndOffset(java.lang.Object, int, long)
		 */
		@Override
		public long setEndOffset(Object buffer, int index, long offset) {
			index = (index<<1)+1;

			long result = valueType.get(buffer, index);
			valueType.set(buffer, index, offset);

			return result;
		}
	}

	public static class DefaultFileAdapter implements ArrayAdapter {
		// Specifies representation of buffer array in memory
		private final IndexValueType valueType;
		// Specifies byte size of file id in data
		private final IndexValueType fileValueType;

		public DefaultFileAdapter(IndexValueType valueType, IndexValueType fileValueType) {
			checkNotNull(valueType);
			checkNotNull(fileValueType);
			checkArgument("Value space for file id is restricted to int: "+fileValueType,
					fileValueType.compareTo(IndexValueType.INTEGER)>0);

			this.valueType = valueType;
			this.fileValueType = fileValueType;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#chunkSize()
		 */
		@Override
		public int chunkSize() {
			return valueType.bytesPerValue()*3;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#createBuffer(int)
		 */
		@Override
		public Object createBuffer(int byteCount) {
			return valueType.newArray(byteCount/valueType.bytesPerValue());
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#bufferSize(java.lang.Object)
		 */
		@Override
		public int bufferSize(Object buffer) {
			return valueType.length(buffer)/3;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#read(java.lang.Object, java.nio.ByteBuffer)
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
		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#write(java.lang.Object, java.nio.ByteBuffer)
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
		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#getFileId(java.lang.Object, int)
		 */
		@Override
		public int getFileId(Object buffer, int index) {
			return (int) valueType.get(buffer, index*3);
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#getBeginOffset(java.lang.Object, int)
		 */
		@Override
		public long getBeginOffset(Object buffer, int index) {
			return valueType.get(buffer, (index*3)+1);
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#getEndOffset(java.lang.Object, int)
		 */
		@Override
		public long getEndOffset(Object buffer, int index) {
			return valueType.get(buffer, (index*3)+2);
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#setFileId(java.lang.Object, int, int)
		 */
		@Override
		public int setFileId(Object buffer, int index, int fileId) {
			index = index*3;

			int result = (int) valueType.get(buffer, index);
			valueType.set(buffer, index, fileId);

			return result;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#setBeginOffset(java.lang.Object, int, long)
		 */
		@Override
		public long setBeginOffset(Object buffer, int index, long offset) {
			index = (index*3)+1;

			long result = valueType.get(buffer, index);
			valueType.set(buffer, index, offset);

			return result;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#setEndOffset(java.lang.Object, int, long)
		 */
		@Override
		public long setEndOffset(Object buffer, int index, long offset) {
			index = (index*3)+2;

			long result = valueType.get(buffer, index);
			valueType.set(buffer, index, offset);

			return result;
		}
	}

//	public static class ShortAdapter implements ArrayAdapter {
//
//		// begin-offset | end-offset
//		private static final int BYTES_PER_ENTRY = SHORT_BYTES + SHORT_BYTES;
//
//		private final int deltaBegin;
//		private final int deltaEnd;
//
//		private ShortAdapter() {
//			this(0, 1);
//		}
//
//		private ShortAdapter(int deltaBegin, int deltaEnd) {
//			this.deltaBegin = deltaBegin;
//			this.deltaEnd = deltaEnd;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#createBuffer(long)
//		 */
//		@Override
//		public Object createBuffer(int byteCount) {
//			int size = byteCount/chunkSize();
//			return new short[size];
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#bufferSize(java.lang.Object)
//		 */
//		@Override
//		public int bufferSize(Object buffer) {
//			return ((short[])buffer).length>>1;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#read(java.lang.Object, java.nio.ByteBuffer)
//		 */
//		@Override
//		public void read(Object target, ByteBuffer buffer, int offset, int length) {
//			short[] array = (short[]) target;
//			int index = offset<<1;
//			while(length-->0) {
//				array[index++] = buffer.getShort();
//				array[index++] = buffer.getShort();
//			}
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#write(java.lang.Object, java.nio.ByteBuffer)
//		 */
//		@Override
//		public void write(Object source, ByteBuffer buffer, int offset, int length) {
//			short[] array = (short[]) source;
//			int index = offset<<1;
//			while(length-->0) {
//				buffer.putShort(array[index++]);
//				buffer.putShort(array[index++]);
//			}
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#getFileId(java.lang.Object, int)
//		 */
//		@Override
//		public int getFileId(Object buffer, int index) {
//			return 0;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#getBeginOffset(java.lang.Object, int)
//		 */
//		@Override
//		public long getBeginOffset(Object buffer, int index) {
//			return ((short[])buffer)[index*3+deltaBegin];
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#getEndOffset(java.lang.Object, int)
//		 */
//		@Override
//		public long getEndOffset(Object buffer, int index) {
//			return ((short[])buffer)[index*3+deltaEnd];
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#setFileId(java.lang.Object, long, int)
//		 */
//		@Override
//		public int setFileId(Object buffer, int index, int fileId) {
//			return 0;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#setBeginOffset(java.lang.Object, long, int)
//		 */
//		@Override
//		public long setBeginOffset(Object buffer, int index, long offset) {
//			short[] array = (short[]) buffer;
//			index = index*3+deltaBegin;
//			short old = array[index];
//			array[index] = (short) offset;
//			return old;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#setEndOffset(java.lang.Object, long, int)
//		 */
//		@Override
//		public long setEndOffset(Object buffer, int index, long offset) {
//			short[] array = (short[]) buffer;
//			index = index*3+deltaEnd;
//			short old = array[index];
//			array[index] = (short) offset;
//			return old;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#maxChunkSize()
//		 */
//		@Override
//		public int chunkSize() {
//			return BYTES_PER_ENTRY;
//		}
//
//	}
//
//	public static class ShortFileAdapter extends IntAdapter implements ArrayAdapter {
//
//		// file-id | begin-offset | end-offset
//		private static final int BYTES_PER_ENTRY = SHORT_BYTES + SHORT_BYTES + SHORT_BYTES;
//
//		private ShortFileAdapter() {
//			super(1, 2);
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#bufferSize(java.lang.Object)
//		 */
//		@Override
//		public int bufferSize(Object buffer) {
//			return ((short[])buffer).length/3;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#read(java.lang.Object, java.nio.ByteBuffer)
//		 */
//		@Override
//		public void read(Object target, ByteBuffer buffer, int offset, int length) {
//			short[] array = (short[]) target;
//			int index = offset*3;
//			while(length-->0) {
//				array[index++] = buffer.getShort();
//				array[index++] = buffer.getShort();
//				array[index++] = buffer.getShort();
//			}
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#write(java.lang.Object, java.nio.ByteBuffer)
//		 */
//		@Override
//		public void write(Object source, ByteBuffer buffer, int offset, int length) {
//			short[] array = (short[]) source;
//			int index = offset*3;
//			while(length-->0) {
//				buffer.putShort(array[index++]);
//				buffer.putShort(array[index++]);
//				buffer.putShort(array[index++]);
//			}
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#getFileId(java.lang.Object, int)
//		 */
//		@Override
//		public int getFileId(Object buffer, int index) {
//			return ((short[])buffer)[index*3];
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#setFileId(java.lang.Object, long, int)
//		 */
//		@Override
//		public int setFileId(Object buffer, int index, int fileId) {
//			short[] array = (short[]) buffer;
//			index = index*3;
//			short old = array[index];
//			array[index] = (short) fileId;
//			return old;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#maxChunkSize()
//		 */
//		@Override
//		public int chunkSize() {
//			return BYTES_PER_ENTRY;
//		}
//
//	}
//
//	public static class IntAdapter implements ArrayAdapter {
//
//		// begin-offset | end-offset
//		private static final int BYTES_PER_ENTRY = INT_BYTES + INT_BYTES;
//
//		private final int deltaBegin;
//		private final int deltaEnd;
//
//		private IntAdapter() {
//			this(0, 1);
//		}
//
//		private IntAdapter(int deltaBegin, int deltaEnd) {
//			this.deltaBegin = deltaBegin;
//			this.deltaEnd = deltaEnd;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#createBuffer(long)
//		 */
//		@Override
//		public Object createBuffer(int byteCount) {
//			int size = byteCount/chunkSize();
//			return new int[size];
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#bufferSize(java.lang.Object)
//		 */
//		@Override
//		public int bufferSize(Object buffer) {
//			return ((int[])buffer).length>>1;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#read(java.lang.Object, java.nio.ByteBuffer)
//		 */
//		@Override
//		public void read(Object target, ByteBuffer buffer, int offset, int length) {
//			int[] array = (int[]) target;
//			int index = offset<<1;
//			while(length-->0) {
//				array[index++] = buffer.getInt();
//				array[index++] = buffer.getInt();
//			}
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#write(java.lang.Object, java.nio.ByteBuffer)
//		 */
//		@Override
//		public void write(Object source, ByteBuffer buffer, int offset, int length) {
//			int[] array = (int[]) source;
//			int index = offset<<1;
//			while(length-->0) {
//				buffer.putInt(array[index++]);
//				buffer.putInt(array[index++]);
//			}
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#getFileId(java.lang.Object, int)
//		 */
//		@Override
//		public int getFileId(Object buffer, int index) {
//			return 0;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#getBeginOffset(java.lang.Object, int)
//		 */
//		@Override
//		public long getBeginOffset(Object buffer, int index) {
//			return ((int[])buffer)[index*3+deltaBegin];
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#getEndOffset(java.lang.Object, int)
//		 */
//		@Override
//		public long getEndOffset(Object buffer, int index) {
//			return ((int[])buffer)[index*3+deltaEnd];
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#setFileId(java.lang.Object, long, int)
//		 */
//		@Override
//		public int setFileId(Object buffer, int index, int fileId) {
//			return 0;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#setBeginOffset(java.lang.Object, long, int)
//		 */
//		@Override
//		public long setBeginOffset(Object buffer, int index, long offset) {
//			int[] array = (int[]) buffer;
//			index = index*3+deltaBegin;
//			int old = array[index];
//			array[index] = (int) offset;
//			return old;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#setEndOffset(java.lang.Object, long, int)
//		 */
//		@Override
//		public long setEndOffset(Object buffer, int index, long offset) {
//			int[] array = (int[]) buffer;
//			index = index*3+deltaEnd;
//			int old = array[index];
//			array[index] = (int) offset;
//			return old;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#maxChunkSize()
//		 */
//		@Override
//		public int chunkSize() {
//			return BYTES_PER_ENTRY;
//		}
//
//	}
//
//	public static class IntFileAdapter extends IntAdapter implements ArrayAdapter {
//
//		// file-id | begin-offset | end-offset
//		private static final int BYTES_PER_ENTRY = INT_BYTES + INT_BYTES + INT_BYTES;
//
//		private IntFileAdapter() {
//			super(1, 2);
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#bufferSize(java.lang.Object)
//		 */
//		@Override
//		public int bufferSize(Object buffer) {
//			return ((int[])buffer).length/3;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#read(java.lang.Object, java.nio.ByteBuffer)
//		 */
//		@Override
//		public void read(Object target, ByteBuffer buffer, int offset, int length) {
//			int[] array = (int[]) target;
//			int index = offset*3;
//			while(length-->0) {
//				array[index++] = buffer.getInt();
//				array[index++] = buffer.getInt();
//				array[index++] = buffer.getInt();
//			}
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#write(java.lang.Object, java.nio.ByteBuffer)
//		 */
//		@Override
//		public void write(Object source, ByteBuffer buffer, int offset, int length) {
//			int[] array = (int[]) source;
//			int index = offset*3;
//			while(length-->0) {
//				buffer.putInt(array[index++]);
//				buffer.putInt(array[index++]);
//				buffer.putInt(array[index++]);
//			}
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#getFileId(java.lang.Object, int)
//		 */
//		@Override
//		public int getFileId(Object buffer, int index) {
//			return ((int[])buffer)[index*3];
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#setFileId(java.lang.Object, long, int)
//		 */
//		@Override
//		public int setFileId(Object buffer, int index, int fileId) {
//			int[] array = (int[]) buffer;
//			index = index*3;
//			int old = array[index];
//			array[index] = fileId;
//			return old;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#maxChunkSize()
//		 */
//		@Override
//		public int chunkSize() {
//			return BYTES_PER_ENTRY;
//		}
//
//	}
//
//	public static class LongAdapter implements ArrayAdapter {
//
//		// begin-offset | end-offset
//		private static final int BYTES_PER_ENTRY = LONG_BYTES + LONG_BYTES;
//
//		private final int deltaBegin;
//		private final int deltaEnd;
//
//		private LongAdapter() {
//			this(0, 1);
//		}
//
//		private LongAdapter(int deltaBegin, int deltaEnd) {
//			this.deltaBegin = deltaBegin;
//			this.deltaEnd = deltaEnd;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#createBuffer(long)
//		 */
//		@Override
//		public Object createBuffer(int byteCount) {
//			int size = byteCount/chunkSize();
//			return new long[size];
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#bufferSize(java.lang.Object)
//		 */
//		@Override
//		public int bufferSize(Object buffer) {
//			return ((long[])buffer).length>>1;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.IntAdapter#read(java.lang.Object, java.nio.ByteBuffer)
//		 */
//		@Override
//		public void read(Object target, ByteBuffer buffer, int offset, int length) {
//			long[] array = (long[]) target;
//			int index = offset*2;
//			while(length-->0) {
//				array[index++] = buffer.getLong();
//				array[index++] = buffer.getLong();
//			}
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.IntAdapter#write(java.lang.Object, java.nio.ByteBuffer)
//		 */
//		@Override
//		public void write(Object source, ByteBuffer buffer, int offset, int length) {
//			long[] array = (long[]) source;
//			int index = offset<<1;
//			while(length-->0) {
//				buffer.putLong(array[index++]);
//				buffer.putLong(array[index++]);
//			}
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#getFileId(java.lang.Object, int)
//		 */
//		@Override
//		public int getFileId(Object buffer, int index) {
//			return 0;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#getBeginOffset(java.lang.Object, int)
//		 */
//		@Override
//		public long getBeginOffset(Object buffer, int index) {
//			return ((long[])buffer)[index*3+deltaBegin];
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#getEndOffset(java.lang.Object, int)
//		 */
//		@Override
//		public long getEndOffset(Object buffer, int index) {
//			return ((long[])buffer)[index*3+deltaEnd];
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#setFileId(java.lang.Object, long, int)
//		 */
//		@Override
//		public int setFileId(Object buffer, int index, int fileId) {
//			return 0;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#setBeginOffset(java.lang.Object, long, int)
//		 */
//		@Override
//		public long setBeginOffset(Object buffer, int index, long offset) {
//			long[] array = (long[]) buffer;
//			index = index*3+deltaBegin;
//			long old = array[index];
//			array[index] = offset;
//			return old;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#setEndOffset(java.lang.Object, long, int)
//		 */
//		@Override
//		public long setEndOffset(Object buffer, int index, long offset) {
//			long[] array = (long[]) buffer;
//			index = index*3+deltaEnd;
//			long old = array[index];
//			array[index] = offset;
//			return old;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#maxChunkSize()
//		 */
//		@Override
//		public int chunkSize() {
//			return BYTES_PER_ENTRY;
//		}
//
//	}
//
//	public static class LongFileAdapter extends IntAdapter implements ArrayAdapter {
//
//		// file-id | begin-offset | end-offset
//		private static final int BYTES_PER_ENTRY = INT_BYTES + LONG_BYTES + LONG_BYTES;
//
//		private LongFileAdapter() {
//			super(1, 2);
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#bufferSize(java.lang.Object)
//		 */
//		@Override
//		public int bufferSize(Object buffer) {
//			return ((long[])buffer).length/3;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.IntAdapter#read(java.lang.Object, java.nio.ByteBuffer)
//		 */
//		@Override
//		public void read(Object target, ByteBuffer buffer, int offset, int length) {
//			long[] array = (long[]) target;
//			int index = offset*3;
//			while(length-->0) {
//				array[index++] = buffer.getInt();
//				array[index++] = buffer.getLong();
//				array[index++] = buffer.getLong();
//			}
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.IntAdapter#write(java.lang.Object, java.nio.ByteBuffer)
//		 */
//		@Override
//		public void write(Object source, ByteBuffer buffer, int offset, int length) {
//			long[] array = (long[]) source;
//			int index = offset*3;
//			while(length-->0) {
//				buffer.putInt((int) array[index++]);
//				buffer.putLong(array[index++]);
//				buffer.putLong(array[index++]);
//			}
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#getFileId(java.lang.Object, int)
//		 */
//		@Override
//		public int getFileId(Object buffer, int index) {
//			return (int) ((long[])buffer)[index*3];
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.DefaultChunkIndex.ArrayAdapter#setFileId(java.lang.Object, long, int)
//		 */
//		@Override
//		public int setFileId(Object buffer, int index, int fileId) {
//			long[] array = (long[]) buffer;
//			index = index*3;
//			int old = (int) array[index];
//			array[index] = fileId;
//			return old;
//		}
//
//		/**
//		 * @see de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkArrays.ArrayAdapter#maxChunkSize()
//		 */
//		@Override
//		public int chunkSize() {
//			return BYTES_PER_ENTRY;
//		}
//
//	}
}
