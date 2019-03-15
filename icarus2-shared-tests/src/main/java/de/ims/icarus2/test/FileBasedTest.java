/**
 *
 */
package de.ims.icarus2.test;

import java.nio.file.Path;

import org.junit.jupiter.api.io.TempDir;

/**
 * @author Markus Gärtner
 *
 */
public class FileBasedTest {

	@TempDir
	private Path rootFolder;

	protected Path makeFile(String name) {
		return rootFolder.resolve(name);
	}
}
