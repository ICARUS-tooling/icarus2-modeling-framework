/**
 *
 */
package de.ims.icarus2.filedriver.io;

import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.util.IcarusUtils.MAX_INTEGER_INDEX;

import de.ims.icarus2.filedriver.io.BufferedIOResource.Block;

/**
 * @author Markus Gärtner
 *
 */
public class FileDriverTestUtils {

	public static int randomId() {
		return random(0, MAX_INTEGER_INDEX);
	}

	public static Block block() {
		return new Block(new Object());
	}

	public static Block block(int id) {
		Block block = block();
		block.setId(id);
		return block;
	}

}
