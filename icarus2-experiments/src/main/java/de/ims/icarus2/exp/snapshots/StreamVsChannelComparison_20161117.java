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
package de.ims.icarus2.exp.snapshots;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Markus Gärtner
 *
 */
public class StreamVsChannelComparison_20161117 {

	public static void main(String[] args) throws IOException {

		StreamVsChannelComparison_20161117 comparison = new StreamVsChannelComparison_20161117();

		comparison.run();
	}

	private static final int MB = 1024*1024;
//	private static final int GB = 1024*1024*1024;
	private static final int DEFAULT_BUFFER_SIZE = 8192;

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

	private static byte[] MB_BUFFER = new byte[1024*1024];
	static {
		Arrays.fill(MB_BUFFER, (byte)1);
	}

	private static final Charset[] CHARSETS = {
		StandardCharsets.UTF_8,
		StandardCharsets.ISO_8859_1,
		StandardCharsets.US_ASCII,
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

//	private static ReferenceQueue<Object> refQueue = new ReferenceQueue<>();
//	private static int remainingRefCount = 0;
//
//	private static void queueRefForGC(Object obj) {
//		new WeakReference<>(obj, refQueue);
//		remainingRefCount++;
//	}

	void cleanup() throws IOException {
//		while(remainingRefCount>0) {
//			System.gc();
//			Reference<?> ref;
//			while((ref=refQueue.poll())!=null) {
//				remainingRefCount--;
//			}
//		}

		Files.deleteIfExists(FILE);
	}

	interface Test {
		void run(Path file, Consumer<TestResult> resultAction) throws IOException;
	}

	private static class TestResult implements Comparable<TestResult> {

		private final Object owner;
		private final long duration;
		@SuppressWarnings("rawtypes")
		private final Class[] stack;
		private final int buffersize;
		private final Charset charset;
		private final String info;

		/**
		 * @param duration
		 * @param buffersize
		 * @param charset
		 * @param info
		 * @param stack
		 */
		public TestResult(Object owner, long duration, int buffersize, Charset charset,
				String info, @SuppressWarnings("rawtypes") Class...stack) {
			this.owner = owner;
			this.duration = duration;
			this.buffersize = buffersize;
			this.charset = charset;
			this.info = info;
			this.stack = stack;
		}

		/**
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(TestResult o) {
			return Long.compare(duration, o.duration);
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
			sb.append("duration=").append(duration);

			sb.append(" stack=");
			for(int i=0; i<stack.length; i++) {
				sb.append(stack[i].getSimpleName());
				if(i<stack.length-1) {
					sb.append(">>");
				}
			}

			sb.append(" buffersize=").append(buffersize);
			if(charset!=null) {
				sb.append(" charset=").append(charset.name());
			}

			if(info!=null) {
				sb.append(" info=").append(info);
			}

			sb.append("]");

			return sb.toString();
		}
	}

	private enum ReadTest implements Test {

		STREAM_UNBUFFERED("Files.newInputStream() -> ChannelInputStream", InputStream.class) {
			@Override
			public void run(Path file, Consumer<TestResult> action) throws IOException {

				begin();

				try(InputStream is = Files.newInputStream(file)) {
					byte[] bb = new byte[MB];
					int read;
					while((read = is.read(bb))>0) {
						dummy += read;
					}
				}

				end();

				action.accept(new TestResult(this, duration(), 0, null, info, stack));
			}
		},

		STREAM_BUFFERED("Files.newInputStream() -> ChannelInputStream", InputStream.class, BufferedInputStream.class) {
			@Override
			public void run(Path file, Consumer<TestResult> action) throws IOException {

				for(int buffersize : BUFFER_SIZES) {
					begin();

					try(InputStream is = new BufferedInputStream(Files.newInputStream(file), buffersize)) {
						byte[] bb = new byte[MB];
						int read;
						while((read = is.read(bb))>0) {
							dummy += read;
						}
					}

					end();

					action.accept(new TestResult(this, duration(), buffersize, null, info, stack));
				}
			}
		},

		FILESTREAM_UNBUFFERED(null, FileInputStream.class) {
			@Override
			public void run(Path file, Consumer<TestResult> action) throws IOException {

				begin();

				try(InputStream is = new FileInputStream(file.toFile())) {
					byte[] bb = new byte[MB];
					int read;
					while((read = is.read(bb))>0) {
						dummy += read;
					}
				}

				end();

				action.accept(new TestResult(this, duration(), 0, null, info, stack));
			}
		},

		FILESTREAM_BUFFERED(null, FileInputStream.class, BufferedInputStream.class) {
			@Override
			public void run(Path file, Consumer<TestResult> action) throws IOException {

				for(int buffersize : BUFFER_SIZES) {
					begin();

					try(InputStream is = new BufferedInputStream(new FileInputStream(file.toFile()), buffersize)) {
						byte[] bb = new byte[MB];
						int read;
						while((read = is.read(bb))>0) {
							dummy += read;
						}
					}

					end();

					action.accept(new TestResult(this, duration(), buffersize, null, info, stack));
				}
			}
		},

		BYTEBUFFER("Files.newByteChannel(), ByteBuffer.allocate()", ReadableByteChannel.class, ByteBuffer.class) {
			@Override
			public void run(Path file, Consumer<TestResult> action) throws IOException {

				for(int buffersize : BUFFER_SIZES) {
					begin();

					try(ReadableByteChannel channel = Files.newByteChannel(file)) {
						ByteBuffer bb = ByteBuffer.allocate(buffersize);
						while(channel.read(bb)>0) {
							bb.flip();
							dummy += bb.remaining();
							bb.clear();
						}
					}

					end();

					action.accept(new TestResult(this, duration(), buffersize, null, info, stack));
				}
			}
		},


		BYTEBUFFER_DIRECT("Files.newByteChannel(), ByteBuffer.allocateDirect()", ReadableByteChannel.class, ByteBuffer.class) {
			@Override
			public void run(Path file, Consumer<TestResult> action) throws IOException {

				for(int buffersize : BUFFER_SIZES) {
					begin();

					try(ReadableByteChannel channel = Files.newByteChannel(file)) {
						ByteBuffer bb = ByteBuffer.allocateDirect(buffersize);
						while(channel.read(bb)>0) {
							bb.flip();
							dummy += bb.remaining();
							bb.clear();
						}
					}

					end();

					action.accept(new TestResult(this, duration(), buffersize, null, info, stack));
				}
			}
		},

		READER_STREAM_UNBUFFERED("Files.newInputStream() -> ChannelInputStream", InputStream.class, InputStreamReader.class) {
			@Override
			public void run(Path file, Consumer<TestResult> action) throws IOException {

				for(Charset charset : CHARSETS) {
					begin();

					try(Reader reader = new InputStreamReader(Files.newInputStream(file), charset)) {
						char[] bb = new char[MB];
						int read;
						while((read = reader.read(bb))>0) {
							dummy += read;
						}
					}

					end();

					action.accept(new TestResult(this, duration(), 0, charset, info, stack));
				}
			}
		},

		READER_STREAM_BUFFERED("Files.newInputStream() -> ChannelInputStream", InputStream.class, BufferedInputStream.class, InputStreamReader.class) {
			@Override
			public void run(Path file, Consumer<TestResult> action) throws IOException {

				for(Charset charset : CHARSETS) {
					for(int buffersize : BUFFER_SIZES) {
						begin();

						try(Reader reader = new InputStreamReader(new BufferedInputStream(
								Files.newInputStream(file), buffersize), charset)) {
							char[] bb = new char[MB];
							int read;
							while((read = reader.read(bb))>0) {
								dummy += read;
							}
						}

						end();

						action.accept(new TestResult(this, duration(), buffersize, charset, info, stack));
					}
				}
			}
		},

		READER_BUFFERED_STREAM("Files.newInputStream() -> ChannelInputStream", InputStream.class, InputStreamReader.class, BufferedReader.class) {
			@Override
			public void run(Path file, Consumer<TestResult> action) throws IOException {

				for(Charset charset : CHARSETS) {
					for(int buffersize : BUFFER_SIZES) {
						begin();

						try(Reader reader = new BufferedReader(new InputStreamReader(
								Files.newInputStream(file), charset), buffersize)) {
							char[] bb = new char[MB];
							int read;
							while((read = reader.read(bb))>0) {
								dummy += read;
							}
						}

						end();

						action.accept(new TestResult(this, duration(), buffersize, charset, info, stack));
					}
				}
			}
		},

		READER_FILESTREAM_UNBUFFERED(null, FileInputStream.class, InputStreamReader.class) {
			@Override
			public void run(Path file, Consumer<TestResult> action) throws IOException {

				for(Charset charset : CHARSETS) {
					begin();

					try(Reader reader = new InputStreamReader(new FileInputStream(file.toFile()), charset)) {
						char[] bb = new char[MB];
						int read;
						while((read = reader.read(bb))>0) {
							dummy += read;
						}
					}

					end();

					action.accept(new TestResult(this, duration(), 0, charset, info, stack));
				}
			}
		},

		READER_BUFFERED_FILESTREAM(null, FileInputStream.class, InputStreamReader.class, BufferedReader.class) {
			@Override
			public void run(Path file, Consumer<TestResult> action) throws IOException {

				for(Charset charset : CHARSETS) {
					for(int buffersize : BUFFER_SIZES) {
						begin();

						try(Reader reader = new BufferedReader(new InputStreamReader(
								new FileInputStream(file.toFile()), charset), buffersize)) {
							char[] bb = new char[MB];
							int read;
							while((read = reader.read(bb))>0) {
								dummy += read;
							}
						}

						end();

						action.accept(new TestResult(this, duration(), buffersize, charset, info, stack));
					}
				}
			}
		},

		MANUAL_BYTEBUFFER("Files.newByteChannel() -> SeekableByteChannel, ByteBuffer.allocate()", ReadableByteChannel.class, ByteBuffer.class, CharsetDecoder.class) {
			@Override
			public void run(Path file, Consumer<TestResult> action) throws IOException {

				for(Charset charset : CHARSETS) {
					for(int buffersize : BUFFER_SIZES) {
						begin();

						try(ReadableByteChannel channel = Files.newByteChannel(file)) {
						    CharsetDecoder decoder = charset.newDecoder();
							ByteBuffer bb = ByteBuffer.allocate(buffersize);
							CharBuffer cb = CharBuffer.allocate(DEFAULT_BUFFER_SIZE);
							dummy += readChannel(channel, decoder, bb, cb);
						}

						end();

						action.accept(new TestResult(this, duration(), buffersize, charset, info, stack));
					}
				}
			}
		},

		MANUAL_BYTEBUFFER_DIRECT("Files.newByteChannel() -> SeekableByteChannel, ByteBuffer.allocateDirect()", ReadableByteChannel.class, ByteBuffer.class, CharsetDecoder.class) {
			@Override
			public void run(Path file, Consumer<TestResult> action) throws IOException {

				for(Charset charset : CHARSETS) {
					for(int buffersize : BUFFER_SIZES) {
						begin();

						try(ReadableByteChannel channel = Files.newByteChannel(file)) {
						    CharsetDecoder decoder = charset.newDecoder();
							ByteBuffer bb = ByteBuffer.allocateDirect(buffersize);
							CharBuffer cb = CharBuffer.allocate(DEFAULT_BUFFER_SIZE);
							dummy += readChannel(channel, decoder, bb, cb);
						}

						end();

						action.accept(new TestResult(this, duration(), buffersize, charset, info, stack));
					}
				}
			}
		},

		;

		protected int dummy;

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
			begin = System.currentTimeMillis();
		}

		void end() {
			end = System.currentTimeMillis();
		}

		long duration() {
			return end-begin;
		}
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
