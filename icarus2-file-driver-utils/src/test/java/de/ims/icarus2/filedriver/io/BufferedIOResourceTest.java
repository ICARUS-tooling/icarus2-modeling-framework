/**
 *
 */
package de.ims.icarus2.filedriver.io;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.util.lang.Primitives._int;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;
import de.ims.icarus2.filedriver.io.BufferedIOResource.PayloadConverter;
import de.ims.icarus2.filedriver.io.BufferedIOResource.ReadWriteAccessor;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.io.resource.VirtualIOResource;

/**
 * @author Markus Gärtner
 *
 */
class BufferedIOResourceTest {

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#builder()}.
	 */
	@Test
	void testBuilder() {
		assertNotNull(BufferedIOResource.builder());
	}

	//TODO enable
	@Disabled
	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#BufferedIOResource(de.ims.icarus2.util.io.resource.IOResource, de.ims.icarus2.filedriver.io.BufferedIOResource.PayloadConverter, de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache, int, int)}.
		 */
		@Test
		void testBufferedIOResourceIOResourcePayloadConverterBlockCacheIntInt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#BufferedIOResource(de.ims.icarus2.filedriver.io.BufferedIOResource.Builder)}.
		 */
		@Test
		void testBufferedIOResourceBuilder() {
			fail("Not yet implemented"); // TODO
		}

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

		for(int bytesPerBlock : new int[]{2, 8, 64}) {
			for(int cacheSize : new int[]{BlockCache.MIN_CAPACITY, BlockCache.MIN_CAPACITY*2, 1024}) {
				for(int entries : new int[]{BlockCache.MIN_CAPACITY*2, 10_000}) {
					Config config = new Config();
					config.bytesPerBlock = bytesPerBlock;
					config.cacheSize = cacheSize;
					config.cache = RUBlockCache.newLeastRecentlyUsedCache();
					config.entries = entries;
					config.resource = new VirtualIOResource(bytesPerBlock * entries);
					config.converter = new PayloadConverter() {
						@Override
						public void write(Object source, ByteBuffer buffer, int length) throws IOException {
							buffer.put((byte[]) source, 0, length);
						}
						@Override
						public int read(Object target, ByteBuffer buffer) throws IOException {
							int length = buffer.remaining();
							buffer.get((byte[])target);
							return length;
						}
						@Override
						public Object newBlockData(int bytesPerBlock) {
							return new byte[bytesPerBlock];
						}
						};
					config.label = String.format("cache=LRU entries=%d bytesPerBlock=%d cacheSize=%d",
							_int(entries), _int(bytesPerBlock), _int(cacheSize));

					buffer.add(config);
				}
			}
		}

		return buffer.stream();
	}

	interface Task {
		void execute(Config config, BufferedIOResource instance) throws IOException;
	}

	private Stream<DynamicNode> basicTests(Task task) {
		return configurations()
				.map(config -> dynamicTest(config.label, () -> {
					BufferedIOResource instance = config.create();
					task.execute(config, instance);
				}));

	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#getResource()}.
	 */
	@TestFactory
	Stream<DynamicNode> testGetResource() {
		return basicTests((config, instance) -> assertSame(config.resource, instance.getResource()));
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
	Stream<DynamicNode> testGetUseCount() {
		return basicTests((config, instance) -> {
			assertEquals(0, instance.getUseCount());

			int count = random(1, 10);
			for (int i = 0; i < count; i++) {
				instance.newAccessor(random().nextBoolean());
			}

			assertEquals(count, instance.getUseCount());
		});
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#isUsed()}.
	 */
	@TestFactory
	Stream<DynamicNode> testIsUsed() {
		return basicTests((config, instance) -> {
			assertFalse(instance.isUsed());
			instance.newAccessor(random().nextBoolean());
			assertTrue(instance.isUsed());
		});
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#delete()}.
	 */
	@TestFactory
	Stream<DynamicNode> testDelete() {
		return basicTests((config, instance) -> {
			assertFalse(instance.isUsed());
			instance.newAccessor(random().nextBoolean());
			assertTrue(instance.isUsed());
		});
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#flush()}.
	 */
	@Test
	void testFlush() {
		fail("Not yet implemented"); // TODO
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


		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#lockBlock(int, de.ims.icarus2.filedriver.io.BufferedIOResource.Block)}.
		 */
		@Test
		void testLockBlock() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.io.BufferedIOResource#getBlock(int, boolean)}.
		 */
		@Test
		void testGetBlock() {
			fail("Not yet implemented"); // TODO
		}
	}

	private static class Config {
		String label;
		VirtualIOResource resource;
		BlockCache cache;
		PayloadConverter converter;
		int entries;
		int cacheSize;
		int bytesPerBlock;

		BufferedIOResource create() {
			return new BufferedIOResource(resource, converter, cache, cacheSize, bytesPerBlock);
		}
	}
}
