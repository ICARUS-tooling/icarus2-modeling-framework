/**
 *
 */
package de.ims.icarus2.filedriver.io;

import static de.ims.icarus2.filedriver.io.FileDriverTestUtils.block;
import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.test.TestUtils.MAX_INTEGER_INDEX;
import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives.strictToByte;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.io.BufferedIOResource.Block;
import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;
import de.ims.icarus2.filedriver.io.BufferedIOResource.PayloadConverter;
import de.ims.icarus2.filedriver.io.BufferedIOResource.ReadWriteAccessor;
import de.ims.icarus2.filedriver.io.BufferedIOResource.StatField;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.func.ThrowingBiConsumer;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.io.resource.IOResource;
import de.ims.icarus2.util.io.resource.VirtualIOResource;

/**
 * @author Markus Gärtner
 *
 */
class BufferedIOResourceTest {

	//TODO enable
	@Disabled
	@Nested
	class ForBuilder {
		//TODO test the builder methods
	}

	@Disabled
	//TODO enable
	@Nested
	class Internals {

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#getLock()}.
		 */
		@Test
		void testGetLock() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#openUnsafe()}.
		 */
		@Test
		void testOpen() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#closeUnsafe()}.
		 */
		@Test
		void testClose() {
			fail("Not yet implemented"); // TODO
		}

	}

	Stream<Config> configurations() {
		List<Config> buffer = new ArrayList<>();

		for(int bytesPerBlock : new int[]{BufferedIOResource.MIN_BLOCK_SIZE, BufferedIOResource.MIN_BLOCK_SIZE*2, 128}) {
			for(int cacheSize : new int[]{BlockCache.MIN_CAPACITY, BlockCache.MIN_CAPACITY*2, 1024}) {
				for(int entries : new int[]{BlockCache.MIN_CAPACITY*2, 10_000}) {
					for(int headerSize : new int[]{0, Long.BYTES, 512}) {
						Config config = new Config();
						config.bytesPerBlock = bytesPerBlock;
						config.cacheSize = cacheSize;
						config.headerSize = headerSize;
						config.cache = RUBlockCache.newLeastRecentlyUsedCache();
						config.entries = entries;
						config.resource = new VirtualIOResource(bytesPerBlock * config.entries + headerSize);
						config.converter = new PayloadConverter() {
							@Override
							public void write(Object source, ByteBuffer buffer, int length) throws IOException {
								buffer.put((byte[]) source, 0, length);
							}
							@Override
							public int read(Object target, ByteBuffer buffer) throws IOException {
								int length = buffer.remaining();
								if(length>0)
									buffer.get((byte[])target);
								return length;
							}
							@Override
							public Object newBlockData(int bytesPerBlock) {
								return new byte[bytesPerBlock];
							}
							};
						config.label = String.format("cache=LRU entries=%d bytesPerBlock=%d headerSize=%d cacheSize=%d",
								_int(entries), _int(bytesPerBlock), _int(headerSize), _int(cacheSize));

						buffer.add(config);
					}
				}
			}
		}

		return buffer.stream();
	}

	private Stream<DynamicNode> basicTests(ThrowingBiConsumer<Config, BufferedIOResource> task) {
		return configurations()
				.map(config -> dynamicTest(config.label, () -> {
					BufferedIOResource instance = config.create();
					task.accept(config, instance);
				}));

	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#getResource()}.
	 */
	@TestFactory
	Stream<DynamicNode> testGetResource() {
		return basicTests((config, instance) -> assertSame(config.resourceMock, instance.getResource()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#getBytesPerBlock()}.
	 */
	@TestFactory
	Stream<DynamicNode> testGetBytesPerBlock() {
		return basicTests((config, instance) -> assertEquals(config.bytesPerBlock, instance.getBytesPerBlock()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#getCacheSize()}.
	 */
	@TestFactory
	Stream<DynamicNode> testGetCacheSize() {
		return basicTests((config, instance) -> assertEquals(config.cacheSize, instance.getCacheSize()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#getUseCount()}.
	 */
	@TestFactory
	@RandomizedTest
	Stream<DynamicNode> testGetUseCount(RandomGenerator rand) {
		return basicTests((config, instance) -> {
			rand.reset();

			assertEquals(0, instance.getUseCount());

			int count = rand.random(1, 10);
			for (int i = 0; i < count; i++) {
				instance.newAccessor(rand.nextBoolean());
			}

			assertEquals(count, instance.getUseCount());
		});
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#isUsed()}.
	 */
	@TestFactory
	@RandomizedTest
	Stream<DynamicNode> testIsUsed(RandomGenerator rand) {
		return basicTests((config, instance) -> {
			assertFalse(instance.isUsed());
			instance.newAccessor(rand.nextBoolean());
			assertTrue(instance.isUsed());
		});
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#delete()}.
	 */
	@TestFactory
	@RandomizedTest
	Stream<DynamicNode> testDelete(RandomGenerator rand) {
		return basicTests((config, instance) -> {
			assertFalse(instance.isUsed());
			instance.newAccessor(rand.nextBoolean());
			assertTrue(instance.isUsed());
		});
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#flush()}.
	 */
	@TestFactory
	Stream<DynamicNode> testFlush() {
		return basicTests((config, instance) -> {
			try(ReadWriteAccessor accessor = instance.newAccessor(false)) {
				Block[] blocks = IntStream.range(0, config.cacheSize/2)
						.mapToObj(accessor::getBlock)
						.toArray(Block[]::new);

				Stream.of(blocks).forEach(b -> accessor.lockBlock(b, config.bytesPerBlock));

				assertTrue(instance.hasLockedBlocks());
				instance.flush();
				assertFalse(instance.hasLockedBlocks());

				for(Block block : blocks) {
					assertFalse(block.isLocked());
				}
			}
		});
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#newAccessor(boolean)}.
	 */
	@TestFactory
	Stream<DynamicNode> testNewAccessor() {
		return basicTests((config, instance) -> {
			// Test read-only
			try(ReadWriteAccessor accessor = instance.newAccessor(true)) {
				assertNotNull(accessor);
				assertSame(instance, accessor.getSource());
				assertTrue(accessor.isReadOnly());
			}
			// Test write access
			try(ReadWriteAccessor accessor = instance.newAccessor(false)) {
				assertNotNull(accessor);
				assertSame(instance, accessor.getSource());
				assertFalse(accessor.isReadOnly());
			}
		});
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#newAccessor(boolean)}.
	 */
	@TestFactory
	Stream<DynamicNode> testNewAccessorNotWritable() {
		return configurations().map(config -> dynamicTest(config.label, () -> {
			// Reset resource to be read-only
			config.resource = new VirtualIOResource(AccessMode.READ);
			BufferedIOResource instance = config.create();

			assertModelException(GlobalErrorCode.NO_WRITE_ACCESS,
					() -> instance.newAccessor(false));
		}));
	}

	@Nested
	class ForAccessor {

		/** Generate tests with pre-initialized accessors, wrapping the tasks inside begin-end code */
		private Stream<DynamicNode> accessorTests(boolean readOnly,
				ThrowingBiConsumer<Config, ReadWriteAccessor> task) {
			return configurations()
					.map(config -> dynamicTest(config.label, () -> {
						BufferedIOResource instance = config.create();
						try(ReadWriteAccessor accessor = instance.newAccessor(readOnly)) {
							accessor.begin();
							try {
								task.accept(config, accessor);
							} finally {
								accessor.end();
							}
						}
					}));

		}

		/** Writes for every given id a block of data consisting of constant bytes of value id+1 */
		private void writeBlocks(Config config, ReadWriteAccessor accessor, int...ids) throws IOException {
			try(SeekableByteChannel channel = config.resource.getWriteChannel()) {
				byte[] b = new byte[config.bytesPerBlock];
				ByteBuffer bb = ByteBuffer.wrap(b);

				for (int id : ids) {
					bb.clear();
					Arrays.fill(b, strictToByte(id+1));
					channel.position(config.offsetFOrBlock(id));
					channel.write(bb);
				}
			}
		}

		private void assertBlock(int id, Block block) {
			assertNotNull(block);
			assertEquals(id, block.getId());
			byte[] b = (byte[]) block.getData();
			assertContent(b, id);
		}

		private void assertContent(byte[] b, int id) {
			byte val = strictToByte(id+1);
			for (int i = 0; i < b.length; i++) {
				assertEquals(val, b[i]);
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource.ReadWriteAccessor#writeHeader(byte[])}.
		 */
		@TestFactory
		Stream<DynamicNode> testWriteHeaderReadOnly() {
			return accessorTests(true, (config, accessor) -> {
				assertModelException(GlobalErrorCode.NO_WRITE_ACCESS,
							() -> accessor.writeHeader(new byte[1]));
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource.ReadWriteAccessor#readHeader()}.
		 */
		@TestFactory
		Stream<DynamicNode> testWriteHeaderReadable() {
			return accessorTests(true, (config, accessor) -> {
				if(config.headerSize==0) {
					assertModelException(GlobalErrorCode.ILLEGAL_STATE,
							() -> accessor.readHeader());
				} else {
					assertNotNull(accessor.readHeader());
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource.ReadWriteAccessor#writeHeader(byte[])}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testWriteHeaderRandom(RandomGenerator rng) {
			return accessorTests(false, (config, accessor) -> {
				if(config.headerSize==0) {
					assertModelException(GlobalErrorCode.ILLEGAL_STATE,
							() -> accessor.writeHeader(new byte[1]));
				} else {
					int size = rng.random(1, config.headerSize+1);
					byte[] header = rng.randomBytes(size, Byte.MIN_VALUE, Byte.MAX_VALUE);
					accessor.writeHeader(header);

					try(SeekableByteChannel channel = config.resource.getReadChannel()) {
						ByteBuffer bb = ByteBuffer.allocate(size);
						channel.position(0L);
						channel.read(bb);
						assertThat(bb.array()).isEqualTo(header);
					}
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource.ReadWriteAccessor#readHeader()}
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testReadHeaderRandom(RandomGenerator rng) {
			return accessorTests(true, (config, accessor) -> {
				if(config.headerSize==0) {
					assertModelException(GlobalErrorCode.ILLEGAL_STATE,
							() -> accessor.readHeader());
				} else {
					byte[] header = rng.randomBytes(config.headerSize, Byte.MIN_VALUE, Byte.MAX_VALUE);

					try(SeekableByteChannel channel = config.resource.getReadChannel()) {
						ByteBuffer bb = ByteBuffer.wrap(header);
						channel.position(0L);
						channel.write(bb);
					}
					assertThat(accessor.readHeader()).isEqualTo(header);
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#lockBlock(int, de.ims.icarus2.filedriver.io.BufferedIOResource.Block)}.
		 */
		@TestFactory
		Stream<DynamicNode> testLockBlockReadOnly() {
			return accessorTests(true, (config, accessor) -> assertModelException(GlobalErrorCode.NO_WRITE_ACCESS,
					() -> accessor.lockBlock(block(), 0)));
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#lockBlock(int, de.ims.icarus2.filedriver.io.BufferedIOResource.Block)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testLockBlock(RandomGenerator rand) {
			return accessorTests(false, (config, accessor) -> {
				// Need to keep it below cacheSize/2, as that's the threshold for automatic flushing
				int count = rand.random(10, config.cacheSize/2);
				int[] locked = IntStream.range(0, count).toArray();
				rand.reset();
				rand.shuffle(locked);
				Block[] blocks = IntStream.range(0, count*2)
						.mapToObj(accessor::getBlock)
						.toArray(Block[]::new);

				// Lock blocks
				for (int id : locked) {
					accessor.lockBlock(blocks[id], config.bytesPerBlock);
				}

				assertEquals(count, accessor.getSource().getStats().getCount(StatField.BLOCK_MARK));

				// Verify locked blocks
				for (int id : locked) {
					assertTrue(blocks[id].isLocked());
					blocks[id] = null;
				}

				// Verify that all other blocks remained unlocked
				for (int i = 0; i < count; i++) {
					if(blocks[i]!=null) {
						assertFalse(blocks[i].isLocked(), "Unexpected locked block at index "+i);
					}
				}
			});
		}

		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testAutoFlushing(RandomGenerator rand) {
			return accessorTests(false, (config, accessor) -> {
				// Locking cacheSize number of blocks should cause exactly 1 flush
				int count = config.cacheSize;
				int[] locked = IntStream.range(0, count).toArray();
				rand.reset();
				rand.shuffle(locked);
				Block[] blocks = IntStream.range(0, count*2)
						.mapToObj(accessor::getBlock)
						.toArray(Block[]::new);

				// Lock blocks
				for (int id : locked) {
					accessor.lockBlock(blocks[id], config.bytesPerBlock);
				}

				assertEquals(1, accessor.getSource().getStats().getCount(StatField.FLUSH));
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#getBlock(int, boolean)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testGetBlock(RandomGenerator rand) {
			return accessorTests(true, (config, accessor) -> {
				rand.reset();
				int[] ids = rand.ints(0, Byte.MAX_VALUE)
						.distinct()
						.limit(BlockCache.MIN_CAPACITY)
						.toArray();

				// Prepare data
				writeBlocks(config, accessor, ids);

				// Access blocks
				for(int id : ids) {
					Block block = accessor.getBlock(id);
					assertBlock(id, block);
				}
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#getBlock(int, boolean)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testGetBlockLastHitCache(RandomGenerator rand) {
			return accessorTests(false, (config, accessor) -> {
				int id = rand.nextInt(MAX_INTEGER_INDEX);

				Block block = accessor.getBlock(id);

				rand.reset();
				int count = rand.random(10, 100);
				for (int i = 0; i < count; i++) {
					assertSame(block, accessor.getBlock(id));
				}

				assertNotSame(block, accessor.getBlock(id+1));

				assertEquals(count, accessor.getSource().getStats().getCount(StatField.LAST_HIT));
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#getBlock(int, boolean)}.
		 */
		@TestFactory
		Stream<DynamicNode> testGetBlockCacheMiss() {
			return accessorTests(false, (config, accessor) -> {
				int[] ids = IntStream.range(0, config.cacheSize*2).toArray();

				// Sequential access, overflowing cacheSize limit
				for(int id : ids) {
					assertNotNull(accessor.getBlock(id));
				}

				assertEquals(config.cacheSize*2, accessor.getSource().getStats().getCount(StatField.CACHE_MISS));
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#getBlock(int, boolean)}.
		 */
		@TestFactory
		Stream<DynamicNode> testGetBlockCacheOverflow() {
			return accessorTests(false, (config, accessor) -> {
				int[] ids = IntStream.range(0, config.cacheSize).toArray();

				// Sequential access within cacheSize limit
				for(int id : ids) {
					assertNotNull(accessor.getBlock(id));
				}

				assertEquals(config.cacheSize, accessor.getSource().getStats().getCount(StatField.CACHE_MISS));
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#getBlock(int, boolean)}.
		 */
		@TestFactory
		Stream<DynamicNode> testGetBlockCacheSuccess() {
			return accessorTests(false, (config, accessor) -> {
				int[] ids = IntStream.range(0, config.cacheSize).toArray();

				// First pass
				for(int id : ids) {
					assertNotNull(accessor.getBlock(id));
				}
				// Second pass
				for(int id : ids) {
					assertNotNull(accessor.getBlock(id));
				}

				// Only the first pass should generate cache misses
				assertEquals(config.cacheSize, accessor.getSource().getStats().getCount(StatField.CACHE_MISS));
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#getBlock(int, boolean)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> testGetBlockNonExistent(RandomGenerator rand) {
			return accessorTests(true, (config, accessor) -> {
				rand.reset();
				int[] ids = rand.ints(0, Byte.MAX_VALUE)
						.distinct()
						.limit(BlockCache.MIN_CAPACITY)
						.toArray();

				// Access blocks
				for(int id : ids) {
					assertNull(accessor.getBlock(id));
				}
			});
		}

		/**
		 * Test method for {@link PayloadConverter#read(Object, ByteBuffer)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> verifyConverterRead(RandomGenerator rand) {
			return accessorTests(true, (config, accessor) -> {
				rand.reset();
				int[] ids = rand.ints(0, Byte.MAX_VALUE)
						.distinct()
						.limit(BlockCache.MIN_CAPACITY)
						.toArray();

				// Prepare data
				writeBlocks(config, accessor, ids);

				// Access blocks
				Block[] blocks = new Block[ids.length];
				for(int i = 0; i<ids.length; i++) {
					blocks[i] = accessor.getBlock(ids[i]);
				}

				verify(config.converterMock, times(ids.length)).newBlockData(config.bytesPerBlock);

				// Verify converter access
				for(int i = 0; i<blocks.length; i++) {
					verify(config.converterMock).read(eq(blocks[i].getData()), any());
				}
			});
		}

		/**
		 * Test method for {@link PayloadConverter#write(Object, ByteBuffer, int)}.
		 */
		@TestFactory
		@RandomizedTest
		Stream<DynamicNode> verifyConverterWrite(RandomGenerator rand) {
			return accessorTests(false, (config, accessor) -> {
				rand.reset();
				int[] ids = rand.ints(0, Byte.MAX_VALUE)
						.distinct()
						.limit(BlockCache.MIN_CAPACITY)
						.toArray();

				// Access blocks
				Block[] blocks = new Block[ids.length];
				for(int i = 0; i<ids.length; i++) {
					blocks[i] = accessor.getBlock(ids[i]);
					byte[] b = (byte[]) blocks[i].getData();
					Arrays.fill(b, strictToByte(ids[i]+1));
					accessor.lockBlock(blocks[i], b.length);
				}

				verify(config.converterMock, times(ids.length)).newBlockData(config.bytesPerBlock);

				// Force blocks to be written
				accessor.flush();

				// Verify converter access
				for(int i = 0; i<blocks.length; i++) {
					verify(config.converterMock).write(eq(blocks[i].getData()), any(), eq(config.bytesPerBlock));
				}

				// Finally make sure the data arrived in our back-end storage
				try(SeekableByteChannel channel = config.resource.getReadChannel()) {
					byte[] b = new byte[config.bytesPerBlock];
					ByteBuffer bb = ByteBuffer.wrap(b);

					for (int id : ids) {
						bb.clear();
						channel.position(config.offsetFOrBlock(id));
						channel.read(bb);
						assertEquals(0, bb.remaining());

						assertContent(b, id);
					}
				}
			});
		}
	}

	private static class Config {
		String label;
		VirtualIOResource resource;
		IOResource resourceMock;
		BlockCache cache;
		BlockCache cacheMock;
		PayloadConverter converter;
		PayloadConverter converterMock;
		int entries;
		int cacheSize;
		int bytesPerBlock;
		int headerSize;

		long offsetFOrBlock(int id) {
			return id* (long) bytesPerBlock + headerSize;
		}

		BufferedIOResource create() {
			resourceMock = mock(IOResource.class, invoc -> {
				return invoc.getMethod().invoke(resource, invoc.getArguments());
			});
			cacheMock = mock(BlockCache.class, invoc -> {
				return invoc.getMethod().invoke(cache, invoc.getArguments());
			});
			converterMock = mock(PayloadConverter.class, invoc -> {
				return invoc.getMethod().invoke(converter, invoc.getArguments());
			});

			// Use the builder, as this way we can activate stats tracking
			return BufferedIOResource.builder()
					.resource(resourceMock)
					.cacheSize(cacheSize)
					.bytesPerBlock(bytesPerBlock)
					.headerSize(headerSize)
					.blockCache(cacheMock)
					.payloadConverter(converterMock)
					.collectStats(true)
					.build();
		}
	}
}
