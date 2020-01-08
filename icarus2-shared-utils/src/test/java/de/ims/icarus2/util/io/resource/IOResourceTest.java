/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.opentest4j.TestAbortedException;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.util.AccessMode;

/**
 * @author Markus Gärtner
 *
 */
public interface IOResourceTest<R extends IOResource> extends ApiGuardedTest<R> {

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default R createTestInstance(TestSettings settings) {
		R instance;
		try {
			instance = create(AccessMode.READ_WRITE);
		} catch (IOException e) {
			throw new TestAbortedException("Failed to create resource", e);
		}
		return settings.process(instance);
	}

	@Provider
	R create(AccessMode accessMode) throws IOException;

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.IOResource#getAccessMode()}.
	 * @throws IOException
	 */
	@TestFactory
	default Stream<DynamicTest> expectAccessModeNotNull() throws IOException {
		return getSupportedAccessModes().stream()
				.map(accessMode -> dynamicTest(accessMode.name(), () -> {
					assertSame(accessMode, create(accessMode).getAccessMode());
				}));
	}

	Set<AccessMode> getSupportedAccessModes();

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.IOResource#getWriteChannel()}.
	 * Test method for {@link de.ims.icarus2.util.io.resource.IOResource#getReadChannel()}.
	 */
	@TestFactory
	default Stream<DynamicTest> verifyAccessModeConsistencyWithChannels() {
		return getSupportedAccessModes().stream()
				.map(accessMode -> dynamicTest(accessMode.name(), () -> {
					R instance = create(accessMode);

					instance.prepare();

					if(accessMode.isWrite()) {
						try(SeekableByteChannel channel = instance.getWriteChannel()) {
							assertNotNull(channel);
						}
					} else {
						assertIcarusException(GlobalErrorCode.UNSUPPORTED_OPERATION,
								() -> instance.getWriteChannel(), "Should not be writable");
					}

					if(accessMode.isRead()) {
						try(SeekableByteChannel channel = instance.getReadChannel()) {
							assertNotNull(channel);
						}
					} else {
						assertIcarusException(GlobalErrorCode.UNSUPPORTED_OPERATION,
								() -> instance.getReadChannel(), "Should not be readable");
					}
				}));

	}

	default byte[] read(IOResource resource) throws IOException {
		try(SeekableByteChannel channel = resource.getReadChannel()) {
			ByteBuffer bb = ByteBuffer.allocate((int) channel.size());
			channel.read(bb);
			return bb.array();
		}
	}

	default void write(IOResource resource, byte[] bytes) throws IOException {
		try(SeekableByteChannel channel = resource.getWriteChannel()) {
			ByteBuffer bb = ByteBuffer.wrap(bytes);
			channel.write(bb);
		}
	}
}
