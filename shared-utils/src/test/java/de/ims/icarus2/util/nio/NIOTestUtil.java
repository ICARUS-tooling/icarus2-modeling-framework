/**
 *
 */
package de.ims.icarus2.util.nio;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * @author Markus
 *
 */
public class NIOTestUtil {


	/**
	 * Assert that the accessible content of {@code bb} matches the
	 * immediately accessible content of {@code channel}.
	 *
	 * @param channel
	 * @param bb
	 */
	public static void assertContentEquals(ReadableByteChannel channel, ByteBuffer bb) throws IOException {
		ByteBuffer tmp = ByteBuffer.allocate(bb.remaining());

		int bytesRead = channel.read(tmp);

		assertEquals(bb.remaining(), bytesRead);
		tmp.flip();

		assertContentEquals(bb, tmp);
	}

	public static void assertContentEquals(ByteBuffer expected, ByteBuffer actual) {
		assertEquals(expected.remaining(), actual.remaining());

		while(expected.hasRemaining()) {
			assertEquals(expected.get(), actual.get());
		}
	}

	public static void assertContentEquals(byte[] expected, ByteBuffer actual) {
		assertEquals(expected.length, actual.remaining());

		byte[] tmp = new byte[expected.length];
		actual.get(tmp);

		assertArrayEquals(expected, tmp);
	}
}
