/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.io.resource;

import static de.ims.icarus2.SharedTestUtils.assertIcarusException;
import static de.ims.icarus2.test.TestUtils.MAX_INTEGER_INDEX;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.nio.MemoryByteStorage;

/**
 * @author Markus Gärtner
 *
 */
class VirtualIOResourceTest implements IOResourceTest<VirtualIOResource> {

	private static final Path DEFAULT_PATH = Paths.get(".");

	@Override
	public Class<? extends VirtualIOResource> getTestTargetClass() {
		return VirtualIOResource.class;
	}

	@Override
	public VirtualIOResource create(AccessMode accessMode) throws IOException {
		return new VirtualIOResource(DEFAULT_PATH, accessMode);
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
				() -> new VirtualIOResource(DEFAULT_PATH, value));
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
	 * Test method for {@link de.ims.icarus2.util.io.resource.VirtualIOResource#getPath()}.
	 * @throws IOException
	 */
	@Test
	void testGetPath() throws IOException {
		assertThat(create().getPath()).isNotNull();
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
	@RandomizedTest
	Stream<DynamicTest> testRead(RandomGenerator rand) throws IOException {
		return rand.ints(10, 1, 1<<15)
				.mapToObj(size -> dynamicTest("bytes="+size, () -> {
					VirtualIOResource resource = create(AccessMode.READ);
					resource.prepare();

					byte[] bytes = new byte[size];
					rand.nextBytes(bytes);
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
	@RandomizedTest
	Stream<DynamicTest> testWrite(RandomGenerator rand) throws IOException {
		return rand.ints(10, 1, 1<<15)
				.mapToObj(size -> dynamicTest("bytes="+size, () -> {
					VirtualIOResource resource = create(AccessMode.WRITE);
					resource.prepare();

					byte[] bytes = new byte[size];
					rand.nextBytes(bytes);
					write(resource, bytes);

					MemoryByteStorage buffer = resource.getBuffer();
					ByteBuffer bb = ByteBuffer.wrap(new byte[size]);
					buffer.read(0, bb);

					assertArrayEquals(bytes, bb.array());
				}));
	}
}
