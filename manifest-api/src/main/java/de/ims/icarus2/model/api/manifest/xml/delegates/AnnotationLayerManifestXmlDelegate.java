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

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.api.manifest.AnnotationFlag;
import de.ims.icarus2.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus2.model.api.manifest.AnnotationManifest;
import de.ims.icarus2.model.api.manifest.LayerGroupManifest;
import de.ims.icarus2.model.api.manifest.ManifestLocation;
import de.ims.icarus2.model.api.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.api.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.model.standard.manifest.AnnotationLayerManifestImpl;
import de.ims.icarus2.model.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AnnotationLayerManifestXmlDelegate extends AbstractLayerManifestXmlDelegate<AnnotationLayerManifest> {

	private AnnotationManifestXmlDelegate annotationManifestXmlDelegate;

	public AnnotationLayerManifestXmlDelegate() {
		// no-op
	}

	public AnnotationLayerManifestXmlDelegate(AnnotationLayerManifest manifest) {
		setInstance(manifest);
	}

	public AnnotationLayerManifestXmlDelegate(LayerGroupManifest groupManifest) {
		setInstance(new AnnotationLayerManifestImpl(groupManifest));
	}

	private AnnotationManifestXmlDelegate getAnnotationManifestXmlDelegate() {
		if(annotationManifestXmlDelegate==null) {
			annotationManifestXmlDelegate = new AnnotationManifestXmlDelegate();
		}

		return annotationManifestXmlDelegate;
	}

	public AnnotationLayerManifestXmlDelegate reset(LayerGroupManifest groupManifest) {
		reset();
		setInstance(new AnnotationLayerManifestImpl(groupManifest));

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.api.manifest.xml.delegates.AbstractXmlDelegate#reset()
	 */
	@Override
	public void reset() {
		super.reset();

		if(annotationManifestXmlDelegate!=null) {
			annotationManifestXmlDelegate.reset();
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractLayerManifest#writeAttributes(de.ims.icarus2.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		AnnotationLayerManifest manifest = getInstance();

		// Write default key
		if(manifest.isLocalDefaultKey()) {
			serializer.writeAttribute(ATTR_DEFAULT_KEY, manifest.getDefaultKey());
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractLayerManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		AnnotationLayerManifest manifest = getInstance();

		// Read default key
		String defaultKey = ManifestXmlUtils.normalize(attributes, ATTR_DEFAULT_KEY);
		if(defaultKey!=null) {
			manifest.setDefaultKey(defaultKey);
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractLayerManifest#writeElements(de.ims.icarus2.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		AnnotationLayerManifest manifest = getInstance();

		// Write annotation manifests
		List<AnnotationManifest> sortedAnnotationManifests = new ArrayList<>(manifest.getLocalAnnotationManifests());
		sortedAnnotationManifests.sort((a1, a2) -> a1.getId().compareTo(a2.getId()));

		for(AnnotationManifest annotationManifest : sortedAnnotationManifests) {
			getAnnotationManifestXmlDelegate().reset(annotationManifest).writeXml(serializer);
		}

		for(AnnotationFlag flag : manifest.getLocalActiveAnnotationFlags()) {
			serializer.startElement(TAG_ANNOTATION_FLAG);
			serializer.writeText(flag.getStringValue());
			serializer.endElement(TAG_ANNOTATION_FLAG);
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractLayerManifest#startElement(de.ims.icarus2.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		switch (qName) {
		case TAG_ANNOTATION_LAYER: {
			readAttributes(attributes);
		} break;

		case TAG_ANNOTATION: {
			return getAnnotationManifestXmlDelegate().reset(getInstance());
		}

		case TAG_ANNOTATION_FLAG: {
			// no-op;
		} break;

		default:
			return super.startElement(manifestLocation, uri, localName, qName, attributes);
		}

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractLayerManifest#endElement(de.ims.icarus2.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ManifestXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
			throws SAXException {
		switch (qName) {
		case TAG_ANNOTATION_LAYER: {
			return null;
		}

		case TAG_ANNOTATION_FLAG: {
			getInstance().setAnnotationFlag(AnnotationFlag.parseAnnotationFlag(text), true);
			return this;
		}

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractModifiableManifest#endNestedHandler(de.ims.icarus2.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus2.model.api.manifest.xml.ManifestXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ManifestXmlHandler handler)
			throws SAXException {
		switch (qName) {
		case TAG_ANNOTATION: {
			getInstance().addAnnotationManifest(((AnnotationManifestXmlDelegate) handler).getInstance());
		} break;

		default:
			super.endNestedHandler(manifestLocation, uri, localName, qName, handler);
			break;
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractManifest#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return TAG_ANNOTATION_LAYER;
	}
}
