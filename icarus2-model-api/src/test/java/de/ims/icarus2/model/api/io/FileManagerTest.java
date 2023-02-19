/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.io;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.stubId;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.test.GenericTest;

/**
 * @author Markus Gärtner
 *
 */
public interface FileManagerTest<M extends FileManager> extends GenericTest<M> {

	/**
	 * Test method for {@link de.ims.icarus2.model.api.io.FileManager#getRootFolder()}.
	 */
	@Test
	default void testGetRootFolder() {
		assertNotNull(create().getRootFolder());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.io.FileManager#getSharedFolder(java.lang.String)}.
	 */
	@Test
	default void testGetSharedFolder() {
		M manager = create();
		for(FileManager.CommonFolders folder : FileManager.CommonFolders.values()) {
			assertNotNull(manager.getSharedFolder(folder.getId()));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.io.FileManager#getCorpusFolder(de.ims.icarus2.model.manifest.api.CorpusManifest)}.
	 */
	@Test
	default void testGetCorpusFolder() {
		M manager = create();

		CorpusManifest corpus1 = stubId(mockTypedManifest(ManifestType.CORPUS_MANIFEST), "corpus1");
		CorpusManifest corpus2 = stubId(mockTypedManifest(ManifestType.CORPUS_MANIFEST), "corpus2");

		Path path1 = manager.getCorpusFolder(corpus1);
		Path path2 = manager.getCorpusFolder(corpus2);

		if(path1==null || path2==null) {
			assertTrue(path1==path2);
		} else {
			assertNotEquals(path1, path2);
		}
	}

}
