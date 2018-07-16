/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 */
package de.ims.icarus2.model.manifest.xml.delegates;

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
public abstract class AbstractForeignImplementationManifestXmlDelegate<M extends ForeignImplementationManifest>
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
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		ForeignImplementationManifest manifest = getInstance();

		if(manifest.isLocalImplementation()) {
			getImplementationManifestXmlDelegate().reset(manifest.getImplementationManifest()).writeXml(serializer);
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractModifiableManifest#startElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		switch (localName) {
		case ManifestXmlTags.IMPLEMENTATION: {
			return getImplementationManifestXmlDelegate().reset(new ImplementationManifestImpl(getInstance()));
		}

		default:
			return super.startElement(manifestLocation, uri, localName, qName, attributes);
		}
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
