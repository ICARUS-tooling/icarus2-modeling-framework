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

import de.ims.icarus2.model.manifest.api.ContainerFlag;
import de.ims.icarus2.model.manifest.api.ContainerType;
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
public class StructureManifestXmlDelegate extends AbstractMemberManifestXmlDelegate<StructureManifest> {

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
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		StructureManifest manifest = getInstance();

		// Write container type
		if(manifest.isLocalContainerType()) {
			serializer.writeAttribute(ManifestXmlAttributes.CONTAINER_TYPE, manifest.getContainerType().getStringValue());
		}

		// Write structure type
		if(manifest.isLocalStructureType()) {
			serializer.writeAttribute(ManifestXmlAttributes.STRUCTURE_TYPE, manifest.getStructureType().getStringValue());
		}
	}

	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		StructureManifest manifest = getInstance();

		for(StructureFlag flag : manifest.getActiveLocalStructureFlags()) {
			serializer.startElement(ManifestXmlTags.STRUCTURE_FLAG);
			serializer.writeText(flag.getStringValue());
			serializer.endElement(ManifestXmlTags.STRUCTURE_FLAG);
		}

		for(ContainerFlag flag : getInstance().getActiveLocalContainerFlags()) {
			serializer.startElement(ManifestXmlTags.CONTAINER_FLAG);
			serializer.writeText(flag.getStringValue());
			serializer.endElement(ManifestXmlTags.CONTAINER_FLAG);
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		// Read structure type
		String structureType = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.STRUCTURE_TYPE);
		if(structureType!=null) {
			getInstance().setStructureType(StructureType.parseStructureType(structureType));
		}

		// Read container type
		String containerType = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.CONTAINER_TYPE);
		if(containerType!=null) {
			getInstance().setContainerType(ContainerType.parseContainerType(containerType));
		}
	}

	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (localName) {
		case ManifestXmlTags.STRUCTURE: {
			readAttributes(attributes);
		} break;

		case ManifestXmlTags.STRUCTURE_FLAG: {
			// no-op
		} break;

		case ManifestXmlTags.CONTAINER_FLAG: {
			// no-op
		} break;

		default:
			return super.startElement(manifestLocation, uri, localName, qName, attributes);
		}

		return this;
	}

	@Override
	public ManifestXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {
		switch (localName) {
		case ManifestXmlTags.STRUCTURE: {
			return null;
		}

		case ManifestXmlTags.STRUCTURE_FLAG: {
			getInstance().setStructureFlag(StructureFlag.parseStructureFlag(text), true);
			return this;
		}

		case ManifestXmlTags.CONTAINER_FLAG: {
			getInstance().setContainerFlag(ContainerFlag.parseContainerFlag(text), true);
			return this;
		}

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}
	}

	@Override
	protected String xmlTag() {
		return ManifestXmlTags.STRUCTURE;
	}
}
