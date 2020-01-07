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
package de.ims.icarus2.filedriver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.filedriver.FileDriver.Builder;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.util.BuilderTest;

/**
 * @author Markus Gärtner
 *
 */
class FileDriverTest {

	//TODO
	class Tmp {

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#addItem(de.ims.icarus2.model.api.layer.ItemLayer, de.ims.icarus2.model.api.members.item.Item, long)}.
		 */
		@Test
		void testAddItem() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#removeItem(de.ims.icarus2.model.api.layer.ItemLayer, de.ims.icarus2.model.api.members.item.Item, long)}.
		 */
		@Test
		void testRemoveItem() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#moveItem(de.ims.icarus2.model.api.layer.ItemLayer, de.ims.icarus2.model.api.members.item.Item, long, long)}.
		 */
		@Test
		void testMoveItem() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#getConverter()}.
		 */
		@Test
		void testGetConverter() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#getFileObject(int)}.
		 */
		@Test
		void testGetFileObject() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#getDataFiles()}.
		 */
		@Test
		void testGetDataFiles() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#getMetadataRegistry()}.
		 */
		@Test
		void testGetMetadataRegistry() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#getFileStates()}.
		 */
		@Test
		void testGetFileStates() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#getResourceProvider()}.
		 */
		@Test
		void testGetResourceProvider() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#resetMappings()}.
		 */
		@Test
		void testResetMappings() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#forEachModule(java.util.function.Consumer)}.
		 */
		@Test
		void testForEachModule() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#getItemCount(de.ims.icarus2.model.api.layer.ItemLayer)}.
		 */
		@Test
		void testGetItemCountItemLayer() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#getItemCount(de.ims.icarus2.model.manifest.api.ItemLayerManifestBase)}.
		 */
		@Test
		void testGetItemCountItemLayerManifestBaseOfQ() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#getLayerBuffer(de.ims.icarus2.model.api.layer.ItemLayer)}.
		 */
		@Test
		void testGetLayerBuffer() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#getChunkIndex(de.ims.icarus2.model.api.layer.ItemLayer)}.
		 */
		@Test
		void testGetChunkIndex() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#getItem(de.ims.icarus2.model.api.layer.ItemLayer, long)}.
		 */
		@Test
		void testGetItem() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#hasChunkIndex()}.
		 */
		@Test
		void testHasChunkIndex() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#scanFile(int)}.
		 */
		@Test
		void testScanFile() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#load(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.layer.ItemLayer, java.util.function.Consumer)}.
		 */
		@Test
		void testLoad() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#release(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.layer.ItemLayer)}.
		 */
		@Test
		void testRelease() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#loadFile(int, java.util.function.Consumer)}.
		 */
		@Test
		void testLoadFile() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#loadAllFiles(java.util.function.Consumer)}.
		 */
		@Test
		void testLoadAllFiles() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#getEncoding()}.
		 */
		@Test
		void testGetEncoding() {
			fail("Not yet implemented"); // TODO
		}

	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.FileDriver#builder()}.
	 */
	@Test
	void testBuilder() {
		assertThat(FileDriver.builder()).isNotNull();
	}

	@Nested
	class ForBuilder implements BuilderTest<FileDriver, FileDriver.Builder> {

		/**
		 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
		 */
		@Override
		public Class<?> getTestTargetClass() {
			return Builder.class;
		}

		/**
		 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
		 */
		@Override
		public Builder createTestInstance(TestSettings settings) {
			return settings.process(FileDriver.builder());
		}

	}
}
