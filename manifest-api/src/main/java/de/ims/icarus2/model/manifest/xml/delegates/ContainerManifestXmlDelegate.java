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
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.standard.ContainerManifestImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 *
 */
public class ContainerManifestXmlDelegate extends AbstractMemberManifestXmlDelegate<ContainerManifest> {

	public ContainerManifestXmlDelegate() {
		// no-op
	}

	public ContainerManifestXmlDelegate(ContainerManifest containerManifest) {
		setInstance(containerManifest);
	}

	public ContainerManifestXmlDelegate(ItemLayerManifest itemLayerManifest) {
		setInstance(new ContainerManifestImpl(itemLayerManifest));
	}

	public ContainerManifestXmlDelegate reset(ItemLayerManifest itemLayerManifest) {
		reset();
		setInstance(new ContainerManifestImpl(itemLayerManifest));

		return this;
	}

	@Override
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		ContainerManifest manifest = getInstance();

		// Write container type
		if(manifest.isLocalContainerType()) {
			serializer.writeAttribute(ATTR_CONTAINER_TYPE, manifest.getContainerType().getStringValue());
		}
	}

	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		for(ContainerFlag flag : getInstance().getActiveLocalContainerFlags()) {
			serializer.startElement(TAG_CONTAINER_FLAG);
			serializer.writeText(flag.getStringValue());
			serializer.endElement(TAG_CONTAINER_FLAG);
		}
	}

	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		// Read container type
		String containerType = ManifestXmlUtils.normalize(attributes, ATTR_CONTAINER_TYPE);
		if(containerType!=null) {
			getInstance().setContainerType(ContainerType.parseContainerType(containerType));
		}
	}

	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_CONTAINER: {
			readAttributes(attributes);
		} break;

		case TAG_CONTAINER_FLAG: {
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
		switch (qName) {
		case TAG_CONTAINER: {
			return null;
		}

		case TAG_CONTAINER_FLAG: {
			getInstance().setContainerFlag(ContainerFlag.parseContainerFlag(text), true);
			return this;
		}

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}
	}

	@Override
	protected String xmlTag() {
		return TAG_CONTAINER;
	}
}
