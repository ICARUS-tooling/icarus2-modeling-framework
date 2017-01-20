/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
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
