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
package de.ims.icarus2.exp.snapshots;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
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
public class SubChannelPerformance_20161121 {

	public static void main(String[] args) throws IOException {
		new SubChannelPerformance_20161121().run();
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

		public TestResult(Object owner, long duration, long bytesread,
				double throughput, int buffersize, String info, @SuppressWarnings("rawtypes") Class...stack) {
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
			StringBuilder sb = new StringBuilder();

			if(owner!=null) {
				sb.append(owner);
			}

			sb.append("[");
			sb.append("throughput=").append(Math.floor(throughput*1000D/MB)).append("MB/s");

			sb.append(" duration=").append(duration);

			sb.append(" bytesread=").append(bytesread/MB).append("MB");

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
}
