/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.VersionManifest;
import de.ims.icarus2.model.manifest.standard.VersionManifestImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlAttributes;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.xml.UnexpectedTagException;
import de.ims.icarus2.util.xml.UnsupportedNestingException;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
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

		serializer.writeAttribute(ManifestXmlAttributes.ID, getInstance().getId());

		if(getInstance().hasTemplate()) {
			serializer.writeAttribute(ManifestXmlAttributes.TEMPLATE_ID, getInstance().getTemplate().getId());
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
		String id = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.ID);
		if(id!=null) {
			getInstance().setId(id);
		}

		String templateId = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.TEMPLATE_ID);
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

		if(localName.equals(xmlTag())) {
			readAttributes(attributes);
			return this;
		} else if(localName.equals(ManifestXmlTags.VERSION)) {
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

		if(localName.equals(xmlTag())) {
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
		if(qName.equals(ManifestXmlTags.VERSION)) {
			getInstance().setVersionManifest(((VersionManifestXmlDelegate) handler).getInstance());
		} else
			throw new UnsupportedNestingException(qName, xmlTag());
	}
}
