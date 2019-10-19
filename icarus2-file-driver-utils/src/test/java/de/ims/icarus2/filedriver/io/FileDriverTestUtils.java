/**
 *
 */
package de.ims.icarus2.filedriver.io;

import de.ims.icarus2.filedriver.io.BufferedIOResource.Block;

/**
 * @author Markus Gärtner
 *
 */
public class FileDriverTestUtils {

	public static Block block() {
		return new Block(new Object());
	}

	public static Block block(int id) {
		Block block = block();
		block.setId(id);
		return block;
	}

}
