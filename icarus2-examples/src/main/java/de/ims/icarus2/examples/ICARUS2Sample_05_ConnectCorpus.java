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
package de.ims.icarus2.examples;

import static de.ims.icarus2.model.util.ModelUtils.getName;

import de.ims.icarus2.common.formats.Template;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.xml.ManifestXmlReader;
import de.ims.icarus2.model.standard.registry.DefaultCorpusManager;

/**
 * @author Markus Gärtner
 *
 */
public class ICARUS2Sample_05_ConnectCorpus {

	public static void main(String[] args) throws Exception {

		// Setup the corpus management
		CorpusManager corpusManager = DefaultCorpusManager.builder()
				/* Setup the manager. This is the place to customize
				 * the environment or inject new implementations.
				 * We use the (virtual) default environment for this demo.
				 */
				.defaultEnvironment()
				.build();

		// Register our corpus manifest
		ManifestRegistry manifestRegistry = corpusManager.getManifestRegistry();
		Template.applyTemplates(manifestRegistry, Template.CONLL);
		ManifestXmlReader.builder()
			.registry(manifestRegistry)
			.useImplementationDefaults()
			.build()
			.addSource(ManifestLocation.builder()
					.input()
					.url(ICARUS2Sample_05_ConnectCorpus.class.getResource("ConnectCorpus01.imf.xml"))
					.build())
			.readAndRegisterAll();

		// Connect to the corpus resource
		CorpusManifest corpusManifest = manifestRegistry.getCorpusManifest("corpus.test.connect")
				.orElseThrow(ManifestException.error("Missing test corpus"));

		// Obtain live corpus now
		Corpus corpus = corpusManager.connect(corpusManifest);

		// Ensure drivers are connected
		corpus.connectAll();

		/*
		 * Finally do something with the corpus...
		 *
		 * To not take away content from subsequent code examples we opt
		 * for dumping this corpus' metadata. This will show some
		 */

		System.out.printf("Metadata for corpus '%s':%n", getName(corpusManifest));
		corpus.getMetadataRegistry().forEachEntry((key, value) -> System.out.printf("%s -> %s%n", key, value));
	}
}
