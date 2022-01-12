/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.io;

import static de.ims.icarus2.test.TestUtils.assertNPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.test.annotations.PostponedTest;

/**
 * @author Markus Gärtner
 *
 */
class BitsTest {

	private static final Random random = new Random(System.currentTimeMillis()*System.nanoTime());

	private static final int POS_0 = 0;
	private static final int SIZE_1 = 1;

	private static final byte[] ARRAY_2 = new byte[2];
	private static final byte[] ARRAY_4 = new byte[4];
	private static final byte[] ARRAY_8 = new byte[8];

	@Nested
	class ForIllegalValues {

		private void assertIllegalArgument(Executable executable) {
			assertThrows(IllegalArgumentException.class, executable);
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#readNBytes(byte[], int, int)}.
		 */
		@Test
		void testReadNBytesNullArray() {
			assertNPE(() -> Bits.readNBytes(null, POS_0, SIZE_1));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#readNBytes(byte[], int, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1, 2, 3})
		void testReadNBytesIllegalOffset(int offset) {
			assertIllegalArgument(() -> Bits.readNBytes(ARRAY_2, offset, SIZE_1));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#readNBytes(byte[], int, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1, 0, 9, 10})
		void testReadNBytesIllegalN(int n) {
			assertIllegalArgument(() -> Bits.readNBytes(ARRAY_8, POS_0, n));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#writeNBytes(byte[], int, long, int)}.
		 */
		@Test
		void testWriteNBytesNullArray() {
			assertNPE(() -> Bits.writeNBytes(null, POS_0, Long.MAX_VALUE, SIZE_1));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#writeNBytes(byte[], int, long, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1, 2, 3})
		void testWriteNBytesIllegalOffset(int offset) {
			assertIllegalArgument(() -> Bits.writeNBytes(ARRAY_2, offset, Long.MAX_VALUE, SIZE_1));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#writeNBytes(byte[], int, long, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1, 0, 9, 10})
		void testWriteNBytesIllegalN(int n) {
			assertIllegalArgument(() -> Bits.writeNBytes(ARRAY_8, POS_0, Long.MAX_VALUE, n));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#readShort(byte[], int)}.
		 */
		@Test
		void testReadShortNullArray() {
			assertNPE(() -> Bits.readShort(null, POS_0));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#readShort(byte[], int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1, 1, 2, 3})
		void testReadShortIllegalOffset(int offset) {
			assertIllegalArgument(() -> Bits.readShort(ARRAY_2, offset));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#writeShort(byte[], int, short)}.
		 */
		@Test
		void testWriteShortNullArray() {
			assertNPE(() -> Bits.writeShort(null, POS_0, Short.MAX_VALUE));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#writeShort(byte[], int, short)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1, 1, 2, 3})
		void testWriteShortIllegalOffset(int offset) {
			assertIllegalArgument(() -> Bits.writeShort(ARRAY_2, offset, Short.MAX_VALUE));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#readInt(byte[], int)}.
		 */
		@Test
		void testReadIntNullArray() {
			assertNPE(() -> Bits.readInt(null, POS_0));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#readInt(byte[], int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1, 1, 2, 3, 4, 5})
		void testReadIntIllegalOffset(int offset) {
			assertIllegalArgument(() -> Bits.readInt(ARRAY_4, offset));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#writeInt(byte[], int, int)}.
		 */
		@Test
		void testWriteIntNullArray() {
			assertNPE(() -> Bits.writeInt(null, POS_0, Integer.MAX_VALUE));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#writeInt(byte[], int, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1, 1, 2, 3, 4, 5})
		void testWriteIntIllegalOffset(int offset) {
			assertIllegalArgument(() -> Bits.writeInt(ARRAY_4, offset, Integer.MAX_VALUE));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#readLong(byte[], int)}.
		 */
		@Test
		void testReadLongNullArray() {
			assertNPE(() -> Bits.readLong(null, POS_0));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#readLong(byte[], int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1, 1, 2, 3, 4, 5, 6, 7, 8})
		void testReadLongIllegalOffset(int offset) {
			assertIllegalArgument(() -> Bits.readLong(ARRAY_8, offset));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#writeLong(byte[], int, long)}.
		 */
		@Test
		void testWriteLongNullArray() {
			assertNPE(() -> Bits.writeLong(null, POS_0, Long.MAX_VALUE));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#writeLong(byte[], int, long)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1, 1, 2, 3, 4, 5, 6, 7, 8})
		void testWriteLongIllegalOffset(int offset) {
			assertIllegalArgument(() -> Bits.writeLong(ARRAY_8, offset, Long.MAX_VALUE));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#extractNBytes(long, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1, 0, 9})
		void testExtractNBytesIllegalN(int n) {
			assertIllegalArgument(() -> Bits.extractNBytes(Long.MAX_VALUE, n));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#extractByte(long, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1, 8, 9})
		void testExtractByteIllegalN(int n) {
			assertIllegalArgument(() -> Bits.extractByte(Long.MAX_VALUE, n));
		}
	}

	@Nested
	class ForFixedValues {

		// Randomize the locality within the buffer
		byte[] array;
		int offset;

		@BeforeEach
		void setUp() {
			offset = random.nextInt(8);
			array = new byte[24];
		}


		@AfterEach
		void tearDown() {
			offset = -1;
			array = null;
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#makeShort(byte, byte)}.
		 */
		@Test
		@PostponedTest
		void testMakeShort() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#makeInt(byte, byte, byte, byte)}.
		 */
		@Test
		@PostponedTest
		void testMakeInt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#makeLong(byte, byte, byte, byte, byte, byte, byte, byte)}.
		 */
		@Test
		@PostponedTest
		void testMakeLong() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#readNBytes(byte[], int, int)}.
		 */
		@TestFactory
		Stream<DynamicTest> testNBytes() {
			return LongStream.of(Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE)
					.boxed()
					.flatMap(v -> IntStream.range(1, 9)
							.mapToObj(n -> DynamicTest.dynamicTest(String.format(
									"v=%d n=%d", v, Integer.valueOf(n)), () -> {
										Bits.writeNBytes(array, offset, v.longValue(), n);
										long actual = Bits.readNBytes(array, offset, n);
										long expected = Bits.extractNBytes(v.longValue(), n);
										assertEquals(expected, actual);
									})));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#readShort(byte[], int)}.
		 */
		@TestFactory
		Stream<DynamicTest> testShort() {
			return IntStream.of(Short.MIN_VALUE, -1, 0, 1, Short.MAX_VALUE)
					.mapToObj(v_int -> DynamicTest.dynamicTest(
							String.valueOf(v_int), () -> {
								short v = (short) v_int;
								Bits.writeShort(array, offset, v);
								short actual = Bits.readShort(array, offset);
								assertEquals(v, actual);
							}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#readInt(byte[], int)}.
		 */
		@TestFactory
		Stream<DynamicTest> testInt() {
			return IntStream.of(Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE)
					.mapToObj(v -> DynamicTest.dynamicTest(
							String.valueOf(v), () -> {
								Bits.writeInt(array, offset, v);
								int actual = Bits.readInt(array, offset);
								assertEquals(v, actual);
							}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#readLong(byte[], int)}.
		 */
		@TestFactory
		Stream<DynamicTest> testLong() {
			return LongStream.of(Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE)
					.mapToObj(v -> DynamicTest.dynamicTest(
							String.valueOf(v), () -> {
								Bits.writeLong(array, offset, v);
								long actual = Bits.readLong(array, offset);
								assertEquals(v, actual);
							}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#extractNBytes(long, int)}.
		 */
		@TestFactory
		Stream<DynamicTest> testExtractNBytes() {
			List<DynamicTest> tests = new ArrayList<>();

			// Extract from 0
			DynamicTest.stream(IntStream.range(1, 9).boxed().iterator(), n -> "v=0, n="+n,
					n -> assertEquals(0L, Bits.extractNBytes(0L, n.intValue())))
				.forEach(tests::add);

			//TODO more important cases to cover?

			return tests.stream();
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#extractByte(long, int)}.
		 */
		@TestFactory
		Stream<DynamicTest> testExtractByte() {
			return IntStream.range(0, 8)
					.boxed()
					.flatMap(n -> LongStream.of(Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE)
							.mapToObj(v -> DynamicTest.dynamicTest(
									String.format("v=%s, n=%d", String.valueOf(v), n),
							() -> {
								Bits.writeLong(array, offset, v);
								byte expected = array[offset+n.intValue()];
								byte actual = Bits.extractByte(v, n.intValue());
								assertEquals(expected, actual);
							})));
		}

	}

	@Nested
	class WithRandomData {

		// Randomize the locality within the buffer
		byte[] array;
		int offset;

		final int RUNS = 100;

		@BeforeEach
		void setUp() {
			offset = random.nextInt(8);
			array = new byte[24];
		}

		@AfterEach
		void tearDown() {
			offset = -1;
			array = null;
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#makeShort(byte, byte)}.
		 */
		@Test
		@PostponedTest
		void testMakeShort() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#makeInt(byte, byte, byte, byte)}.
		 */
		@Test
		@PostponedTest
		void testMakeInt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#makeLong(byte, byte, byte, byte, byte, byte, byte, byte)}.
		 */
		@Test
		@PostponedTest
		void testMakeLong() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#readNBytes(byte[], int, int)}.
		 */
		@TestFactory
		Stream<DynamicTest> testNBytes() {
			return random.longs(RUNS, Long.MIN_VALUE, Long.MAX_VALUE)
					.mapToObj(v -> DynamicTest.dynamicTest(String.valueOf(v), () -> {
						int n = random.nextInt(8)+1;
						Bits.writeNBytes(array, offset, v, n);
						long actual = Bits.readNBytes(array, offset, n);
						long expected = Bits.extractNBytes(v, n);
						assertEquals(expected, actual);
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#readShort(byte[], int)}.
		 */
		@TestFactory
		Stream<DynamicTest> testShort() {
			return random.ints(RUNS, Short.MIN_VALUE, Short.MAX_VALUE)
					.mapToObj(v_int -> DynamicTest.dynamicTest(String.valueOf(v_int), () -> {
						short v = (short) v_int;
						Bits.writeShort(array, POS_0, v);
						short actual = Bits.readShort(array, POS_0);
						assertEquals(v, actual);
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#readInt(byte[], int)}.
		 */
		@TestFactory
		Stream<DynamicTest> testInt() {
			return random.ints(RUNS, Integer.MIN_VALUE, Integer.MAX_VALUE)
					.mapToObj(v -> DynamicTest.dynamicTest(String.valueOf(v), () -> {
						Bits.writeInt(array, POS_0, v);
						int actual = Bits.readInt(array, POS_0);
						assertEquals(v, actual);
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#readLong(byte[], int)}.
		 */
		@TestFactory
		Stream<DynamicTest> testLong() {
			return random.longs(RUNS, Long.MIN_VALUE, Long.MAX_VALUE)
					.mapToObj(v -> DynamicTest.dynamicTest(String.valueOf(v), () -> {
						Bits.writeLong(array, POS_0, v);
						long actual = Bits.readLong(array, POS_0);
						assertEquals(v, actual);
					}));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#extractNBytes(long, int)}.
		 */
		@SuppressWarnings("boxing")
		@TestFactory
		Stream<DynamicTest> testExtractNBytes() {
			final long[] _masks = {
				0xff,
				0xffff,
				0xffffff,
				0xffffffffL,
				0xffffffffffL,
				0xffffffffffffL,
				0xffffffffffffffL,
				0xffffffffffffffffL,
			};

			return random.longs(RUNS)
					.mapToObj(v -> {
						int n = random.nextInt(8)+1;
						return DynamicTest.dynamicTest(String.format("v=%d, n=%d", v, n),
								() -> {
									long expected = v & _masks[n-1];
									long actual = Bits.extractNBytes(v, n);
									assertEquals(expected, actual);
								});
					});
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.io.Bits#extractByte(long, int)}.
		 */
		@TestFactory
		Stream<DynamicTest> testExtractByte() {
			return IntStream.range(0, 8)
					.boxed()
					.flatMap(n -> random.longs(RUNS)
							.mapToObj(v -> DynamicTest.dynamicTest(
									String.format("v=%s, n=%d", String.valueOf(v), n),
							() -> {
								Bits.writeLong(array, offset, v);
								byte expected = array[offset+n.intValue()];
								byte actual = Bits.extractByte(v, n.intValue());
								assertEquals(expected, actual);
							})));
		}

	}
}
