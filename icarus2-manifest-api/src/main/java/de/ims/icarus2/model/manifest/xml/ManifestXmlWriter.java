/**
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
package de.ims.icarus2.model.manifest.xml;

import static java.util.Objects.requireNonNull;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;

import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.xml.delegates.DefaultManifestXmlDelegateFactory;
import de.ims.icarus2.util.xml.XmlSerializer;
import de.ims.icarus2.util.xml.stream.XmlStreamSerializer;

/**
 * Implements an xml writing facility for {@link Manifest} objects.
 * TODO explain usage patterns: fill writer with manifests, write and then reset!!!
 *
 * @author Markus Gärtner
 *
 */
public class ManifestXmlWriter extends ManifestXmlProcessor {

	private final ManifestLocation manifestLocation;

	private final List<Manifest> manifests = new ArrayList<>();

	public ManifestXmlWriter(ManifestLocation manifestLocation, ManifestXmlDelegateFactory delegateFactory) {
		super(delegateFactory);

		requireNonNull(manifestLocation);

		this.manifestLocation = manifestLocation;
	}

	public ManifestXmlWriter(ManifestLocation manifestLocation) {
		this(manifestLocation, new DefaultManifestXmlDelegateFactory());
	}

	protected void checkManifest(Manifest manifest) {
		requireNonNull(manifest);

		//FIXME enable flag to turn off template check, so we can serialize whatever manifest desired
		if(manifest.getManifestLocation().isTemplate()!=manifestLocation.isTemplate())
			throw new IllegalArgumentException("Manifest 'isTemplate' flag differs from value declared for writer: "+manifest.isTemplate()); //$NON-NLS-1$

		// Make sure we have a valid xml delegate saved for the manifest's type
		getDelegate(manifest);
	}

	public ManifestXmlWriter addManifest(Manifest manifest) {
		checkManifest(manifest);

		synchronized (manifests) {
			manifests.add(manifest);
		}

		return this;
	}

	public void addManifests(List<? extends Manifest> manifests) {
		requireNonNull(manifests);

		synchronized (manifests) {
			for(Manifest manifest : manifests) {
				addManifest(manifest);
			}
		}
	}

	public ManifestXmlWriter writeAll() throws Exception {
		synchronized (manifests) {
			if(!manifests.isEmpty()) {
				try(XmlSerializer serializer = newSerializer(manifestLocation.getOutput())) {

					String rootTag = manifestLocation.isTemplate() ? ManifestXmlTags.TEMPLATES : ManifestXmlTags.CORPORA;

					serializer.startDocument();
					serializer.startElement(rootTag);

					ManifestXmlUtils.writeDefaultXsiInfo(serializer);

					writeInline(serializer);

					serializer.endElement(rootTag);
					serializer.endDocument();
				}
			}
		}
		return this;
	}

	public void writeInline(XmlSerializer serializer) throws Exception {
		synchronized (manifests) {
			if(manifests.isEmpty()) {
				// Nothing to do here
				return;
			}

			write0(serializer);
		}
	}

	@SuppressWarnings("unchecked")
	protected void write0(XmlSerializer serializer) throws Exception {
		for(Iterator<Manifest> it = manifests.iterator(); it.hasNext();) {
			Manifest manifest = it.next();

			@SuppressWarnings("rawtypes")
			ManifestXmlDelegate delegate = getDelegate(manifest);

			delegate.reset(manifest);

			delegate.writeXml(serializer);

			if(it.hasNext()) {
				serializer.writeLineBreak();
			}
		}
	}

	public ManifestXmlWriter reset() {
		synchronized (manifests) {
			manifests.clear();

			resetDelegates();
		}
		return this;
	}

	/**
	 * Defaults to {@link #defaultCreateSerializer(Writer)}.
	 * Subclasses can override this method to customize the
	 * actual serializer implementation to be used.
	 *
	 * @param out
	 * @return
	 * @throws Exception
	 */
	protected XmlSerializer newSerializer(Writer out) throws Exception {
		return defaultCreateSerializer(out);
	}

	/**
	 * @see XmlStreamSerializer
	 *
	 * @param out
	 * @return
	 * @throws Exception
	 */
	public static XmlSerializer defaultCreateSerializer(Writer out) throws Exception {

		XMLOutputFactory factory = XMLOutputFactory.newFactory();

		return XmlStreamSerializer.withNamespace(factory.createXMLStreamWriter(out),
				ManifestXmlUtils.MANIFEST_NS_PREFIX, ManifestXmlUtils.MANIFEST_NAMESPACE_URI);
	}
}
