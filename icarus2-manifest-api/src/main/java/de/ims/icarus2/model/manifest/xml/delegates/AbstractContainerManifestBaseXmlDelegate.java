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

import de.ims.icarus2.model.manifest.api.ContainerFlag;
import de.ims.icarus2.model.manifest.api.ContainerManifestBase;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.xml.ManifestXmlAttributes;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractContainerManifestBaseXmlDelegate<M extends ContainerManifestBase<M>>
		extends AbstractMemberManifestXmlDelegate<M> {

	@Override
	protected void writeAttributes(XmlSerializer serializer) throws XMLStreamException {
		super.writeAttributes(serializer);

		ContainerManifestBase<?> manifest = getInstance();

		// Write container type
		if(manifest.isLocalContainerType()) {
			serializer.writeAttribute(ManifestXmlAttributes.CONTAINER_TYPE,
					manifest.getContainerType().getStringValue());
		}
	}

	@Override
	protected void writeElements(XmlSerializer serializer) throws XMLStreamException {
		super.writeElements(serializer);

		for(ContainerFlag flag : getInstance().getActiveLocalContainerFlags()) {
			serializer.startElement(ManifestXmlTags.CONTAINER_FLAG);
			serializer.writeText(flag.getStringValue());
			serializer.endElement(ManifestXmlTags.CONTAINER_FLAG);
		}
	}

	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		// Read container type
		ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.CONTAINER_TYPE)
			.map(ContainerType::parseContainerType)
			.ifPresent(getInstance()::setContainerType);
	}

	@Override
	public Optional<ManifestXmlHandler> startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (localName) {
		case ManifestXmlTags.CONTAINER: {
			readAttributes(attributes);
		} break;

		case ManifestXmlTags.CONTAINER_FLAG: {
			// no-op
		} break;

		default:
			return super.startElement(manifestLocation, uri, localName, qName, attributes);
		}

		return Optional.of(this);
	}

	@Override
	public Optional<ManifestXmlHandler> endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {
		ManifestXmlHandler handler = null;

		switch (localName) {
		case ManifestXmlTags.CONTAINER: {
			// no-op;
		} break;

		case ManifestXmlTags.CONTAINER_FLAG: {
			getInstance().setContainerFlag(ContainerFlag.parseContainerFlag(text), true);
			handler = this;
		} break;

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}

		return Optional.ofNullable(handler);
	}
}
