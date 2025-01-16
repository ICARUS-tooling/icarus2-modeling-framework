/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
class ByteChannelCharacterSequenceTest {

	/**
	 * Test method for {@link de.ims.icarus2.util.nio.ByteChannelCharacterSequence#ByteChannelCharacterSequence(java.nio.channels.SeekableByteChannel)}.
	 * @throws IOException
	 */
	@Test
	void testByteChannelCharacterSequence() throws IOException {
		try(SeekableByteChannel channel = mock(SeekableByteChannel.class)) {
			ByteChannelCharacterSequence sequence = new ByteChannelCharacterSequence(channel);
			assertSame(channel, sequence.getChannel());

			doReturn(Long.valueOf(Long.MAX_VALUE)).when(channel).size();
			assertThrows(IcarusRuntimeException.class, () -> new ByteChannelCharacterSequence(channel));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.nio.ByteChannelCharacterSequence#length()}.
	 * @throws IOException
	 */
	@Test
	void testLength() throws IOException {
		try(SeekableByteChannel channel = mock(SeekableByteChannel.class)) {
			doReturn(Long.valueOf(0)).when(channel).size();

			ByteChannelCharacterSequence sequence = new ByteChannelCharacterSequence(channel);
			assertEquals(0, sequence.length());

			for(long size : new long[]{1, 4, 12, 99, 1024, IcarusUtils.MAX_INTEGER_INDEX}) {
				doReturn(Long.valueOf(size*2)).when(channel).size();
				ByteChannelCharacterSequence seq = new ByteChannelCharacterSequence(channel);
				assertEquals(size, seq.length());
			}
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.nio.ByteChannelCharacterSequence#charAt(int)}.
	 * @throws IOException
	 */
	@Test
	void testCharAt() throws IOException {
		String payload = "this is a test!";
		try(SeekableByteChannel channel = ByteArrayChannel.fromChars(payload)) {
			ByteChannelCharacterSequence sequence = new ByteChannelCharacterSequence(channel);

			for(int i=payload.length()-1; i>0; i--) {
				assertEquals(payload.charAt(i), sequence.charAt(i));
			}
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.nio.ByteChannelCharacterSequence#subSequence(int, int)}.
	 * @throws IOException
	 */
	@Test
	void testSubSequence() throws IOException {
		String payload = "this is a test!";
		try(SeekableByteChannel channel = ByteArrayChannel.fromChars(payload)) {

			ByteChannelCharacterSequence seq0 = new ByteChannelCharacterSequence(channel);

			assertEquals(payload.length(), seq0.length());
			assertEquals(payload, seq0.toString());

			CharSequence seq_0_4 = seq0.subSequence(0, 4);
			assertEquals("this", seq_0_4.toString());

			CharSequence seq_5_9 = seq0.subSequence(5, 9);
			assertEquals("is a", seq_5_9.toString());

			CharSequence seq_sub = seq_5_9.subSequence(3, 4);
			assertEquals("a", seq_sub.toString());
		}
	}

}
