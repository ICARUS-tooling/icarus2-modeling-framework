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
package de.ims.icarus2.model.manifest.io;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import org.xml.sax.SAXException;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.standard.DefaultManifestRegistry;
import de.ims.icarus2.model.manifest.xml.ManifestXmlReader;
import de.ims.icarus2.model.manifest.xml.delegates.DefaultManifestXmlDelegateFactory;

/**
 * @author Markus Gärtner
 *
 */
public class ManifestIO {

	private static final ManifestRegistry NO_REGISTRY = null;

	private static List<Manifest> load0(ManifestLocation location, ManifestRegistry registry, int limit)
			throws SAXException, IOException {
		if(registry==null) {
			registry = new DefaultManifestRegistry();
		}

		ManifestXmlReader reader = ManifestXmlReader.newBuilder()
				.delegateFactory(new DefaultManifestXmlDelegateFactory())
				.registry(registry)
				.source(location)
				.build();

		List<Manifest> loadedManifests = location.isTemplate() ?
				reader.parseTemplates() : reader.parseCorpora();

		if(loadedManifests.size()==0)
			throw new ManifestException(GlobalErrorCode.IO_ERROR,
					"No manifest found in "+location.getUrl());
		if(loadedManifests.size()>limit)
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
					"More than "+limit+" manifests found in "+location.getUrl());

		if(location.isTemplate()) {
			registry.addTemplates(loadedManifests);
		} else {
			for(Manifest corpus : loadedManifests) {
				registry.addCorpusManifest((CorpusManifest) corpus);
			}
		}

		return loadedManifests;
	}

	@SuppressWarnings("unchecked")
	private static <M extends Manifest> M loadSingle0(ManifestLocation location, ManifestRegistry registry)
			throws SAXException, IOException {
		return (M) load0(location, registry, 1).get(0);
	}

	public static <M extends Manifest> M loadTemplate(URL url) throws SAXException, IOException {
		return loadSingle0(ManifestLocation.newBuilder()
				.url(url)
				.template()
				.build(),
				NO_REGISTRY);
	}

	public static <M extends Manifest> M loadTemplate(Path file) throws SAXException, IOException {
		return loadSingle0(ManifestLocation.newBuilder()
				.file(file)
				.template()
				.build(),
				NO_REGISTRY);
	}

	//TODO add methods for loading corpora and batches
}
