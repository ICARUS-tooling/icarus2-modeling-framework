/**
 *
 */
package de.ims.icarus2.model.api.io.resources;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
	 * Test method for {@link de.ims.icarus2.model.api.io.resources.IOResource#getAccessMode()}.
	 * @throws IOException
	 */
	@ParameterizedTest
	@EnumSource(value=AccessMode.class)
	default void expectAccessModeNotNull(AccessMode accessMode) throws IOException {
		assertSame(accessMode, create(accessMode).getAccessMode());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.io.resources.IOResource#getWriteChannel()}.
	 * Test method for {@link de.ims.icarus2.model.api.io.resources.IOResource#getReadChannel()}.
	 */
	@ParameterizedTest
	@EnumSource(value=AccessMode.class)
	default void verifyAccessModeConsistencyWithChannels(AccessMode accessMode) throws IOException {
		R instance = create(accessMode);

		instance.prepare();

		if(accessMode.isWrite()) {
			try(SeekableByteChannel channel = instance.getWriteChannel()) {
				assertNotNull(channel);
			}
		} else {
			assertModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
					() -> instance.getWriteChannel(), "Should not be writable");
		}

		if(accessMode.isRead()) {
			try(SeekableByteChannel channel = instance.getReadChannel()) {
				assertNotNull(channel);
			}
		} else {
			assertModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
					() -> instance.getReadChannel(), "Should not be readable");
		}
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
