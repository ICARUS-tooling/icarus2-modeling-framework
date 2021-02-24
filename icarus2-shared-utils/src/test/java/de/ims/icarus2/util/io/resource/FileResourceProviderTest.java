/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.io.TempDir;

import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.util.io.IOUtil;

/**
 * @author Markus Gärtner
 *
 */
class FileResourceProviderTest implements ResourceProviderTest<FileResourceProvider> {

	@TempDir
	Path tempFolder;

	/**
	 * @throws IOException
	 * @see de.ims.icarus2.util.io.resource.ResourceProviderTest#cleanup(de.ims.icarus2.util.io.resource.ResourceProvider, java.nio.file.Path[])
	 */
	@Override
	public void cleanup(FileResourceProvider provider, Path... paths) throws IOException {
		for(Path path : paths) {
			if(!Files.exists(path)) {
				continue;
			}
			if(Files.isDirectory(path)) {
				IOUtil.deleteDirectory(path);
			} else {
				Files.delete(path);
			}
		}
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends FileResourceProvider> getTestTargetClass() {
		return FileResourceProvider.class;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public FileResourceProvider createTestInstance(TestSettings settings) {
		return settings.process(new FileResourceProvider());
	}

	/**
	 * @throws IOException
	 * @see de.ims.icarus2.util.io.resource.ResourceProviderTest#createRoot()
	 */
	@Override
	public Path createRoot() throws IOException {
		return tempFolder;
	}

	// CHANGED TEST BEHAVIOR!!!

	/**
	 * Changed test behavior, since the {@link FileResourceProvider} does
	 * <b>not</b> require files to actually exist before creating new
	 * {@link FileResource} objects!
	 *
	 * @throws IOException
	 * @see de.ims.icarus2.util.io.resource.ResourceProviderTest#testGetUnregisteredResource()
	 */
	@Override
	public void testGetUnregisteredResource() throws IOException {
		assertNotNull(create().getResource(createRoot().resolve("file")));
	}
}
