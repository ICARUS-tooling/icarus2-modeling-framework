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
package de.ims.icarus2.util.nio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.ims.icarus2.test.TestUtils;

/**
 * @author Markus Gärtner
 *
 */
public class FastChannelReaderTest {

	@SuppressWarnings("boxing")
	static Stream<Arguments> data() {
		return Stream.of(
	    		Arguments.of(StandardCharsets.US_ASCII, 32, true, TestUtils.LOREM_IPSUM_ASCII),
	    		Arguments.of(StandardCharsets.US_ASCII, 1024, true, TestUtils.LOREM_IPSUM_ASCII),
	    		Arguments.of(StandardCharsets.US_ASCII, 32, false, TestUtils.LOREM_IPSUM_ASCII),
	    		Arguments.of(StandardCharsets.US_ASCII, 1024, false, TestUtils.LOREM_IPSUM_ASCII),

	    		Arguments.of(StandardCharsets.ISO_8859_1, 32, true, TestUtils.LOREM_IPSUM_ISO),
	    		Arguments.of(StandardCharsets.ISO_8859_1, 1024, true, TestUtils.LOREM_IPSUM_ISO),
	    		Arguments.of(StandardCharsets.ISO_8859_1, 32, false, TestUtils.LOREM_IPSUM_ISO),
	    		Arguments.of(StandardCharsets.ISO_8859_1, 1024, false, TestUtils.LOREM_IPSUM_ISO),

	    		Arguments.of(StandardCharsets.UTF_8, 32, true, TestUtils.LOREM_IPSUM_CHINESE),
	    		Arguments.of(StandardCharsets.UTF_8, 1024, true, TestUtils.LOREM_IPSUM_CHINESE),
	    		Arguments.of(StandardCharsets.UTF_8, 32, false, TestUtils.LOREM_IPSUM_CHINESE),
	    		Arguments.of(StandardCharsets.UTF_8, 1024, false, TestUtils.LOREM_IPSUM_CHINESE)
		);
	}


	private FastChannelReader prepare(Charset encoding, int bufferSize, boolean allocateDirect, String text) throws IOException {
		ByteArrayChannel channel = new ByteArrayChannel(new byte[text.length()*4], false);

		try(Writer writer = Channels.newWriter(channel, encoding.newEncoder(), -1)) {
			writer.write(text);
		}
		channel.flip();

		return new FastChannelReader(channel, encoding.newDecoder(), bufferSize, allocateDirect);
	}

	@ParameterizedTest(name="run #{index}: encoding= {0} buffer={1} allocateDirect= {2}")
	@MethodSource("data")
	public void testRead(Charset encoding, int bufferSize, boolean allocateDirect, String text) throws Exception {

		try(FastChannelReader reader = prepare(encoding, bufferSize, allocateDirect, text)) {
			assertTrue(reader.ready());

			char[] tmp = new char[text.length()*2];
			int charsRead = reader.read(tmp);
			assertEquals(text.length(), charsRead);

			String readString = new String(tmp, 0, charsRead);

			assertEquals(text, readString);
		}
	}
}
