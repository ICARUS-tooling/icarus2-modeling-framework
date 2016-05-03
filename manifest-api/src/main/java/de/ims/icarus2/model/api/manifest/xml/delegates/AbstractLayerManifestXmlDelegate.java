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
package de.ims.icarus2.model.api.manifest.xml.delegates;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.api.manifest.LayerManifest;
import de.ims.icarus2.model.api.manifest.LayerManifest.TargetLayerManifest;
import de.ims.icarus2.model.api.manifest.ManifestLocation;
import de.ims.icarus2.model.api.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.api.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.model.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractLayerManifestXmlDelegate<L extends LayerManifest> extends AbstractMemberManifestXmlDelegate<L> {


	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractMemberManifest#writeAttributes(de.ims.icarus2.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		LayerManifest manifest = getInstance();

		// Write layer type
		if(manifest.isLocalLayerType()) {
			serializer.writeAttribute(ATTR_LAYER_TYPE, manifest.getLayerType().getId());
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractModifiableManifest#writeElements(de.ims.icarus2.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		// Write base layers
		for(TargetLayerManifest layerManifest : getInstance().getLocalBaseLayerManifests()) {
			ManifestXmlUtils.writeTargetLayerManifestElement(serializer, TAG_BASE_LAYER, layerManifest);
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		String layerTypeId = ManifestXmlUtils.normalize(attributes, ATTR_LAYER_TYPE);
		if(layerTypeId!=null) {
			getInstance().setLayerTypeId(layerTypeId);
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractManifest#startElement(de.ims.icarus2.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		switch (qName) {
		case TAG_BASE_LAYER: {
			String baseLayerId = ManifestXmlUtils.normalize(attributes, ATTR_LAYER_ID);
			getInstance().addBaseLayerId(baseLayerId);
		} break;

		default:
			return super.startElement(manifestLocation, uri, localName, qName, attributes);
		}

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractManifest#endElement(de.ims.icarus2.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ManifestXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
			throws SAXException {
		switch (qName) {
		case TAG_BASE_LAYER: {
			// no-op
		} break;

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}

		return this;
	}
}
