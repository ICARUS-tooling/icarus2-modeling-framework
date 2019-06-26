/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.StructureFlag;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.manifest.standard.StructureManifestImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlAttributes;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 *
 */
public class StructureManifestXmlDelegate extends AbstractContainerManifestBaseXmlDelegate<StructureManifest> {

	public StructureManifestXmlDelegate() {
		// no-op
	}

	public StructureManifestXmlDelegate(StructureManifest structureManifest) {
		setInstance(structureManifest);
	}

	public StructureManifestXmlDelegate(StructureLayerManifest structureLayerManifest) {
		setInstance(new StructureManifestImpl(structureLayerManifest));
	}

	public StructureManifestXmlDelegate reset(StructureLayerManifest structureLayerManifest) {
		reset();
		setInstance(new StructureManifestImpl(structureLayerManifest));

		return this;
	}

	@Override
	protected void writeAttributes(XmlSerializer serializer) throws XMLStreamException {
		super.writeAttributes(serializer);

		StructureManifest manifest = getInstance();

		// Write structure type
		if(manifest.isLocalStructureType()) {
			serializer.writeAttribute(ManifestXmlAttributes.STRUCTURE_TYPE, manifest.getStructureType().getStringValue());
		}
	}

	@Override
	protected void writeElements(XmlSerializer serializer) throws XMLStreamException {
		super.writeElements(serializer);

		StructureManifest manifest = getInstance();

		for(StructureFlag flag : manifest.getActiveLocalStructureFlags()) {
			serializer.startElement(ManifestXmlTags.STRUCTURE_FLAG);
			serializer.writeText(flag.getStringValue());
			serializer.endElement(ManifestXmlTags.STRUCTURE_FLAG);
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		// Read structure type
		ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.STRUCTURE_TYPE)
			.map(StructureType::parseStructureType)
			.ifPresent(getInstance()::setStructureType);
	}

	@Override
	public Optional<ManifestXmlHandler> startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (localName) {
		case ManifestXmlTags.STRUCTURE: {
			readAttributes(attributes);
		} break;

		case ManifestXmlTags.STRUCTURE_FLAG: {
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
		ManifestXmlHandler handler = this;

		switch (localName) {
		case ManifestXmlTags.STRUCTURE: {
			handler = null;
		} break;

		case ManifestXmlTags.STRUCTURE_FLAG: {
			getInstance().setStructureFlag(StructureFlag.parseStructureFlag(text), true);
		} break;

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}

		return Optional.ofNullable(handler);
	}

	@Override
	protected String xmlTag() {
		return ManifestXmlTags.STRUCTURE;
	}
}
