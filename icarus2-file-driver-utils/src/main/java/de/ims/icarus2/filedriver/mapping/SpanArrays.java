/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.filedriver.mapping;

import static java.util.Objects.requireNonNull;

import java.nio.ByteBuffer;
import java.util.Arrays;

import de.ims.icarus2.model.api.driver.indices.IndexValueType;

/**
 * @author Markus Gärtner
 *
 */
@Deprecated
public class SpanArrays {

	private static final int SHORT_BYTES = Short.BYTES;
	private static final int INT_BYTES = Integer.BYTES;
	private static final int LONG_BYTES = Long.BYTES;

	private static final int NOT_FOUND = -1;

//	private static final UnsignedNumbers NUMS = UnsignedNumbers.withOffset(-1);

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface SpanAdapter {

		IndexValueType getValueType();

		/**
		 * Create a new buffer array of given size {@code byteCount}.
		 */
		Object createBuffer(int byteCount);

		int bufferSize(Object buffer);

		long getFrom(Object buffer, int index);

		long getTo(Object buffer, int index);

		long setFrom(Object buffer, int index, long value);

		long setTo(Object buffer, int index, long value);

		void read(Object target, ByteBuffer buffer, int offset, int length);

		void write(Object source, ByteBuffer buffer, int offset, int length);

		int findSorted(Object source, int from, int to, long value);

		int find(Object source, int from, int to, long value);

		/**
		 * Returns the byte size of a single entry. This information
		 * is used to create {@code ByteBuffer} objects of adequate size
		 * for reading and writing.
		 * @return
		 */
		int chunkSize();
	}

	public static SpanAdapter createShortSpanAdapter() {
		return new ShortSpanAdapter();
	}

	public static SpanAdapter createIntSpanAdapter() {
		return new IntSpanAdapter();
	}

	public static SpanAdapter createLongSpanAdapter() {
		return new LongSpanAdapter();
	}

	public static SpanAdapter createSpanAdapter(IndexValueType indexValueType) {
		requireNonNull(indexValueType);

		switch (indexValueType) {
		case BYTE:
		case SHORT:
			return createShortSpanAdapter();

		case INTEGER:
			return createIntSpanAdapter();

		case LONG:
			return createLongSpanAdapter();

		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @deprecated
	 */
	@Deprecated
	private static class ShortSpanAdapter implements SpanAdapter {

		// from | to
		private static final int BYTES_PER_ENTRY = SHORT_BYTES + SHORT_BYTES;

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#createBuffer(int)
		 */
		@Override
		public Object createBuffer(int byteCount) {
			int size = byteCount/SHORT_BYTES;
			short[] buffer = new short[size];
			Arrays.fill(buffer, (short)-1);
			return buffer;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#bufferSize(java.lang.Object)
		 */
		@Override
		public int bufferSize(Object buffer) {
			return ((short[])buffer).length>>1;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#getFrom(int)
		 */
		@Override
		public long getFrom(Object buffer, int index) {
			return ((short[])buffer)[index<<1];
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#getTo(int)
		 */
		@Override
		public long getTo(Object buffer, int index) {
			return ((short[])buffer)[(index<<1)+1];
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#setFrom(int, long)
		 */
		@Override
		public long setFrom(Object buffer, int index, long value) {
			short[] array = (short[]) buffer;
			int idx = index<<1;

			short oldValue = array[idx];
			array[idx] = (short) value;

			return oldValue;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#setTo(int, long)
		 */
		@Override
		public long setTo(Object buffer, int index, long value) {
			short[] array = (short[]) buffer;
			int idx = (index<<1) + 1;

			short oldValue = array[idx];
			array[idx] = (short) value;

			return oldValue;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#chunkSize()
		 */
		@Override
		public int chunkSize() {
			return BYTES_PER_ENTRY;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#read(java.lang.Object, java.nio.ByteBuffer, int, int)
		 */
		@Override
		public void read(Object target, ByteBuffer buffer, int offset,
				int length) {
			short[] array = (short[]) target;
			int index = offset<<1;
			while(length-->0) {
				array[index++] = buffer.getShort();
				array[index++] = buffer.getShort();
			}
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#write(java.lang.Object, java.nio.ByteBuffer, int, int)
		 */
		@Override
		public void write(Object source, ByteBuffer buffer, int offset,
				int length) {
			short[] array = (short[]) source;
			int index = offset<<1;
			while(length-->0) {
				buffer.putShort(array[index++]);
				buffer.putShort(array[index++]);
			}
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#findSorted(java.lang.Object, int, int, long)
		 */
		@Override
		public int findSorted(Object source, int from, int to, long value) {
			short[] array = (short[]) source;

			short index = (short) value;

	        int low = from;
	        int high = to;

	        while (low <= high) {
	            int mid = (low + high) >>> 1;

	            if (array[mid<<1]>index)
	            	// Continue on left area
	            	high = mid - 1;
	            else if (array[(mid<<1)+1]<index)
	            	// Continue on right area
	            	low = mid + 1;
	            else
	                return mid; // span found
	        }

	        return NOT_FOUND;  // span not found.
		}

		@Override
		public int find(Object source, int from, int to, long value) {
			short[] array = (short[]) source;

			short index = (short) value;

	        for(int i=from; i<=to; i++) {
	        	int pos = i<<1;
	        	if(array[pos]<=index && array[pos+1]>=index) {
	        		return i;
	        	}
	        }

	        return NOT_FOUND;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#getValueType()
		 */
		@Override
		public IndexValueType getValueType() {
			return IndexValueType.SHORT;
		}

	}

	/**
	 *
	 * @author Markus Gärtner
	 * @deprecated
	 */
	@Deprecated
	private static class IntSpanAdapter implements SpanAdapter {

		// from | to
		private static final int BYTES_PER_ENTRY = INT_BYTES + INT_BYTES;

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#createBuffer(int)
		 */
		@Override
		public Object createBuffer(int byteCount) {
			int size = byteCount/INT_BYTES;
			int[] buffer = new int[size];
			Arrays.fill(buffer, -1);
			return buffer;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#bufferSize(java.lang.Object)
		 */
		@Override
		public int bufferSize(Object buffer) {
			return ((int[])buffer).length>>1;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#getFrom(int)
		 */
		@Override
		public long getFrom(Object buffer, int index) {
			return ((int[])buffer)[index<<1];
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#getTo(int)
		 */
		@Override
		public long getTo(Object buffer, int index) {
			return ((int[])buffer)[(index<<1)+1];
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#setFrom(int, long)
		 */
		@Override
		public long setFrom(Object buffer, int index, long value) {
			int[] array = (int[]) buffer;
			int idx = index<<1;

			int oldValue = array[idx];
			array[idx] = (int) value;

			return oldValue;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#setTo(int, long)
		 */
		@Override
		public long setTo(Object buffer, int index, long value) {
			int[] array = (int[]) buffer;
			int idx = (index<<1) + 1;

			int oldValue = array[idx];
			array[idx] = (int) value;

			return oldValue;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#chunkSize()
		 */
		@Override
		public int chunkSize() {
			return BYTES_PER_ENTRY;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#read(java.lang.Object, java.nio.ByteBuffer, int, int)
		 */
		@Override
		public void read(Object target, ByteBuffer buffer, int offset,
				int length) {
			int[] array = (int[]) target;
			int index = offset<<1;
			while(length-->0) {
				array[index++] = buffer.getInt();
				array[index++] = buffer.getInt();
			}
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#write(java.lang.Object, java.nio.ByteBuffer, int, int)
		 */
		@Override
		public void write(Object source, ByteBuffer buffer, int offset,
				int length) {
			int[] array = (int[]) source;
			int index = offset<<1;
			while(length-->0) {
				buffer.putInt(array[index++]);
				buffer.putInt(array[index++]);
			}
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#findSorted(java.lang.Object, int, int, long)
		 */
		@Override
		public int findSorted(Object source, int from, int to, long value) {
			int[] array = (int[]) source;

			int index = (int) value;

	        int low = from;
	        int high = to;

	        while (low <= high) {
	            int mid = (low + high) >>> 1;

	            if (array[mid<<1]>index)
	            	// Continue on left area
	            	high = mid - 1;
	            else if (array[(mid<<1)+1]<index)
	            	// Continue on right area
	            	low = mid + 1;
	            else
	                return mid; // span found
	        }

	        return NOT_FOUND;  // span not found.
		}

		@Override
		public int find(Object source, int from, int to, long value) {
			int[] array = (int[]) source;

			int index = (int) value;

	        for(int i=from; i<=to; i++) {
	        	int pos = i<<1;
	        	if(array[pos]<=index && array[pos+1]>=index) {
	        		return i;
	        	}
	        }

	        return NOT_FOUND;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#getValueType()
		 */
		@Override
		public IndexValueType getValueType() {
			return IndexValueType.INTEGER;
		}

	}

	/**
	 *
	 * @author Markus Gärtner
	 * @deprecated
	 */
	@Deprecated
	private static class LongSpanAdapter implements SpanAdapter {

		// from | to
		private static final int BYTES_PER_ENTRY = LONG_BYTES + LONG_BYTES;

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#createBuffer(int)
		 */
		@Override
		public Object createBuffer(int byteCount) {
			int size = byteCount/LONG_BYTES;
			long[] buffer = new long[size];
			Arrays.fill(buffer, -1);
			return buffer;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#bufferSize(java.lang.Object)
		 */
		@Override
		public int bufferSize(Object buffer) {
			return ((long[])buffer).length>>1;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#getFrom(int)
		 */
		@Override
		public long getFrom(Object buffer, int index) {
			return ((long[])buffer)[index<<1];
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#getTo(int)
		 */
		@Override
		public long getTo(Object buffer, int index) {
			return ((long[])buffer)[(index<<1)+1];
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#setFrom(int, long)
		 */
		@Override
		public long setFrom(Object buffer, int index, long value) {
			long[] array = (long[]) buffer;
			int idx = index<<1;

			long oldValue = array[idx];
			array[idx] = value;

			return oldValue;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#setTo(int, long)
		 */
		@Override
		public long setTo(Object buffer, int index, long value) {
			long[] array = (long[]) buffer;
			int idx = (index<<1) + 1;

			long oldValue = array[idx];
			array[idx] = value;

			return oldValue;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#chunkSize()
		 */
		@Override
		public int chunkSize() {
			return BYTES_PER_ENTRY;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#read(java.lang.Object, java.nio.ByteBuffer, int, int)
		 */
		@Override
		public void read(Object target, ByteBuffer buffer, int offset,
				int length) {
			long[] array = (long[]) target;
			int index = offset<<1;
			while(length-->0) {
				array[index++] = buffer.getLong();
				array[index++] = buffer.getLong();
			}
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#write(java.lang.Object, java.nio.ByteBuffer, int, int)
		 */
		@Override
		public void write(Object source, ByteBuffer buffer, int offset,
				int length) {
			long[] array = (long[]) source;
			int index = offset<<1;
			while(length-->0) {
				buffer.putLong(array[index++]);
				buffer.putLong(array[index++]);
			}
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#findSorted(java.lang.Object, int, int, long)
		 */
		@Override
		public int findSorted(Object source, int from, int to, long value) {
			long[] array = (long[]) source;

			long index = value;

	        int low = from;
	        int high = to;

	        while (low <= high) {
	            int mid = (low + high) >>> 1;

	            if (array[mid<<1]>index)
	            	// Continue on left area
	            	high = mid - 1;
	            else if (array[(mid<<1)+1]<index)
	            	// Continue on right area
	            	low = mid + 1;
	            else
	                return mid; // span found
	        }

	        return NOT_FOUND;  // span not found.
		}

		@Override
		public int find(Object source, int from, int to, long value) {
			long[] array = (long[]) source;

			long index = value;

	        for(int i=from; i<=to; i++) {
	        	int pos = i<<1;
	        	if(array[pos]<=index && array[pos+1]>=index) {
	        		return i;
	        	}
	        }

	        return NOT_FOUND;
		}

		/**
		 * @see de.ims.icarus2.filedriver.mapping.SpanArrays.SpanAdapter#getValueType()
		 */
		@Override
		public IndexValueType getValueType() {
			return IndexValueType.LONG;
		}
	}
}
