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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import de.ims.icarus2.util.nio.SubChannel;

/**
 * @author Markus Gärtner
 *
 */
public class SubChannelPerformance {

	public static void main(String[] args) throws IOException {
		new SubChannelPerformance().run();
	}

	private static final int MB = 1024*1024;

	private static byte[] MB_BUFFER = new byte[1024*1024];
	static {
		Arrays.fill(MB_BUFFER, (byte)1);
	}

	private static final int[] BUFFER_SIZES = {
		1024,
		1024<<1,
		1024<<2,
		1024<<3, // DEFAULT_BUFFER_SIZE
		1024<<4,
		1024<<5, // L1 cache size
		1024<<6,
		MB,
		MB<<1,
		MB<<5,
	};

	Path FILE;

	void setup() throws IOException {
		FILE = Paths.get("tmp_file");
		if(!Files.exists(FILE)) {
			Files.createFile(FILE);
		}
	}

	long fillFile(int MB) throws IOException {

		long begin = System.currentTimeMillis();

		try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(
				FILE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE), 4096)) {
			while(MB-- > 0) {
				out.write(MB_BUFFER);
			}

			out.flush();
		}

		long end = System.currentTimeMillis();

		return end-begin;
	}

	@SuppressWarnings("boxing")
	void run() throws IOException {
		int[] fileSizesMB = {
//				1
//				,10
				 50
				,100
				,500
//				,1000
//				,5000
		};

		List<Test> tests = new ArrayList<>();
		Collections.addAll(tests, ReadTest.values());

		for(int fileSizeMB : fileSizesMB) {

			setup();
			try {
				long timeSpentWriting = fillFile(fileSizeMB);
				System.out.printf("\nRunning tests with file of size %d MB - time for writing: %d\n",
						fileSizeMB, timeSpentWriting);

				List<TestResult> results = new ArrayList<>();

				for(Test test : tests) {
					test.run(FILE, results::add);
				}

				Collections.sort(results);

				for(TestResult result : results) {
					System.out.println(result);
				}

			} finally {
				cleanup();
			}
		}
	}

	void cleanup() throws IOException {

		Files.deleteIfExists(FILE);
	}

	static class TestResult implements Comparable<TestResult> {
		final Object owner;
		final long duration;
		final long bytesread;
		final double throughput;
		@SuppressWarnings("rawtypes")
		final Class[] stack;
		final String info;
		final int buffersize;

		@SuppressWarnings("rawtypes")
		public TestResult(Object owner, long duration, long bytesread,
				double throughput, int buffersize, String info, Class...stack) {
			this.owner = owner;
			this.duration = duration;
			this.bytesread = bytesread;
			this.throughput = throughput;
			this.buffersize = buffersize;
			this.info = info;
			this.stack = stack;
		}

		@Override
		public int compareTo(TestResult o) {
			return -Double.compare(throughput, o.throughput);
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("[");

			sb.append("throughput=").append(Math.floor(throughput*1000D/MB)).append("MB/s");

			sb.append(" duration=").append(duration);

			sb.append(" bytesread=").append(bytesread/MB).append("MB");

			if(owner!=null) {
				sb.append(' ').append(owner);
			}

			sb.append(" stack=");
			for(int i=0; i<stack.length; i++) {
				sb.append(stack[i].getSimpleName());
				if(i<stack.length-1) {
					sb.append(">>");
				}
			}

			sb.append(" buffersize=").append(buffersize);

			if(info!=null) {
				sb.append(" info=").append(info);
			}

			sb.append("]");

			return sb.toString();
		}
	}

	interface Test {
		void run(Path file, Consumer<TestResult> action) throws IOException;
	}

	enum ReadTest implements Test {
		CHANNEL_UNBUFFERED(null, SeekableByteChannel.class, SubChannel.class, ByteBuffer.class) {

			@Override
			public void run(Path file, Consumer<TestResult> action) throws IOException {

				try(SeekableByteChannel channel = Files.newByteChannel(file)) {
					SubChannel subChannel = new SubChannel();
					subChannel.setSource(channel);

					int chunkCount = 100;
					long size = Files.size(file);
					long chunkSize = size/100;

					int iterations = 50;

					Random random = new Random(System.currentTimeMillis());

					for(int buffersize : BUFFER_SIZES) {
						ByteBuffer bb = ByteBuffer.allocateDirect(buffersize);

						begin();

						for(int i=0; i<iterations; i++) {

							int slot = random.nextInt(chunkCount);
							long begin = slot*chunkSize;
							long end = Math.min(begin+chunkSize, size-1);

							subChannel.setOffsets(begin, end);

							bytesprocessed += readChannel(subChannel, bb);
						}

						end();

						action.accept(new TestResult(this, duration(), bytesprocessed, throughput(), buffersize, info, stack));
					}
				}
			}

		},

		;

		protected long bytesprocessed;

		private long begin;
		private long end;

		@SuppressWarnings("rawtypes")
		protected final Class[] stack;
		protected final String info;

		@SuppressWarnings("rawtypes")
		private ReadTest(String info, Class...stack) {
			this.info = info;
			this.stack = stack;
		}

		void begin() {
			bytesprocessed = 0;
			begin = System.currentTimeMillis();
		}

		void end() {
			end = System.currentTimeMillis();
		}

		long duration() {
			return end-begin;
		}

		/**
		 * Returns throughput as bytes per millisecond
		 */
		double throughput() {
			return (double)bytesprocessed/ (double)duration();
		}
	}

	private static int readChannel(ReadableByteChannel channel, ByteBuffer bb) throws IOException {
		int count = 0;

		for(;;) {

			int read = channel.read(bb);

			if(read == -1) {
				break;
			}

			count += read;
			bb.clear();
		}

		return count;
	}

	private static int readChannel(ReadableByteChannel channel, CharsetDecoder decoder, ByteBuffer bb, CharBuffer cb) throws IOException {
		int count = 0;

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
	        if(CoderResult.OVERFLOW == res) {
	        	cb.flip();
	        	count += cb.remaining();
	            cb.clear();
	        } else if (CoderResult.UNDERFLOW == res) {
	            bb.compact();
	        }
	    }

	    return count;
	}
}
