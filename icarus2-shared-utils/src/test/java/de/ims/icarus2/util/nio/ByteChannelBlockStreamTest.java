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
/**
 *
 */
package de.ims.icarus2.util.nio;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author Markus Gärtner
 *
 */
public class ByteChannelBlockStreamTest {

	@SuppressWarnings("boxing")
	static Stream<Arguments> data() {
		return Stream.of(
				Arguments.of(32, true),
				Arguments.of(1024, true),
				Arguments.of(32, false),
				Arguments.of(1024, false)
		);
	}

	private final static int SIZE = 100;

	@ParameterizedTest(name="run #{index}: buffer={0} allocateDirect={1}")
	@MethodSource("data")
	public void testFull(int bufferSize, boolean allocateDirect) throws Exception {
		byte[] data = new byte[SIZE];
		NIOUtil.randomize(data);

		try(ByteArrayChannel channel = new ByteArrayChannel(data);
				ByteChannelBlockStream stream = new ByteChannelBlockStream(channel, bufferSize, allocateDirect)) {
			stream.reload(0, SIZE);

			byte[] tmp = new byte[20];
			int offset = 0;
			int bytesRead;
			while((bytesRead=stream.read(tmp))>0) {
				byte[] expected = Arrays.copyOfRange(data, offset, offset+bytesRead);
				byte[] actual = tmp;
				if(bytesRead<tmp.length) {
					actual = Arrays.copyOf(tmp, bytesRead);
				}

				assertArrayEquals(expected, actual);

				offset += bytesRead;
			}

			assertEquals(SIZE, offset);
			assertEquals(0, stream.available());
		}
	}

	@ParameterizedTest(name="run #{index}: buffer={0} allocateDirect={1}")
	@MethodSource("data")
	public void testParts(int bufferSize, boolean allocateDirect) throws Exception {
		byte[] data = new byte[SIZE];
		NIOUtil.randomize(data);

		try(ByteArrayChannel channel = new ByteArrayChannel(data);
				ByteChannelBlockStream stream = new ByteChannelBlockStream(channel, bufferSize, allocateDirect)) {
			stream.reload(0, SIZE);

			byte[] tmp = new byte[20];

			int size = 17;

			for(int i=1; i<5; i++) {
				int offset = i*12;
				stream.reload(offset, size);
				int bytesRead;
				while((bytesRead=stream.read(tmp))>0) {
					byte[] expected = Arrays.copyOfRange(data, offset, offset+bytesRead);
					byte[] actual = tmp;
					if(bytesRead<tmp.length) {
						actual = Arrays.copyOf(tmp, bytesRead);
					}

					assertArrayEquals(expected, actual);

					offset += bytesRead;
				}

				assertEquals(stream.position(), offset);
				assertEquals(0, stream.available());
			}
		}
	}
}
