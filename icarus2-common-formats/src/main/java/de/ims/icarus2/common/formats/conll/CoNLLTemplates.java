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

import java.io.IOException;
import java.net.URL;

import org.xml.sax.SAXException;

import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.standard.DefaultManifestRegistry;
import de.ims.icarus2.model.manifest.xml.ManifestXmlReader;

/**
 * Entry point for programmatically accessing the CoNLL templates.
 *
 * @author Markus Gärtner
 *
 */
public final class CoNLLTemplates {

	public static final String CONLL09_TEMPLATE = "common.format.conll09";

	private CoNLLTemplates() { /* no-op */ }

	private static final String PATH = "templates.conll.xml";

	public static URL getUrl() {
		return CoNLLTemplates.class.getResource(PATH);
	}

	/**
	 * Attempts to read the entire set of templates and register them to
	 * the given registry. This method forwards all the exceptions that
	 * can occur. The reader used for parsing is configured with the
	 * default settings.
	 *
	 * @param registry
	 * @throws IOException
	 * @throws SAXException
	 */
	public static void registerTeplates(ManifestRegistry registry) throws IOException, SAXException {
		ManifestXmlReader reader = ManifestXmlReader.builder()
				.registry(registry)
				.useImplementationDefaults()
				.build();

		reader.addSource(ManifestLocation.builder()
				.url(getUrl())
				.template()
				.build());
		reader.readAndRegisterAll();
	}

	/**
	 * Creates and returns a {@link ManifestRegistry} that is pre-configured with
	 * the CoNLL templates.
	 * Any exceptions encountered will be wrapped in a {@link IcarusRuntimeException}
	 * of code {@link ManifestErrorCode#MANIFEST_ERROR}.
	 */
	public static ManifestRegistry createRegistry() {
		ManifestRegistry registry = new DefaultManifestRegistry();

		try {
			registerTeplates(registry);
		} catch (IOException | SAXException e) {
			throw new IcarusRuntimeException(ManifestErrorCode.MANIFEST_ERROR,
					"Failed to read CoNNL default templates", e);
		}

		return registry;
	}
}
