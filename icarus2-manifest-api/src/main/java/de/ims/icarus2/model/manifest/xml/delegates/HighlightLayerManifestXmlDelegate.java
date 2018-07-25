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

import de.ims.icarus2.model.manifest.api.HighlightFlag;
import de.ims.icarus2.model.manifest.api.HighlightLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.standard.HighlightLayerManifestImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlAttributes;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;
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
			serializer.writeAttribute(ManifestXmlAttributes.PRIMARY_LAYER, manifest.getPrimaryLayerManifest().getId());
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
		String primaryLayerId = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.PRIMARY_LAYER);
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
			serializer.startElement(ManifestXmlTags.HIGHLIGHT_FLAG);
			serializer.writeText(flag.getStringValue());
			serializer.endElement(ManifestXmlTags.HIGHLIGHT_FLAG);
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractLayerManifest#startElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		switch (localName) {
		case ManifestXmlTags.HIGHLIGHT_LAYER: {
			readAttributes(attributes);
		} break;

		case ManifestXmlTags.HIGHLIGHT_FLAG: {
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
		switch (localName) {
		case ManifestXmlTags.HIGHLIGHT_LAYER: {
			return null;
		}

		case ManifestXmlTags.HIGHLIGHT_FLAG: {
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
		return ManifestXmlTags.HIGHLIGHT_LAYER;
	}

}
