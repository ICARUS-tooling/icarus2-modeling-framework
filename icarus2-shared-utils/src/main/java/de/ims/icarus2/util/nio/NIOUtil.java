/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.nio;

import java.nio.ByteBuffer;

/**
 * @author Markus Gärtner
 *
 */
public class NIOUtil {

	private static final byte[] EMPTY_BYTES = {};

	private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(EMPTY_BYTES);

	private static final int MIN_BYTE_BUFFER_SIZE = 32;
	private static final int DEFAULT_BYTE_BUFFER_SIZE = 8192;

	/**
	 * Utility method for {@link FastChannelReader} and {@link ByteChannelBlockStream}
	 * to initialize internal buffers;
	 *
	 * @param mbc desired minimum buffer size or {@code -1} if an implementation
	 * specific minimum should be chosen.
	 * @param allocateDirect
	 * @return
	 */
	static final ByteBuffer allocate(int mbc, boolean allocateDirect) {
		int capacity = mbc < 0 ? DEFAULT_BYTE_BUFFER_SIZE
				: (mbc < MIN_BYTE_BUFFER_SIZE ? MIN_BYTE_BUFFER_SIZE : mbc);

		return allocateDirect ? ByteBuffer.allocateDirect(capacity)
				: ByteBuffer.allocate(capacity);
	}

	public static ByteBuffer emptyBuffer() {
		return EMPTY_BUFFER;
	}
}
