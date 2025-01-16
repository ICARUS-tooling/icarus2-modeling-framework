/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ims.icarus2.util.io.resource;

import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
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

	/**
	 * @see de.ims.icarus2.util.io.resource.IOResourceTest#getSupportedAccessModes()
	 */
	@Override
	public Set<AccessMode> getSupportedAccessModes() {
		return set(AccessMode.values());
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

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.FileResource#FileResource(java.nio.file.Path)}.
	 * @throws IOException
	 */
	@Test
	void testConstructorWithPath() throws IOException {
		Path file = tempFile();
		assertSame(file, new FileResource(file).getPath());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.FileResource#getPath()}.
	 * @throws IOException
	 */
	@Test
	void testGetLocalPath() throws IOException {
		Path file = tempFile();
		FileResource resource = new FileResource(file, AccessMode.READ);
		assertSame(file, resource.getPath());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.FileResource#FileResource(java.nio.file.Path, de.ims.icarus2.util.AccessMode)}.
	 * @throws IOException
	 */
	@ParameterizedTest
	@EnumSource(value=AccessMode.class)
	void testConstructorWithAccessMode(AccessMode accessMode) throws IOException {
		Path file = tempFile();
		FileResource resource = new FileResource(file, accessMode);
		assertSame(file, resource.getPath());
		assertSame(accessMode, resource.getAccessMode());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.FileResource#delete()}.
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
	 * Test method for {@link de.ims.icarus2.util.io.resource.FileResource#size()}.
	 * @throws IOException
	 */
	@Test
	void testSizeEmpty() throws IOException {
		assertEquals(0, create().size());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.FileResource#size()}.
	 * @throws IOException
	 */
	@TestFactory
	@RandomizedTest
	Stream<DynamicTest> testSizeFilled(RandomGenerator rand) throws IOException {
		return rand.ints(10, 1, 1<<15)
				.mapToObj(size -> dynamicTest("bytes="+size, () -> {
					Path file = tempFile();
					FileResource resource = new FileResource(file, AccessMode.READ);
					byte[] bytes = new byte[size];
					rand.nextBytes(bytes);
					Files.write(file, bytes);

					assertEquals(size, resource.size());
				}));
	}

	/**
	 * Test method for reading.
	 * @throws IOException
	 */
	@TestFactory
	@RandomizedTest
	Stream<DynamicTest> testRead(RandomGenerator rand) throws IOException {
		return rand.ints(10, 1, 1<<15)
				.mapToObj(size -> dynamicTest("bytes="+size, () -> {
					Path file = tempFile();
					FileResource resource = new FileResource(file, AccessMode.READ);
					byte[] bytes = new byte[size];
					rand.nextBytes(bytes);
					Files.write(file, bytes);

					assertArrayEquals(bytes, read(resource));
				}));
	}

	/**
	 * Test method for writing.
	 * @throws IOException
	 */
	@TestFactory
	@RandomizedTest
	Stream<DynamicTest> testWrite(RandomGenerator rand) throws IOException {
		return rand.ints(10, 1, 1<<15)
				.mapToObj(size -> dynamicTest("bytes="+size, () -> {
					Path file = tempFile();
					FileResource resource = new FileResource(file, AccessMode.READ_WRITE);
					byte[] bytes = new byte[size];
					rand.nextBytes(bytes);
					write(resource, bytes);

					Files.write(file, bytes);

					byte[] read = Files.readAllBytes(file);

					assertArrayEquals(bytes, read);
				}));
	}

}
