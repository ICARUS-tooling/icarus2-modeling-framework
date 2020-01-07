/*
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
/**
 *
 */
package de.ims.icarus2.exp.io;

import static de.ims.icarus2.test.TestUtils.LOREM_IPSUM_CHINESE;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;

import de.ims.icarus2.test.JmhUtils;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.io.IOUtil;

/**
 * @author Markus Gärtner
 *
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@Warmup(iterations=5, batchSize=10)
@Measurement(iterations=5, batchSize=10)
@Fork(value=5, jvmArgsAppend={"-Xms3g"})
public class ReadBenchmark {

	// Benchmark parameters

	@Param({"10","100","500","1000"})
	private int sizeInMb;

	/*
	 * Default buffer size: 8192
	 * L1 cache: 32768
	 */
	@Param({"1024","2048","4096",
			"8192", // default buffer size of java.io
			"16384",
			"32768", // typical L1 cache
			"65536","131072",
			"1048576", // 1MB
			"4194304", // 4MB
			"16777216", // 16MB
			"33554432" // 32MB
			})
	private int bufferSize;

	@Param({"UTF-8", "ISO-8859-1", "US-ASCII"})
	private String charset;

	// Utilities

	/** Temporary file to read from */
	private Path file;

	@Setup(Level.Trial)
	public void setupFile() throws IOException {
		file = Files.createTempFile(getClass().getSimpleName(), "_tmp");

		fillFile(file, Charset.forName(charset), sizeInMb);

//		System.out.println(file);
	}

	@TearDown(Level.Trial)
	public void cleanupFile() throws IOException {
		Files.deleteIfExists(file);
	}

	/**
	 * Fills the specified file with {@code n} megabytes of random (repeated)
	 * data chunks according to the {@code MB} parameter.
	 */
	private static void fillFile(Path file, Charset charset, int MB) throws IOException {

		byte[] repeatableContent = createRandomData(charset, IOUtil.MB);
		assumeTrue(repeatableContent.length>0, "Empty random data");
		int remainingSize = IOUtil.MB * MB;

		try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(
				file, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE),
				IOUtil.DEFAULT_BUFFER_SIZE)) {
			while(remainingSize > 0) {
				out.write(repeatableContent);
				remainingSize -= repeatableContent.length;
			}

			out.flush();
		}
	}

	private static byte[] createRandomData(Charset charset, int size) throws CharacterCodingException {
		CharsetEncoder encoder = charset.newEncoder();
		RandomGenerator rand = RandomGenerator.random();

		CharBuffer cb;
		if(encoder.maxBytesPerChar()>1) {
			// For UNICODE charsets we try to use complex dummy data
			cb = CharBuffer.allocate(size);
			String tmp = LOREM_IPSUM_CHINESE;
			while(cb.remaining()>=tmp.length()) {
				cb.put(tmp);
			}
			// Pad the remaining space with random alphanumeric strings
			if(cb.hasRemaining()) {
				cb.put(rand.randomString(cb.remaining()));
			}
			cb.flip();
		} else {
			// ASCII style -> simply throw a long random alphanumerical string at it
			cb = CharBuffer.wrap(rand.randomString(size));
		}


		return encoder.encode(cb).array();
	}

	/**
	 * Reads a channel and decodes it using the given {@link CharsetDecoder}.
	 * @param channel
	 * @param decoder
	 * @param bb
	 * @param cb
	 * @return
	 * @throws IOException
	 */
	private static long readChannel(ReadableByteChannel channel, CharsetDecoder decoder,
			ByteBuffer bb, CharBuffer cb) throws IOException {
		long count = 0;

	    for(;;) {
	        if(-1 == channel.read(bb)) {
	            decoder.decode(bb, cb, true);
	            decoder.flush(cb);
	        	cb.flip();
	        	count += cb.remaining();
	            break;
	        }
	        bb.flip();

	        CoderResult res = decoder.decode(bb, cb, false);
	        if(res.isError())
	        	res.throwException();

	        if(res.isUnderflow()) {
	        	bb.compact();
	        }

        	cb.flip();
        	count += cb.remaining();
            cb.clear();
	    }

	    return count;
	}

	// Benchmark methods

	@Benchmark
	public void bytes_ChannelInputStream(Blackhole bh) throws IOException {
		try(InputStream is = Files.newInputStream(file)) {
			byte[] bb = new byte[IOUtil.MB];
			int read;
			while((read = is.read(bb))>0) {
				bh.consume(read);
			}
		}
	}

	@Benchmark
	public void bytes_BufferedInputStream_ChannelInputStream(Blackhole bh) throws IOException {
		try(BufferedInputStream is = new BufferedInputStream(Files.newInputStream(file), bufferSize)) {
			byte[] bb = new byte[IOUtil.MB];
			int read;
			while((read = is.read(bb))>0) {
				bh.consume(read);
			}
		}
	}

	@Benchmark
	public void bytes_FileInputStream(Blackhole bh) throws FileNotFoundException, IOException {
		try(FileInputStream is = new FileInputStream(file.toFile())) {
			byte[] bb = new byte[IOUtil.MB];
			int read;
			while((read = is.read(bb))>0) {
				bh.consume(read);
			}
		}
	}

	@Benchmark
	public void bytes_BufferedInputStream_FileInputStream(Blackhole bh) throws IOException {
		try(BufferedInputStream is = new BufferedInputStream(new FileInputStream(file.toFile()), bufferSize)) {
			byte[] bb = new byte[IOUtil.MB];
			int read;
			while((read = is.read(bb))>0) {
				bh.consume(read);
			}
		}
	}

	@Benchmark
	public void bytes_ByteBuffer_ReadableByteChannel(Blackhole bh) throws IOException {
		try(SeekableByteChannel channel = Files.newByteChannel(file)) {
			ByteBuffer bb = ByteBuffer.allocate(bufferSize);
			while(channel.read(bb)>0) {
				bb.flip();
				bh.consume(bb.remaining());
				bb.clear();
			}
		}
	}

	@Benchmark
	public void bytes_directByteBuffer_ReadableByteChannel(Blackhole bh) throws IOException {
		try(SeekableByteChannel channel = Files.newByteChannel(file)) {
			ByteBuffer bb = ByteBuffer.allocateDirect(bufferSize);
			while(channel.read(bb)>0) {
				bb.flip();
				bh.consume(bb.remaining());
				bb.clear();
			}
		}
	}

	@Benchmark
	public void chars_InputStreamReader_ChannelInputStream(Blackhole bh) throws UnsupportedEncodingException, IOException {
		try(InputStreamReader reader = new InputStreamReader(Files.newInputStream(file), charset)) {
			char[] bb = new char[IOUtil.MB];
			int read;
			while((read = reader.read(bb))>0) {
				bh.consume(read);
			}
		}
	}

	@Benchmark
	public void chars_BufferedReader_InputStreamReader_ChannelInputStream(Blackhole bh) throws UnsupportedEncodingException, IOException {
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(
				Files.newInputStream(file), charset), bufferSize)) {
			char[] cb = new char[IOUtil.MB];
			int read;
			while((read = reader.read(cb))>0) {
				bh.consume(read);
			}
		}
	}

	@Benchmark
	public void chars_InputStreamReader_FileInputStream(Blackhole bh) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		try(InputStreamReader reader = new InputStreamReader(new FileInputStream(file.toFile()), charset)) {
			char[] cb = new char[IOUtil.MB];
			int read;
			while((read = reader.read(cb))>0) {
				bh.consume(read);
			}
		}
	}

	@Benchmark
	public void chars_BufferedReader_InputStreamReader_FileInputStream(Blackhole bh) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(file.toFile()), charset), bufferSize)) {
			char[] cb = new char[IOUtil.MB];
			int read;
			while((read = reader.read(cb))>0) {
				bh.consume(read);
			}
		}
	}

	@Benchmark
	public void chars_CharsetDecoder_ByteBuffer_SeekableByteChannel(Blackhole bh) throws IOException {
		try(SeekableByteChannel channel = Files.newByteChannel(file)) {
		    CharsetDecoder decoder = Charset.forName(charset).newDecoder();
			ByteBuffer bb = ByteBuffer.allocate(bufferSize);
			CharBuffer cb = CharBuffer.allocate(bufferSize);
			long read = readChannel(channel, decoder, bb, cb);
			bh.consume(read);
		}
	}

	@Benchmark
	public void chars_CharsetDecoder_directByteBuffer_SeekableByteChannel(Blackhole bh) throws IOException {
		try(SeekableByteChannel channel = Files.newByteChannel(file)) {
		    CharsetDecoder decoder = Charset.forName(charset).newDecoder();
			ByteBuffer bb = ByteBuffer.allocateDirect(bufferSize);
			CharBuffer cb = CharBuffer.allocate(bufferSize);
			long read = readChannel(channel, decoder, bb, cb);
			bh.consume(read);
		}
	}


	/**
	 * ReadBenchmark -o ReadBenchmark.log -rf csv -rff ReadBenchmark.csv -f 1 -p sizeInMb=10 -p charset=UTF-8 -p bufferSize=8192
	 *
	 *
	 * @param args
	 * @throws RunnerException
	 */
	public static void main(String[] args) throws RunnerException {
		ChainedOptionsBuilder builder = JmhUtils.jmhOptions(ReadBenchmark.class,
				false, ResultFormatType.CSV);

		builder.param("sizeInMb", "10", "30")
			.param("bufferSize", "32768")
//			.param("charset", "UTF-8")
			.forks(0);

		new Runner(builder.build()).run();
	}
}
