/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.util.nio.NIOTestUtil.assertContentEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
public class ByteArrayChannelTest {

	private byte[] data;
	private ByteArrayChannel channel;

	private static final int SIZE = 100;

	@BeforeEach
	public void prepare() {
		data = new byte[SIZE];
		channel = new ByteArrayChannel(data, false);
	}

	@AfterEach
	public void cleanup() {
		channel.close();
		data = null;
	}

	@Test
	public void testReadOnly() throws Exception {
		Assertions.assertThrows(IOException.class, () -> {
			channel = new ByteArrayChannel(data);

			channel.write(NIOUtil.emptyBuffer());
		});
	}

	@Test
	@RandomizedTest
	public void testWrite(RandomGenerator rand) throws Exception {
		byte[] chunk = new byte[20];
		rand.nextBytes(chunk);
		ByteBuffer bb = ByteBuffer.wrap(chunk);

		int offset = 0;
		int bytesWritten;
		while((bytesWritten=channel.write(bb))>0) {
			bb.rewind();
			byte[] expected = Arrays.copyOfRange(data, offset, offset+bytesWritten);
			assertContentEquals(expected, bb);

			rand.nextBytes(chunk);
			offset += bytesWritten;
			bb.clear();
		}

		assertEquals(SIZE, offset);
	}

	@Test
	@RandomizedTest
	public void testRead(RandomGenerator rand) throws Exception {
		rand.nextBytes(data);

		ByteBuffer bb = ByteBuffer.allocate(20);
		int offset = 0;
		int bytesRead;
		while((bytesRead = channel.read(bb))>0) {
			bb.flip();
			byte[] expected = Arrays.copyOfRange(data, offset, offset+bytesRead);
			assertContentEquals(expected, bb);
			offset += bytesRead;
			bb.clear();
		}

		assertEquals(SIZE, offset);
	}

	@Test
	public void testPosition() throws Exception {
		int pos = 25;
		channel.position(pos);
		assertEquals(pos, channel.position());
	}

	@Test
	public void testFromChars() throws Exception {
		String in = "abcdefghijklmonp";
		try(ByteArrayChannel channel = ByteArrayChannel.fromChars(in)) {
			assertEquals(in.length()*2, channel.size());

			ByteChannelCharacterSequence sequence = new ByteChannelCharacterSequence(channel);
			assertEquals(in, sequence.toString());
		}
	}
}
