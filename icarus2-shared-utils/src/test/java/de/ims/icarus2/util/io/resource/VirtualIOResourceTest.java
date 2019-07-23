/**
 *
 */
package de.ims.icarus2.util.io.resource;

import static de.ims.icarus2.SharedTestUtils.assertIcarusException;
import static de.ims.icarus2.test.TestTags.RANDOMIZED;
import static de.ims.icarus2.test.TestUtils.MAX_INTEGER_INDEX;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.nio.MemoryByteStorage;

/**
 * @author Markus Gärtner
 *
 */
class VirtualIOResourceTest implements IOResourceTest<VirtualIOResource> {

	@Override
	public Class<? extends VirtualIOResource> getTestTargetClass() {
		return VirtualIOResource.class;
	}

	@Override
	public VirtualIOResource create(AccessMode accessMode) throws IOException {
		return new VirtualIOResource(accessMode);
	}

	@Override
	public Set<AccessMode> getSupportedAccessModes() {
		return set(AccessMode.values());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.VirtualIOResource#VirtualIOResource()}.
	 */
	@ParameterizedTest
	@ValueSource(ints = {0, -1, MAX_INTEGER_INDEX+1})
	void testVirtualIOResource(int value) {
		assertIcarusException(GlobalErrorCode.INVALID_INPUT,
				() -> new VirtualIOResource(value));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.VirtualIOResource#delete()}.
	 * @throws IOException
	 */
	@Test
	void testDelete() throws IOException {
		VirtualIOResource resource = create();

		resource.prepare();
		assertNotNull(resource.getBuffer());

		resource.delete();
		assertNull(resource.getBuffer());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.VirtualIOResource#prepare()}.
	 * @throws IOException
	 */
	@Test
	void testPrepare() throws IOException {
		create().prepare();
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.VirtualIOResource#size()}.
	 * @throws IOException
	 */
	@Test
	void testSizeEmpty() throws IOException {
		VirtualIOResource resource = create();
		resource.prepare();

		assertEquals(0, resource.size());
	}


	/**
	 * Test method for reading.
	 * @throws IOException
	 */
	@TestFactory
	@Tag(RANDOMIZED)
	Stream<DynamicTest> testRead() throws IOException {
		return random().ints(10, 1, 1<<15)
				.mapToObj(size -> dynamicTest("bytes="+size, () -> {
					VirtualIOResource resource = create(AccessMode.READ);
					resource.prepare();

					byte[] bytes = new byte[size];
					random().nextBytes(bytes);
					ByteBuffer bb = ByteBuffer.wrap(bytes);

					MemoryByteStorage buffer = resource.getBuffer();
					buffer.write(0, bb);

					assertArrayEquals(bytes, read(resource));
				}));
	}

	/**
	 * Test method for writing.
	 * @throws IOException
	 */
	@TestFactory
	@Tag(RANDOMIZED)
	Stream<DynamicTest> testWrite() throws IOException {
		return random().ints(10, 1, 1<<15)
				.mapToObj(size -> dynamicTest("bytes="+size, () -> {
					VirtualIOResource resource = create(AccessMode.WRITE);
					resource.prepare();

					byte[] bytes = new byte[size];
					random().nextBytes(bytes);
					write(resource, bytes);

					MemoryByteStorage buffer = resource.getBuffer();
					ByteBuffer bb = ByteBuffer.wrap(new byte[size]);
					buffer.read(0, bb);

					assertArrayEquals(bytes, bb.array());
				}));
	}
}
