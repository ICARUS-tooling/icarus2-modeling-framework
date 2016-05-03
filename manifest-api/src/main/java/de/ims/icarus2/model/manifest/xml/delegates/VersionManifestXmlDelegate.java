/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus2.model.manifest.xml.delegates;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.VersionManifest;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.model.xml.UnexpectedTagException;
import de.ims.icarus2.model.xml.UnsupportedNestingException;
import de.ims.icarus2.model.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class VersionManifestXmlDelegate extends AbstractXmlDelegate<VersionManifest> {

	public VersionManifestXmlDelegate() {
		//no-op
	}

	public VersionManifestXmlDelegate(VersionManifest manifest) {
		setInstance(manifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#startElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		if(qName.equals(TAG_VERSION)) {
			String formatId = ManifestXmlUtils.normalize(attributes, ATTR_VERSION_FORMAT);
			if(formatId!=null) {
				getInstance().setFormatId(formatId);
			}

			return this;
		} else
			throw new UnexpectedTagException(qName, true, TAG_VERSION);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ManifestXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
			throws SAXException {
		if(qName.equals(TAG_VERSION)) {
			getInstance().setVersionString(text);

			return null;
		} else
			throw new UnexpectedTagException(qName, false, TAG_VERSION);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endNestedHandler(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus2.model.manifest.xml.ManifestXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ManifestXmlHandler handler)
			throws SAXException {
		throw new UnsupportedNestingException(qName, TAG_VERSION);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlElement#writeXml(de.ims.icarus2.model.xml.XmlSerializer)
	 */
	@Override
	public void writeXml(XmlSerializer serializer) throws Exception {
		if(getInstance().getVersionString()==null)
			throw new IllegalArgumentException("Invalid version string in manifest"); //$NON-NLS-1$

		serializer.startElement(TAG_VERSION);

		// ATTRIBUTES
		String formatId = getInstance().getFormatId();
		if(!VersionManifest.DEFAULT_VERSION_FORMAT_ID.equals(formatId)) {
			serializer.writeAttribute(ATTR_VERSION_FORMAT, formatId);
		}

		// CONTENT
		serializer.writeCData(getInstance().getVersionString());

		serializer.endElement(TAG_VERSION);
	}
}
