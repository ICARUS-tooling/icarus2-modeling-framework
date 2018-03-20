/**
 *
 */
package de.ims.icarus2.util.nio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import de.ims.icarus2.TestUtils;

/**
 * @author Markus
 *
 */
@RunWith(Parameterized.class)
public class FastChannelReaderTest {

	private ByteArrayChannel channel;
	private FastChannelReader reader;

    @SuppressWarnings("boxing")
	@Parameters(name="{index}: encoding= {0} buffer={1} allocateDirect= {2}")
    public static Collection<Object[]> data() {
    	return Arrays.asList(new Object[][] {
    		{StandardCharsets.US_ASCII, 32, true, TestUtils.LOREM_IPSUM_ASCII},
    		{StandardCharsets.US_ASCII, 1024, true, TestUtils.LOREM_IPSUM_ASCII},
    		{StandardCharsets.US_ASCII, 32, false, TestUtils.LOREM_IPSUM_ASCII},
    		{StandardCharsets.US_ASCII, 1024, false, TestUtils.LOREM_IPSUM_ASCII},

    		{StandardCharsets.ISO_8859_1, 32, true, TestUtils.LOREM_IPSUM_ISO},
    		{StandardCharsets.ISO_8859_1, 1024, true, TestUtils.LOREM_IPSUM_ISO},
    		{StandardCharsets.ISO_8859_1, 32, false, TestUtils.LOREM_IPSUM_ISO},
    		{StandardCharsets.ISO_8859_1, 1024, false, TestUtils.LOREM_IPSUM_ISO},

    		{StandardCharsets.UTF_8, 32, true, TestUtils.LOREM_IPSUM_CHINESE},
    		{StandardCharsets.UTF_8, 1024, true, TestUtils.LOREM_IPSUM_CHINESE},
    		{StandardCharsets.UTF_8, 32, false, TestUtils.LOREM_IPSUM_CHINESE},
    		{StandardCharsets.UTF_8, 1024, false, TestUtils.LOREM_IPSUM_CHINESE},
    		});
    }

    @Parameter(0)
    public Charset encoding;

    @Parameter(1)
    public int bufferSize;

    @Parameter(2)
    public boolean allocateDirect;

    @Parameter(3)
    public String text;

	@Before
	public void prepare() throws IOException {
		channel = new ByteArrayChannel(new byte[text.length()*4], false);

		try(Writer writer = Channels.newWriter(channel, encoding.newEncoder(), -1)) {
			writer.write(text);
		}
		channel.position(0);

		reader = new FastChannelReader(channel, encoding.newDecoder(), bufferSize, allocateDirect);
	}

	@After
	public void cleanup() throws IOException {
		channel.close();
		reader.close();
	}

	@Test
	public void testRead() throws Exception {
		assertTrue(reader.ready());

		char[] tmp = new char[text.length()*2];
		int charsRead = reader.read(tmp);
		assertEquals(text.length(), charsRead);

		String readString = new String(tmp, 0, charsRead);

		assertEquals(text, readString);
	}
}
