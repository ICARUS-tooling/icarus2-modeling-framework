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
package de.ims.icarus2.model.manifest.xml.delegates;

import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.api.ForeignImplementationManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.standard.ImplementationManifestImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractForeignImplementationManifestXmlDelegate<M extends ForeignImplementationManifest<M>>
		extends AbstractMemberManifestXmlDelegate<M> {

	private ImplementationManifestXmlDelegate implementationManifestXmlDelegate;

	private ImplementationManifestXmlDelegate getImplementationManifestXmlDelegate() {
		if(implementationManifestXmlDelegate==null) {
			implementationManifestXmlDelegate = new ImplementationManifestXmlDelegate();
		}

		return implementationManifestXmlDelegate;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractModifiableManifest#writeElements(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws XMLStreamException {
		super.writeElements(serializer);

		M manifest = getInstance();

		if(manifest.isLocalImplementation()) {
			getImplementationManifestXmlDelegate().reset(manifest.getImplementationManifest().get()).writeXml(serializer);
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractModifiableManifest#startElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public Optional<ManifestXmlHandler> startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		ManifestXmlHandler handler = null;

		switch (localName) {
		case ManifestXmlTags.IMPLEMENTATION: {
			handler = getImplementationManifestXmlDelegate().reset(new ImplementationManifestImpl(getInstance()));
		} break;

		default:
			return super.startElement(manifestLocation, uri, localName, qName, attributes);
		}

		return Optional.ofNullable(handler);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractModifiableManifest#endNestedHandler(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus2.model.manifest.xml.ManifestXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ManifestXmlHandler handler)
			throws SAXException {
		switch (localName) {
		case ManifestXmlTags.IMPLEMENTATION: {
			getInstance().setImplementationManifest(((ImplementationManifestXmlDelegate) handler).getInstance());
		} break;

		default:
			super.endNestedHandler(manifestLocation, uri, localName, qName, handler);
		}
	}
}
