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
package de.ims.icarus2.examples;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import de.ims.icarus2.common.formats.Template;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.io.FileManager;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.standard.DefaultManifestRegistry;
import de.ims.icarus2.model.manifest.xml.ManifestXmlReader;
import de.ims.icarus2.model.standard.io.DefaultFileManager;
import de.ims.icarus2.model.standard.registry.DefaultCorpusManager;
import de.ims.icarus2.model.standard.registry.metadata.JAXBMetadataRegistry;
import de.ims.icarus2.util.io.resource.FileResourceProvider;
import de.ims.icarus2.util.io.resource.ResourceProvider;

/**
 * @author Markus Gärtner
 *
 */
public class ICARUS2Sample_08_CustomizeManager {

	public static void main(String[] args) throws Exception {

		Path folder = Files.createTempDirectory("ICARUS2Sample_08_CustomizeManager");
		Path metadataFile = folder.resolve("metadata.txt");

		/*
		 * We use a file manager with a new temporary folder as root
		 * and also inject a metadata registry that stores data in an
		 * XML format.
		 */
		FileManager fileManager = new DefaultFileManager(folder);
		ResourceProvider resourceProvider = new FileResourceProvider();
		MetadataRegistry metadataRegistry = new JAXBMetadataRegistry(resourceProvider.getResource(metadataFile), StandardCharsets.UTF_8);

		// Setup the corpus management
		CorpusManager corpusManager = DefaultCorpusManager.builder()
				.fileManager(fileManager)
				.resourceProvider(resourceProvider)
				.metadataRegistry(metadataRegistry)
				.manifestRegistry(new DefaultManifestRegistry())
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
					.url(ICARUS2Sample_08_CustomizeManager.class.getResource("ConnectCorpus01.imf.xml"))
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
		 * For demonstration purposes we dump the content of the file
		 * we designated for metadata storage above.
		 */

		Files.readAllLines(metadataFile, StandardCharsets.UTF_8).forEach(System.out::println);
	}
}
