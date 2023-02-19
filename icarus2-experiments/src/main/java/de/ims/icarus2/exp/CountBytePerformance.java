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
package de.ims.icarus2.exp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

import de.ims.icarus2.util.nio.ByteCharMapper;

/**
 * @author Markus Gärtner
 *
 */
@SuppressWarnings("deprecation")
public class CountBytePerformance {

	public static void main(String[] args) throws IOException {
		Path file_ISO = Paths.get("D:","Temp","corpus-compress","split-dewac2-1");

		System.out.println("ISO file - recoding");
		for(int i=0; i<10; i++) {
			try(FileChannel channel = FileChannel.open(file_ISO)) {
				Duration duration = testRecode(channel, StandardCharsets.ISO_8859_1);

				System.out.println(duration);
			}
		}
		System.out.println();

		//RESULT: mapping about 10 times slower
		System.out.println("ISO file - mapping");
		for(int i=0; i<10; i++) {
			try(FileChannel channel = FileChannel.open(file_ISO)) {
				Duration duration = testByteTracking(channel, StandardCharsets.ISO_8859_1);

				System.out.println(duration);
			}
		}
	}

	private static Duration testByteTracking(SeekableByteChannel channel, Charset encoding) throws IOException {

		ByteCharMapper mapper = new ByteCharMapper(channel, encoding.newDecoder(), 2<<15);

		Instant begin = Instant.now();

		long bytesMapped = 0L;

		while(mapper.nextCharacter()!=-1) {
			bytesMapped += mapper.getByteCount();
		}

		System.out.println(bytesMapped+"b mapped");

		Instant end = Instant.now();

		return Duration.between(begin, end);
	}

	private static Duration testRecode(SeekableByteChannel channel, Charset encoding) throws IOException {

		CharsetEncoder encoder = encoding.newEncoder();
		ByteBuffer bb = ByteBuffer.allocate(1000);

		Instant begin = Instant.now();

		try(Reader reader = new BufferedReader(Channels.newReader(channel, encoding.newDecoder(), 2<<14))) {
			CharBuffer cb = CharBuffer.allocate(200);
			long bytesRecoded = 0L;

			while(reader.read(cb)>-1) {
				if(!cb.hasRemaining()) {
					cb.flip();
					encoder.encode(cb, bb, true);

					bb.flip();
					bytesRecoded += bb.remaining();

					encoder.reset();
					bb.clear();
					cb.clear();
				}
			}

			System.out.println(bytesRecoded+"b recoded");
		}

		Instant end = Instant.now();

		return Duration.between(begin, end);
	}
}
