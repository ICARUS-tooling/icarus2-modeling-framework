/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.mem;

import static de.ims.icarus2.test.TestTags.RANDOMIZED;
import static de.ims.icarus2.test.TestTags.SHUFFLE;
import static de.ims.icarus2.test.TestTags.STANDALONE;
import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.test.TestUtils.random;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.test.annotations.TestLocalOnly;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.io.Bits;
import de.ims.icarus2.util.mem.ByteAllocator.Cursor;

/**
 * @author Markus Gärtner
 *
 */
class ByteAllocatorTest {

	/** Object under test **/
	private ByteAllocator allocator;

	/** 12 **/
	private static final int defaultSlotSize = ByteAllocator.MIN_SLOT_SIZE+4;
	/** 7 **/
	private static final int defaultChunkPower = ByteAllocator.MIN_CHUNK_POWER;


	@BeforeEach
	void setUp(TestInfo testInfo) {
		if(!testInfo.getTags().contains(STANDALONE)) {
			allocator = new ByteAllocator(defaultSlotSize, defaultChunkPower);
		}
	}


	@AfterEach
	void tearDown(TestInfo testInfo) {
		if(!testInfo.getTags().contains(STANDALONE)) {
			allocator.clear();
		}
		allocator = null;
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#ByteAllocator(int, int)}.
	 */
	@ParameterizedTest
	@ValueSource(ints = {-1, 0, 7})
	@Tag(STANDALONE)
	void testIllegalSlotSizeConstructor(int slotSize) {
		assertThrows(IllegalArgumentException.class, () -> new ByteAllocator(slotSize, defaultSlotSize));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#ByteAllocator(int, int)}.
	 */
	@ParameterizedTest
	@ValueSource(ints = {-1, 0, 6, 18, Integer.MAX_VALUE})
	@Tag(STANDALONE)
	void testIllegalChunkPowerConstructor(int chunkpower) {
		assertThrows(IllegalArgumentException.class, () -> new ByteAllocator(defaultSlotSize, chunkpower));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getChunkSize()}.
	 */
	@Test
	void testGetChunkSize() {
		assertEquals(1<<defaultChunkPower, allocator.getChunkSize());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getSlotSize()}.
	 */
	@Test
	void testGetSlotSize() {
		assertEquals(defaultSlotSize, allocator.getSlotSize());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#alloc()}.
	 */
	@Test
	void testAlloc() {
		assertTrue(allocator.alloc()>IcarusUtils.UNSET_INT);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#free(int)}.
	 */
	@ParameterizedTest
	@ValueSource(ints = {-1, 0, 1})
	void testFreeIllegal(int id) {
		assertThrows(IndexOutOfBoundsException.class, () -> allocator.free(id));
	}

	@SuppressWarnings("boxing")
	@Test
	@TestLocalOnly
	@Tag(RANDOMIZED)
	void testAllocationConsistency(TestReporter testReporter) {
		final int SIZE = 1_000_000;
		final int RUNS = SIZE * 100;

		final boolean[] used = new boolean[SIZE];
		final long[] stored = new long[SIZE];

		final int offset = 0;

		int alive = 0;
		int changed = 0;
		int freed = 0;
		int misses = 0;
		int allocated = 0;

		Instant t_start = Instant.now();

		// Fill heap with data
		for (int i = 0; i < SIZE; i++) {
			int id = allocator.alloc();
			assertTrue(id!=IcarusUtils.UNSET_INT);
			assertFalse(used[id]);
			long data = random().nextLong();
			allocator.setLong(id, offset, data);
			stored[id] = data;
			used[id] = true;
			alive++;
		}

		Instant t_init = Instant.now();

		testReporter.publishEntry(String.format("Prepared heap with %d slots after %s",
				SIZE, Duration.between(t_start, t_init)));

		/*
		 *  Now simulate some usage scenario:
		 *
		 *  We access a random slot every round and validate its content
		 *  against what we stored externally.
		 *  Every now and then we update the slot's content with a new value (p=25%).
		 *  Later we start releasing or allocating new slots (p=5% each).
		 */
		for(int i=0; i<RUNS; i++) {

			int id = random().nextInt(SIZE);

			// Should only happen in later phase -> just continue
			if(!used[id]) {
				misses++;
				continue;
			}

			long data = allocator.getLong(id, offset);
			assertEquals(stored[id], data);

			int c = random().nextInt(100);

			// Release with a 5% chance
			if(c < 5) {
				allocator.free(id);
				used[id] = false;
				alive--;
				freed++;
			} else
			// Modify with a 25% chance
			if(c < 30) {
				long newData = random().nextLong();
				allocator.setLong(id, offset, newData);
				stored[id] = newData;
				changed++;
			} else
			// Repopulate a previously released slot with a 5% chance
			if(c < 35 && alive<SIZE) {
				int newId = allocator.alloc();
				assertTrue(newId!=IcarusUtils.UNSET_INT);
				assertFalse(used[newId]);
				long newData = random().nextLong();
				allocator.setLong(newId, offset, newData);
				stored[newId] = newData;
				used[newId] = true;
				alive++;
				allocated++;
			}
		}

		Instant t_sim = Instant.now();

		testReporter.publishEntry(String.format(
				"Simulation of %d rounds done after %s [alive=%d,changed=%d,freed=%d,allocated=%d,misses=%d]",
				RUNS, Duration.between(t_init, t_sim),
				alive, changed, freed, allocated, misses));

		// Finally do one run to check that the overall state matches our expectations
		for (int id = 0; id < SIZE; id++) {
			if(used[id]) {
				long v = allocator.getLong(id, offset);
				assertEquals(stored[id], v);
			} else {
				int slot = id;
				assertThrows(IllegalStateException.class, () -> allocator.getLong(slot, offset));
			}
		}

		Instant t_end = Instant.now();

		testReporter.publishEntry(String.format(
				"Finished testing allocation consistency after %s", Duration.between(t_start, t_end)));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#clear()}.
	 */
	@Test
	void testClear() {
		allocator.alloc();

		assertEquals(1, allocator.size());
		assertEquals(1, allocator.chunksUsed());

		allocator.clear();

		assertEquals(0, allocator.size());
		assertEquals(0, allocator.chunksUsed());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#size()}.
	 */
	@Test
	void testSizeEmpty() {
		assertEquals(0, allocator.size());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#chunksUsed()}.
	 */
	@Test
	void testChunksUsedEmpty() {
		assertEquals(0, allocator.chunksUsed());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#size()}.
	 */
	@Test
	void testSizeFilled() {

		int totalChunksToUse = 4;
		int totalSlotsToFill = totalChunksToUse * allocator.getChunkSize();

		for(int i=0; i<totalSlotsToFill; i++) {
			assertEquals(i, allocator.alloc());
			assertEquals(i+1, allocator.size());
		}

		assertEquals(totalChunksToUse, allocator.chunksUsed());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#trim()}.
	 */
	@Test
	void testTrim() {
		assertFalse(allocator.trim());

		int slotsToFill = 2*allocator.getChunkSize();

		// Allocate 2 buffer chunks worth of slots
		for(int i=0; i<slotsToFill; i++) {
			allocator.alloc();
		}
		assertEquals(slotsToFill, allocator.size());
		assertEquals(2, allocator.chunksUsed());

		assertFalse(allocator.trim());

		// Allocate 1 more slot
		int slot = allocator.alloc();
		assertTrue(slot>0);
		assertEquals(slotsToFill+1, allocator.size());
		assertEquals(3, allocator.chunksUsed());

		allocator.free(slot);
		assertEquals(slotsToFill, allocator.size());
		assertEquals(3, allocator.chunksUsed());

		assertTrue(allocator.trim());
		assertEquals(2, allocator.chunksUsed());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#newCursor()}.
	 */
	@Test
	void testNewCursor() {
		Cursor cursor = allocator.newCursor();
		assertNotNull(cursor);
		assertSame(allocator, cursor.source());
	}

	@Nested
	class ForInvalidIdOnEmptySlot {

		private void assertIdOutOfBounds(Executable executable) {
			IndexOutOfBoundsException ex = assertThrows(IndexOutOfBoundsException.class, executable);
			assertTrue(ex.getMessage().contains(" id "));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getByte(int, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,0,1})
		void testGetByte(int id) {
			assertIdOutOfBounds(() -> allocator.getByte(id, 0));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getNBytes(int, int, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,0,1})
		void testGetNBytes(int id) {
			assertIdOutOfBounds(() -> allocator.getNBytes(id, 0, 1));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getShort(int, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,0,1})
		void testGetShort(int id) {
			assertIdOutOfBounds(() -> allocator.getShort(id, 0));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getInt(int, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,0,1})
		void testGetInt(int id) {
			assertIdOutOfBounds(() -> allocator.getInt(id, 0));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getLong(int, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,0,1})
		void testGetLong(int id) {
			assertIdOutOfBounds(() -> allocator.getLong(id, 0));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#setByte(int, int, byte)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,0,1})
		void testSetByte(int id) {
			assertIdOutOfBounds(() -> allocator.setByte(id, 0, Byte.MAX_VALUE));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#setNBytes(int, int, long, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,0,1})
		void testSetNBytes(int id) {
			assertIdOutOfBounds(() -> allocator.setNBytes(id, 0, Long.MAX_VALUE, 3));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#setShort(int, int, short)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,0,1})
		void testSetShort(int id) {
			assertIdOutOfBounds(() -> allocator.setShort(id, 0, Short.MAX_VALUE));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#setInt(int, int, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,0,1})
		void testSetInt(int id) {
			assertIdOutOfBounds(() -> allocator.setInt(id, 0, Integer.MAX_VALUE));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#setLong(int, int, long)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,0,1})
		void testSetLong(int id) {
			assertIdOutOfBounds(() -> allocator.setLong(id, 0, Long.MAX_VALUE));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#writeBytes(int, int, byte[], int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,0,1})
		void testWriteBytes(int id) {
			assertIdOutOfBounds(() -> allocator.writeBytes(id, 0, new byte[1], 1));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#readBytes(int, int, byte[], int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,0,1})
		void testReadBytes(int id) {
			assertIdOutOfBounds(() -> allocator.readBytes(id, 0, new byte[1], 1));
		}
	}

	@Nested
	@DisplayName("expect IndexOutOfBoundsException for offsets [size="+defaultSlotSize+"]")
	class ForInvalidOffsetOnFilledSlot {

		private int id;

		/**
		 * Offsets are checked after slot ids, so we need to prepare a proper slot first
		 */
		@BeforeEach
		void setUp() {
			id = allocator.alloc();
		}

		private void assertOffsetOutOfBounds(Executable executable) {
			IndexOutOfBoundsException ex = assertThrows(IndexOutOfBoundsException.class, executable);
			assertTrue(ex.getMessage().toLowerCase().contains("offset "));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getByte(int, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,defaultSlotSize,defaultSlotSize+1})
		void testGetByte(int offset) {
			assertOffsetOutOfBounds(() -> allocator.getByte(id, offset));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getNBytes(int, int, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,defaultSlotSize,defaultSlotSize+1})
		void testGetNBytes(int offset) {
			assertOffsetOutOfBounds(() -> allocator.getNBytes(id, offset, 1));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getNBytes(int, int, int)}.
		 */
		@TestFactory
		Stream<DynamicTest> testGetNBytes() {
			return IntStream.range(1, Long.BYTES)
				.boxed()
				.flatMap(n -> IntStream.range(defaultSlotSize-n.intValue()+1, defaultSlotSize)
						.mapToObj(offset -> DynamicTest.dynamicTest(
								String.format("n=%d offset=%d", n, Integer.valueOf(offset)),
								() -> assertOffsetOutOfBounds(
										() -> allocator.getNBytes(id, offset, n.intValue())))));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getShort(int, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,
				defaultSlotSize-1,
				defaultSlotSize,
				defaultSlotSize+1})
		void testGetShort(int offset) {
			assertOffsetOutOfBounds(() -> allocator.getShort(id, offset));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getInt(int, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,
				defaultSlotSize-3,
				defaultSlotSize-2,
				defaultSlotSize-1,
				defaultSlotSize,
				defaultSlotSize+1})
		void testGetInt(int offset) {
			assertOffsetOutOfBounds(() -> allocator.getInt(id, offset));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getLong(int, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,
				defaultSlotSize-7,
				defaultSlotSize-6,
				defaultSlotSize-5,
				defaultSlotSize-4,
				defaultSlotSize-3,
				defaultSlotSize-2,
				defaultSlotSize-1,
				defaultSlotSize,
				defaultSlotSize+1})
		void testGetLong(int offset) {
			assertOffsetOutOfBounds(() -> allocator.getLong(id, offset));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#setByte(int, int, byte)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,defaultSlotSize,defaultSlotSize+1})
		void testSetByte(int offset) {
			assertOffsetOutOfBounds(() -> allocator.setByte(id, offset, Byte.MAX_VALUE));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#setNBytes(int, int, long, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,defaultSlotSize,defaultSlotSize+1})
		void testSetNBytes(int offset) {
			assertOffsetOutOfBounds(() -> allocator.setNBytes(id, offset, Long.MAX_VALUE, 1));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#setNBytes(int, int, long, int)}.
		 */
		@TestFactory
		Stream<DynamicTest> testSetNBytes() {
			return IntStream.range(1, Long.BYTES)
				.boxed()
				.flatMap(n -> IntStream.range(defaultSlotSize-n.intValue()+1, defaultSlotSize)
						.mapToObj(offset -> DynamicTest.dynamicTest(
								String.format("n=%d offset=%d", n, Integer.valueOf(offset)),
								() -> assertOffsetOutOfBounds(
										() -> allocator.setNBytes(id, offset, Long.MAX_VALUE, n.intValue())))));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#setShort(int, int, short)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,
				defaultSlotSize-1,
				defaultSlotSize,
				defaultSlotSize+1})
		void testSetShort(int offset) {
			assertOffsetOutOfBounds(() -> allocator.setShort(id, offset, Short.MAX_VALUE));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#setInt(int, int, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,
				defaultSlotSize-3,
				defaultSlotSize-2,
				defaultSlotSize-1,
				defaultSlotSize,
				defaultSlotSize+1})
		void testSetInt(int offset) {
			assertOffsetOutOfBounds(() -> allocator.setInt(id, offset, Integer.MAX_VALUE));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#setLong(int, int, long)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,
				defaultSlotSize-7,
				defaultSlotSize-6,
				defaultSlotSize-5,
				defaultSlotSize-4,
				defaultSlotSize-3,
				defaultSlotSize-2,
				defaultSlotSize-1,
				defaultSlotSize,
				defaultSlotSize+1})
		void testSetLong(int offset) {
			assertOffsetOutOfBounds(() -> allocator.setLong(id, offset, Long.MAX_VALUE));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#writeBytes(int, int, byte[], int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,defaultSlotSize,defaultSlotSize+1})
		void testWriteBytes(int offset) {
			assertOffsetOutOfBounds(() -> allocator.writeBytes(id, offset, new byte[1], 1));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#writeBytes(int, int, byte[], int)}.
		 */
		@TestFactory
		@Tag(RANDOMIZED)
		Stream<DynamicTest> testWriteBytes() {
			return random().ints(5, 1, defaultSlotSize)
				.boxed()
				.flatMap(n -> IntStream.range(defaultSlotSize-n.intValue()+1, defaultSlotSize)
						.mapToObj(offset -> DynamicTest.dynamicTest(
								String.format("n=%d offset=%d", n, Integer.valueOf(offset)),
								() -> assertOffsetOutOfBounds(
										() -> allocator.writeBytes(id, offset, new byte[defaultSlotSize], n.intValue())))));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#readBytes(int, int, byte[], int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1,defaultSlotSize,defaultSlotSize+1})
		void testReadBytes(int offset) {
			assertOffsetOutOfBounds(() -> allocator.readBytes(id, offset, new byte[1], 1));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#readBytes(int, int, byte[], int)}.
		 */
		@TestFactory
		@Tag(RANDOMIZED)
		Stream<DynamicTest> testReadBytes() {
			return random().ints(5, 1, defaultSlotSize)
					.boxed()
					.flatMap(n -> IntStream.range(defaultSlotSize-n.intValue()+1, defaultSlotSize)
							.mapToObj(offset -> DynamicTest.dynamicTest(
									String.format("n=%d offset=%d", n, Integer.valueOf(offset)),
									() -> assertOffsetOutOfBounds(
											() -> allocator.readBytes(id, offset, new byte[defaultSlotSize], n.intValue())))));
		}
	}

	@Nested
	class ForInvalidBatchArgumentsOnFilledSlotOfSize8 {

		private int id;

		/**
		 * Offsets are checked after slot ids, so we need to prepare a proper slot first
		 */
		@BeforeEach
		void setUp() {
			id = allocator.alloc();
		}

		private void assertIllegalArgument(Executable executable) {
			assertThrows(IllegalArgumentException.class, executable);
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#writeBytes(int, int, byte[], int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1, 0,defaultSlotSize+1})
		void testWriteBytesIllegalN(int n) {
			assertIllegalArgument(() -> allocator.writeBytes(id, 0, new byte[defaultSlotSize], n));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#readBytes(int, int, byte[], int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1, 0,defaultSlotSize+1})
		void testReadBytes(int n) {
			assertIllegalArgument(() -> allocator.readBytes(id, 0, new byte[defaultSlotSize], n));
		}
	}

	@Nested
	class WithRandomData {

		private static final int SIZE = 100;

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getByte(int, int)}.
		 */
		@RepeatedTest(RUNS)
		@Tag(RANDOMIZED)
		void testForByte() {
			final int offset = random().nextInt(allocator.getSlotSize());
			byte[] array = new byte[SIZE]; // [id] = byte_stored

			random().ints(SIZE, Byte.MIN_VALUE, Byte.MAX_VALUE)
				.forEach(v -> {
					int id = allocator.alloc();
					allocator.setByte(id, offset, (byte)v);
					array[id] = (byte) v;
				});

			IntStream.range(0, array.length)
				.forEach(id -> assertEquals(array[id], allocator.getByte(id, offset)));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getNBytes(int, int, int)}.
		 */
		@RepeatedTest(RUNS)
		@Tag(RANDOMIZED)
		void testForNBytes() {
			final int offset = random().nextInt(allocator.getSlotSize()-Long.BYTES);
			long[] array = new long[SIZE]; // [id] = long_stored
			int[] n_bytes = new int[SIZE]; // [id] = n

			random().longs(SIZE, Long.MIN_VALUE, Long.MAX_VALUE)
				.forEach(v -> {
					int id = allocator.alloc();
					int n = random().nextInt(Long.BYTES-2)+1;
					allocator.setNBytes(id, offset, v, n);
					array[id] = Bits.extractNBytes(v, n);
					n_bytes[id] = n;
				});

			IntStream.range(0, array.length)
				.forEach(id -> assertEquals(array[id], allocator.getNBytes(id, offset, n_bytes[id])));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getShort(int, int)}.
		 */
		@RepeatedTest(RUNS)
		@Tag(RANDOMIZED)
		void testGetShort() {
			final int offset = random().nextInt(allocator.getSlotSize()-Short.BYTES);
			short[] array = new short[SIZE]; // [id] = short_stored

			random().ints(SIZE, Short.MIN_VALUE, Short.MAX_VALUE)
				.forEach(v -> {
					int id = allocator.alloc();
					allocator.setShort(id, offset, (short)v);
					array[id] = (short) v;
				});

			IntStream.range(0, array.length)
				.forEach(id -> assertEquals(array[id], allocator.getShort(id, offset)));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getInt(int, int)}.
		 */
		@RepeatedTest(RUNS)
		@Tag(RANDOMIZED)
		void testGetInt() {
			final int offset = random().nextInt(allocator.getSlotSize()-Integer.BYTES);
			int[] array = new int[SIZE]; // [id] = int_stored

			random().ints(SIZE, Integer.MIN_VALUE, Integer.MAX_VALUE)
				.forEach(v -> {
					int id = allocator.alloc();
					allocator.setInt(id, offset, v);
					array[id] = v;
				});

			IntStream.range(0, array.length)
				.forEach(id -> assertEquals(array[id], allocator.getInt(id, offset)));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getLong(int, int)}.
		 */
		@RepeatedTest(RUNS)
		@Tag(RANDOMIZED)
		void testGetLong() {
			final int offset = random().nextInt(allocator.getSlotSize()-Long.BYTES);
			long[] array = new long[SIZE]; // [id] = long_stored

			random().longs(SIZE, Long.MIN_VALUE, Long.MAX_VALUE)
				.forEach(v -> {
					int id = allocator.alloc();
					allocator.setLong(id, offset, v);
					array[id] = v;
				});

			IntStream.range(0, array.length)
				.forEach(id -> assertEquals(array[id], allocator.getLong(id, offset)));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#writeBytes(int, int, byte[], int)}.
		 */
		@RepeatedTest(RUNS)
		@Tag(RANDOMIZED)
		void testForBulk() {
			final int offset = random().nextInt(allocator.getSlotSize()-Long.BYTES);
			byte[][] array = new byte[SIZE][]; // [id] = bytes_stored

			random().ints(SIZE, 1, defaultSlotSize-offset)
				.forEach(n -> {
					int id = allocator.alloc();
					byte[] bytes = new byte[n];
					random().nextBytes(bytes);
					allocator.writeBytes(id, offset, bytes, n);
					array[id] = bytes;
				});

			IntStream.range(0, array.length)
				.forEach(id -> {
					byte[] expected = array[id];
					byte[] actual = new byte[expected.length];
					allocator.readBytes(id, offset, actual, actual.length);
					assertArrayEquals(expected, actual);
				});
		}
	}

	@Nested
	class ForCursor {

		/** Object under test **/
		private Cursor cursor;


		@BeforeEach
		void setUp() {
			cursor = allocator.newCursor();
		}


		@AfterEach
		void tearDown() {
			cursor = null;
		}

		private void fill(int slots) {
			while(slots-->0)
				allocator.alloc();
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#clear()}.
		 */
		@Test
		void testClear() {
			assertFalse(cursor.hasChunk());

			allocator.alloc();
			cursor.moveTo(0);

			assertTrue(cursor.hasChunk());

			cursor.clear();

			assertFalse(cursor.hasChunk());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#moveTo(int)}.
		 */
		@Test
		void testMoveToEmpty() {
			assertThrows(IndexOutOfBoundsException.class, () -> cursor.moveTo(0));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#moveTo(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {-2, 10})
		void testMoveToOutOfBounds(int id) {
			fill(3);
			assertThrows(IndexOutOfBoundsException.class, () -> cursor.moveTo(id));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#moveTo(int)}.
		 */
		@Test
		void testMoveToUnsetInt() {
			fill(3);

			cursor.moveTo(1);
			assertTrue(cursor.hasChunk());

			cursor.moveTo(IcarusUtils.UNSET_INT);
			assertFalse(cursor.hasChunk());
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#moveTo(int)}.
		 */
		@Test
		void testMoveTo() {
			fill(defaultSlotSize);

			IntStream.range(0, defaultSlotSize)
				.boxed()
				.collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
					Collections.shuffle(list);
					return list;
				}))
				.stream()
				.forEach(id -> {
					cursor.moveTo(id.intValue());
					assertTrue(cursor.hasChunk());
				});

			// Expect movement to dead storage to fail
			allocator.free(0);
			assertThrows(IllegalStateException.class, () -> cursor.moveTo(0));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getId()}.
		 */
		@Test
		void testGetId() {
			fill(defaultSlotSize);

			assertEquals(IcarusUtils.UNSET_INT, cursor.getId());

			IntStream.range(0, defaultSlotSize)
				.boxed()
				.collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
					Collections.shuffle(list);
					return list;
				}))
				.stream()
				.forEach(id -> {
					cursor.moveTo(id.intValue());
					assertTrue(cursor.hasChunk());
					assertEquals(id.intValue(), cursor.getId());

					cursor.clear();

					assertEquals(IcarusUtils.UNSET_INT, cursor.getId());
				});
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#hasChunk()}.
		 */
		@Test
		void testHasChunk() {
			assertFalse(cursor.hasChunk());

			cursor.alloc();

			assertTrue(cursor.hasChunk());

			cursor.clear();

			assertFalse(cursor.hasChunk());
		}

		@Nested
		class WithRandomData {

			private static final int SIZE = 100;

			private int[] ids = new int[SIZE];

			@BeforeEach
			void setUp(TestInfo testInfo) {
				// Allocate sufficient slots
				for (int i = 0; i < SIZE; i++) {
					ids[i] = allocator.alloc();
				}

				if(testInfo.getTags().contains(SHUFFLE)) {
					shuffleIds();
				}
			}

			private void shuffleIds() {
				for (int i = 0; i < ids.length; i++) {
					ids[i] = ids[random().nextInt(ids.length)];
				}
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getByte(int)}.
			 */
			@RepeatedTest(RUNS)
			@Tag(SHUFFLE)
			@Tag(RANDOMIZED)
			void testBytes() {
				final int[] values = random()
						.ints(SIZE, Byte.MIN_VALUE, Byte.MAX_VALUE)
						.toArray(); // [id] = byte_value
				final int offset = random().nextInt(defaultSlotSize);

				// Write everything
				for (int id : ids) {
					byte v = (byte)values[id];
					cursor.moveTo(id);
					assertTrue(cursor.hasChunk());
					cursor.setByte(offset, v);
					assertEquals(v, cursor.getByte(offset));
				}

				// Ensure we don't use the same order
				shuffleIds();

				// Read and verify everything again
				for(int id : ids) {
					byte v = (byte)values[id];
					cursor.moveTo(id);

					assertTrue(cursor.hasChunk());
					assertEquals(v, cursor.getByte(offset));
				}
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getShort(int)}.
			 */
			@RepeatedTest(RUNS)
			@Tag(SHUFFLE)
			@Tag(RANDOMIZED)
			void testShort() {
				final int[] values = random()
						.ints(SIZE, Short.MIN_VALUE, Short.MAX_VALUE)
						.toArray(); // [id] = short_value
				final int offset = random().nextInt(defaultSlotSize-Short.BYTES);

				// Write everything
				for (int id : ids) {
					short v = (short)values[id];
					cursor.moveTo(id);
					assertTrue(cursor.hasChunk());
					cursor.setShort(offset, v);
					assertEquals(v, cursor.getShort(offset));
				}

				// Ensure we don't use the same order
				shuffleIds();

				// Read and verify everything again
				for(int id : ids) {
					short v = (short)values[id];
					cursor.moveTo(id);

					assertTrue(cursor.hasChunk());
					assertEquals(v, cursor.getShort(offset));
				}
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getInt(int)}.
			 */
			@RepeatedTest(RUNS)
			@Tag(SHUFFLE)
			@Tag(RANDOMIZED)
			void testInt() {
				final int[] values = random()
						.ints(SIZE, Integer.MIN_VALUE, Integer.MAX_VALUE)
						.toArray(); // [id] = int_value
				final int offset = random().nextInt(defaultSlotSize-Integer.BYTES);

				// Write everything
				for (int id : ids) {
					int v = values[id];
					cursor.moveTo(id);
					assertTrue(cursor.hasChunk());
					cursor.setInt(offset, v);
					assertEquals(v, cursor.getInt(offset));
				}

				// Ensure we don't use the same order
				shuffleIds();

				// Read and verify everything again
				for(int id : ids) {
					int v = values[id];
					cursor.moveTo(id);

					assertTrue(cursor.hasChunk());
					assertEquals(v, cursor.getInt(offset));
				}
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getLong(int)}.
			 */
			@RepeatedTest(RUNS)
			@Tag(SHUFFLE)
			@Tag(RANDOMIZED)
			void testLong() {
				final long[] values = random()
						.longs(SIZE, Long.MIN_VALUE, Long.MAX_VALUE)
						.toArray(); // [id] = long_value
				final int offset = random().nextInt(defaultSlotSize-Long.BYTES);

				// Write everything
				for (int id : ids) {
					long v = values[id];
					cursor.moveTo(id);
					assertTrue(cursor.hasChunk());
					cursor.setLong(offset, v);
					assertEquals(v, cursor.getLong(offset));
				}

				// Ensure we don't use the same order
				shuffleIds();

				// Read and verify everything again
				for(int id : ids) {
					long v = values[id];
					cursor.moveTo(id);

					assertTrue(cursor.hasChunk());
					assertEquals(v, cursor.getLong(offset));
				}
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getNBytes(int, int)}.
			 */
			@RepeatedTest(RUNS*2)
			@Tag(SHUFFLE)
			@Tag(RANDOMIZED)
			void testNBytes() {
				final long[] values = random()
						.longs(SIZE, Long.MIN_VALUE, Long.MAX_VALUE)
						.toArray(); // [id] = long_value
				final int n = random().nextInt(8)+1;
				final int offset = random().nextInt(defaultSlotSize-n);

				// Write everything
				for (int id : ids) {
					long v = values[id];
					cursor.moveTo(id);
					assertTrue(cursor.hasChunk());
					cursor.setNBytes(offset, v, n);
					assertEquals(Bits.extractNBytes(v, n), cursor.getNBytes(offset, n));
				}

				// Ensure we don't use the same order
				shuffleIds();

				// Read and verify everything again
				for(int id : ids) {
					long v = values[id];
					cursor.moveTo(id);

					assertTrue(cursor.hasChunk());
					assertEquals(Bits.extractNBytes(v, n), cursor.getNBytes(offset, n));
				}
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#writeBytes(int, byte[], int)}.
			 */
			@RepeatedTest(RUNS*2)
			@Tag(SHUFFLE)
			@Tag(RANDOMIZED)
			void testBytesArray() {
				final byte[][] values = new byte[SIZE][]; // [id] = long_value
				final int n = random().nextInt(8)+1;
				final int offset = random().nextInt(defaultSlotSize-n);

				for (int i = 0; i < values.length; i++) {
					values[i] = new byte[n];
					random().nextBytes(values[i]);
				}

				// Write everything
				for (int id : ids) {
					byte[] v = values[id];
					cursor.moveTo(id);
					assertTrue(cursor.hasChunk());
					cursor.writeBytes(offset, v, n);
					byte[] read = new byte[n];
					cursor.readBytes(offset, read, n);
					assertArrayEquals(v, read);
				}

				// Ensure we don't use the same order
				shuffleIds();

				// Read and verify everything again
				for(int id : ids) {
					byte[] v = values[id];
					cursor.moveTo(id);

					assertTrue(cursor.hasChunk());
					byte[] read = new byte[n];
					cursor.readBytes(offset, read, n);
					assertArrayEquals(v, read);
				}
			}
		}

		@Nested
		class ForInvalidBatchSizes {

			private int id;

			/**
			 * Offsets are checked after slot ids, so we need to prepare a proper slot first
			 */
			@BeforeEach
			void setUp() {
				id = allocator.alloc();
				cursor.moveTo(id);
			}

			private void assertIllegalArgument(Executable executable) {
				assertThrows(IllegalArgumentException.class, executable);
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getNBytes(int, int)}.
			 */
			@ParameterizedTest
			@ValueSource(ints = {-1, 0, 9})
			void testGetNBytes(int n) {
				assertIllegalArgument(() -> cursor.getNBytes(0, n));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#setNBytes(int, long, int)}.
			 */
			@ParameterizedTest
			@ValueSource(ints = {-1, 0, 9})
			void testSetNBytes(int n) {
				assertIllegalArgument(() -> cursor.setNBytes(0, Long.MAX_VALUE, n));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#writeBytes(int, byte[], int)}.
			 */
			@ParameterizedTest
			@ValueSource(ints = {-1, 0,defaultSlotSize+1})
			void testWriteBytes(int n) {
				assertIllegalArgument(() -> allocator.writeBytes(id, 0, new byte[defaultSlotSize], n));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#readBytes(int, byte[], int)}.
			 */
			@ParameterizedTest
			@ValueSource(ints = {-1, 0,defaultSlotSize+1})
			void testReadBytes(int n) {
				assertIllegalArgument(() -> allocator.readBytes(id, 0, new byte[defaultSlotSize], n));
			}
		}

		@Nested
		class ForOffsetOutOfBounds {

			private int id;

			@BeforeEach
			void setUp() {
				id = allocator.alloc();
				cursor.moveTo(id);
			}

			private void assertOffsetOutOfBounds(Executable executable) {
				IndexOutOfBoundsException ex = assertThrows(IndexOutOfBoundsException.class, executable);
				assertTrue(ex.getMessage().toLowerCase().contains("offset"));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getByte(int)}.
			 */
			@ParameterizedTest
			@ValueSource(ints = {-1, defaultSlotSize, defaultSlotSize+1})
			void testGetByte(int offset) {
				assertOffsetOutOfBounds(() -> cursor.getByte(offset));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getNBytes(int, int)}.
			 */
			@ParameterizedTest
			@ValueSource(ints = {-1, defaultSlotSize, defaultSlotSize+1})
			void testGetNBytes(int offset) {
				assertOffsetOutOfBounds(() -> cursor.getNBytes(offset, 1));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getShort(int)}.
			 */
			@ParameterizedTest
			@ValueSource(ints = {-1, defaultSlotSize, defaultSlotSize+1})
			void testGetShort(int offset) {
				assertOffsetOutOfBounds(() -> cursor.getShort(offset));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getInt(int)}.
			 */
			@ParameterizedTest
			@ValueSource(ints = {-1, defaultSlotSize, defaultSlotSize+1})
			void testGetInt(int offset) {
				assertOffsetOutOfBounds(() -> cursor.getInt(offset));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getLong(int)}.
			 */
			@ParameterizedTest
			@ValueSource(ints = {-1, defaultSlotSize, defaultSlotSize+1})
			void testGetLong(int offset) {
				assertOffsetOutOfBounds(() -> cursor.getLong(offset));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#setByte(int, byte)}.
			 */
			@ParameterizedTest
			@ValueSource(ints = {-1, defaultSlotSize, defaultSlotSize+1})
			void testSetByte(int offset) {
				assertOffsetOutOfBounds(() -> cursor.setByte(offset, Byte.MAX_VALUE));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#setNBytes(int, long, int)}.
			 */
			@ParameterizedTest
			@ValueSource(ints = {-1, defaultSlotSize, defaultSlotSize+1})
			void testSetNBytes(int offset) {
				assertOffsetOutOfBounds(() -> cursor.setNBytes(offset, Long.MAX_VALUE, 1));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#setShort(int, short)}.
			 */
			@ParameterizedTest
			@ValueSource(ints = {-1, defaultSlotSize, defaultSlotSize+1})
			void testSetShort(int offset) {
				assertOffsetOutOfBounds(() -> cursor.setShort(offset, Short.MAX_VALUE));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#setInt(int, int)}.
			 */
			@ParameterizedTest
			@ValueSource(ints = {-1, defaultSlotSize, defaultSlotSize+1})
			void testSetInt(int offset) {
				assertOffsetOutOfBounds(() -> cursor.setByte(offset, Byte.MAX_VALUE));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#setLong(int, long)}.
			 */
			@ParameterizedTest
			@ValueSource(ints = {-1, defaultSlotSize, defaultSlotSize+1})
			void testSetLong(int offset) {
				assertOffsetOutOfBounds(() -> cursor.setLong(offset, Long.MAX_VALUE));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#writeBytes(int, byte[], int)}.
			 */
			@ParameterizedTest
			@ValueSource(ints = {-1, defaultSlotSize, defaultSlotSize+1})
			void testWriteBytes(int offset) {
				assertOffsetOutOfBounds(() -> cursor.writeBytes(offset, new byte[1], 1));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#writeBytes(int, int, byte[], int)}.
			 */
			@TestFactory
			Stream<DynamicTest> testWriteBytes() {
				return IntStream.range(1, defaultSlotSize)
					.mapToObj(offset -> DynamicTest.dynamicTest(String.valueOf(offset),
							() -> assertOffsetOutOfBounds(
									() -> cursor.writeBytes(offset,
											new byte[defaultSlotSize], defaultSlotSize-offset+1))));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#readBytes(int, byte[], int)}.
			 */
			@ParameterizedTest
			@ValueSource(ints = {-1, defaultSlotSize, defaultSlotSize+1})
			void testReadBytes(int offset) {
				assertOffsetOutOfBounds(() -> cursor.readBytes(offset, new byte[1], 1));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#readBytes(int, int, byte[], int)}.
			 */
			@TestFactory
			Stream<DynamicTest> testReadBytes() {
				return IntStream.range(1, defaultSlotSize)
					.mapToObj(offset -> DynamicTest.dynamicTest(String.valueOf(offset),
							() -> assertOffsetOutOfBounds(
									() -> cursor.readBytes(offset,
											new byte[defaultSlotSize], defaultSlotSize-offset+1))));
			}
		}

		@Nested
		class ForChunkNotAvailable {

			private void assertIllegalState(Executable executable) {
				assertThrows(IllegalStateException.class, executable);
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getByte(int)}.
			 */
			@Test
			void testGetByte() {
				assertIllegalState(() -> cursor.getByte(0));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getNBytes(int, int)}.
			 */
			@Test
			void testGetNBytes() {
				assertIllegalState(() -> cursor.getNBytes(0, 1));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getShort(int)}.
			 */
			@Test
			void testGetShort() {
				assertIllegalState(() -> cursor.getShort(0));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getInt(int)}.
			 */
			@Test
			void testGetInt() {
				assertIllegalState(() -> cursor.getInt(0));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getLong(int)}.
			 */
			@Test
			void testGetLong() {
				assertIllegalState(() -> cursor.getLong(0));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#setByte(int, byte)}.
			 */
			@Test
			void testSetByte() {
				assertIllegalState(() -> cursor.setByte(0, Byte.MAX_VALUE));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#setNBytes(int, long, int)}.
			 */
			@Test
			void testSetNBytes() {
				assertIllegalState(() -> cursor.setNBytes(0, Long.MAX_VALUE, 1));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#setShort(int, short)}.
			 */
			@Test
			void testSetShort() {
				assertIllegalState(() -> cursor.setShort(0, Short.MAX_VALUE));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#setInt(int, int)}.
			 */
			@Test
			void testSetInt() {
				assertIllegalState(() -> cursor.setInt(0, Integer.MAX_VALUE));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#setLong(int, long)}.
			 */
			@Test
			void testSetLong() {
				assertIllegalState(() -> cursor.setLong(0, Long.MAX_VALUE));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#writeBytes(int, byte[], int)}.
			 */
			@Test
			void testWriteBytes() {
				assertIllegalState(() -> cursor.writeBytes(0, new byte[1], 1));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#readBytes(int, byte[], int)}.
			 */
			@Test
			void testReadBytes() {
				assertIllegalState(() -> cursor.readBytes(0, new byte[1], 1));
			}
		}
	}
}
