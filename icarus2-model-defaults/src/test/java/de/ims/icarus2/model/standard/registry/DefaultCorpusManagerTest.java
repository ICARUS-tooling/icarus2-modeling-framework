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
package de.ims.icarus2.model.standard.registry;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import java.util.function.BiFunction;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.registry.CorpusManagerTest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.standard.DefaultManifestRegistry;
import de.ims.icarus2.model.standard.io.DefaultFileManager;
import de.ims.icarus2.model.standard.registry.DefaultCorpusManager.Builder;
import de.ims.icarus2.model.standard.registry.metadata.VirtualMetadataRegistry;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.util.BuilderTest;
import de.ims.icarus2.util.io.resource.VirtualResourceProvider;

/**
 * @author Markus Gärtner
 *
 */
class DefaultCorpusManagerTest implements CorpusManagerTest<DefaultCorpusManager> {

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends DefaultCorpusManager> getTestTargetClass() {
		return DefaultCorpusManager.class;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public DefaultCorpusManager createTestInstance(TestSettings settings) {
		return settings.process(DefaultCorpusManager.builder()
				.fileManager(new DefaultFileManager(Paths.get(".")))
				.resourceProvider(new VirtualResourceProvider())
				.manifestRegistry(new DefaultManifestRegistry())
				.metadataRegistry(new VirtualMetadataRegistry())
				.build());
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.CorpusManagerTest#createWithCustomProducer(java.util.function.Function)
	 */
	@Override
	public DefaultCorpusManager createCustomManager(BiFunction<CorpusManager, CorpusManifest,
			Corpus> corpusProducer, TestSettings settings) {
		return settings.process(DefaultCorpusManager.builder()
				.fileManager(new DefaultFileManager(Paths.get(".")))
				.resourceProvider(new VirtualResourceProvider())
				.manifestRegistry(new DefaultManifestRegistry())
				.metadataRegistry(new VirtualMetadataRegistry())
				.corpusProducer(corpusProducer)
				.build());
	}

	@Nested
	class ForBuilder implements BuilderTest<DefaultCorpusManager, DefaultCorpusManager.Builder> {

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
			return settings.process(DefaultCorpusManager.builder());
		}

		@Test
		void testDefaultEnvironment() {
			Builder builder = create();
			builder.defaultEnvironment();

			assertThat(builder.getFileManager()).isNotNull();
			assertThat(builder.getResourceProvider()).isNotNull();
			assertThat(builder.getManifestRegistry()).isNotNull();
			assertThat(builder.getMetadataRegistry()).isNotNull();
		}
	}
}
