/**
 *
 */
package de.ims.icarus2.util.nio;

import static de.ims.icarus2.util.nio.NIOTestUtil.assertContentEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Markus
 *
 */
public class ByteArrayChannelTest {

	private byte[] data;
	private ByteArrayChannel channel;

	private static final int SIZE = 100;

	@BeforeEach
	public void prepare() {
		data = new byte[SIZE];
		channel = new ByteArrayChannel(data, false);
	}

	@AfterEach
	public void cleanup() {
		channel.close();
		data = null;
	}

	@Test
	public void testReadOnly() throws Exception {
		Assertions.assertThrows(IOException.class, () -> {
			channel = new ByteArrayChannel(data);

			channel.write(NIOUtil.emptyBuffer());
		});
	}

	@Test
	public void testWrite() throws Exception {
		byte[] chunk = new byte[20];
		NIOUtil.randomize(chunk);
		ByteBuffer bb = ByteBuffer.wrap(chunk);

		int offset = 0;
		int bytesWritten;
		while((bytesWritten=channel.write(bb))>0) {
			bb.rewind();
			byte[] expected = Arrays.copyOfRange(data, offset, offset+bytesWritten);
			assertContentEquals(expected, bb);

			NIOUtil.randomize(chunk);
			offset += bytesWritten;
			bb.clear();
		}

		assertEquals(SIZE, offset);
	}

	@Test
	public void testRead() throws Exception {
		NIOUtil.randomize(data);

		ByteBuffer bb = ByteBuffer.allocate(20);
		int offset = 0;
		int bytesRead;
		while((bytesRead = channel.read(bb))>0) {
			bb.flip();
			byte[] expected = Arrays.copyOfRange(data, offset, offset+bytesRead);
			assertContentEquals(expected, bb);
			offset += bytesRead;
			bb.clear();
		}

		assertEquals(SIZE, offset);
	}

	@Test
	public void testPosition() throws Exception {
		int pos = 25;
		channel.position(pos);
		assertEquals(pos, channel.position());
	}
}