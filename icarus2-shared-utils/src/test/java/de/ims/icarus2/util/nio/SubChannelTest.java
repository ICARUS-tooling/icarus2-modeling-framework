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
/**
 *
 */
package de.ims.icarus2.util.nio;

import static de.ims.icarus2.util.nio.NIOTestUtil.assertContentEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
public class SubChannelTest {

	private SeekableByteChannel channel;
	private SubChannel subChannel;
	private byte[] data;

	private static final int SIZE = 100;

	@SuppressWarnings("resource")
	@BeforeEach
	public void prepare() throws IOException {
		data = new byte[SIZE];
		channel = new ByteArrayChannel(data, false);

		subChannel = new SubChannel(channel).setOffsets(0, channel.size());
	}

	@AfterEach
	public void cleanup() throws IOException {
		subChannel.close();
		channel.close();
	}

	@Test
	public void testInit() throws Exception {
		assertSame(channel, subChannel.getSource());
		assertEquals(SIZE, subChannel.size());
		assertEquals(0, subChannel.position());
	}

	@Test
	@RandomizedTest
	public void testWriteReadOrigin(RandomGenerator rand) throws Exception {

		int size = 20;

		ByteBuffer bb = NIOTestUtil.randomBuffer(rand, size);
		int bytesWritten = subChannel.write(bb);

		assertEquals(size, bytesWritten);
		assertEquals(size, subChannel.position());

		subChannel.position(0);
		bb.rewind();

		assertContentEquals(subChannel, bb);
	}

	@Test
	@RandomizedTest
	public void testWriteReadSection(RandomGenerator rand) throws Exception {

		int size = 20;
		int pos = 30;

		subChannel.setOffsets(pos, size);

		ByteBuffer bb = NIOTestUtil.randomBuffer(rand, size);
		int bytesWritten = subChannel.write(bb);

		assertEquals(size, bytesWritten);
		assertEquals(size, subChannel.position());

		subChannel.position(0);
		bb.rewind();

		assertContentEquals(subChannel, bb);
	}

	@Test
	@RandomizedTest
	public void testEmpty(RandomGenerator rand) throws Exception {
		subChannel.setOffsets(20, 0);
		assertEquals(0, subChannel.size());

		ByteBuffer bb = NIOTestUtil.randomBuffer(rand, 10);
		int bytesWritten = subChannel.write(bb);
		assertTrue(bytesWritten<=0);

		bb.rewind();
		int bytesRead = subChannel.read(bb);
		assertTrue(bytesRead<=0);
	}

	@Test
	public void testPosition() throws Exception {
		int pos = 5;

		subChannel.position(pos);
		assertEquals(pos, subChannel.position());
	}

	@Test
	public void testClose() throws Exception {
		subChannel.close();
		assertFalse(subChannel.isOpen());
	}

	@Test
	public void testClosed() throws Exception {
		assertThrows(ClosedChannelException.class, () -> {
			subChannel.close();
			subChannel.write(NIOUtil.emptyBuffer());
		});
	}
}
