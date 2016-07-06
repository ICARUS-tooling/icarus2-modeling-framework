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

import de.ims.icarus2.model.manifest.api.HighlightFlag;
import de.ims.icarus2.model.manifest.api.HighlightLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.standard.HighlightLayerManifestImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 *
 */
public class HighlightLayerManifestXmlDelegate extends AbstractLayerManifestXmlDelegate<HighlightLayerManifest> {

	public HighlightLayerManifestXmlDelegate() {
		// no-op
	}

	public HighlightLayerManifestXmlDelegate(HighlightLayerManifest manifest) {
		setInstance(manifest);
	}

	public HighlightLayerManifestXmlDelegate(LayerGroupManifest groupManifest) {
		setInstance(new HighlightLayerManifestImpl(groupManifest));
	}

	public HighlightLayerManifestXmlDelegate reset(LayerGroupManifest groupManifest) {
		reset();
		setInstance(new HighlightLayerManifestImpl(groupManifest));

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractLayerManifest#writeAttributes(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		HighlightLayerManifest manifest = getInstance();

		// Write default key
		if(manifest.isLocalPrimaryLayerManifest()) {
			serializer.writeAttribute(ATTR_PRIMARY_LAYER, manifest.getPrimaryLayerManifest().getId());
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractLayerManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		HighlightLayerManifest manifest = getInstance();

		// Read primary layer id
		String primaryLayerId = ManifestXmlUtils.normalize(attributes, ATTR_PRIMARY_LAYER);
		if(primaryLayerId!=null) {
			manifest.setPrimaryLayerId(primaryLayerId);
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractLayerManifest#writeElements(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		HighlightLayerManifest manifest = getInstance();

		for(HighlightFlag flag : manifest.getActiveLocalHighlightFlags()) {
			serializer.startElement(TAG_HIGHLIGHT_FLAG);
			serializer.writeText(flag.getStringValue());
			serializer.endElement(TAG_HIGHLIGHT_FLAG);
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractLayerManifest#startElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		switch (qName) {
		case TAG_HIGHLIGHT_LAYER: {
			readAttributes(attributes);
		} break;

		case TAG_HIGHLIGHT_FLAG: {
			// no-op;
		} break;

		default:
			return super.startElement(manifestLocation, uri, localName, qName, attributes);
		}

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractLayerManifest#endElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ManifestXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
			throws SAXException {
		switch (qName) {
		case TAG_HIGHLIGHT_LAYER: {
			return null;
		}

		case TAG_HIGHLIGHT_FLAG: {
			getInstance().setHighlightFlag(HighlightFlag.parseHighlightFlag(text), true);
			return this;
		}

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return TAG_HIGHLIGHT_LAYER;
	}

}
