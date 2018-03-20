/**
 *
 */
package de.ims.icarus2.util.nio;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * @author Markus
 *
 */
public class NIOUtil {

	private static final Random RANDOM = new Random(System.currentTimeMillis()^NIOTestUtil.class.hashCode());

	private static final byte[] EMPTY_BYTES = {};

	private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(EMPTY_BYTES);

	private static final int MIN_BYTE_BUFFER_SIZE = 32;
	private static final int DEFAULT_BYTE_BUFFER_SIZE = 8192;

	/**
	 * Utility method for {@link FastChannelReader} and {@link ByteChannelBlockStream}
	 * to initialize internal buffers;
	 *
	 * @param mbc desired minimum buffer size or {@code -1} if an implementation
	 * specific minimum should be chosen.
	 * @param allocateDirect
	 * @return
	 */
	static final ByteBuffer allocate(int mbc, boolean allocateDirect) {
		int capacity = mbc < 0 ? DEFAULT_BYTE_BUFFER_SIZE
				: (mbc < MIN_BYTE_BUFFER_SIZE ? MIN_BYTE_BUFFER_SIZE : mbc);

		return allocateDirect ? ByteBuffer.allocateDirect(capacity)
				: ByteBuffer.allocate(capacity);
	}

	public static ByteBuffer emptyBuffer() {
		return EMPTY_BUFFER;
	}

	public static ByteBuffer randomBuffer(int size) {
		byte[] data = new byte[size];
		randomize(data);
		return ByteBuffer.wrap(data);
	}

	public static void randomize(byte[] data) {
		RANDOM.nextBytes(data);
	}

	//TODO add methods for filling channels etc...
}
