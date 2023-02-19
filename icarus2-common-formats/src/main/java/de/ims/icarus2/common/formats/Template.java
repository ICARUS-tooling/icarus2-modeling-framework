/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.common.formats;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.URL;
import java.util.EnumSet;
import java.util.Set;

import org.xml.sax.SAXException;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.standard.DefaultManifestRegistry;
import de.ims.icarus2.model.manifest.xml.ManifestXmlReader;
import de.ims.icarus2.util.function.ThrowingConsumer;

/**
 * Programmatic entry point for the management and application of existing templates.
 *
 * @author Markus Gärtner
 *
 */
public enum Template implements ThrowingConsumer<ManifestRegistry, IcarusApiException> {
	CORE("templates.base.xml"),
	CONLL("conll/templates.conll.xml"),
	;

	private final String[] files;
	private Template[] dependencies = {};

	private Template(String...files) {
		this.files = files;
	}

	// DEPENDENCY LINKING

	private void setDependencies(Template...dependencies) {
		this.dependencies = dependencies;
	}

	static {
		CONLL.setDependencies(CORE);
	}

	// APPLICATION OF TEMPLATES

	/**
	 * Creates and returns a {@link ManifestRegistry} that is pre-configured with
	 * the CoNLL templates.
	 * Any exceptions encountered will be wrapped in a {@link IcarusRuntimeException}
	 * of code {@link ManifestErrorCode#MANIFEST_ERROR}.
	 */
	public static ManifestRegistry createRegistry(Template...templates) throws IcarusApiException {
		ManifestRegistry registry = new DefaultManifestRegistry();
		try {
			applyTemplates(registry, templates);
		} catch (IOException e) {
			throw new IcarusApiException(ManifestErrorCode.MANIFEST_ERROR,
					"Failed to read templates", e);
		}
		return registry;
	}

	public static void applyCoreTemplates(ManifestRegistry registry) throws IcarusApiException, IOException {
		requireNonNull(registry);

		ManifestXmlReader reader = ManifestXmlReader.builder()
				.registry(registry)
				.useImplementationDefaults()
				.build();
		addTemplate(reader, CORE);

		try {
			reader.readAndRegisterAll();
		} catch (SAXException e) {
			throw new IcarusApiException(ManifestErrorCode.MANIFEST_ERROR, "Unexpected error while loading core templates", e);
		}
	}

	public static void applyTemplates(ManifestRegistry registry, Template...templates) throws IcarusApiException, IOException {
		requireNonNull(registry);
		requireNonNull(templates);
		checkArgument(templates.length>0);

		Set<Template> pending = EnumSet.noneOf(Template.class);
		for(Template tpl : templates) {
			collect(pending, tpl);
		}

		ManifestXmlReader reader = ManifestXmlReader.builder()
				.registry(registry)
				.useImplementationDefaults()
				.build();

		while(!pending.isEmpty()) {
			boolean found = false;
			// Just brute-force by finding the first template each round that has all its dependencies parsed already
			for(Template tpl : pending) {
				if(hasNoPendingDependencies(tpl, pending)) {
					// Not a really nice design. We prevent the ConcurrentModificationException by exiting the loop
					pending.remove(tpl);
					addTemplate(reader, tpl);
					found = true;
					break;
				}
			}

			if(!found)
				throw new InternalError("Cyclic dependency in templates: "+pending.toString());
		}

		try {
			reader.readAndRegisterAll();
		} catch (SAXException e) {
			throw new IcarusApiException(ManifestErrorCode.MANIFEST_ERROR, "Unexpected error while loading templates", e);
		}
	}

	private static void collect(Set<Template> set, Template tpl) {
		if(set.add(tpl)) {
			for(Template dep : tpl.dependencies) {
				collect(set, dep);
			}
		}
	}

	private static boolean hasNoPendingDependencies(Template tpl, Set<Template> pending) {
		if(tpl.dependencies.length==0) {
			return true;
		}
		for(Template dep : tpl.dependencies) {
			if(pending.contains(dep)) {
				return false;
			}
		}
		return true;
	}

	private static void addTemplate(ManifestXmlReader reader, Template tpl) {
		for(String path : tpl.files) {
			reader.addSource(ManifestLocation.builder()
					.url(toUrl(path))
					.template()
					.build());
		}
	}

	private static URL toUrl(String path) {
		return Template.class.getResource(path);
	}

	@Override
	public void accept(ManifestRegistry registry) throws IcarusApiException {
		try {
			applyTemplates(registry, this);
		} catch (IOException e) {
			throw new IcarusApiException(GlobalErrorCode.IO_ERROR, "Failed to read template: "+this, e);
		}
	}

}
