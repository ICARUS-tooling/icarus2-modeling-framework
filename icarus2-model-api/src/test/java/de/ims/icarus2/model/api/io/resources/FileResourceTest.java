/**
 *
 */
package de.ims.icarus2.model.api.io.resources;

import static de.ims.icarus2.test.TestTags.RANDOMIZED;
import static de.ims.icarus2.test.TestUtils.random;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import de.ims.icarus2.util.AccessMode;

/**
 * @author Markus Gärtner
 *
 */
class FileResourceTest implements IOResourceTest<FileResource> {

	@TempDir
	Path tempFolder;

	private Path tempFile() throws IOException {
		return Files.createTempFile(tempFolder, "fileResourceTest", ".tmp");
	}

	@Override
	public FileResource create(AccessMode accessMode) throws IOException {
		return new FileResource(tempFile(), accessMode);
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends FileResource> getTestTargetClass() {
		return FileResource.class;
	}

	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.io.resources.FileResource#FileResource(java.nio.file.Path)}.
		 * @throws IOException
		 */
		@Test
		void testFileResourcePath() throws IOException {
			Path file = tempFile();
			assertSame(file, new FileResource(file).getLocalPath());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.io.resources.FileResource#FileResource(java.nio.file.Path, de.ims.icarus2.util.AccessMode)}.
		 * @throws IOException
		 */
		@ParameterizedTest
		@EnumSource(value=AccessMode.class)
		void testFileResourcePathAccessMode(AccessMode accessMode) throws IOException {
			Path file = tempFile();
			FileResource resource = new FileResource(file, accessMode);
			assertSame(file, resource.getLocalPath());
			assertSame(accessMode, resource.getAccessMode());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.io.resources.FileResource#delete()}.
	 * @throws IOException
	 */
	@Test
	void testDelete() throws IOException {
		Path file = tempFile();
		FileResource resource = new FileResource(file, AccessMode.WRITE);
		resource.prepare();

		assertTrue(Files.exists(file, LinkOption.NOFOLLOW_LINKS), "File not created");

		resource.delete();

		assertTrue(Files.notExists(file, LinkOption.NOFOLLOW_LINKS), "File not deleted");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.io.resources.FileResource#size()}.
	 * @throws IOException
	 */
	@Test
	void testSizeEmpty() throws IOException {
		assertEquals(0, create().size());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.io.resources.FileResource#size()}.
	 * @throws IOException
	 */
	@TestFactory
	@Tag(RANDOMIZED)
	Stream<DynamicTest> testSizeFilled() throws IOException {
		return random().ints(10, 1, 1<<15)
				.mapToObj(size -> dynamicTest("bytes="+size, () -> {
					Path file = tempFile();
					FileResource resource = new FileResource(file, AccessMode.READ);
					byte[] bytes = new byte[size];
					random().nextBytes(bytes);
					Files.write(file, bytes);

					assertEquals(size, resource.size());
				}));
	}

	/**
	 * Test method for reading.
	 * @throws IOException
	 */
	@TestFactory
	@Tag(RANDOMIZED)
	Stream<DynamicTest> testRead() throws IOException {
		return random().ints(10, 1, 1<<15)
				.mapToObj(size -> dynamicTest("bytes="+size, () -> {
					Path file = tempFile();
					FileResource resource = new FileResource(file, AccessMode.READ);
					byte[] bytes = new byte[size];
					random().nextBytes(bytes);
					Files.write(file, bytes);

					assertArrayEquals(bytes, read(resource));
				}));
	}

	/**
	 * Test method for writing.
	 * @throws IOException
	 */
	@TestFactory
	@Tag(RANDOMIZED)
	Stream<DynamicTest> testWrite() throws IOException {
		return random().ints(10, 1, 1<<15)
				.mapToObj(size -> dynamicTest("bytes="+size, () -> {
					Path file = tempFile();
					FileResource resource = new FileResource(file, AccessMode.READ_WRITE);
					byte[] bytes = new byte[size];
					random().nextBytes(bytes);
					write(resource, bytes);

					Files.write(file, bytes);

					byte[] read = Files.readAllBytes(file);

					assertArrayEquals(bytes, read);
				}));
	}

}
