/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.common.formats.conll;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.filedriver.Converter;
import de.ims.icarus2.filedriver.FileDriver;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest.PathType;
import de.ims.icarus2.model.manifest.api.ManifestFactory;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.standard.DefaultManifestFactory;
import de.ims.icarus2.model.standard.registry.DefaultCorpusManager;

/**
 * @author Markus Gärtner
 *
 */
class CoNLL2009ConverterTest {

	@Nested
	class WithFullCorpus {

		private CoNLL2009Converter converter;
		private CorpusManager manager;
		private Corpus corpus;

		@BeforeEach
		void setUp() throws Exception {

			manager = DefaultCorpusManager.builder()
					.defaultEnvironment()
					.build();

			ManifestRegistry registry = manager.getManifestRegistry();
			CoNLLTemplates.readTeplates(registry);

			ManifestFactory factory = new DefaultManifestFactory(
					ManifestLocation.builder()
					.input()
					.content("")
					.virtual()
					.build(), registry);

			CorpusManifest manifest = factory.create(CorpusManifest.class)
					.addRootContextManifest((ContextManifest) factory.create(ContextManifest.class)
							.setId("root")
							.addLocationManifest(factory.create(LocationManifest.class)
									.setRootPathType(PathType.FILE)
									.setRootPath(CoNLLTestUtils.getCorpusUrl().toExternalForm()))
							.setTemplateId(CoNLLTemplates.CONLL09_TEMPLATE))
					.setId("test");

			registry.addCorpusManifest(manifest);

			corpus = manager.connect(manifest);

			Driver driver = corpus.getRootContext().getDriver();
			assertThat(driver).isInstanceOf(FileDriver.class);

			Converter converter = ((FileDriver)driver).getConverter();
			assertThat(converter).isInstanceOf(CoNLL2009Converter.class);

			this.converter = (CoNLL2009Converter) converter;
		}

		@AfterEach
		void tearDown() {

		}

		@Test
		void testAddNotify() {

		}
	}

}
