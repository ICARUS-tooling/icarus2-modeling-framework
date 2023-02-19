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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
public class NIOTestUtil {


	/**
	 * Assert that the accessible content of {@code bb} matches the
	 * immediately accessible content of {@code channel}.
	 *
	 * @param channel
	 * @param bb
	 */
	public static void assertContentEquals(ReadableByteChannel channel, ByteBuffer bb) throws IOException {
		ByteBuffer tmp = ByteBuffer.allocate(bb.remaining());

		int bytesRead = channel.read(tmp);

		assertEquals(bb.remaining(), bytesRead);
		tmp.flip();

		assertContentEquals(bb, tmp);
	}

	public static void assertContentEquals(ByteBuffer expected, ByteBuffer actual) {
		assertEquals(expected.remaining(), actual.remaining());

		while(expected.hasRemaining()) {
			assertEquals(expected.get(), actual.get());
		}
	}

	public static void assertContentEquals(byte[] expected, ByteBuffer actual) {
		assertEquals(expected.length, actual.remaining());

		byte[] tmp = new byte[expected.length];
		actual.get(tmp);

		assertArrayEquals(expected, tmp);
	}

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

	public static ByteBuffer randomBuffer(RandomGenerator rand, int size) {
		byte[] data = new byte[size];
		rand.nextBytes(data);
		return ByteBuffer.wrap(data);
	}

	//TODO add methods for filling channels etc...
}
