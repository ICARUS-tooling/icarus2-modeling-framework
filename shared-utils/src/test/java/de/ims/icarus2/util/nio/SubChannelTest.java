/**
 *
 */
package de.ims.icarus2.util.nio;

import static de.ims.icarus2.util.nio.NIOTestUtil.assertContentEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Markus
 *
 */
public class SubChannelTest {

	private SeekableByteChannel channel;
	private SubChannel subChannel;
	private byte[] data;

	private static final int SIZE = 100;

	@SuppressWarnings("resource")
	@Before
	public void prepare() throws IOException {
		data = new byte[SIZE];
		channel = new ByteArrayChannel(data, false);

		subChannel = new SubChannel(channel).setOffsets(0, channel.size());
	}

	@After
	public void cleanup() throws IOException {
		subChannel.close();
		channel.close();
	}

	@Test
	public void testInit() throws Exception {
		assertSame(channel, subChannel.getSource());
		assertEquals(SIZE, subChannel.size());
		assertEquals(0, subChannel.position());
	}

	@Test
	public void testWriteReadOrigin() throws Exception {

		int size = 20;

		ByteBuffer bb = NIOUtil.randomBuffer(size);
		int bytesWritten = subChannel.write(bb);

		assertEquals(size, bytesWritten);
		assertEquals(size, subChannel.position());

		subChannel.position(0);
		bb.rewind();

		assertContentEquals(subChannel, bb);
	}

	@Test
	public void testWriteReadSection() throws Exception {

		int size = 20;
		int pos = 30;

		subChannel.setOffsets(pos, size);

		ByteBuffer bb = NIOUtil.randomBuffer(size);
		int bytesWritten = subChannel.write(bb);

		assertEquals(size, bytesWritten);
		assertEquals(size, subChannel.position());

		subChannel.position(0);
		bb.rewind();

		assertContentEquals(subChannel, bb);
	}

	@Test
	public void testEmpty() throws Exception {
		subChannel.setOffsets(20, 0);
		assertEquals(0, subChannel.size());

		ByteBuffer bb = NIOUtil.randomBuffer(10);
		int bytesWritten = subChannel.write(bb);
		assertTrue(bytesWritten<=0);

		bb.rewind();
		int bytesRead = subChannel.read(bb);
		assertTrue(bytesRead<=0);
	}

	@Test
	public void testPosition() throws Exception {
		int pos = 5;

		subChannel.position(pos);
		assertEquals(pos, subChannel.position());
	}

	@Test
	public void testClose() throws Exception {
		subChannel.close();
		assertFalse(subChannel.isOpen());
	}

	@Test(expected=ClosedChannelException.class)
	public void testClosed() throws Exception {
		subChannel.close();
		subChannel.write(NIOUtil.emptyBuffer());
	}
}
