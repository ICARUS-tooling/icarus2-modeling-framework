/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

	protected void writeElements(XmlSerializer serializer) throws XMLStreamException {
		Optional<VersionManifest> versionManifest = getInstance().getVersionManifest();
		if(versionManifest.isPresent()) {
			new VersionManifestXmlDelegate(versionManifest.get()).writeXml(serializer);
		}
	}

	protected void writeAttributes(XmlSerializer serializer) throws XMLStreamException {
//		if(id==null && isTemplate())
//			throw new ModelException(ModelError.MANIFEST_INVALID_ID, "Id of "+createDummyId()+" is null");

		serializer.writeAttribute(ManifestXmlAttributes.ID, getInstance().getId());

		if(getInstance().hasTemplate()) {
			serializer.writeAttribute(ManifestXmlAttributes.TEMPLATE_ID, getInstance().getTemplate().getId());
		}
	}

	protected boolean isEmpty(M instance) {
		return instance.isEmpty();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlElement#writeXml(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	public void writeXml(XmlSerializer serializer) throws XMLStreamException {
		if(isEmpty(getInstance())) {
			serializer.startEmptyElement(xmlTag());
		} else {
			serializer.startElement(xmlTag());
		}
		writeAttributes(serializer);
		writeElements(serializer);
		serializer.endElement(xmlTag());
	}


	protected void readAttributes(Attributes attributes) {
		ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.ID)
			.ifPresent(getInstance()::setId);
		ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.TEMPLATE_ID)
			.ifPresent(getInstance()::setTemplateId);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#startElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public Optional<ManifestXmlHandler> startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {

		if(localName.equals(xmlTag())) {
			readAttributes(attributes);
			return Optional.of(this);
		} else if(localName.equals(ManifestXmlTags.VERSION)) {
			return Optional.of(new VersionManifestXmlDelegate(new VersionManifestImpl()));
		} else
			throw new UnexpectedTagException(qName, true, xmlTag());
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Optional<ManifestXmlHandler> endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
			throws SAXException {

		if(localName.equals(xmlTag())) {
			return Optional.empty();
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
