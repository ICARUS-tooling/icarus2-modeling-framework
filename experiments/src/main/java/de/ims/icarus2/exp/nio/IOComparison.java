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
package de.ims.icarus2.exp.nio;

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

/**
 * @author Markus Gärtner
 *
 */
public class IOComparison {

	public static void main(String[] args) throws IOException {

		IOComparison comparison = new IOComparison();

		comparison.run();
	}

	private static final int MB = 1024*1024;
	private static final int GB = 1024*1024*1024;
	private static final int L1 = 32768;
	private static final int DEFAULT_BUFFER_SIZE = 8192;

	private static byte[] MB_BUFFER = new byte[1024*1024];
	static {
		Arrays.fill(MB_BUFFER, (byte)1);
	}

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
				,1000
//				,5000
		};

		List<Test> tests = new ArrayList<>();
		Collections.addAll(tests, ByteReadTest.values());
		Collections.addAll(tests, CharacterReadTest.values());

		for(int fileSizeMB : fileSizesMB) {

			setup();
			try {
				long timeSpentWriting = fillFile(fileSizeMB);
				System.out.printf("\nRunning tests with file of size %d MB - time for writing: %d\n",
						fileSizeMB, timeSpentWriting);

				for(Test test : tests) {
					long runtime = test.readFile(FILE);
					System.out.printf("%s %s: %dms\n",
							test.getClass().getName(), test.toString(), runtime);
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
		long readFile(Path file) throws IOException;
	}

	private enum ByteReadTest implements Test {

		STREAM_UNBUFFERED {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(InputStream is = Files.newInputStream(file)) {
					byte[] bb = new byte[MB];
					int read;
					while((read = is.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "ChannelInputStream (unbuffered)";
			}
		},

		STREAM_BUFFERED_MB {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(InputStream is = new BufferedInputStream(Files.newInputStream(file), MB)) {
					byte[] bb = new byte[MB];
					int read;
					while((read = is.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "ChannelInputStream (1MB buffer)";
			}
		},

		STREAM_BUFFERED_L1 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(InputStream is = new BufferedInputStream(Files.newInputStream(file), L1)) {
					byte[] bb = new byte[MB];
					int read;
					while((read = is.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "ChannelInputStream (L1 sized buffer "+L1+")";
			}
		},

		STREAM_BUFFERED_DEFAULT {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(InputStream is = new BufferedInputStream(Files.newInputStream(file))) {
					byte[] bb = new byte[MB];
					int read;
					while((read = is.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "ChannelInputStream (default buffer size)";
			}
		},

		FILESTREAM_UNBUFFERED {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(InputStream is = new FileInputStream(file.toFile())) {
					byte[] bb = new byte[MB];
					int read;
					while((read = is.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "FileInputStream (unbuffered)";
			}
		},

		FILESTREAM_BUFFERED_MB {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(InputStream is = new BufferedInputStream(new FileInputStream(file.toFile()), MB)) {
					byte[] bb = new byte[MB];
					int read;
					while((read = is.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "FileInputStream (1MB buffer)";
			}
		},

		FILESTREAM_BUFFERED_L1 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(InputStream is = new BufferedInputStream(new FileInputStream(file.toFile()), L1)) {
					byte[] bb = new byte[MB];
					int read;
					while((read = is.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "FileInputStream (L1 sized buffer "+L1+")";
			}
		},

		FILESTREAM_BUFFERED_DEFAULT {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(InputStream is = new BufferedInputStream(new FileInputStream(file.toFile()))) {
					byte[] bb = new byte[MB];
					int read;
					while((read = is.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "FileInputStream (default buffer size)";
			}
		},

		BYTEBUFFER_MB {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(ReadableByteChannel channel = Files.newByteChannel(file)) {
					ByteBuffer bb = ByteBuffer.allocate(MB);
					while(channel.read(bb)>0) {
						bb.flip();
						dummy += bb.remaining();
						bb.clear();
					}
				}
			}

			@Override
			public String toString() {
				return "ByteBuffer (1MB)";
			}
		},

		BYTEBUFFER_L1 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(ReadableByteChannel channel = Files.newByteChannel(file)) {
					ByteBuffer bb = ByteBuffer.allocate(L1);
					while(channel.read(bb)>0) {
						bb.flip();
						dummy += bb.remaining();
						bb.clear();
					}
				}
			}

			@Override
			public String toString() {
				return "ByteBuffer (L1 size)";
			}
		},

		BYTEBUFFER_DEFAULT {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(ReadableByteChannel channel = Files.newByteChannel(file)) {
					ByteBuffer bb = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
					while(channel.read(bb)>0) {
						bb.flip();
						dummy += bb.remaining();
						bb.clear();
					}
				}
			}

			@Override
			public String toString() {
				return "ByteBuffer (default size)";
			}
		},

		BYTEBUFFER_DIRECT_MB {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(ReadableByteChannel channel = Files.newByteChannel(file)) {
					ByteBuffer bb = ByteBuffer.allocateDirect(MB);
					while(channel.read(bb)>0) {
						bb.flip();
						dummy += bb.remaining();
						bb.clear();
					}
				}
			}

			@Override
			public String toString() {
				return "DirectByteBuffer (1MB)";
			}
		},

		BYTEBUFFER_DIRECT_L1 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(ReadableByteChannel channel = Files.newByteChannel(file)) {
					ByteBuffer bb = ByteBuffer.allocateDirect(L1);
					while(channel.read(bb)>0) {
						bb.flip();
						dummy += bb.remaining();
						bb.clear();
					}
				}
			}

			@Override
			public String toString() {
				return "DirectByteBuffer (L1 size)";
			}
		},

		BYTEBUFFER_DIRECT_DEFAULT {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(ReadableByteChannel channel = Files.newByteChannel(file)) {
					ByteBuffer bb = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
					while(channel.read(bb)>0) {
						bb.flip();
						dummy += bb.remaining();
						bb.clear();
					}
				}
			}

			@Override
			public String toString() {
				return "DirectByteBuffer (default size)";
			}
		},

//		BYTEBUFFER_MAPPED_1MB {
//			@Override
//			protected void readFile0(Path file) throws IOException {
//
//				try(FileChannel channel = FileChannel.open(file)) {
//					long size = channel.size();
//					long pos = 0L;
//
//					while(pos<size) {
//						MappedByteBuffer bb = channel.map(MapMode.READ_ONLY, pos, Math.min(MB, size - pos));
//						dummy += bb.remaining();
//						pos += bb.remaining();
//						queueRefForGC(bb);
//					}
//
//				}
//			}
//
//			@Override
//			public String toString() {
//				return "MappedByteBuffer (1MB)";
//			}
//		},

//		BYTEBUFFER_MAPPED_100MB {
//			@Override
//			protected void readFile0(Path file) throws IOException {
//
//				try(FileChannel channel = FileChannel.open(file)) {
//					long size = channel.size();
//					long pos = 0L;
//
//					while(pos<size) {
//						MappedByteBuffer bb = channel.map(MapMode.READ_ONLY, pos, Math.min(100*MB, size - pos));
//						dummy += bb.remaining();
//						pos += bb.remaining();
//						queueRefForGC(bb);
//					}
//				}
//			}
//
//			@Override
//			public String toString() {
//				return "MappedByteBuffer (100MB)";
//			}
//		},

//		BYTEBUFFER_MAPPED_1GB {
//			@Override
//			protected void readFile0(Path file) throws IOException {
//
//				try(FileChannel channel = FileChannel.open(file)) {
//					long size = channel.size();
//					long pos = 0L;
//
//					while(pos<size) {
//						MappedByteBuffer bb = channel.map(MapMode.READ_ONLY, pos, Math.min(GB, size - pos));
//						dummy += bb.remaining();
//						pos += bb.remaining();
//						queueRefForGC(bb);
//					}
//				}
//			}
//
//			@Override
//			public String toString() {
//				return "MappedByteBuffer (1GB)";
//			}
//		},

//		BYTEBUFFER_MAPPED_L1 {
//			@Override
//			protected void readFile0(Path file) throws IOException {
//
//				try(FileChannel channel = FileChannel.open(file)) {
//					long size = channel.size();
//					long pos = 0L;
//
//					while(pos<size) {
//						MappedByteBuffer bb = channel.map(MapMode.READ_ONLY, pos, Math.min(L1, size - pos));
//						dummy += bb.remaining();
//						pos += bb.remaining();
//						queueRefForGC(bb);
//					}
//				}
//			}
//
//			@Override
//			public String toString() {
//				return "MappedByteBuffer (L1 size)";
//			}
//		},

//		BYTEBUFFER_MAPPED_DEFAULT {
//			@Override
//			protected void readFile0(Path file) throws IOException {
//
//				try(FileChannel channel = FileChannel.open(file)) {
//					long size = channel.size();
//					long pos = 0L;
//
//					while(pos<size) {
//						MappedByteBuffer bb = channel.map(MapMode.READ_ONLY, pos, Math.min(DEFAULT_BUFFER_SIZE, size - pos));
//						dummy += bb.remaining();
//						pos += bb.remaining();
//						queueRefForGC(bb);
//					}
//				}
//			}
//
//			@Override
//			public String toString() {
//				return "MappedByteBuffer (default size)";
//			}
//		},

		;

		protected int dummy;

		/**
		 * Read file and return time in milliseconds
		 */
		protected abstract void readFile0(Path file) throws IOException;

		@Override
		public long readFile(Path file) throws IOException {
			long start = System.currentTimeMillis();

			readFile0(file);

			long end = System.currentTimeMillis();

			return end-start;
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

	private enum CharacterReadTest implements Test {

		STREAM_UNBUFFERED_UTF8 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(Reader reader = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8)) {
					char[] bb = new char[MB];
					int read;
					while((read = reader.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "InputStreamReader (unbuffered, UTF8)";
			}
		},

		STREAM_UNBUFFERED_ISO_8859_1 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(Reader reader = new InputStreamReader(Files.newInputStream(file), StandardCharsets.ISO_8859_1)) {
					char[] bb = new char[MB];
					int read;
					while((read = reader.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "InputStreamReader (unbuffered, ISO-8859-1)";
			}
		},

		STREAM_BUFFERED_MB_UTF8 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(Reader reader = new InputStreamReader(new BufferedInputStream(
						Files.newInputStream(file), MB), StandardCharsets.UTF_8)) {
					char[] bb = new char[MB];
					int read;
					while((read = reader.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "InputStreamReader (stream, 1MB buffer, UTF8)";
			}
		},

		STREAM_BUFFERED_MB_ISO_8859_1 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(Reader reader = new InputStreamReader(new BufferedInputStream(
						Files.newInputStream(file), MB), StandardCharsets.ISO_8859_1)) {
					char[] bb = new char[MB];
					int read;
					while((read = reader.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "InputStreamReader (stream, 1MB buffer, ISO-8859-1)";
			}
		},

		STREAM_BUFFERED_L1_UTF8 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(Reader reader = new InputStreamReader(new BufferedInputStream(
						Files.newInputStream(file), L1), StandardCharsets.UTF_8)) {
					char[] bb = new char[MB];
					int read;
					while((read = reader.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "InputStreamReader (stream, L1 buffer size, UTF8)";
			}
		},

		STREAM_BUFFERED_L1_ISO_8859_1 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(Reader reader = new InputStreamReader(new BufferedInputStream(
						Files.newInputStream(file), L1), StandardCharsets.ISO_8859_1)) {
					char[] bb = new char[MB];
					int read;
					while((read = reader.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "InputStreamReader (stream, L1 buffer size, ISO-8859-1)";
			}
		},

		STREAM_BUFFERED_DEFAULT_UTF8 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(Reader reader = new InputStreamReader(new BufferedInputStream(
						Files.newInputStream(file), DEFAULT_BUFFER_SIZE), StandardCharsets.UTF_8)) {
					char[] bb = new char[MB];
					int read;
					while((read = reader.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "InputStreamReader (stream, default buffer size, UTF8)";
			}
		},

		STREAM_BUFFERED_DEFAULT_ISO_8859_1 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(Reader reader = new InputStreamReader(new BufferedInputStream(
						Files.newInputStream(file), DEFAULT_BUFFER_SIZE), StandardCharsets.ISO_8859_1)) {
					char[] bb = new char[MB];
					int read;
					while((read = reader.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "InputStreamReader (stream, default buffer size, ISO-8859-1)";
			}
		},

		READER_BUFFERED_MB_UTF8 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(Reader reader = new BufferedReader(new InputStreamReader(
						Files.newInputStream(file), StandardCharsets.UTF_8), MB)) {
					char[] bb = new char[MB];
					int read;
					while((read = reader.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "InputStreamReader (1MB buffer, UTF8)";
			}
		},

		READER_BUFFERED_MB_ISO_8859_1 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(Reader reader = new BufferedReader(new InputStreamReader(
						Files.newInputStream(file), StandardCharsets.ISO_8859_1), MB)) {
					char[] bb = new char[MB];
					int read;
					while((read = reader.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "InputStreamReader (1MB buffer, ISO-8859-1)";
			}
		},

		READER_BUFFERED_L1_UTF8 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(Reader reader = new BufferedReader(new InputStreamReader(
						Files.newInputStream(file), StandardCharsets.UTF_8), L1)) {
					char[] bb = new char[MB];
					int read;
					while((read = reader.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "InputStreamReader (L1 buffer size, UTF8)";
			}
		},

		READER_BUFFERED_L1_ISO_8859_1 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(Reader reader = new BufferedReader(new InputStreamReader(
						Files.newInputStream(file), StandardCharsets.ISO_8859_1), L1)) {
					char[] bb = new char[MB];
					int read;
					while((read = reader.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "InputStreamReader (L1 buffer size, ISO-8859-1)";
			}
		},

		READER_BUFFERED_DEFAULT_UTF8 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(Reader reader = new BufferedReader(new InputStreamReader(
						Files.newInputStream(file), StandardCharsets.UTF_8))) {
					char[] bb = new char[MB];
					int read;
					while((read = reader.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "InputStreamReader (default buffer size, UTF8)";
			}
		},

		READER_BUFFERED_DEFAULT_ISO_8859_1 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(Reader reader = new BufferedReader(new InputStreamReader(
						Files.newInputStream(file), StandardCharsets.ISO_8859_1))) {
					char[] bb = new char[MB];
					int read;
					while((read = reader.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "InputStreamReader (default buffer size, ISO-8859-1)";
			}
		},

		FILESTREAM_UNBUFFERED_UTF8 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(Reader reader = new InputStreamReader(new FileInputStream(file.toFile()), StandardCharsets.UTF_8)) {
					char[] bb = new char[MB];
					int read;
					while((read = reader.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "InputStreamReader (file stream, unbuffered, UTF8)";
			}
		},

		FILESTREAM_UNBUFFERED_ISO_8859_1 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(Reader reader = new InputStreamReader(new FileInputStream(file.toFile()), StandardCharsets.ISO_8859_1)) {
					char[] bb = new char[MB];
					int read;
					while((read = reader.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "InputStreamReader (file stream, unbuffered, ISO-8859-1)";
			}
		},

		FILESTREAM_BUFFERED_MB_UTF8 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(Reader reader = new BufferedReader(new InputStreamReader(
						new FileInputStream(file.toFile()), StandardCharsets.UTF_8), MB)) {
					char[] bb = new char[MB];
					int read;
					while((read = reader.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "InputStreamReader (file stream, 1MB buffer, UTF8)";
			}
		},

		FILESTREAM_BUFFERED_MB_ISO_8859_1 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(Reader reader = new BufferedReader(new InputStreamReader(
						new FileInputStream(file.toFile()), StandardCharsets.ISO_8859_1), MB)) {
					char[] bb = new char[MB];
					int read;
					while((read = reader.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "InputStreamReader (file stream, 1MB buffer, ISO-8859-1)";
			}
		},

		FILESTREAM_BUFFERED_L1_UTF8 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(Reader reader = new BufferedReader(new InputStreamReader(
						new FileInputStream(file.toFile()), StandardCharsets.UTF_8), L1)) {
					char[] bb = new char[MB];
					int read;
					while((read = reader.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "InputStreamReader (file stream, L1 buffer size, UTF8)";
			}
		},

		FILESTREAM_BUFFERED_L1_ISO_8859_1 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(Reader reader = new BufferedReader(new InputStreamReader(
						new FileInputStream(file.toFile()), StandardCharsets.ISO_8859_1), L1)) {
					char[] bb = new char[MB];
					int read;
					while((read = reader.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "InputStreamReader (file stream, L1 buffer size, ISO-8859-1)";
			}
		},

		FILESTREAM_BUFFERED_DEFAULT_UTF8 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(Reader reader = new BufferedReader(new InputStreamReader(
						new FileInputStream(file.toFile()), StandardCharsets.UTF_8), DEFAULT_BUFFER_SIZE)) {
					char[] bb = new char[MB];
					int read;
					while((read = reader.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "InputStreamReader (file stream, default buffer size, UTF8)";
			}
		},

		FILESTREAM_BUFFERED_DEFAULT_ISO_8859_1 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(Reader reader = new BufferedReader(new InputStreamReader(
						new FileInputStream(file.toFile()), StandardCharsets.ISO_8859_1), DEFAULT_BUFFER_SIZE)) {
					char[] bb = new char[MB];
					int read;
					while((read = reader.read(bb))>0) {
						dummy += read;
					}
				}
			}

			@Override
			public String toString() {
				return "InputStreamReader (file stream, default buffer size, ISO-8859-1)";
			}
		},

		BYTEBUFFER_MB_UTF8 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(ReadableByteChannel channel = Files.newByteChannel(file)) {
				    CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
					ByteBuffer bb = ByteBuffer.allocate(MB);
					CharBuffer cb = CharBuffer.allocate(DEFAULT_BUFFER_SIZE);
					dummy += readChannel(channel, decoder, bb, cb);
				}
			}

			@Override
			public String toString() {
				return "ByteBuffer (1MB, UTF8)";
			}
		},

		BYTEBUFFER_MB_ISO_8859_1 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(ReadableByteChannel channel = Files.newByteChannel(file)) {
				    CharsetDecoder decoder = StandardCharsets.ISO_8859_1.newDecoder();
					ByteBuffer bb = ByteBuffer.allocate(MB);
					CharBuffer cb = CharBuffer.allocate(DEFAULT_BUFFER_SIZE);
					dummy += readChannel(channel, decoder, bb, cb);
				}
			}

			@Override
			public String toString() {
				return "ByteBuffer (1MB, ISO-8859-1)";
			}
		},

		BYTEBUFFER_L1_UTF8 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(ReadableByteChannel channel = Files.newByteChannel(file)) {
				    CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
					ByteBuffer bb = ByteBuffer.allocate(L1);
					CharBuffer cb = CharBuffer.allocate(DEFAULT_BUFFER_SIZE);
					dummy += readChannel(channel, decoder, bb, cb);
				}
			}

			@Override
			public String toString() {
				return "ByteBuffer (L1 size, UTF8)";
			}
		},

		BYTEBUFFER_L1_ISO_8859_1 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(ReadableByteChannel channel = Files.newByteChannel(file)) {
				    CharsetDecoder decoder = StandardCharsets.ISO_8859_1.newDecoder();
					ByteBuffer bb = ByteBuffer.allocate(L1);
					CharBuffer cb = CharBuffer.allocate(DEFAULT_BUFFER_SIZE);
					dummy += readChannel(channel, decoder, bb, cb);
				}
			}

			@Override
			public String toString() {
				return "ByteBuffer (L1 size, ISO-8859-1)";
			}
		},

		BYTEBUFFER_DEFAULT_UTF8 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(ReadableByteChannel channel = Files.newByteChannel(file)) {
				    CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
					ByteBuffer bb = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
					CharBuffer cb = CharBuffer.allocate(DEFAULT_BUFFER_SIZE);
					dummy += readChannel(channel, decoder, bb, cb);
				}
			}

			@Override
			public String toString() {
				return "ByteBuffer (default size, UTF8)";
			}
		},

		BYTEBUFFER_DEFAULT_ISO_8859_1 {
			@Override
			protected void readFile0(Path file) throws IOException {

				try(ReadableByteChannel channel = Files.newByteChannel(file)) {
				    CharsetDecoder decoder = StandardCharsets.ISO_8859_1.newDecoder();
					ByteBuffer bb = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
					CharBuffer cb = CharBuffer.allocate(DEFAULT_BUFFER_SIZE);
					dummy += readChannel(channel, decoder, bb, cb);
				}
			}

			@Override
			public String toString() {
				return "ByteBuffer (default size, ISO-8859-1)";
			}
		},

//		BYTEBUFFER_DIRECT_MB {
//			@Override
//			protected void readFile0(Path file) throws IOException {
//
//				try(ReadableByteChannel channel = Files.newByteChannel(file)) {
//					ByteBuffer bb = ByteBuffer.allocateDirect(MB);
//					while(channel.read(bb)>0) {
//						bb.flip();
//						dummy += bb.remaining();
//						bb.clear();
//					}
//				}
//			}
//
//			@Override
//			public String toString() {
//				return "DirectByteBuffer (1MB)";
//			}
//		},
//
//		BYTEBUFFER_DIRECT_L1 {
//			@Override
//			protected void readFile0(Path file) throws IOException {
//
//				try(ReadableByteChannel channel = Files.newByteChannel(file)) {
//					ByteBuffer bb = ByteBuffer.allocateDirect(L1);
//					while(channel.read(bb)>0) {
//						bb.flip();
//						dummy += bb.remaining();
//						bb.clear();
//					}
//				}
//			}
//
//			@Override
//			public String toString() {
//				return "DirectByteBuffer (L1 size)";
//			}
//		},
//
//		BYTEBUFFER_DIRECT_DEFAULT {
//			@Override
//			protected void readFile0(Path file) throws IOException {
//
//				try(ReadableByteChannel channel = Files.newByteChannel(file)) {
//					ByteBuffer bb = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
//					while(channel.read(bb)>0) {
//						bb.flip();
//						dummy += bb.remaining();
//						bb.clear();
//					}
//				}
//			}
//
//			@Override
//			public String toString() {
//				return "DirectByteBuffer (default size)";
//			}
//		},

		;

		protected int dummy;

		/**
		 * Read file and return time in milliseconds
		 */
		protected abstract void readFile0(Path file) throws IOException;

		@Override
		public long readFile(Path file) throws IOException {
			long start = System.currentTimeMillis();

			readFile0(file);

			long end = System.currentTimeMillis();

			return end-start;
		}
	}
}
