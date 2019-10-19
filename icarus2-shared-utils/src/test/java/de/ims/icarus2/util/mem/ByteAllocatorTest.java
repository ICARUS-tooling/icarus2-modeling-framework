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

import static de.ims.icarus2.test.TestTags.STANDALONE;
import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.test.TestUtils.assertIOOB;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.lang.Primitives._int;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.test.annotations.DisabledOnCi;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.test.util.Triple;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.io.Bits;
import de.ims.icarus2.util.mem.ByteAllocator.Cursor;
import de.ims.icarus2.util.mem.ByteAllocator.LockType;

/**
 * @author Markus Gärtner
 *
 */
class ByteAllocatorTest {

	private Stream<Config> configurations() {
		return configurations(UNSET_INT);
	}

	private Stream<Config> configurations(int minSlotSize) {
		return Stream.of(LockType.values())
				.flatMap(lockType -> IntStream.of(ByteAllocator.MIN_SLOT_SIZE, 20, 100)
						.filter(v -> minSlotSize==UNSET_INT || v>=minSlotSize)
						.boxed()
						.map(i -> Pair.pair(lockType, i)))
				.flatMap(p -> IntStream.of(7, 10, 17)
						.boxed()
						.map(i -> Triple.triple(p.first, p.second, i)))
				.map(t -> new Config(t.second.intValue(), t.third.intValue(), t.first));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#ByteAllocator(int, int)}.
	 */
	@ParameterizedTest
	@ValueSource(ints = {-1, 0, 7})
	@Tag(STANDALONE)
	void testIllegalSlotSizeConstructor(int slotSize) {
		assertThrows(IllegalArgumentException.class, () -> new ByteAllocator(slotSize, ByteAllocator.MIN_CHUNK_POWER));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#ByteAllocator(int, int)}.
	 */
	@ParameterizedTest
	@ValueSource(ints = {-1, 0, ByteAllocator.MIN_CHUNK_POWER-1, ByteAllocator.MAX_CHUNK_POWER+1, Integer.MAX_VALUE})
	@Tag(STANDALONE)
	void testIllegalChunkPowerConstructor(int chunkpower) {
		assertThrows(IllegalArgumentException.class, () -> new ByteAllocator(ByteAllocator.MIN_SLOT_SIZE, chunkpower));
	}

	private Stream<DynamicTest> tests(BiConsumer<Config, ByteAllocator> action) {
		return configurations()
				.map(config -> dynamicTest(config.label(), () -> {
					try(ByteAllocator allocator = config.createInstance()) {
						action.accept(config, allocator);
					}
				}));
	}

	private Stream<DynamicNode> parameterizedTests(ObjIntConsumer<ByteAllocator> action, int...values) {
		return configurations()
				.map(config -> dynamicContainer(config.label(),
						IntStream.of(values)
						.mapToObj(value -> dynamicTest(String.valueOf(value), () -> {
							try(ByteAllocator allocator = config.createInstance()) {
								action.accept(allocator, value);
							}
						}))));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getChunkSize()}.
	 */
	@TestFactory
	Stream<DynamicTest> testGetChunkSize() {
		return tests((config, allocator) ->
			assertEquals(1<<config.chunkPower, allocator.getChunkSize()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getSlotSize()}.
	 */
	@TestFactory
	Stream<DynamicTest> testGetSlotSize() {
		return tests((config, allocator) ->
			assertEquals(config.slotSize, allocator.getSlotSize()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#alloc()}.
	 */
	@TestFactory
	Stream<DynamicTest> testAlloc() {
		return tests((config, allocator) ->
			assertTrue(allocator.alloc()>IcarusUtils.UNSET_INT));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#free(int)}.
	 */
	@TestFactory
	Stream<DynamicNode> testFreeIllegal() {
		return parameterizedTests((allocator, id) -> assertIOOB(() -> allocator.free(id)),
				-1, 0, 1);
	}

	@Disabled //TODO enable again
	@SuppressWarnings("boxing")
	@TestFactory
	@DisabledOnCi
	@RandomizedTest
	Stream<DynamicTest> testAllocationConsistency(TestReporter testReporter, RandomGenerator rand) {
		return tests((config, allocator) -> {
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
				long data = rand.nextLong();
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

				int id = rand.nextInt(SIZE);

				// Should only happen in later phase -> just continue
				if(!used[id]) {
					misses++;
					continue;
				}

				long data = allocator.getLong(id, offset);
				assertEquals(stored[id], data);

				int c = rand.nextInt(100);

				// Release with a 5% chance
				if(c < 5) {
					allocator.free(id);
					used[id] = false;
					alive--;
					freed++;
				} else
				// Modify with a 25% chance
				if(c < 30) {
					long newData = rand.nextLong();
					allocator.setLong(id, offset, newData);
					stored[id] = newData;
					changed++;
				} else
				// Repopulate a previously released slot with a 5% chance
				if(c < 35 && alive<SIZE) {
					int newId = allocator.alloc();
					assertTrue(newId!=IcarusUtils.UNSET_INT);
					assertFalse(used[newId]);
					long newData = rand.nextLong();
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
		});
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#clear()}.
	 */
	@TestFactory
	Stream<DynamicTest> testClear() {
		return tests((config, allocator) -> {
				allocator.alloc();

				assertEquals(1, allocator.size());
				assertEquals(1, allocator.chunksUsed());

				allocator.clear();

				assertEquals(0, allocator.size());
				assertEquals(0, allocator.chunksUsed());
			});
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#size()}.
	 */
	@TestFactory
	Stream<DynamicTest> testSizeEmpty() {
		return tests((config, allocator) -> assertEquals(0, allocator.size()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#chunksUsed()}.
	 */
	@TestFactory
	Stream<DynamicTest> testChunksUsedEmpty() {
		return tests((config, allocator) -> assertEquals(0, allocator.chunksUsed()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#size()}.
	 */
	@TestFactory
	Stream<DynamicTest> testSizeFilled() {
		return tests((config, allocator) ->  {
					int totalChunksToUse = 4;
					int totalSlotsToFill = totalChunksToUse * allocator.getChunkSize();

					for(int i=0; i<totalSlotsToFill; i++) {
						assertEquals(i, allocator.alloc());
						assertEquals(i+1, allocator.size());
					}

					assertEquals(totalChunksToUse, allocator.chunksUsed());
				});
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#trim()}.
	 */
	@TestFactory
	Stream<DynamicTest> testTrim() {
		return tests((config, allocator) ->  {
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
				});
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#newCursor()}.
	 */
	@TestFactory
	Stream<DynamicTest> testNewCursor() {
		return tests((config, allocator) ->  {
					Cursor cursor = allocator.newCursor();
					assertNotNull(cursor);
					assertSame(allocator, cursor.source());
				});
	}


	@FunctionalInterface
	interface InvalidOffsetTask {
		void execute(ByteAllocator allocator, int id, int offset);
	}

	@FunctionalInterface
	interface InvalidBatchTask {
		void execute(ByteAllocator allocator, int id, int offset, int n);
	}

	@FunctionalInterface
	interface CursorBatchTask {
		void execute(Cursor cursor, int id, int offset);
	}

	private Stream<DynamicNode> batchTests(Consumer<ByteAllocator> task) {
		return batchTests(UNSET_INT, task);
	}

	private Stream<DynamicNode> batchTests(int minSlotSize, Consumer<ByteAllocator> task) {
		return configurations(minSlotSize)
				.map(config -> dynamicContainer(config.label(),
						IntStream.rangeClosed(1, RUNS).mapToObj(run -> dynamicTest(runLabel(run, RUNS),
										() -> {
											try(ByteAllocator allocator = config.createInstance()) {
												task.accept(allocator);
											}
										}))));
	}

	private static String runLabel(int run, int runs) {
		return String.format("Run %d of %d", _int(run), _int(runs));
	}

	private static class Config {
		private final int slotSize;
		private final int chunkPower;
		private final LockType lockType;

		public Config(int slotSize, int chunkPower, LockType lockType) {
			super();
			this.slotSize = slotSize;
			this.chunkPower = chunkPower;
			this.lockType = lockType;
		}

		ByteAllocator createInstance() {
			return new ByteAllocator(slotSize, chunkPower, lockType);
		}

		String label() {
			return String.format("lock=%s slotSize=%d chunkPower=%d",
					lockType, _int(slotSize), _int(chunkPower));
		}
	}

	@Nested
	class ForInvalidIdOnEmptySlot {

		private void assertIdOutOfBounds(Executable executable) {
			IndexOutOfBoundsException ex = assertIOOB(executable);
			assertTrue(ex.getMessage().contains(" id "));
		}

		private final int[] INVALID_IDS = {-1,0,1};

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getByte(int, int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testGetByte() {
			return parameterizedTests((allocator, id) ->
				assertIdOutOfBounds(() -> allocator.getByte(id, 0)), INVALID_IDS);
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getNBytes(int, int, int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testGetNBytes() {
			return parameterizedTests((allocator, id) ->
				assertIdOutOfBounds(() -> allocator.getNBytes(id, 0, 1)), INVALID_IDS);
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getShort(int, int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testGetShort() {
			return parameterizedTests((allocator, id) ->
				assertIdOutOfBounds(() -> allocator.getShort(id, 0)), INVALID_IDS);
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getInt(int, int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testGetInt() {
			return parameterizedTests((allocator, id) ->
				assertIdOutOfBounds(() -> allocator.getInt(id, 0)), INVALID_IDS);
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getLong(int, int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testGetLong() {
			return parameterizedTests((allocator, id) ->
				assertIdOutOfBounds(() -> allocator.getLong(id, 0)), INVALID_IDS);
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#setByte(int, int, byte)}.
		 */
		@TestFactory
		Stream<DynamicNode> testSetByte() {
			return parameterizedTests((allocator, id) ->
				assertIdOutOfBounds(() -> allocator.setByte(id, 0, Byte.MAX_VALUE)), INVALID_IDS);
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#setNBytes(int, int, long, int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testSetNBytes() {
			return parameterizedTests((allocator, id) ->
				assertIdOutOfBounds(() -> allocator.setNBytes(id, 0, Long.MAX_VALUE, 3)), INVALID_IDS);
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#setShort(int, int, short)}.
		 */
		@TestFactory
		Stream<DynamicNode> testSetShort() {
			return parameterizedTests((allocator, id) ->
				assertIdOutOfBounds(() -> allocator.setShort(id, 0, Short.MAX_VALUE)), INVALID_IDS);
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#setInt(int, int, int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testSetInt() {
			return parameterizedTests((allocator, id) ->
				assertIdOutOfBounds(() -> allocator.setInt(id, 0, Integer.MAX_VALUE)), INVALID_IDS);
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#setLong(int, int, long)}.
		 */
		@TestFactory
		Stream<DynamicNode> testSetLong() {
			return parameterizedTests((allocator, id) ->
				assertIdOutOfBounds(() -> allocator.setLong(id, 0, Long.MAX_VALUE)), INVALID_IDS);
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#writeBytes(int, int, byte[], int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testWriteBytes() {
			return parameterizedTests((allocator, id) ->
				assertIdOutOfBounds(() -> allocator.writeBytes(id, 0, new byte[1], 1)), INVALID_IDS);
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#readBytes(int, int, byte[], int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testReadBytes() {
			return parameterizedTests((allocator, id) ->
				assertIdOutOfBounds(() -> allocator.readBytes(id, 0, new byte[1], 1)), INVALID_IDS);
		}
	}

	@Nested
	@DisplayName("expect IndexOutOfBoundsException for offsets")
	class ForInvalidOffsetOnFilledSlot {

		private void assertOffsetOutOfBounds(Executable executable) {
			IndexOutOfBoundsException ex = assertIOOB(executable);
			assertTrue(ex.getMessage().toLowerCase().contains("offset "));
		}

		private Stream<DynamicNode> invalidOffsetTests(ToIntFunction<Config> bytes, InvalidOffsetTask task) {
			return configurations()
					.map(config -> dynamicContainer(config.label(),
							IntStream.concat(
									IntStream.of(-1, config.slotSize, config.slotSize+1),
									IntStream.range(1, bytes.applyAsInt(config))
									.map(value -> config.slotSize-value)
							)
							.sorted()
							.mapToObj(value -> dynamicTest(String.valueOf(value), () -> {
								try(ByteAllocator allocator = config.createInstance()) {
									int id = allocator.alloc();
									assertOffsetOutOfBounds(() -> task.execute(allocator, id, value));
								}
							}))));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getByte(int, int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testGetByte() {
			return invalidOffsetTests(c -> Byte.BYTES, (alloc, id, offset) -> alloc.getByte(id, offset));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getNBytes(int, int, int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testGetNBytes1() {
			return IntStream.rangeClosed(1, 4)
					.mapToObj(bytes -> dynamicContainer(bytes+" bytes", invalidOffsetTests(
							c -> bytes, (alloc, id, offset) -> alloc.getNBytes(id, offset, bytes))));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getShort(int, int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testGetShort() {
			return invalidOffsetTests(c -> Short.BYTES, (alloc, id, offset) -> alloc.getShort(id, offset));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getInt(int, int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testGetInt() {
			return invalidOffsetTests(c -> Integer.BYTES, (alloc, id, offset) -> alloc.getInt(id, offset));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getLong(int, int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testGetLong() {
			return invalidOffsetTests(c -> Long.BYTES, (alloc, id, offset) -> alloc.getLong(id, offset));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#setByte(int, int, byte)}.
		 */
		@TestFactory
		Stream<DynamicNode> testSetByte() {
			return invalidOffsetTests(c -> Byte.BYTES,
					(alloc, id, offset) -> alloc.setByte(id, offset, Byte.MAX_VALUE));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#setNBytes(int, int, long, int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testSetNBytes() {
			return IntStream.rangeClosed(1, 4)
					.mapToObj(bytes -> dynamicContainer(bytes+" bytes", invalidOffsetTests(
							c -> bytes, (alloc, id, offset) -> alloc.setNBytes(id, offset, Long.MAX_VALUE, bytes))));
		}


		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#setShort(int, int, short)}.
		 */
		@TestFactory
		Stream<DynamicNode> testSetShort() {
			return invalidOffsetTests(c -> Short.BYTES,
					(alloc, id, offset) -> alloc.setShort(id, offset, Short.MAX_VALUE));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#setInt(int, int, int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testSetInt() {
			return invalidOffsetTests(c -> Integer.BYTES,
					(alloc, id, offset) -> alloc.setInt(id, offset, Integer.MAX_VALUE));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#setLong(int, int, long)}.
		 */
		@TestFactory
		Stream<DynamicNode> testSetLong() {
			return invalidOffsetTests(c -> Long.BYTES,
					(alloc, id, offset) -> alloc.setLong(id, offset, Long.MAX_VALUE));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#writeBytes(int, int, byte[], int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testWriteBytes() {
			return invalidOffsetTests(c -> 1,
					(alloc, id, offset) -> alloc.writeBytes(id, offset, new byte[1], 1));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#writeBytes(int, int, byte[], int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testWriteBytesRandom(RandomGenerator rand) {
			return configurations()
					.map(config -> dynamicContainer(config.label(),
							rand.ints(1, config.slotSize).distinct().limit(5).sorted()
				.boxed()
				.flatMap(n -> IntStream.range(config.slotSize-n.intValue()+1, config.slotSize)
						.mapToObj(offset -> DynamicTest.dynamicTest(
								String.format("n=%d offset=%d", n, _int(offset)),
								() -> {
									try(ByteAllocator allocator = config.createInstance()) {
										int id = allocator.alloc();
										assertOffsetOutOfBounds(
												() -> allocator.writeBytes(id, offset, new byte[config.slotSize], n.intValue()));
									}
								})))));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#readBytes(int, int, byte[], int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testReadBytes() {
			return invalidOffsetTests(c -> 1,
					(alloc, id, offset) -> alloc.readBytes(id, offset, new byte[1], 1));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#readBytes(int, int, byte[], int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testReadBytesRandom(RandomGenerator rand) {
			return configurations()
					.map(config -> dynamicContainer(config.label(),
							rand.ints(1, config.slotSize).distinct().limit(5).sorted()
					.boxed()
					.flatMap(n -> IntStream.range(config.slotSize-n.intValue()+1, config.slotSize)
							.mapToObj(offset -> DynamicTest.dynamicTest(
									String.format("n=%d offset=%d", n, _int(offset)),
									() -> {
										try(ByteAllocator allocator = config.createInstance()) {
											int id = allocator.alloc();
											assertOffsetOutOfBounds(
													() -> allocator.readBytes(id, offset, new byte[config.slotSize], n.intValue()));
										}
									})))));
		}
	}

	@Nested
	class ForInvalidBatchArgumentsOnFilledSlotOfSize8 {

		private void assertIllegalArgument(Executable executable) {
			assertThrows(IllegalArgumentException.class, executable);
		}

		private Stream<DynamicNode> invalidBatchTests(InvalidBatchTask task) {
			return configurations()
					.map(config -> dynamicContainer(config.label(),
							IntStream.of(-1, 0, config.slotSize+1)
							.sorted()
							.mapToObj(value -> dynamicTest(String.valueOf(value), () -> {
								try(ByteAllocator allocator = config.createInstance()) {
									int id = allocator.alloc();
									assertIllegalArgument(() -> task.execute(allocator, id, 0, value));
								}
							}))));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#writeBytes(int, int, byte[], int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testWriteBytes() {
			return invalidBatchTests((alloc, id, offset, n) -> alloc.writeBytes(id, 0, new byte[alloc.getSlotSize()], n));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#readBytes(int, int, byte[], int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testReadBytes() {
			return invalidBatchTests((alloc, id, offset, n) -> alloc.readBytes(id, 0, new byte[alloc.getSlotSize()], n));
		}
	}

	@Nested
	class WithRandomData {

		private static final int SIZE = 100;

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getByte(int, int)}.
		 */
		@RandomizedTest
		@TestFactory
		Stream<DynamicNode> testForByte(RandomGenerator rand) {
			return batchTests(alloc -> {
				final int offset = rand.nextInt(alloc.getSlotSize());
				byte[] array = new byte[SIZE]; // [id] = byte_stored

				rand.ints(SIZE, Byte.MIN_VALUE, Byte.MAX_VALUE)
					.forEach(v -> {
						int id = alloc.alloc();
						alloc.setByte(id, offset, (byte)v);
						array[id] = (byte) v;
					});

				IntStream.range(0, array.length)
					.forEach(id -> assertEquals(array[id], alloc.getByte(id, offset)));
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getNBytes(int, int, int)}.
		 */
		@RandomizedTest
		@TestFactory
		Stream<DynamicNode> testForNBytes(RandomGenerator rand) {
			return batchTests(alloc -> {
				final int offset = rand.nextInt(alloc.getSlotSize()-Long.BYTES+1);
				long[] array = new long[SIZE]; // [id] = long_stored
				int[] n_bytes = new int[SIZE]; // [id] = n

				rand.longs(SIZE, Long.MIN_VALUE, Long.MAX_VALUE)
					.forEach(v -> {
						int id = alloc.alloc();
						int n = rand.nextInt(Long.BYTES)+1;
						alloc.setNBytes(id, offset, v, n);
						array[id] = Bits.extractNBytes(v, n);
						n_bytes[id] = n;
					});

				IntStream.range(0, array.length)
					.forEach(id -> assertEquals(array[id], alloc.getNBytes(id, offset, n_bytes[id])));
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getShort(int, int)}.
		 */
		@RandomizedTest
		@TestFactory
		Stream<DynamicNode> testGetShort(RandomGenerator rand) {
			return batchTests(alloc -> {
				final int offset = rand.nextInt(alloc.getSlotSize()-Short.BYTES+1);
				short[] array = new short[SIZE]; // [id] = short_stored

				rand.ints(SIZE, Short.MIN_VALUE, Short.MAX_VALUE)
					.forEach(v -> {
						int id = alloc.alloc();
						alloc.setShort(id, offset, (short)v);
						array[id] = (short) v;
					});

				IntStream.range(0, array.length)
					.forEach(id -> assertEquals(array[id], alloc.getShort(id, offset)));
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getInt(int, int)}.
		 */
		@RandomizedTest
		@TestFactory
		Stream<DynamicNode> testGetInt(RandomGenerator rand) {
			return batchTests(alloc -> {
				final int offset = rand.nextInt(alloc.getSlotSize()-Integer.BYTES+1);
				int[] array = new int[SIZE]; // [id] = int_stored

				rand.ints(SIZE, Integer.MIN_VALUE, Integer.MAX_VALUE)
					.forEach(v -> {
						int id = alloc.alloc();
						alloc.setInt(id, offset, v);
						array[id] = v;
					});

				IntStream.range(0, array.length)
					.forEach(id -> assertEquals(array[id], alloc.getInt(id, offset)));
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getLong(int, int)}.
		 */
		@RandomizedTest
		@TestFactory
		Stream<DynamicNode> testGetLong(RandomGenerator rand) {
			return batchTests(alloc -> {
				final int offset = rand.nextInt(alloc.getSlotSize()-Long.BYTES+1);
				long[] array = new long[SIZE]; // [id] = long_stored

				rand.longs(SIZE, Long.MIN_VALUE, Long.MAX_VALUE)
					.forEach(v -> {
						int id = alloc.alloc();
						alloc.setLong(id, offset, v);
						array[id] = v;
					});

				IntStream.range(0, array.length)
					.forEach(id -> assertEquals(array[id], alloc.getLong(id, offset)));
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#writeBytes(int, int, byte[], int)}.
		 */
		@RandomizedTest
		@TestFactory
		Stream<DynamicNode> testForBulk(RandomGenerator rand) {
			return batchTests(alloc -> {
				final int offset = rand.nextInt(alloc.getSlotSize()-Long.BYTES+1);
				byte[][] array = new byte[SIZE][]; // [id] = bytes_stored

				rand.ints(SIZE, 1, alloc.getSlotSize()-offset)
					.forEach(n -> {
						int id = alloc.alloc();
						byte[] bytes = new byte[n];
						rand.nextBytes(bytes);
						alloc.writeBytes(id, offset, bytes, n);
						array[id] = bytes;
					});

				IntStream.range(0, array.length)
					.forEach(id -> {
						byte[] expected = array[id];
						byte[] actual = new byte[expected.length];
						alloc.readBytes(id, offset, actual, actual.length);
						assertArrayEquals(expected, actual);
					});
			});
		}
	}

	@Nested
	class WhenShrinking {

		private static final int SIZE = 100;

		private static final int MIN_SLOT_SIZE = ByteAllocator.MIN_SLOT_SIZE*2;

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getByte(int, int)}.
		 */
		@RandomizedTest
		@TestFactory
		Stream<DynamicNode> testForByte(RandomGenerator rand) {
			return batchTests(MIN_SLOT_SIZE, alloc -> {
				final int offset = rand.nextInt(alloc.getSlotSize());
				byte[] array = new byte[SIZE]; // [id] = byte_stored

				rand.ints(SIZE, Byte.MIN_VALUE, Byte.MAX_VALUE)
					.forEach(v -> {
						int id = alloc.alloc();
						alloc.setByte(id, offset, (byte)v);
						array[id] = (byte) v;
					});

				int newSlotSize = alloc.getSlotSize()/2;
				alloc.adjustSlotSize(newSlotSize);

				if(offset<newSlotSize) {
					IntStream.range(0, array.length)
						.forEach(id -> assertEquals(array[id], alloc.getByte(id, offset)));
				} else {
					IntStream.range(0, array.length)
						.forEach(id -> assertIOOB(() -> alloc.getByte(id, offset)));
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getNBytes(int, int, int)}.
		 */
		@RandomizedTest
		@TestFactory
		Stream<DynamicNode> testForNBytes(RandomGenerator rand) {
			return batchTests(MIN_SLOT_SIZE, alloc -> {
				final int offset = rand.nextInt(alloc.getSlotSize()-Long.BYTES+1);
				long[] array = new long[SIZE]; // [id] = long_stored
				int[] n_bytes = new int[SIZE]; // [id] = n

				rand.longs(SIZE, Long.MIN_VALUE, Long.MAX_VALUE)
					.forEach(v -> {
						int id = alloc.alloc();
						int n = rand.nextInt(Long.BYTES-2)+1;
						alloc.setNBytes(id, offset, v, n);
						array[id] = Bits.extractNBytes(v, n);
						n_bytes[id] = n;
					});

				int newSlotSize = alloc.getSlotSize()/2;
				alloc.adjustSlotSize(newSlotSize);

				IntStream.range(0, array.length)
					.forEach(id -> {
						if(offset+n_bytes[id]<=newSlotSize) {
							assertEquals(array[id], alloc.getNBytes(
								id, offset, n_bytes[id]));
						} else {
							assertIOOB(() -> alloc.getNBytes(id, offset, n_bytes[id]));
						}
					});
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getShort(int, int)}.
		 */
		@RandomizedTest
		@TestFactory
		Stream<DynamicNode> testForShort(RandomGenerator rand) {
			return batchTests(MIN_SLOT_SIZE, alloc -> {
				final int offset = rand.nextInt(alloc.getSlotSize()-Short.BYTES+1);
				short[] array = new short[SIZE]; // [id] = short_stored

				rand.ints(SIZE, Short.MIN_VALUE, Short.MAX_VALUE)
					.forEach(v -> {
						int id = alloc.alloc();
						alloc.setShort(id, offset, (short)v);
						array[id] = (short) v;
					});

				int newSlotSize = alloc.getSlotSize()/2;
				alloc.adjustSlotSize(newSlotSize);

				if(offset+Short.BYTES<=newSlotSize) {
					IntStream.range(0, array.length)
						.forEach(id -> assertEquals(array[id], alloc.getShort(id, offset)));
				} else {
					IntStream.range(0, array.length)
						.forEach(id -> assertIOOB(() -> alloc.getShort(id, offset)));
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getInt(int, int)}.
		 */
		@RandomizedTest
		@TestFactory
		Stream<DynamicNode> testForInt(RandomGenerator rand) {
			return batchTests(MIN_SLOT_SIZE, alloc -> {
				final int offset = rand.nextInt(alloc.getSlotSize()-Integer.BYTES+1);
				int[] array = new int[SIZE]; // [id] = int_stored

				rand.ints(SIZE, Integer.MIN_VALUE, Integer.MAX_VALUE)
					.forEach(v -> {
						int id = alloc.alloc();
						alloc.setInt(id, offset, v);
						array[id] = v;
					});

				int newSlotSize = alloc.getSlotSize()/2;
				alloc.adjustSlotSize(newSlotSize);

				if(offset+Integer.BYTES<=newSlotSize) {
					IntStream.range(0, array.length)
						.forEach(id -> assertEquals(array[id], alloc.getInt(id, offset)));
				} else {
					IntStream.range(0, array.length)
						.forEach(id -> assertIOOB(() -> alloc.getInt(id, offset)));
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getLong(int, int)}.
		 */
		@RandomizedTest
		@TestFactory
		Stream<DynamicNode> testForLong(RandomGenerator rand) {
			return batchTests(MIN_SLOT_SIZE, alloc -> {
				final int offset = rand.nextInt(alloc.getSlotSize()-Long.BYTES+1);
				long[] array = new long[SIZE]; // [id] = long_stored

				rand.longs(SIZE, Long.MIN_VALUE, Long.MAX_VALUE)
					.forEach(v -> {
						int id = alloc.alloc();
						alloc.setLong(id, offset, v);
						array[id] = v;
					});

				int newSlotSize = alloc.getSlotSize()/2;
				alloc.adjustSlotSize(newSlotSize);

				if(offset+Long.BYTES<=newSlotSize) {
					IntStream.range(0, array.length)
						.forEach(id -> assertEquals(array[id], alloc.getLong(id, offset)));
				} else {
					IntStream.range(0, array.length)
						.forEach(id -> assertIOOB(() -> alloc.getLong(id, offset)));
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#writeBytes(int, int, byte[], int)}.
		 */
		@RandomizedTest
		@TestFactory
		Stream<DynamicNode> testForBulk(RandomGenerator rand) {
			return batchTests(MIN_SLOT_SIZE, alloc -> {
				final int offset = rand.nextInt(alloc.getSlotSize()-Long.BYTES+1);
				byte[][] array = new byte[SIZE][]; // [id] = bytes_stored

				rand.ints(SIZE, 1, alloc.getSlotSize()-offset)
					.forEach(n -> {
						int id = alloc.alloc();
						byte[] bytes = new byte[n];
						rand.nextBytes(bytes);
						alloc.writeBytes(id, offset, bytes, n);
						array[id] = bytes;
					});

				int newSlotSize = alloc.getSlotSize()/2;
				alloc.adjustSlotSize(newSlotSize);

				IntStream.range(0, array.length)
					.forEach(id -> {
						byte[] expected = array[id];
						byte[] actual = new byte[expected.length];
						if(offset+expected.length<=newSlotSize) {
							alloc.readBytes(id, offset, actual, actual.length);
							assertArrayEquals(expected, actual);
						} else {
							assertIOOB(() -> alloc.readBytes(id, offset, actual, actual.length));
						}
					});
			});
		}
	}

	@Nested
	class WhenGrowing {

		private static final int SIZE = 100;

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getByte(int, int)}.
		 */
		@RandomizedTest
		@TestFactory
		Stream<DynamicNode> testForByte(RandomGenerator rand) {
			return batchTests(alloc -> {
				final int offset = rand.nextInt(alloc.getSlotSize());
				byte[] array = new byte[SIZE]; // [id] = byte_stored

				rand.ints(SIZE, Byte.MIN_VALUE, Byte.MAX_VALUE)
					.forEach(v -> {
						int id = alloc.alloc();
						alloc.setByte(id, offset, (byte)v);
						array[id] = (byte) v;
					});

				int newSlotSize = alloc.getSlotSize()*2;
				alloc.adjustSlotSize(newSlotSize);

				IntStream.range(0, array.length)
					.forEach(id -> assertEquals(array[id], alloc.getByte(id, offset)));
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getNBytes(int, int, int)}.
		 */
		@RandomizedTest
		@TestFactory
		Stream<DynamicNode> testForNBytes(RandomGenerator rand) {
			return batchTests(alloc -> {
				final int offset = rand.nextInt(alloc.getSlotSize()-Long.BYTES+1);
				long[] array = new long[SIZE]; // [id] = long_stored
				int[] n_bytes = new int[SIZE]; // [id] = n

				rand.longs(SIZE, Long.MIN_VALUE, Long.MAX_VALUE)
					.forEach(v -> {
						int id = alloc.alloc();
						int n = rand.nextInt(Long.BYTES-2)+1;
						alloc.setNBytes(id, offset, v, n);
						array[id] = Bits.extractNBytes(v, n);
						n_bytes[id] = n;
					});

				int newSlotSize = alloc.getSlotSize()*2;
				alloc.adjustSlotSize(newSlotSize);

				IntStream.range(0, array.length)
					.forEach(id -> assertEquals(array[id], alloc.getNBytes(id, offset, n_bytes[id])));
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getShort(int, int)}.
		 */
		@RandomizedTest
		@TestFactory
		Stream<DynamicNode> testGetShort(RandomGenerator rand) {
			return batchTests(alloc -> {
				final int offset = rand.nextInt(alloc.getSlotSize()-Short.BYTES+1);
				short[] array = new short[SIZE]; // [id] = short_stored

				rand.ints(SIZE, Short.MIN_VALUE, Short.MAX_VALUE)
					.forEach(v -> {
						int id = alloc.alloc();
						alloc.setShort(id, offset, (short)v);
						array[id] = (short) v;
					});

				int newSlotSize = alloc.getSlotSize()*2;
				alloc.adjustSlotSize(newSlotSize);

				IntStream.range(0, array.length)
					.forEach(id -> assertEquals(array[id], alloc.getShort(id, offset)));
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getInt(int, int)}.
		 */
		@RandomizedTest
		@TestFactory
		Stream<DynamicNode> testGetInt(RandomGenerator rand) {
			return batchTests(alloc -> {
				final int offset = rand.nextInt(alloc.getSlotSize()-Integer.BYTES+1);
				int[] array = new int[SIZE]; // [id] = int_stored

				rand.ints(SIZE, Integer.MIN_VALUE, Integer.MAX_VALUE)
					.forEach(v -> {
						int id = alloc.alloc();
						alloc.setInt(id, offset, v);
						array[id] = v;
					});

				int newSlotSize = alloc.getSlotSize()*2;
				alloc.adjustSlotSize(newSlotSize);

				IntStream.range(0, array.length)
					.forEach(id -> assertEquals(array[id], alloc.getInt(id, offset)));
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#getLong(int, int)}.
		 */
		@RandomizedTest
		@TestFactory
		Stream<DynamicNode> testGetLong(RandomGenerator rand) {
			return batchTests(alloc -> {
				final int offset = rand.nextInt(alloc.getSlotSize()-Long.BYTES+1);
				long[] array = new long[SIZE]; // [id] = long_stored

				rand.longs(SIZE, Long.MIN_VALUE, Long.MAX_VALUE)
					.forEach(v -> {
						int id = alloc.alloc();
						alloc.setLong(id, offset, v);
						array[id] = v;
					});

				int newSlotSize = alloc.getSlotSize()*2;
				alloc.adjustSlotSize(newSlotSize);

				IntStream.range(0, array.length)
					.forEach(id -> assertEquals(array[id], alloc.getLong(id, offset)));
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator#writeBytes(int, int, byte[], int)}.
		 */
		@RandomizedTest
		@TestFactory
		Stream<DynamicNode> testForBulk(RandomGenerator rand) {
			return batchTests(alloc -> {
				final int offset = rand.nextInt(alloc.getSlotSize()-Long.BYTES+1);
				byte[][] array = new byte[SIZE][]; // [id] = bytes_stored

				rand.ints(SIZE, 1, alloc.getSlotSize()-offset)
					.forEach(n -> {
						int id = alloc.alloc();
						byte[] bytes = new byte[n];
						rand.nextBytes(bytes);
						alloc.writeBytes(id, offset, bytes, n);
						array[id] = bytes;
					});

				int newSlotSize = alloc.getSlotSize()*2;
				alloc.adjustSlotSize(newSlotSize);

				IntStream.range(0, array.length)
					.forEach(id -> {
						byte[] expected = array[id];
						byte[] actual = new byte[expected.length];
						alloc.readBytes(id, offset, actual, actual.length);
						assertArrayEquals(expected, actual);
					});
			});
		}
	}

	//TODO rework cursor tests

	@Nested
	class ForCursor {

		private Stream<DynamicNode> cursorTests(Consumer<Cursor> task) {
			return configurations()
					.map(config -> dynamicTest(config.label(), () -> {
							try(ByteAllocator allocator = config.createInstance()) {
								Cursor cursor = allocator.newCursor();
								task.accept(cursor);
							}
					}));
		}

		private Stream<DynamicNode> parameterizedCursorTests(ObjIntConsumer<Cursor> task, int...ids) {
			return configurations()
					.map(config -> dynamicContainer(config.label(),
							IntStream.of(ids).mapToObj(id -> dynamicTest(String.valueOf(id), () -> {
								try(ByteAllocator allocator = config.createInstance()) {
									Cursor cursor = allocator.newCursor();
									task.accept(cursor, id);
								}
						}))));
		}

		private void fill(Cursor cursor, int slots) {
			while(slots-->0)
				cursor.source().alloc();
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#clear()}.
		 */
		@TestFactory
		Stream<DynamicNode> testClear() {
			return cursorTests(cursor -> {
				assertFalse(cursor.hasChunk());

				cursor.source().alloc();
				cursor.moveTo(0);

				assertTrue(cursor.hasChunk());

				cursor.clear();

				assertFalse(cursor.hasChunk());
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#clear()}.
		 */
		@TestFactory
		Stream<DynamicNode> testClearViaMove() {
			return cursorTests(cursor -> {
				assertFalse(cursor.hasChunk());

				cursor.source().alloc();
				cursor.moveTo(0);

				assertTrue(cursor.hasChunk());

				cursor.moveTo(UNSET_INT); // has _clear_ semantics

				assertFalse(cursor.hasChunk());
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#moveTo(int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testMoveToEmpty() {
			return cursorTests(cursor -> assertIOOB(() -> cursor.moveTo(0)));
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#moveTo(int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testMoveToOutOfBounds() {
			return parameterizedCursorTests((cursor, id) -> {
				fill(cursor, 3);
				assertIOOB(() -> cursor.moveTo(id));
			}, -2, 10);
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#moveTo(int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testMoveToUnsetInt() {
			return cursorTests(cursor -> {
				fill(cursor, 3);

				cursor.moveTo(1);
				assertTrue(cursor.hasChunk());

				cursor.moveTo(IcarusUtils.UNSET_INT);
				assertFalse(cursor.hasChunk());
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#moveTo(int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testMoveTo() {
			return cursorTests(cursor -> {
				int slotSize = cursor.source().getSlotSize();
				fill(cursor, slotSize);

				IntStream.range(0, slotSize)
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
				cursor.source().free(0);
				assertThrows(IllegalStateException.class, () -> cursor.moveTo(0));
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getId()}.
		 */
		@TestFactory
		Stream<DynamicNode> testGetId() {
			return cursorTests(cursor -> {
				int slotSize = cursor.source().getSlotSize();
				fill(cursor, slotSize);

				assertEquals(IcarusUtils.UNSET_INT, cursor.getId());

				IntStream.range(0, slotSize)
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
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#hasChunk()}.
		 */
		@TestFactory
		Stream<DynamicNode> testHasChunk() {
			return cursorTests(cursor -> {
				assertFalse(cursor.hasChunk());

				cursor.alloc();

				assertTrue(cursor.hasChunk());

				cursor.clear();

				assertFalse(cursor.hasChunk());
			});
		}

		@Nested
		class WithRandomData {

			private static final int SIZE = 100;

			private Stream<DynamicNode> shuffledTests(ToIntFunction<ByteAllocator> bytes,
					int runs, RandomGenerator rand,
					CursorBatchTask writer, CursorBatchTask reader) {
				return configurations()
						.map(config -> dynamicContainer(config.label(),
								IntStream.rangeClosed(1, runs).mapToObj(run -> dynamicTest(
										runLabel(run, runs), () -> {
									try(ByteAllocator allocator = config.createInstance()) {
										// Allocate sufficient slots
										int[] ids = new int[SIZE];
										for (int i = 0; i < SIZE; i++) {
											ids[i] = allocator.alloc();
										}

										Cursor cursor = allocator.newCursor();
										int offset = rand.nextInt(
												allocator.getSlotSize()-bytes.applyAsInt(allocator)+1);

										rand.shuffle(ids);

										// Write everything
										for (int id : ids) {
											cursor.moveTo(id);
											assertTrue(cursor.hasChunk());
											writer.execute(cursor, id, offset);
										}

										// Ensure we don't use the same order
										rand.shuffle(ids);

										// Read and verify everything again
										for(int id : ids) {
											cursor.moveTo(id);
											assertTrue(cursor.hasChunk());
											reader.execute(cursor, id, offset);
										}
									}
							}))));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getByte(int)}.
			 */
			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testBytes(RandomGenerator rand) {
				final int[] values = rand
						.ints(SIZE, Byte.MIN_VALUE, Byte.MAX_VALUE)
						.toArray(); // [id] = byte_value

				return shuffledTests(alloc -> Byte.BYTES, RUNS, rand,
						(c, id, offset) -> {
							byte v = (byte)values[id];
							c.setByte(offset, v);
							assertEquals(v, c.getByte(offset));
						},
						(c, id, offset) -> {
							byte v = (byte)values[id];
							assertEquals(v, c.getByte(offset));
						});
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getShort(int)}.
			 */
			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testShort(RandomGenerator rand) {
				final int[] values = rand
						.ints(SIZE, Short.MIN_VALUE, Short.MAX_VALUE)
						.toArray(); // [id] = short_value

				return shuffledTests(alloc -> Short.BYTES, RUNS, rand,
						(c, id, offset) -> {
							short v = (short)values[id];
							c.setShort(offset, v);
							assertEquals(v, c.getShort(offset));
						},
						(c, id, offset) -> {
							short v = (short)values[id];
							assertEquals(v, c.getShort(offset));
						});
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getInt(int)}.
			 */
			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testInt(RandomGenerator rand) {
				final int[] values = rand
						.ints(SIZE, Integer.MIN_VALUE, Integer.MAX_VALUE)
						.toArray(); // [id] = int_value

				return shuffledTests(alloc -> Integer.BYTES, RUNS, rand,
						(c, id, offset) -> {
							int v = values[id];
							c.setInt(offset, v);
							assertEquals(v, c.getInt(offset));
						},
						(c, id, offset) -> {
							int v = values[id];
							assertEquals(v, c.getInt(offset));
						});
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getLong(int)}.
			 */
			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testLong(RandomGenerator rand) {
				final long[] values = rand
						.longs(SIZE, Long.MIN_VALUE, Long.MAX_VALUE)
						.toArray(); // [id] = long_value

				return shuffledTests(alloc -> Long.BYTES, RUNS, rand,
						(c, id, offset) -> {
							long v = values[id];
							c.setLong(offset, v);
							assertEquals(v, c.getLong(offset));
						},
						(c, id, offset) -> {
							long v = values[id];
							assertEquals(v, c.getLong(offset));
						});
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getNBytes(int, int)}.
			 */
			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testNBytes(RandomGenerator rand) {
				final long[] values = rand
						.longs(SIZE, Long.MIN_VALUE, Long.MAX_VALUE)
						.toArray(); // [id] = long_value
				final int n = rand.nextInt(8)+1;

				return shuffledTests(alloc -> n, RUNS, rand,
						(c, id, offset) -> {
							long v = values[id];
							c.setNBytes(offset, v, n);
							assertEquals(Bits.extractNBytes(v, n), c.getNBytes(offset, n));
						},
						(c, id, offset) -> {
							long v = values[id];
							assertEquals(Bits.extractNBytes(v, n), c.getNBytes(offset, n));
						});
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#writeBytes(int, byte[], int)}.
			 */
			@TestFactory
			@RandomizedTest
			Stream<DynamicNode> testBytesArray(RandomGenerator rand) {
				final byte[][] values = new byte[SIZE][]; // [id] = long_value
				final int n = rand.nextInt(8)+1;

				for (int i = 0; i < values.length; i++) {
					values[i] = new byte[n];
					rand.nextBytes(values[i]);
				}

				return shuffledTests(alloc -> n, RUNS, rand,
						(c, id, offset) -> {
							byte[] v = values[id];
							c.writeBytes(offset, v, n);
							byte[] read = new byte[n];
							c.readBytes(offset, read, n);
							assertArrayEquals(v, read);
						},
						(c, id, offset) -> {
							byte[] v = values[id];
							byte[] read = new byte[n];
							c.readBytes(offset, read, n);
							assertArrayEquals(v, read);
						});
			}
		}

		@Nested
		class ForInvalidBatchSizes {

			private void assertIllegalArgument(Executable executable) {
				assertThrows(IllegalArgumentException.class, executable);
			}

			private Stream<DynamicNode> invalidBatchTests(ObjIntConsumer<Cursor> task) {
				return configurations()
						.map(config -> dynamicContainer(config.label(),
								IntStream.of(-1, 0, config.slotSize+1)
								.sorted()
								.mapToObj(value -> dynamicTest(String.valueOf(value), () -> {
									try(ByteAllocator allocator = config.createInstance()) {
										Cursor cursor = allocator.newCursor();
										cursor.alloc();
										assertIllegalArgument(() -> task.accept(cursor, value));
									}
								}))));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getNBytes(int, int)}.
			 */
			@TestFactory
			Stream<DynamicNode> testGetNBytes() {
				return invalidBatchTests((c, n) -> c.getNBytes(0, n));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#setNBytes(int, long, int)}.
			 */
			@TestFactory
			Stream<DynamicNode> testSetNBytes() {
				return invalidBatchTests((c, n) -> c.setNBytes(0, Long.MAX_VALUE, n));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#writeBytes(int, byte[], int)}.
			 */
			@TestFactory
			Stream<DynamicNode> testWriteBytes() {
				return invalidBatchTests((c, n) -> c.writeBytes(0, new byte[c.source().getSlotSize()], n));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#readBytes(int, byte[], int)}.
			 */
			@TestFactory
			Stream<DynamicNode> testReadBytes() {
				return invalidBatchTests((c, n) -> c.readBytes(0, new byte[c.source().getSlotSize()], n));
			}
		}

		@Nested
		class ForOffsetOutOfBounds {

			private void assertOffsetOutOfBounds(Executable executable) {
				IndexOutOfBoundsException ex = assertIOOB(executable);
				assertTrue(ex.getMessage().toLowerCase().contains("offset"));
			}

			private Stream<DynamicNode> invalidOffsetTests(ToIntFunction<Config> bytes, ObjIntConsumer<Cursor> task) {
				return configurations()
						.map(config -> dynamicContainer(config.label(),
								IntStream.of(-1, config.slotSize, config.slotSize+1)
								.sorted()
								.mapToObj(value -> dynamicTest(String.valueOf(value), () -> {
									try(ByteAllocator allocator = config.createInstance()) {
										Cursor cursor = allocator.newCursor();
										cursor.alloc();
										assertOffsetOutOfBounds(() -> task.accept(cursor, value));
									}
								}))));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getByte(int)}.
			 */
			@TestFactory
			Stream<DynamicNode> testGetByte() {
				return invalidOffsetTests(c -> Byte.BYTES, (c, offset) -> c.getByte(offset));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getNBytes(int, int)}.
			 */
			@TestFactory
			Stream<DynamicNode> testGetNBytes() {
				return invalidOffsetTests(c -> Byte.BYTES, (c, offset) -> c.getNBytes(offset, 1));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getShort(int)}.
			 */
			@TestFactory
			Stream<DynamicNode> testGetShort() {
				return invalidOffsetTests(c -> Short.BYTES, (c, offset) -> c.getShort(offset));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getInt(int)}.
			 */
			@TestFactory
			Stream<DynamicNode> testGetInt() {
				return invalidOffsetTests(c -> Integer.BYTES, (c, offset) -> c.getInt(offset));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getLong(int)}.
			 */
			@TestFactory
			Stream<DynamicNode> testGetLong() {
				return invalidOffsetTests(c -> Long.BYTES, (c, offset) -> c.getLong(offset));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#setByte(int, byte)}.
			 */
			@TestFactory
			Stream<DynamicNode> testSetByte() {
				return invalidOffsetTests(c -> Byte.BYTES, (c, offset) -> c.setByte(offset, Byte.MAX_VALUE));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#setNBytes(int, long, int)}.
			 */
			@TestFactory
			Stream<DynamicNode> testSetNBytes() {
				return invalidOffsetTests(c -> Byte.BYTES, (c, offset) -> c.setNBytes(offset, Byte.MAX_VALUE, 1));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#setShort(int, short)}.
			 */
			@TestFactory
			Stream<DynamicNode> testSetShort() {
				return invalidOffsetTests(c -> Short.BYTES, (c, offset) -> c.setShort(offset,  Short.MAX_VALUE));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#setInt(int, int)}.
			 */
			@TestFactory
			Stream<DynamicNode> testSetInt() {
				return invalidOffsetTests(c -> Integer.BYTES, (c, offset) -> c.setInt(offset,  Integer.MAX_VALUE));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#setLong(int, long)}.
			 */
			@TestFactory
			Stream<DynamicNode> testSetLong() {
				return invalidOffsetTests(c -> Long.BYTES, (c, offset) -> c.setLong(offset,  Long.MAX_VALUE));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#writeBytes(int, byte[], int)}.
			 */
			@TestFactory
			Stream<DynamicNode> testWriteBytes() {
				return invalidOffsetTests(c -> Byte.BYTES, (c, offset) -> c.writeBytes(offset, new byte[1], 1));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#readBytes(int, byte[], int)}.
			 */
			@TestFactory
			Stream<DynamicNode> testReadBytes() {
				return invalidOffsetTests(c -> Byte.BYTES, (c, offset) -> c.readBytes(offset, new byte[1], 1));
			}
		}

		@Nested
		class ForChunkNotAvailable {

			private void assertIllegalState(Executable executable) {
				assertThrows(IllegalStateException.class, executable);
			}

			private Stream<DynamicNode> invalidOffsetTests(Consumer<Cursor> task) {
				return configurations()
						.map(config -> dynamicTest(config.label(), () -> {
							try(ByteAllocator allocator = config.createInstance()) {
								Cursor cursor = allocator.newCursor();
								assertIllegalState(() -> task.accept(cursor));
							}
						}));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getByte(int)}.
			 */
			@TestFactory
			Stream<DynamicNode> testGetByte() {
				return invalidOffsetTests(c -> c.getByte(0));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getNBytes(int, int)}.
			 */
			@TestFactory
			Stream<DynamicNode> testGetNBytes() {
				return invalidOffsetTests(c -> c.getNBytes(0, 1));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getShort(int)}.
			 */
			@TestFactory
			Stream<DynamicNode> testGetShort() {
				return invalidOffsetTests(c -> c.getShort(0));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getInt(int)}.
			 */
			@TestFactory
			Stream<DynamicNode> testGetInt() {
				return invalidOffsetTests(c -> c.getInt(0));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#getLong(int)}.
			 */
			@TestFactory
			Stream<DynamicNode> testGetLong() {
				return invalidOffsetTests(c -> c.getLong(0));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#setByte(int, byte)}.
			 */
			@TestFactory
			Stream<DynamicNode> testSetByte() {
				return invalidOffsetTests(c -> c.setByte(0, Byte.MAX_VALUE));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#setNBytes(int, long, int)}.
			 */
			@TestFactory
			Stream<DynamicNode> testSetNBytes() {
				return invalidOffsetTests(c -> c.setNBytes(0, Long.MAX_VALUE, 1));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#setShort(int, short)}.
			 */
			@TestFactory
			Stream<DynamicNode> testSetShort() {
				return invalidOffsetTests(c -> c.setShort(0, Short.MAX_VALUE));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#setInt(int, int)}.
			 */
			@TestFactory
			Stream<DynamicNode> testSetInt() {
				return invalidOffsetTests(c -> c.setInt(0, Integer.MAX_VALUE));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#setLong(int, long)}.
			 */
			@TestFactory
			Stream<DynamicNode> testSetLong() {
				return invalidOffsetTests(c -> c.setLong(0, Long.MAX_VALUE));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#writeBytes(int, byte[], int)}.
			 */
			@TestFactory
			Stream<DynamicNode> testWriteBytes() {
				return invalidOffsetTests(c -> c.writeBytes(0, new byte[1], 1));
			}

			/**
			 * Test method for {@link de.ims.icarus2.util.mem.ByteAllocator.Cursor#readBytes(int, byte[], int)}.
			 */
			@TestFactory
			Stream<DynamicNode> testReadBytes() {
				return invalidOffsetTests(c -> c.readBytes(0, new byte[1], 1));
			}
		}
	}
}
