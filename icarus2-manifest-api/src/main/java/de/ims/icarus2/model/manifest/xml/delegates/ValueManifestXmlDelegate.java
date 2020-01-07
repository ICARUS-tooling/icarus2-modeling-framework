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
package de.ims.icarus2.model.manifest.xml.delegates;

import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ValueManifest;
import de.ims.icarus2.model.manifest.standard.ValueManifestImpl;
import de.ims.icarus2.model.manifest.types.ValueType;
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
public class ValueManifestXmlDelegate extends AbstractXmlDelegate<ValueManifest> {

	public ValueManifestXmlDelegate() {
		//no-op
	}

	public ValueManifestXmlDelegate(ValueManifest manifest) {
		setInstance(manifest);
	}

	public ValueManifestXmlDelegate(ValueType valueType) {
		setInstance(new ValueManifestImpl(valueType));
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlElement#writeXml(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	public void writeXml(XmlSerializer serializer) throws XMLStreamException {

		ValueManifest manifest = getInstance();
		Object value = manifest.getValue().orElseThrow(ManifestException.error("Missing value in ValueManifest"));
		ValueType type = manifest.getValueType();

		if(type==ValueType.UNKNOWN)
			throw new UnsupportedOperationException("Cannot serialize unknown value: "+value); //$NON-NLS-1$
		if(type==ValueType.CUSTOM)
			throw new UnsupportedOperationException("Cannot serialize custom value: "+value); //$NON-NLS-1$

		serializer.startElement(ManifestXmlTags.VALUE);

		//ATTRIBUTES
		ManifestXmlUtils.writeIdentityAttributes(serializer, manifest);

		// CONTENT

		serializeElement(manifest.getDocumentation(), DocumentationXmlDelegate::new, serializer);

		ManifestXmlUtils.writeIdentityFieldElements(serializer, manifest);

		serializer.startElement(ManifestXmlTags.CONTENT);
		ManifestXmlUtils.writeValue(serializer, value, type);
		serializer.endElement(ManifestXmlTags.CONTENT);

		serializer.endElement(ManifestXmlTags.VALUE);
	}

	protected void readAttributes(Attributes attributes, ManifestLocation manifestLocation) throws SAXException {
		ValueManifest manifest = getInstance();

		ManifestXmlUtils.readIdentityAttributes(attributes, manifest);

		Optional<String> content = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.CONTENT);
		if(content.isPresent()) {
			if(!manifest.getValueType().isSerializable())
				throw new ManifestException(ManifestErrorCode.MANIFEST_UNSUPPORTED_TYPE,
						"Attribute location not supported by non-simple type: "+manifest.getValueType());
			manifest.setValue(ManifestXmlUtils.parse(manifest.getValueType(), manifestLocation, content.get(), true));
		}
	}

	@Override
	public Optional<ManifestXmlHandler> startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		ManifestXmlHandler handler = this;

		switch (localName) {

		case ManifestXmlTags.NAME:
		case ManifestXmlTags.DESCRIPTION: {
			// no-op
		} break;

		case ManifestXmlTags.VALUE: {
			readAttributes(attributes, manifestLocation);
		} break;

		case ManifestXmlTags.DOCUMENTATION: {
			handler = new DocumentationXmlDelegate(getInstance());
		} break;

		case ManifestXmlTags.CONTENT: {
			if(getInstance().getValue().isPresent())
				throw new UnexpectedTagException(qName, true, ManifestXmlTags.VALUE);
		} break;

		default:
			throw new UnexpectedTagException(qName, true, ManifestXmlTags.VALUE);
		}

		return Optional.ofNullable(handler);
	}

	@Override
	public Optional<ManifestXmlHandler> endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {
		ManifestXmlHandler handler = this;

		ValueManifest manifest = getInstance();

		switch (localName) {

		case ManifestXmlTags.NAME: {
			getInstance().setName(text);
		} break;

		case ManifestXmlTags.DESCRIPTION: {
			getInstance().setDescription(text);
		} break;

		case ManifestXmlTags.VALUE: {
			if(!manifest.getDocumentation().isPresent() && text!=null && !text.isEmpty()) {
				manifest.setValue(ManifestXmlUtils.parse(manifest.getValueType(), manifestLocation, text, true));
			}

			handler = null;
		} break;

		case ManifestXmlTags.CONTENT: {
			if(manifest.getValue().isPresent())
				throw new UnexpectedTagException(qName, false, ManifestXmlTags.VALUE);

			manifest.setValue(ManifestXmlUtils.parse(manifest.getValueType(), manifestLocation, text, true));
		} break;

		default:
			throw new UnexpectedTagException(qName, false, ManifestXmlTags.VALUE);
		}

		return Optional.ofNullable(handler);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endNestedHandler(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus2.model.manifest.xml.ManifestXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ManifestXmlHandler handler)
			throws SAXException {
		switch (localName) {

		case ManifestXmlTags.DOCUMENTATION: {
			getInstance().setDocumentation(((DocumentationXmlDelegate) handler).getInstance());
		} break;

		default:
			throw new UnsupportedNestingException(qName, ManifestXmlTags.VALUE);
		}
	}

}
