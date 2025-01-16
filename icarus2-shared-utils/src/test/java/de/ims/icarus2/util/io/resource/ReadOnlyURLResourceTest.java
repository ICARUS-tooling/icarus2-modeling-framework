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

import static de.ims.icarus2.SharedTestUtils.assertIcarusException;
import static de.ims.icarus2.util.collections.CollectionUtils.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;
import org.opentest4j.TestAbortedException;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.guard.ApiGuard;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.AccessMode;

/**
 * @author Markus Gärtner
 *
 */
class ReadOnlyURLResourceTest implements IOResourceTest<ReadOnlyURLResource> {


	@TempDir
	Path tempFolder;

	private Path tempFile() throws IOException {
		return Files.createTempFile(tempFolder, "readOnlyUrlResourceTest", ".tmp");
	}

	private URL toUrl(Path path) throws MalformedURLException {
		return path.toUri().toURL();
	}

	/**
	 * @see de.ims.icarus2.test.ApiGuardedTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
	 */
	@Override
	public void configureApiGuard(ApiGuard<ReadOnlyURLResource> apiGuard) {
		IOResourceTest.super.configureApiGuard(apiGuard);
		apiGuard.parameterResolver(URL.class,
				instance -> {
					try {
						return toUrl(tempFile());
					} catch (IOException e) {
						throw new TestAbortedException("Failed to create dummy url", e);
					}
				});
	}

	@Override
	public Class<? extends ReadOnlyURLResource> getTestTargetClass() {
		return ReadOnlyURLResource.class;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#create()
	 */
	@Override
	public ReadOnlyURLResource create() {
		try {
			return create(toUrl(tempFile()));
		} catch (IOException e) {
			throw new TestAbortedException("Failed to create basic test instance", e);
		}
	}

	@Override
	public ReadOnlyURLResource create(AccessMode accessMode) throws IOException {
		assertEquals(AccessMode.READ, accessMode);
		Path file = tempFile();
		return new ReadOnlyURLResource(file, toUrl(file));
	}

	private ReadOnlyURLResource create(URL url) {
		return new ReadOnlyURLResource(Paths.get("."), url);
	}

	@Override
	public Set<AccessMode> getSupportedAccessModes() {
		return singleton(AccessMode.READ);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ReadOnlyURLResource#getSource()}.
	 */
	@Test
	void testGetSource() throws IOException {
		URL url = toUrl(tempFile());
		assertSame(url, create(url).getSource());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ReadOnlyURLResource#delete()}.
	 */
	@Test
	void testDelete() throws IOException {
		Path file = tempFile();
		ReadOnlyURLResource resource = create(toUrl(file));
		resource.prepare();

		assertTrue(Files.exists(file, LinkOption.NOFOLLOW_LINKS), "File not created");

		resource.delete();

		assertNull(resource.getSource());
		assertTrue(Files.exists(file, LinkOption.NOFOLLOW_LINKS), "File deleted");
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ReadOnlyURLResource#prepare()}.
	 * @throws IOException
	 */
	@Test
	void testPrepare() throws IOException {
		create().prepare();
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ReadOnlyURLResource#size()}.
	 */
	@Test
	void testSizeEmpty() throws IOException {
		ReadOnlyURLResource resource = create();
		resource.prepare();
		assertEquals(0, resource.size());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ReadOnlyURLResource#size()}.
	 */
	@Test
	void testSizeUnprepared() {
		assertIcarusException(GlobalErrorCode.ILLEGAL_STATE,
				() -> create().size());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ReadOnlyURLResource#size()}.
	 * @throws IOException
	 */
	@TestFactory
	@RandomizedTest
	Stream<DynamicTest> testSizeFilled(RandomGenerator rand) throws IOException {
		return rand.ints(10, 1, 1<<15)
				.mapToObj(size -> dynamicTest("bytes="+size, () -> {
					Path file = tempFile();
					ReadOnlyURLResource resource = create(toUrl(file));
					byte[] bytes = new byte[size];
					rand.nextBytes(bytes);
					Files.write(file, bytes);

					resource.prepare();
					assertEquals(size, resource.size());
				}));
	}

}
