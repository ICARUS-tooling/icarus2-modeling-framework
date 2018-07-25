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

import de.ims.icarus2.model.manifest.api.Documentation;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
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
	public void writeXml(XmlSerializer serializer) throws Exception {

		ValueManifest manifest = getInstance();
		Object value = manifest.getValue();
		ValueType type = manifest.getValueType();

		if(type==ValueType.UNKNOWN)
			throw new UnsupportedOperationException("Cannot serialize unknown value: "+value); //$NON-NLS-1$
		if(type==ValueType.CUSTOM)
			throw new UnsupportedOperationException("Cannot serialize custom value: "+value); //$NON-NLS-1$

		boolean isSimple = type.isSimpleType();

		serializer.startElement(ManifestXmlTags.VALUE);

		//ATTRIBUTES
		ManifestXmlUtils.writeIdentityAttributes(serializer, manifest);

		if(isSimple) {
			serializer.writeAttribute(ManifestXmlAttributes.CONTENT, type.toChars(value).toString());
		}

		// CONTENT

		Documentation documentation = manifest.getDocumentation();

		if(documentation!=null) {
			DocumentationXmlDelegate delegate = new DocumentationXmlDelegate();
			delegate.setInstance(documentation);
			delegate.writeXml(serializer);
		}

		if(!isSimple) {
			if(documentation!=null) {
				serializer.startElement(ManifestXmlTags.CONTENT);
			}

			serializer.writeTextOrCData(type.toChars(value));

			if(documentation!=null) {
				serializer.endElement(ManifestXmlTags.CONTENT);
			}
		}

		serializer.endElement(ManifestXmlTags.VALUE);
	}

	protected void readAttributes(Attributes attributes, ManifestLocation manifestLocation) {
		ValueManifest manifest = getInstance();

		ManifestXmlUtils.readIdentityAttributes(attributes, manifest);

		String content = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.CONTENT);
		if(content!=null) {
			if(!manifest.getValueType().isSimpleType())
				throw new ManifestException(ManifestErrorCode.MANIFEST_UNSUPPORTED_TYPE,
						"Attribute location not supported by non-simple type: "+manifest.getValueType());
			manifest.setValue(manifest.getValueType().parse(content, manifestLocation.getClassLoader()));
		}
	}

	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (localName) {

		case ManifestXmlTags.NAME:
		case ManifestXmlTags.DESCRIPTION:
		case ManifestXmlTags.ICON:
			break;

		case ManifestXmlTags.VALUE: {
			readAttributes(attributes, manifestLocation);
		} break;

		case ManifestXmlTags.DOCUMENTATION: {
			return new DocumentationXmlDelegate();
		}

		case ManifestXmlTags.CONTENT: {
			if(getInstance().getValue()!=null)
				throw new UnexpectedTagException(qName, true, ManifestXmlTags.VALUE);
			return this;
		}

		default:
			throw new UnexpectedTagException(qName, true, ManifestXmlTags.VALUE);
		}

		return this;
	}

	@Override
	public ManifestXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {

		ValueManifest manifest = getInstance();

		switch (localName) {

		case ManifestXmlTags.NAME: {
			getInstance().setName(text);
			return this;
		}

		case ManifestXmlTags.DESCRIPTION: {
			getInstance().setDescription(text);
			return this;
		}

		case ManifestXmlTags.ICON: {
			getInstance().setIcon(ManifestXmlUtils.iconValue(text, true));
			return this;
		}

		case ManifestXmlTags.VALUE: {
			if(manifest.getDocumentation()==null && text!=null && !text.isEmpty()) {
				manifest.setValue(manifest.getValueType().parse(text, manifestLocation.getClassLoader()));
			}

			return null;
		}

		case ManifestXmlTags.CONTENT: {
			if(manifest.getValue()!=null)
				throw new UnexpectedTagException(qName, false, ManifestXmlTags.VALUE);

			manifest.setValue(manifest.getValueType().parse(text, manifestLocation.getClassLoader()));
			return this;
		}

		default:
			throw new UnexpectedTagException(qName, false, ManifestXmlTags.VALUE);
		}
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
