/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.examples;

import java.io.IOException;

import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.standard.DefaultManifestRegistry;
import de.ims.icarus2.model.manifest.xml.ManifestXmlReader;

/**
 * @author Markus Gärtner
 *
 */
public class ReadManifests {

	public static void main(String[] args) throws IOException, SAXException {
		// -- registry setup boilerplate code --
		ManifestRegistry registry = new DefaultManifestRegistry();
		// -------------------------------

		// Configure the reader (here with direct location definition via the builder)
		ManifestXmlReader manifestXmlReader = ManifestXmlReader.newBuilder()
				.registry(registry)
				.useImplementationDefaults()
				.source(ManifestLocation.newBuilder()
//						.file(Paths.get("myCorpus.imf.xml"))
						.url(ReadManifests.class.getResource("ReadManifests01.imf.xml"))
						.template()
						.build())
				.build();

		// Read manifests and automatically register them
		manifestXmlReader.readAndRegisterAll();

		System.out.println(registry.getTemplates());
	}
}
