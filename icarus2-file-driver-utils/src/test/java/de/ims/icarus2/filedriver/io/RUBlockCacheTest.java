/**
 *
 */
package de.ims.icarus2.filedriver.io;

import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.test.TestUtils.assertIAE;
import static de.ims.icarus2.test.TestUtils.assertISE;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.util.IcarusUtils.MAX_INTEGER_INDEX;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.filedriver.io.BufferedIOResource.Block;
import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.guard.ApiGuard;

/**
 * @author Markus Gärtner
 *
 */
class RUBlockCacheTest {

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.RUBlockCache#newLeastRecentlyUsedCache()}.
	 */
	@Test
	void testNewLeastRecentlyUsedCache() {
		assertTrue(RUBlockCache.newLeastRecentlyUsedCache().isLRU());
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.RUBlockCache#newMostRecentlyUsedCache()}.
	 */
	@Test
	void testNewMostRecentlyUsedCache() {
		assertFalse(RUBlockCache.newMostRecentlyUsedCache().isLRU());
	}

	private static int randomId() {
		return random(0, MAX_INTEGER_INDEX);
	}

	private static Block block() {
		return new Block(new Object());
	}

	private static Block block(int id) {
		Block block = block();
		block.setId(id);
		return block;
	}

	interface TestBase extends ApiGuardedTest<RUBlockCache> {

		/**
		 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
		 */
		@Override
		default Class<?> getTestTargetClass() {
			return RUBlockCache.class;
		}

		/**
		 * @see de.ims.icarus2.test.ApiGuardedTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
		 */
		@Override
		default void configureApiGuard(ApiGuard<RUBlockCache> apiGuard) {
			ApiGuardedTest.super.configureApiGuard(apiGuard);

			apiGuard.parameterResolver(Block.class, cache -> block());
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.RUBlockCache#getBlock(int)}.
		 */
		@Test
		default void testGetBlockBlank() {
			try(RUBlockCache cache = create()) {
				assertThrows(RuntimeException.class, () -> cache.getBlock(randomId()));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.RUBlockCache#getBlock(int)}.
		 */
		@Test
		default void testGetBlockEmpty() {
			try(RUBlockCache cache = create()) {
				cache.open(100);
				assertNull(cache.getBlock(randomId()));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.RUBlockCache#getBlock(int)}.
		 */
		@Test
		default void testGetBlockForeign() {
			try(RUBlockCache cache = create()) {
				cache.open(200);
				IntStream.generate(() -> random(0, 100))
					.distinct()
					.limit(RUNS)
					.forEach(id -> assertNull(cache.addBlock(block(id))));
				assertNull(cache.getBlock(101));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.RUBlockCache#getBlock(int)}.
		 */
		@Test
		default void testGetBlock() {
			try(RUBlockCache cache = create()) {
				cache.open(100);
				int id = randomId();
				Block block = block(id);

				assertNull(cache.addBlock(block));
				assertSame(block, cache.getBlock(id));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.RUBlockCache#addBlock(de.ims.icarus2.filedriver.io.BufferedIOResource.Block, int)}.
		 */
		@Test
		default void testAddBlockBlank() {
			try(RUBlockCache cache = create()) {
				assertThrows(RuntimeException.class, () -> cache.addBlock(block()));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.RUBlockCache#addBlock(de.ims.icarus2.filedriver.io.BufferedIOResource.Block, int)}.
		 */
		@Test
		default void testAddBlock() {
			try(RUBlockCache cache = create()) {
				cache.open(100);
				assertNull(cache.addBlock(block(randomId())));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.RUBlockCache#addBlock(de.ims.icarus2.filedriver.io.BufferedIOResource.Block, int)}.
		 */
		@Test
		default void testAddBlockDublicate() {
			try(RUBlockCache cache = create()) {
				cache.open(100);
				Block block = block(randomId());

				assertNull(cache.addBlock(block));
				assertISE(() -> cache.addBlock(block));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.RUBlockCache#addBlock(de.ims.icarus2.filedriver.io.BufferedIOResource.Block, int)}.
		 */
		@Test
		default void testAddBlockOverflow() {
			try(RUBlockCache cache = create()) {
				int capacty = BlockCache.MIN_CAPACITY;
				cache.open(capacty);
				Block[] blocks = new Block[capacty];
				for (int i = 0; i < capacty; i++) {
					Block block = blocks[i] = block(i);
					assertNull(cache.addBlock(block));
				}

				Block overflow = cache.addBlock(block(MAX_INTEGER_INDEX));
				if(cache.isLRU()) {
					assertSame(blocks[0], overflow);
				} else {
					assertSame(blocks[capacty-1], overflow);
				}
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.RUBlockCache#addBlock(de.ims.icarus2.filedriver.io.BufferedIOResource.Block, int)}.
		 */
		@Test
		default void testAddBlockLockedOverflow() {
			try(RUBlockCache cache = create()) {
				int capacty = BlockCache.MIN_CAPACITY;
				cache.open(capacty);
				Block[] blocks = new Block[capacty];
				for (int i = 0; i < capacty; i++) {
					Block block = blocks[i] = block(i);
					assertNull(cache.addBlock(block));
				}

				// Now lock all blocks
				for (int i = 0; i < blocks.length; i++) {
					blocks[i].lock();
				}

				assertISE(() -> cache.addBlock(block(MAX_INTEGER_INDEX)));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.RUBlockCache#addBlock(de.ims.icarus2.filedriver.io.BufferedIOResource.Block, int)}.
		 */
		@Test
		default void testReshash() {
			try(RUBlockCache cache = create()) {
				int capacty = BlockCache.MIN_CAPACITY*3;
				cache.open(capacty);
				Block[] blocks = new Block[capacty];
				for (int i = 0; i < capacty; i++) {
					Block block = blocks[i] = block(i);
					assertNull(cache.addBlock(block));
				}

				for (int i = 0; i < blocks.length; i++) {
					assertSame(blocks[i], cache.getBlock(i));
				}
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.RUBlockCache#removeBlock(int)}.
		 */
		@Test
		default void testRemoveBlockBlank() {
			try(RUBlockCache cache = create()) {
				assertThrows(RuntimeException.class, () -> cache.removeBlock(randomId()));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.RUBlockCache#removeBlock(int)}.
		 */
		@Test
		default void testRemoveBlockEmpty() {
			try(RUBlockCache cache = create()) {
				cache.open(100);
				assertNull(cache.removeBlock(randomId()));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.RUBlockCache#removeBlock(int)}.
		 */
		@Test
		default void testRemoveBlockForeign() {
			try(RUBlockCache cache = create()) {
				cache.open(100);
				int id = randomId();
				cache.addBlock(block(id));
				assertNull(cache.removeBlock(id+1));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.RUBlockCache#removeBlock(int)}.
		 */
		@Test
		default void testRemoveBlock() {
			try(RUBlockCache cache = create()) {
				cache.open(100);
				int id = randomId();
				Block block = block(id);
				cache.addBlock(block);

				assertSame(block, cache.getBlock(id));

				assertSame(block, cache.removeBlock(id));

				assertNull(cache.getBlock(id));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.RUBlockCache#open(int)}.
		 */
		@Test
		default void testOpen() {
			try(RUBlockCache cache = create()) {
				cache.open(random(BlockCache.MIN_CAPACITY, 1_000));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.RUBlockCache#open(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -1, BlockCache.MIN_CAPACITY-1})
		default void testOpenInvalidCapacity(int capacity) {
			try(RUBlockCache cache = create()) {
				assertIAE(() -> cache.open(capacity));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.RUBlockCache#close()}.
		 */
		@Test
		default void testClose() {
			@SuppressWarnings("resource")
			RUBlockCache cache = create();
			cache.open(100);
			int id = randomId();
			cache.addBlock(block(id));
			assertNotNull(cache.getBlock(id));

			cache.close();

			assertThrows(RuntimeException.class, () -> cache.getBlock(id));
			assertThrows(RuntimeException.class, () -> cache.addBlock(block(id)));
			assertThrows(RuntimeException.class, () -> cache.removeBlock(id));
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.RUBlockCache#close()}.
		 * Test method for {@link de.ims.icarus2.filedriver.io.RUBlockCache#open(int)}.
		 */
		@Test
		default void testReopen() {
			@SuppressWarnings("resource")
			RUBlockCache cache = create();
			cache.open(100);
			int id = randomId();
			cache.addBlock(block(id));
			assertNotNull(cache.getBlock(id));

			cache.close();

			cache.open(200);
			assertNull(cache.getBlock(id));

			cache.addBlock(block(id));
			assertNotNull(cache.getBlock(id));
		}
	}

	@Nested
	class WithLruInstance implements TestBase {

		@Override
		public RUBlockCache createTestInstance(TestSettings settings) {
			return settings.process(RUBlockCache.newLeastRecentlyUsedCache());
		}

	}

	@Nested
	class WithMruInstance implements TestBase {

		@Override
		public RUBlockCache createTestInstance(TestSettings settings) {
			return settings.process(RUBlockCache.newMostRecentlyUsedCache());
		}

	}
}
