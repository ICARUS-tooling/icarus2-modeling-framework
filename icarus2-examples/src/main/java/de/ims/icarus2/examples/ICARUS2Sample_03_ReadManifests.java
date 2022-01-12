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
public class ICARUS2Sample_03_ReadManifests {

	public static void main(String[] args) throws IOException, SAXException {
		// -- registry setup boilerplate code --
		ManifestRegistry registry = new DefaultManifestRegistry();
		// -------------------------------

		// Configure the reader (here with direct location definition via the builder)
		ManifestXmlReader manifestXmlReader = ManifestXmlReader.builder()
				// Use above registry to store manifests
				.registry(registry)
				// Let the reader select its default settings
				.useImplementationDefaults()
				.source(ManifestLocation.builder()
						/*
						 * Alternatively you could use the file(Path) method on the builder
						 * to point it to a physical file on the file system.
						 */
						.url(ICARUS2Sample_03_ReadManifests.class.getResource("ReadManifests01.imf.xml"))
						.template()
						.build())
				.build();

		// Read manifests and automatically register them with the underlying registry
		manifestXmlReader.readAndRegisterAll();

		// Process manifests, in this case we just dump their toString() output
		System.out.println(registry.getTemplates());
	}
}
