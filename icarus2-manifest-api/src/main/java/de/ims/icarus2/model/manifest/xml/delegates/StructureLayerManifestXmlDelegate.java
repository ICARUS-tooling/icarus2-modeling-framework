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

import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerManifestBase;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.standard.StructureLayerManifestImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 *
 */
public class StructureLayerManifestXmlDelegate extends AbstractItemLayerManifestBaseXmlDelegate<StructureLayerManifest> {

	private StructureManifestXmlDelegate structureManifestXmlDelegate;

	public StructureLayerManifestXmlDelegate() {
		// no-op
	}

	public StructureLayerManifestXmlDelegate(StructureLayerManifest manifest) {
		setInstance(manifest);
	}

	public StructureLayerManifestXmlDelegate(LayerGroupManifest groupManifest) {
		setInstance(new StructureLayerManifestImpl(groupManifest));
	}

	private StructureManifestXmlDelegate getStructureManifestXmlDelegate() {
		if(structureManifestXmlDelegate==null) {
			structureManifestXmlDelegate = new StructureManifestXmlDelegate();
		}

		return structureManifestXmlDelegate;
	}

	public StructureLayerManifestXmlDelegate reset(LayerGroupManifest groupManifest) {
		reset();
		setInstance(new StructureLayerManifestImpl(groupManifest));

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.delegates.AbstractXmlDelegate#reset()
	 */
	@Override
	public void reset() {
		super.reset();

		if(structureManifestXmlDelegate!=null) {
			structureManifestXmlDelegate.reset();
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.delegates.ItemLayerManifestXmlDelegate#writeContainerElement(de.ims.icarus2.util.xml.XmlSerializer, de.ims.icarus2.model.manifest.api.ContainerManifest)
	 */
	@Override
	protected void writeContainerElement(XmlSerializer serializer, ContainerManifestBase<?> containerManifest)
			throws XMLStreamException {
		if(containerManifest.getManifestType()==ManifestType.STRUCTURE_MANIFEST) {
			getStructureManifestXmlDelegate().reset((StructureManifest)containerManifest).writeXml(serializer);
		} else if(containerManifest.getManifestType()==ManifestType.CONTAINER_MANIFEST) {
			getContainerManifestXmlDelegate().reset((ContainerManifest)containerManifest).writeXml(serializer);
		} else
			throw new XMLStreamException("Unsupported container class: "+containerManifest.getClass());
	}

	@Override
	public Optional<ManifestXmlHandler> startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		ManifestXmlHandler handler = this;

		switch (localName) {
		case ManifestXmlTags.STRUCTURE_LAYER: {
			readAttributes(attributes);
		} break;

		case ManifestXmlTags.CONTAINER: {
			handler = getContainerManifestXmlDelegate().reset(getInstance());
		} break;

		case ManifestXmlTags.STRUCTURE: {
			handler = getStructureManifestXmlDelegate().reset(getInstance());
		} break;

		default:
			return super.startElement(manifestLocation, uri, localName, qName, attributes);
		}

		return Optional.ofNullable(handler);
	}

	@Override
	public Optional<ManifestXmlHandler> endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {
		ManifestXmlHandler handler = this;

		switch (localName) {
		case ManifestXmlTags.STRUCTURE_LAYER: {
			handler = null;
		} break;

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
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

		case ManifestXmlTags.STRUCTURE: {
			AbstractItemLayerManifestBaseXmlDelegate.defaultAddContainerManifest(this, (
					(StructureManifestXmlDelegate)handler).getInstance());
		} break;

		default:
			super.endNestedHandler(manifestLocation, uri, localName, qName, handler);
			break;
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return ManifestXmlTags.STRUCTURE_LAYER;
	}
}
