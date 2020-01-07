/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import static de.ims.icarus2.test.TestUtils.assertCollectionEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface ResourceProviderTest<P extends ResourceProvider> extends ApiGuardedTest<P> {

	void cleanup(P provider, Path...paths) throws IOException;

	/** Provides a root folder for testing **/
	Path createRoot() throws IOException;

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ResourceProvider#exists(java.nio.file.Path)}.
	 * @throws IOException
	 */
	@Test
	default void testExists() throws IOException {
		P provider = create();
		Path file = createRoot().resolve("test");

		try {
			assertFalse(provider.exists(file));
		} finally {
			cleanup(provider, file);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ResourceProvider#create(java.nio.file.Path, boolean)}.
	 * @throws IOException
	 */
	@Test
	default void testCreateFolder() throws IOException {
		P provider = create();
		Path folder = createRoot().resolve("folder");
		try {
			provider.create(folder, true);
		} finally {
			cleanup(provider, folder);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ResourceProvider#create(java.nio.file.Path, boolean)}.
	 * @throws IOException
	 */
	@Test
	default void testCreateFile() throws IOException {
		P provider = create();
		Path file = createRoot().resolve("file");
		try {
			provider.create(file, false);
		} finally {
			cleanup(provider, file);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ResourceProvider#isDirectory(java.nio.file.Path)}.
	 * @throws IOException
	 */
	@Test
	default void testIsDirectory() throws IOException {
		P provider = create();
		Path root = createRoot();
		Path file = root.resolve("file");
		Path folder = root.resolve("folder");

		try {
			assertFalse(provider.isDirectory(file));
			assertFalse(provider.isDirectory(folder));

			provider.create(file, false);
			provider.create(folder, true);

			assertFalse(provider.isDirectory(file));
			assertTrue(provider.isDirectory(folder));
		} finally {
			cleanup(provider, file, folder);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ResourceProvider#getLock(java.nio.file.Path)}.
	 */
	@Test
	default void testGetUnregisteredLock() {
		assertIcarusException(GlobalErrorCode.INVALID_INPUT,
				() -> create().getLock(createRoot().resolve("file")));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ResourceProvider#getLock(java.nio.file.Path)}.
	 * @throws IOException
	 */
	@Test
	default void testGetFolderLock() throws IOException {
		P provider = create();
		Path folder = createRoot().resolve("folder");
		provider.create(folder, true);

		try {
			assertIcarusException(GlobalErrorCode.INVALID_INPUT,
					() -> provider.getLock(folder));
		} finally {
			cleanup(provider, folder);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ResourceProvider#getLock(java.nio.file.Path)}.
	 * @throws IOException
	 */
	@Test
	default void testGetLock() throws IOException {
		P provider = create();
		Path file = createRoot().resolve("file");

		try {
			provider.create(file, false);

			Lock lock = provider.getLock(file);
			assertNotNull(lock);

			lock.lock();
			try {
				provider.getResource(file).prepare();
			} finally {
				lock.unlock();
			}
		} finally {
			cleanup(provider, file);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ResourceProvider#children(java.nio.file.Path, java.lang.String)}.
	 * @throws IOException
	 */
	@Test
	default void testChildrenEmpty() throws IOException {
		P provider = create();

		DirectoryStream<Path> stream = provider.children(createRoot(), "*");
		assertNotNull(stream);
		assertFalse(stream.iterator().hasNext());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ResourceProvider#children(java.nio.file.Path, java.lang.String)}.
	 * @throws IOException
	 */
	@Test
	default void testChildrenSimple() throws IOException {
		P provider = create();
		Path root = createRoot();
		Path file1 = root.resolve("file1");
		Path file2 = root.resolve("file2");

		try {
			provider.create(file1, false);
			provider.create(file2, false);

			DirectoryStream<Path> stream = provider.children(root, "*");
			assertNotNull(stream);
			Iterator<Path> iterator = stream.iterator();
			assertTrue(iterator.hasNext());

			Set<Path> children = CollectionUtils.<Path>aggregateAsSet(iterator::forEachRemaining);
			assertCollectionEquals(children, file1, file2);
		} finally {
			cleanup(provider, file1, file2);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ResourceProvider#children(java.nio.file.Path, java.lang.String)}.
	 * @throws IOException
	 */
	@Test
	default void testChildrenComplex() throws IOException {
		P provider = create();
		Path root = createRoot();
		Path file1 = root.resolve("file1.txt");
		Path file2 = root.resolve("file2.xml");

		try {
			provider.create(file1, false);
			provider.create(file2, false);

			DirectoryStream<Path> stream = provider.children(root, "*.xml");
			assertNotNull(stream);
			Iterator<Path> iterator = stream.iterator();
			assertTrue(iterator.hasNext());

			Set<Path> children = CollectionUtils.<Path>aggregateAsSet(iterator::forEachRemaining);
			assertCollectionEquals(children, file2);
		} finally {
			cleanup(provider, file1, file2);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ResourceProvider#getResource(java.nio.file.Path)}.
	 * @throws IOException
	 */
	@Test
	default void testGetUnregisteredResource() throws IOException {
		assertIcarusException(GlobalErrorCode.INVALID_INPUT,
				() -> create().getResource(createRoot().resolve("file")));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ResourceProvider#getResource(java.nio.file.Path)}.
	 * @throws IOException
	 */
	@Test
	default void testGetFolderResource() throws IOException {
		P provider = create();
		Path folder = createRoot().resolve("folder");

		try {
			provider.create(folder, true);

			assertIcarusException(GlobalErrorCode.INVALID_INPUT,
					() -> provider.getResource(folder));
		} finally {
			cleanup(provider, folder);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ResourceProvider#getResource(java.nio.file.Path)}.
	 * @throws IOException
	 */
	@Test
	default void testGetFileResource() throws IOException {
		P provider = create();
		Path file = createRoot().resolve("file");

		try {
			provider.create(file, false);

			assertNotNull(provider.getResource(file));
		} finally {
			cleanup(provider, file);
		}
	}

}
