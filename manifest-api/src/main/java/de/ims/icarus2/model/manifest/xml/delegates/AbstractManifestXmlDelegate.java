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

import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.VersionManifest;
import de.ims.icarus2.model.manifest.standard.VersionManifestImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.xml.UnexpectedTagException;
import de.ims.icarus2.util.xml.UnsupportedNestingException;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractManifestXmlDelegate<M extends Manifest> extends AbstractXmlDelegate<M> {


	protected abstract String xmlTag();

	protected void writeElements(XmlSerializer serializer) throws Exception {
		VersionManifest versionManifest = getInstance().getVersionManifest();
		if(versionManifest!=null) {
			new VersionManifestXmlDelegate(versionManifest).writeXml(serializer);
		}
	}

	protected void writeAttributes(XmlSerializer serializer) throws Exception {
//		if(id==null && isTemplate())
//			throw new ModelException(ModelError.MANIFEST_INVALID_ID, "Id of "+createDummyId()+" is null");

		serializer.writeAttribute(ATTR_ID, getInstance().getId());

		if(getInstance().hasTemplate()) {
			serializer.writeAttribute(ATTR_TEMPLATE_ID, getInstance().getTemplate().getId());
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlElement#writeXml(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	public void writeXml(XmlSerializer serializer) throws Exception {
		if(getInstance().isEmpty()) {
			serializer.startEmptyElement(xmlTag());
		} else {
			serializer.startElement(xmlTag());
		}
		writeAttributes(serializer);
		writeElements(serializer);
		serializer.endElement(xmlTag());
	}


	protected void readAttributes(Attributes attributes) {
		String id = ManifestXmlUtils.normalize(attributes, ATTR_ID);
		if(id!=null) {
			getInstance().setId(id);
		}

		String templateId = ManifestXmlUtils.normalize(attributes, ATTR_TEMPLATE_ID);
		if(templateId!=null) {
			getInstance().setTemplateId(templateId);
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#startElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {

		if(qName.equals(xmlTag())) {
			readAttributes(attributes);
			return this;
		} else if(qName.equals(TAG_VERSION)) {
			return new VersionManifestXmlDelegate(new VersionManifestImpl());
		} else
			throw new UnexpectedTagException(qName, true, xmlTag());
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ManifestXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
			throws SAXException {

		if(qName.equals(xmlTag())) {
			return null;
		} else
			throw new UnexpectedTagException(qName, false, xmlTag());
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endNestedHandler(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus2.model.manifest.xml.ManifestXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ManifestXmlHandler handler)
			throws SAXException {
		if(qName.equals(TAG_VERSION)) {
			getInstance().setVersionManifest(((VersionManifestXmlDelegate) handler).getInstance());
		} else
			throw new UnsupportedNestingException(qName, xmlTag());
	}
}
