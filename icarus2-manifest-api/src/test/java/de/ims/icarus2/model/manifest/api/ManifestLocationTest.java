/*
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
package de.ims.icarus2.model.manifest.api;

import static de.ims.icarus2.test.TestUtils.TEST_URL;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.ManifestLocation.Builder;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.test.util.Triple;
import de.ims.icarus2.util.BuilderTest;

/**
 * @author Markus Gärtner
 *
 */
class ManifestLocationTest {

	//TODO
	class Tmp {

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestLocation#getUrl()}.
		 */
		@Test
		void testGetUrl() {
			fail("Not yet implemented");
			//TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestLocation#getInput()}.
		 */
		@Test
		void testGetInput() {
			fail("Not yet implemented");
			//TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestLocation#getOutput()}.
		 */
		@Test
		void testGetOutput() {
			fail("Not yet implemented");
			//TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestLocation#isReadOnly()}.
		 */
		@Test
		void testIsReadOnly() {
			fail("Not yet implemented");
			//TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestLocation#isTemplate()}.
		 */
		@Test
		void testIsTemplate() {
			fail("Not yet implemented");
			//TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestLocation#getClassLoader()}.
		 */
		@Test
		void testGetClassLoader() {
			fail("Not yet implemented");
			//TODO
		}

	}

	@Nested
	class ForBuilder implements BuilderTest<ManifestLocation, ManifestLocation.Builder> {

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
			return settings.process(ManifestLocation.builder());
		}

		@Test
		void testUtf8() {
			Builder builder = create();
			builder.utf8();
			assertThat(builder.getCharset()).isSameAs(StandardCharsets.UTF_8);
		}

		/**
		 * @see de.ims.icarus2.util.BuilderTest#invalidOps()
		 */
		@Override
		public List<Triple<String, Class<? extends Throwable>, Consumer<? super Builder>>> invalidOps() {
			return list(
					Triple.triple("file after virtual", IllegalStateException.class, b -> b.virtual().file(Paths.get("."))),
					Triple.triple("file after url", IllegalStateException.class, b -> b.url(TEST_URL).file(Paths.get("."))),
					Triple.triple("url after virtual", IllegalStateException.class, b -> b.virtual().url(TEST_URL)),
					Triple.triple("url after file", IllegalStateException.class, b -> b.url(TEST_URL).url(TEST_URL)),
					Triple.triple("virtual after url", IllegalStateException.class, b -> b.url(TEST_URL).virtual()),
					Triple.triple("virtual after file", IllegalStateException.class, b -> b.file(Paths.get(".")).virtual())
			);
		}

		/**
		 * @see de.ims.icarus2.util.BuilderTest#invalidConfigurations()
		 */
		@Override
		public List<Pair<String, Consumer<? super Builder>>> invalidConfigurations() {
			return list(
					Pair.pair("missing url/file/virtual", b -> b.template().input().content("test")),
					Pair.pair("virtual input without content", b -> b.template().input().virtual()),
					Pair.pair("content without input flag", b -> b.template().content("test").url(TEST_URL))
			);
		}
	}
}
