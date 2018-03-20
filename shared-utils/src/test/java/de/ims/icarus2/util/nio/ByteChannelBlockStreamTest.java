/**
 *
 */
package de.ims.icarus2.util.nio;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Markus
 *
 */
@RunWith(Parameterized.class)
public class ByteChannelBlockStreamTest {

	private byte[] data;

	private ByteArrayChannel channel;

	private ByteChannelBlockStream stream;

	@SuppressWarnings("boxing")
	@Parameters(name="{index}: buffer={0} allocateDirect={1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{32, true},
			{1024, true},
			{32, false},
			{1024, false},
		});
	}

	@Parameter(0)
	public int bufferSize;

	@Parameter(1)
	public boolean allocateDirect;

	private final static int SIZE = 100;

	@Before
	public void prepare() {
		data = new byte[SIZE];
		NIOUtil.randomize(data);

		channel = new ByteArrayChannel(data);
		stream = new ByteChannelBlockStream(channel, bufferSize, allocateDirect);
	}

	@After
	public void cleanup() {
		channel.close();
		stream.close();
	}

	@Test
	public void testFull() throws Exception {
		stream.reload(0, SIZE);

		byte[] tmp = new byte[20];
		int offset = 0;
		int bytesRead;
		while((bytesRead=stream.read(tmp))>0) {
			byte[] expected = Arrays.copyOfRange(data, offset, offset+bytesRead);
			byte[] actual = tmp;
			if(bytesRead<tmp.length) {
				actual = Arrays.copyOf(tmp, bytesRead);
			}

			assertArrayEquals(expected, actual);

			offset += bytesRead;
		}

		assertEquals(SIZE, offset);
		assertEquals(0, stream.available());
	}

	@Test
	public void testParts() throws Exception {
		stream.reload(0, SIZE);

		byte[] tmp = new byte[20];

		int size = 17;

		for(int i=1; i<5; i++) {
			int offset = i*12;
			stream.reload(offset, size);
			int bytesRead;
			while((bytesRead=stream.read(tmp))>0) {
				byte[] expected = Arrays.copyOfRange(data, offset, offset+bytesRead);
				byte[] actual = tmp;
				if(bytesRead<tmp.length) {
					actual = Arrays.copyOf(tmp, bytesRead);
				}

				assertArrayEquals(expected, actual);

				offset += bytesRead;
			}

			assertEquals(stream.position(), offset);
			assertEquals(0, stream.available());
		}
	}
}
